package com.sos.scheduler.notification.model.reset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.scheduler.notification.jobs.reset.ResetNotificationsJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResetNotificationsModel extends NotificationModel implements INotificationModel {
	
	final Logger logger = LoggerFactory.getLogger(ResetNotificationsModel.class);
	private ResetNotificationsJobOptions options;
	
	
    public ResetNotificationsModel(SOSHibernateConnection conn, 
    		ResetNotificationsJobOptions opt) throws Exception{
    	
    	super(conn);
    	options = opt;
    }
    
    
    /**
     * 
     */
    @Override
    public void process() throws Exception{
    	logger.info(String.format("process"));
    	
    	logger.info(String.format("process: operation = %s",
   				options.operation.Value()));
    		
   		if(options.operation.Value().toLowerCase().equals(OPERATION_ACKNOWLEDGE)){
   			resetAcknowledged(options.system_id.Value(),options.service_name.Value());
   		}
   		else if(options.operation.Value().toLowerCase().equals(OPERATION_RESET_SERVICES)){
   			resetServices();
   		}
   		else{
   			throw new Exception(String.format("unknown operation = %s",options.operation.Value()));
   		}
 	}
    
    /**
     * 
     * @param systemId
     * @param serviceName
     * @throws Exception
     */
    private void resetAcknowledged(String systemId, String serviceName) throws Exception{
		String method = "resetAcknowledged";
    	
    	if(SOSString.isEmpty(systemId)){
			throw new Exception(String.format("missing systemId"));
		}
		
		logger.info(String.format("%s: systemId = %s, serviceName = %s",
				method,
				systemId,
				serviceName));
    	  	
    	try {
			getDbLayer().getConnection().beginTransaction();
			int count = getDbLayer().resetAcknowledged(systemId,serviceName);
			getDbLayer().getConnection().commit();
			
			logger.info(String.format("%s: updated %s",method,count));
			
		} catch (Exception ex) {
			try {
				getDbLayer().getConnection().rollback();
			} catch (Exception x) {
			}
			throw ex;
		}
    }
      
    
   /**
    * 
    * @throws Exception
    */
    private void resetServices() throws Exception{
    	//@TODO musst gemacht werden
    	/**
    	String systemId = this.options.system_id.Value();
		if(SOSString.isEmpty(systemId)){
			throw new Exception(String.format("missing system_id"));
		}
		
		logger.info(String.format("resetServices. system id = %s",systemId));
		
		File schemaFile = new File(this.options.configuration_schema_file.Value());
		File xmlFile = new File(this.options.configuration_file.Value());
		if(!schemaFile.exists()){
			throw new Exception(String.format("schema file not found: %s",schemaFile.getAbsolutePath()));
		}
		if(!xmlFile.exists()){
			throw new Exception(String.format("xml file not found: %s",xmlFile.getAbsolutePath()));
		}
    	
    	SystemMonitorNotification config = this.getSystemMonitorConfig(schemaFile,xmlFile);
    	
    	ArrayList<String> excluded = new ArrayList<String>(); 
    	String[] es = this.options.excluded_services.Value().trim().split(";");
    	for(int i=0;i<es.length;i++){
    		excluded.add(es[i].trim().toLowerCase());
    	}
    	
    	for(Notification n : config.getNotification()){
    		
			NotificationMonitor nm = n.getNotificationMonitor();
			if(nm.getServiceNameOnSuccess() != null && excluded.contains(nm.getServiceNameOnSuccess().toLowerCase())){
				continue;
			}
			
			if(nm.getServiceNameOnError() != null && excluded.contains(nm.getServiceNameOnError().toLowerCase())){
				continue;
			}
			
			
			ISystemNotifierPlugin plugin = null;
			NotificationInterface ni = nm.getNotificationInterface();
			if(ni == null){
				if(SOSString.isEmpty(this.options.plugin.Value())){
					plugin = new SystemNotifierProcessBuilderPlugin();
				}
				else{
					plugin = initializePlugin(this.options.plugin.Value().trim());
				}
			}
			else{
				if(SOSString.isEmpty(this.options.plugin.Value())){
					plugin = new SystemNotifierSendNscaPlugin();
				}
				else{
					plugin = initializePlugin(this.options.plugin.Value().trim());
				}
			}
			//NEU END
			logger.debug(String.format("using plugin = %s",plugin.getClass().getSimpleName()));
			plugin.init(nm);
			if(nm.getServiceNameOnSuccess() != null && !excluded.contains(nm.getServiceNameOnSuccess().toLowerCase())){
				try{
					plugin.notifySystemReset(nm.getServiceNameOnSuccess(), 
							ServiceStatus.OK, 
							ServiceMessagePrefix.RESET,
							this.options.message.Value());
				}
				catch(Exception ex){
					logger.warn(String.format("could not send %s to %s",
							OPERATION_RESET_SERVICES,
							nm.getServiceNameOnSuccess()));
				}
			}
			
			if(nm.getServiceNameOnError() != null && !excluded.contains(nm.getServiceNameOnError().toLowerCase())){
				try{
					plugin.notifySystemReset(nm.getServiceNameOnError(), 
							ServiceStatus.OK, 
							ServiceMessagePrefix.RESET,
							this.options.message.Value());
				}
				catch(Exception ex){
					logger.warn(String.format("could not send %s to %s",
							OPERATION_RESET_SERVICES,
							nm.getServiceNameOnError()));
				}
			}
    	}*/
    }
    
   
}
