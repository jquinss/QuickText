package controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import data.FileItem;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.web.HTMLEditor;
import managers.FileManager;

public class HTMLEditorController extends TextEditorController {
	public HTMLEditorController(TreeItem<FileItem> treeItem, FileManager fileManager) {
		super(treeItem, fileManager);
	}
	
	@FXML
    private HTMLEditor htmlEditor;
	
    void saveNewFile(String fileName) throws IOException {
    	File folder = folderTreeItem.getValue().getFile();
		File file = new File(folder.toString() + File.separator + fileName + ".html");
    	writeHTMLEditorToFile(file);
    	addFileTreeItemToFolderTreeItem(file);
    }
    
    void saveExistingFile(File file) throws IOException {
		writeHTMLEditorToFile(file);
    }
    
    private void writeHTMLEditorToFile(File file) throws IOException {
    	String htmlEditorContent = htmlEditor.getHtmlText();
    	fileManager.writeStringToFile(htmlEditorContent, file);
    }
    
    public void initialize() {
    	if (fileTreeItem != null) {
    		try {
    			File file = fileTreeItem.getValue().getFile();
    			loadFileToHTMLEditor(file);
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void loadFileToHTMLEditor(File file) throws IOException {
        List<String> fileLines = fileManager.readAllLinesFromFile(file);
        StringBuilder text = new StringBuilder();
        for (String line : fileLines) {
        	text.append(line);
        }
        htmlEditor.setHtmlText(text.toString());
    }
}
