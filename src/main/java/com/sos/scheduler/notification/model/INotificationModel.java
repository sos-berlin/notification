package com.sos.scheduler.notification.model;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public interface INotificationModel {
	/**
	 * 
	 * @throws Exception
	 */
	public void init(DBLayerSchedulerMon dbLayer) throws Exception;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception;
	
	/**
	 * 
	 * @throws Exception
	 */
	public void exit() throws Exception;
	
	/**
	 * 
	 * @return
	 */
	
}
