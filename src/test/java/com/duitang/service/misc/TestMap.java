package com.duitang.service.misc;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.base.ServerBootstrap;
import com.duitang.service.misc.domain.Tiny;

public class TestMap {

	@Test
	public void testmaptype() throws IOException {
		ServerBootstrap boot = new ServerBootstrap();
		boot.addService(E.class, new EImpl());
		boot.startUp(9999, "netty");

		ClientFactory<E> fac = ClientFactory.createFactory(E.class);
		fac.setUrl("netty://localhost:9999");
		E cli = fac.create();
		Tiny oo = cli.get();
		System.out.println(oo.getData());
		System.out.println(oo.getSs());
		
		Map<String, Integer> ddd = cli.getMap();
		System.out.println(ddd);
		
		fac.release(cli);

		boot.shutdown();
	}
}

class EImpl implements E {

	@Override
	public Tiny get() {
		Tiny ret = new Tiny();
		HashMap data = new HashMap();
		data.put("aaa", "bbb");
		data.put("bbb", 1);
		data.put("ccc", new Date());
		ret.setData(data);

		HashSet ss = new HashSet();
		ss.add("aaa");
		ss.add(22);
		ss.add(new Date());
		ret.setSs(ss);
		return ret;
	}

	@Override
	public Map<String, Integer> getMap() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("aaa", 333);
		return ret;
	}

}
