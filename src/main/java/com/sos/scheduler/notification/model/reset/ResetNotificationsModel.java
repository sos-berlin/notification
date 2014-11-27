package com.sos.scheduler.notification.model.reset;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.scheduler.notification.jobs.reset.ResetNotificationsJobOptions;
import com.sos.scheduler.notification.model.NotificationModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class ResetNotificationsModel extends NotificationModel {
	
	final Logger logger = LoggerFactory.getLogger(ResetNotificationsModel.class);
	
	ResetNotificationsJobOptions options = null;
	
	/**
     * 
     * @param pOptions
     */
    public ResetNotificationsModel(
    		ResetNotificationsJobOptions opt){
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
     */
    @Override
    public void process() throws Exception{
    	logger.info(String.format("process"));
    	
    	super.process();
    	
   		logger.info(String.format("process: operation = %s",
   				this.options.operation.Value()));
    		
   		if(this.options.operation.Value().toLowerCase().equals(OPERATION_ACKNOWLEDGE)){
   			this.resetAcknowledged(this.options.system_id.Value(),this.options.service_name.Value());
   		}
   		else if(this.options.operation.Value().toLowerCase().equals(OPERATION_RESET_SERVICES)){
   			this.resetServices();
   		}
   		else{
   			throw new Exception(String.format("unknown operation = %s",this.options.operation.Value()));
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
    
    /**
     * 
     * @param systemId
     * @param serviceName
     * @throws Exception
     */
    private void resetAcknowledged(String systemId, String serviceName) throws Exception{
    	String functionName = "resetAcknowledged";
    	
    	if(SOSString.isEmpty(systemId)){
			throw new Exception(String.format("missing system_id"));
		}
		
		logger.info(String.format("%s: system id = %s, serviceName = %s",
				functionName,
				systemId,
				serviceName));
    	  	
    	try {
			this.getDbLayer().beginTransaction();
			int count = this.getDbLayer().resetAcknowledged(systemId,serviceName);
			
			logger.info(String.format("%s: updated %s",functionName,count));
			
			this.getDbLayer().commit();
		} catch (Exception ex) {
			try {
				this.getDbLayer().rollback();
			} catch (Exception x) {
			}
			throw ex;
		}
    }
    
     /**
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
	private File initConfig() throws Exception{
    	
    	//@TODO musst gemacht werden
    	/**
    	LinkedHashMap<String,ArrayList<String>> jobChains = new LinkedHashMap<String,ArrayList<String>>();
    	LinkedHashMap<String,ArrayList<String>> jobs = new LinkedHashMap<String,ArrayList<String>>();
    	
    	
    	File xmlFile = new File(options.configuration_file.Value().replaceAll("\\{system_id\\}",options.system_id.Value()));
		File schemaFile = new File(
				options.configuration_schema_file.Value());

		if (!schemaFile.exists()) {
			throw new Exception(String.format("schema file not found: %s",
					schemaFile.getAbsolutePath()));
		}

		if (!xmlFile.exists()) {
			throw new Exception(String.format("xml file not found: %s",
					xmlFile.getAbsolutePath()));
		}
		
		logger.info(String.format("xml config file: %s",xmlFile.getAbsolutePath()));
		
		SystemMonitorNotification config = this.getSystemMonitorConfig(
				schemaFile, xmlFile);
		
		for (Notification n : config.getNotification()) {

			NotificationMonitor nm = n.getNotificationMonitor();
			if (nm != null 
					&& nm.getServiceNameOnError() != null
					&& nm.getServiceNameOnError().equalsIgnoreCase(
							this.options.service_name.Value())) {
				NotificationObject no = n.getNotificationObject();
				for (Object o : no.getJobsOrJobChainsOrTimers()) {
					if (o instanceof JobChains) {
						JobChains jcts = (JobChains) o;
						for (JobChain jct : jcts.getJobChain()) {
							jobChains = fillConfigHashMap(jobChains,
									jct.getSchedulerId(), jct.getName());
						}
					} else if (o instanceof Jobs) {
						Jobs js = (Jobs) o;
						for (JobType j : js.getJob()) {
							jobs = fillConfigHashMap(jobs, j.getSchedulerId(),
									j.getName());
						}
					}
				}
			}
		}

		this.setConfigJobChains(jobChains);
		this.setConfigJobs(jobs);
		return xmlFile;
		
		*return null miust entfernt bzw überprüft werden
		*/
		return null;
    }
    
}
