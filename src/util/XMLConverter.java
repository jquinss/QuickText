package util;

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

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import control.FolderTreeItem;
import control.RootTreeItem;
import control.TemplateTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class XMLConverter {
	private static final String XML_ROOT_TEXT = "root";
	private static final String XML_FOLDER_TEXT = "folder";
	private static final String XML_TEMPLATE_TEXT = "template";
	private static final String XML_PATH_TEXT = "path";
	
	public void initializeTreeViewFromXML(File xmlFile, TreeView<File> treeView) throws SAXException,
																		ParserConfigurationException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = createHandler(treeView);
			
		saxParser.parse(xmlFile, handler);
	}
	
	public void convertTreeViewToXML(TreeView<File> treeView, File xmlFile, boolean formatXML) throws XMLStreamException, IOException, ParserConfigurationException, 
																								SAXException, TransformerFactoryConfigurationError, TransformerException {
		XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlStreamWriter = xmlFactory.createXMLStreamWriter(new FileWriter(xmlFile));
		writeXMLFile(treeView, xmlStreamWriter);
		xmlStreamWriter.close();
		
		if (formatXML) {
			formatXMLFile(xmlFile);
		}
	}
	
	private void writeXMLFile(TreeView<File> treeView, XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
		xmlStreamWriter.writeStartDocument();
		xmlStreamWriter.writeStartElement(XML_ROOT_TEXT);
		xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
		xmlStreamWriter.writeCharacters(treeView.getRoot().getValue().getPath());
		xmlStreamWriter.writeEndElement();
		
		if (!treeView.getRoot().getChildren().isEmpty()) {
			for (TreeItem<File> folder : treeView.getRoot().getChildren()) {
				xmlStreamWriter.writeStartElement(XML_FOLDER_TEXT);
				xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
				xmlStreamWriter.writeCharacters(folder.getValue().getPath());
				xmlStreamWriter.writeEndElement();
				
				if (!folder.getChildren().isEmpty()) {
					for (TreeItem<File> template : folder.getChildren()) {
						xmlStreamWriter.writeStartElement(XML_TEMPLATE_TEXT);
						xmlStreamWriter.writeStartElement(XML_PATH_TEXT);
						xmlStreamWriter.writeCharacters(template.getValue().getPath());
						xmlStreamWriter.writeEndElement();
						xmlStreamWriter.writeEndElement();
					}
				}
				
				xmlStreamWriter.writeEndElement();
			}
		}
		
		xmlStreamWriter.writeEndElement();
		xmlStreamWriter.writeEndDocument();
		xmlStreamWriter.flush();
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
	
	private DefaultHandler createHandler(TreeView<File> treeView) {
		DefaultHandler handler = new DefaultHandler() {
			RootTreeItem rootTreeItem;
			FolderTreeItem folderTreeItem;
			TemplateTreeItem templateTreeItem;
				
			boolean enterRootItem;
			boolean enterFolderItem;
			boolean enterTemplateItem;
			boolean enterPath;
				
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
			}
				
			@Override
			public void characters(char ch[], int start, int length) throws SAXException {
				if (enterRootItem) {
					if (enterPath) {
						File rootFolder = new File(new String(ch, start, length));
						rootTreeItem = new RootTreeItem(rootFolder);
						treeView.setRoot(rootTreeItem);
							
						enterRootItem = false;
						enterPath = false;
					}
				}
					
				if (enterFolderItem) {
					if (enterPath) {
						File folder = new File(new String(ch, start, length));
						folderTreeItem = new FolderTreeItem(folder);
						treeView.getRoot().getChildren().add(folderTreeItem);
							
						enterFolderItem = false;
						enterPath = false;
					}
				}
					
				if (enterTemplateItem) {
					if (enterPath) {
						File template = new File(new String(ch, start, length));
						templateTreeItem = new TemplateTreeItem(template);
						folderTreeItem.getChildren().add(templateTreeItem);
								
						enterPath = false;
						enterTemplateItem = false;
					}
				}
			}
				
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.contentEquals("/" + XML_FOLDER_TEXT)) {
					enterFolderItem = false;
				}
			}
		};
		
		return handler;
	}
}