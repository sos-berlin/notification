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
	public void init() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param hibernateConfigFile
	 * @param reconnect
	 * @throws Exception
	 */
	public void doInit(String hibernateConfigFile, boolean reconnect)
			throws Exception {
		File file = new File(hibernateConfigFile);
		if (!file.exists()) {
			throw new Exception(String.format(
					"not found hibernate config file = %s",
					file.getAbsolutePath()));
		}

		logger.info(String.format("init connection with hibernate file = %s",
				file.getAbsolutePath()));
		try {
			this.dbLayer = new DBLayerSchedulerMon(file);
			if (reconnect) {
				try {
					if (this.dbLayer.getSession() != null) {
						this.dbLayer.closeSession();
					}
				} catch (Exception ex) {
					logger.info(String.format(
							"dbLayer.closeSession exception : %s",
							ex.getMessage()));
				}
			}
			this.dbLayer.initSession();
		} catch (Exception ex) {
			throw new Exception(String.format("exception doInit : %s",
					ex.getMessage()));
		}
	}

	/**
	 * 
	 */
	@Override
	public void process() throws Exception {
		if (this.dbLayer == null) {
			throw new Exception("dbLayer object is null");
		}
	}

	/**
	 * 
	 */
	@Override
	public void exit() throws Exception {
		logger.debug(String.format("exit"));

		if (this.dbLayer != null) {
			this.dbLayer.closeSession();
		}

	}

	/**
	 * 
	 */
	@Override
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
	 * @param schemaFile
	 * @param systemId
	 * @return
	 */
	public static File getSystemConfigurationFile(File schemaFile,String systemId){
		return new File(schemaFile.getParent(),"SystemMonitorNotification_"+systemId+".xml");
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
