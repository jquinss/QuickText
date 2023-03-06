package com.jquinss.quicktext.managers;

import com.jquinss.quicktext.enums.Charsets;
import com.jquinss.quicktext.util.OSChecker;
import com.jquinss.quicktext.util.PropertiesReaderWriter;

import java.util.Properties;
import java.io.IOException;

public class SettingsManager {
    private static final String APP_FOLDER_NAME = "QuickText";
    private static final String TEMPLATES_FOLDER_NAME = "templates";
    private static final String XML_FOLDER_NAME = "xml";
    private static final String BACKUPS_FOLDER_NAME = "backups";
    private static final String BACKUP_DATA_FILE_NAME = "backup_data.dat";
    private static final String SCHEDULED_BACKUP_DATA_FILE_NAME = "scheduled_backup_data.dat";
    private static final String REUSABLE_TEXT_DATA_FILE_NAME = "reusable_text.json";
    private static final String XML_FILE_NAME = "filetree.xml";
    private static final String SETTINGS_FILE_NAME = "settings.txt";
	private static final String APP_LOGO = "/com/jquinss/quicktext/images/logo.png";
	private static final String APP_CSS = "/com/jquinss/quicktext/styles/application.css";
    private static final String CACHE_MAX_ITEMS = "10";
    private static final String TEXT_CHARSET = Charsets.UTF_8.toString();
    private static final String BACKUP_MAX_ITEMS = "10";
    private static final String KEY_VALUE_SEPARATOR = "=";
    
    private static final String APP_DIR_PROP = "app_dir";
    private static final String TEMPLATES_DIR_PROP = "templates_dir";
    private static final String BACKUPS_DIR_PROP = "backups_dir";
    private static final String BACKUP_DATA_PATH_PROP = "backup_data_path";
    private static final String SCHEDULED_BACKUP_DATA_PATH_PROP = "scheduled_backup_data_path";
    private static final String REUSABLE_TEXT_DATA_PATH_PROP = "reusable_text_data_path";
    private static final String XML_DIR_PROP = "xml_dir";
    private static final String XML_PATH_PROP = "xml_path";
    private static final String SETTINGS_PATH_PROP = "settings_path";
    private static final String CACHE_MAX_ITEMS_PROP = "cache_max_items";
    private static final String TEXT_CHARSET_PROP = "text_charset";
    private static final String BACKUP_MAX_ITEMS_PROP = "backup_max_items";
	private static final String APP_LOGO_PROP = "app_logo";
	private static final String APP_CSS_PROP = "app_css";

	private final FileManager fileManager = new FileManager();
	private static SettingsManager instance;
	private final Properties defaultSettings;
	private final Properties optSettings;
	private PropertiesReaderWriter propsReaderWriter;
	
	private SettingsManager() {
		defaultSettings = loadDefaultSettings();
		optSettings = loadOptSettings();
	}
	
