package com.ruiyun.jvppeteer.options;

public class TimezoneState extends ActiveProperty{

    public String timezoneId;

    public TimezoneState(boolean active, String timezoneId) {
        super(active);
        this.timezoneId = timezoneId;
    }

    public TimezoneState(boolean active) {
        super(active);
    }

    public String getTimezoneId() {
        return timezoneId;
    }

    public void setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
    }
}
