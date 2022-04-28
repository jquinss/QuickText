package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import data.FileBackup;
import util.ZipUtil;
import managers.FileManager;
import managers.SettingsManager;

public class BackupsPaneController {

    @FXML
    private TableView<FileBackup> backupsTableView;

    @FXML
    private TableView<?> scheduledBackupTasksTableView;
    
    @FXML
    private TableColumn<FileBackup, String> fileNameColumn;
    
    @FXML
    private TableColumn<FileBackup, String> dateTimeColumn;

    @FXML
    private DatePicker scheduledDateTime;

    @FXML
    private Spinner<?> hoursSpinner;

    @FXML
    private Spinner<?> daysSpinner;
    
    private QuickTextController quickTextController;
    
    private Stage stage;
    
    private final ObservableList<FileBackup> fileBackupObsList = FXCollections.observableArrayList();
    
    private final FileManager fileManager = new FileManager();
    
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
    	FileBackup fileBackup = backupsTableView.getSelectionModel().getSelectedItem();
    	if (fileBackup != null) {
    		try {
				Files.deleteIfExists(fileBackup.getFile().toPath());
				fileBackupObsList.remove(fileBackup);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }

    @FXML
    void deleteScheduledBackupTask(ActionEvent event) {
    	System.out.println("Deleting a backup task");
    	/* TO DO */
    }

    @FXML
    void loadBackup(ActionEvent event) {
    	FileBackup fileBackup = backupsTableView.getSelectionModel().getSelectedItem();
    	if (fileBackup != null) {
    		try {
    			//delete existing templates am xml folders
    			fileManager.deleteFileTree(new File(SettingsManager.getInstance().getTemplatesDir()), true);
    			fileManager.deleteFileTree(new File(SettingsManager.getInstance().getXMLDir()), true);
    			// replace with extracted backed up files
				ZipUtil.unzipFiles(fileBackup.getFile().toString(), SettingsManager.getInstance().getAppDir());
				quickTextController.initializeTreeView();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    private void loadBackupFiles() {
    	fileBackupObsList.clear();
    	try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(SettingsManager.getInstance().getBackupDataPath()))) {
    		List<FileBackup> backupData = (ArrayList<FileBackup>) input.readObject();
    		
    		for (FileBackup file : backupData) {
    			// only import files that exist and had not been modified
    			if (file.getFile().exists() && file.getFileSHA256().equals(file.getOrigFileSHA256())) {
    				fileBackupObsList.add(file);
    			}
    		}
    	}
    	catch (FileNotFoundException e) {
    		// if file does not exist, ignore the error
    	}
    	catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
    
	private String generateBackupName() {
		StringBuilder name = new StringBuilder();
		name.append("backup-");
		name.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.S")));
		name.append(".zip");
		return name.toString();
	}
	
	void saveBackupData() {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(SettingsManager.getInstance().getBackupDataPath()))) {
			output.writeObject(new ArrayList<FileBackup>(fileBackupObsList));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    @FXML
    public void initialize() {
    	try {
			Files.createDirectories(new File(SettingsManager.getInstance().getBackupsDir()).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    	initializeTableView();
    }
    
    private void initializeTableView() {
    	backupsTableView.setItems(fileBackupObsList);
    	setTableViewCellValueFactory();
    }
    
    private void setTableViewCellValueFactory() {
    	fileNameColumn.setCellValueFactory(cellData -> {
    		return new SimpleStringProperty(cellData.getValue().getFile().getName());
    	});
    	
    	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    	
    	dateTimeColumn.setCellValueFactory(cellData -> {
    		try {
    			LocalDateTime creationDateTime = cellData.getValue().getCreationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    			return new SimpleStringProperty(creationDateTime.format(dateTimeFormatter));
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    		
    		return new SimpleStringProperty("<no data>");
    	});
    	
    	loadBackupFiles();
    }

    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    }
    
    void setStage(Stage stage) {
    	this.stage = stage;
    }
}
