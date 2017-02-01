package com.sos.scheduler.notification.jobs.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.result.StoreResultsModel;

public class StoreResultsJob extends JSJobUtilitiesClass<StoreResultsJobOptions> {
	private static Logger LOGGER = LoggerFactory.getLogger(StoreResultsJob.class);
	private final String className = StoreResultsJob.class.getSimpleName();
	private SOSHibernateFactory factory;
	private SOSHibernateStatelessConnection connection;

	public StoreResultsJob() {
		super(new StoreResultsJobOptions());
	}

	public void init() throws Exception {
		try {
			factory = new SOSHibernateFactory(
					getOptions().scheduler_notification_hibernate_configuration_file.getValue());
			factory.setAutoCommit(getOptions().scheduler_notification_connection_autocommit.value());
			factory.setTransactionIsolation(
					getOptions().scheduler_notification_connection_transaction_isolation.value());
			factory.addClassMapping(DBLayer.getSchedulerClassMapping());
			factory.addClassMapping(DBLayer.getNotificationClassMapping());
			factory.build();
		} catch (Exception ex) {
			throw new Exception(String.format("reporting connection: %s", ex.toString()));
		}
	}

	public void openSession() throws Exception {
		connection = new SOSHibernateStatelessConnection(factory);
		connection.connect();
	}

	public void closeSession() throws Exception {
		if (connection != null) {
			connection.disconnect();
		}
	}

	public void exit() {
		if (factory != null) {
			factory.close();
		}
	}

	public StoreResultsJob execute() throws Exception {
		final String methodName = className + "::execute";

		LOGGER.debug(methodName);
		try {
			getOptions().checkMandatory();
			LOGGER.debug(getOptions().toString());

			StoreResultsModel model = new StoreResultsModel(connection, getOptions());
			model.process();
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", methodName, e));
			throw e;
		}
		return this;
	}

	public StoreResultsJobOptions getOptions() {
		if (objOptions == null) {
			objOptions = new StoreResultsJobOptions();
		}
		return objOptions;
	}

}