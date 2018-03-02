package io.xunyss.ngrok;

import java.net.InetAddress;

/**
 * 
 * @author XUNYSS
 */
public class Ngrok {
	
	//----------------------------------------------------------------------------------------------
	// https://ngrok.com/
	// other web-hook
	// https://www.pluralsight.com/guides/node-js/exposing-your-local-node-js-app-to-the-world
	// https://www.wa4e.com/md/lt_win.md
	// https://www.pluralsight.com/
	// https://github.com/localtunnel/localtunnel
	// http://www.ultrahook.com/
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
		
	
//		String hosts = "D:\\downloads\\hosts.txt";
//		BufferedReader reader = new BufferedReader(new FileReader(hosts));
//		String line;
//		boolean bb = false;
//		while ((line = reader.readLine()) != null) {
//			if (bb) {
//				System.err.println(line);
//
//				StringTokenizer stz = new StringTokenizer(line);
//				String ip = stz.nextToken();
//				String hn = stz.nextToken();
//				System.out.println(ip);
//				System.out.println(hn);
//			}
//			if (line.equals("# ideax-autogen")) {
//				bb = true;
//			}
//		}
//		reader.close();
//
//		FileWriter fw = new FileWriter(hosts, true);
//		fw.append("hahahahaha");
//		fw.close();
		
		InetAddress addr = InetAddress.getByName("jetbrains.license.laucyun.com");
		String s = addr.getHostAddress();
		System.out.println(addr);
		System.out.println(s);
		
		
		
	}
}
