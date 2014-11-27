package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

public class ElementNotificationTimer {
	private ElementNotificationMonitor monitor;
	private Node xml;
	
	private String name;
	private Long notifications;
	private boolean notifyOnError;
	
	public ElementNotificationTimer(ElementNotificationMonitor monitor,Node timer){
		this.monitor = monitor;
		
		this.xml = timer;
		Element el = (Element)this.xml;
		this.name = this.getValue(NotificationXmlHelper.getJobChainName(el));
		this.notifications = this.getLongValue(NotificationXmlHelper.getNotifications(el));
		this.setNotifyOnError(el);		
	}
	
	private void setNotifyOnError(Element el){
		this.notifyOnError = false;
		String noe = NotificationXmlHelper.getTimerNotifyOnError(el);
		try{
			this.notifyOnError = noe == null ? false : Boolean.parseBoolean(noe);
		}
		catch(Exception ex){
		}
	}
	
	
	private String getValue(String val){
		return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
	}
	private Long getLongValue(String val){
		return SOSString.isEmpty(val) ? new Long(1) : new Long(val);
	}
	
	public ElementNotificationMonitor getMonitor() {
		return this.monitor;
	}
	public Node getXml() {
		return this.xml;
	}
	public String getName() {
		return this.name;
	}
	public Long getNotifications() {
		return this.notifications;
	}
	public boolean getNotifyOnError() {
		return this.notifyOnError;
	}
}
