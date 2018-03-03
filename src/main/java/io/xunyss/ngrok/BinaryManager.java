package io.xunyss.ngrok;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.ResourceUtils;
import io.xunyss.commons.lang.SystemUtils;
import io.xunyss.commons.lang.ZipUtils;

/**
 *
 * XUNYSS
 */
public class BinaryManager {
	
	/**
	 *
	 */
	private static class SingletonHolder {
		// singleton object
		private static final BinaryManager instance = new BinaryManager();
	}
	
	/**
	 *
	 * @return
	 */
	public static BinaryManager getInstance() {
		return SingletonHolder.instance;
	}
	
	
	//----------------------------------------------------------------------------------------------
	
	private static final String BINARY_RESOURCE_ROOT = "/io/xunyss/ngrok/binary";
	private static final String TEMP_DIRECTORY_NAME;
	
	static {
		// 서로 다른 JVM 이 임시 디렉토리를 공유하지 않게 하기 위해
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		TEMP_DIRECTORY_NAME = "io_xunyss_ngrok_bin_" + jvmName.replace('@', '-');
	}
	
	private File tempDirectory;			// temporary directory
	private String tempDirectoryPath;	// temporary directory path (ends with FILE_SEPARATOR)
	private String executable;			// executable binary name
	
	
	/**
	 *
	 */
	private BinaryManager() {
		tempDirectory = new File(FileUtils.getTempDirectory(), TEMP_DIRECTORY_NAME);
		tempDirectoryPath = tempDirectory.getPath() + FileUtils.FILE_SEPARATOR;
		install();
	}
	
	/**
	 *
	 */
	public void install() {
		String executableName = null;
		String suitableBinaryResource = null;
		
		if (SystemUtils.IS_OS_WINDOWS) {
			executableName = "ngrok.exe";
			switch (SystemUtils.OS_ARCH) {
				case "amd64":
					suitableBinaryResource = "/win64/ngrok-stable-windows-amd64.zip";
					break;
			}
		}
		else {
			// TODO: implement
			executableName = "";
			suitableBinaryResource = "";
		}
		
		// full-path of ngrok executable
		executable = tempDirectoryPath + executableName;
		
		try {
			// unpack executable to temporary directory
			String resourcePath = BINARY_RESOURCE_ROOT + suitableBinaryResource;
			ZipUtils.unzip(ResourceUtils.getResourceAsStream(resourcePath), tempDirectory);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			// remove temporary binaries when system exit
			registerShutdownHook();
		}
	}
	
	/**
	 *
	 */
	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				FileUtils.deleteDirectoryQuietly(tempDirectory);
			}
		});
	}
	
	/**
	 *
	 * @return
	 */
	public String getExecutable() {
		return executable;
	}
	
	/**
	 *
	 * @return
	 */
	String getTempDirectoryPath() {
		return tempDirectoryPath;
	}
}
