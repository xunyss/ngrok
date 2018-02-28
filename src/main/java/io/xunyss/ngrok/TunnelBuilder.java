package io.xunyss.ngrok;

/**
 *
 * XUNYSS
 */
public class TunnelBuilder {
	
	public static TunnelBuilder create() {
		return new TunnelBuilder();
	}
	
	private TunnelBuilder() {
		super();
	}
	
	
	private String protocol;
	private String addr;
	
	public final TunnelBuilder setProto(String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	public final TunnelBuilder setAddr(String addr) {
		this.addr = addr;
		return this;
	}
	
	public void /* Tunnel */ build() {
	
	}
}
