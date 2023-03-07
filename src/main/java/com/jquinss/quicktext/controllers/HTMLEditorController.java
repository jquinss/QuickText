package com.jquinss.quicktext.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

import com.jquinss.quicktext.data.FileItem;
import com.jquinss.quicktext.enums.Charsets;
import com.jquinss.quicktext.managers.FileManager;
import com.jquinss.quicktext.managers.SettingsManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import com.jquinss.quicktext.util.DialogBuilder;
import com.jquinss.quicktext.util.HTMLEditorListener;

@SuppressWarnings("restriction")
public class HTMLEditorController extends TextEditorController {
	private static final String HTML_EXT = ".html";
	
	public HTMLEditorController(TreeItem<FileItem> treeItem, FileManager fileManager, File root) {
		super(treeItem, fileManager, root);
	}
	
	@FXML
    private HTMLEditor htmlEditor;
	
	@FXML
	private SplitPane htmlEditorSplitPane;
	
	@FXML
	private VBox htmlTextAreaPane;
	
	@FXML
	private TextArea htmlTextArea;
	
	@FXML
	private ToggleButton htmlTextAreaEditToggleBtn;
	
	@FXML
	private Button htmlTextAreaSaveBtn;
	
	@FXML
	private Button htmlTextAreaFormatHTMLBtn;

	private ChangeListener<String> htmlTextAreaChangeListener;
	
	private final ToggleButton htmlTextAreaToggleBtn = new ToggleButton("<HTML>");
	
	private boolean isInitialHTMLTextLoaded = false;
	
	private boolean isHTMLTextAreaChanged = false;
	
	@FXML
	private void saveHTMLTextArea(ActionEvent event) {
		if (isHTMLTextAreaChanged) {
			htmlEditor.setHtmlText(htmlTextArea.getText());
			setIsNotSavedStatus();
		}
		htmlTextArea.textProperty().removeListener(htmlTextAreaChangeListener);
		disableHTMLTextArea();
	}
	
	@FXML
	private void formatHTMLText(ActionEvent event) {
		Document doc = Jsoup.parse(htmlTextArea.getText());
		htmlTextArea.setText(doc.toString());
	}
	
	void insertText(String text) {
		// TO DO
	}
	
    void saveNewFile(String fileName, String description) throws IOException {
    	File folder = folderTreeItem.getValue().getFile();
		File file = fileManager.buildFilePath(folder.toString(), fileName, HTML_EXT);
		
		if (file.exists()) {
			throw new FileAlreadyExistsException(file.toString());
		}
		
    	writeHTMLEditorToFile(file);
    	createFile(file, description);
    	setIsSavedStatus();
    }
    
    void saveExistingFile(File file) throws IOException {
		writeHTMLEditorToFile(file);
		setIsSavedStatus();
    }
    
    private void writeHTMLEditorToFile(File file) throws IOException {
    	String text = htmlEditor.getHtmlText();
    	writeTextToFile(text, file);
    }
    
    private void initializeListeners() {
		HTMLEditorListener htmlEditorListener = new HTMLEditorListener(htmlEditor);
    	htmlEditorListener.textProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue != null) {
    			// the first time we load the text into the HTML area, the saved status is not changed.
    			if (isInitialHTMLTextLoaded) {
    				setIsNotSavedStatus();
    			}
    			isInitialHTMLTextLoaded = true;
    		}
    	});
    	
    	htmlTextAreaChangeListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
				if (newValue != null) {
	    			isHTMLTextAreaChanged = true;
	    		}
			}
    	};
    }
    
    private void initializeToolBar() {
    	 ToolBar toolBar = (ToolBar) htmlEditor.lookup(".top-toolbar");
    	 toolBar.getItems().addAll(htmlTextAreaToggleBtn);
    	 htmlEditorSplitPane.getItems().remove(htmlTextAreaPane);
    	 initializeHTMLTextAreaToggleBtn();
    }

    private void initializeHTMLTextAreaToggleBtn() {
    	htmlTextAreaToggleBtn.setOnAction(e -> {
    		if (htmlTextAreaToggleBtn.isSelected()) {
    			showHTMLTextAreaPane();
   		 	}
   		 	else {
   		 		hideHTMLTextAreaPane();
   		 	}
    	});
    }
    
    private void showHTMLTextAreaPane() {
    	disableHTMLTextArea();
    	htmlTextArea.setText(htmlEditor.getHtmlText());
		htmlEditorSplitPane.getItems().add(1, htmlTextAreaPane);
 		htmlEditorSplitPane.setDividerPosition(0, 0.7f);
    }
    
    private void hideHTMLTextAreaPane() {
    	disableHTMLTextArea();
    	htmlEditorSplitPane.getItems().remove(htmlTextAreaPane);
    }
    
    private void enableHTMLTextArea() {
		htmlTextAreaEditToggleBtn.setDisable(true);
		htmlTextAreaSaveBtn.setDisable(false);
		htmlTextAreaFormatHTMLBtn.setDisable(false);
    }
    
    private void disableHTMLTextArea() {
		htmlTextArea.setDisable(true);
		htmlTextAreaToggleBtn.setDisable(false);
		htmlTextAreaEditToggleBtn.setDisable(false);
		htmlTextAreaEditToggleBtn.setSelected(false);
		htmlTextAreaSaveBtn.setDisable(true);
		htmlTextAreaFormatHTMLBtn.setDisable(true);
    }
    
    private void initializeHTMLTextArea() {
    	htmlTextAreaEditToggleBtn.selectedProperty().addListener((obs, oldValue, newValue) -> {
    		if (newValue) {
    			enableHTMLTextArea();
    			editHTMLTextArea();
    		}
    	});
    }
    
    private void editHTMLTextArea() {
		htmlTextArea.setText(htmlEditor.getHtmlText());
		htmlTextArea.setDisable(false);
		isHTMLTextAreaChanged = false;
		htmlTextArea.textProperty().addListener(htmlTextAreaChangeListener);
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
    	initializeListeners();
    	initializeToolBar();
    	initializeHTMLTextArea();
    }
    
    private void loadFileToHTMLEditor(File file) throws IOException {
    	HashMap<String, Charsets> charsetsMap = Charsets.getCharsetsHashMap();
    	String charsetName = SettingsManager.getInstance().getTextCharset();
        String text = fileManager.readAllLinesFromFileAsString(file, charsetsMap.get(charsetName).toStandardCharset());
        htmlEditor.setHtmlText(text);
    }
}
