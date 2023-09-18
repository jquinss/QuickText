package com.jquinss.quicktext.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com.jquinss.quicktext.control.FileTreeItem;
import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.data.FolderItem;
import com.jquinss.quicktext.data.RootFolderItem;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.SettingsManager;
import javafx.scene.control.*;
import org.xml.sax.SAXException;

import com.jquinss.quicktext.data.TemplateItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import com.jquinss.quicktext.util.DialogBuilder;
import com.jquinss.quicktext.util.FileItemBuilder;
import com.jquinss.quicktext.util.XMLConverter;

public class TreeViewPaneController {
    @FXML
    private TreeView<FileItem> treeView;
    
    private QuickTextController quickTextController;
    
    private QuickTextController.ContextMenuBuilder contextMenuBuilder;
    
    private final FileManager fileManager = new FileManager();
    
    private final DataFormat dataFormat = new DataFormat("fileItemDataFormat");
    
    private final FileItemBuilder fileItemBuilder = new FileItemBuilder(new File(SettingsManager.getInstance().getTemplatesDir()));;
    
    void setQuickTextController(QuickTextController quickTextController) {
    	this.quickTextController = quickTextController;
    	contextMenuBuilder = this.quickTextController.new ContextMenuBuilder();
    }
    
    void createFolder(String folderName, String folderDescription) throws IOException {
    	File destDir = fileManager.buildFilePath(SettingsManager.getInstance().getTemplatesDir(), folderName);
    	fileManager.createDir(destDir);
    	FileTreeItem folderTreeItem = buildFileTreeItem(destDir, folderDescription);
    	treeView.getRoot().getChildren().add(folderTreeItem);
    }
    
    void deleteTemplate(TreeItem<FileItem> treeItem) throws IOException {
    	File file = treeItem.getValue().getFile();
		fileManager.removeFile(file);
		treeItem.getParent().getChildren().remove(treeItem);
		quickTextController.removeTextFromCache(file);
    	
    }
    
    TreeItem<FileItem> getSelectedTreeItem() {
    	return treeView.getSelectionModel().getSelectedItem();
    }
    
    TreeItem<FileItem> getRoot() {
    	return treeView.getRoot();
    }
    
    boolean isEmptyTreeView() {
    	return treeView.getRoot().getChildren().size() == 0 ? true : false;
    }
    
    void deleteFolder(TreeItem<FileItem> treeItem) throws IOException {
    	File folder = treeItem.getValue().getFile();
		fileManager.removeDir(folder);
		treeView.getRoot().getChildren().remove(treeItem);
    }
    
    void renameFolder(TreeItem<FileItem> treeItem, String newFolderName) throws IOException {
    	TreeItem<FileItem> parentFolderTreeItem = treeItem.getParent();
    	File srcFolder = treeItem.getValue().getFile();
    	String parentDirName = srcFolder.getParent();
   		File destFolder = fileManager.buildFilePath(parentDirName, newFolderName);

    	moveFolder(srcFolder, destFolder, treeItem, parentFolderTreeItem, parentFolderTreeItem);
    }
    
    private void moveFolder(File srcFolder, File destFolder, TreeItem<FileItem> srcFolderTreeItem, TreeItem<FileItem> srcParentFolderTreeItem, 
    		TreeItem<FileItem> destParentFolderTreeItem) throws IOException {
    	fileManager.moveFile(srcFolder, destFolder);
    	    	
    	if (destFolder.getParent().equals(srcFolder.getParent())) {
    		srcFolderTreeItem.getValue().setFile(destFolder);
    	    for (TreeItem<FileItem> templateTreeItem : srcFolderTreeItem.getChildren()) {
    	    	FileItem templateFileItem = templateTreeItem.getValue();
    	    	String fileName = templateFileItem.getFile().getName();
    	    	File destTemplateFile = fileManager.buildFilePath(destFolder, fileName);
    	    	templateFileItem.setFile(destTemplateFile);
    	    }
    	    treeView.refresh();
    	}
    }
    
    void deleteAllFolders() {
    	deleteAllFilesAndFolders(treeView.getRoot(), false);
    }

