package com.sos.scheduler.notification.plugins.notifier;

import sos.spooler.Spooler;

import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public interface ISystemNotifierPlugin {
	
	/**
	 * 
	 * @param monitor
	 * @throws Exception
	 */
	public void init(ElementNotificationMonitor monitor) throws Exception;		
	
	/**
	 * 
	 * @param spooler
	 * @param options
	 * @param dbLayer
	 * @param notification
	 * @param systemNotification
	 * @param check
	 * @param status
	 * @param prefix
	 * @return
	 * @throws Exception
	 */
	public int notifySystem(Spooler spooler,
			SystemNotifierJobOptions options, 
			DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check,
			EServiceStatus status, 
			EServiceMessagePrefix prefix) throws Exception;		
	
	/**
	 * 
	 * @param serviceName
	 * @param status
	 * @param prefix
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public int notifySystemReset(String serviceName,EServiceStatus status,EServiceMessagePrefix prefix, String message) throws Exception;
}
