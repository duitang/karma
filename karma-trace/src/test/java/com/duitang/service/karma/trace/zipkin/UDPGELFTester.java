/**
 * @author laurence
 * @since 2016年9月30日
 *
 */
package com.duitang.service.karma.trace.zipkin;

import java.net.InetSocketAddress;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

/**
 * 
 * @see <a href="https://github.com/Graylog2/gelfclient">gelfclient using
 *      UDP</a>
 * @author laurence
 * @since 2016年9月30日
 *
 */
public class UDPGELFTester {

	public static void main(String[] args) throws Throwable {
		final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress("61.152.115.82", 30011))
				.transport(GelfTransports.UDP).queueSize(51200).connectTimeout(5000).reconnectDelay(1000).tcpNoDelay(true)
				.sendBufferSize(3276800);

		final GelfTransport transport = GelfTransports.create(config);
		final GelfMessageBuilder builder = new GelfMessageBuilder("", "cwjhome2").level(GelfMessageLevel.INFO)
				.additionalField("foo", "bar");

		boolean blocking = true;
		for (int i = 0; i < 2; i++) {
			final GelfMessage message = builder.message("This is message #" + i).additionalField("_count", i).build();

			if (blocking) {
				// Blocks until there is capacity in the queue
				transport.send(message);
			} else {
				// Returns false if there isn't enough room in the queue
				boolean enqueued = transport.trySend(message);
				System.out.println("try send: " + enqueued);
			}
		}
		
		Thread.sleep(3000);
	}

}
