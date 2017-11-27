// A basic hello world program
package src.LogAppend;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.*;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;

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
            if (m.values.get("guest") == null && m.values.get("employee") == null) {
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
        Integer t = 0;
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
            values.put("employee", e);
        }

        return 0;
    }

    private int processGuest(String g) {
        if (!(g.matches("[a-zA-Z]+"))) {
            return 1;
        }
        else {
            values.put("guest", g);
        }
        return 0;
    }
    private String getLastLine(String path) {
        FileReader fr;
        BufferedReader br;
        String test;
        String p = values.get("path");
        String recLine;
        if (p == null) {
            System.out.println("Path not supplied to commandline");
            System.exit(255);
        }

        try {
            fr = new FileReader(p);
            br = new BufferedReader(fr);
        }
        catch (IOException e) {
            System.out.println("No prior arrivals");
            return null;
        }

        while ((test = br.readLine()) != null) {
            recLine = test;
        }

        return recLine;
    }

    private int processArrival() {
        /*
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

        last = br.readLine();
        if (last == null) {
            return 0;
        }
        while ((last = br.readLine()) != null) {

        }
        */

        return 0;
    }

    private int processLeave() {
        return 0;
    }

    private int processRoom(String r) {
        System.out.println("Checking Room");
        Integer room = -1;
        try {
            room = Integer.parseInt(r);
        }
        catch (Exception e) {
            System.out.println("Error parsing integer room");
            System.exit(255);
        }

        if (room < 0 || room > 1073741823) {
            return 1;
        }
        else {
            values.put("room", room.toString());
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
        String testHash = BCrypt.hashpw(values.get("token"), BCrypt.gensalt(12));
        String text;


        while ((text = buf.readLine()) != null) {
            fields = text.split(":");

            if (values.get("path").equals(fields[0])) {
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
        FileOutputStream fsKey = new FileOutputStream(logfile + ".key");
        FileOutputStream fsIV = new FileOutputStream(logfile + ".iv");
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        byte[] iv = new byte[16];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);
        fsKey.write(key);
        fsIV.write(iv);
    }

    private static byte[] getKey(String logfile) {
        byte[] key = new byte[128];
        FileInputStream fs = new FileInputStream(logfile + ".key");
        fs.read(key);
        return key;
    }

    private static byte[] getIV(String logfile) {
        byte[] iv = new byte[16];
        FileInputStream fs = new FileInputStream(logfile + ".iv");
        fs.read(iv);
        return iv;
    }

    private static byte[] encrypt(String message, String path) {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        return cipher.doFinal(message.getBytes());
    }

    private static byte[] decrypt(String encMessage, String path) {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(cipher.DECRYPT_MODE, skeySpec, ivSpec);
        return cipher.doFinal(encMessage.getBytes());
    }

    private int appendToLog() {
        FileWriter fr;
        StringBuilder vars;
        String encVars;
        String recNum;
        String token = values.get("token");
        String path = values.get("path");
        int checkTok = checkToken();
        boolean newLog = false;

        if (checkTok == -1) {
            System.out.println("Invalid token");
            System.exit(255);
        }
        else if (checkTok == 0) {
            System.out.println("Creating new log");
            createLog(path, BCrypt.hashpw(token, BCrypt.gensalt(12)));
            createKey(path.split("/")[path.length() - 1]);
            newLog = true;
        }

        for (Map.Entry<String, String> e : values) {
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
