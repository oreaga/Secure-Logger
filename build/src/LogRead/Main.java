
package src.LogRead;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Main {
    public static void main(String [] args) {
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("-K", null);
        values.put("-S", null);
        values.put("-R", null);
        values.put("-T", null);
        values.put("-I", null);
        values.put("-E", null);
        values.put("-G", null);
        values.put("logfile", null);
        
        values = readArgs(args, values);
        if (!isValidArgs(values)) {
            invalid();
        }
        // if (!checkToken(values)) {
        //     System.out.println("integrity violation");
        //     System.exit(255);
        // }
        executeCommand(values);

        // Debugging
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + " " + value);
        }
    }

    private static void invalid() {
        System.out.println("invalid");
        System.exit(255);
    }

    private static void executeCommand(HashMap<String, String> values) {
        if (values.get("-S") != null) {
            printCurrentState(values);
        } else if (values.get("-R") != null) {
            // listRoomsEntered(values);
        } else if (values.get("-T") != null) {
            // printTotalTime(values);
        } else {
            System.out.println("-S -R or -T should be specified"); //Debug
        }
    }

    private static int printCurrentState(HashMap<String, String> values) {
        // employees hash with name as key and current roomID as the value
        // guests hash with name as key and current roomID as the value
        // <record-id>,<timestamp>,<A or L>,<room-id>,<guest-name>,<employee-name>
        HashMap<String, Integer> employees = new HashMap<String, Integer>();
        HashMap<String, Integer> guests = new HashMap<String, Integer>();
        String recordID;
        String timestamp;
        String arriveOrLeave;
        String roomID;
        String guestName;
        String employeeName;

        String logText = getLogText(values);
        System.out.println(logText); // Debug
        String[] lines = logText.split("\n");
        int x = 1;
        for (String line : lines) {
            String[] record = line.split(",");
            if (record.length < 5) {
                System.out.println("Error: Logfile line has less than 6 fields"); // Debug
                invalid();
            }
            timestamp = record[1];
            arriveOrLeave = record[2];
            roomID = record[3];
            guestName = record[4];

            // since employee name is last it will not be given "" by split
            if (record.length == 6) {
                employeeName = record[5];
            } else {
                employeeName = "";
            }

            // System.out.println("guestName: " + guestName); // Debug
            // System.out.println("employeeName: " + employeeName); // Debug

            // will an absence of a field be null or empty string??
            if (guestName.equals("") && !employeeName.equals("")) {
                if (arriveOrLeave.equals("A") && !roomID.equals("")) {
                    // employee entered a room
                    employees.put(employeeName, Integer.parseInt(roomID));
                } else if (arriveOrLeave.equals("A") && roomID.equals("")) {
                    // employee entered the gallery but not a specific room
                    employees.put(employeeName, -1);
                } else if (arriveOrLeave.equals("L") && !roomID.equals("")) {
                    // employee left a room
                    employees.put(employeeName, -1);
                } else if (arriveOrLeave.equals("L") && roomID.equals("")) {
                    // employee has left the gallery
                    employees.remove(employeeName);
                } else {
                    System.out.println("Every line should either have either A or L"); // Debug
                    invalid();    
                }
            } else if (!guestName.equals("") && employeeName.equals("")) {
                if (arriveOrLeave.equals("A") && !roomID.equals("")) {
                    // guest entered a room
                    guests.put(guestName, Integer.parseInt(roomID));
                } else if (arriveOrLeave.equals("A") && roomID.equals("")) {
                    // guest entered the gallery but not a specific room
                    guests.put(guestName, -1);
                } else if (arriveOrLeave.equals("L") && !roomID.equals("")) {
                    // guest left a room
                    guests.put(guestName, -1);
                } else if (arriveOrLeave.equals("L") && roomID.equals("")) {
                    // guest has left the gallery
                    guests.remove(guestName);
                } else {
                    System.out.println("Every line should either have either A or L"); // Debug
                    invalid();    
                }
            } else {
                System.out.println("Every line should either have an employee or guest name"); // Debug
                invalid();
            }
        }

        // sort and print employee names
        ArrayList<String> employeeNames = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : employees.entrySet()) {
            String name = entry.getKey();
            Object room = entry.getValue();
            employeeNames.add(name);
        }
        Collections.sort(employeeNames, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        int count = 1;
        int size = employeeNames.size();
        for (String s : employeeNames) {
            if (count < size) {
                System.out.print(s + ",");
            } else {
                System.out.print(s + "\n");
            }
            count++;
        }

        // and sort and print guest names
        ArrayList<String> guestNames = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : guests.entrySet()) {
            String name = entry.getKey();
            Object room = entry.getValue();
            guestNames.add(name);
        }
        Collections.sort(guestNames, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        count = 1;
        size = guestNames.size();
        for (String s : guestNames) {
            if (count < size) {
                System.out.print(s + ",");
            } else {
                System.out.print(s + "\n");
            }
            count++;
        }

        return 0;
    }

    private static boolean checkToken(HashMap<String, String> values) {
        String token = values.get("-K");
        String currentLogfile = values.get("logfile");
        BufferedReader br = null;
        FileReader fr = null;
        boolean result = false;

        try {
            fr = new FileReader("hashes.txt");
            br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                String[] lineArr = line.split(":", 2);
                String foundLogfile = lineArr[0];
                String hash = lineArr[1];

                if (foundLogfile.equals(currentLogfile)) {
                    // System.out.println("Found: " + foundLogfile + " Current: " + currentLogfile); // Debug
                    if (BCrypt.checkpw(token, hash)) {
                        System.out.println("Match");
                        result = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // check against hashes.txt for specified logfile
        return result;
    }

    // makes sure command is valid by making checks that could not be made 
    // during initial reading of args
    private static boolean isValidArgs(HashMap<String, String> values) {
        // always must supply a token
        if (values.get("-K") == null) {
            return false;
        }
        // always must supply at least -R, -S or -T
        if (values.get("-R") == null && values.get("-S") == null && values.get("-T") == null) {
            return false;
        }
        // always must supply a logfile
        if (values.get("logfile") == null) {
            return false;
        }
        // -R and -T require that -E or -G be supplied
        if (values.get("-R") != null || values.get("-T") != null) {
            if (values.get("-E") == null && values.get("-G") == null) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidToken(String tok) {
        return tok.matches("[a-zA-Z0-9]+");
    }

    private static boolean isValidLogFile(String log) {
        return log.matches("[a-zA-Z0-9_/.]+");
    }

    // reads command line args into a HashMap and checks for conditions that
    // can be checked while reading
    private static HashMap<String, String> readArgs(String[] args, HashMap<String, String> values) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-K")) {
                i++;
                // check to see that token argument is supplied
                if (i >= args.length) {
                    invalid();
                }

                // make sure token is alphanumeric characters
                if (!isValidToken(args[i])) {
                    invalid();
                }

                // most recent token should be used
                values.put("-K", args[i]);

            } else if (args[i].equals("-S")) {
                // there can be only -S, -R or -T
                if (values.get("-R") != null || values.get("-T") != null ) {
                    invalid();
                }

                values.put("-S", "found");
        
            } else if (args[i].equals("-R")) {
                // there can be only -S, -R or -T
                if (values.get("-S") != null || values.get("-T") != null ) {
                    invalid();
                }

                values.put("-R", "found");
                
            } else if (args[i].equals("-T")) {
                // there can be only -S, -R or -T
                if (values.get("-S") != null || values.get("-R") != null ) {
                    invalid();
                }

                values.put("-T", "found");
                
            } else if (args[i].equals("-I")) {
                System.out.println("unimplemented");
                System.exit(255);
            } else if (args[i].equals("-E")) {
                // should only ever have one -E OR -G supplied
                if (values.get("-E") != null || values.get("-G") != null) {
                    invalid();
                }

                i++;
                // check to see that employee name argument is supplied
                if (i >= args.length) {
                    invalid();
                }

                values.put("-E", args[i]);
            } else if (args[i].equals("-G")) {
                // should only ever have one -E OR -G supplied
                if (values.get("-E") != null || values.get("-G") != null) {
                    invalid();
                }

                i++;
                // check to see that guest name argument is supplied
                if (i >= args.length) {
                    invalid();
                }

                values.put("-G", args[i]);
            } else {
                // this should be the logfile
                if (!isValidLogFile(args[i])) {
                    invalid();
                }

                // only one logfile can be read
                if (values.get("logfile") != null) {
                    invalid();
                }

                values.put("logfile", args[i]);
            }
        }

        return values;
    }


    /****** Encryption Stuff *********/


    private static String getLogText(HashMap<String, String> values) {
        FileInputStream fs = null;
        byte[] encText = null;
        String plainText = null;
        String path = values.get("logfile");
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
}