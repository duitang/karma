/**
 * @author laurence
 * @since 2016年10月17日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author laurence
 * @since 2016年10月17日
 *
 */
public class TestZK {

	private ZooKeeper zk;
	private CountDownLatch connSignal = new CountDownLatch(1);

	// host should be 127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002
	public ZooKeeper connect(String host) throws Exception {
		zk = new ZooKeeper(host, 1000, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected) {
					connSignal.countDown();
				}
			}
		});
		connSignal.await();
		return zk;
	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public void createNode(String path, byte[] data) throws Exception {
		zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	public void updateNode(String path, byte[] data) throws Exception {
		zk.setData(path, data, zk.exists(path, true).getVersion());
	}

	public void deleteNode(String path) throws Exception {
		zk.delete(path, zk.exists(path, true).getVersion());
	}

	public static void main(String args[]) throws Exception {
		TestZK connector = new TestZK();
		ZooKeeper zk = connector.connect("192.168.1.180");
		Thread.sleep(40000);
		String newNode = "/deepakDate" + new Date();
		connector.createNode(newNode, new Date().toString().getBytes());
		List<String> zNodes = zk.getChildren("/", true);
		for (String zNode : zNodes) {
			System.out.println("ChildrenNode " + zNode);
		}
		byte[] data = zk.getData(newNode, true, zk.exists(newNode, true));
		System.out.println("GetData before setting");
		for (byte dataPoint : data) {
			System.out.print((char) dataPoint);
		}

		System.out.println("GetData after setting");
		connector.updateNode(newNode, "Modified data".getBytes());
		data = zk.getData(newNode, true, zk.exists(newNode, true));
		for (byte dataPoint : data) {
			System.out.print((char) dataPoint);
		}
		connector.deleteNode(newNode);
		
		System.out.println("------------------------------------");
	}

}
