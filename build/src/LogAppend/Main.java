// A basic hello world program
package src.LogAppend;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;

public class Main {

    public void Main() {}


    public static void main(String [] args) {
        System.exit(255);
        FileReader fr = null;
        BufferedReader br = null;
        String path = null;
        String command = null;
        String[] commandArgs = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-B")) {
                i++;
                if (i > args.length) {
                    System.out.print("invalid");
                    System.exit(255);
                }
                path = args[i];
            }
        }

        if (path != null) {
            try {
                fr = new FileReader(path);
                br = new BufferedReader(fr);
                while ((command = br.readLine()) != null) {
                    commandArgs = command.split(" ");
                    ArgParser.parseArgs(commandArgs, true);
                }
            }
            catch (Exception e) {
            }
        }
        else {
            ArgParser.parseArgs(args, false);
        }



        /*
        FileWriter fw = null;
        InputStream is = null;
        InputStreamReader fr = null;
        BufferedReader br = null;
        byte[] b = null;
        String enc = null;
        String r = null;
        try {
            fw = new FileWriter("test-read.txt");
            fr = new InputStreamReader(is);
            br = new BufferedReader(fr);
        }
        catch (Exception e) {}
        Main m = new Main();
        enc = Main.encrypt("Fuck this", "test-log.txt");

        try {fw.write(enc);} catch (Exception e) {}
        try{r = br.readLine();} catch (Exception e) {}

        String dec = Main.decrypt(r, "test-log.txt");
        System.out.println(dec);

        LogAppender m = new LogAppender();
        int i;
        int error = 0;
        int validToken = -1;

        if (args.length < 2) {
            System.out.println("Please supply arguments to program");
            System.exit(255);
        }

        // Get Logfile name
        m.put("path", args[args.length - 1]);

        // Get the employee or guest name
        // If one not provided fail with 255
        System.out.println("Getting name");
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-E")) {
                error = error + m.processEmployee(args[i + 1]);
            }
            if (args[i].equals("-G")) {
                error = error + m.processGuest(args[i + 1]);
            }
        }
        if (m.get("guest") == null && m.get("employee") == null) {
            System.out.println("Please provide employee or guest name");
            System.exit(255);
        }

        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-T")) {
                error = error + m.processTime(args[i + 1]);
            }
            if (args[i].equals("-K")) {
                error = error + m.processToken(args[i + 1]);
            }
            if (args[i].equals("-R")) {
                error = error + m.processRoom(args[i + 1]);
            }
        }
        if (m.get("token") == null || m.get("timestamp") == null) {
            System.out.println("Please supply a token and a timestamp");
            System.exit(255);
        }
        if (m.get("room") == null) {
            m.put("room", "-1");
        }

        // Search command line for A or L
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-A")) {
                m.setArrive(true);
            }
            if (args[i].equals("-L")) {
                m.setLeave(true);
            }
        }

        // Set values.arrival to A or L
        if (m.getLeave() == true && m.getArrive() == true) {
            System.out.println("Please specify only one of A or L");
            System.exit(255);
        }
        else if (m.getArrive() == true) {
            m.put("arrival", "A");
        }
        else if (m.getLeave() == true) {
            m.put("arrival", "L");
        }
        else {
            System.out.println("Please supply one of A or L");
        }

        if (error != 0) {
            System.exit(255);
        }

        m.appendToLog();
        */
    }
}
