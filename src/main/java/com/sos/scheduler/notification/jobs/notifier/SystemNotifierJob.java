

package com.sos.scheduler.notification.jobs.notifier;

import org.apache.log4j.Logger;

import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.notifier.SystemNotifierModel;

/**
 * \class 		SystemNotifierJob - Workerclass for "SystemNotifierJob"
 *
 * \brief AdapterClass of SystemNotifierJob for the SOSJobScheduler
 *
 * This Class SystemNotifierJob is the worker-class.
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-3613842323690924441.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20140513161021 
 * \endverbatim
 */
public class SystemNotifierJob extends JSJobUtilitiesClass<SystemNotifierJobOptions> {
	private final String					conClassName						= "SystemNotifierJob";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(SystemNotifierJob.class);
	
    SystemNotifierModel model = null;
    
	/**
	 * 
	 * \brief SystemNotifierJob
	 *
	 * \details
	 *
	 */
	public SystemNotifierJob() {
		super(new SystemNotifierJobOptions());
	}

	public void init(Spooler spooler) throws Exception {
		this.model = new SystemNotifierModel(Options());
		this.model.init();
		this.model.setSpooler(spooler);
	}

	public void exit(){
		final String conMethodName = conClassName + "::exit";  //$NON-NLS-1$
		try {
			this.model.exit();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s",conMethodName,e.getMessage()));
		}
	}
	
	public INotificationModel getModel(){
		return this.model;
	}
	
	/**
	 * 
	 * \brief Options - returns the SystemNotifierJobOptionClass
	 * 
	 * \details
	 * The SystemNotifierJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return SystemNotifierJobOptions
	 *
	 */
	public SystemNotifierJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new SystemNotifierJobOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of SystemNotifierJob
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see SystemNotifierJobMain
	 * 
	 * \return SystemNotifierJob
	 *
	 * @return
	 */
	public SystemNotifierJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			try{
				this.model.process();
			}
			catch(Exception ex){
				logger.info(String.format(
						"this.model.process : %s"
						,ex.getMessage()));
				
				throw ex;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName,e.getMessage()));
            throw e;			
		}
				
		
		return this;
	}

}  // class SystemNotifierJob