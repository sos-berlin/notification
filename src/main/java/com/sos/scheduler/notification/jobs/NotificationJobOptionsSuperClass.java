package com.sos.scheduler.notification.jobs;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * 
 * @author Robert Ehrlich
 *
 */
@JSOptionClass(name = "NotificationJobOptionsSuperClass", description = "NotificationJobOptionsSuperClass")
public class NotificationJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = NotificationJobOptionsSuperClass.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory
			.getLogger(NotificationJobOptionsSuperClass.class);

	/**
	 * \var hibernate_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = true)
	public SOSOptionString hibernate_configuration_file = new SOSOptionString(
			this, conClassName + ".hibernate_configuration_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief gethibernate_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString gethibernate_configuration_file() {
		return hibernate_configuration_file;
	}

	/**
	 * \brief sethibernate_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param hibernate_configuration_file
	 *            :
	 */
	public void sethibernate_configuration_file(SOSOptionString val) {
		this.hibernate_configuration_file = val;
	}

	/**
	 * \var connection_transaction_isolation : Default 2 wegen Oracle, weil
	 * Oracle kein TRANSACTION_READ_UNCOMMITTED unterstützt, sonst wäre 1
	 * 
	 */
	@JSOptionDefinition(name = "connection_transaction_isolation", description = "", key = "connection_transaction_isolation", type = "SOSOptionInterval", mandatory = false)
	public SOSOptionInteger connection_transaction_isolation = new SOSOptionInteger(
			this, conClassName + ".connection_transaction_isolation", // HashMap-Key
			"", // Titel
			"2", // InitValue
			"2", // 1 = TRANSACTION_READ_UNCOMMITTED, 2 =
					// TRANSACTION_READ_COMMITTED
			false // isMandatory
	);

	/**
	 * \brief getconnection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getconnection_transaction_isolation() {
		return connection_transaction_isolation;
	}

	/**
	 * \brief setconnection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_transaction_isolation
	 *            :
	 */
	public void setconnection_transaction_isolation(
			SOSOptionInteger p_connection_transaction_isolation) {
		this.connection_transaction_isolation = p_connection_transaction_isolation;
	}

	/**
	 * \var connection_autocommit :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "connection_autocommit", description = "", key = "connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean connection_autocommit = new SOSOptionBoolean(this,
			conClassName + ".connection_autocommit", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", //
			false // isMandatory
	);

	/**
	 * \brief getconnection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getconnection_autocommit() {
		return connection_autocommit;
	}

	/**
	 * \brief setconnection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_autocommit
	 *            :
	 */
	public void setconnection_autocommit(
			SOSOptionBoolean p_connection_autocommit) {
		this.connection_autocommit = p_connection_autocommit;
	}

	/**
	 * 
	 */
	public NotificationJobOptionsSuperClass() {
		this.objParentClass = this.getClass();
	}

	/**
	 * 
	 * @param listener
	 */
	public NotificationJobOptionsSuperClass(JSListener listener) {
		this();
		this.registerMessageListener(listener);
	}

	/**
	 * 
	 * @param jsSettings
	 * @throws Exception
	 */
	public NotificationJobOptionsSuperClass(HashMap<String, String> jsSettings)
			throws Exception {
		this();
		this.setAllOptions(jsSettings);
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
		final String conMethodName = conClassName + "::getAllOptionsAsString";

		return conClassName + "\n" + this.toString();
	}

	/**
	 * 
	 */
	public void setAllOptions(HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	}

	/**
	 * 
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}

	/**
	 * 
	 */
	@Override
	public void CommandLineArgs(String[] args) {
		super.CommandLineArgs(args);
		this.setAllOptions(super.objSettings);
	}
}