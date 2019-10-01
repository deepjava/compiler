/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.comp.targettest.various;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 25.3.2011, Urs Graf
 * 
 */
@MaxErrors(100)
public class ParameterPassing {
	
	private ParameterPassing() {}

	// checks if parameters are passed correctly 
	static int m1(int a, int b, int c, int d) {
		return a + b + c + d;
	}
	
	@Test
	public static void ParamsTest1() {
		Assert.assertEquals("Test1", 10, m1(1, 2, 3, 4));
		int a = 2;
		Assert.assertEquals("Test2", 4, m1(0, a, a, 0));
		int b = 3;
		Assert.assertEquals("Test3", 11, m1(b, a, b, b));
		Assert.assertEquals("Test4", 13, m1(a, a, 4, 5));

		CmdTransmitter.sendDone();
	}	
	
	// checks if parameters are passed correctly 
	static int m2(int a, long b, byte c, float d, double e, long f, float g) {
		// ([a -> long + b + (c -> long)] -> float + d) -> double + e + (f -> double) + g -> double)
		return (int)(a + b + c + d + e + f + g);
	}
	
	@Test
	public static void ParamsTest2() {
		Assert.assertEquals("Test1", -1, m2(-2, 8L, (byte)-6, 2.5f, -3.5, -4, 3.8f));
		long b = -3;
		int a = 5;
		float c = 2.5f;
		Assert.assertEquals("Test2", 3, m2(a, b, (byte)b, c, c, b, c));
		CmdTransmitter.sendDone();
	}	
	
	// checks if parameters are passed correctly 
	int m3(int a, long b, byte c, float d, double e, long f, float g) {
		return (int)(a + b + c + d + e + f + g);
	}

	@Test
	public static void ParamsTest3() {
		ParameterPassing obj = new ParameterPassing();
		Assert.assertEquals("Test1", -1, obj.m3(-2, 8L, (byte)-6, 2.5f, -3.5, -4, 3.8f));
		long b = -3;
		int a = 5;
		float c = 2.5f;
		Assert.assertEquals("Test2", 3, m2(a, b, (byte)b, c, c, b, c));
		Assert.assertEquals("Test3", 3, obj.m3(a, b, (byte)b, c, c, b, c));
		Assert.assertEquals("Test4", 10, obj.m3((int)c, a, (byte)c, a, b, b, c));
		CmdTransmitter.sendDone();
	}	

	// method with many parameters
	public static byte sum(byte v1, byte v2, byte v3, byte v4, byte v5, byte v6,
			byte v7, byte v8, byte v9, byte v10, byte v11, byte v12, byte v13,
			byte v14, byte v15, byte v16, byte v17) {
			return (byte) (v1 + v2 + v3 + v4+ v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16 + v17);
	}

	@Test
	public static void ParamsTest4(){
		byte res;
		res = sum((byte) 1,(byte) 2,(byte) 3,(byte) 4,(byte) 5,(byte) 6,(byte) 7,(byte) 8,(byte) 9,(byte) 10,(byte) 11,(byte) 12,(byte) 13,(byte) 14,(byte) 15,(byte) 16,(byte) 17);
		Assert.assertEquals("Test1", (byte)-103, res);
		CmdTransmitter.sendDone();
	}
	
	// method with many different parameters
	public static int m10(long p1, int p2, long p3, long p4, byte p5, long p6, int p7, short p8, long p9, char p10) {
		return ((int)p1 + (int)p2 + (int)p3 + (int)p4 + (int)p5 + (int)p6 + p7 + p8 + (int)p9 + p10);
	}

	@Test
	public static void ParamsTest5(){
		int res;
		res = m10(1, 2, 3, 4, (byte)5, 6, 7, (short)8, 9, (char)10);
		Assert.assertEquals("Test1", 55, res);
		res = m10(11, 12, 13, 14, (byte)15, 16, 17, (short)18, 19, (char)20);
		Assert.assertEquals("Test2", 155, res);
		long p6 = 6, p9 = 9;
		int p7 = 7;
		res = m10(1, 2, 3, 4, (byte)5, p6, p7, (short)8, p9, (char)10);
		Assert.assertEquals("Test3", 55, res);
		res = m10(p6, p7, p7, p6, (byte)5, p6, p7, (short)p9, p9, (char)10);
		Assert.assertEquals("Test4", 72, res);
		p6 = 6;
		char p10 = 10;
		long p5 = 5;
		res = m10(p6, p7, p7, p6, (byte)p5, p6, p7, (short)p9, p9, p10);
		Assert.assertEquals("Test5", 72, res);
		CmdTransmitter.sendDone();
	}

