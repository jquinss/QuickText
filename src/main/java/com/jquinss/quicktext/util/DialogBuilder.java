package com.jquinss.quicktext.util;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

public class DialogBuilder {
	private DialogBuilder() {}
	
	public static TextInputDialog buildSingleTextFieldInputDialog(String title, String headerText, 
																String contentText) {
		
		TextInputDialog dialog = new TextInputDialog();
		
		dialog.setTitle(title);
		dialog.setHeaderText(headerText);
		dialog.setContentText(contentText);
		
		final Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		
		okBtn.addEventFilter(ActionEvent.ACTION, e -> {
			if (dialog.getEditor().getText().trim().isEmpty()) {
				Alert alert = buildAlertDialog("Error", "Input Error", "The field cannot be empty", AlertType.ERROR);
				alert.showAndWait();
				e.consume();
			}
		});
		
		return dialog;
	}
	
	public static TextInputDialog buildSingleTextFieldInputDialog(String title, String headerText, 
			String contentText, String defaultInputText) {

		TextInputDialog dialog = buildSingleTextFieldInputDialog(title, headerText, contentText);
		
		dialog.getEditor().setText(defaultInputText);
		
		return dialog;
	}
	
	public static Dialog<Pair<String, String>> buildTwoTextFieldInputDialog(String title, String headerText, String firstFieldName, 
			String secondFieldName, boolean isOptionalSecondField) {
		Dialog<Pair<String, String>> dialog = new Dialog<>();

		dialog.setTitle(title);
		dialog.setHeaderText(headerText);

		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField firstField = new TextField();
		firstField.setPromptText(firstFieldName);

		TextField secondField = new TextField();
		secondField.setPromptText(secondFieldName);

		grid.add(new Label(firstFieldName), 0, 0);
		grid.add(firstField, 1, 0);

		grid.add(new Label(secondFieldName), 0, 1);
		grid.add(secondField, 1, 1);

		Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

		okButton.setDisable(true);

		firstField.textProperty().addListener((observable, oldValue, newValue) -> {
			boolean areRequiredFieldsEmpty = newValue.trim().isEmpty();

			if (!isOptionalSecondField) {
				areRequiredFieldsEmpty = areRequiredFieldsEmpty | secondField.getText().trim().isEmpty();
			}

			okButton.setDisable(areRequiredFieldsEmpty);
		});

		if (!isOptionalSecondField) {
			secondField.textProperty().addListener((observable, oldValue, newValue) -> {
				boolean areRequiredFieldsEmpty = newValue.trim().isEmpty() | firstField.getText().trim().isEmpty();

				okButton.setDisable(areRequiredFieldsEmpty);
			});
		}

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return new Pair<>(firstField.getText().trim(), secondField.getText().trim());
			}

			return null;
		});

		return dialog;
	}
	
	public static Alert buildAlertDialog(String title, String headerText, 
									String contentText, AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		
		return alert;
	}
	
	public static FileChooser buildFileChooser(String title, ExtensionFilter... extFilters) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.getExtensionFilters().addAll(extFilters);
		
		return fileChooser;
	}
}