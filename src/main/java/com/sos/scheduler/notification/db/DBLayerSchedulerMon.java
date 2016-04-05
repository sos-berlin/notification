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

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateConnection.Dbms;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;

public class DBLayerSchedulerMon extends DBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
    private static final String FROM_WITH_SPACES = " from ";
    private static final String FROM = "from ";
    private static final String WHERE_N2_EQUALS_N1 = "  where n2.orderHistoryId = n1.orderHistoryId ";
    private static final String EQUALS_OH = " = oh.";
    private static final String EQUALS_OSH = " = osh.";
    private static final String OSH = " osh ";
    private static final String END_TIME = "END_TIME";
    private static final String HISTORY_ID = "HISTORY_ID";
    private static final String ORDER_END_TIME = "ORDER_END_TIME";
    private static final String DELETE_FROM = "delete from ";
    private static final String DELETE_COUNT = "deleted %s = %s";
    private static final String INNER_JOIN = "inner join ";
    private static final String H_ID = "h.id";
    private static final String H_CAUSE = "h.cause";
    private static final String H_ENDTIME = "h.endTime";
    private static final String H_JOBNAME = "h.jobName";
    private static final String H_STARTTIME = "h.startTime";
    private static final String H_EXIT_CODE = "h.exitCode";
    private static final String OH_END_TIME_UNDERSCORED = "oh.END_TIME";
    private static final String OH_ENDTIME = "oh.endTime";
    private static final String OH_HISTORYID = "oh.historyId";
    private static final String OH_JOBCHAIN = "oh.jobChain";
    private static final String OH_ORDERID = "oh.orderId";
    private static final String OH_SPOOLERID = "oh.spoolerId";
    private static final String OH_STARTTIME = "oh.startTime";
    private static final String OH_STATE = "oh.state";
    private static final String OH_STATETEXT = "oh.stateText";
    private static final String ON_ID_EQUALS_TASK_ID = "on h.ID = osh.TASK_ID ";
    private static final String ON_OSH_HISTORY_ID_EQUALS_OH_HISTORY_ID = "on osh.HISTORY_ID = oh.HISTORY_ID ";
    private static final String ORDER_END_TIME_AS = "orderEndTime";
    private static final String ORDER_HISTORY_ID = "orderHistoryId";
    private static final String ORDER_ID = "orderId";
    private static final String ORDER_JOB_CHAIN = "orderJobChain";
    private static final String ORDER_SCHEDULER_ID = "orderSchedulerId";
    private static final String ORDER_STARTTIME = "orderStartTime";
    private static final String ORDER_STATE = "orderState";
    private static final String ORDER_STATE_TEXT = "orderStateText";
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
            StringBuilder sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
            sql.append(" where created <= :date");
            int count = getConnection().createQuery(sql.toString()).setTimestamp("date", date).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_NOTIFICATIONS, count));

            String whereNotificationIdNotIn = " where notificationId not in (select id from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + ")";

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_RESULTS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_RESULTS, count));

            sql = new StringBuilder(DELETE_FROM);
            sql.append(DBITEM_SCHEDULER_MON_CHECKS);
            sql.append(whereNotificationIdNotIn);
            count = getConnection().createQuery(sql.toString()).executeUpdate();
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_CHECKS, count));

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
            LOGGER.info(String.format(DELETE_COUNT, DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS, count));
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

    public Criteria getSchedulerHistorySteps(Optional<Integer> fetchSize, Long historyId, Long step) throws Exception {
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
        cr.add(Restrictions.le(OSH_ID_HISTORYID, historyId));
        cr.add(Restrictions.le(OSH_ID_STEP, step));
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

    public void setNotificationCheckForRerun(DBItemSchedulerMonChecks check, Date stepFromStartTime, Date stepToEndTime, String text, String resultIds)
            throws Exception {
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

    public int updateUncompletedNotifications(Optional<Integer> fetchSize, boolean isDbDependent, Date maxStartTime) throws Exception {
        boolean executed = false;
        int result = 0;
        try {
            if (isDbDependent) {
                Enum<SOSHibernateConnection.Dbms> dbms = getConnection().getDbms();
                if (dbms.equals(Dbms.MSSQL)) {
                    executed = true;
                    StringBuilder sb = new StringBuilder("update mn");
                    sb.append(" set mn.ORDER_END_TIME = oh.END_TIME");
                    sb.append(" ,mn.TASK_END_TIME = h.END_TIME");
                    sb.append(" ,mn.RETURN_CODE = h.EXIT_CODE");
                    sb.append(" ,mn.ORDER_STEP_END_TIME = osh.END_TIME");
                    sb.append(" ,mn.ERROR = osh.ERROR");
                    sb.append(" ,mn.ERROR_CODE = osh.ERROR_CODE");
                    sb.append(" ,mn.ERROR_TEXT = osh.ERROR_TEXT");
                    sb.append(FROM_WITH_SPACES);
                    sb.append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(" mn ");
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_ORDER_HISTORY).append(" oh");
                    sb.append(" on mn.ORDER_HISTORY_ID = oh.HISTORY_ID ");
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_ORDER_STEP_HISTORY).append(OSH);
                    sb.append(ON_OSH_HISTORY_ID_EQUALS_OH_HISTORY_ID);
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_HISTORY).append(" h ");
                    sb.append(ON_ID_EQUALS_TASK_ID);
                    sb.append(" where mn.ORDER_END_TIME is null");
                    sb.append(" and mn.STEP = osh.STEP");
                    result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
                } else if (dbms.equals(Dbms.ORACLE) || dbms.equals(Dbms.DB2)) {
                    executed = true;
                    StringBuilder sb = new StringBuilder(UPDATE);
                    sb.append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(" mn");
                    sb.append(" set (");
                    sb.append(ORDER_END_TIME);
                    sb.append(",TASK_END_TIME");
                    sb.append(",RETURN_CODE");
                    sb.append(",ORDER_STEP_END_TIME");
                    sb.append(",ERROR");
                    sb.append(",ERROR_CODE");
                    sb.append(",ERROR_TEXT");
                    sb.append(") = (");
                    sb.append("select ");
                    sb.append(OH_END_TIME_UNDERSCORED);
                    sb.append(",h.END_TIME");
                    sb.append(",nvl(h.EXIT_CODE,0)");
                    sb.append(",osh.END_TIME");
                    sb.append(",nvl(osh.ERROR,0)");
                    sb.append(",osh.ERROR_CODE");
                    sb.append(",osh.ERROR_TEXT");
                    sb.append(FROM_WITH_SPACES);
                    sb.append(TABLE_SCHEDULER_ORDER_HISTORY).append(" oh ");
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_ORDER_STEP_HISTORY).append(OSH);
                    sb.append(ON_OSH_HISTORY_ID_EQUALS_OH_HISTORY_ID);
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_HISTORY).append(" h ");
                    sb.append(ON_ID_EQUALS_TASK_ID);
                    sb.append(" where oh.HISTORY_ID = mn.ORDER_HISTORY_ID");
                    sb.append(" and osh.STEP = mn.STEP ");
                    sb.append(") ");
                    sb.append(" where mn.ORDER_END_TIME is null");
                    sb.append(" and exists(");
                    sb.append(" select ");
                    sb.append(OH_END_TIME_UNDERSCORED);
                    sb.append(",h.END_TIME");
                    sb.append(",nvl(h.EXIT_CODE,0)");
                    sb.append(",osh.END_TIME");
                    sb.append(",nvl(osh.ERROR,0)");
                    sb.append(",osh.ERROR_CODE");
                    sb.append(",osh.ERROR_TEXT ");
                    sb.append(FROM);
                    sb.append(TABLE_SCHEDULER_ORDER_HISTORY).append(" oh ");
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_ORDER_STEP_HISTORY).append(OSH);
                    sb.append(ON_OSH_HISTORY_ID_EQUALS_OH_HISTORY_ID);
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_HISTORY).append(" h ");
                    sb.append(ON_ID_EQUALS_TASK_ID);
                    sb.append(" where oh.HISTORY_ID = mn.ORDER_HISTORY_ID");
                    sb.append(" and osh.STEP = mn.STEP ");
                    sb.append(") ");
                    result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
                } else if (dbms.equals(Dbms.MYSQL)) {
                    executed = true;
                    StringBuilder sb = new StringBuilder(UPDATE);
                    sb.append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(" mn,");
                    sb.append(TABLE_SCHEDULER_ORDER_HISTORY).append(" oh,");
                    sb.append(TABLE_SCHEDULER_ORDER_STEP_HISTORY).append(" osh,");
                    sb.append(TABLE_SCHEDULER_HISTORY).append(" h");
                    sb.append(" set ");
                    sb.append(quote("mn.ORDER_END_TIME")).append(" = ").append(quote(OH_END_TIME_UNDERSCORED));
                    sb.append(",").append(quote("mn.TASK_END_TIME")).append(" = ").append(quote("h.END_TIME"));
                    sb.append(",").append(quote("mn.ORDER_STEP_END_TIME")).append(" = ").append(quote("osh.END_TIME"));
                    sb.append(",").append(quote("mn.RETURN_CODE")).append(" = ").append(quote("h.EXIT_CODE"));
                    sb.append(",").append(quote("mn.ERROR")).append(" = ").append(quote("osh.ERROR"));
                    sb.append(",").append(quote("mn.ERROR_CODE")).append(" = ").append(quote("osh.ERROR_CODE"));
                    sb.append(",").append(quote("mn.ERROR_TEXT")).append(" = ").append(quote("osh.ERROR_TEXT"));
                    sb.append(" where ").append(quote("mn.ORDER_END_TIME")).append(" is null");
                    sb.append(" and ").append(quote("mn.STEP")).append(" = ").append(quote("osh.STEP"));
                    sb.append(" and ").append(quote("mn.ORDER_HISTORY_ID")).append(" = ").append(quote("oh.HISTORY_ID"));
                    sb.append(" and ").append(quote("osh.HISTORY_ID")).append(" = ").append(quote("oh.HISTORY_ID"));
                    sb.append(" and ").append(quote("h.ID")).append(" = ").append(quote("osh.TASK_ID"));
                    result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
                } else if (dbms.equals(Dbms.PGSQL) || dbms.equals(Dbms.SYBASE)) {
                    executed = true;
                    StringBuilder sb = new StringBuilder(UPDATE);
                    sb.append(TABLE_SCHEDULER_MON_NOTIFICATIONS);
                    sb.append(" set ");
                    sb.append(quote(ORDER_END_TIME)).append(EQUALS_OH).append(quote(END_TIME));
                    sb.append(",").append(quote("TASK_END_TIME")).append(" = h.").append(quote(END_TIME));
                    sb.append(",").append(quote("RETURN_CODE")).append(" = h.").append(quote("EXIT_CODE"));
                    sb.append(",").append(quote("ORDER_STEP_END_TIME")).append(EQUALS_OSH).append(quote(END_TIME));
                    sb.append(",").append(quote("ERROR")).append(EQUALS_OSH).append(quote("ERROR"));
                    sb.append(",").append(quote("ERROR_CODE")).append(EQUALS_OSH).append(quote("ERROR_CODE"));
                    sb.append(",").append(quote("ERROR_TEXT")).append(EQUALS_OSH).append(quote("ERROR_TEXT"));
                    sb.append(" ").append(FROM);
                    sb.append(TABLE_SCHEDULER_ORDER_HISTORY).append(" oh ");
                    sb.append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_ORDER_STEP_HISTORY).append(OSH);
                    sb.append(" on osh.").append(quote(HISTORY_ID)).append(EQUALS_OH).append(quote(HISTORY_ID));
                    sb.append(" ").append(INNER_JOIN);
                    sb.append(TABLE_SCHEDULER_HISTORY).append(" h");
                    sb.append(" on h.").append(quote("ID")).append(EQUALS_OSH).append(quote("TASK_ID"));
                    sb.append(" where ");
                    sb.append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(".").append(quote("ORDER_HISTORY_ID")).append(EQUALS_OH).append(quote(HISTORY_ID));
                    sb.append(" and ").append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(".").append(quote(ORDER_END_TIME)).append(" is null");
                    sb.append(" and ").append(TABLE_SCHEDULER_MON_NOTIFICATIONS).append(".").append(quote("STEP")).append(EQUALS_OSH).append(quote("STEP"));
                    result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
                }
            }
            // Database independent
            if (!executed) {
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
                        Criteria historyCriteria = getSchedulerHistorySteps(fetchSize, notification.getOrderHistoryId(), notification.getStep());
                        ResultSet historyResultSet = historyProcessor.createResultSet(DBItemNotificationSchedulerHistoryOrderStep.class, historyCriteria,
                                ScrollMode.FORWARD_ONLY, fetchSize);
                        try {
                            while (historyResultSet.next()) {
                                DBItemNotificationSchedulerHistoryOrderStep osh = (DBItemNotificationSchedulerHistoryOrderStep) historyProcessor.get();
                                Query q = getConnection().createQuery(update.toString());
                                q.setParameter("id", notification.getId());
                                q.setParameter(TASK_END_TIME, osh.getTaskEndTime());
                                q.setParameter(ORDER_END_TIME_AS, osh.getOrderEndTime());
                                q.setParameter("orderStepEndTime", osh.getStepEndTime());
                                q.setParameter("error", osh.getStepError());
                                q.setParameter("errorCode", osh.getStepErrorCode());
                                q.setParameter("errorText", osh.getStepErrorText());
                                q.setParameter("returnCode", osh.getTaskExitCode());
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
            }
            return result;
        } catch (Exception ex) {
            throw new Exception(SOSHibernateConnection.getException(ex));
        }
    }

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

            StringBuilder sql = new StringBuilder("select max(sn.notificationId) ");
            sql.append(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS).append(" sn");
            sql.append(" where lower(sn.systemId) = :systemId");
            Query query = getConnection().createQuery(sql.toString());
            query.setParameter(SYSTEM_ID, systemId.toLowerCase());

            Long maxNotificationId = (Long) query.uniqueResult();

            if (maxNotificationId == null || maxNotificationId.equals(new Long(0))) {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS);
                query = getConnection().createQuery(sql.toString());
            } else {
                sql = new StringBuilder(FROM).append(DBITEM_SCHEDULER_MON_NOTIFICATIONS).append(" n");
                sql.append(" where n.id > :maxNotificationId");
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
    public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications(String systemId, String serviceName, Long notificationId) throws Exception {
        try {
            String method = "getSystemNotifications";

            StringBuilder sql = new StringBuilder(FROM);
            sql.append(DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS);
            sql.append(" where notificationId = :notificationId");
            sql.append(" and serviceName = :serviceName ");
            sql.append(" and lower(systemId) = :systemId");

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
    public DBItemSchedulerMonSystemNotifications getSystemNotification(String systemId, String serviceName, Long notificationId, Long checkId, Long objectType,
            boolean onSuccess, String stepFrom, String stepTo, String returnCodeFrom, String returnCodeTo) throws Exception {
        try {
            String method = "getSystemNotification";
            LOGGER.debug(String
                    .format("%s: systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, objectType = %s, onSuccess = %s, stepFrom = %s, stepTo = %s, returnCodeFrom = %s, returnCodeTo = %s",
                            method, systemId, serviceName, notificationId, checkId, objectType, onSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo));
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
            } else {
                LOGGER.debug(String.format(
                        "%s: SystemNotification not found for systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, objectType = %s, onSuccess = %s, "
                                + "stepFrom = %s, stepTo = %s, returnCodeFrom = %s, returnCodeTo = %s", method, systemId, serviceName, notificationId, checkId,
                        objectType, onSuccess, stepFrom, stepTo, returnCodeFrom, returnCodeTo));
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
                LOGGER.warn(String.format("%s: '%s' cannot be converted to date: value = '%s', %s", method, SCHEDULER_VARIABLES_NOTIFICATION,
                        dbItem.getTextValue(), ex.getMessage()));
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
            LOGGER.debug(String.format("%s: orderHistoryId = %s, orderCompleted = %s", method, notification.getOrderHistoryId(), orderCompleted));
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
                LOGGER.debug(String.format("executeQueryList. try rerun %s again in %s. cause exception = %s, sql = %s", functionName,
                        RERUN_TRANSACTION_INTERVAL, ex.getMessage(), sql));
                Thread.sleep(RERUN_TRANSACTION_INTERVAL * 1000);
                result = q.list();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: %s , sql = %s", functionName, ex.getMessage(), sql), ex);
            }
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s , sql = %s", functionName, ex.getMessage(), sql));
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
            item.setResultIds(notification.getSchedulerId() + ";" + (notification.getStandalone() ? "true" : "false") + ";" + notification.getTaskId() + ";"
                    + notification.getStep() + ";" + notification.getOrderHistoryId());
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

        DBItemSchedulerMonSystemNotifications sm = createSystemNotification(systemId, serviceName, notificationId, checkId, returnCodeFrom, returnCodeTo,
                objectType, stepFrom, stepTo, stepFromStartTime, stepToEndTime, currentNotification, notifications, acknowledged, recovered, success);
        sm.setMaxNotifications(true);
        return sm;
    }

    public DBItemSchedulerMonNotifications createNotification(String schedulerId, boolean standalone, Long taskId, Long step, Long orderHistoryId,
            String jobChainName, String jobChainTitle, String orderId, String orderTitle, Date orderStartTime, Date orderEndTime, String orderStepState,
            Date orderStepStartTime, Date orderStepEndTime, String jobName, String jobTitle, Date taskStartTime, Date taskEndTime, boolean recovered,
            Long returnCode, boolean error, String errorCode, String errorText) throws Exception {
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
