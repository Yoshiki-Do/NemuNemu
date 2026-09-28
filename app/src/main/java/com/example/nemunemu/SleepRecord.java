package com.example.nemunemu;

public class SleepRecord {

    private String date;
    private String sleepTime;
    private String wakeTime;
    private float duration;

    public SleepRecord(String date, String sleepTime, String wakeTime, float duration) {

        this.date = date;
        this.sleepTime = sleepTime;
        this.wakeTime= wakeTime;
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public String getSleepTime() {
        return sleepTime;
    }

    public String getWakeTime() {
        return wakeTime;
    }

    public float getDuration() {
        return duration;
    }
}
