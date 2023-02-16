package com.jquinss.quicktext.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;

import com.jquinss.quicktext.data.FileBackup;

public class FileBackupDateComparator implements Comparator<FileBackup> {
	@Override
	public int compare(FileBackup file1, FileBackup file2) {
		Path path1 = Paths.get(file1.getFile().toString());
		Path path2 = Paths.get(file2.getFile().toString());
		
		FileTime creationTimeFile1 = null;
		FileTime creationTimeFile2 = null;
		
		try {
			creationTimeFile1 = (FileTime) Files.getAttribute(path1, "creationTime");
			creationTimeFile2 = (FileTime) Files.getAttribute(path2, "creationTime");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return creationTimeFile1.compareTo(creationTimeFile2);
	}
}