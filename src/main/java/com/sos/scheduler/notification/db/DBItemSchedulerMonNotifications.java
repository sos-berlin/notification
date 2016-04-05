package com.sos.scheduler.notification.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_MON_NOTIFICATIONS)
@SequenceGenerator(name = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS, sequenceName = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS, allocationSize = 1)
/** uniqueConstraints = {@UniqueConstraint(columnNames ={"`SCHEDULER_ID`", "`STANDALONE`","`TASK_ID`","`STEP`","`ORDER_HISTORY_ID`"})}*/
public class DBItemSchedulerMonNotifications extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    /** unique */
    private String schedulerId;
    private boolean standalone;
    private Long taskId;
    private Long step;
    private Long orderHistoryId;
    /** others */
    private String jobChainName;
    private String jobChainTitle;
    private String orderId;
    private String orderTitle;
    private Date orderStartTime;
    private Date orderEndTime;
    private String orderStepState;
    private Date orderStepStartTime;
    private Date orderStepEndTime;
    private String jobName;
    private String jobTitle;
    private Date taskStartTime;
    private Date taskEndTime;
    private boolean recovered;
    private Long returnCode;
    private boolean error;
    private String errorCode;
    private String errorText;
    private Date created;
    private Date modified;
    List<Object> childs;

    public DBItemSchedulerMonNotifications() {
        this.setOrderHistoryId(DBLayer.DEFAULT_EMPTY_NUMERIC);
        this.setStep(DBLayer.DEFAULT_EMPTY_NUMERIC);
        this.setReturnCode(DBLayer.DEFAULT_EMPTY_NUMERIC);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.SEQUENCE_SCHEDULER_MON_NOTIFICATIONS)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** unique */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "`STANDALONE`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setStandalone(boolean val) {
        this.standalone = val;
    }

    @Column(name = "`STANDALONE`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getStandalone() {
        return this.standalone;
    }

    @Column(name = "`TASK_ID`", nullable = false)
    public void setTaskId(Long val) {
        this.taskId = val;
    }

    @Column(name = "`TASK_ID`", nullable = false)
    public Long getTaskId() {
        return this.taskId;
    }

    @Column(name = "`STEP`", nullable = false)
    public void setStep(Long val) {
        this.step = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "`STEP`", nullable = false)
    public Long getStep() {
        return this.step;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        this.orderHistoryId = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return this.orderHistoryId;
    }

    /** others */
    @Column(name = "`JOB_CHAIN_NAME`", nullable = false)
    public void setJobChainName(String val) {
        if (SOSString.isEmpty(val)) {
            val = DBLayer.EMPTY_TEXT_VALUE;
        } else if (val.startsWith("/")) {
            val = val.substring(1);
        }

        this.jobChainName = val;
    }

    @Column(name = "`JOB_CHAIN_NAME`", nullable = false)
    public String getJobChainName() {
        return this.jobChainName;
    }

    @Column(name = "`JOB_CHAIN_TITLE`", nullable = true)
    public void setJobChainTitle(String val) {
        this.jobChainTitle = SOSString.isEmpty(val) ? null : val;
    }

    @Column(name = "`JOB_CHAIN_TITLE`", nullable = true)
    public String getJobChainTitle() {
        return this.jobChainTitle;
    }

    @Column(name = "`ORDER_ID`", nullable = false)
    public void setOrderId(String val) {
        this.orderId = SOSString.isEmpty(val) ? DBLayer.EMPTY_TEXT_VALUE : val;
    }

    @Column(name = "`ORDER_ID`", nullable = false)
    public String getOrderId() {
        return this.orderId;
    }

    @Column(name = "`ORDER_TITLE`", nullable = true)
    public void setOrderTitle(String val) {
        this.orderTitle = SOSString.isEmpty(val) ? null : val;
    }

    @Column(name = "`ORDER_TITLE`", nullable = true)
    public String getOrderTitle() {
        return this.orderTitle;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_START_TIME`", nullable = true)
    public void setOrderStartTime(Date val) {
        this.orderStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_START_TIME`", nullable = true)
    public Date getOrderStartTime() {
        return this.orderStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_END_TIME`", nullable = true)
    public void setOrderEndTime(Date val) {
        this.orderEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_END_TIME`", nullable = true)
    public Date getOrderEndTime() {
        return this.orderEndTime;
    }

    @Column(name = "`ORDER_STEP_STATE`", nullable = false)
    public void setOrderStepState(String val) {
        this.orderStepState = SOSString.isEmpty(val) ? DBLayer.EMPTY_TEXT_VALUE : val;
    }

    @Column(name = "`ORDER_STEP_STATE`", nullable = false)
    public String getOrderStepState() {
        return this.orderStepState;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_STEP_START_TIME`", nullable = true)
    public void setOrderStepStartTime(Date val) {
        this.orderStepStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_STEP_START_TIME`", nullable = true)
    public Date getOrderStepStartTime() {
        return this.orderStepStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_STEP_END_TIME`", nullable = true)
    public void setOrderStepEndTime(Date val) {
        this.orderStepEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`ORDER_STEP_END_TIME`", nullable = true)
    public Date getOrderStepEndTime() {
        return this.orderStepEndTime;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public void setJobName(String val) {
        this.jobName = val;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public String getJobName() {
        return this.jobName;
    }

    @Column(name = "`JOB_TITLE`", nullable = true)
    public void setJobTitle(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.jobTitle = val;
    }

    @Column(name = "`JOB_TITLE`", nullable = true)
    public String getJobTitle() {
        return this.jobTitle;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`TASK_START_TIME`", nullable = false)
    public void setTaskStartTime(Date val) {
        this.taskStartTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`TASK_START_TIME`", nullable = false)
    public Date getTaskStartTime() {
        return this.taskStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`TASK_END_TIME`", nullable = true)
    public void setTaskEndTime(Date val) {
        this.taskEndTime = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`TASK_END_TIME`", nullable = true)
    public Date getTaskEndTime() {
        return this.taskEndTime;
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

    @Column(name = "`RETURN_CODE`", nullable = false)
    public void setReturnCode(Long val) {
        this.returnCode = (val == null) ? DBLayer.DEFAULT_EMPTY_NUMERIC : val;
    }

    @Column(name = "`RETURN_CODE`", nullable = false)
    public Long getReturnCode() {
        return this.returnCode;
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setError(boolean val) {
        this.error = val;
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getError() {
        return this.error;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public void setErrorCode(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.errorCode = val;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public String getErrorCode() {
        return this.errorCode;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public void setErrorText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.errorText = val;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public String getErrorText() {
        return this.errorText;
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

    @Transient
    public List<Object> getChilds() {
        return this.childs;
    }

    @Transient
    public void setChilds(List<Object> val) {
        this.childs = val;
    }

    @Transient
    public void addChild(Object val) {
        if (this.childs == null) {
            this.childs = new ArrayList<Object>();
        }
        this.childs.add(val);
    }

}
