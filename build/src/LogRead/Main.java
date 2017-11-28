
package src.LogRead;
import java.util.*;
import java.io.*;
import org.mindrot.jbcrypt.BCrypt;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;


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
    }

    private static void invalid() {
        System.out.println("invalid");
        System.exit(255);
    }

    private static void executeCommand(HashMap<String, String> values) {
        if (values.get("-S") != null) {
            printCurrentState(values);
        } else if (values.get("-R") != null) {
            listRoomsEntered(values);
        } else if (values.get("-T") != null) {
        	printTotalTime(values);
        } else {
            // System.out.println("-S -R or -T should be specified"); //Debug
        	invalid();
        }
    }

    private static int printCurrentState(HashMap<String, String> values) {
        // employees hash with name as key and current roomID as the value
        // guests hash with name as key and current roomID as the value
        // <record-id>,<timestamp>,<A or L>,<room-id>,<guest-name>,<employee-name>
        HashMap<String, Integer> employees = new HashMap<String, Integer>();
        HashMap<String, Integer> guests = new HashMap<String, Integer>();
        TreeMap<Integer, ArrayList<String>> rooms = new TreeMap<Integer, ArrayList<String>>();
        String currRecordID;
        String prevRecordID = "";
        String timestamp;
        String arriveOrLeave;
        String roomID;
        String guestName;
        String employeeName;
        int lineNum = 1;

        String logText = getLogText(values);
        String logTextTrimmed = logText.trim();
        // System.out.println(logText); // Debug
        String[] lines = logTextTrimmed.split("\n");
        for (String line : lines) {
            // System.out.println("Line: " + line); // Debug
            String[] record = line.split(",");
            if (record.length < 6) {
                // System.out.println("Error: Logfile line has less than 6 fields"); // Debug
                invalid();
            }
            currRecordID = record[0];
            timestamp = record[1];
            arriveOrLeave = record[2];
            roomID = record[3];
            guestName = record[4];
            employeeName = record[5];
            
            // had been causing a slow down when using jBcrypt
            if (lineNum == 1) {
            	checkHash(lineNum, values.get("logfile"), currRecordID);
            } else {
            	checkHash(lineNum, prevRecordID, currRecordID);
            }
            
            prevRecordID = currRecordID;

            // System.out.println("guestName: " + guestName); // Debug
            // System.out.println("employeeName: " + employeeName); // Debug

            if (guestName.equals("null") && !employeeName.equals("null")) {
                if (arriveOrLeave.equals("A") && !roomID.equals("-1")) {
                    // employee entered a room
                    employees.put(employeeName, Integer.parseInt(roomID));
                } else if (arriveOrLeave.equals("A") && roomID.equals("-1")) {
                    // employee entered the gallery but not a specific room
                    employees.put(employeeName, -1);
                } else if (arriveOrLeave.equals("L") && !roomID.equals("-1")) {
                    // employee left a room
                    employees.put(employeeName, -1);
                } else if (arriveOrLeave.equals("L") && roomID.equals("-1")) {
                    // employee has left the gallery
                    employees.remove(employeeName);
                } else {
                    // System.out.println("Every line should either have either A or L"); // Debug
                    invalid();    
                }
            } else if (!guestName.equals("") && employeeName.equals("null")) {
                if (arriveOrLeave.equals("A") && !roomID.equals("-1")) {
                    // guest entered a room
                    guests.put(guestName, Integer.parseInt(roomID));
                } else if (arriveOrLeave.equals("A") && roomID.equals("-1")) {
                    // guest entered the gallery but not a specific room
                    guests.put(guestName, -1);
                } else if (arriveOrLeave.equals("L") && !roomID.equals("-1")) {
                    // guest left a room
                    guests.put(guestName, -1);
                } else if (arriveOrLeave.equals("L") && roomID.equals("-1")) {
                    // guest has left the gallery
                    guests.remove(guestName);
                } else {
                    // System.out.println("Every line should either have either A or L"); // Debug
                    invalid();    
                }
            } else {
                // System.out.println("Every line should either have an employee or guest name"); // Debug
                invalid();
            }
            
            lineNum++;
        }

        // sort and print employee names
        ArrayList<String> employeeNames = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : employees.entrySet()) {
            String name = entry.getKey();
            int room = entry.getValue();
            employeeNames.add(name);

            if (room >= 0) {
                // also associate room number and present employees while we're at it
                if (rooms.putIfAbsent(room, new ArrayList<String>(Arrays.asList(name))) != null) {
                    rooms.get(room).add(name);
                }
            }
        }
        Collections.sort(employeeNames);
        int count = 1;
        int size = employeeNames.size();
        for (String s : employeeNames) {
            if (count < size) {
                System.out.print(s + ",");
            } else {
                System.out.print(s + "\n");
            }
            count++;
        }

        // and sort and print guest names
        ArrayList<String> guestNames = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : guests.entrySet()) {
            String name = entry.getKey();
            int room = entry.getValue();
            guestNames.add(name);

            if (room >= 0) {
                // also associate room number and present guests while we're at it
                if (rooms.putIfAbsent(room, new ArrayList<String>(Arrays.asList(name))) != null) {
                    rooms.get(room).add(name);
                }
            }
        }
        Collections.sort(guestNames);
        count = 1;
        size = guestNames.size();
        for (String s : guestNames) {
            if (count < size) {
                System.out.print(s + ",");
            } else {
                System.out.print(s + "\n");
            }
            count++;
        }

        for (Map.Entry<Integer, ArrayList<String>> entry : rooms.entrySet()) {
            int room = entry.getKey();
            ArrayList<String> names = entry.getValue();
            Collections.sort(names);
            System.out.print(room + ": ");

            count = 1;
            size = names.size();
            for (String name : names) {
                if (count < size) {
                    System.out.print(name + ",");
                } else {
                    System.out.print(name + "\n");
                }
                count++;
            }
        }

        return 0;
    }
    
    private static void listRoomsEntered(HashMap<String, String> values) {
    	ArrayList<Integer> roomsEntered = new ArrayList<Integer>();
    	String name = "";
    	boolean isEmployee = true; // must initialize but will always get changed
    	String currRecordID;
    	String prevRecordID = "";
        String timestamp;
        String arriveOrLeave;
        String roomID;
        String guestName;
        String employeeName;
        int lineNum = 1;
    	
    	if (values.get("-E") == null && values.get("-G") != null) {
    		name = values.get("-G");
    		isEmployee = false;
    	} else if (values.get("-E") != null && values.get("-G") == null) {
    		name = values.get("-E");
    		isEmployee = true;
    	} else {
    		// System.out.println("-E or -G must be specified along with -R");
    		invalid();
    	}
    	
    	String logText = getLogText(values);
        String logTextTrimmed = logText.trim();
        // System.out.println(logText); // Debug
        String[] lines = logTextTrimmed.split("\n");
        for (String line : lines) {
        	// System.out.println("Line: " + line); // Debug
            String[] record = line.split(",");
            if (record.length < 6) {
                // System.out.println("Error: Logfile line has less than 6 fields"); // Debug
                invalid();
            }
            currRecordID = record[0];
            timestamp = record[1];
            arriveOrLeave = record[2];
            roomID = record[3];
            guestName = record[4];
            employeeName = record[5];
            
            if (lineNum == 1) {
            	checkHash(lineNum, values.get("logfile"), currRecordID);
            } else {
            	checkHash(lineNum, prevRecordID, currRecordID);
            }
            
            prevRecordID = currRecordID;
            lineNum++;
            
            if (isEmployee && employeeName.equals(name) &&
            	!roomID.equals("null") && !roomID.equals("-1") && 
            	arriveOrLeave.equals("A")) {
            	roomsEntered.add(Integer.parseInt(roomID));
            } else if (!isEmployee && guestName.equals(name) && 
            		   !roomID.equals("null") && !roomID.equals("-1") && 
            		   arriveOrLeave.equals("A")) {
            	roomsEntered.add(Integer.parseInt(roomID));
            }
        }
        
        
        // print results
        int count = 1;
        int size = roomsEntered.size();
        for (Integer i : roomsEntered) {
        	if (count < size) {
                System.out.print(i + ",");
            } else {
                System.out.print(i + "\n");
            }
            count++;
        }
    }
    
    private static void printTotalTime(HashMap<String, String> values) {
    	String currRecordID;
    	String prevRecordID = "";
        String timestamp;
        String arriveOrLeave;
        String roomID;
        String guestName;
        String employeeName;
        int lineNum = 1;
        
        String name = "";
    	boolean isEmployee = true; // must initialize but will always get changed
        boolean inGallery = false;
        int timeEntered = 0;
        int timeLeft = 0;
        int totalTime = 0;
        
        if (values.get("-E") == null && values.get("-G") != null) {
    		name = values.get("-G");
    		isEmployee = false;
    	} else if (values.get("-E") != null && values.get("-G") == null) {
    		name = values.get("-E");
    		isEmployee = true;
    	} else {
    		// System.out.println("-E or -G must be specified along with -R");
    		invalid();
    	}
        
        String logText = getLogText(values);
        String logTextTrimmed = logText.trim();
        // System.out.println(logText); // Debug
        String[] lines = logTextTrimmed.split("\n");
        int numOfLogs = lines.length;
        int i = 1;
        for (String line : lines) {
        	// System.out.println("Line: " + line); // Debug
            String[] record = line.split(",");
            if (record.length < 6) {
                // System.out.println("Error: Logfile line has less than 6 fields"); // Debug
                invalid();
            }
            currRecordID = record[0];
            timestamp = record[1];
            arriveOrLeave = record[2];
            roomID = record[3];
            guestName = record[4];
            employeeName = record[5];
            
            if (lineNum == 1) {
            	checkHash(lineNum, values.get("logfile"), currRecordID);
            } else {
            	checkHash(lineNum, prevRecordID, currRecordID);
            }
            
            prevRecordID = currRecordID;
            lineNum++;
            
            // check for isEmployee is necessary because what if there is an
            // employee and guest with the same name
            if (isEmployee && employeeName.equals(name)) {
            	// Employee arriving at the gallery
            	if (roomID.equals("-1")) {
            		if (arriveOrLeave.equals("A")) {
						if (!timestamp.equals("null")) {
							timeEntered = Integer.parseInt(timestamp);
							inGallery = true;
						} else {
							// System.out.println("Timestamp should not be null"); //Debug
							invalid();
						}
            		} else if (arriveOrLeave.equals("L")) {
						if (!timestamp.equals("null")) {
							timeLeft = Integer.parseInt(timestamp);
							totalTime += (timeLeft - timeEntered);
							inGallery = false;
						} else {
							// System.out.println("Timestamp should not be null"); //Debug
							invalid();
						}
            		} else {
            			// System.out.println("A or L are the only valid choices"); // Debug
            			invalid();
            		}
            	}
            } else if (!isEmployee && guestName.equals(name)) {
				// Guest arriving at the gallery 
				if (roomID.equals("-1")) {
					if (arriveOrLeave.equals("A")) {
						if (!timestamp.equals("null")) {
							timeEntered = Integer.parseInt(timestamp);
							inGallery = true;
						} else {
							// System.out.println("Timestamp should not be null"); //Debug
							invalid();
						}
					} else if (arriveOrLeave.equals("L")) {
						if (!timestamp.equals("null")) {
							timeLeft = Integer.parseInt(timestamp);
							totalTime += (timeLeft - timeEntered);
							inGallery = false;
						} else {
							// System.out.println("Timestamp should not be null"); //Debug
							invalid();
						}
					} else {
						// System.out.println("A or L are the only valid choices"); // Debug
						invalid();
					}
				}
            }
            
            // we've got to the last log
            if (i == numOfLogs) {
            	if (!timestamp.equals("null")) {
            		int mostRecentTime = Integer.parseInt(timestamp);
            		if (inGallery) {
            			totalTime += (mostRecentTime - timeEntered);
            		}
            	} else {
            		// System.out.println("Timestamp should not be null"); //Debug
            		invalid();
            	}
            }
            
            i++;
        }
        
        // print results
        if (totalTime > 0) {
        	System.out.println(totalTime);
        // case where logread -T gets called when the only record of the employee
        // or guest is the most recent. Their time in the gallery will be 0
        // but they are actually present not just unfound
        } else if (inGallery && totalTime == 0) {
        	System.out.println(totalTime);
        }
    }

    private static boolean checkToken(HashMap<String, String> values) {
        String token = values.get("-K");
        byte[] tokenBytes = token.getBytes();
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
                        // System.out.println("Match"); //Debug
                        result = true;
                    }
                	/*
                	MessageDigest md = null;
                	try {
                		md = MessageDigest.getInstance("SHA-1");
                	} catch (Exception e) {
                		invalid();
                	}

                    md.update(tokenBytes);
                    byte[] tokenDigest = md.digest();
                    String tokenDigestStr = new String(tokenDigest);
                    if (tokenDigestStr.equals(hash)) {
                    	result = true;
                    }
                    */
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // makes sure command is valid by making checks that could not be made 
    // during initial reading of args (in readArgs())
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
                System.out.println("unimplemented");
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


    /****** Encryption Stuff *********/
    
    private static void checkHash(Integer lineNum, String prevHash, String currHash) {
        String num = lineNum.toString();
        String toCompare = (new Integer ((num + prevHash).hashCode())).toString();
        if (!toCompare.equals(currHash)) {
        	System.out.println("integrity violation");
        	System.exit(255);
        }
    }


    private static String getLogText(HashMap<String, String> values) {
        FileInputStream fs = null;
        String foundHash = "";
        byte[] encText = null;
        String plainText = null;
        String path = values.get("logfile");
        try {
            fs = new FileInputStream(path);
            encText = new byte[fs.available()];
            fs.read(encText);
        }
        catch (Exception e) {
            // System.out.println("Unable to open logfile to get text");
        	invalid();
        }
        
        String encTextStr = new String(encText);
        // System.out.println(lastTenToString.hashCode()); // Debug
        
        BufferedReader br = null;
        FileReader fr = null;

        try {
            fr = new FileReader(values.get("logfile") + ".hash");
            br = new BufferedReader(fr);

            foundHash = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (encTextStr.hashCode() != Integer.parseInt(foundHash)) {
        	System.out.println("integrity violation");
        	System.exit(255);
        }

        plainText = decrypt(encText, path);
        return plainText;
    }

    private static byte[] getKey(String path) {
        byte[] key = new byte[16];
        FileInputStream fs = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            fs = new FileInputStream(logfile + ".key");
        }
        catch (FileNotFoundException e) {
            // System.out.println("Could not find key file");
            // System.exit(255);
        	invalid();
        }

        try {
            fs.read(key);
        }
        catch (IOException e) {
//            System.out.println("Could not read key file");
//            System.exit(255);
        	invalid();
        }
        return key;
    }

    private static byte[] getIV(String path) {
        byte[] iv = new byte[16];
        FileInputStream fs = null;
        String[] pathFields = path.split("/");
        String logfile = pathFields[pathFields.length - 1];
        try {
            fs = new FileInputStream(logfile + ".iv");
        }
        catch (FileNotFoundException e) {
            // System.out.println("Could not find iv file");
            // System.exit(255);
        	invalid();
        }

        try {
            fs.read(iv);
        }
        catch (IOException e) {
//            System.out.println("Could not read iv file");
//            System.exit(255);
        	invalid();
        }
        return iv;
    }

    private static String decrypt(byte[] encMessage, String path) {
        Cipher cipher = null;
        String retString = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
//            System.out.println("Could not create cipher for decryption");
        	invalid();
        }
        byte[] key = getKey(path);
        byte[] iv = getIV(path);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher.init(cipher.DECRYPT_MODE, skeySpec, ivSpec);
        }
        catch (Exception e) {
//            System.out.println("Invalid parameters for decryption");
//            System.exit(255);
        	invalid();
        }
        byte[] decrypted = null;

        try {
            decrypted = cipher.doFinal(encMessage);
        }
        catch (Exception e) {
//            System.out.println("Bad block size for decryption");
//            System.exit(255);
        	invalid();
        }
        retString = new String(decrypted);
        return retString;
    }
}