package com.sos.scheduler.notification.plugins.history;

import java.util.Date;
import java.util.LinkedHashMap;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public interface ICheckHistoryPlugin {
	
	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 * @throws Exception
	 */
	public void onInit(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception;
	
	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 * @throws Exception
	 */
	public void onExit(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) throws Exception;
	
	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 * @param dateFrom
	 * @param dateTo
	 * @throws Exception
	 */
	public void onProcess(LinkedHashMap<String, ElementTimer> timers,CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer, Date dateFrom,Date dateTo) throws Exception;		
}
