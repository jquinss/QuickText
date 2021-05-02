package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class QuickTextController {

    @FXML
    private TreeView<?> treeView;

    @FXML
    private TextArea textArea;

    @FXML
    private WebView webView;
    
    private Stage stage;
    
    public void setStage(Stage stage) {
    	this.stage = stage;
    }
    
    public Stage getStage() {
    	return this.stage;
    }

    @FXML
    void createFolder(ActionEvent event) {

    }

    @FXML
    void createHTMLTemplate(ActionEvent event) {

    }

    @FXML
    void createPlainTextTemplate(ActionEvent event) {

    }

    @FXML
    void exitApplication(ActionEvent event) {

    }

    @FXML
    void saveTemplates(ActionEvent event) {

    }

    @FXML
    void showAboutMenu(ActionEvent event) {

    }

}
