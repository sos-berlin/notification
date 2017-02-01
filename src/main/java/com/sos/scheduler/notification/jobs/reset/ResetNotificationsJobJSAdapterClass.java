package com.sos.scheduler.notification.jobs.reset;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

public class ResetNotificationsJobJSAdapterClass extends JobSchedulerJobAdapter {
	private ResetNotificationsJob job;

	@Override
	public boolean spooler_init() {
		try {
			job = new ResetNotificationsJob();
			ResetNotificationsJobOptions options = job.getOptions();
			options.setCurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
			job.setJSJobUtilites(this);
			job.setJSCommands(this);

			if (SOSString.isEmpty(options.hibernate_configuration_file.getValue())) {
				options.hibernate_configuration_file.setValue(getHibernateConfigurationScheduler().toString());
			}

			job.init();
		} catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		}
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();

			ResetNotificationsJobOptions options = job.getOptions();
			options.setCurrentNodeName(this.getCurrentNodeName());
			options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));

			job.openSession();
			job.execute();
		} catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		} finally {
			job.closeSession();
		}
		return signalSuccess();
	}

	@Override
	public void spooler_close() throws Exception {
		if (job != null) {
			job.exit();
		}
		super.spooler_close();
	}
}
