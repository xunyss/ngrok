package ngrok;

import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.PumpStreamHandler;

/**
 * 
 * @author XUNYSS
 */
public class Ngrok {
	
	//----------------------------------------------------------------------------------------------
	// https://ngrok.com/
	//----------------------------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new PumpStreamHandler());
		processExecutor.execute("cmd /c D:\\downloads\\ngrok.exe start -config D:\\xdev\\git\\ngrok\\src\\test\\resources\\conf.yml httpbin");
//		processExecutor.execute("cmd /c D:\\downloads\\ngrok.exe http 9797");
		
		
		
	}
}
