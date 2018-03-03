package io.xunyss.ngrok;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.xunyss.commons.exec.ExecuteException;
import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.ResultHandler;
import io.xunyss.commons.exec.StreamHandler;
import io.xunyss.commons.exec.WatchDog;
import io.xunyss.commons.lang.ArrayUtils;

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
	
	NgrokWatchDog watchDog = new NgrokWatchDog();
	
	
	public Ngrok(Config config) {
		this.config = config;
	}
	
	public void run(String... tunnelNames) {
		String[] commands = {
				BinaryManager.getInstance().getExecutable(), "start",
				"-config", config.getPath()
		};
		
		
		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new NgrokProcessStreamHandler());
		processExecutor.setWatchDog(watchDog);
		
		try {
			processExecutor.execute(ArrayUtils.add(commands, tunnelNames), new ResultHandler() {
				@Override
				public void onProcessComplete(int exitValue) {
					System.err.println("onProcessCompleetet : " + exitValue);
				}
				@Override
				public void onProcessFailed(ExecuteException ex) {
					//
				}
			});
		}
		catch (ExecuteException ex) {
			ex.printStackTrace();
		}
	}
	
	public void kill() {
		watchDog.destroyNgrok();
	}
	
	
	private class NgrokProcessStreamHandler extends StreamHandler {
		
		@Override
		public void start() {
			InputStream inputStream = getProcessInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			int c = 0;
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					c++;
					System.out.println(line);
					
					if (c > 40) {
						// exit
						System.err.println("이제 그만 프로세스를 종료할까 합니다");
						watchDog.destroyNgrok();
					}
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		@Override
		public void stop() {
			System.err.println("Ngrok 스트림 핸들러 중지됨");
		}
	}
	
	private class NgrokWatchDog extends WatchDog {
		
		@Override
		protected void start() {
		}
		
		@Override
		protected void stop() {
		}
		
		public void destroyNgrok() {
			destroyProcess();
		}
	}
}
