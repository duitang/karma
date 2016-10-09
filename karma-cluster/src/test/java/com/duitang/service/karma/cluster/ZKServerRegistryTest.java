package com.duitang.service.karma.cluster;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;

public class ZKServerRegistryTest {

	ZKServerRegistry zkRS;
	WorkerMocker worker;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReg() {
		MockerRPC rpc = new MockerRPC();
		rpc.url = "tcp://192.168.10.111:22334";
		Set<RPCService> sv = null;
		zkRS = new ZKServerRegistry();
		worker = new WorkerMocker(null, null, "");
		zkRS.setWorker(worker);

		sv = zkRS.getRegServices();
		Assert.assertTrue(sv.size() == 0);

		zkRS.register(rpc);
		Assert.assertTrue(rpc == worker.writeQueue.get(0));
		sv = zkRS.getRegServices();
		Assert.assertTrue(sv.size() == 1);

		zkRS.syncPush(rpc);
		Assert.assertTrue(worker.writeQueue.size() == 2);
		Assert.assertTrue(rpc == worker.writeQueue.get(1));
		sv = zkRS.getRegServices();
		Assert.assertTrue(sv.size() == 1);

		zkRS.unregister(rpc);
		Assert.assertTrue(rpc == worker.clearQueue.get(0));
		sv = zkRS.getRegServices();
		Assert.assertTrue(sv.size() == 0);

		zkRS.syncPush(rpc);
		Assert.assertTrue(worker.writeQueue.size() == 2);

	}

}

class WorkerMocker extends CuratorClusterWorker {

	List<RPCService> writeQueue = new ArrayList<>();
	List<RPCService> clearQueue = new ArrayList<>();

	WorkerMocker(ZKServerRegistry zkSR, ZKClientListener lsnr, String conn) {
		super(zkSR, lsnr, conn);
	}

	@Override
	public boolean syncWrite(RPCService rpc) {
		return writeQueue.add(rpc);
	}

	@Override
	public boolean syncClear(RPCService rpc) {
		return clearQueue.add(rpc);
	}

}

class MockerRPC implements RPCService {

	String url;

	@Override
	public void start() throws KarmaException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRouter(Router router) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroup(String grp) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getUptime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceURL() {
		return url;
	}

	@Override
	public String getServiceProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean online() {
		// TODO Auto-generated method stub
		return false;
	}

}