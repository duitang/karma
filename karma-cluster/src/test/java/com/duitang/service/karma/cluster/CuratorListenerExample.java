package com.duitang.service.karma.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class CuratorListenerExample {

	static void log() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Logger logger = (Logger) LoggerFactory.getLogger("org.apache.zookeeper");
		logger.setLevel(Level.INFO);
	}

	public static void main(String[] args) throws Exception {
		log();
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, Integer.MAX_VALUE);
		CuratorFramework curator = CuratorFrameworkFactory.newClient("192.168.1.180:2181", retryPolicy);
		curator.start();
//		curator.getZookeeperClient().blockUntilConnectedOrTimedOut();
		Thread.sleep(2000);
		if (null == curator.checkExists().forPath("/test/abc")){			
			curator.create().creatingParentsIfNeeded().forPath("/test/abc");
		}

		curator.getCuratorListenable().addListener(new CuratorListener() {
			@Override
			public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
				System.out.println(event.getType());
				System.err.println(event.getWatchedEvent() + " watched.");
				event.getChildren();
				System.out.println(client.checkExists().forPath("/test/abc"));
				System.out.println("----------+++++++++++");
			}
		});

		String path = "/test";
		curator.getChildren().watched().forPath(path);
		curator.create().creatingParentsIfNeeded().forPath(path + "/" + Math.random());
		// one-time watch

		Thread.sleep(Integer.MAX_VALUE);
	}

}
