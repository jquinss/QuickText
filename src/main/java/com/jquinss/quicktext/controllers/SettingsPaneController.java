package com.jquinss.quicktext.controllers;

import java.util.function.UnaryOperator;

import com.jquinss.quicktext.enums.Charsets;
import com.jquinss.quicktext.managers.SettingsManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import javafx.fxml.FXML;
import com.jquinss.quicktext.util.DialogBuilder;

public class SettingsPaneController {
    @FXML
    private ComboBox<Charsets> charEncodingComboBox;
    
    @FXML
    private TextField numCachedTemplatesTextField;
    
    @FXML
    private TextField maxBackupsTextField;
    
    @FXML
    Text clearCacheSuccessText;
    
    private QuickTextController quickTextController;
    
    private Stage stage;
    
    private final ObservableList<Charsets> charsetObsList = FXCollections.observableArrayList();

    @FXML
    void clearCache(ActionEvent event) {
    	quickTextController.clearCache();
    	clearCacheSuccessText.setVisible(true);
    }
    
    @FXML
    void applySettings(ActionEvent event) {
    	String validationResult = validateInput();
    	
    	if (!validationResult.isEmpty()) {
    		Alert alert = DialogBuilder.buildAlertDialog("Error", "Invalid Input", validationResult, AlertType.ERROR);
    		alert.showAndWait();
    	}
    	else {
    		saveSettings();
    		stage.close();
    	}
    }

    @FXML
    void cancelSettings(ActionEvent event) {
    	stage.close();
    }

    @FXML
    void resetSettings(ActionEvent event) {
    	loadDefaultSettings();
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    @FXML
    public void initialize() {
    	initializeCharsetSettingsTab();
    	initializeCacheSettingsTab();
    	initializeBackupSettingsTab();
    	loadSettings();
    }
    
    private void initializeCacheSettingsTab() {
    	String defaultCacheMaxItems = SettingsManager.getInstance().getDefaultCacheMaxItems();
    	UnaryOperator<TextFormatter.Change> numCachedTemplatesFilter = createInputFilter("[1-9][0-9]{0,2}", "1");
    	numCachedTemplatesTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), Integer.parseInt(defaultCacheMaxItems), numCachedTemplatesFilter));
    }
    
    private void initializeBackupSettingsTab() {
    	String defaultBackupMaxItems = SettingsManager.getInstance().getDefaultBackupMaxItems();
    	UnaryOperator<TextFormatter.Change> numBackupsFilter = createInputFilter("[1-9][0-9]{0,2}", "1");
    	maxBackupsTextField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), Integer.parseInt(defaultBackupMaxItems), numBackupsFilter));
    }
    
    private void loadSettings() {
    	loadCacheSettings(SettingsManager.getInstance().getCacheMaxItems());
    	loadCharsetSettings(SettingsManager.getInstance().getTextCharset());
    	loadBackupSettings(SettingsManager.getInstance().getBackupMaxItems());
    }
    
    private void loadDefaultSettings() {
    	loadCacheSettings(SettingsManager.getInstance().getDefaultCacheMaxItems());
    	loadCharsetSettings(SettingsManager.getInstance().getDefaultTextCharset());
    }
    
    private void loadCacheSettings(String numEntries) {
    	numCachedTemplatesTextField.setText(numEntries);
    }
    
    private void loadBackupSettings(String numEntries) {
    	maxBackupsTextField.setText(numEntries);
    }
    
    private void initializeCharsetSettingsTab() {
    	initializeCharsetComboBoxCell();
    	
    	for (Charsets charset : Charsets.values()) {
    		charsetObsList.add(charset);
    	}
    	charEncodingComboBox.setItems(charsetObsList);
    }
    
    private void initializeCharsetComboBoxCell() {
    	charEncodingComboBox.setCellFactory(new Callback<ListView<Charsets>, ListCell<Charsets>>() {
    		 
            @Override
            public ListCell<Charsets> call(ListView<Charsets> param) {
                final ListCell<Charsets> cell = new ListCell<Charsets>() {
 
                    @Override
                    protected void updateItem(Charsets item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item != null) {
                            setText(item.toString());
                        } else {
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });;
        
        charEncodingComboBox.setButtonCell(new ListCell<Charsets>() {
       	 
            @Override
            protected void updateItem(Charsets item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.toString());
                } else {
                    setText(null);
                }
            }
        });
    }
    
    private void loadCharsetSettings(String charset) {
    	charEncodingComboBox.getSelectionModel().select(Charsets.getCharsetsHashMap().get(charset));
    }
    
    private String validateInput() {
    	StringBuilder validationText = new StringBuilder();
    	
    	return validationText.toString();
    }
    
    private void applySettings() {
    	String cacheMaxItems = numCachedTemplatesTextField.getText();
    	SettingsManager.getInstance().setCacheMaxItems(cacheMaxItems);
    	quickTextController.setCacheMaxItems(Integer.parseInt(cacheMaxItems));
    	String backupMaxItems = maxBackupsTextField.getText();
    	SettingsManager.getInstance().setBackupMaxItems(backupMaxItems);
    	quickTextController.deleteOldestBackups(Integer.parseInt(backupMaxItems));
    	SettingsManager.getInstance().setTextCharset(charEncodingComboBox.getSelectionModel().getSelectedItem().toString());
    }
    
    private void saveSettings() {
    	applySettings();
    	SettingsManager.getInstance().saveSettings();
    }
    
	private UnaryOperator<TextFormatter.Change> createInputFilter(String validInputRegEx, String defaultText) {
		UnaryOperator<TextFormatter.Change> filter = change -> {
			String newText = change.getControlNewText();
			if (newText.matches(validInputRegEx)) {
				return change;
			} else if (newText.matches("")) {
				change.setText(defaultText);
				change.setCaretPosition(change.getCaretPosition() + 1);

				return change;
			}

			return null;
		};

		return filter;
	}
}
