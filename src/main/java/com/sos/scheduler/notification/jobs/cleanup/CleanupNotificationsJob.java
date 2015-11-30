package com.sos.scheduler.notification.jobs.cleanup;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.cleanup.CleanupNotificationsModel;

public class CleanupNotificationsJob extends JSJobUtilitiesClass<CleanupNotificationsJobOptions> {
	private final String className = CleanupNotificationsJob.class.getSimpleName();
	private static Logger logger = Logger.getLogger(CleanupNotificationsJob.class);
	private SOSHibernateConnection connection; 
	
	public CleanupNotificationsJob() {
		super(new CleanupNotificationsJobOptions());
	}

	public void init() throws Exception {
		try{
			connection = new SOSHibernateConnection(getOptions().hibernate_configuration_file.Value());
			connection.setAutoCommit(getOptions().connection_autocommit.value());
			connection.setIgnoreAutoCommitTransactions(true);
			connection.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
			connection.setUseOpenStatelessSession(true);
			connection.addClassMapping(DBLayer.getNotificationClassMapping());
			connection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("init connection: %s",
					ex.toString()));
		}
	}

	public void exit(){
		if(connection != null){
			connection.disconnect();
		}
	}
	
	public CleanupNotificationsJob execute() throws Exception {
		final String methodName = className + "::execute";

		logger.debug(methodName);

		try { 
			getOptions().CheckMandatory();
			logger.debug(Options().toString());
			
			CleanupNotificationsModel model = new CleanupNotificationsModel(connection,getOptions());
			model.process();
		}
		catch (Exception e) {
			logger.error(String.format("%s: %s",methodName,e.toString()));
            throw e;			
		}
		
		return this;
	}
	
	public CleanupNotificationsJobOptions getOptions() {
		if (objOptions == null) {
			objOptions = new CleanupNotificationsJobOptions();
		}
		return objOptions;
	}

} 