package com.sos.scheduler.notification.jobs.reset;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.reset.ResetNotificationsModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResetNotificationsJob extends JSJobUtilitiesClass<ResetNotificationsJobOptions> {
	private final String	conClassName	= ResetNotificationsJob.class.getSimpleName();
	private static Logger	logger			= Logger.getLogger(ResetNotificationsJob.class);
	private SOSHibernateConnection connection; 
	
	/**
	 * 
	 */
	public ResetNotificationsJob() {
		super(new ResetNotificationsJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
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
	public ResetNotificationsJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			ResetNotificationsModel model = new ResetNotificationsModel(connection,Options());
			model.process();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName,e.toString()));
            throw e;			
		}
		
		return this;
	}
	
	/**
	 * 
	 */
	public ResetNotificationsJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new ResetNotificationsJobOptions();
		}
		return objOptions;
	}

}  // class ResetNotificationsJob