package com.sos.scheduler.notification.jobs.result;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSString;

public class StoreResultsJobJSAdapterClass extends JobSchedulerJobAdapter  {
	private static Logger logger = Logger.getLogger(StoreResultsJobJSAdapterClass.class);

	StoreResultsJob job = null;
	StoreResultsJobOptions options = null;
	
	/**
	 * call in procces_after or in process_task_after
	 * 
	 */
	public void init() throws Exception {
		
		job = new StoreResultsJob();
		options = job.getOptions();
		options.setCurrentNodeName(this.getCurrentNodeName());
		options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
	    job.setJSJobUtilites(this);
		
	    if(SOSString.isEmpty(options.scheduler_notification_hibernate_configuration_file.getValue())){
	    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
	    	options.scheduler_notification_hibernate_configuration_file.setValue(f.getAbsolutePath());
	    }
	    job.init();
	}

	public void exit() throws Exception {
		if(job != null){
			job.exit();
		}
	}
	
	private void doProcessing(boolean isStandalone) throws Exception {
	
		Order order = spooler_task.order();
		if(!isStandalone){
			if(order == null || order.job_chain() == null || order.job_chain_node() == null){
				logger.info(String.format("exit processing. object is null: order = %s, order.job_chain = %s, order.job_chain_node = %s",order,order.job_chain(),order.job_chain_node()));
				
				return;
			}
		}
		
		Variable_set params = this.getParameters();
		if (params != null && params.count() > 0) {
			init();
			
			options.mon_results_scheduler_id.setValue(spooler.id());
			options.mon_results_task_id.value(spooler_task.id());
			
			if(order != null && order.job_chain_node() != null){
				options.mon_results_order_step_state.setValue(order.job_chain_node().state());
			}
			else{
				logger.debug(String.format("set mon_results_order_step_state to NULL"));
				options.mon_results_order_step_state.setValue(null);
			}
			
			options.mon_results_order_id.setValue((order == null ? null : order.id()));
			options.mon_results_standalone.value(order == null ? true : false);
			
			if(order != null && order.job_chain() != null){
				options.mon_results_job_chain_name.setValue(order.job_chain().path());
					
			}
			else{
				logger.debug(String.format("set mon_results_job_chain_name to NULL"));
				options.mon_results_job_chain_name.setValue(null);
			}
			
			job.execute();
			this.exit();
		}
	}
	
	/**
	 * standalone jobs
	 * 
	 */
	@Override
	public void spooler_task_after() throws Exception {
		try{
			super.spooler_task_after();
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			try{
				if(spooler_task.job().order_queue() == null){
					doProcessing(true);
				}
			}
			catch(Exception ex){
				spooler_log.warn(ex.getMessage());
			}
		}
	}
	
	/**
	 * order jobs
	 * @throws Exception 
	 * 
	 */
	@Override
	public boolean spooler_process_after(boolean processResult) throws Exception {
		boolean  result = false;
		try{
			result = super.spooler_process_after(processResult);
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			try{
				if(spooler_task.job().order_queue() != null){
					doProcessing(false);
				}
			}
			catch(Exception ex){
				spooler_log.warn(ex.getMessage());
			}
		}
		return result;
	}
}

