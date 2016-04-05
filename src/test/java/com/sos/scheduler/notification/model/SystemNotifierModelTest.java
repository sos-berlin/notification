package com.sos.scheduler.notification.model;


import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.model.notifier.SystemNotifierModel;

public class SystemNotifierModelTest {
	private static Logger		logger			= LoggerFactory.getLogger(SystemNotifierModelTest.class);
	
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
	    //BasicConfigurator.resetConfiguration();
	    
	    //System.setProperty("log4j","D:/Arbeit/projects/git.release.1.9/all/notification/target/test-classes/log4j.properties");
	    /**
	    Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        Logger.getLogger("org.hibernate.jdbc").setLevel(Level.OFF);
        Logger.getLogger("org.hibernate.loader").setLevel(Level.OFF);
        */
        /**
        List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        for ( Logger logger : loggers ) {
            logger.setLevel(Level.OFF);
        }*/
        
	    SystemNotifierJobOptions opt = new SystemNotifierJobOptions();
		opt.hibernate_configuration_file.Value(Config.HIBERNATE_CONFIGURATION_FILE);
		opt.schema_configuration_file.Value(Config.SCHEMA_CONFIGURATION_FILE);
		opt.system_configuration_file.Value(Config.SYSTEM_CONFIGURATION_FILE);
		
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
