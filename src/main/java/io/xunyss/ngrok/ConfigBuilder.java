package io.xunyss.ngrok;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * XUNYSS
 */
public class ConfigBuilder {
	
	/**
	 *
	 * @return
	 */
	public static ConfigBuilder create() {
		return new ConfigBuilder();
	}
	
	/**
	 *
	 * @param name
	 * @return
	 */
	public static Tunnel createTunnel(String name) {
		return new Tunnel(name);
	}
	
	
	//----------------------------------------------------------------------------------------------
	
	private final boolean consoleUi = false;
	
	private String authtoken;
	private String logLevel;
	private String logFormat;
	private String log;
	private String webAddr;
	private List<Tunnel> tunnels;
	
	
	private ConfigBuilder() {
		super();
	}
	
	public final ConfigBuilder setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
		return this;
	}
	
	public final ConfigBuilder setLogLevel(String logLevel) {
		this.logLevel = logLevel;
		return this;
	}
	
	public final ConfigBuilder setLogFormat(String logFormat) {
		this.logFormat = logFormat;
		return this;
	}
	
	public final ConfigBuilder setLog(String log) {
		this.log = log;
		return this;
	}
	
	public final ConfigBuilder setWebAddr(String webAddr) {
		this.webAddr = webAddr;
		return this;
	}
	
	public final ConfigBuilder addTunnel(Tunnel tunnel) {
		if (this.tunnels == null) {
			this.tunnels = new ArrayList<>();
		}
		this.tunnels.add(tunnel);
		return this;
	}
	
	public Config build() {
		StringBuilder configString = new StringBuilder();
		
		configString.append("console_ui: ").append(consoleUi).append("\r\n");
		if (authtoken != null) {
			configString.append("authtoken: ").append(authtoken).append("\r\n");
		}
		if (logLevel != null) {
			configString.append("log_level: ").append(logLevel).append("\r\n");
		}
		if (logFormat != null) {
			configString.append("log_format: ").append(logFormat).append("\r\n");
		}
		if (log != null) {
			configString.append("log: ").append(log).append("\r\n");
		}
		if (tunnels != null && tunnels.size() > 0) {
			configString.append("tunnels:").append("\r\n");
			for (Tunnel tunnel : tunnels) {
				configString.append("  ").append(tunnel.name).append(":").append("\r\n");
				if (tunnel.proto != null) {
					configString.append("    proto: ").append(tunnel.proto).append("\r\n");
				}
				if (tunnel.addr != null) {
					configString.append("    addr: ").append(tunnel.addr).append("\r\n");
				}
				if (tunnel.hostname != null) {
					configString.append("    hostname: ").append(tunnel.hostname).append("\r\n");
				}
				if (tunnel.subdomain != null) {
					configString.append("    subdomain: ").append(tunnel.subdomain).append("\r\n");
				}
			}
		}
		
		return new Config(configString.toString());
	}
	
	
	//==============================================================================================
	
	/**
	 *
	 */
	static class Tunnel {
		
		private String name;
		
		private String proto;
		private String addr;
		private String hostname;
		private String subdomain;
		
		
		/**
		 *
		 * @param name
		 */
		private Tunnel(String name) {
			this.name = name;
		}
		
		/**
		 *
		 * @param proto
		 * @return
		 */
		public final Tunnel setProto(String proto) {
			this.proto = proto;
			return this;
		}
		
		/**
		 *
		 * @param addr
		 * @return
		 */
		public final Tunnel setAddr(String addr) {
			this.addr = addr;
			return this;
		}
		
		/**
		 *
		 * @param hostname
		 * @return
		 */
		public final Tunnel setHostname(String hostname) {
			this.hostname = hostname;
			return this;
		}
		
		/**
		 *
		 * @param subdomain
		 * @return
		 */
		public final Tunnel setSubdomain(String subdomain) {
			this.subdomain = subdomain;
			return this;
		}
	}
}
