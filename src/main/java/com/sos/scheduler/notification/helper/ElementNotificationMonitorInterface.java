package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

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
	
	
	public ElementNotificationMonitorInterface(Node monitorInterface){
		this.monitorInterface = monitorInterface;
		
		Element el = (Element)this.monitorInterface;
		
		this.serviceHost = NotificationXmlHelper.getServiceHost(el);
		this.monitorHost = NotificationXmlHelper.getMonitorHost(el);
		this.monitorEncryption = NotificationXmlHelper.getMonitorEncryption(el);
		this.monitorPassword = NotificationXmlHelper.getMonitorPassword(el);
		this.command = NotificationXmlHelper.getValue(el);
		if(!SOSString.isEmpty(this.command)){
			this.command = this.command.trim();
		}
		
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
