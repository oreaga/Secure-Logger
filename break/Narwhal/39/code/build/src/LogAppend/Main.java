// A basic hello world program
package src.LogAppend;

import java.io.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;

public class Main {
 public static void main(String [] args) {
    HashMap<String, String> input = null;
    
    if (args[0].equals("-B")) { //Batch command
       String filename = args[1];
       handleBatch(args, filename);
    } else {
       input = mapify(args);
       append(input);
    } 
 }
 
 private static HashMap<String, String> mapify(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    HashMap<String, String> argsMap = new HashMap<String, String>();
    boolean[] marked = new boolean[argsList.size()]; //Used to mark arguments that are commands or used by other commands. At the end, the index remaining false is the log filename index in argsList
    
    for (int i = 0; i < argsList.size(); i++) {
       String val = "";
       
       if (i < argsList.size() - 1) val = argsList.get(i + 1);
       
       switch (argsList.get(i)) {
          case "-T":
             if (!isNumeric(val) || Integer.parseInt(val) < 1 || Integer.parseInt(val) > 1073741823) exit255();
             else {
                argsMap.put("time", val);
                
                marked[i] = true;
                marked[i + 1] = true;
             }
             
             break;
          case "-K":
             if (!isAlphanumeric(val)) exit255();
             else {
                argsMap.put("token", val);
                
                marked[i] = true;
                marked[i + 1] = true;
             }
             
             break;
          case "-E":
             if (argsList.contains("-G") || !isAlphabetic(val)) exit255();
             else {
                argsMap.put("role", "employee");
                argsMap.put("name", val);
                
                marked[i] = true;
                marked[i + 1] = true;
             }
             
             break;
          case "-G":
             if (argsList.contains("-E") || !isAlphabetic(val)) exit255();
             else {
                argsMap.put("role", "guest");
                argsMap.put("name", val);
                
                marked[i] = true;
                marked[i + 1] = true;
             }
             
             break;
          case "-A":
             if (argsList.contains("-L")) exit255();
             else {
                argsMap.put("status", "arrive");
                
                marked[i] = true;
             }
             
             break;
          case "-L":
             if (argsList.contains("-A")) exit255();
             else {
                argsMap.put("status", "leave");
                
                marked[i] = true;
             }
             
             break;
          case "-R":
             if (!isNumeric(new Integer(val).toString()) || Integer.parseInt(val) < 0 || Integer.parseInt(val) > 1073741823) exit255();
             else {
                argsMap.put("room", new Integer(val).toString());
                
                marked[i] = true;
                marked[i + 1] = true;
             }
             
             break;
          case "-B":
             exit255();
          default:
             break;
       }
    }
    
    for (int i = 0, counter = 0; i < argsList.size(); i++) {
       if (marked[i] == false) {
          argsMap.put("log", argsList.get(i));
          
          if (counter == 0) counter++;
          else exit255();
       }
    }
    
    return argsMap;
 }
 
 private static void handleBatch(String[] args, String filename) {
    try {
       FileReader inputFile = new FileReader(filename);
       BufferedReader bufferReader = new BufferedReader(inputFile);
       HashMap<String, String> input = null;
       String line;

       while ((line = bufferReader.readLine()) != null) {
          List<String> argsList = new ArrayList<String>(Arrays.asList(line.split(" ")));
          if (argsList.contains("-B")) System.out.println("invalid");
          else {
             input = mapify(line.split(" "));
             append(input);
          }
       }

       bufferReader.close();
    } catch (Exception e) {
       exit255();
    }
 }
 
 private static void append(HashMap<String, String> line) {
    String token = line.get("token");
    String filename = line.get("log");
    ArrayList<HashMap<String, String>> log = null;
    
    if (token != null && filename != null) log = Utils.read(token, filename);
    else exit255();
    
    if (log != null) validate(line, log);
    else exit255();
    
    //Only gets here if validated
    Utils.write(line, token, filename);
 }
 
 private static void validate(Map<String, String> line, ArrayList<HashMap<String, String>> log) {
    String time = line.get("time");
    String status = line.get("status"); //This is whether they arrived or left. The value will be either "arrive" or "leave"
    String name = line.get("name");
    String role = line.get("role"); //"employee" or "guest"
    String roomID = line.get("room");
    HashMap<String, String> last = null;
    
    if (name == null || time == null || status == null || role == null) exit255(); //incomplete map/line
    
    if ((last = getLastEvent(log)) != null) {
    	if (Integer.parseInt(time) <= Integer.parseInt(last.get("time"))) exit255(); //Violation of time constraints -> invalid
    }
    
    if (status.equals("arrive")) { //If arrived
       last = getLastEvent(name, role, log);
       
       if (roomID == null) { //If no room ID given, then they are arriving at the entire gallery
          if ((last != null) && (last.get("status").equals("arrive") ||
                (last.get("status").equals("leave") && last.get("room") != null))) exit255(); //Arriving twice in a row, leaving a specific room then arriving to the gallery -> invalid
       } else { //room ID is given, they are arriving at a specific room
          if ((last == null) || ((last != null) && ((last.get("status").equals("arrive") && last.get("room") != null) ||
                (last.get("status").equals("leave") && last.get("room") == null)))) exit255(); //Arriving to a specific room without previous events, arriving twice in a row to a specific room -> invalid
       }
    } else if (status.equals("leave")) { //If leaving
       last = getLastEvent(name, role, log);
       
       if (roomID == null) { //If no room ID given, then they are leaving the entire gallery
          if ((last == null) || ((last != null) && ((last.get("status").equals("leave") && last.get("room") == null) ||
                (last.get("status").equals("arrive") && last.get("room") != null)))) exit255(); //Leaving gallery without previous events, leaving gallery twice in a row, leaving gallery without leaving room -> invalid
       } else { //room ID is given, they are leaving a specific room
          if ((last == null) || ((last != null) && ((last.get("status").equals("leave")) ||
                (last.get("status").equals("arrive") && last.get("room") == null) || 
                (last.get("status").equals("arrive") && !last.get("room").equals(roomID))))) exit255(); //Leaving room without previous events, leaving room after already leaving, leaving room after arriving to gallery, leaving wrong room -> invalid
       }
    } 
 }
 
 private static HashMap<String, String> getLastEvent(String name, String role, ArrayList<HashMap<String, String>> log) {
    for (int i = log.size() - 1; i >= 0; i--) {
       if (log.get(i).get("name").equals(name) && log.get(i).get("role").equals(role)) return log.get(i);
    }
    
    return null;
 }
 
 private static HashMap<String, String> getLastEvent(ArrayList<HashMap<String, String>> log) {
    if (!log.isEmpty()) {
    	return log.get(log.size() - 1);
    }
    else return null;
 }
 
 private static boolean isAlphanumeric(String s){
    return s.matches("^[a-zA-Z0-9]*$");
}
 
 private static boolean isNumeric(String s){
    return s.matches("^[0-9]*$");
}
 
 private static boolean isAlphabetic(String s){
    return s.matches("^[a-zA-Z]*$");
}
 
 private static void exit255() {
    System.out.println("invalid");
    System.exit(255);
 }
 
}