package managers;

import java.io.File;
import java.io.IOException;

import control.FolderTreeItem;
import control.RootTreeItem;
import util.XMLConverter;
import interfaces.Contextualizable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class TreeViewManager {
	private static TreeViewManager instance;
	private TreeView<File> treeView;
	private final FileManager fileManager;
	private String rootDir;
	private String xmlFilePath;

	
	private TreeViewManager() {
		fileManager = new FileManager();
	}
	
	public static synchronized TreeViewManager getInstance() {
		if (instance == null) {
			instance = new TreeViewManager();
		}
		
		return instance;
	}
	
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	
	public void createFolder(String folderName, TreeItem<File> parentTreeItem) throws IOException {
		fileManager.createDir(folderName, rootDir);
		File folder = new File(rootDir + File.separator + folderName);
		parentTreeItem.getChildren().add(new FolderTreeItem(folder));
	}
	
	public void initializeTreeview(TreeView<File> treeView, String rootDir, String xmlFilePath) {
		this.treeView = treeView;
		this.xmlFilePath = xmlFilePath;
		
		File xmlFile = new File(this.xmlFilePath);
		
		try {
			initializeTreeViewFromXML(xmlFile, this.treeView);
			setRootDir(treeView.getRoot().getValue().getPath());

		}
		catch (Exception e) {
			setRootDir(rootDir);
			treeView.setRoot(new RootTreeItem(new File(rootDir)));
		}
	
		try {
			fileManager.createDirPath(rootDir);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		setTreeViewCellFactory(this.treeView);
	}
	
	private void initializeTreeViewFromXML(File xmlFile, TreeView<File> treeView) throws Exception {
		XMLConverter xmlConverter = new XMLConverter();
		xmlConverter.initializeTreeViewFromXML(xmlFile, treeView);
	}
	
	private void setTreeViewCellFactory(TreeView<File> treeView) {
		treeView.setCellFactory(new Callback<TreeView<File>,TreeCell<File>>() {
			@Override
			public TreeCell<File> call(TreeView<File> p){
				TreeCell<File> cell = new TreeCell<File>() {
					@Override
					protected void updateItem(File file, boolean empty) {
						super.updateItem(file, empty);
						
						if (empty) {
							setText(null);
						}
						else {
							setText(file.getName());
							setContextMenu(((Contextualizable) getTreeItem()).getContextMenu());
						}
					}
				};
							
				return cell;
			}
		});
	}
}
