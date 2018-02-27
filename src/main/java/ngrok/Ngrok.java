package ngrok;

import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.PumpStreamHandler;

/**
 * 
 * @author XUNYSS
 */
public class Ngrok {
	
	//----------------------------------------------------------------------------------------------
	//
	//----------------------------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new PumpStreamHandler());
//		processExecutor.execute("cmd /c D:\\downloads\\ngrok.exe start -config D:\\downloads\\conf.yml httpbin");
		processExecutor.execute("cmd /c D:\\downloads\\ngrok.exe http 9797");
	}
}
