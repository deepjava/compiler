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
 * NTB 17.06.2009
 * 
 * @author Jan Mrnak
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class ConditionTest {
	
	@Test
	//Test boolean
	public static void testBoolean(){
		int a, b;
		boolean	ba, bb;
		boolean	cond;
		
		ba = false;
		bb = !ba;
		Assert.assertFalse("normal", ba);
		Assert.assertTrue("negation", bb);

		a = 33; b = -44;
		cond = (a + b) == (a - b);
		Assert.assertFalse("equal", cond);
		cond = (a + b) != (a - b);
		Assert.assertTrue("notEqual1", cond);
		Assert.assertEquals("notEqual2", 33, a);
		Assert.assertEquals("notEqual3", -44, b);

		CmdTransmitter.sendDone();
	}

	@Test
	//Test boolean expressions 1
	public static void testBooleanExpr1(){
		int a = 0, b = 1;
		boolean	cond;
		cond = a != b;
		Assert.assertTrue("notEqual1", cond);
		Assert.assertEquals("notEqual2", 0, a);
		Assert.assertEquals("notEqual3", 1, b);
		cond = a <= b;
		Assert.assertTrue("lessOrEqual", cond);
		cond = a < b;
		Assert.assertTrue("less", cond);
		cond = a == b;
		Assert.assertFalse("equal", cond);
		cond = a > b;
		Assert.assertFalse("greater", cond);
		cond = a >= b;
		Assert.assertFalse("greaterOrEqual", cond);
		
		CmdTransmitter.sendDone();
	}

	@Test
	//Test boolean expressions 2
	public static void testBooleanExpr2(){
		int a = 0, b = -1;
		boolean	cond;
		cond = a != b;
		Assert.assertTrue("notEqual1", cond);
		Assert.assertEquals("notEqual2", 0, a);
		Assert.assertEquals("notEqual3", -1, b);
		cond = a <= b;
		Assert.assertFalse("lessOrEqual", cond);
		cond = a < b;
		Assert.assertFalse("less", cond);
		cond = a == b;
		Assert.assertFalse("equal", cond);
		cond = a > b;
		Assert.assertTrue("greater", cond);
		cond = a >= b;
		Assert.assertTrue("greaterOrEqual", cond);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Test boolean expressions 3 (immediates)
	public static void testBooleanExpr3(){
		int a = 10;
		Assert.assertTrue("test1", a == 10);
		Assert.assertTrue("test2", 10 == a);
		Assert.assertTrue("test3", a != 11);
		Assert.assertTrue("test4", 11 != a);
		Assert.assertFalse("test5", a > 10);
		Assert.assertFalse("test6", 10 < a);
		Assert.assertFalse("test7", a < 10);
		Assert.assertFalse("test8", 10 > a);
		Assert.assertTrue("test9", a >= 10);
		Assert.assertTrue("test10", a <= 10);
		Assert.assertTrue("test11", 10 <= a);
		Assert.assertTrue("test12", 10 >= a);
		Assert.assertTrue("test13", 11 >= a);
		Assert.assertFalse("test14", 11 <= a);
		
		CmdTransmitter.sendDone();
	}

	@Test
	//Test ternary variants
	public static void testTernary(){
		int	res, a, b;
		a = 1; b = 2;
		res = (a > b) ? 11 : 12;
		Assert.assertEquals("ternary11", 12, res);
		Assert.assertEquals("ternary12", 1, a);
		Assert.assertEquals("ternary13", 2, b);
		
		a = 1; b = 2;
		res = (a > b) ? (a + 5) : (b - 5);
		Assert.assertEquals("ternary21", -3, res);
		Assert.assertEquals("ternary22", 1, a);
		Assert.assertEquals("ternary23", 2, b);

		a = 1; b = 2;
		res = (a < b) ? (a + 5) : (b - 5);
		Assert.assertEquals("ternary31", 6, res);
		Assert.assertEquals("ternary32", 1, a);
		Assert.assertEquals("ternary33", 2, b);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Test ternary-assignment variants
	public static void testTernaryWithAssignment(){
		int	ia,	ib, ic, res;
		ia = 3;	ib = 17; ic = 5;
		res = (ia = ib + 2) > (ib = ia * ib)	?	ia = ib / ia	:	ia + (ic = 2) * 3;
		//		19			>	323												25
		Assert.assertEquals("ternaryWithAssignment11", 19, ia);
		Assert.assertEquals("ternaryWithAssignment12", 323, ib);
		Assert.assertEquals("ternaryWithAssignment13", 2, ic);
		Assert.assertEquals("ternaryWithAssignment14", 25, res);
		
		ia = 3;	ib = 17; ic = 5;
		res = (ia = ib + 2) < (ib = ia * ib)	?	ia = ib / ia	:	ia + (ic = 2) * 3;
		//		19			<	323					17
		Assert.assertEquals("ternaryWithAssignment21", 17, ia);
		Assert.assertEquals("ternaryWithAssignment22", 323, ib);
		Assert.assertEquals("ternaryWithAssignment23", 5, ic);
		Assert.assertEquals("ternaryWithAssignment24", 17, res);
		
		CmdTransmitter.sendDone();
	}
	
	//ternary-boolean variants
	public static int ternaryBoolean(boolean cond, int a, int b){
		int	res = -1;
		if (cond)	res =  a >= 0 ?	a	:	b;
		return res;
	}
	
	@Test
	//Test ternary-boolean variants
	public static void testTernaryWithBoolean(){
		int res;
		
		res = ternaryBoolean(false, 0,	11);
		Assert.assertEquals("ternaryWithBoolean1", -1, res);
		res = ternaryBoolean(true, 0,	11);
		Assert.assertEquals("ternaryWithBoolean2", 0, res);
		res = ternaryBoolean(true, 1,	11);
		Assert.assertEquals("ternaryWithBoolean3", 1, res);
		res = ternaryBoolean(true, -1,	11);
		Assert.assertEquals("ternaryWithBoolean4", 11, res);
		
		CmdTransmitter.sendDone();
	}

	//ternary-nesting variants
	public static int ternaryNesting1(boolean cL, boolean cR){
		int	res = 0;
		res = (cL? 3 : 5) > (cR? 3 : 4)	? 	11	:	12;
		return res;
	}

	public static int ternaryNesting2(boolean cL, boolean cR){
		int	res = 0;
		res = (cL? 3 : 5) <= (cR? 3 : 4)	? 	11	:	12;
		return res;
	}
	
	@Test
	//Test ternary-nesting variants
	public static void testTernaryNesting(){
		int	res;
		res = ternaryNesting1(false, false);
		Assert.assertEquals("testTernaryNesting11", 11, res);// 5 > 4:	11
		res = ternaryNesting1(false, true);
		Assert.assertEquals("testTernaryNesting12", 11, res);// 5 > 3:	11
		res = ternaryNesting1(true, false);
		Assert.assertEquals("testTernaryNesting13", 12, res);// 3 > 4:	12
		res = ternaryNesting1(true, true);
		Assert.assertEquals("testTernaryNesting14", 12, res);// 3 > 3:	12

		res = ternaryNesting2(false, false);
		Assert.assertEquals("testTernaryNesting21", 12, res);// 5 <= 4:	12
		res = ternaryNesting2(false, true);
		Assert.assertEquals("testTernaryNesting22", 12, res);// 5 <= 3:	12
		res = ternaryNesting2(true, false);
		Assert.assertEquals("testTernaryNesting23", 11, res);// 3 <= 4:	11
		res = ternaryNesting2(true, true);
		Assert.assertEquals("testTernaryNesting24", 11, res);// 3 <= 3:	11

		CmdTransmitter.sendDone();
	}
	
	
}
