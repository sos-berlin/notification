package com.sos.scheduler.notification.jobs.notifier;

import org.apache.log4j.Logger;

import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.notifier.SystemNotifierModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class SystemNotifierJob extends JSJobUtilitiesClass<SystemNotifierJobOptions> {
	private final String	conClassName	= SystemNotifierJob.class.getSimpleName();
	private static Logger	logger			= Logger.getLogger(SystemNotifierJob.class);
	private SOSHibernateConnection connection; 
	private Spooler spooler;
    
	/**
	 * 
	 * \brief SystemNotifierJob
	 *
	 * \details
	 *
	 */
	public SystemNotifierJob() {
		super(new SystemNotifierJobOptions());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void init(Spooler sp) throws Exception {
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		
		logger.debug(conMethodName);
		
		try{
			spooler = sp;
			
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
	public SystemNotifierJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			SystemNotifierModel model = new SystemNotifierModel(connection,Options(),spooler);
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
	public SystemNotifierJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new SystemNotifierJobOptions();
		}
		return objOptions;
	}
}  // class SystemNotifierJob