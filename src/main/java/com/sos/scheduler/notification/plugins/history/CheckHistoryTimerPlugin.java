package com.sos.scheduler.notification.plugins.history;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.slf4j.LoggerFactory;

import com.sos.scheduler.notification.db.DBItemSchedulerMonChecks;
import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;
import com.sos.scheduler.notification.db.DBItemSchedulerMonResults;
import com.sos.scheduler.notification.db.DBLayerSchedulerMon;
import com.sos.scheduler.notification.helper.EStartTimeType;
import com.sos.scheduler.notification.helper.ElementTimer;
import com.sos.scheduler.notification.helper.ElementTimerScript;
import com.sos.scheduler.notification.helper.EEndTimeType;
import com.sos.scheduler.notification.jobs.history.CheckHistoryJobOptions;

/**
 * 
 * @author Robert Ehrlich
 * 
 */
public class CheckHistoryTimerPlugin implements ICheckHistoryPlugin {

	final org.slf4j.Logger logger = LoggerFactory
			.getLogger(CheckHistoryTimerPlugin.class);

	private int countChecks = 0;
	private int countChecksForRerun = 0;
	private int countChecksRemoved = 0;
	private int countChecksSkipped = 0;
	
	/**
	 * 
	 */
	@Override
	public void onInit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer)
			throws Exception {

	}

	/**
	 * 
	 */
	@Override
	public void onExit(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer)
			throws Exception {
	}

	/**
	 * 
	 */
	@Override
	public void onProcess(LinkedHashMap<String, ElementTimer> timers,
			CheckHistoryJobOptions options, DBLayerSchedulerMon dbLayer,
			Date dateFrom, Date dateTo) throws Exception {
		String functionName = "onProcess";
		
		if (dbLayer == null) {
			throw new Exception("dbLayer is NULL");
		}
		if (timers == null) {
			throw new Exception("timers is NULL");
		}

		if (timers.size() == 0) {
			logger.info(String.format("%s: skip. found 0 timers definitions",functionName));
			return;
		}

		this.initCountChecks();
		
		List<DBItemSchedulerMonChecks> result = dbLayer.getSchedulerMonChecksForSetTimer();
		logger.info(String.format("%s: found %s timer definitions and %s timers for check in the db",
				functionName,
				timers.size(),
				result.size()));
		
		for (int i = 0; i < result.size(); i++) {
			DBItemSchedulerMonChecks check = result.get(i);
			if (!timers.containsKey(check.getName())) {
				this.countChecksSkipped++;
				logger.debug(String.format("%s: skip check for %s. timer definition is not found.",
						functionName,
						check.getName()));
				continue;
			}
			this.analyzeCheck(dbLayer, check, timers.get(check.getName()));
		}
		
		logger.info(String.format("%s: checks created = %s, removed = %s, skipped = %s, checks for rerun = %s",
				functionName,
				this.countChecks,
				this.countChecksRemoved,
				this.countChecksSkipped,
				this.countChecksForRerun));
	}

	/**
	 * 
	 */
	private void initCountChecks(){
		this.countChecks = 0;
		this.countChecksForRerun = 0;
		this.countChecksRemoved = 0;
		this.countChecksSkipped = 0;
	}
	
	/**
	 * 
	 * @param dbLayer
	 * @param check
	 * @param timer
	 * @throws Exception
	 */
	private void analyzeCheck(DBLayerSchedulerMon dbLayer,
			DBItemSchedulerMonChecks check, ElementTimer timer) throws Exception {
		String functionName = "analyzeCheck"; 
		
		logger.debug(String.format("%s: id = %s, name = %s, schedulerId = %s, jobChain = %s, stepFrom = %s, stepTo = %s, notificationId = %s",
				functionName,
				check.getId(),
				check.getName(),
				check.getSchedulerId(),
				check.getJobChain(),
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
			logger.debug(String.format("do continue. getOrderStartTime is NULL (minNotification.id = %s",
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
					List<DBItemSchedulerMonResults> params = step.getSchedulerMonResults();

					if (params != null) {
						int ri = 0;
						for (DBItemSchedulerMonResults param : params) {
							ri++;
							if(ri > 1){
								resultIds.append(";");
							}
							resultIds.append(param.getId());
							
							logger.debug(String.format("%s:    param = %s, value = %s", 
									functionName,
									param.getName(),
									param.getValue()));

							if (minElement != null) {
								String min = resolveParam(minElement.getValue(), param.getName(),param.getValue());
								if (min != null) {
									minValue = min;
									logger.debug(String.format("%s:   minValue = %s",
											functionName,
											minValue));
								}
							}
							if (maxElement != null) {
								String max = resolveParam(maxElement.getValue(), param.getName(),param.getValue());
								if (max != null) {
									maxValue = max;

									logger.debug(String.format("%s:   maxValue = %s",
											functionName,
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

	/**
	 * 
	 * @param dbLayer
	 * @param check
	 * @param resultNotification
	 * @param stepFromNotification
	 * @param stepToNotification
	 * @param minimumLang
	 * @param minValue
	 * @param maximumLang
	 * @param maxValue
	 * @param resultIds
	 * @throws Exception
	 */
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
		String functionName = "  createCheck";
		
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
				logger.debug(String.format(
						"%s: do continue. getOrderStepStartTime is NULL (stepFromNotification.id = %s)",
						functionName,
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
			endTime =  DBLayerSchedulerMon.getCurrentDateTime(); 
		}
		
		Long startTimeSeconds = startTime.getTime() / 1000;
		Long endTimeSeconds = endTime.getTime() / 1000;
		Long diffSeconds = endTimeSeconds - startTimeSeconds;

		logger.debug(String
				.format("%s: id = %s, difference = %ss, startTimeType = %s, endTimeType = %s, startTime = %s, endTime = %s",
						functionName,
						check.getId(),
						diffSeconds, 
						startTimeType,
						endTimeType,
						DBLayerSchedulerMon.getDateAsString(startTime),
						DBLayerSchedulerMon.getDateAsString(endTime)
						));

		Double minAsDouble = null;
		Double maxAsDouble = null;

		if (minValue != null && !endTimeType.equals(EEndTimeType.CURRENT)) {
			try {
				minAsDouble = evalScript(minimumLang, minValue);
				logger.debug(String.format(
						"%s: id = %s, minValue = %s",
						functionName,check.getId(), minAsDouble));
			} catch (Exception ex) {
				logger.warn(String
						.format("%s: id = %s, exception during eval minValue = %s : %s",
								functionName,check.getId(), minValue,
								ex.getMessage()));
			}
		}
		if (maxValue != null) {
			try {
				maxAsDouble = evalScript(maximumLang, maxValue);
				logger.debug(String.format(
						"%s: id = %s, maxValue = %s",
						functionName,check.getId(), maxAsDouble));
			} catch (Exception ex) {
				logger.warn(String
						.format("%s: id = %s, exception (during eval manValue = %s) :  %s",
								functionName,check.getId(), maxValue,
								ex.getMessage()));
			}
		}

		String checkText = null;
		String checkTextTime = "";
		if (endTimeType == null) {
			logger.info(String.format("%s: endTimeType is NULL",functionName));
		} else {
			if(startTimeType.equals(EStartTimeType.ORDER)){
				if (endTimeType.equals(EEndTimeType.CURRENT)) {
					checkTextTime = String.format("order started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
							DBLayerSchedulerMon.getDateAsString(startTime),		
							DBLayerSchedulerMon.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER)) {
					checkTextTime = String.format("order started at %s(UTC) and finished at %s(UTC)",
							DBLayerSchedulerMon.getDateAsString(startTime),
							DBLayerSchedulerMon.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER_STEP)) {
					checkTextTime = String.format("order started at %s(UTC) and step %s finished at %s(UTC)",
							DBLayerSchedulerMon.getDateAsString(startTime),
							stepToNotification.getOrderStepState(),
							DBLayerSchedulerMon.getDateAsString(endTime));
				} 
			}
			else if(startTimeType.equals(EStartTimeType.ORDER_STEP)){
				if (endTimeType.equals(EEndTimeType.CURRENT)) {
					checkTextTime = String.format("step %s started at %s(UTC) and is not yet finished... checked vs. current datetime %s(UTC).",
							stepFromNotification.getOrderStepState(),
							DBLayerSchedulerMon.getDateAsString(startTime),		
							DBLayerSchedulerMon.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER)) {
					checkTextTime = String.format("step %s started at %s(UTC) and order finished at %s(UTC)",
							stepFromNotification.getOrderStepState(),
							DBLayerSchedulerMon.getDateAsString(startTime),
							DBLayerSchedulerMon.getDateAsString(endTime));
				} else if (endTimeType.equals(EEndTimeType.ORDER_STEP)) {
					checkTextTime = String.format("step %s started at %s(UTC) and step %s finished at %s(UTC)",
							stepFromNotification.getOrderStepState(),
							DBLayerSchedulerMon.getDateAsString(startTime),
							stepToNotification.getOrderStepState(),
							DBLayerSchedulerMon.getDateAsString(endTime));
				} 
				
			}
			}

		if (minAsDouble != null && diffSeconds < minAsDouble.doubleValue()) {
			// double newVal =
			// Math.round(minAsDouble.doubleValue()*100.0)/100.0;
			String newVal = formatDoubleValue(minAsDouble);
			checkText = String
					.format("execution time %ss is less than the defined minimum time %ss. %s",
							formatDoubleValue(diffSeconds.doubleValue()),
							newVal, checkTextTime);
		}
		if (maxAsDouble != null && diffSeconds > maxAsDouble.doubleValue()) {
			// double newVal =
			// Math.round(maxAsDouble.doubleValue()*100.0)/100.0;
			String newVal = formatDoubleValue(maxAsDouble);
			checkText = String
					.format("execution time %ss is greater than the defined maximum time %ss. %s",
							formatDoubleValue(diffSeconds.doubleValue()),
							newVal, checkTextTime);
		}

		if(checkText == null) {
			if(!endTimeType.equals(EEndTimeType.CURRENT)){
				dbLayer.beginTransaction();
				dbLayer.removeCheck(check.getId());
				dbLayer.commit();
				
				logger.debug(String.format("%s: remove check (id = %s executed and found no problems). check startTimeType = %s endTimeType = %s",functionName,check.getId(),startTimeType,endTimeType));
				this.countChecksRemoved++;
			}
		}
		else{
			try {
				dbLayer.beginTransaction();
				
				if (endTimeType.equals(EEndTimeType.CURRENT) && check.getCheckText() == null) {
					checkText = String.format("not set as checked. do one rerun. %s", checkText);

					logger.debug(String
							.format("%s: id = %s, set checkState: text = %s, result ids = %s",
									functionName,check.getId(), checkText, resultIds));

					dbLayer.setNotificationCheckForRerun(check,
							stepFromStartTime,
							stepToEndTime,
							checkText, resultIds.toString());

					this.countChecksForRerun++;
				} else {
					logger.debug(String
							.format("%s: id = %s, text = %s, result ids = %s",
									functionName,check.getId(), checkText, resultIds));

					dbLayer.setNotificationCheck(check,
							stepFromStartTime,
							stepToEndTime,
							checkText, 
							resultIds.toString());

					this.countChecks++;
				}
				dbLayer.commit();
			} catch (Exception ex) {
				try {
					dbLayer.rollback();
				} catch (Exception e) {
				}
				logger.warn(ex.getMessage());
			}
		}
	}

	/**
	 * Formatierung
	 * 
	 * @param d
	 * @return
	 */
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

	/**
	 * 
	 * @param lang
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public Double evalScript(String lang, String text) throws Exception {
		String functionName = "  evalScript";
		
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
		logger.debug(String.format("%s: lang = %s, text =  %s",
				functionName, lang, text));
		
		return (Double) engine.eval(text);
	}

	/**
	 * 
	 * @param text
	 * @param param
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String resolveParam(String text, String param, String value)
			throws Exception {
		if (text == null || param == null || value == null) {
			return null;
		}

		// sonst gehen die Pfade verloren : zb.: d:\abc
		value = value.replaceAll("\\\\", "\\\\\\\\");
		value = Matcher.quoteReplacement(value);

		String result = text.replaceAll("%(?i)" + param + "%", value);
		return result.indexOf("%") > -1 ? null : result;
	}

	/**
	 * 
	 */
	public void logAvailableEngines() {
		ScriptEngineManager manager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = manager.getEngineFactories();

		if (factories == null) {
			logger.info("No available script engines were found. List of ScriptEngineFactories is null");
		} else {
			logger.info("Available script engines:");
			for (int i = 0; i < factories.size(); i++) {
				ScriptEngineFactory factory = factories.get(i);
				String en = factory.getEngineName();
				String language = factory.getLanguageName();

				logger.info(String.format("- language = %s, engine name = %s",
						language, en));
			}
		}

	}

}
