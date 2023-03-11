package com.jquinss.quicktext.controllers;

import java.io.IOException;

import com.jquinss.quicktext.data.ReusableText;
import com.jquinss.quicktext.managers.ReusableTextManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ReusableTextInsertionDialogController {
	@FXML
	private ListView<ReusableText> reusableTextListView;
	private ObservableList<ReusableText> reusableTextObsList;
	private Stage stage;
	
	private final TextEditorController textEditorController;
	private final ReusableTextManager reusableTextManager;
	
	public ReusableTextInsertionDialogController(TextEditorController textEditorController,
												 ReusableTextManager reusableTextManager) {
		this.textEditorController = textEditorController;
		this.reusableTextManager = reusableTextManager;
	}
	
    @FXML
    void insertReusableText(ActionEvent event) {
    	ReusableText reusableText = reusableTextListView.getSelectionModel().getSelectedItem();
    	if (reusableText != null) {
    		textEditorController.insertText(reusableText.getText());
    	}
    }
    
    @FXML
	void manageReusableText(ActionEvent event) throws IOException {
    	textEditorController.openReusableTextPane(event);
	}
    
    @FXML
    void initialize() {
    	loadReusableText();
    }
    
    private void loadReusableText() {
    	reusableTextObsList = reusableTextManager.getReusableTextObsList();
    	reusableTextListView.setItems(reusableTextObsList);
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
}
