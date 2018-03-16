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
import io.xunyss.commons.lang.ThreadUtils;
import io.xunyss.commons.lang.ZipUtils;
import io.xunyss.ngrok.debug.Debug;

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
	
	
	private boolean tempDirectoryCreated = false;
	private boolean installed = false;
	
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
		
		// 2018.03.05 XUNYSS
		// install 은 getExecutable() 메소드가 최초로 실행될 때 한번 수행 함
//		install();
		
		// remove temporary binaries when system exit
		registerShutdownHook();
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
		// 2018.03.05 XUNYSS
		// 생성자에서 shutdown hook 추가
		// getExecutable 을 수행하지 않아도 Config 객체가 생성되면 Temporary directory 가 생성됨
//		finally {
//			// remove temporary binaries when system exit
//			registerShutdownHook();
//		}
	}
	
	/**
	 *
	 */
	private void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread("Ngrok ShutdownHook") {
			// 정상 Tunneling 수행 중 "Ctrl + C" 입력으로 shutdown-hook 실행되는 case
			// {"Ngrok ShutdownHook"}
			//   > 1. 종료 처리되지 않은 (현재 실행중인) process 종료
			//   > 2. 임시 디렉토리 삭제
			// {"Process Launcher"}
			//   > StreamHandler.start() 메소드에서 streamReader.readLine() 은 'null' 을 리턴
			//   > StreamHandler.stop() > Watchdog.stop() > ResultHandler.onProcessComplete()
			//   * onProcessComplete() 가 실행된 경우 unregisterProcessMonitor() 실행은 의미없는 작업임
			//     "Ngrok ShutdownHook" thread 가 BinaryManager singleton instance 를 lock 잡고 process 를 종료했기 때문
			//   * "Ngrok ShutdownHook" thread 가 끝나면 다른 스레드들은 중지되므로 실행 여부는 보장할 수 없음
			// {"main" or Ngrok.start() 를 실행된 thread}
			//   > 만약 StreamHandler.start() 메소드에서 setupLock.notify() 실행 전이라면
			//     StreamHandler.start() 메소드에서 setupLock.notify() 실행 되면서 Ngrol.start() 메소드 종료
			//   * "Ngrok ShutdownHook" thread 가 끝나면 다른 스레드들은 중지되므로 실행 여부는 보장할 수 없음
			@Override
			public void run() {
				Debug.log("Ngrok BinaryManager 'shutdown-hook' start");
				
				// 1. 종료 처리되지 않은 (현재 실행중인) process 종료
				synchronized (BinaryManager.this) {
					for (Ngrok.NgrokWatchdog processMonitor : processMonitors) {
						Debug.log("registered process monitor: " +
								processMonitor.toString() + " " + processMonitor.getProcessCommands());
						if (processMonitor.isProcessRunning()) {
							Debug.log("destroy running process: " + processMonitor.toString());
							processMonitor.destroyProcess();
						}
					}
				}
				
				// 2. 임시 디렉토리 삭제
				// Process.destroy() 이후에 아주 아주 즉시 executable 파일을 삭제시도 할 경우 실패하는 현상 발생
				// "Ngrok ShutdownHook" thread 의 종료시간을 Thread.sleep() 으로 0.1 초만 지연 시켜도 거의 잘 지워짐
//				FileUtils.deleteDirectoryQuietly(tempDirectory);
				// 2018.03.16 XUNYSS
				// 임시 디렉토리 삭제시 re-try 로직을 추가 하여 (retry count: 10, delay time: 100ms)
				// "Ngrok ShutdownHook" thread 의 종료시간을 지연 시켜 정상적으로 삭제 될 수 있도록 유도
				// "Process Launcher" thread 도 종료 지연으로 인해 자연스럽게 onProcessComplete() 에 도달 할 수 있음
				for (int retryCount = 0; retryCount < 10; retryCount++) {
					FileUtils.deleteDirectoryQuietly(tempDirectory);
					if (!tempDirectory.exists()) {
						Debug.log("temporary directory is deleted");
						return;
					}
					ThreadUtils.sleep(100);
				}
				Debug.log("temporary directory is not deleted");
			}
		});
	}
	
//	/**
//	 *
//	 * @return
//	 */
//	File getTempDirectory() {
//		return tempDirectory;
//	}
	
	/**
	 *
	 * @return
	 */
	String getTempDirectoryPath() {
		if (!tempDirectoryCreated) {
			tempDirectoryCreated = true;
			tempDirectory.mkdir();
		}
		return tempDirectoryPath;
	}
	
	/**
	 *
	 * @return
	 */
	String getExecutable() {
		if (!installed) {
			installed = true;
			install();
		}
		return executable;
	}
	
	/**
	 *
	 * @param watchdog
	 */
	void registerProcessMonitor(Ngrok.NgrokWatchdog watchdog) {
		synchronized (this) {
			processMonitors.add(watchdog);
		}
	}
	
	/**
	 *
	 * @param watchdog
	 */
	void unregisterProcessMonitor(Ngrok.NgrokWatchdog watchdog) {
		synchronized (this) {
			processMonitors.remove(watchdog);
		}
	}
}
