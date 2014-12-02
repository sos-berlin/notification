package com.sos.scheduler.notification.model.result;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBItemSchedulerHistory;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonResults;
import com.sos.scheduler.notification.db.DBItemSchedulerOrderHistory;
import com.sos.scheduler.notification.db.DBItemSchedulerOrderStepHistory;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.jobs.result.StoreResultsJobOptions;
import com.sos.scheduler.notification.model.NotificationModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class StoreResultsModel extends NotificationModel {

	final Logger logger = LoggerFactory.getLogger(StoreResultsModel.class);
	private StoreResultsJobOptions options;
	//Der Job läuft als Monitor, d.h. die Datenbank Session wird bereits vom "Haupt"-Job initialisiert
	//Problem - aus irgendeinem Grund sind die neuen gemappten notification Klassen in der hibernate.cfg.xml Datei 
	//bei der ersten Verbindung nicht bekannt
	//private boolean reconnect = true;
	
	/**
	 * 
	 */
	public StoreResultsModel(){
    }
	
	/**
	 * 
	 * @param opt
	 * @param db
	 * @throws Exception
	 */
	public void init(StoreResultsJobOptions opt, DBLayerSchedulerMon db) throws Exception {
		super.init(db);
		this.options = opt;
	}

	/**
	 * 
	 */
	@Override
	public void process() throws Exception {
		
		super.process();
		
		ArrayList<String> resultParamsAsList = getResultParamsAsArrayList(this.options.scheduler_notification_result_parameters.Value());
		
		boolean hasResultParams = resultParamsAsList.size() > 0;
	
		
		HashMap<String,String> hm = this.options.Settings();
		HashMap<String,String> hmInsert = new HashMap<String,String>();
		if(hm != null){
			for( String name : hm.keySet() )
			{
				if(!hasResultParams || resultParamsAsList.contains(name)){
					hmInsert.put(name,hm.get(name));
				}
			}
		}
		
		logger.info(String.format("inserting %s params ",hmInsert.size()));
		
		if(hmInsert.size() > 0){
			try{
				this.getDbLayer().beginTransaction();
				
				DBItemSchedulerMonNotifications n = this.getNotification();			
				for( String name : hmInsert.keySet() )
				{
					this.insertParam(n.getId(),name,hmInsert.get(name));
				}
				
				this.getDbLayer().commit();
			}
			catch(Exception ex){
				try{ this.getDbLayer().rollback();}catch(Exception x){}
				throw  ex;
			}
	
		}

	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private DBItemSchedulerMonNotifications getNotification() throws Exception{
		
		DBItemSchedulerMonNotifications tmp = new DBItemSchedulerMonNotifications();
		
		tmp.setSchedulerId(this.options.mon_results_scheduler_id.Value());
		tmp.setStandalone(this.options.mon_results_standalone.value());
		tmp.setTaskId(new Long(this.options.mon_results_task_id.value()));		
		tmp.setOrderStepState(this.options.mon_results_order_step_state.Value());
		
		tmp.setJobChainName(this.options.mon_results_job_chain_name.Value());
		tmp.setOrderId(this.options.mon_results_order_id.Value());
				
		if(tmp.getStandalone()){
			DBItemSchedulerHistory h = this.getDbLayer().getSchedulerHistory(tmp.getTaskId());
			if(h != null){
				tmp.setStep(new Long(h.getSteps()));
				tmp.setJobName(h.getJobName());
				tmp.setTaskStartTime(h.getStartTime());
				tmp.setTaskEndTime(h.getEndTime());
				tmp.setError(h.haveError());
				tmp.setErrorCode(h.getErrorCode());
				tmp.setErrorText(h.getErrorText());
			}
		}
		else{			
			DBItemSchedulerOrderHistory o = this.getDbLayer().getNotFinishedOrderStepHistory(
					tmp.getSchedulerId(), 
					tmp.getTaskId(), 
					tmp.getOrderStepState(), 
					tmp.getJobChainName(), 
					tmp.getOrderId());
			
			logger.debug(String.format("schedulerId = %s, taskId = %s, orderStepState = %s, jobChainName = %s, orderId = %s",
					tmp.getSchedulerId(), 
					tmp.getTaskId(), 
					tmp.getOrderStepState(), 
					tmp.getJobChainName(), 
					tmp.getOrderId()));
			
			
			if(o != null){
				DBItemSchedulerOrderStepHistory osh = o.getSchedulerOrderStepHistory().get(0);
				if(osh == null){
					throw new Exception(String.format("order step history not found for order history id = %s", o.getHistoryId()));
				}
				
				DBItemSchedulerHistory h = osh.getSchedulerHistoryDBItem();
				if(h == null){
					throw new Exception(String.format("task history not found for order history id = %s", o.getHistoryId()));
				}
				
				tmp.setStep(osh.getId().getStep());
				tmp.setOrderHistoryId(o.getHistoryId());
				
				tmp.setOrderStartTime(o.getStartTime());
				tmp.setOrderEndTime(o.getEndTime());
				tmp.setOrderStepStartTime(osh.getStartTime());
				tmp.setOrderStepEndTime(osh.getEndTime());
				
				tmp.setJobName(h.getJobName());
				tmp.setTaskStartTime(h.getStartTime());
				tmp.setTaskEndTime(h.getEndTime());
				
				tmp.setError(osh.getError() == null ? false : osh.getError());
				tmp.setErrorCode(osh.getErrorCode());
				tmp.setErrorText(osh.getErrorText());				
			}
		}
				
		DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification
				(tmp.getSchedulerId(),
						tmp.getStandalone(), 
						tmp.getTaskId(), 
						tmp.getStep(),
						tmp.getOrderHistoryId());
			
		if(dbItem == null){
			dbItem = this.getDbLayer().createNotification(
					tmp.getSchedulerId(), 
					tmp.getStandalone(), 
					tmp.getTaskId(), 
					tmp.getStep(), 
					tmp.getOrderHistoryId(), 
					tmp.getJobChainName(), 
					tmp.getJobChainName(), 
					tmp.getOrderId(), 
					tmp.getOrderId(), 
					tmp.getOrderStartTime(), 
					tmp.getOrderEndTime(), 
					tmp.getOrderStepState(), 
					tmp.getOrderStepStartTime(), 
					tmp.getOrderStepEndTime(), 
					tmp.getJobName(), 
					tmp.getJobName(), 
					tmp.getTaskStartTime(), 
					tmp.getTaskEndTime(),
					tmp.getError(),
					tmp.getError(), 
					tmp.getErrorCode(), 
					tmp.getErrorText());
			
			logger.debug(String.format("create new notification: schedulerId = %s, standalone = %s, taskId = %s, historyId = %s, stepState = %s",
					tmp.getSchedulerId(),
					tmp.isStandalone(),
					tmp.getTaskId(),
					tmp.getOrderHistoryId(),
					tmp.getOrderStepState())
					);
			
			this.getDbLayer().save(dbItem);
		}
	
		return dbItem;
	}
	
	/**
	 * 
	 * @param notificationId
	 * @param name
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private DBItemSchedulerMonResults insertParam(Long notificationId, String name, String value) throws Exception{
		logger.debug(String.format("create new result: notificationId = %s, name = %s, value = %s",notificationId,name,value));
		
		DBItemSchedulerMonResults dbItem = this.getDbLayer().createResult(notificationId, name, value);
		this.getDbLayer().save(dbItem);
		
		return dbItem;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	private ArrayList<String> getResultParamsAsArrayList(String params){
		ArrayList<String> list = new ArrayList<String>();
		if(!SOSString.isEmpty(params)){
			String[] arr = params.split(";");
			for(int i=0;i<arr.length;i++){
				String val = arr[i].trim();
				if(val.length() > 0){
					list.add(val);
				}
			}
		}
		return list;
	}

}
