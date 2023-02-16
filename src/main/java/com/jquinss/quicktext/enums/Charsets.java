package com.jquinss.quicktext.enums;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public enum Charsets {
	ISO_8859_1, US_ASCII, UTF_8, UTF_16, UTF_16BE, UTF_16LE;
	
	@Override
	public String toString() {
		return switch (this) {
			case ISO_8859_1 -> StandardCharsets.ISO_8859_1.displayName();
			case US_ASCII -> StandardCharsets.US_ASCII.displayName();
			case UTF_8 -> StandardCharsets.UTF_8.displayName();
			case UTF_16 -> StandardCharsets.UTF_16.displayName();
			case UTF_16BE -> StandardCharsets.UTF_16BE.displayName();
			case UTF_16LE -> StandardCharsets.UTF_16LE.displayName();
			default -> throw new IllegalArgumentException();
		};
	}
	
	public Charset toStandardCharset() {
		return switch (this) {
			case ISO_8859_1 -> StandardCharsets.ISO_8859_1;
			case US_ASCII -> StandardCharsets.US_ASCII;
			case UTF_8 -> StandardCharsets.UTF_8;
			case UTF_16 -> StandardCharsets.UTF_16;
			case UTF_16BE -> StandardCharsets.UTF_16BE;
			case UTF_16LE -> StandardCharsets.UTF_16LE;
			default -> throw new IllegalArgumentException();
		};
	}
	
	public static HashMap<String, Charsets> getCharsetsHashMap(){
		HashMap<String, Charsets> hashMap = new HashMap<String, Charsets>();
		for (Charsets charset : values()) {
			hashMap.put(charset.toString(), charset);
		}
		
		return hashMap;
	}
}