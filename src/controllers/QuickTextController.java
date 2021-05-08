package controllers;

import java.io.File;
import java.util.Properties;

import control.FileTreeItem;
import data.FileItem;
import managers.FileManager;
import managers.SettingsManager;
import util.OSChecker;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;


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
    	System.out.println("Creating folder");
    }
    
    @FXML
    void deleteTemplate(ActionEvent event) {
    	System.out.println("Deleting template");
    }
    
    @FXML
    void deleteFolder(ActionEvent event) {
    	System.out.println("Deleting folder");
    }
    
    @FXML
    void deleteAllFolders(ActionEvent event) {
    	System.out.println("Deleting all folders");
    }

    @FXML
    void createHTMLTemplate(ActionEvent event) {

    }

    @FXML
    void createPlainTextTemplate(ActionEvent event) {

    }
    
    @FXML
    void importTemplate(ActionEvent event) {
    	
    }

    @FXML
    void exitApplication(ActionEvent event) {

    }

    @FXML
    void saveTemplates(ActionEvent event) {

    }

    @FXML
    void showAboutMenu(ActionEvent event) {

    }
    
    void copyTemplateToClipboard() {
    	
    }
    
    void viewTemplate() {
    	
    }

    void editTemplate() {
    	
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
    	setSelectedTreeItemBindings();
    	setTreeViewCellFactory();
    }
    
    private void setRootDirectory() {
    	Properties settings = SettingsManager.getInstance().getSettings();
    	File rootFile = new File(settings.getProperty("templates_dir"));
    	FileItem rootFileItem = new FileItem(rootFile);
    	rootFileItem.setIsRoot(true);
    	
    	FileTreeItem rootTreeItem = new FileTreeItem(rootFileItem);
    	ContextMenu contextMenu = buildContextMenu(rootTreeItem);
		rootTreeItem.setValue(rootFileItem);
		rootTreeItem.setContextMenu(contextMenu);
		treeView.setRoot(rootTreeItem);
    }
    
    private void setSelectedTreeItemBindings() {
    	ObjectProperty<FileItem> selectedTreeItem = new SimpleObjectProperty<>();
    	
		selectedTreeItem.bind(Bindings.createObjectBinding(() -> {
		    TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
		    return selectedItem == null ?  null : selectedItem.getValue();
		}, treeView.getSelectionModel().selectedItemProperty()));
		
		selectedTreeItem.addListener((obs, oldValue, newValue) -> {
		    if (newValue != null) {
		    	if (newValue.isRootDirectory()) {
		    		setRootMenuItemsVisibility();
		    	}
		    	if (newValue.isDirectory()) {
		    		setFolderMenuItemsVisibility();
		    	}
		    	else if (newValue.isFile()) {
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
    
    private ContextMenu buildContextMenu(TreeItem<FileItem> treeItem) {
    	ContextMenu contextMenu = new ContextMenu();
    	
    	FileItem fileItem = treeItem.getValue();
    	
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
		
		createPlainTextTemplateItem.setOnAction(e -> createPlainTextTemplate(e));
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
}
