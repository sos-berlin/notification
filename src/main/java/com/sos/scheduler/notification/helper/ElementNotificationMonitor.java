package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;

public class ElementNotificationMonitor {
	private Node monitor;
	
	private String serviceNameOnError;
	private String serviceNameOnSuccess;
	private String serviceStatusOnError;
	private String serviceStatusOnSuccess;
	private ElementNotificationMonitorInterface monitorInterface;
	private ElementNotificationMonitorCommand monitorCommand;
	
	public ElementNotificationMonitor(Node monitor) throws Exception{
		this.monitor = monitor;
		
		Element el = (Element)this.monitor;
	
		this.serviceNameOnError = NotificationXmlHelper.getServiceNameOnError(el);
		this.serviceNameOnSuccess = NotificationXmlHelper.getServiceNameOnSuccess(el);
		this.serviceStatusOnError = NotificationXmlHelper.getServiceStatusOnError(el);
		this.serviceStatusOnSuccess = NotificationXmlHelper.getServiceStatusOnSuccess(el);
		
		Node mi =  NotificationXmlHelper.selectNotificationInterface(el);
		if(mi != null){
			this.monitorInterface  = new ElementNotificationMonitorInterface(mi);
		}
		Node mc =  NotificationXmlHelper.selectNotificationCommand(el);
		if(mc != null){
			this.monitorCommand  = new ElementNotificationMonitorCommand(mc);
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

	public ISystemNotifierPlugin getPluginObject() throws Exception{
		ISystemNotifierPlugin pluginObject = null;
		
		if(this.monitorCommand != null){
			pluginObject =  this.monitorCommand.getPluginObject();
		}
		else if(this.monitorInterface != null){
			pluginObject = this.monitorInterface.getPluginObject();
		}
		
		if(pluginObject == null){
			throw new Exception("getPluginObject: pluginObject is NULL");
		}
		
		return pluginObject;
	}
	
	public ElementNotificationMonitorInterface getMonitorInterface() {
		return this.monitorInterface;
	}

	public ElementNotificationMonitorCommand getMonitorCommand() {
		return this.monitorCommand;
	}
	
	public Node getXml() {
		return this.monitor;
	}
	
}
