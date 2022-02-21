package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class BackupsPaneController {

    @FXML
    private TableView<?> backupsTableView;

    @FXML
    private TableView<?> scheduledBackupsTableView;

    @FXML
    private DatePicker scheduledDateTime;

    @FXML
    private ComboBox<?> hoursComboBox;

    @FXML
    private ComboBox<?> daysComboBox;
    
    private QuickTextController quickTextController;
    
    private Stage stage;

    @FXML
    void createBackup(ActionEvent event) {

    }

    @FXML
    void createScheduledBackup(ActionEvent event) {

    }

    @FXML
    void deleteBackup(ActionEvent event) {

    }

    @FXML
    void deleteScheduledBackup(ActionEvent event) {

    }

    @FXML
    void loadBackup(ActionEvent event) {

    }

    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
}
