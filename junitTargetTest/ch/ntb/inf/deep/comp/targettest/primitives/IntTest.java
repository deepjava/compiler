package ch.ntb.inf.deep.comp.targettest.primitives;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 11.06.2009
 * 
 * @author Jan Mrnak
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class IntTest {

	private static IntTest objA, objB;

	private static int staticVar = 4;
	private static int staticHex = 0x39AC5;
	private static int staticOct = 017152;

	//Global assignment and calculation
	private int var = staticVar, hex = staticHex, oct = staticOct; // expected 4,236229,7786	
	private int sub = (1 - var); // expected -3
	private int add = (2147483647 + var); // expected -2147483645
	private int mult = (536871000 * var); // expected -2147483296
	private int div = (1147483647 / var); // expected 286870911
	private int rem = (1147483647 % var); // expected 3
	private int lsr = (-1147483647 >>> var); // expected 196717728
	private int asr = (-1147483647 >> var); // expected -71717728
	private int asl = (-1147483647 << var); // expected -1179869168
	private int and = (21567334 & oct); // expected 5730
	private int or = (21567334 | oct); // expected 21569390
	private int xor = (21567334 ^ oct); // expected 21563660
	private int not = ~oct; // expected -7787
	private int preInc = (13 + ++hex); // expected 236243
	private int preDec = (13 + --hex); // expected 236242
	private int postInc = (13 + hex++); // expected 236242
	private int postDec = (13 + hex--); // expected 236243
	
	public IntTest() {
	}
	
	//Constructor assignment and calculation
	public IntTest(int var, int hex, int oct) {
		this.var = var; // expected 4
		this.hex = hex; // expected 236229
		this.oct = oct; // expected 7786
		sub = (1 - var); // expected -3
		add = (2147483647 + var); // expected -2147483645
		mult = (536871000 * var); // expected -2147483296
		div = (1147483647 / var); // expected 286870911
		rem = (1147483647 % var); // expected 3
		lsr = (-1147483647 >>> var); // expected 196717728
		asr = (-1147483647 >> var); // expected -71717728
		asl = (-1147483647 << var); // expected -1179869168
		and = (21567334 & oct); // expected 5730
		or = (21567334 | oct); // expected 21569390
		xor = (21567334 ^ oct); // expected 21563660
		not = ~oct; // expected -7787
		preInc = (13 + ++hex); // expected 236243
		preDec = (13 + --hex); // expected 236242
		postInc = (13 + hex++); // expected 236242
		postDec = (13 + hex--); // expected 236243
	}

	@Before
	public static void setUp() {
		objA = new IntTest();
		objB = new IntTest(3, 0xB237F, 056427);
		CmdTransmitter.sendDone();
	}

	@Test
	//test local and global variables
	public static void testVar() {
		int var = 15125, staticVar = 13567;
		Assert.assertEquals("localVar", 15125, var);
		Assert.assertEquals("localVar, same Name", 13567, staticVar);
		Assert.assertEquals("staticVar", 4, IntTest.staticVar);
		Assert.assertEquals("staticHex", 0x39AC5, IntTest.staticHex);
		Assert.assertEquals("staticOct", 017152, IntTest.staticOct);
		CmdTransmitter.sendDone();
	}

	@Test
	//test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", 4, objA.var);
		Assert.assertEquals("hex", 236229, objA.hex);
		Assert.assertEquals("oct", 7786, objA.oct);

		Assert.assertEquals("add", -2147483645, objA.add);
		Assert.assertEquals("sub", -3, objA.sub);
		Assert.assertEquals("mult", -2147483296, objA.mult);
		Assert.assertEquals("div", 286870911, objA.div);
		Assert.assertEquals("rem", 3, objA.rem);
		Assert.assertEquals("lsr", 196717728, objA.lsr);
		Assert.assertEquals("asr", -71717728, objA.asr);
		Assert.assertEquals("asl", -1179869168, objA.asl);
		Assert.assertEquals("and", 5730, objA.and);
		Assert.assertEquals("or", 21569390, objA.or);
		Assert.assertEquals("xor", 21563660, objA.xor);
		Assert.assertEquals("not", -7787, objA.not);
		Assert.assertEquals("preInc", 236243, objA.preInc);
		Assert.assertEquals("preDec", 236242, objA.preDec);
		Assert.assertEquals("postInc", 236242, objA.postInc);
		Assert.assertEquals("postDec", 236243, objA.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", 3, objB.var);
		Assert.assertEquals("hex", 729983, objB.hex);
		Assert.assertEquals("oct", 23831, objB.oct);

		Assert.assertEquals("add", -2147483646, objB.add);
		Assert.assertEquals("sub", -2, objB.sub);
		Assert.assertEquals("mult", 1610613000, objB.mult);
		Assert.assertEquals("div", 382494549, objB.div);
		Assert.assertEquals("rem", 0, objB.rem);
		Assert.assertEquals("lsr", 393435456, objB.lsr);
		Assert.assertEquals("asr", -143435456, objB.asr);
		Assert.assertEquals("asl", -589934584, objB.asl);
		Assert.assertEquals("and", 5382, objB.and);
		Assert.assertEquals("or", 21585783, objB.or);
		Assert.assertEquals("xor", 21580401, objB.xor);
		Assert.assertEquals("not", -23832, objB.not);
		Assert.assertEquals("preInc", 729997, objB.preInc);
		Assert.assertEquals("preDec", 729996, objB.preDec);
		Assert.assertEquals("postInc", 729996, objB.postInc);
		Assert.assertEquals("postDec", 729997, objB.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//Test addition variants
	public static void testAdd() {
		int res,v1, v2,v3;
		//Normal addition test
		v1 = 145023; v2 = 2398100;
		res = (v1 + v2);
		Assert.assertEquals("normal", 2543123, res);
	    //Addition with 0
		v1 = 147689; v2 = 0;
		res = (v1+v2);
		Assert.assertEquals("normal",147689,res);
		//Positive overflow test
		v1 = 2147483647; v2 = 2147483646; v3 = 2147483645;
		res = (v1 + v2 + v3);
		Assert.assertEquals("posOverflow", 2147483642, res);
		//Negative overflow test
		v1 = -2147483648; v2 = -2147483647; v3 = -2147483646;
		res = (v1 + v2 + v3);
		Assert.assertEquals("negOverflow", -2147483645, res);
		//Short form test
		res = 904037; v1 =545995; v2 = 1415885;
		res += v1 + v2;
		Assert.assertEquals("shortForm", 2865917, res);
		//Post increment test
		res = 214748364;
		Assert.assertEquals("postInc1", 214748364, res++);
		Assert.assertEquals("postInc2", 214748365, res);
		//Pre increment test
		res = 214748364;
		Assert.assertEquals("preInc", 214748365, ++res);	
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Test subtraction variants
	public static void testSub(){
		int res,v1, v2, v3;
		//Normal subtraction test
		v1 = 145023; v2 = 2398100;
		res = (v2 - v1);
		Assert.assertEquals("normal", 2253077, res);
		//Subtraction with 0
		v1 = 147690; v2 = 0;
		res = (v1-v2);
		Assert.assertEquals("normal",147690,res);
		//Positive overflow test
		v1 = 2147483647; v2 = -2147483646; v3 = -2147483645;
		res = (v1 - v2 - v3);
		Assert.assertEquals("posOverflow", 2147483642, res);
		//Negative overflow test
		v1 = -2147483648; v2 = 2147483647; v3 = 2147483646;
		res = (v1 - v2 - v3);
		Assert.assertEquals("negOverflow", -2147483645, res);
		//Short form test
		res = 904037; v1 =545995; v2 = 1415885;
		res -= -v1 - v2;
		Assert.assertEquals("shortForm", 2865917, res);
		//Post decrement test
		res = -214748364;
		Assert.assertEquals("postDec1", -214748364, res--);
		Assert.assertEquals("postDec2", -214748365, res);
		//Pre decrement test
		res = -214748364;
		Assert.assertEquals("preDec", -214748365, --res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Multiplication test
	public static void testMult(){
		int res,v1, v2;
		//Normal multiplication test
		v1 = 14689; v2 = 5127;
		res = (v2 * v1);
		Assert.assertEquals("normal", 75310503, res);
		//Multiplication with 0, -1 and negatives
		v1 = 156784; v2 = 0;
		res = (v1 * v2);
		Assert.assertEquals("normal",0,res);
		v1 = 126783; v2 = -1;
		res = (v1 * v2);
		Assert.assertEquals("normal",-126783,res);
		v1 = -126783; v2 = -1;
		res = (v1 * v2);
		Assert.assertEquals("normal",126783,res);
		v1 = -126783; v2 = 1;
		res = (v1 * v2);
		Assert.assertEquals("normal",-126783,res);
		//Negative multiplication test
		v1 = -14689; v2 = 5127;
		res = (v2 * v1);
		Assert.assertEquals("negative", -75310503, res);
		//Negatives multiplication test
		v1 = -14689; v2 = -5127;
		res = (v2 * v1);
		Assert.assertEquals("negatives", 75310503, res);
		//Positive overflow test
		v1 = 14689; v2 = 151196;
		res = (v1 * v2);
		Assert.assertEquals("posOverflow", -2074049252, res);
		//Short form test
		res = 14689; v1 =1540; v2 = 11;
		res *= v1 * v2;
		Assert.assertEquals("shortForm", 248831660, res);
		//Immediate test
		v1 = 1001;
		Assert.assertEquals("imm1", 1001, v1 * 1);
		Assert.assertEquals("imm2", 2002, v1 * 2);
		Assert.assertEquals("imm3", 100100, v1 * 100);
		Assert.assertEquals("imm4", 128128, v1 * 128);
		Assert.assertEquals("imm5", 1073741824, v1 * 1073741824); // 2^30
		Assert.assertEquals("imm11", -1001, v1 * -1);
		Assert.assertEquals("imm12", -2002, v1 * -2);
		Assert.assertEquals("imm13", -100100, v1 * -100);
		Assert.assertEquals("imm14", -128128, v1 * -128);
		Assert.assertEquals("imm15", -1073741824, v1 * -1073741824); // 2^30
		Assert.assertEquals("imm15", -2147483648, v1 * -2147483648); // 2^31
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Division test
	public static void testDiv(){
		int res, v1, v2;
		//Normal division test
		v1 = 214748364; v2 = 34587;
		res = (v1 / v2);
		Assert.assertEquals("normal1", 6208, res);
		//Division with -1 and negatives
		v1 = 12678; v2 = -1;
		res = (v1 / v2);
		Assert.assertEquals("normal2", -12678, res);		
		v1 = -12678; v2 = -1;
		res = (v1 / v2);
		Assert.assertEquals("normal3", 12678, res);
		v1 = -12678; v2 = 1;
		res = (v1 / v2);
		Assert.assertEquals("normal4", -12678, res);
		//Negative division test
		v1 = -214748364; v2 = 34587;
		res = (v1 / v2);
		Assert.assertEquals("negative1", -6208, res);
		//Negatives division test
		v1 = -214748364; v2 = -34587;
		res = (v1 / v2);
		Assert.assertEquals("negative2", 6208, res);
		//Short form test
		res = 214748364; v1 = 3458700; v2 = 100;
		res /= v1 / v2;
		Assert.assertEquals("shortForm", 6208, res);
		//Immediate & power of 2 test
		v1 = 3458700; 
		res = v1 / 1;
		Assert.assertEquals("imm1", 3458700, res);
		res = v1 / 2;
		Assert.assertEquals("imm2", 1729350, res);
		res = v1 / 127;
		Assert.assertEquals("imm3", 27233, res);
		res = v1 / 128;
		Assert.assertEquals("imm4", 27021, res);
		res = v1 / 1073741823;
		Assert.assertEquals("imm5", 0, res);
		res = v1 / 1073741824;	// 2 ^ 30
		Assert.assertEquals("imm6", 0, res);
		v1 = -3458701; 
		res = v1 / 1;
		Assert.assertEquals("imm11", -3458701, res);
		res = v1 / 2;
		Assert.assertEquals("imm12", -1729350, res);
		res = v1 / 127;
		Assert.assertEquals("imm13", -27233, res);
		res = v1 / 128;
		Assert.assertEquals("imm14", -27021, res);
		res = v1 / 1073741823;
		Assert.assertEquals("imm15", 0, res);
		res = v1 / 1073741824;	// 2 ^ 30
		Assert.assertEquals("imm16", 0, res);
		v1 = 128;
		res = 134785674 / v1;
		Assert.assertEquals("imm20", 1053013, res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Remainder test
	public static void testRem(){
		int res, v1, v2;
		//Normal remainder test
		v1 = 4415326; v2 = 31617;
		res = (v1 % v2);
		Assert.assertEquals("normal", 20563, res);
		//Negative remainder test
		v1 = -4415326; v2 = 31617;
		res = (v1 % v2);
		Assert.assertEquals("negative", -20563, res);
		//Short form test
		res = 4415326; v1 = 31617; v2 = 618;
		res %= v1 % v2;
		Assert.assertEquals("shortForm", 25, res);
		//Immediate & power of 2 test
		v1 = 3458703; 
		res = v1 % 1;
		Assert.assertEquals("imm1", 0, res);
		res = v1 % 2;
		Assert.assertEquals("imm2", 1, res);
		res = v1 % 127;
		Assert.assertEquals("imm3", 112, res);
		res = v1 % 128;
		Assert.assertEquals("imm4", 15, res);
		res = v1 % 1073741823;
		Assert.assertEquals("imm5", 3458703, res);
		res = v1 % 1073741824;	// 2 ^ 30
		Assert.assertEquals("imm6", 3458703, res);
		v1 = -3458703; 
		res = v1 % 1;
		Assert.assertEquals("imm11", 0, res);
		res = v1 % 2;
		Assert.assertEquals("imm12", -1, res);
		res = v1 % 127;
		Assert.assertEquals("imm13", -112, res);
		res = v1 % 128;
		Assert.assertEquals("imm14", -15, res);
		res = v1 % 1073741823;
		Assert.assertEquals("imm15", -3458703, res);
		res = v1 % 1073741824;	// 2 ^ 30
		Assert.assertEquals("imm16", -3458703, res);
		v1 = 128;
		res = 134785674 % v1;
		Assert.assertEquals("imm20", 10, res);
		v1 = 349;
		res = v1 % -1;
		Assert.assertEquals("imm30", 0, res);
		res = v1 % -2;
		Assert.assertEquals("imm31", 1, res);
		res = v1 % -3;
		Assert.assertEquals("imm32", 1, res);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Logical shift right test
	public static void testLsr(){
		int res, v1, v2;
		//Normal lsr test
		v1 = 214748364; v2 = 3;
		res = (v1 >>> v2);
		Assert.assertEquals("normal", 26843545, res);
		//Negative lsr test
		v1 = -1932735284; v2 = 3;
		res = (v1 >>> v2);
		Assert.assertEquals("negative", 295279001, res);
		v1 = 0x55443322;
		res = (v1 >>> 0);
		Assert.assertEquals("imm0", 0x55443322, res);
		res = (v1 >>> 4);
		Assert.assertEquals("imm4", 0x5544332, res);
		res = (v1 >>> 28);
		Assert.assertEquals("imm28", 5, res);
		res = (v1 >>> 32);
		Assert.assertEquals("imm32", 0x55443322, res);
		//Short form test
		res = 214748364; v1 = 12; v2 = 2;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm", 26843545, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift right test
	public static void testAsr(){
		int res,v1,v2;
		//Normal asr test
		v1 = 214748364; v2 = 3;
		res =  (v1 >> v2);
		Assert.assertEquals("normal", 26843545, res);
		//Negative asr test
		v1 = -1932735284; v2 = 3;
		res = (v1 >> v2);
		Assert.assertEquals("negative", -241591911, res);
		v1 = 0xaa443322;
		res = (v1 >> 0);
		Assert.assertEquals("imm0", 0xaa443322, res);
		res = (v1 >> 4);
		Assert.assertEquals("imm4", 0xfaa44332, res);
		res = (v1 >> 28);
		Assert.assertEquals("imm28", 0xfffffffa, res);
		res = (v1 >> 32);
		Assert.assertEquals("imm32", 0xaa443322, res);
		//Short form test
		res = 214748364; v1 = 12; v2 = 2;
		res >>= v1 >> v2;
		Assert.assertEquals("shortForm", 26843545, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift left test
	public static void testAsl(){
		int res,v1,v2;
		//Normal asl test
		v1 = 214748364; v2 = 3;
		res = (v1 << v2);
		Assert.assertEquals("normal", 1717986912, res);
		//Negative asl test
		v1 = 214748364; v2 = 4;
		res = (v1 << v2);
		Assert.assertEquals("negative", -858993472, res);
		v1 = 0xaa443322;
		res = (v1 << 0);
		Assert.assertEquals("imm0", 0xaa443322, res);
		res = (v1 << 4);
		Assert.assertEquals("imm4", 0xa4433220, res);
		res = (v1 << 28);
		Assert.assertEquals("imm28", 0x20000000, res);
		res = (v1 << 32);
		Assert.assertEquals("imm32", 0xaa443322, res);
		//Short form test
		res = 214748364; v1 = 1; v2 = 2;
		res <<= v1 << v2;
		Assert.assertEquals("shortForm", -858993472, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//And test
	public static void testAnd(){
		int res,v1,v2;
		//Normal and test
		v1 = 0x224646; v2 = 0xAA3F3F;
		res = (v1 & v2);
		Assert.assertEquals("normal", 0x220606, res);
		res = (v1 & 0xffff0000);
		Assert.assertEquals("imm1", 0x220000, res);
		res = (0xffff0000 & v1);
		Assert.assertEquals("imm2", 0x220000, res);
		res = (v1 & 0xffff);
		Assert.assertEquals("imm3", 0x4646, res);
		res = (0xffff & v1);
		Assert.assertEquals("imm4", 0x4646, res);
		res = (v1 & -1);
		Assert.assertEquals("imm5", 0x224646, res);
		res = (-1 & v1);
		Assert.assertEquals("imm6", 0x224646, res);
		//Negative and test
		v1 = 0xFFFF5353; v2 = 0x996464;
		res = (v1 & v2);
		Assert.assertEquals("negative", 0x994040, res);
		//Short form test
		res = 0x224646; v1 = 0xAA3F3F; v2 = 0xFF5252;
		res &= v1 & v2;
		Assert.assertEquals("shortForm", 0x220202, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Or test
	public static void testOr(){
		int res,v1,v2;
		//Normal or test
		v1 = 0x224646; v2 = 0xAA3F3F;
		res = (v1 | v2);
		Assert.assertEquals("normal", 0xAA7F7F, res);
		res = (v1 | 0xffff0000);
		Assert.assertEquals("imm1", 0xffff4646, res);
		res = (0xffff0000 | v1);
		Assert.assertEquals("imm2", 0xffff4646, res);
		res = (v1 | 0xffff);
		Assert.assertEquals("imm3", 0x22ffff, res);
		res = (0xffff | v1);
		Assert.assertEquals("imm4", 0x22ffff, res);
		res = (v1 | -1);
		Assert.assertEquals("imm5", 0xffffffff, res);
		res = (-1 | v1);
		Assert.assertEquals("imm6", 0xffffffff, res);
		//Negative or test
		v1 = -0x224646; v2 = 0x996464; //v1=FFDDB9BA
		res = (v1 | v2);
		Assert.assertEquals("negative", 0xFFDDFDFE, res);
		//Short form test
		res = 0xABCD62; v1 = -0x5353; v2 = 0x6464;
		res |= v1 | v2;
		Assert.assertEquals("shortForm", 0xFFFFEDEF, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Xor test
	public static void testXor(){
		int res,v1,v2;
		//Normal xor test
		v1 = 0x185D5D; v2 = 0x813737;
		res = (v1 ^ v2);
		Assert.assertEquals("normal", 0x996A6A, res);
		v1 = 0x185D5D; ;
		res = (v1 ^ 0xff);
		Assert.assertEquals("imm1", 0x185Da2, res);
		res = (v1 ^ 0xff0000);
		Assert.assertEquals("imm2", 0xe75D5D, res);
		v1 = 0x185D5D; ;
		res = (0xff ^ v1);
		Assert.assertEquals("imm3", 0x185Da2, res);
		res = (0xff0000 ^ v1);
		Assert.assertEquals("imm4", 0xe75D5D, res);
		res = (v1 ^ -1);
		Assert.assertEquals("imm5", 0xffe7a2a2, res);
		res = (-1 ^ v1);
		Assert.assertEquals("imm6", 0xffe7a2a2, res);
		//Short form test
		res = -0x9A0101; v1 = -0x152323; v2 = 0x886464;
		res ^= v1 ^ v2;
		Assert.assertEquals("shortForm", 0x74646, res);
	
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Not test
	public static void testNot(){
		int res,v1;
		//Normal not test
		v1 = 0xABCD5555;
		res = ~v1;
		Assert.assertEquals("normal", 0x5432AAAA, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testPrio(){
		int res,v1,v2;
		//Grade 1 and 2
		v1 = 0xABCD; v2 = 0x5555;
		res = (v1++ * ~v1-- / ++v2 % --v1); 
		Assert.assertEquals("grade12", -587, res);
		//Grade 2 and 3
		v1 = 0xABCD; v2 = 0x5555;
		res = (v1 + v2 / v1 - v2 * v1 + v2 % v1 + v2); 
		Assert.assertEquals("grade23", 0xC6BD3A66, res);
		//Grade 3 and 4
		v1 = 0xABCD; v2 = 0xABD1;
		res = (v1 + v2 >> v2 - v1 << -v1 + v2 >>> v2 - v1); 
		Assert.assertEquals("grade34", 5497, res);
		//Grade 4 and 7
		v1 = 0xFE2A2A; v2 = 1;
		res = (v1 >> v2 &  v1 << v2 & v1 >>> v2); 
		Assert.assertEquals("grade47", 0x7C1414, res);
		//Grade 7 and 8
		v1 = 0xFE2A2A; v2 = 0x662323;
		res = (v1 ^ v2 & v1); 
		Assert.assertEquals("grade78", 0x980808, res);
		//Grade 8 and 9
		v1 = 0xFE2A2A; v2 = 0x662323;
		res = (v1 ^ v2 | v1); 
		Assert.assertEquals("grade89", 0xFE2B2B, res);

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Priority rules test
	public static void testBraces(){
		int res,v1,v2;
		//Grade 2 and 3
		v1 = 0xABCD; v2 = 0x5555;
		res = ((v1 + v2) / (v1 - v2) * (v1 + v2) % (v1 + v2)); 
		Assert.assertEquals("grade23", 0, res);
		//Grade 3 and 4
		v1 = 130; v2 = 125;
		res = (v1 + (v2 >> v2) - (v1 << -v1) + (v2 >>> v2) - v1); 
		Assert.assertEquals("grade34", -2147483648, res);
		//Grade 4 and 7
		v1 = 0xFE2A2A; v2 = 1;
		res = (v1 >> (v2 &  v1) << (v2 & v1) >>> v2); 
		Assert.assertEquals("grade47", 0x7F1515, res);
		//Grade 7 and 8
		v1 = 0xFE2A2A; v2 = 0x662323;
		res = ((v1 ^ v2) & v1); 
		Assert.assertEquals("grade78", 0x980808, res);
		//Grade 8 and 9
		v1 = 0xFE2A2A; v2 = 0x662323;
		res = (v1 ^ (v2 | v1)); 
		Assert.assertEquals("grade89", 0x101, res);

		CmdTransmitter.sendDone();
	}

}
