package com.duitang.service.karma.demo;

import com.duitang.service.karma.support.ServicesExporter;
import com.google.common.collect.Lists;

/**
 * 
 * @author kevx
 * @since 3:27:15 PM May 21, 2015
 */
public class QuantitativeSvcStarter {

    public static void main(String[] args) {
        QuantitativeBenchService svc = new QuantitativeBenchServiceImpl();
        ServicesExporter se = new ServicesExporter();
        se.setServices(Lists.newArrayList((Object)svc));
        se.setMaxQueuingLatency(500);
        se.setPort(11990);
        se.init();
    }

}
