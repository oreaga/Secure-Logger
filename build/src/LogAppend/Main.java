// A basic hello world program
package src.LogAppend;

public class Main {
    Integer timestamp = null;
    String token = null;
    String employee = null;
    String guest = null;
    Boolean arrival = null;
    Integer room = null;
    String path = null;

    public static void main(String [] args) {
        Main m = new Main();
        int i;
        int error = 0;

        for (i = 0; i < args.length; i++) {
            System.out.println(i);
            if (args[i].equals("-T")) {
                error = error + m.processTime(args[i + 1]);
            }
            if (args[i].equals("-K")) {
                error = error + m.processToken(args[i+1]);
            }
            if (args[i].equals("-E")) {
                error = error + m.processEmployee(args[i + 1]);
            }
            if (args[i].equals("-G")) {
                error = error + m.processGuest(args[i + i]);
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
            else {
                m.appendToLog();
            }
        }

    }

    // Method for checking time constraints
    private int processTime(String time) {
        int t = 0;
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
            timestamp = t;
        }

        return 0;

    }

    // Method for checking token constraints
    private int processToken(String tok) {
        if (!(tok.matches("\\w+"))) {
            return 1;
        }
        else {
            token = tok;
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
        return 0;
    }

    private int processArrival() {
        return 0;
    }

    private int processLeave() {
        return 0;
    }

    private int processRoom(String r) {
        return 0;
    }

    private int appendToLog() {
        return 0;
    }
}
