package com.sos.scheduler.notification.model.history;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.scheduler.notification.db.DBItemNotificationSchedulerHistoryOrderStep;
import com.sos.scheduler.notification.db.DBItemNotificationSchedulerVariables;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.CounterCheckHistory;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.helper.ElementTimerJobChain;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.scheduler.notification.model.INotificationModel;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.history.ICheckHistoryPlugin;

public class CheckHistoryModel extends NotificationModel implements INotificationModel {

	final Logger logger = LoggerFactory.getLogger(CheckHistoryModel.class);

	private CheckHistoryJobOptions options;
	private LinkedHashMap<String, ElementTimer> timers = null;
	private LinkedHashMap<String, ArrayList<String>> jobChains = null;
	private boolean checkInsertNotifications = true;
	private List<ICheckHistoryPlugin> plugins = null;
	private CounterCheckHistory counter;
	private Optional<Integer> largeResultFetchSize = Optional.empty();
    
	public CheckHistoryModel(SOSHibernateConnection conn,
			CheckHistoryJobOptions opt) throws Exception{
		
		super(conn);
		options = opt;
		
		try{
            int fetchSize = options.large_result_fetch_size.value();
            if(fetchSize != -1){
                largeResultFetchSize = Optional.of(fetchSize);
            }
		}
        catch(Exception ex){}
		
		initConfig();
		registerPlugins();
		pluginsOnInit(timers,options,getDbLayer());
	}

	private void initCounters(){
		counter = new CounterCheckHistory();
	}

	public void initConfig() throws Exception {
		plugins = new ArrayList<ICheckHistoryPlugin>();
		timers = new LinkedHashMap<String, ElementTimer>();
		jobChains = new LinkedHashMap<String,ArrayList<String>>();
				
		File dir = null;

		File schemaFile = new File(options.schema_configuration_file.Value());

		if (!schemaFile.exists()) {
			throw new Exception(String.format("schema file not found: %s",
					schemaFile.getAbsolutePath()));
		}

		if (SOSString.isEmpty(this.options.configuration_dir.Value())) {
			dir = new File(this.options.configuration_dir.Value());
		} else {
			dir = schemaFile.getParentFile().getAbsoluteFile();
		}

		if (!dir.exists()) {
			throw new Exception(String.format(
					"configuration dir not found: %s", dir.getAbsolutePath()));
		}

		logger.debug(String.format("schemaFile=%s, configDir=%s",schemaFile,dir.getAbsolutePath()));
				
		readConfigFiles(dir);
	}

	private void readConfigFiles(File dir) throws Exception {
		String method = "readConfigFiles";
		
		jobChains = new LinkedHashMap<String,ArrayList<String>>();
		timers = new LinkedHashMap<String, ElementTimer>();
		checkInsertNotifications = true;
				
		File[] files = getAllConfigurationFiles(dir);
		if (files.length == 0) {
			throw new Exception(String.format(
					"%s: configuration files not found. directory : %s",
					method,
					dir.getAbsolutePath()));
		}

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			logger.info(String.format("%s: read configuration file %s",
					method,
					f.getAbsolutePath()));

			SOSXMLXPath xpath = new SOSXMLXPath(f.getAbsolutePath());
			
			setConfigAllJobChains(xpath);
			setConfigTimers(xpath);
		}

