package io.xunyss.ngrok;

import java.util.Date;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokShutdownTest {
	
	// java -cp classes;test-classes;commons-base-1.0.0-RELEASE.jar;gson-2.8.2.jar io.xunyss.ngrok.NgrokShutdownTest
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
			}
		});
		ngrok.start("xtn");
		
		SetupDetails setupDetails = ngrok.getSetupDetails();
		System.out.print(new Date() + " : ");
		System.out.println("local admin address: " + setupDetails.getAddr());
		System.out.print(new Date() + " : ");
		System.out.println("established tunnel hostname:" + setupDetails.getHostname());
	}
}
