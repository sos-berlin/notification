package com.sos.scheduler.notification.jobs.notifier;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
/**
 * 
 * @author Robert Ehrlich
 *
 */
public class SystemNotifierJobJSAdapterClass extends JobSchedulerJobAdapter  {
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(SystemNotifierJobJSAdapterClass.class);

	SystemNotifierJob job = null;
	SystemNotifierJobOptions options = null;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		job = new SystemNotifierJob();
		options = job.getOptions();
		options.CurrentNodeName(this.getCurrentNodeName());
		options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
	    job.setJSJobUtilites(this);
	    
	    if(SOSString.isEmpty(options.hibernate_configuration_file.Value())){
	    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
	    	options.hibernate_configuration_file.Value(f.getAbsolutePath());
	    }

	    job.init(spooler);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void exit() throws Exception {
		if(job != null){
			job.exit();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean spooler_init() {
		try{
			init();
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
			job.Execute();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
		finally {
		} // finally
        return signalSuccess();

	} 

	/**
	 * 
	 */
	@Override
	public void spooler_exit() {
		super.spooler_exit();
		
		try{
			exit();
		}
		catch(Exception ex){
			spooler_log.warn(ex.getMessage());
		}
	}

}

