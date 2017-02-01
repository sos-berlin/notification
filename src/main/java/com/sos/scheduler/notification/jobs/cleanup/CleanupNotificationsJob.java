package com.sos.scheduler.notification.jobs.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.cleanup.CleanupNotificationsModel;

public class CleanupNotificationsJob extends JSJobUtilitiesClass<CleanupNotificationsJobOptions> {
	private static Logger LOGGER = LoggerFactory.getLogger(CleanupNotificationsJob.class);
	private final String className = CleanupNotificationsJob.class.getSimpleName();
	private SOSHibernateFactory factory;
	private SOSHibernateStatelessConnection connection;

	public CleanupNotificationsJob() {
		super(new CleanupNotificationsJobOptions());
	}

	public void init() throws Exception {
		try {
			factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file.getValue());
			factory.setAutoCommit(getOptions().connection_autocommit.value());
			factory.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
			factory.addClassMapping(DBLayer.getNotificationClassMapping());
			factory.build();
		} catch (Exception ex) {
			throw new Exception(String.format("init connection: %s", ex.toString()));
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

	public CleanupNotificationsJob execute() throws Exception {
		final String methodName = className + "::execute";

		LOGGER.debug(methodName);

		try {
			getOptions().checkMandatory();
			LOGGER.debug(getOptions().toString());

			CleanupNotificationsModel model = new CleanupNotificationsModel(connection, getOptions());
			model.process();
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", methodName, e.toString()));
			throw e;
		}

		return this;
	}

	public CleanupNotificationsJobOptions getOptions() {
		if (objOptions == null) {
			objOptions = new CleanupNotificationsJobOptions();
		}
		return objOptions;
	}

}