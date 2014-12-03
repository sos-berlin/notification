package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.scheduler.notification.plugins.notifier.SystemNotifierSendNscaPlugin;

public class ElementNotificationMonitorInterface {
	private Node monitorInterface;
	
	private String serviceHost;
	private int monitorPort;
	private String monitorHost;
	private String monitorEncryption;
	private String monitorPassword;
	private int monitorConnectionTimeout;
	private int monitorResponseTimeout;
	private String command;
	private String plugin;
	
	
	
	public ElementNotificationMonitorInterface(Node monitorInterface){
		this.monitorInterface = monitorInterface;
		
		Element el = (Element)this.monitorInterface;
		
		this.serviceHost = NotificationXmlHelper.getServiceHost(el);
		this.monitorHost = NotificationXmlHelper.getMonitorHost(el);
		this.monitorEncryption = NotificationXmlHelper.getMonitorEncryption(el);
		this.monitorPassword = NotificationXmlHelper.getMonitorPassword(el);
		
		this.monitorPort = -1;
		this.monitorConnectionTimeout = -1;
		this.monitorResponseTimeout = -1;
		String mp = NotificationXmlHelper.getMonitorPort(el);
		String ct = NotificationXmlHelper.getMonitorConnectionTimeout(el);
		String rt = NotificationXmlHelper.getMonitorResponseTimeout(el);
		try{if(mp != null){	this.monitorPort = Integer.parseInt(mp);}}
		catch(Exception ex){}
		try{if(ct != null){	this.monitorConnectionTimeout = Integer.parseInt(ct);}}
		catch(Exception ex){}
		try{if(rt != null){	this.monitorResponseTimeout = Integer.parseInt(rt);}}
		catch(Exception ex){}
		
		String c = NotificationXmlHelper.getValue(el);
		if(!SOSString.isEmpty(c)){
			this.command = c.trim();
		}
		String p = NotificationXmlHelper.getPlugin(el);
		if(!SOSString.isEmpty(p)){
			this.plugin = p.trim();
		}
		
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public ISystemNotifierPlugin getPluginObject() throws Exception{
		ISystemNotifierPlugin pluginObject = null;

		if (SOSString.isEmpty(this.plugin)) {
			pluginObject = new SystemNotifierSendNscaPlugin();
		} else {
			pluginObject = this.initializePlugin(this.plugin);
		}
		return pluginObject;
	}
	

	/**
	 * 
	 * @param plugin
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private ISystemNotifierPlugin initializePlugin(String plugin) throws Exception{
    	
    	try{
    	 	Class<ISystemNotifierPlugin> c = (Class<ISystemNotifierPlugin>)Class.forName(plugin);
    		return c.newInstance();
    	}
    	catch(Exception ex){
    		throw new Exception(String.format("plugin cannot be initialized(%s) : %s",plugin,ex.getMessage()));
    	}
    }
	
	public String getPlugin() {
		return plugin;
	}

	
	public String getServiceHost() {
		return this.serviceHost;
	}

	public String getMonitorPassword() {
		return this.monitorPassword;
	}
	
	public int getMonitorConnectionTimeout() {
		return this.monitorConnectionTimeout;
	}

	public int getMonitorResponseTimeout() {
		return this.monitorResponseTimeout;
	}
	
	public int getMonitorPort() {
		return this.monitorPort;
	}
	
	public String getMonitorHost() {
		return this.monitorHost;
	}

	public String getMonitorEncryption() {
		return this.monitorEncryption;
	}

	public String getCommand() {
		return this.command;
	}

	public Node getXml() {
		return this.monitorInterface;
	}
	
}
