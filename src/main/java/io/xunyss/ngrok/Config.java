package io.xunyss.ngrok;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.xunyss.commons.io.FileUtils;

/**
 *
 * XUNYSS
 */
public class Config {
	
	private static List<String> generatedConfigFiles = Collections.synchronizedList(new ArrayList<String>());
	
	
	private String configFilePath;
	
	Config(String configString) {
		configFilePath = "D:/downloads/ngrokconf/ngrok_" + hashCode() + ".conf";
		try {
			FileUtils.writeString(new File(configFilePath), configString);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
		// add static list
		generatedConfigFiles.add(configFilePath);
	}
	
	public String getPath() {
		return configFilePath;
	}
	
	/**
	 *
	 */
	@Override
	protected void finalize() /* throws Throwable */ {
		if (configFilePath != null) {
			generatedConfigFiles.remove(configFilePath);
			new File(configFilePath).delete();
		}
	}
}
