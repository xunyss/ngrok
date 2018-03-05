package io.xunyss.ngrok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import io.xunyss.commons.exec.ExecuteException;
import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.PumpStreamHandler;
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
	private boolean setupFinished = false;
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
	 * @throws NgrokException
	 */
	public void start(String... tunnelNames) throws NgrokException {
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
//					tunnelNames == null ?												// for process TEST
//					ArrayUtils.toArray(BinaryManager.getInstance().getExecutable()) :	// for process TEST
					ArrayUtils.add(commandsUsingConfing, ArrayUtils.nullToEmpty(tunnelNames)),
					
					new ResultHandler() {
						@Override
						public void onProcessComplete(int exitValue) {
							// case 1 - 수행도중 destroy 호출 되는 경우 (stop 메소드 호출, runtime shutdown hook 호출 등..)
							// case 2 - 프로세스가 할 일 다하고 스스로 정상적으로 종료 됨 (exitValue == process.exitValue())
							//          > watchdog start > stream handler start
							//          > Ngrok.start 메소드에서 NgrokException 던진 후 catch 문 수행
							//          > stream handler stop > watchdog stop
							//          > ResultHandler.onProcessComplete
							System.err.println("onProcessComplete : " + exitValue);
						}
						
						@Override
						public void onProcessFailed(ExecuteException ex) {
							// case 3 -
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
			while (!setupFinished) {
				try {
					setupLock.wait();
				}
				catch (InterruptedException ex) {
					throw new NgrokException(ex);
				}
			}
		}
		// case 1, case 3 - waiting loop 종료와 함께 즉시 start 메소드 종료
		////////// case 3 일경우에도 여기서 exception 던질 필요가 있나..............
		// case 2 - throw NgrokException
		if (setupDetails.isError()) {
			throw new NgrokException(setupDetails.getErrorMessage());
		}
		
		System.err.println("run 메소드의 마지막 줄");
	}
	
	/**
	 *
	 */
	public void stop() {
		processMonitor.destroyProcess();
	}
	
	/**
	 *
	 */
	public void reset() {
	
	}
	
	/**
	 *
	 */
	public void printUsage(OutputStream outputStream) {
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new PumpStreamHandler(outputStream));
		try {
			processExecutor.execute(BinaryManager.getInstance().getExecutable());
		}
		catch (ExecuteException ex) {
			throw new NgrokException(ex);
		}
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
		public void start() {System.err.println("Ngrok 스트림 핸들러 시작됨");
			InputStream inputStream = getLogInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			// parse setup details
			try {
				setupDetails = logParser.parse(reader);
			}
			// case 3
			catch (IOException ex) {
				// close process input stream
				IOUtils.closeQuietly(reader);
				// fire ResultHandler.onProcessFailed()
				throw new NgrokException(ex);
			}
			// case 1 / case 2
			finally {
				synchronized (setupLock) {
					setupFinished = true;
					setupLock.notify();
				}
			}
			
			// invoke log handler
			// setup details 가 parsing 성공 이후에 수행 함
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					if (logHandler != null) {
						logHandler.handle(line);
					}
				}
			}
			catch (IOException | RuntimeException ex) {
				// fire ResultHandler.onProcessFailed()
				throw new NgrokException(ex);
			}
			finally {
				// close process input stream
				IOUtils.closeQuietly(reader);
			}
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
			System.err.println(">> watchdog start");
		}
		
		@Override
		protected void stop() {
			System.err.println(">> watchdog stop");
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
