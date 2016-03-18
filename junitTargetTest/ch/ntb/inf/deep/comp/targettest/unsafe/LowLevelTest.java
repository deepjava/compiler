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

package ch.ntb.inf.deep.comp.targettest.unsafe;

import ch.ntb.inf.deep.lowLevel.LL;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 4.3.2011, Urs Graf
 * 
 *         Changes:
 */
@MaxErrors(100)
public class LowLevelTest {
	
	@Test
	public static void testDoubleToBits() {
		double d1 = 0.0;
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			Assert.assertEquals("Test1", 0x0000000000000000L, LL.doubleToBits(d1));
			d1 = 1.0;
			Assert.assertEquals("Test2", 0x3FF0000000000000L, LL.doubleToBits(d1));
			d1 = 2.5;
			Assert.assertEquals("Test3", 0x4004000000000000L, LL.doubleToBits(d1));
			d1 = 0.1;
			Assert.assertEquals("Test4", 0x3FB999999999999AL, LL.doubleToBits(d1));
			d1 = -0.1;
			Assert.assertEquals("Test5", 0xBFB999999999999AL, LL.doubleToBits(d1));
			float f1 = 0.0f;
			Assert.assertEquals("Test6", 0x0000000000000000L, LL.doubleToBits(f1));
			f1 = 1.0f;
			Assert.assertEquals("Test7", 0x3FF0000000000000L, LL.doubleToBits(f1));
			f1 = 2.0f;
			Assert.assertEquals("Test8", 0x4000000000000000L, LL.doubleToBits(f1));
			f1 = 2.5f;
			Assert.assertEquals("Test9", 0x4004000000000000L, LL.doubleToBits(f1));
			f1 = 0.1f;
			Assert.assertEquals("Test10", 0x3FB99999A0000000L, LL.doubleToBits(f1));
			f1 = -0.1f;
			Assert.assertEquals("Test11", 0xBFB99999A0000000L, LL.doubleToBits(f1));
			
			d1 = 10;
			Assert.assertEquals("Test12", 0x4024000000000000L, LL.doubleToBits(d1));
			f1 = 10;
			Assert.assertEquals("Test13", 0x4024000000000000L, LL.doubleToBits(f1));
		}
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testBitsToDouble() {
		long bits = 0x4004000000000000L;
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			Assert.assertEquals("Test1", 2.5, LL.bitsToDouble(bits), 0.0);
			bits = 0xBFB999999999999AL;
			Assert.assertEquals("Test2", -0.1, LL.bitsToDouble(bits), 0.0);
		}
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testFloatToBits() {
		float f1 = 0.0f;
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			Assert.assertEquals("Test1", 0, LL.floatToBits(f1));
			f1 = 1.0f;
			Assert.assertEquals("Test2", 0x3F800000, LL.floatToBits(f1));
			f1 = 2.0f;
			Assert.assertEquals("Test3", 0x40000000, LL.floatToBits(f1));
			f1 = 2.5f;
			Assert.assertEquals("Test4", 0x40200000, LL.floatToBits(f1));
			f1 = 0.1f;
			Assert.assertEquals("Test5", 0x3dcccccd, LL.floatToBits(f1));
			f1 = -0.1f;
			Assert.assertEquals("Test6", 0xbdcccccd, LL.floatToBits(f1));
		}
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testBitsToFloat() {
		int bits = 0x40200000;
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			Assert.assertEquals("Test1", 2.5f, LL.bitsToFloat(bits), 0.0);
			bits = 0xbdcccccd;
			Assert.assertEquals("Test2", -0.1, LL.bitsToFloat(bits), 1e-5);
		}
		CmdTransmitter.sendDone();
	}
	

}
