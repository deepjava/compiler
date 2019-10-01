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

import ch.ntb.inf.deep.unsafe.arm.US;
import ch.ntb.inf.junitTarget.*;

/**
 * NTB 31.3.2019, Urs Graf
 * 
 *         Changes:
 */
@MaxErrors(100)
public class UnsafeTestARM {
	static final int addr = 0x3e0; // choose an address in the exception vector space, which is unused
	
	@Test
	public static void testGetPut() {
		if (CmdTransmitter.plattform != CmdTransmitter.pHost) {
			US.PUT1(addr, 0x11);
			US.PUT1(addr + 1, 0x22);
			US.PUT2(addr + 2, 0x3344);
			US.PUT4(addr + 4, 0x55667788);
			Assert.assertEquals("Test1", 0x11, US.GET1(addr));
			Assert.assertEquals("Test2", 0x22, US.GET1(addr + 1));
			Assert.assertEquals("Test3", 0x3344, US.GET2(addr + 2));
			Assert.assertEquals("Test4", 0x55667788, US.GET4(addr + 4));
			Assert.assertEquals("Test5", 0x5566778833442211L, US.GET8(addr));
			US.PUT8(addr, 0xccddaabb99887755L);
			Assert.assertEquals("Test6", 0xccddaabb99887755L, US.GET8(addr));
		}
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testBit() {
		if (CmdTransmitter.plattform != CmdTransmitter.pHost) {
			US.PUT4(addr, 0xaaaaaaaa);
			Assert.assertFalse("Test1", US.BIT(addr, 0));
			Assert.assertTrue("Test2", US.BIT(addr, 1));
			Assert.assertTrue("Test3", US.BIT(addr, 7));
			Assert.assertFalse("Test4", US.BIT(addr, 30));
			Assert.assertTrue("Test5", US.BIT(addr, 31));
			int bit = 0;
			Assert.assertFalse("Test10", US.BIT(addr, bit++));
			Assert.assertTrue("Test11", US.BIT(addr, bit));
			bit = 7;
			Assert.assertTrue("Test12", US.BIT(addr, bit));
			bit = 30;
			Assert.assertFalse("Test13", US.BIT(addr, bit++));
			Assert.assertTrue("Test14", US.BIT(addr, bit));
		}
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testRegisters() {
		if (CmdTransmitter.plattform != CmdTransmitter.pHost) {
			// make sure register is not used by this method
			US.PUTGPR(6, 0x3456789a);
			Assert.assertEquals("Test1", 0x3456789a, US.GETGPR(6));
			US.PUTEXTRD(6, 3.67548E23);
			Assert.assertEquals("Test2", 3.67548E23, US.GETEXTRD(6), 0.0);
			US.PUTEXTRS(6, 0.003456f);
			Assert.assertEquals("Test3", 0.003456, US.GETEXTRS(6), 0.0001);
		}
		CmdTransmitter.sendDone();
	}

	static int res;
	static long l1;

}
