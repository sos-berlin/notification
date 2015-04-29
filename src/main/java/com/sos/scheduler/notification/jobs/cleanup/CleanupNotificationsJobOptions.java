package com.sos.scheduler.notification.jobs.cleanup;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
@JSOptionClass(name = "CleanupNotificationsJobOptions", description = "CleanupNotificationsJobOptions")
public class CleanupNotificationsJobOptions extends
		NotificationJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	private final String conClassName = CleanupNotificationsJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CleanupNotificationsJobOptions.class);

	/**
	 * \var days :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "age", description = "", key = "age", type = "SOSOptionString", mandatory = false)
	public SOSOptionString age = new SOSOptionString(this, conClassName
			+ ".age", // HashMap-Key
			"", // Titel
			"1d", // InitValue
			"1d", // DefaultValue
			false // isMandatory
	);

	/**
	 * \brief getage :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getage() {
		return this.age;
	}

	/**
	 * \brief setage :
	 * 
	 * \details
	 * 
	 * 
	 * @param days
	 *            :
	 */
	public void setage(SOSOptionString p_age) {
		this.age = p_age;
	}
} // public class CleanupNotificationsJobOptions