

package com.sos.scheduler.notification.jobs.result;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.notification.helper.DBConnector;
import com.sos.scheduler.notification.model.result.StoreResultsModel;

/**
 * \class 		StoreResultsJob - Workerclass for "StoreResultsJob"
 *
 * \brief AdapterClass of StoreResultsJob for the SOSJobScheduler
 *
 * This Class StoreResultsJob is the worker-class.
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-1003156690106171278.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20140508144459 
 * \endverbatim
 */
public class StoreResultsJob extends JSJobUtilitiesClass<StoreResultsJobOptions> {
	private final String					conClassName						= "StoreResultsJob";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(StoreResultsJob.class);
	
	private DBConnector db;

	/**
	 * 
	 * \brief StoreResultsJob
	 *
	 * \details
	 *
	 */
	public StoreResultsJob() {
		super(new StoreResultsJobOptions());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		this.db = new DBConnector();
		this.db.connect(Options().scheduler_notification_hibernate_configuration_file.Value(),this.Options().force_reconnect.value());
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
	 * \brief Options - returns the StoreResultsJobOptionClass
	 * 
	 * \details
	 * The StoreResultsJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return StoreResultsJobOptions
	 *
	 */
	public StoreResultsJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new StoreResultsJobOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of StoreResultsJob
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see StoreResultsJobMain
	 * 
	 * \return StoreResultsJob
	 *
	 * @return
	 */
	public StoreResultsJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$
		
		logger.debug(conMethodName);
		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			
			StoreResultsModel model = new StoreResultsModel();
			model.init(Options(),this.db.getDbLayer());
			model.process();
			model.exit();
		}	
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName, e));
            throw e;			
		}
		finally {
			//logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName ) );
		}
		return this;
	}

}  // class StoreResultsJob