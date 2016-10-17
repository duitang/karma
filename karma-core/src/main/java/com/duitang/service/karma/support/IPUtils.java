package com.duitang.service.karma.support;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;

public class IPUtils {

	final public static String localhost = "127.0";

	protected static String[] divide(String url) {
		String[] ret = new String[3];
		if (url == null) {
			return ret;
		}
		if (url.contains("://")) {
			int p = url.indexOf("://");
			ret[0] = url.substring(0, p);
			url = url.substring(p + 3);
		}
		String[] r = StringUtils.split(url, ':');
		ret[1] = r[0];
		if (r.length > 1) {
			ret[2] = r[1];
		}
		return ret;
	}

	public static String getSchema(String url) {
		return divide(url)[0];
	}

	public static String getHost(String url) {
		return divide(url)[1];
	}

	public static Integer getPort(String url) {
		try {
			return Integer.valueOf(divide(url)[2]);
		} catch (Throwable t) {
			return null;
		}
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
					if (!(inetAddress instanceof Inet4Address)) {
						continue;
					}
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
					if (!(inetAddress instanceof Inet4Address)) {
						continue;
					}
					return inetAddress.getHostAddress();
				}
			}
		}
		return Inet4Address.getLocalHost().getHostAddress();
	}

	private IPUtils() {
		// disable
	}

}
