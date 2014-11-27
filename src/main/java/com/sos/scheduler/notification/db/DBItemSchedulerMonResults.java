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

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

//@MappedSuperclass
@Entity
@Table(name = DBLayerSchedulerMon.TABLE_SCHEDULER_MON_RESULTS)
@SequenceGenerator(
		name=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_RESULTS, 
		sequenceName=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_RESULTS,
		allocationSize=1)
public class DBItemSchedulerMonResults extends DbItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** id */
	private Long id;
	
	/** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */	
	private Long notificationId;
	
	/** others */
	private String name;
	private String value;
	private Date created;
	private Date modified;

	/** parent table SCHEDULER_MON_NOTIFICATIONS */
	private DBItemSchedulerMonNotifications dbItemSchedulerMonNotifications;
	
	public DBItemSchedulerMonResults() {

	}
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "`NOTIFICATION_ID`", insertable = false, updatable = false)
	public DBItemSchedulerMonNotifications getDbItemSchedulerMonNotifications() {
		return this.dbItemSchedulerMonNotifications;
	}
	public void setDbItemSchedulerMonNotifications(
			DBItemSchedulerMonNotifications val) {
		this.dbItemSchedulerMonNotifications = val;
	}


	/** id */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_RESULTS)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_RESULTS)
    @Column(name="`ID`", nullable = false)
    public void setId(Long val) {
       this.id = val;
    }

    /** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */
    @Column(name="`NOTIFICATION_ID`", nullable = false)
    public Long getNotificationId() {
        return this.notificationId;
    }
    
    @Column(name="`NOTIFICATION_ID`", nullable = false)
    public void setNotificationId(Long val) {
       this.notificationId = val;
    }
    
    /** others */
	@Column(name = "`NAME`", nullable = false)
	public void setName(String val) {
		this.name = val;
	}

	@Column(name = "`NAME`", nullable = false)
	public String getName() {
		return this.name;
	}

	@Column(name = "`VALUE`", nullable = true)
	public void setValue(String val) {
		if(SOSString.isEmpty(val)){ val = null;}
		this.value = val;
	}

	@Column(name = "`VALUE`", nullable = true)
	public String getValue() {
		return this.value;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`CREATED`", nullable = false)
	public void setCreated(Date val) {
		this.created = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`CREATED`", nullable = false)
	public Date getCreated() {
		return this.created;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`MODIFIED`", nullable = false)
	public void setModified(Date val) {
		this.modified = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`MODIFIED`", nullable = false)
	public Date getModified() {
		return this.modified;
	}
}
