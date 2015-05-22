package com.duitang.service.karma.demo;

import org.apache.commons.lang3.math.NumberUtils;

import com.duitang.service.karma.support.ServicesExporter;
import com.google.common.collect.Lists;

/**
 * 
 * @author kevx
 * @since 3:27:15 PM May 21, 2015
 */
public class QuantitativeSvcStarter {

    public static void main(String[] args) {
//        int mql = NumberUtils.toInt(args[0], 500);
        int mql = 500;
        if(args.length == 1) {
            mql = NumberUtils.toInt(args[0], 500);
        }
        QuantitativeBenchService svc = new QuantitativeBenchServiceImpl();
        ServicesExporter se = new ServicesExporter();
        se.setServices(Lists.newArrayList((Object)svc));
        se.setMaxQueuingLatency(mql);
        se.setPort(11990);
        se.init();
        System.out.println(String.format("QuantitativeSvc_started: with maxQeLatency %d \n", mql));
    }

}
