package com.sos.scheduler.notification.helper;

public class ElementTimerScript {
	private String language;
	private String value;
	
	public ElementTimerScript(String l, String s){
		this.language = l;
		this.value = s;
	}
	
	public String getLanguage() {
		return language;
	}
	public String getValue() {
		return value;
	}
}
