package io.xunyss.ngrok;

import java.io.BufferedReader;
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
	
	/**
	 * Ngrok Configuration.
	 */
	private final Config config;
	
	private LogHandler logHandler;
	private NgrokWatchdog processMonitor;
	
	
	String addr;
	String hostname;
	private boolean established = false;
	private Object establishLock = new Object();
	
	/**
	 *
	 * @param config
	 */
	public Ngrok(Config config) {
		this.config = config;
	}
	
	/**
	 *
	 * @param logHandler
	 */
	public void setLogHandler(LogHandler logHandler) {
		this.logHandler = logHandler;
	}
	
	/**
	 *
	 * @param tunnelNames
	 */
	public void start(String... tunnelNames) {
		// ngrok commands
		String[] commandsUsingConfing = {
				BinaryManager.getInstance().getExecutable(), "start",
				"-config", config.getPath()
		};
		
		// process executor
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new NgrokProcessStreamHandler());
		processExecutor.setWatchdog(processMonitor = new NgrokWatchdog());
		
		// execute ngrok process
		try {
			processExecutor.execute(
					tunnelNames != null ?
					ArrayUtils.add(commandsUsingConfing, tunnelNames) :
					ArrayUtils.toArray(BinaryManager.getInstance().getExecutable()),
					new NgrokResultHandler()
			);
		}
		catch (ExecuteException ex) {
			throw new NgrokException(ex);
		}
		catch (NgrokException ex) {
			throw ex;
		}
		
		// register process watchdog - when process
		BinaryManager.getInstance().registerProcessMonitor(processMonitor);
		
		// waiting for establish
		synchronized (establishLock) {
			while (!established) {
				try {
					establishLock.wait();
				}
				catch (InterruptedException ex) {
					throw new NgrokException(ex);
				}
			}
		}
		
		System.err.println("run 메소드의 마지막 줄");
	}
	
	/**
	 *
	 */
	public void usage() {
		start(null);
	}
	
	/**
	 *
	 */
	public void stop() {
		processMonitor.destroyProcess();
	}
	
	
	//==============================================================================================
	
	/**
	 *
	 * @author XUNYSS
	 */
	class NgrokProcessStreamHandler extends StreamHandler {
		
		@Override
		public void start() {
			InputStream inputStream = getProcessInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					if (logHandler != null) {
						logHandler.handle(line);
					}
					
					Gson gson = new Gson();
					Map log = gson.fromJson(line, Map.class);
					
					synchronized (establishLock) {
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
									established = true;
									establishLock.notify();
								}
							}
						}
					}
				}
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
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
	 * @author XUNYSS
	 */
	class NgrokWatchdog extends Watchdog {
		
		@Override
		protected void start() {
		}
		
		@Override
		protected void stop() {
		}
		
		@Override
		public boolean isProcessRunning() {
			return super.isProcessRunning();
		}
		
		@Override
		public void destroyProcess() {
			super.destroyProcess();
		}
	}
	
	/**
	 *
	 * @author XUNYSS
	 */
	class NgrokResultHandler implements ResultHandler {
		
		@Override
		public void onProcessComplete(int exitValue) {
			System.err.println("onProcessComplete : " + exitValue);
			
			shutdownProcess();
		}
		
		@Override
		public void onProcessFailed(ExecuteException ex) {
			System.err.println("onProcessFailed : " + ex);
			System.err.println(processMonitor.isProcessRunning());
			
			shutdownProcess();
		}
		
		private void shutdownProcess() {
			synchronized (establishLock) {
				//
				BinaryManager.getInstance().unregisterProcessMonitor(processMonitor);
				processMonitor.destroyProcess();
				
				establishLock.notify();
			}
		}
	}
}
