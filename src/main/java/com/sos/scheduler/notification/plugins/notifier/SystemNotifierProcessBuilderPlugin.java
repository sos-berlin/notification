package com.sos.scheduler.notification.plugins.notifier;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.ElementNotificationMonitorCommand;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;

public class SystemNotifierProcessBuilderPlugin extends SystemNotifierPlugin {
	final Logger logger = LoggerFactory.getLogger(SystemNotifierProcessBuilderPlugin.class);
		
	@Override
	public void init(ElementNotificationMonitor monitor) throws Exception{
		super.init(monitor);
		
		ElementNotificationMonitorCommand configuredCommand = this.getNotificationMonitor().getMonitorCommand();
		if(configuredCommand == null){
			throw new Exception(String.format("%s: Command element is missing (not configured)"
					,getClass().getSimpleName()));
		}
		setCommand(configuredCommand.getCommand());
	}

	/**
	 * @TODO not implemented yet
	 */
	@Override
	public int notifySystemReset(
			String serviceName,
			EServiceStatus status,
			EServiceMessagePrefix prefix,
			String message)
			throws Exception {
		
		return 0;
	}
	
	/**
	 * @TODO calculate elapsed etc. 
	 */
	@Override
	public int notifySystem(Spooler spooler,
			SystemNotifierJobOptions options,
			DBLayerSchedulerMon dbLayer, 
			DBItemSchedulerMonNotifications notification,
			DBItemSchedulerMonSystemNotifications systemNotification,
			DBItemSchedulerMonChecks check,
			EServiceStatus status,
			EServiceMessagePrefix prefix)
			throws Exception {
	
		String method = "notifySystem";
		Process p = null;
		int exitCode = 0;
		try{
			String serviceStatus = this.getServiceStatusValue(status);
			String servicePrefix = this.getServiceMessagePrefixValue(prefix);
			
			resolveCommandAllTableFieldVars(dbLayer, notification,systemNotification,check);
			resolveCommandServiceNameVar(systemNotification.getServiceName());
			resolveCommandServiceStatusVar(serviceStatus);
			resolveCommandServiceMessagePrefixVar(servicePrefix);
			resolveCommandAllEnvVars();
					
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(createProcessBuilderCommand(getCommand()));
			
			logger.info(String.format("command configured = %s",getCommand()));
			logger.info(String.format("command to execute = %s",pb.command()));
						
			//Process ENV Variables setzen
			Map<String,String> env = pb.environment();
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_STATUS",serviceStatus);
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_NAME",systemNotification.getServiceName());
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_MESSAGE_PREFIX",servicePrefix.trim());
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_COMMAND",getCommand());
			if(this.getTableFields() != null){
				for (Entry<String, String> entry : getTableFields().entrySet()) {
					env.put(VARIABLE_ENV_PREFIX_TABLE_FIELD+"_"+entry.getKey().toUpperCase(), 
							normalizeVarValue(entry.getValue()));
				}
			}
						
			p = pb.start();
			if (p.waitFor() != 0) {
		    	exitCode = p.exitValue();
            }
			
			if(exitCode > 0){
			    StringBuffer inputStream = new StringBuffer();
			    StringBuffer errorStream = new StringBuffer();
                
			    Scanner s = null;
			    try{
			        s = new Scanner(p.getInputStream());
			        while(s.hasNext()) { 
			            String m = s.next();
			            if(m.trim().length() > 0){
			                inputStream.append(m.trim());
			                inputStream.append(" ");
			            }
			        }
			    }
			    catch(Exception ex){
			        logger.warn(String.format("error reading process input stream = %s",ex.toString()));
		        }
			    finally{
			        if(s != null){
			            try{s.close();}catch(Exception ex){}
			        }
			    }
			    try{
                    s = new Scanner(p.getErrorStream());
			        while(s.hasNext()) { 
			            String m = s.next();
			            if(m.trim().length() > 0){
			                errorStream.append(m.trim());
			                errorStream.append(" ");
			            }
			        }
			    }
			    catch(Exception ex){
			        logger.warn(String.format("error reading process error stream = %s",ex.toString())); 
			    }
			    finally{
			        if(s != null){
                        try{s.close();}catch(Exception ex){}
                    }
			    }
				
				if(inputStream.length() > 0 || errorStream.length() > 0){
				    throw new Exception(String.format("command executed with exitCode = %s, input stream = %s, error stream = %s",
				            exitCode,
				            inputStream.toString(),
				            errorStream.toString()));
	     		}
			}
			
			logger.info(String.format("command executed with exitCode= %s",exitCode));
			
			return exitCode;
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: %s",method,ex.getMessage()));
		}
		finally{
			try{
				p.destroy();
			}catch(Exception e){}
		}
	}
	
	private boolean isWindows(){
		try{
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}
		catch(Exception x){
			return false;
		}
	}

	private String[] createProcessBuilderCommand(String command){
		String[] c = new String[3];
		//if(OperatingSystem.isWindows){
		if(this.isWindows()){
			String executable = System.getenv("comspec");
			if(SOSString.isEmpty(executable)){
				executable = "cmd.exe";
			}
			c[0] = executable;
			c[1] = "/C";
			c[2] = command;
		}
		else{
			String executable = System.getenv("SHELL");
			if(SOSString.isEmpty(executable)){
				executable = "/bin/sh";
			}
			c[0] = executable;
			c[1] = "-c";
			c[2] = command;
		}
		
	return c;
	}
}