		if(jobChains.size() == 0 && timers.size() == 0){
			throw new Exception(String.format("%s: jobChains or timers definitions not founded",method));
		}
		
	}

	private void setConfigTimers(SOSXMLXPath xpath) throws Exception{
		NodeList nlTimers = NotificationXmlHelper.selectTimerDefinitions(xpath);
		for (int j = 0; j < nlTimers.getLength(); j++) {
			Node n = nlTimers.item(j);
			String name = NotificationXmlHelper.getTimerName((Element)n);
			if (name != null && !timers.containsKey(name)) {
				timers.put(name, new ElementTimer(n));
			}
		}
	}
	
	private void setConfigAllJobChains(SOSXMLXPath xpath) throws Exception{
		NodeList notificationJobChains = NotificationXmlHelper.selectNotificationJobChainDefinitions(xpath);
		
		setConfigJobChains(xpath,notificationJobChains);
		if(checkInsertNotifications){
			NodeList timerJobChains = NotificationXmlHelper.selectTimerJobChainDefinitions(xpath);
			setConfigJobChains(xpath,timerJobChains);
		}
	}
	
	private void setConfigJobChains(SOSXMLXPath xpath,NodeList nlJobChains) throws Exception{
		for (int j = 0; j < nlJobChains.getLength(); j++) {
			Element jobChain = (Element)nlJobChains.item(j);
			
			String schedulerId = NotificationXmlHelper.getSchedulerId(jobChain);
			String name = NotificationXmlHelper.getJobChainName(jobChain);
			
			schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
			name        = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
			
			ArrayList<String> al = new ArrayList<String>();
			if(schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
				jobChains = new LinkedHashMap<String,ArrayList<String>>();
				al.add(name);
				jobChains.put(schedulerId,al);
				checkInsertNotifications = false;
				return;
			}
			
			if(jobChains.containsKey(schedulerId)){
				al = jobChains.get(schedulerId);
			}
			if(!al.contains(name)){
				al.add(name);
			}
			jobChains.put(schedulerId,al);
		}
	}
	
	private boolean checkInsertNotification(CounterCheckHistory counter, DBItemNotificationSchedulerHistoryOrderStep step) throws Exception{
		
		//Indent for the output
		String method = "  checkInsertNotification";
		logger.debug(String.format("%s: %s) checkInsertNotifications = %s",
				method,
				counter.getTotal(),
				checkInsertNotifications));
				
		
		if(!checkInsertNotifications){
			return true;
		}
		if(jobChains == null || jobChains.size() == 0){
			return false;
		}
		
		logger.debug(String.format("%s: %s) order: schedulerId = %s, jobChain = %s",
				method,
				counter.getTotal(),
				step.getOrderSchedulerId(),
				step.getOrderJobChain()));
		
		Set<Map.Entry<String, ArrayList<String>>> set = jobChains.entrySet();
		for (Map.Entry<String, ArrayList<String>> jc : set) {
			String schedulerId = jc.getKey();
			ArrayList<String> jobChains = jc.getValue();

			boolean checkJobChains = true;
			if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
				try{
					if (!step.getOrderSchedulerId().matches(schedulerId)) {
						checkJobChains = false;
					}
				}
				catch(Exception ex){
					throw new Exception(String.format("%s: %s) check with configured scheduler_id = %s: %s",
							method,
							counter.getTotal(),
							schedulerId,
							ex));
				}
			}
			if (checkJobChains) {
				for (int i = 0; i < jobChains.size(); i++) {
					String jobChain = jobChains.get(i);
					
					logger.debug(String.format("%s: %s) check with configured: schedulerId = %s, jobChain = %s",
							method,
							counter.getTotal(),
							schedulerId,
							jobChain));
					
					if(jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
					  return true;	
					}
					try{
						if(step.getOrderJobChain().matches(jobChain)) {
							return true;
						}
					}
					catch(Exception ex){
						throw new Exception(String.format("%s: %s) check with configured scheduler_id = %s, name = %s: %s",
								method,
								counter.getTotal(),
								schedulerId,
								jobChain,
								ex));
					}
				}
			}
		}
		
	return false;
	}
	
	private Date getLastNotificationDate(DBItemNotificationSchedulerVariables dbItem, Date dateTo) throws Exception{
		Date dateFrom = getDbLayer().getLastNotificationDate(dbItem);
		int maxAge = NotificationModel.resolveAge2Minutes(options.max_history_age.Value());
		
		if(dateFrom != null){
			Long startTimeMinutes = dateFrom.getTime() / 1000 / 60;
			Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
			Long diffMinutes = endTimeMinutes - startTimeMinutes;
			if(diffMinutes > maxAge){
				dateFrom = null;
			}
		}
		if(dateFrom == null){
			dateFrom = DBLayer.getDateTimeMinusMinutes(dateTo,maxAge);
		}
		return dateFrom;
	}
	
	private void updateExistingNotifications() throws Exception{
		String method = "updateExistingNotifications";
	
		getDbLayer().getConnection().beginTransaction();
		
		
		Date maxStartTime = null;
		int uncompletedAge = NotificationModel.resolveAge2Minutes(options.max_uncompleted_age.Value());
		if(uncompletedAge > 0){
			maxStartTime = DBLayer.getCurrentDateTimeMinusMinutes(uncompletedAge);
		}
				
		int result = getDbLayer().updateUncompletedNotifications(largeResultFetchSize, options.allow_db_dependent_queries.value(),maxStartTime);
		logger.info(String.format("%s: bulk updateUncompletedNotifications, max_uncompleted_age = %s (from %s), updated = %s",
				method,
				options.max_uncompleted_age.Value(),
				DBLayer.getDateAsString(maxStartTime),
				result));
		
		result = getDbLayer().setOrderNotificationsRecovered();
		logger.info(String.format("%s: bulk setOrderNotificationsRecovered, updated = %s",method,result));
		
		getDbLayer().getConnection().commit();
	}
	
	@Override
	public void process() throws Exception {
		String method = "process";
		
		Criteria criteria = null;
		
		SOSHibernateResultSetProcessor rspHistory = new SOSHibernateResultSetProcessor(getDbLayer().getConnection());
		SOSHibernateBatchProcessor bpNotifications = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
		SOSHibernateBatchProcessor bpTimers = new SOSHibernateBatchProcessor(getDbLayer().getConnection());
				
		initCounters();
		
		Date dateTo = DBLayer.getCurrentDateTime();
		Date dateFrom = null;
		DBItemNotificationSchedulerVariables schedulerVariable = null;
		boolean success = false;
		
		try {
			DateTime start = new DateTime();
		
			schedulerVariable = getDbLayer().getSchedulerVariable();
			dateFrom = getLastNotificationDate(schedulerVariable,dateTo);
						
			updateExistingNotifications();
				
			bpNotifications.createInsertBatch(DBItemSchedulerMonNotifications.class);
			bpTimers.createInsertBatch(DBItemSchedulerMonChecks.class);
			
			getDbLayer().getConnection().beginTransaction();
			criteria = getDbLayer().getSchedulerHistorySteps(largeResultFetchSize, dateFrom, dateTo);
			ResultSet rsHistory = rspHistory.createResultSet(DBItemNotificationSchedulerHistoryOrderStep.class,criteria,ScrollMode.FORWARD_ONLY,largeResultFetchSize);
			while (rsHistory.next()) {
				counter.addTotal();
				
				DBItemNotificationSchedulerHistoryOrderStep step = (DBItemNotificationSchedulerHistoryOrderStep) rspHistory.get();
				
				if (step.getOrderHistoryId() == null && step.getOrderId() == null && step.getOrderStartTime() == null) {
					counter.addSkip();
					logger.debug(String
							.format("%s: %s) order object is null. step = %s, historyId = %s ",
									method, counter.getTotal(), step.getStepState(),
									step.getStepHistoryId()));

					continue;
				}
				if (step.getTaskId() == null && step.getTaskJobName() == null && step.getTaskCause() == null) {
					counter.addSkip();
					logger.debug(String
							.format("%s: %s) task object is null. jobChain = %s, order = %s, step = %s, taskId = %s ",
									method, counter.getTotal(), step.getOrderJobChain(),
									step.getOrderId(), step.getStepState(),
									step.getStepTaskId()));
					continue;
				}

				logger.debug(String
						.format("%s: %s) schedulerId = %s, orderHistoryId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
								method, counter.getTotal(), step.getOrderSchedulerId(),
								step.getOrderHistoryId(), step.getOrderJobChain(),
								step.getOrderId(), step.getStepStep(),
								step.getStepState()));

				if(!this.checkInsertNotification(counter,step)){
					counter.addSkip();
					logger.debug(String.format("%s: %s) skip insert notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							method,
							counter.getTotal(),
							step.getOrderSchedulerId(),
							step.getOrderJobChain(),
							step.getOrderId(),
							step.getStepStep(),
							step.getStepState()));
					continue;
				}
				
				if(counter.getTotal() % options.batch_size.value() == 0){
					counter.addBatchInsert(SOSHibernateBatchProcessor.getExecutedBatchSize(bpNotifications.executeBatch()));
					counter.addBatchInsertTimer(SOSHibernateBatchProcessor.getExecutedBatchSize(bpTimers.executeBatch()));
				}
				
				DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification(
						step.getOrderSchedulerId(), false,
						step.getTaskId(),
						step.getStepStep(),
						step.getOrderHistoryId());

				boolean hasStepError = step.getStepError();
				if (dbItem == null) {
					counter.addInsert();
					logger.debug(String.format("%s: %s) create new notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							method,
							counter.getTotal(),
							step.getOrderSchedulerId(),
							step.getOrderJobChain(),
							step.getOrderId(),
							step.getStepStep(),
							step.getStepState()));

					dbItem = getDbLayer().createNotification(
							step.getOrderSchedulerId(), false,
							step.getTaskId(),
							step.getStepStep(),
							step.getOrderHistoryId(),
							step.getOrderJobChain(),
							step.getOrderJobChain(),
							step.getOrderId(), 
							step.getOrderId(),
							step.getOrderStartTime(),
							step.getOrderEndTime(), 
							step.getStepState(),
							step.getStepStartTime(), 
							step.getStepEndTime(),
							step.getTaskJobName(), 
							step.getTaskJobName(),
							step.getTaskStartTime(), 
							step.getTaskEndTime(),
							false, 
							hasStepError,
							step.getStepErrorCode(),
							step.getStepErrorText());

					bpNotifications.addBatch(dbItem);
				}
				else {
					counter.addUpdate();
					//kann inserted sein durch StoreResult Job
					
					dbItem.setJobChainName(step.getOrderJobChain());
					dbItem.setJobChainTitle(step.getOrderJobChain());

					dbItem.setOrderId(step.getOrderId());
					dbItem.setOrderTitle(step.getOrderId());
					dbItem.setOrderStartTime(step.getOrderStartTime());
					dbItem.setOrderEndTime(step.getOrderEndTime());

					dbItem.setOrderStepState(step.getStepState());
					dbItem.setOrderStepStartTime(step.getStepStartTime());
					dbItem.setOrderStepEndTime(step.getStepEndTime());

					dbItem.setJobName(step.getTaskJobName());
					dbItem.setJobTitle(step.getTaskJobName());
					dbItem.setTaskStartTime(step.getTaskStartTime());
					dbItem.setTaskEndTime(step.getTaskEndTime());

					// hatte error und wird auf nicht error gesetzt
					dbItem.setRecovered(dbItem.getError() && !hasStepError);
					dbItem.setError(hasStepError);
					dbItem.setErrorCode(step.getStepErrorCode());
					dbItem.setErrorText(step.getStepErrorText());

					dbItem.setModified(DBLayer.getCurrentDateTime());

					logger.debug(String.format("%s: %s) update notification. notification id = %s, order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							method,
							counter.getTotal(),
							dbItem.getId(),
							dbItem.getSchedulerId(),
							dbItem.getJobChainName(),
							dbItem.getOrderId(),
							dbItem.getStep(),
							dbItem.getOrderStepState()));
					
					getDbLayer().getConnection().update(dbItem);
				}
				
				bpTimers = insertTimer(counter,bpTimers,dbItem);	
			}//while
			
			counter.addBatchInsert(SOSHibernateBatchProcessor.getExecutedBatchSize(bpNotifications.executeBatch()));
			counter.addBatchInsertTimer(SOSHibernateBatchProcessor.getExecutedBatchSize(bpTimers.executeBatch()));
			
			getDbLayer().getConnection().commit();
			
			success = true;
			
			logger.info(String.format("%s: duration = %s",method,NotificationModel.getDuration(start,new DateTime())));
		} 
		catch (Exception ex) {
		    Throwable e = SOSHibernateConnection.getException(ex);
		    try{
	            getDbLayer().getConnection().rollback();
	        }
	        catch(Exception exx){
	            logger.warn(String.format("%s: %s", method, exx.toString()),exx);
	        }
			//schedulerConnection.rollback();
			throw new Exception(String.format("%s: %s", method, e.toString()),e);
		}
		finally{
			try{bpNotifications.close();}catch(Exception ex){}
			try{bpTimers.close();}catch(Exception ex){}
			try{if(rspHistory != null){	rspHistory.close();}}catch(Exception ex){}
		}
		
		logger.info(String
				.format("%s: total %s: inserted = %s (batch = %s), updated = %s, skipped = %s, inserted timers = %s (batch = %s)",
						method,
						counter.getTotal(),
						counter.getInsert(),
						counter.getBatchInsert(),
						counter.getUpdate(),
						counter.getSkip(),
						counter.getInsertTimer(),
						counter.getBatchInsertTimer()));
		
		if(success){
			pluginsOnProcess(timers, options, getDbLayer(),dateFrom,dateTo);
			
			getDbLayer().getConnection().beginTransaction();
			getDbLayer().setLastNotificationDate(schedulerVariable,dateTo);
			getDbLayer().getConnection().commit();
		}

	}

	/**
	 * 
	 * @param counter
	 * @param bp
	 * @param dbItem
	 * @return
	 * @throws Exception
	 */
	private SOSHibernateBatchProcessor insertTimer(CounterCheckHistory counter, SOSHibernateBatchProcessor bp, DBItemSchedulerMonNotifications dbItem) throws Exception{
		//Indent für die Ausgabe
		String method = "  insertTimer";
		if(timers == null){
			logger.debug(String
					.format("%s: %s) skip do check. timers is null. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s)",
						method,
						counter.getTotal(),
						dbItem.getId(),
						dbItem.getSchedulerId(),
						dbItem.getJobChainName(),
						dbItem.getStep(),
						dbItem.getOrderStepState()));
			return bp;
		}
		
		//wir schreiben nur die erste notification (step 1)
		if(dbItem.getStep().equals(new Long(1))){
			Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
			for (Map.Entry<String, ElementTimer> me : set) {
				String timerName = me.getKey();
				ElementTimer timer = me.getValue();
				
				ArrayList<ElementTimerJobChain> jobChains = timer.getJobChains();
				
				if(jobChains.size() == 0){
					logger.warn(String
							.format("%s: %s) timer = %s. timer JobChains not found. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s)",
								method,
								counter.getTotal(),
								timerName,
								jobChains.size(),
								dbItem.getId(),
								dbItem.getSchedulerId(),
								dbItem.getJobChainName(),
								dbItem.getStep(),
								dbItem.getOrderStepState()));
					continue;
				}
				
				for (int i = 0; i < jobChains.size(); i++) {
					ElementTimerJobChain jobChain = jobChains.get(i);
					String schedulerId = jobChain.getSchedulerId();
					String name = jobChain.getName();
					
					boolean insert = true;
					if(!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
						if(!dbItem.getSchedulerId().matches(schedulerId)){
							logger.debug(String
									.format("%s: %s) skip insert check. notification.schedulerId \"%s\" not match timer schedulerId \"%s\" ( timer  name = %s, notification.id = %s (jobChain = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
										method, 
										counter.getTotal(),
										dbItem.getSchedulerId(),
										schedulerId,
										timerName,
										dbItem.getId(),
										dbItem.getJobChainName(),
										dbItem.getStep(),
										dbItem.getOrderStepState(),
										jobChain.getStepFrom(),
										jobChain.getStepTo()));
					
							insert = false;
						}
					}
					
					if(insert){
						if(!name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
							if(!dbItem.getJobChainName().matches(name)){
								logger.debug(String
										.format("%s: %s) skip insert check. notification.jobChain \"%s\" not match timer job chain \"%s\" ( timer  name = %s, notification.id = %s (scheduler = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
											method, 
											counter.getTotal(),
											dbItem.getJobChainName(),
											name,
											timerName,
											dbItem.getId(),
											dbItem.getSchedulerId(),
											dbItem.getStep(),
											dbItem.getOrderStepState(),
											jobChain.getStepFrom(),
											jobChain.getStepTo()));
								
								insert = false;
							}
						}	
					}
					
					if(insert){
						counter.addInsertTimer();
						logger.debug(String
									.format("%s: %s) insert check. name = %s, notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
										method, 
										counter.getTotal(),
										timerName,
										dbItem.getId(),
										dbItem.getSchedulerId(),
										dbItem.getJobChainName(),
										dbItem.getStep(),
										dbItem.getOrderStepState(),
										jobChain.getStepFrom(),
										jobChain.getStepTo()));

						DBItemSchedulerMonChecks item = getDbLayer()
									.createCheck(timerName, dbItem,
											jobChain.getStepFrom(),
											jobChain.getStepTo(),
											dbItem.getOrderStartTime(),
											dbItem.getOrderEndTime());

						bp.addBatch(item);
					}
					else{
						logger.debug(String.format("%s: %s) not inserted. timer (name = %s, schedulerId = %s, jobChain = %s, stepFrom = %s, stepTo = %s),  notification (id = %s, jobChain = %s, step = %s, step state = %s)",
								method, 
								counter.getTotal(),
								timerName,
								jobChain.getSchedulerId(),
								jobChain.getName(),
								jobChain.getStepFrom(),
								jobChain.getStepTo(),
								dbItem.getId(),
								dbItem.getJobChainName(),
								dbItem.getStep(),
								dbItem.getOrderStepState()));
					}
				}
			}
		}
		else{
			logger.debug(String
					.format("%s: %s) skip do check. step is not equals 1. notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s)",
						method,
						counter.getTotal(),
						dbItem.getId(),
						dbItem.getSchedulerId(),
						dbItem.getJobChainName(),
						dbItem.getStep(),
						dbItem.getOrderStepState()));
		}
		return bp;
	}
	
	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 */
	private void pluginsOnInit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {

		for (ICheckHistoryPlugin plugin : plugins) {
			try {
				plugin.onInit(timers, options, dbLayer);
			} catch (Exception ex) {
				logger.warn(ex.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 */
	private void pluginsOnExit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {

		for (ICheckHistoryPlugin plugin : plugins) {
			try {
				plugin.onExit(timers, options, dbLayer);
			} catch (Exception ex) {
				logger.warn(ex.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 * @param dateFrom
	 * @param dateTo
	 */
	private void pluginsOnProcess(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer,
			Date dateFrom, Date dateTo) {

		for (ICheckHistoryPlugin plugin : plugins) {
			try {
				plugin.onProcess(timers, options, dbLayer, dateFrom, dateTo);
			} catch (Exception ex) {
				logger.warn(String.format("plugin.onProcess: %s",
						ex.getMessage()));
			}
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void registerPlugins() throws Exception {
		plugins = new ArrayList<ICheckHistoryPlugin>();

		if (!SOSString.isEmpty(this.options.plugins.Value())) {
			String[] arr = this.options.plugins.Value().trim().split(";");
			for (int i = 0; i < arr.length; i++) {
				try {
					Class<ICheckHistoryPlugin> c = (Class<ICheckHistoryPlugin>) Class
							.forName(arr[i].trim());
					addPlugin(c.newInstance());

					logger.info(String.format("plugin created = %s", arr[i]));
				} catch (Exception ex) {
					logger.error(String.format(
							"plugin cannot be registered(%s) : %s", arr[i],
							ex.getMessage()));
				}
			}
		}
		logger.debug(String.format("plugins registered = %s",
				plugins.size()));

	}

	/**
	 * 
	 * @param handler
	 */
	public void addPlugin(ICheckHistoryPlugin handler) {
		plugins.add(handler);
	}

	/**
	 * 
	 */
	public void resetPlugins() {
		plugins = new ArrayList<ICheckHistoryPlugin>();
	}
	

}
