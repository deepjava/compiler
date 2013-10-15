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
 */
@MaxErrors(100)
public class CharTest {
	private static CharTest objA, objB;

	private static char staticVar = '!'; // 33
	private static char staticHex = 0x22; // " or 34
	private static char staticOct = 043; // # or 35

	// Global assignment and calculation
	private char var = staticVar, hex = staticHex, oct = staticOct; // expected
	// !,",#
	private char sub = (char) (1 - var); 
	private char add = (char) (32755 + var); 
	private char mult = (char) (995 * var); 
	private char div = (char) (118 / var); 
	private char rem = (char) (118 % var); 
	private char lsr = (char) (-var >>> 4); 
	private char asr = (char) (-var >> var); 
	private char asl = (char) (var << 1); 
	private char and = (char) (117 & oct); 
	private char or = (char) (117 | oct); 
	private char xor = (char) (117 ^ oct); 
	private char not = (char) ~oct; 
	private char preInc = (char) (13 + ++hex);  
	private char preDec = (char) (13 + --hex); 
	private char postInc = (char) (13 + hex++);  
	private char postDec = (char) (13 + hex--); 

	public CharTest() {
	}

	// Constructor assignment and calculation
	public CharTest(char var, char hex, char oct) {
		this.var = var; // expected " or 34
		this.hex = hex; // expected # or 35
		this.oct = oct; // expected ! or 33
		this.sub = ((char) (1 - var));
		this.add = (char) (32755 + var); 
		this.mult = (char) (995 * var); 
		this.div = (char) (145 / var); 
		this.rem = (char) (145 % var); 
		this.lsr = (char) (-var >>> 3); 
		this.asr = (char) (-var >> var); 
		this.asl = (char) (var << 1); 
		this.and = (char) (116 & oct); 
		this.or = (char) (116 | oct); 
		this.xor = (char) (116 ^ oct);
		this.not = (char) ~oct; 
		this.preInc = (char) (13 + ++hex);
		this.preDec = (char) (13 + --hex); 
		this.postInc = (char) (13 + hex++); 
		this.postDec = (char) (13 + hex--); 
	}

	@Before
	public static void setUp() {
		objA = new CharTest();
		objB = new CharTest((char) '"', (char) 0x23, (char) 041);
		CmdTransmitter.sendDone();
	}

	@Test
	// test local and global variables
	public static void testVar() {
		char var = 15, staticVar = 12;
		Assert.assertEquals("localVar", (char) 15, var);
		Assert.assertEquals("localVar, same Name", (char) 12, staticVar);
		Assert.assertEquals("staticVar", (char) '!', CharTest.staticVar);
		Assert.assertEquals("staticHex", (char) 0x22, CharTest.staticHex);
		Assert.assertEquals("staticOct", (char) 043, CharTest.staticOct);
		CmdTransmitter.sendDone();
	}

	@Test
	// test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", (char) '!', objA.var);
		Assert.assertEquals("hex", (char) 0x22, objA.hex);
		Assert.assertEquals("oct", (char) 043, objA.oct);

