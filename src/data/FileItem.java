package data;

import java.io.File;

public class FileItem {
	private File file;
	private String description;
	private static final String PLAIN_TEXT_EXT = ".txt";
	private static final String HTML_EXT = ".html";
	private boolean isFile = false;
	private boolean isDirectory = false;
	private boolean isRootDirectory = false;
	private boolean isPlainTextTemplate = false;
	private boolean isHTMLTemplate = false;
	
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
			setFileSubType();
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
	
	private void setFileSubType() {
		String fileName = file.toString();
		int txtExtLength = PLAIN_TEXT_EXT.length();
		int htmlExtLength = HTML_EXT.length();
		int fileLength = fileName.length();
		
		if (fileName.regionMatches(true, fileLength - txtExtLength, PLAIN_TEXT_EXT, 0, txtExtLength)) {
			isPlainTextTemplate = true;
		}
		else if (fileName.regionMatches(true, fileLength - htmlExtLength, HTML_EXT, 0, htmlExtLength)) {
			isHTMLTemplate = true;
		}
	}
	
	public boolean isPlainTextTemplate() {
		return isPlainTextTemplate;
	}
	
	public boolean isHTMLTemplate() {
		return isHTMLTemplate;
	}
}
