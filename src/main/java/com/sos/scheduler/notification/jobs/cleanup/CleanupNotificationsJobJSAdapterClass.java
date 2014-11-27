

package com.sos.scheduler.notification.jobs.cleanup;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
/**
 * \class 		CleanupNotificationsJobJSAdapterClass - JobScheduler Adapter for "CleanupNotifications"
 *
 * \brief AdapterClass of CleanupNotificationsJob for the SOSJobScheduler
 *
 * This Class CleanupNotificationsJobJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class CleanupNotificationsJob.
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-7198855759937042280.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20140512133635
 * \endverbatim
 */
public class CleanupNotificationsJobJSAdapterClass extends JobSchedulerJobAdapter  {
	private static Logger		logger			= Logger.getLogger(CleanupNotificationsJobJSAdapterClass.class);

	CleanupNotificationsJob objR = null;
	CleanupNotificationsJobOptions objO = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		this.objR = new CleanupNotificationsJob();
		this.objO = objR.Options();
		this.objO.CurrentNodeName(this.getCurrentNodeName());
		this.objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
	    this.objR.setJSJobUtilites(this);
	    
	    if(SOSString.isEmpty(objO.hibernate_configuration_file.Value())){
	    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
	    	objO.hibernate_configuration_file.Value(f.getAbsolutePath());
	    }
	    
        this.objR.init();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void exit() throws Exception {
		if(this.objR != null){
			this.objR.exit();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean spooler_init() {
		try{
			this.init();
		}
		catch(Exception ex){
			spooler_log.error(ex.getMessage());
			return false;
		}
		
		return super.spooler_init();
	}

	/**
	 * 
	 */
	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
        return signalSuccess();

	} // spooler_process

	/**
	 * 
	 */
	@Override
	public void spooler_exit() {
		super.spooler_exit();
		
		try{
			this.exit();
		}
		catch(Exception ex){
			spooler_log.warn(ex.getMessage());
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void doProcessing() throws Exception {
		objR.Execute();
	}

}

