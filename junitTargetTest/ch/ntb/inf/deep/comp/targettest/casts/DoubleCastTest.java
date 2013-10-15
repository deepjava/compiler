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

package ch.ntb.inf.deep.comp.targettest.casts;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 8.4.2011
 * 
 * @author Urs Graf
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class DoubleCastTest {

	@Test
	// Test double to byte
	public static void testDoubleToByte(){
		int i = 0x27ab45ab;
		byte b = (byte)i;
		Assert.assertEquals("intToByte1", (byte)0xab, b);
		i = -1;
		b = (byte)i;
		Assert.assertEquals("intToByte2", -1, b);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test double to short 
	public static void testDoubleToShort() {
		double d1 = 3456.9876;
		Assert.assertEquals("doubleToShort1", 3456, (short)d1);
		d1 = 5000000000.7;
		Assert.assertEquals("doubleToShort2", -1, (short)d1);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test double to char
	public static void testDoubleToChar(){
		int i = 0xa874938f;
		char ch = (char)i;
		Assert.assertEquals("intToChar1", (char)0x938f, ch);
		i = -1;
		ch = (char)i;
		Assert.assertEquals("intToChar2", (char)-1, ch);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test double to int 
	public static void testDoubleToInt() {
		double d1 = 0.0;
		Assert.assertEquals("doubleToInt1", 0, (int)d1);
		d1 = 100.0;
		Assert.assertEquals("doubleToInt2", 100, (int)d1);
		d1 = 3456.9876;
		Assert.assertEquals("doubleToInt3", 3456, (int)d1);
		d1 = -3456.9876;
		Assert.assertEquals("doubleToInt4", -3456, (int)d1);
		d1 = 2147483647.7;
		Assert.assertEquals("doubleToInt5", 2147483647, (int)d1);
		d1 = -2147483648.7;
		Assert.assertEquals("doubleToInt6", -2147483648, (int)d1);
		d1 = 5000000000.7;
		Assert.assertEquals("doubleToInt7", 2147483647, (int)d1);
		d1 = -5000000000.7;
		Assert.assertEquals("doubleToInt8", -2147483648, (int)d1);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test double to long 
	public static void testDoubleToLong() {
		double d1 = 0.0;
		Assert.assertEquals("test1", 0, (long)d1);
		d1 = 100.0;
		Assert.assertEquals("test2", 100, (long)d1);
		d1 = 3456.9876;
		Assert.assertEquals("test3", 3456, (long)d1);
		d1 = -3456.9876;
		Assert.assertEquals("test4", -3456, (long)d1);
		d1 = 1e6;
		Assert.assertEquals("test5", 1000000, (long)d1);
		d1 = -1e6;
		Assert.assertEquals("test6", -1000000, (long)d1);
		
		d1 = 2147483647.7;
		Assert.assertEquals("test11", 0x7fffffff, (long)d1);
		d1 = 2147483648.0;
		Assert.assertEquals("test12", 0x80000000L, (long)d1);
		d1 = 2147483648.7;
		Assert.assertEquals("test13", 0x80000000L, (long)d1);
		d1 = 2147483649.0;
		Assert.assertEquals("test14", 0x80000001L, (long)d1);
		d1 = -2147483648.7;
		Assert.assertEquals("test15", 0xFFFFFFFF80000000L, (long)d1);

		d1 = 4294967296.7;
		Assert.assertEquals("test20", 0x100000000L, (long)d1);
		d1 = 4294967297.0;
		Assert.assertEquals("test21", 0x100000001L, (long)d1);
		
		d1 = 5000000000.01;
		Assert.assertEquals("test31", 5000000000L, (long)d1);
		d1 = -5000000000.01;
		Assert.assertEquals("test32", -5000000000L, (long)d1);
		d1 = 2.5e-12;
		Assert.assertEquals("test33", 0L, (long)d1);
		d1 = 2.5e16;
		Assert.assertEquals("test34", 25000000000000000L, (long)d1);
		d1 = -2.585342346878534e18;
		Assert.assertEquals("test35", -2585342346878534144L, (long)d1);
		d1 = 9.223372036854775e18;
		Assert.assertEquals("test35", 0x7FFFFFFFFFFFFC00L, (long)d1);
		d1 = 9.2233720368547760e18; 
		Assert.assertEquals("test36", 0x7FFFFFFFFFFFFFFFL, (long)d1);
		d1 = 9.2233720368547753e18; 
		Assert.assertEquals("test361", 0x7FFFFFFFFFFFFFFFL, (long)d1);
		d1 = 9.2233720368547752e18; 
		Assert.assertEquals("test362", 0x7FFFFFFFFFFFFC00L, (long)d1);
		d1 = -9.223372036854775808e18;
		Assert.assertEquals("test37", 0x8000000000000000L, (long)d1);
		d1 = -9.2233720368547753e18;
		Assert.assertEquals("test371", 0x8000000000000000L, (long)d1);
		d1 = -9.2233720368547752e18;
		Assert.assertEquals("test372", 0x8000000000000400L, (long)d1);
		d1 = 1.0e20;
		Assert.assertEquals("test38", 0x7FFFFFFFFFFFFFFFL, (long)d1);
		d1 = -1.0e20;
		Assert.assertEquals("test39", 0x8000000000000000L, (long)d1);
		d1 = 0.9;
		Assert.assertEquals("test50", 0, (long)d1);
		d1 = -0.9;
		Assert.assertEquals("test51", 0, (long)d1);
		d1 = 1.9;
		Assert.assertEquals("test52", 1, (long)d1);
		d1 = -1.9;
		Assert.assertEquals("test53", -1, (long)d1);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test double to float
	public static void testDoubleToFloat(){
		int i = 0x80000000;
		float f = i;
		Assert.assertEquals("doubleToFloat1", -2.147483648e9f, f, 0);
		i = -1;
		f = i;
		Assert.assertEquals("doubleToFloat2", -1.0f, f, 0);
		i = 5812;
		f = i;
		Assert.assertEquals("doubleToFloat3", 5812.0f, f, 0);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Tests specifics of double to long subroutine
	public static void testDoubleToLongSub() {
		double d1 = 100.0;
		long a = (long)d1;	// assigned registers r31/r30
		d1 = 200.0;
		long b = (long)d1;	// assigned registers r29/r28
		d1 = 300.0;
		long c = (long)d1;	// assigned registers r27/r26
		Assert.assertEquals("test1", 100, a);	
		Assert.assertEquals("test2", 200, b);	
		Assert.assertEquals("test3", 300, c);	
		double d2 = a + b + c; // force variables in nonvolatiles
		
		CmdTransmitter.sendDone();
	}

}
