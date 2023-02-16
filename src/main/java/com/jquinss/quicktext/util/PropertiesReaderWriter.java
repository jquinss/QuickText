package com.jquinss.quicktext.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesReaderWriter {
	private final File fileName;
	private final String keyValueSeparator;
	private final String regex;
	private final Pattern keyValuePattern;
	
	public PropertiesReaderWriter(File fileName, String keyValueSeparator) {
		this.fileName = fileName;
		this.keyValueSeparator = keyValueSeparator;
		this.regex = "(?<key>[\\p{ASCII}]+)" + this.keyValueSeparator + "(?<value>[\\p{ASCII}]+)";
		keyValuePattern = Pattern.compile(regex);
	}
	
	public PropertiesReaderWriter(String fileName, String keyValueSeparator) {
		this(new File(fileName), keyValueSeparator);
	}
	
	public Properties readProperties() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
			String line;
			
			while ((line = bufferedReader.readLine()) != null) {
				Matcher matcher = this.keyValuePattern.matcher(line);
				if (matcher.matches()) {
					props.setProperty(matcher.group("key"), matcher.group("value"));
				}
			}
		}
		catch (FileNotFoundException e) {
			throw e;
		}
		catch (IOException e) {
			throw e;
		}
		
		return props;
	}
	
	public void writeProperties(Properties props) throws IOException {
		try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));) {
			@SuppressWarnings("unchecked")
			Enumeration<String> enumeration = (Enumeration<String>) props.propertyNames();
			
			while (enumeration.hasMoreElements()) {
				String key = enumeration.nextElement();
				String value = props.getProperty(key);
				printWriter.println(key + keyValueSeparator + value);
			}
		}
		catch (IOException e) {
			throw e;
		}
	}
}
