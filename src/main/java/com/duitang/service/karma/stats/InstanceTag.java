package com.duitang.service.karma.stats;

public class InstanceTag {
    public final String app;
    public final long pid;
    public final String host;

    public InstanceTag(String app, long pid, String host) {
        this.app = app;
        this.pid = pid;
        this.host = host;
    }
}
