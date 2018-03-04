package io.xunyss.ngrok.parselog;

import io.xunyss.ngrok.SetupDetails;

/**
 *
 * @author XUNYSS
 */
public interface LogParser {
	
	void parseLine(final String line, final SetupDetails setupDetails);
}
