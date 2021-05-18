package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import data.FileItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import managers.FileManager;

public class PlainTextEditorController extends TextEditorController {
	public PlainTextEditorController(TreeItem<FileItem> treeItem, FileManager fileManager) {
		super(treeItem, fileManager);
	}
	@FXML
    private TextArea textArea;
	
    void saveNewFile(String fileName) throws IOException {
    	File folder = folderTreeItem.getValue().getFile();
		File file = new File(folder.toString() + File.separator + fileName + ".txt");
    	writeTextAreaToFile(file);
    	addFileTreeItemToFolderTreeItem(file);
		setIsSavedStatus();
    }
    
    void saveExistingFile(File file) throws IOException {
		writeTextAreaToFile(file);
		setIsSavedStatus();
    }
    
    private void writeTextAreaToFile(File file) throws IOException {
    	String textAreaContent = textArea.getText();
    	fileManager.writeStringToFile(textAreaContent, file);
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
        List<String> fileLines = fileManager.readAllLinesFromFile(file);
        for (String line : fileLines) {
        	textArea.appendText(line + "\n");
        }
    }
}
