package io.xunyss.ngrok;

import java.util.Date;

/**
 *
 * @author XUNYSS
 */
public class NgrokTunnelTest {
	
	public static void main(String[] args) throws Exception {
		
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
			}
		});
		ngrok.start("xtn");

		SetupDetails setupDetails = ngrok.getSetupDetails();
		System.out.print(new Date() + " : ");
		System.out.println("local admin address: " + setupDetails.getAddr());
		System.out.print(new Date() + " : ");
		System.out.println("established tunnel hostname:" + setupDetails.getHostname());
		
		Thread.sleep(10_000);
		ngrok.stop();
		System.err.print(new Date() + " : ");
		System.err.println("test program is stopped normally");
	}
}
