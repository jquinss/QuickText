package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

import data.FileItem;
import enums.Charsets;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.web.HTMLEditor;
import managers.FileManager;
import managers.SettingsManager;

public class HTMLEditorController extends TextEditorController {
	private static final String HTML_EXT = ".html";
	
	public HTMLEditorController(TreeItem<FileItem> treeItem, FileManager fileManager, File root) {
		super(treeItem, fileManager, root);
	}
	
	@FXML
    private HTMLEditor htmlEditor;
	
    void saveNewFile(String fileName, String description) throws IOException {
    	File folder = folderTreeItem.getValue().getFile();
		File file = fileManager.buildFilePath(folder.toString(), fileName, HTML_EXT);
		
		if (file.exists()) {
			throw new FileAlreadyExistsException(file.toString());
		}
		
    	writeHTMLEditorToFile(file);
    	addFileTreeItemToFolderTreeItem(file, description);
    }
    
    void saveExistingFile(File file) throws IOException {
		writeHTMLEditorToFile(file);
    }
    
    private void writeHTMLEditorToFile(File file) throws IOException {
    	String text = htmlEditor.getHtmlText();
    	writeTextToFile(text, file);
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
    	HashMap<String, Charsets> charsetsMap = Charsets.getCharsetsHashMap();
    	String charsetName = SettingsManager.getInstance().getTextCharset();
        String text = fileManager.readAllLinesFromFileAsString(file, charsetsMap.get(charsetName).toStandardCharset());
        htmlEditor.setHtmlText(text);
    }
}
