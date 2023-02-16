package com.jquinss.quicktext.util;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.jquinss.quicktext.enums.Recurrence;
import com.jquinss.quicktext.exceptions.InvalidDateTimeException;

public class Schedule implements Serializable {
	private static final long serialVersionUID = -8915113489567988115L;
	private long lastRun;  // last scheduled date/time in seconds since epoch time
	private long nextRun;  // next scheduled date/time in seconds since epoch time
	private boolean isFirstRun = true; // indicates if it is the first time the task runs
	private final long frequency;
	private final Recurrence recurrence;
	private final ZoneOffset zoneOffset;
	
	public Schedule(LocalDateTime scheduledDateTime, Recurrence recurrence, int frequency, ZoneOffset zoneOffset) throws InvalidDateTimeException {
		LocalDateTime currentDateTime = LocalDateTime.now();
		if (scheduledDateTime.isBefore(currentDateTime)) {
			// throws an exception if the scheduled date/time is in the past
			throw new InvalidDateTimeException("Invalid date. Date " + scheduledDateTime + " is before date " + currentDateTime);
		}
		this.zoneOffset = zoneOffset;
		this.nextRun = scheduledDateTime.toEpochSecond(zoneOffset);
		this.recurrence = recurrence;
		this.frequency = frequency;
	}
	
	public long getNextRun() {
		if (recurrence != Recurrence.NO_RECURRENT && isFirstRun != true) {
			// this applies in situations when the task is recurrent and is not the first time it runs
			lastRun = nextRun;
			LocalDateTime currentDateTime = LocalDateTime.now();
			LocalDateTime nextDateTime = LocalDateTime.ofEpochSecond(nextRun, 0, ZoneOffset.UTC);
			if (nextDateTime.isBefore(currentDateTime)) {
				nextRun = calculateNextRunFromPastDateTime(nextDateTime, recurrence, frequency).toEpochSecond(zoneOffset);
			}
			else {
				nextRun = calculateNextRun(nextDateTime, recurrence, frequency).toEpochSecond(zoneOffset);
			}
		}
		else if (recurrence == Recurrence.NO_RECURRENT && isFirstRun != true) {
			// this applies in situations when the task is not recurrent and is not the first time it runs
			lastRun = nextRun;
		}
		
		isFirstRun = false;

		return nextRun;
	}
	
	// calculates the nextRun based on the current nextRun, recurrence and frequency
	private LocalDateTime calculateNextRun(LocalDateTime nextRun, Recurrence recurrence, long frequency) {
		switch (recurrence) {
		case HOURLY:
			nextRun = nextRun.plusHours(frequency);
			break;
		case DAILY:
			nextRun = nextRun.plusDays(frequency); 
			break;
		default:
			break;
		}
		
		return nextRun;
	}
	
	// if the nextRun is in the past, it will calculate the new nextRun based on the past nextRun, recurrence and frequency
	private LocalDateTime calculateNextRunFromPastDateTime(LocalDateTime dateTime, Recurrence recurrence, long frequency) {
		LocalDateTime currentDateTime = LocalDateTime.now();
		while (dateTime.isBefore(currentDateTime)) {
			dateTime = calculateNextRun(dateTime, recurrence, frequency);
		}
		return dateTime;
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
}
