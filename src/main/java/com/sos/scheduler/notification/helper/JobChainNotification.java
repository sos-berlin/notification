package com.sos.scheduler.notification.helper;

import java.util.List;

import com.sos.scheduler.notification.db.DBItemSchedulerMonNotifications;

public class JobChainNotification {

    private DBItemSchedulerMonNotifications lastStepForNotification;
    private DBItemSchedulerMonNotifications lastStep;
    private DBItemSchedulerMonNotifications stepFrom;
    private DBItemSchedulerMonNotifications stepTo;
    private List<DBItemSchedulerMonNotifications> steps;
    private Long stepFromIndex = new Long(0);
    private Long stepToIndex = new Long(0);

    public DBItemSchedulerMonNotifications getLastStepForNotification() {
        return lastStepForNotification;
    }

    public void setLastStepForNotification(DBItemSchedulerMonNotifications step) {
        this.lastStepForNotification = step;
    }

    public DBItemSchedulerMonNotifications getLastStep() {
        return lastStep;
    }

    public void setLastStep(DBItemSchedulerMonNotifications step) {
        this.lastStep = step;
    }

    public DBItemSchedulerMonNotifications getStepFrom() {
        return stepFrom;
    }

    public void setStepFrom(DBItemSchedulerMonNotifications stepFrom) {
        this.stepFrom = stepFrom;
    }

    public DBItemSchedulerMonNotifications getStepTo() {
        return stepTo;
    }

    public void setStepTo(DBItemSchedulerMonNotifications stepTo) {
        this.stepTo = stepTo;
    }

    public List<DBItemSchedulerMonNotifications> getSteps() {
        return steps;
    }

    public void setSteps(List<DBItemSchedulerMonNotifications> steps) {
        this.steps = steps;
    }

    public Long getStepFromIndex() {
        return stepFromIndex;
    }

    public void setStepFromIndex(Long stepFromIndex) {
        this.stepFromIndex = stepFromIndex;
    }

    public Long getStepToIndex() {
        return stepToIndex;
    }

    public void setStepToIndex(Long stepToIndex) {
        this.stepToIndex = stepToIndex;
    }

}
