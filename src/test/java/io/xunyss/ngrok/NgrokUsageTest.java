package io.xunyss.ngrok;

/**
 *
 * @author XUNYSS
 */
public class NgrokUsageTest {
	
	public static void main(String[] args) {
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create().build());
		ngrok.setLogHandler(new LogHandler() {
			@Override
			protected void handle(String line) {
				System.out.println(line);
			}
		});
		ngrok.usage();
	}
}
