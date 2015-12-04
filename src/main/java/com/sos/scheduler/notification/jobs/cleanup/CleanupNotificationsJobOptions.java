package com.sos.scheduler.notification.jobs.cleanup;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

@JSOptionClass(name = "CleanupNotificationsJobOptions", description = "CleanupNotificationsJobOptions")
public class CleanupNotificationsJobOptions extends NotificationJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = CleanupNotificationsJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "age", description = "", key = "age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString age = new SOSOptionString(this, conClassName + ".age", // HashMap-Key
    "", // Titel
    "1d", // InitValue
    "1d", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getage() {
        return this.age;
    }

    public void setage(SOSOptionString val) {
        this.age = val;
    }
}