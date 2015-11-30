package com.sos.scheduler.notification.jobs.history;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;

public class CheckHistoryJobMain extends JSToolBox {
	private final static String	className = CheckHistoryJobMain.class.getSimpleName();
	private static Logger logger	= Logger.getLogger(CheckHistoryJobMain.class);

	protected CheckHistoryJobOptions	objOptions			= null;

	public final static void main(String[] args) {
		final String methodName = className + "::main";

		logger.info(String.format(methodName));
		int exitCode = 0;
		CheckHistoryJob job = new CheckHistoryJob();
		try {
			CheckHistoryJobOptions options = job.Options();
			options.CommandLineArgs(args);
			
			job.init();
			job.execute();

			logger.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
		}
		catch (Exception e) {
			exitCode = 99;
			logger.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, exitCode), e);		
		}
		finally{
			job.exit();
		}
		System.exit(exitCode);
	}

}