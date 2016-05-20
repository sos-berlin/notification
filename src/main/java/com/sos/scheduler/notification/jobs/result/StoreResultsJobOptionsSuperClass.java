package com.sos.scheduler.notification.jobs.result;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "StoreResultsJobOptionsSuperClass", description = "StoreResultsJobOptionsSuperClass")
public class StoreResultsJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private final String conClassName = StoreResultsJobOptionsSuperClass.class.getSimpleName();

    @JSOptionDefinition(name = "scheduler_notification_result_parameters", description = "", key = "scheduler_notification_result_parameters", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_notification_result_parameters = new SOSOptionString(this, conClassName
            + ".scheduler_notification_result_parameters", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getscheduler_notification_result_parameters() {
        return scheduler_notification_result_parameters;
    }

    public void setscheduler_notification_result_parameters(SOSOptionString val) {
        this.scheduler_notification_result_parameters = val;
    }

    @JSOptionDefinition(name = "scheduler_notification_hibernate_configuration_file", description = "", key = "scheduler_notification_hibernate_configuration_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionString scheduler_notification_hibernate_configuration_file = new SOSOptionString(this, conClassName
            + ".scheduler_notification_hibernate_configuration_file", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getscheduler_notification_hibernate_configuration_file() {
        return scheduler_notification_hibernate_configuration_file;
    }

    public void setscheduler_notification_hibernate_configuration_file(SOSOptionString val) {
        this.scheduler_notification_hibernate_configuration_file = val;
    }

    @JSOptionDefinition(name = "scheduler_notification_connection_transaction_isolation", description = "", key = "scheduler_notification_connection_transaction_isolation", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger scheduler_notification_connection_transaction_isolation = new SOSOptionInteger(this, conClassName
            + ".scheduler_notification_connection_transaction_isolation", // HashMap-Key
    "", // Titel
    "2", // InitValue
    "2", // 1 = TRANSACTION_READ_UNCOMMITTED, 2 =
         // TRANSACTION_READ_COMMITTED
    false // isMandatory
    );

    public SOSOptionInteger getscheduler_notification_connection_transaction_isolation() {
        return scheduler_notification_connection_transaction_isolation;
    }

    public void setscheduler_notification_connection_transaction_isolation(SOSOptionInteger val) {
        this.scheduler_notification_connection_transaction_isolation = val;
    }

    @JSOptionDefinition(name = "scheduler_notification_connection_autocommit", description = "", key = "scheduler_notification_connection_autocommit", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean scheduler_notification_connection_autocommit = new SOSOptionBoolean(this, conClassName
            + ".scheduler_notification_connection_autocommit", // HashMap-Key
    "", // Titel
    "false", // InitValue
    "false", //
    false // isMandatory
    );

    public SOSOptionBoolean getscheduler_notification_connection_autocommit() {
        return scheduler_notification_connection_autocommit;
    }

    public void setscheduler_notification_connection_autocommit(SOSOptionBoolean val) {
        this.scheduler_notification_connection_autocommit = val;
    }

    @JSOptionDefinition(name = "mon_results_scheduler_id", description = "", key = "mon_results_scheduler_id", type = "SOSOptionString", mandatory = true)
    public SOSOptionString mon_results_scheduler_id = new SOSOptionString(this, conClassName + ".mon_results_scheduler_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionString getmon_results_scheduler_id() {
        return mon_results_scheduler_id;
    }

    public void setmon_results_scheduler_id(SOSOptionString val) {
        this.mon_results_scheduler_id = val;
    }

    @JSOptionDefinition(name = "mon_results_task_id", description = "", key = "mon_results_task_id", type = "SOSOptionInteger", mandatory = true)
    public SOSOptionInteger mon_results_task_id = new SOSOptionInteger(this, conClassName + ".mon_results_task_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    true // isMandatory
    );

    public SOSOptionInteger getmon_results_task_id() {
        return mon_results_task_id;
    }

    public void setmon_results_task_id(SOSOptionInteger val) {
        this.mon_results_task_id = val;
    }

    @JSOptionDefinition(name = "mon_results_standalone", description = "", key = "mon_results_standalone", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mon_results_standalone = new SOSOptionBoolean(this, conClassName + ".mon_results_standalone", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionBoolean getmon_results_standalone() {
        return mon_results_standalone;
    }

    public void setmon_results_standalone(SOSOptionBoolean val) {
        this.mon_results_standalone = val;
    }

    @JSOptionDefinition(name = "mon_results_order_step_state", description = "", key = "mon_results_order_step_state", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mon_results_order_step_state = new SOSOptionString(this, conClassName + ".mon_results_order_step_state", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getmon_results_order_step_state() {
        return mon_results_order_step_state;
    }

    public void setmon_results_order_step_state(SOSOptionString val) {
        this.mon_results_order_step_state = val;
    }

    @JSOptionDefinition(name = "mon_results_job_chain_name", description = "", key = "mon_results_job_chain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mon_results_job_chain_name = new SOSOptionString(this, conClassName + ".mon_results_job_chain_name", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getmon_results_job_chain_name() {
        return mon_results_job_chain_name;
    }

    public void setmon_results_job_chain_name(SOSOptionString val) {
        this.mon_results_job_chain_name = val;
    }

    @JSOptionDefinition(name = "mon_results_order_id", description = "", key = "mon_results_order_id", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mon_results_order_id = new SOSOptionString(this, conClassName + ".mon_results_order_id", // HashMap-Key
    "", // Titel
    "", // InitValue
    "", // DefaultValue
    false // isMandatory
    );

    public SOSOptionString getmon_results_order_id() {
        return mon_results_order_id;
    }

    public void setmon_results_order_id(SOSOptionString val) {
        this.mon_results_order_id = val;
    }

    @JSOptionDefinition(name = "force_reconnect", description = "", key = "force_reconnect", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean force_reconnect = new SOSOptionBoolean(this, conClassName + ".force_reconnect", // HashMap-Key
    "", // Titel
    "false", // InitValue
    "false", // DefaultValue
    false // isMandatory
    );

    public SOSOptionBoolean getforce_reconnect() {
        return this.force_reconnect;
    }

    public void setforce_reconnect(SOSOptionBoolean val) {
        this.force_reconnect = val;
    }

    public StoreResultsJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public StoreResultsJobOptionsSuperClass(JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public StoreResultsJobOptionsSuperClass(HashMap<String, String> settings) throws Exception {
        this();
        this.setAllOptions(settings);
    }

    public void setAllOptions(HashMap<String, String> settings) {
        flgSetAllOptions = true;
        objSettings = settings;
        super.setSettings(objSettings);
        super.setAllOptions(settings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(String[] args) {
        super.commandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }
}