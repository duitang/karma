package com.duitang.service.karma.client;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class KarmaIORouterTest {

  @Test
  public void test() {
    List<String> lst = Arrays.asList(new String[]{"a", "b", "c", "d", "e", "f"});

    RoundRobinRouter router = new RoundRobinRouter(lst);
    for (int i = 0; i < lst.size() * 3 + 1; i++) {
      System.out.print(router.next(null));
    }
    System.out.println();
  }

}
