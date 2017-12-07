package src.LogAppend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;
import share.DB;

public class Main
{
  private static int timestamp;
  private static int room = -1;
  private static String token;
  private static String name;
  private static String type; // employee | guest
  private static String event; // arrival | departure
  private static String log;
  private static String batchFile;
  private static boolean BATCH = false;
  private static ArrayList<String> opts = new ArrayList<String>();

  private static final int ERRCODE = 255;
  private static final int PASS = 0;
  private static final int FAIL = -1;
  private static final boolean debug = false; // use debug err()

  private static void err(String s, int i)
  {
    System.out.println(s);
    System.exit(i);
  }

  private static void err()
  {
    System.out.println("invalid");
    System.exit(ERRCODE);
  }

  private static void reset()
  {
    token = null;
    name = null;
    type = null;
    event = null;
    log = null;
    room = -1;

    opts = new ArrayList<String>();
  }

  private static int optA(String s)
  {
    if (opts.contains("-L"))
    {
      if (debug)
      {
        err("invalid : LogAppend : optA : invalid option '-A' with '-L'", ERRCODE);
      }

      return -1;
    }
    else
    {
      opts.add(s);
      event = "enter";

      return 0;
    }
  }

  private static void optB(String s)
  {
    if (BATCH)
    {
      if (debug)
      {
        err("invalid : LogAppend : optB : '-B' within batch file", ERRCODE);
      }
      else
      {
        System.out.println("batch");
        err();
      }
    }
    else if (opts.isEmpty())
    {
      BATCH = true;
      batchFile = s;
    }
    else
    {
      if (debug)
      {
        err("invalid : LogAppend : optB : other arguments specified with '-B'", ERRCODE);
      }
      else
      {
        err();
      }
    }
  }

  private static void optE(String s)
  {
    type = "employee";
    name = s;
  }

  private static void optG(String s)
  {
    type = "guest";
    name = s;
  }

  private static int optK(String s)
  {
    if (s.matches("[a-zA-Z0-9]+"))
    {
      token = s;

      return 0;
    }
    else
    {
      if (debug)
      {
        err("invalid : LogAppend : optK : invalid characters in token", ERRCODE);
      }

      return -1;
    }
  }

  private static int optL(String s)
  {
    if (opts.contains("-A"))
    {
      if (debug)
      {
        err("invalid : LogAppend : optL : invalid option '-L' with '-A'", ERRCODE);
      }

      return -1;
    }
    else
    {
      opts.add(s);
      event = "leave";

      return 0;
    }
  }

  private static void optR(String s)
  {
    room = Integer.parseInt(s);
  }

  private static void optT(String s)
  {
    int t = Integer.parseInt(s);
    timestamp = t;
  }

  private static int optLog(String s)
  {
    if (s.matches("[a-zA-Z0-9_./]+"))
    {
      log = s;

      return 0;
    }
    else
    {
      if (debug)
      {
        err("invalid : LogAppend : optLog : invalid characters in log", ERRCODE);
      }

      return -1;
    }
  }

  private static int processB()
  {
    // TODO: check for invalid characters? Spec doesn't say.

    try
    {
      File file = new File(batchFile);

      if (file.isFile())
      {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        List<String> lines = new ArrayList<String>();
        String line = null;

        while ((line = br.readLine()) != null)
        {
          lines.add(line);
        }

        br.close();

        String[] cmds = lines.toArray(new String[lines.size()]);

        for (String st : cmds)
        {
          reset();

          String[] args = st.split(" ");

          for (int i = 0; i < args.length; i++)
          {
            String opt = args[i];

            switch (opt)
            {
              case "-A":  optA(opt);
                          break;

              case "-B":  optB(args[++i]);
                          break;

              case "-E":  optE(args[++i]);
                          break;

              case "-G":  optG(args[++i]);
                          break;

              case "-K":  optK(args[++i]);
                          break;

              case "-L":  optL(opt);
                          break;

              case "-R":  optR(args[++i]);
                          break;

              case "-T":  optT(args[++i]);
                          break;

              default:    optLog(opt);
                          break;
            }
          }

          int ret = process();

          if (ret == FAIL)
          {
            if (debug)
            {
              err("invalid : LogAppend : processB : something...", ERRCODE);
            }
            else
            {
              System.out.println("invalid");
            }
          }
        }
      }
      else
      {
        if (debug)
        {
          err("invalid : LogAppend : processB : batch file does not exist", ERRCODE);
        }
        else
        {
          err();
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return PASS;
  }

  private static int process()
  {
    try
    {
      File file = new File(log);

      // hash and reduce key to 128 bits for AES
      byte[] shaKey = token.getBytes("UTF-8");
      MessageDigest sha = MessageDigest.getInstance("SHA-1");
      shaKey = sha.digest(shaKey);
      shaKey = Arrays.copyOf(shaKey, 16); // 128 bit

      Key aesKey = new SecretKeySpec(shaKey, "AES");
      Cipher cipher = Cipher.getInstance("AES");

      DB db;

      if (file.isFile())
      {
        // log exists, add to existing DB
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CipherInputStream cis = new CipherInputStream(bis, cipher);
        ObjectInputStream ois = new ObjectInputStream(cis);

        // read existing DB
        db = (DB) ois.readObject();
        ois.close();
      }
      else
      {
        // first entry to log, create new DB
        db = new DB();
      }

      // add new entry, validated before added
      // added here so that invalidation does not result with blank log
      if (room == -1)
      {
        if (event.equals("enter"))
        {
          event = "arrive";
        }
        else
        {
          event = "depart";
        }
      }

      int ret = db.add(timestamp, room, name, type, event);

      if (ret == FAIL)
      {
        return FAIL;
      }

      cipher.init(Cipher.ENCRYPT_MODE, aesKey);

      // create writing stream
      FileOutputStream fos = new FileOutputStream(file);
      BufferedOutputStream bos = new BufferedOutputStream(fos);
      CipherOutputStream cos = new CipherOutputStream(bos, cipher);
      ObjectOutputStream oos = new ObjectOutputStream(cos);

      // write objects
      oos.writeObject(db);
      oos.flush();
      oos.close();

      return PASS;
    }
    catch (StreamCorruptedException e)
    {
      // invalid authentication token for existing log or log corrupted
      err();
    }
    catch (Exception e)
    {
      // i.e. FileNotFoundException
      err();
    }

    return PASS;
  }

  public static void main(String[] args)
  {
    for (int i = 0; i < args.length; i++)
    {
      String opt = args[i];

      switch (opt)
      {
        case "-A":  optA(opt);
                    break;

        case "-B":  optB(args[++i]);
                    break;

        case "-E":  optE(args[++i]);
                    break;

        case "-G":  optG(args[++i]);
                    break;

        case "-K":  optK(args[++i]);
                    break;

        case "-L":  optL(opt);
                    break;

        case "-R":  optR(args[++i]);
                    break;

        case "-T":  optT(args[++i]);
                    break;

        default:    optLog(opt);
                    break;
      }
    }

    int ret = PASS;

    if (BATCH)
    {
      ret = processB();
    }
    else
    {
      ret = process();
    }

    if (ret == FAIL)
    {
      err();
    }

    System.exit(0);
  }
}
