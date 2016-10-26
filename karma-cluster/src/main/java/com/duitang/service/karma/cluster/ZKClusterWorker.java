/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
package com.duitang.service.karma.cluster;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.DaemonJobs;
import com.duitang.service.karma.support.RPCNode;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RegistryInfo;

/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
public class ZKClusterWorker implements Watcher {

	static final String zkBase = "/karma_rpc";
	static final String zkNodeBase = zkBase + "/nodes";

	final static long WORK_PERIOD = 10 * 1000; // 10s
	final static int timeout = 1000;
	final static int retries = 3;

	final static Charset enc = Charset.forName("utf8");
	final static Set<String> schema = new HashSet<String>(Arrays.asList("tcp", "udp"));

	final static ConcurrentHashMap<String, ZKClusterWorker> owner = new ConcurrentHashMap<>();

	protected String conn;
	// protected CuratorFramework cur;
	protected ZooKeeper zkCli;
	protected ZKServerRegistry zkSR;
	protected ZKClientListener lsnr;

	/**
	 * <pre>
	 *  notice:
	 *  it's just like NIO Selector
	 * </pre>
	 * 
	 * @author laurence
	 * @since 2016年10月11日
	 *
	 */
	static class ClusterMonitor implements Runnable {

		final protected Semaphore latch = new Semaphore(0);

		void resume() {
			latch.release();
		}

