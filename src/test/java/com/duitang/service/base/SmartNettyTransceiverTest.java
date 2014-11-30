package com.duitang.service.base;

import java.net.InetSocketAddress;
import java.util.Date;

import junit.framework.Assert;
import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Opcodes;
import net.sf.cglib.proxy.Mixin;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SmartNettyTransceiverTest {

	protected SmartNettyTransceiver tr;

	protected Server server;
	protected Echo client;

	protected ServerBootstrap karamServer;
	protected StringBuilder sb;

	@Before
	public void setUp() throws Exception {
		sb = new StringBuilder();
		for (int i = 0; i < 40000; i++) {
			sb.append("+");
		}

//		server = new NettyServer(new ReflectResponder(Echo.class, new EchoService()), new InetSocketAddress(11222));
//		server.start();
//		tr = new SmartNettyTransceiver(new InetSocketAddress("localhost", 11222));
//		client = ReflectRequestor.getClient(Echo.class, tr);
//		Assert.assertNotNull(client.echo("......................initialization......................" + sb.toString()));
//		System.out.println("StartUp finished!!");
		// System.exit(1);
	}

	@After
	public void tearDown() throws Exception {
		server.close();
	}

	protected static Class genInterface(final String destName, Class[] clz) {
		final String[] interfaces = new String[clz.length];
		int i = 0;
		for (Class<?> interfac : clz) {
			interfaces[i] = interfac.getName().replace('.', '/');
			i++;
		}
		Class<?> klass = new ClassLoader(ServerBootstrap.class.getClassLoader()) {
			public Class<?> defineClass() {
				ClassWriter cw = new ClassWriter(0);
				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,
				        destName.replaceAll("\\.", "/"), null, "java/lang/Object", interfaces);
				cw.visitEnd();
				byte[] bytes = cw.toByteArray();
				return defineClass(destName.replaceAll("/", "\\."), bytes, 0, bytes.length);
			}
		}.defineClass();
		return klass;
	}

	// @Test
	public void testClientWithSetupServer() {
		ClientFactory<Echo> fac = ClientFactory.createFactory(Echo.class);
		fac.setUrl("netty://localhost:11222");
		fac.init();
		Echo cli = fac.create();
		for (int i = 0; i < 10000; i++) {
			System.out.println(cli.echo("****"));
		}
	}

	// @Test
	public void testServerAndClientNoSetupDependency() throws Exception {
		String SERVICE_INTERFACE = ServerBootstrap.class.getPackage().getName() + ".FacadeServiceImpl";
		System.out.println("using service type ...... " + SERVICE_INTERFACE);
		Class clztype = genInterface(SERVICE_INTERFACE, new Class[] { Echo.class, Echo2.class });
		Object servicetype = Mixin.create(new Class[] { Echo.class, Echo2.class }, new Object[] { new EchoImpl1(),
		        new EchoImpl2() });

		System.out.println(((Echo) (servicetype)).echo("|||"));
		System.out.println(((Echo2) (servicetype)).echo2("|++|"));

		Server server = new NettyServer(new ReflectResponder(clztype, servicetype), new InetSocketAddress(22222));
		server.start();

//		SmartNettyTransceiver tr1 = new SmartNettyTransceiver(new InetSocketAddress("localhost", 22222));
//		SmartNettyTransceiver tr2 = new SmartNettyTransceiver(new InetSocketAddress("localhost", 22222));
//		Echo client = ReflectRequestor.getClient(Echo.class, tr1);
//		Echo2 client2 = ReflectRequestor.getClient(Echo2.class, tr2);

		System.out.println(client.echo("---------"));
//		System.out.println(client2.echo2("+-------+"));
	}

	// @Test
	public void testMiniCodeServerAndClientWithSetup() {
		tt(1000);
	}

	@Test
	public void testKarmaServerClient() throws Exception {
		karamServer = new ServerBootstrap();
		karamServer.addService(Echo.class, new EchoImpl1());
		karamServer.addService(Echo2.class, new EchoImpl2());
		karamServer.startUp(9991, "netty");

		ClientFactory<Echo> fac = ClientFactory.createFactory(Echo.class);
		ClientFactory<Echo2> fac2 = ClientFactory.createFactory(Echo2.class);
		fac.setUrl("netty://localhost:9991");
		fac2.setUrl("netty://localhost:9991");
		fac.init();
		fac2.init();

		for (int i = 0; i < 10; i++) {
			Echo cli = fac.create();
			Echo2 cli2 = fac2.create();
			System.out.println(cli.echo("****" + i + "****" + sb.toString()));
			System.out.println(cli2.echo2("----" + i + "----" + sb.toString()));
			fac.release(cli);
			fac2.release(cli2);
		}

		Thread.sleep(5000);
		karamServer.shutdown();
	}

	// @Test
	public void testKarmaServerWithMT() throws Exception {
		karamServer = new ServerBootstrap();
		karamServer.addService(Echo.class, new EchoImpl1());
		karamServer.addService(Echo2.class, new EchoImpl2());
		karamServer.startUp(9991, "netty");

		Thread[] t = new Thread[5];
		final ClientFactory<Echo> fac = ClientFactory.createFactory(Echo.class);
		fac.setUrl("netty://localhost:9991");
		fac.init();

		for (int i = 0; i < t.length; i++) {
			t[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int j = 0; j < 1000; j++) {
						Echo cli = fac.create();
						cli.echo(Thread.currentThread().getName() + " -----> " + j);
						fac.release(cli);
					}
				}

			});
			t[i].setDaemon(true);
			t[i].start();
		}

		for (int i = 0; i < t.length; i++) {
			t[i].join();
		}

		karamServer.shutdown();
	}

	protected void tt(int loop) {
		for (int i = 0; i < loop; i++) {
			String msg = client.echo("aaaa" + i);
			System.out.println(msg);
		}
	}

	static String getStr(char ch, int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(ch);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 40000; i++) {
			sb.append("+");
		}

		MyNettyServer server = new MyNettyServer(new ReflectResponder(Echo.class, new EchoService()),
		        new InetSocketAddress(11222));
		server.start();
		// NettyTransceiver tr = new NettyTransceiver(new
		// InetSocketAddress("localhost", 11222));
//		SmartNettyTransceiver tr = new SmartNettyTransceiver(new InetSocketAddress("localhost", 11222));
//		Echo client = ReflectRequestor.getClient(Echo.class, tr);
//		Assert.assertNotNull(client.echo("......................initialization......................" + sb.toString()));
//		System.out.println("StartUp finished!!");
//		System.err.close();
//		long ts = System.currentTimeMillis();
//		for(int i=0; i<10000; i++){
//			client.echo(sb.toString());
//		}
//		ts = System.currentTimeMillis() - ts;
//		System.out.println("time elapsed: " + ts + "ms");
	}

}

class EchoService implements Echo, Echo2 {

	@Override
	public String echo(String msg) {
		String ret = msg + "@" + new Date();
//		System.err.println(ret);
		return ret;
	}

	@Override
	public String echo2(String msg) {
		String ret = msg + "@@" + new Date();
//		System.err.println(ret);
		return ret;
	}

}

class EchoImpl1 implements Echo {

	@Override
	public String echo(String msg) {
		String ret = msg + "@" + new Date();
//		System.err.println(ret);
		return ret;
	}

}

class EchoImpl2 implements Echo2 {

	@Override
	public String echo2(String msg) {
		String ret = msg + "@@" + new Date();
//		System.err.println(ret);
		return ret;
	}

}
