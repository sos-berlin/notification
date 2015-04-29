package com.sos.scheduler.notification.helper;

public class CounterCheckHistory {
	int total = 0;
	int skip = 0;
	int insert = 0;
	int update = 0;
	int insertTimer = 0;
	int batchInsert = 0;
	int batchInsertTimer = 0;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public void addTotal() {
		this.total++;
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
	public int getInsert() {
		return insert;
	}
	public void setInsert(int insert) {
		this.insert = insert;
	}
	public void addInsert() {
		this.insert++;
	}
	public int getUpdate() {
		return update;
	}
	public void setUpdate(int update) {
		this.update = update;
	}
	public void addUpdate() {
		this.update++;
	}
	public int getInsertTimer() {
		return insertTimer;
	}
	public void setInsertTimer(int insertTimer) {
		this.insertTimer = insertTimer;
	}
	public void addInsertTimer() {
		this.insertTimer++;
	}
	
	
	public int getBatchInsert() {
		return batchInsert;
	}
	public void setBatchInsert(int insert) {
		this.batchInsert = insert;
	}
	public void addBatchInsert(int insert) {
		this.batchInsert+= insert;
	}
	public void addBatchInsert() {
		addBatchInsert(1);
	}
	
	
	public int getBatchInsertTimer() {
		return batchInsertTimer;
	}
	public void setBatchInsertTimer(int insert) {
		this.batchInsertTimer = insert;
	}
	public void addBatchInsertTimer(int insert) {
		this.batchInsertTimer+= insert;
	}
	public void addBatchInsertTimer() {
		addBatchInsertTimer(1);
	}
}
