package com.sos.scheduler.notification.jobs.reset;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.reset.ResetNotificationsModel;

public class ResetNotificationsJob extends JSJobUtilitiesClass<ResetNotificationsJobOptions> {
	private final String	className	= ResetNotificationsJob.class.getSimpleName();
	private static Logger	logger			= Logger.getLogger(ResetNotificationsJob.class);
	private SOSHibernateConnection connection; 
	
	public ResetNotificationsJob() {
		super(new ResetNotificationsJobOptions());
	}

	public void init() throws Exception {
		final String methodName = className + "::init";
		
		logger.debug(methodName);
		
		try{
			connection = new SOSHibernateConnection(Options().hibernate_configuration_file.Value());
			connection.setAutoCommit(Options().connection_autocommit.value());
			connection.setIgnoreAutoCommitTransactions(true);
			connection.setTransactionIsolation(Options().connection_transaction_isolation.value());
			connection.setUseOpenStatelessSession(true);
			connection.addClassMapping(DBLayer.getNotificationClassMapping());
			connection.connect();
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
	
	public ResetNotificationsJob execute() throws Exception {
		final String methodName = className + "::execute";

		logger.debug(methodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			ResetNotificationsModel model = new ResetNotificationsModel(connection,Options());
			model.process();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",methodName,e.toString()));
            throw e;			
		}
		
		return this;
	}
	
	public ResetNotificationsJobOptions Options() {

		if (objOptions == null) {
			objOptions = new ResetNotificationsJobOptions();
		}
		return objOptions;
	}

}