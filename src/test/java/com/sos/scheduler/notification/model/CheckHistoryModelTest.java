package com.sos.scheduler.notification.model;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.scheduler.notification.model.history.CheckHistoryModel;

public class CheckHistoryModelTest {
	private static Logger		logger			= Logger.getLogger(CheckHistoryModelTest.class);
	
	private SOSHibernateConnection connection;
	private CheckHistoryJobOptions options;
	
	public CheckHistoryModelTest(CheckHistoryJobOptions opt){
		options = opt;
	}
	
	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
		connection.setAutoCommit(options.connection_autocommit.value());
		connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		connection.setIgnoreAutoCommitTransactions(true);
		connection.setUseOpenStatelessSession(true);
		connection.addClassMapping(DBLayer.getSchedulerClassMapping());
		connection.addClassMapping(DBLayer.getNotificationClassMapping());
		connection.connect();
	}

	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static void main(String[] args) throws Exception {
		
		CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
		opt.hibernate_configuration_file.Value(Config.HIBERNATE_CONFIGURATION_FILE);
		opt.schema_configuration_file.Value(Config.SCHEMA_CONFIGURATION_FILE);
		opt.allow_db_dependent_queries.value(true);
		opt.max_history_age.Value("1d");
		opt.batch_size.value(1000000);
		opt.plugins.Value("com.sos.scheduler.notification.plugins.history.CheckHistoryTimerPlugin");
		
		CheckHistoryModelTest t = new CheckHistoryModelTest(opt);

		try {
			logger.info("START --");
			t.init();

			CheckHistoryModel model = new CheckHistoryModel(t.connection,t.options);
			model.process();
			
			logger.info("END --");
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

	
}
