package control;

import data.FileItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FileTreeItem extends TreeItem<FileItem> {
	private static final String FOLDER_IMG = "/images/folder_img.png";
	private static final String TXT_IMG = "/images/txt_img.png";
	private static final String HTML_IMG = "/images/html_img.png";
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
	
	public String getImgURL() {
		if (getValue().isDirectory()) {
			return FOLDER_IMG;
		}
		else if (getValue().isPlainTextTemplate()) {
			return TXT_IMG;
		}
		else {
			return HTML_IMG;
		}
	}
}
