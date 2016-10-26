/**
 * @author laurence
 * @since 2016年10月26日
 *
 */
package com.duitang.service.karma.demo.cluster;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * @author laurence
 * @since 2016年10月26日
 *
 */
public class TestArgs {

	@Option(name = "-threads", usage = "total working threads")
	int threadCount = 3;

	@Option(name = "-nodes", usage = "total RPC nodes")
	int nodesCount = 5;

	@Option(name = "-zk", usage = "zookeeper url")
	String zk = "localhost:2181";

	@Option(name = "-zipkin", usage = "zipkin url")
	String zipkin = null;

	@Argument
	List<String> arguments = new ArrayList<String>();

}
