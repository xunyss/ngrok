package io.xunyss.ngrok;

import io.xunyss.commons.exec.ExecuteException;
import io.xunyss.commons.exec.ProcessExecutor;
import io.xunyss.commons.exec.PumpStreamHandler;
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
	
	private Config config;
	
	
	public Ngrok(Config config) {
		this.config = config;
	}
	
	public void run(String... tunnelNames) {
		String[] commands = {
				BinaryManager.getInstance().getExecutable(), "start",
				"-config", config.getPath()
		};

		ProcessExecutor processExecutor = new ProcessExecutor();
		processExecutor.setStreamHandler(new PumpStreamHandler());
		try {
			processExecutor.execute(ArrayUtils.add(commands, tunnelNames));
		}
		catch (ExecuteException ex) {
			ex.printStackTrace();
		}
	}
	
	public void kill() {
	
	}
}
