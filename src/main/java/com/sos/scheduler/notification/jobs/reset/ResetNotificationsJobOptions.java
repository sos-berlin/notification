

package com.sos.scheduler.notification.jobs.reset;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/**
 * \class 		ResetNotificationsJobOptions - ResetNotifications
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-7198855759937042280.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20140512133635
 * \endverbatim
 */
@JSOptionClass(name = "ResetNotificationsJobOptions", description = "ResetNotifications")
public class ResetNotificationsJobOptions extends ResetNotificationsJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String					conClassName						= "ResetNotificationsJobOptions";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(ResetNotificationsJobOptions.class);

    /**
    * constructors
    */

	public ResetNotificationsJobOptions() {
	} // public ResetNotificationsJobOptions

	public ResetNotificationsJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public ResetNotificationsJobOptions

		//

	public ResetNotificationsJobOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public ResetNotificationsJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // ResetNotificationsJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

