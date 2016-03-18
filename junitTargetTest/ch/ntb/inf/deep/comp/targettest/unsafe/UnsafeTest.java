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

import ch.ntb.inf.deep.runtime.ppc32.Decrementer;
import ch.ntb.inf.deep.unsafe.US;
import ch.ntb.inf.junitTarget.*;

/**
 * NTB 4.3.2011, Urs Graf
 * 
 *         Changes:
 */
@MaxErrors(100)
public class UnsafeTest {
	static final int addr = 0x350; // choose an address in the exception vector space, which is unused
	
	@Test
	public static void testGetPut() {
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			US.PUT1(addr, 0x11);
			US.PUT1(addr + 1, 0x22);
			US.PUT2(addr + 2, 0x3344);
			US.PUT4(addr + 4, 0x55667788);
			Assert.assertEquals("Test1", 0x11, US.GET1(addr));
			Assert.assertEquals("Test2", 0x33445566, US.GET4(addr + 2));
			Assert.assertEquals("Test3", 0x1122334455667788L, US.GET8(addr));
			US.PUT8(addr, 0xccddaabb99887755L);
			Assert.assertEquals("Test1", 0xccddaabb99887755L, US.GET8(addr));
		}
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testBit() {
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			US.PUT1(addr, 0xf0);
			Assert.assertFalse("Test1", US.BIT(addr, 0));
			Assert.assertTrue("Test2", US.BIT(addr, 7));
			Assert.assertTrue("Test3", US.BIT(addr, 4));
			Assert.assertFalse("Test4", US.BIT(addr, 3));
		}
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testRegisters() {
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			// make sure register is not used by this method
			US.PUTGPR(6, 0x3456789a);
			Assert.assertEquals("Test1", 0x3456789a, US.GETGPR(6));
			US.PUTFPR(6, 3.67548E23);
			Assert.assertEquals("Test2", 3.67548E23, US.GETFPR(6), 0.0);
			US.PUTSPR(272, 0x3456789a);
			Assert.assertEquals("Test3", 0x3456789a, US.GETSPR(272));
		}
		CmdTransmitter.sendDone();
	}

	static int res;
	static long l1;

	@Test
	// tests use of floats in exceptions 
	public static void testFloatsInExc() {
		if (CmdTransmitter.plattform != CmdTransmitter.host) {
			US.PUTFPR(0, 200.0);
			US.PUTFPR(2, 201.0);
			US.PUTFPR(12, 212.0);
			US.PUTFPR(20, 220.0);
			US.PUTFPR(30, 230.0);
			US.PUTFPR(31, 231.0);
			double d1 = 1.0;
			res = 1;
			Assert.assertEquals("Test1", 1, res);
			new DecTest();
			Assert.assertEquals("Test2", 1, res);
			d1 += Math.sin(0.5);
			double a = 1.3, b = -1.8;
			for (int i = 0; i < 5000; i++);
			double c = a + b;
			Assert.assertEquals("Test3", 3, res);
			Assert.assertEquals("Test4", -0.5, c, 0.0);
			Assert.assertEquals("Test5", 103, l1);
			Assert.assertEquals("Test6", 1.479425538, d1, 1e-5);
			Assert.assertEquals("Test10", 0.0, US.GETFPR(0), 1e-5);
			Assert.assertEquals("Test11", 0.0, US.GETFPR(2), 1e-5);	// used in Math.sin
			Assert.assertEquals("Test12", 212.0, US.GETFPR(12), 1e-5);
			Assert.assertEquals("Test13", 120.0, US.GETFPR(20), 1e-5);	// from DecTest.action, this nonvolatile is not saved!
			Assert.assertEquals("Test14", 1.4794255386, US.GETFPR(30), 1e-5);	// double d1
			Assert.assertEquals("Test15", -0.5, US.GETFPR(31), 1e-5);	// double c
		}
		CmdTransmitter.sendDone();
	}

}

class DecTest extends Decrementer {
	
	public void action () {
		US.ENABLE_FLOATS();
		US.PUTFPR(0, 100.0);
		US.PUTFPR(1, 101.0);
		US.PUTFPR(12, 112.0);
		US.PUTFPR(20, 120.0);
		US.PUTFPR(30, 130.0);
		US.PUTFPR(31, 131.0);
		int n = 11;
		int m = 21;
		this.decPeriodUs = -1;
		UnsafeTest.res++;
		double a = 57.4, b = 10.9;
		double c = a + b + 3;
		@SuppressWarnings("unused")
		double d = 1.0;
		d += Math.sin(0.1);
		UnsafeTest.l1 = (long)c + n + m;
	}
	
	// there are 2 dec exceptions, the first after decPeriodUs, then the second again after decPeriodUs. Though
	// during excecution of the first the period is set to the maximum this takes effect only by the 2nd 
	// excecution of the action method
	DecTest() {
		decPeriodUs = 1000;
		Decrementer.install(this);
	}
}
