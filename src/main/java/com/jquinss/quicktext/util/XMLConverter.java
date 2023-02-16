package com.jquinss.quicktext.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.jquinss.quicktext.control.FileTreeItem;
import com.jquinss.quicktext.data.FileItem;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class XMLConverter {
	private static final String XML_ROOT_TEXT = "root";
	private static final String XML_FOLDER_TEXT = "folder";
	private static final String XML_TEMPLATE_TEXT = "template";
	private static final String XML_PATH_TEXT = "path";
	private static final String XML_DESCRIPTION_TEXT = "description";
	
	private final File root;
	private final FileItemBuilder fileItemBuilder;
	
	public XMLConverter(File root) {
		this.root = root;
		fileItemBuilder = new FileItemBuilder(this.root);
		
	}
	
	public void initializeTreeViewFromXML(File xmlFile, TreeView<FileItem> treeView) throws SAXException,
																		ParserConfigurationException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = createHandler(treeView);
			
		saxParser.parse(xmlFile, handler);
	}
	
	public void convertTreeViewToXML(TreeView<FileItem> treeView, File xmlFile, boolean formatXML) throws XMLStreamException, IOException, ParserConfigurationException, 
																								SAXException, TransformerFactoryConfigurationError, TransformerException {
		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlStreamWriter = xmlFactory.createXMLStreamWriter(new FileWriter(xmlFile));
		writeXMLFile(treeView, xmlStreamWriter);
		xmlStreamWriter.close();
		
		if (formatXML) {
			formatXMLFile(xmlFile);
		}
	}
	
	private void writeXMLFile(TreeView<FileItem> treeView, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
		writeTreeToXML(treeView, xmlStreamWriter);
	}
	
	private void writeTreeToXML(TreeView<FileItem> treeView, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
		xmlStreamWriter.writeStartDocument();
		xmlStreamWriter.writeStartElement(XML_ROOT_TEXT);
		xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
		xmlStreamWriter.writeCharacters(treeView.getRoot().getValue().getFile().getPath());
		xmlStreamWriter.writeEndElement();
		writeFolders(treeView.getRoot(), xmlStreamWriter);
		xmlStreamWriter.writeEndElement();
		xmlStreamWriter.writeEndDocument();
		xmlStreamWriter.flush();
	}
	
	private void writeFolders(TreeItem<FileItem> root, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
		if (!root.getChildren().isEmpty()) {
			for (TreeItem<FileItem> folder : root.getChildren()) {
				xmlStreamWriter.writeStartElement(XML_FOLDER_TEXT);
				xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
				xmlStreamWriter.writeCharacters(folder.getValue().getFile().getPath());
				xmlStreamWriter.writeEndElement();
				xmlStreamWriter.writeStartElement(XML_DESCRIPTION_TEXT);
				xmlStreamWriter.writeCharacters(folder.getValue().getDescription());
				xmlStreamWriter.writeEndElement();
				writeTemplates(folder, xmlStreamWriter);
				xmlStreamWriter.writeEndElement();
			}
		}
	}
	
	private void writeTemplates(TreeItem<FileItem> folder, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
		if (!folder.getChildren().isEmpty()) {
			for (TreeItem<FileItem> template : folder.getChildren()) {
				xmlStreamWriter.writeStartElement(XML_TEMPLATE_TEXT);
				xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
				xmlStreamWriter.writeCharacters(template.getValue().getFile().getPath());
				xmlStreamWriter.writeEndElement();
				xmlStreamWriter.writeStartElement(XML_DESCRIPTION_TEXT);
				xmlStreamWriter.writeCharacters(template.getValue().getDescription());
				xmlStreamWriter.writeEndElement();
				xmlStreamWriter.writeEndElement();
			}
		}
	}
	
	private void formatXMLFile(File xmlFile) throws ParserConfigurationException, FileNotFoundException, SAXException, 
													IOException, TransformerFactoryConfigurationError, TransformerException {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
			Document document = docBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(xmlFile))));
		
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
			Source source = new DOMSource(document);
			Result result = new StreamResult(xmlFile);
		
			transformer.transform(source, result);
	}

	private DefaultHandler createHandler(TreeView<FileItem> treeView) {
		DefaultHandler handler = new DefaultHandler() {
			FileTreeItem rootTreeItem;
			FileTreeItem folderTreeItem;
			FileTreeItem templateTreeItem;
				
			boolean enterRootItem;
			boolean enterFolderItem;
			boolean enterTemplateItem;
			boolean enterPath;
			boolean enterDescription;
				
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				
				if (qName.contentEquals(XML_ROOT_TEXT)) {
					enterRootItem = true;
				}
					
				if (qName.contentEquals(XML_FOLDER_TEXT)) {
					enterFolderItem = true;
				}
					
				if (qName.contentEquals(XML_TEMPLATE_TEXT)) {
					enterTemplateItem = true;
				}
					
				if (qName.contentEquals(XML_PATH_TEXT)) {
					enterPath = true;
				}
				
				if (qName.contentEquals(XML_DESCRIPTION_TEXT)) {
					enterDescription = true;
				}
			}
				
			@Override
			public void characters(char ch[], int start, int length) throws SAXException {
				if (enterRootItem) {
					if (enterPath) {
						File root = new File(new String(ch, start, length));
						
						if (root.exists()) {
							rootTreeItem = buildFileTreeItem(root);
							treeView.setRoot(rootTreeItem);
						}
							
						enterRootItem = false;
						enterPath = false;
					}
				}
					
				if (enterFolderItem) {
					if (enterPath) {
						File folder = new File(new String(ch, start, length));
						
						if (folder.exists()) {
							folderTreeItem = buildFileTreeItem(folder);
							rootTreeItem.getChildren().add(folderTreeItem);
						}
						
						enterPath = false;
					}
				}
				
				if (enterFolderItem) {
					if (enterDescription) {
						folderTreeItem.getValue().setDescription(new String(ch, start, length).trim());

						enterFolderItem = false;
						enterDescription = false;
					}
				}
					
				if (enterTemplateItem) {
					if (enterPath) {
						File template = new File(new String(ch, start, length));

						if (template.exists()) {
							templateTreeItem = buildFileTreeItem(template);
							folderTreeItem.getChildren().add(templateTreeItem);
						}
						
						enterPath = false;
					}
				}
				
				if (enterTemplateItem) {
					if (enterDescription) {
						templateTreeItem.getValue().setDescription(new String(ch, start, length).trim());

						enterTemplateItem = false;
						enterDescription = false;
					}
				}
			}
				
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.contentEquals("/" + XML_FOLDER_TEXT)) {
					enterFolderItem = false;
				}
			}
			
			private FileTreeItem buildFileTreeItem(File file) {
				FileItem fileItem = fileItemBuilder.buildFileItem(file);
				FileTreeItem fileTreeItem = new FileTreeItem(fileItem);
				fileTreeItem.setExpanded(true);
				
				return fileTreeItem;
			}
		};
		
		return handler;
	}
}