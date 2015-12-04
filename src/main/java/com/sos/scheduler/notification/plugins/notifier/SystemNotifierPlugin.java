package com.sos.scheduler.notification.plugins.notifier;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;

public class SystemNotifierPlugin implements ISystemNotifierPlugin {
	final Logger logger = LoggerFactory.getLogger(SystemNotifierPlugin.class);
	
	private ElementNotificationMonitor notificationMonitor = null;
	private String command;
	private Map<String, String> tableFields = null;
	
	public static final String VARIABLE_TABLE_PREFIX_NOTIFICATIONS = "MON_N";
	public static final String VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS = "MON_SN";
	public static final String VARIABLE_TABLE_PREFIX_CHECKS = "MON_C";
		
	public static final String VARIABLE_ENV_PREFIX = "SCHEDULER_MON";
	public static final String VARIABLE_ENV_PREFIX_TABLE_FIELD = VARIABLE_ENV_PREFIX+"_TABLE";
	
	@Override
	public void init(ElementNotificationMonitor monitor) throws Exception {
		notificationMonitor = monitor;	
	}
	
	public void setCommand(String cmd){
		command = cmd;
	}
	
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
	
	public String getServiceStatusValue(EServiceStatus status) throws Exception{
		String method="getServiceStatusValue";
		
		if(this.getNotificationMonitor() == null){
			throw new Exception(String.format("%s: this.getNotificationMonitor() is NULL",method));
		}
		
		/**
		 * e.g Nagios
		 * 0- OK
		 * 1-Warning
		 * 2-Critical
		 * 3-Unknown
		 */
		String serviceStatus = "0";
		if(status.equals(EServiceStatus.OK)){
			if(getNotificationMonitor().getServiceStatusOnSuccess() != null){
				serviceStatus = getNotificationMonitor().getServiceStatusOnSuccess();
			}
			else{
				serviceStatus = EServiceStatus.OK.name();
			}
		}
		else{
			if(this.getNotificationMonitor().getServiceStatusOnError() != null){
				serviceStatus = getNotificationMonitor().getServiceStatusOnError();
			}
			else{
				serviceStatus = EServiceStatus.CRITICAL.name();
			}
		}
		logger.debug(String.format("%s: serviceStatus = %s",method,serviceStatus));
		
		return serviceStatus;
	}
	
	public String getServiceMessagePrefixValue(EServiceMessagePrefix prefix){
		String method="getServiceMessagePrefixValue";
		String servicePrefix = "";
		if(prefix != null && !prefix.equals(EServiceMessagePrefix.NONE)){
			servicePrefix = prefix.name()+" ";
		}
		
		logger.debug(String.format("%s: servicePrefix = %s",method,servicePrefix));
		return servicePrefix;
	}
	
	public void resolveCommandAllTableFieldVars(DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check) throws Exception{
		
		if(command == null){ return;}
		
		setTableFields(notification,systemNotification,check);
		for (Entry<String, String> entry : tableFields.entrySet()) {
			String name = entry.getKey();
			String value = normalizeVarValue(entry.getValue());
			//wegen RegExp
			value = Matcher.quoteReplacement(value);
			command = command.replaceAll("%(?i)" + name + "%", value);
		}
	}
	
