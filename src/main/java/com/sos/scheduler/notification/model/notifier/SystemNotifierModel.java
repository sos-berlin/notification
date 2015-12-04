package com.sos.scheduler.notification.model.notifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.sos.scheduler.notification.helper.ElementNotificationJobChain;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.ElementNotificationTimerRef;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;

public class SystemNotifierModel extends NotificationModel implements INotificationModel {

    final Logger logger = LoggerFactory.getLogger(SystemNotifierModel.class);
    private Spooler spooler;

    private SystemNotifierJobOptions options;
    private String systemId;
    private File systemFile;

    private ArrayList<ElementNotificationJobChain> monitorOnErrorJobChains;
    private ArrayList<ElementNotificationJobChain> monitorOnSuccessJobChains;
    private ArrayList<ElementNotificationTimerRef> monitorOnErrorTimers;
    private ArrayList<ElementNotificationTimerRef> monitorOnSuccessTimers;

    private Optional<Integer> largeResultFetchSize = Optional.empty();
    private CounterSystemNotifier counter;

    public SystemNotifierModel(SOSHibernateConnection conn, SystemNotifierJobOptions opt, Spooler sp) throws Exception {

        super(conn);
        options = opt;
        spooler = sp;
        
        try{
            int fetchSize = options.large_result_fetch_size.value();
            if(fetchSize != -1){
                largeResultFetchSize = Optional.of(fetchSize);
            }
        }
        catch(Exception ex){}
    }

    private void initMonitorObjects() {
        monitorOnErrorJobChains = new ArrayList<ElementNotificationJobChain>();
        monitorOnSuccessJobChains = new ArrayList<ElementNotificationJobChain>();
        monitorOnErrorTimers = new ArrayList<ElementNotificationTimerRef>();
        monitorOnSuccessTimers = new ArrayList<ElementNotificationTimerRef>();
    }

    private void initSendCounters() {
        counter = new CounterSystemNotifier();
    }

    private void initConfig() throws Exception {
        String method = "initConfig";

        File schemaFile = new File(options.schema_configuration_file.Value());
        if (!schemaFile.exists()) {
            throw new Exception(String.format("%s: schema file not found: %s", method, schemaFile.getCanonicalPath()));
        }

        systemFile = new File(this.options.system_configuration_file.Value());
        if (!systemFile.exists()) {
            throw new Exception(String.format("%s: system configuration file not found: %s", method, systemFile.getCanonicalPath()));
        }

        logger.debug(String.format("%s: read configuration file %s", method, systemFile.getCanonicalPath()));

        SOSXMLXPath xpath = new SOSXMLXPath(systemFile.getCanonicalPath());

        initMonitorObjects();

        systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
        if (SOSString.isEmpty(systemId)) {
            throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
        }
        logger.info(String.format("%s: system id = %s (%s)", method, systemId, systemFile.getCanonicalPath()));

        NodeList monitorsOnError = NotificationXmlHelper.selectNotificationMonitorOnErrorDefinitions(xpath);
        setMonitorObjects(xpath, monitorsOnError, monitorOnErrorJobChains, monitorOnErrorTimers);

        NodeList monitorsOnSuccess = NotificationXmlHelper.selectNotificationMonitorOnSuccessDefinitions(xpath);
        setMonitorObjects(xpath, monitorsOnSuccess, monitorOnSuccessJobChains, monitorOnSuccessTimers);
    }

