package com.jquinss.quicktext.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.jquinss.quicktext.enums.Charsets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jquinss.quicktext.util.HtmlToPlainText;
import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.data.FolderItem;
import com.jquinss.quicktext.data.HTMLTemplateItem;
import com.jquinss.quicktext.data.PlainTextTemplateItem;
import com.jquinss.quicktext.data.RootFolderItem;
import com.jquinss.quicktext.data.TemplateItem;
import com.jquinss.quicktext.managers.BackupManager;
import com.jquinss.quicktext.managers.CacheManager;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.ReusableTextManager;
import com.jquinss.quicktext.managers.SettingsManager;
import com.jquinss.quicktext.util.DialogBuilder;
import com.jquinss.quicktext.util.StringCache;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;


public class QuickTextController {

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
    private MenuItem createPlainTextTemplateMenuItem;
    
    @FXML
    private MenuItem createHTMLTemplateMenuItem;
    
    @FXML
    private MenuItem duplicateTemplateMenuItem;
    
    @FXML
    private MenuItem copyTemplateToClipboardMenuItem;
    
    @FXML
    private MenuItem importTemplateMenuItem;
    
    @FXML
    private MenuItem editTemplateMenuItem;

	@FXML
	private Button createPlainTextTemplateToolbarButton;

	@FXML
	private Button createHTMLTemplateToolbarButton;

	@FXML
	private Button deleteTemplateToolbarButton;

	@FXML
	private Button duplicateTemplateToolbarButton;

	@FXML
	private Button viewTemplateToolbarButton;

	@FXML
	private Button editTemplateToolbarButton;

	@FXML
	private Button copyTemplateToClipboardToolbarButton;

	@FXML
	private Button createFolderToolbarButton;

	@FXML
	private Button deleteFolderToolbarButton;

	@FXML
	private ImageView createPlainTextTemplateImage;

	@FXML
	private ImageView createHTMLTemplateImage;

	@FXML
	private ImageView deleteTemplateImage;

	@FXML
	private ImageView duplicateTemplateImage;

	@FXML
	private ImageView viewTemplateImage;

	@FXML
	private ImageView editTemplateImage;

	@FXML
	private ImageView copyTemplateToClipboardImage;

	@FXML
	private ImageView createFolderImage;

	@FXML
	private ImageView deleteFolderImage;

	@FXML
	private ImageView settingsImage;

	@FXML
	private ImageView backupsManagerImage;

	@FXML
	private ImageView reusableTextImage;

    @FXML
    private ScrollPane detailsPane;
    
    @FXML
    private VBox viewDescriptionPane;
    
    @FXML
    private VBox editDescriptionPane;

    @FXML
    private Text descriptionText;
    
    @FXML
    private TextField descriptionTextField;
    
    @FXML
    private TreeViewPaneController treeViewPaneController;
    
    private File initialImportTemplateDirectory;
    
    private Stage stage;
    
    private CacheManager cacheManager;
    
    private final FileManager fileManager = new FileManager();
    
    private final BackupManager backupManager = new BackupManager();
    
