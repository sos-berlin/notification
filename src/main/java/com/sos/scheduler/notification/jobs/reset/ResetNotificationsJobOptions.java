package com.sos.scheduler.notification.jobs.reset;

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
@JSOptionClass(name = "ResetNotificationsJobOptionsSuperClass", description = "ResetNotificationsJobOptionsSuperClass")
public class ResetNotificationsJobOptions extends
		NotificationJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	private final String conClassName = ResetNotificationsJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ResetNotificationsJobOptions.class);

	/**
	 * \var system_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "system_configuration_file", description = "", key = "system_configuration_file", mandatory = false)
	public SOSOptionString system_configuration_file = new SOSOptionString(
			this, conClassName + ".system_configuration_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
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
		return this.system_configuration_file;
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
			SOSOptionString p_configuration_file) {
		this.system_configuration_file = p_configuration_file;
	}

	/**
	 * \var schema_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionString schema_configuration_file = new SOSOptionString(
			this, conClassName + ".schema_configuration_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
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
		return this.schema_configuration_file;
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
	 * \var system_id :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "system_id", description = "", key = "system_id", type = "SOSOptionString", mandatory = true)
	public SOSOptionString system_id = new SOSOptionString(this, conClassName
			+ ".system_id", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getsystem_id :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getsystem_id() {
		return this.system_id;
	}

	/**
	 * \brief setsystem_id :
	 * 
	 * \details
	 * 
	 * 
	 * @param system_id
	 *            :
	 */
	public void setsystem_id(SOSOptionString p_system_id) {
		this.system_id = p_system_id;
	}

	/**
	 * \var service_name :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "service_name", description = "", key = "service_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString service_name = new SOSOptionString(this,
			conClassName + ".service_name", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getservice_name :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getservice_name() {
		return this.service_name;
	}

	/**
	 * \brief setservice_name :
	 * 
	 * \details
	 * 
	 * 
	 * @param service_name
	 *            :
	 */
	public void setservice_name(SOSOptionString p_service_name) {
		this.service_name = p_service_name;
	}

	/**
	 * \var operation :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = true)
	public SOSOptionString operation = new SOSOptionString(this, conClassName
			+ ".operation", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getoperation :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getoperation() {
		return this.operation;
	}

	/**
	 * \brief setoperation :
	 * 
	 * \details
	 * 
	 * 
	 * @param operation
	 *            :
	 */
	public void setoperation(SOSOptionString p_operation) {
		this.operation = p_operation;
	}

	/**
	 * \var excluded_services :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "excluded_services", description = "", key = "excluded_services", type = "SOSOptionString", mandatory = false)
	public SOSOptionString excluded_services = new SOSOptionString(this,
			conClassName + ".excluded_services", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getexcluded_services :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getexcluded_services() {
		return this.excluded_services;
	}

	/**
	 * \brief setexcluded_services :
	 * 
	 * \details
	 * 
	 * 
	 * @param excluded_services
	 *            :
	 */
	public void setexcluded_services(SOSOptionString p_excluded_services) {
		this.excluded_services = p_excluded_services;
	}

	/**
	 * \var plugin :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "plugin", description = "", key = "plugin", type = "SOSOptionString", mandatory = false)
	public SOSOptionString plugin = new SOSOptionString(this, conClassName
			+ ".plugin", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getplugin :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getplugin() {
		return this.plugin;
	}

	/**
	 * \brief setplugin :
	 * 
	 * \details
	 * 
	 * 
	 * @param plugin
	 *            :
	 */
	public void setplugin(SOSOptionString p_plugin) {
		this.plugin = p_plugin;
	}

	/**
	 * \var message :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "message", description = "", key = "message", type = "SOSOptionString", mandatory = false)
	public SOSOptionString message = new SOSOptionString(this, conClassName
			+ ".message", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getmessage :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmessage() {
		return this.message;
	}

	/**
	 * \brief setmessage :
	 * 
	 * \details
	 * 
	 * 
	 * @param message
	 *            :
	 */
	public void setmessage(SOSOptionString p_message) {
		this.message = p_message;
	}
}