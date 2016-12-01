package com.sos.scheduler.notification.jobs.history;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.history.CheckHistoryModel;

public class CheckHistoryJob extends JSJobUtilitiesClass<CheckHistoryJobOptions> {
	private final String className = CheckHistoryJob.class.getSimpleName();
	private static Logger logger = Logger.getLogger(CheckHistoryJob.class);
	private SOSHibernateConnection connection; 
	
	public CheckHistoryJob() {
		super(new CheckHistoryJobOptions());
	}

	public void init() throws Exception {
		try{
			connection = new SOSHibernateConnection(getOptions().hibernate_configuration_file.getValue());
			connection.setAutoCommit(getOptions().connection_autocommit.value());
			connection.setIgnoreAutoCommitTransactions(true);
			connection.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
			connection.setUseOpenStatelessSession(true);
			connection.addClassMapping(DBLayer.getSchedulerClassMapping());
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
	
	public CheckHistoryJob execute() throws Exception {
		final String methodName = className + "::execute";

		logger.debug(methodName);

		try { 
			getOptions().checkMandatory();
			logger.debug(getOptions().toString());
			
			CheckHistoryModel model = new CheckHistoryModel(connection,getOptions());
			model.process();
		}
		catch (Exception e) {
			logger.error(String.format("%s: %s",methodName,e.getMessage()));
            throw e;			
		}
		
		return this;
	}
	
	public CheckHistoryJobOptions getOptions() {
		if (objOptions == null) {
		    objOptions = new CheckHistoryJobOptions();
		}
		return objOptions;
	}

}