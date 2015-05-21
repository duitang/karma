package com.duitang.service.karma.support;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 2:45:49 PM May 12, 2015
 */
public class DynamicConfig implements Runnable {

    private String connString;
    
    private CuratorFramework client;
    
    private CountDownLatch itemInitLatch = new CountDownLatch(1);
    private Map<String, String> dynamicItems = Maps.newHashMap();
    
    public void registerItem(String itemName, String defaultVal) {
        dynamicItems.put(itemName, defaultVal);
    }
    
    public void setDynamicItem(String itemKey, String data) {
        String path = String.format("/config/%s", itemKey);
        try {
            dynamicItems.put(itemKey, data);
            client.setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String queryItem(String itemKey) {
        return dynamicItems.get(itemKey);
    }
    
    public void init() {
        new Thread(this, "dynamicConfig").start();
        final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 30);
        client =  CuratorFrameworkFactory.newClient(connString, retryPolicy);
        client.start();
    }
    
    @Override
    public void run() {
        try {
            client.blockUntilConnected();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        while(true) {
            try {
                Map<String, String> dyItemsCopy = Maps.newHashMap(dynamicItems);
                for (Map.Entry<String, String> e : dyItemsCopy.entrySet()) {
                    String path = String.format("/config/%s", e.getKey());
                    byte[] bb = client.getData().forPath(path);
                    if (bb != null && bb.length > 0) {
                        dynamicItems.put(e.getKey(), new String(bb));
                    }
                }
                itemInitLatch.countDown();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setItemmap(Map<String, String> itemmap) {
        if (itemmap != null) {
            for (Map.Entry<String, String> item : itemmap.entrySet()) {
                dynamicItems.put(item.getKey(), item.getValue());
            }
        }
    }

    public void setConnString(String connString) {
        this.connString = connString;
    }
}
