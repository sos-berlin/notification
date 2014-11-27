package com.sos.scheduler.notification.plugins.notifier;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.persistence.Column;

import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class SystemNotifierPlugin implements ISystemNotifierPlugin {

	private ElementNotificationMonitor notificationMonitor = null;
	private String command;
	private Map<String, String> tableFields = null;
	
	
	public static final String VARIABLE_TABLE_PREFIX_NOTIFICATIONS = "MON_N";
	public static final String VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS = "MON_SN";
	public static final String VARIABLE_TABLE_PREFIX_CHECKS = "MON_C";
		
	public static final String VARIABLE_ENV_PREFIX = "SCHEDULER_MON";
	public static final String VARIABLE_ENV_PREFIX_TABLE_FIELD = VARIABLE_ENV_PREFIX+"_TABLE_FIELD";
		
	final org.slf4j.Logger logger = LoggerFactory
			.getLogger(SystemNotifierPlugin.class);
	
	/**
	 * 
	 */
	@Override
	public void init(ElementNotificationMonitor monitor) throws Exception {
		this.notificationMonitor = monitor;	
	}
	
	/**
	 * 
	 * @param cmd
	 */
	public void setCommand(String cmd){
		this.command = cmd;
	}
	
	/**
	 * 
	 */
	@Override
	public int notifySystem(Spooler spooler, SystemNotifierJobOptions options,
			DBLayerSchedulerMon dbLayer,
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check,
			EServiceStatus status,
			EServiceMessagePrefix prefix) throws Exception {
		return 0;
	}
	
	/**
	 * 
	 * @param status
	 * @return
	 */
	public String getServiceStatusValue(EServiceStatus status) throws Exception{
		String functionName="getServiceStatusValue";
		
		if(this.getNotificationMonitor() == null){
			throw new Exception(String.format("%s: this.getNotificationMonitor() is NULL",functionName));
		}
		
		/**
		 * zB bei Nagios
		 * 0- OK
		 * 1-Warning
		 * 2-Critical
		 * 3-Unknown
		 */
		String serviceStatus = "0";
		if(status.equals(EServiceStatus.OK)){
			if(this.getNotificationMonitor().getServiceStatusOnSuccess() != null){
				serviceStatus = this.getNotificationMonitor().getServiceStatusOnSuccess();
			}
			else{
				serviceStatus = EServiceStatus.OK.name();
			}
		}
		else{
			if(this.getNotificationMonitor().getServiceStatusOnError() != null){
				serviceStatus = this.getNotificationMonitor().getServiceStatusOnError();
			}
			else{
				serviceStatus = EServiceStatus.CRITICAL.name();
			}
		}
		logger.info(String.format("%s: serviceStatus = %s",functionName,serviceStatus));
		
		return serviceStatus;
	}
	
	/**
	 * 
	 * @param prefix
	 * @return
	 */
	public String getServiceMessagePrefixValue(EServiceMessagePrefix prefix){
		String functionName="getServiceMessagePrefixValue";
		String servicePrefix = "";
		if(prefix != null && !prefix.equals(EServiceMessagePrefix.NONE)){
			servicePrefix = prefix.name()+" ";
		}
		
		logger.info(String.format("%s: servicePrefix = %s",functionName,servicePrefix));
		return servicePrefix;
	}
	
	/**
	 * 
	 * @param dbLayer
	 * @param notification
	 * @param systemNotification
	 * @param check
	 * @throws Exception
	 */
	public void resolveCommandAllTableFieldVars(DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check) throws Exception{
		if(this.command == null){ return;}
		
		this.setTableFields(notification,systemNotification,check);
		for (Entry<String, String> entry : tableFields.entrySet()) {
			String name = entry.getKey();
			String value = this.normalizeVarValue(entry.getValue());
			//wegen RegExp
			value = Matcher.quoteReplacement(value);
			this.command = this.command.replaceAll("%(?i)" + name + "%", value);
		}
	}
	
	/**
	 * 
	 * @param notification
	 * @param systemNotification
	 * @param check
	 * @throws Exception
	 */
	private void setTableFields(DbItem notification,DbItem systemNotification,DbItem check) throws Exception{
		if(notification == null){
			throw new Exception("Cannot get table fields. DbItem notification is null");
		}
		if(systemNotification == null){
			throw new Exception("Cannot get table fields. DbItem systemNotification is null");
		}
		this.tableFields = new HashMap<String,String>();
		this.setDbItemTableFields(notification,VARIABLE_TABLE_PREFIX_NOTIFICATIONS);
		this.setDbItemTableFields(systemNotification,VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS);
		this.setDbItemTableFields(check == null ? new DBItemSchedulerMonChecks() : check,VARIABLE_TABLE_PREFIX_CHECKS);
		
		this.setTableFieldElapsed("ORDER");
		this.setTableFieldElapsed("TASK");
	}
	
	/**
	 * 
	 * @param name
	 * @throws Exception
	 */
	private void setTableFieldElapsed(String name) throws Exception{
		String nost = VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_"+name+"_START_TIME";
		String noet = VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_"+name+"_END_TIME";
		if(this.tableFields.containsKey(nost) && this.tableFields.containsKey(noet)){
			String vnost = this.tableFields.get(nost);
			String vnoet = this.tableFields.get(noet);
			if(!SOSString.isEmpty(vnost) && !SOSString.isEmpty(vnoet)){
				Date dnost = DBLayerSchedulerMon.getDateFromString(vnost);
				Date dnoet = DBLayerSchedulerMon.getDateFromString(vnoet);
				Long diffSeconds = dnoet.getTime()/1000-dnost.getTime()/1000;
				this.tableFields.put(VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_"+name+"_TIME_ELAPSED",diffSeconds.toString());
			}
			else{
				this.tableFields.put(VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_"+name+"_TIME_ELAPSED","");
			}
		}
		else{
			this.tableFields.put(VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_"+name+"_TIME_ELAPSED","");
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @param prefix
	 * @throws Exception
	 */
	private void setDbItemTableFields(DbItem obj,String prefix) throws Exception{
		Method[] ms = obj.getClass().getDeclaredMethods();
        for (Method m : ms) {
            if(m.getName().startsWith("get")){
            	Column c = m.getAnnotation(Column.class);
            	if(c != null){
            		String name = c.name().replaceAll("`","");
            		name = prefix+"_"+name;
            		if(!this.tableFields.containsKey(name)){
            			Object objVal = m.invoke(obj); 
            			String val = ""; 
            			if(objVal != null){
            				if(objVal instanceof Timestamp){
            					val = DBLayerSchedulerMon.getDateAsString((Date)objVal);
            				}
            				else if(objVal instanceof Boolean){
            					val = (Boolean)objVal ? "1" : "0";
            				}
            				else{
            					val = objVal.toString();
            				}
            			}
            			
            			this.tableFields.put(name,val);
            		}
            	}
            }
        }
	}
	

	
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public String normalizeVarValue(String value){
		//new lines
		value = value.replaceAll("\\r\\n|\\r|\\n", " ");
		//sonst gehen die pfade verloren : zb.: d:\abc
		value = value.replaceAll("\\\\","\\\\\\\\");
		return value;
	}
	
	/**
	 * 
	 * @param serviceName
	 */
	public void resolveCommandServiceNameVar(String serviceName){
		this.resolveCommandVar("SERVICE_NAME",serviceName);
	}
	
	/**
	 * 
	 * @param prefix
	 */
	public void resolveCommandServiceMessagePrefixVar(String prefix){
		this.resolveCommandVar("SERVICE_MESSAGE_PREFIX",prefix);
	}
	
	/**
	 * 
	 * @param serviceStatus
	 */
	public void resolveCommandServiceStatusVar(String serviceStatus){
		this.resolveCommandVar("SERVICE_STATUS",serviceStatus);

	}
	
	
	/**
	 * 
	 */
	public void resolveCommandAllEnvVars(){
		@SuppressWarnings("unused")
		String functionName="resolveCommandAllEnvVars";
		
		if(this.command == null){ return;}
		
		Map<String, String> envs = System.getenv();  
		for (Map.Entry<String, String> entry : envs.entrySet())  
		{  
			String name  = entry.getKey();
		    String value = this.normalizeVarValue(entry.getValue());
		    //wegen RegExp
			value = Matcher.quoteReplacement(value);
		    
			//logger.debug(String.format("%s: env var: name = %s, value = %S",functionName,name,value));
		    this.command = this.command.replaceAll("%(?i)"+name+"%",value);
		}  
	}
	
	/**
	 * 
	 * @return
	 */
	public ElementNotificationMonitor getNotificationMonitor(){
		return this.notificationMonitor;
	}
	
    /**
     * 	
     * @return
     */
	public String getCommand(){
		return this.command;
	}

	public Map<String,String> getTableFields(){
		return this.tableFields;
	}

	/**
	 * 
	 */
	@Override
	public int notifySystemReset(String serviceName, EServiceStatus status,
			EServiceMessagePrefix prefix, String command) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 
	 * @param varName
	 * @param valValue
	 */
	private void resolveCommandVar(String varName, String varValue){
		if(this.command == null){ return;}
		if(varValue == null){ return;}
		
		this.command = this.command.replaceAll("%"+varName+"%",varValue);

	}	

}
