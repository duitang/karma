package com.duitang.service.misc.domain;

public class MockComplex {

	protected float primary;
	protected double slave;

	public float getPrimary() {
		return primary;
	}

	public void setPrimary(float primary) {
		this.primary = primary;
	}

	public double getSlave() {
		return slave;
	}

	public void setSlave(double slave) {
		this.slave = slave;
	}

	@Override
	public String toString() {
		return primary + "," + slave;
	}

}
