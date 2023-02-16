package com.jquinss.quicktext.data;

import java.io.File;
import java.io.Serializable;

public abstract class FileItem implements Serializable {
	File file;
	String description = "";
	
	public FileItem(File file) {
		this.file = file;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
}
