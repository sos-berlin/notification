package com.sos.scheduler.notification.jobs.notifier;

import org.apache.log4j.Logger;

import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.notifier.SystemNotifierModel;

public class SystemNotifierJob extends JSJobUtilitiesClass<SystemNotifierJobOptions> {
	private final String className = SystemNotifierJob.class.getSimpleName();
	private static Logger logger = Logger.getLogger(SystemNotifierJob.class);
	private SOSHibernateConnection connection; 
	private Spooler spooler;
    
	public SystemNotifierJob() {
		super(new SystemNotifierJobOptions());
	}

	public void init(Spooler sp) throws Exception {
		spooler = sp;
		
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
	
	public SystemNotifierJob execute() throws Exception {
		final String methodName = className + "::execute";

		logger.debug(methodName);

		try { 
			getOptions().CheckMandatory();
			logger.debug(getOptions().toString());
			
			SystemNotifierModel model = new SystemNotifierModel(connection,getOptions(),spooler);
			model.process();
		}
		catch (Exception e) {
			logger.error(String.format("%s: %s",methodName,e.getMessage()));
            throw e;			
		}
		return this;
	}

	public SystemNotifierJobOptions getOptions() {

		if (objOptions == null) {
			objOptions = new SystemNotifierJobOptions();
		}
		return objOptions;
	}
}