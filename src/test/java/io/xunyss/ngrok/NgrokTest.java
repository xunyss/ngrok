package io.xunyss.ngrok;

import java.util.Date;

public class NgrokTest {
	
	public static void main(String[] args) throws Exception {
	
		Config config = ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
				.setLogFormat("logfmt")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("xtunnel")
						.setProto("http")
						.setAddr("9797")
				)
				.build();
		
		Ngrok ngrok = new Ngrok(config);
		ngrok.run("xtunnel1");
		
		System.err.println(new Date());
		Thread.sleep(5000);
		System.err.println("5초 잠");
		System.err.println(new Date());
		
		ngrok.kill();
	}
}
