package com.duitang.service.base;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.Transceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.codecs.NettyFrameDecoder;
import com.duitang.service.codecs.NettyFrameEncoder;

/**
 * A Netty-based {@link Transceiver} implementation.
 */
public class SmartNettyTransceiver extends Transceiver implements Closeable {
	/** If not specified, the default connection timeout will be used (60 sec). */
	public static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60 * 1000L;
	public static final String NETTY_CONNECT_TIMEOUT_OPTION = "connectTimeoutMillis";
	public static final String NETTY_TCP_NODELAY_OPTION = "tcpNoDelay";
	public static final String NETTY_KEEPALIVE_OPTION = "keepAlive";
	public static final boolean DEFAULT_TCP_NODELAY_VALUE = true;

	private static final Logger LOG = LoggerFactory.getLogger(SmartNettyTransceiver.class.getName());

	private final AtomicInteger serialGenerator = new AtomicInteger(0);
	private final Map<Integer, Callback<List<ByteBuffer>>> requests = new ConcurrentHashMap<Integer, Callback<List<ByteBuffer>>>();

	// private final ChannelFactory channelFactory;
	private final long connectTimeoutMillis;
	private final Bootstrap bootstrap;
	private final InetSocketAddress remoteAddr;

	volatile ChannelFuture channelFuture;
	volatile boolean stopping;
	private final Object channelFutureLock = new Object();

	/**
	 * Read lock must be acquired whenever using non-final state. Write lock
	 * must be acquired whenever modifying state.
	 */
	private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
	private Channel channel; // Synchronized on stateLock
	private Protocol remote; // Synchronized on stateLock

	SmartNettyTransceiver() {
		// channelFactory = null;
		connectTimeoutMillis = 0L;
		bootstrap = null;
		remoteAddr = null;
		channelFuture = null;
	}

	/**
	 * Creates a NettyTransceiver, and attempts to connect to the given address.
	 * {@link #DEFAULT_CONNECTION_TIMEOUT_MILLIS} is used for the connection
	 * timeout.
	 * 
	 * @param addr
	 *            the address to connect to.
	 * @throws IOException
	 *             if an error occurs connecting to the given address.
	 */
	public SmartNettyTransceiver(InetSocketAddress addr) throws IOException {
		this(addr, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
	}

	/**
	 * Creates a NettyTransceiver, and attempts to connect to the given address.
	 * 
	 * @param addr
	 *            the address to connect to.
	 * @param connectTimeoutMillis
	 *            maximum amount of time to wait for connection establishment in
	 *            milliseconds, or null to use
	 *            {@link #DEFAULT_CONNECTION_TIMEOUT_MILLIS}.
	 * @throws IOException
	 *             if an error occurs connecting to the given address.
	 */
	public SmartNettyTransceiver(InetSocketAddress addr, Long connectTimeoutMillis) throws IOException {
		this(addr, null, connectTimeoutMillis);
	}

	/**
	 * Creates a NettyTransceiver, and attempts to connect to the given address.
	 * {@link #DEFAULT_CONNECTION_TIMEOUT_MILLIS} is used for the connection
	 * timeout.
	 * 
	 * @param addr
	 *            the address to connect to.
	 * @param channelFactory
	 *            the factory to use to create a new Netty Channel.
	 * @throws IOException
	 *             if an error occurs connecting to the given address.
	 */
	public SmartNettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory) throws IOException {
		this(addr, channelFactory, buildDefaultBootstrapOptions(null));
	}

