/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.demo;

import java.io.IOException;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.KarmaClientTest;
import com.duitang.service.karma.server.TCPServerTest;
import com.duitang.service.karma.trace.Finder;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class WithZipkinEnabled {

	public static void main(String[] args) throws Exception {
//		Finder.enableZipkin(null, "console");
//		Finder.enableZipkin(null, "thrift://localhost:9410");
		Finder.enableConsole(true);
		Finder.enableZipkin(null, "http://192.168.10.216:9411");

		System.out.println("starting server .....");
		new Thread() {
			public void run() {
				TCPServerTest tcpServer = new TCPServerTest();
				tcpServer.setUp();
				try {
					tcpServer.test1();
				} catch (KarmaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("started server .....");
			}
		}.start();

		Thread.sleep(5000);

		new Thread() {
			public void run() {
				System.out.println("starting client .....");
				KarmaClientTest t = new KarmaClientTest();
				t.setUp();
				try {
					t.test();
				} catch (KarmaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("started client .....");
			}
		}.start();
		Thread.sleep(5000);

		System.out.println("bye!");
	}

}
