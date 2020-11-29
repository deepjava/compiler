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

package org.deepjava.comp.targettest.statements;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 02.12.2009
 * 
 */
@MaxErrors(100)
public class WhileTest {

	public static int normal1(int c) {
		int res = -1;
		while (c > 0) {
			res = 11;
			c--;
		}
		return res;
	}

	public static int normal2(byte c) {
		int res = 10;
		while (c > 0) {
			res += 1;
			c--;
		}
		return res;
	}

	public static int normal3(byte c) {
		while (c > 0) {
			c += 1;
		}
		return c;
	}

	@Test
	public static void testNormal() {
		int res;

		res = normal1(2);
		Assert.assertEquals("normal1", 11, res);
		res = normal1(0);
		Assert.assertEquals("normal2", -1, res);
		res = normal1(1);
		Assert.assertEquals("normal3", 11, res);
		res = normal2((byte) 10);
		Assert.assertEquals("normal4", 20, res);
		res = normal2((byte) 127);
		Assert.assertEquals("normal5", 137, res);
		res = normal3((byte) 127);
		Assert.assertEquals("normal6", -128, res);

		CmdTransmitter.sendDone();
	}

	public static int noStatement(byte c) {
		int res = -1;
		while (c-- > 0)
			;
		return res;
	}

	// @Test
	public static void testNoStatement() {
		int res;

		res = noStatement((byte) 10);
		Assert.assertEquals("noStatement", -1, res);
		res = noStatement((byte) -10);
		Assert.assertEquals("noStatement", -1, res);

		CmdTransmitter.sendDone();
	}

	public static int break1(int c) {
		int res = -1;
		while (c > 0) {
			if (c == 5)
				break;
			res += 1;
			c--;
		}
		return res;
	}

	@Test
	public static void testBreak1() {
		int res;

		res = break1(7);
		Assert.assertEquals("break11", 1, res);
		res = break1(4);
		Assert.assertEquals("break12", 3, res);

		CmdTransmitter.sendDone();
	}

	public static int continue1(int c) {
		int res = 0;
		// continue without label
		while (c > 0) {
			if (c % 2 == 0) {
				c--;
				continue;
			}
			c--;
			res++;
		}
		return res;
	}

	public static int continue2(int c) {
		int res = 0;
		int i = 0;
		// continue with label
		test: while (i < c) {
			c--;
			int n = 7;
			while (n != 0) {
				n--;
				if (n % 4 == 0) {
					continue test;
				}
				res++;
			}
		}
		return res;
	}

	@Test
	public static void testContinue() {
		int res;

		res = continue1(7);
		Assert.assertEquals("continue11", 4, res);
		res = continue1(4);
		Assert.assertEquals("continue12", 2, res);
		res = continue2(7);
		Assert.assertEquals("continue21", 14, res);
		res = continue2(4);
		Assert.assertEquals("continue22", 8, res);

		CmdTransmitter.sendDone();
	}
	
	public static int whileTrue1() {
		int a = 5;
		int i = 0;
		while (true) {
			int b = a + i++;
			if (b > 10) return i;
		}
	}

	static short r = -10;
	static public int whileTrue2() {
		while (true) {
			if (r == -5) return r;
			r++;
		}
	}

	short x = 5;
	public int whileTrue3() {
		while (true) {
			if (x == 25) return x;
			x++;
		}
	}
	
	static byte s = 10;
	public static int whileTrue4(byte b1) {
		while (true) {
			if (b1 > s) return 1;
			s--;
		}
	}
	
	byte y = 10;
	int whileTrue5(byte b1) {
		while (true) {
			if (b1 > s) return 1;
			y--;
		}
	}
	
	@Test
	public static void testWhileTrue() {
		Assert.assertEquals("whileTrue1", 7, whileTrue1());
		Assert.assertEquals("whileTrue2", -5, whileTrue2());
		Assert.assertEquals("whileTrue3", 25, new WhileTest().whileTrue3());
		Assert.assertEquals("whileTrue4", 1, whileTrue4((byte) 5));
		Assert.assertEquals("whileTrue4", 1, new WhileTest().whileTrue5((byte) 5));

		CmdTransmitter.sendDone();
	}

}