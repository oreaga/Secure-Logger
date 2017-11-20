// A basic hello world program
package src.LogAppend;

public class Main {
    public static void main(String [] args) {
        // You will need to delete all of this
        System.out.println("Hello World. - This is logappend");

        int argNum = 1;
        for (String arg : args) {
            System.out.println("" + (argNum++) + " " + arg );
        }
    }
}