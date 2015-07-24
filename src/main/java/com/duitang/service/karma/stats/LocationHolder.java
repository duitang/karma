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
    private static String APP_NAME = getAppName();

    private static String genHostName() {
        String _hostname = null;
        try {
            _hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.error("get hostname error", e);
            _hostname = System.getenv("HOSTNAME");
            if (_hostname == null || _hostname.isEmpty()) {
                _hostname = "unknown_" + ThreadLocalRandom.current().nextLong(10000, 99999);
            }
        }
        return  _hostname;
    }

    public static String getAppName() {
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
