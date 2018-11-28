package com.sos.scheduler.notification.db;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;
import com.sos.scheduler.notification.model.NotificationModel;

import sos.util.SOSString;

public class DBLayerSchedulerMon extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String FROM_WITH_SPACES = " from ";
    private static final String FROM = "from ";
    private static final String WHERE_N2_EQUALS_N1 = "  where n2.orderHistoryId = n1.orderHistoryId ";
    private static final String OSH = " osh ";
    private static final String DELETE_FROM = "delete from ";
    private static final String H_ID = "h.id";
    private static final String H_CAUSE = "h.cause";
    private static final String H_ENDTIME = "h.endTime";
    private static final String H_JOBNAME = "h.jobName";
    private static final String H_STARTTIME = "h.startTime";
    private static final String H_EXIT_CODE = "h.exitCode";
    private static final String OH_CAUSE = "oh.cause";
    private static final String OH_ENDTIME = "oh.endTime";
    private static final String OH_HISTORYID = "oh.historyId";
    private static final String OH_JOBCHAIN = "oh.jobChain";
    private static final String OH_ORDERID = "oh.orderId";
    private static final String OH_SPOOLERID = "oh.spoolerId";
    private static final String OH_STARTTIME = "oh.startTime";
    private static final String OH_STATE = "oh.state";
    private static final String OH_STATETEXT = "oh.stateText";
    private static final String ORDER_END_TIME_AS = "orderEndTime";
    private static final String ORDER_HISTORY_ID = "orderHistoryId";
    private static final String ORDER_ID = "orderId";
    private static final String ORDER_JOB_CHAIN = "orderJobChain";
    private static final String ORDER_SCHEDULER_ID = "orderSchedulerId";
    private static final String ORDER_STARTTIME = "orderStartTime";
    private static final String ORDER_STATE = "orderState";
    private static final String ORDER_STATE_TEXT = "orderStateText";
    private static final String ORDER_TITLE = "orderTitle";
    private static final String OSH_ENDTIME = "osh.endTime";
    private static final String OSH_ERROR = "osh.error";
    private static final String OSH_ERROR_CODE = "osh.errorCode";
    private static final String OSH_ERROR_TEXT = "osh.errorText";
    private static final String OSH_ID_HISTORYID = "osh.id.historyId";
    private static final String OSH_ID_STEP = "osh.id.step";
    private static final String OSH_STARTTIME = "osh.startTime";
    private static final String OSH_STATE = "osh.state";
    private static final String OSH_TASKID = "osh.taskId";
    private static final String OSH_SCHEDULER_ORDER_HISTORY_DB_ITEM = "osh.schedulerOrderHistoryDBItem";
    private static final String OSH_SCHEDULER_TASK_HISTORY_DB_ITEM = "osh.schedulerTaskHistoryDBItem";
    private static final String SERVICE_NAME = "serviceName";
    private static final String STEP_END_TIME = "stepEndTime";
    private static final String STEP_ERROR = "stepError";
    private static final String STEP_ERROR_CODE = "stepErrorCode";
    private static final String STEP_ERROR_TEXT = "stepErrorText";
    private static final String STEP_HISTORYID = "stepHistoryId";
    private static final String STEP_STARTTIME = "stepStartTime";
    private static final String STEP_STATE = "stepState";
    private static final String STEP_STEP = "stepStep";
    private static final String STEP_TASKID = "stepTaskId";
    private static final String SYSTEM_ID = "systemId";
    private static final String TASK_CAUSE = "taskCause";
    private static final String TASK_END_TIME = "taskEndTime";
    private static final String TASK_ID = "taskId";
    private static final String TASK_EXIT_CODE = "taskExitCode";
    private static final String TASK_JOB_NAME = "taskJobName";
    private static final String TASK_START_TIME = "taskStartTime";
    private static final String UPDATE = "update ";

    public DBLayerSchedulerMon(SOSHibernateConnection conn) {
        super(conn);
    }

    @SuppressWarnings("unchecked")
    public DBItemNotificationSchedulerHistoryOrderStep getNotFinishedOrderStepHistory(Optional<Integer> fetchSize, String schedulerId, Long taskId,
            String state, String jobChain, String orderId) throws Exception {

        try {
            Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class, "osh");
            // join
            cr.createAlias(OSH_SCHEDULER_ORDER_HISTORY_DB_ITEM, "oh");
            cr.createAlias(OSH_SCHEDULER_TASK_HISTORY_DB_ITEM, "h");
            ProjectionList pl = Projections.projectionList();
            // select field list osh
            pl.add(Projections.property(OSH_ID_STEP).as(STEP_STEP));
            pl.add(Projections.property(OSH_ID_HISTORYID).as(STEP_HISTORYID));
            pl.add(Projections.property(OSH_TASKID).as(STEP_TASKID));
            pl.add(Projections.property(OSH_STARTTIME).as(STEP_STARTTIME));
            pl.add(Projections.property(OSH_ENDTIME).as(STEP_END_TIME));
            pl.add(Projections.property(OSH_STATE).as(STEP_STATE));
            pl.add(Projections.property(OSH_ERROR).as(STEP_ERROR));
            pl.add(Projections.property(OSH_ERROR_CODE).as(STEP_ERROR_CODE));
            pl.add(Projections.property(OSH_ERROR_TEXT).as(STEP_ERROR_TEXT));
            // select field list oh
            pl.add(Projections.property(OH_HISTORYID).as(ORDER_HISTORY_ID));
            pl.add(Projections.property(OH_SPOOLERID).as(ORDER_SCHEDULER_ID));
            pl.add(Projections.property(OH_ORDERID).as(ORDER_ID));
            pl.add(Projections.property(OH_CAUSE).as(ORDER_TITLE));
            pl.add(Projections.property(OH_JOBCHAIN).as(ORDER_JOB_CHAIN));
            pl.add(Projections.property(OH_STATE).as(ORDER_STATE));
            pl.add(Projections.property(OH_STATETEXT).as(ORDER_STATE_TEXT));
            pl.add(Projections.property(OH_STARTTIME).as(ORDER_STARTTIME));
            pl.add(Projections.property(OH_ENDTIME).as(ORDER_END_TIME_AS));
            // select field list h
            pl.add(Projections.property(H_ID).as(TASK_ID));
            pl.add(Projections.property(H_JOBNAME).as(TASK_JOB_NAME));
            pl.add(Projections.property(H_CAUSE).as(TASK_CAUSE));
            pl.add(Projections.property(H_STARTTIME).as(TASK_START_TIME));
            pl.add(Projections.property(H_ENDTIME).as(TASK_END_TIME));
            pl.add(Projections.property(H_EXIT_CODE).as(TASK_EXIT_CODE));
            cr.setProjection(pl);
            cr.add(Restrictions.eq(OH_SPOOLERID, schedulerId));
            cr.add(Restrictions.eq(OSH_TASKID, taskId));
            cr.add(Restrictions.eq(OSH_STATE, state));
            cr.add(Restrictions.eq(OH_JOBCHAIN, jobChain));
            cr.add(Restrictions.eq(OH_ORDERID, orderId));
            cr.add(Restrictions.isNull(OSH_ENDTIME));
            cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
            cr.setReadOnly(true);
            if (fetchSize.isPresent()) {
                cr.setFetchSize(fetchSize.get());
            }
            List<DBItemNotificationSchedulerHistoryOrderStep> result = cr.list();
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public void cleanupNotifications(Date date) throws Exception {
        try {
            String method = "cleanupNotifications";

            LOGGER.info(String.format("[%s]delete <= %s", method, DBLayer.getDateAsString(date)));

            StringBuilder sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where created <= :date");
            int count = getConnection().createQuery(sql.toString()).setTimestamp("date", date).executeUpdate();
            LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_NOTIFICATIONS, count));

            String whereNotificationIdNotIn = " where notificationId not in (select id from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + ")";

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_RESULTS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_RESULTS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_CHECKS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_SYSRESULTS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(whereNotificationIdNotIn);
            int countS1 = getConnection().createQuery(sql.toString()).executeUpdate();

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where checkId > 0");
            sql.append(" and checkId not in (select id from " + DBITEM_SCHEDULER_MON_CHECKS + ")");
            int countS2 = getConnection().createQuery(sql.toString()).executeUpdate();
            count = countS1 + countS2;
            LOGGER.info(String.format("[%s][%s]%s", method, TABLE_SCHEDULER_MON_SYSNOTIFICATIONS, count));
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int resetAcknowledged(String systemId, String serviceName) throws Exception {
        try {
            StringBuilder sql = new StringBuilder(UPDATE);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" set acknowledged = 1");
            sql.append(" where lower(systemId) = :systemId");
            if (!SOSString.isEmpty(serviceName)) {
                sql.append(" and serviceName =:serviceName");
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            if (!SOSString.isEmpty(serviceName)) {
                query.setParameter(SERVICE_NAME, serviceName);
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[resetAcknowledged][systemId=%s][serviceName=%s]%s", systemId, serviceName, sql.toString()));
            }

            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public Criteria getSchedulerHistorySteps(Optional<Integer> fetchSize, Date dateFrom, Date dateTo) throws Exception {
        Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class, "osh");
        // join
        cr.createAlias(OSH_SCHEDULER_ORDER_HISTORY_DB_ITEM, "oh");
        cr.createAlias(OSH_SCHEDULER_TASK_HISTORY_DB_ITEM, "h");
        ProjectionList pl = Projections.projectionList();
        // select field list osh
        pl.add(Projections.property(OSH_ID_STEP).as(STEP_STEP));
        pl.add(Projections.property(OSH_ID_HISTORYID).as(STEP_HISTORYID));
        pl.add(Projections.property(OSH_TASKID).as(STEP_TASKID));
        pl.add(Projections.property(OSH_STARTTIME).as(STEP_STARTTIME));
        pl.add(Projections.property(OSH_ENDTIME).as(STEP_END_TIME));
        pl.add(Projections.property(OSH_STATE).as(STEP_STATE));
        pl.add(Projections.property(OSH_ERROR).as(STEP_ERROR));
        pl.add(Projections.property(OSH_ERROR_CODE).as(STEP_ERROR_CODE));
        pl.add(Projections.property(OSH_ERROR_TEXT).as(STEP_ERROR_TEXT));
        // select field list oh
        pl.add(Projections.property(OH_HISTORYID).as(ORDER_HISTORY_ID));
        pl.add(Projections.property(OH_SPOOLERID).as(ORDER_SCHEDULER_ID));
        pl.add(Projections.property(OH_ORDERID).as(ORDER_ID));
        pl.add(Projections.property(OH_CAUSE).as(ORDER_TITLE));
        pl.add(Projections.property(OH_JOBCHAIN).as(ORDER_JOB_CHAIN));
        pl.add(Projections.property(OH_STATE).as(ORDER_STATE));
        pl.add(Projections.property(OH_STATETEXT).as(ORDER_STATE_TEXT));
        pl.add(Projections.property(OH_STARTTIME).as(ORDER_STARTTIME));
        pl.add(Projections.property(OH_ENDTIME).as(ORDER_END_TIME_AS));
        // select field list h
        pl.add(Projections.property(H_ID).as(TASK_ID));
        pl.add(Projections.property(H_JOBNAME).as(TASK_JOB_NAME));
        pl.add(Projections.property(H_CAUSE).as(TASK_CAUSE));
        pl.add(Projections.property(H_STARTTIME).as(TASK_START_TIME));
        pl.add(Projections.property(H_ENDTIME).as(TASK_END_TIME));
        pl.add(Projections.property(H_EXIT_CODE).as(TASK_EXIT_CODE));
        cr.setProjection(pl);
        // where
        if (dateTo != null) {
            cr.add(Restrictions.le(OSH_STARTTIME, dateTo));
            if (dateFrom != null) {
                cr.add(Restrictions.ge(OSH_STARTTIME, dateFrom));
            }
        }
        cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    public Criteria getSchedulerHistoryStep(Optional<Integer> fetchSize, Long historyId, Long step) throws Exception {
        Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class, "osh");
        // join
        cr.createAlias(OSH_SCHEDULER_ORDER_HISTORY_DB_ITEM, "oh");
        cr.createAlias(OSH_SCHEDULER_TASK_HISTORY_DB_ITEM, "h");
        ProjectionList pl = Projections.projectionList();
        // select field list osh
        pl.add(Projections.property(OSH_ID_STEP).as(STEP_STEP));
        pl.add(Projections.property(OSH_ID_HISTORYID).as(STEP_HISTORYID));
        pl.add(Projections.property(OSH_TASKID).as(STEP_TASKID));
        pl.add(Projections.property(OSH_STARTTIME).as(STEP_STARTTIME));
        pl.add(Projections.property(OSH_ENDTIME).as(STEP_END_TIME));
        pl.add(Projections.property(OSH_STATE).as(STEP_STATE));
        pl.add(Projections.property(OSH_ERROR).as(STEP_ERROR));
        pl.add(Projections.property(OSH_ERROR_CODE).as(STEP_ERROR_CODE));
        pl.add(Projections.property(OSH_ERROR_TEXT).as(STEP_ERROR_TEXT));
        // select field list oh
        pl.add(Projections.property(OH_HISTORYID).as(ORDER_HISTORY_ID));
        pl.add(Projections.property(OH_SPOOLERID).as(ORDER_SCHEDULER_ID));
        pl.add(Projections.property(OH_ORDERID).as(ORDER_ID));
        pl.add(Projections.property(OH_CAUSE).as(ORDER_TITLE));
        pl.add(Projections.property(OH_JOBCHAIN).as(ORDER_JOB_CHAIN));
        pl.add(Projections.property(OH_STATE).as(ORDER_STATE));
        pl.add(Projections.property(OH_STATETEXT).as(ORDER_STATE_TEXT));
        pl.add(Projections.property(OH_STARTTIME).as(ORDER_STARTTIME));
        pl.add(Projections.property(OH_ENDTIME).as(ORDER_END_TIME_AS));
        // select field list h
        pl.add(Projections.property(H_ID).as(TASK_ID));
        pl.add(Projections.property(H_JOBNAME).as(TASK_JOB_NAME));
        pl.add(Projections.property(H_CAUSE).as(TASK_CAUSE));
        pl.add(Projections.property(H_STARTTIME).as(TASK_START_TIME));
        pl.add(Projections.property(H_ENDTIME).as(TASK_END_TIME));
        pl.add(Projections.property(H_EXIT_CODE).as(TASK_EXIT_CODE));
        cr.setProjection(pl);
        // where
        cr.add(Restrictions.eq(OSH_ID_HISTORYID, historyId));
        cr.add(Restrictions.eq(OSH_ID_STEP, step));
        cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
        cr.setReadOnly(true);
        if (fetchSize.isPresent()) {
            cr.setFetchSize(fetchSize.get());
        }
        return cr;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getNotificationOrderSteps(Long notificationId) throws Exception {
        try {
            String method = "getNotificationOrderSteps";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
            sql.append(" where exists (");
            sql.append("   select n2.orderHistoryId ");
            sql.append("   from ");
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2");
            sql.append("   where n1.orderHistoryId = n2.orderHistoryId");
            sql.append("   and n2.id = :id ");
            sql.append(" )");
            sql.append(" order by n1.step");

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", notificationId);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotificationOrderSteps][notificationId=%s]%s", notificationId, sql.toString()));
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonResults> getNotificationResults(Long notificationId) throws Exception {
        try {
            String method = "getNotificationResults";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_RESULTS).append(" r");
            sql.append(" where r.notificationId = :id");

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", notificationId);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotificationResults][notificationId=%s]%s", notificationId, sql.toString()));
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonChecks> getSchedulerMonChecksForSetTimer(Optional<Integer> fetchSize) throws Exception {
        try {
            String method = "getSchedulerMonChecksForSetTimer";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where checked = 0");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSchedulerMonChecksForSetTimer]%s", sql.toString()));
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public void setNotificationCheck(DBItemSchedulerMonChecks check, Date stepFromStartTime, Date stepToEndTime, String text, String resultIds)
            throws Exception {
        try {
            check.setStepFromStartTime(stepFromStartTime);
            check.setStepToEndTime(stepToEndTime);
            check.setChecked(true);
            check.setCheckText(text);
            check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
            check.setModified(DBLayer.getCurrentDateTime());
            getConnection().update(check);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public void setNotificationCheckForRerun(DBItemSchedulerMonChecks check, Date stepFromStartTime, Date stepToEndTime, String text,
            String resultIds) throws Exception {
        try {
            check.setStepFromStartTime(stepFromStartTime);
            check.setStepToEndTime(stepToEndTime);
            check.setChecked(false);
            check.setCheckText("1");
            check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
            check.setModified(DBLayer.getCurrentDateTime());
            getConnection().update(check);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int updateUncompletedNotifications(Optional<Integer> fetchSize, Date maxStartTime) throws Exception {
        int result = 0;
        try {
            // Database independent
            SOSHibernateResultSetProcessor notificationProcessor = new SOSHibernateResultSetProcessor(getConnection());
            SOSHibernateResultSetProcessor historyProcessor = new SOSHibernateResultSetProcessor(getConnection());
            Criteria notificationCriteria = getConnection().createCriteria(DBItemSchedulerMonNotifications.class, "n");
            ProjectionList pl = Projections.projectionList();
            pl.add(Projections.property("n.id").as("id"));
            pl.add(Projections.property("n.orderHistoryId").as(ORDER_HISTORY_ID));
            pl.add(Projections.property("n.step").as("step"));
            notificationCriteria.setProjection(pl);
            notificationCriteria.add(Restrictions.isNull("n.orderEndTime"));
            if (maxStartTime != null) {
                notificationCriteria.add(Restrictions.ge("n.orderStartTime", maxStartTime));
            }
            notificationCriteria.setReadOnly(true);
            if (fetchSize.isPresent()) {
                notificationCriteria.setFetchSize(fetchSize.get());
            }
            StringBuilder update = new StringBuilder(UPDATE);
            update.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            update.append(" set taskEndTime = :taskEndTime");
            update.append(" ,orderEndTime = :orderEndTime");
            update.append(" ,orderStepEndTime = :orderStepEndTime");
            update.append(" ,error = :error");
            update.append(" ,errorCode = :errorCode");
            update.append(" ,errorText = :errorText");
            update.append(" ,returnCode = :returnCode");
            update.append(" ,modified = :modified");
            update.append(" where id = :id");
            try {
                ResultSet notificationResultSet = notificationProcessor.createResultSet(notificationCriteria, ScrollMode.FORWARD_ONLY, fetchSize);
                int readCount = 0;
                while (notificationResultSet.next()) {
                    readCount++;
                    flushScrollableResults(readCount);
                    DBItemSchedulerMonNotifications notification = (DBItemSchedulerMonNotifications) notificationProcessor.get();

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[updateUncompletedNotifications][%s]%s", readCount, NotificationModel.toString(notification)));
                    }

                    Criteria historyCriteria = getSchedulerHistoryStep(fetchSize, notification.getOrderHistoryId(), notification.getStep());
                    ResultSet historyResultSet = historyProcessor.createResultSet(DBItemNotificationSchedulerHistoryOrderStep.class, historyCriteria,
                            ScrollMode.FORWARD_ONLY, fetchSize);
                    try {
                        int c = 0;
                        while (historyResultSet.next()) {
                            c++;

                            DBItemNotificationSchedulerHistoryOrderStep osh = (DBItemNotificationSchedulerHistoryOrderStep) historyProcessor.get();

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[updateUncompletedNotifications][%s][%s]%s", readCount, c, NotificationModel.toString(
                                        osh)));
                            }

                            Query q = getConnection().createQuery(update.toString());
                            q.setParameter("id", notification.getId());
                            q.setParameter(TASK_END_TIME, osh.getTaskEndTime());
                            q.setParameter(ORDER_END_TIME_AS, osh.getOrderEndTime());
                            q.setParameter("orderStepEndTime", osh.getStepEndTime());
                            q.setParameter("error", osh.getStepError());
                            q.setParameter("errorCode", osh.getStepErrorCode());
                            q.setParameter("errorText", osh.getStepErrorText());
                            q.setParameter("returnCode", new Long(osh.getTaskExitCode() == null ? 0 : osh.getTaskExitCode()));
                            q.setParameter("modified", DBLayer.getCurrentDateTime());
                            result += q.executeUpdate();

                        }
                    } catch (Exception ex) {
                        throw new Exception(SOSHibernateConnection.getException(ex));
                    } finally {
                        historyProcessor.close();
                    }
                }
            } catch (Exception ex) {
                throw ex;
            } finally {
                try {
                    if (notificationProcessor != null) {
                        notificationProcessor.close();
                    }
                } catch (Exception e) {
                    // no exception handling
                }
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    // TODO not used. to remove
    public int setOrderNotificationsRecovered() throws Exception {
        try {
            StringBuilder sb = new StringBuilder(UPDATE);
            sb.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" mn");
            sb.append(" set mn.recovered = 1");
            sb.append(" where mn.error = 1");
            sb.append(" and exists (");
            sb.append(" select osh.id.historyId from ");
            sb.append(SchedulerOrderStepHistoryDBItem.class.getSimpleName()).append(OSH);
            sb.append("	where mn.orderHistoryId = osh.id.historyId");
            sb.append("		and mn.orderStepState = osh.state");
            sb.append("		and mn.step <= osh.id.step");
            sb.append("		and osh.error = 0");
            sb.append("		and osh.endTime is not null");
            sb.append(")");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[setOrderNotificationsRecovered]%s", sb.toString()));
            }

            Query query = getConnection().createQuery(sb.toString());
            return query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public SchedulerTaskHistoryDBItem getSchedulerHistory(Long taskId) throws Exception {
        try {
            String method = "getSchedulerHistory";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(SchedulerTaskHistoryDBItem.class.getSimpleName());
            sql.append(" where id = :taskId");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSchedulerHistory][taskId=%s]%s", taskId, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setReadOnly(true);
            query.setParameter(TASK_ID, taskId);

            List<SchedulerTaskHistoryDBItem> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId)
            throws Exception {
        try {
            String method = "getNotification";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where schedulerId = :schedulerId");
            sql.append(" and standalone = :standalone");
            sql.append(" and taskId = :taskId");
            sql.append(" and step = :step");
            sql.append(" and orderHistoryId = :orderHistoryId ");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotification][schedulerId=%s][standalone=%s][taskId=%s][step=%s][orderHistoryId=%s]%s", schedulerId,
                        standalone, taskId, step, orderHistoryId, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("schedulerId", schedulerId);
            query.setParameter("standalone", standalone);
            query.setParameter(TASK_ID, taskId);
            query.setParameter("step", step);
            query.setParameter(ORDER_HISTORY_ID, orderHistoryId);

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (result.size() > 0) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotification(Long id) throws Exception {
        try {
            String method = "getNotification";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where id = :id ");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotification][id=%s]%s", id, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("id", id);

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications4NotifyAgain(String systemId, Long objectType) throws Exception {
        try {
            String method = "getSystemNotifications4NotifyAgain";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where lower(systemId) = :systemId");
            sql.append(" and maxNotifications = false");
            sql.append(" and acknowledged = false");
            if (objectType != null) {
                sql.append(" and objectType = :objectType");
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSystemNotifications4NotifyAgain][systemId=%s][objectType=%s]%s", systemId, objectType, sql
                        .toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            if (objectType != null) {
                query.setParameter("objectType", objectType);
            }

            return executeQueryList(method, sql, query);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getNotifications4NotifyNew(String systemId) throws Exception {
        try {
            String method = "getNotifications4NotifyNew";

            List<DBItemSchedulerMonNotifications> result = null;

            StringBuilder sql = new StringBuilder("select sn.notificationId from ");
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS).append(" sn ");
            sql.append("where lower(sn.systemId) = :systemId ");
            sql.append("and objectType = :objectType");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotifications4NotifyNew][systemId=%s]%s", systemId, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            query.setParameter("objectType", DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY);

            Long maxNotificationId = (Long) query.uniqueResult();

            if (maxNotificationId == null || maxNotificationId.equals(new Long(0))) {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getNotifications4NotifyNew]%s", sql.toString()));
                }

                query = getConnection().createQuery(sql.toString());
            } else {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n ");
                sql.append("where n.id > :maxNotificationId");

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[getNotifications4NotifyNew][maxNotificationId=%s]%s", maxNotificationId, sql.toString()));
                }

                query = getConnection().createQuery(sql.toString());
                query.setParameter("maxNotificationId", maxNotificationId);
            }
            result = executeQueryList(method, sql, query);
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications(String systemId, String serviceName, Long notificationId)
            throws Exception {
        try {
            String method = "getSystemNotifications";

            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where notificationId = :notificationId");
            sql.append(" and serviceName = :serviceName ");
            sql.append(" and lower(systemId) = :systemId");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSystemNotifications][systemId=%s][serviceName=%s][notificationId=%s]%s", systemId, serviceName,
                        notificationId, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("notificationId", notificationId);
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            query.setParameter(SERVICE_NAME, serviceName);

            return executeQueryList(method, sql, query);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonSystemNotifications getSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId,
            Long objectType, boolean onSuccess, String stepFrom, String stepTo, String returnCodeFrom, String returnCodeTo) throws Exception {
        try {
            String method = "getSystemNotification";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where notificationId = :notificationId");
            sql.append(" and checkId = :checkId");
            sql.append(" and objectType = :objectType");
            sql.append(" and serviceName = :serviceName");
            sql.append(" and lower(systemId) = :systemId");
            sql.append(" and success = :success");
            if (stepFrom != null) {
                sql.append(" and stepFrom = :stepFrom");
            }
            if (stepTo != null) {
                sql.append(" and stepTo = :stepTo");
            }
            if (returnCodeFrom != null) {
                sql.append(" and returnCodeFrom = :returnCodeFrom");
            }
            if (returnCodeTo != null) {
                sql.append(" and returnCodeTo = :returnCodeTo");
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format(
                        "[getSystemNotifications][systemId=%s][serviceName=%s][notificationId=%s][checkId=%s][objectType=%s][onSuccess=%s][stepFrom=%s][stepTo=%s][returnCodeFrom=%s][returnCodeTo=%s]%s",
                        systemId, serviceName, notificationId, checkId, objectType, onSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo, sql
                                .toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("notificationId", notificationId);
            query.setParameter("checkId", checkId);
            query.setParameter("objectType", objectType);
            query.setParameter(SERVICE_NAME, serviceName);
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());
            query.setParameter("success", onSuccess);
            if (stepFrom != null) {
                query.setParameter("stepFrom", stepFrom);
            }
            if (stepTo != null) {
                query.setParameter("stepTo", stepTo);
            }
            if (returnCodeFrom != null) {
                query.setParameter("returnCodeFrom", returnCodeFrom);
            }
            if (returnCodeTo != null) {
                query.setParameter("returnCodeTo", returnCodeTo);
            }

            List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemNotificationSchedulerVariables getSchedulerVariable() throws Exception {
        String method = "getSchedulerVariable";
        StringBuilder sql = new StringBuilder(FROM);
        sql.append(DBITEM_SCHEDULER_VARIABLES);
        sql.append(" where name = :name");

        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("name", SCHEDULER_VARIABLES_NOTIFICATION);

        List<DBItemNotificationSchedulerVariables> result = executeQueryList(method, sql, query);
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    public Date getLastNotificationDate(DBItemNotificationSchedulerVariables dbItem) throws Exception {
        try {
            String method = "getLastNotificationDate";
            Date lastDate = null;
            try {
                if (dbItem != null) {
                    lastDate = DBLayer.getDateFromString(dbItem.getTextValue());
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s: '%s' cannot be converted to date: value = '%s', %s", method, SCHEDULER_VARIABLES_NOTIFICATION, dbItem
                        .getTextValue(), ex.getMessage()));
            }
            return lastDate;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemSchedulerMonResults createResult(Long notificationId, String name, String value) {
        DBItemSchedulerMonResults dbItem = new DBItemSchedulerMonResults();
        dbItem.setNotificationId(notificationId);
        dbItem.setName(name);
        dbItem.setValue(value);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonChecks> getChecksForNotifyTimer(Optional<Integer> fetchSize) throws Exception {
        try {
            String method = "getChecksForNotifyTimer";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where checked = 1");

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotificationFirstStep(DBItemSchedulerMonNotifications notification) throws Exception {
        try {
            String method = "getNotificationFirstStep";

            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n");
            sql.append(" where n.orderHistoryId = :orderHistoryId");
            sql.append(" and n.step = 1");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotificationFirstStep][orderHistoryId=%s]%s", notification.getOrderHistoryId(), sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(ORDER_HISTORY_ID, notification.getOrderHistoryId());

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotificationsOrderLastStep(Optional<Integer> fetchSize, DBItemSchedulerMonNotifications notification,
            boolean orderCompleted) throws Exception {
        try {
            String method = "getNotificationsOrderLastStep";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
            sql.append(" where n1.orderHistoryId = :orderHistoryId");
            sql.append(" and n1.step = ");
            sql.append(" (select max(n2.step) ");
            sql.append(FROM_WITH_SPACES);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2 ");
            sql.append(WHERE_N2_EQUALS_N1);
            sql.append(" ) ");
            if (orderCompleted) {
                sql.append(" and n1.orderEndTime is not null");
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotificationsOrderLastStep][orderHistoryId=%s]%s", notification.getOrderHistoryId(), sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(ORDER_HISTORY_ID, notification.getOrderHistoryId());
            query.setReadOnly(true);
            if (fetchSize.isPresent()) {
                query.setFetchSize(fetchSize.get());
            }

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonNotifications getNotificationsOrderLastErrorStep(Optional<Integer> fetchSize,
            DBItemSchedulerMonNotifications notification, boolean orderCompleted) throws Exception {
        try {
            String method = "getNotificationsOrderLastStep";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n1");
            sql.append(" where n1.orderHistoryId = :orderHistoryId");
            sql.append(" and n1.step = ");
            sql.append(" (select max(n2.step) ");
            sql.append(FROM_WITH_SPACES);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n2 ");
            sql.append(WHERE_N2_EQUALS_N1);
            sql.append(" and n2.error=1) ");
            if (orderCompleted) {
                sql.append(" and n1.orderEndTime is not null");
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getNotificationsOrderLastStep][orderHistoryId=%s]%s", notification.getOrderHistoryId(), sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(ORDER_HISTORY_ID, notification.getOrderHistoryId());
            query.setReadOnly(true);
            if (fetchSize.isPresent()) {
                query.setFetchSize(fetchSize.get());
            }

            List<DBItemSchedulerMonNotifications> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    @SuppressWarnings("rawtypes")
    private List executeQueryList(String functionName, StringBuilder sql, Query q) throws Exception {
        List result = null;
        try {
            try {
                result = q.list();
            } catch (LockAcquisitionException ex) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[executeQueryList]try rerun %s again in %s. cause exception = %s, sql = %s", functionName,
                            RERUN_TRANSACTION_INTERVAL, ex.getMessage(), sql));
                }
                Thread.sleep(RERUN_TRANSACTION_INTERVAL * 1000);
                result = q.list();
            } catch (Exception ex) {
                throw new Exception(String.format("[%s]%s, sql=%s", functionName, ex.getMessage(), sql), ex);
            }
        } catch (Exception ex) {
            throw new Exception(String.format("[%s]%s, sql=%s", functionName, ex.getMessage(), sql));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<DBItemSchedulerMonNotifications> getOrderNotifications(Optional<Integer> fetchSize, Long orderHistoryId) throws Exception {
        try {
            String method = "getOrderNotifications";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where orderHistoryId = :orderHistoryId");
            sql.append(" order by step");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getOrderNotifications][orderHistoryId=%s]%s", orderHistoryId, sql.toString()));
            }

            Query q = getConnection().createQuery(sql.toString());
            q.setReadOnly(true);
            if (fetchSize.isPresent()) {
                q.setFetchSize(fetchSize.get());
            }
            q.setParameter(ORDER_HISTORY_ID, orderHistoryId);

            return executeQueryList(method, sql, q);
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public int removeCheck(Long checkId) throws Exception {
        try {
            StringBuilder sql = new StringBuilder("delete ");
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(" where id = :id ");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[removeCheck][id=%s]%s", checkId, sql.toString()));
            }

            Query q = getConnection().createQuery(sql.toString());
            q.setParameter("id", checkId);

            return q.executeUpdate();
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemSchedulerMonChecks createCheck(String name, DBItemSchedulerMonNotifications notification, String stepFrom, String stepTo,
            Date stepFromStartTime, Date stepToEndTime) {
        DBItemSchedulerMonChecks item = new DBItemSchedulerMonChecks();
        item.setName(name);
        Long notificationId = notification.getId();
        // NULL wegen batch Insert bei den Datenbanken, die kein Autoincrement
        // haben (Oracle ...)
        if (notificationId == null || notificationId.equals(new Long(0))) {
            notificationId = new Long(0);
            item.setResultIds(notification.getSchedulerId() + ";" + (notification.getStandalone() ? "true" : "false") + ";" + notification.getTaskId()
                    + ";" + notification.getStep() + ";" + notification.getOrderHistoryId());
        }
        item.setNotificationId(notificationId);
        item.setStepFrom(stepFrom);
        item.setStepTo(stepTo);
        item.setStepFromStartTime(stepFromStartTime);
        item.setStepToEndTime(stepToEndTime);
        item.setChecked(false);
        item.setCreated(DBLayer.getCurrentDateTime());
        item.setModified(DBLayer.getCurrentDateTime());
        return item;
    }

    public DBItemSchedulerMonSystemNotifications createSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId,
            String returnCodeFrom, String returnCodeTo, Long objectType, String stepFrom, String stepTo, Date stepFromStartTime, Date stepToEndTime,
            Long currentNotification, Long notifications, boolean acknowledged, boolean recovered, boolean success) {
        DBItemSchedulerMonSystemNotifications dbItem = new DBItemSchedulerMonSystemNotifications();
        dbItem.setSystemId(systemId);
        dbItem.setServiceName(serviceName);
        dbItem.setNotificationId(notificationId);
        dbItem.setCheckId(checkId);
        dbItem.setReturnCodeFrom(returnCodeFrom);
        dbItem.setReturnCodeTo(returnCodeTo);
        dbItem.setObjectType(objectType);
        dbItem.setStepFrom(stepFrom);
        dbItem.setStepTo(stepTo);
        dbItem.setStepFromStartTime(stepFromStartTime);
        dbItem.setStepToEndTime(stepToEndTime);
        dbItem.setMaxNotifications(false);
        dbItem.setCurrentNotification(currentNotification);
        dbItem.setNotifications(notifications);
        dbItem.setAcknowledged(acknowledged);
        dbItem.setRecovered(recovered);
        dbItem.setSuccess(success);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    public void deleteDummySystemNotification(String systemId) throws Exception {
        StringBuffer sql = new StringBuffer(DELETE_FROM);
        sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
        sql.append(" where objectType = :objectType");
        sql.append(" and lower(systemId) = :systemId");

        if (isDebugEnabled) {
            LOGGER.debug(String.format("[deleteDummySystemNotification][objectType=%s][systemId=%s]%s", DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY,
                    systemId, sql.toString()));
        }

        Query query = getConnection().createQuery(sql.toString());
        query.setParameter("objectType", DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY);
        query.setParameter("systemId", systemId.toLowerCase());

        query.executeUpdate();
    }

    public DBItemSchedulerMonSystemNotifications createDummySystemNotification(String systemId, Long notificationId) {

        String serviceName = DBLayer.DEFAULT_EMPTY_NAME;
        Long checkId = new Long(0);
        String returnCodeFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String returnCodeTo = DBLayer.DEFAULT_EMPTY_NAME;
        Long objectType = DBLayer.NOTIFICATION_OBJECT_TYPE_DUMMY;
        String stepFrom = DBLayer.DEFAULT_EMPTY_NAME;
        String stepTo = DBLayer.DEFAULT_EMPTY_NAME;
        Date stepFromStartTime = null;
        Date stepToEndTime = null;
        Long currentNotification = new Long(0);
        Long notifications = new Long(0);
        boolean acknowledged = false;
        boolean recovered = false;
        boolean success = false;

        DBItemSchedulerMonSystemNotifications sm = createSystemNotification(systemId, serviceName, notificationId, checkId, returnCodeFrom,
                returnCodeTo, objectType, stepFrom, stepTo, stepFromStartTime, stepToEndTime, currentNotification, notifications, acknowledged,
                recovered, success);
        sm.setMaxNotifications(true);
        return sm;
    }

    public DBItemSchedulerMonNotifications createNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId,
            String jobChainName, String jobChainTitle, String orderId, String orderTitle, Date orderStartTime, Date orderEndTime,
            String orderStepState, Date orderStepStartTime, Date orderStepEndTime, String jobName, String jobTitle, Date taskStartTime,
            Date taskEndTime, boolean recovered, Long returnCode, boolean error, String errorCode, String errorText) throws Exception {
        DBItemSchedulerMonNotifications dbItem = new DBItemSchedulerMonNotifications();
        // set unique key
        dbItem.setSchedulerId(schedulerId);
        dbItem.setStandalone(standalone);
        dbItem.setTaskId(taskId);
        dbItem.setStep(step);
        dbItem.setOrderHistoryId(orderHistoryId);
        // set others
        dbItem.setJobChainName(jobChainName);
        dbItem.setJobChainTitle(jobChainTitle);
        dbItem.setOrderId(orderId);
        dbItem.setOrderTitle(orderTitle);
        dbItem.setOrderStartTime(orderStartTime);
        dbItem.setOrderEndTime(orderEndTime);
        dbItem.setOrderStepState(orderStepState);
        dbItem.setOrderStepStartTime(orderStepStartTime);
        dbItem.setOrderStepEndTime(orderStepEndTime);
        dbItem.setJobName(jobName);
        dbItem.setJobTitle(jobTitle);
        dbItem.setTaskStartTime(taskStartTime);
        dbItem.setTaskEndTime(taskEndTime);
        dbItem.setRecovered(recovered);
        dbItem.setReturnCode(returnCode);
        dbItem.setError(error);
        dbItem.setErrorCode(errorCode);
        dbItem.setErrorText(errorText);
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonSystemResults getSystemResult(Long sysNotificationId, Long notificationId) throws Exception {
        try {
            String method = "getSystemResult";
            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS);
            sql.append(" where sysNotificationId = :sysNotificationId and ");
            sql.append(" notificationId = :notificationId");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSystemResult][sysNotificationId=%s][notificationId=%s]%s", sysNotificationId, notificationId, sql
                        .toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("sysNotificationId", sysNotificationId);
            query.setParameter("notificationId", notificationId);

            List<DBItemSchedulerMonSystemResults> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public DBItemSchedulerMonSystemResults createSystemResult(DBItemSchedulerMonSystemNotifications sm,
            DBItemSchedulerMonNotifications notification) {
        DBItemSchedulerMonSystemResults dbItem = new DBItemSchedulerMonSystemResults();
        dbItem.setSysNotificationId(sm.getId());
        dbItem.setNotificationId(notification.getId());
        dbItem.setOrderStep(notification.getStep());
        dbItem.setOrderStepState(notification.getOrderStepState());
        dbItem.setRecovered(false);
        dbItem.setCurrentNotification(new Long(0));
        dbItem.setCreated(DBLayer.getCurrentDateTime());
        dbItem.setModified(DBLayer.getCurrentDateTime());
        return dbItem;
    }

    @SuppressWarnings("unchecked")
    public DBItemSchedulerMonSystemResults getSystemResultLastStep(Optional<Integer> fetchSize, Long sysNotificationId) throws Exception {
        try {
            String method = "getSystemResultLastStep";
            StringBuilder sql = new StringBuilder("from ");
            sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS).append(" s1 ");
            sql.append("where s1.sysNotificationId = :sysNotificationId");
            sql.append(" and s1.orderStep = ");
            sql.append(" (select max(s2.orderStep) from ");
            sql.append(DBITEM_SCHEDULER_MON_SYSRESULTS).append(" s2 ");
            sql.append("where s2.sysNotificationId = s1.sysNotificationId)");

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[getSystemResultLastStep][sysNotificationId=%s]%s", sysNotificationId, sql.toString()));
            }

            Query query = getConnection().createQuery(sql.toString());
            query.setParameter("sysNotificationId", sysNotificationId);
            query.setReadOnly(true);
            if (fetchSize.isPresent()) {
                query.setFetchSize(fetchSize.get());
            }

            List<DBItemSchedulerMonSystemResults> result = executeQueryList(method, sql, query);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

    public void setLastNotificationDate(DBItemNotificationSchedulerVariables dbItem, Date date) throws Exception {
        DBItemNotificationSchedulerVariables itemNotificationSchedulerVariables = dbItem;
        try {
            if (itemNotificationSchedulerVariables == null) {
                itemNotificationSchedulerVariables = new DBItemNotificationSchedulerVariables();
                itemNotificationSchedulerVariables.setName(SCHEDULER_VARIABLES_NOTIFICATION);
                itemNotificationSchedulerVariables.setTextValue(DBLayer.getDateAsString(date));
                getConnection().save(itemNotificationSchedulerVariables);
            } else {
                itemNotificationSchedulerVariables.setTextValue(DBLayer.getDateAsString(date));
                getConnection().update(itemNotificationSchedulerVariables);
            }
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

}
