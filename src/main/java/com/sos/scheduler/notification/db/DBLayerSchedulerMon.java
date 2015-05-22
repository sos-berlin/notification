package com.sos.scheduler.notification.db;

import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.hibernate.classes.SOSHibernateConnection.DBMS;
import com.sos.hibernate.classes.SOSHibernateResultSetProcessor;
import com.sos.scheduler.history.db.SchedulerOrderStepHistoryDBItem;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBItem;

public class DBLayerSchedulerMon extends DBLayer{

	final Logger logger = LoggerFactory.getLogger(DBLayerSchedulerMon.class);
		
	/**
	 * 
	 * @param conn
	 */
	public DBLayerSchedulerMon(SOSHibernateConnection conn){
		super(conn);
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
	public DBItemNotificationSchedulerHistoryOrderStep getNotFinishedOrderStepHistory(
			String schedulerId, 
			Long taskId, 
			String state, 
			String jobChain,
			String orderId) throws Exception {

		try{
			Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class,"osh");
			//join
			cr.createAlias("osh.schedulerOrderHistoryDBItem","oh");
			cr.createAlias("osh.schedulerTaskHistoryDBItem","h");
			
			ProjectionList pl = Projections.projectionList();
			//select field list osh
			pl.add(Projections.property("osh.id.step").as("stepStep"));
			pl.add(Projections.property("osh.id.historyId").as("stepHistoryId"));
			pl.add(Projections.property("osh.taskId").as("stepTaskId"));
			pl.add(Projections.property("osh.startTime").as("stepStartTime"));
			pl.add(Projections.property("osh.endTime").as("stepEndTime"));
			pl.add(Projections.property("osh.state").as("stepState"));
			pl.add(Projections.property("osh.error").as("stepError"));
			pl.add(Projections.property("osh.errorCode").as("stepErrorCode"));
			pl.add(Projections.property("osh.errorText").as("stepErrorText"));
			//select field list oh
			pl.add(Projections.property("oh.historyId").as("orderHistoryId"));
			pl.add(Projections.property("oh.spoolerId").as("orderSchedulerId"));
			pl.add(Projections.property("oh.orderId").as("orderId"));
			pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
			pl.add(Projections.property("oh.state").as("orderState"));
			pl.add(Projections.property("oh.stateText").as("orderStateText"));
			pl.add(Projections.property("oh.startTime").as("orderStartTime"));
			pl.add(Projections.property("oh.endTime").as("orderEndTime"));
			//select field list h
			pl.add(Projections.property("h.id").as("taskId"));
			pl.add(Projections.property("h.jobName").as("taskJobName"));
			pl.add(Projections.property("h.cause").as("taskCause"));
			pl.add(Projections.property("h.startTime").as("taskStartTime"));
			pl.add(Projections.property("h.endTime").as("taskEndTime"));
			
			cr.setProjection(pl);
	
			cr.add(Restrictions.eq("oh.spoolerId",schedulerId));
			cr.add(Restrictions.eq("osh.taskId",taskId));
			cr.add(Restrictions.eq("osh.state",state));
			cr.add(Restrictions.eq("oh.jobChain",jobChain));
			cr.add(Restrictions.eq("oh.orderId",orderId));
			cr.add(Restrictions.isNull("osh.endTime"));
			
			cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
			cr.setReadOnly(true);
			
			@SuppressWarnings("unchecked")
			List<DBItemNotificationSchedulerHistoryOrderStep> result = cr.list();
			if (result.size() > 0) {
				return result.get(0);
			}

			return null;
		
		}
		catch(Exception ex){
			ex.printStackTrace(System.err);
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param date
	 * @throws Exception
	 */
	public void cleanupNotifications(Date date) throws Exception{
		try{
			StringBuffer sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
			.append("where created <= :date");
			int count = getConnection().createQuery(sql.toString()).setTimestamp("date",date).executeUpdate();
			logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_NOTIFICATIONS,count));
			
			sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_RESULTS+" ")
			.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
			count = getConnection().createQuery(sql.toString()).executeUpdate();
			logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_RESULTS,count));
		
			sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
			.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
			count = getConnection().createQuery(sql.toString()).executeUpdate();
			logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_CHECKS,count));
			
			sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS+" ")
			.append("where notificationId not in (select id from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+") ");
			int countS1 = getConnection().createQuery(sql.toString()).executeUpdate();
			
			sql = new StringBuffer("delete from "+DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS+" ")
			.append("where checkId > 0 and checkId not in (select id from "+DBITEM_SCHEDULER_MON_CHECKS+") ");
			int countS2 = getConnection().createQuery(sql.toString()).executeUpdate();
			count = countS1+countS2;
			logger.info(String.format("deleted %s = %s", DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS,count));
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
			
	/**
	 * 
	 * @param systemId
	 * @param serviceName
	 * @return
	 * @throws Exception 
	 */
	public int resetAcknowledged(String systemId,String serviceName) throws Exception{
		try{
			StringBuffer sql = new StringBuffer("update "
					+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
					.append("set acknowledged = 1 ")
					.append("where lower(systemId) = :systemId ");
			if(!SOSString.isEmpty(serviceName)){
				sql.append(" and serviceName =:serviceName");
			}
			
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("systemId", systemId.toLowerCase());
	
			if(!SOSString.isEmpty(serviceName)){
				query.setParameter("serviceName",serviceName);
			}
			return query.executeUpdate();
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	
	/**
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	public Criteria getSchedulerHistorySteps(Date dateFrom, Date dateTo) throws Exception{
		Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class,"osh");
		//join
		cr.createAlias("osh.schedulerOrderHistoryDBItem","oh");
		cr.createAlias("osh.schedulerTaskHistoryDBItem","h");
		
		ProjectionList pl = Projections.projectionList();
		//select field list osh
		pl.add(Projections.property("osh.id.step").as("stepStep"));
		pl.add(Projections.property("osh.id.historyId").as("stepHistoryId"));
		pl.add(Projections.property("osh.taskId").as("stepTaskId"));
		pl.add(Projections.property("osh.startTime").as("stepStartTime"));
		pl.add(Projections.property("osh.endTime").as("stepEndTime"));
		pl.add(Projections.property("osh.state").as("stepState"));
		pl.add(Projections.property("osh.error").as("stepError"));
		pl.add(Projections.property("osh.errorCode").as("stepErrorCode"));
		pl.add(Projections.property("osh.errorText").as("stepErrorText"));
		//select field list oh
		pl.add(Projections.property("oh.historyId").as("orderHistoryId"));
		pl.add(Projections.property("oh.spoolerId").as("orderSchedulerId"));
		pl.add(Projections.property("oh.orderId").as("orderId"));
		pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
		pl.add(Projections.property("oh.state").as("orderState"));
		pl.add(Projections.property("oh.stateText").as("orderStateText"));
		pl.add(Projections.property("oh.startTime").as("orderStartTime"));
		pl.add(Projections.property("oh.endTime").as("orderEndTime"));
		//select field list h
		pl.add(Projections.property("h.id").as("taskId"));
		pl.add(Projections.property("h.jobName").as("taskJobName"));
		pl.add(Projections.property("h.cause").as("taskCause"));
		pl.add(Projections.property("h.startTime").as("taskStartTime"));
		pl.add(Projections.property("h.endTime").as("taskEndTime"));
		
		cr.setProjection(pl);
	
		//where
		if(dateTo != null){
			cr.add(Restrictions.le("oh.startTime",dateTo));
			if(dateFrom != null){
				cr.add(Restrictions.ge("oh.startTime",dateFrom));
			}	
		}
		cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
		cr.setReadOnly(true);
		
		return cr;
	}

	
	/**
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws Exception
	 */
	public Criteria getSchedulerHistorySteps(Long historyId, Long step) throws Exception{
		Criteria cr = getConnection().createCriteria(SchedulerOrderStepHistoryDBItem.class,"osh");
		//join
		cr.createAlias("osh.schedulerOrderHistoryDBItem","oh");
		cr.createAlias("osh.schedulerTaskHistoryDBItem","h");
		
		ProjectionList pl = Projections.projectionList();
		//select field list osh
		pl.add(Projections.property("osh.id.step").as("stepStep"));
		pl.add(Projections.property("osh.id.historyId").as("stepHistoryId"));
		pl.add(Projections.property("osh.taskId").as("stepTaskId"));
		pl.add(Projections.property("osh.startTime").as("stepStartTime"));
		pl.add(Projections.property("osh.endTime").as("stepEndTime"));
		pl.add(Projections.property("osh.state").as("stepState"));
		pl.add(Projections.property("osh.error").as("stepError"));
		pl.add(Projections.property("osh.errorCode").as("stepErrorCode"));
		pl.add(Projections.property("osh.errorText").as("stepErrorText"));
		//select field list oh
		pl.add(Projections.property("oh.historyId").as("orderHistoryId"));
		pl.add(Projections.property("oh.spoolerId").as("orderSchedulerId"));
		pl.add(Projections.property("oh.orderId").as("orderId"));
		pl.add(Projections.property("oh.jobChain").as("orderJobChain"));
		pl.add(Projections.property("oh.state").as("orderState"));
		pl.add(Projections.property("oh.stateText").as("orderStateText"));
		pl.add(Projections.property("oh.startTime").as("orderStartTime"));
		pl.add(Projections.property("oh.endTime").as("orderEndTime"));
		//select field list h
		pl.add(Projections.property("h.id").as("taskId"));
		pl.add(Projections.property("h.jobName").as("taskJobName"));
		pl.add(Projections.property("h.cause").as("taskCause"));
		pl.add(Projections.property("h.startTime").as("taskStartTime"));
		pl.add(Projections.property("h.endTime").as("taskEndTime"));
		
		cr.setProjection(pl);
	
		//where
		cr.add(Restrictions.le("osh.id.historyId",historyId));
		cr.add(Restrictions.le("osh.id.step",step));
		
		cr.setResultTransformer(Transformers.aliasToBean(DBItemNotificationSchedulerHistoryOrderStep.class));
		cr.setReadOnly(true);
		
		return cr;
	}
	/**
	 * 
	 * @param notificationId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationOrderSteps(Long notificationId) throws Exception{
		try{	
			String method = "getNotificationOrderSteps";
			StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n1 ")
			.append("where exists (")
			.append("   select n2.orderHistoryId ") 
			.append("   from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ")
			.append("   where n1.orderHistoryId = n2.orderHistoryId ")
			.append("   and n2.id = :id ")
			.append(") ")
			.append("order by n1.step");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setParameter("id",notificationId);
			
			return executeQueryList(method,sql,q);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	/**
	 * 
	 * @param notificationId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonResults> getNotificationResults(Long notificationId) throws Exception{
		try{	
			String method = "getNotificationResults";
			StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_RESULTS+" r ")
			.append("where r.notificationId = :id");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setParameter("id",notificationId);
			
			return executeQueryList(method,sql,q);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonChecks> getSchedulerMonChecksForSetTimer() throws Exception{
		try{
			String method = "getSchedulerMonChecksForSetTimer";
			StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
			.append("where checked = 0");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setReadOnly(true);
			
			return executeQueryList(method,sql,q);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		
		try{
			check.setStepFromStartTime(stepFromStartTime);
			check.setStepToEndTime(stepToEndTime);
			check.setChecked(true);
			check.setCheckText(text);
			check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
			
			check.setModified(DBLayer.getCurrentDateTime());
			getConnection().update(check);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		
		try{
			check.setStepFromStartTime(stepFromStartTime);
			check.setStepToEndTime(stepToEndTime);
			check.setChecked(false);
			check.setCheckText("1");
			check.setResultIds(SOSString.isEmpty(resultIds) ? null : resultIds);
			check.setModified(DBLayer.getCurrentDateTime());
			getConnection().update(check);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param isDbDependent
	 * @param maxStartTime
	 * @return
	 * @throws Exception
	 */
	public int updateUncompletedNotifications(boolean isDbDependent,Date maxStartTime) throws Exception {
		
		boolean executed = false;
		int result = 0;
		try{
			if(isDbDependent){
				//Table t_mn = DBItemSchedulerMonNotifications.class.getAnnotation(Table.class);
				Enum<SOSHibernateConnection.DBMS> dbms = getConnection().getDbms();
				if(dbms.equals(DBMS.MSSQL)){
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
					result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
				}
				else if(dbms.equals(DBMS.ORACLE) || dbms.equals(DBMS.DB2)){
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
					 result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
				}
				else if(dbms.equals(DBMS.MYSQL)){
					executed = true;
					
					StringBuffer sb = new StringBuffer("update " + TABLE_SCHEDULER_MON_NOTIFICATIONS+ " mn")
					.append(","+TABLE_SCHEDULER_ORDER_HISTORY + " oh")
					.append("," + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh")
					.append("," + TABLE_SCHEDULER_HISTORY + " h ")
					.append("set "+quote("mn.ORDER_END_TIME")+" = "+quote("oh.END_TIME")+" ")
					.append(","+quote("mn.TASK_END_TIME")+" = "+quote("h.END_TIME")+" ")
					.append(","+quote("mn.ORDER_STEP_END_TIME")+" = "+quote("osh.END_TIME")+" ")
					.append(","+quote("mn.ERROR")+" = "+quote("osh.ERROR")+" ")
					.append(","+quote("mn.ERROR_CODE")+" = "+quote("osh.ERROR_CODE")+" ")
					.append(","+quote("mn.ERROR_TEXT")+" = "+quote("osh.ERROR_TEXT")+" ")
					.append("where "+quote("mn.ORDER_END_TIME")+" is null ")
					.append("and "+quote("mn.STEP")+" = "+quote("osh.STEP")+" ")
					.append("and "+quote("mn.ORDER_HISTORY_ID")+" = "+quote("oh.HISTORY_ID")+" ")
					.append("and "+quote("osh.HISTORY_ID")+" = "+quote("oh.HISTORY_ID")+" ")
					.append("and "+quote("h.ID")+" = "+quote("osh.TASK_ID"));
					result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
			
				}
				else if(dbms.equals(DBMS.PGSQL) || dbms.equals(DBMS.SYBASE)){
					executed = true;
					StringBuffer sb = new StringBuffer("update "+TABLE_SCHEDULER_MON_NOTIFICATIONS+" ")
					.append("set "+quote("ORDER_END_TIME")+" = oh."+quote("END_TIME")+" ")
					.append(","+quote("TASK_END_TIME")+" = h."+quote("END_TIME")+" ")
					.append(","+quote("ORDER_STEP_END_TIME")+" = osh."+quote("END_TIME")+" ")
					.append(","+quote("ERROR")+" = osh."+quote("ERROR")+" ")
					.append(","+quote("ERROR_CODE")+" = osh."+quote("ERROR_CODE")+" ")
					.append(","+quote("ERROR_TEXT")+" = osh."+quote("ERROR_TEXT")+" ")
					.append("from " + TABLE_SCHEDULER_ORDER_HISTORY + " oh ")
					.append("inner join " + TABLE_SCHEDULER_ORDER_STEP_HISTORY + " osh ")
					.append("on osh."+quote("HISTORY_ID")+" = oh."+quote("HISTORY_ID")+" ")
					.append("inner join " + TABLE_SCHEDULER_HISTORY + " h ")
					.append("on h."+quote("ID")+" = osh."+quote("TASK_ID")+" ")
					.append("where "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quote("ORDER_HISTORY_ID")+" = oh."+quote("HISTORY_ID")+" ")
					.append("and "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quote("ORDER_END_TIME")+" is null ")
					.append("and "+TABLE_SCHEDULER_MON_NOTIFICATIONS+"."+quote("STEP")+" = osh."+quote("STEP"));
					
					result = getConnection().createSQLQuery(sb.toString()).executeUpdate();
				}
				/**
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
				}*/
				//Firebird kann nicht bzw. sehr umständlich in einem Update Tabelle mit join aktualisieren - kein extra Update
						
			}
			
			//Datenbankunabhängig
			if(!executed){
				SOSHibernateResultSetProcessor notificationProcessor = new SOSHibernateResultSetProcessor(getConnection());
				SOSHibernateResultSetProcessor historyProcessor = new SOSHibernateResultSetProcessor(getConnection());
								
				Criteria notificationCriteria = getConnection().createCriteria(DBItemSchedulerMonNotifications.class,"n");
				ProjectionList pl = Projections.projectionList();
				pl.add(Projections.property("n.id").as("id"));
				pl.add(Projections.property("n.orderHistoryId").as("orderHistoryId"));
				pl.add(Projections.property("n.step").as("step"));
				notificationCriteria.setProjection(pl);
				notificationCriteria.add(Restrictions.isNull("n.orderEndTime"));
				if(maxStartTime != null){
					notificationCriteria.add(Restrictions.ge("n.orderStartTime",maxStartTime));
				}
				
				notificationCriteria.setReadOnly(true);
				
				StringBuffer update = new StringBuffer("update "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
				.append("set taskEndTime = :taskEndTime ")
				.append(",orderEndTime = :orderEndTime ")
				.append(",orderStepEndTime = :orderStepEndTime ")
				.append(",error = :error ")
				.append(",errorCode = :errorCode ")
				.append(",errorText = :errorText ")
				.append(",modified = :modified ")
				.append("where id = :id");
							
				try{
					ResultSet notificationResultSet = notificationProcessor.createResultSet(notificationCriteria,ScrollMode.FORWARD_ONLY);
					
					int readCount = 0;
					while (notificationResultSet.next() )
					{
						readCount++;
						flushScrollableResults(readCount);
					
						DBItemSchedulerMonNotifications notification = (DBItemSchedulerMonNotifications)notificationProcessor.get();
						
						Criteria historyCriteria = getSchedulerHistorySteps(notification.getOrderHistoryId(),notification.getStep());
						ResultSet historyResultSet = historyProcessor.createResultSet(DBItemNotificationSchedulerHistoryOrderStep.class,historyCriteria,ScrollMode.FORWARD_ONLY);
						try{
							while(historyResultSet.next()){
								DBItemNotificationSchedulerHistoryOrderStep osh =  (DBItemNotificationSchedulerHistoryOrderStep)historyProcessor.get();
								
								//wegen flushScrollableResults lieber update als this.save(dbItem)
								Query q = getConnection().createQuery(update.toString());
								q.setParameter("id",notification.getId());
								q.setParameter("taskEndTime",osh.getTaskEndTime());
								q.setParameter("orderEndTime",osh.getOrderEndTime());
								q.setParameter("orderStepEndTime",osh.getStepEndTime());
								q.setParameter("error",osh.getStepError());
								q.setParameter("errorCode",osh.getStepErrorCode());
								q.setParameter("errorText",osh.getStepErrorText());
								q.setParameter("modified",DBLayer.getCurrentDateTime());
								
								result += q.executeUpdate();
							}	
						}
						catch(Exception ex){
							throw new Exception(SOSHibernateConnection.getException(ex));
						}
						finally{
							historyProcessor.close();
						}
					}
				}
				catch(Exception ex){
					throw ex;
				}
				finally{
					try{
						if(notificationProcessor != null){notificationProcessor.close();}
					}
					catch(Exception e){}
				}
			}
			return result;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		try{
			StringBuffer sb = new StringBuffer("update "
					+ DBITEM_SCHEDULER_MON_NOTIFICATIONS
					+ " mn ")
					.append("set mn.recovered = 1 ")
					.append("where mn.error = 1 ")
					.append(" and exists (")
					.append("select osh.id.historyId from "
							+ SchedulerOrderStepHistoryDBItem.class.getSimpleName()
							+ " osh ")
					.append("	where mn.orderHistoryId = osh.id.historyId")
					.append("		and mn.orderStepState = osh.state")
					.append("		and mn.step <= osh.id.step")
					.append("		and osh.error = 0")
					.append("		and osh.endTime is not null")
					.append(")");
			
			Query query = getConnection().createQuery(sb.toString());
	
			return query.executeUpdate();
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}

	/**
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public SchedulerTaskHistoryDBItem getSchedulerHistory(Long taskId) throws Exception {
		try{
			StringBuffer sql = new StringBuffer("from "
					+ SchedulerTaskHistoryDBItem.class.getSimpleName() + " ")
					.append("where id = :taskId");
			String method = "getSchedulerHistory";
			
			Query query = getConnection().createQuery(sql.toString());
			query.setReadOnly(true);
			
			query.setParameter("taskId", taskId);
	
			@SuppressWarnings("unchecked")
			List<SchedulerTaskHistoryDBItem> result = executeQueryList(method,sql,query);
			if (result.size() > 0) {
				return result.get(0);
			}
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		
		try{
			String method = "getNotification";
			
			StringBuffer sql = new StringBuffer("from "
					+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " ")
					.append("where schedulerId = :schedulerId ")
					.append("and standalone = :standalone ")
					.append("and taskId = :taskId ")
					.append("and step = :step ")
					.append("and orderHistoryId = :orderHistoryId ");
				
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("schedulerId", schedulerId);
			query.setParameter("standalone", standalone);
			query.setParameter("taskId", taskId);
			query.setParameter("step", step);
			query.setParameter("orderHistoryId", orderHistoryId);
			
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerMonNotifications> result = executeQueryList(method,sql,query);
			if (result.size() > 0) {
				return result.get(0);
			}
			
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception 
	 */
	public DBItemSchedulerMonNotifications getNotification(
			Long id) throws Exception {
		
		try{
			String method = "getNotification";
			
			StringBuffer sql = new StringBuffer("from "
					+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " ")
					.append("where id = :id ");
				
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("id", id);
		
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerMonNotifications> result = executeQueryList(method,sql,query);
			if (result.size() > 0) {
				return result.get(0);
			}
	
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		try{
			String method = "getSystemNotifications";
			
			StringBuffer sql = new StringBuffer("from "
					+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
					.append("where notificationId = :notificationId ")
					.append("and serviceName = :serviceName ")
					.append("and lower(systemId) = :systemId");
			
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("notificationId", notificationId);
			query.setParameter("systemId", systemId.toLowerCase());
			query.setParameter("serviceName", serviceName);
			
			return executeQueryList(method,sql,query);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		
		try{
			String method = "getSystemNotification";
			
			logger.debug(
					String.format("%s: systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
					method,
					systemId,
					serviceName,
					notificationId,
					checkId,
					stepFrom,
					stepTo));
			
			StringBuffer sql = new StringBuffer("from "
					+ DBITEM_SCHEDULER_MON_SYSNOTIFICATIONS + " ")
					.append("where notificationId = :notificationId ")
					.append("and checkId = :checkId ")
					.append("and stepFrom = :stepFrom ")
					.append("and stepTo = :stepTo ")
					.append("and serviceName = :serviceName ")
					.append("and lower(systemId) = :systemId ");
				
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("notificationId", notificationId);
			query.setParameter("checkId", checkId);
			query.setParameter("stepFrom", stepFrom);
			query.setParameter("stepTo", stepTo);
			query.setParameter("serviceName", serviceName);
			query.setParameter("systemId", systemId.toLowerCase());
			
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerMonSystemNotifications> result = executeQueryList(method,sql,query);
			if (result.size() > 0) {
				return result.get(0);
			}
			else{
				logger.debug(
						String.format("%s: SystemNotification not found for systemId = %s, serviceName = %s, notificationId = %s, checkId = %s, stepFrom = %s, stepTo = %s",
						method,
						systemId,
						serviceName,
						notificationId,
						checkId,
						stepFrom,
						stepTo));
			}
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public DBItemNotificationSchedulerVariables getSchedulerVariable() throws Exception{
		String method = "getSchedulerVariable";
		
		StringBuffer sql = new StringBuffer("from "
				+ DBITEM_SCHEDULER_VARIABLES
				+ " where name = :name");

		Query query = getConnection().createQuery(sql.toString());
		query.setParameter("name", SCHEDULER_VARIABLES_NOTIFICATION);
		
		@SuppressWarnings("unchecked")
		List<DBItemNotificationSchedulerVariables> result = executeQueryList(method,sql,query);
		if(result.size() > 0){
			return result.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public Date getLastNotificationDate(DBItemNotificationSchedulerVariables dbItem) throws Exception {

		try{
			String method = "getLastNotificationDate";
			Date lastDate = null;
			
			try {
				if(dbItem != null){
					lastDate = DBLayer.getDateFromString(dbItem.getTextValue());
				}
			} 
			catch (Exception ex) {
				logger.warn(String.format("%s: '%s' cannot be converted to date: value = '%s', %s", 
						method,
						SCHEDULER_VARIABLES_NOTIFICATION,
						dbItem.getTextValue(),
						ex.getMessage()));
			}
			return lastDate;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
		dbItem.setCreated(DBLayer.getCurrentDateTime());
		dbItem.setModified(DBLayer.getCurrentDateTime());

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
		try{
			String method = "getChecksForNotifyTimer";
			
			StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_CHECKS+" ")
			.append("where checked = 1");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setReadOnly(true);
			
			return executeQueryList(method,sql,q);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param notification
	 * @return
	 * @throws Exception
	 */
	public DBItemSchedulerMonNotifications getOrderLastStepErrorNotification(DBItemSchedulerMonNotifications notification) throws Exception{
		
		try{
			String method = "getOrderLastStepErrorNotification";
			
			logger.debug(String
					.format("%s: orderHistoryId = %s",
							method,
							notification.getOrderHistoryId()));
			
			StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
			.append("where n1.orderHistoryId = :orderHistoryId ")
			.append("and n1.step = ")
			.append("  (select max(n2.step) ") 
			.append("  from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ") 
			.append("  where n2.orderHistoryId = n1.orderHistoryId ") 
			.append("  		and n2.error = 1 ")
			.append("  		and n2.recovered = 0 ")
			.append("  ) "); 
		
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("orderHistoryId",notification.getOrderHistoryId());
			query.setReadOnly(true);
			
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerMonNotifications> result = executeQueryList(method,sql,query);
			if(result.size() > 0){
				return result.get(0);
			}
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifyError()
			throws Exception {
		try{
			String method = "getNotificationsForNotifyError";
			
			StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
			.append("where n1.step = 1 ")
			.append("and ")
			.append("  (select count(n2.id) ") 
			.append("  from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ") 
			.append("  where n2.orderHistoryId = n1.orderHistoryId ") 
			.append("  		and n2.error = 1 ")
			.append("  		and n2.recovered = 0 ")
			.append("  ) > 0 "); 
		
			Query query = getConnection().createQuery(sql.toString());
			query.setReadOnly(true);
			
			return executeQueryList(method,sql,query);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}

	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifyRecovered()
			throws Exception {
		try{
			String method = "getNotificationsForNotifyRecovered";
			
			StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
			.append("where n1.step = 1 ")
			.append("and ")
			.append("  (select count(n2.id) ")
			.append("  from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n2 ")
			.append("  where n2.orderHistoryId = n1.orderHistoryId ")
			.append("  		and n2.recovered = 1 ")
			.append("  ) > 0 ");
	
			Query query = getConnection().createQuery(sql.toString());
			query.setReadOnly(true);
			
			return executeQueryList(method,sql,query);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param notification
	 * @param orderCompleted
	 * @return
	 * @throws Exception
	 */
	public DBItemSchedulerMonNotifications getOrderLastStep(DBItemSchedulerMonNotifications notification, boolean orderCompleted) throws Exception{
		
		try{
			String method = "getLastStep";
			
			logger.debug(String
					.format("%s: orderHistoryId = %s, orderCompleted = %s",
							method,
							notification.getOrderHistoryId(),
							orderCompleted));
			
			StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
			.append("where n1.orderHistoryId = :orderHistoryId ")
			.append("and n1.step = ")
			.append("  (select max(n2.step) ") 
			.append("  from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" n2 ") 
			.append("  where n2.orderHistoryId = n1.orderHistoryId ") 
			.append("  ) "); 
			if(orderCompleted){
				sql.append(" and n1.orderEndTime is not null");
			}
		
			Query query = getConnection().createQuery(sql.toString());
			query.setParameter("orderHistoryId",notification.getOrderHistoryId());
			query.setReadOnly(true);
			
			@SuppressWarnings("unchecked")
			List<DBItemSchedulerMonNotifications> result = executeQueryList(method,sql,query);
			if(result.size() > 0){
				return result.get(0);
			}
			return null;
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<DBItemSchedulerMonNotifications> getNotificationsForNotifySuccess() throws Exception{
		try{
			String method = "getNotificationsForNotifySuccess";
		
			StringBuffer sql = new StringBuffer("from "+ DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n1 ")
			.append("where n1.step = 1 ")
			.append("and n1.orderEndTime is not null ")
			.append("and ")
			.append("  (select count(n2.id) ")
			.append("  from " + DBITEM_SCHEDULER_MON_NOTIFICATIONS + " n2 ")
			.append("  where n2.orderHistoryId = n1.orderHistoryId ")
			.append("  		and n2.error = 0 ")
			.append("  ) > 0 ");
			
			Query query = getConnection().createQuery(sql.toString());
			query.setReadOnly(true);
			return executeQueryList(method,sql,query);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param functionName
	 * @param sql
	 * @param q
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private List executeQueryList(String functionName, StringBuffer sql, Query q) throws Exception{
		List result = null;
		try{
			try{
				result = q.list();
			}
			catch(Exception ex){
				if(ex instanceof LockAcquisitionException){
					logger.debug(String.format("executeQueryList. try rerun %s again in %s. cause exception = %s, sql = %s",
						functionName,
						RERUN_TRANSACTION_INTERVAL,
						ex.getMessage()
						,sql));
					Thread.sleep(RERUN_TRANSACTION_INTERVAL*1000);
					result = q.list();
				}
				else{
					throw new Exception(String.format("%s: %s , sql = %s",functionName,ex.getMessage(),sql),ex);
				}
			}
		}
		catch(Exception ex){
			throw new Exception(String.format("%s: %s , sql = %s",functionName,ex.getMessage(),sql));
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
		try{
			String method = "getOrderNotifications";
			
			StringBuffer sql = new StringBuffer("from "+DBITEM_SCHEDULER_MON_NOTIFICATIONS+" ")
			.append("where orderHistoryId = :orderHistoryId ")
			.append("order by step");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setReadOnly(true);
			
			q.setParameter("orderHistoryId",orderHistoryId);
			
			return executeQueryList(method,sql,q);
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
	/**
	 * 
	 * @param checkId
	 * @return
	 * @throws Exception
	 */
	public int removeCheck(Long checkId) throws Exception{
		try{
			StringBuffer sql = new StringBuffer("delete "+DBITEM_SCHEDULER_MON_CHECKS+" ")
			.append("where id = :id ");
			
			Query q = getConnection().createQuery(sql.toString());
			q.setParameter("id",checkId);
			
			return q.executeUpdate();
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
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
			DBItemSchedulerMonNotifications notification,
			String stepFrom,
			String stepTo,
			Date stepFromStartTime,
			Date stepToEndTime){
		
		DBItemSchedulerMonChecks item = new DBItemSchedulerMonChecks();
		item.setName(name);
		
		Long notificationId = notification.getId();
		//NULL wegen batch Insert bei den Datenbanken, die kein Autoincrement haben (Oracle ...)
		if(notificationId == null || notificationId.equals(new Long(0))){
			notificationId = new Long(0);
			item.setResultIds(notification.getSchedulerId()+";"+(notification.getStandalone() ? "true" : "false")+";"+notification.getTaskId()+";"+notification.getStep()+";"+notification.getOrderHistoryId());
		}
				
		item.setNotificationId(notificationId);
		item.setStepFrom(stepFrom);
		item.setStepTo(stepTo);
		item.setStepFromStartTime(stepFromStartTime);
		item.setStepToEndTime(stepToEndTime);
		
		item.setChecked(false);
		item.setCreated(DBLayer.getCurrentDateTime());
		item.setModified(DBLayer.getCurrentDateTime());
		
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
		
		dbItem.setCreated(DBLayer.getCurrentDateTime());
		dbItem.setModified(DBLayer.getCurrentDateTime());

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
		dbItem.setCreated(DBLayer.getCurrentDateTime());
		dbItem.setModified(DBLayer.getCurrentDateTime());

		return dbItem;
	}

	
	/**
	 * 
	 * @param dbItem
	 * @param date
	 * @throws Exception
	 */
	public void setLastNotificationDate(DBItemNotificationSchedulerVariables dbItem,Date date) throws Exception {
		try{
			if(dbItem == null){
				dbItem = new DBItemNotificationSchedulerVariables();
				dbItem.setName(SCHEDULER_VARIABLES_NOTIFICATION);
				dbItem.setTextValue(DBLayer.getDateAsString(date));
				getConnection().save(dbItem);
			}
			else{
				dbItem.setTextValue(DBLayer.getDateAsString(date));
				getConnection().update(dbItem);
			}
		}
		catch(Exception ex){
			throw new Exception(SOSHibernateConnection.getException(ex));
		}
	}
	
}
