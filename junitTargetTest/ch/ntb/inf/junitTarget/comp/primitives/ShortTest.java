package ch.ntb.inf.junitTarget.comp.primitives;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Test;

public class ShortTest {
	private static ShortTest objA, objB;

	private static short staticVar = 4;
	private static short staticHex = 0x1213;
	private static short staticOct = 024356;

	//Global assignment and calculation
	private short var = staticVar, hex = staticHex, oct = staticOct; // expected 4,18,20	
	private short add = (short) (32767 + var); // expected -32765
	private short sub = (short) (1 - var); // expected -3
	private short mult = (short) (8250 * var); // expected -32536
	private short div = (short) (31125 / var); // expected 7781
	private short rem = (short) (31125 % var); // expected 1
	private short lsr = (short) (-31125 >>> var); // expected -1946
	private short asr = (short) (-31125 >> var); // expected -1946
	private short asl = (short) (2512 << var); // expected -25344
	private short and = (short) (131512 & oct); // expected 168
	private short or = (short) (131512 | oct); // expected 10750
	private short xor = (short) (12458 ^ oct); // expected 6212
	private short not = (short) ~oct; // expected -10479
	private short preInc = (short) (13 + ++hex); // expected 4641
	private short preDec = (short) (13 + --hex); // expected 4640
	private short postInc = (short) (13 + hex++); // expected 4640
	private short postDec = (short) (13 + hex--); // expected 4641

	public ShortTest() {
	}
	
	//Constructor assignment and calculation
	public ShortTest(short var, short hex, short oct) {
		this.var = var; // expected 3
		this.hex = hex; // expected 4933
		this.oct = oct; // expected 1382
		add = (short) (32767 + var); // expected -32766
		sub = (short) (1 - var); // expected -3
		mult = (short) (8250 * var); // expected -24750
		div = (short) (31125 / var); // expected 10375
		rem = (short) (31125 % var); // expected 0
		lsr = (short) (-31125 >>> var); // expected -3891
		asr = (short) (-31125 >> var); // expected -3891
		asl = (short) (2512 << var); // expected 20096
		and = (short) (131512 & oct); // expected 288
		or = (short) (131512 | oct); // expected 1534
		xor = (short) (12458 ^ oct); // expected 13772
		not = (short) ~oct; // expected -1383
		preInc = (short) (13 + ++hex); // expected 4947
		preDec = (short) (13 + --hex); // expected 4946
		postInc = (short) (13 + hex++); // expected 4946
		postDec = (short) (13 + hex--); // expected 4947
	}

	@Before
	public static void setUp() {
		objA = new ShortTest();
		objB = new ShortTest((short) 3, (short) 0x1345, (short) 02546);
		CmdTransmitter.sendDone();
	}

	@Test
	//test local and global variables
	public static void testVar() {
		short var = 15125, staticVar = 13567;
		Assert.assertEquals("localVar", (short) 15125, var);
		Assert.assertEquals("localVar, same Name", (short) 13567, staticVar);
		Assert.assertEquals("staticVar", (short) 4, ShortTest.staticVar);
		Assert.assertEquals("staticHex", (short) 0x1213, ShortTest.staticHex);
		Assert.assertEquals("staticOct", (short) 024356, ShortTest.staticOct);
		CmdTransmitter.sendDone();
	}

	@Test
	//test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", (short) 4, objA.var);
		Assert.assertEquals("hex", (short) 4627, objA.hex);
		Assert.assertEquals("oct", (short) 10478, objA.oct);

