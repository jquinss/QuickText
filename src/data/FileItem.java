package data;

import java.io.File;

public class FileItem {
	private File file;
	private String description;
	private boolean isFile = false;
	private boolean isDirectory = false;
	private boolean isRootDirectory = false;
	
	public FileItem(File file) {
		this.file = file;
		setFileType();
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setIsRoot(boolean isRootDirectory) {
		this.isRootDirectory = isRootDirectory;
	}
	
	public String getDescription() {
		return description;
	}
	
	private void setFileType() {
		if (file.isFile()) {
			isFile = true;
		}
		else if (file.isDirectory()) {
			isDirectory = true;
		}
	}
	
	public boolean isFile() {
		return isFile;
	}
	
	public boolean isDirectory() {
		return isDirectory;
	}
	
	public boolean isRootDirectory() {
		return isRootDirectory;
	}
}
