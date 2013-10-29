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

package ch.ntb.inf.deep.comp.targettest.statements;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
/**
 * NTB 02.12.2009
 * 
 * @author Roger Millischer
 *
 */
@MaxErrors(100)
public class DoWhileTest {

	public static int normal1(int c) {
		int res = -1;
		do {
			res = 11;
			c--;
		} while (c > 0);
		return res;
	}

	public static int normal2(byte c) {
		int res = 10;
		do {
			res += 1;
			c--;
		} while (c > 0);
		return res;
	}

	public static int normal3(byte c) {
		do {
			c += 1;
		} while (c > 0);
		return c;
	}

	@Test
	public static void testNormal() {
		int res;

		res = normal1(2);
		Assert.assertEquals("normal1", 11, res);
		res = normal1(0);
		Assert.assertEquals("normal2", 11, res);
		res = normal2((byte) 10);
		Assert.assertEquals("normal4", 20, res);
		res = normal2((byte) 127);
		Assert.assertEquals("normal5", 137, res);
		res = normal3((byte) 125);
		Assert.assertEquals("normal6", -128, res);

		CmdTransmitter.sendDone();
	}

	public static int noStatement(byte c) {
		int res = -1;
		do{ 
			;
			}while(c-- > 0);
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
		 do{
			if (c == 5)
				break;
			res += 1;
			c--;
		}while (c > 0);
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
		do {
			if (c % 2 == 0){
				c--;
				continue;
			}
			c--;
			res++;
		}while (c > 0);
		return res;
	}

	public static int continue2(int c) {
		int res = 0;
		int i = 0;
		// continue with label
		test: do{
			c--;
			int n = 7;
			while (n != 0) {
				n--;
				if (n % 4 == 0) {
					continue test;
				}
				res++;
			}
		}while (i < c) ;
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
}