package com.sos.scheduler.notification.jobs.notifier;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "SystemNotifierJobOptions", description = "SystemNotifierJobOptions")
public class SystemNotifierJobOptions extends NotificationJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	private final String conClassName = SystemNotifierJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SystemNotifierJobOptions.class);

	/**
	 * \var schema_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = true)
	public SOSOptionString schema_configuration_file = new SOSOptionString(
			this, conClassName + ".schema_configuration_file", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getschema_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getschema_configuration_file() {
		return schema_configuration_file;
	}

	/**
	 * \brief setschema_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param schema_configuration_file
	 *            :
	 */
	public void setschema_configuration_file(
			SOSOptionString p_schema_configuration_file) {
		this.schema_configuration_file = p_schema_configuration_file;
	}

	/**
	 * \var system_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "system_configuration_file", description = "", key = "system_configuration_file", type = "SOSOptionString", mandatory = true)
	public SOSOptionString system_configuration_file = new SOSOptionString(
			this, conClassName + ".system_configuration_file", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getsystem_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getsystem_configuration_file() {
		return system_configuration_file;
	}

	/**
	 * \brief setsystem_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param system_configuration_file
	 *            :
	 */
	public void setsystem_configuration_file(
			SOSOptionString p_system_configuration_file) {
		this.system_configuration_file = p_system_configuration_file;
	}

	/**
	 * \var plugin_job_name :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "plugin_job_name", description = "", key = "plugin_job_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString plugin_job_name = new SOSOptionString(this,
			conClassName + ".plugin_job_name", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getplugin_job_name :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getplugin_job_name() {
		return plugin_job_name;
	}

	/**
	 * \brief setplugin_job_name :
	 * 
	 * \details
	 * 
	 * 
	 * @param p_plugin_job_name
	 *            :
	 */
	public void setplugin_job_name(SOSOptionString p_plugin_job_name) {
		this.plugin_job_name = p_plugin_job_name;
	}

}