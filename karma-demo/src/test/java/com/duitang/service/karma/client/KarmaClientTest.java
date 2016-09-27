package com.duitang.service.karma.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.demo.IDemoService;

public class KarmaClientTest {

  @Test
  public void test() throws KarmaException, IOException, Exception {
//    Logger.getLogger(KarmaClient.class).setLevel(Level.DEBUG);
    List<String> urls = Arrays.asList(new String[]{"localhost:9999"});
    KarmaClient<IDemoService> cli = KarmaClient.createKarmaClient(IDemoService.class, urls, "dev1");
    IDemoService client = cli.getService();
    System.out.println(client.memory_getString("aaaa"));
    System.out.println(client.trace_msg("laurence", 200));
    System.out.println(client.noparam());
    System.out.println(client.getM(new HashSet()));
    try {
      System.out.println(client.trace_msg("laurence", 600));
    } catch (Throwable e) {
      e.printStackTrace();
    }
    try {
      client.getError();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    System.out.println(client.memory_setBytes("aaa", "fuck".getBytes(), 5000));
    System.out.println(new String(client.memory_getBytes("aaa")));

    // Thread.sleep(100000);
  }

}
