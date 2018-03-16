package io.xunyss.ngrok;

import java.io.File;

import io.xunyss.commons.io.FileUtils;

/**
 *
 * @author XUNYSS
 */
public class BinaryManagerTest {
	
	public void deleteProcessFile() throws Exception {
		final String source = "D:/downloads/ngrok.exe";
		final String exe = "ngrok.exe";
		
		FileUtils.copy(new File(source), new File(FileUtils.getTempDirectory(), exe));
		
		String executable = FileUtils.getTempDirectory().getAbsolutePath() + FileUtils.FILE_SEPARATOR_CHAR + exe;
		final Process process = Runtime.getRuntime().exec(executable);
		
		new Thread() {
			@Override
			public void run() {
				process.destroy();
				boolean success = new File("C:\\Users\\xunyss\\AppData\\Local\\Temp\\ngrok.exe").delete();
				System.out.println("delete success: " + success);
				/*
				 * success 는 주로 true 이지만, 가끔씩 false 가 됨
				 * proc.destroy() 직후에는 executable file 이 지워지지 않을 수 있음
				 */
			}
		}.start();
		
		process.waitFor();
	}
	
	// temp 삭제 안되는 상황 자주 발생
	public static void main(String[] args) throws Exception {
		new BinaryManagerTest().deleteProcessFile();
	}
}
