package com.jquinss.quicktext.enums;

public enum Result {
	SUCCESS, FAIL, NOT_AVAILABLE;
	
	public String toString() {
		return switch (this) {
			case SUCCESS -> "Success";
			case FAIL -> "Failed";
			case NOT_AVAILABLE -> "N/A";
			default -> throw new IllegalArgumentException();
		};
	}
}
