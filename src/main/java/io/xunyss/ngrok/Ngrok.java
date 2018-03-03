package io.xunyss.ngrok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.Gson;

import io.xunyss.commons.exec.ExecuteException;
import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.ResultHandler;
import io.xunyss.commons.exec.StreamHandler;
import io.xunyss.commons.exec.Watchdog;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.lang.ArrayUtils;
import io.xunyss.commons.lang.StringUtils;

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
	// http://www.ultrahook.com/
	//----------------------------------------------------------------------------------------------
	
	private final Config config;
	
	private LogHandler logHandler;
	
	private NgrokWatchdog watchdog;
	
	
	String addr;
	String hostname;
	boolean setup = false;
	
	
	public Ngrok(Config config) {
		this.config = config;
	}
	
	public void setLogHandler(LogHandler logHandler) {
		this.logHandler = logHandler;
	}
	
	public void start(String... tunnelNames) {
		// ngrok commands
		String[] commands = {
				BinaryManager.getInstance().getExecutable(), "start",
				"-config", config.getPath()
		};
		
		// process executor
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new NgrokProcessStreamHandler());
		processExecutor.setWatchdog(watchdog = new NgrokWatchdog());
		
		// execute ngrok process
		try {
//			processExecutor.execute(ArrayUtils.add(commands, tunnelNames), new NgrokResultHandler());
			processExecutor.execute(new String[] { BinaryManager.getInstance().getExecutable() }, new NgrokResultHandler());
		}
		catch (ExecuteException ex) {
			ex.printStackTrace();
		}
		
		// register process watchdog
		BinaryManager.getInstance().registerProcessWatchdog(watchdog);
		
//		synchronized (this) {
//			while (!setup && watchdog.isProcessRunning()) {
//				try {
//					wait();
//				}
//				catch (InterruptedException ex) {
//					ex.printStackTrace();
//				}
//			}
//		}
		
		System.err.println("run 메소드의 마지막 줄");
	}
	
	public void stop() {
		watchdog.destroyProcess();
	}
	
	
	//==============================================================================================
	
	/**
	 *
	 */
	class NgrokProcessStreamHandler extends StreamHandler {
		
		@Override
		public void start() {
			InputStream inputStream = getProcessInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					System.out.println("readline>> " + line);
					
//					if (logHandler != null) {
//						logHandler.handle(line);
//					}
					
//					if (setup) {
//						System.err.println(line);
//					}
//					else {
//						System.out.println(line);
//					}
					
					Gson gson = new Gson();
					Map log = gson.fromJson(line, Map.class);
					
					synchronized (Ngrok.this) {
						System.out.println("readline>>sync>> "+line);
						
						if ("starting web service".equals(log.get("msg"))) {
							addr = log.get("addr").toString();
						}
						else if ("start tunnel listen".equals(log.get("msg"))) {
							if ("http".equals(log.get("proto"))) {
								String opts = log.get("opts").toString();
								int ps = opts.indexOf('{');
								int pe = opts.lastIndexOf('}');
								
								opts = opts.substring(ps + 1, pe);
								int hs = opts.indexOf(':');
								int he = opts.indexOf(' ', hs);
								
								hostname = opts.substring(hs + 1, he);
								if (StringUtils.isNotEmpty(hostname)) {
									setup = true;
									Ngrok.this.notifyAll();
								}
							}
						}
					}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			finally {
				IOUtils.closeQuietly(reader);
			}
		}
		
		@Override
		public void stop() {
			System.err.println("Ngrok 스트림 핸들러 중지됨");
		}
	}
	
	/**
	 *
	 */
	class NgrokWatchdog extends Watchdog {
		
		@Override
		protected void start() {
		}
		
		@Override
		protected void stop() {
		}
		
		public boolean isProcessRunning() {
			return super.isProcessRunning();
		}
		
		public void destroyProcess() {
			super.destroyProcess();
		}
	}
	
	/**
	 *
	 */
	class NgrokResultHandler implements ResultHandler {
		
		@Override
		public void onProcessComplete(int exitValue) {
			System.err.println("onProcessComplete : " + exitValue);
			
			synchronized (Ngrok.this) {
				BinaryManager.getInstance().unregisterProcessWatchdog(watchdog);
				Ngrok.this.notifyAll();
			}
		}
		
		@Override
		public void onProcessFailed(ExecuteException ex) {
			System.err.println("onProcessFailed : " + ex);
			System.err.println(watchdog.isProcessRunning());
		}
	}
}
