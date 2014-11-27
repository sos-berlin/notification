package com.sos.scheduler.notification.db;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

 

@Embeddable
public class DBCompoundKeySchedulerOrderStepHistory implements Serializable {
		 /**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		 private Long historyId;
	     private Long step;

	     public DBCompoundKeySchedulerOrderStepHistory() {
	    	  }
	     
	     public DBCompoundKeySchedulerOrderStepHistory(Long historyId, Long step) {
	    	    this.historyId = historyId;
	    	    this.step =step;
	    	  }
	   
	     @Column(name="`HISTORY_ID`",nullable=false) 
	     public Long getHistoryId() {
	    	 return historyId;
	     }
	     
	     @Column(name="`HISTORY_ID`",nullable=false) 
	     public void setHistoryId(Long historyId) {
	       this.historyId = historyId;
	     }
	     
	     @Column(name="`STEP`",nullable=false)
	     public Long getStep() {
	    	 return step;
	     }
	     
	     @Column(name="`STEP`",nullable=false)
	     public void setStep(Long step) {
	       this.step = step;
	     }
	     
	     public boolean equals(Object key) {
	    	   boolean result = true;
	    	   if (!(key instanceof DBCompoundKeySchedulerOrderStepHistory)) {return false;}
	    	    Long otherHistoryId = ((DBCompoundKeySchedulerOrderStepHistory)key).getHistoryId();
	    	    Long otherStep = ((DBCompoundKeySchedulerOrderStepHistory)key).getStep();
	    	    if (step == null || otherStep == null) {
	    	      result = false;
	    	    }else {
	    	      result = step.equals(otherStep);
	    	    }
	    	    if (historyId == null || otherHistoryId == null) {
	    	      result = false;
	    	    }else {
	    	      result = historyId.equals(otherHistoryId);
	    	    }
	    	   return result;
	    	  }

	    	  public int hashCode() {
	    	    int code = 0;
	    	    if (step!=null) {code +=step;}
	    	    if (historyId!=null) {code +=historyId;}
	    	    return code;
	    	  }
	 }