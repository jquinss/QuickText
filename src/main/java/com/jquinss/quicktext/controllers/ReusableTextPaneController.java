package com.jquinss.quicktext.controllers;

import java.io.IOException;

import com.jquinss.quicktext.data.ReusableText;
import com.jquinss.quicktext.managers.ReusableTextManager;
import com.jquinss.quicktext.managers.SettingsManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReusableTextPaneController {
	
	@FXML
	private ListView<ReusableText> reusableTextListView;
	
	@FXML
	private TextField reusableTextNameTextField;
	
	@FXML
	private TextField reusableTextDescriptionTextField;
	
	@FXML
	private TextArea reusableTextTextArea;
	
	private final ReusableTextManager reusableTextManager;
	
	private ReusableText editedReusableText;
	
	private ObservableList<ReusableText> reusableTextObsList;
	
	private Stage stage;
	
	private Stage dialogStage;
	
	public ReusableTextPaneController(ReusableTextManager reusableTextManager) {
		this.reusableTextManager = reusableTextManager;
	}
	
    @FXML
    void addReusableText(ActionEvent event) throws IOException {
    	openReusableTextDialog();
    }

    @FXML
    void editReusableText(ActionEvent event) throws IOException {
    	ReusableText reusableText = reusableTextListView.getSelectionModel().getSelectedItem();
    	if (reusableText != null) {
    		this.editedReusableText = reusableText;
    		openReusableTextDialog();
    	}
    }

    @FXML
    void removeReusableText(ActionEvent event) {
    	ReusableText reusableText = reusableTextListView.getSelectionModel().getSelectedItem();
    	reusableTextObsList.remove(reusableText);
    }
    
    @FXML
    void insertReusableText(ActionEvent event) {
    	// TO DO
    	System.out.println("Inserting reusable text");
    }
    
    @FXML
	void manageReusableText(ActionEvent event) throws IOException {
    	openReusableTextDialog();
	}
    
    private void openReusableTextDialog() throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/ReusableTextDialog.fxml"));
		
		fxmlLoader.setController(this);
		
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent, 400, 320);
		scene.getStylesheets().add(getClass().getResource(SettingsManager.getInstance().getCSSPath()).toString());
        dialogStage = new Stage();
        dialogStage.setResizable(false);
        dialogStage.setTitle("Add reusable text");
		dialogStage.getIcons().add(new Image(getClass().getResource(SettingsManager.getInstance().getLogoPath()).toString()));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    @FXML
    void saveReusableText(ActionEvent event) {
    	String name = reusableTextNameTextField.getText().trim();
    	String description = reusableTextDescriptionTextField.getText().trim();
    	String text = reusableTextTextArea.getText().trim();
    	
    	if (editedReusableText != null) {
    		editedReusableText.setName(name);
    		editedReusableText.setDescription(description);
    		editedReusableText.setText(text);
    		int editedReusableTextIndex = reusableTextObsList.indexOf(editedReusableText);
    		reusableTextObsList.remove(editedReusableText);
    		reusableTextObsList.add(editedReusableTextIndex, editedReusableText);
    		reusableTextListView.getSelectionModel().select(editedReusableText);
    	}
    	else {
    		reusableTextObsList.add(new ReusableText(name, text, description));
    	}
    	
    	editedReusableText = null;
    	dialogStage.close();
    }
    
    @FXML
    void cancelReusableTextDialog(ActionEvent event) {
    	editedReusableText = null;
    	dialogStage.close();
    }
    
    @FXML
    void initialize() {
    	loadReusableText();
    	if (editedReusableText != null) {
        	reusableTextNameTextField.setText(editedReusableText.getName());
        	reusableTextDescriptionTextField.setText(editedReusableText.getDescription());
        	reusableTextTextArea.setText(editedReusableText.getText());
    	}
    }
    
    
    private void loadReusableText() {
    	reusableTextObsList = reusableTextManager.getReusableTextObsList();
    	reusableTextListView.setItems(reusableTextObsList);
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
}