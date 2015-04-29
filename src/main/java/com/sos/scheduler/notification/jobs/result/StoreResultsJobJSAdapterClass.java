package com.sos.scheduler.notification.jobs.result;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSString;
/**
 * 
 * @author Robert Ehrlich
 *
 */
public class StoreResultsJobJSAdapterClass extends JobSchedulerJobAdapter  {
	private static Logger		logger			= Logger.getLogger(StoreResultsJobJSAdapterClass.class);

	StoreResultsJob job = null;
	StoreResultsJobOptions options = null;
	
	/**
	 * wird im procces_after aufgerufen
	 * evtl in process_task_before aurufen
	 * 
	 */
	public void init() throws Exception {
		
		job = new StoreResultsJob();
		options = job.Options();
		options.CurrentNodeName(this.getCurrentNodeName());
		options.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
	    job.setJSJobUtilites(this);
		
	    if(SOSString.isEmpty(options.scheduler_notification_hibernate_configuration_file.Value())){
	    	File f = new File(new File(spooler.configuration_directory()).getParent(), "hibernate.cfg.xml");
	    	options.scheduler_notification_hibernate_configuration_file.Value(f.getAbsolutePath());
	    }
	    job.init();
	}

	/**
	 * ohne getSpecialParameters hat in den Versionen vor 1.9 Fehler produziert
	@Override
	public HashMap<String, String> getSpecialParameters() {

		HashMap<String, String> specialParams = new HashMap<String, String>();
		if (spooler == null) {  // junit test specific
			return specialParams;
		}
		// specialParams.put("SCHEDULER_RETURN_VALUES", remoteCommandScriptOutputParamsFileName);
		specialParams.put("SCHEDULER_HOST", spooler.hostname());
		specialParams.put("SCHEDULER_TCP_PORT", "" + spooler.tcp_port());
		specialParams.put("SCHEDULER_UDP_PORT", "" + spooler.udp_port());
		specialParams.put("SCHEDULER_ID", spooler.id());
		specialParams.put("SCHEDULER_DIRECTORY", spooler.directory());
		specialParams.put("SCHEDULER_CONFIGURATION_DIRECTORY", spooler.configuration_directory());

		if (isJobchain()) {
			specialParams.put("SCHEDULER_JOB_CHAIN_NAME", spooler_task.order().job_chain().name());
			specialParams.put("SCHEDULER_JOB_CHAIN_TITLE", spooler_task.order().job_chain().title());
			specialParams.put("SCHEDULER_ORDER_ID", spooler_task.order().id());
			specialParams.put("SCHEDULER_NODE_NAME", getCurrentNodeName());
			
			//RE Anpassungen
			Job_chain_node node = spooler_task.order().job_chain_node();
			specialParams.put("SCHEDULER_NEXT_NODE_NAME", node == null ? "" : node.next_state());
			specialParams.put("SCHEDULER_NEXT_ERROR_NODE_NAME", node == null ? "" : node.error_state());
		}

		specialParams.put("SCHEDULER_JOB_NAME", this.getJobName());
		specialParams.put("SCHEDULER_JOB_FOLDER", this.getJobFolder());
		specialParams.put("SCHEDULER_JOB_PATH", this.getJobFolder()+"/"+this.getJobName());
		specialParams.put("SCHEDULER_JOB_TITLE", this.getJobTitle());
		specialParams.put("SCHEDULER_TASK_ID", "" + spooler_task.id());

		Supervisor_client objRemoteConfigurationService;
		try {
			objRemoteConfigurationService = spooler.supervisor_client();
			if (objRemoteConfigurationService != null) {
				specialParams.put("SCHEDULER_SUPERVISOR_HOST", objRemoteConfigurationService.hostname());
				specialParams.put("SCHEDULER_SUPERVISOR_PORT", "" + objRemoteConfigurationService.tcp_port());
			}
		}
		catch (Exception e) {
			specialParams.put("SCHEDULER_SUPERVISOR_HOST", "n.a.");
			specialParams.put("SCHEDULER_SUPERVISOR_PORT", "n.a.");
		}

		return specialParams;
	}*/
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public void exit() throws Exception {
		if(job != null){
			job.exit();
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
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
			
			options.mon_results_scheduler_id.Value(spooler.id());
			options.mon_results_task_id.value(spooler_task.id());
			
			if(order != null && order.job_chain_node() != null){
				options.mon_results_order_step_state.Value(order.job_chain_node().state());
			}
			else{
				logger.debug(String.format("set mon_results_order_step_state to NULL"));
				
				options.mon_results_order_step_state.Value(null);
			}
			
			options.mon_results_order_id.Value((order == null ? null : order
					.id()));
			
			options.mon_results_standalone
					.value(order == null ? true : false);

			
			// in neuen job scheduler versionen
			if(order != null && order.job_chain() != null){
				options.mon_results_job_chain_name.Value(order.job_chain().path());
					
			}
			else{
				logger.debug(String.format("set mon_results_job_chain_name to NULL"));
				options.mon_results_job_chain_name.Value(null);
			}
			
			
			/**
			 * workaround bis 1.6 if(order == null){
			 * this.objO.mon_results_job_chain_name.Value(null); } else{
			 * this.objO
			 * .mon_results_job_chain_name.Value(spooler_job.folder_path
			 * ()+"/"+order.job_chain().name()); }
			 */

			job.Execute();
	
			this.exit();
		}
	}
	
	/**
	 * Bei standalone Jobs wird in spooler_task_after aufgerufen
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
	 * Bei order Jobs wird in spooler_process_after aufgerufen
	 * @throws Exception 
	 * 
	 */
	@Override
	public boolean spooler_process_after(boolean spooler_process_result) throws Exception {
		boolean  result = false;
		try{
			result = super.spooler_process_after(spooler_process_result);
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

