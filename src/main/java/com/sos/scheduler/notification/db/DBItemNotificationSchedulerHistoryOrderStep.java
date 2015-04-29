package com.sos.scheduler.notification.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

@Entity
public class DBItemNotificationSchedulerHistoryOrderStep implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long stepHistoryId;
	private Long stepStep;
	private Long stepTaskId;
	private Date stepStartTime;
	private Date stepEndTime;
	private String stepState;
	private Boolean stepError;
	private String stepErrorCode;
	private String stepErrorText;
	
	private Long orderHistoryId;
	private String orderSchedulerId;
	private String orderId;
	private String orderJobChain;
	private String orderState;
	private String orderStateText;
	private Date orderStartTime;
	private Date orderEndTime;
	
	private Long taskId;
	private String taskJobName;
	private String taskCause;
	private Date taskStartTime;
	private Date taskEndTime;
		
	public DBItemNotificationSchedulerHistoryOrderStep(){}
	
	@Id
	@Column(name="`STEP_HISTORY_ID`", nullable = false)
    public Long getStepHistoryId() {
		return stepHistoryId;
	}
	@Id
	@Column(name="`STEP_HISTORY_ID`", nullable = false)
    public void setStepHistoryId(Long val) {
		this.stepHistoryId = val;
	}
	
	@Id
	@Column(name="`STEP_STEP`", nullable = false)
    public Long getStepStep() {
		return stepStep;
	}
	@Id
	@Column(name="`STEP_STEP`", nullable = false)
    public void setStepStep(Long stepStep) {
		this.stepStep = stepStep;
	}
	
	@Column(name="`STEP_TASK_ID`", nullable = false)
    public Long getStepTaskId() {
		return stepTaskId;
	}
	@Column(name="`STEP_TASK_ID`", nullable = false)
    public void setStepTaskId(Long val) {
		this.stepTaskId = val;
	}
	
	@Column(name = "`STEP_START_TIME`", nullable = false)
	public Date getStepStartTime() {
		return stepStartTime;
	}
	
	@Column(name = "`STEP_START_TIME`", nullable = false)
	public void setStepStartTime(Date stepStartTime) {
		this.stepStartTime = stepStartTime;
	}
	
	@Column(name = "`STEP_END_TIME`", nullable = true)
	public Date getStepEndTime() {
		return stepEndTime;
	}
	@Column(name = "`STEP_END_TIME`", nullable = true)
	public void setStepEndTime(Date stepEndTime) {
		this.stepEndTime = stepEndTime;
	}
	
	@Column(name = "`STEP_STATE`", nullable = false)
	public String getStepState() {
		return stepState;
	}
	@Column(name = "`STEP_STATE`", nullable = false)	
	public void setStepState(String stepState) {
		this.stepState = stepState;
	}
	
	
	@Column(name = "`STEP_ERROR`", nullable = true)
	@Type(type="numeric_boolean")
	public Boolean getStepError() {
		return stepError;
	}
	@Column(name = "`STEP_ERROR`", nullable = true)
	@Type(type="numeric_boolean")
	public void setStepError(Boolean val) {
		if(val == null){
			val = false;
		}
		this.stepError = val;
	}
	/**
	@Column(name = "`STEP_ERROR`", nullable = true)
	@Type(type="numeric_boolean")
	public boolean getStepError() {
		return stepError;
	}
	@Column(name = "`STEP_ERROR`", nullable = true)
	@Type(type="numeric_boolean")
	public void setStepError(Boolean val) {
		if(val == null){
			val = false;
		}
		this.stepError = val;
	}
	
	@Column(name = "`STEP_ERROR`", nullable = true)
	@Type(type="numeric_boolean")
	public void setStepError(boolean val) {
		this.stepError = val;
	}*/
	
	@Column(name = "`STEP_ERROR_CODE`", nullable = true)
	public String getStepErrorCode() {
		return stepErrorCode;
	}
	@Column(name = "`STEP_ERROR_CODE`", nullable = true)
	public void setStepErrorCode(String stepErrorCode) {
		this.stepErrorCode = stepErrorCode;
	}
	@Column(name = "`STEP_ERROR_TEXT`", nullable = true)
	public String getStepErrorText() {
		return stepErrorText;
	}
	@Column(name = "`STEP_ERROR_TEXT`", nullable = true)
	public void setStepErrorText(String stepErrorText) {
		this.stepErrorText = stepErrorText;
	}
	
	@Column(name="`ORDER_HISTORY_ID`", nullable = true)
    public Long getOrderHistoryId() {
		return orderHistoryId;
	}
	@Column(name="`ORDER_HISTORY_ID`", nullable = true)
    public void setOrderHistoryId(Long val) {
		this.orderHistoryId = val;
	}
	
	@Column(name="`ORDER_SCHEDULER_ID`", nullable = true)
    public String getOrderSchedulerId() {
		return orderSchedulerId;
	}
	@Column(name="`ORDER_SCHEDULER_ID`", nullable = true)
    public void setOrderSchedulerId(String orderSchedulerId) {
		this.orderSchedulerId = orderSchedulerId;
	}
	
	@Column(name = "`ORDER_ID`", nullable = true)
	public String getOrderId() {
		return orderId;
	}
	@Column(name = "`ORDER_ID`", nullable = true)
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	@Column(name = "`ORDER_JOB_CHAIN`", nullable = true)
	public String getOrderJobChain() {
		return orderJobChain;
	}
	@Column(name = "`ORDER_JOB_CHAIN`", nullable = true)
	public void setOrderJobChain(String orderJobChain) {
		this.orderJobChain = orderJobChain;
	}
	@Column(name = "`ORDER_STATE`", nullable = true)
	public String getOrderState() {
		return orderState;
	}
	@Column(name = "`ORDER_STATE`", nullable = true)
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	@Column(name = "`ORDER_STATE_TEXT`", nullable = true)
	public String getOrderStateText() {
		return orderStateText;
	}
	@Column(name = "`ORDER_STATE_TEXT`", nullable = true)
	public void setOrderStateText(String orderStateText) {
		this.orderStateText = orderStateText;
	}
	@Column(name = "`ORDER_START_TIME`", nullable = true)
	public Date getOrderStartTime() {
		return orderStartTime;
	}
	@Column(name = "`ORDER_START_TIME`", nullable = true)
	public void setOrderStartTime(Date orderStartTime) {
		this.orderStartTime = orderStartTime;
	}
	@Column(name = "`ORDER_END_TIME`", nullable = true)
	public Date getOrderEndTime() {
		return orderEndTime;
	}
	@Column(name = "`ORDER_END_TIME`", nullable = true)
	public void setOrderEndTime(Date orderEndTime) {
		this.orderEndTime = orderEndTime;
	}
	
	@Column(name="`TASK_ID`", nullable = true)
    public Long getTaskId() {
		return taskId;
	}
	@Column(name="`TASK_ID`", nullable = true)
    public void setTaskId(Long val) {
		this.taskId = val;
	}
		
	@Column(name = "`TASK_JOB_NAME`", nullable = true)
	public String getTaskJobName() {
		return taskJobName;
	}
	@Column(name = "`TASK_JOB_NAME`", nullable = true)
	public void setTaskJobName(String taskJobName) {
		this.taskJobName = taskJobName;
	}
	@Column(name = "`TASK_CAUSE`", nullable = true)
	public String getTaskCause() {
		return taskCause;
	}
	@Column(name = "`TASK_CAUSE`", nullable = true)
	public void setTaskCause(String taskCause) {
		this.taskCause = taskCause;
	}
	@Column(name = "`TASK_START_TIME`", nullable = true)
	public Date getTaskStartTime() {
		return taskStartTime;
	}
	@Column(name = "`TASK_START_TIME`", nullable = true)
	public void setTaskStartTime(Date startTime) {
		this.taskStartTime = startTime;
	}
	@Column(name = "`TASK_END_TIME`", nullable = true)
	public Date getTaskEndTime() {
		return taskEndTime;
	}
	@Column(name = "`TASK_END_TIME`", nullable = true)
	public void setTaskEndTime(Date endTime) {
		this.taskEndTime = endTime;
	}
	
	@Transient
	public boolean equals(Object o) {
		boolean result = false;
		if(o instanceof DBItemNotificationSchedulerHistoryOrderStep){
			DBItemNotificationSchedulerHistoryOrderStep obj = (DBItemNotificationSchedulerHistoryOrderStep)o;
			if(getOrderHistoryId() !=null && getStepStep() != null && obj.getOrderHistoryId() != null && obj.getStepStep() != null){
				if(obj.getOrderHistoryId().equals(getOrderHistoryId()) && obj.getStepStep().equals(getStepStep())){
					return true;
				}
			}
		}
		return result;
	}
	
	@Transient
	public int hashCode() {
 	    int code = 0;
 	    if (getOrderHistoryId() != null) {code += getOrderHistoryId();}
 	    if (getStepStep() !=null) {code += getStepStep();}
 	    return code;
 	}
}
