package com.sos.scheduler.notification.model.history;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.scheduler.notification.db.DBItemNotificationSchedulerHistoryOrderStep;
import com.sos.scheduler.notification.db.DBItemNotificationSchedulerVariables;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.CounterCheckHistory;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.helper.ElementTimerJobChain;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.history.ICheckHistoryPlugin;

public class CheckHistoryModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private CheckHistoryJobOptions options;
    private LinkedHashMap<String, ElementTimer> timers = null;
    private LinkedHashMap<String, ArrayList<String>> jobChains = null;
    private LinkedHashMap<String, ArrayList<String>> jobs = null;
    private boolean checkInsertNotifications = true;
    private List<ICheckHistoryPlugin> plugins = null;
    private CounterCheckHistory counter;
    private Optional<Integer> largeResultFetchSize = Optional.empty();

    public CheckHistoryModel(SOSHibernateConnection conn, CheckHistoryJobOptions opt) throws Exception {
        super(conn);
        options = opt;
        try {
            int fetchSize = options.large_result_fetch_size.value();
            if (fetchSize != -1) {
                largeResultFetchSize = Optional.of(fetchSize);
            }
        } catch (Exception ex) {
            // no exception handling
        }
        initConfig();
        registerPlugins();
        pluginsOnInit(timers, options, getDbLayer());
    }

    private void initCounters() {
        counter = new CounterCheckHistory();
    }

    public void initConfig() throws Exception {
        String method = "initConfig";
        plugins = new ArrayList<ICheckHistoryPlugin>();
        timers = new LinkedHashMap<String, ElementTimer>();
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();
        File dir = null;
        File schemaFile = new File(options.schema_configuration_file.getValue());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("[%s][schema file not found]%s", method, schemaFile.getAbsolutePath()));
        }
        if (SOSString.isEmpty(this.options.configuration_dir.getValue())) {
            dir = new File(this.options.configuration_dir.getValue());
        } else {
            dir = schemaFile.getParentFile().getAbsoluteFile();
        }
        if (!dir.exists()) {
            throw new Exception(String.format("[%s][configuration dir not found]%s", method, dir.getAbsolutePath()));
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]", method, schemaFile, dir.getAbsolutePath()));
        }
        readConfigFiles(dir);
    }

    private void readConfigFiles(File dir) throws Exception {
        String method = "readConfigFiles";
        jobChains = new LinkedHashMap<String, ArrayList<String>>();
        jobs = new LinkedHashMap<String, ArrayList<String>>();
        timers = new LinkedHashMap<String, ElementTimer>();
        checkInsertNotifications = true;
        File[] files = getAllConfigurationFiles(dir);
        if (files.length == 0) {
            throw new Exception(String.format("[%s][configuration files not found]%s", method, dir.getAbsolutePath()));
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            LOGGER.info(String.format("[%s][%s]%s", method, (i + 1), f.getAbsolutePath()));
            SOSXMLXPath xpath = new SOSXMLXPath(f.getAbsolutePath());
            setConfigAllJobChains(xpath);
            setConfigAllJobs(xpath);
            setConfigTimers(xpath);
        }
        if (jobChains.isEmpty() && jobs.isEmpty() && timers.isEmpty()) {
            throw new Exception(String.format("[%s]jobChains or jobs or timers definitions not founded", method));
        }
    }

    private void setConfigTimers(SOSXMLXPath xpath) throws Exception {
        NodeList nlTimers = NotificationXmlHelper.selectTimerDefinitions(xpath);
        for (int j = 0; j < nlTimers.getLength(); j++) {
            Node n = nlTimers.item(j);
            String name = NotificationXmlHelper.getTimerName((Element) n);
            if (name != null && !timers.containsKey(name)) {
                timers.put(name, new ElementTimer(n));
            }
        }
    }

    private void setConfigAllJobs(SOSXMLXPath xpath) throws Exception {
        if (!checkInsertNotifications) {
            return;
        }
        NodeList notificationJobs = NotificationXmlHelper.selectNotificationJobDefinitions(xpath);
        setConfigJobs(xpath, notificationJobs);
    }

    private void setConfigJobs(SOSXMLXPath xpath, NodeList nlJobs) throws Exception {
        for (int j = 0; j < nlJobs.getLength(); j++) {
            Element job = (Element) nlJobs.item(j);
            String schedulerId = NotificationXmlHelper.getSchedulerId(job);
            String name = NotificationXmlHelper.getJobName(job);
            schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
            name = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
            ArrayList<String> al = new ArrayList<String>();
            if (schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                jobs = new LinkedHashMap<String, ArrayList<String>>();
                al.add(name);
                jobs.put(schedulerId, al);
                checkInsertNotifications = false;
                return;
            }
            if (jobs.containsKey(schedulerId)) {
                al = jobs.get(schedulerId);
            }
            if (!al.contains(name)) {
                al.add(name);
            }
            jobs.put(schedulerId, al);
        }
    }

    private void setConfigAllJobChains(SOSXMLXPath xpath) throws Exception {
        NodeList notificationJobChains = NotificationXmlHelper.selectNotificationJobChainDefinitions(xpath);
        setConfigJobChains(xpath, notificationJobChains);
        if (checkInsertNotifications) {
            NodeList timerJobChains = NotificationXmlHelper.selectTimerJobChainDefinitions(xpath);
            setConfigJobChains(xpath, timerJobChains);
        }
    }

    private void setConfigJobChains(SOSXMLXPath xpath, NodeList nlJobChains) throws Exception {
        for (int j = 0; j < nlJobChains.getLength(); j++) {
            Element jobChain = (Element) nlJobChains.item(j);
            String schedulerId = NotificationXmlHelper.getSchedulerId(jobChain);
            String name = NotificationXmlHelper.getJobChainName(jobChain);
            schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
            name = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
            ArrayList<String> al = new ArrayList<String>();
            if (schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                jobChains = new LinkedHashMap<String, ArrayList<String>>();
                al.add(name);
                jobChains.put(schedulerId, al);
                checkInsertNotifications = false;
                return;
            }
            if (jobChains.containsKey(schedulerId)) {
                al = jobChains.get(schedulerId);
            }
            if (!al.contains(name)) {
                al.add(name);
            }
            jobChains.put(schedulerId, al);
        }
    }

    private boolean checkDoInsert(CounterCheckHistory counter, DBItemNotificationSchedulerHistoryOrderStep step) throws Exception {
        // Indent for the output
        String method = "  [" + counter.getTotal() + "][checkDoInsert]";

        if (!checkInsertNotifications) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]checkInsertNotifications=false", method));
            }
            return true;
        }
        if ((jobs == null || jobs.isEmpty()) && (jobChains == null || jobChains.isEmpty())) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]missing jobs and job chain definitions", method));
            }
            return false;
        }
        Set<Map.Entry<String, ArrayList<String>>> set = jobChains.entrySet();
        for (Map.Entry<String, ArrayList<String>> jc : set) {
            String schedulerId = jc.getKey();
            ArrayList<String> jobChainsFromSet = jc.getValue();
            boolean checkJobChains = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                try {
                    if (!step.getOrderSchedulerId().matches(schedulerId)) {
                        checkJobChains = false;

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[jobChains][checkJobChains=%s][schedulerId not match][%s][%s]", method, checkJobChains, step
                                    .getOrderSchedulerId(), schedulerId));
                        }
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("%s[jobChains][check with configured scheduler_id=%s]%s", method, schedulerId, ex));
                }
            }
            if (checkJobChains) {
                for (int i = 0; i < jobChainsFromSet.size(); i++) {
                    String jobChain = jobChainsFromSet.get(i);
                    if (jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[jobChains][match][%s][%s][%s][%s]", method, step.getOrderSchedulerId(), schedulerId, step
                                    .getOrderJobChain(), jobChain));
                        }
                        return true;
                    }
                    try {
                        if (step.getOrderJobChain().matches(jobChain)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobChains][match][%s][%s][%s][%s]", method, step.getOrderSchedulerId(), schedulerId,
                                        step.getOrderJobChain(), jobChain));
                            }
                            return true;
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobChains][not match][%s][%s]", method, step.getOrderJobChain(), jobChain));
                            }
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s[jobChains][check with configured scheduler_id=%s, name=%s]%s", method, schedulerId,
                                jobChain, ex));
                    }
                }
            }
        }

        set = jobs.entrySet();
        for (Map.Entry<String, ArrayList<String>> jc : set) {
            String schedulerId = jc.getKey();
            ArrayList<String> jobsFromSet = jc.getValue();
            boolean checkJobs = true;
            if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                try {
                    if (!step.getOrderSchedulerId().matches(schedulerId)) {
                        checkJobs = false;
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[jobs][checkJobs=%s][schedulerId not match][%s][%s]", method, checkJobs, step
                                    .getOrderSchedulerId(), schedulerId));
                        }
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("%s[jobs][check with configured scheduler_id=%s]%s", method, schedulerId, ex));
                }
            }
            if (checkJobs) {
                for (int i = 0; i < jobsFromSet.size(); i++) {
                    String job = jobsFromSet.get(i);
                    if (job.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[jobs][match][%s][%s][%s][%s]", method, step.getOrderSchedulerId(), schedulerId, step
                                    .getTaskJobName(), job));
                        }
                        return true;
                    }
                    try {
                        if (step.getTaskJobName().matches(job)) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobs][match][%s][%s][%s][%s]", method, step.getOrderSchedulerId(), schedulerId, step
                                        .getTaskJobName(), job));
                            }
                            return true;
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[jobs][not match][%s][%s]", method, step.getTaskJobName(), job));
                            }
                        }
                    } catch (Exception ex) {
                        throw new Exception(String.format("%s[jobs][check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, job,
                                ex));
                    }
                }
            }
        }

        return false;
    }

    private Date getLastNotificationDate(DBItemNotificationSchedulerVariables dbItem, Date dateTo) throws Exception {
        Date dateFrom = getDbLayer().getLastNotificationDate(dbItem);
        int maxAge = NotificationModel.resolveAge2Minutes(options.max_history_age.getValue());
        if (dateFrom != null) {
            Long startTimeMinutes = dateFrom.getTime() / 1000 / 60;
            Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
            Long diffMinutes = endTimeMinutes - startTimeMinutes;
            if (diffMinutes > maxAge) {
                dateFrom = null;
            }
        }
        if (dateFrom == null) {
            dateFrom = DBLayer.getDateTimeMinusMinutes(dateTo, maxAge);
        }
        return dateFrom;
    }

    private void updateExistingNotifications() throws Exception {
        String method = "updateExistingNotifications";
        getDbLayer().getConnection().beginTransaction();
        Date maxStartTime = null;
        int uncompletedAge = NotificationModel.resolveAge2Minutes(options.max_uncompleted_age.getValue());
        if (uncompletedAge > 0) {
            maxStartTime = DBLayer.getCurrentDateTimeMinusMinutes(uncompletedAge);
        }

        // ignore options.allow_db_dependent_queries.value(). always independent
        int result = getDbLayer().updateUncompletedNotifications(largeResultFetchSize, maxStartTime);
        LOGGER.info(String.format("[%s][updateUncompletedNotifications][%s][%s]updated=%s", method, options.max_uncompleted_age.getValue(), DBLayer
                .getDateAsString(maxStartTime), result));

        //result = getDbLayer().setOrderNotificationsRecovered();
        //LOGGER.info(String.format("[%s][setOrderNotificationsRecovered]updated=%s", method, result));

        getDbLayer().getConnection().commit();
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        Criteria criteria = null;
        SOSHibernateResultSetProcessor rspHistory = new SOSHibernateResultSetProcessor(getDbLayer().getConnection());
        SOSHibernateBatchProcessor bpNotifications = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
        SOSHibernateBatchProcessor bpTimers = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
        initCounters();
        Date dateTo = DBLayer.getCurrentDateTime();
        Date dateFrom = null;
        DBItemNotificationSchedulerVariables schedulerVariable = null;
        boolean success = false;
        try {
            DateTime start = new DateTime();
            schedulerVariable = getDbLayer().getSchedulerVariable();
            dateFrom = getLastNotificationDate(schedulerVariable, dateTo);
            updateExistingNotifications();
            bpNotifications.createInsertBatch(DBItemSchedulerMonNotifications.class);
            bpTimers.createInsertBatch(DBItemSchedulerMonChecks.class);
            getDbLayer().getConnection().beginTransaction();
            criteria = getDbLayer().getSchedulerHistorySteps(largeResultFetchSize, dateFrom, dateTo);
            ResultSet rsHistory = rspHistory.createResultSet(DBItemNotificationSchedulerHistoryOrderStep.class, criteria, ScrollMode.FORWARD_ONLY,
                    largeResultFetchSize);
            while (rsHistory.next()) {
                counter.addTotal();
                DBItemNotificationSchedulerHistoryOrderStep step = (DBItemNotificationSchedulerHistoryOrderStep) rspHistory.get();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s]%s", method, counter.getTotal(), NotificationModel.toString(step)));
                }

                if (step.getOrderHistoryId() == null && step.getOrderId() == null && step.getOrderStartTime() == null) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip]order object is null", method, counter.getTotal()));
                    }
                    continue;
                }
                if (step.getTaskId() == null && step.getTaskJobName() == null && step.getTaskCause() == null) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip]task object is null", method, counter.getTotal()));
                    }
                    continue;
                }
                if (!this.checkDoInsert(counter, step)) {
                    counter.addSkip();
                    // if (isDebugEnabled) {
                    // LOGGER.debug(String.format("[%s][%s][skip]checkInsertNotification=false", method, counter.getTotal()));
                    // }
                    continue;
                }

                if (counter.getTotal() % options.batch_size.value() == 0) {
                    counter.addBatchInsert(SOSHibernateBatchProcessor.getExecutedBatchSize(bpNotifications.executeBatch()));
                    counter.addBatchInsertTimer(SOSHibernateBatchProcessor.getExecutedBatchSize(bpTimers.executeBatch()));
                }
                DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification(step.getOrderSchedulerId(), false, step.getTaskId(), step
                        .getStepStep(), step.getOrderHistoryId());
                boolean hasStepError = step.getStepError();
                if (dbItem == null) {
                    counter.addInsert();
                    dbItem = getDbLayer().createNotification(step.getOrderSchedulerId(), false, step.getTaskId(), step.getStepStep(), step
                            .getOrderHistoryId(), step.getOrderJobChain(), step.getOrderJobChain(), step.getOrderId(), step.getOrderTitle(), step
                                    .getOrderStartTime(), step.getOrderEndTime(), step.getStepState(), step.getStepStartTime(), step.getStepEndTime(),
                            step.getTaskJobName(), step.getTaskJobName(), step.getTaskStartTime(), step.getTaskEndTime(), false, new Long(step
                                    .getTaskExitCode() == null ? 0 : step.getTaskExitCode()), hasStepError, step.getStepErrorCode(), step
                                            .getStepErrorText());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][insert][addBatch]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));
                    }
                    bpNotifications.addBatch(dbItem);
                } else {
                    counter.addUpdate();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][update][before]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));
                    }
                    // kann inserted sein durch StoreResult Job
                    dbItem.setJobChainName(step.getOrderJobChain());
                    dbItem.setJobChainTitle(step.getOrderJobChain());
                    dbItem.setOrderId(step.getOrderId());
                    dbItem.setOrderTitle(step.getOrderTitle());
                    dbItem.setOrderStartTime(step.getOrderStartTime());
                    dbItem.setOrderEndTime(step.getOrderEndTime());
                    dbItem.setOrderStepState(step.getStepState());
                    dbItem.setOrderStepStartTime(step.getStepStartTime());
                    dbItem.setOrderStepEndTime(step.getStepEndTime());
                    dbItem.setJobName(step.getTaskJobName());
                    dbItem.setJobTitle(step.getTaskJobName());
                    dbItem.setTaskStartTime(step.getTaskStartTime());
                    dbItem.setTaskEndTime(step.getTaskEndTime());
                    dbItem.setReturnCode(new Long(step.getTaskExitCode() == null ? 0 : step.getTaskExitCode()));
                    // hatte error und wird auf nicht error gesetzt
                    dbItem.setRecovered(dbItem.getError() && !hasStepError);
                    dbItem.setError(hasStepError);
                    dbItem.setErrorCode(step.getStepErrorCode());
                    dbItem.setErrorText(step.getStepErrorText());
                    dbItem.setModified(DBLayer.getCurrentDateTime());
                    getDbLayer().getConnection().update(dbItem);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][update][after]%s", method, counter.getTotal(), NotificationModel.toString(dbItem)));
                    }
                }
                bpTimers = insertTimer(counter, bpTimers, dbItem);
            }
            counter.addBatchInsert(SOSHibernateBatchProcessor.getExecutedBatchSize(bpNotifications.executeBatch()));
            counter.addBatchInsertTimer(SOSHibernateBatchProcessor.getExecutedBatchSize(bpTimers.executeBatch()));
            getDbLayer().getConnection().commit();
            success = true;
            LOGGER.info(String.format("[%s]duration=%s", method, NotificationModel.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            Throwable e = SOSHibernateConnection.getException(ex);
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception exx) {
                LOGGER.warn(String.format("[%s]%s", method, exx.toString()), exx);
            }
            throw new Exception(String.format("[%s]%s", method, e.toString()), e);
        } finally {
            try {
                bpNotifications.close();
            } catch (Exception ex) {
                // no exception handling
            }
            try {
                bpTimers.close();
            } catch (Exception ex) {
                // no exception handling
            }
            try {
                if (rspHistory != null) {
                    rspHistory.close();
                }
            } catch (Exception ex) {
                // no exception handling
            }
        }
        LOGGER.info(String.format("[%s][total=%s][inserted=%s][batch=%s][updated=%s][skipped=%s][inserted timers=%s][batch=%s]", method, counter
                .getTotal(), counter.getInsert(), counter.getBatchInsert(), counter.getUpdate(), counter.getSkip(), counter.getInsertTimer(), counter
                        .getBatchInsertTimer()));
        if (success) {
            pluginsOnProcess(timers, options, getDbLayer(), dateFrom, dateTo);
            getDbLayer().getConnection().beginTransaction();
            getDbLayer().setLastNotificationDate(schedulerVariable, dateTo);
            getDbLayer().getConnection().commit();
        }

    }

    private SOSHibernateBatchProcessor insertTimer(CounterCheckHistory counter, SOSHibernateBatchProcessor bp, DBItemSchedulerMonNotifications dbItem)
            throws Exception {
        // output indent
        String method = "  [" + counter.getTotal() + "][insertTimer]";
        if (timers == null || timers.isEmpty()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip]timers is null or empty", method));
            }
            return bp;
        }
        // only first notification (step 1)
        if (dbItem.getStep().equals(new Long(1))) {
            Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
            for (Map.Entry<String, ElementTimer> me : set) {
                String timerName = me.getKey();
                ElementTimer timer = me.getValue();
                ArrayList<ElementTimerJobChain> timerJobChains = timer.getJobChains();
                if (timerJobChains.isEmpty()) {
                    LOGGER.warn(String.format("%s[timer=%s]timer JobChains not found", method, timerName));
                    continue;
                }
                for (int i = 0; i < timerJobChains.size(); i++) {
                    ElementTimerJobChain jobChain = timerJobChains.get(i);
                    String schedulerId = jobChain.getSchedulerId();
                    String name = jobChain.getName();
                    boolean insert = true;
                    if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getSchedulerId().matches(schedulerId)) {
                        insert = false;
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[timer=%s][skip][insert=%s][scheduler_id not match][%s][%s]", method, timerName, insert,
                                    dbItem.getSchedulerId(), schedulerId));
                        }
                    }
                    if (insert && !name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && !dbItem.getJobChainName().matches(name)) {
                        insert = false;
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[timer=%s][skip][insert=%s][not match][%s][%s]", method, timerName, insert, dbItem
                                    .getJobChainName(), name));
                        }
                    }
                    if (insert) {
                        counter.addInsertTimer();
                        DBItemSchedulerMonChecks item = getDbLayer().createCheck(timerName, dbItem, jobChain.getStepFrom(), jobChain.getStepTo(),
                                dbItem.getOrderStartTime(), dbItem.getOrderEndTime());

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[timer=%s][insert][addBatch]%s", method, timerName, NotificationModel.toString(item)));
                        }

                        bp.addBatch(item);
                    }
                }
            }
        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip][step is not equals 1]%s", method, NotificationModel.toString(dbItem)));
            }
        }
        return bp;
    }

    private void pluginsOnInit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onInit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnInit]%s", ex.getMessage()), ex);
            }
        }
    }

    @SuppressWarnings("unused")
    private void pluginsOnExit(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onExit(timers, options, dbLayer);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnExit]%s", ex.getMessage()), ex);
            }
        }
    }

    private void pluginsOnProcess(LinkedHashMap<String, ElementTimer> timers, CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer,
            Date dateFrom, Date dateTo) {
        for (ICheckHistoryPlugin plugin : plugins) {
            try {
                plugin.onProcess(timers, options, dbLayer, dateFrom, dateTo);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[pluginsOnProcess]%s", ex.getMessage()), ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerPlugins() throws Exception {
        String method = "registerPlugins";
        plugins = new ArrayList<ICheckHistoryPlugin>();
        if (!SOSString.isEmpty(this.options.plugins.getValue())) {
            String[] arr = this.options.plugins.getValue().trim().split(";");
            for (int i = 0; i < arr.length; i++) {
                try {
                    Class<ICheckHistoryPlugin> c = (Class<ICheckHistoryPlugin>) Class.forName(arr[i].trim());
                    addPlugin(c.newInstance());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][registered]%s", method, arr[i]));
                    }
                } catch (Exception ex) {
                    LOGGER.error(String.format("[%s][cannot be registered][%s]%s", method, arr[i], ex.getMessage()),ex);
                }
            }
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]registered=%s", method, plugins.size()));
        }
    }

    public void addPlugin(ICheckHistoryPlugin handler) {
        plugins.add(handler);
    }

    public void resetPlugins() {
        plugins = new ArrayList<ICheckHistoryPlugin>();
    }

}
