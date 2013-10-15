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
 * NTB 30.01.2009
 * 
 * @author Simon Pertschy
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class ByteTest {

	private static ByteTest objA, objB;

	private static byte staticVar = 4;
	private static byte staticHex = 0x12;
	private static byte staticOct = 024;

	//Global assignment and calculation
	private byte var = staticVar, hex = staticHex, oct = staticOct; // expected 4,18,20	
	private byte sub = (byte) (1 - var); // expected -3
	private byte add = (byte) (127 + var); // expected -125
	private byte mult = (byte) (40 * var); // expected -96
	private byte div = (byte) (118 / var); // expected 29
	private byte rem = (byte) (118 % var); // expected 2
	private byte lsr = (byte) (-33 >>> var); // expected -3
	private byte asr = (byte) (-33 >> var); // expected -3
	private byte asl = (byte) (13 << var); // expected -48
	private byte and = (byte) (117 & oct); // expected 20
	private byte or = (byte) (117 | oct); // expected 117
	private byte xor = (byte) (117 ^ oct); // expected 97
	private byte not = (byte) ~oct; // expected -21
	private byte preInc = (byte) (13 + ++hex); // expected 32
	private byte preDec = (byte) (13 + --hex); // expected 31
	private byte postInc = (byte) (13 + hex++); // expected 31
	private byte postDec = (byte) (13 + hex--); // expected 32

	public ByteTest() {
	}
	
	//Constructor assignment and calculation
	public ByteTest(byte var, byte hex, byte oct) {
		this.var = var; // expected 3
		this.hex = hex; // expected 19
		this.oct = oct; // expected 21
		this.sub = (byte) (1 - var); // expected -2
		this.add = (byte) (127 + var); // expected -126
		this.mult = (byte) (40 * var); // expected 120
		this.div = (byte) (118 / var); // expected 39
		this.rem = (byte) (118 % var); // expected 1
		this.lsr = (byte) (-33 >>> var); // expected -5
		this.asr = (byte) (-33 >> var); // expected -5
		this.asl = (byte) (13 << var); // expected 104
		this.and = (byte) (117 & oct); // expected 21
		this.or = (byte) (117 | oct); // expected 117
		this.xor = (byte) (117 ^ oct);// expected 96
		this.not = (byte) ~oct; // expected -22
		this.preInc = (byte) (13 + ++hex); // expected 33
		this.preDec = (byte) (13 + --hex); // expected 32
		this.postInc = (byte) (13 + hex++); // expected 32
		this.postDec = (byte) (13 + hex--); // expected 33
	}

	@Before
	public static void setUp() {
		objA = new ByteTest();
		objB = new ByteTest((byte) 3, (byte) 0x13, (byte) 025);
		CmdTransmitter.sendDone();
	}

	@Test
	//test local and global variables
	public static void testVar() {
		byte var = 15, staticVar = 12;
		Assert.assertEquals("localVar", (byte) 15, var);
		Assert.assertEquals("localVar, same Name", (byte) 12, staticVar);
		Assert.assertEquals("staticVar", (byte) 4, ByteTest.staticVar);
		Assert.assertEquals("staticHex", (byte) 18, ByteTest.staticHex);
		Assert.assertEquals("staticOct", (byte) 20, ByteTest.staticOct);
		CmdTransmitter.sendDone();
	}

	@Test
	//test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", (byte) 4, objA.var);
		Assert.assertEquals("hex", (byte) 18, objA.hex);
		Assert.assertEquals("oct", (byte) 20, objA.oct);

		Assert.assertEquals("sub", (byte) -3, objA.sub);
		Assert.assertEquals("add", (byte) -125, objA.add);
		Assert.assertEquals("mult", (byte) -96, objA.mult);
		Assert.assertEquals("div", (byte) 29, objA.div);
		Assert.assertEquals("rem", (byte) 2, objA.rem);
		Assert.assertEquals("lsr", (byte) -3, objA.lsr);
		Assert.assertEquals("asr", (byte) -3, objA.asr);
		Assert.assertEquals("asl", (byte) -48, objA.asl);
		Assert.assertEquals("and", (byte) 20, objA.and);
		Assert.assertEquals("or", (byte) 117, objA.or);
		Assert.assertEquals("xor", (byte) 97, objA.xor);
		Assert.assertEquals("not", (byte) -21, objA.not);
		Assert.assertEquals("preInc", (byte) 32, objA.preInc);
		Assert.assertEquals("preDec", (byte) 31, objA.preDec);
		Assert.assertEquals("postInc", (byte) 31, objA.postInc);
		Assert.assertEquals("postDec", (byte) 32, objA.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", (byte) 3, objB.var);
		Assert.assertEquals("hex", (byte) 19, objB.hex);
		Assert.assertEquals("oct", (byte) 21, objB.oct);

		Assert.assertEquals("sub", (byte) -2, objB.sub);
		Assert.assertEquals("add", (byte) -126, objB.add);
		Assert.assertEquals("mult", (byte) 120, objB.mult);
		Assert.assertEquals("div", (byte) 39, objB.div);
		Assert.assertEquals("rem", (byte) 1, objB.rem);
		Assert.assertEquals("lsr", (byte) -5, objB.lsr);
		Assert.assertEquals("asr", (byte) -5, objB.asr);
		Assert.assertEquals("asl", (byte) 104, objB.asl);
		Assert.assertEquals("and", (byte) 21, objB.and);
		Assert.assertEquals("or", (byte) 117, objB.or);
		Assert.assertEquals("xor", (byte) 96, objB.xor);
		Assert.assertEquals("not", (byte) -22, objB.not);
		Assert.assertEquals("preInc", (byte) 33, objB.preInc);
		Assert.assertEquals("preDec", (byte) 32, objB.preDec);
		Assert.assertEquals("postInc", (byte) 32, objB.postInc);
		Assert.assertEquals("postDec", (byte) 33, objB.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//Test addition variants
	public static void testAdd() {
		byte res,v1, v2,v3;
		//Normal addition test
		v1 = 10; v2 = 23;
		res =(byte) (v1 + v2);
		Assert.assertEquals("normal",(byte) 33,res);
		//Addition with 0 and -1
		v1 = 5; v2 = 0;
		res =(byte) (v1 + v2);
		Assert.assertEquals("normal",(byte) 5,res);
		v1 = 5; v2 = -1;
		res =(byte) (v1 + v2);
		Assert.assertEquals("normal",(byte) 4,res);		
		//Positive overflow test
		v1 = 127; v2 = 126; v3 = 125;
		res = (byte) (v1 + v2 + v3);
		Assert.assertEquals("posOverflow",(byte) 122,res);
		//Negative overflow test
		v1 = -128; v2 = -127; v3 = -126;
		res = (byte) (v1 + v2 + v3);
		Assert.assertEquals("negOverflow",(byte) -125,res);
		//Short form test
		res = 10; v1 =13; v2 = 15;
		res += v1 + v2;
		Assert.assertEquals("shortForm",(byte) 38,res);
		//Post increment test
		res = 10;
		Assert.assertEquals("postInc1",(byte) 10,res++);
		Assert.assertEquals("postInc2",(byte) 11,res);
		//Pre increment test
		res = 10;
		Assert.assertEquals("preInc",(byte) 11,++res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Test subtraction variants
	public static void testSub(){
		byte res,v1, v2,v3;
		//Normal subtraction test
		v1 = 10; v2 = 23;
		res =(byte) (v2 - v1);
		Assert.assertEquals("normal",(byte) 13,res);
		//Subtraction with 0 and -1
		v1 = 0; v2 = 10;
		res =(byte) (v2 - v1);
		Assert.assertEquals("normal",(byte) 10,res);
		v1 = -1; v2 = 10;
		res =(byte) (v2 - v1);
		Assert.assertEquals("normal",(byte) 11,res);
		//Positive overflow test
		v1 = 127; v2 = -126; v3 = -125;
		res = (byte) (v1 - v2 - v3);
		Assert.assertEquals("posOverflow",(byte) 122,res);
		//Negative overflow test
		v1 = -128; v2 = 127; v3 = 126;
		res = (byte) (v1 - v2 - v3);
		Assert.assertEquals("negOverflow",(byte) -125,res);
		//Short form test
		res = 10; v1 =13; v2 = 15;
		res -= -v1 - v2;
		Assert.assertEquals("shortForm",(byte) 38,res);
		//Post decrement test
		res = -10;
		Assert.assertEquals("postDec1",(byte) -10,res--);
		Assert.assertEquals("postDec2",(byte) -11,res);
		//Pre decrement test
		res = -10;
		Assert.assertEquals("preDec",(byte) -11,--res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Multiplication test
	public static void testMult(){
		byte res,v1, v2;
		//Normal multiplication test
		v1 = 5; v2 = 13;
		res =(byte) (v2 * v1);
		Assert.assertEquals("normal",(byte) 65,res);
		//Multiplication with 0 and -1
		v1 = 10; v2 = 0;
		res =(byte) (v2 * v1);
		Assert.assertEquals("normal",(byte) 0,res);
		v1 = -1; v2 = 13;
		res =(byte) (v2 * v1);
		Assert.assertEquals("normal",(byte) -13,res);
		//Negative multiplication test
		v1 = -5; v2 = 13;
		res =(byte) (v2 * v1);
		Assert.assertEquals("negative",(byte) -65,res);
		//Negatives multiplication test
		v1 = -5; v2 = -13;
		res =(byte) (v2 * v1);
		Assert.assertEquals("negatives",(byte) 65,res);
		//Positive overflow test
		v1 = 12; v2 = 13;
		res = (byte) (v1 * v2);
		Assert.assertEquals("posOverflow",(byte) -100,res);
		//Short form test
		res = 2; v1 =5; v2 = 6;
		res *= v1 * v2;
		Assert.assertEquals("shortForm",(byte) 60,res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Division test
	public static void testDiv(){
		byte res,v1, v2;
		//Normal division test
		v1 = 31; v2 = 5;
		res =(byte) (v1 / v2);
		Assert.assertEquals("normal",(byte) 6,res);
		//Division with -1
		v1 = 31; v2 = -1;
		res =(byte) (v1 / v2);
		Assert.assertEquals("normal",(byte) -31,res);
		//Negative division test
		v1 = -31; v2 = 5;
		res =(byte) (v1 / v2);
		Assert.assertEquals("negative",(byte) -6,res);
		//Negatives division test
		v1 = -31; v2 = -5;
		res =(byte) (v1 / v2);
		Assert.assertEquals("negatives",(byte) 6,res);
		//Short form test
		res = 125; v1 =10; v2 = 2;
		res /= v1 / v2;
		Assert.assertEquals("shortForm",(byte) 25,res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Remainder test
	public static void testRem(){
		byte res,v1,v2;
		//Normal remainder test
		v1 = 22; v2 = 16;
		res = (byte) (v1 % v2);
		Assert.assertEquals("normal",(byte) 6,res);
		//Negative remainder test
		v1 = -22; v2 = 16;
		res = (byte) (v1 % v2);
		Assert.assertEquals("negative",(byte) -6,res);
		//Short form test
		res = 127; v1 = 10; v2 = 4;
		res %= v1 % v2;
		Assert.assertEquals("shortForm",(byte) 1,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Logical shift right test
	public static void testLsr(){
		byte res,v1,v2;
		//Normal lsr test
		v1 = 33; v2 = 3;
		res = (byte) (v1 >>> v2);
		Assert.assertEquals("normal",(byte) 4,res);
		//Negative lsr test
		v1 = -5; v2 = 1;
		res = (byte) (v1 >>> v2);
		Assert.assertEquals("negative",(byte) -3,res);
		//Short form test
		res = 127; v1 = 4; v2 = 1;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm",(byte) 31,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift right test
	public static void testAsr(){
		byte res,v1,v2;
		//Normal asr test
		v1 = 33; v2 = 3;
		res = (byte) (v1 >> v2);
		Assert.assertEquals("normal",(byte) 4,res);
		//Negative asr test
		v1 = -5; v2 = 1;
		res = (byte) (v1 >> v2);
		Assert.assertEquals("negative",(byte) -3,res);
		//Short form test
		res = 127; v1 = 4; v2 = 1;
		res >>= v1 >> v2;
		Assert.assertEquals("shortForm",(byte) 31,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift left test
	public static void testAsl(){
		byte res,v1,v2;
		//Normal asl test
		v1 = 15; v2 = 3;
		res = (byte) (v1 << v2);
		Assert.assertEquals("normal",(byte) 120,res);
		//Negative asl test
		v1 = 30; v2 = 3;
		res = (byte) (v1 << v2);
		Assert.assertEquals("negative",(byte) -16,res);
		//Short form test
		res = 15; v1 = 2; v2 = 1;
		res <<= v1 << v2;
		Assert.assertEquals("shortForm",(byte) -16,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//And test
	public static void testAnd(){
		byte res,v1,v2;
		//Normal and test
		v1 = 0x46; v2 = 0x3F;
		res = (byte) (v1 & v2);
		Assert.assertEquals("normal",(byte) 0x06,res);
		//Negative and test
		v1 = -0x53; v2 = 0x64;
		res = (byte) (v1 & v2);
		Assert.assertEquals("negative",(byte) 0x24,res);
		//Short form test
		res = 0xF; v1 = -0x53; v2 = 0x64;
		res &= v1 & v2;
		Assert.assertEquals("shortForm",(byte) 0x4,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Or test
	public static void testOr(){
		byte res,v1,v2;
		//Normal and test
		v1 = 0x46; v2 = 0x3F;
		res = (byte) (v1 | v2);
		Assert.assertEquals("normal",(byte) 0x7F,res);
		//Negative and test
		v1 = -0x53; v2 = 0x64;
		res = (byte) (v1 | v2);
		Assert.assertEquals("negative",(byte) -0x13,res);
		//Short form test
		res = 0xF; v1 = -0x53; v2 = 0x64;
		res |= v1 | v2;
		Assert.assertEquals("shortForm",(byte) -0x11,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Xor test
	public static void testXor(){
		byte res,v1,v2;
		//Normal and test
		v1 = 0x5D; v2 = 0x37;
		res = (byte) (v1 ^ v2);
		Assert.assertEquals("normal",(byte) 0x6A,res);
		//Negative and test
		v1 = -0x23; v2 = 0x64;
		res = (byte) (v1 ^ v2);
		Assert.assertEquals("negative",(byte) -0x47,res);
		//Short form test
		res = -0x1; v1 = -0x23; v2 = 0x64;
		res ^= v1 ^ v2;
		Assert.assertEquals("shortForm",(byte) 0x46,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Not test
	public static void testNot(){
		byte res,v1;
		//Normal and test
		v1 = 0x55;
		res = (byte)~v1;
		Assert.assertEquals("normal",(byte) -0x56,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testPrio(){
		byte res,v1,v2;
		//Grade 1 and 2
		v1 = 5; v2 = 8;
		res = (byte) (v1++ * ~v1-- / ++v2 % --v1); 
		Assert.assertEquals("grade12",(byte) -3,res);
		//Grade 2 and 3
		v1 = 5; v2 = 8;
		res = (byte) (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2); 
		Assert.assertEquals("grade23",(byte) -23,res);
		//Grade 3 and 4
		v1 = 6; v2 = 8;
		res = (byte) (v1 + v2 >> v2 - v1 << -v1 + v2 >>> v2 - v1); 
		Assert.assertEquals("grade34",(byte) 3,res);
		//Grade 4 and 7
		v1 = 0x2A; v2 = 1;
		res = (byte) (v1 >> v2 &  v1 << v2 & v1 >>> v2); 
		Assert.assertEquals("grade47",(byte) 0x14,res);
		//Grade 7 and 8
		v1 = 0x2A; v2 = 0x23;
		res = (byte) (v1 ^ v2 & v1); 
		Assert.assertEquals("grade78",(byte) 0x8,res);
		//Grade 8 and 9
		v1 = 0x2A; v2 = 0x23;
		res = (byte) (v1 ^ v2 | v1); 
		Assert.assertEquals("grade89",(byte) 0x2B,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testBraces(){
		byte res,v1,v2;
		//Grade 2 and 3
		v1 = 5; v2 = 8;
		res = (byte) ((v1 + v2) / (v1 - v2) * (v1 + v2) % (v1 + v2)); 
		Assert.assertEquals("grade23",(byte) 0,res);
		//Grade 3 and 4
		v1 = 6; v2 = 8;
		res = (byte) (v1 + (v2 >> v2) - (v1 << -v1) + (v2 >>> v2) - v1); 
		Assert.assertEquals("grade34",(byte)0,res);
		//Grade 4 and 7
		v1 = 0x2A; v2 = 1;
		res = (byte) (v1 >> (v2 &  v1) << (v2 & v1) >>> v2); 
		Assert.assertEquals("grade47",(byte) 0x15,res);
		//Grade 7 and 8
		v1 = 0x2A; v2 = 0x23;
		res = (byte) ((v1 ^ v2) & v1); 
		Assert.assertEquals("grade78",(byte) 0x8,res);
		//Grade 8 and 9
		v1 = 0x2A; v2 = 0x23;
		res = (byte) (v1 ^ (v2 | v1)); 
		Assert.assertEquals("grade89",(byte) 0x01,res);
		CmdTransmitter.sendDone();
	}
	
	
	
	
}
