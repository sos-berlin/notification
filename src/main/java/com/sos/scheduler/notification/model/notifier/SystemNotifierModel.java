package com.sos.scheduler.notification.model.notifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemResults;
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

import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class SystemNotifierModel extends NotificationModel implements INotificationModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierModel.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String THREE_PARAMS_LOGGING = "[%s][%s][%s]-%s";
    private static final String CALL_PLUGIN_LOGGING = "[%s][%s][%s][%s]notification %s of %s. call plugin %s";
    private static final String SENT_LOGGING = "[%s]sended=%s, error=%s, skipped=%s (total checked=%s)";
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
    private ArrayList<Long> handledByNotifyAgain;

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
            throw new Exception(String.format("[%s][schema file not found]%s", method, schemaFile.getCanonicalPath()));
        }
        systemFile = new File(this.options.system_configuration_file.getValue());
        if (!systemFile.exists()) {
            throw new Exception(String.format("[%s][system configuration file not found]%s", method, systemFile.getCanonicalPath()));
        }
        SOSXMLXPath xpath = new SOSXMLXPath(systemFile.getCanonicalPath());
        initMonitorObjects();
        systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
        if (SOSString.isEmpty(systemId)) {
            throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
        }

        NodeList monitors = NotificationXmlHelper.selectNotificationMonitorDefinitions(xpath);
        setMonitorObjects(xpath, monitors);

        LOGGER.info(String.format("[%s][%s][jobChains=%s][jobs=%s]%s", method, systemId, monitorJobChains.size(), monitorJobs.size(), systemFile
                .getCanonicalPath()));
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

    private void executeNotifyTimer(String systemId, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer,
            boolean isNotifyOnErrorService) throws Exception {
        // Output indent
        String method = "  executeNotifyTimer";
        String serviceName = (isNotifyOnErrorService) ? timer.getMonitor().getServiceNameOnError() : timer.getMonitor().getServiceNameOnSuccess();
        EServiceStatus pluginStatus = (isNotifyOnErrorService) ? EServiceStatus.CRITICAL : EServiceStatus.OK;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(check)));
        }
        DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(check.getNotificationId());
        if (notification == null) {
            throw new Exception(String.format("[%s]serviceName=%s, notification id=%s not found", method, serviceName, check.getNotificationId()));
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s]%s", method, NotificationModel.toString(notification)));
        }
        String stepFrom = check.getStepFrom();
        String stepTo = check.getStepTo();
        String returnCodeFrom = null;
        String returnCodeTo = null;
        Long notifications = timer.getNotifications();
        if (notifications < 1) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][skip]notifications %s < 1", method, serviceName, notifications));
            }
            return;
        }
        DBItemSchedulerMonSystemNotifications sm = null;
        DBItemSchedulerMonSystemNotifications smNotTimer = null;
        boolean isNew = false;
        if (timer.getNotifyOnError()) {
            sm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), check.getId(),
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !isNotifyOnErrorService, stepFrom, stepTo, returnCodeFrom, returnCodeTo);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][notify_on_error=true]%s", method, NotificationModel.toString(sm)));
            }
        } else {
            List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications(systemId, serviceName, notification.getId());
            if (result == null || result.size() == 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][notify_on_error=false]result ist null or empty", method));
                }
            } else {
                for (int i = 0; i < result.size(); i++) {
                    DBItemSchedulerMonSystemNotifications resultSm = result.get(i);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][result][%s]%s", method, i, NotificationModel.toString(resultSm)));
                    }

                    if (resultSm.getCheckId().equals(new Long(0))) {
                        smNotTimer = resultSm;
                    }
                    if (resultSm.getCheckId().equals(check.getId())) {
                        sm = resultSm;
                    }
                }
            }
        }
        if (smNotTimer != null) {
            if (!(smNotTimer.getCurrentNotification().equals(new Long(0)) || (smNotTimer.getCurrentNotification() > 0 && smNotTimer
                    .getRecovered()))) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][skip][notification has the error]%s", method, serviceName, NotificationModel.toString(
                            smNotTimer)));
                }
                return;
            }
        }
        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), check.getId(), returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, stepFrom, stepTo, notification.getOrderStartTime(), notification.getOrderEndTime(),
                    new Long(0), notifications, false, false, true);
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][isNew=%s]%s", method, isNew, NotificationModel.toString(sm)));
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]maxNotifications=true", method));
            }
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip]acknowledged=true", method));
            }
            return;
        }
        if (sm.getCurrentNotification() >= notifications) {
            if (!isNew) {
                closeSystemNotification(sm, notification.getOrderEndTime());
            }
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip][%s]count notifications was reached", method, notifications));
            }
            return;
        }

        try {
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            sm.setSuccess(true);
            sm.setModified(DBLayer.getCurrentDateTime());
            sm.setNotifications(notifications);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][isNew=%s]%s", method, isNew, NotificationModel.toString(sm)));
            }

            ISystemNotifierPlugin pl = timer.getMonitor().getPluginObject();
            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, "notifyOnTimer", serviceName, notification.getJobChainName(), sm
                    .getCurrentNotification(), sm.getNotifications(), pl.getClass().getSimpleName()));
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
            LOGGER.info("----------------------------------------------------------------");
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

    private void closeSystemNotification(DBItemSchedulerMonSystemNotifications sm, Date stepToEndTime) throws Exception {
        sm.setStepToEndTime(stepToEndTime);
        sm.setMaxNotifications(true);
        sm.setModified(DBLayer.getCurrentDateTime());
        getDbLayer().getConnection().beginTransaction();
        getDbLayer().getConnection().update(sm);
        getDbLayer().getConnection().commit();
    }

    private boolean checkDoNotifyByReturnCodes(DBItemSchedulerMonNotifications notification, String serviceName, String notifyMsg,
            String configuredName, String configuredReturnCodeFrom, String configuredReturnCodeTo) {
        String method = "checkDoNotifyByReturnCodes";

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, notifyMsg, serviceName, NotificationModel.toString(notification)));
        }

        if (notification.getOrderStepEndTime() == null) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip]step is not completed - step end time is empty", method, notifyMsg, serviceName));
            }

            return false;
        }

        if (!configuredReturnCodeFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeFrom);
                if (notification.getReturnCode() < rc) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip]return code (%s) less than configured return_code_from (%s)", method, notifyMsg,
                                serviceName, notification.getReturnCode(), configuredReturnCodeFrom));
                    }
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[%s][%s][%s][%s][skip][configured return_code_from \"%s\" is not a valid integer value]%s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeFrom, ex.getMessage()));
                return false;
            }
        }
        if (!configuredReturnCodeTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                Long rc = Long.parseLong(configuredReturnCodeTo);
                if (notification.getReturnCode() > rc) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][skip]return code (%s) greater than configured return_code_to (%s)", method,
                                notifyMsg, serviceName, notification.getReturnCode(), configuredReturnCodeTo));
                    }
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[%s][%s][%s][%s][skip][configured return_code_to \"%s\" is not a valid integer value]%s", method,
                        notifyMsg, serviceName, configuredName, configuredReturnCodeTo, ex.getMessage()));
                return false;
            }
        }

        return true;
    }

    private boolean checkDoNotifyTimer(int currentCounter, DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer) {
        String method = "  [" + currentCounter + "][checkDoNotifyTimer]";
        boolean notify = true;
        String ref = timer.getRef();
        if (!check.getName().equals(ref)) {
            notify = false;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[notify=%s][ref=%s]%s", method, notify, ref, NotificationModel.toString(check)));
        }
        return notify;
    }

    private boolean checkDoNotify(int currentCounter, DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "  [" + currentCounter + "][checkDoNotify]";
        boolean notify = true;
        String schedulerId = jc.getSchedulerId();
        String jobChain = jc.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][schedulerId not match][%s][%s]", method, notify, notification.getSchedulerId(),
                                schedulerId));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s]%s", method, schedulerId, ex));
            }
        }
        if (notify && !jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getJobChainName().matches(jobChain)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][not match][%s][%s]", method, notify, notification.getJobChainName(), jobChain));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][match][%s][%s][%s][%s]", method, notify, notification.getSchedulerId(), schedulerId,
                                notification.getJobChainName(), jobChain));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, jobChain, ex));
            }
        }

        return notify;
    }

    private boolean checkDoNotify(int currentCounter, DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "  [" + currentCounter + "][checkDoNotify]";
        boolean notify = true;
        String schedulerId = job.getSchedulerId();
        String jobName = job.getName();
        if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getSchedulerId().matches(schedulerId)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][schedulerId not match][%s][%s]", method, notify, notification.getSchedulerId(),
                                schedulerId));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s]%s", method, schedulerId, ex));
            }
        }
        if (notify && !jobName.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            try {
                if (!notification.getJobName().matches(jobName)) {
                    notify = false;

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][not match][%s][%s]", method, notify, notification.getJobName(), jobName));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notify=%s][match][%s][%s][%s][%s]", method, notify, notification.getSchedulerId(), schedulerId,
                                notification.getJobName(), jobName));
                    }
                }
            } catch (Exception ex) {
                throw new Exception(String.format("%s[check with configured scheduler_id=%s, name=%s]%s", method, schedulerId, jobName, ex));
            }
        }
        return notify;
    }

    private JobChainNotification getJobChainNotification(int currentCounter, DBItemSchedulerMonNotifications notification,
            ElementNotificationJobChain jc) throws Exception {
        String method = "    [" + currentCounter + "][getJobChainNotification]";

        JobChainNotification jcn = new JobChainNotification();
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;
        // stepFrom, stepTo handling
        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s analyze configured step_from, step_to", method));
            }
            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(largeResultFetchSize, notification
                    .getOrderHistoryId());
            if (steps == null || steps.isEmpty()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[no steps found for orderHistoryId=%s]%s", method, notification.getOrderHistoryId(),
                            NotificationModel.toString(notification)));
                }
                throw new Exception(String.format("%sno steps found for orderHistoryId=%s", method, notification.getOrderHistoryId()));
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
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "%s can't set setLastStepForNotification. configured step_from \"%s\" not founded in the notification table.", method,
                            stepFrom));
                }
            } else {
                for (DBItemSchedulerMonNotifications step : jcn.getSteps()) {
                    if (step.getStep() >= jcn.getStepFromIndex() && step.getStep() <= jcn.getStepToIndex()) {
                        jcn.setLastStepForNotification(step);
                    }
                }
            }

        } else {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s get last step from db", method));
            }

            jcn.setLastStepForNotification(getDbLayer().getNotificationsOrderLastStep(largeResultFetchSize, notification, false));
            jcn.setStepFrom(notification);
            if (jcn.getLastStepForNotification() != null) {
                jcn.setStepTo(jcn.getLastStepForNotification());
                jcn.setStepToIndex(jcn.getStepTo().getStep());
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[stepFromIndex=%s][stepToIndex=%s]", method, jcn.getStepFromIndex(), jcn.getStepToIndex()));
            LOGGER.debug(String.format("%s[stepFrom][%s]", method, NotificationModel.toString(jcn.getStepFrom())));
            LOGGER.debug(String.format("%s[stepTo][%s]", method, NotificationModel.toString(jcn.getStepTo())));
        }

        return jcn;
    }

    private void executeNotifyJob(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJob job) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJob]";
        String serviceNameOnError = job.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = job.getMonitor().getServiceNameOnSuccess();

        if (job.getNotifications() < 1) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][skip][notifications < 1]%s", method, NotificationModel.toString(job)));
            }
            return;
        }
        if (!StringUtils.isEmpty(serviceNameOnError)) {
            executeNotifyJob(currentCounter, sm, systemId, notification, job, true);
        }
        if (!StringUtils.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJob(currentCounter, sm, systemId, notification, job, false);
        }
    }

    private void executeNotifyJob(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJob job, boolean notifyOnError) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJob]";

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
            serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
        }

        String returnCodeFrom = job.getReturnCodeFrom();
        String returnCodeTo = job.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = job.getNotifications();

        Long checkId = new Long(0);
        String stepFrom = notification.getOrderStepState();
        String stepTo = notification.getOrderStepState();

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s]%s", method, notifyMsg, serviceName, NotificationModel.toString(notification)));
        }

        if (notification.getOrderStepEndTime() == null) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip][order]step is not completed - orderStepEndTime is null", method, notifyMsg,
                        serviceName));
            }
            return;
        }

        if (hasReturnCodes) {
            if (!checkDoNotifyByReturnCodes(notification, serviceName, notifyMsg, job.getName(), returnCodeFrom, returnCodeTo)) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]checkDoNotifyByReturnCodes=false", method, notifyMsg,
                            serviceName, notifyOnError));
                }

                return;
            }
        } else {
            if (notifyOnError && !notification.getError()) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]notification has no error", method, notifyMsg, serviceName,
                            notifyOnError));
                }
                return;
            } else if (!notifyOnError && notification.getError()) {
                counter.addSkip();

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip][notifyOnError=%s]job has an error", method, notifyMsg, serviceName,
                            notifyOnError));
                }
                return;
            }
        }

        boolean isNew = false;
        if (sm == null) {
            sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, DBLayer.NOTIFICATION_OBJECT_TYPE_JOB,
                    !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);
        }
        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                    DBLayer.NOTIFICATION_OBJECT_TYPE_JOB, stepFrom, stepTo, notification.getTaskStartTime(), notification.getTaskEndTime(), new Long(
                            0), notifications, false, false, !notifyOnError);

        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][isNew=%s]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sm)));
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip]maxNotifications=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip]acknowledged=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sm.getCurrentNotification() >= notifications) {
            if (!isNew) {
                closeSystemNotification(sm, notification.getOrderEndTime());
            }
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][skip][%s]count notifications was reached", method, notifyMsg, serviceName, notifications));
            }
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

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][isNew=%s]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sm)));
            }

            ISystemNotifierPlugin pl = job.getMonitor().getPluginObject();
            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format(CALL_PLUGIN_LOGGING, method, notifyMsg, serviceName, notification.getJobName(), sm.getCurrentNotification(), sm
                    .getNotifications(), pl.getClass().getSimpleName()));
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
            LOGGER.info("----------------------------------------------------------------");
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

    private void executeNotifyJobChain(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jobChain) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJobChain]";

        if (jobChain.getNotifications() < 1) {
            counter.addSkip();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[skip][notifications < 1]%s", method, NotificationModel.toString(jobChain)));
            }
            return;
        }

        String serviceNameOnError = jobChain.getMonitor().getServiceNameOnError();
        String serviceNameOnSuccess = jobChain.getMonitor().getServiceNameOnSuccess();
        if (!StringUtils.isEmpty(serviceNameOnError)) {
            executeNotifyJobChain(currentCounter, sm, systemId, notification, jobChain, true);
        }
        if (!StringUtils.isEmpty(serviceNameOnSuccess)) {
            executeNotifyJobChain(currentCounter, sm, systemId, notification, jobChain, false);
        }
    }

    private void executeNotifyJobChain(int currentCounter, DBItemSchedulerMonSystemNotifications sm, String systemId,
            DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jobChain, boolean notifyOnError) throws Exception {
        String method = "    [" + currentCounter + "][executeNotifyJobChain]";

        String notifyMsg = null;
        String serviceName = null;
        EServiceStatus serviceStatus = null;
        EServiceMessagePrefix serviceMessagePrefix = null;
        boolean recovered = false;
        boolean maxNotifications = false;

        Long checkId = new Long(0);
        String stepFrom = jobChain.getStepFrom();
        String stepTo = jobChain.getStepTo();
        String returnCodeFrom = jobChain.getReturnCodeFrom();
        String returnCodeTo = jobChain.getReturnCodeTo();
        boolean hasReturnCodes = hasReturnCodes(returnCodeFrom, returnCodeTo);
        Long notifications = jobChain.getNotifications();
        boolean isNotifyAgain = sm != null;
        boolean isNew = false;

        if (notifyOnError) {
            notifyMsg = "notifyOnError";
            serviceName = jobChain.getMonitor().getServiceNameOnError();
            serviceStatus = EServiceStatus.CRITICAL;
            serviceMessagePrefix = EServiceMessagePrefix.ERROR;
        } else {
            notifyMsg = "notifyOnSuccess";
            serviceName = jobChain.getMonitor().getServiceNameOnSuccess();
            serviceStatus = EServiceStatus.OK;
            serviceMessagePrefix = EServiceMessagePrefix.SUCCESS;
        }

        if (isNotifyAgain) {
            if (!handledByNotifyAgain.contains(sm.getId())) {
                handledByNotifyAgain.add(sm.getId());
            }
            if (notifyOnError && sm.getSuccess()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip][notifyAgain]is notifyOnError but system notification has succes=1", method,
                            notifyMsg, serviceName));
                }
                return;
            }

            if (!notifyOnError && !sm.getSuccess()) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip][notifyAgain]is notifyOnSuccess but system notification has succes=0", method,
                            notifyMsg, serviceName));
                }
                return;
            }

        }

        if (sm == null) {
            try {
                getDbLayer().getConnection().beginTransaction();

                sm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId,
                        DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, !notifyOnError, stepFrom, stepTo, returnCodeFrom, returnCodeTo);

                if (sm == null) {
                    isNew = true;
                    sm = getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, returnCodeFrom, returnCodeTo,
                            DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN, stepFrom, stepTo, notification.getOrderStartTime(), notification
                                    .getOrderEndTime(), new Long(0), notifications, false, false, !notifyOnError);
                    getDbLayer().getConnection().save(sm);
                }
                getDbLayer().getConnection().commit();
            } catch (Exception ex) {
                try {
                    getDbLayer().getConnection().rollback();
                } catch (Exception exx) {
                }
                LOGGER.error(String.format("%s %s", method, ex.toString()), ex);
                return;
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s][%s][isNew=%s]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sm)));
        }

        if (!isNotifyAgain) {
            if (handledByNotifyAgain.contains(sm.getId())) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]already handled by notifyAgain", method, notifyMsg, serviceName));
                }
                return;
            }
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip]maxNotifications=true", method, notifyMsg, serviceName));
            }
            return;
        }
        if (sm.getAcknowledged()) {
            counter.addSkip();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip]acknowledged=true", method, notifyMsg, serviceName));
            }
            return;
        }

        JobChainNotification jcn = getJobChainNotification(currentCounter, notification, jobChain);
        if (jcn.getLastStepForNotification() == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][skip][isNew=%s]getLastStepForNotification=null", method, notifyMsg, serviceName, isNew));
            }
            return;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[%s][%s][isNew=%s][getLastStepForNotification]%s", method, notifyMsg, serviceName, isNew, NotificationModel
                    .toString(jcn.getLastStepForNotification())));
        }

        DBItemSchedulerMonNotifications notification2send = null;
        DBItemSchedulerMonSystemResults lastErrorSended = null;
        if (notifyOnError) {
            lastErrorSended = getDbLayer().getSystemResultLastStep(largeResultFetchSize, sm.getId());

            if (jcn.getLastStepForNotification().getError()) {
                if (lastErrorSended != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onError][lastErrorSended]%s", method, notifyMsg, serviceName, NotificationModel
                                .toString(lastErrorSended)));
                    }

                    if (lastErrorSended.getOrderStepState().equals(jcn.getLastStepForNotification().getOrderStepState())) {
                        if (lastErrorSended.getCurrentNotification() >= notifications) {
                            counter.addSkip();

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[%s][%s][onError][skip][%s][count notifications for this state was reached]%s of %s",
                                        method, notifyMsg, serviceName, lastErrorSended.getOrderStepState(), lastErrorSended.getCurrentNotification(),
                                        notifications));
                            }

                            if (notification.getOrderEndTime() != null) {
                                closeSystemNotification(sm, notification.getOrderEndTime());
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format(
                                            "%s[%s][%s][onError][close]count notifications was reached and orderEndTime is not null", method,
                                            notifyMsg, serviceName));
                                }
                            }
                            return;
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][onError][state changed]%s -> %s", method, notifyMsg, serviceName, lastErrorSended
                                    .getOrderStepState(), jcn.getLastStepForNotification().getOrderStepState()));
                        }
                        lastErrorSended = null;
                    }
                }
                notification2send = jcn.getLastStepForNotification();
            } else {
                // check for recovery
                if (lastErrorSended != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onNoError][lastErrorSended]%s", method, notifyMsg, serviceName, NotificationModel
                                .toString(lastErrorSended)));
                    }
                    if (lastErrorSended.getRecovered()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]recovery already sended", method, notifyMsg,
                                    serviceName));
                        }
                    } else {
                        if (jcn.getLastStepForNotification().getId() > lastErrorSended.getNotificationId()) {

                            boolean setRecovery = true;
                            if (lastErrorSended.getOrderStepState().equals(jcn.getLastStepForNotification().getOrderStepState())) {
                                if (jcn.getLastStepForNotification().getOrderStepEndTime() == null) {
                                    if (isDebugEnabled) {
                                        LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery][%s]state rerun is not completed", method,
                                                notifyMsg, serviceName, lastErrorSended.getOrderStepState()));
                                    }
                                    setRecovery = false;
                                }
                            }
                            if (setRecovery) {
                                // send recovery
                                notification2send = getDbLayer().getNotification(lastErrorSended.getNotificationId());
                                lastErrorSended.setRecovered(true);
                                recovered = true;
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("%s[%s][%s][onNoError][found][recovery]%s", method, notifyMsg, serviceName,
                                            NotificationModel.toString(notification2send)));
                                }
                            }

                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]lastStep %s <= lastErrorSended %s", method,
                                        notifyMsg, serviceName, jcn.getLastStepForNotification().getId(), lastErrorSended.getNotificationId()));
                            }
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][onNoError][skip][recovery]lastErrorSended not found", method, notifyMsg, serviceName));
                    }
                }
            }

            if (notification2send == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]notification2send is null", method, notifyMsg, serviceName));
                }

                if (isNotifyAgain) {
                    if (notification.getOrderEndTime() != null) {
                        closeSystemNotification(sm, notification.getOrderEndTime());
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[%s][%s][close][notification2send is null]orderEndTime is not null", method, notifyMsg,
                                    serviceName));
                        }
                    }
                }
                return;
            }

            if (recovered) {
                serviceStatus = EServiceStatus.OK;
                serviceMessagePrefix = EServiceMessagePrefix.RECOVERED;
            } else {
                if (jobChain.getExcludedSteps().contains(notification2send.getOrderStepState())) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][%s][step is excluded]%s", method, notifyMsg, serviceName, notification2send
                                .getOrderStepState(), NotificationModel.toString(notification2send)));
                    }
                    // close
                    return;
                }

                if (hasReturnCodes && !checkDoNotifyByReturnCodes(notification2send, serviceName, notifyMsg, jobChain.getName(), returnCodeFrom,
                        returnCodeTo)) {
                    counter.addSkip();

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][checkDoNotifyByReturnCodes=false][%s]", method, notifyMsg, serviceName,
                                NotificationModel.toString(notification2send)));
                    }

                    // close
                    return;
                }
            }

            if (lastErrorSended == null) {
                lastErrorSended = getDbLayer().getSystemResult(sm.getId(), notification2send.getId());
                if (lastErrorSended == null) {
                    lastErrorSended = getDbLayer().createSystemResult(sm, notification2send);
                }
            }
            lastErrorSended.setCurrentNotification(lastErrorSended.getCurrentNotification() + 1);
            sm.setCurrentNotification(sm.getCurrentNotification() + 1);

            if (notification.getOrderEndTime() != null) {
                if (lastErrorSended != null) {
                    if (lastErrorSended.getCurrentNotification() >= notifications) {
                        maxNotifications = true;
                    }
                }
            }

        }// notifyOnError
        else {// success
            lastErrorSended = null;
            if (notification.getOrderEndTime() == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip]notification orderEndTime is null", method, notifyMsg, serviceName));
                }
                return;
            } else {
                if (jcn.getLastStepForNotification().getError()) {
                    counter.addSkip();
                    closeSystemNotification(sm, notification.getOrderEndTime());
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[%s][%s][skip][close][lastStepForNotification ends with error]orderEndTime is not null", method,
                                notifyMsg, serviceName));
                    }
                    return;
                }
            }
            notification2send = jcn.getLastStepForNotification();

            if (hasReturnCodes && !checkDoNotifyByReturnCodes(notification2send, serviceName, notifyMsg, jobChain.getName(), returnCodeFrom,
                    returnCodeTo)) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][%s][skip][checkDoNotifyByReturnCodes=false][%s]", method, notifyMsg, serviceName,
                            NotificationModel.toString(notification2send)));
                }
                // close
                return;
            }

            sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            if (sm.getCurrentNotification() >= notifications) {
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
            // sm.setCurrentNotification(sm.getCurrentNotification() + 1);
            sm.setMaxNotifications(maxNotifications);
            sm.setNotifications(notifications);
            sm.setModified(DBLayer.getCurrentDateTime());
            sm.setSuccess(!notifyOnError);
            sm.setRecovered(recovered);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(sm)));
                LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(
                        notification2send)));
                if (lastErrorSended != null) {
                    LOGGER.debug(String.format("%s[%s][%s][isNew=%s][send]%s", method, notifyMsg, serviceName, isNew, NotificationModel.toString(
                            lastErrorSended)));
                }
            }

            ISystemNotifierPlugin pl = jobChain.getMonitor().getPluginObject();

            String jobChainInfo = notification2send.getJobChainName() + "-" + notification2send.getOrderId();

            LOGGER.info("----------------------------------------------------------------");
            LOGGER.info(String.format("[executeNotifyJobChain][%s][%s][%s]notification %s of %s. call plugin %s", notifyMsg, serviceName,
                    jobChainInfo, sm.getCurrentNotification(), sm.getNotifications(), pl.getClass().getSimpleName()));
            pl.init(jobChain.getMonitor());
            pl.notifySystem(this.getSpooler(), this.options, getDbLayer(), notification2send, sm, null, serviceStatus, serviceMessagePrefix);

            getDbLayer().getConnection().beginTransaction();

            getDbLayer().getConnection().update(sm);
            if (lastErrorSended != null) {
                if (lastErrorSended.getId() == null) {
                    getDbLayer().getConnection().save(lastErrorSended);
                } else {
                    getDbLayer().getConnection().update(lastErrorSended);
                }
            }

            getDbLayer().getConnection().commit();
            counter.addSuccess();
            LOGGER.info("----------------------------------------------------------------");
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
                // no exception handling
            }
            LOGGER.warn(String.format(THREE_PARAMS_LOGGING, method, notifyMsg, serviceName, ex.getMessage()), ex);
            counter.addError();
        }
    }

    private void insertDummySysNotification(String systemId, Long notificationId) {
        String method = "insertDummySysNotification";
        try {

            DBItemSchedulerMonSystemNotifications sm = getDbLayer().createDummySystemNotification(systemId, notificationId);

            getDbLayer().getConnection().beginTransaction();
            getDbLayer().deleteDummySystemNotification(systemId);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][objectType=%s][systemId=%s]", method, sm.getObjectType(), sm.getSystemId()));
            }

            getDbLayer().getConnection().save(sm);
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            LOGGER.warn(String.format("[%s]%s", method, ex.toString()));
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
            LOGGER.info(String.format("[%s][skip]found 0 Timer definitions", method));
        }
    }

    private void notifyTimer(String systemId, ArrayList<ElementNotificationTimerRef> timersOnSuccess,
            ArrayList<ElementNotificationTimerRef> timersOnError) throws Exception {
        String method = "notifyTimer";
        List<DBItemSchedulerMonChecks> result = getDbLayer().getChecksForNotifyTimer(largeResultFetchSize);
        LOGGER.info(String.format(
                "[%s]found %s \"service_name_on_success\" and %s \"service_name_on_error\" timer definitions and %s checks for timers in the db",
                method, timersOnSuccess.size(), timersOnError.size(), result.size()));
        initSendCounters();
        int currentCounter = 0;
        for (DBItemSchedulerMonChecks check : result) {
            currentCounter++;
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]check timers for \"service_name_on_success\"", method, currentCounter));
            }
            for (int i = 0; i < timersOnSuccess.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnSuccess.get(i);
                if (checkDoNotifyTimer(currentCounter, check, t)) {
                    executeNotifyTimer(systemId, check, t, false);
                } else {
                    counter.addSkip();
                }
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]check timers for \"service_name_on_error\"", method, currentCounter));
            }
            for (int i = 0; i < timersOnError.size(); i++) {
                counter.addTotal();
                ElementNotificationTimerRef t = timersOnError.get(i);
                if (checkDoNotifyTimer(currentCounter, check, t)) {
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

        handledByNotifyAgain = new ArrayList<Long>();

        List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications4NotifyAgain(systemId);
        LOGGER.info(String.format("[%s]found=%s", method, result.size()));

        initSendCounters();
        int c = 0;
        for (DBItemSchedulerMonSystemNotifications systemNotification : result) {
            c++;
            counter.addTotal();

            String notifyMsg = systemNotification.getSuccess() ? "notifyOnSuccess" : "notifyOnError";
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", c, method, notifyMsg, NotificationModel.toString(systemNotification)));
            }
            if (!systemNotification.getCheckId().equals(new Long(0))) {
                // timer
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]is timer system notifier", c, method, notifyMsg));
                }
                counter.addSkip();
                continue;
            }

            DBItemSchedulerMonNotifications notification = getDbLayer().getNotification(systemNotification.getNotificationId());
            if (notification == null) {
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]not foud notification", c, method, notifyMsg));
                }
                continue;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s]%s", c, method, notifyMsg, NotificationModel.toString(notification)));
            }

            if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN)) {
                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                for (int i = 0; i < monitorJobChains.size(); i++) {
                    // counter.addTotal();
                    ElementNotificationJobChain jc = monitorJobChains.get(i);
                    if (checkDoNotify(c, notification, jc)) {
                        String serviceNameOnError = StringUtils.isEmpty(jc.getMonitor().getServiceNameOnError()) ? "" : jc.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = StringUtils.isEmpty(jc.getMonitor().getServiceNameOnSuccess()) ? "" : jc.getMonitor()
                                .getServiceNameOnSuccess();
                        if (systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess)) {
                            executeNotifyJobChain(c, systemNotification, systemId, notification, jc);
                        } else {

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][%s][skip]service names not match", c, method, notifyMsg));
                            }

                            // counter.addSkip();
                        }
                    } else {
                        // counter.addSkip();
                    }
                }
                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore) && systemNotification.getStepToEndTime() != null) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the JobChain configuration
                    // was changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            } else if (systemNotification.getObjectType().equals(DBLayer.NOTIFICATION_OBJECT_TYPE_JOB)) {
                Long currentNotificationBefore = systemNotification.getCurrentNotification();
                for (int i = 0; i < monitorJobs.size(); i++) {
                    // counter.addTotal();
                    ElementNotificationJob job = monitorJobs.get(i);
                    if (checkDoNotify(c, notification, job)) {
                        String serviceNameOnError = StringUtils.isEmpty(job.getMonitor().getServiceNameOnError()) ? "" : job.getMonitor()
                                .getServiceNameOnError();
                        String serviceNameOnSuccess = StringUtils.isEmpty(job.getMonitor().getServiceNameOnSuccess()) ? "" : job.getMonitor()
                                .getServiceNameOnSuccess();

                        if ((systemNotification.getServiceName().equalsIgnoreCase(serviceNameOnError) || systemNotification.getServiceName()
                                .equalsIgnoreCase(serviceNameOnSuccess))) {
                            executeNotifyJob(c, systemNotification, systemId, notification, job);
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s][%s][skip]service names not match", method, c));
                            }
                            // counter.addSkip();
                        }
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][%s][skip]checkDoNotify=false", method, c));
                        }
                        // counter.addSkip();
                    }
                }
                Long currentNotificationAfter = systemNotification.getCurrentNotification();
                if (currentNotificationAfter.equals(currentNotificationBefore)) {
                    // LOGGER.debug(String.format("%s: [%s] disable notify again (system notification(id = %s) was not sent. maybe the Job configuration was
                    // changed)",method,systemId,systemNotification.getId()));
                    // setMaxNotifications(false,systemNotification);
                }

            } else {
                // dummy for max notification
                counter.addSkip();
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][%s][%s][skip]dummy notification...", c, method, notifyMsg));
                }
            }
        }

        LOGGER.info(String.format("[%s]sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
                .getSkip(), counter.getTotal()));

    }

    private void notifyNew(String systemId) throws Exception {
        String method = "notifyNew";

        Long maxNotificationId = new Long(0);

        List<DBItemSchedulerMonNotifications> result = getDbLayer().getNotifications4NotifyNew(systemId);
        LOGGER.info(String.format("[%s]found=%s", method, result.size()));
        initSendCounters();

        ArrayList<String> checkedJobchans = new ArrayList<String>();
        int c = 0;
        for (DBItemSchedulerMonNotifications notification : result) {
            c++;
            counter.addTotal();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s]%s", method, c, NotificationModel.toString(notification)));
            }

            String identifier = notification.getOrderHistoryId().toString();

            if (notification.getId() > maxNotificationId) {
                maxNotificationId = notification.getId();
            }

            if (notification.getStep().equals(new Long(1))) {// only for 1st
                                                             // step
                if (!checkedJobchans.contains(identifier)) {
                    checkedJobchans.add(identifier);
                    for (int i = 0; i < monitorJobChains.size(); i++) {
                        // counter.addTotal();

                        ElementNotificationJobChain jc = monitorJobChains.get(i);
                        if (checkDoNotify(c, notification, jc)) {
                            executeNotifyJobChain(c, null, systemId, notification, jc);
                        } else {
                            // counter.addSkip();
                        }
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][step=1]order history id=%s already processed", method, c, notification.getStep(),
                                identifier));
                    }
                }
            } else {
                if (checkedJobchans.contains(identifier)) {
                    counter.addSkip();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][skip][step %s > 1]order history id=%s already processed", method, c, notification
                                .getStep(), identifier));
                    }

                } else {
                    checkedJobchans.add(identifier);

                    DBItemSchedulerMonNotifications n = null;
                    for (int i = 0; i < monitorJobChains.size(); i++) {
                        // counter.addTotal();

                        ElementNotificationJobChain jc = monitorJobChains.get(i);
                        if (checkDoNotify(c, notification, jc)) {
                            if (n == null) {
                                n = getDbLayer().getNotificationFirstStep(notification);
                            }
                            if (n == null) {
                                counter.addSkip();
                                LOGGER.info(String.format("[%s][%s][skip][!!!first step not found in the database]%s", method, c, NotificationModel
                                        .toString(notification)));
                                break;
                            } else {
                                executeNotifyJobChain(c, null, systemId, n, jc);
                            }
                        } else {
                            // counter.addSkip();
                        }
                    }
                }
            }

            for (int i = 0; i < monitorJobs.size(); i++) {
                // counter.addTotal();

                ElementNotificationJob job = monitorJobs.get(i);
                if (checkDoNotify(c, notification, job)) {
                    executeNotifyJob(c, null, systemId, notification, job);
                } else {
                    // counter.addSkip();
                }
            }
        }

        LOGGER.info(String.format("[%s]sended=%s, error=%s, skipped=%s (total checked=%s)", method, counter.getSuccess(), counter.getError(), counter
                .getSkip(), counter.getTotal()));

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
