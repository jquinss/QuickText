package com.jquinss.quicktext.enums;

public enum Recurrence {
	NO_RECURRENT, HOURLY, DAILY;
	
	public String toString() {
		return switch (this) {
			case NO_RECURRENT -> "No recurrent";
			case HOURLY -> "Hourly";
			case DAILY -> "Daily";
			default -> throw new IllegalArgumentException();
		};
	}
}
