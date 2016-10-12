package com.duitang.service.karma.support;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;

class SystemIPAddress {
	public static void main(String[] arguments) {
		String systemipaddress = "";
		try {
			URL url_name = new URL("http://bot.whatismyipaddress.com");
			BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
			systemipaddress = sc.readLine().trim();
			if (!(systemipaddress.length() > 0)) {
				try {
					InetAddress localhost = InetAddress.getLocalHost();
					System.out.println((localhost.getHostAddress()).trim());
					systemipaddress = (localhost.getHostAddress()).trim();
				} catch (Exception e1) {
					systemipaddress = "Cannot Execute Properly";
				}
			}
		} catch (Exception e2) {
			systemipaddress = "Cannot Execute Properly";
		}
		System.out.println("\nYour IP Address: " + systemipaddress + "\n");
	}
}
