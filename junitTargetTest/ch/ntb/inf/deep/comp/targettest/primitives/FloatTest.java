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
 * NTB 01.12.2009
 * 
 * @author Roger Millischer
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class FloatTest {

	private static FloatTest objA, objB;

	private static float staticVar = 4.23f; 
	private static float zero = 0.0f;
	private static final float 	NaN =	0f/0f;
	private static final float 	NEGATIVE_INFINITY =	-1f/0f;
	private static final float 	POSITIVE_INFINITY =	1f/0f;

	// Global assignment and calculation
	private float var = staticVar; // expected 4.23
	private float add = (789456 + var); // expected 789460.23
	private float sub = (2 - var); // expected -2.23
	private float mult = (58 * var); // expected 245.34
	private float div = (4649321 / var); // expected 1099130.2
	private float rem = (15.68f % var); // expected 2.99f
	private float preInc = (13 + ++var); // expected 18.23
	private float preDec = (13 + --var); // expected 17.23
	private float postInc = (13 + var++); // expected 17.23
	private float postDec = (13 + var--); // expected 18.23
	private float nan = (-var / zero) * zero; // expected NaN
	private float negInf = (-var / zero) * 0.5f;// expected -Infinity
	private float posInf = (var / zero) * (1.0f / zero); // expected Infinity

	public FloatTest() {
	}

	// Constructor assignment and calculation
	public FloatTest(float var) {
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
		negInf = (-var / zero) * 0.5f;// expected -Infinity
		posInf = (var / zero) * (1.0f / zero); // expected Infinity
	}

	@Before
	public static void setUp() {
		objA = new FloatTest();
		objB = new FloatTest(6.89f);
		CmdTransmitter.sendDone();
	}

	@Test
	// test local and global variables
	public static void testVar() {
		float var = 7.95f, staticVar = 41.789f;
		Assert.assertEquals("localVar", 7.95f, var, 0);
		Assert.assertEquals("localVar, same Name", 41.789f, staticVar, 0);
		Assert.assertEquals("staticVar", 4.23f, FloatTest.staticVar, 0);
		CmdTransmitter.sendDone();
	}

	@Test
	// test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", 4.23f, objA.var,0);
		Assert.assertEquals("add", 789460.23f, objA.add,0);
		Assert.assertEquals("sub", -2.23f, objA.sub,0);
		Assert.assertEquals("mult", 245.34f, objA.mult,0);
		Assert.assertEquals("div", 1099130.2f, objA.div,0);
		Assert.assertEquals("rem", 2.9900002f, objA.rem, 0);
		Assert.assertEquals("preInc", 18.23f, objA.preInc,0);
		Assert.assertEquals("preDec", 17.23f, objA.preDec,0);
		Assert.assertEquals("postInc", 17.23f, objA.postInc,0);
		Assert.assertEquals("postDec", 18.23f, objA.postDec,0);
		Assert.assertEquals("nan", NaN, objA.nan,0);
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, objA.negInf,0);
		Assert.assertEquals("posInf", POSITIVE_INFINITY, objA.posInf,0);
		CmdTransmitter.sendDone();
	}

	@Test
	// test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", 6.89f, objB.var,0);
		Assert.assertEquals("add", 15654243.89f, objB.add,0);
		Assert.assertEquals("sub", -3.8899999f, objB.sub,0);
		Assert.assertEquals("mult", 5828.94f, objB.mult,0);
		Assert.assertEquals("div", 22469.086f, objB.div,0);
		Assert.assertEquals("rem", 0.59299994f, objB.rem,0);
		Assert.assertEquals("preInc", 20.89f, objB.preInc,0);
		Assert.assertEquals("preDec", 19.89f, objB.preDec,0);
		Assert.assertEquals("postInc", 19.89f, objB.postInc,0);
		Assert.assertEquals("postDec", 20.89f, objB.postDec,0);
		Assert.assertEquals("nan", NaN, objA.nan,0);
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, objA.negInf,0);
		Assert.assertEquals("posInf", POSITIVE_INFINITY, objA.posInf,0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test addition variants
	public static void testAdd() {
		float res, res2, res3, v1, v2;
		int v3;
		byte v4;
		// Normal addition test
		v1 = 789156.5671f;
		v2 = 1238.159f;
		res = (v1 + v2);
		Assert.assertEquals("normal", 790394.7261f, res, 0);
		// Diverse types test
		v1 = 8971.1384f;
		v2 = 4868.3684f;
		v3 = 35419663;
		v4 = 127;
		res = (v1 + v2 + v3);
		v3 = 48689489;
		res2 = (v1 + v2 + v3);
		res3 = (v2 + v3 + v4);
		Assert.assertEquals("divTypes1", 3.5433504E7f, res, 0);
		Assert.assertEquals("divTypes2", 4.8703328E7f, res2, 0);
		Assert.assertEquals("divTypes3", 4.8694484E7f, res3, 0);
		// Short form test
		res = 2368.45f;
		v1 = 84248.71f;
		v3 = 8971456;
		res += v1 + v3;
		Assert.assertEquals("shortForm", 9058073.16f, res, 0);
		// Post increment test
		res = 54684.239f;
		Assert.assertEquals("postInc1", 54684.239f, res++, 0);
		Assert.assertEquals("postInc2", 54685.239f, res, 0);
		// Pre increment test
		res = 89156.963f;
		Assert.assertEquals("preInc", 89157.963f, ++res, 0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test subtraction variants
	public static void testSub() {
		float res, res2, res3, v1, v2;
		int v3;
		byte v4;
		// Normal subtraction test
		v1 = 456239.156f;
		v2 = 2336.4189f;
		res = (v2 - v1);
		Assert.assertEquals("normal", -453902.75f, res,0);
		// Diverse types test
		v1 = 66106.3549f;
		v2 = 8915.156f;
		v3 = 895893;
		v4 = 121;
		res = (v1 - v2 - v3);
		v3 = 869156;
		res2 = (v1 - v2 - v3);
		res3 = (v2 - v3 - v4);
		Assert.assertEquals("divTypes1", -838701.8f, res,0);
		Assert.assertEquals("divTypes2", -811964.8f, res2,0);
		Assert.assertEquals("divTypes3", -860361.9f, res3,0);
		// Short form test
		res = 12389.74f;
		v1 = 4566.8156f;
		v3 = 378410;
		res -= -v1 - v3;
		Assert.assertEquals("shortForm", 395366.56f, res,0);
		// Post decrement test
		res = -458.57f;
		Assert.assertEquals("postDec1", -458.57f, res--,0);
		Assert.assertEquals("postDec2", -459.57f, res,0);
		// Pre decrement test
		res = -49814.65f;
		Assert.assertEquals("preDec", -49815.65f, --res,0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Multiplication test
	public static void testMult() {
		float res, res2, res3, v1, v2;
		int v3;
		byte v4;
		// Normal multiplication test
		v1 = 14689.81f;
		v2 = 5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("normal", 7.532288E7f, res,0);
		// Negative multiplication test
		v1 = -14689.81f;
		v2 = 5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("negative", -7.532288E7f, res,0);
		// Negatives multiplication test
		v1 = -14689.81f;
		v2 = -5127.56f;
		res = (v2 * v1);
		Assert.assertEquals("negatives", 7.532288E7f, res,0);
		// Diverse types test
		v1 = 14689.81f;
		v2 = 151196.56f;
		v3 = 4832;
		v4 = 119;
		res = (v1 * v3);
		res2 = (v2 * v4);
		res3 = (v1 * v2);
		Assert.assertEquals("divTypes1", 7.098116E7f, res,0);
		Assert.assertEquals("divTypes2", 1.799239E7f, res2,0);
		Assert.assertEquals("divTypes3", 2.22104883E9f, res3,0);
		// Short form test
		res = 14689.96f;
		v1 = 1540.03f;
		v2 = 11.72f;
		res *= v1 * v2;
		Assert.assertEquals("shortForm", 2.65141328E8f, res,0);
		CmdTransmitter.sendDone();
	}

	@Test
	// Division test
	public static void testDiv() {
		float res, v1, v2;
		// Normal division test
		v1 = 5646.315f;
		v2 = 347.356f;
		res = (v1 / v2 );
		Assert.assertEquals("normal", 16.255125f, res,0);
		// Negative division test
		v1 = -21474.8364f;
		v2 = 3458.77f;
		res = (v1 / v2);
		Assert.assertEquals("negative", -6.208807f, res,0);
		// Negatives division test
		v1 = -21474.8364f;
		v2 = -3458.77f;
		res = (v1 / v2);
		Assert.assertEquals("negatives", 6.208807f, res,0);
		// Short form test
		res = 21474.8364f;
		v1 = 3458.77f;
		v2 = 100.1f;
		res /= v1 / v2;
		Assert.assertEquals("shortForm", 621.5016f, res,0);

		CmdTransmitter.sendDone();
	}

	@Test
	// Remainder test
	public static void testRem() {
		float res, v1, v2;
		int v3;
		// Normal remainder test
		v1 = 4415.326f;
		v2 = 316.17f;
		res = (v1 % v2);
		Assert.assertEquals("normal", 305.116f, res,0);
		// Negative remainder test
		v1 = -4415.326f;
		v2 = 316.17f;
		res = (v1 % v2);
		Assert.assertEquals("negative", -305.116f, res,0);
		// Short form test
		res = 4415.326f;
		v1 = 316.17f;
		v3 = 618;
		res %= v1 % v3;
		Assert.assertEquals("shortForm", 305.116f, res,0);

		CmdTransmitter.sendDone();
	}

	@Test
	//Neg test
	public static void testNeg(){
		float f = 1.0f;
		Assert.assertEquals("neg1", -1.0f, -f, 0);                
		f = -1.783645e-7f;
		Assert.assertEquals("neg2", 1.783645e-7, -f, 1e-6);                

		CmdTransmitter.sendDone();
	}

	@Test
	// Not a number test
	public static void testNaN() {
		float res, v1, v2;
		v1 = -654.32f;
		v2 = 4189.723f;
		res = (v1 / zero) - (1.0f / zero);
		Assert.assertEquals("NaN1", NaN, res,0);
		res = (v2 / zero) / (1.0f /zero);
		Assert.assertEquals("NaN2", NaN, res,0);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Infinity test
	public static void testInf(){
		float res, v1;
		// Negative Infinity
		v1 = -467.32f;
		res = (v1 / zero) + 2.0f;
		Assert.assertEquals("negInf", NEGATIVE_INFINITY, res,0);
		//Positive Infinity
		v1= 467.32f;
		res = (v1 / zero) * 0.5f;
		Assert.assertEquals("negInf", POSITIVE_INFINITY, res,0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testPrio() {
		float res, v1, v2;
		// Grade 1 and 2
		v1 = 516.23f;
		v2 = 984.456f;
		res = (v1++ * v1-- / ++v2 % --v1);
		Assert.assertEquals("grade12", 270.95032f, res,0);
		// Grade 2 and 3
		v1 = 516.23f;
		v2 = 984.456f;
		res = (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2);
		Assert.assertEquals("grade23", -506234.88f, res,0);

		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testBraces() {
		float res, v1, v2;
		// Grade 2 and 3
		v1 = 516.23f;
		v2 = 984.456f;
		res = ((v1 + v2) / (--v1 - v2) * (v1 + v2) % (v1 + v2));
		Assert.assertEquals("grade23", -297.26123046875f, res, 0);

		CmdTransmitter.sendDone();
	}
	
}
