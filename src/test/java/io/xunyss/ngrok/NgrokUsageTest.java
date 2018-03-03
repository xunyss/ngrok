package io.xunyss.ngrok;

/**
 *
 * @author XUNYSS
 */
public class NgrokUsageTest {
	
	public static void main(String[] args) {
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create().build());
		ngrok.usage();
	}
}
