package com.sos.scheduler.notification.jobs.reset;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

@JSOptionClass(name = "ResetNotificationsJobOptionsSuperClass", description = "ResetNotificationsJobOptionsSuperClass")
public class ResetNotificationsJobOptions extends NotificationJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = ResetNotificationsJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "system_configuration_file", description = "", key = "system_configuration_file", mandatory = false)
    public SOSOptionString system_configuration_file = new SOSOptionString(this, conClassName + ".system_configuration_file", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getsystem_configuration_file() {
        return this.system_configuration_file;
    }

    public void setsystem_configuration_file(SOSOptionString val) {
        this.system_configuration_file = val;
    }

    @JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString schema_configuration_file = new SOSOptionString(this, conClassName + ".schema_configuration_file", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getschema_configuration_file() {
        return this.schema_configuration_file;
    }

    public void setschema_configuration_file(SOSOptionString val) {
        this.schema_configuration_file = val;
    }

    @JSOptionDefinition(name = "system_id", description = "", key = "system_id", type = "SOSOptionString", mandatory = true)
    public SOSOptionString system_id = new SOSOptionString(this, conClassName + ".system_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getsystem_id() {
        return this.system_id;
    }

    public void setsystem_id(SOSOptionString val) {
        this.system_id = val;
    }

    @JSOptionDefinition(name = "service_name", description = "", key = "service_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString service_name = new SOSOptionString(this, conClassName + ".service_name", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getservice_name() {
        return this.service_name;
    }

    public void setservice_name(SOSOptionString val) {
        this.service_name = val;
    }

    @JSOptionDefinition(name = "operation", description = "", key = "operation", type = "SOSOptionString", mandatory = true)
    public SOSOptionString operation = new SOSOptionString(this, conClassName + ".operation", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getoperation() {
        return this.operation;
    }

    public void setoperation(SOSOptionString val) {
        this.operation = val;
    }

    @JSOptionDefinition(name = "excluded_services", description = "", key = "excluded_services", type = "SOSOptionString", mandatory = false)
    public SOSOptionString excluded_services = new SOSOptionString(this, conClassName + ".excluded_services", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getexcluded_services() {
        return this.excluded_services;
    }

    public void setexcluded_services(SOSOptionString val) {
        this.excluded_services = val;
    }

    @JSOptionDefinition(name = "plugin", description = "", key = "plugin", type = "SOSOptionString", mandatory = false)
    public SOSOptionString plugin = new SOSOptionString(this, conClassName + ".plugin", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getplugin() {
        return this.plugin;
    }

    public void setplugin(SOSOptionString val) {
        this.plugin = val;
    }

    @JSOptionDefinition(name = "message", description = "", key = "message", type = "SOSOptionString", mandatory = false)
    public SOSOptionString message = new SOSOptionString(this, conClassName + ".message", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getmessage() {
        return this.message;
    }

    public void setmessage(SOSOptionString val) {
        this.message = val;
    }
}