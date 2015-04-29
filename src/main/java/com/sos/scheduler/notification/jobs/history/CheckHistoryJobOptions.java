package com.sos.scheduler.notification.jobs.history;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "CheckHistoryJobOptions", description = "CheckHistoryJobOptions")
public class CheckHistoryJobOptions extends NotificationJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	private final String conClassName = CheckHistoryJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(CheckHistoryJobOptions.class);

	/**
	 * \var plugins :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "plugins", description = "", key = "plugins", type = "SOSOptionString", mandatory = false)
	public SOSOptionString plugins = new SOSOptionString(this, conClassName
			+ ".plugins", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getplugins :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getplugins() {
		return plugins;
	}

	/**
	 * \brief setplugins :
	 * 
	 * \details
	 * 
	 * 
	 * @param plugins
	 *            :
	 */
	public void setplugins(SOSOptionString p_plugins) {
		this.plugins = p_plugins;
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
			" ", // InitValue
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
	public void setschema_configuration_file(SOSOptionString p_schema_file) {
		this.schema_configuration_file = p_schema_file;
	}

	/**
	 * \var configuration_dir :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "configuration_dir", description = "", key = "configuration_dir", type = "SOSOptionString", mandatory = false)
	public SOSOptionString configuration_dir = new SOSOptionString(this,
			conClassName + ".configuration_dir", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getconfiguration_dir :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getconfiguration_dir() {
		return configuration_dir;
	}

	/**
	 * \brief setconfiguration_dir :
	 * 
	 * \details
	 * 
	 * 
	 * @param configuration_dir
	 *            :
	 */
	public void setconfiguration_dir(SOSOptionString p_configuration_dir) {
		this.configuration_dir = p_configuration_dir;
	}

	/**
	 * \var max_read_history_interval :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionString", mandatory = true)
	public SOSOptionString max_history_age = new SOSOptionString(this,
			conClassName + ".max_history_age", // HashMap-Key
			"", // Titel
			"1h", // InitValue
			"1h", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getmax_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmax_history_age() {
		return max_history_age;
	}

	/**
	 * \brief setmax_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * @param max_history_age
	 *            :
	 */
	public void setmax_history_age(SOSOptionString p_max_history_age) {
		this.max_history_age = p_max_history_age;
	}
	
	/**
	 * orders mit endTime null werden als uncompleted markiert und werden immer wieder synchronisiert.
	 * max Differenze zwischen currentTime und startTime in Minuten um den "uncompleted" Zustand bei der Synchronisierung zu reduzieren.
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "max_uncompleted_age", description = "", key = "max_uncompleted_age", type = "SOSOptionString", mandatory = false)
	public SOSOptionString max_uncompleted_age = new SOSOptionString(
			this, conClassName + ".max_uncompleted_age", // HashMap-Key
			"", // Titel
			"1d", // InitValue
			"1d", // DefaultValue 1 day (1w 1d 1h 1m)
			false // isMandatory
	);

	/**
	 * \brief getmax_uncompleted_age :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmax_uncompleted_age() {
		return max_uncompleted_age;
	}

	/**
	 * \brief setmax_uncompleted_age :
	 * 
	 * \details
	 * 
	 * 
	 * @param max_uncompleted_age
	 *            :
	 */
	public void setmax_uncompleted_age(
			SOSOptionString p_max_uncompleted_age) {
		this.max_uncompleted_age = p_max_uncompleted_age;
	}


	/**
	 * \var allow_db_dependent_queries :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "allow_db_dependent_queries", description = "", key = "allow_db_dependent_queries", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean allow_db_dependent_queries = new SOSOptionBoolean(
			this, conClassName + ".allow_db_dependent_queries", // HashMap-Key
			"", // Titel
			"true", // InitValue
			"true", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getallow_db_dependent_queries :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getallow_db_dependent_queries() {
		return allow_db_dependent_queries;
	}

	/**
	 * \brief setallow_db_dependent_queries :
	 * 
	 * \details
	 * 
	 * 
	 * @param allow_db_dependent_queries
	 *            :
	 */
	public void setallow_db_dependent_queries(
			SOSOptionBoolean p_allow_db_dependent_queries) {
		this.allow_db_dependent_queries = p_allow_db_dependent_queries;
	}
	
	/**
	 * \var batch_size :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger batch_size = new SOSOptionInteger(
			this, conClassName + ".batch_size", // HashMap-Key
			"", // Titel
			"100", // InitValue
			"100", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getbatch_size :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getbatch_size() {
		return batch_size;
	}

	/**
	 * \brief setbatch_size :
	 * 
	 * \details
	 * 
	 * 
	 * @param batch_size
	 *            :
	 */
	public void setbatch_size(
			SOSOptionInteger p_batch_size) {
		this.batch_size = p_batch_size;
	}

} // public class CheckHistoryJobOptionsSuperClass