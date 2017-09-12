package com.phaz1.mcc.datalayer;

import com.orm.SugarRecord;

/**
 * Created by altaf on 03/05/2017.
 */

public class CallTemplate extends SugarRecord<CallTemplate> {
    private String contactPhoneNumber;
    private String title;
    private String type = "Fixed";
    private String recurringRule = "No Repeat";
    private long startDate;
    private long endDate;
    private int complete;
    private int alertBefore;

    public CallTemplate() {
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public int getAlertBefore() {
        return alertBefore;
    }

    public void setAlertBefore(int alertBefore) {
        this.alertBefore = alertBefore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecurringRule() {
        return recurringRule;
    }

    public void setRecurringRule(String recurringRule) {
        this.recurringRule = recurringRule;
    }
}