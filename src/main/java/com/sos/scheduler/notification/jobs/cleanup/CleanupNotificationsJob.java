

package com.sos.scheduler.notification.jobs.cleanup;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.notification.helper.DBConnector;
import com.sos.scheduler.notification.model.cleanup.CleanupNotificationsModel;

/**
 * \class 		CleanupNotificationsJob - Workerclass for "CleanupNotifications"
 *
 * \brief AdapterClass of CleanupNotificationsJob for the SOSJobScheduler
 *
 * This Class CleanupNotificationsJob is the worker-class.
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-7198855759937042280.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20140512133635 
 * \endverbatim
 */
public class CleanupNotificationsJob extends JSJobUtilitiesClass<CleanupNotificationsJobOptions> {
	private final String					conClassName						= "CleanupNotificationsJob";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(CleanupNotificationsJob.class);

	private DBConnector db;
	/**
	 * 
	 * \brief CleanupNotificationsJob
	 *
	 * \details
	 *
	 */
	public CleanupNotificationsJob() {
		super(new CleanupNotificationsJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		this.db = new DBConnector();
		this.db.connect(Options().hibernate_configuration_file.Value(),false);
	}

	/**
	 * 
	 */
	public void exit(){
		final String conMethodName = conClassName + "::exit";  //$NON-NLS-1$
		try {
			this.db.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s",conMethodName,e.getMessage()));
		}
	}
		
	/**
	 * 
	 * \brief Options - returns the CleanupNotificationsJobOptionClass
	 * 
	 * \details
	 * The CleanupNotificationsJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return CleanupNotificationsJobOptions
	 *
	 */
	public CleanupNotificationsJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new CleanupNotificationsJobOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of CleanupNotificationsJob
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see CleanupNotificationsJobMain
	 * 
	 * \return CleanupNotificationsJob
	 *
	 * @return
	 */
	public CleanupNotificationsJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			CleanupNotificationsModel model = new CleanupNotificationsModel();
			model.init(Options(),this.db.getDbLayer());
			model.process();
			model.exit();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName,e.getMessage()));
            throw e;			
		}
		
		return this;
	}

}  // class CleanupNotificationsJob