package com.sos.scheduler.notification.helper;

public class CounterSystemNotifier {
	private int success = 0;
	private int error = 0;
	private int skip = 0;
	private int total = 0;
	
	
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	public void addSuccess() {
		this.success++;
	}
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public void addError() {
		this.error++;
	}
	public int getSkip() {
		return skip;
	}
	public void setSkip(int skip) {
		this.skip = skip;
	}
	public void addSkip() {
		this.skip++;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public void addTotal() {
		this.total++;
	}
}
