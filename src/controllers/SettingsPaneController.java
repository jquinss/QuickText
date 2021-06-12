package controllers;

import enums.Charsets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.fxml.FXML;
import managers.SettingsManager;
import util.DialogBuilder;

public class SettingsPaneController {
    @FXML
    private ComboBox<Charsets> charEncodingComboBox;
    
    @FXML
    private TextField numCachedTemplatesTextField;
    
    private QuickTextController quickTextController;
    
    private Stage stage;
    
    private final ObservableList<Charsets> charsetObsList = FXCollections.observableArrayList();

    @FXML
    void clearCache(ActionEvent event) {
    	quickTextController.clearCache();
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
    	SettingsManager.getInstance().resetSettings();
    	applySettings();
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    @FXML
    public void initialize() {
    	initializeSettingsTabs();
    }
    
    private void initializeSettingsTabs() {
    	initializeCacheSettingsTab();
    	initializeCharsetSettingsTab();
    }
    
    private void initializeCacheSettingsTab() {
    	numCachedTemplatesTextField.setText(SettingsManager.getInstance().getCacheMaxItems());
    }
    
    private void initializeCharsetSettingsTab() {
    	initializeCharsetComboBoxCell();
    	
    	String defaultCharset = SettingsManager.getInstance().getTextCharset();
    	Charsets defaultCharsetEnum = null;
    	
    	for (Charsets charset : Charsets.values()) {
    		charsetObsList.add(charset);
    		if (charset.toString().equals(defaultCharset)) {
    			defaultCharsetEnum = charset;
    		}
    	}
    	charEncodingComboBox.setItems(charsetObsList);
    	charEncodingComboBox.getSelectionModel().select(defaultCharsetEnum);
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
    
    private String validateInput() {
    	StringBuilder validationText = new StringBuilder();
    	
    	return validationText.toString();
    }
    
    private void applySettings() {
    	String cacheMaxItems = numCachedTemplatesTextField.getText();
    	SettingsManager.getInstance().setCacheMaxItems(cacheMaxItems);
    	quickTextController.setCacheMaxItems(Integer.parseInt(cacheMaxItems));
    	SettingsManager.getInstance().setTextCharset(charEncodingComboBox.getSelectionModel().getSelectedItem().toString());
    }
    
    private void saveSettings() {
    	applySettings();
    	SettingsManager.getInstance().saveSettings();
    }
    

}
