
package src.LogRead;
import java.util.*;

public class Main {
    public static void main(String [] args) {
		// First go at reading command line options into a HashMap
        HashMap<String, String> options = new HashMap<String, String>();
        boolean isOption = true;
        String option = "";

        for (String arg : args) {
            if (isOption) {
            	option = arg;
            	isOption = false;

            } else {
            	options.put(option, arg);
            	isOption = true;

            }
        }

        for (Map.Entry<String, String> entry : options.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
		    System.out.println(key + " " + value);
		}
    }
}