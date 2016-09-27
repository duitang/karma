package com.duitang.service.karma.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public class RuleTest {

  static ObjectMapper mapper = new ObjectMapper();

  @Test
  public void test2() {
    ArrayList<String> ss = new ArrayList<String>();
    System.out.println(ss.getClass().getTypeParameters()[0]);
    Class persistentClass = (Class) ((ParameterizedType) ss.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    System.out.println(persistentClass.getName());
    ParameterizedType tp = (ParameterizedType) ss.getClass().getGenericSuperclass();
    System.out.println(tp.getActualTypeArguments()[0]);
  }

}
