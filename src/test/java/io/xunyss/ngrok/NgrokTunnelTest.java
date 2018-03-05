package io.xunyss.ngrok;

/**
 *
 * @author XUNYSS
 */
public class NgrokTunnelTest {
	
	public static void main(String[] args) {
		
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
				System.out.println(line);
			}
		});
		ngrok.start("xtn");
	}
}
