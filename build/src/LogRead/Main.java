
package src.LogRead;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;

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
        if (!checkToken(values)) {
            System.out.println("integrity violation");
            System.exit(255);
        }
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

    private static int printCurrentState(HashMap<String, String> values) {
        // employees hash with name as key and current roomID as the value
        // guests hash with name as key and current roomID as the value
        HashMap<String, Integer> employees = new HashMap<String, Integer>();
        HashMap<String, Integer> guests = new HashMap<String, Integer>();

        FileReader in = null;
        BufferedReader buf = null;
        String line = null;

        try {
            in = new FileReader(values.get("logfile"));
            buf = new BufferedReader(in);
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not open hashes.txt to check token");
            System.exit(255);
        }

        try {
            while ((line = buf.readLine()) != null) {
                // record = decrypt(line);
                // store stuff about record
            }
        }
        catch (IOException e) {
            System.out.println("Error reading from hash file");
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

    private static void executeCommand(HashMap<String, String> values) {
        if (values.get("-S") != null) {
            printCurrentState(values);
        } else if (values.get("-R") != null) {
            // listRoomsEntered(values);
        } else if (values.get("-T") != null) {
            //printTotalTime(values);
        } else {
            System.out.println("Shouldn't get here");
        }
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
                System.out.println("Unimplemented");
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
}