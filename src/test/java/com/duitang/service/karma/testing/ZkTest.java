package com.duitang.service.karma.testing;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 * 
 * @author kevx
 * @since 11:29:21 AM May 14, 2015
 */
public class ZkTest {

    @Test
    public void test() throws Exception {
        final String cs = "192.168.172.15:3881";
        final RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 30);
        CuratorFramework client =  CuratorFrameworkFactory.newClient(cs, retryPolicy);
        client.start();
        try {
            client.blockUntilConnected();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/app/test");
        
        while (true) {
            System.out.println(client.getState());
            Thread.sleep(1000);
        }
        //System.out.println("alldone!");
    }
}
