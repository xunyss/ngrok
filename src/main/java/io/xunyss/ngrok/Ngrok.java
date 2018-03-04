package io.xunyss.ngrok;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.xunyss.commons.exec.ExecuteException;
import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.ResultHandler;
import io.xunyss.commons.exec.StreamHandler;
import io.xunyss.commons.exec.Watchdog;
import io.xunyss.commons.io.IOUtils;
import io.xunyss.commons.lang.ArrayUtils;
import io.xunyss.ngrok.parselog.LogParser;
import io.xunyss.ngrok.parselog.LogParserFactory;

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
	
	private SetupDetails setupDetails;
	private boolean setupComplete = false;
	private boolean setupError = false;
	private Object setupLock = new Object();
	
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
					
					new ResultHandler() {
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
							synchronized (setupLock) {
								//
								BinaryManager.getInstance().unregisterProcessMonitor(processMonitor);
								processMonitor.destroyProcess();
								
								setupLock.notify();
							}
						}
					}
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
		synchronized (setupLock) {
			while (!setupComplete) {
				try {
					setupLock.wait();
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
		
		private final LogParser logParser;
		
		
		private NgrokProcessStreamHandler() {
			logParser = LogParserFactory.createParser(config);
		}
		
		@Override
		public void start() {
			InputStream inputStream = getLogInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					
					if (setupError) {
						setupDetails.appendErrorLine(line);
						continue;
					}
					
					if (!setupComplete) {
						try {
							// parse line for setup details
							logParser.parseLine(line, setupDetails);
						}
						catch (Exception ex) {
							// fail ed to parse
							setupDetails.setError(setupError = true);
							setupDetails.appendErrorLine(line);
							continue;
						}
						if (setupDetails.isComplete()) {	// 모든 필요한 setup details 정보가 전부 파싱된 상태
							synchronized (setupLock) {		// waiting 중이던 setup 메소드를 깨움
								setupComplete = true;
								setupLock.notify();
							}
						}
					}
					
					// invoke log handler
					if (logHandler != null) {
						logHandler.handle(line);
					}
				}
			}
			catch (Exception ex) {
				setupComplete = true;/////////////////// start 바로 위에 wait 깨워야지, while 조건문이 setupComplete 이니.........
				throw new RuntimeException(ex);
			}
			finally {
				IOUtils.closeQuietly(reader);
			}
			
			synchronized (setupLock) {
				setupComplete = true;
				setupLock.notify();
			}
			//if (occurError) System.err.println(errmsg);
		}
		
		@Override
		public void stop() {
			System.err.println("Ngrok 스트림 핸들러 중지됨");
		}
		
		private InputStream getLogInputStream() {
			String log = config.getLog();
			if ("stdout".equals(log)) {
				return getProcessInputStream();
			}
			else if ("stderr".equals(log)) {
				return getProcessErrorStream();
			}
			return null;
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
}
