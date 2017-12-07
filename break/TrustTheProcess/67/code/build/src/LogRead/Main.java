package src.LogRead;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.BufferedReader;
import java.util.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Main {
	private class Visitor {
		String name;
		//Boolean arrival;
		Boolean employee; 
		//int lastRoom;
		//int currRoomID;
		//int currTime;
		//LinkedHashMap<String, List<String>> prevLine = new LinkedHashMap<String, List<String>>(); // roomId -> times

		//file decrypted at this point
		private void getInfo() {
			File logFile = new File(logName);
			if (!logFile.exists()) {
				printError(INVALID);
			}
			decrypt(logFile);

			String [] prev;
			String line;
			if (params.containsKey("-R")) {
				populateVisitor();
				List<String> rooms = new ArrayList<String>();
				try {
					/// Format for each line: Name,employee,roomID,arrival,time
					BufferedReader input = new BufferedReader(new FileReader(logName));
					while ((line = input.readLine()) != null) {
						prev = line.split(",");
						// must be equal name and employee type
						if (prev[0].equals(name) && prev[1].equals(employee.toString())) {
							if (Integer.parseInt(prev[2]) >= 0) 
								rooms.add(prev[2]);
							//success = true;
						} 
					}
					input.close();
				} catch (IOException e) {
					printError(INVALID);
				}

				String csv =  (rooms.isEmpty()) ? null : String.join(",", rooms);
				if (csv != null) 
					System.out.println(csv);

			} else if (params.containsKey("-S")) { //don't need to call populate visitor here
				SortedSet<String> employeeList = new TreeSet<String>();
				SortedSet<String> guestList = new TreeSet<String>();
				SortedMap<Integer, ArrayList<String>> roomToVisitors = new TreeMap<Integer, ArrayList<String>>();
				HashMap<String, String> visitorToRoom = new HashMap<String, String>();
				try {
					/// Format for each line: Name,employee,roomID,arrival,time
					BufferedReader input = new BufferedReader(new FileReader(logName));
					while ((line = input.readLine()) != null) {
						prev = line.split(",");
						if (prev[1].equals("true")) {
							employeeList.add(prev[0]);
							if (Integer.parseInt(prev[2]) < -2) {
								employeeList.remove(prev[0]);
							}
						} else if (prev[1].equals("false")) {
							guestList.add(prev[0]);
							if (Integer.parseInt(prev[2]) < -2) {
								guestList.remove(prev[0]);
							}
						}
				
						//entered room within gallery
						if (Integer.parseInt(prev[2]) >= 0) {
							visitorToRoom.put(prev[0] + "@" + prev[1], prev[2]);
							roomToVisitors.put(Integer.parseInt(prev[2]), new ArrayList<String>());
						}
					}
					input.close();
				} catch (IOException e) {
					printError(INVALID);
				}

				String csv1 =  (employeeList.isEmpty()) ? null : String.join(",", employeeList);
				if (csv1 != null) {
					System.out.println(csv1);
				} else {
					System.out.println();
				}
				

				String csv2 =  (guestList.isEmpty()) ? null : String.join(",", guestList);
				if (csv2 != null) {
					System.out.println(csv2);
				} else {
					System.out.println();
				}
			
				
				for (String vis : visitorToRoom.keySet()) {
					if (employeeList.contains(vis.substring(0, vis.indexOf("@"))) || guestList.contains(vis.substring(0, vis.indexOf("@")))) 
						roomToVisitors.get(Integer.parseInt(visitorToRoom.get(vis))).add(vis.substring(0, vis.indexOf("@")));
				}

				for (Integer r : roomToVisitors.keySet()) {
					if (!roomToVisitors.get(r).isEmpty()) {
						Collections.sort(roomToVisitors.get(r));
						System.out.println(r + ": " + String.join(",", roomToVisitors.get(r)));
					}
				}

			} else if (params.containsKey("-T")) {
				populateVisitor();
				boolean appearsInLog = false;
				ArrayList<Integer> timesEntered = new ArrayList<Integer>();
				ArrayList<Integer> timesLeft = new ArrayList<Integer>();
				int lastTimeEntry = 0;
				int totalTime = 0;
				
				try {
					/// Format for each line: Name,employee,roomID,arrival,time
					BufferedReader input = new BufferedReader(new FileReader(logName));
					while ((line = input.readLine()) != null) {
						prev = line.split(",");
						
						if (prev[0].equals(name) && prev[1].equals(employee.toString())) {
							appearsInLog = true;
							if (Integer.parseInt(prev[2]) == -2) {
								if (prev[3].equals("true")) {
									timesEntered.add(Integer.parseInt(prev[4]));
								} 
							} else if (Integer.parseInt(prev[2]) < -2) {
								timesLeft.add(Integer.parseInt(prev[4]));
							}
						} 
						lastTimeEntry = Integer.parseInt(prev[4]);
					}
					input.close();
					
					if (!appearsInLog) {
						printError(INVALID);
					}
				} catch (IOException e) {
					printError(INVALID);
				}
				
				
				if (timesEntered.size() == timesLeft.size()) {
					for (int i = 0; i<timesEntered.size(); i++) {
						totalTime += timesLeft.get(i) - timesEntered.get(i);
					}
				} else {
					for (int j = 0; j<timesEntered.size(); j++) {
						if (j == timesLeft.size()) {
							totalTime += lastTimeEntry - timesEntered.get(j);
						} else {
							totalTime += timesLeft.get(j) - timesEntered.get(j);
						}
					}
				}
				System.out.println(totalTime);
				
			}
			encrypt(logFile);
		}

		private void populateVisitor() {
			employee = params.containsKey("-E") ? true : false; 
			name = employee ? params.get("-E").get(0) : params.get("-G").get(0);
		}
	}
	private static final int INTEGRITY = 256;
	private static final int INVALID = 255;
	private static final int SUCCESS = 0;

	private static final int DEFAULT_MODE = -7; // Jlin baby
	private static final int ROOM_MODE = 0;
	private static final int STATE_MODE = 1;
	private static final int TIME_MODE = 2;
	private static final int I_MODE = 3; //???

	private static Cipher cipher;
	
	private static int mode;
	private static int inputFlag;
	private static boolean hasE, hasG;
	private static boolean hasName;
	private static String logName;
	private static List<String> argVal;
	private static Map<String, List<String>> params;

	// arg is supposedly a Flag of -T, -K, -E, -G, -R, -L, -B
	private static void checkFlag(final String arg, final String[] args) {
		switch (arg) {
		case "-T": //non-negative integer (ranging from 1 to 1,073,741,823 inclusively)
			// Cannot use difference modes at the same time
			if (mode != TIME_MODE && mode != DEFAULT_MODE) {
				printError(INVALID);
			}
			inputFlag = 0;
			mode = TIME_MODE;
			break;
		case "-K": //arbitrary-sized string of alphanumeric (a-z, A-Z, and 0-9) characters
			inputFlag = 1;
			break;
		case "-E": //alphabetic characters (a-z, A-Z) in upper and lower case. Names may not contain spaces
			// in logread cannot have multiple -G/-E. May need to change for -I
			if (hasG || hasE) {
				printError(INVALID);
			}
			hasE = true;
			inputFlag = 2;
			break;
		case "-G": //^^
			// in logread cannot have multiple -G/-E. May need to change for -I
			if (hasG || hasE) {
				printError(INVALID);
			}
			hasG = true;
			inputFlag = 3;
			break;
		case "-R": 
			// Cannot use difference modes at the same time
			if (mode != ROOM_MODE && mode != DEFAULT_MODE) {
				printError(INVALID);
			}
			inputFlag = 4;
			mode = ROOM_MODE;
			break;
		case "-S": 
			// Cannot use difference modes at the same time
			if (mode != STATE_MODE && mode != DEFAULT_MODE) {
				printError(INVALID);
			}
			inputFlag = 5;
			mode = STATE_MODE;
			break;
		case "-I":  
			// Cannot use difference modes at the same time
			if (mode != I_MODE && mode != DEFAULT_MODE) {
				printError(INVALID);
			}
			inputFlag = 6;
			System.out.println("unimplemented");
			System.exit(SUCCESS);
			break;
		default:
			printError(INVALID);
		}

		argVal = new ArrayList<>();
		params.put(arg, argVal);
		// If -T, -R, -S, -I no mappings need to be checked
		if (inputFlag == 0 || inputFlag == 4 || inputFlag == 5 || inputFlag == 6) {
			argVal = null;
		} 

	}

	private static void checkMapping(final String val) {
		boolean validMapping = false;
		switch (inputFlag) {
		case 1:
			// Valid mapping for -K means the value is a valid name (A-Z and a-z and 0-9)
			validMapping = val.matches("^([A-Za-z]|[0-9])+$");
			if (validMapping) {
				argVal.add(val);
				argVal = null;
			} else {
				printError(INTEGRITY);
			}
			break;
		case 2:
		case 3:
			// Valid mapping for -E | -G means the value is a valid name (A-Z and a-z)
			validMapping = val.matches("^([A-Za-z])+$");
			if (validMapping) {
				argVal.add(val);
				argVal = null;
			} else {
				printError(INVALID);
			}	
			break;
		default:
			printError(INVALID);
		}    	
	}

	private static void decrypt(File log) {
		try {
	        int ivSize = 16, keySize = 16;
	        // Read data into a buffer
	        FileInputStream inputStream = new FileInputStream(log);
	        byte[] encryptedData = new byte[(int) log.length()];
			inputStream.read(encryptedData);
			//System.out.println(encryptedData.length);
			
	        // Extract IV from the file
	        byte[] iv = new byte[ivSize];
	        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
	        IvParameterSpec ivSpec = new IvParameterSpec(iv);

	        // Extract encrypted information from the file
	        int encryptedSize = encryptedData.length - ivSize;
	        byte[] encryptedBytes = new byte[encryptedSize];
	        System.arraycopy(encryptedData, ivSize, encryptedBytes, 0, encryptedSize);
		    
	        // Hash key
		    byte[] key = params.get("-K").get(0).getBytes("UTF-8");
		    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.update(key);
	        byte[] keyBytes = new byte[keySize];
	        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
	        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		    
	        // Decrypt data with key
	        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
	        byte[] decrypted = cipher.doFinal(encryptedBytes);
	        
	        // Write clean data to file to be read
	        FileOutputStream outputStream = new FileOutputStream(log);
			outputStream.write(decrypted);
	        
			inputStream.close();
			outputStream.close();
			
		    } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
		             | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException e) {
		    	printError(INTEGRITY);
		    }
	}
	
	private static void encrypt(File log) {
		try {
			// Generate random IV
	        int ivSize = 16;
	        byte[] iv = new byte[ivSize];
	        SecureRandom random = new SecureRandom();
	        random.nextBytes(iv);
	        IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
		    // Hash key
		    byte[] key = params.get("-K").get(0).getBytes("UTF-8");
		    MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.update(key);
	        byte[] keyBytes = new byte[16];
	        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
	        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
		    
	        // Read file into buffer and encrypt into array
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
	        FileInputStream inputStream = new FileInputStream(log);
			byte[] inputBytes = new byte[(int) log.length()];
			inputStream.read(inputBytes);		
			byte[] encrypted = cipher.doFinal(inputBytes);
	        
	        // Encrypt with IV
	        byte[] encryptedFinal = new byte[ivSize + encrypted.length];
	        System.arraycopy(iv, 0, encryptedFinal, 0, ivSize);
	        System.arraycopy(encrypted, 0, encryptedFinal, ivSize, encrypted.length);
			//System.out.println(encryptedFinal.length);
	        
			FileOutputStream outputStream = new FileOutputStream(log);
			outputStream.write(encryptedFinal);
			
			inputStream.close();
			outputStream.close();
		
		    } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
		             | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException e) {
		    	printError(INVALID);
		    }
	}
	
	private static void printError(int code) {
		if (code == INVALID) {
			if (hasName && params.containsKey("-K")) {
				File log = new File(logName);
				encrypt(log);
			}
			System.out.println("invalid");
			System.exit(INVALID);
		} else {
			System.out.println("integrity violation");
			System.exit(INVALID);
		}
	}

	private static void readArgs(final String[] args) {
		// Everytime a new line of arguments is read, all booleans should be false,
		// and logName,argVal should be reset to null, inputFlag = DEFAULT_MODE
		inputFlag = DEFAULT_MODE;
		mode = DEFAULT_MODE;
		hasName = false;
		hasE = false;
		hasG = false; 
		logName = null;
		argVal = null;
		params = new HashMap<>();

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.charAt(0) == '-' && argVal == null) {
				Main.checkFlag(arg, args);
				// If -K, -E, -G there need to be at least something after it
				if (i + 1 >= args.length && (inputFlag == 1 || inputFlag == 2 || inputFlag == 3)) {
					printError(INVALID);
				}


			} else if (argVal != null) { //for any cmd input after each -{flag}
				Main.checkMapping(arg); //Checks if mapping is fine
			} else if (!hasName) { //anything not tied to -T/-R/-K/-E/-G is assumed to be <log> 
				//cannot input multiple log names ("last value accepted" does not apply)
				//may be specified with a string of alphanumeric characters (including underscores and periods) 
				//slashes and periods may be used to reference log files in other directories
				boolean validLogName = arg.matches("^[a-zA-Z0-9/._~]+$");
				if (validLogName) {
					logName = arg;
				} else {
					printError(INVALID);
				}
				hasName = true;
			} else {
				printError(INVALID);
			}
		}
		// Checks for valid format
		// All input must have a key and a log name
		if (params.containsKey("-K") && hasName) {
			// If -R or -T is specified, -E or -G must be present
			if (params.containsKey("-R") || params.containsKey("-T")) {
				if (!(params.containsKey("-E") || params.containsKey("-G"))) {
					printError(INVALID);
				}
				// If -S is specified, -E or -G cannot be present	
			} else if (params.containsKey("-S")){
				if (params.containsKey("-E") || params.containsKey("-G")) {
					printError(INVALID);
				}
				// If none of the above for some reason, invalid
			} else {
				printError(INVALID);
			}
		} else {
			printError(INVALID);
		}	
	}

	public static void main(String [] args) {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			printError(INVALID);
		}
		readArgs(args);
		Main m = new Main();
		Main.Visitor v = m.new Visitor();
		v.getInfo();
		System.exit(SUCCESS);
	}
}