    private final ReusableTextManager reusableTextManager = new ReusableTextManager();
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    	this.stage.setOnCloseRequest(e -> handleStageClosure(e));
    }
    
    public Stage getStage() {
    	return this.stage;
    }

    @FXML
    void createFolder(ActionEvent event) {
    	Dialog<Pair<String, String>> dialog = DialogBuilder.buildTwoTextFieldInputDialog("Create folder", "Create a new folder:", "Folder name", 
    			"Description", true);
		setLogo(dialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
		setStyle(dialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
    	
    	Optional<Pair<String, String>> result = dialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String folderName = result.get().getKey();
    		String folderDescription = result.get().getValue();
    		
    		try {
    			treeViewPaneController.createFolder(folderName, folderDescription);
    		}
        	catch (FileAlreadyExistsException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error creating the folder", "There is already a folder with the same name", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
        	}
    		catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error creating the folder", "Folder cannot be created", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
    		}
    	}
    }
    
    @FXML
    void deleteTemplate(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	
    	try {
			treeViewPaneController.deleteTemplate(treeItem);
    	}
    	catch (IOException e) {
			Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error removing the file", "An error has occurred while trying to remove the file", AlertType.ERROR);
			setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
			setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
			alertDialog.showAndWait();
    	}
    }
    
    @FXML
    void deleteFolder(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	
    	try {
			treeViewPaneController.deleteFolder(treeItem);
    	}
    	catch (DirectoryNotEmptyException e) {
    		Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "The folder is not empty", "Are you sure you want to delete all the files?", AlertType.CONFIRMATION);
			setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
			setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
    		alertDialog.showAndWait().ifPresent(response -> {
    			if (response == ButtonType.OK) {
    				treeViewPaneController.deleteAllFilesAndFolders(treeItem, true);
    			}
    		});;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void renameFolder() {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	String folderName = treeItem.getValue().getFile().getName();
    	
    	Dialog<String> inputDialog = DialogBuilder.buildSingleTextFieldInputDialog("Rename folder", "Rename the selected folder", "Name of the folder", 
    																				folderName);
		setStyle(inputDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
		setLogo(inputDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());

    	Optional<String> result = inputDialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String newFolderName = result.get();
    		
    		try {
    			treeViewPaneController.renameFolder(treeItem, newFolderName);
    		} catch (FileAlreadyExistsException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error renaming the folder", "The folder " + e.getFile() + " already exists", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			} catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error renaming the folder", "An error has occurred when renaming the folder", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}	
    }
    
    @FXML
    void deleteAllFolders(ActionEvent event) {
    	if (!isEmptyTreeView()) {
    		Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "The folder is not empty", "Are you sure you want to delete all the files and folders?", AlertType.CONFIRMATION);
			setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
			setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
			alertDialog.showAndWait().ifPresent(response -> {
    			if (response == ButtonType.OK) {
    				deleteAllFolders();
    			}
    		});
    	}
    }
    
    void deleteAllFolders() {
    	treeViewPaneController.deleteAllFolders();
		clearCache();
    }

    @FXML
    void createHTMLTemplate(ActionEvent event) {
    	try {
			openHTMLEditor(treeViewPaneController.getSelectedTreeItem());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void createPlainTextTemplate(ActionEvent event) {
    	try {
    		openPlainTextEditor(treeViewPaneController.getSelectedTreeItem());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    void createTemplate(File file, String description, TreeItem<FileItem> folderTreeItem) {
    	treeViewPaneController.createTemplate(file, description, folderTreeItem);
    }
    
    @FXML
    void importTemplates(ActionEvent event) {
    	FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Templates", "*.txt", "*.html");
    	FileChooser fileChooser = DialogBuilder.buildFileChooser("Select a template", extensions);
    	
    	if (initialImportTemplateDirectory != null) {
    		fileChooser.setInitialDirectory(initialImportTemplateDirectory);
    	}
    	
    	List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
    	
    	if (selectedFiles != null) {
    		initialImportTemplateDirectory = selectedFiles.get(0).getParentFile();
    		
    		TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    		try {
    			treeViewPaneController.importTemplates(treeItem, selectedFiles);
			} catch (FileAlreadyExistsException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error importing the file", "The file " + e.getFile() + " already exists", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			} catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error copying the file", "An error has occurred when copying the file", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}
    }
    
    @FXML
    void copyTemplateToClipboard(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	FileItem fileItem = treeItem.getValue();
    	File file = fileItem.getFile();
    	
    	try {
        	String text = readTextFromFile(file);
        	
        	if (fileItem instanceof PlainTextTemplateItem) {
        		content.putString(text);
        	}
        		
        	if (fileItem instanceof HTMLTemplateItem) {
        		HtmlToPlainText formatter = new HtmlToPlainText();
        		Document doc = Jsoup.parse(text);
        		String plainText = formatter.getPlainText(doc).trim();
        		content.putHtml(text);
        		content.putString(plainText);
        	}
        	
        	clipboard.setContent(content);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @FXML
    void editTemplate(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	FileItem selectedFileItem = treeItem.getValue();
    	try {
    		if (selectedFileItem instanceof PlainTextTemplateItem) {
    			openPlainTextEditor(treeItem);
    		}
    		else if (selectedFileItem instanceof HTMLTemplateItem) { 
    			openHTMLEditor(treeItem);
    		}
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @FXML
    void duplicateTemplate(ActionEvent event) {
    	TreeItem<FileItem> selectedTreeItem = treeViewPaneController.getSelectedTreeItem();
    	TreeItem<FileItem> folderTreeItem = selectedTreeItem.getParent();
    	
    	File srcFile = selectedTreeItem.getValue().getFile();
    	String dirName = srcFile.getParent();
    	String fileName = srcFile.getName();
    	// auto-generates a name for the duplicated file
    	String suffix = fileManager.getExtensionFromFile(fileName);
    	String prefix = fileManager.removeFileExtension(fileName, suffix);
    	String nextPrefix = fileManager.getNextAvailableFileName(dirName, prefix, suffix);
    	
    	Dialog<String> inputDialog = DialogBuilder.buildSingleTextFieldInputDialog("Duplicate template", "Create a new duplicate template", "Name of the template", 
    																				nextPrefix);
		setLogo(inputDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
		setStyle(inputDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
    	
    	Optional<String> result = inputDialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String newPrefix = result.get();
    		File destFile = fileManager.buildFilePath(dirName, newPrefix, suffix);
    		try {
    			treeViewPaneController.copyTemplate(srcFile, destFile, folderTreeItem);
    		} catch (FileAlreadyExistsException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error copying the file", "The file " + e.getFile() + " already exists", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			} catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error copying the file", "An error has occurred when copying the file", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}	
    }

    @FXML
    void exitApplication(ActionEvent event) {
    	saveTreeView();
    	cancelScheduledBackupTasks();
    	shutdownExecutors();
    	saveBackups();
    	saveReusableText();
    	stage.close();
    }

    @FXML
    void showAboutDialog(ActionEvent event) {
    	Alert aboutDialog = DialogBuilder.buildAlertDialog("About", "", "QuickText v1.0\n\nCreated by Joaquin Sampedro", AlertType.INFORMATION);
		setLogo(aboutDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
		setStyle(aboutDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
		aboutDialog.showAndWait();
    }
    
    @FXML
    void saveDescription(ActionEvent event) {
    	String description = descriptionTextField.getText().trim();
    	if (!description.isEmpty()) {
        	FileItem fileItem = treeViewPaneController.getSelectedTreeItem().getValue();
        	fileItem.setDescription(description);
        	viewFileDetails(fileItem);
    	}
    }
    
    @FXML
	void openSettingsDialog(ActionEvent event) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/SettingsPane.fxml"));
		Parent parent = fxmlLoader.load();
		
		SettingsPaneController settingsPaneController = fxmlLoader.getController();
		settingsPaneController.setQuickTextController(this);
		
		Scene scene = new Scene(parent, 450, 250);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Settings");
		setLogo(stage, SettingsManager.getInstance().getLogoPath());
		setStyle(scene, SettingsManager.getInstance().getCSSPath());
        
        settingsPaneController.setStage(stage);
        
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
	}
    
    @FXML
    void openBackupsPane(ActionEvent event) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/BackupsPane.fxml"));
    	
		BackupsPaneController backupsPaneController = new BackupsPaneController(backupManager);
		backupsPaneController.setQuickTextController(this);
		
		fxmlLoader.setController(backupsPaneController);
		
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent, 450, 500);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Manage backups");
		setLogo(stage, SettingsManager.getInstance().getLogoPath());
		setStyle(scene, SettingsManager.getInstance().getCSSPath());
        
       	stage.setOnCloseRequest(e -> {
        	this.saveBackups();
        });
        
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    @FXML
    void openReusableTextPane(ActionEvent event) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/ReusableTextManagerPane.fxml"));
    	
		ReusableTextPaneController reusableTextPaneController = new ReusableTextPaneController(reusableTextManager);
		fxmlLoader.setController(reusableTextPaneController);
		
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent, 400, 320);

        Stage stage = new Stage();
        reusableTextPaneController.setStage(stage);
        stage.setResizable(false);
        stage.setTitle("Reusable Text Manager");
		setLogo(stage, SettingsManager.getInstance().getLogoPath());
		setStyle(scene, SettingsManager.getInstance().getCSSPath());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

	@FXML
    void viewTemplate() {
    	TreeItem<FileItem> treeItem = treeViewPaneController.getSelectedTreeItem();
    	try {
    		FileItem fileItem = treeItem.getValue();
        	if (fileItem instanceof PlainTextTemplateItem) {
        		viewPlainTextTemplate(fileItem.getFile());
        	}
        	else if (fileItem instanceof HTMLTemplateItem) {
        		viewHTMLTemplate(fileItem.getFile());
        	}
		} catch (IOException e) {
			e.printStackTrace();
		} 	
    }
    
    private void renameTemplate() {
    	TreeItem<FileItem> selectedTreeItem = treeViewPaneController.getSelectedTreeItem();
    	TreeItem<FileItem> folderTreeItem = selectedTreeItem.getParent();
    	
    	File srcFile = selectedTreeItem.getValue().getFile();
    	String dirName = srcFile.getParent();
    	String fileName = srcFile.getName();
    	String suffix = fileManager.getExtensionFromFile(fileName);
    	String prefix = fileManager.removeFileExtension(fileName, suffix);
    	
    	Dialog<String> inputDialog = DialogBuilder.buildSingleTextFieldInputDialog("Rename template", "Rename the selected template", "Name of the template", 
    																				prefix);
		setLogo(inputDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
		setStyle(inputDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
    	
    	Optional<String> result = inputDialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String newPrefix = result.get();
    		File destFile = fileManager.buildFilePath(dirName, newPrefix, suffix);
    		try {
    			treeViewPaneController.moveTemplate(srcFile, destFile, selectedTreeItem, folderTreeItem, folderTreeItem);
    		} catch (FileAlreadyExistsException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error renaming the file", "The file " + e.getFile() + " already exists", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			} catch (IOException e) {
				Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error renaming the file", "An error has occurred when renaming the file", AlertType.ERROR);
				setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
				setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
				alertDialog.showAndWait();
			}
    	}	
    }
    
    @FXML
	public void initialize() {;
		initializeToolBar();
		initializeCache();
		initializeTreeView();
		initializeDescriptionPane();
		initializeBackups();
		initializeReusableText();
	}

	private void initializeToolBar() {
		createPlainTextTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/create_plaintext_template.png").toString()));
		createHTMLTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/create_html_template.png").toString()));
		deleteTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/delete_template.png").toString()));
		duplicateTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/duplicate_template.png").toString()));
		viewTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/view_template.png").toString()));
		editTemplateImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/edit_template.png").toString()));
		copyTemplateToClipboardImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/copy_clipboard.png").toString()));
		createFolderImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/create_folder.png").toString()));
		deleteFolderImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/delete_folder.png").toString()));
		settingsImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/settings.png").toString()));
		backupsManagerImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/backups_manager.png").toString()));
		reusableTextImage.setImage(new Image(getClass().getResource("/com/jquinss/quicktext/images/reusable_text.png").toString()));
	}
    
    private void initializeCache() {
    	String cacheMaxItems = (String) SettingsManager.getInstance().getCacheMaxItems();
    	cacheManager = new CacheManager(new StringCache(Integer.parseInt(cacheMaxItems)));
    }
    
    void initializeTreeView() {
    	treeViewPaneController.setQuickTextController(this);
    	try {
			treeViewPaneController.initializeTreeView();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    void saveTreeView() {
    	try {
			treeViewPaneController.saveTreeViewToXML();
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
    
    boolean isEmptyTreeView() {
    	return treeViewPaneController.isEmptyTreeView();
    }
    
    private void initializeDescriptionPane() {
    	descriptionText.wrappingWidthProperty().bind(editDescriptionPane.widthProperty());
    }
    
    private void initializeBackups() {
    	backupManager.loadBackupFiles(SettingsManager.getInstance().getBackupDataPath());
    	backupManager.loadScheduledBackupTasks(SettingsManager.getInstance().getScheduledBackupDataPath());
    	backupManager.scheduleBackupTasks();
    }
    
    private void initializeReusableText() {
    	try {
			reusableTextManager.loadReusableText(SettingsManager.getInstance().getReusableTextDataPath());
    	} catch (FileNotFoundException e) {
    		// if the file is not found, ignore
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void disableRootRelatedMenuItems(boolean disable) {
    	deleteAllFoldersMenuItem.setDisable(disable);
    	createFolderMenuItem.setDisable(disable);
    }
    
    private void disableFolderRelatedMenuItems(boolean disable) {
    	createPlainTextTemplateMenuItem.setDisable(disable);
    	createHTMLTemplateMenuItem.setDisable(disable);
		importTemplateMenuItem.setDisable(disable);
		deleteFolderMenuItem.setDisable(disable);
    }
    
    private void disableTemplateRelatedMenuItems(boolean disable) {
		deleteTemplateMenuItem.setDisable(disable);
		copyTemplateToClipboardMenuItem.setDisable(disable);
		duplicateTemplateMenuItem.setDisable(disable);
		editTemplateMenuItem.setDisable(disable);
    }
    
    void enableRootRelatedMenuItems() {
    	disableRootRelatedMenuItems(false);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(true);
    }
    
    void enableFolderRelatedMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(false);
    	disableTemplateRelatedMenuItems(true);
    }
    
    void enableTemplateRelatedMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(false);
    }
    
    void disableAllMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(true);
    }

	private void disableRootRelatedToolbarButtons(boolean disable) {
		createFolderToolbarButton.setDisable(disable);
	}

	private void disableFolderRelatedToolBarButtons(boolean disable) {
		createPlainTextTemplateToolbarButton.setDisable(disable);
		createHTMLTemplateToolbarButton.setDisable(disable);
		deleteFolderToolbarButton.setDisable(disable);
	}

	private void disableTemplateRelatedToolbarButtons(boolean disable) {
		deleteTemplateToolbarButton.setDisable(disable);
		duplicateTemplateToolbarButton.setDisable(disable);
		viewTemplateToolbarButton.setDisable(disable);
		editTemplateToolbarButton.setDisable(disable);
		copyTemplateToClipboardToolbarButton.setDisable(disable);
	}

	void disableAllToolbarButtons() {
		disableRootRelatedToolbarButtons(true);
		disableFolderRelatedToolBarButtons(true);
		disableTemplateRelatedToolbarButtons(true);
	}

	void enableRootRelatedToolbarButtons() {
		disableRootRelatedToolbarButtons(false);
		disableFolderRelatedToolBarButtons(true);
		disableTemplateRelatedToolbarButtons(true);
	}

	void enableFolderRelatedToolbarButtons() {
		disableRootRelatedToolbarButtons(true);
		disableFolderRelatedToolBarButtons(false);
		disableTemplateRelatedToolbarButtons(true);
	}

	void enableTemplateRelatedToolbarButtons() {
		disableRootRelatedToolbarButtons(true);
		disableFolderRelatedToolBarButtons(true);
		disableTemplateRelatedToolbarButtons(false);
	}
    
    void hideAllViewAreas() {
    	hideTextArea();
    	hideWebView();
    	hideDescriptionPanes();
    }
    
    private void hideTextArea() {
    	textArea.setVisible(false);
    }
    
    private void showTextArea() {
    	textArea.setVisible(true);
    }
    
    private void hideWebView() {
    	webView.setVisible(false);
    }
    
    private void showWebView() {
    	webView.setVisible(true);
    }
    
    private void hideEditDescriptionPane() {
    	editDescriptionPane.setVisible(false);
    }
    
    private void showEditDescriptionPane() {
    	editDescriptionPane.setVisible(true);
    }
    
    private void hideViewDescriptionPane() {
    	viewDescriptionPane.setVisible(false);
    }
    
    private void showViewDescriptionPane() {
    	viewDescriptionPane.setVisible(true);
    }
    
    private void hideDescriptionPanes() {
    	hideViewDescriptionPane();
    	hideEditDescriptionPane();
    }
    
    // inner class that builds the context menus based on the type of element (root folder, non-root folder, template)
    class ContextMenuBuilder {
    	ContextMenu buildContextMenu(FileItem fileItem) {
    		ContextMenu contextMenu = null;
    	
    		if (fileItem instanceof RootFolderItem) {
    			contextMenu = new RootFolderContextMenu();
    		}
    		else if (fileItem instanceof FolderItem) {
    			contextMenu = new FolderContextMenu();
    		}
    		else if (fileItem instanceof TemplateItem) {
    			contextMenu = new FileContextMenu();
    		}
    	
    		return contextMenu;
    	}
    }
    
    private class RootFolderContextMenu extends ContextMenu {
    	MenuItem addFolder = new MenuItem("Add Folder");
		MenuItem removeFolders = new MenuItem("Delete All Folders");
		
		RootFolderContextMenu() {
			addFolder.setOnAction(e -> createFolder(e));
			removeFolders.setOnAction(e -> deleteAllFolders(e));
			getItems().addAll(addFolder, removeFolders);
		}
    }
    
    private class FolderContextMenu extends ContextMenu {
		Menu createTemplateMenu = new Menu("Create Template...");
		MenuItem createPlainTextTemplateMenuItem = new MenuItem("Plain-Text Template");
		MenuItem createHTMLTemplateMenuItem = new MenuItem("HTML Template");
		MenuItem importTemplatesMenuItem = new MenuItem("Import Templates...");
		MenuItem renameFolderMenuItem = new MenuItem("Rename");
		MenuItem deleteFolderMenuItem = new MenuItem("Delete");
		
		FolderContextMenu() {
			createPlainTextTemplateMenuItem.setOnAction(e -> createPlainTextTemplate(e));
			createHTMLTemplateMenuItem.setOnAction(e -> createHTMLTemplate(e));
			importTemplatesMenuItem.setOnAction(e -> importTemplates(e));
			renameFolderMenuItem.setOnAction(e -> renameFolder());
			deleteFolderMenuItem.setOnAction(e -> deleteFolder(e));
			
			createTemplateMenu.getItems().addAll(createPlainTextTemplateMenuItem, createHTMLTemplateMenuItem);
			getItems().addAll(createTemplateMenu, importTemplatesMenuItem, renameFolderMenuItem, deleteFolderMenuItem);
		}
    }
    
    private class FileContextMenu extends ContextMenu {
    	MenuItem copyTemplateItem = new MenuItem("Copy To Clipboard");
    	MenuItem duplicateTemplateItem = new MenuItem("Duplicate");
    	MenuItem viewTemplateItem = new MenuItem("View");
    	MenuItem editTemplateItem = new MenuItem("Edit");
    	MenuItem renameTemplateItem = new MenuItem("Rename");
		MenuItem deleteTemplateItem = new MenuItem("Delete");
		
		FileContextMenu() {
			copyTemplateItem.setOnAction(e -> copyTemplateToClipboard(e));
			duplicateTemplateItem.setOnAction(e -> duplicateTemplate(e));
			viewTemplateItem.setOnAction(e -> viewTemplate());
			editTemplateItem.setOnAction(e -> editTemplate(e));
			deleteTemplateItem.setOnAction(e -> deleteTemplate(e));
			renameTemplateItem.setOnAction(e -> renameTemplate());
			
			getItems().addAll(copyTemplateItem, viewTemplateItem, duplicateTemplateItem, editTemplateItem, renameTemplateItem, deleteTemplateItem);
		}
    }
    
   void openPlainTextEditor(TreeItem<FileItem> selectedTreeItem) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/PlainTextEditor.fxml"));
    	File root = new File(SettingsManager.getInstance().getTemplatesDir());
    	TextEditorController plainTextEditorController = new PlainTextEditorController(selectedTreeItem, fileManager, root);
    	fxmlLoader.setController(plainTextEditorController);
 
		Parent parent = fxmlLoader.load();
		
		Scene scene = buildTextEditorScene(parent);
        Stage stage = buildTextEditorStage("Plain-text editor", scene);
	   	setLogo(stage, SettingsManager.getInstance().getLogoPath());
        
        initializeTextEditorController(plainTextEditorController, stage);
        
        stage.showAndWait();
    }
    
    void openHTMLEditor(TreeItem<FileItem> selectedTreeItem) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/jquinss/quicktext/fxml/HTMLTextEditor.fxml"));
    	File root = new File(SettingsManager.getInstance().getTemplatesDir());
    	HTMLEditorController htmlEditorController = new HTMLEditorController(selectedTreeItem, fileManager, root);
    	fxmlLoader.setController(htmlEditorController);
    	
		Parent parent = fxmlLoader.load();
		
		Scene scene = buildTextEditorScene(parent);
        Stage stage = buildTextEditorStage("HTML editor", scene);
		setLogo(stage, SettingsManager.getInstance().getLogoPath());
        
        initializeTextEditorController(htmlEditorController, stage);
        
        stage.showAndWait();
    }
    
    private Scene buildTextEditorScene(Parent parent) {
    	Scene scene = new Scene(parent, 800, 600);
		scene.getStylesheets().add(getClass().getResource("/com/jquinss/quicktext/styles/application.css").toString());
		
		return scene;
    }
    
    private Stage buildTextEditorStage(String title, Scene scene) {
    	Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        
        return stage;
    }
    
    private void initializeTextEditorController(TextEditorController textEditorController, Stage stage) {
        textEditorController.setStage(stage);
		stage.setOnCloseRequest(e -> {
			textEditorController.quit(e);
		});

        textEditorController.setQuickTextController(this);
        textEditorController.setReusableTextManager(reusableTextManager);
    }
    
    void viewPlainTextTemplate(File file) throws IOException {
    	hideWebView();
    	showTextArea();

    	String text = readTextFromFile(file);
        textArea.setText(text);
    }
    
    void viewHTMLTemplate(File file) throws IOException {
    	hideTextArea();
    	showWebView();
    	
    	String text = readTextFromFile(file);
    	webView.getEngine().loadContent(text, "text/html");
    }
    
    void viewFileDetails(FileItem fileItem) {
    	String description = fileItem.getDescription();
    	if (description != null && !description.isEmpty()) {
    		showViewDescriptionPane();
        	showEditDescriptionPane();
        	descriptionText.setText(description);
    	}
    	else if (!(fileItem instanceof RootFolderItem)) {
    		hideViewDescriptionPane();
        	showEditDescriptionPane();
    	}
    }
    
    String readTextFromFile(File file) throws IOException {
    	String text = getTextFromCache(getCacheKey(file));
    	HashMap<String, Charsets> charsetsMap = Charsets.getCharsetsHashMap();
    	String charsetName = SettingsManager.getInstance().getTextCharset();
    	
    	if (text == null) {
    		text = fileManager.readAllLinesFromFileAsString(file, charsetsMap.get(charsetName).toStandardCharset());
    		addTextToCache(file, text);
    	}

    	return text;
    }
    
    private String getTextFromCache(String key) {
    	return cacheManager.getFromCache(key);
    }

    private void addTextToCache(File file, String text) {
    	String key = getCacheKey(file);
    	if (!cacheManager.isInCache(key)) {
    		cacheManager.addToCache(key, text);
    	}
    }
    
    void updateTextInCache(File file, String text) {
    	String key = getCacheKey(file);
    	if (cacheManager.isInCache(key)) {
    		cacheManager.updateCache(key, text);
    	}
    }
    
    void removeTextFromCache(File file) {
    	String key = getCacheKey(file);
    	if (cacheManager.isInCache(key)) {
    		cacheManager.removeFromCache(key);
    	}
    }
    
    void setCacheMaxItems(int maxItems) {
    	cacheManager.setCacheMaxItems(maxItems);
    }
     
    void clearCache() {
    	cacheManager.clearCache();
    }
    
    private String getCacheKey(File file) {
    	File root = treeViewPaneController.getRoot().getValue().getFile();
    	return fileManager.getRelativePath(root, file);
    }
    
    void deleteOldestBackups(int maxNumBackups) {
    	backupManager.deleteOldestBackups(maxNumBackups);
    }

    public void handleStageClosure(WindowEvent e) {
    	saveTreeView();
    }
    
    private void cancelScheduledBackupTasks() {
    	backupManager.cancelScheduledBackupTasks();
    }

    private void shutdownExecutors() {
    	backupManager.shutdownScheduledExecutor();
    }
    
    private void saveBackups() {
    	backupManager.saveBackupFiles(SettingsManager.getInstance().getBackupDataPath());
    	backupManager.saveScheduledBackupTasks(SettingsManager.getInstance().getScheduledBackupDataPath());
    }
    
    private void saveReusableText() {
    	try {
			reusableTextManager.saveReusableText(SettingsManager.getInstance().getReusableTextDataPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	void setLogo(Object object, String logo) {
		if (object instanceof Stage) {
			((Stage) object).getIcons().add(new Image(getClass().getResource(logo).toString()));
		}

		if (object instanceof Pane) {
			Stage stage = (Stage) ((Pane)object).getScene().getWindow();
			stage.getIcons().add(new Image(getClass().getResource(logo).toString()));
		}
	}

	void setStyle(Object object, String css) {
		if (object instanceof Scene) {
			((Scene) object).getStylesheets().add(getClass().getResource(css).toString());
		}

		if (object instanceof Pane) {
			((Pane) object).getStylesheets().add(getClass().getResource(css).toString());
		}
	}
}
