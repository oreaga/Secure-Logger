// A basic hello world program
package src.LogAppend;
import java.io.*;

public class Main {

    public void Main() {}


    public static void main(String [] args) {
        FileReader fr = null;
        BufferedReader br = null;
        String path = null;
        String command = null;
        String[] commandArgs = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-B")) {
                i++;
                if (i > args.length) {
                    System.out.print("invalid");
                    System.exit(255);
                }
                path = args[i];
            }
        }

        if (path != null) {
            try {
                fr = new FileReader(path);
                br = new BufferedReader(fr);
                while ((command = br.readLine()) != null) {
                    commandArgs = command.split(" ");
                    ArgParser.parseArgs(commandArgs, true);
                }
            }
            catch (Exception e) {
            }
        }
        else {
            ArgParser.parseArgs(args, false);
        }

    }
}
