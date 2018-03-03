package io.xunyss.ngrok;

import java.io.File;
import java.io.IOException;

import io.xunyss.commons.io.FileUtils;

/**
 *
 * XUNYSS
 */
public class Config {
	
	/**
	 *
	 */
	private String path;
	
	
	/**
	 *
	 * @param configuration
	 */
	Config(String configuration) {
		BinaryManager binaryManager = BinaryManager.getInstance();
		
		// full-path of temporary config file
		path = binaryManager.getTempDirectoryPath() + String.format("ngrok_%s.conf", hashCode());
		
		// write configuration file
		try {
			FileUtils.writeString(new File(path), configuration);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 *
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 *
	 */
	@Override
	protected void finalize() /* throws Throwable */ {
		new File(path).delete();
	}
}
