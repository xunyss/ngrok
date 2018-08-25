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
	
	private SetupDetails setupDetails = null;
	private boolean setupFinished = false;
	private Object setupLock = new Object();
	
	private boolean running = false;
	
	
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
		Debug.log("called");
		
		if (running) {
			stop();
			throw new NgrokException("Ngrok is already running");
		}
		else {
			Debug.log("reset");
			setupDetails = null;
			setupFinished = false;
			running = true;
		}
		
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
//							synchronized (binaryManager) {
								// 2018.03.17 XUNYSS synchronized 제거
								// unregisterProcessMonitor 이미 동기화 처리 되어 있음
								Debug.log("exitValue: " + exitValue);
								binaryManager.unregisterProcessMonitor(processMonitor);
								running = false;
//							}
						}
						
						@Override
						public void onProcessFailed(ExecuteException ex) {
							// case 3 - 정상 수행도중 StreamHandler or LogHandler 에서 RuntimeException 발생
							//          > Watchdog.start() > StreamHandler.start()
							//          > StreamHandler.start() 메소드에서 RuntimeException 발생
							//          > ResultHandler.onProcessFailed()
//							synchronized (binaryManager) {
								// 2018.03.17 XUNYSS synchronized 제거
								// unregisterProcessMonitor 이미 동기화 처리 되어 있고
								// 뭐 굳이 다른 스레드와 경합할 일이 없어 보임
								//   가능성 거의 없지만 binaryManager 의 shutdown-hook 스레드와 경합한다 해도
								//   processMonitor.destroyProcess() 도 뭐 두번 이상 실행되도 상관 없기도 하고
								Debug.log(ex.toString());
								binaryManager.unregisterProcessMonitor(processMonitor);
								processMonitor.destroyProcess();
								running = false;
//							}
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
		
		Debug.log("ended");
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
		Debug.log("called");
		processMonitor.destroyProcess();
	}
	
	/**
	 *
	 */
	public void printUsage(OutputStream outputStream) {
		Debug.log("called");
		
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
	 * TODO: LineReadHandler 를 상속받는 형태로 변환 가능한지 검토
	 * 
	 * @author XUNYSS
	 */
	private final class NgrokProcessStreamHandler extends StreamHandler {
		
		private final LogParser logParser;
		
		
		private NgrokProcessStreamHandler() {
			logParser = LogParserFactory.createParser(config);
		}
		
		@Override
		public void start() {
			Debug.log("called");
			
			InputStream inputStream = getLogInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			// parse setup details
			try {
				setupDetails = logParser.parse(reader);
				Debug.log("succeeded to paring setup details");
			}
			// case 3
			catch (Exception ex) {
				Debug.log("failed to paring setup details");
				// close process input stream
				IOUtils.closeQuietly(reader);
				// fire ResultHandler.onProcessFailed()
				throw new NgrokException(ex);
			}
			// case 1 / case 2
			finally {
				synchronized (setupLock) {
					Debug.log("notify to waiting thread");
					setupFinished = true;
					setupLock.notify();
				}
			}
			
			// invoke log handler
			// setup details 가 parsing 성공 이후에 수행 함
			Debug.log("start log handler");
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					if (logHandler != null) {
						logHandler.handle(line);
					}
				}
				Debug.log("end log handler");
			}
			// case 3
			catch (Exception ex) {
				Debug.log("failed to handle log");
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
			Debug.log("called");
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
	private final class NgrokWatchdog extends Watchdog {
		
		@Override
		protected void start() {
			Debug.log("called");
		}
		
		@Override
		protected void stop() {
			Debug.log("called");
		}
		
		@Override
		public boolean isProcessRunning() {
			// 2018.03.11 XUNYSS Watchdog.isProcessRunning() 수정
			// 가능성은 낮지만 BinaryManager 의 shutdown-hook 에서 호출시
			// ProcessExecutor.execute() 수행 후, Watchdog.startMonitoring() 수행 전 의 시점이라면
			// Watchdog.startMonitoring() 수행 될때까지 isProcessRunning 호출한 스레드를 waiting 하도록
			// 그렇지 않으면 process 가 수행중이 아니라고 판단해 ngrok executable 은 좀비 프로세스가 됨
			Debug.log("called");
			return super.isProcessRunning();
		}
		
		@Override
		public void destroyProcess() {
			// 2018.03.11 XUNYSS Watchdog.isProcessRunning() 수정
			// 가능성은 낮지만 BinaryManager 의 shutdown-hook 에서 호출시
			// ProcessExecutor.execute() 수행 후, Watchdog.startMonitoring() 수행 전 의 시점이라면
			// Watchdog.startMonitoring() 수행 될때까지 isProcessRunning 호출한 스레드를 waiting 하도록
			// 그렇지 않으면 process 가 수행중이 아니라고 판단해 ngrok executable 은 좀비 프로세스가 됨
			Debug.log("called");
			super.destroyProcess();
		}
	}
}
