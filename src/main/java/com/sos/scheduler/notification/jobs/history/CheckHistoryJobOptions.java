package com.sos.scheduler.notification.jobs.history;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.scheduler.notification.jobs.NotificationJobOptionsSuperClass;

@JSOptionClass(name = "CheckHistoryJobOptions", description = "CheckHistoryJobOptions")
public class CheckHistoryJobOptions extends NotificationJobOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = CheckHistoryJobOptions.class.getSimpleName();

    @JSOptionDefinition(name = "plugins", description = "", key = "plugins", type = "SOSOptionString", mandatory = false)
    public SOSOptionString plugins = new SOSOptionString(this, conClassName + ".plugins", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getplugins() {
        return plugins;
    }

    public void setplugins(SOSOptionString val) {
        this.plugins = val;
    }

    @JSOptionDefinition(name = "schema_configuration_file", description = "", key = "schema_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString schema_configuration_file = new SOSOptionString(this, conClassName + ".schema_configuration_file", // HashMap-Key
    "", // Titel
    " ", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getschema_configuration_file() {
        return schema_configuration_file;
    }

    public void setschema_configuration_file(SOSOptionString val) {
        this.schema_configuration_file = val;
    }

    @JSOptionDefinition(name = "configuration_dir", description = "", key = "configuration_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString configuration_dir = new SOSOptionString(this, conClassName + ".configuration_dir", // HashMap-Key
    "", // Titel
    " ", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getconfiguration_dir() {
        return configuration_dir;
    }

    public void setconfiguration_dir(SOSOptionString val) {
        this.configuration_dir = val;
    }

    @JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionString", mandatory = true)
    public SOSOptionString max_history_age = new SOSOptionString(this, conClassName + ".max_history_age", // HashMap-Key
    "", // Titel
    "1h", // InitValue
    "1h", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getmax_history_age() {
        return max_history_age;
    }

    public void setmax_history_age(SOSOptionString val) {
        this.max_history_age = val;
    }

    @JSOptionDefinition(name = "max_uncompleted_age", description = "", key = "max_uncompleted_age", type = "SOSOptionString", mandatory = false)
    public SOSOptionString max_uncompleted_age = new SOSOptionString(this, conClassName + ".max_uncompleted_age", // HashMap-Key
    "", // Titel
    "1d", // InitValue
    "1d", // DefaultValue 1 day (1w 1d 1h 1m)
    false // isMandatory
    );

    public SOSOptionString getmax_uncompleted_age() {
        return max_uncompleted_age;
    }

    public void setmax_uncompleted_age(SOSOptionString val) {
        this.max_uncompleted_age = val;
    }

    @JSOptionDefinition(name = "allow_db_dependent_queries", description = "", key = "allow_db_dependent_queries", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean allow_db_dependent_queries = new SOSOptionBoolean(this, conClassName + ".allow_db_dependent_queries", // HashMap-Key
    "", // Titel
    "true", // InitValue
    "true", // DefaultValue
    false // isMandatory
    );

    public SOSOptionBoolean getallow_db_dependent_queries() {
        return allow_db_dependent_queries;
    }

    public void setallow_db_dependent_queries(SOSOptionBoolean val) {
        this.allow_db_dependent_queries = val;
    }

    @JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger batch_size = new SOSOptionInteger(this, conClassName + ".batch_size", // HashMap-Key
    "", // Titel
    "100", // InitValue
    "100", // DefaultValue
    false // isMandatory
    );

    public SOSOptionInteger getbatch_size() {
        return batch_size;
    }

    public void setbatch_size(SOSOptionInteger val) {
        this.batch_size = val;
    }
    
    @JSOptionDefinition(name = "max_execution_time", description = "", key = "max_execution_time", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger max_execution_time = new SOSOptionInteger(this, conClassName + ".max_execution_time", // HashMap-Key
            "", // Titel
            "-1", // InitValue
            "-1", // DefaultValue
            false // isMandatory
    );

    public SOSOptionInteger getmax_execution_time() {
        return max_execution_time;
    }

    public void setmax_execution_time(SOSOptionInteger val) {
        this.max_execution_time = val;
    }

    @JSOptionDefinition(name = "max_execution_time_exit", description = "", key = "max_execution_time_exit", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger max_execution_time_exit = new SOSOptionInteger(this, conClassName + ".max_execution_time_exit", // HashMap-Key
            "", // Titel
            "5", // InitValue
            "5", // DefaultValue
            false // isMandatory
    );

    public SOSOptionInteger getmax_execution_time_exit() {
        return max_execution_time_exit;
    }

    public void setmax_execution_time_exit(SOSOptionInteger val) {
        this.max_execution_time_exit = val;
    }
}