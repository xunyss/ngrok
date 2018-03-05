package io.xunyss.ngrok.parselog;

import java.io.BufferedReader;
import java.io.IOException;
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
	public SetupDetails parse(BufferedReader reader) throws IOException {
		
		/*
		 * case 1 - setup details 정보가 올바르게 parsing 됨 (정상 case)
		 *          > 정상적으로 setupDetails 객체 리턴
		 * case 2 - parsing 도중 RuntimeException 발생 (정상 case)
		 *          > setupDetails 객체에 error flag, message 셋팅하여 리턴
		 * case 3 - reader 객체에서 stream read 도중 IOException 발생
		 *          > throw IOException
		 */
		
		SetupDetails setupDetails = new SetupDetails();
		
		String line;
		while ((line = reader.readLine()) != null) {
			// 파싱중 한번이라도 에러나면 stream 끝날때 까지 다 에러로 가정
			if (setupDetails.isError()) {
				setupDetails.appendErrorLine(line);
				continue;
			}
			// setup 정보 파싱
			try {
				// parse line for setup details
				parseLine(line, setupDetails);
				// setup 정보 파싱 완료시 종료
				if (setupDetails.isComplete()) {
					break;
				}
			}
			catch (Exception ex) {
				// fail ed to parse
				setupDetails.setError(true);
				setupDetails.appendErrorLine(line);
				continue;
			}
		}
		
		return setupDetails;
	}
	
	@SuppressWarnings({"unused", "unchecked"})
	private void parseLine(final String line, final SetupDetails setupDetails) {
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