	@Test
	public static void ParamsTest6(){
		long p1 = 1;
		int p2 = 2;
		long p3 = 3, p4 = 4;
		byte p5 = 5;
		long p6 = 6;
		int p7 = 7;
		short p8 = 8;
		long p9 = 9;
		char p10 = 10;
		int res = m10(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10);
		Assert.assertEquals("Test1", 55, res);
		CmdTransmitter.sendDone();
	}

	// method with many parameters (double)
	public static int m11(double p1, double p2, double p3, double p4, double p5, double p6, double p7, double p8, double p9, double p10, double p11, double p12) {
		return (int)(p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9 + p10 + p11 + p12);
	}

	@Test
	public static void ParamsTest7(){
		int res;
		res = m11(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
		Assert.assertEquals("Test1", 78, res);
		CmdTransmitter.sendDone();
	}

	static void help() {}
	
	// method with many parameters (int)
	static int m12(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, int p10, int p11, int p12) {
		int x1 = p1;
		int x3 = p3;
		int x5 = p5;
		int x6 = p6;
		int x10 = p10;
		int x12 = p12;
		help();
		return (int)(x1 + p2 + x3 + p4 + x5 + x6 + p7 + p8 + p9 + x10 + p11 + x12);
	}

	@Test
	public static void ParamsTest8(){
		int res;
		res = m12(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120);
		Assert.assertEquals("Test1", 780, res);
		CmdTransmitter.sendDone();
	}

	float f1, f2, f3, f4, f5, f6;
	double d1, d2, d3, d4, d5, d6;
	long l1, l2, l3, l4, l5, l6;
	
	ParameterPassing(long p1, double p2, float p3, long p4, double p5, float p6, long p7, double p8, float p9, long p10, double p11, float p12, long p13, double p14, float p15, long p16, double p17, float p18) {
// PPC
// parameters passed in
//		r2	      r3,r4     fr1         fr2     r5,r6       fr3        fr4    r7,r8      fr5         fr6      r9,r10     fr7        fr8    stack,stack   stack      stack  stack,stack    stack       stack 
// copied to
//      r31      r30,r29    fr31        fr30   r28,r27      fr29       fr28  r26,r25     fr27        fr26    r24,r23     fr25       fr24    r22,r21       fr23       fr22   r20,r19       fr21         fr20  
// ARM
// parameters passed in
//		r0	      r1,r2     fr1         fr2     r5,r6       fr3        fr4    r7,r8      fr5         fr6      r9,r10     fr7        fr8    stack,stack   stack      stack  stack,stack    stack       stack // copied to
//		r31      r30,r29    fr31        fr30   r28,r27      fr29       fr28  r26,r25     fr27        fr26    r24,r23     fr25       fr24    r22,r21       fr23       fr22   r20,r19       fr21         fr20  
		this.l1 = p1; this.l2 = p4; this.l3 = p7; this.l4 = p10; this.l5 = p13; this.l6 = p16;
		this.d1 = p2; this.d2 = p5; this.d3 = p8; this.d4 = p11; this.d5 = p14; this.d6 = p17;
		this.f1 = p3; this.f2 = p6; this.f3 = p9; this.f4 = p12; this.f5 = p15; this.f6 = p18;
	}
	
	@Test
	//Test constructor with many parameters
	public static void ParamsTest10(){
		ParameterPassing obj = new ParameterPassing(100, 100.1, 100.2f, 200, 200.1, 200.2f, 300, 300.1, 300.2f, 400, 400.1, 400.2f, 500, 500.1, 500.2f, 600, 600.1, 600.2f);
		Assert.assertEquals("Test1", obj.l1, 100);
		Assert.assertEquals("Test2", obj.d1, 100.1, 1e-10);
		Assert.assertEquals("Test3", obj.f1, 100.2f, 1e-10);
		Assert.assertEquals("Test4", obj.l2, 200);
		Assert.assertEquals("Test5", obj.d2, 200.1, 1e-10);
		Assert.assertEquals("Test6", obj.f2, 200.2f, 1e-10);
		Assert.assertEquals("Test7", obj.l3, 300);
		Assert.assertEquals("Test8", obj.d3, 300.1, 1e-10);
		Assert.assertEquals("Test9", obj.f3, 300.2f, 1e-10);
		Assert.assertEquals("Test10", obj.l4, 400);
		Assert.assertEquals("Test11", obj.d4, 400.1, 1e-10);
		Assert.assertEquals("Test12", obj.f4, 400.2f, 1e-10);
		Assert.assertEquals("Test13", obj.l5, 500);
		Assert.assertEquals("Test14", obj.d5, 500.1, 1e-10);
		Assert.assertEquals("Test15", obj.f5, 500.2f, 1e-10);
		Assert.assertEquals("Test16", obj.l6, 600);
		Assert.assertEquals("Test17", obj.d6, 600.1, 1e-10);
		Assert.assertEquals("Test18", obj.f6, 600.2f, 1e-10);
		CmdTransmitter.sendDone();
	}

	// method with many locals
	public static long m20(long p1, int p2) {
		short a1 = 10, a2 = 11;
		int a3 = 20, a4 = 21;
		long a5 = 30, a6 = 31, a7 = 32, a8 = 33, a9 = 34, a10 = 35, a11 = 36, a12 = 37;
		byte a13 = 40, a14 = 41, a15 = 42, a16 = 43, a17 = 44, a18 = 45, a19 = 46, a20 = 47, a21 = 48, a22 = 49, a23 = 50;
		long res = p1 + p2 + a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10;
		res = res + a11 + a12 + a13 + a14 + a15 + a16 + a17 + a18 + a19 + a20 + a21 + a22 + a23;
		return res;
	}

	// method with many locals (float)
	public static double m21(float p1, double p2) {
		double d0 = 0.1, d1 = 0.2, d2 = 0.3, d3 = 0.4, d4 = 0.5;
		double d5 = 1.0, d6 = 1.1, d7 = 1.2, d8 = 1.3, d9 = 1.4;
		double d10 = 2.0, d11 = 2.1, d12 = 2.2, d13 = 2.3, d14 = 2.4;
		double res = d0 + d1 + d2 + d3 + d4 + Math.sin(p1) + d5 + d6 + d7 + d8 + d9;;
		if (p2 < 100) res += p2 + d10 + d11 + d12 + d13 + d14;
		return res;
	}

	// method with many locals (float)
	public static double m22(float[] r1, double p2) {
		double d0 = 0.1, d1 = 0.2, d2 = 0.3, d3 = 0.4, d4 = 0.5;
		float d5 = 1.0f, d6 = 1.1f, d7 = 1.2f, d8 = 1.3f, d9 = 1.4f;
		double d10 = 2.0, d11 = 2.1, d12 = 2.2, d13 = 2.3, d14 = 2.4;
		double[] a1 = {10.0, 10.1};
		float[] a2 = {20.0f, 20.1f, 20.2f};
		double res = d0 + d1 + d2 + d3 + d4 + Math.sin(a2[1] + d8);
		for (int i = 0; i < 3; i++) a2[0] += r1[0] + d12 + d7;
		res += p2 + a1[0] + a2[0] + d11 + d12 + d13 + d14;
		a1[1] = d11;
		res -= d5 + d6 - d10 - a1[1] - d9;
		return res;
	}
	
	@Test
	public static void ParamsTest11(){
		Assert.assertEquals("Test1", 1125, m20(100, 200));
		Assert.assertEquals("Test2", 10003000030825L, m20(10001000010000L, 2000020000));
		Assert.assertEquals("Test3", 25.649216, m21(3.5f, 7.5), 1e-5);
		Assert.assertEquals("Test4", 7.149216, m21(3.5f, 200.1), 1e-5);
		Assert.assertEquals("Test5", 62.657316, m22(new float[]{3.5f, 1.5f}, -2.5), 1e-5);
		CmdTransmitter.sendDone();
	}

}
