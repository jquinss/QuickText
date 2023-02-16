package com.jquinss.quicktext.managers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.jquinss.quicktext.data.ReusableText;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReusableTextManager {
	private final ObservableList<ReusableText> reusableTextObsList = FXCollections.observableArrayList();
	private final Gson gson = new Gson();
	
	public ObservableList<ReusableText> getReusableTextObsList() {
		return reusableTextObsList;
	}
	
	public void loadReusableText(String fileName) throws IOException {
		JsonReader jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
		
		jsonReader.beginArray();
		while (jsonReader.hasNext()) {
			ReusableText reusableText = gson.fromJson(jsonReader, ReusableText.class);
			reusableTextObsList.add(reusableText);
		}
		jsonReader.endArray();
		jsonReader.close();
	}
	
	public void saveReusableText(String fileName) throws IOException {
		JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		
		jsonWriter.beginArray();
		for (ReusableText reusableText : reusableTextObsList) {
			gson.toJson(reusableText, ReusableText.class, jsonWriter);
		}
		jsonWriter.endArray();
		jsonWriter.close();
	}
	
	public void addReusableText(ReusableText reusableText) {
		reusableTextObsList.add(reusableText);
	}
	
	public void removeReusableText(ReusableText reusableText) {
		reusableTextObsList.remove(reusableText);
	}
}