		Assert.assertEquals("sub", (char) 65504, objA.sub);
		Assert.assertEquals("add", (char) 32788, objA.add);
		Assert.assertEquals("mult", (char) 32835, objA.mult);
		Assert.assertEquals("div", (char) 3, objA.div);
		Assert.assertEquals("rem", (char) 19, objA.rem);
		Assert.assertEquals("lsr", (char) 65533, objA.lsr);
		Assert.assertEquals("asr", (char) 65519, objA.asr);
		Assert.assertEquals("asl", (char) 66, objA.asl);
		Assert.assertEquals("and", (char) 33, objA.and);
		Assert.assertEquals("or", (char) 119, objA.or);
		Assert.assertEquals("xor", (char) 86, objA.xor);
		Assert.assertEquals("not", (char) 65500, objA.not);
		Assert.assertEquals("preInc", (char) 48, objA.preInc);
		Assert.assertEquals("preDec", (char) 47, objA.preDec);
		Assert.assertEquals("postInc", (char) 47, objA.postInc);
		Assert.assertEquals("postDec", (char) 48, objA.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	// test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", (char) '"', objB.var);
		Assert.assertEquals("hex", (char) 0x23, objB.hex);
		Assert.assertEquals("oct", (char) 041, objB.oct);

		Assert.assertEquals("sub", (char) 65503, objB.sub);
		Assert.assertEquals("add", (char) 32789, objB.add);
		Assert.assertEquals("mult", (char) 33830, objB.mult);
		Assert.assertEquals("div", (char) 4, objB.div);
		Assert.assertEquals("rem", (char) 9, objB.rem);
		Assert.assertEquals("lsr", (char) 65531, objB.lsr);
		Assert.assertEquals("asr", (char) 65527, objB.asr);
		Assert.assertEquals("asl", (char) 68, objB.asl);
		Assert.assertEquals("and", (char) 32, objB.and);
		Assert.assertEquals("or", (char) 117, objB.or);
		Assert.assertEquals("xor", (char) 85, objB.xor);
		Assert.assertEquals("not", (char) 65502, objB.not);
		Assert.assertEquals("preInc", (char) 49, objB.preInc);
		Assert.assertEquals("preDec", (char) 48, objB.preDec);
		Assert.assertEquals("postInc", (char) 48, objB.postInc);
		Assert.assertEquals("postDec", (char) 49, objB.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test addition variants
	public static void testAdd() {
		char res, v1, v2, v3;
		// Normal addition test
		v1 = 10;
		v2 = 23;
		res = (char) (v1 + v2);
		Assert.assertEquals("normal", (char) 33, res);
		// Positive overflow test
		v1 = 32767;
		v2 = 32766;
		v3 = 32765;
		res = (char) (v1 + v2 + v3);
		Assert.assertEquals("posOverflow", (char) 32762, res);
		// Negative overflow test
		v1 = (char) 32768;
		v2 = (char) 32769;
		v3 = (char) 32770;
		res = (char) (v1 - v2 - v3);
		Assert.assertEquals("negOverflow", (char) 32765, res);
		// Short form test
		res = 12000;
		v1 = 10500;
		v2 = 9555;
		res += v1 + v2;
		Assert.assertEquals("shortForm", (char) 32055, res);
		// Post increment test
		res = 23458;
		Assert.assertEquals("postInc1", (char) 23458, res++);
		Assert.assertEquals("postInc2", (char) 23459, res);
		// Pre increment test
		res = 17956;
		Assert.assertEquals("preInc", (char) 17957, ++res);

		CmdTransmitter.sendDone();
	}

	@Test
	// Test subtraction variants
	public static void testSub() {
		char res, v1, v2, v3;
		// Normal subtraction test
		v1 = 10987;
		v2 = 23456;
		res = (char) (v2 - v1);
		Assert.assertEquals("normal", (char) 12469, res);
		// Positive overflow test
		v1 = 32767;
		v2 = (char) -32766;
		v3 = (char) -32765;
		res = (char) (v1 - v2 - v3);
		Assert.assertEquals("posOverflow", (char) 32762, res);
		// Negative overflow test
		v1 = (char) 32768;
		v2 = 32767;
		v3 = 32766;
		res = (char) (v1 - v2 - v3);
		Assert.assertEquals("negOverflow", (char) 32771, res);
		// Short form test
		res = 4567;
		v1 = 7896;
		v2 = 1354;
		res -= -v1 - v2;
		Assert.assertEquals("shortForm", (char) 13817, res);
		// Post decrement test
		res = (char) 46938;
		Assert.assertEquals("postDec1", (char) 46938, res--);
		Assert.assertEquals("postDec2", (char) 46937, res);
		// Pre decrement test
		res = (char) 48683;
		Assert.assertEquals("preDec", (char) 48682, --res);

		CmdTransmitter.sendDone();
	}

	@Test
	// Multiplication test
	public static void testMult() {
		char res, v1, v2;
		// Normal multiplication test
		v1 = 51;
		v2 = 478;
		res = (char) (v2 * v1);
		Assert.assertEquals("normal", (char) 24378, res);
		// Positive overflow test
		v1 = 79;
		v2 = 513;
		res = (char) (v1 * v2);
		Assert.assertEquals("posOverflow", (char) 40527, res);
		// Short form test
		res = 29;
		v1 = 31;
		v2 = 32;
		res *= v1 * v2;
		Assert.assertEquals("shortForm", (char) 28768, res);

		CmdTransmitter.sendDone();
	}

	@Test
	// Division test
	public static void testDiv() {
		char res, v1, v2;
		// Normal division test
		v1 = 31589;
		v2 = 456;
		res = (char) (v1 / v2);
		Assert.assertEquals("normal", (char) 69, res);
		// Short form test
		res = 25665;
		v1 = 2478;
		v2 = 42;
		res /= v1 / v2;
		Assert.assertEquals("shortForm", (char) 435, res);

		CmdTransmitter.sendDone();
	}

	@Test
	// Remainder test
	public static void testRem() {
		char res, v1, v2;
		// Normal remainder test
		v1 = 3691;
		v2 = 387;
		res = (char) (v1 % v2);
		Assert.assertEquals("normal", (char) 208, res);
		// Short form test
		res = 32767;
		v1 = 981;
		v2 = 17;
		res %= v1 % v2;
		Assert.assertEquals("shortForm", (char) 7, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Logical shift right test
	public static void testLsr() {
		char res, v1, v2;
		// Normal lsr test
		v1 = 24631;
		v2 = 8;
		res = (char) (v1 >>> v2);
		Assert.assertEquals("normal", (char) 96, res);
		// Short form test
		res = 32767;
		v1 = 7;
		v2 = 1;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm", (char) 4095, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Arithmetic shift right test
	public static void testAsr() {
		char res, v1, v2;
		// Normal asr test
		v1 = 22568;
		v2 = 9;
		res = (char) (v1 >> v2);
		Assert.assertEquals("normal", (char) 44, res);
		// Short form test
		res = 32767;
		v1 = 36;
		v2 = 2;
		res >>= v1 >> v2;
		Assert.assertEquals("shortForm", (char) 63, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Arithmetic shift left test
	public static void testAsl() {
		char res, v1, v2;
		// Normal asl test
		v1 = 45;
		v2 = 7;
		res = (char) (v1 << v2);
		Assert.assertEquals("normal", (char) 5760, res);
		// Negative asl test
		v1 = 458;
		v2 = 9;
		res = (char) (v1 << v2);
		Assert.assertEquals("negative", (char) 37888, res);
		// Short form test
		res = 28697;
		v1 = 3;
		v2 = 2;
		res <<= v1 << v2;
		Assert.assertEquals("shortForm", (char) 36864, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// And test
	public static void testAnd() {
		char res, v1, v2;
		// Normal and test
		v1 = 0x4637;
		v2 = 0x3F5A;
		res = (char) (v1 & v2);
		Assert.assertEquals("normal", (char) 0x0612, res);
		// Short form test
		res = 0xF5A9;
		v1 = (char) -0x53BC;
		v2 = 0x64D4;
		res &= v1 & v2;
		Assert.assertEquals("shortForm", (char) 0x2400, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Or test
	public static void testOr() {
		char res, v1, v2;
		// Normal and test
		v1 = 0x463A;
		v2 = 0x3F8F;
		res = (char) (v1 | v2);
		Assert.assertEquals("normal", (char) 0x7FBF, res);
		// Short form test
		res = 0xF5A9;
		v1 = (char) -0x53BC;
		v2 = 0x64D4;
		res |= v1 | v2;
		Assert.assertEquals("shortForm", (char) -0x0203, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Xor test
	public static void testXor() {
		char res, v1, v2;
		// Normal and test
		v1 = 0x5DA8;
		v2 = 0x37B3;
		res = (char) (v1 ^ v2);
		Assert.assertEquals("normal", (char) 0x6A1B, res);
		// Short form test
		res = (char) -0x1;
		v1 = (char) -0x2391;
		v2 = 0x6473;
		res ^= v1 ^ v2;
		Assert.assertEquals("shortForm", (char) 0x47E3, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Not test
	public static void testNot() {
		char res, v1;
		// Normal and test
		v1 = 0x5555;
		res = (char) ~v1;
		Assert.assertEquals("normal", (char) 43690, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testPrio() {
		char res, v1, v2;
		// Grade 1 and 2
		v1 = 9303;
		v2 = 5688;
		res = (char) (v1++ * ~v1-- / ++v2 % --v1);
		Assert.assertEquals("grade12", (char) 59622, res);
		// Grade 2 and 3
		v1 = 9303;
		v2 = 5688;
		res = (char) (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2);
		Assert.assertEquals("grade23", (char) 58303, res);
		// Grade 3 and 4
		v1 = 6732;
		v2 = 28943;
		res = (char) (v1 + v2 >> v2 - v1 << -v1 + v2 >>> v2 - v1);
		Assert.assertEquals("grade34", (char) 4459, res);
		// Grade 4 and 7
		v1 = 0x2A37;
		v2 = 4;
		res = (char) (v1 >> v2 & v1 << v2 & v1 >>> v2);
		Assert.assertEquals("grade47", (char) 0x220, res);
		// Grade 7 and 8
		v1 = 0x2A5C;
		v2 = 0x239D;
		res = (char) (v1 ^ v2 & v1);
		Assert.assertEquals("grade78", (char) 0x840, res);
		// Grade 8 and 9
		v1 = 0x2A5C;
		v2 = 0x239D;
		res = (char) (v1 ^ v2 | v1);
		Assert.assertEquals("grade89", (char) 0x2BDD, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Priority rules test
	public static void testBraces() {
		char res, v1, v2;
		// Grade 2 and 3
		v1 = 9303;
		v2 = 5688;
		res = (char) ((v1 + v2) / (v1 - v2) * (v1 + v2) % (v1 + v2));
		Assert.assertEquals("grade23", (char) 0, res);
		// Grade 3 and 4
		v1 = 6732;
		v2 = 28943;
		res = (char) (v1 + (v2 >> v2) - (v1 << -v1) + (v2 >>> v2) - v1);
		Assert.assertEquals("grade34", (char) 0, res);
		// Grade 4 and 7
		v1 = 0x2A37;
		v2 = 5;
		res = (char) (v1 >> (v2 & v1) << (v2 & v1) >>> v2);
		Assert.assertEquals("grade47", (char) 0x151, res);
		// Grade 7 and 8
		v1 = 0x2A5C;
		v2 = 0x239D;
		res = (char) ((v1 ^ v2) & v1);
		Assert.assertEquals("grade78", (char) 0x840, res);
		// Grade 8 and 9
		v1 = 0x2A5C;
		v2 = 0x239D;
		res = (char) (v1 ^ (v2 | v1));
		Assert.assertEquals("grade89", (char) 0x181, res);
		CmdTransmitter.sendDone();
	}
}
