package com.sos.scheduler.notification.jobs.history;

import java.io.File;

import sos.scheduler.job.JobSchedulerJobAdapter; 
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class CheckHistoryJobJSAdapterClass extends JobSchedulerJobAdapter  {

	@Override
	public boolean spooler_process() throws Exception {
		
		CheckHistoryJob job = new CheckHistoryJob();
		
		try {
			super.spooler_process();
			
			CheckHistoryJobOptions options = job.getOptions();
			options.CurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
		    job.setJSJobUtilites(this);
		    job.setJSCommands(this);
		    
		    if(SOSString.isEmpty(options.hibernate_configuration_file.Value())){
		    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
		    	options.hibernate_configuration_file.Value(f.getAbsolutePath());
		    }
		    
	        job.init();
			job.execute();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
		finally{
			job.exit();
		}
        return signalSuccess();
	}

}

