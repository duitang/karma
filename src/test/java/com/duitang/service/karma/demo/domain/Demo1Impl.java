package com.duitang.service.karma.demo.domain;

import java.util.List;

public class Demo1Impl implements Demo1 {

	@Override
	public int m_a1(int p1, List<Integer> p2) {
		return p1 + p2.get(0) + p2.get(1);
	}

	@Override
	public int[] m_a2(Integer p1, List<Integer> p2) {
		return new int[] { p1 + p2.get(0), p1 + p2.get(1) };
	}

	@Override
	public boolean m_b1(boolean p1, List<Boolean> p2) {
		return p1 || (p2.get(0) && p2.get(1));
	}

	@Override
	public boolean[] m_b2(Boolean p1, List<Boolean> p2) {
		return new boolean[] { p1 && p2.get(0), p1 && p2.get(1) };
	}

	@Override
	public long m_c1(long p1, List<Long> p2) {
		return p1 + p2.get(0) + p2.get(1);
	}

	@Override
	public long[] m_c2(Long p1, List<Long> p2) {
		return new long[] { p1 + p2.get(0), p1 + p2.get(1) };
	}

	@Override
	public float m_d1(float p1, List<Float> p2) {
		return p1 + p2.get(0) + p2.get(1);
	}

	@Override
	public float[] m_d2(Float p1, List<Float> p2) {
		return new float[] { p1 + p2.get(0), p1 + p2.get(1) };
	}

	@Override
	public double m_e1(double p1, List<Double> p2) {
		return p1 + p2.get(0) + p2.get(1);
	}

	@Override
	public double[] m_e2(Double p1, List<Double> p2) {
		return new double[] { p1 + p2.get(0), p1 + p2.get(1) };
	}

	@Override
	public short m_f1(short p1, List<Short> p2) {
		return (short) (p1 + p2.get(0) + p2.get(1));
	}

	@Override
	public short[] m_f2(Short p1, List<Short> p2) {
		return new short[] { (short) (p1 + p2.get(0)), (short) (p1 + p2.get(1)) };
	}

	@Override
	public char m_g1(char p1, List<Character> p2) {
		return (char) (p1 + p2.get(0) + p2.get(1));
	}

	@Override
	public char[] m_g2(Character p1, List<Character> p2) {
		return new char[] { (char) (p1 + p2.get(0)), (char) (p1 + p2.get(1)) };
	}

	@Override
	public byte m_h1(byte p1, List<Byte> p2) {
		return (byte) (p1 + p2.get(0) + p2.get(1));
	}

	@Override
	public byte[] m_h2(Byte p1, List<Byte> p2) {
		return new byte[] { (byte) (p1 + p2.get(0)), (byte) (p1 + p2.get(1)) };
	}

}
