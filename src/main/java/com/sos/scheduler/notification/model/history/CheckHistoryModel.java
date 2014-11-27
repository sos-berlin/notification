package com.sos.scheduler.notification.model.history;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

import com.sos.scheduler.notification.db.DBItemSchedulerHistory;
import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerOrderHistory;
import com.sos.scheduler.notification.db.DBItemSchedulerOrderStepHistory;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.helper.ElementTimerJobChain;
import com.sos.scheduler.notification.helper.NotificationXmlHelper;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.scheduler.notification.model.NotificationModel;
import com.sos.scheduler.notification.plugins.history.ICheckHistoryPlugin;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
public class CheckHistoryModel extends NotificationModel {

	final Logger logger = LoggerFactory.getLogger(CheckHistoryModel.class);

	CheckHistoryJobOptions options = null;
	private LinkedHashMap<String, ElementTimer> timers = null;
	private LinkedHashMap<String, ArrayList<String>> jobChains = null;
	private boolean checkInsertNotifications = true;
	private List<ICheckHistoryPlugin> plugins = null;
	
	private int countInsert = 0;
	private int countUpdate = 0;
	private int countSkip = 0;
	private int countTotal = 0;
	private int countInsertTimer = 0;

	/**
	 * 
	 * @param pOptions
	 */
	public CheckHistoryModel(CheckHistoryJobOptions opt) {
		this.options = opt;
		this.plugins = new ArrayList<ICheckHistoryPlugin>();
		this.timers = new LinkedHashMap<String, ElementTimer>();
		this.jobChains = new LinkedHashMap<String,ArrayList<String>>();
	}

	/**
	 * 
	 */
	private void initCounters(){
		this.countInsert = 0;
		this.countUpdate = 0;
		this.countSkip = 0;
		this.countTotal = 0;
		this.countInsertTimer = 0;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
		logger.info(String.format("init"));

		super.doInit(this.options.hibernate_configuration_file.Value(), false);

		this.initConfig();

		this.registerPlugins();
		this.pluginsOnInit(this.timers, this.options, this.getDbLayer());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void initConfig() throws Exception {
		File dir = null;

		File schemaFile = new File(options.configuration_schema_file.Value());

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
				
		this.readConfigFiles(dir);
	}

	/**
	 * 
	 * @param dir
	 * @throws Exception
	 */
	private void readConfigFiles(File dir) throws Exception {
		this.jobChains = new LinkedHashMap<String,ArrayList<String>>();
		this.timers = new LinkedHashMap<String, ElementTimer>();
		this.checkInsertNotifications = true;
				
		File[] files = getAllConfigurationFiles(dir);
		if (files.length == 0) {
			throw new Exception(String.format(
					"configuration files not found. directory : %s",
					dir.getAbsolutePath()));
		}

		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			logger.info(String.format("reading configuration file %s",
					f.getAbsolutePath()));

			SOSXMLXPath xpath = new SOSXMLXPath(f.getAbsolutePath());
			
			this.setConfigAllJobChains(xpath);
			this.setConfigTimers(xpath);
		}

		if(this.jobChains.size() == 0 && this.timers.size() == 0){
			throw new Exception("jobChains or timers definitions not founded");
		}
		
	}

	/**
	 * 
	 * @param xpath
	 * @throws Exception
	 */
	private void setConfigTimers(SOSXMLXPath xpath) throws Exception{
		NodeList nlTimers = NotificationXmlHelper.selectTimerDefinitions(xpath);
		for (int j = 0; j < nlTimers.getLength(); j++) {
			Node n = nlTimers.item(j);
			String name = NotificationXmlHelper.getTimerName((Element)n);
			if (name != null && !this.timers.containsKey(name)) {
				this.timers.put(name, new ElementTimer(n));
			}
		}
	}
	
	/**
	 * 
	 * @param xpath
	 * @throws Exception
	 */
	private void setConfigAllJobChains(SOSXMLXPath xpath) throws Exception{
		NodeList notificationJobChains = NotificationXmlHelper.selectNotificationJobChainDefinitions(xpath);
		this.setConfigJobChains(xpath,notificationJobChains);
		if(this.checkInsertNotifications){
			NodeList timerJobChains = NotificationXmlHelper.selectTimerJobChainDefinitions(xpath);
			this.setConfigJobChains(xpath,timerJobChains);
		}
	}
	
