package com.sos.scheduler.notification.helper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSString;

import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;
import com.sos.scheduler.notification.plugins.notifier.SystemNotifierProcessBuilderPlugin;

public class ElementNotificationMonitorCommand {
	private Node notificationCommand;
	
	private String command;
	private String plugin;
	
	
	
	public ElementNotificationMonitorCommand(Node nCommand) throws Exception{
		String functionName = "ElementNotificationMonitorCommand";
				
		this.notificationCommand = nCommand;
		
		Element el = (Element)this.notificationCommand;
		String c = NotificationXmlHelper.getValue(el);
		if(!SOSString.isEmpty(c)){
			this.command = c.trim();
		}
		else{
			throw new Exception(String.format("%s: not found value on %s", 
					functionName,
					this.notificationCommand.getNodeName()));
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
			pluginObject = new SystemNotifierProcessBuilderPlugin();
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

	public String getCommand() {
		return this.command;
	}

	public Node getXml() {
		return this.notificationCommand;
	}
	
}
