package io.xunyss.ngrok;

/**
 *
 * @author XUNYSS
 */
public class NgrokException extends RuntimeException {
	
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
