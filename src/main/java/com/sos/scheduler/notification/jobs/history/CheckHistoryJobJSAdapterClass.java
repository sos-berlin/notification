package com.sos.scheduler.notification.jobs.history;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.model.history.CheckHistoryModel;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.util.SOSString;

public class CheckHistoryJobJSAdapterClass extends JobSchedulerJobAdapter {

    private boolean loggerConfigured = false;

    @Override
    protected void initializeLog4jAppenderClass() {
        logger = Logger.getRootLogger();
        if (!loggerConfigured) {
            int schedulerLogLevel = spooler_log.level();
            if (schedulerLogLevel > 1) {
                logger.setLevel(Level.ERROR);
            } else if (schedulerLogLevel == 1) {
                logger.setLevel(Level.WARN);
            } else if (schedulerLogLevel == 0) {
                logger.setLevel(Level.INFO);
            } else if (schedulerLogLevel == -9) {
                logger.setLevel(Level.TRACE);
            } else if (schedulerLogLevel < 0) {
                logger.setLevel(Level.DEBUG);
            }
            loggerConfigured = true;
        }
    }

    @Override
    public boolean spooler_process() throws Exception {
        super.spooler_process();

        CheckHistoryJob job = new CheckHistoryJob();

        CheckHistoryJobOptions options = job.getOptions();
        options.setCurrentNodeName(this.getCurrentNodeName());
        options.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
        job.setJSJobUtilites(this);
        job.setJSCommands(this);

        if (SOSString.isEmpty(options.hibernate_configuration_file.getValue())) {
            File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
            options.hibernate_configuration_file.setValue(f.getAbsolutePath());
        }

        if (options.max_execution_time.value() > 0) {
            executeJobTimeLimited(job, options);
        } else {
            executeJob(job);
        }

        return signalSuccess();
    }

    private boolean executeJob(CheckHistoryJob job) {
        try {
            job.init();
            job.execute();
        } catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
        } finally {
            job.exit();
        }
        return true;
    }

    private boolean executeJobTimeLimited(CheckHistoryJob job, CheckHistoryJobOptions options) throws Exception {
        String hibernateConfigurationFile = job.getOptions().hibernate_configuration_file.getValue();
        boolean autocommit = job.getOptions().connection_autocommit.value();
        int connectionTransactionIsolation = job.getOptions().connection_transaction_isolation.value();
        int maxExecutionTime = options.max_execution_time.value();
        int maxExecutionTimeExit = options.max_execution_time_exit.value();

        SOSHibernateConnection connection = new SOSHibernateConnection(hibernateConfigurationFile);
        try {
            final SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter(Executors.newSingleThreadExecutor());
            @SuppressWarnings("unchecked")
            final Callable<Boolean> timeLimitedCall = timeLimiter.newProxy(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        connection.setAutoCommit(autocommit);
                        connection.setIgnoreAutoCommitTransactions(true);
                        connection.setTransactionIsolation(connectionTransactionIsolation);
                        connection.setUseOpenStatelessSession(true);
                        connection.addClassMapping(DBLayer.getSchedulerClassMapping());
                        connection.addClassMapping(DBLayer.getNotificationClassMapping());
                        connection.connect();

                        CheckHistoryModel model = new CheckHistoryModel(connection, options);
                        model.process();
                    } catch (Exception e) {
                        logger.error(String.format(e.getMessage()), e);
                        throw e;
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                    return true;
                }
            }, Callable.class, maxExecutionTime, TimeUnit.SECONDS);
            return timeLimitedCall.call();
        } catch (UncheckedTimeoutException e) {
            logger.info("try to db rollback, disconnect and throw a timeout exception");
            jobExitTimeLimited(connection, maxExecutionTimeExit);
            throw new JobSchedulerException("Job execution failed due to timeout " + maxExecutionTime + "s:" + e.getMessage(), e);
        }
    }

    private void jobExitTimeLimited(SOSHibernateConnection connection, int maxExecutionTimeExit) throws Exception {
        try {
            final SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter(Executors.newSingleThreadExecutor());
            @SuppressWarnings("unchecked")
            final Callable<Boolean> timeLimitedCall = timeLimiter.newProxy(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        if (connection != null) {
                            try {
                                connection.rollback();
                            } catch (Throwable t) {
                                logger.debug(String.format("Exception on jobExitTimeLimited rollback: %s", t.getMessage()), t);
                            }
                            connection.disconnect();
                        }
                    } catch (Throwable t) {
                        logger.debug(String.format("Exception on jobExitTimeLimited: %s", t.getMessage()), t);
                    }
                    return true;
                }
            }, Callable.class, maxExecutionTimeExit, TimeUnit.SECONDS);
            timeLimitedCall.call();
        } catch (Throwable t) {
            logger.debug(String.format("Exception on jobExitTimeLimited: %s", t.getMessage()), t);
        }
    }
}