		@Override
		public void run() {
			while (true) {
				for (ZKClusterWorker o : owner.values()) {
					if (!o.zkCli.getState().isConnected()) {
						continue;
					}

					for (RPCService rpc : o.zkSR.getRegServices()) {
						o.syncWrite(rpc);
					}

					try {
						eventReceived(o);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					latch.tryAcquire(WORK_PERIOD, TimeUnit.MILLISECONDS);
					latch.drainPermits();
				} catch (InterruptedException e) {
				}
			}
		}

		public void eventReceived(ZKClusterWorker w) throws Exception {
			if (w != null) {
				try {
					// no problem just ignore all return, we will refresh force
					RegistryInfo ret = w.lsnr.syncPull();
					if (ret == null) {
						return;
					}

					if (ret.isFreezeMode()) {
						w.lsnr.updateAllNodes(ret.getHashing().reverseToMap());
					} else {
						w.lsnr.updateAllNodes(ret.getURLs());
					}
					// watch it again
					w.zkCli.getChildren(zkNodeBase, true);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

	}

	static ClusterMonitor monitor;
	static {
		monitor = new ClusterMonitor();
		DaemonJobs.runJob(monitor);
	}

	public static ZKClusterWorker createInstance(String conn) {
		ZKClusterWorker ret = owner.get(conn);
		if (ret == null) {
			synchronized (ZKClusterWorker.class) {
				ret = owner.get(conn);
				if (ret == null) {
					ZKServerRegistry zkSR = new ZKServerRegistry();
					ZKClientListener lsnr = new ZKClientListener();
					ret = new ZKClusterWorker(zkSR, lsnr, conn);
					try {
						// 1.5s reconnect
						ret.zkCli = new ZooKeeper(conn, 1500, ret);
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Warning: not connected Zookeeper --> " + conn);
					}
					zkSR.setWorker(ret);
					lsnr.setWorker(ret);
					owner.put(conn, ret);
				}
			}
		}
		return ret;
	}

	ZKClusterWorker(ZKServerRegistry zkSR, ZKClientListener lsnr, String conn) {
		this.zkSR = zkSR;
		this.lsnr = lsnr;
		this.conn = conn;
	}

	synchronized void ensureBasedir() {
		try {
			if (zkCli.exists(zkBase, null) == null) {
				zkCli.create(zkBase, "".getBytes(enc), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			// discard
		}
		try {
			if (zkCli.exists(zkNodeBase, null) == null) {
				zkCli.create(zkNodeBase, "".getBytes(enc), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			// discard
		}
	}

	public boolean syncWrite(RPCService rpc) {
		return syncWrite(rpc, rpc.online());
	}

	synchronized boolean syncWrite(RPCService rpc, boolean online) {
		RPCNode info = new RPCNode();
		info.url = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		info.protocol = rpc.getServiceProtocol();
		info.group = rpc.getGroup();
		info.up = rpc.getUptime().getTime();
		info.heartbeat = new Date().getTime();
		info.halted = online ? null : info.heartbeat + 1;
		String ret0 = info.toDataString();

		ensureBasedir();
		String nodepath = zkNodeBase + "/" + RPCNodeHashing.getSafeConnURL(info.url);
		boolean ret = false;
		try {
			if (zkCli.exists(nodepath, false) == null) {
				zkCli.create(nodepath, ret0.getBytes(enc), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			} else {
				zkCli.setData(nodepath, ret0.getBytes(enc), -1);
			}
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	synchronized public List<RPCNode> syncRead() {
		List<RPCNode> ret = new ArrayList<RPCNode>();
		List<String> children = null;
		try {
			children = zkCli.getChildren(zkNodeBase, false);
		} catch (Exception e) {
			children = Collections.emptyList();
		}
		for (String p : children) {
			try {
				String pathnode = zkNodeBase + "/" + p;
				byte[] buf = zkCli.getData(pathnode, false, zkCli.exists(pathnode, false));
				RPCNode node = RPCNode.fromBytes(buf);
				if (schema.contains(StringUtils.lowerCase(node.protocol)) && node.isAlive()) {
					ret.add(node);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	synchronized public RPCNode syncRead(String url) {
		List<RPCNode> ret = syncRead();
		url = RPCNodeHashing.getRawConnURL(url);
		for (RPCNode n : ret) {
			if (url.equals(RPCNodeHashing.getRawConnURL(n.url))) {
				return n;
			}
		}
		return null;
	}

	synchronized public ClusterMode syncGetMode() {
		ClusterMode ret = null;
		try {
			boolean r = zkCli.exists(zkNodeBase, false) == null;
			if (!r) {
				byte[] buf = zkCli.getData(zkNodeBase, false, zkCli.exists(zkNodeBase, false));
				ret = ClusterMode.fromBytes(buf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	synchronized public boolean syncSetMode(ClusterMode mode) {
		boolean ret = false;
		try {
			String data = mode.toDataString();
			boolean r = zkCli.exists(zkNodeBase, false) == null;
			if (r) {
				zkCli.create(zkNodeBase, data.getBytes(enc), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} else {
				zkCli.setData(zkNodeBase, data.getBytes(enc), -1);
			}
			ret = true;
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

	synchronized public boolean syncClearMode() {
		boolean ret = false;
		try {
			boolean r = zkCli.exists(zkNodeBase, false) == null;
			if (!r) {
				zkCli.setData(zkNodeBase, "".getBytes(enc), -1);
			}
			ret = true;
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

	public boolean syncClearRPCNode(RPCService rpc) {
		String nodepath = zkNodeBase + "/" + RPCNodeHashing.getSafeConnURL(rpc.getServiceURL());
		boolean ret = false;
		try {
			zkCli.delete(nodepath, -1);
			ret = true;
		} catch (Exception e) {
			// maybe already deleted, no problem
			ret = false;
		}
		return ret;
	}

	protected RegistryInfo refreshRPCNodes() {
		boolean freezing = false;
		RPCNodeHashing hashing;
		// refresh Cluster Mode
		ClusterMode mode = syncGetMode();
		if (mode != null && mode.freeze != null && mode.freeze && mode.nodes != null) {
			// freezing mode
			freezing = true;
			hashing = RPCNodeHashing.createFromHashMap(mode.nodes);
		} else {
			// not freezing, refresh Nodes
			freezing = false;
			List<RPCNode> nodes = syncRead();
			hashing = RPCNodeHashing.createFromNodes(nodes);
		}

		return new RegistryInfo(freezing, hashing);
	}

	@Override
	public void process(WatchedEvent event) {
		monitor.resume();
	}

}
