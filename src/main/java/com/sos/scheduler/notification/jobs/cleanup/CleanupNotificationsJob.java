package com.sos.scheduler.notification.jobs.cleanup;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.cleanup.CleanupNotificationsModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class CleanupNotificationsJob extends JSJobUtilitiesClass<CleanupNotificationsJobOptions> {
	private final String		conClassName = CleanupNotificationsJob.class.getSimpleName();  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(CleanupNotificationsJob.class);
	private SOSHibernateConnection connection; 
	
	/**
	 * 
	 */
	public CleanupNotificationsJob() {
		super(new CleanupNotificationsJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
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
			throw new Exception(String.format("reporting connection: %s",
					ex.toString()));
		}
	}

	/**
	 * 
	 */
	public void exit(){
		final String conMethodName = conClassName + "::exit"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		try {
			connection.disconnect();
		} catch (Exception e) {
			logger.warn(String.format("%s:%s", conMethodName, e.toString()));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public CleanupNotificationsJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			getOptions().CheckMandatory();
			logger.debug(getOptions().toString());
			
			CleanupNotificationsModel model = new CleanupNotificationsModel(connection,getOptions());
			model.process();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName,e.getMessage()));
            throw e;			
		}
		
		return this;
	}
	
	/**
	 * 
	 */
	public CleanupNotificationsJobOptions getOptions() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new CleanupNotificationsJobOptions();
		}
		return objOptions;
	}

}  // class CleanupNotificationsJob