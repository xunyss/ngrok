package io.xunyss.ngrok;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokUsageTest {
	
	public static void main(String[] args) {
		
		Debug.setDebugEnable(true);
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create().build());
		
		// print usage output
		ngrok.printUsage(System.out);
		
		// occur error
//		try {
//			ngrok.start(null);
//		}
//		catch (NgrokException ex) {
//			System.out.println("TEST >> NgrokException occurred >>");
//			System.out.println(ex.getMessage());
//		}
	}
}
