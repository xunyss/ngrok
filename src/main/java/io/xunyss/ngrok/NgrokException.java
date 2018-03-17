package io.xunyss.ngrok;

/**
 *
 * @author XUNYSS
 */
public class NgrokException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3888505567589332371L;
	
	
	public NgrokException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NgrokException(String message) {
		super(message);
	}
	
	public NgrokException(Throwable cause) {
		super(cause);
	}
	
	public NgrokException() {
		super();
	}
}
