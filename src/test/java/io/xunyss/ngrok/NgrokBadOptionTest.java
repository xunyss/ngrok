package io.xunyss.ngrok;

import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokBadOptionTest {
	
	public static void main(String[] args) {
		
		Debug.setDebugEnable(true);
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
				.setLogFormat("json")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("xtn")
						.setProto("http")
						.setAddr("9797")
				)
				.build()
		);
		
		try {
			ngrok.start("invalid_tunnel_name");
		}
		catch (NgrokException ex) {
			System.out.println("TEST >> NgrokException occurred >>");
			System.out.println(ex.getMessage());
		}
	}
}
