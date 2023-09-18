package com.jquinss.quicktext.control;

import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.data.FolderItem;
import com.jquinss.quicktext.data.HTMLTemplateItem;
import com.jquinss.quicktext.data.PlainTextTemplateItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

public class FileTreeItem extends TreeItem<FileItem> {
	private static final String FOLDER_IMG = "/com/jquinss/quicktext/images/folder.png";
	private static final String TXT_IMG = "/com/jquinss/quicktext/images/plaintext_template.png";
	private static final String HTML_IMG = "/com/jquinss/quicktext/images/html_template.png";
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
