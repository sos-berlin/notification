package com.sos.scheduler.notification.jobs.history;

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
 * \class CheckHistoryJobOptionsSuperClass - CheckHistory
 * 
 * \brief An Options-Super-Class with all Options. This Class will be extended
 * by the "real" Options-class (\see CheckHistoryJobOptions. The "real" Option
 * class will hold all the things, which are normaly overwritten at a new
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
 * ("		CheckHistoryJobOptionsSuperClass.auth_file", "test"); // This parameter
 * specifies the path and name of a user's pr return pobjHM; } // private void
 * SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) \endverbatim
 */
@JSOptionClass(name = "CheckHistoryJobOptionsSuperClass", description = "CheckHistoryJobOptionsSuperClass")
public class CheckHistoryJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = "CheckHistoryJobOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(CheckHistoryJobOptionsSuperClass.class);

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
	public SOSOptionString schema_configuration_file = new SOSOptionString(this, conClassName
			+ ".schema_configuration_file", // HashMap-Key
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
	@JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionInterval", mandatory = true)
	public SOSOptionInteger max_history_age = new SOSOptionInteger(
			this, conClassName + ".max_history_age", // HashMap-Key
			"", // Titel
			"60", // InitValue
			"60", // DefaultValue
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
	public SOSOptionInteger getmax_history_age() {
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
	public void setmax_history_age(
			SOSOptionInteger p_max_history_age) {
		this.max_history_age = p_max_history_age;
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

	public CheckHistoryJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public CheckHistoryJobOptionsSuperClass

	public CheckHistoryJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public CheckHistoryJobOptionsSuperClass

	//

	public CheckHistoryJobOptionsSuperClass(HashMap<String, String> JSSettings)
			throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public CheckHistoryJobOptionsSuperClass (HashMap JSSettings)

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
} // public class CheckHistoryJobOptionsSuperClass