package io.xunyss.ngrok;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokUsageTest {
	
	// temp 삭제 안되는 상황 가끔씩 발생
	// => shutdown-hook 에 retry/delay 반영하여 해결
	public static void printUsage() {
		Ngrok ngrok = new Ngrok(ConfigBuilder.create().build());
		
		// print usage output
		ngrok.printUsage(System.out);
	}
	
	// temp 삭제 안되는 상황 발생
	// => shutdown-hook 에 retry/delay 반영하여 해결
	public static void startNull() {
		Ngrok ngrok = new Ngrok(ConfigBuilder.create().build());
		
		// occur error
		try {
			ngrok.start();
		}
		catch (NgrokException ex) {
			System.out.println("TEST >> NgrokException occurred >>");
			System.out.println(ex.getMessage());
		}
	}
	
	public static void main(String[] args) {
		
		Debug.setDebugEnable(true);

		printUsage();
		startNull();
	}
}
