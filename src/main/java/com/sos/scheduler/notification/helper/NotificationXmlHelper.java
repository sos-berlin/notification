package com.sos.scheduler.notification.helper;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

public class NotificationXmlHelper {

	public static String getSystemMonitorNotificationSystemId(SOSXMLXPath xpath) throws Exception{
		if(xpath.root == null){
			throw new Exception("xpath.root is NULL");
		}
		if(!xpath.root.getNodeName().equalsIgnoreCase("SystemMonitorNotification")){
			throw new Exception(String.format("root is %s and not the SystemMonitorNotification Element",xpath.root.getNodeName()));
		}
		return xpath.root.getAttribute("system_id");
	}
	
	public static NodeList selectNotificationJobChainDefinitions(SOSXMLXPath xpath) throws Exception{
		
		return xpath.selectNodeList("/SystemMonitorNotification/Notification/NotificationObjects/JobChain");
	}

    public static NodeList selectNotificationMonitorOnErrorDefinitions(SOSXMLXPath xpath) throws Exception{
		
		return xpath.selectNodeList("/SystemMonitorNotification/Notification/NotificationMonitor[@service_name_on_error]");
	}
	
    public static NodeList selectNotificationMonitorOnSuccessDefinitions(SOSXMLXPath xpath) throws Exception{
		
		return xpath.selectNodeList("/SystemMonitorNotification/Notification/NotificationMonitor[@service_name_on_success]");
	}
	
    public static NodeList selectTimerJobChainDefinitions(SOSXMLXPath xpath) throws Exception{
		
		return xpath.selectNodeList("/SystemMonitorNotification/Timer/TimerJobChain");
	}

	public static NodeList selectTimerDefinitions(SOSXMLXPath xpath) throws Exception{
		
		return xpath.selectNodeList("/SystemMonitorNotification/Timer");
	}
	
	public static NodeList selectNotificationDefinitions(SOSXMLXPath xpath) throws Exception{
		return xpath.selectNodeList("/SystemMonitorNotification/Notification");
	}
	
	public static Node selectNotificationMonitor(SOSXMLXPath xpath,Node notification) throws Exception{
		return xpath.selectSingleNode(notification,"NotificationMonitor");
	}
	
	public static NodeList selectNotificationJobChains(SOSXMLXPath xpath,Node notification) throws Exception{
		return xpath.selectNodeList(notification,"NotificationObjects/JobChain");
	}
	
	public static NodeList selectNotificationTimers(SOSXMLXPath xpath,Node notification) throws Exception{
		return xpath.selectNodeList(notification,"NotificationObjects/Timer");
	}
	
	
	public static String getNotificationMonitorPlugin(Node monitor) throws Exception{
		return ((Element)monitor).getAttribute("plugin");
	}
	public static String getNotificationMonitorServiceNameOnError(Node monitor) throws Exception{
		return ((Element)monitor).getAttribute("service_name_on_error");
	}
	public static String getNotificationMonitorServiceNameOnSuccess(Node monitor) throws Exception{
		return ((Element)monitor).getAttribute("service_name_on_success");
	}
	public static String getNotificationMonitorServiceStatusOnError(Node monitor) throws Exception{
		return ((Element)monitor).getAttribute("service_status_on_error");
	}
	public static String getNotificationMonitorServiceStatusOnSuccess(Node monitor) throws Exception{
		return ((Element)monitor).getAttribute("service_status_on_success");
	}

	
	public static Node selectNotificationMonitorCommand(SOSXMLXPath xpath,Node monitor) throws Exception{
		return xpath.selectSingleNode(monitor,"//NotificationCommand");
	}
	
	public static NodeList selectNotificationMonitorNotificationObjects(SOSXMLXPath xpath,Node monitor) throws Exception{
		return xpath.selectNodeList(monitor,"../NotificationObjects/*");
	}
	
	public static Node selectNotificationMonitorInterface(SOSXMLXPath xpath,Node monitor) throws Exception{
		return xpath.selectSingleNode(monitor,"//NotificationInterface");
	}
	
	public static String getTimerName(Element el){
		return el.getAttribute("name");
	}
	
	public static String getTimerRef(Element el){
		return el.getAttribute("ref");
	}
	
