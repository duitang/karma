package com.duitang.service.karma.client;

import java.util.Map;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 12:01:27 PM Mar 12, 2015
 */
public class CloudLogger {
    
    private transient static final Map<String, CloudLogger> cache = Maps.newConcurrentMap();
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private Logger log = null;
    
    public static CloudLogger get(String biz) {
        CloudLogger cl = cache.get(biz);
        if (cl == null) {
            synchronized (CloudLogger.class) {
                if (!cache.containsKey(biz)) {
                    cl = new CloudLogger(biz);
                    cache.put(biz, cl);
                }
            }
        }
        return cl;
    }
    
    private CloudLogger(String biz) {
        PatternLayout layout = new PatternLayout();
        String conversionPattern = "%m%n";
        layout.setConversionPattern(conversionPattern);
        
        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
        rollingAppender.setFile("/duitang/logs/cloud/" + biz + "/main.pending");
        rollingAppender.setDatePattern("'.'yyyy-MM-dd");
        rollingAppender.setLayout(layout);
        rollingAppender.activateOptions();
        log = Logger.getLogger(biz);
        log.removeAllAppenders();
        log.setLevel(Level.ALL);
        log.addAppender(rollingAppender);
    }
    
    public void log(Object obj) {
        try {
            String s = mapper.writeValueAsString(obj);
            log(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void log(String s) {
        log.warn(s);
    }
}
