package com.sos.scheduler.notification.db;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class DBLayerSchedulerMon extends SOSHibernateDBLayer {

	final Logger logger = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
	
	public final static String SCHEDULER_VARIABLES_NOTIFICATION = "notification_date";
	public final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** Table SCHEDULER_VARIABLES */
	public final static String DBITEM_SCHEDULER_VARIABLES = DBItemSchedulerVariables.class.getSimpleName();
	public final static String TABLE_SCHEDULER_VARIABLES = "SCHEDULER_VARIABLES";
	
	/** Table SCHEDULER_HISTORY */	
	public final static String DBITEM_SCHEDULER_HISTORY = DBItemSchedulerHistory.class.getSimpleName();
	public final static String TABLE_SCHEDULER_HISTORY = "SCHEDULER_HISTORY";
	
	/** Table SCHEDULER_ORDER_HISTORY */
	public final static String DBITEM_SCHEDULER_ORDER_HISTORY = DBItemSchedulerOrderHistory.class.getSimpleName();
	public final static String TABLE_SCHEDULER_ORDER_HISTORY = "SCHEDULER_ORDER_HISTORY";
	
	/** Table SCHEDULER_ORDER_STEP_HISTORY */
	public final static String DBITEM_SCHEDULER_ORDER_STEP_HISTORY = DBItemSchedulerOrderStepHistory.class.getSimpleName();
	public final static String TABLE_SCHEDULER_ORDER_STEP_HISTORY = "SCHEDULER_ORDER_STEP_HISTORY";
		
	/** Table SCHEDULER_MON_NOTIFICATIONS */
	public final static String DBITEM_SCHEDULER_MON_NOTIFICATIONS = DBItemSchedulerMonNotifications.class.getSimpleName();
	public final static String TABLE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOTIFICATIONS";
	public final static String SEQUENCE_SCHEDULER_MON_NOTIFICATIONS = "SCHEDULER_MON_NOT_ID_SEQ";
	
	/** Table SCHEDULER_MON_RESULTS */
	public final static String DBITEM_SCHEDULER_MON_RESULTS = DBItemSchedulerMonResults.class.getSimpleName();
	public final static String TABLE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RESULTS";
	public final static String SEQUENCE_SCHEDULER_MON_RESULTS = "SCHEDULER_MON_RES_ID_SEQ";
	
	/** Table SCHEDULER_MON_SYSNOTIFICATIONS */
	public final static String DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS = DBItemSchedulerMonSystemNotifications.class.getSimpleName();
	public final static String TABLE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOTIFICATIONS";
	public final static String SEQUENCE_SCHEDULER_MON_SYSNOTIFICATIONS = "SCHEDULER_MON_SYSNOT_ID_SEQ";
	
	/** Table SCHEDULER_MON_CHECKS */
	public final static String DBITEM_SCHEDULER_MON_CHECKS = DBItemSchedulerMonChecks.class.getSimpleName();
	public final static String TABLE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS";
	public final static String SEQUENCE_SCHEDULER_MON_CHECKS = "SCHEDULER_MON_CHECKS_ID_SEQ";
	
	/** in seconds */
	public final static int RERUN_TRANSACTION_INTERVAL = 2;
	
	public final static String DEFAULT_EMPTY_NAME = "*";
	
	private String dialectClassName = null;
	/**
	 * 
	 * @param configurationFile
	 */
	public DBLayerSchedulerMon(File configurationFile) {
		super();
		this.setConfigurationFile(configurationFile);
		this.setDialictClassName();
	}

	/**
	 * 
	 * @param session
	 */
	public DBLayerSchedulerMon(Session session) {
		this.setSession(session);
		this.setDialictClassName();
	}
	
	
	private void setDialictClassName(){
		SessionFactory sf = this.getSession().getSessionFactory();
		Dialect dialect = ((SessionFactoryImplementor)sf).getDialect();
		this.dialectClassName = dialect.getClass().getSimpleName().toLowerCase();
	}
	
	/**
	 * 
	 * @param schedulerId
	 * @param taskId
	 * @param state
	 * @param jobChain
	 * @param orderId
	 * @return
	 * @throws Exception
	 */
	public DBItemSchedulerOrderHistory getNotFinishedOrderStepHistory(
			String schedulerId, 
			Long taskId, 
			String state, 
			String jobChain,
			String orderId) throws Exception {

		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_ORDER_HISTORY + " oh ")
				.append("left join fetch oh.schedulerOrderStepHistory osh ")
				.append("left join fetch osh.schedulerHistoryDBItem h ")
				.append("where oh.spoolerId = :schedulerId ")
				.append("and osh.taskId = :taskId ")
				.append("and osh.state = :state ")
				.append("and oh.jobChain = :jobChain ")
				.append("and oh.orderId = :orderId ")
				.append("and osh.endTime is null");

		Query query = session.createQuery(sql.toString());
		query.setReadOnly(true);
		
		query.setParameter("schedulerId", schedulerId);
		query.setParameter("taskId", taskId);
		query.setParameter("state", state);
		query.setParameter("jobChain", jobChain);
		query.setParameter("orderId", orderId);

		@SuppressWarnings("unchecked")
		List<DBItemSchedulerOrderHistory> result = executeQueryList(query);
		if (result.size() > 0) {
			return result.get(0);
		}

		return null;
	}
	
	/**
	 * 
	 * @param date
	 */
	public void cleanupNotifications(Date date){
		StringBuffer sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
		.append("where created <= :date");
		int count = session.createQuery(sql.toString()).setTimestamp("date",date).executeUpdate();
		logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_NOTIFICATIONS,count));
		
		sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_RESULTS+" ")
		.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
		count = session.createQuery(sql.toString()).executeUpdate();
		logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_RESULTS,count));
	
		sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
		.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
		count = session.createQuery(sql.toString()).executeUpdate();
		logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_CHECKS,count));
		
		sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS+" ")
		.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
		count = session.createQuery(sql.toString()).executeUpdate();
		logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS,count));
		
		sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS+" ")
		.append("where checkId > 0 and checkId not in (select id from "+DBITEM_SCHEDULER_MON_CHECKS+") ");
		count = session.createQuery(sql.toString()).executeUpdate();
		logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS,count));
	}
			
	/**
	 * 
	 * @param systemId
	 * @param serviceName
	 * @return
	 */
	public int resetAcknowledged(String systemId,String serviceName){
		
		StringBuffer sql = new StringBuffer("update "
				+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
				.append("set acknowledged = 1 ")
				.append("where lower(systemId) = :systemId ");
		if(!SOSString.isEmpty(serviceName)){
			sql.append(" and serviceName =:serviceName");
		}
		
		Query query = session.createQuery(sql.toString());
		query.setParameter("systemId", systemId.toLowerCase());

		if(!SOSString.isEmpty(serviceName)){
			query.setParameter("serviceName",serviceName);
		}
		return query.executeUpdate();
	}
	
	/**
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	public ScrollableResults getOrderStepsScrollable(Date dateFrom, Date dateTo)
			throws Exception {

		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_ORDER_STEP_HISTORY + " sh ")
				.append("left join fetch sh.schedulerOrderHistoryDBItem oh ")
				.append("left join fetch sh.schedulerHistoryDBItem h ")
				.append("where oh.startTime <= :startTimeTo ");
		if (dateFrom != null) {
			sql.append("and oh.startTime >= :startTimeFrom ");
		}

		Query query = session.createQuery(sql.toString());
		query.setReadOnly(true);
		query.setFetchSize(1000).setCacheable(false);
		query.setCacheMode(CacheMode.IGNORE);
		// query.setFlushMode(FlushMode.MANUAL);

		if (dateFrom != null) {
			query.setTimestamp("startTimeFrom", dateFrom);
		}
		if (dateTo != null) {
			query.setTimestamp("startTimeTo", dateTo);
		}

		return query.scroll(ScrollMode.FORWARD_ONLY);
	}

	/**
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerOrderStepHistory> getOrderStepsAsList(
			Date dateFrom, Date dateTo) throws Exception {

		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_ORDER_STEP_HISTORY + " sh ")
				.append("left join fetch sh.schedulerOrderHistoryDBItem oh ")
				.append("left join fetch sh.schedulerHistoryDBItem h ")
				.append("where oh.startTime <= :startTimeTo ");
		if (dateFrom != null) {
			sql.append("and oh.startTime >= :startTimeFrom ");
		}

		Query query = session.createQuery(sql.toString());
		query.setReadOnly(true);

		if (dateFrom != null) {
			query.setTimestamp("startTimeFrom", dateFrom);
		}
		if (dateTo != null) {
			query.setTimestamp("startTimeTo", dateTo);
		}

		return executeQueryList(query);
	}
	
	
	/**
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
		@SuppressWarnings("unchecked")
		public List<DBItemSchedulerOrderHistory> getOrdersAsListX(
			Date dateFrom, 
			Date dateTo)
			throws Exception {

		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_ORDER_HISTORY + " t ")
				.append("left join fetch t.schedulerOrderStepHistory sh ")
				.append("left join fetch sh.schedulerHistoryDBItem h ")
				.append("where t.startTime <= :startTimeTo ");
				if(dateFrom != null){
					sql.append("and t.startTime >= :startTimeFrom ");
				}

		
		Query query = session.createQuery(sql.toString());
		query.setReadOnly(true);
			
		if (dateFrom != null) {
			query.setTimestamp("startTimeFrom", dateFrom);
		}
		if (dateTo != null) {
			query.setTimestamp("startTimeTo", dateTo);
		}

		return executeQueryList(query);
	}

	

	/**
	 * 
	 * @param notificationId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationOrderSteps(Long notificationId) throws Exception{
			
		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n1 ")
		.append("where exists (")
		.append("   select n2.schedulerId,n2.orderHistoryId ") 
		.append("   from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ")
		.append("   where n1.orderHistoryId = n2.orderHistoryId ")
		.append("   and n2.id = :id ")
		.append(")")
		.append("order by n1.step");
		
		Query q = session.createQuery(sql.toString());
		q.setParameter("id",notificationId);
		
		return executeQueryList(q);
	}
	
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonChecks> getSchedulerMonChecksForSetTimer() throws Exception{
		
		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
		.append("where checked = 0");
		
		Query q = session.createQuery(sql.toString());
		q.setReadOnly(true);
		
		return executeQueryList(q);
	}
	
	
	/**
	 * 
	 * @param check
	 * @param stepFromStartTime
	 * @param stepToEndTime
	 * @param text
	 * @param resultIds
	 * @throws Exception
	 */
	public void setNotificationCheck(
			DBItemSchedulerMonChecks check,
			Date stepFromStartTime,
			Date stepToEndTime,
			String text,
			String resultIds) throws Exception{
		
		check.setStepFromStartTime(stepFromStartTime);
		check.setStepToEndTime(stepToEndTime);
		check.setChecked(true);
		check.setCheckText(text);
		check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
		
		check.setModified(DBLayerSchedulerMon.getCurrentDateTime());
		this.update(check);
	}
	
	/**
	 * 
	 * @param check
	 * @param stepFromStartTime
	 * @param stepToEndTime
	 * @param text
	 * @param resultIds
	 * @throws Exception
	 */
	public void setNotificationCheckForRerun(DBItemSchedulerMonChecks check,Date stepFromStartTime,
			Date stepToEndTime,String text,String resultIds) throws Exception{
		check.setStepFromStartTime(stepFromStartTime);
		check.setStepToEndTime(stepToEndTime);
		check.setChecked(false);
		check.setCheckText("1");
		check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
		check.setModified(DBLayerSchedulerMon.getCurrentDateTime());
		this.update(check);
	}
	/**
	 * 
	 * @param readCount
	 * @throws Exception
	 */
	public void flushScrollableResults(int readCount) throws Exception{
		//Moreover if session cache is enabled, 
		//you need to add explicit code to clear the session cache, 
		//such as a code snippet here to clear cache every 100 rows:
			
		if ( readCount % 100 == 0) { 
			session.flush(); 
			session.clear();
		}
	}
	
	
	private String quoteName(String s){
		return "\""+s+"\"";
	}
	
	/**
	 * 
	 * @param isDbDependent
	 * @return
	 * @throws Exception
	 */
	public int updateNotifications(boolean isDbDependent) throws Exception {
		
		boolean executed = false;
		int result = 0;
		
		if(isDbDependent){
			//Table t_mn = DBItemSchedulerMonNotifications.class.getAnnotation(Table.class);
			
			//contains statt instanceof weil in den Namen auch die Versionen vorkommen können
			if(this.dialectClassName.contains("sqlserver")){
				// MSSQL
				executed = true;
				StringBuffer sb = new StringBuffer("update mn ")
						.append("set mn.ORDER_END_TIME = oh.END_TIME ")
						.append(",mn.TASK_END_TIME = h.END_TIME ")
						.append(",mn.ORDER_STEP_END_TIME = osh.END_TIME ")
						.append(",mn.ERROR = osh.ERROR ")
						.append(",mn.ERROR_CODE = osh.ERROR_CODE ")
						.append(",mn.ERROR_TEXT = osh.ERROR_TEXT ")
						.append("from " + TABLE_SCHEDULER_MON_NOTIFICATIONS + " mn ")
						.append("inner join " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
						.append("on mn.ORDER_HISTORY_ID = oh.HISTORY_ID ")
						.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
						.append("on osh.HISTORY_ID = oh.HISTORY_ID ")
						.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
						.append("on h.ID = osh.TASK_ID ")
						//.append("where mn.SCHEDULER_ID = oh.SPOOLER_ID ")
						//.append("and mn.ORDER_END_TIME is null ")
						.append("where mn.ORDER_END_TIME is null ")
						.append("and mn.STEP = osh.STEP");
				result = session.createSQLQuery(sb.toString()).executeUpdate();
			}
			else if (this.dialectClassName.contains("oracle") 
					|| this.dialectClassName.contains("db2")) {
				// ORACLE & DB2								
				executed = true;
				StringBuffer sb = new StringBuffer("update "+TABLE_SCHEDULER_MON_NOTIFICATIONS+" mn ")
				.append("set (")
				.append("ORDER_END_TIME")
				.append(",TASK_END_TIME")
				.append(",ORDER_STEP_END_TIME")
				.append(",ERROR")
				.append(",ERROR_CODE")
				.append(",ERROR_TEXT")
				.append(") = (")
				.append("select ")
				.append("oh.END_TIME")
				.append(",h.END_TIME")
				.append(",osh.END_TIME")
				.append(",osh.ERROR")
				.append(",osh.ERROR_CODE")
				.append(",osh.ERROR_TEXT ")
				.append("from " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
				.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
				.append("on osh.HISTORY_ID = oh.HISTORY_ID ")
				.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
				.append("on h.ID = osh.TASK_ID ")
				.append("where oh.HISTORY_ID = mn.ORDER_HISTORY_ID ")
				.append("and osh.STEP = mn.STEP ")
				.append(") ")
				.append("where mn.ORDER_END_TIME is null");
				 result = session.createSQLQuery(sb.toString()).executeUpdate();
			}
			else if(this.dialectClassName.contains("mysql")){
				//MYSQL
				executed = true;
				StringBuffer sb = new StringBuffer("update " + TABLE_SCHEDULER_MON_NOTIFICATIONS
						+ " mn ").append("inner join " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
						.append("on mn.ORDER_HISTORY_ID = oh.HISTORY_ID ")
						.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
						.append("on osh.HISTORY_ID = oh.HISTORY_ID ")
						.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
						.append("on h.ID = osh.TASK_ID ")
						.append("set mn.ORDER_END_TIME = oh.END_TIME ")
						.append(",mn.TASK_END_TIME = h.END_TIME ")
						.append(",mn.ORDER_STEP_END_TIME = osh.END_TIME ")
						.append(",mn.ERROR = osh.ERROR ")
						.append(",mn.ERROR_CODE = osh.ERROR_CODE ")
						.append(",mn.ERROR_TEXT = osh.ERROR_TEXT ")
						.append("where mn.ORDER_END_TIME is null ")
						.append("and mn.STEP = osh.STEP");

				result = session.createSQLQuery(sb.toString()).executeUpdate();
			}
			else if(this.dialectClassName.contains("postgre")
				|| this.dialectClassName.contains("sybase")){
				//POSTGRESQL & SYBASE
				executed = true;
				StringBuffer sb = new StringBuffer("update "+TABLE_SCHEDULER_MON_NOTIFICATIONS+" ")
				.append("set "+quoteName("ORDER_END_TIME")+" = oh."+quoteName("END_TIME")+" ")
				.append(","+quoteName("TASK_END_TIME")+" = h."+quoteName("END_TIME")+" ")
				.append(","+quoteName("ORDER_STEP_END_TIME")+" = osh."+quoteName("END_TIME")+" ")
				.append(","+quoteName("ERROR")+" = osh."+quoteName("ERROR")+" ")
				.append(","+quoteName("ERROR_CODE")+" = osh."+quoteName("ERROR_CODE")+" ")
				.append(","+quoteName("ERROR_TEXT")+" = osh."+quoteName("ERROR_TEXT")+" ")
				.append("from " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
				.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
				.append("on osh."+quoteName("HISTORY_ID")+" = oh."+quoteName("HISTORY_ID")+" ")
				.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
				.append("on h."+quoteName("ID")+" = osh."+quoteName("TASK_ID")+" ")
				.append("where "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quoteName("ORDER_HISTORY_ID")+" = oh."+quoteName("HISTORY_ID")+" ")
				.append("and "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quoteName("ORDER_END_TIME")+" is null ")
				.append("and "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quoteName("STEP")+" = osh."+quoteName("STEP"));
				
				result = session.createSQLQuery(sb.toString()).executeUpdate();
			}
			else if(this.dialectClassName.contains("XXsybase")){
				//POSTGRESQL & SYBASE
				executed = true;
				StringBuffer sb = new StringBuffer("update "+TABLE_SCHEDULER_MON_NOTIFICATIONS+" ")
				.append("set "+quoteName("ORDER_END_TIME")+" = oh."+quoteName("END_TIME")+" ")
				.append(","+quoteName("TASK_END_TIME")+" = h."+quoteName("END_TIME")+" ")
				.append(","+quoteName("ORDER_STEP_END_TIME")+" = osh."+quoteName("END_TIME")+" ")
				.append(","+quoteName("ERROR")+" = osh."+quoteName("ERROR")+" ")
				.append(","+quoteName("ERROR_CODE")+" = osh."+quoteName("ERROR_CODE")+" ")
				.append(","+quoteName("ERROR_TEXT")+" = osh."+quoteName("ERROR_TEXT")+" ")
				.append("from " + TABLE_SCHEDULER_MON_NOTIFICATIONS + " mn ")
				.append("inner join " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
				.append("on mn."+quoteName("ORDER_HISTORY_ID")+" = oh."+quoteName("HISTORY_ID")+" ")
				.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
				.append("on osh."+quoteName("HISTORY_ID")+" = oh."+quoteName("HISTORY_ID")+" ")
				.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
				.append("on h."+quoteName("ID")+" = osh."+quoteName("TASK_ID")+" ")
				//.append("where mn."+quoteName("SCHEDULER_ID")+" = oh."+quoteName("SPOOLER_ID")+" ")
				//.append("and mn."+quoteName("ORDER_END_TIME")+" is null ")
				.append("where mn."+quoteName("ORDER_END_TIME")+" is null ")
				.append("and mn."+quoteName("STEP")+" = osh."+quoteName("STEP"));
				
				result = session.createSQLQuery(sb.toString()).executeUpdate();
			}
			//Firebird kann nicht bzw. sehr umständlich in einem Update Tabelle mit join aktualisieren - kein extra Update
					
		}
		
		//Datenbankunabhängig
		if(!executed){
			StringBuffer sqlSelectNotifications = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
			.append("where orderEndTime is null ");
			
			Query querySelectNotifications = session.createQuery(sqlSelectNotifications.toString());
			querySelectNotifications.setReadOnly(true);
			querySelectNotifications.setFetchSize(1000);
			querySelectNotifications.setCacheMode(CacheMode.IGNORE);
			
			
			StringBuffer sqlSelectHistory = new StringBuffer("from "+DBITEM_SCHEDULER_ORDER_STEP_HISTORY+" osh ")
			.append("inner join fetch osh.schedulerOrderHistoryDBItem oh ")
			.append("inner join fetch osh.schedulerHistoryDBItem h ")
			.append("where osh.id.historyId =:historyId ")
			.append("and osh.id.step = :step ");
			
			
			StringBuffer sqlUpdateNotification = new StringBuffer("update "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
			.append("set taskEndTime = :taskEndTime ")
			.append(",orderEndTime = :orderEndTime ")
			.append(",orderStepEndTime = :orderStepEndTime ")
			.append(",error = :error ")
			.append(",errorCode = :errorCode ")
			.append(",errorText = :errorText ")
			.append(",modified = :modified ")
			.append("where id = :id");
						
			ScrollableResults sr = null;
			try{
				sr = querySelectNotifications.scroll(ScrollMode.FORWARD_ONLY);
				int readCount = 0;
				while (sr.next() )
				{
					readCount++;
					flushScrollableResults(readCount);
				
					DBItemSchedulerMonNotifications dbItem = (DBItemSchedulerMonNotifications)sr.get(0);
					
					Query querySelect = session.createQuery(sqlSelectHistory.toString());
					querySelect.setReadOnly(true);
					querySelect.setParameter("historyId",dbItem.getOrderHistoryId());
					querySelect.setParameter("step",dbItem.getStep());

					@SuppressWarnings("unchecked")
					List<DBItemSchedulerOrderStepHistory> resultOsh = querySelect.list();
					if (resultOsh.size() > 0) {
						DBItemSchedulerOrderStepHistory osh =  resultOsh.get(0);
						DBItemSchedulerHistory h = osh.getSchedulerHistoryDBItem();
						DBItemSchedulerOrderHistory oh = osh.getSchedulerOrderHistoryDBItem();
						
						//wegen flushScrollableResults lieber update als this.save(dbItem)
						Query queryUpdate = session.createQuery(sqlUpdateNotification.toString());
						queryUpdate.setParameter("id",dbItem.getId());
						queryUpdate.setParameter("taskEndTime",(h == null) ? null : h.getEndTime());
						queryUpdate.setParameter("orderEndTime",(oh == null) ? null : oh.getEndTime());
						queryUpdate.setParameter("orderStepEndTime",osh.getEndTime());
						queryUpdate.setParameter("error",osh.getError());
						queryUpdate.setParameter("errorCode",osh.getErrorCode());
						queryUpdate.setParameter("errorText",osh.getErrorText());
						queryUpdate.setParameter("modified",DBLayerSchedulerMon.getCurrentDateTime());
						
						result = queryUpdate.executeUpdate();
						
						/**
						if(h != null){
							dbItem.setTaskEndTime(h.getEndTime());
						}
						if(oh != null){
							dbItem.setOrderEndTime(oh.getEndTime());
						}
						
						dbItem.setOrderStepEndTime(osh.getEndTime());
						dbItem.setError(osh.isError());
						dbItem.setErrorCode(osh.getErrorCode());
						dbItem.setErrorText(osh.getErrorText());
						
						this.save(dbItem);*/
					}					
					
				}
			}
			catch(Exception ex){
				throw ex;
			}
			finally{
				try{
					if(sr != null){	sr.close();}
				}
				catch(Exception e){}
			}
		}
		return result;
	}


	/**
	 * @TODO evtl. mit Join statt where wegen sybase umschreiben. 
	 * auch evtl. für alle DB nur eine spalte im exists lesen
	 * 
	 *  
	 * @return
	 * @throws Exception
	 */
	public int setOrderNotificationsRecovered() throws Exception {
		//bei Sybase soll org.hibernate.dialect.SybaseASE15Dialect
		//benutzt werden, sonst zB. SybaseDialect produziert Ausgabe 
		//(hibernate bug https://hibernate.atlassian.net/browse/HHH-5356) "cross join" <- das wird nicht unterstützt
		// ausserdem sybase liefert fehler wenn mehrere spalten in exists selectiert werden
		/**
		String select = "";
		if(this.dialectClassName.toLowerCase().contains("sybase")){
			select = " select osh.id.historyId ";
		}
				
		StringBuffer sb = new StringBuffer("update "
				+ DBITEM_SCHEDULER_MON_NOTIFICATIONS
				+ " mn ")
				.append("set mn.recovered = 1 ")
				.append("where mn.error = 1 ")
				.append(" and exists (")
				.append(select+" from "
						+ DBITEM_SCHEDULER_ORDER_STEP_HISTORY
						+ " osh,")
				.append("	" + DBITEM_SCHEDULER_ORDER_HISTORY
						+ " oh")
				.append("	where osh.id.historyId = oh.id.historyId")
				.append("		and mn.schedulerId = oh.spoolerId")
				.append("	    and mn.orderHistoryId = osh.id.historyId")
				.append("		and mn.orderStepState = osh.state")
				.append("		and mn.step <= osh.id.step")
				.append("		and osh.error = 0")
				.append("		and osh.endTime is not null")
				.append(")");
		*/
		StringBuffer sb = new StringBuffer("update "
				+ DBITEM_SCHEDULER_MON_NOTIFICATIONS
				+ " mn ")
				.append("set mn.recovered = 1 ")
				.append("where mn.error = 1 ")
				.append(" and exists (")
				.append("select osh.id.historyId from "
						+ DBITEM_SCHEDULER_ORDER_STEP_HISTORY
						+ " osh ")
				.append("	where mn.orderHistoryId = osh.id.historyId")
				.append("		and mn.orderStepState = osh.state")
				.append("		and mn.step <= osh.id.step")
				.append("		and osh.error = 0")
				.append("		and osh.endTime is not null")
				.append(")");
		
		Query query = session.createQuery(sb.toString());

		return query.executeUpdate();
	}

	/**
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public DBItemSchedulerHistory getSchedulerHistory(Long taskId) throws Exception {
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_HISTORY + " ")
				.append("where id = :taskId");

		Query query = session.createQuery(sql.toString());
		query.setReadOnly(true);
		
		query.setParameter("taskId", taskId);

		@SuppressWarnings("unchecked")
		List<DBItemSchedulerHistory> result = executeQueryList(query);
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param schedulerId
	 * @param standalone
	 * @param taskId
	 * @param step
	 * @param orderHistoryId
	 * @return
	 */
	public DBItemSchedulerMonNotifications getNotification(
			String schedulerId, 
			boolean standalone,
			Long taskId, 
			Long step, 
			Long orderHistoryId) throws Exception {
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " ")
				.append("where schedulerId = :schedulerId ")
				.append("and standalone = :standalone ")
				.append("and taskId = :taskId ")
				.append("and step = :step ")
				.append("and orderHistoryId = :orderHistoryId ");
			
		Query query = session.createQuery(sql.toString());
		query.setParameter("schedulerId", schedulerId);
		query.setParameter("standalone", standalone);
		query.setParameter("taskId", taskId);
		query.setParameter("step", step);
		query.setParameter("orderHistoryId", orderHistoryId);
		
		@SuppressWarnings("unchecked")
		List<DBItemSchedulerMonNotifications> result = executeQueryList(query);
		if (result.size() > 0) {
			return result.get(0);
		}
		
	return null;
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public DBItemSchedulerMonNotifications getNotification(
			Long id) throws Exception {
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " ")
				.append("where id = :id ");
			
		Query query = session.createQuery(sql.toString());
		query.setParameter("id", id);
	
		@SuppressWarnings("unchecked")
		List<DBItemSchedulerMonNotifications> result = executeQueryList(query);
		if (result.size() > 0) {
			return result.get(0);
		}

		return null;
	}

	/**
	 * 
	 * @param systemId
	 * @param serviceName
	 * @param notificationId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonSystemNotifications> getSystemNotifications(
			String systemId,
			String serviceName,
			Long notificationId
			) throws Exception {
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
				.append("where notificationId = :notificationId ")
				.append("and serviceName = :serviceName ")
				.append("and lower(systemId) = :systemId");
			
		Query query = session.createQuery(sql.toString());
		query.setParameter("notificationId", notificationId);
		query.setParameter("systemId", systemId.toLowerCase());
		query.setParameter("serviceName", serviceName);
		
		return executeQueryList(query);
	}
	
	/**
	 * 
	 * @param systemId
	 * @param serviceName
	 * @param notificationId
	 * @param checkId
	 * @param stepFrom
	 * @param stepTo
	 * @return
	 * @throws Exception 
	 */
	public DBItemSchedulerMonSystemNotifications getSystemNotification(
			String systemId,
			String serviceName,
			Long notificationId,
			Long checkId,
			String stepFrom,
			String stepTo
			) throws Exception {
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
				.append("where notificationId = :notificationId ")
				.append("and checkId = :checkId ")
				.append("and stepFrom = :stepFrom ")
				.append("and stepTo = :stepTo ")
				.append("and serviceName = :serviceName ")
				.append("and lower(systemId) = :systemId");
			
		Query query = session.createQuery(sql.toString());
		query.setParameter("notificationId", notificationId);
		query.setParameter("checkId", checkId);
		query.setParameter("stepFrom", stepTo);
		query.setParameter("stepTo", stepTo);
		query.setParameter("serviceName", serviceName);
		query.setParameter("systemId", systemId.toLowerCase());
		
		@SuppressWarnings("unchecked")
		List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(query);
		if (result.size() > 0) {
			return result.get(0);
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Date getLastNotificationDate() throws Exception {

		Date lastDate = null;

		Query query = session.createQuery("from "
				+ DBITEM_SCHEDULER_VARIABLES
				+ " where name = :name");
		query.setParameter("name", SCHEDULER_VARIABLES_NOTIFICATION);
		try {
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerVariables> result = executeQueryList(query);
			if(result.size() > 0){
				DBItemSchedulerVariables dbItem = result.get(0);
				try {
					lastDate = DBLayerSchedulerMon.getDateFromString(dbItem
						.getTextValue());
				} catch (Exception ex) {
				logger.warn(String.format("'%s' cannot be converted to date: value = '%s', %s", 
						SCHEDULER_VARIABLES_NOTIFICATION,
						dbItem.getTextValue(),
						ex.getMessage()));
				}
			}
		} catch (Exception ex) {
			logger.warn(String.format("'%s' cannot be given from db:  %s", 
					SCHEDULER_VARIABLES_NOTIFICATION,
					ex.getMessage()));
		}

		if (lastDate == null) {
			lastDate = getCurrentDateTimeMinusDays(1);
		}
		return lastDate;
	}

	/**
	 * 
	 * @param notificationId
	 * @param name
	 * @param value
	 * @return
	 */
	public DBItemSchedulerMonResults createResult(
			Long notificationId,
			String name, 
			String value) {

		DBItemSchedulerMonResults dbItem = new DBItemSchedulerMonResults();
		dbItem.setNotificationId(notificationId);
		dbItem.setName(name);
		dbItem.setValue(value);
		dbItem.setCreated(DBLayerSchedulerMon.getCurrentDateTime());
		dbItem.setModified(DBLayerSchedulerMon.getCurrentDateTime());

		return dbItem;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonChecks> getChecksForNotifyTimer()
			throws Exception {
		
		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
		.append("where checked = 1");
		
		Query q = session.createQuery(sql.toString());
		q.setReadOnly(true);
		
		return executeQueryList(q);

	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifyError()
			throws Exception {

		StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
		.append("where n1.step = 1 ")
		.append("and ")
		.append("  (select count(n2.id) ") 
		.append("  from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ") 
		.append("  where n2.orderHistoryId = n1.orderHistoryId ") 
		.append("  		and n2.error = 1 ")
		.append("  		and n2.recovered = 0 ")
		.append("  ) > 0 "); 
	
		Query query = this.session.createQuery(sql.toString());
		query.setReadOnly(true);
		
		return executeQueryList(query);
	}

	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifyRecovered()
			throws Exception {

		StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
		.append("where n1.step = 1 ")
		.append("and ")
		.append("  (select count(n2.id) ")
		.append("  from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n2 ")
		.append("  where n2.orderHistoryId = n1.orderHistoryId ")
		.append("  		and n2.recovered = 1 ")
		.append("  ) > 0 ");

		Query query = this.session.createQuery(sql.toString());
		query.setReadOnly(true);
		
		return executeQueryList(query);
	}
			
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifySuccess() throws Exception{
		
		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n1 ")
		.append("where n1.step = 1 ")
		.append("and n1.orderEndTime is not null ")
		.append("and ") 
		.append("  (select n2.error ") 
		.append("  from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ") 
		.append("  where n2.id = ") 
		.append("    (select MAX(n3.id) ") 
		.append("    from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n3 ")
		.append("    where n3.orderHistoryId = n1.orderHistoryId ") 
		.append("   )") 
		.append("  ) = 0 ");  
		
		Query query = this.session.createQuery(sql.toString());
		query.setReadOnly(true);
		
		/**
		List<DBItemSchedulerMonNotifications> result = null;
		
		try{
			result = query.list();
		}
		catch(Exception ex){
			logger.info(String.format("getNotificationsForNotifySuccess. try again in %s. cause exception = %s",RERUN_TRANSACTION_INTERVAL,ex.getMessage()));
			Thread.sleep(RERUN_TRANSACTION_INTERVAL*1000);
			result = query.list();
		}*/
		return executeQueryList(query);
	}
	
	@SuppressWarnings("rawtypes")
	private List executeQueryList(Query q) throws Exception{
		List result = null;
		
		try{
			result = q.list();
		}
		catch(Exception ex){
			logger.info(String.format("executeQueryList. try again in %s. cause exception = %s",RERUN_TRANSACTION_INTERVAL,ex.getMessage()));
			Thread.sleep(RERUN_TRANSACTION_INTERVAL*1000);
			result = q.list();
		}
		return result;
	}
	
	/**
	 * 
	 * @param orderHistoryId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getOrderNotifications(Long orderHistoryId) throws Exception{
		
		StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
		.append("where orderHistoryId = :orderHistoryId ")
		.append("order by step");
		
		Query q = session.createQuery(sql.toString());
		q.setReadOnly(true);
		
		q.setParameter("orderHistoryId",orderHistoryId);
		
		return executeQueryList(q);
	}
	
	/**
	 * 
	 * @param checkId
	 * @return
	 * @throws Exception
	 */
	public int removeCheck(Long checkId) throws Exception{
		StringBuffer sql = new StringBuffer("delete "+DBITEM_SCHEDULER_MON_CHECKS+" ")
		.append("where id = :id ");
		
		Query q = session.createQuery(sql.toString());
		q.setParameter("id",checkId);
		
		return q.executeUpdate();
	}
	
	/**
	 * 
	 * @param name
	 * @param notificationId
	 * @param stepFrom
	 * @param stepTo
	 * @param stepFromStartTime
	 * @param stepToEndTime
	 * @return
	 */
	public DBItemSchedulerMonChecks createCheck(String name,
			Long notificationId,
			String stepFrom,
			String stepTo,
			Date stepFromStartTime,
			Date stepToEndTime){
		DBItemSchedulerMonChecks item = new DBItemSchedulerMonChecks();
		item.setName(name);
		item.setNotificationId(notificationId);
		item.setStepFrom(stepFrom);
		item.setStepTo(stepTo);
		item.setStepFromStartTime(stepFromStartTime);
		item.setStepToEndTime(stepToEndTime);
		
		item.setChecked(false);
		item.setCreated(DBLayerSchedulerMon.getCurrentDateTime());
		item.setModified(DBLayerSchedulerMon.getCurrentDateTime());
		
		return item;
	}
	
	/**
	 * 
	 * @param systemId
	 * @param serviceName
	 * @param notificationId
	 * @param checkId
	 * @param stepFrom
	 * @param stepTo
	 * @param stepFromStartTime
	 * @param stepToEndTime
	 * @param notifications
	 * @param acknowledged
	 * @param recovered
	 * @param success
	 * @return
	 */
	public DBItemSchedulerMonSystemNotifications createSystemNotification(
			String systemId,
			String serviceName,
			Long notificationId,
			Long checkId,
			String stepFrom,
			String stepTo,
			Date stepFromStartTime,
			Date stepToEndTime,
			Long notifications,
			boolean acknowledged,
			boolean recovered,
			boolean success) {

		DBItemSchedulerMonSystemNotifications dbItem = new DBItemSchedulerMonSystemNotifications();
		dbItem.setSystemId(systemId);
		dbItem.setServiceName(serviceName);
		dbItem.setNotificationId(notificationId);
		dbItem.setCheckId(checkId);
		dbItem.setStepFrom(stepFrom);
		dbItem.setStepTo(stepTo);
		dbItem.setStepFromStartTime(stepFromStartTime);
		dbItem.setStepToEndTime(stepToEndTime);
		dbItem.setMaxNotifications(false);
		dbItem.setNotifications(notifications);
		dbItem.setAcknowledged(acknowledged);
		dbItem.setRecovered(recovered);
		dbItem.setSuccess(success);
		
		dbItem.setCreated(DBLayerSchedulerMon.getCurrentDateTime());
		dbItem.setModified(DBLayerSchedulerMon.getCurrentDateTime());

		return dbItem;
	}

	/**
	 * 
	 * @param schedulerId
	 * @param standalone
	 * @param taskId
	 * @param step
	 * @param orderHistoryId
	 * @param jobChainName
	 * @param jobChainTitle
	 * @param orderId
	 * @param orderTitle
	 * @param orderStartTime
	 * @param orderEndTime
	 * @param orderStepState
	 * @param orderStepStartTime
	 * @param orderStepEndTime
	 * @param jobName
	 * @param jobTitle
	 * @param taskStartTime
	 * @param taskEndTime
	 * @param recovered
	 * @param error
	 * @param errorCode
	 * @param errorText
	 * @return
	 * @throws Exception
	 */
	public DBItemSchedulerMonNotifications createNotification(
			String schedulerId, 
			boolean standalone,
			Long taskId, 
			Long step, 
			Long orderHistoryId, 
			String jobChainName,
			String jobChainTitle, 
			String orderId, 
			String orderTitle,
			Date orderStartTime, 
			Date orderEndTime, 
			String orderStepState, 
			Date orderStepStartTime, 
			Date orderStepEndTime,
			String jobName,
			String jobTitle, 
			Date taskStartTime, 
			Date taskEndTime,
			boolean recovered,
			boolean error, 
			String errorCode, 
			String errorText
			) throws Exception {

		DBItemSchedulerMonNotifications dbItem = new DBItemSchedulerMonNotifications();

		// set unique key
		dbItem.setSchedulerId(schedulerId);
		dbItem.setStandalone(standalone);
		dbItem.setTaskId(taskId);
		dbItem.setStep(step);
		dbItem.setOrderHistoryId(orderHistoryId);
		
		// set others
		dbItem.setJobChainName(jobChainName);
		dbItem.setJobChainTitle(jobChainTitle);
		dbItem.setOrderId(orderId);
		dbItem.setOrderTitle(orderTitle);
		dbItem.setOrderStartTime(orderStartTime);
		dbItem.setOrderEndTime(orderEndTime);
		dbItem.setOrderStepState(orderStepState);
		dbItem.setOrderStepStartTime(orderStepStartTime);
		dbItem.setOrderStepEndTime(orderStepEndTime);
		dbItem.setJobName(jobName);
		dbItem.setJobTitle(jobTitle);
		dbItem.setTaskStartTime(taskStartTime);
		dbItem.setTaskEndTime(taskEndTime);
		dbItem.setRecovered(recovered); 
		dbItem.setError(error); 
		dbItem.setErrorCode(errorCode);
		dbItem.setErrorText(errorText);
		dbItem.setCreated(DBLayerSchedulerMon.getCurrentDateTime());
		dbItem.setModified(DBLayerSchedulerMon.getCurrentDateTime());

		return dbItem;
	}

	/**
	 * 
	 * @return
	 */
	public static Date getCurrentDateTime() {
		return new DateTime(DateTimeZone.UTC).toLocalDateTime().toDate();
	}
	
	/**
	 * 
	 * @return
	 */
	public static Date getCurrentDateTimeMinusDays(int days) {
		return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusDays(days).toDate();
	}
	
	public static Date getCurrentDateTimeMinusMinutes(int minutes) {
		return new DateTime(DateTimeZone.UTC).toLocalDateTime().minusMinutes(minutes).toDate();
	}
	/**
	 * 
	 * @param date
	 * @throws Exception
	 */
	public void setLastNotificationDate(Date date) throws Exception {

		DBItemSchedulerVariables dbItem = new DBItemSchedulerVariables();
		dbItem.setName(SCHEDULER_VARIABLES_NOTIFICATION);
		dbItem.setTextValue(DBLayerSchedulerMon.getDateAsString(date));

		this.saveOrUpdate(dbItem);
	}
	
	/**
	 * 
	 * @param d
	 * @return
	 * @throws Exception
	 */
	public static String getDateAsString(Date d) throws Exception {
		DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
		DateTime dt = new DateTime(d);
		return f.print(dt);
	}

	/**
	 * 
	 * @param d
	 * @return
	 * @throws Exception
	 */
	public static Date getDateFromString(String d) throws Exception {
		DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
		return f.parseDateTime(d).toDate();
	}

	
	/**
	 * 
	 */
	public void rollback(){
		if(this.getTransaction() != null){
			this.getTransaction().rollback();
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unused")
	private String quoteX(String value){
		if(value != null){
			value = value.replaceAll("'", "''"); 
		}
		return value;
	}
	
}
