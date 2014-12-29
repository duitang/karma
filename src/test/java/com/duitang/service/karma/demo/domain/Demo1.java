package com.duitang.service.karma.demo.domain;

import java.util.List;

public interface Demo1 {

	int m_a1(int p1, List<Integer> p2);

	int[] m_a2(Integer p1, List<Integer> p2);

	boolean m_b1(boolean p1, List<Boolean> p2);

	boolean[] m_b2(Boolean p1, List<Boolean> p2);

	long m_c1(long p1, List<Long> p2);

	long[] m_c2(Long p1, List<Long> p2);

	float m_d1(float p1, List<Float> p2);

	float[] m_d2(Float p1, List<Float> p2);

	double m_e1(double p1, List<Double> p2);

	double[] m_e2(Double p1, List<Double> p2);

	short m_f1(short p1, List<Short> p2);

	short[] m_f2(Short p1, List<Short> p2);

	@Deprecated
	char m_g1(char p1, List<Character> p2);

	@Deprecated
	char[] m_g2(Character p1, List<Character> p2);

	@Deprecated
	byte m_h1(byte p1, List<Byte> p2);

	@Deprecated
	byte[] m_h2(Byte p1, List<Byte> p2);

}