	public static String getNotifications(Element el){
		return el.getAttribute("notifications");
	}
	
	public static String getSchedulerId(Element n){
		return n.getAttribute("scheduler_id");
	}
	
	public static String getJobChainName(Element n){
		return n.getAttribute("name");
	}
	
	public static String getStepFrom(Element n){
		return n.getAttribute("step_from");
	}
	
	public static String getStepTo(Element n){
		return n.getAttribute("step_to");
	}
	
	public static String getExcludedSteps(Element n){
		return n.getAttribute("excluded_steps");
	}
	
	public static String getServiceHost(Element n){
		return n.getAttribute("service_host");
	}
	
	public static String getMonitorPort(Element n){
		return n.getAttribute("monitor_port");
	}
	
	public static String getMonitorHost(Element n){
		return n.getAttribute("monitor_host");
	}
	
	public static String getMonitorEncryption(Element n){
		return n.getAttribute("monitor_encryption");
	}
	
	public static String getMonitorPassword(Element n){
		return n.getAttribute("monitor_password");
	}
	
	public static String getMonitorConnectionTimeout(Element n){
		return n.getAttribute("monitor_connection_timeout");
	}
	
	public static String getMonitorResponseTimeout(Element n){
		return n.getAttribute("monitor_response_timeout");
	}
	
	public static String getPlugin(Element el){
		return el.getAttribute("plugin");
	}
	
	public static String getServiceNameOnError(Element el){
		return el.getAttribute("service_name_on_error");
	}
	
	public static String getServiceNameOnSuccess(Element el){
		return el.getAttribute("service_name_on_success");
	}
	
	public static String getServiceStatusOnError(Element el){
		return el.getAttribute("service_status_on_error");
	}
	
	public static String getServiceStatusOnSuccess(Element el){
		return el.getAttribute("service_status_on_success");
	}
	
	public static String getTimerScriptLanguage(Element el){
		return el.getAttribute("language");
	}
	
	public static String getTimerNotifyOnError(Element el){
		return el.getAttribute("notify_on_error");
	}
	public static String getValue(Element n){
		return n.getTextContent();
	}
	
	public static NodeList selectTimerJobChains(Node timer) throws Exception{
		
		return ((Element)timer).getElementsByTagName("TimerJobChain");
	}
	
    public static Node selectNotificationCommand(Element monitor) throws Exception{
    	NodeList nl = monitor.getElementsByTagName("NotificationCommand");
		return nl == null ? null : nl.item(0);
	}
	
    public static Node selectNotificationInterface(Element monitor) throws Exception{
    	NodeList nl = monitor.getElementsByTagName("NotificationInterface");
		return nl == null ? null : nl.item(0);
	}
    
    /**
    public static Node selectTimerMinimum(Element timer) throws Exception{
    	NodeList nl = timer.getElementsByTagName("Minimum");
		return nl == null ? null : nl.item(0);
	}
    public static Node selectTimerMaximum(Element timer) throws Exception{
    	NodeList nl = timer.getElementsByTagName("Maximum");
		return nl == null ? null : nl.item(0);
	}
    public static Node selectTimerMaximumOrMinimumScript(Element timerMaximumOrMinimum) throws Exception{
    	NodeList nl = timerMaximumOrMinimum.getElementsByTagName("Script");
		return nl == null ? null : nl.item(0);
	}*/
   public static ElementTimerScript getTimerMinimum(XPath xPath,Node timer) throws Exception{
		
	return getTimerScriptElement(xPath,timer, "Minimum");
	}
	
	public static ElementTimerScript getTimerMaximum(XPath xPath,Node timer) throws Exception{
		
	return getTimerScriptElement(xPath,timer, "Maximum");
	}
	
	private static ElementTimerScript getTimerScriptElement(XPath xPath,Node timer,String name) throws Exception{
		//XPath xPath 	=  XPathFactory.newInstance().newXPath();
		String language = xPath.compile(name+"/Script/@language").evaluate(timer);
		String script   = xPath.compile(name+"/Script").evaluate(timer);
		if(!SOSString.isEmpty(language) && !SOSString.isEmpty(script)){
			return new ElementTimerScript(language,script);
		}
	return null;
	}
	
	
}
