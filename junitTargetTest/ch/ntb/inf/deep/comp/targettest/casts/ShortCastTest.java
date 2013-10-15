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
public class ShortCastTest {

	@Test
	// Test short to byte
	public static void testShortToByte(){
		short s = 0x45ab;
		byte b = (byte)s;
		Assert.assertEquals("shortToByte1", (byte)0xab, b);
		s = -1;
		b = (byte)s;
		Assert.assertEquals("shortToByte2", -1, b);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test short to char
	public static void testShortToChar(){
		short s = 2400;
		char ch = (char)s;
		Assert.assertEquals("shortToChar1", (char)2400, ch);
		s = -1;
		ch = (char)s;
		Assert.assertEquals("shortToChar2", (char)-1, ch);
		s = 128;
		ch = (char)s;
		Assert.assertEquals("shortToChar3", (char)128, ch);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test short to int
	public static void testShortToInt(){
		short s = 240;
		int i = s;
		Assert.assertEquals("shortToInt1", 240, i);
		s = -1;
		i = s;
		Assert.assertEquals("shortToInt2", -1, i);
		s = -32000;
		i = s;
		Assert.assertEquals("shortToInt3", -32000, i);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test short to long
	public static void testShortToLong(){
		short s = -28450;
		long l = s;
		Assert.assertEquals("shortToLong1", (long)-28450, l);
		s = -1;
		l = s;
		Assert.assertEquals("shortToLong2", (long)-1, l);
		s = 128;
		l = s;
		Assert.assertEquals("shortToLong3", (long)128, l);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test short to float
	public static void testShortToFloat(){
		short s = -32768;
		float f = s;
		Assert.assertEquals("shortToFloat1", -3.2768e4f, f, 0);
		s = -1;
		f = s;
		Assert.assertEquals("shortToFloat2", -1.0f, f, 0);
		s = 5812;
		f = s;
		Assert.assertEquals("shortToFloat3", 5812.0f, f, 0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test short to double
	public static void testShortToDouble(){
		short s = 23890;
		double d = s;
		Assert.assertEquals("shortToDouble1", 2.3890e4, d, 0);
		s = -1;
		d = s;
		Assert.assertEquals("shortToDouble2", -1.0, d, 0);
		s = -128;
		d = s;
		Assert.assertEquals("shortToDouble3", -1.28e2, d, 0);
		s = 1;
		d = s;
		Assert.assertEquals("shortToDouble4", 1.0, d, 0);
		
		CmdTransmitter.sendDone();
	}

}
