package io.xunyss.ngrok;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import io.xunyss.commons.io.FileUtils;
import io.xunyss.commons.io.ResourceUtils;
import io.xunyss.commons.lang.StringUtils;
import io.xunyss.commons.lang.SystemUtils;
import io.xunyss.commons.lang.ZipUtils;

/**
 * 
 * @author XUNYSS
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
	
	private List<Ngrok.NgrokWatchdog> processMonitors;
	
	
	/**
	 *
	 */
	private BinaryManager() {
		tempDirectory = new File(FileUtils.getTempDirectory(), TEMP_DIRECTORY_NAME);
		tempDirectoryPath = tempDirectory.getPath() + FileUtils.FILE_SEPARATOR;
		// 2018.03.04 XUNYSS
		// iterator 사용으로 인해
		// synchronizedList 대신 ArrayList 를 synchronized 구문을 사용해서 사용
//		processMonitors = Collections.synchronizedList(new ArrayList<Ngrok.NgrokWatchdog>());
		processMonitors = new ArrayList<>();
		install();
	}
	
	/**
	 *
	 */
	private void install() {
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
		
		// set full-path executable
		// unpack executable to temporary directory
		unpackExecutable(executableName, suitableBinaryResource);
	}
	
	/**
	 *
	 * @param executableName
	 * @param suitableBinaryResource
	 */
	private void unpackExecutable(String executableName, String suitableBinaryResource) {
		//------------------------------------------------------------------------------------------
		// throw exception if no suitable binary
		if (StringUtils.isEmpty(suitableBinaryResource)) {
			throw new NgrokException("No suitable binary resource");
		}
		
		//------------------------------------------------------------------------------------------
		// full-path of ngrok executable
		executable = tempDirectoryPath + executableName;
		
		//------------------------------------------------------------------------------------------
		// unpack executable to temporary directory
		try {
			String resourcePath = BINARY_RESOURCE_ROOT + suitableBinaryResource;
			ZipUtils.unzip(ResourceUtils.getResourceAsStream(resourcePath), tempDirectory);
		}
		catch (IOException ex) {
			throw new NgrokException("Cannot unpack executable resource", ex);
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
				// 1. 종료 처리되지 않은 (현재 실행중인) ngrok process 종료
				synchronized (processMonitors) {
					for (Ngrok.NgrokWatchdog processMonitor : processMonitors) {
						if (processMonitor.isProcessRunning()) {
							processMonitor.destroyProcess();
						}
					}
				}
				// 2. 임시 디렉토리 삭제
				FileUtils.deleteDirectoryQuietly(tempDirectory);
			}
		});
	}
	
	/**
	 *
	 * @return
	 */
	String getExecutable() {
		return executable;
	}
	
	/**
	 *
	 * @return
	 */
	String getTempDirectoryPath() {
		return tempDirectoryPath;
	}
	
	/**
	 *
	 * @param watchdog
	 */
	void registerProcessMonitor(Ngrok.NgrokWatchdog watchdog) {
		synchronized (processMonitors) {
			processMonitors.add(watchdog);
		}
	}
	
	/**
	 *
	 * @param watchdog
	 */
	void unregisterProcessMonitor(Ngrok.NgrokWatchdog watchdog) {
		synchronized (processMonitors) {
			processMonitors.remove(watchdog);
		}
	}
}
