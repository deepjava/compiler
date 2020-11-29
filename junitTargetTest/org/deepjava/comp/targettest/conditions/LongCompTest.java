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

package org.deepjava.comp.targettest.conditions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 29.02.2011
 * 
 * @author Urs Graf
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class LongCompTest {
	@Test
	public static void testLongComp1() {
		boolean res = false;
		long l1 = 2340345346346L, l2 = 2340345346346L;
		res = l1 == l2;
		Assert.assertTrue("equal1", res);
		res = l2 == l1;
		Assert.assertTrue("equal2", res);
		res = l1 != l2;
		Assert.assertFalse("notEqual1", res);
		res = l2 != l1;
		Assert.assertFalse("notEqual2", res);
		res = l2 > l1;
		Assert.assertFalse("greater1", res);
		res = l1 > l2;
		Assert.assertFalse("greater2", res);
		res = l2 >= l1;
		Assert.assertTrue("greaterOrEqual1", res);
		res = l1 >= l2;
		Assert.assertTrue("greaterOrEqual2", res);
		res = l1 < l2;
		Assert.assertFalse("less1", res);
		res = l2 < l1;
		Assert.assertFalse("less2", res);
		res = l1 <= l2;
		Assert.assertTrue("lessOrEqual1", res);
		res = l2 <= l1;
		Assert.assertTrue("lessOrEqual2", res);

		CmdTransmitter.sendDone();
	}

	@Test
	public static void testLongComp2() {
		boolean res = false;
		long l1 = 2340345346346L, l2 = 23403456346L;
		res = l1 == l2;
		Assert.assertFalse("equal1", res);
		res = l2 == l1;
		Assert.assertFalse("equal2", res);
		res = l1 != l2;
		Assert.assertTrue("notEqual1", res);
		res = l2 != l1;
		Assert.assertTrue("notEqual2", res);
		res = l2 > l1;
		Assert.assertFalse("greater1", res);
		res = l1 > l2;
		Assert.assertTrue("greater2", res);
		res = l2 >= l1;
		Assert.assertFalse("greaterOrEqual1", res);
		res = l1 >= l2;
		Assert.assertTrue("greaterOrEqual2", res);
		res = l1 < l2;
		Assert.assertFalse("less1", res);
		res = l2 < l1;
		Assert.assertTrue("less2", res);
		res = l1 <= l2;
		Assert.assertFalse("lessOrEqual1", res);
		res = l2 <= l1;
		Assert.assertTrue("lessOrEqual2", res);

		CmdTransmitter.sendDone();
	}

	@Test
	public static void testLongComp3() {
		boolean res = false;
		long l1 = 23403446346L, l2 = 2340346785346346L;
		res = l1 == l2;
		Assert.assertFalse("equal1", res);
		res = l2 == l1;
		Assert.assertFalse("equal2", res);
		res = l1 != l2;
		Assert.assertTrue("notEqual1", res);
		res = l2 != l1;
		Assert.assertTrue("notEqual2", res);
		res = l2 > l1;
		Assert.assertTrue("greater1", res);
		res = l1 > l2;
		Assert.assertFalse("greater2", res);
		res = l2 >= l1;
		Assert.assertTrue("greaterOrEqual1", res);
		res = l1 >= l2;
		Assert.assertFalse("greaterOrEqual2", res);
		res = l1 < l2;
		Assert.assertTrue("less1", res);
		res = l2 < l1;
		Assert.assertFalse("less2", res);
		res = l1 <= l2;
		Assert.assertTrue("lessOrEqual1", res);
		res = l2 <= l1;
		Assert.assertFalse("lessOrEqual2", res);

		CmdTransmitter.sendDone();
	}
}
