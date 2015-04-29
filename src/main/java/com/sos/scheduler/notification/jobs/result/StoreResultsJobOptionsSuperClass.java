package com.sos.scheduler.notification.jobs.result;

import java.util.HashMap;

import org.apache.log4j.Logger;

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
@JSOptionClass(name = "StoreResultsJobOptionsSuperClass", description = "StoreResultsJobOptionsSuperClass")
public class StoreResultsJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = StoreResultsJobOptionsSuperClass.class.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(StoreResultsJobOptionsSuperClass.class);

	/**
	 * \var scheduler_notification_result_parameters :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "scheduler_notification_result_parameters", description = "", key = "scheduler_notification_result_parameters", type = "SOSOptionString", mandatory = false)
	public SOSOptionString scheduler_notification_result_parameters = new SOSOptionString(
			this, conClassName + ".scheduler_notification_result_parameters", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getscheduler_notification_result_parameters :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getscheduler_notification_result_parameters() {
		return scheduler_notification_result_parameters;
	}

	/**
	 * \brief setscheduler_notification_result_parameters :
	 * 
	 * \details
	 * 
	 * 
	 * @param scheduler_notification_result_parameters
	 *            :
	 */
	public void setscheduler_notification_result_parameters(
			SOSOptionString p_scheduler_notification_result_parameters) {
		this.scheduler_notification_result_parameters = p_scheduler_notification_result_parameters;
	}

	/**
	 * \var hibernate_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "scheduler_notification_hibernate_configuration_file", description = "", key = "scheduler_notification_hibernate_configuration_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionString scheduler_notification_hibernate_configuration_file = new SOSOptionString(
			this, conClassName
					+ ".scheduler_notification_hibernate_configuration_file", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getscheduler_notification_hibernate_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getscheduler_notification_hibernate_configuration_file() {
		return scheduler_notification_hibernate_configuration_file;
	}

	/**
	 * \brief setscheduler_notification_hibernate_configuration_file :
	 * 
	 * \details
	 * 
	 * 
	 * @param scheduler_notification_hibernate_configuration_file
	 *            :
	 */
	public void setscheduler_notification_hibernate_configuration_file(
			SOSOptionString p_hibernate_configuration_file) {
		this.scheduler_notification_hibernate_configuration_file = p_hibernate_configuration_file;
	}

	/**
	 * \var connection_transaction_isolation : Default 2 wegen Oracle, weil
	 * Oracle kein TRANSACTION_READ_UNCOMMITTED unterstützt, sonst wäre 1
	 * 
	 */
	@JSOptionDefinition(name = "scheduler_notification_connection_transaction_isolation", description = "", key = "scheduler_notification_connection_transaction_isolation", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger scheduler_notification_connection_transaction_isolation = new SOSOptionInteger(
			this, conClassName + ".scheduler_notification_connection_transaction_isolation", // HashMap-Key
			"", // Titel
			"2", // InitValue
			"2", // 1 = TRANSACTION_READ_UNCOMMITTED, 2 =
					// TRANSACTION_READ_COMMITTED
			false // isMandatory
	);

	/**
	 * \brief getscheduler_notification_connection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getscheduler_notification_connection_transaction_isolation() {
		return scheduler_notification_connection_transaction_isolation;
	}

	/**
	 * \brief setscheduler_notification_connection_transaction_isolation :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_transaction_isolation
	 *            :
	 */
	public void setscheduler_notification_connection_transaction_isolation(
			SOSOptionInteger val) {
		this.scheduler_notification_connection_transaction_isolation = val;
	}

	/**
	 * \var scheduler_notification_connection_autocommit :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "scheduler_notification_connection_autocommit", description = "", key = "scheduler_notification_connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean scheduler_notification_connection_autocommit = new SOSOptionBoolean(this,
			conClassName + ".scheduler_notification_connection_autocommit", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", //
			false // isMandatory
	);

	/**
	 * \brief getscheduler_notification_connection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getscheduler_notification_connection_autocommit() {
		return scheduler_notification_connection_autocommit;
	}

	/**
	 * \brief setscheduler_notification_connection_autocommit :
	 * 
	 * \details
	 * 
	 * 
	 * @param scheduler_notification_connection_autocommit
	 *            :
	 */
	public void setscheduler_notification_connection_autocommit(
			SOSOptionBoolean val) {
		this.scheduler_notification_connection_autocommit = val;
	}
	
	
	/**
	 * \var mon_results_scheduler_id :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_scheduler_id", description = "", key = "mon_results_scheduler_id", type = "SOSOptionString", mandatory = true)
	public SOSOptionString mon_results_scheduler_id = new SOSOptionString(this,
			conClassName + ".mon_results_scheduler_id", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getmon_results_scheduler_id :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmon_results_scheduler_id() {
		return mon_results_scheduler_id;
	}

	/**
	 * \brief setmon_results_scheduler_id :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_scheduler_id
	 *            :
	 */
	public void setmon_results_scheduler_id(
			SOSOptionString p_mon_results_scheduler_id) {
		this.mon_results_scheduler_id = p_mon_results_scheduler_id;
	}

	/**
	 * \var mon_results_task_id :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_task_id", description = "", key = "mon_results_task_id", type = "SOSOptionInteger", mandatory = true)
	public SOSOptionInteger mon_results_task_id = new SOSOptionInteger(this,
			conClassName + ".mon_results_task_id", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getmon_results_task_id :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getmon_results_task_id() {
		return mon_results_task_id;
	}

	/**
	 * \brief setmon_results_task_id :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_task_id
	 *            :
	 */
	public void setmon_results_task_id(SOSOptionInteger p_mon_results_task_id) {
		this.mon_results_task_id = p_mon_results_task_id;
	}

	/**
	 * \var mon_results_standalone :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_standalone", description = "", key = "mon_results_standalone", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean mon_results_standalone = new SOSOptionBoolean(this,
			conClassName + ".mon_results_standalone", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getmon_results_standalone :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getmon_results_standalone() {
		return mon_results_standalone;
	}

	/**
	 * \brief setmon_results_standalone :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_standalone
	 *            :
	 */
	public void setmon_results_standalone(
			SOSOptionBoolean p_mon_results_standalone) {
		this.mon_results_standalone = p_mon_results_standalone;
	}

	/**
	 * \var mon_results_order_step_state :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_order_step_state", description = "", key = "mon_results_order_step_state", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mon_results_order_step_state = new SOSOptionString(
			this, conClassName + ".mon_results_order_step_state", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getmon_results_order_step_state :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmon_results_order_step_state() {
		return mon_results_order_step_state;
	}

	/**
	 * \brief setmon_results_order_step_state :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_order_step_state
	 *            :
	 */
	public void setmon_results_order_step_state(
			SOSOptionString p_mon_results_order_step_state) {
		this.mon_results_order_step_state = p_mon_results_order_step_state;
	}

	/**
	 * \var mon_results_job_chain_name :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_job_chain_name", description = "", key = "mon_results_job_chain_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mon_results_job_chain_name = new SOSOptionString(
			this, conClassName + ".mon_results_job_chain_name", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getmon_results_job_chain_name :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmon_results_job_chain_name() {
		return mon_results_job_chain_name;
	}

	/**
	 * \brief setmon_results_job_chain_name :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_job_chain_name
	 *            :
	 */
	public void setmon_results_job_chain_name(
			SOSOptionString p_mon_results_job_chain_name) {
		this.mon_results_job_chain_name = p_mon_results_job_chain_name;
	}

	/**
	 * \var mon_results_order_id :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "mon_results_order_id", description = "", key = "mon_results_order_id", type = "SOSOptionString", mandatory = false)
	public SOSOptionString mon_results_order_id = new SOSOptionString(this,
			conClassName + ".mon_results_order_id", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getmon_results_order_id :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmon_results_order_id() {
		return mon_results_order_id;
	}

	/**
	 * \brief setmon_results_order_id :
	 * 
	 * \details
	 * 
	 * 
	 * @param mon_results_order_id
	 *            :
	 */
	public void setmon_results_order_id(SOSOptionString p_mon_results_order_id) {
		this.mon_results_order_id = p_mon_results_order_id;
	}

	/**
	 * \var force_reconnect :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "force_reconnect", description = "", key = "force_reconnect", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean force_reconnect = new SOSOptionBoolean(this,
			conClassName + ".force_reconnect", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getforce_reconnect :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getforce_reconnect() {
		return this.force_reconnect;
	}

	/**
	 * \brief setforce_reconnect :
	 * 
	 * \details
	 * 
	 * 
	 * @param reconnect
	 *            :
	 */
	public void setforce_reconnect(SOSOptionBoolean p_reconnect) {
		this.force_reconnect = p_reconnect;
	}

	public StoreResultsJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public StoreResultsJobOptionsSuperClass

	public StoreResultsJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public StoreResultsJobOptionsSuperClass

	//

	public StoreResultsJobOptionsSuperClass(HashMap<String, String> JSSettings)
			throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public StoreResultsJobOptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 * 
	 * \details
	 * 
	 * \see toString \see toOut
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
		final String conMethodName = conClassName + "::getAllOptionsAsString";
		String strT = conClassName + "\n";
		final StringBuffer strBuffer = new StringBuffer();
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this,
		// JSOptionsClass.IterationTypes.toString, strBuffer);
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
		// strBuffer);
		strT += this.toString(); // fix
		//
		return strT;
	} // private String getAllOptionsAsString ()

	/**
	 * \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
	 * 
	 * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
	 * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
	 * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
	 * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
	 * werden die Schlüssel analysiert und, falls gefunden, werden die
	 * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
	 * 
	 * Nicht bekannte Schlüssel werden ignoriert.
	 * 
	 * \see JSOptionsClass::getItem
	 * 
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	public void setAllOptions(HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	} // public void setAllOptions (HashMap <String, String> JSSettings)

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 * 
	 * \details
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 *             - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 * 
	 * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
	 * Kommandozeile
	 * 
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap übertragen und danach
	 * den Optionen als Wert zugewiesen.
	 * 
	 * \return void
	 * 
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class StoreResultsJobOptionsSuperClass