package io.xunyss.ngrok.parselog;

import java.io.BufferedReader;
import java.io.IOException;

import io.xunyss.ngrok.SetupDetails;

/**
 *
 * @author XUNYSS
 */
public interface LogParser {
	
	SetupDetails parse(BufferedReader reader) throws IOException;
}
