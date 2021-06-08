package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;

import control.FileTreeItem;
import data.FileItem;
import data.FolderItem;
import data.TemplateItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Pair;
import managers.FileManager;
import util.DialogBuilder;
import util.FileItemBuilder;

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
    
    FileItemBuilder fileItemBuilder;
    
    File root;
    
    QuickTextController quickTextController;
    
    public TextEditorController(TreeItem<FileItem> fileTreeItem, FileManager fileManager, File root) {
    	this.fileManager = fileManager;
    	this.root = root;
    	fileItemBuilder = new FileItemBuilder(this.root);
    	
    	if (fileTreeItem.getValue() instanceof FolderItem) {
    		folderTreeItem = fileTreeItem;
    	}
    	else if (fileTreeItem.getValue() instanceof TemplateItem) {
    		this.fileTreeItem = fileTreeItem;
    	}
    }

    @FXML
    void quit(ActionEvent event) {
    	if (!isSavedText) {
    		System.out.println("Text has been modified since last save.");
    		Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "Some changes made have not been saved", 
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
    			DialogBuilder.buildAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR);
    		}
    	}
    }

    @FXML
    void saveAs(ActionEvent event) {
    	Dialog<Pair<String, String>> dialog = DialogBuilder.buildTwoTextFieldInputDialog("Create template", "Create a new template:", "Template name", 
    			"Description", true);
    	
    	Optional<Pair<String, String>> result = dialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String fileName = result.get().getKey();
    		String fileDescription = result.get().getValue();
    		
    		try {
				saveNewFile(fileName, fileDescription);
			}
    		catch (FileAlreadyExistsException e) {
    			DialogBuilder.buildAlertDialog("Error", "Error saving the file", "The file " + e.getFile() + " already exists", AlertType.ERROR).showAndWait();
    		}
			catch (IOException e) {
				DialogBuilder.buildAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR).showAndWait();
			}
    	}
    }
    
    abstract void saveExistingFile(File file) throws IOException;
    
    abstract void saveNewFile(String fileName, String description) throws IOException;
    
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    public void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void addFileTreeItemToFolderTreeItem(File file, String description) {
		FileItem fileItem = fileItemBuilder.buildFileItem(file);
		if (!description.isEmpty()) {
			fileItem.setDescription(description);
		}
		FileTreeItem fileTreeItem = new FileTreeItem(fileItem);

		quickTextController.setContextMenu(fileTreeItem);
		this.fileTreeItem = fileTreeItem;
		folderTreeItem.getChildren().add(fileTreeItem);
    }
    
    void writeTextToFile(String text, File file) throws IOException {
    	fileManager.writeStringToFile(text, file);
    	quickTextController.updateTextInCache(file, text);
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
