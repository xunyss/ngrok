package io.xunyss.ngrok;

public class NgrokBadOptionTest {
	
	public static void main(String[] args) {
		
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
			System.err.println(">>>> NgrokException occurred >>>>");
			System.err.println(ex.getMessage());
		}
	}
}
