// A basic hello world program
package src.LogAppend;
import org.apache.commons.crypto.cipher.*;
import java.security.*;
import javax.Cipher;
import java.util.*;
import org.mindrot.jbcrypt.Bcrypt;

public class Main {
    private Integer timestamp = null;
    private String token = null;
    private String employee = null;
    private String guest = null;
    private Boolean arrival = null;
    private Integer room = null;
    private String path = null;
    private Integer recentID = null;
    private String logText = null;
    private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    private HashMap<String, String> values;

    public void Main() {
        this.values = new HashMap<String, String>();
        values.put("timestamp", null);
        values.put("token", null);
        values.put("employee", null);
        values.put("guest", null);
        values.put("arrival", null);
        values.put("room", null);
        values.put("path", null);
        values.put("recentID", null);
    }

    public static void main(String [] args) {
        Main m = new Main();
        int i;
        int error = 0;

        System.out.println("Getting name");
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-E")) {
                error = error + m.processEmployee(args[i + 1]);
            }
            if (args[i].equals("-G")) {
                error = error + m.processGuest(args[i + i]);
            }
            if (m.guest == null && m.employee == null) {
                System.out.println("Please provide employee or guest name");
                System.exit(255);
            }
        }

        for (i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (args[i].equals("-T")) {
                error = error + m.processTime(args[i + 1]);
            }
            if (args[i].equals("-K")) {
                error = error + m.processToken(args[i+1]);
            }
            if (args[i].equals("-A")) {
                error = error + m.processArrival();
            }
            if (args[i].equals("-L")) {
                error = error + m.processLeave();
            }
            if (args[i].equals("-R")) {
                error = error + m.processRoom(args[i + 1]);
            }

        if (error != 0) {
            System.exit(255);
        }

        m.appendToLog();
        }

    }

    // Method for checking time constraints
    private int processTime(String time) {
        int t = 0;
        System.out.println("Checking the time");
        try {
            t = Integer.parseInt(time);
        }
        catch (Exception e) {
            System.out.println("Error parsing integer time");
            System.exit(255);
        }
        if (t < 1 || t > 1073741823) {
            return 1;
        }
        else {
            values.put("timestamp", t.toString());
        }

        return 0;

    }

    // Method for checking token constraints
    private int processToken(String tok) {
        if (!(tok.matches("\\w+"))) {
            return 1;
        }
        else {
            values.put("token", tok);
        }

        return 0;
    }

    // Method for checking employee constraints
    private int processEmployee(String e) {
        if (!(e.matches("[a-zA-Z]+"))) {
            return 1;
        }
        else {
            employee = e;
        }

        return 0;
    }

    private int processGuest(String g) {
        if (!(g.matches("[a-zA-Z]+"))) {
            return 1;
        }
        else {
            guest = g;
        }
        return 0;
    }

    private int processArrival() {
        return 0;
    }

    private int processLeave() {
        return 0;
    }

    private int processRoom(String r) {
        System.out.println("Checking Room");
        try {
            room = Integer.parseInt(r);
        }
        catch (Exception e) {
            System.out.println("Error parsing integer room");
            System.exit(255);
        }
        return 0;
    }

    // Checks the supplied token to determine whether it is correct for the log
    // file specified.
    // Returns 1 if correct, 0 if logfile doesn't exist, and -1 if incorrect
    private int checkToken() {
        FileReader in = new FileReader("hashes.txt");
        BufferedReader buf = new BufferedReader(in);
        String[] fields;
        String testHash = BCrypt.hashpw(token, BCrypt.gensalt(12));


        while ((text = buf.readline()) != null) {
            fields = text.split(":");

            if (path.equals(fields[0])) {
                System.out.println("Found the right log file");

                if (BCrypt.checkpw(testHash, fields[0])) {
                    System.out.println("Hashes match");
                    return 0;
                }
                else {
                    System.out.println("Hashes do not match");
                    return 0;
                }
            }
        }

        return 0;
    }

    private static void createLog(String log, String hash) {
        FileWriter fw;
        try {
            fw = new FileWriter("hashes.txt", true);
        }
        catch (IOException e) {
            System.out.println("Error opening file hashes.txt");
        }

        fw.write(log + hash + "\n");
    }

    private int appendToLog() {
        FileWriter fr;
        int checkTok = checkToken(token);

        if (checkTok == -1) {
            System.out.println("Invalid token");
            System.exit(255);
        }
        else if (checkTok == 0) {
            System.out.println("Creating new log");
            createLog(path, BCrypt.hashpw(token, BCrypt.gensalt(12)));
        }

        fr = new FileWriter(path, true);


        return 0;
    }
}
