package com.sos.scheduler.notification.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_SYSNOTIFICATIONS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS, allocationSize = 1)
/** uniqueConstraints = { @UniqueConstraint(columnNames = {"`NOTIFICATION_ID`", "`SYSTEM_ID`"})} */
public class DBItemSchedulerMonSystemNotifications extends DbItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** unique */
    // logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID
    private Long notificationId;
    // logical foreign key SCHEDULER_MON_CHECKS.ID
    private Long checkId;
    private String systemId;
    private String serviceName;
    private String stepFrom;
    private String stepTo;
    private String returnCodeFrom;
    private String returnCodeTo;
    private Long objectType; // notification object 0 - JobChain, 1 - Job  
    private Long notifications;
    private Long currentNotification;
    private boolean maxNotifications;
    private boolean acknowledged;
    private boolean recovered;
    private boolean success;
    private Date stepFromStartTime;
    private Date stepToEndTime;
    private Date created;
    private Date modified;
    /** parent table SCHEDULER_MON_NOTIFICATIONS */
    private DBItemSchedulerMonNotifications dbItemSchedulerMonNotifications;

    public DBItemSchedulerMonSystemNotifications() {
        this.setNotifications(new Long(0));
        this.setCurrentNotification(new Long(0));
        this.setMaxNotifications(false);
        this.setAcknowledged(false);
        this.setRecovered(false);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** unique */
    // logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID
    @Column(name = "`NOTIFICATION_ID`", nullable = false)
    public Long getNotificationId() {
        return this.notificationId;
    }

    @Column(name = "`NOTIFICATION_ID`", nullable = false)
    public void setNotificationId(Long val) {
        this.notificationId = val;
    }

    // logical foreign key SCHEDULER_MON_CHECKS.ID
    @Column(name = "`CHECK_ID`", nullable = false)
    public Long getCheckId() {
        return this.checkId;
    }

    @Column(name = "`CHECK_ID`", nullable = false)
    public void setCheckId(Long val) {
        this.checkId = val;
    }

    @Column(name = "`SYSTEM_ID`", nullable = false)
    public void setSystemId(String val) {
        this.systemId = val;
    }

    @Column(name = "`SYSTEM_ID`", nullable = false)
    public String getSystemId() {
        return this.systemId;
    }

    @Column(name = "`SERVICE_NAME`", nullable = false)
    public void setServiceName(String val) {
        this.serviceName = val;
    }

    @Column(name = "`SERVICE_NAME`", nullable = false)
    public String getServiceName() {
        return this.serviceName;
    }

    @Column(name = "`STEP_FROM`", nullable = false)
    public void setStepFrom(String val) {
        this.stepFrom = val;
    }

    @Column(name = "`STEP_FROM`", nullable = false)
    public String getStepFrom() {
        return this.stepFrom;
    }

    @Column(name = "`STEP_TO`", nullable = false)
    public void setStepTo(String val) {
        this.stepTo = val;
    }

    @Column(name = "`STEP_TO`", nullable = false)
    public String getStepTo() {
        return this.stepTo;
    }
    
    @Column(name = "`RETURN_CODE_FROM`", nullable = false)
    public void setReturnCodeFrom(String val) {
        this.returnCodeFrom = (val == null) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    @Column(name = "`RETURN_CODE_FROM`", nullable = false)
    public String getReturnCodeFrom() {
        return this.returnCodeFrom;
    }

    @Column(name = "`RETURN_CODE_TO`", nullable = false)
    public void setReturnCodeTo(String val) {
        this.returnCodeTo = (val == null) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    @Column(name = "`RETURN_CODE_TO`", nullable = false)
    public String getReturnCodeTo() {
        return this.returnCodeTo;
    }
    
    @Column(name = "`OBJECT_TYPE`", nullable = false)
    public void setObjectType(Long val) {
        this.objectType = (val == null) ? DBLayer.NOTIFICATION_OBJECT_TYPE_JOB_CHAIN : val;
    }

    @Column(name = "`OBJECT_TYPE`", nullable = false)
    public Long getObjectType() {
        return this.objectType;
    }
    
    @Column(name = "`NOTIFICATIONS`", nullable = true)
    public void setNotifications(Long val) {
        this.notifications = val;
    }

    @Column(name = "`NOTIFICATIONS`", nullable = false)
    public Long getNotifications() {
        return this.notifications;
    }
    
    @Column(name = "`CURRENT_NOTIFICATION`", nullable = false)
    public void setCurrentNotification(Long val) {
        this.currentNotification = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "`CURRENT_NOTIFICATION`", nullable = false)
    public Long getCurrentNotification() {
        return this.currentNotification;
    }

    @Column(name = "`MAX_NOTIFICATIONS`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setMaxNotifications(boolean val) {
        this.maxNotifications = val;
    }

    @Column(name = "`MAX_NOTIFICATIONS`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getMaxNotifications() {
        return this.maxNotifications;
    }

    @Column(name = "`ACKNOWLEDGED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setAcknowledged(boolean val) {
        this.acknowledged = val;
    }

    @Column(name = "`ACKNOWLEDGED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getAcknowledged() {
        return this.acknowledged;
    }

    @Column(name = "`RECOVERED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setRecovered(boolean val) {
        this.recovered = val;
    }

    @Column(name = "`RECOVERED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getRecovered() {
        return this.recovered;
    }

    @Column(name = "`SUCCESS`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setSuccess(boolean val) {
        this.success = val;
    }

    @Column(name = "`SUCCESS`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getSuccess() {
        return this.success;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STEP_FROM_START_TIME`", nullable = true)
    public void setStepFromStartTime(Date val) {
        this.stepFromStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STEP_FROM_START_TIME`", nullable = true)
    public Date getStepFromStartTime() {
        return this.stepFromStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STEP_TO_END_TIME`", nullable = true)
    public void setStepToEndTime(Date val) {
        this.stepToEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STEP_TO_END_TIME`", nullable = true)
    public Date getStepToEndTime() {
        return this.stepToEndTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @ManyToOne(optional = true)
    @JoinColumn(name = "`NOTIFICATION_ID`", insertable = false, updatable = false)
    public DBItemSchedulerMonNotifications getDbItemSchedulerMonNotifications() {
        return this.dbItemSchedulerMonNotifications;
    }

    public void setDbItemSchedulerMonNotifications(DBItemSchedulerMonNotifications val) {
        this.dbItemSchedulerMonNotifications = val;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer("sm.id = %s, sm.notificationId = %s, sm.objectType = %s")
        .append(", sm.stepFrom = %s, sm.stepTo = %s")
        .append(", sm.returnCodeFrom = %s, sm.returnCodeTo = %s")
        .append(", sm.currentNotification = %s, sm.notifications = %s, sm.maxNotifications = %s")
        .append(", sm.acknowledged = %s, sm.recovered = %s, sm.success = %s");
        return String.format(sb.toString(),id,notificationId,objectType
                ,stepFrom,stepTo
                ,returnCodeFrom,returnCodeTo
                ,currentNotification,notifications,maxNotifications
                ,acknowledged,recovered,success);
    }
    
}
