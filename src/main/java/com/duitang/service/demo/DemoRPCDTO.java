package com.duitang.service.demo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DemoRPCDTO implements Serializable {

	private static final long serialVersionUID = 1L;

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

}
