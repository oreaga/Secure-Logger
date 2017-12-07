package src.LogRead;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.util.*;

import share.DB;
import share.DB.Entry;

public class Main
{
  private static String token;
  private static String log;
  private static String person;
  private static String type;
  private static ArrayList<String> opts = new ArrayList<String>();

  private static final int ERRCODE = 255;
  private static final boolean debug = false;

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

  private static void optK(String s)
  {
    if (s.matches("[a-zA-Z0-9]+"))
    {
      token = s;
    }
    else
    {
      if (debug)
      {
        err("invalid : LogRead : optK : invalid characters in token", ERRCODE);
      }
      else
      {
        err();
      }
    }
  }

  private static void optS()
  {
    if ((opts.contains("-T")) || (opts.contains("-R")))
    {
      if (debug)
      {
        err("invalid : LogRead : optT : invalid combination of arguments with '-S'", ERRCODE);
      }
      else
      {
        err();
      }
    }
    opts.add("-S");
  }

  private static void optR()
  {
    if ((opts.contains("-S")) || (opts.contains("-T")))
    {
      if (debug)
      {
        err("invalid : LogRead : optT : invalid combination of arguments with '-R'", ERRCODE);
      }
      else
      {
        err();
      }
    }
    opts.add("-R");
  }

  private static void optT()
  {
    if ((opts.contains("-S")) || (opts.contains("-R")))
    {
      if (debug)
      {
        err("invalid : LogRead : optT : invalid combination of arguments with '-T'", ERRCODE);
      }
      else
      {
        err();
      }
    }

    opts.add("-T");
  }

  private static void optI()
  {
    // this is the proper way to handle while unimplemented, change later
    System.out.println("umimplemented");
    System.exit(255);
  }

  private static void optE(String s)
  {
    if (opts.contains("-G"))
    {
      if (debug)
      {
        err("invalid : LogRead : optG : specifying guest after employee", ERRCODE);
      }
      else
      {
        err();
      }
    }
    else
    {
      if (s.matches("[a-zA-Z]+"))
      {
        person = s;
        type = "employee";
        opts.add("-E");
      }

      // else won't be in log because of invalid name, do nothing
    }
  }

  private static void optG(String s)
  {
    if (opts.contains("-E"))
    {
      if (debug)
      {
        err("invalid : LogRead : optG : specifying guest after employee", ERRCODE);
      }
      else
      {
        err();
      }
    }
    else
    {
      if (s.matches("[a-zA-Z]+"))
      {
        person = s;
        type = "guest";
        opts.add("-G");
      }
      
      // else won't be in log because of invalid name, do nothing
    }
  }

  private static void optLog(String s)
  {
    if (s.matches("[a-zA-Z0-9_./]+"))
    {
      log = s;
    }
    else
    {
      if (debug)
      {
        err("invalid : LogRead : optLog : invalid characters in log name", ERRCODE);
      }
      else
      {
        err();
      }
    }
  }

  private static void processS(DB db)
  {
    ArrayList<String> employees = new ArrayList<String>();
    ArrayList<String> guests = new ArrayList<String>();
    HashMap<Integer, ArrayList<String>> rooms = new HashMap<Integer, ArrayList<String>>();

    for (Entry e : db.getEntries())
    {
      String name = e.getName();
      String type = e.getType();
      String event = e.getEvent();
      int room = e.getRoom();

      switch (event)
      {
        case "arrive" : if (rooms.get(room) == null)
                        {
                          rooms.put(room, new ArrayList<String>());
                        }
                        rooms.get(room).add(name);
                        switch (type)
                        {
                          case "employee" : employees.add(name);
                                            break;

                          case "guest"    : guests.add(name);
                                            break;
                        }
                        break;

        case "depart" : rooms.get(room).remove(name);
                        switch (type)
                        {
                          case "employee" : employees.remove(name);
                                            break;

                          case "guest"    : guests.remove(name);
                                            break;
                        }
                        break;

        case "enter"  : if (rooms.get(room) == null)
                        {
                          rooms.put(room, new ArrayList<String>());
                        }
                        rooms.get(room).add(name);
                        rooms.get(-1).remove(name);
                        break;

        case "leave"  : rooms.get(room).remove(name);
                        break;
      }
    }

    if (!(employees.isEmpty()))
    {
      String[] arr = employees.toArray(new String[0]);
      Arrays.sort(arr);
      System.out.println(String.join(",", arr));
    }

    if (!(guests.isEmpty()))
    {
      String[] arr = guests.toArray(new String[0]);
      Arrays.sort(arr);
      System.out.println(String.join(",", arr));
    }
    
    SortedSet<Integer> keys = new TreeSet<Integer>(rooms.keySet());

    for (Integer key : keys)
    {
      if (key != -1)
      {
        if (!(rooms.get(key).isEmpty()))
        {
          String[] arr = rooms.get(key).toArray(new String[0]);
          Arrays.sort(arr);
          System.out.println(key + ": " + String.join(",", arr));
        }
      }
    }
  }

