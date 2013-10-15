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
public class FloatCastTest {

	@Test
	// Test float to byte
	public static void testFloatToByte(){
		int i = 0x27ab45ab;
		byte b = (byte)i;
		Assert.assertEquals("intToByte1", (byte)0xab, b);
		i = -1;
		b = (byte)i;
		Assert.assertEquals("intToByte2", -1, b);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test float to short 
	public static void testFloatToShort() {
		float f = 3456.9876f;
		Assert.assertEquals("floatToShort1", 3456, (short)f);
		f = -3456.9876f;
		Assert.assertEquals("floatToShort2", -3456, (short)f);
		f = 1E5f;
		Assert.assertEquals("floatToShort3", -31072, (short)f);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test float to char
	public static void testFloatToChar(){
		int i = 0xa874938f;
		char ch = (char)i;
		Assert.assertEquals("intToChar1", (char)0x938f, ch);
		i = -1;
		ch = (char)i;
		Assert.assertEquals("intToChar2", (char)-1, ch);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test float to int 
	public static void testFloatToInt() {
		float f = 0.0f;
		Assert.assertEquals("floatToInt1", 0, (int)f);
		f = 100.0f;
		Assert.assertEquals("floatToInt2", 100, (int)f);
		f = 3456.9876f;
		Assert.assertEquals("floatToInt3", 3456, (int)f);
		f = -3456.9876f;
		Assert.assertEquals("floatToInt4", -3456, (int)f);
		f = 2147483647.7f;
		Assert.assertEquals("floatToInt5", 2147483647, (int)f);
		f = -2147483648.7f;
		Assert.assertEquals("floatToInt6", -2147483648, (int)f);
		f = 5000000000.7f;
		Assert.assertEquals("floatToInt7", 2147483647, (int)f);
		f = -5000000000.7f;
		Assert.assertEquals("floatToInt8", -2147483648, (int)f);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test float to long 
	public static void testFloatToLong() {
		float f = 0.0f;
		Assert.assertEquals("test1", 0, (long)f);
		f = 100.0f;
		Assert.assertEquals("test2", 100, (long)f);
		f = 3456.9876f;
		Assert.assertEquals("test3", 3456, (long)f);
		f = -3456.9876f;
		Assert.assertEquals("test4", -3456, (long)f);
		f = 2147483647.7f;
		Assert.assertEquals("test5", 2147483648L, (long)f);
		f = -2147483648.7f;
		Assert.assertEquals("test6", -2147483648, (long)f);
		f = 5000000000.7f;
		Assert.assertEquals("test7", 5000000000L, (long)f);
		f = -5000000000.7f;
		Assert.assertEquals("test8", -5000000000L, (long)f);
		f = 9.2233720368547760e18f; // 7FFFFFFFFFFFFFFFH
		Assert.assertEquals("test9", 9223372036854775807L, (long)f);
		f = -9.2233720368547760e18f; // 8000000000000000H
		Assert.assertEquals("test10", -9223372036854775808L, (long)f);
		f = 1.0e20f;
		Assert.assertEquals("test11", 9223372036854775807L, (long)f);
		f = -1.0e20f;
		Assert.assertEquals("test12", -9223372036854775808L, (long)f);
		f = 0.9f;
		Assert.assertEquals("test20", 0, (long)f);
		f = -0.9f;
		Assert.assertEquals("test21", 0, (long)f);
		f = 1.9f;
		Assert.assertEquals("test22", 1, (long)f);
		f = -1.9f;
		Assert.assertEquals("test23", -1, (long)f);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test float to double
	public static void testFloatToDouble(){
		float f = 2.3890e4f;
		Assert.assertEquals("floatToDouble1", 2.3890e4f, f, 0);
		f = 1.2e18f;
		Assert.assertEquals("floatToDouble2", 1.2e18f, f, 0);
		f = -1.2e18f;
		Assert.assertEquals("floatToDouble2", -1.2e18f, f, 0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Tests specifics of double to long subroutine
	public static void testFloatToLongSub() {
		float f1 = 100.0f;
		long a = (long)f1;	// assigned registers r31/r30
		f1 = 200.0f;
		long b = (long)f1;	// assigned registers r29/r28
		f1 = 300.0f;
		long c = (long)f1;	// assigned registers r27/r26
		Assert.assertEquals("test1", 100, a);	
		Assert.assertEquals("test2", 200, b);	
		Assert.assertEquals("test3", 300, c);	
		float f2 = a + b + c; // force variables in nonvolatiles
		
		CmdTransmitter.sendDone();
	}

}
