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
    private boolean batch;

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
        try {
            t = Integer.parseInt(time);
        }
        catch (Exception e) {
            return 1;
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

    public int processRoom(String r) {
        Integer room = -1;
        try {
            room = Integer.parseInt(r);
        }
        catch (Exception e) {
            return 1;
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
            return null;
        }
        else {
            sr = new StringReader(logText);
            br = new BufferedReader(sr);

            try {
                prevLine = br.readLine();
            }
            catch (IOException e) {
            }
        }

        // Initialize employee/guest state
        prevFields = prevLine.split(",");
        currStamp = Integer.parseInt(prevFields[1]);
        if (prevFields[geIndex].equals(ge)) {
            currRoom = prevFields[3];
            if (prevFields[2].equals("A") && prevFields[3].equals("-1")) {
                inGallery = true;
            }
            else if (prevFields[2].equals("L") && prevFields[3].equals("-1")) {
                inGallery = false;
            }
            else if (prevFields[2].equals("A")) {
                free = false;
                currRoom = prevFields[3];
            }
            else if (prevFields[2].equals("L")) {
                free = true;
                currRoom = "0";
            }
        }

        while ((currLine = br.readLine()) != null) {
            prevFields = prevLine.split(",");
            currFields = currLine.split(",");

            /* Ensure log has not been modified
            if (checkHash(i, prevFields[0], currFields[0]) != 0) {
                return null;
            }
            */

            // Ensure timestamp is being incremented
            try {
                prevStamp = Integer.parseInt(prevFields[1]);
                currStamp = Integer.parseInt(currFields[1]);
            }
            catch (Exception e) {
            }
            if (!(currStamp > prevStamp)) {
                return null;
            }

            // Update employee/guest state
            if (currFields[geIndex].equals(ge)) {
                if (currFields[2].equals("A") && currFields[3].equals("-1")) {
                    inGallery = true;
                }
                else if (currFields[2].equals("L") && currFields[3].equals("-1")) {
                    inGallery = false;
                }
                else if (currFields[2].equals("A")) {
                    free = false;
                    currRoom = currFields[3];
                }
                else if (currFields[2].equals("L")) {
                    free = true;
                    currRoom = "-1";
                }
            }

            // Update block
            prevLine = currLine;
            i++;
        }

        //Update prevFields
        prevFields = prevLine.split(",");

        // Check that record to be appended is consistent with state
        if ((stamp > currStamp) && ((inGallery == false && newRoom.equals("-1") && al.equals("A")) ||
            (inGallery == true && free == true && al == "A" && !(newRoom.equals("-1"))) ||
            (inGallery == true && free == false && al == "L" && newRoom.equals(currRoom)) ||
            (inGallery == true && free == true && al == "L" && newRoom.equals("-1")))
            ) {

            valid = true;
        }

        if (valid) {
            retString = i.toString(); //+ prevFields[0];
        }

        return retString;
    }

    private boolean checkValidInitial() {
        boolean valid = false;

        if (values.get("room") == "-1" && values.get("arrival") == "A") {
            valid = true;
        }

        return valid;
    }

    private int checkHash(Integer lineNum, String prevHash, String currHash) {
        String num = lineNum.toString();
        if (!(BCrypt.checkpw(num + prevHash, currHash))) {
            return 255;
        }
        return 0;
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
            buf = null;
        }

        if (buf != null) {
            try {
                while ((text = buf.readLine()) != null) {
                    fields = text.split(":");

                    if (values.get("path").equals(fields[0])) {

                        if (BCrypt.checkpw(values.get("token"), fields[1])) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            } catch (IOException e) {
            }
        }

        return 0;
    }

    private static void createLog(String log, String hash) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("hashes.txt", true);
        }
        catch (IOException e) {
        }

        try {
            fw.write(log + ":" + hash + "\n");
            fw.close();
        }
        catch (IOException e) {
        }
    }

    private static void createKey(String path) {
        FileOutputStream fsKey = null;
        KeyGenerator keygen = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            keygen = KeyGenerator.getInstance("AES");
        }
        catch (NoSuchAlgorithmException e) {
        }
        keygen.init(128);
        byte[] key = keygen.generateKey().getEncoded();

        try {
            fsKey = new FileOutputStream(logfile + ".key");
        }
        catch (FileNotFoundException e) {
        }

        try {
            fsKey.write(key);
        }
        catch (IOException e) {
        }
    }

    private static void createIV(String path) {
        FileOutputStream fsIV = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        byte[] iv = new byte[16];
        SecureRandom secRandom = new SecureRandom();
        secRandom.nextBytes(iv);
        try {
            fsIV = new FileOutputStream(logfile + ".iv");
        }
        catch (FileNotFoundException e) {
        }

        try {
            fsIV.write(iv);
        }
        catch (IOException e) {
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
        }

        try {
            fs.read(key);
        }
        catch (IOException e) {
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
        }

        try {
            fs.read(iv);
        }
        catch (IOException e) {
        }
        return iv;
    }

    private static byte[] encrypt(String message, String path) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
        }
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher.init(cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        }
        catch (Exception e) {
        }
        byte[] encrypted = new byte[cipher.getOutputSize(message.getBytes().length)];

        try {
            encrypted = cipher.doFinal(message.getBytes());
        }
        catch (Exception e) {
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
        }
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher.init(cipher.DECRYPT_MODE, skeySpec, ivSpec);
        }
        catch (Exception e) {
        }
        byte[] decrypted = null;

        try {
            decrypted = cipher.doFinal(encMessage);
        }
        catch (Exception e) {
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
        }

        plainText = decrypt(encText, path);
        return plainText;
    }

    public int appendToLog() {
        FileOutputStream fs = null;
        FileWriter fw = null;
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
            return 255;
        }
        else if (checkTok == 0) {
            createLog(path, BCrypt.hashpw(token, BCrypt.gensalt(12)));
            createKey(path);
            createIV(path);
            newLog = true;
        }

        if (!newLog) {
            logText = getLogText();
            try {
                validText = checkValid();
            }
            catch (IOException e) {
            }
            if (validText == null) {
                return 255;
            }
        }
        else {
            if (!checkValidInitial()) {
                return 255;
            }
            logText = "";
        }

        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getValue() == null) {
                values.put(e.getKey(), "null");
            }
        }


        if (newLog) {
            recID =  "1"; //+ path;
        }
        else {
            recID = validText;
        }
        vars = /*BCrypt.hashpw(recID, BCrypt.gensalt(12))*/ recID + "," + values.get("timestamp") + "," + values.get("arrival") + "," + values.get("room") + "," +  values.get("guest") + "," + values.get("employee") + "\n";
        logText = logText + vars;
        createIV(path);
        byteText = encrypt(logText, path);
        byte[] lastTen = Arrays.copyOfRange(byteText, byteText.length - 10, byteText.length);
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            fw = new FileWriter(logfile + ".hash");
            fw.write(BCrypt.hashpw(new String(lastTen), BCrypt.gensalt(12)));
            fw.close();
        }
        catch (Exception e) {}
        try {
            fs = new FileOutputStream(path);
        }
        catch (IOException e) {

        }

        try {
            fs.write(byteText);
            fs.close();
        }
        catch (IOException e) {

        }

        return 0;
    }
}
