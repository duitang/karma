package com.duitang.service.karma.stats;

public class LocationTag {
    public final String app;
    public final long pid;
    public final String host;

    public LocationTag(String app, long pid, String host) {
        this.app = app;
        this.pid = pid;
        this.host = host;
    }
}
