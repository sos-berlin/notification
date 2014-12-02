package com.sos.scheduler.notification.model;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.RegExFilenameFilter;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class NotificationModel implements INotificationModel {

	final Logger logger = LoggerFactory.getLogger(NotificationModel.class);
	DBLayerSchedulerMon dbLayer = null;

	public static final String OPERATION_ACKNOWLEDGE = "acknowledge";
	public static final String OPERATION_RESET_SERVICES = "reset_services";
	
	
	@Override
	public void init(DBLayerSchedulerMon db) throws Exception {
		this.dbLayer = db;
		if(this.dbLayer == null){
			throw new Exception("dbLayer is NULL");
		}

	}

	
	/**
	 * 
	 */
	@Override
	public void process() throws Exception {
	}

	/**
	 * 
	 */
	@Override
	public void exit() throws Exception {
		logger.debug(String.format("exit"));
	}

	public DBLayerSchedulerMon getDbLayer() {
		return this.dbLayer;
	}

	/**
	 * 
	 * @param dir
	 * @param regex
	 * @return
	 */
	public static File[] getFiles( File dir,String regex){
	    
    	return dir.listFiles(new RegExFilenameFilter(regex));
    }

	/**
	 * 
	 * @param dir
	 * @return
	 */
	public static File[] getAllConfigurationFiles(File dir){
		String regex = "^SystemMonitorNotificationTimers\\.xml$|(^SystemMonitorNotification_){1}(.)*\\.xml$";
		
		return getFiles(dir, regex);
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
	public static File[] getConfigurationFiles(File dir){
		String regex = "(^SystemMonitorNotification_){1}(.)*\\.xml$";
		
		return getFiles(dir, regex);
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
	public static File getTimerConfigurationFileX(File dir){
		File f = new File(dir,"SystemMonitorNotificationTimers.xml");
		return f.exists() ? f : null;
	}
	
	
	/**
	 * 
	 * @param dir
	 * @return
	 */
	public static File getConfigurationSchemaFile(File dir){
		String regex = "(^SystemMonitorNotification_){1}(.)*\\.xsd$";
		
		File[] result = getFiles(dir, regex);
		if(result.length > 0){
			return result[0];
		}
		return null;
	}
	
}
