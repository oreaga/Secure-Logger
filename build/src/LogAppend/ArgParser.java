package src.LogAppend;

public class ArgParser {

    public ArgParser() {}

    public static int parseArgs(String[] args, boolean isBatch) {
        LogAppender m = new LogAppender();
        int i;
        int error = 0;

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
            if (args[i].equals("-B")) {
                error++;
            }
            if (args[i].equals("-E")) {
                error = error + m.processEmployee(args[i + 1]);
            }
            if (args[i].equals("-G")) {
                error = error + m.processGuest(args[i + 1]);
            }
        }
        if (m.get("guest") == null && m.get("employee") == null) {
            System.out.println("Please provide employee or guest name");
            error++;
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
            if (isBatch == true) {
                System.out.println("Invalid");
                return 255;
            }
            else {
                System.out.println("Invalid");
                System.exit(255);
            }
        }

        error = m.appendToLog();

        if (error != 0) {
            if (isBatch == true) {
                System.out.println("Invalid");
                return 255;
            }
            else {
                System.out.println("Invalid");
                System.exit(255);
            }
        }

    return 0;
    }
}
