package com.jquinss.quicktext.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;

public class HTMLEditorListener {
	private final StringProperty textProperty;
	private String htmlRef;
	
	public HTMLEditorListener(HTMLEditor htmlEditor) {
		textProperty = new SimpleStringProperty();
		textProperty.set("");

		htmlEditor.setOnMouseClicked(e -> {
			checkEdition(htmlEditor.getHtmlText());
		});
		htmlEditor.addEventFilter(KeyEvent.KEY_TYPED, e -> {
			checkEdition(htmlEditor.getHtmlText());
		});
		
		for (Node node : htmlEditor.lookupAll("ToolBar"))
		{
			node.setOnMouseExited(e -> {
				checkEdition(htmlEditor.getHtmlText());
		  });
		};
	}

	public StringProperty textProperty() {
		return textProperty;
	}
	
	private void checkEdition(String htmlText) {
		htmlRef = htmlText;
		if (htmlText != null) {
			textProperty.set(htmlText);
		}
	}
}
