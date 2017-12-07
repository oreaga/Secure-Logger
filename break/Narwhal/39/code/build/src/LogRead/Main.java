package src.LogRead;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        Map<String, String> fixedCommandLineArgs = fixArgs(args);
        String key = fixedCommandLineArgs.get("-K"), filePath = fixedCommandLineArgs.get("log");
        String name = "";

        ArrayList<HashMap<String, String>> file = Utils.read(key, filePath);

        if(fixedCommandLineArgs.containsKey("-E")) {

            name = fixedCommandLineArgs.get("-E");
        }
        else if(fixedCommandLineArgs.containsKey("-G")) {

            name = fixedCommandLineArgs.get("-G");
        }

        if(fixedCommandLineArgs.containsKey("-S")) {

            sOption(file);
        }
        else if(fixedCommandLineArgs.containsKey("-T")) {

            tOption(file, name);
        }
        else if(fixedCommandLineArgs.containsKey("-R")) {

            rOption(file, name);
        }
    }

    /*
    Print the current state of the log to stdout.
    The state should be printed to stdout on at least two lines, with lines separated by the \n (newline) character.
    The first line should be a comma-separated list of employees currently in the gallery. The second line should be a
    comma-separated list of guests currently in the gallery. The remaining lines should provide room-by-room
    information indicating which guest or employee is in which room. Each line should begin with a room ID, printed
    as a decimal integer, followed by a colon, followed by a space, followed by a comma-separated list of guests and
    employees. Room IDs should be printed in ascending integer order, all guest/employee names should be printed in
    ascending lexicographic string order.

    @param file The file array
     */

    private static void sOption(ArrayList<HashMap<String, String>> file) {

        TreeSet<String> employees = new TreeSet<String>();
        TreeSet<String> guests = new TreeSet<String>();
        TreeMap<String, TreeSet<String>> rooms = new TreeMap<String, TreeSet<String>>(new RoomComp());

        //create the first two lines of output.
        for(int i = 0; i < file.size(); i++) {

            //get employee names

            if(file.get(i).get("role").compareTo("employee") == 0) {

                if(file.get(i).get("status").equals("arrive")) {
                    employees.add(file.get(i).get("name"));
                }
                else if(file.get(i).get("status").equals("leave") && employees.contains(file.get(i).get("name"))) {
                    employees.remove(file.get(i).get("name"));
                }
            }

            //get guests names
            else if(file.get(i).get("role").compareTo("guest") == 0) {

                if(file.get(i).get("status").equals("arrive")) {
                    guests.add(file.get(i).get("name"));
                }
                else if(file.get(i).get("status").equals("leave") && guests.contains(file.get(i).get("name"))) {
                    guests.remove(file.get(i).get("name"));
                }
            }
        }

        //get room information
        for(int i = 0; i < file.size(); i++) {
            if (file.get(i).containsKey("room") && rooms.containsKey(file.get(i).get("room"))) {

                if(file.get(i).get("status").equals("arrive")) {

                    rooms.get(file.get(i).get("room")).add(file.get(i).get("name"));
                }
                else if(file.get(i).get("status").equals("leave") &&
                        rooms.get(file.get(i).get("room")).contains(file.get(i).get("name"))) {

                    rooms.get(file.get(i).get("room")).remove(file.get(i).get("name"));
                }
            }
            else if(file.get(i).containsKey("room") && !rooms.containsKey(file.get(i).get("room"))) {
                TreeSet<String> names = new TreeSet<String>();

                if(file.get(i).get("status").equals("arrive")) {

                    names.add(file.get(i).get("name"));
                    rooms.put(file.get(i).get("room"), names);
                }
            }
        }



        //print out line 1
        if(!employees.isEmpty()) {
            StringJoiner j = new StringJoiner(",");
            for (String r : employees) {
                j.add(r);
            }

            if (j.length() > 0) {
                System.out.println(j);
            }
        }

        //print out line 2
        if(!guests.isEmpty()) {
            StringJoiner x = new StringJoiner(",");
            for (String r : guests) {
                x.add(r);
            }
            if (x.length() > 0) {
                System.out.println(x);
            }
        }

        //print out the room information

        if(rooms.size() > 0) {
            rooms.forEach((k, v) -> {
                if (!v.isEmpty()) {
                    System.out.print(Integer.parseInt(k) + ": ");

                    StringJoiner l = new StringJoiner(",");
                    for (String r : v) {
                        l.add(r);
                    }
                    System.out.println(l);
                }
            });
        }

    }

    /*
    Give a list of all rooms entered by an employee or guest. Output the list of rooms in chronological order.
    If this argument is specified, either -E or -G must be specified. The list is printed to stdout in one
    comma-separated list of room identifiers. This list should include all rooms visited over the history of the log,
    regardless of how many separate visits the employee/guest has made to the gallery. If the specified employee or
    guest does not appear in the gallery log, then nothing is printed.

    @param file The file array
    @param name The name to check the log for.
     */
    private static void rOption(ArrayList<HashMap<String, String>> file, String name) {
        ArrayList<String> rooms = new ArrayList<String>();

        //loop through file to find name and room nums
        for(int i = 0; i < file.size(); i++) {

            if(file.get(i).get("name").equals(name)) {

                if(file.get(i).containsKey("room") && file.get(i).get("status").equals("arrive")) {
                    rooms.add(file.get(i).get("room"));
                }
            }
        }

        //print out the rooms
        if(!rooms.isEmpty()) {
            StringJoiner j = new StringJoiner(",");
            for (String r : rooms) {
                j.add(r);
            }
            System.out.println(j);
        }
    }

    /*
    Gives the total time spent in the gallery by an employee or guest, over the whole history of the log. If the
    employee or guest is currently in the gallery, include the time spent so far in this visit as well as total time
    spent in prior visits. Output is an integer on a single line. If the specified employee or guest does not appear
    in the gallery log, then nothing is printed. If an employee or guest enters at time 1 and leaves at time 10, the
    total time is 9 seconds.
    @param file The file array.
     */

    private static void tOption(ArrayList<HashMap<String, String>> file, String name) {

        int time = 0;
        int timeIn = 0;

        for(int i = 0; i < file.size(); i++) {
            if(file.get(i).get("name").equals(name) && file.get(i).get("status").equals("arrive")) {

                timeIn = Integer.parseInt(file.get(i).get("time"));
            }
            else if(file.get(i).get("name").equals(name) && file.get(i).get("status").equals("leave")) {

                time += Integer.parseInt(file.get(i).get("time")) - timeIn;
            }
        }


        if(time > 0) {
            System.out.println(time);
        }
    }

    /*
    Prints the rooms, as a comma-separated list of room IDs, that were occupied by all the specified employees and
    guests at the same time over the complete history of the gallery. Room IDs should be printed in ascending
    numerical order. If a specified employee or guest does not appear in the gallery, it is ignored. If no room ever
    contained all of the specified persons, then nothing is printed. This feature is optional.

     */

    private static void iOption() {
        System.out.println("unimplemented");

    }


    /*
    The fixArgs method fixes the command line input for processing the requests. Since arguments can be sent
    in the command line in any order.

    Command line arguments can appear in any order. If the same argument is provided multiple times,
    the last value is accepted.
    @param args The command line arguments.
    @return A map containing the correct arguments.
     */
    private static Map<String, String> fixArgs(String[] args) {
        int i;
        Map<String, String> fixedArgs = new HashMap<String, String>();
        String kOption = "-K", sOption = "-S", tOption = "-T", rOption = "-R", iOption = "-I";
        String employee = "-E", guest = "-G";


        //break apart command line args into a map for input validation
        for(i = 0; i < args.length; i++) {

            if(args[i].compareTo(kOption) == 0) {
                fixedArgs.put(kOption, args[i + 1]);
            }
            else if(args[i].compareTo(sOption) == 0) {

                fixedArgs.put(sOption, "1");
            }
            else if(args[i].compareTo(tOption) == 0) {

                fixedArgs.put(tOption, "1");
            }
            else if(args[i].compareTo(rOption) == 0) {

                fixedArgs.put(rOption, "1");
            }
            else if(args[i].compareTo(iOption) == 0) {

                fixedArgs.put(iOption, "1");
            }
            else if(args[i].compareTo(employee) == 0) {

                fixedArgs.put(employee, args[i + 1]);
            }
            else if(args[i].compareTo(guest) == 0) {

                fixedArgs.put(guest, args[i + 1]);
            }
            else if(i == args.length - 1 && !fixedArgs.containsValue(args[i])) {

                fixedArgs.put("log", args[i]);
            }
        }

        argValidation(fixedArgs);

        return fixedArgs;
    }

    /*
    The argValidation method ensure that the command line arguments are correct.
    If logread is invoked with an incomplete, contradictory, or otherwise non-compliant command line,
    it should print "invalid" to stdout and exit returning 255.

    @param args The command line arguments.
     */

    private static void argValidation(Map<String, String> args) {
        String kOption = "-K", sOption = "-S", tOption = "-T", rOption = "-R", iOption = "-I";
        String employee = "-E", guest = "-G";

        //valid the commands listed in the args map
        if(args.containsKey(sOption) && (args.containsKey(tOption) || args.containsKey(rOption)
                || args.containsKey(iOption))) {

            printInputError();
        }
        else if(args.containsKey(tOption) && (args.containsKey(sOption) || args.containsKey(rOption) ||
                args.containsKey(iOption))) {
            printInputError();
        }
        else if(args.containsKey(tOption) && !(args.containsKey(employee) || args.containsKey(guest))) {

            printInputError();
        }
        else if(args.containsKey(rOption) && (args.containsKey(sOption) || args.containsKey(tOption) ||
                args.containsKey(iOption))) {
            printInputError();
        }
        else if(args.containsKey(rOption) && !(args.containsKey(employee) || args.containsKey(guest))) {

            printInputError();
        }
        else if(args.containsKey(iOption) && (args.containsKey(sOption) || args.containsKey(tOption) ||
                args.containsKey(rOption))) {
            printInputError();
        }
        else if(args.containsKey("log") && !args.containsKey(kOption)) {
            printInputError();
        }
        else if(!args.containsKey("log") || !args.containsKey(kOption) || !(args.containsKey(iOption) ||
                args.containsKey(rOption) || args.containsKey(sOption) || args.containsKey(tOption))) {

            printInputError();
        }
    }

    /*
    prints out invalid if any of the command line arguments are messed up then exit with error code 255.
     */

    private static void printInputError() {
        System.out.println("invalid");
        System.exit(255);
    }
}

class RoomComp implements Comparator<String> {

   public int compare(String s1, String s2) {
      return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
   }
}
