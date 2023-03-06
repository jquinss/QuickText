package com.jquinss.quicktext.controllers;

import com.jquinss.quicktext.control.DateTimePicker;
import com.jquinss.quicktext.data.BackupTask;
import com.jquinss.quicktext.data.FileBackup;
import com.jquinss.quicktext.data.ScheduledBackupTask;
import com.jquinss.quicktext.enums.Recurrence;
import com.jquinss.quicktext.exceptions.InvalidDateTimeException;
import com.jquinss.quicktext.managers.BackupManager;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.SettingsManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.jquinss.quicktext.util.DialogBuilder;
import com.jquinss.quicktext.util.Schedule;

import com.jquinss.quicktext.util.ZipUtil;

public class BackupsPaneController implements Initializable {

    @FXML
    private TableView<FileBackup> backupsTableView;

    @FXML
    private TableView<ScheduledBackupTask> scheduledBackupTasksTableView;
    
    @FXML
    private TableColumn<FileBackup, String> backupFileNameColumn;
    
    @FXML
    private TableColumn<FileBackup, String> backupCreationDateTimeColumn;
    
    @FXML
    private TableColumn<ScheduledBackupTask, String> nextBackupRunDateTimeColumn;
    
    @FXML
    private TableColumn<ScheduledBackupTask, String> lastBackupRunDateTimeColumn;
    
    @FXML
    private TableColumn<ScheduledBackupTask, String> lastBackupResultColumn;
    
    @FXML
    private TableColumn<ScheduledBackupTask, String> backupRecurrenceColumn;

    @FXML
    private DateTimePicker dateTimePicker;

    @FXML
    private Spinner<Integer> hoursSpinner;

    @FXML
    private Spinner<Integer> daysSpinner;
    
    @FXML
    private ToggleGroup toggleRadioButtonGroup;
    
    @FXML
    private RadioButton noRecurrencyRadioButton;
    
    @FXML
    private RadioButton hoursRadioButton;
    
    @FXML
    private RadioButton daysRadioButton;
    
    private QuickTextController quickTextController;
    
    private final FileManager fileManager = new FileManager();
    
    private final BackupManager backupManager;
    
    public BackupsPaneController(BackupManager backupManager) {
    	this.backupManager = backupManager;
    }
    
    @FXML
    void createBackup(ActionEvent event) {
    	String sourceFolder = SettingsManager.getInstance().getAppDir();
    	String destZipFile = Paths.get(SettingsManager.getInstance().getBackupsDir(), generateBackupName()).toString();
    	String[] ignoredFiles = new String[] {SettingsManager.getInstance().getBackupsDir(), SettingsManager.getInstance().getSettingsPath()};
    	
    	backupManager.createBackup(sourceFolder, destZipFile, ignoredFiles);
    }

    @FXML
    void createScheduledBackupTask(ActionEvent event) {
    	Toggle selectedToggle = toggleRadioButtonGroup.getSelectedToggle();
    	Recurrence recurrence = (Recurrence) selectedToggle.getUserData();
    	int frequency = 0;
    	
    	switch (recurrence) {
    	case NO_RECURRENT:
    		frequency = 0;
    		break;
    	case HOURLY:
    		frequency = hoursSpinner.getValue();
    		break;
    	case DAILY:
    		frequency = daysSpinner.getValue();
    		break;
    	}
    	try {
    		LocalDateTime dateTime = dateTimePicker.getDateTimeValue();
    		Schedule schedule = new Schedule(dateTime, recurrence, frequency, ZoneOffset.UTC);
    		String sourceFolder = SettingsManager.getInstance().getAppDir();
        	String destZipFile = Paths.get(SettingsManager.getInstance().getBackupsDir(), generateBackupName()).toString();
        	String[] ignoredFiles = new String[] {SettingsManager.getInstance().getBackupsDir(), SettingsManager.getInstance().getSettingsPath()};
        	
    		BackupTask backupTask = new BackupTask(sourceFolder, destZipFile, ignoredFiles);
    		ScheduledBackupTask scheduledBackupTask = new ScheduledBackupTask(schedule, backupTask);
    		backupManager.scheduleBackupTask(scheduledBackupTask);
    	}
    	catch (InvalidDateTimeException e) {
    		Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error scheduling backup task", e.getMessage(), AlertType.ERROR);
			quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
			quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
			alertDialog.showAndWait();
    	}
    }

    @FXML
    void deleteBackup(ActionEvent event) {
    	FileBackup fileBackup = backupsTableView.getSelectionModel().getSelectedItem();
    	if (fileBackup != null) {
    		backupManager.deleteBackup(fileBackup);
    	}
    }

