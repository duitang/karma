package com.duitang.service.karma.stats;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.collect.ImmutableMap;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationHolder {
    private static final Logger logger = LoggerFactory.getLogger(LocationHolder.class);

    public static volatile ImmutableMap<String, String> LOCATION;
    static String HOSTNAME = genHostName();
    private static String APP_NAME = genAppName();
    private static long PID = genPID();

    private static long genPID() {
        long pid = -1;
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String[] split = name.split("@", 2);
            if (split.length > 1) {
                pid = Long.parseLong(split[0]);
            }
        } catch (Exception e) {
            logger.error("get pid error", e);
        }
        return pid;
    }

    private static String genHostName() {
        String name = null;
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("get hostname error", e);
        }
        return name == null ? "" : name;
    }

    private static String genAppName() {
        String _appName = null;
        try {
            _appName = SystemPropertyUtil.get("app.name");
        } catch (Exception e) {
            logger.error("get APP_NAME error", e);
        }
        if (_appName == null) {
            _appName = "";
        }
        return _appName;
    }

    public static void setHostname(String hostname) {
        HOSTNAME = hostname;
        resetLocation();
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
        resetLocation();
    }

    public static String getHostname() {
        return HOSTNAME;
    }

    public static void resetLocation() {
        LOCATION = ImmutableMap.of(
                "host", HOSTNAME,
                "app", APP_NAME,
                "pid", String.valueOf(PID)
        );
    }

    static {
        resetLocation();
    }
}
