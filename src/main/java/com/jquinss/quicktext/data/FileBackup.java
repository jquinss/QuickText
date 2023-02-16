package com.jquinss.quicktext.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;

import com.jquinss.quicktext.util.HashCalculator;

public class FileBackup implements Serializable {
	private static final long serialVersionUID = 5961329031476444943L;
	private final File file;
	private final String fileSHA256;
	
	public FileBackup(File file) throws FileNotFoundException, IOException {
		this.file = file;
		fileSHA256 = getFileSHA256();
	}
	
	public File getFile() {
		return file;
	}
	
	public String getOrigFileSHA256() {
		return fileSHA256;
	}
	
	public String getFileSHA256() throws FileNotFoundException, IOException {
		String fileSHA256 = null;
		try {
			fileSHA256 = HashCalculator.calculateFileHash("SHA-256", file.toString());
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return fileSHA256;
	}
	
	public FileTime getCreationTime() throws IOException {
		return (FileTime) Files.getAttribute(file.toPath(), "basic:creationTime");
	}
}
