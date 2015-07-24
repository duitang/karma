package com.duitang.service.karma.stats;

import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationHolder {
    private static final Logger logger = LoggerFactory.getLogger(LocationHolder.class);

    public static volatile String LOCATION;
    static String HOSTNAME = genHostName();
    private static String APP_NAME = genAppName();

    private static String genHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.error("get hostname error", e);
        }
        return "host-" + String.valueOf(ThreadLocalRandom.current().nextInt(10000,99999));
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
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    public static String getHostname() {
        return HOSTNAME;
    }

    public static void resetLocation() {
        StringBuilder b = new StringBuilder();
        b.append(HOSTNAME);
        if (!APP_NAME.isEmpty()) {
            b.append('-').append(APP_NAME);
        }
        LOCATION = b.toString();
    }

    static {
        resetLocation();
    }
}
