package com.jquinss.quicktext.data;

public class ReusableText {
	private String name;
	private String text;
	private String description;
	
	public ReusableText(String name, String text, String description) {
		this.name = name;
		this.text = text;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return name;
	}
}
