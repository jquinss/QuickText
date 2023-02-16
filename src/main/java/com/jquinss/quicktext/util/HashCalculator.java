package com.jquinss.quicktext.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {
	private static final int BUFFER_SIZE = 4096;
	
	private HashCalculator() { }
	
	// calculates Base64 file hash
	public static String calculateFileHash(String hashType, String fileName) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int count;
		MessageDigest digest = MessageDigest.getInstance(hashType);
		
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
			while ((count = inputStream.read(buffer)) > 0) {
		        digest.update(buffer, 0, count);
		    }
		}
		
		byte[] hash = digest.digest();
		
		// converts hash to Base64
		StringBuilder sb = new StringBuilder();
		for (int i=0; i< hash.length; i++) {
			sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		return sb.toString();
	}
}
