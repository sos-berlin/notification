package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBLayerSchedulerMon;

public class ElementNotificationJob {

    private Node xml;
    private ElementNotificationMonitor monitor;
    private String schedulerId;
    private String name;
    private Long notifications;
    private String returnCodeFrom;
    private String returnCodeTo;
    
    public ElementNotificationJob(ElementNotificationMonitor monitor, Node job) {
        this.monitor = monitor;
        this.xml = job;
        Element el = (Element) this.xml;
        this.schedulerId = this.getValue(NotificationXmlHelper.getSchedulerId(el));
        this.name = this.getValue(NotificationXmlHelper.getJobName(el));
        this.notifications = this.getLongValue(NotificationXmlHelper.getNotifications(el));
        this.returnCodeFrom = this.getValue(NotificationXmlHelper.getReturnCodeFrom(el));
        this.returnCodeTo = this.getValue(NotificationXmlHelper.getReturnCodeTo(el));
    }

    private String getValue(String val) {
        return SOSString.isEmpty(val) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : val;
    }

    private Long getLongValue(String val) {
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

    public String getReturnCodeFrom() {
        return this.returnCodeFrom;
    }

    public String getReturnCodeTo() {
        return this.returnCodeTo;
    }
 
}