	/**
	 * Creates a NettyTransceiver, and attempts to connect to the given address.
	 * 
	 * @param addr
	 *            the address to connect to.
	 * @param channelFactory
	 *            the factory to use to create a new Netty Channel.
	 * @param connectTimeoutMillis
	 *            maximum amount of time to wait for connection establishment in
	 *            milliseconds, or null to use
	 *            {@link #DEFAULT_CONNECTION_TIMEOUT_MILLIS}.
	 * @throws IOException
	 *             if an error occurs connecting to the given address.
	 */
	public SmartNettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory, Long connectTimeoutMillis)
	        throws IOException {
		this(addr, channelFactory, buildDefaultBootstrapOptions(connectTimeoutMillis));
	}

	/**
	 * Creates a NettyTransceiver, and attempts to connect to the given address.
	 * It is strongly recommended that the {@link #NETTY_CONNECT_TIMEOUT_OPTION}
	 * option be set to a reasonable timeout value (a Long value in
	 * milliseconds) to prevent connect/disconnect attempts from hanging
	 * indefinitely. It is also recommended that the
	 * {@link #NETTY_TCP_NODELAY_OPTION} option be set to true to minimize RPC
	 * latency.
	 * 
	 * @param addr
	 *            the address to connect to.
	 * @param channelFactory
	 *            the factory to use to create a new Netty Channel.
	 * @param nettyClientBootstrapOptions
	 *            map of Netty ClientBootstrap options to use.
	 * @throws IOException
	 *             if an error occurs connecting to the given address.
	 */
	public SmartNettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory,
	        Map<String, Object> nettyClientBootstrapOptions) throws IOException {
		// if (channelFactory == null) {
		// throw new NullPointerException("channelFactory is null");
		// }

		// Set up.
		// this.channelFactory = channelFactory;
		this.connectTimeoutMillis = (Long) nettyClientBootstrapOptions.get(NETTY_CONNECT_TIMEOUT_OPTION);
		bootstrap = getBootstrap();
		remoteAddr = addr;

		// Make a new connection.
		stateLock.readLock().lock();
		try {
			getChannel();
		} finally {
			stateLock.readLock().unlock();
		}
	}

	/**
	 * Creates a Netty ChannelUpstreamHandler for handling events on the Netty
	 * client channel.
	 * 
	 * @return the ChannelUpstreamHandler to use.
	 */
	protected SimpleChannelInboundHandler<NettyDataPack> createNettyClientAvroHandler() {
		return new SmartNettyClientAvroHandler();
	}

	/**
	 * Creates the default options map for the Netty ClientBootstrap.
	 * 
	 * @param connectTimeoutMillis
	 *            connection timeout in milliseconds, or null if no timeout is
	 *            desired.
	 * @return the map of Netty bootstrap options.
	 */
	protected static Map<String, Object> buildDefaultBootstrapOptions(Long connectTimeoutMillis) {
		Map<String, Object> options = new HashMap<String, Object>(3);
		options.put(NETTY_TCP_NODELAY_OPTION, DEFAULT_TCP_NODELAY_VALUE);
		options.put(NETTY_KEEPALIVE_OPTION, true);
		options.put(NETTY_CONNECT_TIMEOUT_OPTION, connectTimeoutMillis == null ? DEFAULT_CONNECTION_TIMEOUT_MILLIS
		        : connectTimeoutMillis);
		return options;
	}

	/**
	 * Tests whether the given channel is ready for writing.
	 * 
	 * @return true if the channel is open and ready; false otherwise.
	 */
	private static boolean isChannelReady(Channel channel) {
		return (channel != null) && channel.isOpen() && channel.isRegistered() && channel.isActive();
	}

	/**
	 * Gets the Netty channel. If the channel is not connected, first attempts
	 * to connect. NOTE: The stateLock read lock *must* be acquired before
	 * calling this method.
	 * 
	 * @return the Netty channel
	 * @throws IOException
	 *             if an error occurs connecting the channel.
	 */
	private Channel getChannel() throws IOException {
		if (!isChannelReady(channel)) {
			// Need to reconnect
			// Upgrade to write lock
			stateLock.readLock().unlock();
			stateLock.writeLock().lock();
			try {
				if (!isChannelReady(channel)) {
					synchronized (channelFutureLock) {
						if (!stopping) {
							LOG.debug("Connecting to " + remoteAddr);
							channelFuture = bootstrap.connect(remoteAddr);
						}
					}
					if (channelFuture != null) {
						try {
							channelFuture.await(connectTimeoutMillis);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); // Reset
							                                    // interrupt
							                                    // flag
							throw new IOException("Interrupted while connecting to " + remoteAddr);
						}

						synchronized (channelFutureLock) {
							if (!channelFuture.isSuccess()) {
								throw new IOException("Error connecting to " + remoteAddr, channelFuture.cause());
							}
							channel = channelFuture.channel();
							channelFuture = null;
						}
					}
				}
			} finally {
				// Downgrade to read lock:
				stateLock.readLock().lock();
				stateLock.writeLock().unlock();
			}
		}
		return channel;
	}

	// /**
	// * Closes the connection to the remote peer if connected.
	// */
	// private void disconnect() {
	// disconnect(false, false, null);
	// }

	/**
	 * Closes the connection to the remote peer if connected.
	 * 
	 * @param awaitCompletion
	 *            if true, will block until the close has completed.
	 * @param cancelPendingRequests
	 *            if true, will drain the requests map and send an IOException
	 *            to all Callbacks.
	 * @param cause
	 *            if non-null and cancelPendingRequests is true, this Throwable
	 *            will be passed to all Callbacks.
	 */
	private void disconnect(boolean awaitCompletion, boolean cancelPendingRequests, Throwable cause) {
		Channel channelToClose = null;
		Map<Integer, Callback<List<ByteBuffer>>> requestsToCancel = null;
		boolean stateReadLockHeld = stateLock.getReadHoldCount() != 0;

		ChannelFuture channelFutureToCancel = null;
		synchronized (channelFutureLock) {
			if (stopping && channelFuture != null) {
				channelFutureToCancel = channelFuture;
				channelFuture = null;
			}
		}
		if (channelFutureToCancel != null) {
			channelFutureToCancel.cancel(true);
		}

		if (stateReadLockHeld) {
			stateLock.readLock().unlock();
		}
		stateLock.writeLock().lock();
		try {
			if (channel != null) {
				if (cause != null) {
					LOG.debug("Disconnecting from " + remoteAddr, cause);
				} else {
					LOG.debug("Disconnecting from " + remoteAddr);
				}
				channelToClose = channel;
				channel = null;
				remote = null;
				if (cancelPendingRequests) {
					// Remove all pending requests (will be canceled after
					// relinquishing
					// write lock).
					requestsToCancel = new ConcurrentHashMap<Integer, Callback<List<ByteBuffer>>>(requests);
					requests.clear();
				}
			}
		} finally {
			if (stateReadLockHeld) {
				stateLock.readLock().lock();
			}
			stateLock.writeLock().unlock();
		}

		// Cancel any pending requests by sending errors to the callbacks:
		if ((requestsToCancel != null) && !requestsToCancel.isEmpty()) {
			LOG.debug("Removing " + requestsToCancel.size() + " pending request(s).");
			for (Callback<List<ByteBuffer>> request : requestsToCancel.values()) {
				request.handleError(cause != null ? cause : new IOException(getClass().getSimpleName() + " closed"));
			}
		}

		// Close the channel:
		if (channelToClose != null) {
			ChannelFuture closeFuture = channelToClose.close();
			if (awaitCompletion && (closeFuture != null)) {
				try {
					closeFuture.await(connectTimeoutMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // Reset interrupt flag
					LOG.warn("Interrupted while disconnecting", e);
				}
			}
		}
	}

	/**
	 * Netty channels are thread-safe, so there is no need to acquire locks.
	 * This method is a no-op.
	 */
	@Override
	public void lockChannel() {

	}

	/**
	 * Netty channels are thread-safe, so there is no need to acquire locks.
	 * This method is a no-op.
	 */
	@Override
	public void unlockChannel() {

	}

	/**
	 * Closes this transceiver and disconnects from the remote peer. Cancels all
	 * pending RPCs, sends an IOException to all pending callbacks, and blocks
	 * until the close has completed.
	 */
	@Override
	public void close() {
		close(true);
	}

	/**
	 * Closes this transceiver and disconnects from the remote peer. Cancels all
	 * pending RPCs and sends an IOException to all pending callbacks.
	 * 
	 * @param awaitCompletion
	 *            if true, will block until the close has completed.
	 */
	public void close(boolean awaitCompletion) {
		try {
			// Close the connection:
			stopping = true;
			disconnect(awaitCompletion, true, null);
		} finally {
			// Shut down all thread pools to exit.
			// channelFactory.releaseExternalResources();
		}
	}

	@Override
	public String getRemoteName() throws IOException {
		stateLock.readLock().lock();
		try {
			return getChannel().remoteAddress().toString();
		} finally {
			stateLock.readLock().unlock();
		}
	}

	/**
	 * Override as non-synchronized method because the method is thread safe.
	 */
	@Override
	public List<ByteBuffer> transceive(List<ByteBuffer> request) throws IOException {
		try {
			CallFuture<List<ByteBuffer>> transceiverFuture = new CallFuture<List<ByteBuffer>>();
			transceive(request, transceiverFuture);
			return transceiverFuture.get();
		} catch (InterruptedException e) {
			LOG.debug("failed to get the response", e);
			return null;
		} catch (ExecutionException e) {
			LOG.debug("failed to get the response", e);
			return null;
		}
	}

	@Override
	public void transceive(List<ByteBuffer> request, Callback<List<ByteBuffer>> callback) throws IOException {
		stateLock.readLock().lock();
		try {
			int serial = serialGenerator.incrementAndGet();
			NettyDataPack dataPack = new NettyDataPack(serial, request);
			requests.put(serial, callback);
			writeDataPack(dataPack);
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Override
	public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		ChannelFuture writeFuture;
		stateLock.readLock().lock();
		try {
			writeFuture = writeDataPack(new NettyDataPack(serialGenerator.incrementAndGet(), buffers));
		} finally {
			stateLock.readLock().unlock();
		}

		if (!writeFuture.isDone()) {
			try {
				writeFuture.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Reset interrupt flag
				throw new IOException("Interrupted while writing Netty data pack", e);
			}
		}
		if (!writeFuture.isSuccess()) {
			throw new IOException("Error writing buffers", writeFuture.cause());
		}
	}

	/**
	 * Writes a NettyDataPack, reconnecting to the remote peer if necessary.
	 * NOTE: The stateLock read lock *must* be acquired before calling this
	 * method.
	 * 
	 * @param dataPack
	 *            the data pack to write.
	 * @return the Netty ChannelFuture for the write operation.
	 * @throws IOException
	 *             if an error occurs connecting to the remote peer.
	 */
	private ChannelFuture writeDataPack(NettyDataPack dataPack) throws IOException {
		// return getChannel().write(dataPack);
		return getChannel().writeAndFlush(dataPack);
	}

	@Override
	public List<ByteBuffer> readBuffers() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Protocol getRemote() {
		stateLock.readLock().lock();
		try {
			return remote;
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Override
	public boolean isConnected() {
		stateLock.readLock().lock();
		try {
			return remote != null;
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Override
	public void setRemote(Protocol protocol) {
		stateLock.writeLock().lock();
		try {
			this.remote = protocol;
		} finally {
			stateLock.writeLock().unlock();
		}
	}

	/**
	 * A ChannelFutureListener for channel write operations that notifies a
	 * {@link Callback} if an error occurs while writing to the channel.
	 */
	protected class WriteFutureListener implements ChannelFutureListener {
		protected final Callback<List<ByteBuffer>> callback;

		/**
		 * Creates a WriteFutureListener that notifies the given callback if an
		 * error occurs writing data to the channel.
		 * 
		 * @param callback
		 *            the callback to notify, or null to skip notification.
		 */
		public WriteFutureListener(Callback<List<ByteBuffer>> callback) {
			this.callback = callback;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess() && (callback != null)) {
				callback.handleError(new IOException("Error writing buffers", future.cause()));
			}
		}
	}

	protected final Bootstrap getBootstrap() {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class);
		b.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipe = ch.pipeline();
				// pipe.addLast("frameDecoder")
				pipe.addLast("decoder", new NettyFrameDecoder());
				pipe.addLast("encoder", new NettyFrameEncoder());
				pipe.addLast("handler", createNettyClientAvroHandler());
			}
		});
		b.option(ChannelOption.SO_KEEPALIVE, true);
		return b;
	}

	/**
	 * Avro client handler for the Netty transport
	 */
	protected class SmartNettyClientAvroHandler extends SimpleChannelInboundHandler<NettyDataPack> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, NettyDataPack msg) throws Exception {
			// disconnect(false, true, null);
			NettyDataPack dataPack = msg;
			Callback<List<ByteBuffer>> callback = requests.get(dataPack.getSerial());
			if (callback == null) {
				throw new RuntimeException("Missing previous call info");
			}
			try {
				callback.handleResult(dataPack.getDatas());
			} finally {
				requests.remove(dataPack.getSerial());
			}
		}

	}

}
