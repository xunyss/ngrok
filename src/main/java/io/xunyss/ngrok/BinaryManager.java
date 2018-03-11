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
			// 얘가 processExec execute 실행 직후 실행된다면,, process 객체 생성전에 실행되면...
			// 이 thread 가 실행되고 execute 에 의해 process 가 실행된다면....
			// ** ctrl+C 테스트
			// java -cp classes;test-classes;commons-base-1.0.0-RELEASE.jar;gson-2.8.2.jar io.xunyss.ngrok.NgrokTunnelTest
			@Override
			public void run() {
				// boolean shutdown = true; 해서 true 이면 register/ unregister 안되게 할까
				// 1. 종료 처리되지 않은 (현재 실행중인) ngrok process 종료
				synchronized (BinaryManager.this) {
					Debug.log("BinaryManager shutdown-hook start");
					for (Ngrok.NgrokWatchdog processMonitor : processMonitors) {
						System.out.println("pppppppppppppppppppp"+processMonitor);
						// ** temp 안지워지는문제
						// 아래줄 있던없던 destroyprocess 했음에도 임시디렉토리 안지워지는 경우 계속 발생
						// 원인을 찾자
						// isRunning 은 ..... thread 순서 안맞으수있나?
						if (processMonitor.isProcessRunning()) {
							//	System.out.println(" >>>>> desssssssssssssssss");
							processMonitor.destroyProcess();
						}
					}
				}
//				try { Thread.sleep(100); } catch (Exception e) {}
				// 2. 임시 디렉토리 삭제
				// Process.destroy() 이후에 즉시 수행될 경우 삭제 되지 않는 현상 발생
				FileUtils.deleteDirectoryQuietly(tempDirectory);
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
