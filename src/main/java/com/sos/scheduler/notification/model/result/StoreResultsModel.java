package com.sos.scheduler.notification.model.result;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;
import com.sos.scheduler.notification.db.DBItemNotificationSchedulerHistoryOrderStep;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonResults;
import com.sos.scheduler.notification.jobs.result.StoreResultsJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class StoreResultsModel extends NotificationModel implements INotificationModel {

	final Logger logger = LoggerFactory.getLogger(StoreResultsModel.class);
	private StoreResultsJobOptions options;
	
	
	/**
	 * 
	 * @param conn
	 * @param opt
	 * @throws Exception
	 */
	public StoreResultsModel(SOSHibernateConnection conn, 
			StoreResultsJobOptions opt) throws Exception {
		
		super(conn);
		options = opt;
	}

	/**
	 * 
	 */
	@Override
	public void process() throws Exception {
		ArrayList<String> resultParamsAsList = getResultParamsAsArrayList(options.scheduler_notification_result_parameters.Value());
		
		boolean hasResultParams = resultParamsAsList.size() > 0;
		
		HashMap<String,String> hm = options.Settings();
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
				getDbLayer().getConnection().beginTransaction();
				
				DBItemSchedulerMonNotifications n = getNotification();			
				for( String name : hmInsert.keySet() )
				{
					insertParam(n.getId(),name,hmInsert.get(name));
				}
				
				getDbLayer().getConnection().commit();
			}
			catch(Exception ex){
				try{ getDbLayer().getConnection().rollback();}catch(Exception x){}
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
		String method = "getNotification";
		
		DBItemSchedulerMonNotifications tmp = new DBItemSchedulerMonNotifications();
		
		tmp.setSchedulerId(options.mon_results_scheduler_id.Value());
		tmp.setStandalone(options.mon_results_standalone.value());
		tmp.setTaskId(new Long(options.mon_results_task_id.value()));		
		tmp.setOrderStepState(options.mon_results_order_step_state.Value());
		
		tmp.setJobChainName(options.mon_results_job_chain_name.Value());
		tmp.setOrderId(options.mon_results_order_id.Value());
				
		if(tmp.getStandalone()){
			SchedulerTaskHistoryDBItem h = getDbLayer().getSchedulerHistory(tmp.getTaskId());
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
			logger.debug(String.format("%s: getNotFinishedOrderStepHistory: schedulerId = %s, taskId = %s, orderStepState = %s, jobChainName = %s, orderId = %s",
					method,
					tmp.getSchedulerId(), 
					tmp.getTaskId(), 
					tmp.getOrderStepState(), 
					tmp.getJobChainName(), 
					tmp.getOrderId()));
			
			DBItemNotificationSchedulerHistoryOrderStep os = getDbLayer().getNotFinishedOrderStepHistory(
					tmp.getSchedulerId(), 
					tmp.getTaskId(), 
					tmp.getOrderStepState(), 
					tmp.getJobChainName(), 
					tmp.getOrderId());
			
			if(os == null){
				logger.debug(String.format("%s: getNotFinishedOrderStepHistory not found for: schedulerId = %s, taskId = %s, orderStepState = %s, jobChainName = %s, orderId = %s",
						method,
						tmp.getSchedulerId(), 
						tmp.getTaskId(), 
						tmp.getOrderStepState(), 
						tmp.getJobChainName(), 
						tmp.getOrderId()));
			}
			else{
				if(os.getStepStep() == null){
					throw new Exception(String.format("order step history not found for order history id = %s", os.getOrderHistoryId()));
				}
				
				if(os.getTaskId() == null){
					throw new Exception(String.format("task history not found for order history id = %s", os.getOrderHistoryId()));
				}
				
				tmp.setStep(os.getStepStep());
				tmp.setOrderHistoryId(os.getOrderHistoryId());
				
				tmp.setOrderStartTime(os.getOrderStartTime());
				tmp.setOrderEndTime(os.getOrderEndTime());
				tmp.setOrderStepStartTime(os.getStepStartTime());
				tmp.setOrderStepEndTime(os.getStepEndTime());
				
				tmp.setJobName(os.getTaskJobName());
				tmp.setTaskStartTime(os.getTaskStartTime());
				tmp.setTaskEndTime(os.getTaskEndTime());
				
				tmp.setError(os.getStepError());
				tmp.setErrorCode(os.getStepErrorCode());
				tmp.setErrorText(os.getStepErrorText());				
			}
		}
				
		DBItemSchedulerMonNotifications dbItem = getDbLayer().getNotification
				(tmp.getSchedulerId(),
						tmp.getStandalone(), 
						tmp.getTaskId(), 
						tmp.getStep(),
						tmp.getOrderHistoryId());
			
		if(dbItem == null){
			dbItem = getDbLayer().createNotification(
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
			
			getDbLayer().getConnection().save(dbItem);
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
		
		DBItemSchedulerMonResults dbItem = getDbLayer().createResult(notificationId, name, value);
		getDbLayer().getConnection().save(dbItem);
		
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
