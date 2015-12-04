package com.sos.scheduler.notification.model;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.model.history.CheckHistoryModel;
import com.sos.scheduler.notification.model.notifier.SystemNotifierModel;

public class SystemNotifierModelTest {
	private static Logger		logger			= Logger.getLogger(SystemNotifierModelTest.class);
	
	private SOSHibernateConnection connection;
	private SystemNotifierJobOptions options;
	
	public SystemNotifierModelTest(SystemNotifierJobOptions opt){
		options = opt;
	}
	
	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.hibernate_configuration_file.Value());
		connection.setAutoCommit(options.connection_autocommit.value());
		connection.setTransactionIsolation(options.connection_transaction_isolation.value());
		connection.setIgnoreAutoCommitTransactions(true);
		connection.setUseOpenStatelessSession(true);
		connection.addClassMapping(DBLayer.getNotificationClassMapping());
		connection.connect();
	}

	public void exit() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public static void main(String[] args) throws Exception {
		String config = "D:/scheduler/config";
		
		SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
		opt.hibernate_configuration_file.Value(config+"/hibernate.cfg.xml");
		opt.schema_configuration_file.Value(config+"/notification/SystemMonitorNotification_v1.0.xsd");
		opt.system_configuration_file.Value(config+"/notification/SystemMonitorNotification_MonitorSystem.xml");
		
		SystemNotifierModelTest t = new SystemNotifierModelTest(opt);

		try {
			logger.info("START --");
			t.init();

			SystemNotifierModel model = new SystemNotifierModel(t.connection,t.options,null);
			model.process();
			
			logger.info("END --");
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

	
}
