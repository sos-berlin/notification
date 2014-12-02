package com.sos.scheduler.notification.helper;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

public class DBConnector {
	final Logger logger = LoggerFactory.getLogger(DBConnector.class);
	private DBLayerSchedulerMon dbLayer;
	
	/**
	 * 
	 * @param hibernateConfigFile
	 * @param reconnect
	 * @throws Exception
	 */
	public void connect(String hibernateConfigFile, boolean reconnect)
			throws Exception {
		
		String functionName = "connect";
		
		File file = new File(hibernateConfigFile);
		if (!file.exists()) {
			throw new Exception(String.format(
					"%s: not found hibernate config file = %s",
					functionName,
					file.getAbsolutePath()));
		}

		logger.debug(String.format("%s: init connection with hibernate file = %s",
				functionName,
				file.getAbsolutePath()));
		try {
			this.dbLayer = new DBLayerSchedulerMon(file);
			if (reconnect) {
				try {
					if (this.dbLayer.getSession() != null) {
						this.dbLayer.closeSession();
					}
				} catch (Exception ex) {
					logger.error(String.format(
							"%s: dbLayer.closeSession exception : %s",
							functionName,
							ex.getMessage()));
				}
			}
			this.dbLayer.initSession();
		} catch (Exception ex) {
			throw new Exception(String.format("%s: exception : %s",
					functionName,
					ex.getMessage()));
		}
	}

	/**
	 * 
	 * @return
	 */
	public DBLayerSchedulerMon getDbLayer(){
		return this.dbLayer;
	}
	
	/**
	 * 
	 */
	public void disconnect(){
		
		logger.debug(String.format("disconnect"));
	
		if(this.dbLayer != null){
			this.dbLayer.closeSession();
		}		
	}
	
}
