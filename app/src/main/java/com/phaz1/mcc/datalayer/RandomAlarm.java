package com.phaz1.mcc.datalayer;

import com.orm.SugarRecord;

/**
 * Created by altaf on 16/05/2017.
 */

public class RandomAlarm extends SugarRecord<RandomAlarm> {
    public Long eventId;
    public Long startTime;
    public Long endTime;
    public Long randomDate;

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setRandomDate(Long randomDate) {
        this.randomDate = randomDate;
    }
}