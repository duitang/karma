/**
 * @author laurence
 * @since 2016年10月18日
 *
 */
package com.duitang.service.karma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

/**
 * @author laurence
 * @since 2016年10月18日
 *
 */
public class ZKEmbed {

	final static int port = 2181;
	final static String logdir = "/tmp/zk";

	final static ZooKeeperServerMain zooKeeperServer = new ZooKeeperServerMain();
	static ZKEmbed server = null;
	static ServerConfig cfg = null;

	synchronized public static void start() throws Exception {
		if (server != null) {
			return;
		}
		server = new ZKEmbed();
		cfg = new ServerConfig();
		cfg.readFrom(server.getQuorumPeerConfig(port, logdir));
		rmr(logdir);
		new File(logdir).mkdirs();

		Thread t = new Thread() {
			public void run() {
				try {
					// hang here
					zooKeeperServer.runFromConfig(cfg);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
		};
		t.setDaemon(true);
		t.start();
		Thread.sleep(300);
		System.err.println("ZooKeeper startup!");
	}

	static void rmr(String dir) throws Exception {
		Path directory = Paths.get(dir);
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private QuorumPeerConfig getQuorumPeerConfig(int clientPort, String logdir) throws Exception {
		QuorumPeerConfig config = new QuorumPeerConfig();
		Properties properties = new Properties();
		properties.setProperty("clientPort", Integer.valueOf(clientPort).toString());
		properties.setProperty("dataDir", logdir + "/server-0");
		properties.setProperty("dataLogDir", logdir + "/logs-0");
		properties.setProperty("tickTime", "2000");
		properties.setProperty("initLimit", "10");
		properties.setProperty("syncLimit", "5");
		properties.setProperty("electionAlg", "3");
		properties.setProperty("maxClientCnxns", "0");
		createDirs(logdir + "/server-0", logdir + "/logs-0", 0);
		try {
			config.parseProperties(properties);
		} catch (ConfigException e) {
			throw new IOException(e);
		}
		return config;
	}

	protected void createDirs(String dataDir, String logDir, int myid) throws IOException {
		// Create the data directory
		File datadir = new File(dataDir);
		if (datadir.exists()) {
			if (!datadir.isDirectory()) {
				throw new IOException("Datadir " + datadir + " exists and is not a directory");
			}
		} else {
			if (!datadir.mkdirs()) {
				throw new IOException("Unable to create datadir " + datadir);
			}
		}

		File logdir = new File(logDir);
		if (logdir.exists()) {
			if (!logdir.isDirectory()) {
				throw new IOException("Logdir " + logdir + " exists and is not a directory");
			}
		} else {
			if (!logdir.mkdirs()) {
				throw new IOException("Unable to create logdir " + logdir);
			}
		}

		// Create the id file
		File idfile = new File(dataDir + "/myid");
		if (idfile.exists()) {
			if (!idfile.delete()) {
				throw new IOException("Couldn't delete idfile " + idfile);
			}
		}

		// create does not list filename if operations fails due to permissions
		boolean createSuccess = false;
		try {
			createSuccess = idfile.createNewFile();
		} catch (IOException e) {
			throw new IOException("Do not have permissions to create idfile " + idfile);
		}
		if (!createSuccess) {
			throw new IOException("Couldn't create idfile " + idfile);
		}
		FileWriter fw = new FileWriter(idfile);
		fw.write(Integer.valueOf(myid).toString() + "\n");
		fw.close();
	}

}
