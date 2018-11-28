package com.sos.scheduler.notification.plugins.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.ElementNotificationMonitorInterface;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;

/** com.googlecode.jsendnsca.encryption.Encryption unterstützt nur 3 Encryptions : NONE, XOR, TRIPLE_DES
 * 
 * Auszug aus "send_nsca.cfg" Note: The encryption method you specify here must match the decryption method the nsca daemon uses (as specified in the nsca.cfg
 * file)!! Values: 0 = None (Do NOT use this option) <- Encryption.NONE 1 = Simple XOR (No security, just obfuscation, but very fast) <- Encryption.XOR 2 = DES
 * 3 = 3DES (Triple DES) <- Encryption.TRIPLE_DES 4 = CAST-128 5 = CAST-256 6 = xTEA 7 = 3WAY 8 = BLOWFISH 9 = TWOFISH 10 = LOKI97 11 = RC2 12 = ARCFOUR 14 =
 * RIJNDAEL-128 15 = RIJNDAEL-192 16 = RIJNDAEL-256 19 = WAKE 20 = SERPENT 22 = ENIGMA (Unix crypt) 23 = GOST 24 = SAFER64 25 = SAFER128 26 = SAFER+ */
public class SystemNotifierSendNscaPlugin extends SystemNotifierPlugin {

    final Logger logger = LoggerFactory.getLogger(SystemNotifierSendNscaPlugin.class);
    private NagiosSettings settings = null;

    @Override
    public void init(ElementNotificationMonitor monitor) throws Exception {

        super.init(monitor);

        ElementNotificationMonitorInterface ni = getNotificationMonitor().getMonitorInterface();
        if (ni == null) {
            throw new Exception("[init]NotificationInterface element is missing (not configured)");
        }

        NagiosSettingsBuilder nb = new NagiosSettingsBuilder().withNagiosHost(ni.getMonitorHost());

        if (ni.getMonitorPort() > -1) {
            nb.withPort(ni.getMonitorPort());
        }
        if (ni.getMonitorConnectionTimeout() > -1) {
            nb.withConnectionTimeout(ni.getMonitorConnectionTimeout());
        }
        if (ni.getMonitorResponseTimeout() > -1) {
            nb.withResponseTimeout(ni.getMonitorResponseTimeout());
        }
        if (ni.getMonitorPort() > -1) {
            nb.withPort(ni.getMonitorPort());
        }
        if (!SOSString.isEmpty(ni.getMonitorEncryption())) {
            nb.withEncryption(Encryption.valueOf(ni.getMonitorEncryption()));
        }
        if (!SOSString.isEmpty(ni.getMonitorPassword())) {
            nb.withPassword(ni.getMonitorPassword());
        }
        settings = nb.create();
    }

    private Level resolveServiceStatus(String status) {
        Level l = null;

        if (status != null) {
            if (status.equals("0")) {
                l = Level.OK;
            } else if (status.equals("1")) {
                l = Level.WARNING;
            } else if (status.equals("2")) {
                l = Level.CRITICAL;
            } else if (status.equals("3")) {
                l = Level.UNKNOWN;
            }
        }
        return l;
    }

    @Override
    public int notifySystem(Spooler spooler, SystemNotifierJobOptions options, DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonChecks check,
            EServiceStatus status, EServiceMessagePrefix prefix) throws Exception {

        ElementNotificationMonitorInterface ni = this.getNotificationMonitor().getMonitorInterface();
        setCommand(ni.getCommand());

        String serviceStatus = getServiceStatusValue(status);
        String servicePrefix = prefix == null ? "" : prefix.name();

        resolveCommandAllTableFieldVars(dbLayer, notification, systemNotification, check);
        resolveCommandServiceNameVar(systemNotification.getServiceName());
        resolveCommandServiceStatusVar(serviceStatus);
        resolveCommandServiceMessagePrefixVar(servicePrefix);
        resolveCommandAllEnvVars();
        setCommandPrefix(prefix);

        MessagePayload payload = new MessagePayloadBuilder().withHostname(ni.getServiceHost()).withLevel(getLevel(status)).withServiceName(
                systemNotification.getServiceName()).withMessage(getCommand()).create();

        logger.info(String.format("[send][monitor host=%s:%s][service host=%s][level=%s]%s", settings.getNagiosHost(), settings.getPort(), payload
                .getHostname(), payload.getLevel(), payload.getMessage()));

        try {
            NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);
            sender.send(payload);
        } catch (Throwable t) {
            throw new Exception(String.format("[NagiosPassiveCheckSender]%s", t.toString()), t);
        }

        return 0;
    }

    @Override
    public int notifySystemReset(String serviceName, EServiceStatus status, EServiceMessagePrefix prefix, String message) throws Exception {

        try {
            Level level = status.equals(EServiceStatus.OK) ? Level.OK : Level.CRITICAL;
            ElementNotificationMonitorInterface ni = this.getNotificationMonitor().getMonitorInterface();

            MessagePayload payload = new MessagePayloadBuilder().withHostname(ni.getServiceHost()).withLevel(level).withServiceName(serviceName)
                    .withMessage(message).create();

            logger.info(String.format("[send][monitor host=%s:%s][service host=%s][service name=%s][level=%s]%s", settings.getNagiosHost(), settings
                    .getPort(), payload.getHostname(), payload.getServiceName(), payload.getLevel(), payload.getMessage()));

            NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);
            sender.send(payload);
        } catch (Throwable t) {
            throw new Exception(String.format("[NagiosPassiveCheckSender]%s", t.toString()), t);
        }

        return 0;
    }

    private Level getLevel(EServiceStatus status) {
        Level level = null;
        if (status.equals(EServiceStatus.OK)) {
            level = resolveServiceStatus(getNotificationMonitor().getServiceStatusOnSuccess());
            if (level == null) {
                level = Level.OK;
            }
        } else {
            level = resolveServiceStatus(getNotificationMonitor().getServiceStatusOnError());
            if (level == null) {
                level = Level.CRITICAL;
            }
        }
        return level;
    }

    private void setCommandPrefix(EServiceMessagePrefix prefix) {
        if (getCommand() == null) {
            return;
        }
        if (prefix == null) {
            return;
        }

        if (!prefix.equals(EServiceMessagePrefix.SUCCESS)) {
            String command = this.getCommand().trim().toLowerCase();
            String prefixName = prefix.name().trim().toLowerCase();
            if (!command.startsWith(prefixName)) {
                setCommand(prefix.name() + " " + getCommand());
            }
        }
    }

}
