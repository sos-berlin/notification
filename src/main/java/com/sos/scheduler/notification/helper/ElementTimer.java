package com.sos.scheduler.notification.helper;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementTimer {
	private Node xml;
	
	private String name;
	private ArrayList<ElementTimerJobChain> jobChains;
	private ElementTimerScript minimum;
	private ElementTimerScript maximum;
	
	
	public ElementTimer(Node timer) throws Exception{
		
		this.xml = timer;
		Element el = (Element)this.xml;
		this.name = NotificationXmlHelper.getTimerName(el);
		
		this.jobChains = new ArrayList<ElementTimerJobChain>();
		NodeList nl = NotificationXmlHelper.selectTimerJobChains(this.xml);
		for (int i = 0; i < nl.getLength(); i++) {
			this.jobChains.add(new ElementTimerJobChain(this,nl.item(i)));
		}
		
		XPath xPath =  XPathFactory.newInstance().newXPath();	
		this.maximum = NotificationXmlHelper.getTimerMaximum(xPath,this.xml);
		this.minimum = NotificationXmlHelper.getTimerMinimum(xPath,this.xml);
		
	}
	
	public Node getXml() {
		return this.xml;
	}
	public String getName() {
		return this.name;
	}
	public ArrayList<ElementTimerJobChain> getJobChains() {
		return this.jobChains;
	}
	public ElementTimerScript getMinimum() {
		return this.minimum;
	}
	public ElementTimerScript getMaximum() {
		return this.maximum;
	}
}