    private void setMonitorObjects(SOSXMLXPath xpath, NodeList monitors, ArrayList<ElementNotificationJobChain> jobChains,
            ArrayList<ElementNotificationTimerRef> timers) throws Exception {

        for (int i = 0; i < monitors.getLength(); i++) {
            Node n = monitors.item(i);

            ElementNotificationMonitor monitor = new ElementNotificationMonitor(n);
            NodeList objects = NotificationXmlHelper.selectNotificationMonitorNotificationObjects(xpath, n);
            for (int j = 0; j < objects.getLength(); j++) {
                Node object = objects.item(j);
                if (object.getNodeName().equalsIgnoreCase("JobChain")) {
                    jobChains.add(new ElementNotificationJobChain(monitor, object));
                } else if (object.getNodeName().equalsIgnoreCase("TimerRef")) {
                    timers.add(new ElementNotificationTimerRef(monitor, object));
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
        Long maxNotifications = timer.getNotifications();
        if (maxNotifications < 1) {
            counter.addSkip();
            logger.debug(String.format("%s: serviceName = %s. skip notify timer (maxNotifications is %s): check.id = %s, schedulerId = %s, jobChain = %s", method, serviceName, maxNotifications, check.getId(), check.getSchedulerId(), check.getJobChain()));

            return;
        }

        DBItemSchedulerMonSystemNotifications sm = null;
        DBItemSchedulerMonSystemNotifications smNotTimer = null;
        boolean isNew = false;

        if (timer.getNotifyOnError()) {
            sm = getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), check.getId(), stepFrom, stepTo);
        } else {
            List<DBItemSchedulerMonSystemNotifications> result = getDbLayer().getSystemNotifications(systemId, serviceName, notification.getId());
            logger.debug(String.format("%s: found %s system notifications in the db for systemId = %s, serviceName = %s, notificationId = %s)", method, result.size(), systemId, serviceName, notification.getId()));

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
            if (smNotTimer.getNotifications().equals(new Long(0)) || (smNotTimer.getNotifications() > 0 && smNotTimer.getRecovered())) {

            } else {
                counter.addSkip();
                logger.debug(String.format("%s: serviceName = %s. skip notify timer(notification has the error): smNotTimer.id = %s, smNotTimer.recovered = %s, smNotTimer.notifications = %s", method, serviceName, smNotTimer.getId(), smNotTimer.getRecovered(), smNotTimer.getNotifications()));
                return;
            }
        }

        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), check.getId(), stepFrom, stepTo, notification.getOrderStartTime(), notification.getOrderEndTime(), new Long(0), false, false, true);
        }

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getMaxNotifications()));
            return;
        }

        if (sm.getAcknowledged()) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify timer (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getAcknowledged()));
            return;
        }

        if (sm.getNotifications() >= maxNotifications) {
            setMaxNotifications(isNew, sm);
            counter.addSkip();
            // @TODO maybe remove SystemNotification
            logger.debug(String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, notifications = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications()));
            return;
        }

        try {
            sm.setNotifications(sm.getNotifications() + 1);
            sm.setSuccess(true);
            sm.setModified(DBLayer.getCurrentDateTime());

            if (isNew) {
                logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            } else {
                logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
            }

            ISystemNotifierPlugin pl = timer.getMonitor().getPluginObject();
            logger.info(String.format("%s: --- ", method));
            logger.info(String.format("%s: call plugin %s. notification %s of %s to service \"%s\"", method, pl.getClass().getSimpleName(), sm.getNotifications(), maxNotifications, sm.getServiceName()));
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
            }

            logger.warn(String.format("%s: %s - %s", method, serviceName, ex.getMessage()));
            counter.addError();
        }
    }

    private void setMaxNotifications(boolean isNew, DBItemSchedulerMonSystemNotifications sm) throws Exception {
        sm.setMaxNotifications(true);
        sm.setModified(DBLayer.getCurrentDateTime());

        getDbLayer().getConnection().beginTransaction();
        if (isNew) {
            getDbLayer().getConnection().save(sm);
        } else {
            getDbLayer().getConnection().update(sm);
        }
        getDbLayer().getConnection().commit();
    }

    private void executeNotifySuccess(String systemId, DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "executeNotifySuccess";

        String serviceName = jc.getMonitor().getServiceNameOnSuccess();
        Long checkId = new Long(0);
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();
        Long maxNotifications = jc.getNotifications();
        boolean isNew = false;

        if (maxNotifications < 1) {
            counter.addSkip();
            logger.debug(String.format("%s: serviceName = %s. skip notify success (maxNotifications is %s): notification.id = %s, schedulerId = %s, jobChain = %s", method, serviceName, maxNotifications, notification.getId(), notification.getSchedulerId(), notification.getJobChain()));

            return;
        }

        DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo);

        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo, notification.getOrderStartTime(), notification.getOrderEndTime(), new Long(0), false, false, true);
        }

        logger.debug(String.format("%s: %s. id = %s, notificationId = %s, stepFrom = %s, stepTo = %s, notifications = %s, maxNotifications = %s, acknowledged = %s, recovered = %s, success = %s", method, isNew ? "new system notification"
                : "old system notification", sm.getId(), sm.getNotificationId(), sm.getStepFrom(), sm.getStepTo(), sm.getNotifications(), sm.getMaxNotifications(), sm.getAcknowledged(), sm.getRecovered(), sm.getSuccess()));

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            String msg = sm.getNotifications().equals(IDENTIFIER_NOTIFICATION_NOT_SEND) ? "notification was skipped"
                    : "count notifications was reached";
            logger.debug(String.format("%s: skip notify success (%s): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s", method, msg, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getMaxNotifications()));
            return;
        }

        if (sm.getAcknowledged()) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify success (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getAcknowledged()));
            return;
        }

        if (sm.getNotifications() >= maxNotifications) {

            setMaxNotifications(isNew, sm);
            counter.addSkip();
            // @TODO evtl an dieser Stelle SystemNotification löschen
            logger.debug(String.format("%s: skip notify success (count notifications was reached): id = %s, , serviceName = %s, notifications = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications()));
            return;
        }

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;

        // Handlung of stepFrom, stepTo, excludedSteps
        boolean doNotify = true;
        DBItemSchedulerMonNotifications lastStepForNotification = null;

        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)
                || jc.getExcludedSteps().size() > 0) {
            doNotify = false;

            DBItemSchedulerMonNotifications lastNotification = null;
            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = getDbLayer().getOrderNotifications(largeResultFetchSize,notification.getOrderHistoryId());
            if (steps == null || steps.size() == 0) {
                throw new Exception(String.format("%s: no steps found for orderHistoryId = %s", method, notification.getOrderHistoryId()));
            }
            logger.debug(String.format("%s: steps size = %s for orderHistoryId = %s", method, steps.size(), notification.getOrderHistoryId()));

            for (DBItemSchedulerMonNotifications step : steps) {
                if (stepFrom != null && step.getOrderStepState().equalsIgnoreCase(stepFrom) && stepFromIndex.equals(new Long(0))) {
                    stepFromIndex = step.getStep();
                    stepFromNotification = step;
                }
                if (stepTo != null && step.getOrderStepState().equalsIgnoreCase(stepTo)) {
                    stepToIndex = step.getStep();
                    stepToNotification = step;
                }
                lastNotification = step;
            }

            if (stepToIndex.equals(new Long(0))) {
                stepToIndex = lastNotification.getStep();
            }

            for (DBItemSchedulerMonNotifications step : steps) {
                if (step.getStep() >= stepFromIndex && step.getStep() <= stepToIndex) {
                    if (!step.getError()) {
                        lastStepForNotification = step;
                        doNotify = true;
                    }
                }
            }

            if (doNotify && lastStepForNotification != null) {
                if (jc.getExcludedSteps().contains(lastStepForNotification.getOrderStepState())) {
                    doNotify = false;
                    counter.addSkip();
                    logger.info(String.format("%s: step = %s is configured as excluded.  SKIP create and do notify system: notificationId = %s, systemId = %s, serviceName = %s. ", method, lastStepForNotification.getOrderStepState(), notification.getId(), sm.getSystemId(), sm.getServiceName()));
                }
            }
        } else {
            logger.debug(String.format("%s:  find lastStepForNotification for orderHistoryId = %s. notificationId = %s, systemId = %s. ", method, notification.getOrderHistoryId(), notification.getId(), systemId));

            lastStepForNotification = getDbLayer().getOrderLastStep(largeResultFetchSize, notification, true);
            if (lastStepForNotification == null) {
                throw new Exception(String.format("lastStepForNotification is NULL"));
            } else {
                if (lastStepForNotification.getError()) {
                    doNotify = false;

                    counter.addSkip();
                    logger.debug(String.format("%s: skip notify success (last step has error): lastStep = %s, lastStepNotificationId = %s, lastStepError = %s, serviceName = %s", method, lastStepForNotification.getOrderStepState(), lastStepForNotification.getId(), lastStepForNotification.getErrorText(), systemId));
                }

            }
        }

        if (doNotify) {
            try {
                if (lastStepForNotification == null) {
                    throw new Exception(String.format("lastStepForNotification is NULL"));
                }

                if (stepFromNotification != null) {
                    sm.setStepFromStartTime(stepFromNotification.getOrderStepStartTime());
                }
                if (stepToNotification != null) {
                    sm.setStepToEndTime(stepToNotification.getOrderStepEndTime());
                }

                sm.setNotifications(sm.getNotifications() + 1);
                sm.setSuccess(true);
                sm.setModified(DBLayer.getCurrentDateTime());

                if (isNew) {
                    logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
                } else {
                    logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
                }

                logger.debug(String.format("%s: lastStepForNotification: id = %s, orderHistoryId = %s, step = %s, orderStepState = %s", method, lastStepForNotification.getId(), lastStepForNotification.getOrderHistoryId(), lastStepForNotification.getStep(), lastStepForNotification.getOrderStepState()));

                ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
                logger.info(String.format("%s: --- ", method));
                logger.info(String.format("%s: call plugin %s. notification %s of %s to service \"%s\"", method, pl.getClass().getSimpleName(), sm.getNotifications(), maxNotifications, sm.getServiceName()));
                pl.init(jc.getMonitor());
                pl.notifySystem(getSpooler(), options, getDbLayer(), lastStepForNotification, sm, null, EServiceStatus.OK, null);

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
                }
                logger.warn(String.format("%s: %s - %s", method, serviceName, ex.getMessage()));
                counter.addError();
            }
        } else {
            if (isNew) {
                handleSkippedNewSystemNotification(sm);
            }
        }
    }

    private void handleSkippedNewSystemNotification(DBItemSchedulerMonSystemNotifications sm) {
        String method = "handleSkippedNewSystemNotification";
        try {
            logger.debug(String.format("%s: sending skipped. create and close system notification. notificationId = %s, stepFrom = %s, stepTo = %s, notifications = %s, maxNotifications = %s, acknowledged = %s, recovered = %s, success = %s", method, sm.getNotificationId(), sm.getStepFrom(), sm.getStepTo(), sm.getNotifications(), sm.getMaxNotifications(), sm.getAcknowledged(), sm.getRecovered(), sm.getSuccess()));

            getDbLayer().getConnection().beginTransaction();
            sm.setNotifications(IDENTIFIER_NOTIFICATION_NOT_SEND);
            sm.setMaxNotifications(true);
            sm.setModified(DBLayer.getCurrentDateTime());
            getDbLayer().getConnection().save(sm);
            getDbLayer().getConnection().commit();
        } catch (Exception ex) {
            try {
                getDbLayer().getConnection().rollback();
            } catch (Exception e) {
            }
            logger.debug(String.format("%s: %s - %s", method, sm.getServiceName(), ex.toString()));
        }
    }

    private void notifySuccess(String systemId, ArrayList<ElementNotificationJobChain> jobChains) throws Exception {
        String method = "notifySuccess";

        List<DBItemSchedulerMonNotifications> result = this.getDbLayer().getNotificationsForNotifySuccess(largeResultFetchSize);
        logger.info(String.format("%s: found %s \"service_name_on_success\" definitions and %s notifications for success in the db", method, jobChains.size(), result.size()));

        initSendCounters();

        for (DBItemSchedulerMonNotifications notification : result) {
            for (int i = 0; i < jobChains.size(); i++) {
                counter.addTotal();
                ElementNotificationJobChain jc = jobChains.get(i);
                if (checkDoNotification(notification, jc)) {
                    executeNotifySuccess(systemId, notification, jc);
                } else {
                    counter.addSkip();
                }
            }
        }

        logger.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));

    }

    private boolean checkDoNotificationTimer(DBItemSchedulerMonChecks check, ElementNotificationTimerRef timer) {
        String method = "  checkDoNotificationTimer";

        boolean notify = true;

        String ref = timer.getRef();
        if (!check.getName().equals(ref)) {
            notify = false;
        }

        logger.debug(String.format("%s: %s. check db(name = %s) and configured(ref = %s)", method, notify ? "ok" : "skip", check.getName(), ref));

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
        if (notify) {
            if (!jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
                try {
                    if (!notification.getJobChainName().matches(jobChain)) {
                        notify = false;
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("%s: check with configured scheduler_id = %s, name = %s: %s", method, schedulerId, jobChain, ex));
                }
            }
        }

        logger.debug(String.format("%s: %s. do check db(schedulerId = %s, jobChain = %s) and configured(schedulerId = %s, jobChain = %s)", method, notify ? "ok"
                : "skip", notification.getSchedulerId(), notification.getJobChainName(), schedulerId, jobChain));
        return notify;
    }

    /** Step independent
     * 
     * @param notification
     * @param jc
     * @throws Exception */
    private void executeNotifyRecovered(String systemId, DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc)
            throws Exception {
        // Output indent
        String method = "  executeNotifyRecovered";

        String serviceName = jc.getMonitor().getServiceNameOnError();
        Long checkId = new Long(0);
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();
        Long maxNotifications = jc.getNotifications();
        boolean isNew = false;

        if (maxNotifications < 1) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify recovered (configured maxNotifications is %s): serviceName = %s, notification.id = %s, schedulerId = %s, jobChain = %s", method, maxNotifications, serviceName, notification.getId(), notification.getSchedulerId(), notification.getJobChain()));
            return;
        }

        DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo);

        if (sm == null) {
            isNew = true;
            counter.addSkip();
            logger.debug(String.format("%s: skip notify recovered (system notification not found): systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s, excludedSteps = %s", method, systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo, jc.getExcludedStepsAsString()));
            return;
        }

        if (sm.getRecovered()) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify recovered (is already recovered): id = %s, systemId = %s, serviceName = %s, recovered = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getRecovered()));
            return;
        }

        if (sm.getNotifications().equals(new Long(0))) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify recovered (system notification was not sended): id = %s, systemId = %s, serviceName = %s, notifications = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getNotifications()));
            return;
        }

        try {
            sm.setNotifications(sm.getNotifications() + 1);
            sm.setRecovered(true);
            sm.setModified(DBLayer.getCurrentDateTime());

            ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
            logger.info(String.format("%s: --- ", method));
            logger.info(String.format("%s: call plugin %s. recovery to service \"%s\"", method, pl.getClass().getSimpleName(), sm.getServiceName()));
            pl.init(jc.getMonitor());
            pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), notification, sm, null, EServiceStatus.OK, EServiceMessagePrefix.RECOVERED);

            logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, stepFrom = %s, stepTo = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getStepFrom(), sm.getStepTo()));

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
            }
            logger.warn(String.format("%s: %s - %s", method, serviceName, ex.getMessage()));
            counter.addError();
        }

    }

    private void notifyRecovered(String systemId, ArrayList<ElementNotificationJobChain> jobChains) throws Exception {
        String method = "notifyRecovered";

        List<DBItemSchedulerMonNotifications> result = this.getDbLayer().getNotificationsForNotifyRecovered(largeResultFetchSize);
        logger.info(String.format("%s: found %s \"service_name_on_error\" definitions and %s notifications for recovery in the db", method, jobChains.size(), result.size()));

        initSendCounters();

        for (DBItemSchedulerMonNotifications notification : result) {
            for (int i = 0; i < jobChains.size(); i++) {
                counter.addTotal();
                ElementNotificationJobChain jc = jobChains.get(i);
                if (this.checkDoNotification(notification, jc)) {
                    this.executeNotifyRecovered(systemId, notification, jc);
                } else {
                    counter.addSkip();
                }
            }
        }

        logger.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));

    }

    private void executeNotifyError(String systemId, DBItemSchedulerMonNotifications notification, ElementNotificationJobChain jc) throws Exception {
        String method = "executeNotifyError";

        String serviceName = jc.getMonitor().getServiceNameOnError();
        Long checkId = new Long(0);
        String stepFrom = jc.getStepFrom();
        String stepTo = jc.getStepTo();
        long maxNotifications = jc.getNotifications();
        boolean isNew = false;

        if (maxNotifications < 1) {
            counter.addSkip();
            logger.debug(String.format("%s: serviceName = %s. skip notify error (maxNotifications is %s): notification.id = %s, schedulerId = %s, jobChain = %s", method, serviceName, maxNotifications, notification.getId(), notification.getSchedulerId(), notification.getJobChain()));
            return;
        }

        DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo);

        if (sm == null) {
            isNew = true;
            sm = this.getDbLayer().createSystemNotification(systemId, serviceName, notification.getId(), checkId, stepFrom, stepTo, notification.getOrderStartTime(), notification.getOrderEndTime(), new Long(0), false, false, false);
        }

        logger.debug(String.format("%s: %s. id = %s, notificationId = %s, stepFrom = %s, stepTo = %s, notifications = %s, maxNotifications = %s, acknowledged = %s, recovered = %s, success = %s", method, isNew ? "new system notification"
                : "old system notification", sm.getId(), sm.getNotificationId(), sm.getStepFrom(), sm.getStepTo(), sm.getNotifications(), sm.getMaxNotifications(), sm.getAcknowledged(), sm.getRecovered(), sm.getSuccess()));

        if (sm.getMaxNotifications()) {
            counter.addSkip();
            String msg = sm.getNotifications().equals(IDENTIFIER_NOTIFICATION_NOT_SEND) ? "notification was skipped"
                    : "count notifications was reached";
            logger.debug(String.format("%s: skip notify error (%s): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s", method, msg, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getMaxNotifications()));
            return;
        }

        if (sm.getAcknowledged()) {
            counter.addSkip();
            logger.debug(String.format("%s: skip notify error (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s", method, sm.getId(), sm.getServiceName(), sm.getNotifications(), sm.getAcknowledged()));
            return;
        }

        if (sm.getNotifications() >= maxNotifications) {
            this.setMaxNotifications(isNew, sm);
            counter.addSkip();
            // @TODO evtl an dieser Stelle SystemNotification löschen
            logger.debug(String.format("%s: skip notify error (count notifications was reached): id = %s, notifications = %s, serviceName = %s", method, sm.getId(), sm.getNotifications(), sm.getServiceName()));
            return;
        }

        DBItemSchedulerMonNotifications stepFromNotification = null;
        DBItemSchedulerMonNotifications stepToNotification = null;

        boolean doNotify = true;
        DBItemSchedulerMonNotifications lastStepForNotification = null;

        // Handlung of stepFrom, stepTo, excludedSteps
        if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) || !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)
                || jc.getExcludedSteps().size() > 0) {
            doNotify = false;

            DBItemSchedulerMonNotifications lastNotification = null;
            Long stepFromIndex = new Long(0);
            Long stepToIndex = new Long(0);
            List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(largeResultFetchSize, notification.getOrderHistoryId());
            if (steps == null || steps.size() == 0) {
                throw new Exception(String.format("%s: serviceName = %s. no steps found for orderHistoryId = %s", method, sm.getServiceName(), notification.getOrderHistoryId()));
            }
            logger.debug(String.format("%s: serviceName = %s. steps size = %s for orderHistoryId = %s", method, sm.getServiceName(), steps.size(), notification.getOrderHistoryId()));

            for (DBItemSchedulerMonNotifications step : steps) {
                if (stepFrom != null && step.getOrderStepState().equalsIgnoreCase(stepFrom) && stepFromIndex.equals(new Long(0))) {

                    stepFromIndex = step.getStep();
                    stepFromNotification = step;
                }
                if (stepTo != null && step.getOrderStepState().equalsIgnoreCase(stepTo)) {

                    stepToIndex = step.getStep();
                    stepToNotification = step;
                }
                lastNotification = step;
            }

            if (stepToIndex.equals(new Long(0))) {
                stepToIndex = lastNotification.getStep();
            }

            for (DBItemSchedulerMonNotifications step : steps) {
                if (step.getStep() >= stepFromIndex && step.getStep() <= stepToIndex) {
                    // max error (last) not recovered - is not important if the
                    // next step ends successfully.
                    // The error was already detected
                    if (step.getError() && !step.getRecovered()) {
                        lastStepForNotification = step;
                        doNotify = true;
                    }
                }
            }

            if (doNotify && lastStepForNotification != null) {
                if (jc.getExcludedSteps().contains(lastStepForNotification.getOrderStepState())) {
                    if (lastStepForNotification.getOrderEndTime() != null
                            && lastStepForNotification.getOrderStepState().equals(lastNotification.getState())) {
                        logger.debug(String.format("%s: serviceName = %s. order is completed and error step state equals config step = %s and this is last order step.  create and do notify system: notificationId = %s, systemId = %s. ", method, sm.getServiceName(), lastStepForNotification.getOrderStepState(), notification.getId(), systemId));
                    } else {
                        doNotify = false;
                        counter.addSkip();
                        logger.info(String.format("%s: serviceName = %s. order is not completed or error step equals config step = %s and this is not last order step.  SKIP create and do notify system: notificationId = %s, systemId = %s. ", method, sm.getServiceName(), lastStepForNotification.getOrderStepState(), notification.getId(), systemId));
                    }
                }
            }
        } else {
            logger.debug(String.format("%s:  find lastStepForNotification for orderHistoryId = %s. notificationId = %s, systemId = %s. ", method, notification.getOrderHistoryId(), notification.getId(), systemId));

            lastStepForNotification = getDbLayer().getOrderLastStepErrorNotification(largeResultFetchSize, notification);
            if (lastStepForNotification == null) {
                doNotify = false;

                counter.addSkip();
                logger.info(String.format("%s: !!!! not found lastStepForNotification for orderHistoryId = %s.  SKIP create and do notify system: notificationId = %s, systemId = %s. ", method, sm.getServiceName(), notification.getOrderHistoryId(), notification.getId(), systemId));
            }
        }

        if (doNotify) {
            try {
                if (lastStepForNotification == null) {
                    throw new Exception(String.format("lastStepForNotification is NULL"));
                }

                if (stepFromNotification != null) {
                    sm.setStepFromStartTime(stepFromNotification.getOrderStepStartTime());
                }
                if (stepToNotification != null) {
                    sm.setStepToEndTime(stepToNotification.getOrderStepEndTime());
                }

                sm.setNotifications(sm.getNotifications() + 1);
                if (sm.getNotifications() >= maxNotifications) {
                    sm.setMaxNotifications(true);
                }
                sm.setModified(DBLayer.getCurrentDateTime());

                if (isNew) {
                    logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
                } else {
                    logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s", method, sm.getId(), sm.getSystemId(), sm.getServiceName(), sm.getNotifications(), sm.getNotificationId(), sm.getCheckId(), sm.getStepFrom(), sm.getStepTo()));
                }

                logger.debug(String.format("%s: lastStepForNotification: id = %s, orderHistoryId = %s, step = %s, orderStepState = %s", method, lastStepForNotification.getId(), lastStepForNotification.getOrderHistoryId(), lastStepForNotification.getStep(), lastStepForNotification.getOrderStepState()));

                ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
                logger.info(String.format("%s: --- ", method));
                logger.info(String.format("%s: call plugin %s. notification %s of %s to service \"%s\"", method, pl.getClass().getSimpleName(), sm.getNotifications(), maxNotifications, sm.getServiceName()));
                pl.init(jc.getMonitor());
                pl.notifySystem(this.getSpooler(), this.options, this.getDbLayer(), lastStepForNotification, sm, null, EServiceStatus.CRITICAL, EServiceMessagePrefix.ERROR);

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
                }
                logger.warn(String.format("%s: %s - %s", method, serviceName, ex.getMessage()));
                counter.addError();
            }
        } else {
            if (isNew) {
                handleSkippedNewSystemNotification(sm);
            }
        }
    }

    private void notifyTimer(String systemId, ArrayList<ElementNotificationTimerRef> timersOnSuccess,
            ArrayList<ElementNotificationTimerRef> timersOnError) throws Exception {
        String method = "notifyTimer";

        List<DBItemSchedulerMonChecks> result = getDbLayer().getChecksForNotifyTimer(largeResultFetchSize);
        logger.info(String.format("%s: found %s \"service_name_on_success\" and %s \"service_name_on_error\" timer definitions and %s checks for timers in the db", method, timersOnSuccess.size(), timersOnError.size(), result.size()));

        initSendCounters();

        for (DBItemSchedulerMonChecks check : result) {
            logger.debug(String.format("%s: notify timer \"service_name_on_success\"", method));

            for (int i = 0; i < timersOnSuccess.size(); i++) {
                counter.addTotal();

                ElementNotificationTimerRef t = timersOnSuccess.get(i);
                if (checkDoNotificationTimer(check, t)) {
                    executeNotifyTimer(systemId, check, t, false);
                } else {
                    counter.addSkip();
                }
            }

            logger.debug(String.format("%s: notify timer \"service_name_on_error\"", method));
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

        logger.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));

    }

    private void notifyError(String systemId, ArrayList<ElementNotificationJobChain> jobChains) throws Exception {
        String method = "notifyError";

        List<DBItemSchedulerMonNotifications> result = getDbLayer().getNotificationsForNotifyError(largeResultFetchSize);
        logger.info(String.format("%s: found %s \"service_name_on_error\" definitions and %s notifications for error in the db", method, jobChains.size(), result.size()));

        initSendCounters();

        for (DBItemSchedulerMonNotifications notification : result) {
            for (int i = 0; i < jobChains.size(); i++) {
                counter.addTotal();
                ElementNotificationJobChain jc = jobChains.get(i);
                if (checkDoNotification(notification, jc)) {
                    executeNotifyError(systemId, notification, jc);
                } else {
                    counter.addSkip();
                }
            }
        }

        logger.info(String.format("%s: sended = %s, error = %s, skipped = %s (total checked = %s)", method, counter.getSuccess(), counter.getError(), counter.getSkip(), counter.getTotal()));

    }

    @Override
    public void process() throws Exception {
        initConfig();

        String method = "process";

        boolean found = false;
        if (monitorOnSuccessJobChains != null && monitorOnSuccessJobChains.size() > 0) {
            found = true;
            notifySuccess(systemId, monitorOnSuccessJobChains);
        } else {
            logger.info(String.format("%s: skip notify success. found 0 \"service_name_on_success\" definitions", method));
        }

        if (monitorOnErrorJobChains != null && monitorOnErrorJobChains.size() > 0) {
            found = true;
            notifyRecovered(systemId, monitorOnErrorJobChains);
            notifyError(systemId, monitorOnErrorJobChains);
        } else {
            logger.info(String.format("%s: skip notify recovery & notify error. found 0 \"service_name_on_error\" definitions", method));
        }

        if (monitorOnSuccessTimers.size() > 0 || monitorOnErrorTimers.size() > 0) {
            found = true;
            notifyTimer(systemId, monitorOnSuccessTimers, monitorOnErrorTimers);
        } else {
            logger.info(String.format("%s: skip notify timer. found 0 timer definitions", method));
        }

        if (!found) {
            throw new Exception(String.format("%s: not found configured \"service_name_on_error\" or \"service_name_on_sucess\" for system id = %s (%s) ", method, systemId, systemFile.getAbsolutePath()));
        }
    }

    public Spooler getSpooler() {
        return spooler;
    }

}
