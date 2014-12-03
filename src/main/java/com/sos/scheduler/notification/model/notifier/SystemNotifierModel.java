package com.sos.scheduler.notification.model.notifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.spooler.Spooler;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.ElementNotificationJobChain;
import com.sos.scheduler.notification.helper.ElementNotificationMonitor;
import com.sos.scheduler.notification.helper.ElementNotificationTimer;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.helper.EServiceMessagePrefix;
import com.sos.scheduler.notification.helper.EServiceStatus;
import com.sos.scheduler.notification.jobs.notifier.SystemNotifierJobOptions;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.notifier.ISystemNotifierPlugin;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class SystemNotifierModel extends NotificationModel {
	
	final Logger logger = LoggerFactory.getLogger(SystemNotifierModel.class);
	private Spooler spooler;
	
	private SystemNotifierJobOptions options;
	private String systemId;
	private File systemFile;
	
	private ArrayList<ElementNotificationJobChain> monitorOnErrorJobChains;
	private ArrayList<ElementNotificationJobChain> monitorOnSuccessJobChains;
	private ArrayList<ElementNotificationTimer> monitorOnErrorTimers;
	private ArrayList<ElementNotificationTimer> monitorOnSuccessTimers;
	
	private int countSendSuccess = 0;
	private int countSendError = 0;
	private int coundSendSkip = 0;
	private int countSendTotal = 0;
	
	/**
	 * 
	 */
	public SystemNotifierModel(){
	}
	
	public void init(SystemNotifierJobOptions opt, DBLayerSchedulerMon db,Spooler sp) throws Exception{
		super.init(db);
		this.options = opt;
		this.spooler = sp;
	}
	
	/**
	 * 
	 */
	private void initMonitorObjects(){
		this.monitorOnErrorJobChains = new ArrayList<ElementNotificationJobChain>();
		this.monitorOnSuccessJobChains = new ArrayList<ElementNotificationJobChain>();
		this.monitorOnErrorTimers = new ArrayList<ElementNotificationTimer>();
		this.monitorOnSuccessTimers = new ArrayList<ElementNotificationTimer>();
	}
	
	/**
	 * 
	 */
	private void initSendCounters(){
		this.countSendSuccess = 0;
		this.countSendError = 0;
		this.coundSendSkip = 0;
		this.countSendTotal = 0;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void initConfig() throws Exception{
		String functionName = "initConfig";
		
		File schemaFile = new File(this.options.schema_configuration_file.Value());
		if(!schemaFile.exists()){
			throw new Exception(String.format("%s: schema file not found: %s",
					functionName,
					schemaFile.getAbsolutePath()));
		}
		
		this.systemFile = new File(this.options.system_configuration_file.Value());
		if(!this.systemFile.exists()){
			throw new Exception(String.format("%s: system configuration file not found: %s",
					functionName,
					this.systemFile.getAbsolutePath()));
		}
		
		logger.debug(String.format("%s: read configuration file %s",
				functionName,
				this.systemFile.getAbsolutePath()));
		
		SOSXMLXPath xpath = new SOSXMLXPath(this.systemFile.getAbsolutePath());
		
		this.initMonitorObjects();
		
		this.systemId = NotificationXmlHelper.getSystemMonitorNotificationSystemId(xpath);
		if(SOSString.isEmpty(this.systemId)){
			throw new Exception(String.format("systemId is NULL (configured SystemMonitorNotification/@system_id is not found)"));
		}
		logger.info(String.format("%s: system id = %s (%s)",
				functionName,
				this.systemId,
				this.systemFile.getAbsolutePath()));
		
		NodeList monitorsOnError = NotificationXmlHelper.selectNotificationMonitorOnErrorDefinitions(xpath);
		this.setMonitorObjects(xpath, monitorsOnError, this.monitorOnErrorJobChains,this.monitorOnErrorTimers);
		
		NodeList monitorsOnSuccess = NotificationXmlHelper.selectNotificationMonitorOnSuccessDefinitions(xpath);
		this.setMonitorObjects(xpath, monitorsOnSuccess, this.monitorOnSuccessJobChains,this.monitorOnSuccessTimers);
	}
	
	/**
	 * 
	 * @param xpath
	 * @param monitors
	 * @param jobChains
	 * @param timers
	 * @throws Exception
	 */
	private void setMonitorObjects(SOSXMLXPath xpath,
			NodeList monitors,
			ArrayList<ElementNotificationJobChain> jobChains, 
			ArrayList<ElementNotificationTimer> timers) throws Exception{
		
		for(int i=0;i<monitors.getLength();i++){
			Node n = monitors.item(i);
			
			ElementNotificationMonitor monitor = new ElementNotificationMonitor(n);
			NodeList objects = NotificationXmlHelper.selectNotificationMonitorNotificationObjects(xpath,n);
			for(int j=0;j<objects.getLength();j++){
				Node object = objects.item(j);
				if(object.getNodeName().equalsIgnoreCase("JobChain")){
					jobChains.add(new ElementNotificationJobChain(monitor,object));
				}
				else if(object.getNodeName().equalsIgnoreCase("Timer")){
					timers.add(new ElementNotificationTimer(monitor,object));
				}
			}
		}
	}
	
	/**
	 * @TODO notification gleich mit dem Check auslesen
	 * @TODO steps abhängig (smNotTimer)
	 * 
	 * @param systemId
	 * @param check
	 * @param timer
	 * @param sendOnError
	 * @throws Exception
	 */
	private void executeNotifyTimer(String systemId,
			DBItemSchedulerMonChecks check,
			ElementNotificationTimer timer,
			boolean isNotifyOnErrorService) throws Exception{
		//Indent für die Ausgabe
		String functionName = "  executeNotifyTimer";
				
		String serviceName = (isNotifyOnErrorService) ? timer.getMonitor().getServiceNameOnError() : timer.getMonitor().getServiceNameOnSuccess();
		EServiceStatus pluginStatus = (isNotifyOnErrorService) ? EServiceStatus.CRITICAL : EServiceStatus.OK;
		
		//@TODO notification gleich mit dem Check auslesen
		DBItemSchedulerMonNotifications notification = this.getDbLayer().getNotification(check.getNotificationId());
		if(notification == null){
			throw new Exception(String.format("%s: serviceName = %s, notification id = %s not found",
					functionName,
					serviceName,
					check.getNotificationId()));
		}
		
		String stepFrom = check.getStepFrom();
		String stepTo = check.getStepTo();
		Long maxNotifications = timer.getNotifications();
		if(maxNotifications < 1){ 
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: serviceName = %s. skip notify timer (maxNotifications is %s): check.id = %s, schedulerId = %s, jobChain = %s",
					functionName,
					serviceName,
					maxNotifications,
					check.getId(),
					check.getSchedulerId(),
					check.getJobChain()));

			return;
		}
		
		DBItemSchedulerMonSystemNotifications sm = null;
		DBItemSchedulerMonSystemNotifications smNotTimer = null;
		
		if(timer.getNotifyOnError()){
			sm = this.getDbLayer().getSystemNotification(
					systemId, 
					serviceName,
					notification.getId(), 
					check.getId(), 
					stepFrom, 
					stepTo);
		}
		else{
			List<DBItemSchedulerMonSystemNotifications> result = this.getDbLayer().getSystemNotifications(systemId, serviceName, notification.getId()); 
			logger.debug(String.format("%s: found %s system notifications in the db for system = %s, serviceName = %s, notificationId = %s)",
					functionName,
					result.size(),
					systemId,
					serviceName,
					notification.getId()));
			
			for(int i=0;i<result.size();i++){
				DBItemSchedulerMonSystemNotifications resultSm = result.get(i);
				if(resultSm.getCheckId().equals(new Long(0))){
					smNotTimer = resultSm;
				}
				if(resultSm.getCheckId().equals(check.getId())){
					sm = resultSm;
				}
			}
		}
		
		if(smNotTimer != null){
			if(smNotTimer.getNotifications().equals(new Long(0)) || (smNotTimer.getNotifications() > 0 && smNotTimer.getRecovered())){
			
			}
			else{
				this.coundSendSkip++;
				logger.debug(
					String.format("%s: serviceName = %s. skip notify timer(notification has the error): smNotTimer.id = %s, smNotTimer.recovered = %s, smNotTimer.notifications = %s",
					functionName,
					serviceName,
					smNotTimer.getId(),
					smNotTimer.getRecovered(),
					smNotTimer.getNotifications()));
				return;
			}
		}
		
		if(sm == null){
			sm = this.getDbLayer().createSystemNotification(
					systemId,
					serviceName,
					notification.getId(),
					check.getId(),
					stepFrom,
					stepTo,
					notification.getOrderStartTime(),
					notification.getOrderEndTime(),
					new Long(0),
					false,
					false,
					true);
		}
		
		if(sm.getMaxNotifications()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getMaxNotifications()
					));
			return;
		}
		
		if(sm.getAcknowledged()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify timer (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getAcknowledged()
					));
			return;
		}
		
		if(sm.getNotifications() >= maxNotifications){
			this.setMaxNotifications(sm);
			this.coundSendSkip++;
			//@TODO evtl an dieser Stelle SystemNotification löschen
			logger.debug(
					String.format("%s: skip notify timer (count notifications was reached): id = %s, serviceName = %s, notifications = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications()
					));
			return;
		}
		
		try{
			sm.setNotifications(sm.getNotifications()+1);
			sm.setSuccess(true);
			sm.setModified(DBLayerSchedulerMon.getCurrentDateTime());
		
			if(sm.getId() == null){
				logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
						functionName,
						sm.getSystemId(),
						sm.getServiceName(),
						sm.getNotifications(),
						sm.getNotificationId(),
						sm.getCheckId(),
						sm.getStepFrom(),
						sm.getStepTo()));
			}
			else{
				logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
						functionName,
						sm.getId(),
						sm.getSystemId(),
						sm.getServiceName(),
						sm.getNotifications(),
						sm.getNotificationId(),
						sm.getCheckId(),
						sm.getStepFrom(),
						sm.getStepTo()));
			}
			
			ISystemNotifierPlugin pl = timer.getMonitor().getPluginObject();
			logger.info(String.format("%s: call plugin %s",
					functionName,
					pl.getClass().getSimpleName()));
			pl.init(timer.getMonitor());
			pl.notifySystem(
					this.getSpooler(),
					this.options,
					this.getDbLayer(),
					notification,
					sm,
					check,
					pluginStatus,
					EServiceMessagePrefix.TIMER);
				
					
			this.getDbLayer().beginTransaction();
			this.getDbLayer().saveOrUpdate(sm);
			this.getDbLayer().commit();
				
			this.countSendSuccess++;
		}
		catch(Exception ex){
			try{this.getDbLayer().rollback();}catch(Exception e){}
				logger.warn(String.format("%s: %s - %s",
					functionName,
					serviceName,
					ex.getMessage()));
				this.countSendError++;
		}
	}

	/**
	 * 
	 * @param sm
	 * @throws Exception
	 */
	private void setMaxNotifications(DBItemSchedulerMonSystemNotifications sm) throws Exception{
		sm.setMaxNotifications(true);
		sm.setModified(DBLayerSchedulerMon.getCurrentDateTime());
		
		this.getDbLayer().beginTransaction();
		this.getDbLayer().saveOrUpdate(sm);
		this.getDbLayer().commit();
	}
	
	/**
	 * 
	 * @param systemId
	 * @param notification
	 * @param jc
	 * @throws Exception
	 */
	private void executeNotifySuccess(String systemId,DBItemSchedulerMonNotifications notification,ElementNotificationJobChain jc) throws Exception{
		String functionName = "executeNotifySuccess";
		
		String serviceName = jc.getMonitor().getServiceNameOnSuccess();
		Long checkId = new Long(0);
		String stepFrom = jc.getStepFrom();
		String stepTo 	= jc.getStepTo();
		Long maxNotifications = jc.getNotifications();
		if(maxNotifications < 1){ 
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: serviceName = %s. skip notify success (maxNotifications is %s): notification.id = %s, schedulerId = %s, jobChain = %s",
					functionName,
					serviceName,
					maxNotifications,
					notification.getId(),
					notification.getSchedulerId(),
					notification.getJobChain()));

			return;
		}
		
		DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(
				systemId,
				serviceName,
				notification.getId(),
				checkId,
				stepFrom,
				stepTo);
		
		if(sm == null){
			sm = this.getDbLayer().createSystemNotification(
					systemId,
					serviceName,
					notification.getId(),
					checkId,
					stepFrom,
					stepTo,
					notification.getOrderStartTime(),
					notification.getOrderEndTime(),
					new Long(0),
					false,
					false,
					true);
		}
		/**
		// recovered, acknowledged???? werden nicht berücksichtigt
		if(!sm.getSuccess() || (sm.getSuccess() && (sm.getNotifications() < maxNotifications ))){
			doNotify = true;
		}
		}*/
		
		if(sm.getMaxNotifications()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify success (count notifications was reached): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getMaxNotifications()
					));
			return;
		}
		
		if(sm.getAcknowledged()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify success (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getAcknowledged()
					));
			return;
		}
		
		if(sm.getNotifications() >= maxNotifications){
			
			this.setMaxNotifications(sm);
			this.coundSendSkip++;
			//@TODO evtl an dieser Stelle SystemNotification löschen
			logger.debug(
					String.format("%s: skip notify success (count notifications was reached): id = %s, , serviceName = %s, notifications = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications()
					));
			return;
		}
		
		DBItemSchedulerMonNotifications stepFromNotification = null;
		DBItemSchedulerMonNotifications stepToNotification = null;
		
		//Behandlung stepFrom, stepTo, excludedSteps
		boolean doNotify = true;
		if(!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) 
			|| !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) 
			|| jc.getExcludedSteps().size() > 0){
			doNotify = false;
								
			DBItemSchedulerMonNotifications lastSuccessStepNotification = null;
			DBItemSchedulerMonNotifications lastNotification = null;
			Long stepFromIndex 	= new Long(0);
			Long stepToIndex 	= new Long(0);
			List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(notification.getOrderHistoryId());
			if(steps == null || steps.size() == 0){
				throw new Exception(String.format("%s: no steps found for orderHistoryId = %s", 
						functionName,
						notification.getOrderHistoryId()));
			}
			logger.debug(String.format("%s: steps size = %s for orderHistoryId = %s",
					functionName,
					steps.size(),
					notification.getOrderHistoryId()));
				
			for(DBItemSchedulerMonNotifications step : steps){
				if(stepFrom != null && step.getOrderStepState().equalsIgnoreCase(stepFrom)
						&& stepFromIndex.equals(new Long(0))){
					stepFromIndex = step.getStep();
					stepFromNotification = step;
				}
				if(stepTo != null && step.getOrderStepState().equalsIgnoreCase(stepTo)){
					stepToIndex = step.getStep();
					stepToNotification = step;
				}
				lastNotification = step;							
			}
				
			if(stepToIndex.equals(new Long(0))){
				stepToIndex = lastNotification.getStep();
			}
				
			for(DBItemSchedulerMonNotifications step: steps){
				if(step.getStep() >= stepFromIndex && step.getStep() <= stepToIndex){
					if(!step.getError()){
						lastSuccessStepNotification = step;
						doNotify= true;
					}
				}
			}
				
			if(doNotify && lastSuccessStepNotification != null){
				if(jc.getExcludedSteps().contains(lastSuccessStepNotification.getOrderStepState())){
					doNotify = false;
					this.coundSendSkip++;
					logger.info(String.format("%s: step = %s is configured as excluded.  SKIP create and do notify system: notificationId = %s, systemId = %s, serviceName = %s. ",
							functionName,
							lastSuccessStepNotification.getOrderStepState(),
							notification.getId(),
							sm.getSystemId(),
							sm.getServiceName()));
				}
			}
		}
		
		if(doNotify){
			try{
				if(stepFromNotification != null){
					sm.setStepFromStartTime(stepFromNotification.getOrderStepStartTime());
				}
				if(stepToNotification != null){
					sm.setStepToEndTime(stepToNotification.getOrderStepEndTime());
				}
				
				sm.setNotifications(sm.getNotifications()+1);
				sm.setSuccess(true);
				sm.setModified(DBLayerSchedulerMon.getCurrentDateTime());
				
				if(sm.getId() == null){
					logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
							functionName,
							sm.getSystemId(),
							sm.getServiceName(),
							sm.getNotifications(),
							sm.getNotificationId(),
							sm.getCheckId(),
							sm.getStepFrom(),
							sm.getStepTo()));
				}
				else{
					logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
							functionName,
							sm.getId(),
							sm.getSystemId(),
							sm.getServiceName(),
							sm.getNotifications(),
							sm.getNotificationId(),
							sm.getCheckId(),
							sm.getStepFrom(),
							sm.getStepTo()));
				}
				
				ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
				logger.info(String.format("%s: call plugin %s",
						functionName,
						pl.getClass().getSimpleName()));
				pl.init(jc.getMonitor());
				pl.notifySystem(
						this.getSpooler(),
						this.options,
						this.getDbLayer(),
						notification,
						sm,
						null,
						EServiceStatus.OK,
						null);
				
				
				this.getDbLayer().beginTransaction();
				this.getDbLayer().saveOrUpdate(sm);
				this.getDbLayer().commit();
				
				this.countSendSuccess++;
			}
			catch(Exception ex){
				try{this.getDbLayer().rollback();}catch(Exception e){}
				logger.warn(String.format("%s: %s - %s",
						functionName,
						serviceName,
						ex.getMessage()));
				this.countSendError++;
			}
		}
	}
	
	/**
	 * 
	 * @param jobChains
	 * @throws Exception
	 */
	private void notifySuccess(String systemId,ArrayList<ElementNotificationJobChain> jobChains) throws Exception{
		String functionName = "notifySuccess";
		
		List<DBItemSchedulerMonNotifications> result = this.getDbLayer().getNotificationsForNotifySuccess();
		logger.info(String.format("%s: found %s \"service_name_on_success\" definitions and %s notifications for success in the db",
				functionName,
				jobChains.size(),
				result.size()));
		
		this.initSendCounters();
		
		for(DBItemSchedulerMonNotifications notification : result){
			for(int i=0;i<jobChains.size();i++){
				this.countSendTotal++;
				ElementNotificationJobChain jc = jobChains.get(i);
				if(this.checkDoNotification(notification, jc)){
					this.executeNotifySuccess(systemId,notification,jc);
				}
				else{
					this.coundSendSkip++;
				}
			}
		}
		
		logger.info(String.format("%s: total checked = %s: sended = %s, error = %s, skipped = %s",
				functionName,
				this.countSendTotal,
				this.countSendSuccess,
				this.countSendError,
				this.coundSendSkip));
		

	}
	
	/**
	 * 
	 * @param check
	 * @param timer
	 * @return
	 */
	private boolean checkDoNotificationTimer(
			DBItemSchedulerMonChecks check,
			ElementNotificationTimer timer){
		String functionName = "  checkDoNotificationTimer";
		
		boolean notify = true;
		
		String name = timer.getName();
		if(!check.getName().equals(name)){
			notify = false;
		}
		
		logger.debug(String.format("%s: %s. check db(name = %s) and configured(name = %s)",
				functionName,
				notify ? "ok" : "skip",
				check.getName(),
				name
				));
		
	return notify;
	}
	
	/**
	 * 
	 * @param notification
	 * @param jc
	 * @return
	 */
	private boolean checkDoNotification(
			DBItemSchedulerMonNotifications notification,
			ElementNotificationJobChain jc) throws Exception{
		String functionName = "  checkDoNotification";
		boolean notify = true;
		
		String schedulerId = jc.getSchedulerId();
		String jobChain    = jc.getName();
		
		if(!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
			try{
				if(!notification.getSchedulerId().matches(schedulerId)){
					notify = false;
				}
			}
			catch(Exception ex){
				throw new Exception(String.format("%s: check with configured scheduler_id = %s: %s",
						functionName,
						schedulerId,
						ex));
			}
		}
		if(notify){
			if(!jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
				try{
					if(!notification.getJobChainName().matches(jobChain)){
						notify = false;
					}
				}
				catch(Exception ex){
					throw new Exception(String.format("%s: check with configured scheduler_id = %s, name = %s: %s",
							functionName,
							schedulerId,
							jobChain,
							ex));
				}
			}	
		}
		
		logger.debug(String.format("%s: %s. check db(schedulerId = %s, jobChain = %s) and configured(schedulerId = %s, jobChain = %s)",
				functionName,
				notify ? "ok" : "skip",
				notification.getSchedulerId(),
				notification.getJobChainName(),
				schedulerId,
				jobChain
				));
	return notify;
	}
	
	/**
	 * ist nicht Step abhängig
	 * 
	 * @param notification
	 * @param jc
	 * @throws Exception
	 */
	private void executeNotifyRecovered(String systemId,DBItemSchedulerMonNotifications notification,ElementNotificationJobChain jc) throws Exception{
		//Indent für die Ausgabe
		String functionName = "  executeNotifyRecovered";
		
		String serviceName = jc.getMonitor().getServiceNameOnError();
		Long checkId = new Long(0);
		String stepFrom = jc.getStepFrom();
		String stepTo = jc.getStepTo();
		Long maxNotifications = jc.getNotifications();
		
		if(maxNotifications < 1){ 
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify recovered (configured maxNotifications is %s): serviceName = %s, notification.id = %s, schedulerId = %s, jobChain = %s",
					functionName,
					maxNotifications,
					serviceName,
					notification.getId(),
					notification.getSchedulerId(),
					notification.getJobChain()));
			return;
		}
		DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(
				systemId,
				serviceName,
				notification.getId(),
				checkId,
				stepFrom,
				stepTo);
		
		if(sm == null){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify recovered (system notification not found): systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s, excludedSteps = %s",
					functionName,
					systemId,
					serviceName,
					notification.getId(),
					checkId,
					stepFrom,
					stepTo,
					jc.getExcludedStepsAsString()));
			return;
		}
		
		if(sm.getRecovered()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify recovered (is already recovered): id = %s, systemId = %s, serviceName = %s, recovered = %s",
					functionName,
					sm.getId(),
					sm.getSystemId(),
					sm.getServiceName(),
					sm.getRecovered()));
			return;
		}
		
		if(sm.getNotifications().equals(new Long(0))){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify recovered (system notification was not sended): id = %s, systemId = %s, serviceName = %s, notifications = %s",
					functionName,
					sm.getId(),
					sm.getSystemId(),
					sm.getServiceName(),
					sm.getNotifications()));
			return;
		}
		
		try{
			sm.setNotifications(sm.getNotifications()+1);
			sm.setRecovered(true);
			sm.setModified(DBLayerSchedulerMon.getCurrentDateTime());
				
			ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
			logger.info(String.format("%s: call plugin %s",
					functionName,
					pl.getClass().getSimpleName()));
			pl.init(jc.getMonitor());
			pl.notifySystem(
					this.getSpooler(),
					this.options,
					this.getDbLayer(),
					notification,
					sm,
					null,
					EServiceStatus.OK,
					EServiceMessagePrefix.RECOVERED);
				
			logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, stepFrom = %s, stepTo = %s",
						functionName,
						sm.getId(),
						sm.getSystemId(),
						sm.getServiceName(),
						sm.getNotifications(),
						sm.getStepFrom(),
						sm.getStepTo()));
				
			this.getDbLayer().beginTransaction();
			this.getDbLayer().saveOrUpdate(sm);
			this.getDbLayer().commit();
				
			this.countSendSuccess++;
		}
		catch(Exception ex){
			try{this.getDbLayer().rollback();}catch(Exception e){}
			logger.warn(String.format("%s: %s - %s",
					functionName,
					serviceName,
					ex.getMessage()));
			this.countSendError++;
		}

	}

	
	/**
	 * 
	 * @param jobChains
	 * @throws Exception
	 */
	private void notifyRecovered(String systemId,ArrayList<ElementNotificationJobChain> jobChains) throws Exception{
		String functionName = "notifyRecovered";
		
		List<DBItemSchedulerMonNotifications> result = this.getDbLayer().getNotificationsForNotifyRecovered();
		logger.info(String.format("%s: found %s \"service_name_on_error\" definitions and %s notifications for recovery in the db",
				functionName,
				jobChains.size(),
				result.size()));
		
		this.initSendCounters();
		
		for(DBItemSchedulerMonNotifications notification : result){
			for(int i=0;i<jobChains.size();i++){
				this.countSendTotal++;
				ElementNotificationJobChain jc = jobChains.get(i);
				if(this.checkDoNotification(notification, jc)){
					this.executeNotifyRecovered(systemId,notification,jc);
				}
				else{
					this.coundSendSkip++;
				}
			}
		}
		
		logger.info(String.format("%s: total checked = %s: sended = %s, error = %s, skipped = %s",
				functionName,
				this.countSendTotal,
				this.countSendSuccess,
				this.countSendError,
				this.coundSendSkip));
		
	}
	
	
	/**
	 * 
	 * @param notification
	 * @param jc
	 * @throws Exception
	 */
	private void executeNotifyError(String systemId,DBItemSchedulerMonNotifications notification,ElementNotificationJobChain jc) throws Exception{
		String functionName = "executeNotifyError";
		
		String serviceName = jc.getMonitor().getServiceNameOnError();
		Long checkId = new Long(0);
		String stepFrom = jc.getStepFrom();
		String stepTo 	= jc.getStepTo();
		long maxNotifications = jc.getNotifications();
		if(maxNotifications < 1){ 
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: serviceName = %s. skip notify error (maxNotifications is %s): notification.id = %s, schedulerId = %s, jobChain = %s",
					functionName,
					serviceName,
					maxNotifications,
					notification.getId(),
					notification.getSchedulerId(),
					notification.getJobChain()));
			return;
		}
		
		DBItemSchedulerMonSystemNotifications sm = this.getDbLayer().getSystemNotification(
				systemId,
				serviceName,
				notification.getId(),
				checkId,
				stepFrom,
				stepTo);
		
		if(sm == null){
			sm = this.getDbLayer().createSystemNotification(
					systemId,
					serviceName,
					notification.getId(), 
					checkId,
					stepFrom,
					stepTo,
					notification.getOrderStartTime(),
					notification.getOrderEndTime(),
					new Long(0),
					false,
					false,
					false);
		}
		/**
		if(!sm.getAcknowledged() 
			//&& !sm.getRecovered() wird nicht berücksichtig, da es sich um einen nicht recovered fehler handelt
			&& !sm.getSuccess() 
			&& sm.getNotifications() < maxNotifications){
			doNotify = true;
		}
		}*/
		
		if(sm.getMaxNotifications()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify error (count notifications was reached): id = %s, serviceName = %s, notifications = %s, maxNotifictions = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getMaxNotifications()
					));
			return;
		}
		
		if(sm.getAcknowledged()){
			this.coundSendSkip++;
			logger.debug(
					String.format("%s: skip notify error (is acknowledged): id = %s, serviceName = %s, notifications = %s, acknowledged = %s",
					functionName,
					sm.getId(),
					sm.getServiceName(),
					sm.getNotifications(),
					sm.getAcknowledged()
					));
			return;
		}
		
		if(sm.getNotifications() >= maxNotifications){
			this.setMaxNotifications(sm);
			this.coundSendSkip++;
			//@TODO evtl an dieser Stelle SystemNotification löschen
			logger.debug(
					String.format("%s: skip notify error (count notifications was reached): id = %s, notifications = %s, serviceName = %s",
					functionName,
					sm.getId(),
					sm.getNotifications(),
					sm.getServiceName()));
			return;
		}
		
		
		DBItemSchedulerMonNotifications stepFromNotification = null;
		DBItemSchedulerMonNotifications stepToNotification = null;

		boolean doNotify = true;
		// Behandlung stepFrom, stepTo, excludedSteps
		if (!stepFrom.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)
			|| !stepTo.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)
			|| jc.getExcludedSteps().size() > 0) {
			doNotify = false;

			DBItemSchedulerMonNotifications lastErrorStepNotification = null;
			DBItemSchedulerMonNotifications lastNotification = null;
			Long stepFromIndex = new Long(0);
			Long stepToIndex = new Long(0);
			List<DBItemSchedulerMonNotifications> steps = this.getDbLayer().getOrderNotifications(notification.getOrderHistoryId());
			if (steps == null || steps.size() == 0) {
				throw new Exception(String.format(
								"%s: serviceName = %s. no steps found for orderHistoryId = %s",
								functionName, sm.getServiceName(),
								notification.getOrderHistoryId()));
			}
			logger.debug(String.format("%s: serviceName = %s. steps size = %s for orderHistoryId = %s",
							functionName, sm.getServiceName(), steps.size(),
							notification.getOrderHistoryId()));

			for (DBItemSchedulerMonNotifications step : steps) {
				if (stepFrom != null
					&& step.getOrderStepState().equalsIgnoreCase(stepFrom)
					&& stepFromIndex.equals(new Long(0))) {
					
					stepFromIndex = step.getStep();
					stepFromNotification = step;
				}
				if (stepTo != null
					&& step.getOrderStepState().equalsIgnoreCase(stepTo)) {
					
					stepToIndex = step.getStep();
					stepToNotification = step;
				}
				lastNotification = step;
			}

			if (stepToIndex.equals(new Long(0))) {
				stepToIndex = lastNotification.getStep();
			}

			for (DBItemSchedulerMonNotifications step : steps) {
				if (step.getStep() >= stepFromIndex
						&& step.getStep() <= stepToIndex) {
					// max error (letzter) not recovered - egal ob der nächste
					// nicht fehlerhaft ist
					// weil es wurde bereits festgelegt, dass der fehler
					// gemeldet werden soll
					if (step.getError() && !step.getRecovered()) {
						lastErrorStepNotification = step;
						doNotify = true;
					}
				}
			}

			if (doNotify && lastErrorStepNotification != null) {
				if (jc.getExcludedSteps().contains(
						lastErrorStepNotification.getOrderStepState())) {
					if (lastErrorStepNotification.getOrderEndTime() != null
							&& lastErrorStepNotification.getOrderStepState()
									.equals(lastNotification.getState())) {
						logger.debug(String
								.format("%s: serviceName = %s. order is completed and error step state equals config step = %s and this is last order step.  create and do notify system: notificationId = %s, systemId = %s. ",
										functionName, sm.getServiceName(),
										lastErrorStepNotification
												.getOrderStepState(),
										notification.getId(), systemId));
					} else {
						doNotify = false;
						this.coundSendSkip++;
						logger.info(String
								.format("%s: serviceName = %s. order is not completed or error step equals config step = %s and this is not last order step.  SKIP create and do notify system: notificationId = %s, systemId = %s. ",
										functionName, sm.getServiceName(),
										lastErrorStepNotification
												.getOrderStepState(),
										notification.getId(), systemId));
					}
				}
			}
		}
			
		if(doNotify){
			try {
				if(stepFromNotification != null){
					sm.setStepFromStartTime(stepFromNotification.getOrderStepStartTime());
				}
				if(stepToNotification != null){
					sm.setStepToEndTime(stepToNotification.getOrderStepEndTime());
				}
				
				sm.setNotifications(sm.getNotifications()+1);
				sm.setModified(DBLayerSchedulerMon.getCurrentDateTime());
				
				if(sm.getId() == null){
					logger.debug(String.format("%s: create system notification: systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
							functionName,
							sm.getSystemId(),
							sm.getServiceName(),
							sm.getNotifications(),
							sm.getNotificationId(),
							sm.getCheckId(),
							sm.getStepFrom(),
							sm.getStepTo()));
				}
				else{
					logger.debug(String.format("%s: update system notification: id = %s, systemId = %s, serviceName = %s, notifications = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
							functionName,
							sm.getId(),
							sm.getSystemId(),
							sm.getServiceName(),
							sm.getNotifications(),
							sm.getNotificationId(),
							sm.getCheckId(),
							sm.getStepFrom(),
							sm.getStepTo()));
				}
				
				ISystemNotifierPlugin pl = jc.getMonitor().getPluginObject();
				logger.info(String.format("%s: call plugin %s",
						functionName,
						pl.getClass().getSimpleName()));
				pl.init(jc.getMonitor());
				pl.notifySystem(
						this.getSpooler(), this.options,
						this.getDbLayer(), 
						notification,
						sm,
						null,
						EServiceStatus.CRITICAL,
						EServiceMessagePrefix.ERROR);
					
				this.getDbLayer().beginTransaction();
				this.getDbLayer().saveOrUpdate(sm);
				this.getDbLayer().commit();

				this.countSendSuccess++;
			} catch (Exception ex) {
				try {
					this.getDbLayer().rollback();
				} catch (Exception e) {	}
				logger.warn(String.format("%s: %s - %s", 
					functionName,
					serviceName,
					ex.getMessage()));
				this.countSendError++;
			}
		}
	}

	
	/**
	 * 
	 * @param systemId
	 * @param timersOnSuccess
	 * @param timersOnError
	 * @throws Exception
	 */
	private void notifyTimer(String systemId,
			ArrayList<ElementNotificationTimer> timersOnSuccess,
			ArrayList<ElementNotificationTimer> timersOnError) throws Exception{
		String functionName = "notifyTimer";
		
		List<DBItemSchedulerMonChecks> result = this.getDbLayer().getChecksForNotifyTimer();
		logger.info(String.format("%s: found %s \"service_name_on_success\" and %s \"service_name_on_error\" timer definitions and %s checks for timers in the db",
				functionName,
				timersOnSuccess.size(),
				timersOnError.size(),
				result.size()));
		
		this.initSendCounters();
		
		for(DBItemSchedulerMonChecks check : result){
			logger.debug(String.format("%s: notify timer \"service_name_on_success\"",functionName));
			
			for(int i=0;i<timersOnSuccess.size();i++){
				this.countSendTotal++;
				
				ElementNotificationTimer t = timersOnSuccess.get(i);
				if(this.checkDoNotificationTimer(check, t)){
					this.executeNotifyTimer(systemId,check,t,false);
				}
				else{
					this.coundSendSkip++;
				}
			}
			
			logger.debug(String.format("%s: notify timer \"service_name_on_error\"",functionName));
			for(int i=0;i<timersOnError.size();i++){
				this.countSendTotal++;
				
				ElementNotificationTimer t = timersOnError.get(i);
				if(this.checkDoNotificationTimer(check, t)){
					this.executeNotifyTimer(systemId,check,t,true);
				}
				else{
					this.coundSendSkip++;
				}
			}
		}
		
		logger.info(String.format("%s: total checked = %s: sended = %s, error = %s, skipped = %s",
				functionName,
				this.countSendTotal,
				this.countSendSuccess,
				this.countSendError,
				this.coundSendSkip));
		
	}
    
	
	/**
	 * 
	 * @param systemId
	 * @param jobChains
	 * @throws Exception
	 */
	private void notifyError(String systemId,ArrayList<ElementNotificationJobChain> jobChains) throws Exception{
		String functionName = "notifyError";
		
		List<DBItemSchedulerMonNotifications> result = this.getDbLayer().getNotificationsForNotifyError();
		logger.info(String.format("%s: found %s \"service_name_on_error\" definitions and %s notifications for error in the db",
				functionName,
				jobChains.size(),
				result.size()));
		
		this.initSendCounters();
		
		for(DBItemSchedulerMonNotifications notification : result){
			for(int i=0;i<jobChains.size();i++){
				this.countSendTotal++;
				ElementNotificationJobChain jc = jobChains.get(i);
				if(this.checkDoNotification(notification, jc)){
					this.executeNotifyError(systemId,notification,jc);
				}
				else{
					this.coundSendSkip++;
				}
			}
		}
		
		logger.info(String.format("%s: total checked = %s: sended = %s, error = %s, skipped = %s",
				functionName,
				this.countSendTotal,
				this.countSendSuccess,
				this.countSendError,
				this.coundSendSkip));
		
	}
    /**
     * 
     */
	@Override
	public void process() throws Exception{
    	super.process();
		this.initConfig();
		
		String functionName = "process";
		String systemId = this.systemId;
		
		boolean found = false;
    	if(this.monitorOnSuccessJobChains != null && this.monitorOnSuccessJobChains.size() > 0){
    		found = true;
    		this.notifySuccess(systemId,this.monitorOnSuccessJobChains);		
    	}
    	else{
    		logger.info(String.format("%s: skip notify success. found 0 \"service_name_on_success\" definitions",functionName));
    	}
    	
    	if(this.monitorOnErrorJobChains != null && this.monitorOnErrorJobChains.size() > 0){
    		found = true;
    		this.notifyRecovered(systemId,this.monitorOnErrorJobChains);
    		this.notifyError(systemId,this.monitorOnErrorJobChains);
    	}
    	else{
    		logger.info(String.format("%s: skip notify recovery & notify error. found 0 \"service_name_on_error\" definitions",functionName));
    	}
    	
    	if(this.monitorOnSuccessTimers.size() > 0 || this.monitorOnErrorTimers.size() > 0){
    		found = true;
    		this.notifyTimer(systemId,this.monitorOnSuccessTimers,this.monitorOnErrorTimers);
    	}
    	else{
    		logger.info(String.format("%s: skip notify timer. found 0 timer definitions",functionName));
    	}
    	
    	if(!found){
    		throw new Exception(String.format("%s: not found configured \"service_name_on_error\" or \"service_name_on_sucess\" for system id = %s (%s) ",
    				functionName,
    				this.systemId,
    				this.systemFile.getAbsolutePath()));
    	}
  }
	
	
	/**
	 * 
	 * @return
	 */
	public Spooler getSpooler() {
		return spooler;
	}
	
}
