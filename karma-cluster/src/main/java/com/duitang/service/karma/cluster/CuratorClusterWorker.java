/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.duitang.service.karma.client.RegistryInfo;
import com.duitang.service.karma.server.RPCService;

/**
 * @author laurence
 * @since 2016年10月5日
 *
 */
public class CuratorClusterWorker {

	final static long WORK_PERIOD = 10 * 1000; // 10s
	final static int timeout = 1000;
	final static int retries = 3;

	final static ConcurrentHashMap<String, CuratorClusterWorker> owner = new ConcurrentHashMap<>();

	protected String conn;
	protected boolean readOnly;
	protected CuratorFramework cur;
	protected ZKServerRegistry zkSR;
	protected ZKClientListener lsnr;
	volatile protected Map<String, ClusterNode> snapshot = new HashMap<String, ClusterNode>();

	static class ClusterMonitor extends Thread implements CuratorListener {

		@Override
		public void run() {
			while (true) {
				for (CuratorClusterWorker o : owner.values()) {
					heartbeat(o);
				}
				try {
					Thread.sleep(WORK_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
			CuratorClusterWorker w = owner.get(conn);
			if (w != null) {
				List<ClusterNode> nodes = w.syncRead();
				boolean dirty = false;
				for (ClusterNode n : nodes) {
					ClusterNode n2 = w.snapshot.get(n.url);
					if (n.diff(n2)) {
						dirty = true;
						break;
					}
				}
				if (dirty) {
					RegistryInfo cfg = ClusterNode.toTinyMap(nodes);
					w.lsnr.updateAllNodes(cfg.wNodes);
				}
			}
		}

	}

	static ClusterMonitor monitor;
	static {
		monitor = new ClusterMonitor();
		monitor.setDaemon(true);
		monitor.start();
	}

	synchronized public static CuratorClusterWorker createInstance(String conn) {
		CuratorClusterWorker ret = owner.get(conn);
		if (ret == null) {
			ZKServerRegistry zkSR = new ZKServerRegistry();
			ZKClientListener lsnr = new ZKClientListener();
			ret = new CuratorClusterWorker(zkSR, lsnr, conn);
			lsnr.setWorker(ret);
			owner.put(conn, ret);
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
		if (readOnly) {
			return false;
		}
		ClusterNode info = new ClusterNode();
		info.url = rpc.getServiceURL();
		info.protocol = rpc.getServiceProtocol();
		info.group = rpc.getGroup();
		info.online = rpc.online();
		info.up = rpc.getUptime().getTime();
		info.heartbeat = new Date().getTime();
		String ret0 = info.toDataString();

		String nodepath = ClusterNode.zkNodeBase + "/" + safePath(info.url);
		boolean ret = false;
		try {
			if (cur.checkExists().forPath(nodepath) != null) {
				cur.delete().forPath(nodepath);
			}
			cur.create().creatingParentsIfNeeded().forPath(nodepath, ret0.getBytes());
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public List<ClusterNode> syncRead() {
		List<ClusterNode> ret = new ArrayList<ClusterNode>();
		List<String> children = null;
		try {
			children = cur.getChildren().forPath(ClusterNode.zkNodeBase);
		} catch (Exception e) {
			children = Collections.emptyList();
		}
		for (String p : children) {
			try {
				byte[] buf = cur.getData().forPath(ClusterNode.zkNodeBase + "/" + p);
				ret.add(ClusterNode.fromBytes(buf));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public ClusterNode syncRead(String url) {
		List<ClusterNode> ret = syncRead();
		url = safePath(url);
		for (ClusterNode n : ret) {
			if (url.equals(safePath(n.url))) {
				return n;
			}
		}
		return null;
	}

	public ClusterMode syncGetMode() {
		ClusterMode ret = null;
		try {
			boolean r = cur.checkExists().forPath(ClusterNode.zkNodeBase) == null;
			if (!r) {
				byte[] buf = cur.getData().forPath(ClusterNode.zkNodeBase);
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
			boolean r = cur.checkExists().forPath(ClusterNode.zkNodeBase) == null;
			if (r) {
				cur.create().creatingParentsIfNeeded().forPath(ClusterNode.zkNodeBase, data.getBytes());
			} else {
				cur.setData().forPath(ClusterNode.zkNodeBase, data.getBytes());
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
			boolean r = cur.checkExists().forPath(ClusterNode.zkNodeBase) == null;
			if (!r) {
				cur.setData().forPath(ClusterNode.zkNodeBase, "".getBytes());
			}
			ret = true;
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

	public boolean syncClearRPCNode(RPCService rpc) {
		String nodepath = ClusterNode.zkNodeBase + "/" + safePath(rpc.getServiceURL());
		boolean ret = false;
		try {
			cur.delete().forPath(nodepath);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	protected List<ClusterNode> refreshRPCNodes() {
		LinkedHashMap<String, ClusterNode> newSnap = new LinkedHashMap<String, ClusterNode>();
		ArrayList<ClusterNode> ret = new ArrayList<ClusterNode>();

		boolean freezing = false;
		// 1. refresh Cluster Mode
		ClusterMode mode = syncGetMode();
		if (mode != null && mode.freeze != null && mode.freeze && mode.nodes != null) {
			// do nothing
			freezing = true;
		}
		
		// 2. refresh Nodes
		if (freezing){ // cluster freezing
			newSnap.clear();
		}else{ // freeze
			List<ClusterNode> nodes = syncRead();
			double total = 0;
			double nload = 0d;
			for (ClusterNode n : nodes) {
				if (n.isAlive()) {
					nload = n.load == null ? 1 : n.load;
					total += nload;
					newSnap.put(n.url, n);
					ret.add(n);
				}
			}			
		}
		
		snapshot = newSnap;


		List<ClusterNode> nodes = syncRead();
		boolean dirty = false;
		for (ClusterNode n : nodes) {
			ClusterNode n2 = snapshot.get(n.url);
			if (n.diff(n2)) {
				dirty = true;
				break;
			}
		}
		if (dirty) {
			RegistryInfo cfg = ClusterNode.toTinyMap(nodes);
			lsnr.updateAllNodes(cfg.wNodes);
		}
	}

	protected String safePath(String url) {
		if (url == null) {
			return null;
		}
		int pos = url.indexOf("://");
		if (pos > 0) {
			pos += 3;
			url = url.substring(pos);
		}
		return url.replace(':', '_');
	}

}
