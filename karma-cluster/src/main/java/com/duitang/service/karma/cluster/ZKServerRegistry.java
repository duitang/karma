/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNodeHashing;

/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
public class ZKServerRegistry implements AsyncRegistryWriter {

	protected ZKClusterWorker worker;
	protected ConcurrentHashMap<String, RPCService> service = new ConcurrentHashMap<>();

	class AsyncReg implements Runnable {

		RPCService rpc;

		AsyncReg(RPCService rpc) {
			this.rpc = rpc;
		}

		@Override
		public void run() {
			while (!worker.syncWrite(rpc)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void setWorker(ZKClusterWorker worker) {
		this.worker = worker;
	}

	@Override
	public void register(RPCService rpc) {
		String conn = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		service.put(conn, rpc);
		if (!worker.syncWrite(rpc)) {
			Thread t = new Thread(new AsyncReg(rpc));
			t.setDaemon(true);
			t.setName("async-register-" + rpc.getClass().getName());
			t.start();
		} // later using async mode
	}

	@Override
	public void unregister(RPCService rpc) {
		String conn = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		service.remove(conn, rpc);
		worker.syncClearRPCNode(rpc); // later using async mode
	}

	@Override
	public void syncPush(RPCService rpc) {
		String conn = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		if (service.containsKey(conn)) {
			worker.syncWrite(rpc);
		}
	}

	public Set<RPCService> getRegServices() {
		return new HashSet<RPCService>(service.values());
	}

}
