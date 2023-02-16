package com.jquinss.quicktext.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.enums.Charsets;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;

public class PlainTextEditorController extends TextEditorController {
	
	private static final String PLAIN_TEXT_EXT = ".txt";
	
	public PlainTextEditorController(TreeItem<FileItem> treeItem, FileManager fileManager, File root) {
		super(treeItem, fileManager, root);
	}
	
	@FXML
    private TextArea textArea;
	
    void saveNewFile(String fileName, String description) throws IOException {
    	File folder = folderTreeItem.getValue().getFile();
		File file = fileManager.buildFilePath(folder.toString(), fileName, PLAIN_TEXT_EXT);
		
		if (file.exists()) {
			throw new FileAlreadyExistsException(file.toString());
		}
    	
		writeTextAreaToFile(file);
    	createFile(file, description);
		setIsSavedStatus();
    }
    
    void saveExistingFile(File file) throws IOException {
		writeTextAreaToFile(file);
		setIsSavedStatus();
    }
    
    private void writeTextAreaToFile(File file) throws IOException {
    	String text = textArea.getText();
    	writeTextToFile(text, file);
    }
    
	void insertText(String text) {
		textArea.replaceSelection(text);
	}
    
    private void initializeMenuItemBindings() {
    	textArea.textProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			setIsNotSavedStatus();
    		}
    	});
    }
    
    public void initialize() {
    	initializeMenuItemBindings();
    	
    	if (fileTreeItem != null) {
    		try {
    			File file = fileTreeItem.getValue().getFile();
    			loadFileToTextArea(file);
    			setIsSavedStatus();
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void loadFileToTextArea(File file) throws IOException {
    	HashMap<String, Charsets> charsetsMap = Charsets.getCharsetsHashMap();
    	String charsetName = SettingsManager.getInstance().getTextCharset();
    	
        String text = fileManager.readAllLinesFromFileAsString(file, charsetsMap.get(charsetName).toStandardCharset());
        textArea.setText(text);
    }
}
