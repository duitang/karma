package com.duitang.service.karma;

public class TestingHosts {

	// final public static String zk = "192.168.10.216:2181";
	// final public static String zk = "192.168.1.180:2181";
	final public static String zk = getZK();

	synchronized static String getZK() {
		try {
			ZKEmbed.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "127.0.0.1:2181";
	}

	public static void main(String[] args) throws Exception {
		System.out.println("1111");
	}

}
