// A basic hello world program
package src.LogAppend;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;

public class LogAppender {
    private HashMap<String, String> values;
    private String logText;
    private boolean arrive;
    private boolean leave;

    public LogAppender() {
        values = new HashMap<String, String>();
        values.put("timestamp", null);
        values.put("token", null);
        values.put("employee", null);
        values.put("guest", null);
        values.put("arrival", null);
        values.put("room", null);
        values.put("path", null);
        values.put("recentID", null);
        logText = null;
        arrive = false;
        leave = false;
    }

    public void put(String k, String v) {
        this.values.put(k, v);
    }

    public String get(String k) {
        return this.values.get(k);
    }

    public boolean getArrive() {
        return arrive;
    }

    public boolean getLeave() {
        return leave;
    }

    public void setArrive(boolean val) {
        this.arrive = val;
    }

    public void setLeave(boolean val) {
        this.leave = val;
    }

    // Method for checking time constraints
    public int processTime(String time) {
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
    public int processToken(String tok) {
        if (!(tok.matches("\\w+"))) {
            return 1;
        }
        else {
            values.put("token", tok);
        }

        return 0;
    }

    // Method for checking employee constraints
    public int processEmployee(String e) {
        if (!(e.matches("[a-zA-Z]+"))) {
            return 1;
        }
        else {
            values.put("employee", e);
        }

        return 0;
    }

    public int processGuest(String g) {
        if (!(g.matches("[a-zA-Z]+"))) {
            return 1;
        }
        else {
            values.put("guest", g);
        }
        return 0;
    }

/*
    private String getLastLine(String path) {
        FileReader fr;
        BufferedReader br;
        String test;
        String p = values.get("path");
        String recLine = null;
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

        try {
            while ((test = br.readLine()) != null) {
                recLine = test;
            }
        }
        catch (IOException e) {
            System.out.println("Error reading last line of log");
            System.exit(255);
        }

        return recLine;
    }
*/

    /*
    private int checkArrival(String a) {
        FileReader fr;
        BufferedReader br;
        String last = null;
        String prevRec = null;
        String decRec = null;
        String[] recFields = null;
        String[] fields = null;
        boolean entGallery = false;
        boolean left = true;
        boolean guest = false;
        int index = -1;

        if (values.get("guest") != null) {
            guest = true;
            index = 4;
        }
        else {
            index = 3;
        }



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
        else {
            decRec = new String(decrypt(last.split(":")[1]));
            prevHash = decRec.split(",")[0];
        }

        while ((last = br.readLine()) != null) {
            fields = last.split(":");
            currNum = fields[0];
            decRec = new String(decrypt(fields[1]));
            recFields = decRec.split(",");

            // Check if the log has been tampered with
            if (!(BCrypt.checkpw(currNum + prevHash, decRec.split(",")[0]))) {
                System.out.println("Invalid log state, record hashes don't match");
                System.exit(255);
            }

            // Check for info about current guest/employee
            if (recFields[index] == values.get(""))
        }

        return 0;
    }

    private int checkLeave() {
        return 0;
    }
    */

    public int processRoom(String r) {
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

    private String checkValid() throws IOException {
        String al;
        Integer currStamp = null;
        Integer prevStamp = null;
        Integer stamp;
        String currRoom = null;
        String newRoom = values.get("room");
        String ge = null;
        Integer geIndex = null;
        boolean free = true;
        StringReader sr = null;
        BufferedReader br = null;
        String currLine = null;
        String prevLine = null;
        String[] currFields = null;
        String[] prevFields = null;
        boolean inGallery = false;
        boolean valid = false;
        String retString = null;
        Integer i = 2;

        // Determine whether A or L and guest or employee
        al = values.get("arrival");
        stamp = Integer.parseInt(values.get("timestamp"));
        if (values.get("guest") != null) {
            geIndex = 4;
            ge = values.get("guest");
        }
        else {
            geIndex = 5;
            ge = values.get("employee");
        }

        // Ensure that log has been decrypted
        if (logText == null) {
            System.out.println("No plaintext to check for validity");
            System.exit(255);
        }
        else {
            sr = new StringReader(logText);
            br = new BufferedReader(sr);

            try {
                prevLine = br.readLine();
            }
            catch (IOException e) {
                System.out.println("Error reading first line of file");
            }
        }

        while ((currLine = br.readLine()) != null) {
            prevFields = prevLine.split(",");
            currFields = currLine.split(",");

            // Ensure log has not been modified
            checkHash(i, prevFields[0], currFields[0]);

            // Ensure timestamp is being incremented
            try {
                prevStamp = Integer.parseInt(prevFields[1]);
                currStamp = Integer.parseInt(currFields[1]);
            }
            catch (Exception e) {
                System.out.println("Error parsing timestamps for validity");
                System.exit(255);
            }
            if (!(currStamp > prevStamp)) {
                System.out.println("Invalid timestamp: Not Incrementing");
                System.exit(255);
            }

            // Update employee/guest state
            if (currFields[geIndex].equals(ge)) {
                if (currFields[2].equals("A") && currFields[3].equals("0")) {
                    inGallery = true;
                }
                else if (currFields[2].equals("L") && currFields[3].equals("0")) {
                    inGallery = false;
                }
                else if (currFields[2].equals("A")) {
                    free = false;
                    currRoom = currFields[3];
                }
                else if (currFields[2].equals("L")) {
                    free = true;
                    currRoom = "0";
                }
            }

            // Update block
            prevLine = currLine;
            i++;
        }

        // Check that record to be appended is consistent with state
        if ((stamp > currStamp) && (inGallery == false && newRoom.equals("0") && al.equals("A")) ||
            (inGallery == true && free == true && al == "A" && !(newRoom.equals("0"))) ||
            (inGallery == true && free == false && al == "L" && newRoom.equals(currRoom)) ||
            (inGallery == true && free == true && al == "L" && newRoom.equals("0"))
            ) {

            valid = true;
        }

        if (valid) {
            retString = i.toString() + prevFields[1];
        }

        return retString;
    }

    private void checkHash(Integer lineNum, String prevHash, String currHash) {
        String num = lineNum.toString();
        if (!(BCrypt.checkpw(num + prevHash, currHash))) {
            System.out.println("Hashes do not match, log modified illegally");
            System.exit(255);
        }
    }

    // Checks the supplied token to determine whether it is correct for the log
    // file specified.
    // Returns 1 if correct, 0 if logfile doesn't exist, and -1 if incorrect
    private int checkToken() {
        FileReader in = null;
        BufferedReader buf = null;
        String[] fields;
        String text = null;

        try {
            in = new FileReader("hashes.txt");
            buf = new BufferedReader(in);
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not open hashes.txt to check token");
            buf = null;
        }

        if (buf != null) {
            try {
                while ((text = buf.readLine()) != null) {
                    fields = text.split(":");

                    if (values.get("path").equals(fields[0])) {
                        System.out.println("Found the right log file");

                        if (BCrypt.checkpw(values.get("token"), fields[1])) {
                            System.out.println("Hashes match");
                            return 1;
                        } else {
                            System.out.println("Hashes do not match");
                            return -1;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from hash file");
            }
        }

        return 0;
    }

    private static void createLog(String log, String hash) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("hashes.txt");
        }
        catch (IOException e) {
            System.out.println("Error opening file hashes.txt");
        }

        try {
            fw.write(log + ":" + hash + "\n");
            fw.close();
        }
        catch (IOException e) {
            System.out.println("Could not write to hash file");
            System.exit(255);
        }
    }

    private static void createKey(String path) {
        FileOutputStream fsKey = null;
        FileOutputStream fsIV = null;
        KeyGenerator keygen = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            keygen = KeyGenerator.getInstance("AES");
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Failure at keygen");
            System.exit(255);
        }
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();
        byte[] iv = new byte[16];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);

        try {
            fsKey = new FileOutputStream(logfile + ".key");
            fsIV = new FileOutputStream(logfile + ".iv");
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not find key or iv file");
            System.exit(255);
        }

        try {
            fsKey.write(key);
            fsIV.write(iv);
        }
        catch (IOException e) {
            System.out.println("Could not write to key or iv file");
        }
    }

    private static byte[] getKey(String path) {
        byte[] key = new byte[16];
        FileInputStream fs = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            fs = new FileInputStream(logfile + ".key");
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not find key file");
            System.exit(255);
        }

        try {
            fs.read(key);
        }
        catch (IOException e) {
            System.out.println("Could not read key file");
            System.exit(255);
        }
        return key;
    }

