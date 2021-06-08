package control;

import data.FileItem;
import data.FolderItem;
import data.HTMLTemplateItem;
import data.PlainTextTemplateItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

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
		String imgURL = null;
		FileItem fileItem = getValue();
		
		if (fileItem instanceof FolderItem) {
			imgURL = FOLDER_IMG;
		}
		else if (fileItem instanceof PlainTextTemplateItem) {
			imgURL = TXT_IMG;
		}
		else if (fileItem instanceof HTMLTemplateItem) {
			imgURL = HTML_IMG;
		}
		
		return imgURL;
	}
}
