

package com.sos.scheduler.notification.jobs.notifier;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/**
 * \class 		SystemNotifierJobOptions - SystemNotifierJob
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-3613842323690924441.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20140513161021
 * \endverbatim
 */
@JSOptionClass(name = "SystemNotifierJobOptions", description = "SystemNotifierJob")
public class SystemNotifierJobOptions extends SystemNotifierJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String					conClassName						= "SystemNotifierJobOptions";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(SystemNotifierJobOptions.class);

    /**
    * constructors
    */

	public SystemNotifierJobOptions() {
	} // public SystemNotifierJobOptions

	public SystemNotifierJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SystemNotifierJobOptions

		//

	public SystemNotifierJobOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public SystemNotifierJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // SystemNotifierJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

