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
public class CharCastTest {

	@Test
	// Test char to byte
	public static void testCharToByte(){
		char ch = 0xe5ab;
		byte b = (byte)ch;
		Assert.assertEquals("charToByte1", (byte)0xab, b);
		ch = 0xffff;
		b = (byte)ch;
		Assert.assertEquals("charToByte2", -1, b);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test char to short
	public static void testCharToShort(){
		char ch = 2400;
		short s = (short)ch;
		Assert.assertEquals("charToShort1", 2400, s);
		ch = 0xffff;
		s = (short)ch;
		Assert.assertEquals("charToShort2", -1, s);
		ch = 128;
		s = (short)ch;
		Assert.assertEquals("charToShort3", 128, s);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test char to int
	public static void testCharToInt(){
		char ch = 240;
		int i = ch;
		Assert.assertEquals("charToInt1", 240, i);
		ch = 0xffff;
		i = ch;
		Assert.assertEquals("charToInt2", 65535, i);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test char to long
	public static void testCharToLong(){
		char ch = 28450;
		long l = ch;
		Assert.assertEquals("charToLong1", (long)28450, l);
		ch = 0xffff;
		l = ch;
		Assert.assertEquals("charToLong2", (long)65535, l);
		ch = 128;
		l = ch;
		Assert.assertEquals("charToLong3", (long)128, l);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test char to float
	public static void testCharToFloat(){
		char ch = 32768;
		float f = ch;
		Assert.assertEquals("charToFloat1", 3.2768e4f, f, 0);
		ch = 0xffff;
		f = ch;
		Assert.assertEquals("charToFloat2", 6.5535e4f, f, 0);
		ch = 5812;
		f = ch;
		Assert.assertEquals("charToFloat3", 5812.0f, f, 0);
		ch = 1;
		f = ch;
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test char to double
	public static void testCharToDouble(){
		char ch = 23890;
		double d = ch;
		Assert.assertEquals("charToDouble1", 2.3890e4, d, 0);
		ch = 0xffff;
		d = ch;
		Assert.assertEquals("charToDouble2", 65535.0, d, 0);
		
		CmdTransmitter.sendDone();
	}

}
