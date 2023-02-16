 package com.jquinss.quicktext.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledFuture;

import com.jquinss.quicktext.enums.Recurrence;
import com.jquinss.quicktext.enums.Result;
import com.jquinss.quicktext.exceptions.InvalidDateTimeException;
import com.jquinss.quicktext.util.Observable;
import com.jquinss.quicktext.util.Observer;
import com.jquinss.quicktext.util.Schedule;

public class ScheduledBackupTask implements Observer, Observable, Serializable {
	private static final long serialVersionUID = 5396454915229312534L;
	private final Recurrence recurrence;
	private final ZoneOffset zoneOffset;
	private Result lastResult = Result.NOT_AVAILABLE;
	private long nextRun;
	private long lastRun;
	private final Schedule schedule;
	private BackupTask backupTask;
	// declare variables as transient to prevent them from being serialized
	private transient ScheduledFuture<?> scheduledFuture;
	private transient Observer observer;
	
	public ScheduledBackupTask(Schedule schedule, BackupTask backupTask) throws InvalidDateTimeException {
		this.schedule = schedule;
		this.backupTask = backupTask;
		recurrence = schedule.getRecurrence();
		nextRun = schedule.getNextRun();
		lastRun = schedule.getLastRun();
		zoneOffset = schedule.getZoneOffset();
	}
	
	public long getNextRun() {
		return nextRun;
	}
	
	public long getLastRun() {
		return lastRun;
	}
	
	public Recurrence getRecurrence() {
		return recurrence;
	}
	
	public ZoneOffset getZoneOffset() {
		return zoneOffset;
	}
	
	public Result getLastResult() {
		return lastResult;
	}
	
	public Observer getObserver() {
		return observer;
	}

	@Override
	public void update(Observable observable) {
		// as soon as the BackupTask (observable) is finished, it gets the result, updates the schedule and notifies its own observer (BackupManager)
		BackupTask backupTask = (BackupTask) observable;
		lastResult = backupTask.getBackupResult();
		updateSchedule();
		notifyObserver();
	}
	
	public BackupTask getBackupTask() {
		return backupTask;
	}
	
	public BackupTask createBackupTask() {
		backupTask = new BackupTask(backupTask.getSourceFolder(), backupTask.getDestZipFile(), backupTask.getIgnoredFiles());
		backupTask.setObserver(this);
		
		return backupTask;
	}
	
	public void updateSchedule() {
		if (LocalDateTime.ofEpochSecond(nextRun, 0, zoneOffset).isBefore(LocalDateTime.now())) {
			nextRun = schedule.getNextRun();
			lastRun = schedule.getLastRun();
		}
	}

	@Override
	public void notifyObserver() {
		if (observer != null) {
			observer.update(this);
		}
	}
	
	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
		this.scheduledFuture = scheduledFuture;
	}
	
	public void cancelBackupTask() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
	}
}
