package com.jquinss.quicktext.util;

import java.io.File;
import java.io.IOException;

import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.data.FolderItem;
import com.jquinss.quicktext.data.HTMLTemplateItem;
import com.jquinss.quicktext.data.PlainTextTemplateItem;
import com.jquinss.quicktext.data.RootFolderItem;

public class FileItemBuilder {
	private static final String HTML_TEXT_EXT = ".html";
	private static final String PLAIN_TEXT_EXT = ".txt";
	
	private final File root;
	private final FileManager fileManager = new FileManager();
	
	public FileItemBuilder(File root) {
		this.root = root;
	}
	
	public FileItem buildFileItem(File file) {
		FileItem fileItem = null;
		
		try {
			if (file.getCanonicalPath().equals(root.getCanonicalPath())) {
				fileItem = new RootFolderItem(file);
			}
			else if (file.isDirectory()) {
				fileItem = new FolderItem(file);
			}
			else if (file.isFile()) {
				if (fileManager.getExtensionFromFile(file.toString()).equalsIgnoreCase(HTML_TEXT_EXT)) {
					fileItem = new HTMLTemplateItem(file);
				}
				else if (fileManager.getExtensionFromFile(file.toString()).equalsIgnoreCase(PLAIN_TEXT_EXT)) {
					fileItem = new PlainTextTemplateItem(file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileItem;
	}
}
