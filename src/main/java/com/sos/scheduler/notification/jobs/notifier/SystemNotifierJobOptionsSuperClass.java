

package com.sos.scheduler.notification.jobs.notifier;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

/**
 * \class 		SystemNotifierJobOptionsSuperClass - SystemNotifierJob
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see SystemNotifierJobOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-2443402619267845799.html for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by D:\Arbeit\scheduler\jobscheduler_data\re-dell_4444\config\JOETemplates\java\xsl\JSJobDoc2JSOptionSuperClass.xsl from http://www.sos-berlin.com at 20140513162136 
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 * 
 * Tabelle mit allen Optionen
 * 
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 * 
 * 
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		SystemNotifierJobOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "SystemNotifierJobOptionsSuperClass", description = "SystemNotifierJobOptionsSuperClass")
public class SystemNotifierJobOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String					conClassName						= "SystemNotifierJobOptionsSuperClass";
		@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(SystemNotifierJobOptionsSuperClass.class);

		

/**
 * \var hibernate_configuration_file : 
 * 
 *
 */
    @JSOptionDefinition(name = "hibernate_configuration_file", 
    description = "", 
    key = "hibernate_configuration_file", 
    type = "SOSOptionString", 
    mandatory = false)
    
    public SOSOptionString hibernate_configuration_file = new SOSOptionString(this, conClassName + ".hibernate_configuration_file", // HashMap-Key
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
    public SOSOptionString  gethibernate_configuration_file() {
        return hibernate_configuration_file;
    }

/**
 * \brief sethibernate_configuration_file : 
 * 
 * \details
 * 
 *
 * @param hibernate_configuration_file : 
 */
    public void sethibernate_configuration_file (SOSOptionString p_hibernate_configuration_file) { 
        this.hibernate_configuration_file = p_hibernate_configuration_file;
    }

                        

/**
 * \var configuration_schema_file : 
 * 
 *
 */
    @JSOptionDefinition(name = "configuration_schema_file", 
    description = "", 
    key = "configuration_schema_file", 
    type = "SOSOptionString", 
    mandatory = true)
    
    public SOSOptionString configuration_schema_file = new SOSOptionString(this, conClassName + ".configuration_schema_file", // HashMap-Key
                                                                "", // Titel
                                                                " ", // InitValue
                                                                "", // DefaultValue
                                                                true // isMandatory
                    );

/**
 * \brief getconfiguration_schema_file : 
 * 
 * \details
 * 
 *
 * \return 
 *
 */
    public SOSOptionString  getconfiguration_schema_file() {
        return configuration_schema_file;
    }

/**
 * \brief setconfiguration_schema_file : 
 * 
 * \details
 * 
 *
 * @param configuration_schema_file : 
 */
    public void setconfiguration_schema_file (SOSOptionString p_configuration_schema_file) { 
        this.configuration_schema_file = p_configuration_schema_file;
    }

    
    /**
     * \var system_id : 
     * 
     *
     */
        @JSOptionDefinition(name = "system_id", 
        description = "", 
        key = "system_id", 
        type = "SOSOptionString", 
        mandatory = true)
        
        public SOSOptionString system_id = new SOSOptionString(this, conClassName + ".system_id", // HashMap-Key
                                                                    "", // Titel
                                                                    " ", // InitValue
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
        public SOSOptionString  getsystem_id() {
            return system_id;
        }

    /**
     * \brief setsystem_id : 
     * 
     * \details
     * 
     *
     * @param system_id : 
     */
        public void setsystem_id (SOSOptionString p_system_id) { 
            this.system_id = p_system_id;
        }
    
            /**
             * \var plugin_job_name : 
             * 
             *
             */
                @JSOptionDefinition(name = "plugin_job_name", 
                description = "", 
                key = "plugin_job_name", 
                type = "SOSOptionString", 
                mandatory = false)
                
                public SOSOptionString plugin_job_name = new SOSOptionString(this, conClassName + ".plugin_job_name", // HashMap-Key
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
                public SOSOptionString  getplugin_job_name() {
                    return plugin_job_name;
                }

            /**
             * \brief setplugin_job_name : 
             * 
             * \details
             * 
             *
             * @param p_plugin_job_name : 
             */
                public void setplugin_job_name (SOSOptionString p_plugin_job_name) { 
                    this.plugin_job_name = p_plugin_job_name;
                }

        
	public SystemNotifierJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public SystemNotifierJobOptionsSuperClass

	public SystemNotifierJobOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SystemNotifierJobOptionsSuperClass

		//

	public SystemNotifierJobOptionsSuperClass (HashMap <String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SystemNotifierJobOptionsSuperClass (HashMap JSSettings)
/**
 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
 * Optionen als String
 *
 * \details
 * 
 * \see toString 
 * \see toOut
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
	public void setAllOptions(HashMap <String, String> pobjJSSettings) {
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
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
		, Exception {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
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
} // public class SystemNotifierJobOptionsSuperClass