    private static byte[] getIV(String path) {
        byte[] iv = new byte[16];
        FileInputStream fs = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            fs = new FileInputStream(logfile + ".iv");
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not find iv file");
            System.exit(255);
        }

        try {
            fs.read(iv);
        }
        catch (IOException e) {
            System.out.println("Could not read iv file");
            System.exit(255);
        }
        return iv;
    }

    private static byte[] encrypt(String message, String path) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
            System.out.println("No support for padding in encrypt");
            System.exit(255);
        }
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher.init(cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        }
        catch (Exception e) {
            System.out.println("Invalid parameters for encryption");
            System.exit(255);
        }
        byte[] encrypted = new byte[cipher.getOutputSize(message.getBytes().length)];

        try {
            encrypted = cipher.doFinal(message.getBytes());
        }
        catch (Exception e) {
            System.out.println("Bad block size for encryption");
            System.exit(255);
        }
        return encrypted;
    }

    private static String decrypt(byte[] encMessage, String path) {
        Cipher cipher = null;
        String retString = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
            System.out.println("Could not create cipher for decryption");
        }
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher.init(cipher.DECRYPT_MODE, skeySpec, ivSpec);
        }
        catch (Exception e) {
            System.out.println("Invalid parameters for decryption");
            System.exit(255);
        }
        byte[] decrypted = null;

        try {
            decrypted = cipher.doFinal(encMessage);
        }
        catch (Exception e) {
            System.out.println("Bad block size for decryption");
            System.exit(255);
        }
        retString = new String(decrypted);
        return retString;
    }

    private String getLogText() {
        FileInputStream fs = null;
        byte[] encText = null;
        String plainText = null;
        String path = values.get("path");
        try {
            fs = new FileInputStream(path);
            encText = new byte[fs.available()];
            fs.read(encText);
        }
        catch (Exception e) {
            System.out.println("Unable to open logfile to get text");
            System.exit(255);
        }

        plainText = decrypt(encText, path);
        return plainText;
    }

    public int appendToLog() {
        FileOutputStream fw = null;
        String vars;
        String encVars;
        String recID;
        String token = values.get("token");
        String path = values.get("path");
        int checkTok = checkToken();
        String validText = null;
        boolean newLog = false;
        byte[] byteText;

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

        if (!newLog) {
            logText = getLogText();
            try {
                validText = checkValid();
            }
            catch (IOException e) {
                System.out.println("IO error while checking log validity");
                System.exit(255);
            }
            if (validText == null) {
                System.out.println("Record not consistent with log state");
                System.exit(255);
            }
        }
        else {
            logText = "";
        }

        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getValue() == null) {
                values.put(e.getKey(), "null");
            }
        }



        // ********** TODO ********
        // Figure out a good way to implement the record-id,
        // incrementing integers might not be great
        if (newLog) {
            recID =  "1" + path;
        }
        else {
            recID = validText;
        }
        vars = BCrypt.hashpw(recID, BCrypt.gensalt(12)) + "," + values.get("timestamp") + "," + values.get("arrival") + "," + values.get("room") + "," +  values.get("guest") + "," + values.get("employee") + "\n";
        logText = logText + vars;
        byteText = encrypt(logText, path);
        try {
            fw = new FileOutputStream(path);
        }
        catch (IOException e) {
            System.out.println("Error opening log");
            System.exit(255);
        }

        try {
            fw.write(byteText);
            fw.close();
        }
        catch (IOException e) {
            System.out.println("Error appending to log");
            System.exit(255);
        }

        return 0;
    }
}