		Assert.assertEquals("add", (short) -32765, objA.add);
		Assert.assertEquals("sub", (short) -3, objA.sub);
		Assert.assertEquals("mult", (short) -32536, objA.mult);
		Assert.assertEquals("div", (short) 7781, objA.div);
		Assert.assertEquals("rem", (short) 1, objA.rem);
		Assert.assertEquals("lsr", (short) -1946, objA.lsr);
		Assert.assertEquals("asr", (short) -1946, objA.asr);
		Assert.assertEquals("asl", (short) -25344, objA.asl);
		Assert.assertEquals("and", (short) 168, objA.and);
		Assert.assertEquals("or", (short) 10750, objA.or);
		Assert.assertEquals("xor", (short) 6212, objA.xor);
		Assert.assertEquals("not", (short) -10479, objA.not);
		Assert.assertEquals("preInc", (short) 4641, objA.preInc);
		Assert.assertEquals("preDec", (short) 4640, objA.preDec);
		Assert.assertEquals("postInc", (short) 4640, objA.postInc);
		Assert.assertEquals("postDec", (short) 4641, objA.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", (short) 3, objB.var);
		Assert.assertEquals("hex", (short) 4933, objB.hex);
		Assert.assertEquals("oct", (short) 1382, objB.oct);

		Assert.assertEquals("add", (short) -32766, objB.add);
		Assert.assertEquals("sub", (short) -2, objB.sub);
		Assert.assertEquals("mult", (short) 24750, objB.mult);
		Assert.assertEquals("div", (short) 10375, objB.div);
		Assert.assertEquals("rem", (short) 0, objB.rem);
		Assert.assertEquals("lsr", (short) -3891, objB.lsr);
		Assert.assertEquals("asr", (short) -3891, objB.asr);
		Assert.assertEquals("asl", (short) 20096, objB.asl);
		Assert.assertEquals("and", (short) 288, objB.and);
		Assert.assertEquals("or", (short) 1534, objB.or);
		Assert.assertEquals("xor", (short) 13772, objB.xor);
		Assert.assertEquals("not", (short) -1383, objB.not);
		Assert.assertEquals("preInc", (short) 4947, objB.preInc);
		Assert.assertEquals("preDec", (short) 4946, objB.preDec);
		Assert.assertEquals("postInc", (short) 4946, objB.postInc);
		Assert.assertEquals("postDec", (short) 4947, objB.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//Test addition variants
	public static void testAdd() {
		short res,v1, v2,v3;
		//Normal addition test
		v1 = 1023; v2 = 2300;
		res =(short) (v1 + v2);
		Assert.assertEquals("normal",(short) 3323,res);
		//Addition with 0 and -1
		v1 = 1023; v2 = 0;
		res =(short) (v1 + v2);
		Assert.assertEquals("normal",(short) 1023,res);
		v1 = 1023; v2 = -1;
		res =(short) (v1 + v2);
		Assert.assertEquals("normal",(short) 1022,res);
		//Positive overflow test
		v1 = 32767; v2 = 32766; v3 = 32765;
		res = (short) (v1 + v2 + v3);
		Assert.assertEquals("posOverflow",(short) 32762,res);
		//Negative overflow test
		v1 = -32768; v2 = -32767; v3 = -32766;
		res = (short) (v1 + v2 + v3);
		Assert.assertEquals("negOverflow",(short) -32765,res);
		//Short form test
		res = 1030; v1 =1040; v2 = 1050;
		res += v1 + v2;
		Assert.assertEquals("shortForm",(short) 3120,res);
		//Post increment test
		res = 1500;
		Assert.assertEquals("postInc1",(short) 1500,res++);
		Assert.assertEquals("postInc2",(short) 1501,res);
		//Pre increment test
		res = 1500;
		Assert.assertEquals("preInc",(short) 1501,++res);	
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Test subtraction variants
	public static void testSub(){
		short res,v1, v2,v3;
		//Normal subtraction test
		v1 = 1033; v2 = 2300;
		res =(short) (v2 - v1);
		Assert.assertEquals("normal",(short) 1267,res);
		//Subtraction with 0 and -1
		v1 = 0; v2 = 2300;
		res =(short) (v2 - v1);
		Assert.assertEquals("normal",(short) 2300,res);
		v1 = -1; v2 = 2300;
		res =(short) (v2 - v1);
		Assert.assertEquals("normal",(short) 2301,res);
		//Positive overflow test
		v1 = -32767; v2 = -32766; v3 = -32765;
		res = (short) (v1 - v2 - v3);
		Assert.assertEquals("posOverflow",(short) 32764,res);
		//Negative overflow test
		v1 = -32768; v2 = 32767; v3 = 32766;
		res = (short) (v1 - v2 - v3);
		Assert.assertEquals("negOverflow",(short) -32765,res);
		//Short form test
		res = 1500; v1 =1024; v2 = 1800;
		res -= -v1 - v2;
		Assert.assertEquals("shortForm",(short) 4324,res);
		//Post decrement test
		res = -1256;
		Assert.assertEquals("postDec1",(short) -1256,res--);
		Assert.assertEquals("postDec2",(short) -1257,res);
		//Pre decrement test
		res = -1256;
		Assert.assertEquals("preDec",(short) -1257,--res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Multiplication test
	public static void testMult(){
		short res,v1, v2;
		//Normal multiplication test
		v1 = 1221; v2 = 15;
		res =(short) (v2 * v1);
		Assert.assertEquals("normal",(short) 18315,res);
		//Multiplication with 0 and -1
		v1 = 1221; v2 = 0;
		res =(short) (v2 * v1);
		Assert.assertEquals("normal",(short) 0,res);
		v1 = 1221; v2 = -1;
		res =(short) (v2 * v1);
		Assert.assertEquals("normal",(short) -1221,res);
		//Negative multiplication test
		v1 = -1221; v2 = 15;
		res =(short) (v2 * v1);
		Assert.assertEquals("negative",(short) -18315,res);
		//Negatives multiplication test
		v1 = -1221; v2 = -15;
		res =(short) (v2 * v1);
		Assert.assertEquals("negatives",(short) 18315,res);
		//Positive overflow test
		v1 = 1221; v2 = 31;
		res = (short) (v1 * v2);
		Assert.assertEquals("posOverflow",(short) -27685,res);
		//Short form test
		res = 124; v1 =12; v2 = 30;
		res *= v1 * v2;
		Assert.assertEquals("shortForm",(short) -20896,res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Division test
	public static void testDiv(){
		short res,v1, v2;
		//Normal division test
		v1 = 15025; v2 = 1325;
		res =(short) (v1 / v2);
		Assert.assertEquals("normal",(short) 11,res);
		//Division with -1
		v1 = 15025; v2 = -1;
		res =(short) (v1 / v2);
		Assert.assertEquals("normal",(short) -15025,res);
		//Negative division test
		v1 = -15025; v2 = 1325;
		res =(short) (v1 / v2);
		Assert.assertEquals("negative",(short) -11,res);
		//Negatives division test
		v1 = -15025; v2 = -1325;
		res =(short) (v1 / v2);
		Assert.assertEquals("negatives",(short) 11,res);
		//Short form test
		res = 32100; v1 =32100; v2 = 100;
		res /= v1 / v2;
		Assert.assertEquals("shortForm",(short) 100,res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Remainder test
	public static void testRem(){
		short res,v1,v2;
		//Normal remainder test
		v1 = 15326; v2 = 1617;
		res = (short) (v1 % v2);
		Assert.assertEquals("normal",(short) 773,res);
		//Negative remainder test
		v1 = -15326; v2 = 1617;
		res = (short) (v1 % v2);
		Assert.assertEquals("negative",(short) -773,res);
		//Short form test
		res = 32123; v1 = 1024; v2 = 15;
		res %= v1 % v2;
		Assert.assertEquals("shortForm",(short) 3,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Logical shift right test
	public static void testLsr(){
		short res,v1,v2;
		//Normal lsr test
		v1 = 12536; v2 = 3;
		res = (short) (v1 >>> v2);
		Assert.assertEquals("normal",(short) 1567,res);
		//Negative lsr test
		v1 = -12536; v2 = 3;
		res = (short) (v1 >>> v2);
		Assert.assertEquals("negative",(short) -1567,res);
		//Short form test
		res = 1325; v1 = 10; v2 = 1;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm",(short) 41,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift right test
	public static void testAsr(){
		short res,v1,v2;
		//Normal asr test
		v1 = 12536; v2 = 3;
		res = (short) (v1 >>> v2);
		Assert.assertEquals("normal",(short) 1567,res);
		//Negative asr test
		v1 = -12536; v2 = 3;
		res = (short) (v1 >>> v2);
		Assert.assertEquals("negative",(short) -1567,res);
		//Short form test
		res = 1325; v1 = 10; v2 = 1;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm",(short) 41,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift left test
	public static void testAsl(){
		short res,v1,v2;
		//Normal asl test
		v1 = 155; v2 = 3;
		res = (short) (v1 << v2);
		Assert.assertEquals("normal",(short) 1240,res);
		//Negative asl test
		v1 = 5024; v2 = 3;
		res = (short) (v1 << v2);
		Assert.assertEquals("negative",(short) -25344,res);
		//Short form test
		res = 130; v1 = 4; v2 = 1;
		res <<= v1 << v2;
		Assert.assertEquals("shortForm",(short) -32256,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//And test
	public static void testAnd(){
		short res,v1,v2;
		//Normal and test
		v1 = 0x4646; v2 = 0x3F3F;
		res = (short) (v1 & v2);
		Assert.assertEquals("normal",(short) 0x0606,res);
		//Negative and test
		v1 = -0x5353; v2 = 0x6464;
		res = (short) (v1 & v2);
		Assert.assertEquals("negative",(short) 0x2424,res);
		//Short form test
		res = 0x0F0F; v1 = -0x5353; v2 = 0x6464;
		res &= v1 & v2;
		Assert.assertEquals("shortForm",(short) 0x0404,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Or test
	public static void testOr(){
		short res,v1,v2;
		//Normal and test
		v1 = 0x4646; v2 = 0x3F3F;
		res = (short) (v1 | v2);
		Assert.assertEquals("normal",(short) 0x7F7F,res);
		//Negative and test
		v1 = -0x5353; v2 = 0x6464;
		res = (short) (v1 | v2);
		Assert.assertEquals("negative",(short) -0x1313,res);
		//Short form test
		res = 0x0F0F; v1 = -0x5353; v2 = 0x6464;
		res |= v1 | v2;
		Assert.assertEquals("shortForm",(short) 0xEFEF,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Xor test
	public static void testXor(){
		short res,v1,v2;
		//Normal and test
		v1 = 0x5D5D; v2 = 0x3737;
		res = (short) (v1 ^ v2);
		Assert.assertEquals("normal",(short) 0x6A6A,res);
		//Negative and test
		v1 = -0x2323; v2 = 0x6464;
		res = (short) (v1 ^ v2);
		Assert.assertEquals("negative",(short) -0x4747,res);
		//Short form test
		res = -0x0101; v1 = -0x2323; v2 = 0x6464;
		res ^= v1 ^ v2;
		Assert.assertEquals("shortForm",(short) 0x4646,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Not test
	public static void testNot(){
		short res,v1;
		//Normal and test
		v1 = 0x5555;
		res = (short)~v1;
		Assert.assertEquals("normal",(short) 0xAAAA,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testPrio(){
		short res,v1,v2;
		//Grade 1 and 2
		v1 = 130; v2 = 125;
		res = (short) (v1++ * ~v1-- / ++v2 % --v1); 
		Assert.assertEquals("grade12",(short) -7,res);
		//Grade 2 and 3
		v1 = 130; v2 = 125;
		res = (short) (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2); 
		Assert.assertEquals("grade23",(short) -15870,res);
		//Grade 3 and 4
		v1 = 1300; v2 = 1302;
		res = (short) (v1 + v2 >> v2 - v1 << -v1 + v2 >>> v2 - v1); 
		Assert.assertEquals("grade34",(short) 650,res);
		//Grade 4 and 7
		v1 = 0x2A2A; v2 = 1;
		res = (short) (v1 >> v2 &  v1 << v2 & v1 >>> v2); 
		Assert.assertEquals("grade47",(short) 0x1414,res);
		//Grade 7 and 8
		v1 = 0x2A2A; v2 = 0x2323;
		res = (short) (v1 ^ v2 & v1); 
		Assert.assertEquals("grade78",(short) 0x0808,res);
		//Grade 8 and 9
		v1 = 0x2A2A; v2 = 0x2323;
		res = (short) (v1 ^ v2 | v1); 
		Assert.assertEquals("grade89",(short) 0x2B2B,res);
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testBraces(){
		short res,v1,v2;
		//Grade 2 and 3
		v1 = 130; v2 = 125;
		res = (short) ((v1 + v2) / (v1 - v2) * (v1 + v2) % (v1 + v2)); 
		Assert.assertEquals("grade23",(short) 0,res);
		//Grade 3 and 4
		v1 = 130; v2 = 125;
		res = (short) (v1 + (v2 >> v2) - (v1 << -v1) + (v2 >>> v2) - v1); 
		Assert.assertEquals("grade34",(short)0,res);
		//Grade 4 and 7
		v1 = 0x2A2A; v2 = 1;
		res = (short) (v1 >> (v2 &  v1) << (v2 & v1) >>> v2); 
		Assert.assertEquals("grade47",(short) 0x1515,res);
		//Grade 7 and 8
		v1 = 0x2A2A; v2 = 0x2323;
		res = (short) ((v1 ^ v2) & v1); 
		Assert.assertEquals("grade78",(short) 0x0808,res);
		//Grade 8 and 9
		v1 = 0x2A2A; v2 = 0x2323;
		res = (short) (v1 ^ (v2 | v1)); 
		Assert.assertEquals("grade89",(short) 0x0101,res);
		CmdTransmitter.sendDone();
	}
}
