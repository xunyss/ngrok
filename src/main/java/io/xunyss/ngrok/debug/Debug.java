package io.xunyss.ngrok.debug;

import java.io.PrintStream;
import java.util.Date;

import io.xunyss.commons.lang.SystemUtils;

/**
 *
 * @author XUNYSS
 */
public final class Debug {

	public static final String NGROK_DEBUG = "io.xunyss.ngrok.debug";
	private static final String LOG_FORMAT = "[%1$5s] %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS.%2$tL {%3$s} %4$s.%5$s(:%6$d): %7$s";
	
	private static boolean debugEnable;
	static {
		debugEnable = Boolean.valueOf(SystemUtils.getSystemProperty(NGROK_DEBUG, "false"));
	}
	
	public static void setDebugEnable(boolean debugEnable) {
		Debug.debugEnable = debugEnable;
	}
	
	public static boolean isDebugEnable() {
		return debugEnable;
	}
	
	
	private static final PrintStream stream = System.out;
	private static final Date datetime = new Date();
	
	public static void out(String msg) {
		if (debugEnable) {
			stream.println(msg);
		}
	}
	
	public static void log(String msg) {
		if (debugEnable) {
			datetime.setTime(System.currentTimeMillis());
			
			String threadName = Thread.currentThread().getName();
			
			final int callerDepth = 1;
			StackTraceElement[] traces = new Throwable().getStackTrace();
			String className  = traces[callerDepth].getClassName();
			String methodName = traces[callerDepth].getMethodName();
			int lineNumber    = traces[callerDepth].getLineNumber();
			
			stream.println(String.format(LOG_FORMAT, "DEBUG", datetime, threadName, className, methodName, lineNumber, msg));
		}
	}
}