  private static void processR(DB db)
  {
    ArrayList<String> rooms = new ArrayList<String>();

    if (!((opts.contains("-E")) || (opts.contains("-G"))))
    {
      if (debug)
      {
        err("invalid : LogRead : processR : no person specified", ERRCODE);
      }
      else
      {
        err();
      }
    }

    for (Entry e : db.getEntries())
    {
      if (e.getName().equals(person))
      {
        if (e.getType().equals(type))
        {
          if (e.getEvent().equals("enter"))
          {
            rooms.add(String.valueOf(e.getRoom()));
          }
        }
      }
    }

    if (!(rooms.isEmpty()))
    {
      System.out.println(String.join(",", rooms.toArray(new String[rooms.size()])));
    }
  }

  private static void processT(DB db)
  {
    int total = 0;
    int last = -1;
    boolean in = false;

    for (Entry e : db.getEntries())
    {
      if (e.getName().equals(person))
      {
        if (e.getType().equals(type))
        {
          if (e.getEvent().equals("arrive"))
          {
            in = true;
            last = e.getTimestamp();
          }
          else if (e.getEvent().equals("depart"))
          {
            in = false;
            total += e.getTimestamp() - last;
          }
          else
          {
            int cur = e.getTimestamp();
            total += cur - last;
            last = cur;
          }
        }
        else if (in)
        {
          int cur = e.getTimestamp();
          total += cur - last;
          last = cur;
        }
      }
      else if (in)
      {
        int cur = e.getTimestamp();
        total += cur - last;
        last = cur;
      }
    }

    System.out.println(total);
  }

/*
  private static void debug()
  {
    opts.add("DEBUG");
  }
*/

  private static void process()
  {
    try
    {
      // hash and reduce key to 128 bits for AES
      byte[] shaKey = token.getBytes("UTF-8");
      MessageDigest sha = MessageDigest.getInstance("SHA-1");
      shaKey = sha.digest(shaKey);
      shaKey = Arrays.copyOf(shaKey, 16); // 128 bit

      Key aesKey = new SecretKeySpec(shaKey, "AES");
      Cipher cipher = Cipher.getInstance("AES");

      cipher.init(Cipher.DECRYPT_MODE, aesKey);

      // create stream
      FileInputStream fis = new FileInputStream(log);
      BufferedInputStream bis = new BufferedInputStream(fis);
      CipherInputStream cis = new CipherInputStream(bis, cipher);
      ObjectInputStream ois = new ObjectInputStream(cis);

      DB db = (DB) ois.readObject();
      ois.close();

      if (opts.contains("-S"))
      {
        // '-S' option : print current state of log
        processS(db);
      }
      else if (opts.contains("-R"))
      {
        if (!((opts.contains("-E")) || (opts.contains("-G"))))
        {
          if (debug)
          {
            err("invalid : LogRead : process : no person given for argument '-R'", ERRCODE);
          }
          else
          {
            err();
          }
        }

        processR(db);
      }
      else if (opts.contains("-T"))
      {
        if (!((opts.contains("-E")) || (opts.contains("-G"))))
        {
          if (debug)
          {
            err("invalid : LogRead : process : no person given for argument '-T'", ERRCODE);
          }
          else
          {
            err();
          }
        }

        processT(db);
      }
/*
      else if (opts.contains("DEBUG"))
      {
        System.out.println(db.toString());
      }
*/
    }
    catch (StreamCorruptedException e)
    {
      // invalid authentication token for existing log or log corrupted
      err("integrity violation", ERRCODE);
    }
    catch (FileNotFoundException e)
    {
      System.out.println("Unable to open file " + log);

    }
    catch (EOFException e)
    {
      // read all lines, close file
    }
    catch (IOException e)
    {
      System.out.println("Error reading file " + log);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String [] args)
  {
    for (int i = 0; i < args.length; i++)
    {
      String opt = args[i];

      switch (opt)
      {
        case "-K":  optK(args[++i]);
                    break;

        case "-S":  optS();
                    break;

        case "-R":  optR();
                    break;

        case "-T":  optT();
                    break;

        case "-I":  optI();
                    break;

        case "-E":  optE(args[++i]);
                    break;

        case "-G":  optG(args[++i]);
                    break;
/*
        case "DEBUG": debug();
                      break;
*/
        default:    optLog(opt);
                    break;
      }
    }

    process();

    System.exit(0);
  }
}