	public static synchronized SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		
		return instance;
	}
	
	private Properties loadOptSettings() {
		Properties optSettings = new Properties();
		propsReaderWriter = new PropertiesReaderWriter(getSettingsPath(), KEY_VALUE_SEPARATOR);
		
		try {
			optSettings = propsReaderWriter.readProperties();
		}
		catch (IOException e) {
			optSettings = new Properties();
		}
		
		return optSettings;
	}
	
	public void saveSettings() {
		propsReaderWriter = new PropertiesReaderWriter(getSettingsPath(), KEY_VALUE_SEPARATOR);
		
		try {
			propsReaderWriter.writeProperties(optSettings);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Properties loadDefaultSettings() {
    	Properties settings = new Properties();
    	
    	String appDir = fileManager.buildFilePath(OSChecker.getOSDataDirectory(), APP_FOLDER_NAME).toString();
    	String templatesDir = fileManager.buildFilePath(appDir, TEMPLATES_FOLDER_NAME).toString();
    	String xmlDir = fileManager.buildFilePath(appDir, XML_FOLDER_NAME).toString();
    	String xmlPath = fileManager.buildFilePath(xmlDir, XML_FILE_NAME).toString();
    	String settingsPath = fileManager.buildFilePath(appDir, SETTINGS_FILE_NAME).toString();
    	String backupsPath = fileManager.buildFilePath(appDir, BACKUPS_FOLDER_NAME).toString();
    	String backupDataPath = fileManager.buildFilePath(backupsPath, BACKUP_DATA_FILE_NAME).toString();
    	String scheduledBackupDataPath = fileManager.buildFilePath(backupsPath, SCHEDULED_BACKUP_DATA_FILE_NAME).toString();
    	String reusableTextDataPath = fileManager.buildFilePath(appDir, REUSABLE_TEXT_DATA_FILE_NAME).toString();
    	
    	settings.setProperty(APP_DIR_PROP, appDir);
    	settings.setProperty(TEMPLATES_DIR_PROP, templatesDir);
    	settings.setProperty(XML_DIR_PROP, xmlDir);
    	settings.setProperty(XML_PATH_PROP, xmlPath);
    	settings.setProperty(SETTINGS_PATH_PROP, settingsPath);
    	settings.setProperty(CACHE_MAX_ITEMS_PROP, CACHE_MAX_ITEMS);
    	settings.setProperty(BACKUP_MAX_ITEMS_PROP, BACKUP_MAX_ITEMS);
    	settings.setProperty(TEXT_CHARSET_PROP, TEXT_CHARSET);
    	settings.setProperty(BACKUPS_DIR_PROP, backupsPath);
    	settings.setProperty(BACKUP_DATA_PATH_PROP, backupDataPath);
    	settings.setProperty(SCHEDULED_BACKUP_DATA_PATH_PROP, scheduledBackupDataPath);
    	settings.setProperty(REUSABLE_TEXT_DATA_PATH_PROP, reusableTextDataPath);
		settings.setProperty(APP_LOGO_PROP, APP_LOGO);
		settings.setProperty(APP_CSS_PROP, APP_CSS);
    	
    	return settings;
	}
	
	public void resetSettings() {
		optSettings.clear();
	}
	
	public String getAppDir() {
		return defaultSettings.getProperty(APP_DIR_PROP);
	}
	
	public String getTemplatesDir() {
		return defaultSettings.getProperty(TEMPLATES_DIR_PROP);
	}
	
	public String getBackupsDir() {
		return defaultSettings.getProperty(BACKUPS_DIR_PROP);
	}
	
	public String getBackupDataPath() {
		return defaultSettings.getProperty(BACKUP_DATA_PATH_PROP);
	}
	
	public String getScheduledBackupDataPath() {
		return defaultSettings.getProperty(SCHEDULED_BACKUP_DATA_PATH_PROP);
	}
	
	public String getReusableTextDataPath() {
		return defaultSettings.getProperty(REUSABLE_TEXT_DATA_PATH_PROP);
	}
	
	public String getXMLDir() {
		return defaultSettings.getProperty(XML_DIR_PROP);
	}
	
	public String getXMLPath() {
		return defaultSettings.getProperty(XML_PATH_PROP);
	}
	
	public String getSettingsPath() {
		return defaultSettings.getProperty(SETTINGS_PATH_PROP);
	}
	public String getLogoPath() { return defaultSettings.getProperty(APP_LOGO_PROP); }
	public String getCSSPath() { return defaultSettings.getProperty(APP_CSS_PROP); }
	
	public String getCacheMaxItems() {
		if (optSettings.containsKey(CACHE_MAX_ITEMS_PROP)) {
			return optSettings.getProperty(CACHE_MAX_ITEMS_PROP);
		}
		
		return getDefaultCacheMaxItems();
	}
	
	public void setCacheMaxItems(String maxItems) {
		optSettings.setProperty(CACHE_MAX_ITEMS_PROP, maxItems);
	}
	
	public String getDefaultCacheMaxItems() {
		return defaultSettings.getProperty(CACHE_MAX_ITEMS_PROP);
	}
	
	public String getBackupMaxItems() {
		if (optSettings.containsKey(BACKUP_MAX_ITEMS_PROP)) {
			return optSettings.getProperty(BACKUP_MAX_ITEMS_PROP);
		}
		
		return getDefaultBackupMaxItems();
	}
	
	public void setBackupMaxItems(String maxItems) {
		optSettings.setProperty(BACKUP_MAX_ITEMS_PROP, maxItems);
	}
	
	
	public String getDefaultBackupMaxItems() {
		return defaultSettings.getProperty(BACKUP_MAX_ITEMS_PROP);
	}
	
	public String getTextCharset() {
		if (optSettings.containsKey(TEXT_CHARSET_PROP)) {
			return optSettings.getProperty(TEXT_CHARSET_PROP);
		}
		
		return getDefaultTextCharset();
	}
	
	public void setTextCharset(String charset) {
		optSettings.setProperty(TEXT_CHARSET_PROP, charset);
	}
	
	public String getDefaultTextCharset() {
		return defaultSettings.getProperty(TEXT_CHARSET_PROP);
	}
}
