package com.sos.scheduler.notification.jobs.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateStatelessConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.reset.ResetNotificationsModel;

public class ResetNotificationsJob extends JSJobUtilitiesClass<ResetNotificationsJobOptions> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResetNotificationsJob.class);
	private SOSHibernateFactory factory;
	private SOSHibernateStatelessConnection connection;

	public ResetNotificationsJob() {
		super(new ResetNotificationsJobOptions());
	}

	public void init() throws Exception {
		final String methodName = "ResetNotificationsJob::init";

		LOGGER.debug(methodName);

		try {
			factory = new SOSHibernateFactory(getOptions().hibernate_configuration_file.getValue());
			factory.setAutoCommit(getOptions().connection_autocommit.value());
			factory.setTransactionIsolation(getOptions().connection_transaction_isolation.value());
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

	public ResetNotificationsJob execute() throws Exception {
		final String methodName = "ResetNotificationsJob::execute";

		LOGGER.debug(methodName);

		try {
			getOptions().checkMandatory();
			LOGGER.debug(getOptions().toString());

			ResetNotificationsModel model = new ResetNotificationsModel(connection, getOptions());
			model.process();
		} catch (Exception e) {
			LOGGER.error(String.format("%s: %s", methodName, e.getMessage()), e);
			throw e;
		}

		return this;
	}

	public ResetNotificationsJobOptions getOptions() {

		if (objOptions == null) {
			objOptions = new ResetNotificationsJobOptions();
		}
		return objOptions;
	}

}