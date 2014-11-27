package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

public class ElementTimerJobChain {
	private Node xml;
	
	private ElementTimer timer;
	private String schedulerId;
	private String name;
	private String stepFrom;
	private String stepTo;
	
	public ElementTimerJobChain(ElementTimer timer,Node jobChain){
		this.timer = timer;
		
		this.xml = jobChain;
		Element el = (Element)this.xml;
		this.schedulerId = this.getValue(NotificationXmlHelper.getSchedulerId(el));
		this.name = this.getValue(NotificationXmlHelper.getJobChainName(el));
		this.stepFrom = this.getValue(NotificationXmlHelper.getStepFrom(el));
		this.stepTo = this.getValue(NotificationXmlHelper.getStepTo(el));
	}
	
	private String getValue(String val){
		return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
	}
	
	public ElementTimer getTimer() {
		return this.timer;
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
	public String getStepFrom() {
		return this.stepFrom;
	}
	public String getStepTo() {
		return this.stepTo;
	}
	
}
