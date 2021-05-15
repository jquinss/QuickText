package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;
import java.util.Properties;

import control.FileTreeItem;
import data.FileItem;
import managers.FileManager;
import managers.SettingsManager;
import util.DialogBuilder;
import util.OSChecker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;


public class QuickTextController {

    @FXML
    private TreeView<FileItem> treeView;

    @FXML
    private TextArea textArea;

    @FXML
    private WebView webView;
    
    @FXML
    private MenuItem createFolderMenuItem;
    
    @FXML
    private MenuItem deleteTemplateMenuItem;
    
    @FXML
    private MenuItem deleteFolderMenuItem;
    
    @FXML
    private MenuItem deleteAllFoldersMenuItem;
    
    @FXML
    private Menu newTemplateMenu;
    
    @FXML
    private MenuItem importTemplateMenuItem;

    
    private Stage stage;
    
    private final FileManager fileManager = new FileManager();
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    public Stage getStage() {
    	return this.stage;
    }

    @FXML
    void createFolder(ActionEvent event) {
    	Dialog<Pair<String, String>> dialog = DialogBuilder.getTwoTextFieldInputDialog("Create folder", "Create a new folder:", "Folder name", 
    			"Description", true);
    	
    	Optional<Pair<String, String>> result = dialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String folderName = result.get().getKey();
    		String folderDescription = result.get().getValue();
    		
    		String templatesDir = getRootDirectory();
    		
    		try {
    			fileManager.createDir(folderName, templatesDir);
    			FileItem folderItem = new FileItem(new File(templatesDir + File.separator + folderName));
    			
    			if (!folderDescription.isEmpty()) {
    				folderItem.setDescription(folderDescription);
    			}
    			
    			FileTreeItem folderTreeItem = new FileTreeItem(folderItem);
    			setContextMenu(folderTreeItem);
    			treeView.getRoot().getChildren().add(folderTreeItem);
    		}
        	catch (FileAlreadyExistsException e) {
        		DialogBuilder.getAlertDialog("Error", "Error creating the folder", "There is already a folder with the same name", AlertType.ERROR).showAndWait();
        	}
    		catch (IOException e) {
    			DialogBuilder.getAlertDialog("Error", "Error creating the folder", "Folder cannot be created", AlertType.ERROR).showAndWait();
    		}
    	}
    }
    
    @FXML
    void deleteTemplate(ActionEvent event) {
    	// TO-DO
    	System.out.println("Deleting template");
    }
    
    @FXML
    void deleteFolder(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeView.getSelectionModel().getSelectedItem();
    	File selectedFolder = treeItem.getValue().getFile();
    	try {
			fileManager.removeDir(selectedFolder);
			treeView.getRoot().getChildren().remove(treeItem);
    	}
    	catch (DirectoryNotEmptyException e) {
    		Alert alertDialog = DialogBuilder.getAlertDialog("Confirmation", "The folder is not empty", "Are you sure you want to delete all the files?", AlertType.CONFIRMATION);
    		alertDialog.showAndWait().ifPresent(response -> {
    			if (response == ButtonType.OK) {
    				deleteAllFilesAndFolders(treeItem, true);
    			}
    		});;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    void deleteAllFolders(ActionEvent event) {
    	deleteAllFilesAndFolders(treeView.getRoot(), false);
    }

    @FXML
    void createHTMLTemplate(ActionEvent event) {
    	// TO-DO
    	System.out.println("Creating HTML template");
    }

    @FXML
    void createPlainTextTemplate(ActionEvent event) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("..\\view\\PlainTextEditor.fxml"));
		Parent parent = fxmlLoader.load();
		
		PlainTextEditorController plainTextEditorController = fxmlLoader.getController();
		
		Scene scene = new Scene(parent, 800, 600);
		scene.getStylesheets().add(getClass().getResource("..\\styles\\application.css").toExternalForm());
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Plain-Text Editor");
        
        TreeItem<FileItem> selectedTreeItem = treeView.getSelectionModel().getSelectedItem();
        
        plainTextEditorController.setStage(stage);
        plainTextEditorController.setFileManager(fileManager);
        plainTextEditorController.setFolderTreeItem(selectedTreeItem);
        plainTextEditorController.setQuickTextController(this);
        
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    @FXML
    void importTemplate(ActionEvent event) {
    	// TO-DO
    	System.out.println("Importing template");
    }

    @FXML
    void exitApplication(ActionEvent event) {
    	// TO-DO
    	System.out.println("Exiting application");
    }

    @FXML
    void saveTemplates(ActionEvent event) {
    	// TO-DO
    	System.out.println("Saving templates");
    }

    @FXML
    void showAboutMenu(ActionEvent event) {
    	// TO-DO
    	System.out.println("Showing about menu");
    }
    
    void copyTemplateToClipboard() {
    	// TO-DO
    	System.out.println("Copying template to clipboard");
    }
    
    void viewTemplate() {
    	// TO-DO
    	System.out.println("Viewing template");
    }

    void editTemplate() {
    	// TO-DO
    	System.out.println("Editing template");
    }

    @FXML
	public void initialize() {
		loadSettings();
		initializeTreeView();
	}
    
    private void loadSettings() {
    	Properties defaultSettings = getDefaultSettings();
    	SettingsManager.getInstance().loadSettings(defaultSettings);
    }
    
    private Properties getDefaultSettings() {
    	Properties settings = new Properties();
    	String appDir = OSChecker.getOSDataDirectory() + File.separator + "QuickText";
    	String templatesDir = appDir + File.separator + "templates";
    	String xmlDir = appDir + File.separator + "xml";
    	settings.setProperty("templates_dir", templatesDir);
    	settings.setProperty("xml_dir", xmlDir);
    	
    	return settings;
    }
    
    private void initializeTreeView() {
    	setRootDirectory();
    	setSelectedTreeItemListener();
    	setTreeViewCellFactory();
    }
    
    private void setRootDirectory() {
    	String templatesDir = getRootDirectory();
    	
    	try {
    		fileManager.createDirPath(templatesDir);
    		FileItem rootFileItem = new FileItem(new File(templatesDir));
        	rootFileItem.setIsRoot(true);
        	FileTreeItem rootTreeItem = new FileTreeItem(rootFileItem);
        	setContextMenu(rootTreeItem);
    		treeView.setRoot(rootTreeItem);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    }
    
    private String getRootDirectory() {
    	Properties settings = SettingsManager.getInstance().getSettings();
    	String templatesDir = settings.getProperty("templates_dir");
    	
    	return templatesDir;
    }
    
    private void setSelectedTreeItemListener() {
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
		    if (newValue != null) {
		    	if (newValue.getValue().isRootDirectory()) {
		    		setRootMenuItemsVisibility();
		    	}
		    	else if (newValue.getValue().isDirectory()) {
		    		setFolderMenuItemsVisibility();
		    	}
		    	else if (newValue.getValue().isFile()) {
		    		setFileMenuItemsVisibility();
		    	}
		    	
		    }
		    else {
		    	disableAllMenuItems();
		    }
		});
    }
    
    private void setRootMenuItemsVisibility() {
    	newTemplateMenu.setDisable(true);
		createFolderMenuItem.setDisable(false);
		deleteAllFoldersMenuItem.setDisable(false);
		importTemplateMenuItem.setDisable(true);
		deleteTemplateMenuItem.setDisable(true);
		deleteFolderMenuItem.setDisable(true);
    }
    
    private void setFolderMenuItemsVisibility() {
    	createFolderMenuItem.setDisable(true);
		newTemplateMenu.setDisable(false);
		importTemplateMenuItem.setDisable(false);
		deleteTemplateMenuItem.setDisable(true);
		deleteFolderMenuItem.setDisable(false);
    }
    
    private void setFileMenuItemsVisibility() {
    	createFolderMenuItem.setDisable(true);
		newTemplateMenu.setDisable(true);
		deleteAllFoldersMenuItem.setDisable(true);
		importTemplateMenuItem.setDisable(true);
		deleteTemplateMenuItem.setDisable(false);
		deleteFolderMenuItem.setDisable(true);
    }
    
    private void disableAllMenuItems() {
    	createFolderMenuItem.setDisable(true);
    	newTemplateMenu.setDisable(true);
		deleteAllFoldersMenuItem.setDisable(true);
		importTemplateMenuItem.setDisable(true);
		deleteTemplateMenuItem.setDisable(true);
		deleteFolderMenuItem.setDisable(true);
    }
    
    private void setTreeViewCellFactory() {
		treeView.setCellFactory(new Callback<TreeView<FileItem>,TreeCell<FileItem>>() {
			@Override
			public TreeCell<FileItem> call(TreeView<FileItem> p){
				TreeCell<FileItem> cell = new TreeCell<FileItem>() {
					@Override
					protected void updateItem(FileItem fileItem, boolean empty) {
						super.updateItem(fileItem, empty);
						
						if (empty) {
							setText(null);
						}
						else {
							setText(fileItem.getFile().getName());
							setContextMenu(((FileTreeItem) getTreeItem()).getContextMenu());
						}
					}
				};
							
				return cell;
			}
		});
	}
    
    void setContextMenu(FileTreeItem fileTreeItem) {
    	FileItem fileItem = fileTreeItem.getValue();
    	ContextMenu contextMenu = buildContextMenu(fileItem);
    	fileTreeItem.setContextMenu(contextMenu);
    }
    
    private ContextMenu buildContextMenu(FileItem fileItem) {
    	ContextMenu contextMenu = new ContextMenu();
    	
    	if (fileItem.isRootDirectory()) { 
    		setRootDirContextMenu(contextMenu);
    	}
    	else if (fileItem.isDirectory()) {
    		setDirContextMenu(contextMenu);
    	}
    	else if (fileItem.isFile()) {
    		setFileContextMenu(contextMenu);
    	}
    	
    	return contextMenu;
    }
    
    private void setRootDirContextMenu(ContextMenu contextMenu) {
    	MenuItem addFolder = new MenuItem("Add Folder");
		MenuItem removeFolders = new MenuItem("Remove All Folders");
		
		addFolder.setOnAction(e -> createFolder(e));
		removeFolders.setOnAction(e -> deleteAllFolders(e));
		
		contextMenu.getItems().addAll(addFolder, removeFolders);
    }
    
    private void setDirContextMenu(ContextMenu contextMenu) {
		Menu createTemplateMenu = new Menu("Create Template...");
		MenuItem createPlainTextTemplateItem = new MenuItem("Plain-Text Template");
		MenuItem createHTMLTemplateItem = new MenuItem("HTML Template");
		MenuItem deleteFolderItem = new MenuItem("Delete");
		
		createPlainTextTemplateItem.setOnAction(e -> {
			try {
				createPlainTextTemplate(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		createHTMLTemplateItem.setOnAction(e -> createHTMLTemplate(e));
		deleteFolderItem.setOnAction(e -> deleteFolder(e));
		
		createTemplateMenu.getItems().addAll(createPlainTextTemplateItem, createHTMLTemplateItem);
		contextMenu.getItems().addAll(createTemplateMenu, deleteFolderItem);
    }
    
    private void setFileContextMenu(ContextMenu contextMenu) {
    	MenuItem copyTemplateItem = new MenuItem("Copy To Clipboard");
    	MenuItem viewTemplateItem = new MenuItem("View");
    	MenuItem editTemplateItem = new MenuItem("Edit");
		MenuItem deleteTemplateItem = new MenuItem("Delete");
		
		copyTemplateItem.setOnAction(e -> copyTemplateToClipboard());
		viewTemplateItem.setOnAction(e -> viewTemplate());
		editTemplateItem.setOnAction(e -> editTemplate());
		deleteTemplateItem.setOnAction(e -> deleteTemplate(e));
		
		contextMenu.getItems().addAll(copyTemplateItem, viewTemplateItem, editTemplateItem, deleteTemplateItem);
    }
    
    private void deleteAllFilesAndFolders(TreeItem<FileItem> startTreeItem, boolean includeStartTreeItem) {
    	File startDirectory = startTreeItem.getValue().getFile();
    	
    	try {
    		fileManager.deleteFileTree(startDirectory, includeStartTreeItem);
    		if (includeStartTreeItem) {
    			startTreeItem.getParent().getChildren().remove(startTreeItem);
    		}
    		else {
    			startTreeItem.getChildren().clear();
    		}
    		
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
