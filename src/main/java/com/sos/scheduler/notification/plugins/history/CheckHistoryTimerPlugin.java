package com.sos.scheduler.notification.plugins.history;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonResults;
import com.sos.scheduler.notification.db.DBLayer;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.CounterCheckHistoryTimer;
import com.sos.scheduler.notification.helper.EEndTimeType;
import com.sos.scheduler.notification.helper.EStartTimeType;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.helper.ElementTimerScript;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;

public class CheckHistoryTimerPlugin implements ICheckHistoryPlugin {

	static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryTimerPlugin.class);
	private CounterCheckHistoryTimer counter;
	
	@Override
	public void onInit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer)
			throws Exception {

	}

	@Override
	public void onExit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer)
			throws Exception {
	}

	@Override
	public void onProcess(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer,
			Date dateFrom, Date dateTo) throws Exception {
		String method = "onProcess";
		
		if (dbLayer == null) {
			throw new Exception("dbLayer is NULL");
		}
		if (timers == null) {
			throw new Exception("timers is NULL");
		}

		if (timers.size() == 0) {
			LOGGER.info(String.format("%s: skip. found 0 timers definitions",method));
			return;
		}

		initCountChecks();
		
		Optional<Integer> largeResultFetchSize = Optional.empty();
		try{
            int fetchSize = options.large_result_fetch_size.value();
            if(fetchSize != -1){
                largeResultFetchSize = Optional.of(fetchSize);
            }
		}
        catch(Exception ex){}
		
		List<DBItemSchedulerMonChecks> result = dbLayer.getSchedulerMonChecksForSetTimer(largeResultFetchSize);
		LOGGER.info(String.format("%s: found %s timer definitions and %s timers for check in the db",
				method,
				timers.size(),
				result.size()));
		
		for (int i = 0; i < result.size(); i++) {
			DBItemSchedulerMonChecks check = result.get(i);
			if (!timers.containsKey(check.getName())) {
				counter.addSkip();
				LOGGER.debug(String.format("%s: skip check for %s. timer definition is not found.",
						method,
						check.getName()));
				continue;
			}
			
			check = checkNotification(check, dbLayer);
			analyzeCheck(dbLayer, check, timers.get(check.getName()));
		}
		
		LOGGER.info(String.format("%s: checks created = %s, removed = %s, skipped = %s, checks for rerun = %s",
				method,
				counter.getTotal(),
				counter.getRemove(),
				counter.getSkip(),
				counter.getRerun()));
	}

	private DBItemSchedulerMonChecks checkNotification(DBItemSchedulerMonChecks check,DBLayerSchedulerMon dbLayer) throws Exception{
		String method = "checkNotification";
		
		//wegen batch insert bei den Datenbanken ohne autoincrement
		if(check.getNotificationId().equals(new Long(0))){
			if(SOSString.isEmpty(check.getResultIds())){
				throw new Exception(String.format("%s: could not execute check (id = %s): notificationId = 0, resultIds is empty",
						method,
						check.getId()));
			}
			String[] arr = check.getResultIds().split(";");
			if(arr.length < 5){
				throw new Exception(String.format("%s: could not execute check (id = %s): missing notification infos. resultIds = %s",
						method,
						check.getId(),
						check.getResultIds()));
			}
			
			DBItemSchedulerMonNotifications notification = dbLayer.getNotification(arr[0],Boolean.parseBoolean(arr[1]),new Long(arr[2]),new Long(arr[3]),new Long(arr[4]));
			if(notification == null){
				throw new Exception(String.format("%s: could not execute check (id = %s): notification not found, schedulerId = %s, standalone = %s, taskId = %s, step = %s, orderHistoryId = %s",
						method,
						check.getId(),
						arr[0],
						arr[1],
						arr[2],
						arr[3],
						arr[4]));
			}
			check.setNotificationId(notification.getId());
			check.setResultIds(null);
		}
				
		return check;
	}
	
	private void initCountChecks(){
		counter = new CounterCheckHistoryTimer();
	}
	
	private void analyzeCheck(DBLayerSchedulerMon dbLayer,
			DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
		String method = "analyzeCheck"; 
		
		LOGGER.debug(String.format("%s: id = %s, name = %s, stepFrom = %s, stepTo = %s, notificationId = %s",
				method,
				check.getId(),
				check.getName(),
				check.getStepFrom(),
				check.getStepTo(),
				check.getNotificationId()));
		
		String minValue = null;
		String maxValue = null;
	
		Long stepFromIndex = new Long(0);
		Long stepToIndex = new Long(0);
		Long lastIndex = new Long(0);
		DBItemSchedulerMonNotifications minNotification = null;
		DBItemSchedulerMonNotifications stepFromNotification = null;
		DBItemSchedulerMonNotifications stepToNotification = null;

		List<DBItemSchedulerMonNotifications> steps = dbLayer.getNotificationOrderSteps(check.getNotificationId());
		for (DBItemSchedulerMonNotifications step : steps) {
			if (step.getId().equals(check.getNotificationId())) {
				minNotification = step;
			}
			if (stepFromIndex.equals(new Long(0)) && step.getOrderStepState().equalsIgnoreCase(check.getStepFrom())) {
				stepFromIndex = step.getStep();
				stepFromNotification = step;
			}
			if (step.getOrderStepState().equalsIgnoreCase(check.getStepTo())) {
				stepToIndex = step.getStep();
				stepToNotification = step;
			}
			lastIndex = step.getStep();
		}
		
		if (minNotification == null) {
			return;
		}
		if (minNotification.getOrderStartTime() == null) {
			LOGGER.debug(String.format("do continue. getOrderStartTime is NULL (minNotification.id = %s",
					minNotification.getId()));
			return;
		}
		if (stepToIndex.equals(new Long(0))) {
			stepToIndex = lastIndex;
		}

		ElementTimerScript minElement = timer.getMinimum();
		ElementTimerScript maxElement = timer.getMaximum();
		
		boolean selectResults = false;
		if (minElement != null) {
			if(minElement.getValue() == null){
				throw new Exception("Script/Minimum value is null");
			}
			if (minElement.getValue().contains("%")) {
				selectResults = true;
			}
		}
		
		if (maxElement != null) {
			if(maxElement.getValue() == null){
				throw new Exception("Script/Maximum value is null");
			}
			if (maxElement.getValue().contains("%")) {
				selectResults = true;
			}
		}

		StringBuffer resultIds = new StringBuffer();
		if (selectResults) {
			for (DBItemSchedulerMonNotifications step : steps) {
				if (step.getStep() >= stepFromIndex	&& step.getStep() <= stepToIndex) {
					LOGGER.debug(String.format("%s: get params for notification = %s",method,step.getId()));
					
					List<DBItemSchedulerMonResults> params = dbLayer.getNotificationResults(step.getId()); //step.getSchedulerMonResults();

					if (params != null) {
						int ri = 0;
						for (DBItemSchedulerMonResults param : params) {
							ri++;
							if(ri > 1){
								resultIds.append(";");
							}
							resultIds.append(param.getId());
							
							LOGGER.debug(String.format("%s:    param = %s, value = %s", 
									method,
									param.getName(),
									param.getValue()));

							if (minElement != null) {
								String min = resolveParam(minElement.getValue(), param.getName(),param.getValue());
								if (min != null) {
									minValue = min;
									LOGGER.debug(String.format("%s:   minValue = %s",
											method,
											minValue));
								}
							}
							if (maxElement != null) {
								String max = resolveParam(maxElement.getValue(), param.getName(),param.getValue());
								if (max != null) {
									maxValue = max;

									LOGGER.debug(String.format("%s:   maxValue = %s",
											method,
											maxValue));
								}
							}
						}
					}
				}
			}
		}

		String minimumLang = null;
		String maximumLang = null;
		if (minElement != null) {
			minimumLang = minElement.getLanguage();
			if (minValue == null) {
				minValue = minElement.getValue();
			}
		}
		if (maxElement != null) {
			maximumLang = maxElement.getLanguage();
			if (maxValue == null) {
				maxValue = maxElement.getValue();
			}
		}
		
		this.createCheck(dbLayer, 
				check, 
				minNotification, 
				stepFromNotification,
				stepToNotification,
				minimumLang, minValue, maximumLang, maxValue,resultIds);
	}

	private void createCheck(DBLayerSchedulerMon dbLayer,
			DBItemSchedulerMonChecks check,
			DBItemSchedulerMonNotifications resultNotification, 
			DBItemSchedulerMonNotifications stepFromNotification, 
			DBItemSchedulerMonNotifications stepToNotification, 
			String minimumLang,
			String minValue, 
			String maximumLang, 
			String maxValue,
			StringBuffer resultIds)
			throws Exception {

		//Indent für die Ausgabe
		String method = "  createCheck";
		
		Date startTime = null;
		Date endTime = null;
	
		Date stepFromStartTime = resultNotification.getOrderStartTime();
		Date stepToEndTime = resultNotification.getOrderEndTime();
		
		EStartTimeType startTimeType = EStartTimeType.ORDER;
		EEndTimeType endTimeType = EEndTimeType.ORDER;
		
		if(stepFromNotification == null){
			startTime = resultNotification.getOrderStartTime();
		}
		else{
			startTimeType  	= EStartTimeType.ORDER_STEP;
			startTime 		= stepFromNotification.getOrderStepStartTime();
			stepFromStartTime = startTime;
			if(startTime == null){
				LOGGER.debug(String.format(
						"%s: do continue. getOrderStepStartTime is NULL (stepFromNotification.id = %s)",
						method,
						stepFromNotification.getId()));
				return;
			}
		}
		if(stepToNotification == null){
			endTime = resultNotification.getOrderEndTime();
		}
		else{
			endTimeType = EEndTimeType.ORDER_STEP;
			endTime = stepToNotification.getOrderStepEndTime();
			stepToEndTime = endTime;
		}
		
		if(endTime == null){
			endTimeType = EEndTimeType.CURRENT;
			endTime =  DBLayer.getCurrentDateTime(); 
		}
		
		Long startTimeSeconds = startTime.getTime() / 1000;
		Long endTimeSeconds = endTime.getTime() / 1000;
		Long diffSeconds = endTimeSeconds - startTimeSeconds;

		LOGGER.debug(String
				.format("%s: id = %s, difference = %ss, startTimeType = %s, endTimeType = %s, startTime = %s, endTime = %s",
						method,
						check.getId(),
						diffSeconds, 
						startTimeType,
						endTimeType,
						DBLayer.getDateAsString(startTime),
						DBLayer.getDateAsString(endTime)
						));

		Double minAsDouble = null;
		Double maxAsDouble = null;

		if (minValue != null && !endTimeType.equals(EEndTimeType.CURRENT)) {
			try {
				minAsDouble = evalScript(minimumLang, minValue);
				LOGGER.debug(String.format(
						"%s: id = %s, minValue = %s",
						method,check.getId(), minAsDouble));
			} catch (Exception ex) {
				LOGGER.warn(String
						.format("%s: id = %s, exception during eval minValue = %s : %s",
								method,check.getId(), minValue,
								ex.getMessage()));
			}
		}
		if (maxValue != null) {
			try {
				maxAsDouble = evalScript(maximumLang, maxValue);
				LOGGER.debug(String.format(
						"%s: id = %s, maxValue = %s",
						method,check.getId(), maxAsDouble));
			} catch (Exception ex) {
				LOGGER.warn(String
						.format("%s: id = %s, exception (during eval maxValue = %s) :  %s",
								method,check.getId(), maxValue,
								ex.getMessage()));
			}
		}

		String checkText = null;
		String checkTextTime = "";
		if (endTimeType == null) {
			LOGGER.info(String.format("%s: endTimeType is NULL",method));
		} else {
			if(startTimeType.equals(EStartTimeType.ORDER)){
				if (endTimeType.equals(EEndTimeType.CURRENT)) {
					checkTextTime = String.format("order started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
							DBLayer.getDateAsString(startTime),		
							DBLayer.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER)) {
					checkTextTime = String.format("order started at %s(UTC) and finished at %s(UTC)",
							DBLayer.getDateAsString(startTime),
							DBLayer.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER_STEP)) {
					checkTextTime = String.format("order started at %s(UTC) and step %s finished at %s(UTC)",
							DBLayer.getDateAsString(startTime),
							stepToNotification.getOrderStepState(),
							DBLayer.getDateAsString(endTime));
				} 
			}
			else if(startTimeType.equals(EStartTimeType.ORDER_STEP)){
				if (endTimeType.equals(EEndTimeType.CURRENT)) {
					checkTextTime = String.format("step %s started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
							stepFromNotification.getOrderStepState(),
							DBLayer.getDateAsString(startTime),		
							DBLayer.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER)) {
					checkTextTime = String.format("step %s started at %s(UTC) and order finished at %s(UTC)",
							stepFromNotification.getOrderStepState(),
							DBLayer.getDateAsString(startTime),
							DBLayer.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER_STEP)) {
					checkTextTime = String.format("step %s started at %s(UTC) and step %s finished at %s(UTC)",
							stepFromNotification.getOrderStepState(),
							DBLayer.getDateAsString(startTime),
							stepToNotification.getOrderStepState(),
							DBLayer.getDateAsString(endTime));
				} 
				
			}
			}

		if (minAsDouble != null && diffSeconds < minAsDouble.doubleValue()) {
			String newVal = formatDoubleValue(minAsDouble);
			checkText = String
					.format("execution time %ss is less than the defined minimum time %ss. %s",
							formatDoubleValue(diffSeconds.doubleValue()),
							newVal, checkTextTime);
		}
		if (maxAsDouble != null && diffSeconds > maxAsDouble.doubleValue()) {
			String newVal = formatDoubleValue(maxAsDouble);
			checkText = String
					.format("execution time %ss is greater than the defined maximum time %ss. %s",
							formatDoubleValue(diffSeconds.doubleValue()),
							newVal, checkTextTime);
		}

		if(checkText == null) {
			if(!endTimeType.equals(EEndTimeType.CURRENT)){
				dbLayer.getConnection().beginTransaction();
				dbLayer.removeCheck(check.getId());
				dbLayer.getConnection().commit();
				
				LOGGER.debug(String.format("%s: remove check (id = %s executed and found no problems). check startTimeType = %s endTimeType = %s",method,check.getId(),startTimeType,endTimeType));
				counter.addRemove();
			}
		}
		else{
			try {
				dbLayer.getConnection().beginTransaction();
				
				if (endTimeType.equals(EEndTimeType.CURRENT) && check.getCheckText() == null) {
					checkText = String.format("not set as checked. do one rerun. %s", checkText);

					LOGGER.debug(String
							.format("%s: id = %s, set checkState: text = %s, result ids = %s",
									method,check.getId(), checkText, resultIds));

					dbLayer.setNotificationCheckForRerun(check,
							stepFromStartTime,
							stepToEndTime,
							checkText, resultIds.toString());

					counter.addRerun();
				} else {
					LOGGER.debug(String
							.format("%s: id = %s, text = %s, result ids = %s",
									method,check.getId(), checkText, resultIds));

					dbLayer.setNotificationCheck(check,
							stepFromStartTime,
							stepToEndTime,
							checkText, 
							resultIds.toString());

					counter.addTotal();
				}
				dbLayer.getConnection().commit();
			} catch (Exception ex) {
				try {
					dbLayer.getConnection().rollback();
				} catch (Exception e) {
				}
				LOGGER.warn(ex.getMessage());
			}
		}
	}

	public String formatDoubleValue(Double d) {
		String s = String.format("%.2f", d);
		if (d < 0.01) { // weitere Prüfung damit keine 0.00 in der Ausgabe steht
			if (d > 0.0001) {
				s = String.format("%.4f", d);
			} else if (d > 0.000001) {
				s = String.format("%.8f", d);
			}
		}
		return s;
	}

	public Double evalScript(String lang, String text) throws Exception {
		String method = "  evalScript";
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(lang);
		if (engine == null) {
			try {
				logAvailableEngines();
			} catch (Exception ex) {
			}
			throw new Exception(String.format(
					"ScriptEngine \"%s\" is not available", lang));
		}
		// text = text.replaceAll("\", replacement)
		LOGGER.debug(String.format("%s: lang = %s, text =  %s",
				method, lang, text));
		
		return ((Number)engine.eval(text)).doubleValue();
	}

	private String resolveParam(String text, String param, String value)
			throws Exception {
		if (text == null || param == null || value == null) {
			return null;
		}

		//quote for values with paths
		value = value.replaceAll("\\\\", "\\\\\\\\");
		value = Matcher.quoteReplacement(value);

		String result = text.replaceAll("%(?i)" + param + "%", value);
		return result.indexOf("%") > -1 ? null : result;
	}

	public void logAvailableEngines() {
		ScriptEngineManager manager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = manager.getEngineFactories();

		if (factories == null) {
			LOGGER.info("No available script engines were found. List of ScriptEngineFactories is null");
		} else {
			LOGGER.info("Available script engines:");
			for (int i = 0; i < factories.size(); i++) {
				ScriptEngineFactory factory = factories.get(i);
				String en = factory.getEngineName();
				String language = factory.getLanguageName();

				LOGGER.info(String.format("- language = %s, engine name = %s",
						language, en));
			}
		}

	}

}
