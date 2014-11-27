package com.sos.scheduler.notification.model.cleanup;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.jobs.cleanup.CleanupNotificationsJobOptions;
import com.sos.scheduler.notification.model.NotificationModel;

public class CleanupNotificationsModel extends NotificationModel {
	
	final Logger logger = LoggerFactory.getLogger(CleanupNotificationsModel.class);
	
	CleanupNotificationsJobOptions options = null;
	
	/**
     * 
     * @param pOptions
     */
    public CleanupNotificationsModel(CleanupNotificationsJobOptions opt){
    	this.options = opt;
    }
    
    /**
     * 
     * @throws Exception
     */
    @Override
    public void init() throws Exception{
    	logger.info(String.format("init"));
    	
    	super.doInit(this.options.hibernate_configuration_file.Value(),false);
    }
    
    
    
    /**
     * 
     */
    @Override
    public void exit() throws Exception{
    	logger.info(String.format("exit"));
    	
    	super.exit();
   }
    
    
    /**
     * 
     * @throws Exception
     */
    @Override
    public void process() throws Exception{
    	super.process();
    	
    	try{
    		int minutes = this.options.minutes.value();
    		Date date = DBLayerSchedulerMon.getCurrentDateTimeMinusMinutes(minutes);
    		
    		logger.info(String.format("process: delete where created <= %s minutes ago (%s)",
    				minutes,DBLayerSchedulerMon.getDateAsString(date)));
        			
    		this.getDbLayer().beginTransaction();
    		this.getDbLayer().cleanupNotifications(date);
    		this.getDbLayer().commit();
    	}
    	catch(Exception ex){
    		this.getDbLayer().rollback();
    		throw ex;
    	}
	}
    
}
