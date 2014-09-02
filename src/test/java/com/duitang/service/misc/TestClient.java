package com.duitang.service.misc;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.AvroRemoteException;

import com.duitang.service.l2.L2Service;
import com.duitang.service.l2.L2ServiceFactory;

public class TestClient {

	public static void main(String[] args) {
		test();
	}

	static void test() {
		int loop = 1000;
		L2ServiceFactory fac = new L2ServiceFactory();
		fac.setTimeout(5000);
		fac.setUrl("http://localhost:9090");
		String sss = "v4:napi:gandalf-2.3.3-0:574b5a175628dc7819fc52f3f24d06b7";
		Set<Integer> sz = new HashSet<Integer>();
		long ts = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			L2Service cli = fac.create();
			try {
				String r = cli.cat_getstring(sss);
				sz.add(r == null ? 0 : r.length());
			} catch (AvroRemoteException e) {
				e.printStackTrace();
			} finally {
				try {
					fac.release(cli);
				} catch (Exception e) {
				}
			}
		}
		ts = System.currentTimeMillis() - ts;
		System.out.println("total elapsed: " + ts + "ms with [" + loop + "]");
		System.out.println("total sizes: " + sz);
	}

}
