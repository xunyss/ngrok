package io.xunyss.ngrok;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokErrorTest {
	
	// temp 삭제 안되는 상황 계속 발생
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
			@Override
			protected void handle(String line) {
				System.out.println("[LOG] " + line);
				// occur exception
				System.out.println(100 / 0);
			}
		});
		ngrok.start("xtn");
	}
}
