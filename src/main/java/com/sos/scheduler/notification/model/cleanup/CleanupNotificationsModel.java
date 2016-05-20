package com.sos.scheduler.notification.model.cleanup;

import java.util.Date;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;

public class CleanupNotificationsModel extends NotificationModel implements INotificationModel {

    final Logger logger = LoggerFactory.getLogger(CleanupNotificationsModel.class);
    private CleanupNotificationsJobOptions options;

    public CleanupNotificationsModel(SOSHibernateConnection conn, CleanupNotificationsJobOptions opt) throws Exception {

        super(conn);
        options = opt;
    }

    @Override
    public void process() throws Exception {
        String method = "process";
        try {
            DateTime start = new DateTime();

            int minutes = NotificationModel.resolveAge2Minutes(this.options.age.getValue());
            Date date = DBLayerSchedulerMon.getCurrentDateTimeMinusMinutes(minutes);

            logger.info(String.format("%s: age = %s, delete where created <= %s minutes ago (%s)", method, this.options.age.getValue(), minutes, DBLayer.getDateAsString(date)));

            getDbLayer().getConnection().beginTransaction();
            getDbLayer().cleanupNotifications(date);
            getDbLayer().getConnection().commit();

            logger.info(String.format("%s: duration = %s", method, NotificationModel.getDuration(start, new DateTime())));
        } catch (Exception ex) {
            getDbLayer().getConnection().rollback();
            throw ex;
        }
    }

}
