package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.scheduler.notification.plugins.notifier.SystemNotifierProcessBuilderPlugin;
import com.sos.scheduler.notification.plugins.notifier.SystemNotifierSendNscaPlugin;

public class ElementNotificationMonitor {
	private Node monitor;
	
	private String serviceNameOnError;
	private String serviceNameOnSuccess;
	private String serviceStatusOnError;
	private String serviceStatusOnSuccess;
	private String plugin;
	private String command;
	private ElementNotificationMonitorInterface monitorInterface;
	
	
	public ElementNotificationMonitor(Node monitor) throws Exception{
		this.monitor = monitor;
		
		Element el = (Element)this.monitor;
	
		this.serviceNameOnError = NotificationXmlHelper.getServiceNameOnError(el);
		this.serviceNameOnSuccess = NotificationXmlHelper.getServiceNameOnSuccess(el);
		this.serviceStatusOnError = NotificationXmlHelper.getServiceStatusOnError(el);
		this.serviceStatusOnSuccess = NotificationXmlHelper.getServiceStatusOnSuccess(el);
		this.plugin = NotificationXmlHelper.getPlugin(el);
		
		Node c = NotificationXmlHelper.selectNotificationCommand(el);
		if(c != null){
			this.command  = NotificationXmlHelper.getValue((Element)c);
			if(!SOSString.isEmpty(this.command)){
				this.command = this.command.trim();
			}
		}
		
		Node mi =  NotificationXmlHelper.selectNotificationInterface(el);
		if(mi != null){
			this.monitorInterface  = new ElementNotificationMonitorInterface(mi);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public ISystemNotifierPlugin getPluginObject() throws Exception{
		ISystemNotifierPlugin pluginObject = null;

		if (this.monitorInterface == null) {
			if (SOSString.isEmpty(this.plugin)) {
				pluginObject = new SystemNotifierProcessBuilderPlugin();
			} else {
				pluginObject = this.initializePlugin(this.plugin);
			}
		} else {
			if (SOSString.isEmpty(this.plugin)) {
				pluginObject = new SystemNotifierSendNscaPlugin();
			} else {
				pluginObject = this.initializePlugin(this.plugin);
			}
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
		
	public String getServiceNameOnSuccess() {
		return serviceNameOnSuccess;
	}

	public void setServiceNameOnSuccess(String serviceNameOnSuccess) {
		this.serviceNameOnSuccess = serviceNameOnSuccess;
	}

	public String getServiceNameOnError() {
		return serviceNameOnError;
	}

	public String getServiceStatusOnError() {
		return serviceStatusOnError;
	}

	public String getServiceStatusOnSuccess() {
		return serviceStatusOnSuccess;
	}

	public String getPlugin() {
		return plugin;
	}

	public String getCommand() {
		return command;
	}

	public ElementNotificationMonitorInterface getMonitorInterface() {
		return monitorInterface;
	}

	public Node getXml() {
		return this.monitor;
	}
	
}
