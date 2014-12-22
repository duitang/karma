package com.duitang.service.demo.domain;

public class Demo1Impl implements Demo1 {

	@Override
	public int m_a1(int p1, int[] p2) {
		return p1 + p2[0] + p2[1];
	}

	@Override
	public int[] m_a2(int p1, int[] p2) {
		return new int[] { p1 + p2[0], p1 + p2[1] };
	}

	@Override
	public boolean m_b1(boolean p1, boolean[] p2) {
		return p1 || (p2[0] && p2[1]);
	}

	@Override
	public boolean[] m_b2(boolean p1, boolean[] p2) {
		return new boolean[] { p1 && p2[0], p1 && p2[1] };
	}

	@Override
	public long m_c1(long p1, long[] p2) {
		return p1 + p2[0] + p2[1];
	}

	@Override
	public long[] m_c2(long p1, long[] p2) {
		return new long[] { p1 + p2[0], p1 + p2[1] };
	}

	@Override
	public float m_d1(float p1, float[] p2) {
		return p1 + p2[0] + p2[1];
	}

	@Override
	public float[] m_d2(float p1, float[] p2) {
		return new float[] { p1 + p2[0], p1 + p2[1] };
	}

	@Override
	public double m_e1(double p1, double[] p2) {
		return p1 + p2[0] + p2[1];
	}

	@Override
	public double[] m_e2(double p1, double[] p2) {
		return new double[] { p1 + p2[0], p1 + p2[1] };
	}

	@Override
	public short m_f1(short p1, short[] p2) {
		return (short) (p1 + p2[0] + p2[1]);
	}

	@Override
	public short[] m_f2(short p1, short[] p2) {
		return new short[] { (short) (p1 + p2[0]), (short) (p1 + p2[1]) };
	}

	@Override
	public char m_g1(char p1, char[] p2) {
		return (char) (p1 + p2[0] + p2[1]);
	}

	@Override
	public char[] m_g2(char p1, char[] p2) {
		return new char[] { (char) (p1 + p2[0]), (char) (p1 + p2[1]) };
	}

	@Override
	public byte m_h1(byte p1, byte[] p2) {
		return (byte) (p1 + p2[0] + p2[1]);
	}

	@Override
	public byte[] m_h2(byte p1, byte[] p2) {
		return new byte[] { (byte) (p1 + p2[0]), (byte) (p1 + p2[1]) };
	}

}
