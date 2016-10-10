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

	protected CuratorClusterWorker worker;
	protected ConcurrentHashMap<String, RPCService> service = new ConcurrentHashMap<>();

	public void setWorker(CuratorClusterWorker worker) {
		this.worker = worker;
	}

	@Override
	public void register(RPCService rpc) {
		String conn = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		service.put(conn, rpc);
		worker.syncWrite(rpc);
	}

	@Override
	public void unregister(RPCService rpc) {
		String conn = RPCNodeHashing.getRawConnURL(rpc.getServiceURL());
		service.remove(conn, rpc);
		worker.syncClearRPCNode(rpc);
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
