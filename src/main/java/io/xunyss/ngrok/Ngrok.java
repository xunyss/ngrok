package io.xunyss.ngrok;

import java.io.BufferedReader;
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
import io.xunyss.ngrok.debug.Debug;
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
		// BinaryManager singleton instance
		final BinaryManager binaryManager = BinaryManager.getInstance();
		
		// ngrok execution commands
		String[] commandsUsingConfing = {
				binaryManager.getExecutable(), "start",
				"-config", config.getPath()
		};
		
		// process executor
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new NgrokProcessStreamHandler());
		processExecutor.setWatchdog(processMonitor = new NgrokWatchdog());
		
		// register process watchdog - when process
		binaryManager.registerProcessMonitor(processMonitor);
		
		// execute ngrok process
		try {
			processExecutor.execute(
//					tunnelNames == null ?									// for process TEST
//					ArrayUtils.toArray(binaryManager.getExecutable()) :		// for process TEST
					ArrayUtils.add(commandsUsingConfing, ArrayUtils.nullToEmpty(tunnelNames)),
					
					new ResultHandler() {
						@Override
						public void onProcessComplete(int exitValue) {
							// case 1 - 정상 수행도중 Ngrok.stop() > Process.destroy 호출 되는 경우
							//          > Watchdog.start() > StreamHandler.start()
							//          > Ngrok.start() 메소드 정상적으로 종료 / LogHandler.handle() 수행 지속
							//          > Ngrok.stop() > Process.destroy()
							//          > StreamHandler.stop() > Watchdog.stop()
							//          > ResultHandler.onProcessComplete()
							// case 1 - Ctrl + C 입력으로 Runtime Shutdown Hook 스레드 실행
							//          > Watchdog.start() > StreamHandler.start()
							//          > Ngrok.start() 메소드 정상적으로 종료 / LogHandler.handle() 수행 지속
							//          > Runtime Shutdown Hook 스레드 실행 > Process.destroy()
							//            Runtime Shutdown Hook 스레드가 종료하면 다른 스레드들은 즉시 종료 (내 추축)
							//            StreamHandler.stop() > Watchdog.stop() > ResultHandler.onProcessComplete()
							//            이 세개의 실행 여부는 보장 할 수 없음
							//            Runtime Shutdown Hook 스레드가 천천히 끝나면 실행 될 꺼임
							// case 2 - 프로세스가 할 일 다하고 스스로 정상적으로 종료 됨 (exitValue == process.exitValue())
							//          > Watchdog.start() > StreamHandler.start()
							//          > StreamHandler.start() 정상 종료
							//          > Ngrok.start() 메소드에서 NgrokException 던짐 (catch 문 수행)
							//          > StreamHandler.stop() > Watchdog.stop()
							//          > ResultHandler.onProcessComplete()
							synchronized (binaryManager) {
								Debug.log("onProcessComplete: " + exitValue);
								binaryManager.unregisterProcessMonitor(processMonitor);
							}
						}
						
						@Override
						public void onProcessFailed(ExecuteException ex) {
							// case 3 - 정상 수행도중 StreamHandler or LogHandler 에서 RuntimeException 발생
							//          > Watchdog.start() > StreamHandler.start()
							//          > StreamHandler.start() 메소드에서 RuntimeException 발생
							//          > ResultHandler.onProcessFailed()
							synchronized (binaryManager) {
								Debug.log("onProcessFailed: " + ex.toString());
								binaryManager.unregisterProcessMonitor(processMonitor);
								processMonitor.destroyProcess();
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
		// case 1 - waiting loop 종료와 함께 즉시 start 메소드 종료
		// case 2 - waiting loop 종료와 후 throw NgrokException
		// case 3 - waiting loop 종료와 함께 즉시 start 메소드 종료
		if (setupDetails.isError()) {
			throw new NgrokException(setupDetails.getErrorMessage());
		}
		
		Debug.log("Ngrok.start() finished");
	}
	
	/**
	 *
	 * @return
	 */
	public SetupDetails getSetupDetails() {
		return setupDetails;
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
		// TODO implements
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
		public void start() {
			Debug.log("StreamHandler.start() called");
			
			InputStream inputStream = getLogInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			// parse setup details
			try {
				setupDetails = logParser.parse(reader);
			}
			// case 3
			catch (Exception ex) {
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
			// case 3
			catch (Exception ex) {
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
			Debug.log("StreamHandler.stop() called");
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
			Debug.log("Watchdog.start() called");
		}
		
		@Override
		protected void stop() {
			Debug.log("Watchdog.stop() called");
		}
		
		@Override
		public boolean isProcessRunning() {
			// 아직 internalStart, start 함수가 한번도 실행 안된상태에서 함수 호출시 (셧다운훅에서(가능성은낮지만))
			// 즉 execute 수행 시점, start() 수행 시점 사이에 isProcessRunning 호출됐을때
			// 대비하기 위해 그럴땐 start 먹을때 까지 isProcessRunning 호출한 스레드를 waiting
			// ## commons.exec 의 Watchdog 도 같이 수정해야 함
			return super.isProcessRunning();
		}
		
		@Override
		public void destroyProcess() {
			// 이 함수도 마찬가지임
			// execute 수행 시점, start() 수행 시점 사이에 isProcessRunning 호출됐을때
			super.destroyProcess();
		}
	}
}
