package io.xunyss.ngrok.parselog;

import io.xunyss.ngrok.Config;
import io.xunyss.ngrok.NgrokException;

/**
 *
 * @author XUNYSS
 */
public class LogParserFactory {
	
	/**
	 *
	 * @param config
	 * @return
	 */
	public static LogParser createParser(Config config) {
		switch (config.getLogFormat()) {
			case "logfmt":
				return new FmtLogParser();
			case "json":
				return new JsonLogParser();
		}
		
		throw new NgrokException("Invalid log format type: " + config.getLogFormat());
	}
}
