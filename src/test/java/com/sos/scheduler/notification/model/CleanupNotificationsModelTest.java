package com.sos.scheduler.notification.model;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.scheduler.notification.model.cleanup.CleanupNotificationsModel;

public class CleanupNotificationsModelTest {
	private SOSHibernateConnection connection;
	private CleanupNotificationsJobOptions options;
	
	/**
	 * 
	 * @param opt
	 */
	public CleanupNotificationsModelTest(CleanupNotificationsJobOptions opt){
		options = opt;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
		connection.setAutoCommit(options.connection_autocommit.value());
		connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		connection.setIgnoreAutoCommitTransactions(true);
		connection.addClassMapping(DBLayer.getNotificationClassMapping());
		connection.connect();
	}

	/**
	 * 
	 */
	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String config = "D:/Arbeit/scheduler/jobscheduler_data/re-dell_4646_snap_1_8/config";
		
		CleanupNotificationsJobOptions opt = new CleanupNotificationsJobOptions();
		opt.hibernate_configuration_file.Value(config+"/hibernate_reporting.cfg.xml");
		
		CleanupNotificationsModelTest t = new CleanupNotificationsModelTest(opt);

		try {
			t.init();

			CleanupNotificationsModel model = new CleanupNotificationsModel(t.connection,t.options);
			model.process();
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

	
}