	private void setTableFields(DbItem notification,DbItem systemNotification,DbItem check) throws Exception{
		if(notification == null){
			throw new Exception("Cannot get table fields. DbItem notification is null");
		}
		if(systemNotification == null){
			throw new Exception("Cannot get table fields. DbItem systemNotification is null");
		}
		tableFields = new HashMap<String,String>();
		setDbItemTableFields(notification,VARIABLE_TABLE_PREFIX_NOTIFICATIONS);
		setDbItemTableFields(systemNotification,VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS);
		setDbItemTableFields(check == null ? new DBItemSchedulerMonChecks() : check,VARIABLE_TABLE_PREFIX_CHECKS);
		
		//NOTIFICATIONS
		setTableFieldElapsed(
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_TIME_ELAPSED",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_START_TIME",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_END_TIME");
		setTableFieldElapsed(
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_TASK_TIME_ELAPSED",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_TASK_START_TIME",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_TASK_END_TIME");
		setTableFieldElapsed(
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_STEP_TIME_ELAPSED",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_STEP_START_TIME",
				VARIABLE_TABLE_PREFIX_NOTIFICATIONS+"_ORDER_STEP_END_TIME");
		
		//SYSNOTOFICATIONS
		setTableFieldElapsed(
				VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS+"_STEP_TIME_ELAPSED",
				VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS+"_STEP_FROM_START_TIME",
				VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS+"_STEP_TO_END_TIME");
		
		//CHECKS
		setTableFieldElapsed(
				VARIABLE_TABLE_PREFIX_CHECKS+"_STEP_TIME_ELAPSED",
				VARIABLE_TABLE_PREFIX_CHECKS+"_STEP_FROM_START_TIME",
				VARIABLE_TABLE_PREFIX_CHECKS+"_STEP_TO_END_TIME");
		
	}
	
	private void setTableFieldElapsed(String newField,String startTimeField,String endTimeField) throws Exception{
		tableFields.put(newField,"");
		
		if(tableFields.containsKey(startTimeField) && tableFields.containsKey(endTimeField)){
			String vnost = tableFields.get(startTimeField);
			String vnoet = tableFields.get(endTimeField);
			if(!SOSString.isEmpty(vnost) && !SOSString.isEmpty(vnoet)){
				Date dnost = DBLayer.getDateFromString(vnost);
				Date dnoet = DBLayer.getDateFromString(vnoet);
				Long diffSeconds = dnoet.getTime()/1000-dnost.getTime()/1000;
				tableFields.put(newField,diffSeconds.toString());
			}
		}
	}
	
	private void setDbItemTableFields(DbItem obj,String prefix) throws Exception{
		Method[] ms = obj.getClass().getDeclaredMethods();
        for (Method m : ms) {
            if(m.getName().startsWith("get")){
            	Column c = m.getAnnotation(Column.class);
            	if(c != null){
            		String name = c.name().replaceAll("`","");
            		name = prefix+"_"+name;
            		if(!tableFields.containsKey(name)){
            			Object objVal = m.invoke(obj); 
            			String val = ""; 
            			if(objVal != null){
            				if(objVal instanceof Timestamp){
            					val = DBLayer.getDateAsString((Date)objVal);
            				}
            				else if(objVal instanceof Boolean){
            					val = (Boolean)objVal ? "1" : "0";
            				}
            				else{
            					val = objVal.toString();
            				}
            			}
            			
            			tableFields.put(name,val);
            		}
            	}
            }
        }
	}

	public String normalizeVarValue(String value){
		//new lines
		value = value.replaceAll("\\r\\n|\\r|\\n", " ");
		//for values with paths: e.g.: d:\abc
		value = value.replaceAll("\\\\","\\\\\\\\");
		return value;
	}
	
	public void resolveCommandServiceNameVar(String serviceName){
		resolveCommandVar("SERVICE_NAME",serviceName);
	}
	
	public void resolveCommandServiceMessagePrefixVar(String prefix){
		resolveCommandVar("SERVICE_MESSAGE_PREFIX",prefix);
	}
	
	public void resolveCommandServiceStatusVar(String serviceStatus){
		resolveCommandVar("SERVICE_STATUS",serviceStatus);

	}
	
	public void resolveCommandAllEnvVars(){
		@SuppressWarnings("unused")
		String method="resolveCommandAllEnvVars";
		
		if(command == null){ return;}
		
		Map<String, String> envs = System.getenv();  
		for (Map.Entry<String, String> entry : envs.entrySet())  
		{  
			String name  = entry.getKey();
		    String value = this.normalizeVarValue(entry.getValue());
		    value = Matcher.quoteReplacement(value);
		    
		    command = command.replaceAll("%(?i)"+name+"%",value);
		}  
	}
	
	public ElementNotificationMonitor getNotificationMonitor(){
		return notificationMonitor;
	}
	
	public String getCommand(){
		return command;
	}

	public Map<String,String> getTableFields(){
		return tableFields;
	}

	@Override
	public int notifySystemReset(String serviceName, EServiceStatus status,
			EServiceMessagePrefix prefix, String command) throws Exception {
		return 0;
	}
	
	private void resolveCommandVar(String varName, String varValue){
		if(command == null){ return;}
		if(varValue == null){ return;}
		
		command = command.replaceAll("%"+varName+"%",varValue);
	}	

}
