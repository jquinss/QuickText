package com.jquinss.quicktext.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.jquinss.quicktext.enums.Result;
import com.jquinss.quicktext.util.Observable;
import com.jquinss.quicktext.util.Observer;
import com.jquinss.quicktext.util.ZipUtil;

public class BackupTask implements Observable, Runnable, Serializable {
	private static final long serialVersionUID = 7024037453754683118L;
	private final String sourceFolder;
	private final String destZipFile;
	private final String[] ignoredFiles;
	private FileBackup fileBackup;
	private Result backupResult = Result.NOT_AVAILABLE;
	private Observer observer;
	
	public BackupTask(String sourceFolder, String destZipFile, String[] ignoredFiles) {
		this.sourceFolder = sourceFolder;
		this.destZipFile = destZipFile;
		this.ignoredFiles = ignoredFiles;
	}
	
	public FileBackup getFileBackup() {
		return fileBackup;
	}
	
	public Result getBackupResult() {
		return backupResult;
	}
	
	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	public String getSourceFolder() {
		return sourceFolder;
	}
	
	public String getDestZipFile() {
		return destZipFile;
	}
	
	public String[] getIgnoredFiles() {
		return ignoredFiles;
	}
	
	@Override
	public void run() {
		try {;
			ZipUtil.zipFolder(sourceFolder, destZipFile, ignoredFiles);
			fileBackup = new FileBackup(new File(destZipFile));
			backupResult = Result.SUCCESS;
		} catch (IOException e) {
			backupResult = Result.FAIL;
		} finally {
			notifyObserver();
		}
	}

	@Override
	public void notifyObserver() {
		if (observer != null) {
			observer.update(this);
		}
	}

}
