package managers;

import util.OSChecker;
import util.PropertiesReaderWriter;

import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SettingsManager {
	private static SettingsManager instance;
	private static final String SETTINGS_FILE_NAME = "settings.txt";
	private static final String SETTINGS_DIR_NAME = "QuickText";
	private static final String SETTINGS_ROOT_DIR = OSChecker.getOSDataDirectory() + File.separator + SETTINGS_DIR_NAME;
	private static final String SETTINGS_PATH = SETTINGS_ROOT_DIR + File.separator + SETTINGS_FILE_NAME;
	private static final String KEY_VALUE_SEPARATOR = "=";
	
	private Properties settings;
	private Properties defaultSettings;
	private PropertiesReaderWriter propsReaderWriter;
	
	private SettingsManager() {
	}
	
	public static synchronized SettingsManager getInstance() {
		if (instance == null) {
			instance = new SettingsManager();
		}
		
		return instance;
	}
	
	public Properties getSettings() {
		return settings;
	}
	
	public Properties getDefaultSettings() {
		return defaultSettings;
	}
	
	public void setSettings(Properties settings) {
		this.settings = settings;
	}
	
	public void loadSettings(Properties defaultSettings) {
		this.defaultSettings = defaultSettings;
		
		propsReaderWriter = new PropertiesReaderWriter(SETTINGS_PATH, KEY_VALUE_SEPARATOR);
		
		try {
			settings = propsReaderWriter.readProperties();
		}
		catch (IOException e) {
			settings = this.defaultSettings;
		}
	}
	
	public void saveSettings() {
		propsReaderWriter = new PropertiesReaderWriter(SETTINGS_PATH, KEY_VALUE_SEPARATOR);
		
		try {
			Files.createDirectories(Paths.get(SETTINGS_ROOT_DIR));
			propsReaderWriter.writeProperties(settings);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
