package com.jquinss.quicktext.controllers;

import java.io.IOException;

import com.jquinss.quicktext.data.ReusableText;
import com.jquinss.quicktext.managers.ReusableTextManager;
import com.jquinss.quicktext.managers.SettingsManager;
import com.jquinss.quicktext.util.DialogBuilder;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
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

	private boolean isEditMode = false;
	
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
    		editedReusableText = reusableText;
			isEditMode = true;
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
        dialogStage.setTitle("Reusable Text Editor");
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
    	
    	if (isEditMode && isValidReusableText(name, text)) {
    		editedReusableText.setName(name);
    		editedReusableText.setDescription(description);
    		editedReusableText.setText(text);
    		int editedReusableTextIndex = reusableTextObsList.indexOf(editedReusableText);
    		reusableTextObsList.remove(editedReusableText);
    		reusableTextObsList.add(editedReusableTextIndex, editedReusableText);
    		reusableTextListView.getSelectionModel().select(editedReusableText);
			dialogStage.close();
    	}
    	else {
			if (isValidReusableText(name, text)) {
				reusableTextObsList.add(new ReusableText(name, text, description));
				dialogStage.close();
			}
			else {
				StringBuilder validationMessage = new StringBuilder();
				validationMessage.append("The following fields are required:\n");

				if (name.isEmpty()) {
					validationMessage.append("- Name\n");
				}

				if (text.isEmpty()) {
					validationMessage.append("- Text");
				}

				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Invalid reusable text", validationMessage.toString(), Alert.AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}
    	
    	isEditMode = false;
    }
    
    @FXML
    void cancelReusableTextDialog(ActionEvent event) {
    	isEditMode = false;
    	dialogStage.close();
    }
    
    @FXML
    void initialize() {
    	loadReusableText();
    	if (isEditMode) {
        	reusableTextNameTextField.setText(editedReusableText.getName());
        	reusableTextDescriptionTextField.setText(editedReusableText.getDescription());
        	reusableTextTextArea.setText(editedReusableText.getText());
    	}
    }
    
    private void loadReusableText() {
    	reusableTextObsList = reusableTextManager.getReusableTextObsList();
    	reusableTextListView.setItems(reusableTextObsList);
    }

	private boolean isValidReusableText(String name, String text) {
		return !(name.isEmpty() || text.isEmpty());
	}
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }

	void setLogo(Object object, String logo) {
		if (object instanceof Stage) {
			((Stage) object).getIcons().add(new Image(getClass().getResource(logo).toString()));
		}

		if (object instanceof Pane) {
			Stage stage = (Stage) ((Pane)object).getScene().getWindow();
			stage.getIcons().add(new Image(getClass().getResource(logo).toString()));
		}
	}

	void setStyle(Object object, String css) {
		if (object instanceof Scene) {
			((Scene) object).getStylesheets().add(getClass().getResource(css).toString());
		}

		if (object instanceof Pane) {
			((Pane) object).getStylesheets().add(getClass().getResource(css).toString());
		}
	}
}