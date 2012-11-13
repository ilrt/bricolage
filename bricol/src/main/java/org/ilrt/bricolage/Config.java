package org.ilrt.bricolage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

	public static final String URI_STEM = getInstance().get("linked.data.stem.url");
	// Elda control - must be visible from Bricolage webapp
	public static final String ELDA_CONTROL = getInstance().get("linked.data.control.url");
	public static final String DATA_FOLDER = getInstance().get("linked.data.folder");
	
	private static Config instance = null;
	
	private Properties props = new Properties();
	
	private Config() {
		InputStream is = Config.class.getResourceAsStream("/bricol.properties");
		if(is != null) {
			try {
				props.load(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}
	
	public String get(String name) {
		return props.getProperty(name);
	}


}
