

package com.sos.scheduler.notification.jobs.result;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/**
 * \class 		StoreResultsJobOptions - NotificationMonitor
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see C:\Users\Robert Ehrlich\AppData\Local\Temp\scheduler_editor-1003156690106171278.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20140508144459
 * \endverbatim
 */
@JSOptionClass(name = "StoreResultsJobOptions", description = "NotificationMonitor")
public class StoreResultsJobOptions extends StoreResultsJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String					conClassName						= "StoreResultsJobOptions";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(StoreResultsJobOptions.class);

    /**
    * constructors
    */

	public StoreResultsJobOptions() {
	} // public StoreResultsJobOptions

	public StoreResultsJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public StoreResultsJobOptions

		//

	public StoreResultsJobOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public StoreResultsJobOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // StoreResultsJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

