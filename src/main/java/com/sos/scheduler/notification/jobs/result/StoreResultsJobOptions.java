package com.sos.scheduler.notification.jobs.result;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "StoreResultsJobOptions", description = "NotificationMonitor")
public class StoreResultsJobOptions extends StoreResultsJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final String conClassName = StoreResultsJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(StoreResultsJobOptions.class);

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

	public StoreResultsJobOptions(HashMap<String, String> JSSettings)
			throws Exception {
		super(JSSettings);
	} // public StoreResultsJobOptions (HashMap JSSettings)

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
	// StoreResultsJobOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}
