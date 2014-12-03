package com.sos.scheduler.notification.plugins.notifier;


import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;

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

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class SystemNotifierProcessBuilderPlugin extends SystemNotifierPlugin {

	final org.slf4j.Logger logger = LoggerFactory.getLogger(SystemNotifierProcessBuilderPlugin.class);
	
	/**
	 * 
	 */
	@Override
	public void init(ElementNotificationMonitor monitor) throws Exception{
		super.init(monitor);
		
		ElementNotificationMonitorCommand configuredCommand = this.getNotificationMonitor().getMonitorCommand();
		if(configuredCommand == null){
			throw new Exception(String.format("%s: Command element is missing (not configured)"
					,this.getClass().getSimpleName()));
		}
		this.setCommand(configuredCommand.getCommand());
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
	 * @TODO elapsed etc. ausrechnen
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
	
		String functionName = "notifySystem";
		Process p = null;
		int exitCode = 0;
		try{
			String serviceStatus = this.getServiceStatusValue(status);
			String servicePrefix = this.getServiceMessagePrefixValue(prefix);
			
			this.resolveCommandAllTableFieldVars(dbLayer, notification,systemNotification,check);
			this.resolveCommandServiceNameVar(systemNotification.getServiceName());
			this.resolveCommandServiceStatusVar(serviceStatus);
			this.resolveCommandServiceMessagePrefixVar(servicePrefix);
			this.resolveCommandAllEnvVars();
					
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(this.createProcessBuilderCommand(this.getCommand()));
			
			logger.info(String.format("command configured = %s",this.getCommand()));
			logger.info(String.format("command to execute = %s",pb.command()));
						
			//Process ENV Variables setzen
			Map<String,String> env = pb.environment();
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_STATUS",serviceStatus);
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_NAME",systemNotification.getServiceName());
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_MESSAGE_PREFIX",servicePrefix.trim());
			env.put(VARIABLE_ENV_PREFIX+"_SERVICE_COMMAND",this.getCommand());
			if(this.getTableFields() != null){
				for (Entry<String, String> entry : this.getTableFields().entrySet()) {
					env.put(VARIABLE_ENV_PREFIX_TABLE_FIELD+"_"+entry.getKey().toUpperCase(), 
							normalizeVarValue(entry.getValue()));
				}
			}
						
			p = pb.start();
			if (p.waitFor() != 0) {
		    	exitCode = p.exitValue();
            }
			
			if(exitCode > 0){
				Scanner s = new Scanner(p.getInputStream());
				StringBuffer msg = new StringBuffer();
				while(s.hasNext()) { 
					String m = s.next();
					if(m.trim().length() > 0){
						msg.append(m.trim());
						msg.append(" ");
					}
				}
				s.close();
				
				s = new Scanner(p.getErrorStream());
				while(s.hasNext()) { 
					String m = s.next();
					if(m.trim().length() > 0){
						msg.append(m.trim());
						msg.append(" ");
					}
				}
				s.close();
				
				if(msg.length() > 0){
					throw new Exception(msg.toString());
				}
			}
			
			logger.info(String.format("command executed with exitCode= %s",exitCode));
			
			return exitCode;
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: %s",functionName,ex.getMessage()));
		}
		finally{
			try{
				p.destroy();
			}catch(Exception e){}
		}
	}
	
	/**
	 * Input: cmd.exe /c | c:\xxx\test.exe param param param > test.txt
	 * 
	 * "cmd","/c",this.getCommand
	 * "/bin/sh","-c",this.getCommand
	 * 
	 * @param input
	 * @return
	 */
	private ArrayList<String> splitCommandX(String input) {
		ArrayList<String> result = new ArrayList<String>();
		String[] resultBefore = input.split("\\|", 2);
		if(resultBefore.length > 1){
			String[] arr = resultBefore[0].trim().split(" ");
			for(int i=0;i<arr.length;i++){
				result.add(arr[i]);
			}
			result.add(resultBefore[1].trim());
		}
		else{
			StringTokenizer st = new StringTokenizer(input);
			while(st.hasMoreTokens()) {
				result.add(st.nextToken());
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean isWindows(){
		try{
			return System.getProperty("os.name").toLowerCase().contains("windows");
		}
		catch(Exception x){
			return false;
		}
	}
	/**
	 * 
	 * @param command
	 * @return
	 */
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
