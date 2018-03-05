package io.xunyss.ngrok;

import io.xunyss.commons.lang.StringUtils;

/**
 *
 * @author XUNYSS
 */
public class SetupDetails {
	
	private static final String EOL = System.lineSeparator();
	
	private String addr;
	private String hostname;
	
	private boolean error = false;
	private StringBuilder errorMessage = null;
	
	
	public void setAddr(String addr) {
		this.addr = addr;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public boolean isComplete() {
		return	StringUtils.isNotEmpty(addr) &&
				StringUtils.isNotEmpty(hostname);
	}
	
	public String getAddr() {
		return addr;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setError(boolean error) {
		this.error = error;
	}
	
	public boolean isError() {
		return error;
	}
	
	public void appendErrorLine(String line) {
		if (errorMessage == null) {
			errorMessage = new StringBuilder();
		}
		errorMessage.append(line).append(EOL);
	}
	
	public String getErrorMessage() {
		return errorMessage.toString();
	}
}
