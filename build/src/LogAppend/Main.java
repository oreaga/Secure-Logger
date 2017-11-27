// A basic hello world program
package src.LogAppend;
import org.apache.commons.crypto.cipher.*;
import java.security.*;
import javax.Cipher;
import java.util.*;
import org.mindrot.jbcrypt.Bcrypt;

public class Main {
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

        // Get the employee or guest name
        // If one not provided fail with 255
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
            if (args[i].equals("-R")) {
                error = error + m.processRoom(args[i + 1]);
            }

        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-A")) {
                error = error + m.processArrival();
            }
            if (args[i].equals("-L")) {
                error = error + m.processLeave();
            }
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
    private String getLastLine(String path) {
        FileReader fr;
        BufferedReader br;
        String test;
        String recLine;if (path == null) {
            System.out.println("Path not supplied to commandline");
            System.exit(255);
        }

        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);
        }
        catch (IOException e) {
            System.out.println("No prior arrivals");
            return 0;
        }

        while ((test = br.readline()) != null) {
            recLine = test;
        }

        return recLine;
    }

    private int processArrival() {
        FileReader fr;
        BufferedReader br;
        String last = null;
        String prevRec = null;
        boolean entGallery = false;
        boolean left = true;


        if (path == null) {
            System.out.println("Path not supplied to commandline");
            System.exit(255);
        }

        try {
            fr = new FileReader(path);
            br = new BufferedReader(fr);
        }
        catch (IOException e) {
            System.out.println("No prior arrivals");
            return 0;
        }

        last = br.readline();
        if (last == null) {
            return 0;
        }
        while ((last = br.readline()) != null) {

        }


        return 0;
    }

    private int processLeave() {
        return 0;
    }

    private int processRoom(String r) {
        System.out.println("Checking Room");
        try {
            r = Integer.parseInt(r);
        }
        catch (Exception e) {
            System.out.println("Error parsing integer room");
            System.exit(255);
        }

        if (r < 0 || r > 1073741823) {
            return 1;
        }
        else {
            values.put("room", r);
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
        fw.write(log + ":" + hash + "\n");
    }

    private static void createKey(String logfile) {
        FileWriter fr = new FileWriter(logfile + ".key");
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
    }

    private static String encrypt(String message) {
        private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");


    }

    private static String decrypt(String encMessage) {
        private Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        return "Filler Plaintext";
    }

    private int appendToLog() {
        FileWriter fr;
        StringBuilder vars;
        String encVars;
        int recordNum;
        int checkTok = checkToken(token);
        boolean newLog = false;

        if (checkTok == -1) {
            System.out.println("Invalid token");
            System.exit(255);
        }
        else if (checkTok == 0) {
            System.out.println("Creating new log");
            createLog(path, BCrypt.hashpw(token, BCrypt.gensalt(12)));
            createKey(path);
            newLog = true;
        }

        for (Map.Entry(String k, String v) e : values) {
            if (e.getValue() == null) {
                values.put(e.getKey(), "null");
            }
        }



        // ********** TODO ********
        // Figure out a good way to implement the record-id,
        // incrementing integers might not be great
        if (newLog) {
            recNum =  "1:" + path;
        }
        else {
            recNum = getNextRecNum(path);
        }
        vars = BCrypt.hashpw(recNum, BCrypt.gensalt(12)) + values.get("timestamp") + "," + values.get("arrival") + "," + values.get("room") + "," +  values.get("guest") + "," + values.get("employee");
        encVars = encrypt(vars);
        try {
            fr = new FileWriter(path, true);
        }
        catch (IOException e) {
            System.out.println("Error opening log");
            System.exit(255);
        }
        fr.write(recNum + ":" + encVars + "\n");




        return 0;
    }
}
