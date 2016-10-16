/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
package com.duitang.service.karma.cluster;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.DeamonJobs;
import com.duitang.service.karma.support.RPCNode;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RegistryInfo;

/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
public class CuratorClusterWorker {

	static final String zkBase = "/karma_rpc";
	static final String zkNodeBase = zkBase + "/nodes";

	final static long WORK_PERIOD = 10 * 1000; // 10s
	final static int timeout = 1000;
	final static int retries = 3;

	final static Charset enc = Charset.forName("utf8");

	final static ConcurrentHashMap<String, CuratorClusterWorker> owner = new ConcurrentHashMap<>();

	protected String conn;
	protected CuratorFramework cur;
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
	static class ClusterMonitor implements Runnable, CuratorListener {

		Object lock = new Object();

		@Override
		public void run() {
			while (true) {
				synchronized (lock) {
					for (CuratorClusterWorker o : owner.values()) {
						heartbeat(o);
					}
				}

				try {
					Thread.sleep(WORK_PERIOD);
				} catch (InterruptedException e) {

				}
			}
		}

		void heartbeat(CuratorClusterWorker worker) {
			// send heartbeat sync here
			Set<RPCService> sv = worker.zkSR.getRegServices();
			for (RPCService s : sv) {
				if (s.online()) {
					worker.syncWrite(s);
				} else {
					worker.syncClearRPCNode(s);
				}
			}
		}

		@Override
		public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
			String conn = client.getZookeeperClient().getCurrentConnectionString();
			// conns.add(conn);
			// rLock.release();
			synchronized (lock) {
				CuratorClusterWorker w = owner.get(conn);
				if (w != null) {
					RegistryInfo ret = w.lsnr.syncPull();
					if (ret == null) {
						return;
					}

					if (ret.isFreezeMode()) {
						w.lsnr.updateAllNodes(ret.getHashing().reverseToMap());
					} else {
						w.lsnr.updateAllNodes(ret.getURLs());
					}
					try {
						// watch it again
						w.cur.getChildren().watched().forPath(zkNodeBase);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	static ClusterMonitor monitor;
	static {
		monitor = new ClusterMonitor();
		DeamonJobs.runJob(monitor);
	}

	public static CuratorClusterWorker createInstance(String conn) {
		CuratorClusterWorker ret = owner.get(conn);
		if (ret == null) {
			synchronized (CuratorClusterWorker.class) {
				ret = owner.get(conn);
				if (ret == null) {
					ZKServerRegistry zkSR = new ZKServerRegistry();
					ZKClientListener lsnr = new ZKClientListener();
					ret = new CuratorClusterWorker(zkSR, lsnr, conn);
					zkSR.setWorker(ret);
					lsnr.setWorker(ret);
					owner.put(conn, ret);
				}
			}
			ret.cur.getCuratorListenable().addListener(monitor);
			try {
				ret.cur.getChildren().watched().forPath(zkNodeBase);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	CuratorClusterWorker(ZKServerRegistry zkSR, ZKClientListener lsnr, String conn) {
		this.zkSR = zkSR;
		this.lsnr = lsnr;
		this.conn = conn;
		this.cur = createSimple();
		this.cur.start();
	}

	CuratorFramework createSimple() {
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(timeout, retries);
		return CuratorFrameworkFactory.newClient(conn, retryPolicy);
	}

	public boolean syncWrite(RPCService rpc) {
		RPCNode info = new RPCNode();
		info.url = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		info.protocol = rpc.getServiceProtocol();
		info.group = rpc.getGroup();
		info.online = rpc.online();
		info.up = rpc.getUptime().getTime();
		info.heartbeat = new Date().getTime();
		String ret0 = info.toDataString();

		String nodepath = zkNodeBase + "/" + RPCNodeHashing.getSafeConnURL(info.url);
		boolean ret = false;
		try {
			if (cur.checkExists().forPath(nodepath) == null) {
				cur.create().creatingParentsIfNeeded().forPath(nodepath);
			}
			cur.setData().forPath(nodepath, ret0.getBytes(enc));
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public List<RPCNode> syncRead() {
		List<RPCNode> ret = new ArrayList<RPCNode>();
		List<String> children = null;
		try {
			children = cur.getChildren().forPath(zkNodeBase);
		} catch (Exception e) {
			children = Collections.emptyList();
		}
		for (String p : children) {
			try {
				byte[] buf = cur.getData().forPath(zkNodeBase + "/" + p);
				ret.add(RPCNode.fromBytes(buf));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public RPCNode syncRead(String url) {
		List<RPCNode> ret = syncRead();
		url = RPCNodeHashing.getRawConnURL(url);
		for (RPCNode n : ret) {
			if (url.equals(RPCNodeHashing.getRawConnURL(n.url))) {
				return n;
			}
		}
		return null;
	}

	public ClusterMode syncGetMode() {
		ClusterMode ret = null;
		try {
			boolean r = cur.checkExists().forPath(zkNodeBase) == null;
			if (!r) {
				byte[] buf = cur.getData().forPath(zkNodeBase);
				ret = ClusterMode.fromBytes(buf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean syncSetMode(ClusterMode mode) {
		boolean ret = false;
		try {
			String data = mode.toDataString();
			boolean r = cur.checkExists().forPath(zkNodeBase) == null;
			if (r) {
				cur.create().creatingParentsIfNeeded().forPath(zkNodeBase, data.getBytes(enc));
			} else {
				cur.setData().forPath(zkNodeBase, data.getBytes(enc));
			}
			ret = true;
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

	public boolean syncClearMode() {
		boolean ret = false;
		try {
			boolean r = cur.checkExists().forPath(zkNodeBase) == null;
			if (!r) {
				cur.setData().forPath(zkNodeBase, "".getBytes(enc));
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
			cur.delete().forPath(nodepath);
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

}
