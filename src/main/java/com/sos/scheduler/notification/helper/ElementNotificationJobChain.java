package com.sos.scheduler.notification.helper;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

import sos.util.SOSString;

public class ElementNotificationJobChain {
	private Node xml;
	
	private ElementNotificationMonitor monitor;
	private String schedulerId;
	private String name;
	private Long notifications;
	private String stepFrom;
	private String stepTo;
	private ArrayList<String> excludedSteps;
	
	public ElementNotificationJobChain(ElementNotificationMonitor monitor,Node jobChain){
		this.monitor = monitor;
		
		this.xml = jobChain;
		Element el = (Element)this.xml;
		this.schedulerId = this.getValue(NotificationXmlHelper.getSchedulerId(el));
		this.name = this.getValue(NotificationXmlHelper.getJobChainName(el));
		this.notifications = this.getLongValue(NotificationXmlHelper.getNotifications(el));
		this.stepFrom = this.getValue(NotificationXmlHelper.getStepFrom(el));
		this.stepTo = this.getValue(NotificationXmlHelper.getStepTo(el));
		this.setExcludedSteps(el);
		
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
	public String getSchedulerId() {
		return this.schedulerId;
	}
	public String getName() {
		return this.name;
	}
	public Long getNotifications() {
		return this.notifications;
	}
	public String getStepFrom() {
		return this.stepFrom;
	}
	public String getStepTo() {
		return this.stepTo;
	}
	
	/**
	 * 
	 * @param jobChain
	 */
	private void setExcludedSteps(Element jobChain){
		this.excludedSteps = new ArrayList<String>();
		String es = NotificationXmlHelper.getExcludedSteps(jobChain);
		if(!SOSString.isEmpty(es)){
			String[] arr = es.trim().split(";");
			for(int i=0;i<arr.length;i++){
				if(arr[i].trim().length() > 0){
					this.excludedSteps.add(arr[i].trim());
				}
			}
		}
	}
	
	public ArrayList<String> getExcludedSteps(){
		return this.excludedSteps;
	}
}
