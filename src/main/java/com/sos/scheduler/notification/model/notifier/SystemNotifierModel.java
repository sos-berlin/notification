package com.sos.scheduler.notification.model.notifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.CounterSystemNotifier;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.helper.ElementNotificationJob;
import com.sos.scheduler.notification.helper.ElementNotificationJobChain;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.ElementNotificationTimerRef;
import com.sos.scheduler.notification.helper.JobChainNotification;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;

public class SystemNotifierModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);
    private static final String THREE_PARAMS_LOGGING = "%s:[%s][%s] - %s";
    private static final String METHOD_LOGGING = "%s: --- ";
    private static final String CALL_PLUGIN_LOGGING = "%s:[%s][%s][%s]. notification %s of %s. call plugin %s";
    private static final String CREATE_NOTIFICATION_LOGGING = "%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, "
            + "notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s";
    private static final String SENT_LOGGING = "%s: sended = %s, error = %s, skipped = %s (total checked = %s)";
    private static final String UPDATE_NOTIFICATION_LOGGING = "%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, "
            + "notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s";
    private static final String LAST_STEP_IS_NULL = "lastStepForNotification is NULL";
    private Spooler spooler;
    private SystemNotifierJobOptions options;
    private String systemId;
    private File systemFile;
    private ArrayList<ElementNotificationJob> monitorJobs;
    private ArrayList<ElementNotificationJobChain> monitorJobChains;
    private ArrayList<ElementNotificationTimerRef> monitorOnErrorTimers;
    private ArrayList<ElementNotificationTimerRef> monitorOnSuccessTimers;
    private Optional<Integer> largeResultFetchSize = Optional.empty();
    private CounterSystemNotifier counter;

    public SystemNotifierModel(SOSHibernateConnection conn, SystemNotifierJobOptions opt, Spooler sp) throws Exception {
        super(conn);
        options = opt;
        spooler = sp;
        try {
            int fetchSize = options.large_result_fetch_size.value();
            if (fetchSize != -1) {
                largeResultFetchSize = Optional.of(fetchSize);
            }
        } catch (Exception ex) {
            // no exception handling
        }
    }

    private void initMonitorObjects() {
        monitorJobs = new ArrayList<ElementNotificationJob>();
        monitorJobChains = new ArrayList<ElementNotificationJobChain>();
        monitorOnErrorTimers = new ArrayList<ElementNotificationTimerRef>();
        monitorOnSuccessTimers = new ArrayList<ElementNotificationTimerRef>();
    }

    private void initSendCounters() {
        counter = new CounterSystemNotifier();
    }

    private void initConfig() throws Exception {
        String method = "initConfig";
        File schemaFile = new File(options.schema_configuration_file.getValue());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("%s: schema file not found: %s", method, schemaFile.getCanonicalPath()));
        }
        systemFile = new File(this.options.system_configuration_file.getValue());
        if (!systemFile.exists()) {
            throw new Exception(String.format("%s: system configuration file not found: %s", method, systemFile.getCanonicalPath()));
        }
        LOGGER.debug(String.format("%s: read configuration file %s", method, systemFile.getCanonicalPath()));
        SOSXMLXPath xpath = new SOSXMLXPath(systemFile.getCanonicalPath());
        initMonitorObjects();
        systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
        if (SOSString.isEmpty(systemId)) {
            throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
        }
        LOGGER.info(String.format("%s: system id = %s (%s)", method, systemId, systemFile.getCanonicalPath()));

        NodeList monitors = NotificationXmlHelper.selectNotificationMonitorDefinitions(xpath);
        setMonitorObjects(xpath, monitors);

        LOGGER.info(String.format("%s: found configured JobChains = %s, Jobs = %s", method, monitorJobChains.size(), monitorJobs.size()));
    }

    private void setMonitorObjects(SOSXMLXPath xpath, NodeList monitors) throws Exception {
        for (int i = 0; i < monitors.getLength(); i++) {
            Node n = monitors.item(i);
            ElementNotificationMonitor monitor = new ElementNotificationMonitor(n);
            NodeList objects = NotificationXmlHelper.selectNotificationMonitorNotificationObjects(xpath, n);
            for (int j = 0; j < objects.getLength(); j++) {
                Node object = objects.item(j);
                if ("Job".equalsIgnoreCase(object.getNodeName())) {
                    monitorJobs.add(new ElementNotificationJob(monitor, object));
                } else if ("JobChain".equalsIgnoreCase(object.getNodeName())) {
                    monitorJobChains.add(new ElementNotificationJobChain(monitor, object));
                } else if ("TimerRef".equalsIgnoreCase(object.getNodeName())) {
                    if (!StringUtils.isEmpty(monitor.getServiceNameOnError())) {
                        monitorOnErrorTimers.add(new ElementNotificationTimerRef(monitor, object));
                    }
                    if (!StringUtils.isEmpty(monitor.getServiceNameOnSuccess())) {
                        monitorOnSuccessTimers.add(new ElementNotificationTimerRef(monitor, object));
                    }
                }
            }
        }
    }

    private void executeNotifyTimer(String systemId, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer, boolean isNotifyOnErrorService)
            throws Exception {
        // Output indent
        String method = "  executeNotifyTimer";
        String serviceName = (isNotifyOnErrorService) ? timer.getMonitor().getServiceNameOnError() : timer.getMonitor().getServiceNameOnSuccess();
        EServiceStatus pluginStatus = (isNotifyOnErrorService) ? EServiceStatus.CRITICAL : EServiceStatus.OK;
        DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(check.getNotificationId());
        if (notification == null) {
            throw new Exception(String.format("%s: serviceName = %s, notification id = %s not found", method, serviceName, check.getNotificationId()));
        }
        String stepFrom = check.getStepFrom();
        String stepTo = check.getStepTo();
        String returnCodeFrom = null;
        String returnCodeTo = null;
        Long notifications = timer.getNotifications();
        if (notifications < 1) {
            counter.addSkip();
            LOGGER.debug(String.format("%s: serviceName = %s. skip notify timer (notifications is %s): check.id = %s, schedulerId = %s, jobChain = %s", method,
                    serviceName, notifications, check.getId(), check.getSchedulerId(), check.getJobChain()));
            return;
        }
        DBItemSchedulerMonSystemNotifications sm = null;
        DBItemSchedulerMonSystemNotifications smNotTimer = null;
        boolean isNew = false;
        if (timer.getNotifyOnError()) {
            sm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), check.getId(),
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !isNotifyOnErrorService, stepFrom, stepTo, returnCodeFrom,
                    returnCodeTo);
        } else {
            List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications(systemId, serviceName, notification.getId());
            LOGGER.debug(String.format("%s: found %s system notifications in the db for systemId = %s, serviceName = %s, notificationId = %s)", method,
                    result.size(), systemId, serviceName, notification.getId()));
            for (int i = 0; i < result.size(); i++) {
                DBItemSchedulerMonSystemNotifications resultSm = result.get(i);
                if (resultSm.getCheckId().equals(new Long(0))) {
                    smNotTimer = resultSm;
                }
                if (resultSm.getCheckId().equals(check.getId())) {
                    sm = resultSm;
                }
            }
        }
        if (smNotTimer != null) {
            if (!(smNotTimer.getCurrentNotification().equals(new Long(0)) || (smNotTimer.getCurrentNotification() > 0 && smNotTimer.getRecovered()))) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s: serviceName = %s. skip notify timer(notification has the error): smNotTimer.id = %s, smNotTimer.recovered = %s, "
                                + "smNotTimer.notifications = %s", method, serviceName, smNotTimer.getId(), smNotTimer.getRecovered(),
                        smNotTimer.getCurrentNotification()));
                return;
            }
        }
        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), check.getId(), returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, stepFrom, stepTo, notification.getOrderStartTime(),
                    notification.getOrderEndTime(), new Long(0), notifications, false, false, true);
        }
        if (sm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, notifications = %s, "
                    + "maxNotifictions = %s", method, sm.getId(), sm.getServiceName(), sm.getCurrentNotification(), sm.getMaxNotifications()));
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s: skip notify timer (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s", method,
                    sm.getId(), sm.getServiceName(), sm.getCurrentNotification(), sm.getAcknowledged()));
            return;
        }
        if (sm.getCurrentNotification() >= notifications) {
            setMaxNotifications(isNew, sm);
            counter.addSkip();
            LOGGER.debug(String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, currentNotification = %s", method,
                    sm.getId(), sm.getServiceName(), sm.getCurrentNotification()));
            return;
        }

        try {
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            sm.setSuccess(true);
            sm.setModified(DBLayer.getCurrentDateTime());
            sm.setNotifications(notifications);
            if (isNew) {
                LOGGER.debug(String.format(CREATE_NOTIFICATION_LOGGING, method, sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            } else {
                LOGGER.debug(String.format(UPDATE_NOTIFICATION_LOGGING, method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            }
            ISystemNotifierPlugin pl = timer.getMonitor().getPluginObject();
            LOGGER.info(String.format(METHOD_LOGGING, method));
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, "notifyOnTimer", serviceName, notification.getJobChainName(), sm.getCurrentNotification(),
                    sm.getNotifications(), pl.getClass().getSimpleName()));
            pl.init(timer.getMonitor());
            pl.notifySystem(getSpooler(), options, getDbLayer(), notification, sm, check, pluginStatus, EServiceMessagePrefix.TIMER);
            getDbLayer().getConnection().beginTransaction();
            if (isNew) {
                getDbLayer().getConnection().save(sm);
            } else {
                getDbLayer().getConnection().update(sm);
            }
            getDbLayer().getConnection().commit();
            counter.addSuccess();
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling for rollback
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, "notifyOnTimer", serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private void setMaxNotifications(boolean isNew, DBItemSchedulerMonSystemNotifications sm) throws Exception {
        if (!isNew) {
            sm.setMaxNotifications(true);
            sm.setModified(DBLayer.getCurrentDateTime());
            getDbLayer().getConnection().beginTransaction();
            getDbLayer().getConnection().update(sm);
            getDbLayer().getConnection().commit();
        }
    }

    private boolean checkDoNotificationByReturnCodes(DBItemSchedulerMonNotifications notification, String serviceName, String notifyMsg, String configuredName,
            String configuredReturnCodeFrom, String configuredReturnCodeTo) {
        String method = "checkDoNotificationByReturnCodes";

        if (notification.getOrderStepEndTime() == null) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s]. skip notify (step is not completed - step end time is empty): notification.id = %s, notification.jobName = %s", method,
                    notifyMsg, serviceName, notification.getId(), notification.getJobName()));
            return false;
        }

        if (!configuredReturnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeFrom);
                if (notification.getReturnCode() < rc) {
                    LOGGER.debug(String
                            .format("%s:[%s][%s]. skip notify (return code (%s) less than configured return_code_from (%s)): notification.id = %s, notification.step = %s, notification.jobName = %s, notification.jobChainName = %s",
                                    method, notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeFrom, notification.getId(),
                                    notification.getOrderStepState(), notification.getJobName(), notification.getJobChainName()));
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s:[%s][%s][%s]. skip notify (configured return_code_from \"%s\" is not a valid integer value): %s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeFrom, ex.getMessage()));
                return false;
            }
        }
        if (!configuredReturnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeTo);
                if (notification.getReturnCode() > rc) {
                    LOGGER.debug(String
                            .format("%s:[%s][%s]. skip notify (return code (%s) greater than configured return_code_to (%s)): notification.id = %s, notification.step = %s, notification.jobName = %s, notification.jobChainName = %s",
                                    method, notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeTo, notification.getId(),
                                    notification.getOrderStepState(), notification.getJobName(), notification.getJobChainName()));
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s:[%s][%s][%s]. skip notify (configured return_code_to \"%s\" is not a valid integer value): %s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeTo, ex.getMessage()));
                return false;
            }
        }

        return true;
    }

    private boolean checkDoNotificationTimer(DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer) {
        String method = "  checkDoNotificationTimer";
        boolean notify = true;
        String ref = timer.getRef();
        if (!check.getName().equals(ref)) {
            notify = false;
        }
        LOGGER.debug(String.format("%s: %s(name = %s) and configured(ref = %s)", method, notify ? "ok. check db " : "skip. ", check.getName(), ref));
        return notify;
    }

    private boolean checkDoNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "  checkDoNotification";
        boolean notify = true;
        String schedulerId = jc.getSchedulerId();
        String jobChain = jc.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id = %s: %s", method, schedulerId, ex));
            }
        }
        if (notify && !jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getJobChainName().matches(jobChain)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id = %s, name = %s: %s", method, schedulerId, jobChain, ex));
            }
        }
        LOGGER.debug(String.format("%s: %s(schedulerId = %s, jobChain = %s) and configured(schedulerId = %s, jobChain = %s)", method,
                notify ? "ok. do check db " : "skip. ", notification.getSchedulerId(), notification.getJobChainName(), schedulerId, jobChain));
        return notify;
    }

    private boolean checkDoNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "  checkDoNotification";
        boolean notify = true;
        String schedulerId = job.getSchedulerId();
        String jobName = job.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id = %s: %s", method, schedulerId, ex));
            }
        }
        if (notify && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getJobName().matches(jobName)) {
                    notify = false;
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s: check with configured scheduler_id = %s, name = %s: %s", method, schedulerId, jobName, ex));
            }
        }
        LOGGER.debug(String.format("%s: %s(schedulerId = %s, jobName = %s) and configured(schedulerId = %s, jobName = %s)", method, notify ? "ok. do check db "
                : "skip. ", notification.getSchedulerId(), notification.getJobName(), schedulerId, jobName));
        return notify;
    }

    private JobChainNotification getJobChainNotification(DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "getJobChainNotification";

        JobChainNotification jcn = new JobChainNotification();
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;
        // stepFrom, stepTo handling
        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)
                || !jc.getExcludedSteps().isEmpty()) {

            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(largeResultFetchSize, notification.getOrderHistoryId());
            if (steps == null || steps.isEmpty()) {
                throw new Exception(String.format("%s: no steps found for orderHistoryId = %s", method, notification.getOrderHistoryId()));
            }
            for (DBItemSchedulerMonNotifications step : steps) {
                if (stepFrom != null && step.getOrderStepState().equalsIgnoreCase(stepFrom) && stepFromIndex.equals(new Long(0))) {
                    stepFromIndex = step.getStep();
                    stepFromNotification = step;
                }
                if (stepTo != null && step.getOrderStepState().equalsIgnoreCase(stepTo)) {
                    stepToIndex = step.getStep();
                    stepToNotification = step;
                }
                jcn.setLastStep(step);
            }
            if (stepToIndex.equals(new Long(0))) {
                stepToIndex = jcn.getLastStep().getStep();
            }
            jcn.setSteps(steps);
            jcn.setStepFromIndex(stepFromIndex);
            jcn.setStepToIndex(stepToIndex);
            jcn.setStepFrom(stepFromNotification);
            jcn.setStepTo(stepToNotification);

            if (stepFrom != null && !stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && jcn.getStepFrom() == null) {
                jcn.setSteps(null);

                LOGGER.debug(String
                        .format("%s: skip. configured stepFrom \"%s\" not founded. notification.getOrderHistoryId() = %s, jcn.getStepFromIndex() = %s, jcn.getStepToIndex() = %s, configured stepTo = %s",
                                method, stepFrom, notification.getOrderHistoryId(), jcn.getStepFromIndex(), jcn.getStepToIndex(), stepTo));
            } else {
                for (DBItemSchedulerMonNotifications step : jcn.getSteps()) {
                    if (step.getStep() >= jcn.getStepFromIndex() && step.getStep() <= jcn.getStepToIndex()) {
                        jcn.setLastStepForNotification(step);
                    }
                }

                LOGGER.debug(String
                        .format("%s: notification.getOrderHistoryId() = %s, jcn.getSteps().size() = %s, jcn.getStepFromIndex() = %s, jcn.getStepToIndex() = %s, configured stepFrom = %s, configured stepTo = %s",
                                method, notification.getOrderHistoryId(), jcn.getSteps().size(), jcn.getStepFromIndex(), jcn.getStepToIndex(), stepFrom, stepTo));
            }

        } else {
            LOGGER.debug(String.format("%s:  find last step for notification.getOrderHistoryId = %s. notification.id = %s", method,
                    notification.getOrderHistoryId(), notification.getId()));
            jcn.setLastStepForNotification(getDbLayer().getNotificationsOrderLastStep(largeResultFetchSize, notification, false));
            jcn.setStepFrom(notification);
            jcn.setStepTo(jcn.getLastStepForNotification());
        }
        return jcn;
    }

    private void executeNotifyJob(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJob job) throws Exception {
        String method = "executeNotifyJob";
        String serviceNameOnError = job.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = job.getMonitor().getServiceNameOnSuccess();

        if (job.getNotifications() < 1) {
            counter.addSkip();
            LOGGER.debug(String
                    .format("%s: serviceNameOnError = %s, serviceNameOnSuccess = %s. skip notify Job (maxNotifications is %s): notification.id = %s, schedulerId = %s, job = %s",
                            method, serviceNameOnError, serviceNameOnSuccess, job.getNotifications(), notification.getId(), notification.getSchedulerId(),
                            notification.getJobName()));
            return;
        }
        if (!StringUtils.isEmpty(serviceNameOnError)) {
            executeNotifyJob(sm, systemId, notification, job, true);
        }
        if (!StringUtils.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJob(sm, systemId, notification, job, false);
        }
    }

    private void executeNotifyJob(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJob job, boolean notifyOnError) throws Exception {
        String method = "executeNotifyJob";

        String notifyMsg = null;
        String serviceName = null;
        EServiceStatus serviceStatus = null;
        EServiceMessagePrefix serviceMessagePrefix = null;

        if (notifyOnError) {
            notifyMsg = "notifyOnError";
            serviceName = job.getMonitor().getServiceNameOnError();
            serviceStatus = EServiceStatus.CRITICAL;
            serviceMessagePrefix = EServiceMessagePrefix.ERROR;
        } else {
            notifyMsg = "notifyOnSuccess";
            serviceName = job.getMonitor().getServiceNameOnSuccess();
            serviceStatus = EServiceStatus.OK;
            serviceMessagePrefix = EServiceMessagePrefix.NONE;
        }

        String returnCodeFrom = job.getReturnCodeFrom();
        String returnCodeTo = job.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = job.getNotifications();

        Long checkId = new Long(0);
        String stepFrom = notification.getOrderStepState();
        String stepTo = notification.getOrderStepState();

        if (notification.getOrderStepEndTime() == null) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s]. skip notify Job (step is not completed - step end time is empty): notification.id = %s, notification.jobName = %s", method,
                    notifyMsg, serviceName, notification.getId(), notification.getJobName()));
            return;
        }

        if (hasReturnCodes) {
            if (!checkDoNotificationByReturnCodes(notification, serviceName, notifyMsg,job.getName(),returnCodeFrom, returnCodeTo)) {
                counter.addSkip();
                return;
            }
        } else {
            if (notifyOnError && !notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[%s][%s]. skip notify Job (job has no error): notification.id = %s, notification.jobName = %s", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName()));
                return;
            } else if (!notifyOnError && notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String.format(
                        "%s:[%s][%s]. skip notify Job (job has error): notification.id = %s, notification.jobName = %s, notification.errorText = %s", method,
                        notifyMsg, serviceName, notification.getId(), notification.getJobName(), notification.getErrorText()));
                return;
            }
        }

        boolean isNew = false;
        if (sm == null) {
            sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        }
        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, stepFrom, stepTo, notification.getTaskStartTime(),
                    notification.getTaskEndTime(), new Long(0), notifications, false, false, !notifyOnError);

        }
        LOGGER.debug(String.format("%s:[%s][%s]. %s. %s", method, notifyMsg, serviceName, isNew ? "new system notification" : "old system notification",
                sm.toString()));

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s]. skip notify Job (count notifications was reached): sm.id = %s, sm.currentNotification = %s, sm.maxNotifictions = %s", method,
                    notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getMaxNotifications()));
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s:[%s][%s]. skip notify Job (is acknowledged): sm.id = %s, sm.currentNotification = %s, sm.acknowledged = %s", method,
                    notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getAcknowledged()));
            return;
        }
        if (sm.getCurrentNotification() >= notifications) {
            this.setMaxNotifications(isNew, sm);
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s]. skip notify Job (count notifications was reached): sm.id = %s, sm.currentNotification = %s, configured notifications = %s",
                    method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), notifications));
            return;
        }

        try {
            sm.setStepFromStartTime(notification.getOrderStepStartTime());
            sm.setStepToEndTime(notification.getOrderStepEndTime());
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            if (sm.getCurrentNotification() >= notifications || sm.getAcknowledged()) {
                sm.setMaxNotifications(true);
            }
            sm.setNotifications(notifications);
            sm.setModified(DBLayer.getCurrentDateTime());
            if (isNew) {
                LOGGER.debug(String.format(CREATE_NOTIFICATION_LOGGING, method, sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            } else {
                LOGGER.debug(String.format(UPDATE_NOTIFICATION_LOGGING, method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            }
            LOGGER.debug(String.format("%s:[%s][%s]. notification.id = %s, notification.jobName = %s", method, notifyMsg, serviceName, notification.getId(),
                    notification.getJobName()));

            ISystemNotifierPlugin pl = job.getMonitor().getPluginObject();
            LOGGER.info(String.format(METHOD_LOGGING, method));
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, notification.getJobName(), sm.getCurrentNotification(),
                    sm.getNotifications(), pl.getClass().getSimpleName()));
            pl.init(job.getMonitor());
            pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), notification, sm, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getConnection().beginTransaction();
            if (isNew) {
                getDbLayer().getConnection().save(sm);
            } else {
                getDbLayer().getConnection().update(sm);
            }
            getDbLayer().getConnection().commit();
            counter.addSuccess();
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, notifyMsg, serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private boolean hasReturnCodes(String returnCodeFrom, String returnCodeTo) {
        return !(returnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && returnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME));
    }

    private void executeNotifyJobChain(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jobChain) throws Exception {
        String method = "executeNotifyJobChain";
        String serviceNameOnError = jobChain.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = jobChain.getMonitor().getServiceNameOnSuccess();

        if (jobChain.getNotifications() < 1) {
            counter.addSkip();
            LOGGER.debug(String
                    .format("%s: serviceNameOnError = \"%s\", serviceNameOnSuccess = \"%s\". skip notify JobChain (maxNotifications is %s): notification.id = %s, schedulerId = %s, job = %s",
                            method, serviceNameOnError, serviceNameOnSuccess, jobChain.getNotifications(), notification.getId(), notification.getSchedulerId(),
                            notification.getJobName()));
            return;
        }

        LOGGER.debug(String.format(
                "%s: serviceNameOnError = \"%s\", serviceNameOnSuccess = \"%s\". notification.id = %s, schedulerId = %s, notification.jobChainName = %s",
                method, serviceNameOnError, serviceNameOnSuccess, notification.getId(), notification.getSchedulerId(), notification.getJobChainName()));

        JobChainNotification jcn = getJobChainNotification(notification, jobChain);
        if (!StringUtils.isEmpty(serviceNameOnError)) {
            if (jcn.getLastStepForNotification() == null) {
                counter.addSkip();
            } else {
                executeNotifyJobChain(sm, systemId, notification, jobChain, jcn, true);
            }
        }

        if (!StringUtils.isEmpty(serviceNameOnSuccess)) {
            if (jcn.getLastStepForNotification() == null) {
                counter.addSkip();
            } else {
                executeNotifyJobChain(sm, systemId, notification, jobChain, jcn, false);
            }
        }
    }

    private void executeNotifyJobChain(DBItemSchedulerMonSystemNotifications sm, String systemId, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jobChain, JobChainNotification jcn, boolean notifyOnError) throws Exception {
        String method = "executeNotifyJobChain";

        if (jcn.getLastStepForNotification() == null) {
            throw new Exception(String.format(LAST_STEP_IS_NULL));
        }

        String notifyMsg = null;
        String serviceName = null;
        EServiceStatus serviceStatus = null;
        EServiceMessagePrefix serviceMessagePrefix = null;
        boolean recovered = false;

        Long checkId = new Long(0);
        String stepFrom = jobChain.getStepFrom();
        String stepTo = jobChain.getStepTo();
        String returnCodeFrom = jobChain.getReturnCodeFrom();
        String returnCodeTo = jobChain.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = jobChain.getNotifications();

        if (notifyOnError) {
            notifyMsg = "notifyOnError";
            serviceName = jobChain.getMonitor().getServiceNameOnError();
            serviceStatus = EServiceStatus.CRITICAL;
            serviceMessagePrefix = EServiceMessagePrefix.ERROR;

            if (!hasReturnCodes && !jcn.getLastStepForNotification().getError() && !notification.getError()) {
                counter.addSkip();
                LOGGER.debug(String
                        .format("%s: [%s][%s]. skip notify JobChain (step %s has no error): jcn.getLastStepForNotification().id = %s, jcn.getLastStepForNotification().jobChainName = %s",
                                method, notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), jcn.getLastStepForNotification().getId(),
                                jcn.getLastStepForNotification().getJobChainName()));
                return;
            }

            if (notification.getRecovered()) {
                serviceStatus = EServiceStatus.OK;
                serviceMessagePrefix = EServiceMessagePrefix.RECOVERED;
                recovered = true;
            }
        } else {
            notifyMsg = "notifyOnSuccess";
            serviceName = jobChain.getMonitor().getServiceNameOnSuccess();
            serviceStatus = EServiceStatus.OK;
            serviceMessagePrefix = EServiceMessagePrefix.NONE;

            if (!hasReturnCodes && jcn.getLastStepForNotification().getError()) {
                counter.addSkip();
                LOGGER.debug(String
                        .format("%s: [%s][%s]. skip notify JobChain (last step %s ends with error): jcn.getLastStepForNotification().id = %s, jcn.getLastStepForNotification().jobChainName = %s",
                                method, notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), jcn.getLastStepForNotification().getId(),
                                jcn.getLastStepForNotification().getJobChainName()));
                return;
            }

            if (jcn.getLastStepForNotification().getOrderEndTime() == null) {
                counter.addSkip();
                LOGGER.debug(String.format("%s:[%s][%s]. skip notify JobChain (order is not yet to end): notification.id = %s, notification.jobChainName = %s",
                        method, notifyMsg, serviceName, notification.getId(), notification.getJobChainName()));
                return;
            }
        }

        if (hasReturnCodes && !checkDoNotificationByReturnCodes(jcn.getLastStepForNotification(), serviceName, notifyMsg,jobChain.getName(),returnCodeFrom, returnCodeTo)) {
            counter.addSkip();
            return;
        }

        if (jobChain.getExcludedSteps().contains(jcn.getLastStepForNotification().getOrderStepState())) {
            if (notifyOnError) {
                if (jcn.getLastStepForNotification().getOrderEndTime() != null
                        && jcn.getLastStepForNotification().getOrderStepState().equals(jcn.getLastStep().getOrderStepState())) {
                    LOGGER.debug(String.format("%s: [%s][%s]. order is completed and error step state equals config step = %s and this is "
                            + "the last order step.  create and do notify system: notification.id = %s", method, notifyMsg, serviceName, jcn
                            .getLastStepForNotification().getOrderStepState(), notification.getId()));
                } else {
                    counter.addSkip();
                    LOGGER.info(String.format("%s:[%s][%s]. skip notify JobChain (order is not completed or error step equals config step = %s and this is "
                            + "not the last order step). notification.id = %s", method, notifyMsg, serviceName, jcn.getLastStepForNotification()
                            .getOrderStepState(), notification.getId()));
                    return;
                }
            } else {
                counter.addSkip();
                LOGGER.info(String.format("%s:[%s][%s]. skip notify JobChain (step = %s is configured as excluded). notification.id = %s"
                        + "serviceName = %s. ", method, notifyMsg, serviceName, jcn.getLastStepForNotification().getOrderStepState(), notification.getId()));
                return;
            }
        }

        boolean isNew = false;
        if (sm == null) {
            sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        }

        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, stepFrom, stepTo, notification.getOrderStartTime(),
                    notification.getOrderEndTime(), new Long(0), notifications, false, false, !notifyOnError);
        }

        if (sm.getAcknowledged()) {
            counter.addSkip();
            LOGGER.debug(String.format("%s: [%s][%s]. skip notify JobChain (is acknowledged): sm.id = %s, sm.currentNotification = %s, sm.acknowledged = %s",
                    method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getAcknowledged()));
            return;
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            LOGGER.debug(String.format(
                    "%s:[%s][%s]. skip notify JobChain (count notifications was reached): sm.id = %s, sm.currentNotification = %s, sm.maxNotifications = %s",
                    method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getMaxNotifications()));
            return;
        }

        boolean maxNotifications = false;
        if (notifyOnError) {
            if (isNew && notification.getRecovered()) {
                counter.addSkip();
                LOGGER.debug(String
                        .format("%s: [%s][%s]. skip notify JobChains (notifications already recovered): notification.id = %s, notification.jobChainName = %s, notification.orderId = %s",
                                method, notifyMsg, serviceName, notification.getId(), notification.getJobChainName(), notification.getOrderId()));
                return;
            }

            if (sm.getRecovered()) {
                this.setMaxNotifications(isNew, sm);
                counter.addSkip();

                LOGGER.debug(String.format(
                        "%s: [%s][%s]. skip notify JobChains (notifications already recovered): sm.id = %s, sm.currentNotification = %s, sm.recovered = %s",
                        method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), sm.getRecovered()));
                return;
            }

            if (recovered) {
                maxNotifications = true;
            } else {
                if (notification.getOrderEndTime() == null) {
                    if (sm.getCurrentNotification() >= notifications) {
                        counter.addSkip();
                        LOGGER.debug(String
                                .format("%s: [%s][%s]. skip notify JobChains (count notifications was reached): sm.id = %s, sm.currentNotification = %s, configured notifications = %s",
                                        method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), notifications));
                        return;
                    }
                } else {
                    if (sm.getCurrentNotification() >= notifications) {
                        this.setMaxNotifications(isNew, sm);

                        counter.addSkip();
                        LOGGER.debug(String
                                .format("%s: [%s][%s]. skip notify JobChains (count notifications was reached): sm.id = %s, sm.currentNotification = %s, configured notifications = %s",
                                        method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), notifications));
                        return;
                    }

                    if (sm.getCurrentNotification() + 1 >= notifications) {
                        maxNotifications = true;
                    }
                }
            }

        } else {
            if (sm.getCurrentNotification() >= notifications) {
                this.setMaxNotifications(isNew, sm);
                counter.addSkip();
                LOGGER.debug(String
                        .format("%s: [%s][%s]. skip notify JobChains (count notifications was reached): sm.id = %s, sm.currentNotification = %s, configured notifications = %s",
                                method, notifyMsg, serviceName, sm.getId(), sm.getCurrentNotification(), notifications));
                return;
            }

            if (sm.getCurrentNotification() + 1 >= notifications) {
                maxNotifications = true;
            }
        }

        try {
            if (jcn.getStepFrom() != null) {
                sm.setStepFromStartTime(jcn.getStepFrom().getOrderStepStartTime());
            }
            if (jcn.getStepTo() != null) {
                sm.setStepToEndTime(jcn.getStepTo().getOrderStepEndTime());
            }
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            sm.setMaxNotifications(maxNotifications);
            sm.setNotifications(notifications);
            sm.setModified(DBLayer.getCurrentDateTime());
            sm.setSuccess(!notifyOnError);
            sm.setRecovered(recovered);

            if (isNew) {
                LOGGER.debug(String.format(CREATE_NOTIFICATION_LOGGING, method, sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            } else {
                LOGGER.debug(String.format(UPDATE_NOTIFICATION_LOGGING, method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getCurrentNotification(),
                        sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            }
            LOGGER.debug(String.format("%s:[%s][%s]. lastStepForNotification: id = %s, orderHistoryId = %s, step = %s, orderStepState = %s", method, notifyMsg,
                    serviceName, jcn.getLastStepForNotification().getId(), jcn.getLastStepForNotification().getOrderHistoryId(), jcn
                            .getLastStepForNotification().getStep(), jcn.getLastStepForNotification().getOrderStepState()));

            ISystemNotifierPlugin pl = jobChain.getMonitor().getPluginObject();
            LOGGER.info(String.format(METHOD_LOGGING, method));
            String jobChainInfo = jcn.getLastStepForNotification().getJobChainName() + "-" + jcn.getLastStepForNotification().getOrderId();
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, jobChainInfo, sm.getCurrentNotification(), sm.getNotifications(), pl
                    .getClass().getSimpleName()));
            pl.init(jobChain.getMonitor());
            pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), jcn.getLastStepForNotification(), sm, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getConnection().beginTransaction();
            if (isNew) {
                getDbLayer().getConnection().save(sm);
            } else {
                getDbLayer().getConnection().update(sm);
            }
            getDbLayer().getConnection().commit();
            counter.addSuccess();
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, notifyMsg, serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private void insertDummySysNotification(String systemId, Long notificationId) {
        String method = "insertDummySysNotification";
        try {

            DBItemSchedulerMonSystemNotifications sm = getDbLayer().createDummySystemNotification(systemId, notificationId);

            getDbLayer().getConnection().beginTransaction();
            getDbLayer().deleteDummySystemNotification(systemId);
            getDbLayer().getConnection().save(sm);
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            LOGGER.warn(String.format("%s:%s", method, ex.toString()));
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
            }
        }
    }

    private void notifyTimer(String systemId) throws Exception {
        String method = "notifyTimer";
        if (!monitorOnSuccessTimers.isEmpty() || !monitorOnErrorTimers.isEmpty()) {
            notifyTimer(systemId, monitorOnSuccessTimers, monitorOnErrorTimers);
        } else {
            LOGGER.info(String.format("%s: skip notify timer. found 0 Timer definitions", method));
        }
    }

    private void notifyTimer(String systemId, ArrayList<ElementNotificationTimerRef> timersOnSuccess, ArrayList<ElementNotificationTimerRef> timersOnError)
            throws Exception {
        String method = "notifyTimer";
        List<DBItemSchedulerMonChecks> result = getDbLayer().getChecksForNotifyTimer(largeResultFetchSize);
        LOGGER.info(String.format(
                "%s: found %s \"service_name_on_success\" and %s \"service_name_on_error\" timer definitions and %s checks for timers in the db", method,
                timersOnSuccess.size(), timersOnError.size(), result.size()));
        initSendCounters();
        for (DBItemSchedulerMonChecks check : result) {
            LOGGER.debug(String.format("%s: notify timer \"service_name_on_success\"", method));
            for (int i = 0; i < timersOnSuccess.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnSuccess.get(i);
                if (checkDoNotificationTimer(check, t)) {
                    executeNotifyTimer(systemId, check, t, false);
                } else {
                    counter.addSkip();
                }
            }
            LOGGER.debug(String.format("%s: notify timer \"service_name_on_error\"", method));
            for (int i = 0; i < timersOnError.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnError.get(i);
                if (checkDoNotificationTimer(check, t)) {
                    executeNotifyTimer(systemId, check, t, true);
                } else {
                    counter.addSkip();
                }
            }
        }
        LOGGER.info(String.format(SENT_LOGGING, method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));
    }

    private void notifyAgain(String systemId) throws Exception {
        String method = "notifyAgain";

        Long objectType = null;
        if (monitorJobChains.size() == 0 && monitorJobs.size() > 0) {
            objectType = DBLayer.NOTIFICATION_OBJECT_TYPE_JOB;
        } else if (monitorJobChains.size() > 0 && monitorJobs.size() == 0) {
            objectType = DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN;
        }

        List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications4NotifyAgain(systemId, objectType);
        LOGGER.info(String.format("%s: [%s] found %s system notifications in the db for notify again", method, systemId, result.size()));
        initSendCounters();
        for (DBItemSchedulerMonSystemNotifications systemNotification : result) {
            counter.addTotal();
            DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(systemNotification.getNotificationId());
            if (notification == null) {
                counter.addSkip();
                continue;
            }

            if (!systemNotification.getCheckId().equals(new Long(0))) {
                // timer
                counter.addSkip();
                continue;
            }

            if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {

                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                for (int i = 0; i < monitorJobChains.size(); i++) {
                    counter.addTotal();
                    ElementNotificationJobChain jc = monitorJobChains.get(i);
                    if (checkDoNotification(notification, jc)) {
                        String serviceNameOnError = StringUtils.isEmpty(jc.getMonitor().getServiceNameOnError()) ? "" : jc.getMonitor().getServiceNameOnError();
                        String serviceNameOnSuccess = StringUtils.isEmpty(jc.getMonitor().getServiceNameOnSuccess()) ? "" : jc.getMonitor()
                                .getServiceNameOnSuccess();
                        if (systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError)
                                || systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnSuccess)) {
                            executeNotifyJobChain(systemNotification, systemId, notification, jc);
                        } else {
                            counter.addSkip();
                        }
                    } else {
                        counter.addSkip();
                    }
                }
                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore) && systemNotification.getStepToEndTime() != null) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the JobChain configuration was changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            } else if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                for (int i = 0; i < monitorJobs.size(); i++) {
                    counter.addTotal();
                    ElementNotificationJob job = monitorJobs.get(i);
                    if (checkDoNotification(notification, job)) {
                        String serviceNameOnError = StringUtils.isEmpty(job.getMonitor().getServiceNameOnError()) ? "" : job.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = StringUtils.isEmpty(job.getMonitor().getServiceNameOnSuccess()) ? "" : job.getMonitor()
                                .getServiceNameOnSuccess();

                        if ((systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName().equalsIgnoreCase(
                                serviceNameOnSuccess))) {
                            executeNotifyJob(systemNotification, systemId, notification, job);
                        } else {
                            counter.addSkip();
                        }
                    } else {
                        counter.addSkip();
                    }
                }
                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore)) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the Job configuration was changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            }
        }

        LOGGER.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(),
                counter.getSkip(), counter.getTotal()));

    }

    private void notifyNew(String systemId) throws Exception {
        String method = "notifyNew";

        Long maxNotificationId = new Long(0);

        List<DBItemSchedulerMonNotifications> result = getDbLayer().getNotifications4NotifyNew(systemId);
        LOGGER.info(String.format("%s: [%s] found %s new notifications in the db", method, systemId, result.size()));
        initSendCounters();

        ArrayList<String> checkedJobchans = new ArrayList<String>();
        for (DBItemSchedulerMonNotifications notification : result) {
            counter.addTotal();

            String identifier = notification.getOrderHistoryId().toString();

            if (notification.getId() > maxNotificationId) {
                maxNotificationId = notification.getId();
            }

            if (notification.getStep().equals(new Long(1))) {// only for 1st
                                                             // step
                if (!checkedJobchans.contains(identifier)) {
                    checkedJobchans.add(identifier);
                    for (int i = 0; i < monitorJobChains.size(); i++) {
                        counter.addTotal();

                        ElementNotificationJobChain jc = monitorJobChains.get(i);
                        if (checkDoNotification(notification, jc)) {
                            executeNotifyJobChain(null, systemId, notification, jc);
                        } else {
                            counter.addSkip();
                        }
                    }
                }
            } else {
                if (checkedJobchans.contains(identifier)) {
                    counter.addSkip();
                    LOGGER.debug(String
                            .format("%s: [%s] skip analyze JobChain notification (step greater than 1). notification.id = %s, notification.jobChainName = %s, notification.step = %s, notification.orderStepState = %s",
                                    method, systemId, notification.getId(), notification.getJobChainName(), notification.getStep(),
                                    notification.getOrderStepState()));
                } else {
                    checkedJobchans.add(identifier);
                    DBItemSchedulerMonNotifications n = getDbLayer().getNotificationFirstStep(notification);
                    if (n != null) {
                        for (int i = 0; i < monitorJobChains.size(); i++) {
                            counter.addTotal();

                            ElementNotificationJobChain jc = monitorJobChains.get(i);
                            if (checkDoNotification(n, jc)) {
                                executeNotifyJobChain(null, systemId, n, jc);
                            } else {
                                counter.addSkip();
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < monitorJobs.size(); i++) {
                counter.addTotal();

                ElementNotificationJob job = monitorJobs.get(i);
                if (checkDoNotification(notification, job)) {
                    executeNotifyJob(null, systemId, notification, job);
                } else {
                    counter.addSkip();
                }
            }
        }

        LOGGER.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(),
                counter.getSkip(), counter.getTotal()));

        if (maxNotificationId > 0 && counter.getError() == 0) {
            insertDummySysNotification(systemId, maxNotificationId);
        }

    }

    @Override
    public void process() throws Exception {
        initConfig();

        notifyAgain(systemId);
        notifyNew(systemId);
        notifyTimer(systemId);
    }

    public Spooler getSpooler() {
        return spooler;
    }

}
