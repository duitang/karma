package com.duitang.service.karma.demo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class DemoObject implements Serializable, Comparable<DemoObject> {

	private static final long serialVersionUID = 1L;
	protected String domain;
	protected String method;
	protected int i_v;
	protected float f_v;
	protected boolean b_v;
	protected long l_v;
	protected byte[] bs_v;
	protected Map<String, String> m_v;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getI_v() {
		return i_v;
	}

	public void setI_v(int i_v) {
		this.i_v = i_v;
	}

	public float getF_v() {
		return f_v;
	}

	public void setF_v(float f_v) {
		this.f_v = f_v;
	}

	public boolean isB_v() {
		return b_v;
	}

	public void setB_v(boolean b_v) {
		this.b_v = b_v;
	}

	public long getL_v() {
		return l_v;
	}

	public void setL_v(long l_v) {
		this.l_v = l_v;
	}

	public byte[] getBs_v() {
		return bs_v;
	}

	public void setBs_v(byte[] bs_v) {
		this.bs_v = bs_v;
	}

	public Map<String, String> getM_v() {
		return m_v;
	}

	public void setM_v(Map<String, String> m_v) {
		this.m_v = m_v;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(domain).append("|");
		sb.append(method).append("|");
		sb.append(i_v).append("|");
		sb.append(f_v).append("|");
		sb.append(b_v).append("|");
		sb.append(l_v).append("|");
		sb.append(Arrays.toString(bs_v)).append("|");
		sb.append(m_v).append("|");
		return sb.toString();
	}

	@Override
	public int compareTo(DemoObject o) {
		if (o == null) {
			return -1;
		}
		return this.toString().compareTo(o.toString());
	}

}
