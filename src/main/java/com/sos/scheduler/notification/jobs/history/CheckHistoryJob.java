package com.sos.scheduler.notification.jobs.history;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.history.CheckHistoryModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class CheckHistoryJob extends JSJobUtilitiesClass<CheckHistoryJobOptions> {
	private final String	conClassName	= CheckHistoryJob.class.getSimpleName();
	private static Logger	logger			= Logger.getLogger(CheckHistoryJob.class);
	private SOSHibernateConnection connection; 
	
	/**
	 * 
	 * \brief CheckHistoryJob
	 *
	 * \details
	 *
	 */
	public CheckHistoryJob() {
		super(new CheckHistoryJobOptions());
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
			connection.addClassMapping(DBLayer.getSchedulerClassMapping());
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
	public CheckHistoryJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			getOptions().CheckMandatory();
			logger.debug(getOptions().toString());
			
			CheckHistoryModel model = new CheckHistoryModel(connection,getOptions());
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
	public CheckHistoryJobOptions getOptions() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new CheckHistoryJobOptions();
		}
		return objOptions;
	}


}  // class CheckHistoryJob