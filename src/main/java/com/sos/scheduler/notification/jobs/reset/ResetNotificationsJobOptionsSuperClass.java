package com.sos.scheduler.notification.jobs.reset;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * \class ResetNotificationsJobOptionsSuperClass - ResetNotifications
 * 
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see ResetNotificationsOptions. The "real"
 * Option class will hold all the things, which are normaly overwritten at a new
 * generation of the super-class.
 * 
 * 
 * 
 * 
 * see \see C:\Users\Robert
 * Ehrlich\AppData\Local\Temp\scheduler_editor-7198855759937042280.html for
 * (more) details.
 * 
 * \verbatim ; mechanicaly created by
 * D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444
 * \config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from
 * http://www.sos-berlin.com at 20140512133635 \endverbatim \section
 * OptionsTable Tabelle der vorhandenen Optionen
 * 
 * Tabelle mit allen Optionen
 * 
 * MethodName Title Setting Description IsMandatory DataType InitialValue
 * TestValue
 * 
 * 
 * 
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 * 
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 * 
 * \verbatim private HashMap <String, String> SetJobSchedulerSSHJobOptions
 * (HashMap <String, String> pobjHM) { pobjHM.put
 * ("		ResetNotificationsJobOptionsSuperClass.auth_file", "test"); // This
 * parameter specifies the path and name of a user's pr return pobjHM; } //
 * private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "ResetNotificationsJobOptionsSuperClass", description = "ResetNotificationsJobOptionsSuperClass")
public class ResetNotificationsJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = "ResetNotificationsJobOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ResetNotificationsJobOptionsSuperClass.class);

	/**
	 * \var hibernate_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "hibernate_configuration_file", description = "", key = "hibernate_configuration_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionString hibernate_configuration_file = new SOSOptionString(
			this, conClassName + ".hibernate_configuration_file", // HashMap-Key
			"", // Titel
			" ", // InitValue
			"", // DefaultValue
			false // isMandatory
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
	public void sethibernate_configuration_file(
			SOSOptionString p_hibernate_configuration_file) {
		this.hibernate_configuration_file = p_hibernate_configuration_file;
	}
	
	
	/**
	 * \var system_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "system_configuration_file", description = "", key = "system_configuration_file", mandatory = false)
	public SOSOptionString system_configuration_file = new SOSOptionString(this,
			conClassName + ".system_configuration_file", // HashMap-Key
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
	public void setsystem_configuration_file(SOSOptionString p_configuration_file) {
		this.system_configuration_file = p_configuration_file;
	}

	/**
	 * \var schema_configuration_file :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionString schema_configuration_file = new SOSOptionString(this,
			conClassName + ".schema_configuration_file", // HashMap-Key
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
	public void setschema_configuration_file(SOSOptionString p_schema_configuration_file) {
		this.schema_configuration_file = p_schema_configuration_file;
	}
	
	
	/**
	 * \var system_id :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "system_id", description = "", key = "system_id", type = "SOSOptionString", mandatory = true)
	public SOSOptionString system_id = new SOSOptionString(this,
			conClassName + ".system_id", // HashMap-Key
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
	public SOSOptionString operation = new SOSOptionString(this,
			conClassName + ".operation", // HashMap-Key
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

	
	public ResetNotificationsJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public ResetNotificationsJobOptionsSuperClass

	public ResetNotificationsJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public ResetNotificationsJobOptionsSuperClass

	//

	public ResetNotificationsJobOptionsSuperClass(
			HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public ResetNotificationsJobOptionsSuperClass (HashMap JSSettings)

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
} // public class ResetNotificationsJobOptionsSuperClass