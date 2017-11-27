
package src.LogRead;
import java.util.*;

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

    private static int printCurrentState() {
        return 0;
    }

    private static boolean checkToken(HashMap<String, String> values) {
        String token = values.get("-K");
        String logfile = values.get("logfile");

        // check against hashes.txt for specified logfile
        return true;
    }

    private static void executeCommand(HashMap<String, String> values) {
        // first open file specified by logfile by decrypting with token
        // next check if we're doing -R, -S or -T and call the appropriate function
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