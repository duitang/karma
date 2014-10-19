package com.duitang.service.misc.domain;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.avro.reflect.Nullable;

public class MockObject {

	protected long id;
	protected String name;
	protected int seq;
	protected Date created;
	protected float score;
	protected double p;
	protected boolean active;
	protected byte[] src;

	@Nullable
	protected MockComplex some;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public byte[] getSrc() {
		return src;
	}

	public void setSrc(byte[] src) {
		this.src = src;
	}

	public MockComplex getSome() {
		return some;
	}

	public void setSome(MockComplex some) {
		this.some = some;
	}

	@Override
	public String toString() {
		List<String> lst = new ArrayList<String>();
		for (Field f : this.getClass().getDeclaredFields()) {
			try {
				Object val = null;
				if (f.getType().isArray()) {
					val = f.get(this);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < Array.getLength(val); i++) {
						sb.append(Array.get(val, i));
					}
					val = sb.toString();
				} else {
					val = f.get(this);
				}
				lst.add(f.getName() + ":" + val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(lst);
		return lst.toString();
	}

}
