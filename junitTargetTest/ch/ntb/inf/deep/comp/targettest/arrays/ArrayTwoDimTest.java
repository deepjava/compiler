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

package ch.ntb.inf.deep.comp.targettest.arrays;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 18.06.2009
 * 
 * @author Jan Mrnak
 * 
 */
@MaxErrors(100)
public class ArrayTwoDimTest {
	
	@Test
	// Main test method for array length testing
	public static void testArrayLength() {
		int res;
		res = (new byte[3][4]).length;
		Assert.assertEquals("byteArrayLength1", 3, res);
		res = (new byte[3][4])[1].length;
		Assert.assertEquals("byteArrayLength2", 4, res);
		
		res = (new byte[3][4]).length;
		Assert.assertEquals("shortArrayLength1", 3, res);
		res = (new byte[3][4])[1].length;
		Assert.assertEquals("shortArrayLength2", 4, res);
		
		res = (new char[3][345]).length;
		Assert.assertEquals("charArrayLength1", 3, res);
		res = (new char[3][345])[0].length;
		Assert.assertEquals("charArrayLength2", 345, res);
		
		res = (new char[27][4]).length;
		Assert.assertEquals("intArrayLength1", 27, res);
		res = (new char[27][4])[22].length;
		Assert.assertEquals("intArrayLength2", 4, res);
		
		res = (new long[3][2]).length;
		Assert.assertEquals("longArrayLength1", 3, res);
		res = (new long[3][2])[2].length;
		Assert.assertEquals("longArrayLength2", 2, res);
		
		res = (new float[3][1]).length;
		Assert.assertEquals("floatArrayLength1", 3, res);
		res = (new float[3][1])[0].length;
		Assert.assertEquals("floatArrayLength2", 1, res);
		
		res = (new double[3][5]).length;
		Assert.assertEquals("doubleArrayLength1", 3, res);
		res = (new double[3][5])[2].length;
		Assert.assertEquals("doubleArrayLength2", 5, res);
		CmdTransmitter.sendDone();
	}


	@Test
	// Main test method for array value test
	public static void testArrayValue() {
		int res;
		byte[][] b = {{0, 1, 2}, {3, 4, 5}};
		res = b[0][2];
		Assert.assertEquals("byteArrayValue", 2, res);
		
		short[][] s = {{250, 251, 252}, {253, 254, 255}};
		res = s[1][2];
		Assert.assertEquals("shortArrayValue", 255, res);

		char[][] ch = {{'a', 'b', 'c', 'd', 'e'}, {'1', '2', '3', '4', '5'}};
		res = ch[1][3];
		Assert.assertEquals("charArrayValue", '4', res);
		
		int[][] i = {{65000, 65001}, {65002, 65003}};
		res = i[0][1];
		Assert.assertEquals("intArrayValue", 65001, res);
		
		long[][] l = {{23348456749506556L, 6501}, {-234563455, 345345665456457L}};
		long resLong = l[1][0];
		Assert.assertEquals("longArrayValue", -234563455, resLong);
		
		float[][] f = {{0.5f, 1.5f, 2.5f, 3.5f}, {4.5f, 5.5f, 6.5f, 7.5f}};
		float resf = f[1][3];
		Assert.assertEquals("floatArrayValue2", 7.5f, resf, 0);
		
		double[][] d = {{0.5, 1.5, 2.5}, {3.5, 4.5, 5.5}};
		double resd = d[0][0];
		Assert.assertEquals("doubleArrayValue1", 0.5, resd, 0.0);
			
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Main test method for array value test
	public static void testUnbalancedArray() {
		int res;
		byte[][] b = {{0, 1, 2}, {3, 4}};
		res = b[0][2];
		Assert.assertEquals("byteArrayUnBal1", 2, res);
		res = b[1][1];
		Assert.assertEquals("byteArrayUnBal2", 4, res);
		
		double[][] d = {{0.5, 1.5, 2.5, 3.5}, {4.5}};
		double resd = d[0][3];
		Assert.assertEquals("doubleArrayUnBal1", 3.5, resd, 0.0);
			
		CmdTransmitter.sendDone();
	}
	@Test
	//
	public static void testArrayVarious(){
		byte[] firstRow = {3, 1, 2, 3, 4};
		byte[][] data = new byte[4][];	
		int len = data.length;
		Assert.assertEquals("test1", 4, len);
		data[0] = firstRow;
		len = data[0].length;
		Assert.assertEquals("test2", 5, len);
		data[1] = new byte[] {11, 12, 13};
		len = data[1].length;
		Assert.assertEquals("test3", 3, len);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// test multiarray instruction
	public static void testMultiarray() {
		int res;
		byte[][] b = new byte[2][2];
		b[1][1] = 100;
		res = b[1][1];
		Assert.assertEquals("byteArrayValue", 100, res);
		if (b[0] instanceof byte[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);
		
		short[][] s = new short[2][2];
		s[1][1] = 10000;
		res = s[1][1];
		Assert.assertEquals("shortArrayValue", 10000, res);
		if (s[0] instanceof short[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);

		int[][] i = new int[2][2];
		i[1][1] = 1000000;
		res = i[1][1];
		Assert.assertEquals("intArrayValue", 1000000, res);
		if (i[0] instanceof int[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);

		long[][] l = new long[2][2];
		l[1][1] = 10000000000L;
		long res1 = l[1][1];
		Assert.assertEquals("longArrayValue", 10000000000L, res1);
		if (l[0] instanceof long[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);

		char[][] c = new char[2][2];
		c[1][1] = 'u';
		char res3 = c[1][1];
		Assert.assertEquals("charArrayValue", 'u', res3);
		if (c[0] instanceof char[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);

		float[][] f = new float[2][2];
		f[1][1] = 2.8f;
		float res4 = f[1][1];
		Assert.assertEquals("floatArrayValue", 2.8f, res4, 0.0);
		if (f[0] instanceof float[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);

		double[][] d = new double[2][2];
		d[1][1] = 4.3456e-27;
		double res5 = d[1][1];
		Assert.assertEquals("doubleArrayValue", 4.3456e-27, res5, 0.0);
		if (d[0] instanceof double[]) res = 2; else res = 1;
		Assert.assertEquals("checkType", 2, res);


		CmdTransmitter.sendDone();
	}
	
	public class Foo {
		double[][] A = new double[][] {{ 0, 1},{ 2, 3}};
	}

	double calc() {
		Foo f = new ArrayTwoDimTest.Foo();

		double sum = 0;
		for (int n = 0; n < 2; n++) {
			for (int m = 0; m < 2; m++)	{
				sum += f.A[n][m];
			}
		}
		return sum;
	}

	@Test
	// test multiarray which is object instance
	public static void testMultiarrayInstance() {
		double res = new ArrayTwoDimTest().calc();;
		Assert.assertEquals("test1", 6.0, res, 1e-8);
		
		CmdTransmitter.sendDone();
	}

}
