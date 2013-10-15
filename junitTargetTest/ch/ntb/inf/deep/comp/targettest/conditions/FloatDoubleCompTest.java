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

package ch.ntb.inf.deep.comp.targettest.conditions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 23.06.2009
 * 
 * @author Jan Mrnak
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class FloatDoubleCompTest {

	// Float comparison tests
	public static int test92D000get(float f1, float f2) {
		if (f1 == f2)
			return 1;
		else
			return 0;
	}
	public static int test92D001get(float f1, float f2) {
		if (f1 != f2)
			return 1;
		else
			return 0;
	}
	public static int test92D002get(float f1, float f2) {
		if (f1 < f2)
			return 1;
		else
			return 0;
	}
	public static int test92D003get(float f1, float f2) {
		if (f1 <= f2)
			return 1;
		else
			return 0;
	}
	public static int test92D004get(float f1, float f2) {
		if (f1 > f2)
			return 1;
		else
			return 0;
	}
	public static int test92D005get(float f1, float f2) {
		if (f1 >= f2)
			return 1;
		else
			return 0;
	}

	@Test
	//
	public static void testFloatComp() {
		int	res;
		res = test92D000get(1.5f, 1.5f);	
		Assert.assertEquals("equal1", 1, res);
		res = test92D000get(1.5f, -1.5f);
		Assert.assertEquals("equal2", 0, res);
		res = test92D000get(-1.5f, -1.5f);
		Assert.assertEquals("equal3", 1, res);
		res = test92D000get(-1.5f, 1.5f);
		Assert.assertEquals("equal4", 0, res);
		res = test92D000get(0.0f, 0.0f);
		Assert.assertEquals("equal5", 1, res);
		
		res = test92D001get(1.5f, 1.5f);	
		Assert.assertEquals("notEqual1", 0, res);
		res = test92D001get(1.5f, -1.5f);
		Assert.assertEquals("notEqual2", 1, res);
		res = test92D001get(-1.5f, -1.5f);
		Assert.assertEquals("notEqual3", 0, res);
		res = test92D001get(-1.5f, 1.5f);
		Assert.assertEquals("notEqual4", 1, res);
		res = test92D001get(0.0f, 0.0f);
		Assert.assertEquals("notEqual5", 0, res);
		
		res = test92D002get(1.5f, 2.5f);
		Assert.assertEquals("less1", 1, res);
		res = test92D002get(-1.5f, 2.5f);
		Assert.assertEquals("less2", 1, res);
		res = test92D002get(2.5f, 1.5f);	
		Assert.assertEquals("less3", 0, res);
		res = test92D002get(2.5f, -1.5f);
		Assert.assertEquals("less4", 0, res);
		res = test92D002get(0.0f, 0.0f);
		Assert.assertEquals("less5", 0, res);
		
		res = test92D003get(1.5f, 2.5f);	
		Assert.assertEquals("lessOrEqual1", 1, res);
		res = test92D003get(-1.5f, 2.5f);
		Assert.assertEquals("lessOrEqual2", 1, res);
		res = test92D003get(2.5f, 1.5f);
		Assert.assertEquals("lessOrEqual3", 0, res);
		res = test92D003get(2.5f, -1.5f);
		Assert.assertEquals("lessOrEqual4", 0, res);
		res = test92D003get(0.0f, 0.0f);
		Assert.assertEquals("lessOrEqual5", 1, res);
		
		res = test92D004get(2.5f, 1.5f);	
		Assert.assertEquals("greater1", 1, res);
		res = test92D004get(2.5f, -1.5f);
		Assert.assertEquals("greater2", 1, res);
		res = test92D004get(1.5f, 2.5f);	
		Assert.assertEquals("greater3", 0, res);
		res = test92D004get(-1.5f, 2.5f);
		Assert.assertEquals("greater4", 0, res);
		res = test92D004get(0.0f, 0.0f);
		Assert.assertEquals("greater5", 0, res);
		
		res = test92D005get(2.5f, 1.5f);
		Assert.assertEquals("greaterOrEqual1", 1, res);
		res = test92D005get(2.5f, -1.5f);	
		Assert.assertEquals("greaterOrEqual2", 1, res);
		res = test92D005get(1.5f, 2.5f);	
		Assert.assertEquals("greaterOrEqual3", 0, res);
		res = test92D005get(-1.5f, 2.5f);	
		Assert.assertEquals("greaterOrEqual4", 0, res);
		res = test92D005get(0.0f, 0.0f);	
		Assert.assertEquals("greaterOrEqual5", 1, res);
		
		CmdTransmitter.sendDone();
	}

	// Double comparison tests
	public static int test92D010get(double d1, double d2) {
		if (d1 == d2)
			return 1;
		else
			return 0;
	}
	public static int test92D011get(double d1, double d2) {
		if (d1 != d2)
			return 1;
		else
			return 0;
	}
	public static int test92D012get(double d1, double d2) {
		if (d1 < d2)
			return 1;
		else
			return 0;
	}
	public static int test92D013get(double d1, double d2) {
		if (d1 <= d2)
			return 1;
		else
			return 0;
	}
	public static int test92D014get(double d1, double d2) {
		if (d1 > d2)
			return 1;
		else
			return 0;
	}
	public static int test92D015get(double d1, double d2) {
		if (d1 >= d2)
			return 1;
		else
			return 0;
	}
	
	@Test
	// Test double comparison
	public static void testDoubleComp() {
		int	res;
		res = test92D010get(1.5, 1.5);	
		Assert.assertEquals("equal1", 1, res);
		res = test92D010get(1.5, -1.5);
		Assert.assertEquals("equal2", 0, res);
		res = test92D010get(-1.5, -1.5);
		Assert.assertEquals("equal3", 1, res);
		res = test92D010get(-1.5, 1.5);	
		Assert.assertEquals("equal4", 0, res);
		res = test92D010get(0.0, 0.0);	
		Assert.assertEquals("equal5", 1, res);
		
		res = test92D011get(1.5, 1.5);	
		Assert.assertEquals("notEqual1", 0, res);
		res = test92D011get(1.5, -1.5);
		Assert.assertEquals("notEqual2", 1, res);
		res = test92D011get(-1.5, -1.5);
		Assert.assertEquals("notEqual3", 0, res);
		res = test92D011get(-1.5, 1.5);
		Assert.assertEquals("notEqual4", 1, res);
		res = test92D011get(0.0, 0.0);	
		Assert.assertEquals("notEqual5", 0, res);
		
		res = test92D012get(1.5, 2.5);
		Assert.assertEquals("less1", 1, res);
		res = test92D012get(-1.5, 2.5);
		Assert.assertEquals("less2", 1, res);
		res = test92D012get(2.5, 1.5);
		Assert.assertEquals("less3", 0, res);
		res = test92D012get(2.5, -1.5);	
		Assert.assertEquals("less4", 0, res);
		res = test92D012get(0.0, 0.0);	
		Assert.assertEquals("less5", 0, res);
		
		res = test92D013get(1.5, 2.5);	
		Assert.assertEquals("lessOrEqual1", 1, res);
		res = test92D013get(-1.5, 2.5);	
		Assert.assertEquals("lessOrEqual2", 1, res);
		res = test92D013get(2.5, 1.5);
		Assert.assertEquals("lessOrEqual3", 0, res);
		res = test92D013get(2.5, -1.5);
		Assert.assertEquals("lessOrEqual4", 0, res);
		res = test92D013get(0.0, 0.0);	
		Assert.assertEquals("lessOrEqual5", 1, res);
		
		res = test92D014get(2.5, 1.5);
		Assert.assertEquals("greater1", 1, res);
		res = test92D014get(2.5, -1.5);	
		Assert.assertEquals("greater2", 1, res);
		res = test92D014get(1.5, 2.5);
		Assert.assertEquals("greater3", 0, res);
		res = test92D014get(-1.5, 2.5);
		Assert.assertEquals("greater4", 0, res);
		res = test92D014get(0.0, 0.0);
		Assert.assertEquals("greater5", 0, res);
		
		res = test92D015get(2.5, 1.5);	
		Assert.assertEquals("greaterOrEqual1", 1, res);
		res = test92D015get(2.5, -1.5);
		Assert.assertEquals("greaterOrEqual2", 1, res);
		res = test92D015get(1.5, 2.5);	
		Assert.assertEquals("greaterOrEqual3", 0, res);
		res = test92D015get(-1.5, 2.5);
		Assert.assertEquals("greaterOrEqual4", 0, res);
		res = test92D015get(0.0, 0.0);	
		Assert.assertEquals("greaterOrEqual5", 1, res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//
	public static void testFloatComp2() {
		boolean res = false;
		float f1 = 1.5f, f2 = 2.5f;
		res = f1 == f1;
		Assert.assertTrue("equal1", res);
		res = f1 == f2;
		Assert.assertFalse("equal2", res);
		res = f2 != f1;
		Assert.assertTrue("notEqual1", res);
		res = f1 != f1;
		Assert.assertFalse("notEqual2", res);
		res = f2 > f1;
		Assert.assertTrue("greater1", res);
		res = f1 > f2;
		Assert.assertFalse("greater2", res);
		res = f2 >= f1;
		Assert.assertTrue("greaterOrEqual1", res);
		res = f1 >= f1;
		Assert.assertTrue("greaterOrEqual2", res);
		res = f1 >= f2;
		Assert.assertFalse("greaterOrEqual3", res);
		res = f1 < f2;
		Assert.assertTrue("less1", res);
		res = f2 < f1;
		Assert.assertFalse("less2", res);
		res = f1 <= f2;
		Assert.assertTrue("lessOrEqual1", res);
		res = f1 <= f1;
		Assert.assertTrue("lessOrEqual2", res);
		res = f2 <= f1;
		Assert.assertFalse("lessOrEqual3", res);

		CmdTransmitter.sendDone();
	}

	@Test
	//
	public static void testDoubleComp2() {
		boolean res = false;
		double f1 = 1.5, f2 = 2.5;
		res = f1 == f1;
		Assert.assertTrue("equal1", res);
		res = f1 == f2;
		Assert.assertFalse("equal2", res);
		res = f2 != f1;
		Assert.assertTrue("notEqual1", res);
		res = f1 != f1;
		Assert.assertFalse("notEqual2", res);
		res = f2 > f1;
		Assert.assertTrue("greater1", res);
		res = f1 > f2;
		Assert.assertFalse("greater2", res);
		res = f2 >= f1;
		Assert.assertTrue("greaterOrEqual1", res);
		res = f1 >= f1;
		Assert.assertTrue("greaterOrEqual2", res);
		res = f1 >= f2;
		Assert.assertFalse("greaterOrEqual3", res);
		res = f1 < f2;
		Assert.assertTrue("less1", res);
		res = f2 < f1;
		Assert.assertFalse("less2", res);
		res = f1 <= f2;
		Assert.assertTrue("lessOrEqual1", res);
		res = f1 <= f1;
		Assert.assertTrue("lessOrEqual2", res);
		res = f2 <= f1;
		Assert.assertFalse("lessOrEqual3", res);

		CmdTransmitter.sendDone();
	}

}
