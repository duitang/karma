/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
package com.duitang.service.karma.cluster;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.impl.PeriodCountCPBalancer;
import com.duitang.service.karma.client.impl.TraceableBalancer;
import com.duitang.service.karma.client.impl.TraceableBalancerFactory;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.trace.TracePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author laurence
 * @since 2016年10月11日
 *
 */
public class FinderTest {

	@Before
	public void setUp() {

	}

	@After
	public void destrory() {

	}

	@Test
	public void test1() throws KarmaException, Exception {
		Finder.enableZKRegistry("192.168.1.180:2181", Arrays.asList("localhost:8899"));
		// Finder.enableZKRegistry("192.168.10.216:2181");
		RPCRegistry rg = Finder.getRegistry();

		MyDummy rpc = new MyDummy();
		rg.registerWrite(rpc);

		RPCNodeHashing hashing = RPCNodeHashing
				.createFromString(Arrays.asList("aaa:111", "bbb:222", "ccc:333", "ddd:444", "eee:555"));
		TraceableBalancerFactory fac = new TraceableBalancerFactory(5000, 0, false);
		PeriodCountCPBalancer bl = (PeriodCountCPBalancer) fac.createIOBalance(rg, hashing);

		System.out.println(rg.getInfo());

//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("now Service online: " + rpc.online);
			System.out.println(bl.getDebugInfo());

			String s = null;
			for (int i = 0; i < 50; i++) {
				s = bl.next(s);
				TracePoint tp = new TracePoint();
				Thread.sleep(Double.valueOf(10 * Math.random()).longValue() + 1);
				tp.close();
				bl.traceFeed(s, tp.getCell());
			}

			// printDebugInfo(bl);
			System.out.println("press enter key to change online mode ......");
			// String line = br.readLine();
			// if (line != null) {
			// boolean online = line.toLowerCase().startsWith("y");
			rpc.online = !rpc.online;
			// }

			Thread.sleep(1000);
		}

	}

	static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	static void printDebugInfo(PeriodCountCPBalancer balancer) throws Exception {
		Field f = TraceableBalancer.class.getDeclaredField("nap");
		f.setAccessible(true);
		Object nap = f.get(balancer);
		String ret = mapper.writeValueAsString(nap);
		System.out.println(ret);
	}

}

class MyDummy implements RPCService {

	Date created = new Date();
	String grp;
	boolean online = true;

	@Override
	public void start() throws KarmaException {

	}

	@Override
	public void stop() {

	}

	@Override
	public void setRouter(Router router) {

	}

	@Override
	public void setGroup(String grp) {

	}

	@Override
	public Date getUptime() {
		return created;
	}

	@Override
	public String getGroup() {
		return grp;
	}

	@Override
	public String getServiceURL() {
		return "192.168.10.110:8899";
	}

	@Override
	public String getServiceProtocol() {
		return "tcp";
	}

	@Override
	public boolean online() {
		return online;
	}

}
