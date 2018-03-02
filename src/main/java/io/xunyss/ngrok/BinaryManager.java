package io.xunyss.ngrok;

import java.io.File;

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
//	private final File temporaryDirectory = new File(FileUtils.getTempDirectory(), "io_xunyss_ngrok_bin");
	private final File temporaryDirectory = new File("D:/downloads/tmpdir", "io_xunyss_ngrok_bin");
	private String binaryName;		// executable binary name
	
	
	private BinaryManager() {
		install();
	}
	
	public void install() {
		String suitableBinary = null;
		
		if (SystemUtils.IS_OS_WINDOWS) {
			binaryName = temporaryDirectory.getPath() + FileUtils.FILE_SEPARATOR + "ngrok.exe";
			String osArch = SystemUtils.getSystemProperty("os.arch");
			if ("amd64".equals(osArch)) {
				suitableBinary = "/win64/ngrok-stable-windows-amd64.zip";
			}
		}
		else {
			binaryName = temporaryDirectory.getPath() + FileUtils.FILE_SEPARATOR + "ngrok";
			// TODO: impl
		}
		
		try {
			String resourcePath = BINARY_RESOURCE_ROOT + suitableBinary;
			ZipUtils.unzip(ResourceUtils.getResourceAsStream(resourcePath), temporaryDirectory);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String getBinaryName() {
		return binaryName;
	}
	
	public File getTemporaryDirectory() {
		return temporaryDirectory;
	}
}
