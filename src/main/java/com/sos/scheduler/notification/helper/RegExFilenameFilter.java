package com.sos.scheduler.notification.helper;

import java.io.File;
import java.io.FilenameFilter;

public class RegExFilenameFilter implements FilenameFilter{

	private String regex;
	
	public RegExFilenameFilter(String val){
		this.regex = val;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.matches(this.regex); 
	}

}
