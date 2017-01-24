package com.sos.scheduler.notification.jobs.result;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.result.StoreResultsModel;

public class StoreResultsJob extends JSJobUtilitiesClass<StoreResultsJobOptions> {
	private final String className = StoreResultsJob.class.getSimpleName();
	private static Logger logger = Logger.getLogger(StoreResultsJob.class);
	private SOSHibernateConnection connection; 
	
	public StoreResultsJob() {
		super(new StoreResultsJobOptions());
	}
	
	public void init() throws Exception {
		try{
//			connection = new SOSHibernateConnection(getOptions().scheduler_notification_hibernate_configuration_file.getValue());
//			connection.setAutoCommit(getOptions().scheduler_notification_connection_autocommit.value());
//			connection.setIgnoreAutoCommitTransactions(true);
//			connection.setTransactionIsolation(getOptions().scheduler_notification_connection_transaction_isolation.value());
//			connection.setUseOpenStatelessSession(true);
//			connection.addClassMapping(DBLayer.getSchedulerClassMapping());
//			connection.addClassMapping(DBLayer.getNotificationClassMapping());
//			connection.connect();
		}
		catch(Exception ex){
			throw new Exception(String.format("reporting connection: %s",
					ex.toString()));
		}
	}

	public void exit(){
		if(connection != null){
			connection.disconnect();
		}
	}
	
	public StoreResultsJob execute() throws Exception {
		final String methodName = className + "::execute"; 
		
		logger.debug(methodName);
		try { 
			getOptions().checkMandatory();
			logger.debug(getOptions().toString());
			
			StoreResultsModel model = new StoreResultsModel(connection,getOptions());
			model.process();
		}	
		catch (Exception e) {
			logger.error(String.format("%s: %s",methodName, e));
            throw e;			
		}
		return this;
	}
	
	public StoreResultsJobOptions getOptions() {
		if (objOptions == null) {
			objOptions = new StoreResultsJobOptions();
		}
		return objOptions;
	}

}