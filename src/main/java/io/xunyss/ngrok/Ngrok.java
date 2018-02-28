package io.xunyss.ngrok;

import java.io.FileWriter;

/**
 * 
 * @author XUNYSS
 */
public class Ngrok {
	
	//----------------------------------------------------------------------------------------------
	// https://ngrok.com/
	//----------------------------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {
//		ProcessExecutor processExecutor = new ProcessExecutor();
//		processExecutor.setStreamHandler(new PumpStreamHandler());
//		int ev = processExecutor.execute("cmd /c C:\\downloads\\ngrok.exe start -config C:\\xdev\\git\\ngrok\\src\\test\\resources\\conf.yml httpbin");
//		System.out.println("exit value: " + ev);
//		processExecutor.execute("cmd /c D:\\downloads\\ngrok.exe http 9797");
		
		
//		TunnelBuilder tb = TunnelBuilder.create()
//				.setProto("http")
//				.setAddr("")
//				.build();
		
	
		String hosts = "C:\\Windows\\System32\\drivers\\etc\\hosts";
		
//		String out = FileUtils.toString(new File(hosts));
//		System.out.println(out);
//
		FileWriter fw = new FileWriter(hosts, true);
		fw.append("hahahahaha");
		fw.close();
	
	
	}
}
