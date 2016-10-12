/**
 * @author laurence
 * @since 2016年10月8日
 *
 */
package com.duitang.service.karma.support;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.PeriodCountCPBalancer;
import com.duitang.service.karma.client.impl.TraceableBalancerFactory;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;

/**
 * <pre>
 * the registry object for cluster aware support
 * </pre>
 * 
 * @author laurence
 * @since 2016年10月8日
 *
 */
public class RPCRegistry {

	final ConcurrentLinkedQueue<AsyncRegistryReader> readers = new ConcurrentLinkedQueue<>();
	final ConcurrentLinkedQueue<AsyncRegistryWriter> writers = new ConcurrentLinkedQueue<>();
	final ConcurrentHashMap<String, String> bootstrapURLs = new ConcurrentHashMap<>();

	// change to map if upgrade to connection->IOBalanceFactory
	protected IOBalanceFactory fac = new TraceableBalancerFactory(PeriodCountCPBalancer.PERIOD,
			PeriodCountCPBalancer.COUNT, false);

	public IOBalanceFactory getFactory() {
		return fac;
	}

	public void setFactory(IOBalanceFactory fac) {
		this.fac = fac;
	}

	public void registerWrite(RPCService rpc) throws KarmaException {
		for (AsyncRegistryWriter w : writers) {
			w.register(rpc);
		}
	}

	public void unRegisterWrite(RPCService rpc) throws KarmaException {
		for (AsyncRegistryWriter w : writers) {
			w.unregister(rpc);
		}
	}

	public void registerRead(IOBalance balancer) throws KarmaException {
		for (AsyncRegistryReader r : readers) {
			r.register(balancer);
		}
	}

	public void unRegisterRead(IOBalance balancer) throws KarmaException {
		for (AsyncRegistryReader r : readers) {
			r.unregister(balancer);
		}
	}

	public void addWriters(List<AsyncRegistryWriter> w) {
		writers.addAll(w);
	}

	public void addReaders(List<AsyncRegistryReader> r) {
		readers.addAll(r);
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Readers: [");
		for (AsyncRegistryReader r : readers) {
			sb.append(r.getClass().getName()).append(", ");
		}
		sb.append("]");
		sb.append("Writers: [");
		for (AsyncRegistryWriter w : writers) {
			sb.append(w.getClass().getName()).append(", ");
		}
		sb.append("]");
		sb.append("Bootstrap URLs: [");
		for (Entry<String, String> en : bootstrapURLs.entrySet()) {
			sb.append(en.getKey()).append(" --> ").append(en.getValue());
		}
		sb.append("]");
		return sb.toString();
	}

}
