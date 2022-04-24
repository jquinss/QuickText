package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import data.FileBackup;
import util.ZipUtil;
import managers.SettingsManager;

public class BackupsPaneController {

    @FXML
    private TableView<FileBackup> backupsTableView;

    @FXML
    private TableView<?> scheduledBackupTasksTableView;

    @FXML
    private DatePicker scheduledDateTime;

    @FXML
    private ComboBox<?> hoursComboBox;

    @FXML
    private ComboBox<?> daysComboBox;
    
    private QuickTextController quickTextController;
    
    private Stage stage;
    
    private ObservableList<FileBackup> fileBackupObsList = FXCollections.observableArrayList();

    @FXML
    void createBackup(ActionEvent event) {
    	Path source = Paths.get(SettingsManager.getInstance().getAppDir());
    	String destZipFile = Paths.get(SettingsManager.getInstance().getBackupsDir(), generateBackupName()).toString();
    	String[] ignoredFiles = new String[] {SettingsManager.getInstance().getBackupsDir(), SettingsManager.getInstance().getSettingsPath()};
    	
    	try {
			ZipUtil.zipFolder(source, destZipFile, ignoredFiles);
			FileBackup fileBackup = new FileBackup(new File(destZipFile));
			fileBackupObsList.add(fileBackup);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void createScheduledBackupTask(ActionEvent event) {
    	System.out.println("Creating a backup task");
    	/* TO DO */
    }

    @FXML
    void deleteBackup(ActionEvent event) {
    	System.out.println("Deleting a backup file");
    	/* TO DO */
    }

    @FXML
    void deleteScheduledBackupTask(ActionEvent event) {
    	System.out.println("Deleting a backup task");
    	/* TO DO */
    }

    @FXML
    void loadBackup(ActionEvent event) {
    	System.out.println("Loading backup");
    	/* TO DO */
    }
    
    private void loadBackupFiles() {
    	System.out.println("Loading backup files");
    	/* TO DO */
    }
    
	private String generateBackupName() {
		StringBuilder name = new StringBuilder();
		name.append("backup-");
		name.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss")));
		name.append(".zip");
		return name.toString();
	}
    
    @FXML
    public void initialize() {
    	try {
			Files.createDirectory(new File(SettingsManager.getInstance().getBackupsDir()).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	backupsTableView.setItems(fileBackupObsList);
    	loadBackupFiles();
    }

    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
}
