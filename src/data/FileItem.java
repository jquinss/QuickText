package data;

import java.io.File;

public abstract class FileItem {
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
	
	public File getFile() {
		return file;
	}
}
