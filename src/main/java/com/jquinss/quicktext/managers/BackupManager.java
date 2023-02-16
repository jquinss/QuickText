package com.jquinss.quicktext.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jquinss.quicktext.data.BackupTask;
import com.jquinss.quicktext.data.FileBackup;
import com.jquinss.quicktext.data.ScheduledBackupTask;
import com.jquinss.quicktext.enums.Result;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.jquinss.quicktext.util.FileBackupDateComparator;
import com.jquinss.quicktext.util.Observable;
import com.jquinss.quicktext.util.Observer;

public class BackupManager implements Observer {
	private final ObservableList<FileBackup> backupsObsList = FXCollections.observableArrayList();
    private final ObservableList<ScheduledBackupTask> scheduledBackupTasksObsList = FXCollections.observableArrayList();
    
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
    
    public ObservableList<FileBackup> getBackupsObsList() {
    	return backupsObsList;
    }
    
    public ObservableList<ScheduledBackupTask> getScheduledBackupTasksObsList() {
    	return scheduledBackupTasksObsList;
    }
    
	public void loadBackupFiles(String fileName) {
		backupsObsList.clear();
    	try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(fileName))) {
    		@SuppressWarnings("unchecked")
			List<FileBackup> backupData = (ArrayList<FileBackup>) input.readObject();
    		
    		for (FileBackup file : backupData) {
    			// only import files that exist and had not been modified
    			if (file.getFile().exists() && file.getFileSHA256().equals(file.getOrigFileSHA256())) {
    				backupsObsList.add(file);
    			}
    		}
    	}
    	catch (FileNotFoundException e) {
    		// if file does not exist, ignore the error
    	}
    	catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void saveBackupFiles(String fileName) {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName))) {
			output.writeObject(new ArrayList<FileBackup>(backupsObsList));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadScheduledBackupTasks(String fileName) {
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(fileName))) {
			scheduledBackupTasksObsList.addAll((List<ScheduledBackupTask>) input.readObject());
    	}
    	catch (FileNotFoundException e) {
    		// if file does not exist, ignore the error
    	}
    	catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void saveScheduledBackupTasks(String fileName) {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName))) {
			output.writeObject(new ArrayList<ScheduledBackupTask>(scheduledBackupTasksObsList));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createBackup(String sourceFolder, String destZipFile, String[] ignoredFiles) {
		BackupTask backupTask = new BackupTask(sourceFolder, destZipFile, ignoredFiles);
		backupTask.run();
    	
    	if (backupTask.getBackupResult() == Result.SUCCESS) {
    		backupsObsList.add(backupTask.getFileBackup());
    		deleteOldestBackups(Integer.parseInt((SettingsManager.getInstance().getBackupMaxItems())));
    	}
	}
	
	public void deleteBackup(FileBackup fileBackup) {
		try {
			Files.deleteIfExists(fileBackup.getFile().toPath());
			backupsObsList.remove(fileBackup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteScheduledBackupTask(ScheduledBackupTask scheduledBackupTask) {
		scheduledBackupTask.cancelBackupTask();
		scheduledBackupTasksObsList.remove(scheduledBackupTask);
	}
	
	public void scheduleBackupTask(ScheduledBackupTask scheduledBackupTask) {
		scheduleBackupTask(scheduledBackupTask, getScheduledDateTimeInSeconds(scheduledBackupTask.getNextRun(), scheduledBackupTask.getZoneOffset()), TimeUnit.SECONDS);
	}
	
	public void scheduleBackupTasks() {
		for (ScheduledBackupTask scheduledBackupTask : scheduledBackupTasksObsList) {
			scheduleBackupTask(scheduledBackupTask, getScheduledDateTimeInSeconds(scheduledBackupTask.getNextRun(), scheduledBackupTask.getZoneOffset()), TimeUnit.SECONDS);
		}
	}
	
	private void scheduleBackupTask(ScheduledBackupTask scheduledBackupTask, long scheduledDateTimeInSeconds, TimeUnit timeUnit) {
		BackupTask backupTask = scheduledBackupTask.createBackupTask();
		if (scheduledBackupTask.getObserver() == null) {
			scheduledBackupTask.setObserver(this);
		}
		
		if (!scheduledBackupTasksObsList.contains(scheduledBackupTask)) {
			scheduledBackupTasksObsList.add(scheduledBackupTask);
		}
		
		ScheduledFuture<?> scheduledFuture = scheduledExecutor.schedule(backupTask, scheduledDateTimeInSeconds, timeUnit);
		scheduledBackupTask.setScheduledFuture(scheduledFuture);
	}
	
	public void cancelScheduledBackupTask(ScheduledBackupTask scheduledBackupTask) {
		scheduledBackupTask.cancelBackupTask();
		scheduledBackupTasksObsList.remove(scheduledBackupTask);
	}

	@Override
	public void update(Observable observable) {
		ScheduledBackupTask scheduledBackupTask = (ScheduledBackupTask) observable;
		// if the backup task was successful, we add the FileBackup object created to the list of FileBackup objects created
		if (scheduledBackupTask.getLastResult() == Result.SUCCESS) {
			backupsObsList.add(scheduledBackupTask.getBackupTask().getFileBackup());
			deleteOldestBackups(Integer.parseInt((SettingsManager.getInstance().getBackupMaxItems())));
		}
		
		LocalDateTime scheduledDateTime = LocalDateTime.ofEpochSecond(scheduledBackupTask.getNextRun(), 0, scheduledBackupTask.getZoneOffset());
		// if the new scheduled date/time is in the future, we send the schedule the backuptask for execution
		if (scheduledDateTime.isAfter(LocalDateTime.now())) {
			scheduleBackupTask(scheduledBackupTask, getScheduledDateTimeInSeconds(scheduledBackupTask.getNextRun(), scheduledBackupTask.getZoneOffset()), TimeUnit.SECONDS);
		}
	}
	
	public void cancelScheduledBackupTasks() {
		for (ScheduledBackupTask scheduledBackupTasks : scheduledBackupTasksObsList) {
			scheduledBackupTasks.cancelBackupTask();
		}
	}
	
	public void shutdownScheduledExecutor() {
		scheduledExecutor.shutdown();
	}
	
	private long getScheduledDateTimeInSeconds(long scheduledEpochSecond, ZoneOffset zoneOffset) {
		return scheduledEpochSecond - LocalDateTime.now().toEpochSecond(zoneOffset);
	}
	
    // deletes the oldest backup files up to the maximum number of files specified
    public void deleteOldestBackups(int maxNumBackups) {
    	backupsObsList.sort(new FileBackupDateComparator());
    	while (backupsObsList.size() > maxNumBackups) {
    		deleteBackup(backupsObsList.get(0));
    	}
    }
}
