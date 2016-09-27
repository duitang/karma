package com.duitang.service.karma.support;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;

public class IPUtils {

	public static String localhost = "127.0";

	public static String getHost(String url) {
		return StringUtils.split(url, ':')[0];
	}

	public static Integer getPort(String url) {
		String[] ret = StringUtils.split(url, ':');
		if (ret.length > 1) {
			return Integer.valueOf(ret[1]);
		}
		return null;
	}

	public static int getIPAsInt() {
		try {
			return getIp(pickUpInetAddressNot(localhost));
		} catch (IOException e) {
		}
		return 0;
	}

	public static String getIPAsString() {
		try {
			return pickUpIpNot(localhost);
		} catch (IOException e) {
		}
		return "127.0.0.1";
	}

	public static int getIp(InetAddress ip) {
		int ret = 0;
		for (byte b : ip.getAddress()) {
			ret = ret << 8 | (b & 0xFF);
		}
		return ret;
	}

	public static String pickUpIp(String prefix) throws IOException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if (inetAddress.getHostAddress().startsWith(prefix)) {
					return inetAddress.getHostAddress();
				}
			}
		}
		return Inet4Address.getLocalHost().getHostAddress();
	}

	public static InetAddress pickUpInetAddress(String prefix) throws IOException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if (inetAddress.getHostAddress().startsWith(prefix)) {
					return InetAddress.getByName(inetAddress.getHostName());
				}
			}
		}
		return InetAddress.getByName(Inet4Address.getLocalHost().getHostName());
	}

	public static InetAddress pickUpInetAddressNot(String prefix) throws IOException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if (!inetAddress.getHostAddress().startsWith(prefix)) {
					return InetAddress.getByName(inetAddress.getHostName());
				}
			}
		}
		return InetAddress.getByName(Inet4Address.getLocalHost().getHostName());
	}

	public static String pickUpIpNot(String prefix) throws IOException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if (!inetAddress.getHostAddress().startsWith(prefix)) {
					return inetAddress.getHostAddress();
				}
			}
		}
		return Inet4Address.getLocalHost().getHostAddress();
	}

}
