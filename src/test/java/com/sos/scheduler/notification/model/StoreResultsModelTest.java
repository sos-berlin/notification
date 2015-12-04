package com.sos.scheduler.notification.model;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.jobs.result.StoreResultsJobOptions;
import com.sos.scheduler.notification.model.result.StoreResultsModel;

public class StoreResultsModelTest {
	private static Logger		logger			= Logger.getLogger(StoreResultsModelTest.class);
	
	private SOSHibernateConnection connection;
	private StoreResultsJobOptions options;
	
	public StoreResultsModelTest(StoreResultsJobOptions opt){
		options = opt;
	}
	
	public void init() throws Exception {
		connection = new SOSHibernateConnection(options.scheduler_notification_hibernate_configuration_file.Value());
		connection.setAutoCommit(options.scheduler_notification_connection_autocommit.value());
		connection.setTransactionIsolation(options.scheduler_notification_connection_transaction_isolation.value());
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
		String config = "D:/scheduler/config";
		
		StoreResultsJobOptions opt = new StoreResultsJobOptions();
		opt.scheduler_notification_hibernate_configuration_file.Value(config+"/hibernate.cfg.xml");
		
		opt.mon_results_scheduler_id.Value("my_scheduler_id");
		opt.mon_results_task_id.value(17600149);
		opt.mon_results_order_step_state.Value("moveCSV");
		opt.mon_results_job_chain_name.Value("orders_setback/Move");
		opt.mon_results_order_id.Value("Get");
		opt.mon_results_standalone.value(false);
		
		StoreResultsModelTest t = new StoreResultsModelTest(opt);

		try {
			logger.info("START --");
			t.init();

			StoreResultsModel model = new StoreResultsModel(t.connection,t.options);
			model.process();

			/**
			model.getDbLayer().getNotFinishedOrderStepHistory(opt.mon_results_scheduler_id.Value(), 
					new Long(opt.mon_results_task_id.value()), 
					opt.mon_results_order_step_state.Value(), 
					opt.mon_results_job_chain_name.Value(), 
					opt.mon_results_order_id.Value());*/
			
			logger.info("END --");
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			t.exit();
		}

	}

	
}