    @FXML
    void deleteScheduledBackupTask(ActionEvent event) {
    	ScheduledBackupTask scheduledBackupTask = scheduledBackupTasksTableView.getSelectionModel().getSelectedItem();
    	if (scheduledBackupTask != null) {
    		backupManager.deleteScheduledBackupTask(scheduledBackupTask);
    	}
    }

    @FXML
    void loadBackup(ActionEvent event) {
    	FileBackup fileBackup = backupsTableView.getSelectionModel().getSelectedItem();
    	if (fileBackup != null) {
    		if (!quickTextController.isEmptyTreeView()) {
    			Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "The root folder is not empty", "After loading the backup, all existing templates and folders will be deleted. Are you sure?", AlertType.CONFIRMATION);
				quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait().ifPresent(response -> {
    	    		if (response == ButtonType.OK) {
    	    			try {
							loadBackup(fileBackup);
						} catch (IOException e) {
							e.printStackTrace();
						}
    	    		}
    	    	});
    		}
    		else {
    			try {
					loadBackup(fileBackup);
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
    
    private void loadBackup(FileBackup fileBackup) throws IOException {
		//delete existing templates am xml folders
		quickTextController.deleteAllFolders();
		// replace with extracted backed up files
		ZipUtil.unzipFiles(fileBackup.getFile().toString(), SettingsManager.getInstance().getAppDir());
		quickTextController.initializeTreeView();
    }
    
	private String generateBackupName() {
		StringBuilder name = new StringBuilder();
		name.append("backup-");
		name.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.S")));
		name.append(".zip");
		return name.toString();
	}
    
    private void initializeBackupsTableView() {
    	backupsTableView.setItems(backupManager.getBackupsObsList());
    	setBackupsTableViewCellValueFactory();
    }
    
    private void initializeScheduledBackupTasksTableView() {
    	scheduledBackupTasksTableView.setItems(backupManager.getScheduledBackupTasksObsList());
    	setScheduledBackupTasksTableViewCellValueFactory();
    }
    
    private void initializeRadioButtons() {
    	noRecurrencyRadioButton.setUserData(Recurrence.NO_RECURRENT);
    	hoursRadioButton.setUserData(Recurrence.HOURLY);
    	daysRadioButton.setUserData(Recurrence.DAILY);
    }
    
    private void setBackupsTableViewCellValueFactory() {
    	backupFileNameColumn.setCellValueFactory(cellData -> {
    		return new SimpleStringProperty(cellData.getValue().getFile().getName());
    	});
    	
    	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    	
    	backupCreationDateTimeColumn.setCellValueFactory(cellData -> {
    		try {
    			LocalDateTime creationDateTime = cellData.getValue().getCreationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    			return new SimpleStringProperty(creationDateTime.format(dateTimeFormatter));
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		return new SimpleStringProperty("N/A");
    	});
    }
    
    private void setScheduledBackupTasksTableViewCellValueFactory() {
    	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    	nextBackupRunDateTimeColumn.setCellValueFactory(cellData -> {
    		ScheduledBackupTask scheduledBackupTask = cellData.getValue();
    		if (LocalDateTime.ofEpochSecond(scheduledBackupTask.getNextRun(), 0, scheduledBackupTask.getZoneOffset()).isBefore(LocalDateTime.now()))
    			return new SimpleStringProperty("N/A");
    		return new SimpleStringProperty(LocalDateTime.ofEpochSecond(scheduledBackupTask.getNextRun(), 0, scheduledBackupTask.getZoneOffset()).format(dateTimeFormatter));
    	});
    	
    	lastBackupRunDateTimeColumn.setCellValueFactory(cellData -> {
    		ScheduledBackupTask scheduledBackupTask = cellData.getValue();
    		LocalDateTime lastRun = LocalDateTime.ofEpochSecond(scheduledBackupTask.getLastRun(), 0, scheduledBackupTask.getZoneOffset());
    		if (lastRun.equals(LocalDateTime.ofEpochSecond(0, 0, scheduledBackupTask.getZoneOffset())))
    			return new SimpleStringProperty("N/A");
    		return new SimpleStringProperty(lastRun.format(dateTimeFormatter));
    	});
    	
    	lastBackupResultColumn.setCellValueFactory(cellData -> {
    		return new SimpleStringProperty(cellData.getValue().getLastResult().toString());
    	});
    	
    	backupRecurrenceColumn.setCellValueFactory(cellData -> {
    		return new SimpleStringProperty(cellData.getValue().getRecurrence().toString());
    	});
    }

    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			Files.createDirectories(new File(SettingsManager.getInstance().getBackupsDir()).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	initializeBackupsTableView();
    	initializeScheduledBackupTasksTableView();
    	initializeRadioButtons();
	}
}
