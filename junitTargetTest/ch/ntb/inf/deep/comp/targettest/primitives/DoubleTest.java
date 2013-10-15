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

package ch.ntb.inf.deep.comp.targettest.primitives;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 02.12.2009
 * 
 * @author Roger Millischer
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class DoubleTest {

	private static DoubleTest objA, objB;

	private static double staticVar = 4.23;
	private static double zero = 0.0;
	
	private static final double NaN = 0d / 0d;
	private static final double NEGATIVE_INFINITY = -1d / 0d;
	private static final double POSITIVE_INFINITY = 1d / 0d;

	// Global assignment and calculation
	private double var = staticVar; // expected 4.23

	private double sub = (2 - var); // expected -2.23
	private double add = (789456 + var); // expected 789460.23
	private double mult = (58 * var); // expected 245.34
	private double div = (4649321 / var); // expected 1099130.2600472812
	private double rem = (8717867 % var); // expected 3.849999912151592
	private double preInc = (13 + ++var); // expected 18.23
	private double preDec = (13 + --var); // expected 17.23
	private double postInc = (13 + var++); // expected 17.23
	private double postDec = (13 + var--); // expected 18.23
	private double nan = (-var / zero) * zero; // expected NaN
	private double negInf = (-var / zero) * 0.5;// expected -Infinity
	private double posInf = (var / zero) * (1.0 / zero); // expected Infinity

	public DoubleTest() {
	}

	// Constructor assignment and calculation
	public DoubleTest(double var) {
		this.var = var; // expected 6.89
		sub = (3 - var); // expected -3.8899999
		add = (15654237 + var); // expected 15654243.89
		mult = (846 * var); // expected 5828.94
		div = (154812 / var); // expected 22469.086
		rem = (154812 % var); // expected 0.59299994
		preInc = (13 + ++var); // expected 20.89
		preDec = (13 + --var); // expected 19.89
		postInc = (13 + var++); // expected 19.89
		postDec = (13 + var--); // expected 20.89
		nan = (-var / zero) * zero; // expected NaN
		negInf = (-var / zero) * 0.5;// expected -Infinity
		posInf = (var / zero) * (1.0 / zero); // expected Infinity
	}

	@Before
	public static void setUp() {
		objA = new DoubleTest();
		objB = new DoubleTest(6.89);
		CmdTransmitter.sendDone();
	}

	@Test
	// test local and global variables
	public static void testVar() {
		double var = 7.95, staticVar = 41.789;
		Assert.assertEquals("localVar", 7.95, var, 0.0000001);
		Assert
				.assertEquals("localVar, same Name", 41.789, staticVar,
						0.0000001);
		Assert.assertEquals("staticVar", 4.23, DoubleTest.staticVar, 0.0000001);
		CmdTransmitter.sendDone();
	}

	@Test
	// test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", 4.23, objA.var, 0.0000001);
		Assert.assertEquals("add", 789460.23, objA.add, 0.0000001);
		Assert.assertEquals("sub", -2.23, objA.sub, 0.0000001);
		Assert.assertEquals("mult", 245.34, objA.mult, 0.0000001);
		Assert.assertEquals("div", 1099130.2600472, objA.div, 0.0000001);
		Assert.assertEquals("rem", 1.9699999, objA.rem, 0.0000001);
		Assert.assertEquals("preInc", 18.23, objA.preInc, 0.0000001);
		Assert.assertEquals("preDec", 17.23, objA.preDec, 0.0000001);
		Assert.assertEquals("postInc", 17.23, objA.postInc, 0.0000001);
		Assert.assertEquals("postDec", 18.23, objA.postDec, 0.0000001);
		Assert.assertEquals("nan", NaN, objA.nan, 0);
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, objA.negInf, 0);
		Assert.assertEquals("posInf", POSITIVE_INFINITY, objA.posInf, 0);
		CmdTransmitter.sendDone();
	}

	@Test
	// test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", 6.89, objB.var, 0.0000001);
		Assert.assertEquals("add", 15654243.89, objB.add, 0.0000001);
		Assert.assertEquals("sub", -3.8899999, objB.sub, 0.0000001);
		Assert.assertEquals("mult", 5828.94, objB.mult, 0.0000001);
		Assert.assertEquals("div", 22469.0856313, objB.div, 0.0000001);
		Assert.assertEquals("rem", 0.59, objB.rem, 0.0000001);
		Assert.assertEquals("preInc", 20.89, objB.preInc, 0.0000001);
		Assert.assertEquals("preDec", 19.89, objB.preDec, 0.0000001);
		Assert.assertEquals("postInc", 19.89, objB.postInc, 0.0000001);
		Assert.assertEquals("postDec", 20.89, objB.postDec, 0.0000001);
		Assert.assertEquals("nan", NaN, objA.nan, 0);
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, objA.negInf, 0);
		Assert.assertEquals("posInf", POSITIVE_INFINITY, objA.posInf, 0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test addition variants
	public static void testAdd() {
		double res, res2, res3, v1, v2;
		int v3;
		byte v4;
		// Normal addition test
		v1 = 789156.5671;
		v2 = 1238.159;
		res = (v1 + v2);
		Assert.assertEquals("normal", 790394.7261, res, 0.0000001);
		// Diverse types test
		v1 = 8971.1384;
		v2 = 4868.3684;
		v3 = 35419663;
		v4 = 127;
		res = (v1 + v2 + v3);
		v3 = 48689489;
		res2 = (v1 + v2 + v3);
		res3 = (v2 + v3 + v4);
		Assert.assertEquals("divTypes1", 3.54335025068E7, res, 0.0000001);
		Assert.assertEquals("divTypes2", 4.87033285068E7, res2, 0.0000001);
		Assert.assertEquals("divTypes3", 4.86944843684E7, res3, 0.0000001);
		// Short form test
		res = 2368.45;
		v1 = 84248.71;
		v3 = 8971456;
		res += v1 + v3;
		Assert.assertEquals("shortForm", 9058073.16, res, 0.0000001);
		// Post increment test
		res = 54684.239;
		Assert.assertEquals("postInc1", 54684.239, res++, 00.0000001);
		Assert.assertEquals("postInc2", 54685.239, res, 0.0000001);
		// Pre increment test
		res = 89156.963;
		Assert.assertEquals("preInc", 89157.963, ++res, 0.0000001);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test subtraction variants
	public static void testSub() {
		double res, res2, res3, v1, v2;
		int v3;
		byte v4;
		// Normal subtraction test
		v1 = 456239.156;
		v2 = 2336.4189;
		res = (v2 - v1);
		Assert.assertEquals("normal", -453902.7371, res, 0.0000001);
		// Diverse types test
		v1 = 66106.3549;
		v2 = 8915.156;
		v3 = 895893;
		v4 = 121;
		res = (v1 - v2 - v3);
		v3 = 869156;
		res2 = (v1 - v2 - v3);
		res3 = (v2 - v3 - v4);
		Assert.assertEquals("divTypes1", -838701.8011, res, 0.0000001);
		Assert.assertEquals("divTypes2", -811964.8011, res2, 0.0000001);
		Assert.assertEquals("divTypes3", -860361.844, res3, 0.0000001);
		// Short form test
		res = 12389.74;
		v1 = 4566.8156;
		v3 = 378410;
		res -= -v1 - v3;
		Assert.assertEquals("shortForm", 395366.5555999, res, 0.0000001);
		// Post decrement test
		res = -458.57;
		Assert.assertEquals("postDec1", -458.57, res--, 0.0000001);
		Assert.assertEquals("postDec2", -459.57, res, 0.0000001);
		// Pre decrement test
		res = -49814.65;
		Assert.assertEquals("preDec", -49815.65, --res, 0.0000001);
		CmdTransmitter.sendDone();
	}

	@Test
	// Multiplication test
	public static void testMult() {
		double res, res2, res3, v1;
		float v2;
		int v3;
		byte v4;
		// Normal multiplication test
		v1 = 14689.81;
		v2 = 5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("normal", 7.532288302433105E7, res, 0);
		// Negative multiplication test
		v1 = -14689.81;
		v2 = 5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("negative", -7.532288302433105E7, res, 0);
		// Negatives multiplication test
		v1 = -14689.81;
		v2 = -5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("negatives", 7.532288302433105E7, res, 0);
		// Diverse types test
		v1 = 14689.81;
		v2 = 151196.56f;
		v3 = 4832;
		v4 = 119;
		res = (v1 * v3);
		res2 = (v2 * v4);
		res3 = (v1 * v2);
		Assert.assertEquals("divTypes1", 7.098116192E7, res, 0);
		Assert.assertEquals("divTypes2", 1.799239E7, res2, 0);
		Assert.assertEquals("divTypes3", 2.221048775778125E9, res3, 0);
		// Short form test
		res = 14689.96;
		v1 = 1540.03;
		v2 = 11.72f;
		res *= v1 * v2;
		Assert.assertEquals("shortForm", 2.651413210789231E8, res, 0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Division test
	public static void testDiv() {
		double res, v1, v2;
		// Normal division test
		v1 = 5646.315;
		v2 = 347.356;
		res = (v1 / v2);
		Assert.assertEquals("normal", 16.2551244, res, 0.0000001);
		// Negative division test
		v1 = -21474.8364;
		v2 = 3458.77;
		res = (v1 / v2);
		Assert.assertEquals("negative", -6.2088072, res, 0.0000001);
		// Negatives division test
		v1 = -21474.8364;
		v2 = -3458.77;
		res = (v1 / v2);
		Assert.assertEquals("negatives", 6.2088072, res, 0.0000001);
		// Short form test
		res = 21474.8364;
		v1 = 3458.77;
		v2 = 100.1;
		res /= v1 / v2;
		Assert.assertEquals("shortForm", 621.50161, res, 0.0000001);

		CmdTransmitter.sendDone();
	}

	@Test
	// Remainder test
	public static void testRem() {
		double res, v1, v2;
		int v3;
		// Normal remainder test
		v1 = 4415.326;
		v2 = 316.17;
		res = (v1 % v2);
		Assert.assertEquals("normal", 305.1159999, res, 0.0000001);
		// Negative remainder test
		v1 = -4415.326;
		v2 = 316.17;
		res = (v1 % v2);
		Assert.assertEquals("negative", -305.1159999, res, 0.0000001);
		// Short form test
		res = 4415.326;
		v1 = 316.17;
		v3 = 618;
		res %= v1 % v3;
		Assert.assertEquals("shortForm", 305.1159999, res, 0.0000001);

		CmdTransmitter.sendDone();
	}

	@Test
	// Not a number test
	public static void testNaN() {
		double res, v1, v2;
		v1 = -654.32;
		v2 = 4189.723;
		res = (v1 / zero) - (1.0 / zero);
		Assert.assertEquals("NaN1", NaN, res, 0);
		res = (v2 / zero) / (1.0 / zero);
		Assert.assertEquals("NaN2", NaN, res, 0);

		CmdTransmitter.sendDone();
	}

	@Test
	// Infinity test
	public static void testInf() {
		double res, v1;
		// Negative Infinity
		v1 = -467.32;
		res = (v1 / zero) + 2.0;
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, res, 0);
		// Positive Infinity
		v1 = 467.32;
		res = (v1 / zero) * 0.5;
		Assert.assertEquals("negInf", POSITIVE_INFINITY, res, 0);

		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testPrio() {
		double res, v1, v2;
		// Grade 1 and 2
		v1 = 516.23;
		v2 = 984.456;
		res = (v1++ * v1-- / ++v2 % --v1);
		Assert.assertEquals("grade12", 270.9503447, res, 0.0000001);
		// Grade 2 and 3
		v1 = 516.23;
		v2 = 984.456;
		res = (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2);
		Assert.assertEquals("grade23", -506234.9018695, res, 0.0000001);

		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testBraces() {
		double res, v1, v2;
		// Grade 2 and 3
		v1 = 516.23;
		v2 = 984.456;
		res = ((v1 + v2) / (--v1 - v2) * (v1 + v2++) % (v1 + v2));
		Assert.assertEquals("grade23", -294.2614379, res, 0.0000001);

		CmdTransmitter.sendDone();
	}

}
