package controllers;

import java.io.File;
import java.io.IOException;

import control.FileTreeItem;
import data.FileItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import managers.FileManager;
import util.DialogBuilder;

public abstract class TextEditorController {
    @FXML
    MenuItem saveMenuItem;

    @FXML
    MenuItem saveAsMenuItem;

    @FXML
    MenuItem quitMenuItem;
    
    Stage stage;
    
    TreeItem<FileItem> folderTreeItem;
    
    TreeItem<FileItem> fileTreeItem;
    
    boolean isSavedText = true;
    
    final FileManager fileManager;
    
    QuickTextController quickTextController;
    
    public TextEditorController(TreeItem<FileItem> fileTreeItem, FileManager fileManager) {
    	this.fileManager = fileManager;
    	if (fileTreeItem.getValue().isDirectory()) {
    		folderTreeItem = fileTreeItem;
    	}
    	else if (fileTreeItem.getValue().isFile()) {
    		this.fileTreeItem = fileTreeItem;
    	}
    }

    @FXML
    void quit(ActionEvent event) {
    	if (!isSavedText) {
    		System.out.println("Text has been modified since last save.");
    		Alert alertDialog = DialogBuilder.getAlertDialog("Confirmation", "Some changes made have not been saved", 
    				"Are you sure you want to exit?", AlertType.CONFIRMATION);
    		alertDialog.showAndWait().ifPresent(response -> {
    			if (response == ButtonType.OK) {
    				stage.close();
    			}
    		});;
    	}
    	else {
    		stage.close();
    	}
    }

    @FXML
    void save(ActionEvent event) {
    	if (fileTreeItem == null) {
    		saveAs(event);
    	}
    	else {
    		try {
    			File template = fileTreeItem.getValue().getFile();
    			saveExistingFile(template);
    		}
    		catch (IOException e) {
    			DialogBuilder.getAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR);
    		}
    	}
    }

    @FXML
    void saveAs(ActionEvent event) {
    	Alert invalidInputAlert = DialogBuilder.getAlertDialog("Informational alert", "Invalid input",
				"The field cannot be empty", AlertType.ERROR);
		TextInputDialog textInputDialog = DialogBuilder.getSingleTextFieldInputDialog("Save Template As", 
				"Enter the name of the template", "Template name", invalidInputAlert);
		
		textInputDialog.showAndWait().ifPresent(fileName -> {
			
			try {
				saveNewFile(fileName);
			}
			catch (IOException e) {
				DialogBuilder.getAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR);
			}
		});
    }
    
    abstract void saveExistingFile(File file) throws IOException;
    
    abstract void saveNewFile(String fileName) throws IOException;
    
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    public void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void addFileTreeItemToFolderTreeItem(File file) {
		FileItem fileItem = new FileItem(file);
		FileTreeItem fileTreeItem = new FileTreeItem(fileItem);
		quickTextController.setContextMenu(fileTreeItem);
		this.fileTreeItem = fileTreeItem;
		folderTreeItem.getChildren().add(fileTreeItem);
    }
    
    void setIsSavedStatus() {
    	saveMenuItem.setDisable(true);
		isSavedText = true;
    }
    
    void setIsNotSavedStatus() {
		saveMenuItem.setDisable(false);
		isSavedText = false;
    }
}
