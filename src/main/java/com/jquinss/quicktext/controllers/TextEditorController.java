package com.jquinss.quicktext.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;

import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.data.FolderItem;
import com.jquinss.quicktext.enums.Charsets;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.ReusableTextManager;
import com.jquinss.quicktext.managers.SettingsManager;
import com.jquinss.quicktext.data.TemplateItem;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import com.jquinss.quicktext.util.DialogBuilder;
import com.jquinss.quicktext.util.FileItemBuilder;

public abstract class TextEditorController {
    @FXML
    MenuItem saveMenuItem;

    @FXML
    MenuItem saveAsMenuItem;

    @FXML
    MenuItem quitMenuItem;
    
    Stage stage;

	Stage reusableTextMenuStage;
    
    TreeItem<FileItem> folderTreeItem;
    
    TreeItem<FileItem> fileTreeItem;
    
    boolean isSavedText = true;
    
    final FileManager fileManager;
    
    FileItemBuilder fileItemBuilder;
    
    File root;
    
    QuickTextController quickTextController;
    
    ReusableTextManager reusableTextManager;
    
    public TextEditorController(TreeItem<FileItem> fileTreeItem, FileManager fileManager, File root) {
    	this.fileManager = fileManager;
    	this.root = root;
    	fileItemBuilder = new FileItemBuilder(this.root);
    	
    	if (fileTreeItem.getValue() instanceof FolderItem) {
    		folderTreeItem = fileTreeItem;
    	}
    	else if (fileTreeItem.getValue() instanceof TemplateItem) {
    		this.fileTreeItem = fileTreeItem;
    		folderTreeItem = this.fileTreeItem.getParent();
    	}
    }

    @FXML
    void quit(Event event) {
		if (!isSavedText) {
			Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "Some changes made have not been saved",
					"Are you sure you want to exit?", AlertType.CONFIRMATION);
			quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
			quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
			alertDialog.showAndWait().ifPresent(response -> {
				if (response == ButtonType.OK) {
					stage.close();
				}

				if (event instanceof WindowEvent) {
					event.consume();
				}
			});;
		}
		else {
			stage.close();
		}

		if (reusableTextMenuStage != null) {
			reusableTextMenuStage.close();
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
    			Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR);
				quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
    		}
    	}
    }

    @FXML
    void saveAs(ActionEvent event) {
    	Dialog<Pair<String, String>> dialog = DialogBuilder.buildTwoTextFieldInputDialog("Create template", "Create a new template:", "Template name", 
    			"Description", true);
		quickTextController.setLogo(dialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
		quickTextController.setStyle(dialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
    	
    	Optional<Pair<String, String>> result = dialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String fileName = result.get().getKey();
    		String fileDescription = result.get().getValue();
    		
    		try {
				saveNewFile(fileName, fileDescription);
			}
    		catch (FileAlreadyExistsException e) {
    			Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error saving the file", "The file " + e.getFile() + " already exists", AlertType.ERROR);
				quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
    		}
			catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error saving the file", "An error has occurred while saving the file", AlertType.ERROR);
				quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}
    }
    
	@FXML
	void openReusableTextMenu(ActionEvent event) throws IOException {
		if (reusableTextMenuStage == null) {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/ReusableTextInsertDialog.fxml"));
			TextEditorReusableTextDialogController textEditorReusableTextDialogController = new TextEditorReusableTextDialogController(this, reusableTextManager);
			fxmlLoader.setController(textEditorReusableTextDialogController);
			Parent parent = fxmlLoader.load();
			Scene scene = new Scene(parent, 400, 320);
			reusableTextMenuStage = new Stage();
			textEditorReusableTextDialogController.setStage(reusableTextMenuStage);
			reusableTextMenuStage.setResizable(false);
			reusableTextMenuStage.setTitle("Insert reusable text");
			quickTextController.setStyle(scene, SettingsManager.getInstance().getCSSPath());
			quickTextController.setLogo(stage, SettingsManager.getInstance().getLogoPath());
			reusableTextMenuStage.setScene(scene);
		}

		if (!reusableTextMenuStage.isShowing()) {
			reusableTextMenuStage.showAndWait();
		}

		reusableTextMenuStage.toFront();
	}
    
    abstract void saveExistingFile(File file) throws IOException;
    
    abstract void saveNewFile(String fileName, String description) throws IOException;
    
    abstract void insertText(String text);
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    void openReusableTextPane(ActionEvent event) throws IOException {
    	quickTextController.openReusableTextPane(event);
    }
    
    public void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void setReusableTextManager(ReusableTextManager reusableTextManager) {
    	this.reusableTextManager = reusableTextManager;
    }
    
    void createFile(File file, String description) {
    	quickTextController.createTemplate(file, description, folderTreeItem);
    }
    
    void writeTextToFile(String text, File file) throws IOException {
    	Charset charset = Charsets.getCharsetsHashMap().get(SettingsManager.getInstance().getTextCharset()).toStandardCharset();
    	fileManager.writeStringToFile(text, file, charset);
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
