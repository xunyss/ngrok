package io.xunyss.ngrok.parselog;

import java.util.Map;

import com.google.gson.Gson;

import io.xunyss.ngrok.SetupDetails;

/**
 *
 * @author XUNYSS
 */
public class JsonLogParser implements LogParser {
	
	private Gson gson = new Gson();
	
	
	@Override
	@SuppressWarnings({"unused", "unchecked"})
	public void parseLine(final String line, final SetupDetails setupDetails) {
		// parse to map
		Map<String, String> logMap = gson.fromJson(line, Map.class);
		
		//------------------------------------------------------------------------------------------
		if ("starting web service".equals(logMap.get("msg"))) {
			// local admin address
			String addr = logMap.get("addr");
			setupDetails.setAddr(addr);
		}
		//------------------------------------------------------------------------------------------
		else if ("start tunnel listen".equals(logMap.get("msg"))) {
			if ("http".equals(logMap.get("proto"))) {
				String opts = logMap.get("opts");
				int ps = opts.indexOf('{');
				int pe = opts.lastIndexOf('}');
				
				opts = opts.substring(ps + 1, pe);
				int hs = opts.indexOf(':');
				int he = opts.indexOf(' ', hs);
				
				// hostname
				String hostname = opts.substring(hs + 1, he);
				setupDetails.setHostname(hostname);
			}
		}
		//------------------------------------------------------------------------------------------
	}
}
