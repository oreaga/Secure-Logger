package src.LogRead;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
	public static ArrayList<HashMap<String, String>> read(String key, String filePath) {
		File file = new File(filePath);
		
		if (!file.exists()) {
			return new ArrayList<HashMap<String, String>>();
		}
		
		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		ArrayList<HashMap<String, String>> results = new ArrayList<>();
		
		try {
			String fileHash = buf.readLine();
			if (!BCrypt.checkpw(key, fileHash)) {
				System.out.println("invalid");
				System.exit(255);
			}
			
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes("UTF-8"));
			byte[] finalKey = digest.digest();
			SecretKeySpec secretKey = new SecretKeySpec(finalKey, "AES");
			
			byte[] paddedKey = Arrays.copyOf(key.getBytes("UTF-8"), 16);
			IvParameterSpec ivSpec = new IvParameterSpec(paddedKey);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			
			String line;
			HashMap<String, String> lineMap;
			while ((line = buf.readLine()) != null) {
				byte[] decodedBytes = Base64.getDecoder().decode(line);
			    byte[] decipheredBytes = cipher.doFinal(decodedBytes);
			    String decipheredText = new String(decipheredBytes);
			    
			    String[] split = decipheredText.split(" ");
			    
			    if (split.length > 5) {
			    	System.out.println("integrity violation");
			    	System.exit(255);
			    }
			    lineMap = new HashMap<String, String>();
			    
			    lineMap.put("time", split[0]);
			    lineMap.put("status", split[1]);
			    lineMap.put("role", split[2]);
			    lineMap.put("name", split[3]);
			    
			    if (split.length == 5) {
			    	lineMap.put("room", split[4]);
			    }
			    
			    results.add(lineMap);
			}
		} catch (IOException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		
		return results;
		
	}
	
	public static void write (HashMap<String, String> line, String key, String filePath) {
		try {
			File file = new File(filePath);
			boolean addKey = false;
			
			if (!file.exists()) {
				file.createNewFile();
				addKey = true;
			} else {
				BufferedReader buf = null;
				try {
					buf = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} 
				
				String fileHash = buf.readLine();
				if (!BCrypt.checkpw(key, fileHash)) {
					System.out.println("integrity violation");
					System.exit(255);
				}
			}
			
			String hashKey = BCrypt.hashpw(key, BCrypt.gensalt());
			PrintWriter printWriter = new PrintWriter(new FileOutputStream(file, true));
			
			if (addKey) {
				printWriter.println(hashKey);
			}
			
			String noRoom = line.get("time") + " " + line.get("status") + " " + line.get("role") + " " + line.get("name");
			
			String toEncrypt;
			if (line.get("room") != null) {
				toEncrypt = noRoom + " " + line.get("room");
			} else {
				toEncrypt = noRoom;
			}
			
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes("UTF-8"));
			byte[] finalKey = digest.digest();
			SecretKeySpec secretKey = new SecretKeySpec(finalKey, "AES");
			
			byte[] paddedKey = Arrays.copyOf(key.getBytes("UTF-8"), 16);
			IvParameterSpec ivSpec = new IvParameterSpec(paddedKey);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			
			byte[] encryptedBytes = cipher.doFinal(toEncrypt.getBytes("UTF-8"));
			String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
			
			printWriter.println(encryptedText);
			printWriter.close();
		} catch (FileNotFoundException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("integrity violation");
			System.exit(255);
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
}
