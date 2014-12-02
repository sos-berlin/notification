

package com.sos.scheduler.notification.jobs.history;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;


/**
 * \class 		CheckHistoryJobMain - Main-Class for "CheckHistory"
 *
 * \brief MainClass to launch CheckHistoryJob as an executable command-line program
 *
 * This Class CheckHistoryJobMain is the worker-class.
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-7198855759937042280.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20140512133635 
 * \endverbatim
 */
public class CheckHistoryJobMain extends JSToolBox {
	private final static String					conClassName						= "CheckHistoryJobMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(CheckHistoryJobMain.class);

	protected CheckHistoryJobOptions	objOptions			= null;

	/**
	 * 
	 * \brief main
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info("CheckHistoryJob - Main"); //$NON-NLS-1$

		try {
			CheckHistoryJob objM = new CheckHistoryJob();
			CheckHistoryJobOptions objO = objM.Options();
			
			objO.CommandLineArgs(pstrArgs);
			objM.Execute();
		}
		
		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage()); 
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);		
			System.exit(intExitCode);
		}
		
		logger.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));		
	}

}  // class CheckHistoryJobMain