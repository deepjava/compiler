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
public class IntCastTest {

	@Test
	// Test int to byte
	public static void testIntToByte(){
		int i = 0x27ab45ab;
		byte b = (byte)i;
		Assert.assertEquals("intToByte1", (byte)0xab, b);
		i = -1;
		b = (byte)i;
		Assert.assertEquals("intToByte2", -1, b);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test int to short
	public static void testIntToShort(){
		int i = 240;
		short s = (short)i;
		Assert.assertEquals("intToShort1", (short)240, s);
		i = -1;
		s = (short)i;
		Assert.assertEquals("intToShort2", (short)-1, s);
		i = 0x1a34c;
		s = (short)i;
		Assert.assertEquals("intToShort3", (short)0xa34c, s);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test int to char
	public static void testIntToChar(){
		int i = 0xa874938f;
		char ch = (char)i;
		Assert.assertEquals("intToChar1", (char)0x938f, ch);
		i = -1;
		ch = (char)i;
		Assert.assertEquals("intToChar2", (char)-1, ch);
		
		CmdTransmitter.sendDone();
	}


	@Test
	// Test int to long
	public static void testIntToLong(){
		int i = 213345;
		long l = i;
		Assert.assertEquals("intToLong1", (long)213345, l);
		i = -1;
		l = i;
		Assert.assertEquals("intToLong2", (long)-1, l);
		i = 0xa7665544;
		l = i;
		Assert.assertEquals("intToLong3", (long)0xa7665544, l);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test int to float
	public static void testIntToFloat(){
		int i = 0x80000000;
		float f = i;
		Assert.assertEquals("intToFloat1", -2.147483648e9f, f, 0);
		i = -1;
		f = i;
		Assert.assertEquals("intToFloat2", -1.0f, f, 0);
		i = 5812;
		f = i;
		Assert.assertEquals("intToFloat3", 5812.0f, f, 0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test int to double
	public static void testIntToDouble(){
		int i = 23890;
		double d = i;
		Assert.assertEquals("intToDouble1", 2.3890e4, d, 0);
		i = -1;
		d = i;
		Assert.assertEquals("intToDouble2", -1.0, d, 0);
		i = -128;
		d = i;
		Assert.assertEquals("intToDouble3", -1.28e2, d, 0);
		i = 1;
		d = i;
		Assert.assertEquals("intToDouble4", 1.0, d, 0);
		
		CmdTransmitter.sendDone();
	}

}