    void importTemplates(TreeItem<FileItem> treeItem, List<File> selectedFiles) throws IOException {
    	File destFolder = treeItem.getValue().getFile();
    	for (File srcFile : selectedFiles) {
    		String srcFileName = srcFile.getName();
    		File destFile = fileManager.buildFilePath(destFolder, srcFileName);
    		copyTemplate(srcFile, destFile, treeItem);
    	}
    }

    void copyTemplate(File srcFile, File destFile, TreeItem<FileItem> folderTreeItem) throws IOException {
    	fileManager.copyFile(srcFile, destFile);
		FileTreeItem fileTreeItem = buildFileTreeItem(destFile);
		folderTreeItem.getChildren().add(fileTreeItem);
    }
    
    TreeItem<FileItem> moveTemplate(File srcFile, File destFile, TreeItem<FileItem> srcTemplateTreeItem, TreeItem<FileItem> srcFolderTreeItem, 
    		TreeItem<FileItem> destFolderTreeItem) throws IOException {
    	fileManager.moveFile(srcFile, destFile);
    	int srcTemplateTreeItemIndex = srcFolderTreeItem.getChildren().indexOf(srcTemplateTreeItem);
    	srcFolderTreeItem.getChildren().remove(srcTemplateTreeItem);
    	FileTreeItem fileTreeItem = buildFileTreeItem(destFile);
    	destFolderTreeItem.getChildren().add(srcTemplateTreeItemIndex, fileTreeItem);
    	treeView.getSelectionModel().select(fileTreeItem);
    	
    	quickTextController.removeTextFromCache(srcFile);
    	
    	return fileTreeItem;
    }
    
