package com.sos.scheduler.notification.jobs.result;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.result.StoreResultsModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class StoreResultsJob extends JSJobUtilitiesClass<StoreResultsJobOptions> {
	private final String	conClassName	= StoreResultsJob.class.getSimpleName();
	private static Logger	logger			= Logger.getLogger(StoreResultsJob.class);
	private SOSHibernateConnection connection; 
	
	/**
	 * 
	 */
	public StoreResultsJob() {
		super(new StoreResultsJobOptions());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
		try{
			connection = new SOSHibernateConnection(Options().scheduler_notification_hibernate_configuration_file.Value());
			connection.setAutoCommit(Options().scheduler_notification_connection_autocommit.value());
			connection.setIgnoreAutoCommitTransactions(true);
			connection.setTransactionIsolation(Options().scheduler_notification_connection_transaction_isolation.value());
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
	public StoreResultsJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$
		
		logger.debug(conMethodName);
		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			StoreResultsModel model = new StoreResultsModel(connection,Options());
			model.process();
		}	
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s",conMethodName, e));
            throw e;			
		}
		finally {
			//logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName ) );
		}
		return this;
	}
	
	/**
	 * 
	 */
	public StoreResultsJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new StoreResultsJobOptions();
		}
		return objOptions;
	}

}  // class StoreResultsJob