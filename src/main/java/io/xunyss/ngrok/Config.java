package io.xunyss.ngrok;

import java.io.File;
import java.io.IOException;

import io.xunyss.commons.io.FileUtils;

/**
 *
 * @author XUNYSS
 */
public class Config {
	
	private String logLevel;
	private String logFormat;
	private String log;
	
	/**
	 *
	 */
	private String path;
	
	
	/**
	 *
	 * @param logLevel
	 * @param logFormat
	 * @param log
	 * @param configuration
	 */
	Config(String logLevel, String logFormat, String log, String configuration) {
		this.logLevel = logLevel;
		this.logFormat = logFormat;
		this.log = log;
		
		BinaryManager binaryManager = BinaryManager.getInstance();
		
		// make temporary directory
		binaryManager.getTempDirectory().mkdirs();
		
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
	public String getLogLevel() {
		return logLevel;
	}
	
	/**
	 *
	 * @return
	 */
	public String getLogFormat() {
		return logFormat;
	}
	
	/**
	 *
	 * @return
	 */
	public String getLog() {
		return log;
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
