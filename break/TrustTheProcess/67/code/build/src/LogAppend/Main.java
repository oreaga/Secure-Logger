// A basic hello world program
package src.LogAppend;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Main {
	private class Visitor {
		String name;
		Boolean arrival;
		Boolean employee; 
		//int lastRoom;
		int currRoomID;
		int currTime;
		// Maps most recent line of data from file. 
		HashMap<String, String> prevLine = new HashMap<String, String>(); 

		private void appendInfo() {
			// Check if file exists
			File logFile = new File(logName);
			if (logFile.isFile()) { // Log File already exists..
				//decrypt log file here
				decrypt(logFile);
				if (!batchError) {
					populateVisitor();
					// If there is previous data...
					if (getPrevLine()) {
						writeToLog(OLD_USER,logFile);
					} else { // otherwise add as new user
						writeToLog(NEW_USER,logFile);
					}
					//encrypt log file here
					if (!batchError)
						encrypt(logFile);
				}
			} else { // new Log File needs to be created
				populateVisitor();
				writeToLog(NEW_LOG,logFile);
				//encrypt log file here
				encrypt(logFile);
			}  
		}

		private boolean getPrevLine() {
			/// Format for each line: Name,employee,roomID,arrival,time
			boolean success = false;
			String [] prev;
			try {
				BufferedReader input = new BufferedReader(new FileReader(logName));
				String line;
				lastTimeEntry = Integer.MAX_VALUE;
				while ((line = input.readLine()) != null) {
					prev = line.split(",");
					// must be equal name and employee type
					if (prev[0].equals(name) && prev[1].equals(employee.toString())) {
						prevLine.put("name", name);
						prevLine.put("employee", prev[1]);
						prevLine.put("roomID", prev[2]);
						prevLine.put("arrival", prev[3]);
						prevLine.put("time", prev[4]);
						success = true;
					} 
					//lastRoom = Integer.parseInt(prev[2]);
					lastTimeEntry = Integer.parseInt(prev[4]);
				}
				input.close();
			} catch (IOException e) {
				printError();
			}
			return success;
		}

		private void populateVisitor() {
			employee = params.containsKey("-E") ? true : false; 
			name = employee ? params.get("-E").get(0) : params.get("-G").get(0);
			// Check if currRoomId is valid
			currRoomID = params.containsKey("-R") ? Integer.parseInt(params.get("-R").get(0)) : GALLERY;
			currTime = Integer.parseInt(params.get("-T").get(0));
			arrival = params.containsKey("-A");
		}

		private void writeToLog(int mode, File logFile) {
			switch(mode) {
			// For a new LOG: room MUST BE GALLERY and MUST BE ARRIVAL
			case NEW_LOG:
				if (currRoomID == GALLERY && arrival) {
					try {
						logFile.createNewFile();
						PrintWriter writer = new PrintWriter(new FileWriter(logName));
						writer.println(name + ',' + employee + ',' + currRoomID + ',' + arrival + ',' + currTime);
						writer.close();
					} catch (IOException e) {
						printError();
					}

				} else {
					printError();
				}
				break;
				// For a new USER: room MUST BE GALLERY, MUST BE ARRIVAL, time validation
			case NEW_USER:
				if (currRoomID == GALLERY && arrival && lastTimeEntry < currTime) {
					try {
						PrintWriter writer = new PrintWriter(new FileWriter(logName, true));
						writer.println(name + ',' + employee + ',' + currRoomID + ',' + arrival + ',' + currTime);
						writer.close();
					} catch (IOException e) {
						printError();
					}

				} else {
					printError();
				}
				break;

			case OLD_USER:
				//handle if they leave gallery and enter again?
				if (lastTimeEntry < currTime) {
					int lastRoom = Integer.parseInt(prevLine.get("roomID"));

					if (params.containsKey("-A")) { 
						if (lastRoom > GALLERY) {
							printError();
						} else if (lastRoom == GALLERY && !params.containsKey("-R")) {
							printError();
						}
					} else if (params.containsKey("-L")) { // case where user tries to leave gallery without entering is valid
						if (lastRoom > GALLERY) { // if currently in a room
							if (lastRoom != Integer.parseInt(params.get("-R").get(0))) {
								printError();
							} 
							currRoomID = GALLERY;
						} else if (lastRoom == GALLERY) { // if in gallery and not in room
							//can't leave room haven't entered at this point
							if (params.containsKey("-R")) {
								printError();
							}
							currRoomID = OUTSIDE; 
						} else { 
							printError();
						}
					}

					if (!batchError) {
						try {
							PrintWriter writer = new PrintWriter(new FileWriter(logName, true));
							writer.println(name + ',' + employee + ',' + currRoomID + ',' + arrival + ',' + currTime);
							writer.close();
						} catch (IOException e) {
							printError();
						}
					}
				} else {
					printError();
				}
				break;
			}
		}

	}
	private static final int MAX_VALUE = 1073741823;
	
	private static final int INVALID = 255;
	private static final int SUCCESS = 0;
	
	private static final int GALLERY = -2;
	private static final int OUTSIDE = -12;

	private static final int NEW_LOG = 0;
	private static final int NEW_USER = 1;
	private static final int OLD_USER = 2;
	// For Visitor Class
	private static int lastTimeEntry;
	//private static int lastRoom;
	private static Cipher cipher;
	// For Main
	private static int inputFlag;
	private static boolean batchError = false;
	private static boolean batchFlag = false;
	private static boolean hasE, hasG, hasA, hasL;
	private static boolean hasName;
	private static boolean wrongKey = false;
	private static String logName;
	private static List<String> argVal;
	private static Map<String, List<String>> params;

	// arg is supposedly a Flag of -T, -K, -E, -G, -R, -L, -B
	private static void checkFlag(final String arg, final String[] args) {
		switch (arg) {
		case "-T": //non-negative integer (ranging from 1 to 1,073,741,823 inclusively)
			inputFlag = 0;
			break;
		case "-K": //arbitrary-sized string of alphanumeric (a-z, A-Z, and 0-9) characters
			inputFlag = 1;
			break;
		case "-E": //alphabetic characters (a-z, A-Z) in upper and lower case. Names may not contain spaces
			hasE = true;
			if (hasG) {
				printError();
			}
			inputFlag = 2;
			break;
		case "-G": //^^
			hasG = true;
			if (hasE) {
				printError();
			}
			inputFlag = 3;
			break;
		case "-R": //non-negative integer characters with no spaces (ranging from 0 to 1,073,741,823 inclusively). 
			//Leading zeros in room IDs should be dropped, such that 003, 03, and 3 are all equivalent room IDs
			inputFlag = 4;
			break;
		case "-A":
			hasA = true;
			if (hasL) {
				printError();
			}
			inputFlag = 5;
			break;
		case "-L":
			hasL = true;
			if (hasA) {
				printError();
			}
			inputFlag = 5;
			break;
		case "-B":
			// If params has other keys than -B, printError
			if (params.keySet().size() > 1) {
				printError();
			}
			inputFlag = 24;
			break;
		default:
			printError();
		}
		if (!batchError) {
			argVal = new ArrayList<>();
			params.put(arg, argVal);
			// If -A | -L, no mappings need to be checked
			if (inputFlag == 5) {
				argVal = null;
			} 
		}
	}
	// arg -> val, variable called val becuase arg should now supposedly be the value after a flag
	private static void checkMapping(final String val) {
		boolean validMapping = false;
		switch (inputFlag) {
		case 0:
			try {
				Integer num = Integer.parseInt(val);
				// Valid mapping for -T means the value is a valid integer
				validMapping = num >= 1 && num <= MAX_VALUE;
			} catch (NumberFormatException e) {
				printError();
			}
			break;
		case 1:
			// Valid mapping for -K means the value is a valid name (A-Z and a-z and 0-9)
			validMapping = val.matches("^([A-Za-z]|[0-9])+$");
			break;
		case 2:
		case 3:
			// Valid mapping for -E | -G means the value is a valid name (A-Z and a-z)
			validMapping = val.matches("^([A-Za-z])+$");
			break;
		case 4:
			try {
				Integer num = Integer.parseInt(val);
				// Valid mapping for -R means the value is a valid integer
				validMapping = num >= 0 && num <= MAX_VALUE;
			} catch (NumberFormatException e) {
				printError();
			}
			break;
		case 24:
			// If in batch mode, do nothing. At the end it will add to this new arraylist that isn't referenced
			if (batchFlag) {
				batchError = true;
			} else {
				File file = new File(val);
				readBatch(file);
				argVal = params.get("-B");
			}
			// Valid mapping for -B means the file was opened and its contents are valid
			// If it gets to this point, the statement above is true
			validMapping = true;
			break;
		}

		if (validMapping && !batchError) {
			argVal.add(val);
			argVal = null;
		} else {
			printError();
		}	    	
	}

	private static void printError() {
		if (batchFlag) {
			if (!wrongKey && hasName) {
				File log = new File(logName);
				encrypt(log);
			}
			System.out.println("invalid");
			batchError = true;
		} else {
			if (!wrongKey && hasName && params.containsKey("-K")) {
				File log = new File(logName);
				encrypt(log);
			}
			System.out.println("invalid");
			System.exit(INVALID);
		}	
	}

	private static void readArgs(final String[] args) {
		// Everytime a new line of arguments is read, all booleans should be false,
		// and logName,argVal should be reset to null, inputFlag = -1
		inputFlag = -1;
		hasName = false;
		hasE = false;
		hasG = false; 
		hasA = false; 
		hasL = false;
		logName = null;
		argVal = null;
		params = new HashMap<>();

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (arg.charAt(0) == '-' && argVal == null) {
				Main.checkFlag(arg, args);
				if (i + 1 >= args.length && inputFlag != 5) {
					printError();
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
					printError();
				}
				hasName = true;
			} else {
				printError();
			}
			
			if (batchError) {
				break;
			}
		}
		// if params doesnt have -B, it must be other format
		if (!params.containsKey("-B") && !batchError) {
			// params must have -T, -K, -E|-G, -A|-L, logName. -R is optional
			// Checks for valid format
			if (!(params.containsKey("-T") && params.containsKey("-K") &&
					(params.containsKey("-E") || params.containsKey("-G")) &&
					(params.containsKey("-A") || params.containsKey("-L")) && hasName) ) {
				printError();
			}
		}
	}
	
	private static void decrypt(File log) {
		try {
	        int ivSize = 16, keySize = 16;
	        // Read data into a buffer
	        FileInputStream inputStream = new FileInputStream(log);
	        byte[] encryptedData = new byte[(int) log.length()];
			inputStream.read(encryptedData);
//			System.out.println(encryptedData.length);
			
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
		    	wrongKey = true;
		    	printError();
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
//			System.out.println(encryptedFinal.length);
	        
			FileOutputStream outputStream = new FileOutputStream(log);
			outputStream.write(encryptedFinal);
			
			inputStream.close();
			outputStream.close();
		
		    } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
		             | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException e) {
		    	wrongKey = true;
		    	printError();
		    }
	}
	
	private static void readBatch(File file) {
		batchFlag = true;
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			// read/parse line by line
			while ((line = bufferedReader.readLine()) != null) {
				batchError = false;
				// split line into args, check each one
				String[] args = line.split("\\s");
				readArgs(args);
				if (!batchError) {
					Main m = new Main();
					Main.Visitor v = m.new Visitor();
					v.appendInfo();
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			printError();
		}
		batchFlag = false;
		System.exit(SUCCESS);
	}

	public static void main(String [] args) {		
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			printError();
		}
		readArgs(args);
		Main m = new Main();
		Main.Visitor v = m.new Visitor();
		v.appendInfo();
		System.exit(SUCCESS);
		
	}
}