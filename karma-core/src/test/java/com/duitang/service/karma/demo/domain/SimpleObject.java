package com.duitang.service.karma.demo.domain;

import java.util.List;
import java.util.Map;

public class SimpleObject {

  protected String a;
  protected List<Float> b;
  protected Map<String, Double> c;

  public String getA() {
    return a;
  }

  public void setA(String a) {
    this.a = a;
  }

  public List<Float> getB() {
    return b;
  }

  public void setB(List<Float> b) {
    this.b = b;
  }

  public Map<String, Double> getC() {
    return c;
  }

  public void setC(Map<String, Double> c) {
    this.c = c;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(a).append("|").append(b).append("|").append(c);
    return sb.toString();
  }

}
