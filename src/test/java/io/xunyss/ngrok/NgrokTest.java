package io.xunyss.ngrok;

import org.junit.Ignore;
import org.junit.Test;

import io.xunyss.commons.exec.ExecuteException;

public class NgrokTest {
	
	@Ignore
	@Test
	public void test() throws Exception{
	
		Config config = ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
				.setLogFormat("logfmt")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("httpbin")
						.setProto("http")
						.setAddr("9797")
				)
				.build();
		
		Ngrok ngrok = new Ngrok(config);
//		ngrok.run("httpbin");
		
		Thread.sleep(5000);
	}
}
