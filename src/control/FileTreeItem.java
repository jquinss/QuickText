package control;

import data.FileItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<FileItem> {
	private ContextMenu contextMenu;
	
	public FileTreeItem(FileItem fileItem) {
		setValue(fileItem);
	}
	
	public void setContextMenu(ContextMenu contextMenu) {
		this.contextMenu = contextMenu;
	}
	
	public ContextMenu getContextMenu() {
		return contextMenu;
	}
}
