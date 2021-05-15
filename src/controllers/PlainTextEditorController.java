package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import control.FileTreeItem;
import data.FileItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import managers.FileManager;
import util.DialogBuilder;

public class PlainTextEditorController {

    @FXML
    private TextArea textArea;

    @FXML
    private MenuItem saveMenuItem;

    @FXML
    private MenuItem saveAsMenuItem;

    @FXML
    private MenuItem quitMenuItem;
    
    private Stage stage;
    
    private TreeItem<FileItem> folderTreeItem;
    
    private FileTreeItem fileTreeItem;
    
    private boolean isSavedText = true;
    
    private FileManager fileManager;
    
    private QuickTextController quickTextController;

    @FXML
    void quit(ActionEvent event) {
    	if (!isSavedText) {
    		System.out.println("Text has been modified since last save.");
    	}
    }

    @FXML
    void save(ActionEvent event) {
    	if (fileTreeItem == null) {
    		saveAs(event);
    	}
    	else {
    		System.out.println("Calling Save action");
    		setIsSavedStatus();
    	}
    }

    @FXML
    void saveAs(ActionEvent event) {
    	System.out.println("Calling Save As action");
    	Alert invalidInputAlert = DialogBuilder.getAlertDialog("Informational alert", "Invalid input",
				"The field cannot be empty", AlertType.ERROR);
		TextInputDialog textInputDialog = DialogBuilder.getSingleTextFieldInputDialog("Save Template As", 
				"Enter the name of the template", "Template name", invalidInputAlert);
		
		textInputDialog.showAndWait().ifPresent(fileName -> {
			File folder = folderTreeItem.getValue().getFile();
			File file = new File(folder.toString() + File.separator + fileName + ".txt");
			
			try {
				writeTextAreaToFile(file);
				FileItem fileItem = new FileItem(file);
				fileTreeItem = new FileTreeItem(fileItem);
				quickTextController.setContextMenu(fileTreeItem);
				folderTreeItem.getChildren().add(fileTreeItem);
				setIsSavedStatus();
			}
			catch (IOException e) {
				
			}
		});
    }
    
    @FXML
    public void initialize() {
    	initializeMenuItemBindings();
    }
    
    private void initializeMenuItemBindings() {
    	textArea.textProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			setIsNotSavedStatus();
    		}
    	});
    }
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    public void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    public void setFileManager(FileManager fileManager) {
    	this.fileManager = fileManager;
    }
    
    public void setFolderTreeItem(TreeItem<FileItem> folderTreeItem) {
    	this.folderTreeItem = folderTreeItem;
    }
    
    private void setIsSavedStatus() {
    	saveMenuItem.setDisable(true);
		isSavedText = true;
    }
    
    private void setIsNotSavedStatus() {
		saveMenuItem.setDisable(false);
		isSavedText = false;
    }
    
    private void writeTextAreaToFile(File file) throws IOException {
    	String textAreaContent = textArea.getText();
    	fileManager.writeStringToFile(textAreaContent, file);
    }
}