package io.xunyss.ngrok;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokErrorTest {
	
	public static void main(String[] args) {
		
		Debug.setDebugEnable(true);
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
			//	.setLogFormat("logfmt")
				.setLogFormat("json")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("xtn")
						.setProto("http")
						.setAddr("9797")
				)
				.build()
		);
		ngrok.setLogHandler(new LogHandler() {
			int logCount = 0;
			@Override
			protected void handle(String line) {
				System.out.println("[LOG] " + line);
				if (logCount++ > 10) {
					// occur exception
					System.out.println(100 / 0);
				}
			}
		});
		ngrok.start("xtn");
	}
}
