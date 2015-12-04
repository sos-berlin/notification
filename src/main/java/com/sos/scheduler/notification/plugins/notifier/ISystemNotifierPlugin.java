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

public interface ISystemNotifierPlugin {
	
	public void init(ElementNotificationMonitor monitor) throws Exception;		
	
	public int notifySystem(Spooler spooler,
			SystemNotifierJobOptions options, 
			DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check,
			EServiceStatus status, 
			EServiceMessagePrefix prefix) throws Exception;		
	
	public int notifySystemReset(String serviceName,EServiceStatus status,EServiceMessagePrefix prefix, String message) throws Exception;
}
