package io.xunyss.ngrok;

import java.util.Date;
import java.util.Map;

import com.google.gson.Gson;
import org.junit.Test;

public class NgrokTest {
	
	public static void main(String[] args) throws Exception {
	
		Ngrok ngrok = new Ngrok(ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
			//	.setLogFormat("logfmt")
				.setLogFormat("json")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("xtunnel")
						.setProto("http")
						.setAddr("9797")
				)
				.build()
		);
//		ngrok.setLogHandler(new LogHandler() {
//			@Override
//			protected void handle(String line) {
//				System.out.println(line);
//			}
//		});
		ngrok.start("xtunnel");
		
		System.out.println(ngrok.addr);
		System.out.println(ngrok.hostname);
		
		
//		System.err.println(new Date());
//		Thread.sleep(5000);
//		System.err.println("5초 잠");
//		System.err.println(new Date());
//
//		ngrok.kill();
	}
}