	/**
	 * 
	 * @param xpath
	 * @param nlJobChains
	 * @throws Exception
	 */
	private void setConfigJobChains(SOSXMLXPath xpath,NodeList nlJobChains) throws Exception{
		//@TODO jobChains Definitions auch von timers lesen
		for (int j = 0; j < nlJobChains.getLength(); j++) {
			Element jobChain = (Element)nlJobChains.item(j);
			
			String schedulerId = NotificationXmlHelper.getSchedulerId(jobChain);
			String name = NotificationXmlHelper.getJobChainName(jobChain);
			
			schedulerId = SOSString.isEmpty(schedulerId) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : schedulerId;
			name        = SOSString.isEmpty(name) ? DBLayerSchedulerMon.DEFAULT_EMPTY_NAME : name;
			
			ArrayList<String> al = new ArrayList<String>();
			if(schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME) && name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
				this.jobChains = new LinkedHashMap<String,ArrayList<String>>();
				al.add(name);
				this.jobChains.put(schedulerId,al);
				this.checkInsertNotifications = false;
				return;
			}
			
			if(this.jobChains.containsKey(schedulerId)){
				al = this.jobChains.get(schedulerId);
			}
			if(!al.contains(name)){
				al.add(name);
			}
			this.jobChains.put(schedulerId,al);
		}
	}
	
	/**
     * 
     */
	@Override
	public void exit() throws Exception {
		logger.info(String.format("exit"));

		this.pluginsOnExit(this.timers, this.options, this.getDbLayer());
		super.exit();
	}

	/**
	 * @TODO eventuell einen Fleck für insertTimer setzen, 
	 * damit nicht noch mal die Timers geprüft werden
	 * 
	 * @param order
	 * @return
	 * @throws Exception
	 */
	private boolean checkInsertNotification(DBItemSchedulerOrderHistory order) throws Exception{
		
		//Indent für die Ausgabe
		String functionName = "  checkInsertNotification";
		logger.debug(String.format("%s: checkInsertNotifications = %s",
				functionName,
				this.checkInsertNotifications));
				
		
		if(!this.checkInsertNotifications){
			return true;
		}
		if(this.jobChains == null || this.jobChains.size() == 0){
			return false;
		}
		
		logger.debug(String.format("%s: order: schedulerId = %s, jobChain = %s",
				functionName,
				order.getSpoolerId(),
				order.getJobChain()));
		
		Set<Map.Entry<String, ArrayList<String>>> set = this.jobChains.entrySet();
		for (Map.Entry<String, ArrayList<String>> jc : set) {
			String schedulerId = jc.getKey();
			ArrayList<String> jobChains = jc.getValue();

			boolean checkJobChains = true;
			if (!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
				try{
					if (!order.getSpoolerId().matches(schedulerId)) {
						checkJobChains = false;
					}
				}
				catch(Exception ex){
					throw new Exception(String.format("%s: check with configured scheduler_id = %s: %s",
							functionName,
							schedulerId,
							ex));
				}
			}
			if (checkJobChains) {
				for (int i = 0; i < jobChains.size(); i++) {
					String jobChain = jobChains.get(i);
					
					logger.debug(String.format("%s: check with configured: schedulerId = %s, jobChain = %s",
							functionName,
							schedulerId,
							jobChain));
					
					if(jobChain.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)) {
					  return true;	
					}
					try{
						if(order.getJobChain().matches(jobChain)) {
							return true;
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
		}
		
		
	return false;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Override
	public void process() throws Exception {
		String functionName = "process";
		
		logger.info(String.format("%s: start",functionName));

		super.process();

		this.getDbLayer().beginTransaction();

		logger.info(String.format("%s: bulk updateNotifications",functionName));
		this.getDbLayer().updateNotifications(
				this.options.allow_db_dependent_queries.value());

		logger.info(String
				.format("%s: bulk setOrderNotificationsRecovered",functionName));
		this.getDbLayer().setOrderNotificationsRecovered();
		this.getDbLayer().commit();

		Date dateFrom = this.getDbLayer().getLastNotificationDate();
		Date dateTo = DBLayerSchedulerMon.getCurrentDateTime();
		
		
		this.initCounters();
		
		List<DBItemSchedulerOrderStepHistory> steps = this.getDbLayer().getOrderStepsAsList(dateFrom, dateTo);
		this.countTotal = steps.size();
		logger.info(String
				.format("%s: founded %s entries. getOrderStepsAsList (dateFrom = %s (UTC), dateTo = %s (UTC))",
						functionName,
						this.countTotal,
						DBLayerSchedulerMon.getDateAsString(dateFrom),
						DBLayerSchedulerMon.getDateAsString(dateTo)));
		/**
		ScrollableResults sr = this.getDbLayer().getOrderStepsScrollable(
				dateFrom, dateTo);
		int m = 0;
		*/
		
		this.getDbLayer().beginTransaction();
		
		try {
			for(int m=0;m<steps.size();m++){
			//while (sr.next()) {
				//m++;
				//this.getDbLayer().flushScrollableResults(m);
				//DBItemSchedulerOrderStepHistory step = (DBItemSchedulerOrderStepHistory)sr.get(0);
				
				DBItemSchedulerOrderStepHistory step = steps.get(m);
				DBItemSchedulerOrderHistory order = step.getSchedulerOrderHistoryDBItem();
				DBItemSchedulerHistory task = step.getSchedulerHistoryDBItem();
				if(order == null){
					this.countSkip++;
					logger.debug(String
							.format("%s: %s) order object is null. order notification: step = %s, taskId = %s ",
									functionName,
									m,
									step.getState(),
									step.getTaskId()));
					continue;
				}
				if(task == null){
					this.countSkip++;
					logger.debug(String
							.format("%s: %s) task object is null. order notification: jobChain = %s, order = %s, step = %s, taskId = %s ",
									functionName,
									m,
									order.getJobChain(),
									order.getOrderId(),
									step.getState(),
									step.getTaskId()));
					continue;
				}
				logger.debug(String.format("%s: %s) order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
						functionName,
						m,
						order.getSchedulerId(),
						order.getJobChain(),
						order.getOrderId(),
						step.getId().getStep(),
						step.getState()));
				
				if(!this.checkInsertNotification(order)){
					this.countSkip++;
					logger.debug(String.format("%s: %s) skip insert notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							functionName,
							m,
							order.getSchedulerId(),
							order.getJobChain(),
							order.getOrderId(),
							step.getId().getStep(),
							step.getState()));
					continue;
				}
								
				DBItemSchedulerMonNotifications dbItem = this.getDbLayer().getNotification(
										order.getSchedulerId(), false,
										step.getTaskId(),
										step.getId().getStep(),
										order.getHistoryId());

				boolean hasStepError = (step.getError() != null && step.getError());

				if (dbItem == null) {
					this.countInsert++;
					logger.debug(String.format("%s: %s) create new notification. order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							functionName,
							m,
							order.getSchedulerId(),
							order.getJobChain(),
							order.getOrderId(),
							step.getId().getStep(),
							step.getState()));

					dbItem = this.getDbLayer().createNotification(
							order.getSchedulerId(), false,
							step.getTaskId(),
							step.getId().getStep(),
							order.getHistoryId(),
							order.getJobChain(),
							order.getJobChain(),
							order.getOrderId(), order.getOrderId(),
							order.getStartTime(),
							order.getEndTime(), step.getState(),
							step.getStartTime(), step.getEndTime(),
							task.getJobName(), task.getJobName(),
							task.getStartTime(), task.getEndTime(),
							false, hasStepError,
							step.getErrorCode(),
							step.getErrorText());

					this.getDbLayer().save(dbItem);
				}
				else {
					this.countUpdate++;
					//kann inserted sein durch StoreResult Job
					
					dbItem.setJobChainName(order.getJobChain());
					dbItem.setJobChainTitle(order.getJobChain());

					dbItem.setOrderId(order.getOrderId());
					dbItem.setOrderTitle(order.getOrderId());
					dbItem.setOrderStartTime(order.getStartTime());
					dbItem.setOrderEndTime(order.getEndTime());

					dbItem.setOrderStepState(step.getState());
					dbItem.setOrderStepStartTime(step.getStartTime());
					dbItem.setOrderStepEndTime(step.getEndTime());

					dbItem.setJobName(task.getJobName());
					dbItem.setJobTitle(task.getJobName());
					dbItem.setTaskStartTime(task.getStartTime());
					dbItem.setTaskEndTime(task.getEndTime());

					// hatte error und wird auf nicht error gesetzt
					dbItem.setRecovered(dbItem.getError() && !hasStepError);
					dbItem.setError(hasStepError);
					dbItem.setErrorCode(step.getErrorCode());
					dbItem.setErrorText(step.getErrorText());

					dbItem.setModified(DBLayerSchedulerMon.getCurrentDateTime());

					logger.debug(String.format("%s: %s) update notification. notification id = %s, order schedulerId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
							functionName,
							m,
							dbItem.getId(),
							dbItem.getSchedulerId(),
							dbItem.getJobChainName(),
							dbItem.getOrderId(),
							dbItem.getStep(),
							dbItem.getOrderStepState()));
					
					this.getDbLayer().update(dbItem);
				}
				this.insertTimer(dbItem);
						//this.getDbLayer().commit();
			}
		} catch (Exception ex) {
			this.getDbLayer().rollback();
			throw new Exception(ex);

		} finally {
			/**
			try {
				if (sr != null) {
					sr.close();
				}
			} catch (Exception e) {
			}*/
		}

		this.getDbLayer().setLastNotificationDate(dateTo);
		this.getDbLayer().commit();

		logger.info(String
				.format("%s: total %s: inserted = %s, updated = %s, skipped = %s, inserted timers = %s",
						functionName,
						this.countTotal,
						this.countInsert,
						this.countUpdate,
						this.countSkip,
						this.countInsertTimer));
		
		this.pluginsOnProcess(this.timers, this.options, this.getDbLayer(),
				dateFrom, dateTo);

		logger.info(String.format("%s: end",functionName));

	}

	/**
	 * 
	 * @param dbItem
	 * @throws Exception
	 */
	private void insertTimer(DBItemSchedulerMonNotifications dbItem) throws Exception{
		//Indent für die Ausgabe
		String functionName = "  insertTimer";
		//wir schreiben nur die erste notification (step 1)
		
		if(this.timers != null && dbItem.getStep().equals(new Long(1))){
			Set<Map.Entry<String, ElementTimer>> set = this.timers.entrySet();
			for (Map.Entry<String, ElementTimer> me : set) {
				String timerName = me.getKey();
				ElementTimer timer = me.getValue();
				
				ArrayList<ElementTimerJobChain> jobChains = timer.getJobChains();
				for (int i = 0; i < jobChains.size(); i++) {
					ElementTimerJobChain jobChain = jobChains.get(i);
					String schedulerId = jobChain.getSchedulerId();
					String name = jobChain.getName();
					
					boolean insert = true;
					if(!schedulerId.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
						if(!dbItem.getSchedulerId().matches(schedulerId)){
							insert = false;
						}
					}
					
					if(insert){
						if(!name.equals(DBLayerSchedulerMon.DEFAULT_EMPTY_NAME)){
							if(!dbItem.getJobChainName().matches(name)){
								insert = false;
							}
						}	
					}
					if(insert){
						this.countInsertTimer++;
						logger.debug(String
									.format("%s: insert check. name = %s, notification.id = %s (scheduler = %s, jobChain = %s, step = %s, step state = %s), stepFrom = %s, stepTo = %s ",
										functionName, timerName,
										dbItem.getId(),
										dbItem.getSchedulerId(),
										dbItem.getJobChainName(),
										dbItem.getStep(),
										dbItem.getOrderStepState(),
										jobChain.getStepFrom(),
										jobChain.getStepTo()));

						DBItemSchedulerMonChecks item = this.getDbLayer()
									.createCheck(timerName, dbItem.getId(),
											jobChain.getStepFrom(),
											jobChain.getStepTo(),
											dbItem.getOrderStartTime(),
											dbItem.getOrderEndTime());

						this.getDbLayer().save(item);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param timers
	 * @param options
	 * @param dbLayer
	 */
	private void pluginsOnInit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer) {

		for (ICheckHistoryPlugin plugin : this.plugins) {
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

		for (ICheckHistoryPlugin plugin : this.plugins) {
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

		for (ICheckHistoryPlugin plugin : this.plugins) {
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
		logger.info(String.format("plugins registered = %s",
				this.plugins.size()));

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
