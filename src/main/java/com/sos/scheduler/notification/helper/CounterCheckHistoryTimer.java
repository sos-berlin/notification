package com.sos.scheduler.notification.helper;

public class CounterCheckHistoryTimer {
	private int total = 0;
	private int rerun = 0;
	private int remove = 0;
	private int skip = 0;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public void addTotal() {
		this.total++;
	}
	public int getRerun() {
		return rerun;
	}
	public void setRerun(int rerun) {
		this.rerun = rerun;
	}
	public void addRerun() {
		this.rerun++;
	}
	public int getRemove() {
		return remove;
	}
	public void setRemove(int remove) {
		this.remove = remove;
	}
	public void addRemove() {
		this.remove++;
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
}
