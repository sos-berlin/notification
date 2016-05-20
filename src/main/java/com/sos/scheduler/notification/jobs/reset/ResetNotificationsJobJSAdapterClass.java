package com.sos.scheduler.notification.jobs.reset;

import java.io.File;

import sos.scheduler.job.JobSchedulerJobAdapter; 
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class ResetNotificationsJobJSAdapterClass extends JobSchedulerJobAdapter  {

	@Override
	public boolean spooler_process() throws Exception {
		
		ResetNotificationsJob job = new ResetNotificationsJob();
		
		try {
			super.spooler_process();
			
			ResetNotificationsJobOptions options = job.getOptions();
			options.setCurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
		    job.setJSJobUtilites(this);
		    job.setJSCommands(this);
		    	    
		    if(SOSString.isEmpty(options.hibernate_configuration_file.getValue())){
		    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
		    	options.hibernate_configuration_file.setValue(f.getAbsolutePath());
		    }
		    
	        job.init();
			job.execute();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
        return signalSuccess();
	}
}

