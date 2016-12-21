package com.sos.scheduler.notification.db;

import java.util.Date;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.ClassList;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerOrderStepHistoryDBItem;
import com.sos.jitl.schedulerhistory.db.SchedulerTaskHistoryDBItem;

public class DBLayer {

    final Logger logger = LoggerFactory.getLogger(DBLayer.class);

    public final static String SCHEDULER_VARIABLES_NOTIFICATION = "notification_date";
    public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** */
    public final static String TABLE_SCHEDULER_HISTORY = "SCHEDULER_HISTORY";
    public final static String TABLE_SCHEDULER_ORDER_HISTORY = "SCHEDULER_ORDER_HISTORY";
    public final static String TABLE_SCHEDULER_ORDER_STEP_HISTORY = "SCHEDULER_ORDER_STEP_HISTORY";

    /** Table SCHEDULER_VARIABLES */
    public final static String DBITEM_SCHEDULER_VARIABLES = DBItemNotificationSchedulerVariables.class.getSimpleName();
    public final static String TABLE_SCHEDULER_VARIABLES = "SCHEDULER_VARIABLES";

    /** Table SCHEDULER_MON_NOTIFICATIONS */
    public final static String DBITEM_SCHEDULER_MON_NOTIFICATIONS = DBItemSchedulerMonNotifications.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOTIFICATIONS";
    public final static String SEQUENCE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOT_ID_SEQ";

    /** Table SCHEDULER_MON_RESULTS */
    public final static String DBITEM_SCHEDULER_MON_RESULTS = DBItemSchedulerMonResults.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RESULTS";
    public final static String SEQUENCE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RES_ID_SEQ";

    /** Table SCHEDULER_MON_SYSNOTIFICATIONS */
    public final static String DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS = DBItemSchedulerMonSystemNotifications.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOTIFICATIONS";
    public final static String SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOT_ID_SEQ";

    /** Table SCHEDULER_MON_CHECKS */
    public final static String DBITEM_SCHEDULER_MON_CHECKS = DBItemSchedulerMonChecks.class.getSimpleName();
    public final static String TABLE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS";
    public final static String SEQUENCE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS_ID_SEQ";

    /** in seconds */
    public final static int RERUN_TRANSACTION_INTERVAL = 3;
    public final static String EMPTY_TEXT_VALUE = "";
    public final static String DEFAULT_EMPTY_NAME = "*";
    public final static Long DEFAULT_EMPTY_NUMERIC = new Long(0);

    public static final Long NOTIFICATION_OBJECT_TYPE_JOB_CHAIN = new Long(0);
    public static final Long NOTIFICATION_OBJECT_TYPE_JOB = new Long(1);
    public static final Long NOTIFICATION_OBJECT_TYPE_DUMMY = new Long(100);

    private SOSHibernateConnection connection;

    public DBLayer(SOSHibernateConnection conn) {
        connection = conn;
    }

    public SOSHibernateConnection getConnection() {
        return connection;
    }

    /** @return */
    public static ClassList getSchedulerClassMapping() {
        ClassList cl = new ClassList();

        cl.add(DBItemNotificationSchedulerVariables.class);

        cl.add(SchedulerTaskHistoryDBItem.class);
        cl.add(SchedulerOrderHistoryDBItem.class);
        cl.add(SchedulerOrderStepHistoryDBItem.class);

        return cl;
    }

    /** @return */
    public static ClassList getNotificationClassMapping() {
        ClassList cl = new ClassList();

        cl.add(DBItemNotificationSchedulerHistoryOrderStep.class);
        cl.add(DBItemSchedulerMonChecks.class);
        cl.add(DBItemSchedulerMonNotifications.class);
        cl.add(DBItemSchedulerMonResults.class);
        cl.add(DBItemSchedulerMonSystemNotifications.class);
        return cl;
    }

    /** @param fieldName
     * @return */
    public String quote(String fieldName) {
        return connection.quoteFieldName(fieldName);
    }

    /** @return */
    public static Date getCurrentDateTime() {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
    }

    /** @return */
    public static Date getCurrentDateTimeMinusDays(int days) {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusDays(days).toDate();
    }

    public static Date getCurrentDateTimeMinusMinutes(int minutes) {
        return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusMinutes(minutes).toDate();
    }

    public static Date getDateTimeMinusMinutes(Date date, int minutes) {
        return new DateTime(date).toLocalDateTime().minusMinutes(minutes).toDate();
    }

    /** @param d
     * @return
     * @throws Exception */
    public static String getDateAsString(Date d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        DateTime dt = new DateTime(d);
        return f.print(dt);
    }

    /** @param d
     * @return
     * @throws Exception */
    public static Date getDateFromString(String d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        return f.parseDateTime(d).toDate();
    }

    public void flushScrollableResults(int readCount) throws Exception {
        // Moreover if session cache is enabled,
        // you need to add explicit code to clear the session cache,
        // such as a code snippet here to clear cache every 100 rows:

        if (readCount % 100 == 0) {
            if (getConnection().getCurrentSession() instanceof Session) {
                Session s = (Session) getConnection().getCurrentSession();
                s.clear();
                s.flush();
            }
        }
    }

}