    TreeItem<FileItem> createTemplate(File file, String description, TreeItem<FileItem> folderTreeItem) {
    	FileItem fileItem = fileItemBuilder.buildFileItem(file);
		if (!description.isEmpty()) {
			fileItem.setDescription(description);
		}
		FileTreeItem fileTreeItem = new FileTreeItem(fileItem);

		setContextMenu(fileTreeItem);
		folderTreeItem.getChildren().add(fileTreeItem);
		
		return fileTreeItem;
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
    
    void setContextMenu(FileTreeItem fileTreeItem) {
    	ContextMenu contextMenu = contextMenuBuilder.buildContextMenu(fileTreeItem.getValue());
    	fileTreeItem.setContextMenu(contextMenu);
    }
    
    void initializeTreeView() throws IOException {
    	setXMLDirectory();
    	setSelectedTreeItemListener();
    	try {
			buildTreeViewFromXML();
		} catch (Exception e) {
			// if there is an issue building the treeview from the xml file, then we just set the root directory
			setRootDirectory();
		}
    	initializeContextMenu();
    	setTreeViewCellFactory();
    }
    
    private void setXMLDirectory() throws IOException {
		fileManager.createDirPath(SettingsManager.getInstance().getXMLDir());
    }
    
    private void setSelectedTreeItemListener() {
    	// we add listeners to the treeview. Depending on the type of element selected (root folder, normal
    	// folder or template), the selection behavior will be different
    	treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
		    if (newValue != null) {
		    	quickTextController.hideAllViewAreas();
		    	FileItem fileItem = newValue.getValue();
		    	if (fileItem instanceof RootFolderItem) {
					quickTextController.enableRootRelatedToolbarButtons();
		    		quickTextController.enableRootRelatedMenuItems();
		    	}
		    	else if (fileItem instanceof FolderItem) {
					quickTextController.enableFolderRelatedToolbarButtons();
		    		quickTextController.enableFolderRelatedMenuItems();
		    		quickTextController.viewFileDetails(newValue.getValue());
		    	}
		    	else if (fileItem instanceof TemplateItem) {
					quickTextController.enableTemplateRelatedToolbarButtons();
		    		quickTextController.enableTemplateRelatedMenuItems();
		    		quickTextController.viewFileDetails(newValue.getValue());
		    	}
		    }
		    else {
		    	// when no items are selected, we disable all menu items and hide all the view areas.
		    	quickTextController.disableAllMenuItems();
				quickTextController.disableAllToolbarButtons();
		    	quickTextController.hideAllViewAreas();
		    }
		});
    }
    
    void buildTreeViewFromXML() throws SAXException, ParserConfigurationException, IOException {
    	File root = new File(SettingsManager.getInstance().getTemplatesDir());
    	XMLConverter xmlConverter = new XMLConverter(root);
		xmlConverter.initializeTreeViewFromXML(new File(SettingsManager.getInstance().getXMLPath()), treeView);

    }
    
    private void setRootDirectory() throws IOException {
    	String templatesDir = SettingsManager.getInstance().getTemplatesDir();
    	fileManager.createDirPath(templatesDir);
    	FileTreeItem rootTreeItem = buildFileTreeItem(new File(templatesDir));
    	treeView.setRoot(rootTreeItem);
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
    
    private void setTreeViewCellFactory() {
    	// we set the cell factory for each different element. The context menu and graphic will be different
    	// depending on the type of element.
		treeView.setCellFactory(new Callback<TreeView<FileItem>,TreeCell<FileItem>>() {
			private TreeItem<FileItem> srcTreeItem;
			private TreeItem<FileItem> destTreeItem;
			private TreeItem<FileItem> resultTreeItem;
			
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
				
				cell.setOnDragDetected(e -> {
					if (cell.getItem() instanceof TemplateItem) {
						Dragboard dragBoard = cell.startDragAndDrop(TransferMode.MOVE);
						ClipboardContent content = new ClipboardContent();
						content.put(dataFormat, cell.getItem());
						dragBoard.setContent(content);
						dragBoard.setDragView(cell.snapshot(null, null));
						e.consume();
					}
				});
				
				
				cell.setOnDragOver(e ->{
					Dragboard dragboard = e.getDragboard();
					destTreeItem = cell.getTreeItem();
					srcTreeItem = p.getSelectionModel().getSelectedItem();
					
					if ((dragboard.hasContent(dataFormat)) && 
						(destTreeItem.getValue() instanceof FolderItem) && 
						!(destTreeItem.getValue() instanceof RootFolderItem) &&
						(destTreeItem != srcTreeItem.getParent())) {
						e.acceptTransferModes(TransferMode.MOVE);
					}
				});
				
				cell.setOnDragDropped(e -> {
					File destFolder = destTreeItem.getValue().getFile();
					File srcFile = srcTreeItem.getValue().getFile();
					File destFile = Paths.get(destFolder.toString()).resolve(Paths.get(srcFile.getName())).toFile();

					try {
						resultTreeItem = moveTemplate(srcFile, destFile, srcTreeItem, srcTreeItem.getParent(), destTreeItem);
						e.setDropCompleted(true);
					} catch (FileAlreadyExistsException e1) {		
							Alert alertDialog = DialogBuilder.buildAlertDialog("Error", "Error moving the file", "The file " + e1.getFile() + " already exists", AlertType.ERROR);
							quickTextController.setLogo(alertDialog.getDialogPane(), SettingsManager.getInstance().getLogoPath());
							quickTextController.setStyle(alertDialog.getDialogPane(), SettingsManager.getInstance().getCSSPath());
							alertDialog.showAndWait();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				});
				
				cell.setOnDragDone(e -> {
					if (resultTreeItem != null) {
						p.getSelectionModel().select(resultTreeItem);
					}
				});
				
				return cell;
			}
		});
	}
    
    void deleteAllFilesAndFolders(TreeItem<FileItem> startTreeItem, boolean includeStartTreeItem) {
    	// depending on the value of the includeStartTreeItem, we will either delete or not the element that is selected
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
    
    void saveTreeViewToXML() throws XMLStreamException, IOException, ParserConfigurationException, SAXException, TransformerFactoryConfigurationError, TransformerException {
    	File root = new File(SettingsManager.getInstance().getTemplatesDir());
    	XMLConverter xmlConverter = new XMLConverter(root);
		xmlConverter.convertTreeViewToXML(treeView, new File(SettingsManager.getInstance().getXMLPath()), true);
    }
}
