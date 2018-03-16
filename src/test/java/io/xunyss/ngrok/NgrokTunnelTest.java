package io.xunyss.ngrok;

import java.util.Date;

import io.xunyss.commons.lang.ThreadUtils;
import io.xunyss.ngrok.debug.Debug;

/**
 *
 * @author XUNYSS
 */
public class NgrokTunnelTest {
	
	// temp 삭제 안되는 상황 한번도 없네, 후진 컴퓨터에서 테스트해봐도 잘 지워짐
	public static void main(String[] args) {
		
		Debug.setDebugEnable(false);
		
		Ngrok ngrok = new Ngrok(ConfigBuilder.create()
				.setAuthtoken(null)
				.setLogLevel("debug")
			//	.setLogFormat("logfmt")
				.setLogFormat("json")
				.setLog("stdout")
				.addTunnel(ConfigBuilder.createTunnel("xtn")
						.setProto("http")
						.setAddr("9797")
				)
				.build()
		);
		ngrok.setLogHandler(new LogHandler() {
			@Override
			protected void handle(String line) {
				System.out.println("[LOG] " + line);
			}
		});
		ngrok.start("xtn");

		SetupDetails setupDetails = ngrok.getSetupDetails();
		System.out.print(new Date() + " : ");
		System.out.println("local admin address: " + setupDetails.getAddr());
		System.out.print(new Date() + " : ");
		System.out.println("established tunnel hostname:" + setupDetails.getHostname());
		
		// 특정 시점에 stop 메소드로 종료
		ThreadUtils.sleep(1_000);
		ngrok.stop();
		System.out.print(new Date() + " : ");
		System.out.println("test program is stopped normally");
	}
}
