package io.xunyss.ngrok;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author XUNYSS
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
	
	private static final String EOL = System.lineSeparator();
	
	private final boolean consoleUi = false;
	
	private String logLevel = "debug";
	private String logFormat = "json";
	private String log = "stdout";
	
	private String authtoken;
	private String webAddr;
	private List<Tunnel> tunnels;
	
	
	private ConfigBuilder() {
		super();
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
	
	public final ConfigBuilder setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
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
		StringBuilder conf = new StringBuilder();
		
		conf.append("console_ui: ").append(consoleUi).append(EOL);
		if (logLevel != null) {
			conf.append("log_level: ").append(logLevel).append(EOL);
		}
		if (logFormat != null) {
			conf.append("log_format: ").append(logFormat).append(EOL);
		}
		if (log != null) {
			conf.append("log: ").append(log).append(EOL);
		}
		if (authtoken != null) {
			conf.append("authtoken: ").append(authtoken).append(EOL);
		}
		if (tunnels != null && tunnels.size() > 0) {
			conf.append("tunnels:").append(EOL);
			for (Tunnel tunnel : tunnels) {
				conf.append("  ").append(tunnel.name).append(":").append(EOL);
				if (tunnel.proto != null) {
					conf.append("    proto: ").append(tunnel.proto).append(EOL);
				}
				if (tunnel.addr != null) {
					conf.append("    addr: ").append(tunnel.addr).append(EOL);
				}
				if (tunnel.hostname != null) {
					conf.append("    hostname: ").append(tunnel.hostname).append(EOL);
				}
				if (tunnel.subdomain != null) {
					conf.append("    subdomain: ").append(tunnel.subdomain).append(EOL);
				}
			}
		}
		
		return new Config(logLevel, logFormat, log, conf.toString());
	}
	
	
	//==============================================================================================
	
	/**
	 *
	 */
	public static class Tunnel {
		
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
