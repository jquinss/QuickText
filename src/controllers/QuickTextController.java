package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import util.HtmlToPlainText;
import control.FileTreeItem;
import data.FileItem;
import data.FolderItem;
import data.HTMLTemplateItem;
import data.PlainTextTemplateItem;
import data.RootFolderItem;
import data.TemplateItem;
import enums.Charsets;
import managers.CacheManager;
import managers.FileManager;
import managers.SettingsManager;
import util.DialogBuilder;
import util.FileItemBuilder;
import util.StringCache;
import util.XMLConverter;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    private ScrollPane detailsPane;
    
    @FXML
    private VBox viewDescriptionPane;
    
    @FXML
    private VBox editDescriptionPane;

    @FXML
    private Text descriptionText;
    
    @FXML
    private TextField descriptionTextField;
    
    private String templatesDir;
    
    private String xmlDir;
    
    private String xmlPath;
    
    private Stage stage;
    
    private CacheManager cacheManager;
    
    private FileItemBuilder fileItemBuilder;
    
    private ContextMenuBuilder contextMenuBuilder;
    
    private final FileManager fileManager = new FileManager();
    
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
    	
    	Optional<Pair<String, String>> result = dialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String folderName = result.get().getKey();
    		String folderDescription = result.get().getValue();
    		
    		try {
    			File destDir = fileManager.buildFilePath(templatesDir, folderName);
    			fileManager.createDir(destDir);
    			FileTreeItem folderTreeItem = buildFileTreeItem(destDir, folderDescription);
    			treeView.getRoot().getChildren().add(folderTreeItem);
    		}
        	catch (FileAlreadyExistsException e) {
        		DialogBuilder.buildAlertDialog("Error", "Error creating the folder", "There is already a folder with the same name", AlertType.ERROR).showAndWait();
        	}
    		catch (IOException e) {
    			DialogBuilder.buildAlertDialog("Error", "Error creating the folder", "Folder cannot be created", AlertType.ERROR).showAndWait();
    		}
    	}
    }
    
    @FXML
    void deleteTemplate(ActionEvent event) {
    	TreeItem<FileItem> treeItem = treeView.getSelectionModel().getSelectedItem();
    	File selectedFile = treeItem.getValue().getFile();
    	try {
			fileManager.removeFile(selectedFile);
			treeItem.getParent().getChildren().remove(treeItem);
			removeTextFromCache(selectedFile);
    	}
    	catch (IOException e) {
    		DialogBuilder.buildAlertDialog("Error", "Error removing the file", "An error has occurred while trying to remove the file", AlertType.ERROR).showAndWait();
    	}
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
    		Alert alertDialog = DialogBuilder.buildAlertDialog("Confirmation", "The folder is not empty", "Are you sure you want to delete all the files?", AlertType.CONFIRMATION);
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
    	clearCache();
    }

    @FXML
    void createHTMLTemplate(ActionEvent event) {
    	try {
			openHTMLEditor(treeView.getSelectionModel().getSelectedItem());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void createPlainTextTemplate(ActionEvent event) {
    	try {
			openPlainTextEditor(treeView.getSelectionModel().getSelectedItem());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    void importTemplates(ActionEvent event) {
    	FileChooser.ExtensionFilter extensions = new FileChooser.ExtensionFilter("Templates", "*.txt", "*.html");
    	List<File> selectedFiles = DialogBuilder.buildFileChooser("Select a template", extensions).showOpenMultipleDialog(stage);
    	
    	if (selectedFiles != null) {
    		TreeItem<FileItem> folderTreeItem = treeView.getSelectionModel().getSelectedItem();
    		File destFolder = folderTreeItem.getValue().getFile();
    		for (File srcFile : selectedFiles) {
    			String srcFileName = srcFile.getName();
    			File destFile = fileManager.buildFilePath(destFolder, srcFileName);
    			try {
    				copyTemplate(srcFile, destFile, folderTreeItem);
				} catch (FileAlreadyExistsException e) {
					DialogBuilder.buildAlertDialog("Error", "Error importing the file", "The file " + e.getFile() + " already exists", AlertType.ERROR).showAndWait();
				} catch (IOException e) {
					DialogBuilder.buildAlertDialog("Error", "Error copying the file", "An error has occurred when copying the file", AlertType.ERROR).showAndWait();
				}
    		}
    	}
    }
    
    @FXML
    void copyTemplateToClipboard(ActionEvent event) {
    	Clipboard clipboard = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	FileItem fileItem = treeView.getSelectionModel().getSelectedItem().getValue();
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
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	clipboard.setContent(content);
    }
    
    @FXML
    void editTemplate(ActionEvent event) {
    	TreeItem<FileItem> selectedTreeItem = treeView.getSelectionModel().getSelectedItem();
    	FileItem selectedFileItem = selectedTreeItem.getValue();
    	
    	if (selectedFileItem instanceof PlainTextTemplateItem) {
        	try {
    			openPlainTextEditor(selectedTreeItem);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	else if (selectedFileItem instanceof HTMLTemplateItem) {
        	try {
    			openHTMLEditor(selectedTreeItem);
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    @FXML
    void duplicateTemplate(ActionEvent event) {
    	TreeItem<FileItem> selectedTreeItem = treeView.getSelectionModel().getSelectedItem();
    	TreeItem<FileItem> folderTreeItem = selectedTreeItem.getParent();
    	
    	File srcFile = selectedTreeItem.getValue().getFile();
    	String dirName = srcFile.getParent();
    	String fileName = srcFile.getName();
    	String suffix = fileManager.getExtensionFromFile(fileName);
    	String prefix = fileManager.removeFileExtension(fileName, suffix);
    	String nextPrefix = fileManager.getNextAvailableFileName(dirName, prefix, suffix);
    	
    	Dialog<String> inputDialog = DialogBuilder.buildSingleTextFieldInputDialog("Duplicate template", "Create a new duplicate template", "Name of the template", 
    																				nextPrefix);
    	
    	Optional<String> result = inputDialog.showAndWait();
    	
    	if (result.isPresent()) {
    		String newPrefix = result.get();
    		File destFile = fileManager.buildFilePath(dirName, newPrefix, suffix);
    		try {
    			copyTemplate(srcFile, destFile, folderTreeItem);
    		} catch (FileAlreadyExistsException e) {
				DialogBuilder.buildAlertDialog("Error", "Error copying the file", "The file " + e.getFile() + " already exists", AlertType.ERROR).showAndWait();
			} catch (IOException e) {
				DialogBuilder.buildAlertDialog("Error", "Error copying the file", "An error has occurred when copying the file", AlertType.ERROR).showAndWait();
			}
    	}	
    }

    @FXML
    void exitApplication(ActionEvent event) {
    	saveTreeViewToXML();
    	stage.close();
    }

    @FXML
    void showAboutMenu(ActionEvent event) {
    	DialogBuilder.buildAlertDialog("About", "", "QuickText v1.0\n\nDesigned by Joaquin Sampedro", AlertType.INFORMATION).show();
    }
    
    @FXML
    void saveDescription(ActionEvent event) {
    	String description = descriptionTextField.getText().trim();
    	if (!description.isEmpty()) {
        	FileItem fileItem = treeView.getSelectionModel().getSelectedItem().getValue();
        	fileItem.setDescription(description);
        	viewFileDetails(fileItem);
    	}
    }
    
    @FXML
	void openSettingsDialog(ActionEvent event) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SettingsPane.fxml"));
		Parent parent = fxmlLoader.load();
		
		SettingsPaneController settingsPaneController = fxmlLoader.getController();
		settingsPaneController.setQuickTextController(this);
		
		Scene scene = new Scene(parent, 450, 250);
		scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Settings");
        
        settingsPaneController.setStage(stage);
        
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
	}
    
    void viewTemplate() {
    	FileItem fileItem = treeView.getSelectionModel().getSelectedItem().getValue();
    	
    	if (fileItem instanceof PlainTextTemplateItem) {
    		try {
				viewPlainTextTemplate(fileItem.getFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else if (fileItem instanceof HTMLTemplateItem) {
    		try {
    			viewHTMLTemplate(fileItem.getFile());
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void renameTemplate() {
    	System.out.println("Renaming template");
    }
    
    private void copyTemplate(File srcFile, File destFile, TreeItem<FileItem> folderTreeItem) throws IOException {
    	fileManager.copyFile(srcFile, destFile);
		FileTreeItem fileTreeItem = buildFileTreeItem(destFile);
		folderTreeItem.getChildren().add(fileTreeItem);
    }
    

    private FileTreeItem buildFileTreeItem(File file, String description) {
    	FileTreeItem fileTreeItem = buildFileTreeItem(file);
    	if (description != null && !description.isEmpty()) {
    		fileTreeItem.getValue().setDescription(description);
    	}
		
		return fileTreeItem;
    }
    
    private FileTreeItem buildFileTreeItem(File file) {
    	FileItem fileItem = fileItemBuilder.buildFileItem(file);
    	FileTreeItem fileTreeItem = new FileTreeItem(fileItem);
		setContextMenu(fileTreeItem);
		fileTreeItem.setExpanded(true);
		
		return fileTreeItem;
    }
    
    @FXML
	public void initialize() {
		loadDefaultSettings();
		initializeBuilders();
		initializeCache();
		initializeTreeView();
		initializeDescriptionPane();
	}
    
    private void loadDefaultSettings() {
    	templatesDir = SettingsManager.getInstance().getTemplatesDir();
    	xmlDir = SettingsManager.getInstance().getXMLDir();
    	xmlPath = SettingsManager.getInstance().getXMLPath();
    }
    
    private void initializeCache() {
    	String cacheMaxItems = (String) SettingsManager.getInstance().getCacheMaxItems();
    	cacheManager = new CacheManager(new StringCache(Integer.parseInt(cacheMaxItems)));
    }
    
    private void initializeBuilders() {
    	fileItemBuilder = new FileItemBuilder(new File(templatesDir));
    	contextMenuBuilder = new ContextMenuBuilder();
    }
    
    private void initializeTreeView() {
    	setXMLDirectory();
    	setSelectedTreeItemListener();
    	try {
			buildTreeViewFromXML();
		} catch (Exception e) {
			e.printStackTrace();
			setRootDirectory();
		}
    	initializeContextMenu();
    	setTreeViewCellFactory();
    }
    
    private void initializeDescriptionPane() {
    	descriptionText.wrappingWidthProperty().bind(editDescriptionPane.widthProperty());
    }
    
    private void setRootDirectory() {
    	try {
    		fileManager.createDirPath(templatesDir);
    		FileTreeItem rootTreeItem = buildFileTreeItem(new File(templatesDir));
    		treeView.setRoot(rootTreeItem);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    private void setXMLDirectory() {
    	try {
			fileManager.createDirPath(xmlDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void setSelectedTreeItemListener() {
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
		    if (newValue != null) {
		    	hideAllViewAreas();
		    	FileItem fileItem = newValue.getValue();
		    	if (fileItem instanceof RootFolderItem) {
		    		enableRootRelatedMenuItems();
		    	}
		    	else if (fileItem instanceof FolderItem) {
		    		enableFolderRelatedMenuItems();
		    		viewFileDetails(newValue.getValue());
		    	}
		    	else if (fileItem instanceof TemplateItem) {
		    		enableTemplateRelatedMenuItems();
		    		viewFileDetails(newValue.getValue());
		    	}
		    }
		    else {
		    	disableAllMenuItems();
		    	hideAllViewAreas();
		    }
		});
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
    
    private void enableRootRelatedMenuItems() {
    	disableRootRelatedMenuItems(false);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(true);
    }
    
    private void enableFolderRelatedMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(false);
    	disableTemplateRelatedMenuItems(true);
    }
    
    private void enableTemplateRelatedMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(false);
    }
    
    private void disableAllMenuItems() {
    	disableRootRelatedMenuItems(true);
    	disableFolderRelatedMenuItems(true);
    	disableTemplateRelatedMenuItems(true);
    }
    
    private void hideAllViewAreas() {
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
							setGraphic(null);
						}
						else {
							setText(fileItem.getFile().getName());
							setContextMenu(((FileTreeItem) getTreeItem()).getContextMenu());
							setGraphic(new ImageView(new Image(getClass().getResourceAsStream(((FileTreeItem) getTreeItem()).getImgURL()))));
						}
					}
				};
							
				return cell;
			}
		});
	}
    
    private void buildTreeViewFromXML() throws SAXException, ParserConfigurationException, IOException {
    	File root = new File(templatesDir);
    	XMLConverter xmlConverter = new XMLConverter(root);
		xmlConverter.initializeTreeViewFromXML(new File(xmlPath), treeView);

    }
    
    void setContextMenu(FileTreeItem fileTreeItem) {
    	ContextMenu contextMenu = contextMenuBuilder.buildContextMenu(fileTreeItem.getValue());
    	fileTreeItem.setContextMenu(contextMenu);
    }
    
    private class ContextMenuBuilder {
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
		MenuItem deleteFolderMenuItem = new MenuItem("Delete");
		
		FolderContextMenu() {
			createPlainTextTemplateMenuItem.setOnAction(e -> createPlainTextTemplate(e));
			createHTMLTemplateMenuItem.setOnAction(e -> createHTMLTemplate(e));
			importTemplatesMenuItem.setOnAction(e -> importTemplates(e));
			deleteFolderMenuItem.setOnAction(e -> deleteFolder(e));
			
			createTemplateMenu.getItems().addAll(createPlainTextTemplateMenuItem, createHTMLTemplateMenuItem);
			getItems().addAll(createTemplateMenu, importTemplatesMenuItem, deleteFolderMenuItem);
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
			
			getItems().addAll(copyTemplateItem, viewTemplateItem, duplicateTemplateItem, editTemplateItem, deleteTemplateItem);
		}
    }
    
    private void initializeContextMenu() {
    	FileTreeItem rootTreeItem = (FileTreeItem) treeView.getRoot();
    	setContextMenu(rootTreeItem);
    	
    	for (TreeItem<FileItem> folderTreeItem : rootTreeItem.getChildren()) {
    		setContextMenu((FileTreeItem) folderTreeItem);
    		
    		for (TreeItem<FileItem> templateTreeItem : folderTreeItem.getChildren()) {
    			setContextMenu((FileTreeItem) templateTreeItem);
    		} 
    	}
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
    
    private void openPlainTextEditor(TreeItem<FileItem> selectedTreeItem) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/PlainTextEditor.fxml"));
    	File root = new File(templatesDir);
    	TextEditorController plainTextEditorController = new PlainTextEditorController(selectedTreeItem, fileManager, root);
    	fxmlLoader.setController(plainTextEditorController);
 
		Parent parent = fxmlLoader.load();
		
		Scene scene = buildTextEditorScene(parent);
        Stage stage = buildTextEditorStage("Plain-text editor", scene);
        
        initializeTextEditorController(plainTextEditorController, stage);
        
        stage.showAndWait();
    }
    
    private void openHTMLEditor(TreeItem<FileItem> selectedTreeItem) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/HTMLTextEditor.fxml"));
    	File root = new File(templatesDir);
    	HTMLEditorController htmlEditorController = new HTMLEditorController(selectedTreeItem, fileManager, root);
    	fxmlLoader.setController(htmlEditorController);
    	
		Parent parent = fxmlLoader.load();
		
		Scene scene = buildTextEditorScene(parent);
        Stage stage = buildTextEditorStage("HTML editor", scene);
        
        initializeTextEditorController(htmlEditorController, stage);
        
        stage.showAndWait();
    }
    
    private Scene buildTextEditorScene(Parent parent) {
    	Scene scene = new Scene(parent, 800, 600);
		scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
		
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
        textEditorController.setQuickTextController(this);
    }
    
    private void viewPlainTextTemplate(File file) throws IOException {
    	hideWebView();
    	showTextArea();

    	String text = readTextFromFile(file);
        textArea.setText(text);
    }
    
    private void viewHTMLTemplate(File file) throws IOException {
    	hideTextArea();
    	showWebView();
    	
    	String text = readTextFromFile(file);
    	webView.getEngine().loadContent(text, "text/html");
    }
    
    private void viewFileDetails(FileItem fileItem) {
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
    
    private void removeTextFromCache(File file) {
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
    	File root = treeView.getRoot().getValue().getFile();
    	return fileManager.getRelativePath(root, file);
    }
    
    private void saveTreeViewToXML() {
    	File root = new File(templatesDir);
    	XMLConverter xmlConverter = new XMLConverter(root);
    	try {
			xmlConverter.convertTreeViewToXML(treeView, new File(xmlPath), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void handleStageClosure(WindowEvent e) {
    	saveTreeViewToXML();
    }
}
