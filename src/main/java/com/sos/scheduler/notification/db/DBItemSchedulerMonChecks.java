package com.sos.scheduler.notification.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayerSchedulerMon.TABLE_SCHEDULER_MON_CHECKS)
@SequenceGenerator(
		name=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_CHECKS, 
		sequenceName=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_CHECKS,
		allocationSize=1)
public class DBItemSchedulerMonChecks extends DbItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** id */
	private Long id;
	
	/** logical foreign key SCHEDULER_MON_NOTIFICATIONS.ID */	
	private Long notificationId;
		
	/** others */
	private String name;
	private String stepFrom;
	private String stepTo;
	private Date stepFromStartTime;
	private Date stepToEndTime;
	/** logical foreign key SCHEDULER_MON_RESULTS.ID durch ; getrennt */	
	private String resultIds;	
	private boolean checked;
	private String checkText;
	
	private Date created;
	private Date modified;

	/**
	 * 
	 */
	public DBItemSchedulerMonChecks() {
		this.setNotificationId(new Long(0));
		this.setChecked(false);		
	}
	
	/** id */
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_CHECKS)
    @Column(name="`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }
    
    @Id
    @GeneratedValue(
        	strategy=GenerationType.AUTO,
        	generator=DBLayerSchedulerMon.SEQUENCE_SCHEDULER_MON_CHECKS)
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
	
	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`STEP_FROM_START_TIME`", nullable = true)
	public void setStepFromStartTime(Date val) {
		this.stepFromStartTime = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`STEP_FROM_START_TIME`", nullable = true)
	public Date getStepFromStartTime() {
		return this.stepFromStartTime;
	}
	
	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`STEP_TO_END_TIME`", nullable = true)
	public void setStepToEndTime(Date val) {
		this.stepToEndTime = val;
	}

	@Temporal (TemporalType.TIMESTAMP)
	@Column(name = "`STEP_TO_END_TIME`", nullable = true)
	public Date getStepToEndTime() {
		return this.stepToEndTime;
	}
	
	/** logical foreign key SCHEDULER_MON_RESULTS.ID */
    @Column(name="`RESULT_IDS`", nullable = true)
    public String getResultIds() {
        return this.resultIds;
    }
    
    @Column(name="`RESULT_IDS`", nullable = true)
    public void setResultIds(String val) {
       this.resultIds = val;
    }
    
	
	@Column(name = "`CHECKED`", nullable = false)
    @Type(type="numeric_boolean")
    public void setChecked(boolean val) {
		this.checked = val;
	}

	@Column(name = "`CHECKED`", nullable = false)
	@Type(type="numeric_boolean")
    public boolean getChecked() {
		return this.checked;
	}
	
	@Column(name = "`CHECK_TEXT`", nullable = true)
	public void setCheckText(String val) {
		if(SOSString.isEmpty(val)){ val = null;}
		this.checkText = val;
	}

	@Column(name = "`CHECK_TEXT`", nullable = true)
	public String getCheckText() {
		return this.checkText;
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
