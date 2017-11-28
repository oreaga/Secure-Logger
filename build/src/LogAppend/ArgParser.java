package src.LogAppend;

public class ArgParser {

    public ArgParser() {}

    public static boolean checkIndex(int i, int length) {
        if (i > length) {
            return false;
        }
        return true;
    }

    public static int parseArgs(String[] args, boolean isBatch) {
        LogAppender m = new LogAppender();
        int i;
        int error = 0;

        if (args.length < 2) {
            error++;
        }

        // Get the employee or guest name
        // If one not provided fail with 255
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-B")) {
                error++;
            }
            else if (args[i].equals("-E")) {
                i++;
                if (!checkIndex(i, args.length)) {
                    return 255;
                }
                error = error + m.processEmployee(args[i]);
            }
            else if (args[i].equals("-G")) {
                i++;
                if (!checkIndex(i, args.length)) {
                    return 255;
                }
                error = error + m.processGuest(args[i]);
            }
            else if (args[i].equals("-T")) {
                i++;
                if (!checkIndex(i, args.length)) {
                    return 255;
                }
                error = error + m.processTime(args[i]);
            }
            else if (args[i].equals("-K")) {
                i++;
                if (!checkIndex(i, args.length)) {
                    return 255;
                }
                error = error + m.processToken(args[i]);
            }
            else if (args[i].equals("-R")) {
                i++;
                if (!checkIndex(i, args.length)) {
                    return 255;
                }
                error = error + m.processRoom(args[i]);
            }
            else if (args[i].equals("-A")) {
                m.setArrive(true);
            }
            else if (args[i].equals("-L")) {
                m.setLeave(true);
            }
            else {
                // Get Logfile name
                m.put("path", args[i]);
            }
        }

        if (m.get("guest") == null && m.get("employee") == null) {
            error++;
        }
        if (m.get("token") == null || m.get("timestamp") == null) {
            error++;
        }
        if (m.get("room") == null) {
            m.put("room", "-1");
        }

        // Set values.arrival to A or L
        if (m.getLeave() == true && m.getArrive() == true) {
            error++;
        }
        else if (m.getArrive() == true) {
            m.put("arrival", "A");
        }
        else if (m.getLeave() == true) {
            m.put("arrival", "L");
        }
        else {
            error++;
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
