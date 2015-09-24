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
 * NTB 10.02.2011
 * 
 */
@MaxErrors(100)
public class LongTest {

	private static LongTest objA, objB;

	private static long staticVar = 43457904567L;
	private static long staticHex = 0x39AC534ffa8L;
	private static long staticOct = 01715245346234L;

	//Global assignment and calculation
	private long var = staticVar, hex = staticHex, oct = staticOct; 	
	private long add = (23372036854775807L + var); // expected 23372080312680374
	private long sub = (1 - var); // expected -43457904566L
	private long mult = (1234567 * var); // expected 53651694867567489
	private long div = (23372036854775807L / var); // expected 537808
	private long rem = (23372036854775807L % var); // expected 28115406671
	private long lsr = (-23372036854775807L >>> var); // expected 511
	private long asr = (-23372036854775807L >> var); // expected -1
	private long asl = (-23372036854775807L << var); // expected 36028797018963968
	private long and = (21567334 & oct); // expected 5730
	private long or = (21567334 | oct); // expected 21569390
	private long xor = (21567334 ^ oct); // expected 21563660
	private long not = ~oct; // expected -7787
	private long preInc = (13 + ++hex); // expected 3963268431798
	private long preDec = (13 + --hex); // expected 3963268431797
	private long postInc = (13 + hex++); // expected 3963268431797
	private long postDec = (13 + hex--); // expected 3963268431798
	
	public LongTest() {
	}
	
	//Constructor assignment and calculation
	public LongTest(long var, long hex, long oct) {
		this.var = var; 
		this.hex = hex; 
		this.oct = oct; 
		add = (23372036854775807L + var); // expected 23372080312680374
		sub = (1 - var); // expected -43457904566L
		mult = (1234567 * var); // expected 53651694867567489
		div = (23372036854775807L / var); // expected 537808
		rem = (23372036854775807L % var); // expected 28115406671
		lsr = (-23372036854775807L >>> var); // expected 511
		asr = (-23372036854775807L >> var); // expected -1
		asl = (-23372036854775807L << var); // expected 36028797018963968
		and = (21567334 & oct); // expected 5730
		or = (21567334 | oct); // expected 21569390
		xor = (21567334 ^ oct); // expected 21563660
		not = ~oct; // expected -7787
		preInc = (13 + ++hex); // expected 3963268431798
		preDec = (13 + --hex); // expected 3963268431797
		postInc = (13 + hex++); // expected 3963268431797
		postDec = (13 + hex--); // expected 3963268431798
	}

	@Before
	public static void setUp() {
		objA = new LongTest();
		objB = new LongTest(43457904567L, 0x39AC534ffa8L, 01715245346234L);
		CmdTransmitter.sendDone();
	}

	@Test
	//test local and global variables
	public static void testVar() {
		long var = 12355675684597L, staticVar = 1356753453657L;
		Assert.assertEquals("localVar", 12355675684597L, var);
		Assert.assertEquals("localVar, same Name", 1356753453657L, staticVar);
		Assert.assertEquals("staticVar", 43457904567L, LongTest.staticVar);
		Assert.assertEquals("staticHex", 0x39AC534ffa8L, LongTest.staticHex);
		Assert.assertEquals("staticOct", 01715245346234L, LongTest.staticOct);
		CmdTransmitter.sendDone();
	}

	@Test
	//test global assignment and calculation
	public static void testObjA() {
		Assert.assertEquals("var", 43457904567L, objA.var);
		Assert.assertEquals("hex", 0x39AC534ffa8L, objA.hex);
		Assert.assertEquals("oct", 01715245346234L, objA.oct);
		Assert.assertEquals("add", 23372080312680374L, objA.add);
		Assert.assertEquals("sub", -43457904566L, objA.sub);
		Assert.assertEquals("mult", 53651694867567489L, objA.mult);
		Assert.assertEquals("div", 537808, objA.div);
		Assert.assertEquals("rem", 28115406671L, objA.rem);
		Assert.assertEquals("lsr", 511, objA.lsr);
		Assert.assertEquals("asr", -1, objA.asr);
		Assert.assertEquals("asl", 36028797018963968L, objA.asl);
		Assert.assertEquals("and", 66564, objA.and);
		Assert.assertEquals("or", 130658721790L, objA.or);
		Assert.assertEquals("xor", 130658655226L, objA.xor);
		Assert.assertEquals("not", -130637221021L, objA.not);
		Assert.assertEquals("preInc", 3963268431798L, objA.preInc);
		Assert.assertEquals("preDec", 3963268431797L, objA.preDec);
		Assert.assertEquals("postInc", 3963268431797L, objA.postInc);
		Assert.assertEquals("postDec", 3963268431798L, objA.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//test constructor assignment and calculation
	public static void testObjB() {
		Assert.assertEquals("var", 43457904567L, objB.var);
		Assert.assertEquals("hex", 0x39AC534ffa8L, objB.hex);
		Assert.assertEquals("oct", 01715245346234L, objB.oct);
		Assert.assertEquals("add", 23372080312680374L, objB.add);
		Assert.assertEquals("sub", -43457904566L, objB.sub);
		Assert.assertEquals("mult", 53651694867567489L, objB.mult);
		Assert.assertEquals("div", 537808, objB.div);
		Assert.assertEquals("rem", 28115406671L, objB.rem);
		Assert.assertEquals("lsr", 511, objB.lsr);
		Assert.assertEquals("asr", -1, objB.asr);
		Assert.assertEquals("asl", 36028797018963968L, objB.asl);
		Assert.assertEquals("and", 66564, objB.and);
		Assert.assertEquals("or", 130658721790L, objB.or);
		Assert.assertEquals("xor", 130658655226L, objB.xor);
		Assert.assertEquals("not", -130637221021L, objB.not);
		Assert.assertEquals("preInc", 3963268431798L, objB.preInc);
		Assert.assertEquals("preDec", 3963268431797L, objB.preDec);
		Assert.assertEquals("postInc", 3963268431797L, objB.postInc);
		Assert.assertEquals("postDec", 3963268431798L, objB.postDec);
		CmdTransmitter.sendDone();
	}

	@Test
	//Test addition variants
	public static void testAdd() {
		long res, v1, v2, v3;
		//Normal addition test
		v1 = 14502300000L; v2 = 239810000L;
		res = (v1 + v2);
		Assert.assertEquals("normal", 14742110000L, res);
	    //Addition with 0
		v1 = 14768900000L; v2 = 0;
		res = (v1 + v2);
		Assert.assertEquals("normal",14768900000L,res);
		res = (v1 + 1);
		Assert.assertEquals("imm1",14768900001L,res);
		res = (1 + v1);
		Assert.assertEquals("imm2",14768900001L,res);
      //Positive overflow test
		v1 = 9223372036854775807L; v2 = 9223372036854775806L; v3 = 9223372036854775805L;
		res = (v1 + v2 + v3);
		Assert.assertEquals("posOverflow", 9223372036854775802L, res);
		//Negative overflow test
		v1 = -9223372036854775808L; v2 = -9223372036854775807L; v3 = -9223372036854775806L;
		res = (v1 + v2 + v3);
		Assert.assertEquals("negOverflow", -9223372036854775805L, res);            
		//Short form test
		res = 904037; v1 =545995; v2 = 1415885;
		res += v1 + v2;
		Assert.assertEquals("shortForm", 2865917, res);                      
		//Post increment test
		res = 21474836400L;
		Assert.assertEquals("postInc1", 21474836400L, res++);
		Assert.assertEquals("postInc2", 21474836401L, res);
		//Pre increment test
		res = 21474836400L;
		Assert.assertEquals("preInc", 21474836401L, ++res);	
		//test in loop
		v1 = -1000000000000L; v2 = 3000000000L;
		while(v1 < 0) v1 += v2;
		Assert.assertEquals("loop1", 2000000000, v1);	
		
		CmdTransmitter.sendDone();
	}
	
	@SuppressWarnings("unused")
	@Test
	//Test subtraction variants
	public static void testSub(){
		long res,v1, v2, v3;
		//Normal subtraction test
		v1 = 145023; v2 = 2398100000L;
		res = (v2 - v1);
		Assert.assertEquals("normal", 2397954977L, res);
		//Subtraction with 0
		v1 = 14769000000L; v2 = 0;
		res = (v1-v2);
		Assert.assertEquals("normal",14769000000L,res);
		v1 = 345792384054L;
		res = v1 - 1;
		Assert.assertEquals("imm1", 345792384053L, res);               
		res = v1 - 100000;
		Assert.assertEquals("imm2", 345792284054L, res);               
		res = v1 - 100000000000L;
		Assert.assertEquals("imm3", 245792384054L, res);               
		res = 1 - v1;
		Assert.assertEquals("imm4", -345792384053L, res);               
		res = 100000 - v1;
		Assert.assertEquals("imm5", -345792284054L, res);               
		res = 1000000000000L - v1;
		Assert.assertEquals("imm6", 654207615946L, res);               
		//Short form test
		res = 904037; v1 =545995; v2 = 1415885;
		res -= -v1 - v2;
		Assert.assertEquals("shortForm", 2865917, res);                     
		//Post decrement test
		res = -21474836400L;
		Assert.assertEquals("postDec1", -21474836400L, res--);
		Assert.assertEquals("postDec2", -21474836401L, res);
		//Pre decrement test
		res = -21474836400L;
		Assert.assertEquals("preDec", -21474836401L, --res);
		//test in loop
		v1 = 1000000000000L; v2 = 3000000000L;
		while(v1 > 0) v1 -= v2;
		Assert.assertEquals("loop1", -2000000000, v1);	
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Multiplication test
	public static void testMult(){
		long res,v1, v2;
		//Normal multiplication test
		v1 = 146890; v2 = 51270;
		res = (v2 * v1);
		Assert.assertEquals("normal", 7531050300L, res);
		//Multiplication with 0, -1 and negatives
		v1 = 156784; v2 = 0;
		res = (v1 * v2);
		Assert.assertEquals("normal",0,res);
		v1 = 12678300000L; v2 = -1;
		res = (v1 * v2);
		Assert.assertEquals("normal",-12678300000L,res);
		v1 = -12678300000L; v2 = -1;
		res = (v1 * v2);
		Assert.assertEquals("normal",12678300000L,res);
		v1 = -12678300000L; v2 = 1;
		res = (v1 * v2);
		Assert.assertEquals("normal",-12678300000L,res);
		//Negative multiplication test
		v1 = -146890; v2 = 51270;
		res = (v2 * v1);
		Assert.assertEquals("negative", -7531050300L, res);
		//Negatives multiplication test
		v1 = -146890; v2 = -51270;
		res = (v2 * v1);
		Assert.assertEquals("negatives", 7531050300L, res);
		v1 = 200000000000L; 
		res = (v1 * 100);
		Assert.assertEquals("imm1", 20000000000000L, res);
		res = (100 * v1);
		Assert.assertEquals("imm2", 20000000000000L, res);
		//Short form test
		res = 14689; v1 = 15400; v2 = 11;
		res *= v1 * v2;
		Assert.assertEquals("shortForm", 2488316600L, res);
		//Immediate test
		v1 = 103;
		Assert.assertEquals("imm1", 103, v1 * 1);
		Assert.assertEquals("imm2", 206, v1 * 2);
		Assert.assertEquals("imm3", 10300, v1 * 100);
		Assert.assertEquals("imm4", 13184, v1 * 128);
		Assert.assertEquals("imm5", 110595407872L, v1 * 1073741824); // 2^30
		Assert.assertEquals("imm6", 221190815744L, v1 * 2147483648L); // 2^31
		Assert.assertEquals("imm7", 442381631488L, v1 * 4294967296L); // 2^32
		Assert.assertEquals("imm8", 442381631591L, v1 * 4294967297L); // 2^32 + 1
		Assert.assertEquals("imm9", -4611686018427387904L, v1 * 4611686018427387904L); // 2^62
		Assert.assertEquals("imm10", 9223372036854775705L, v1 * 9223372036854775807L); // 2^63 - 1
		Assert.assertEquals("imm11", -103, v1 * -1);
		Assert.assertEquals("imm12", -206, v1 * -2);
		Assert.assertEquals("imm13", -10300, v1 * -100);
		Assert.assertEquals("imm14", -13184, v1 * -128);
		Assert.assertEquals("imm15", -110595407872L, v1 * -1073741824); // 2^30
		Assert.assertEquals("imm16", -221190815744L, v1 * -2147483648L); // 2^31
		Assert.assertEquals("imm17", -442381631488L, v1 * -4294967296L); // 2^32
		Assert.assertEquals("imm18", -442381631591L, v1 * -4294967297L); // 2^32 + 1
		Assert.assertEquals("imm19", 4611686018427387904L, v1 * -4611686018427387904L); // 2^62
		Assert.assertEquals("imm20", -9223372036854775808L, v1 * -9223372036854775808L); // 2^63
		v1 = -10000000003L;
		Assert.assertEquals("imm21", -10000000003L, v1 * 1);
		Assert.assertEquals("imm22", -20000000006L, v1 * 2);
		Assert.assertEquals("imm23", -1000000000300L, v1 * 100);
		Assert.assertEquals("imm24", -1280000000384L, v1 * 128);
		Assert.assertEquals("imm25", 7709325830488326144L, v1 * 1073741824); // 2^30
		Assert.assertEquals("imm26", -3028092412732899328L, v1 * 2147483648L); // 2^31
		Assert.assertEquals("imm27", -6056184825465798656L, v1 * 4294967296L); // 2^32
		Assert.assertEquals("imm28", -6056184835465798659L, v1 * 4294967297L); // 2^32 + 1
		Assert.assertEquals("imm29", 4611686018427387904L, v1 * 4611686018427387904L); // 2^62
		//test in loop
		v1 = 3000000000L; v2 = 10;
		while(v1 < 4000000000000L) v1 *= v2;
		Assert.assertEquals("loop1", 30000000000000L, v1);	
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Division test
	public static void testDiv(){
		long res, v1, v2;
		//Normal division test
		v1 = 2147483640056785667L; v2 = 345;
		res =  (v1 / v2);	// 6224590261034161.3536231884057971
		Assert.assertEquals("test1", 6224590261034161L, res);
		v1 = -2147483640056785667L; v2 = 345;
		res =  (v1 / v2);
		Assert.assertEquals("test2", -6224590261034161L, res);
		v1 = 2147483640056785667L; v2 = -345;
		res =  (v1 / v2);
		Assert.assertEquals("test3", -6224590261034161L, res);
		v1 = -2147483640056785667L; v2 = -345;
		res =  (v1 / v2);
		Assert.assertEquals("test4", 6224590261034161L, res);
		v1 = 2147483640056785545L; v2 = 345;
		
		res =  (v1 / v2);	// 6224590261034161
		Assert.assertEquals("test11", 6224590261034161L, res);
		v1 = -2147483640056785545L; v2 = 345;
		res =  (v1 / v2);	// 6224590261034161
		Assert.assertEquals("test12", -6224590261034161L, res);
		v1 = 2147483640056785545L; v2 = -345;
		res =  (v1 / v2);	// 6224590261034161
		Assert.assertEquals("test13", -6224590261034161L, res);
		v1 = -2147483640056785545L; v2 = -345;
		res =  (v1 / v2);	// 6224590261034161
		Assert.assertEquals("test14", 6224590261034161L, res);
		
		v1 = 2147483640056785667L; v2 = 345234537546756345L;
		res =  (v1 / v2);
		Assert.assertEquals("test21", 6, res);
		v1 = 2000000000000000002L; v2 = 2;
		res =  (v1 / v2);
		Assert.assertEquals("test22", 1000000000000000001L, res);
		v1 = 2000000000000000001L; v2 = 1;
		res =  (v1 / v2);
		Assert.assertEquals("test23", 2000000000000000001L, res);
		v1 = -2000000000000000002L; v2 = 2;
		res =  (v1 / v2);
		Assert.assertEquals("test24", -1000000000000000001L, res);
		v1 = -2000000000000000001L; v2 = -1;
		res =  (v1 / v2);
		Assert.assertEquals("test24", 2000000000000000001L, res);
		
		v1 = 21474836400L; v2 = 3458700;
		res =  (v1 / v2);
		Assert.assertEquals("test31", 6208, res);
		v1 = 21474836400L; v2 = -3458700;
		res =  (v1 / v2);
		Assert.assertEquals("test32", -6208, res);
		v1 = -21474836400L; v2 = 3458700;
		res =  (v1 / v2);
		Assert.assertEquals("test33", -6208, res);
		v1 = -21474836400L; v2 = -3458700;
		res =  (v1 / v2);
		Assert.assertEquals("test34", 6208, res);
		
		v1 = 34546890089234576L;
		res =  (v1 / 567657);
		Assert.assertEquals("test40", 60858740558L, res);
		v1 = -34546890089234576L;
		res =  (v1 / 567657);
		Assert.assertEquals("test41", -60858740558L, res);
		v1 = 34546890089234576L;
		res =  (v1 / -567657);
		Assert.assertEquals("test42", -60858740558L, res);
		v1 = -34546890089234576L;
		res =  (v1 / -567657);
		Assert.assertEquals("test43", 60858740558L, res);
		
		v1 = 34546890089234576L;
		res =  (v1 / 1);
		Assert.assertEquals("test50", 34546890089234576L, res);
		v1 = -34546890089234576L;
		res =  (v1 / 1);
		Assert.assertEquals("test51", -34546890089234576L, res);
		v1 = 34546890089234576L;
		res =  (v1 / -1);
		Assert.assertEquals("test52", -34546890089234576L, res);
		v1 = -34546890089234576L;
		res =  (v1 / -1);
		Assert.assertEquals("test53", 34546890089234576L, res);

		v1 = 34546890089234576L;
		res =  (v1 / 32);	
		Assert.assertEquals("test60", 1079590315288580L, res);
		v1 = -34546890089234576L;
		res =  (v1 / 32);
		Assert.assertEquals("test61", -1079590315288580L, res);
		v1 = 34546890089234576L;
		res =  (v1 / -32);
		Assert.assertEquals("test62", -1079590315288580L, res);
		v1 = -34546890089234576L;
		res =  (v1 / -32);
		Assert.assertEquals("test63", 1079590315288580L, res);
		
		v1 = 34546890089234576L;
		res =  (v1 / (1L<<33));	// 2 ^ 33
		Assert.assertEquals("test70", 4021787, res);
		v1 = -34546890089234576L;
		res =  (v1 / (1L<<33));
		Assert.assertEquals("test71", -4021787, res);
		v1 = 34546890089234576L;
		res =  (v1 / -(1L<<33));
		Assert.assertEquals("test72", -4021787, res);
		v1 = -34546890089234576L;
		res =  (v1 / -(1L<<33));
		Assert.assertEquals("test73", 4021787, res);
		
		v1 = 34546890089234576L;
		Assert.assertEquals("test80", 16087149, v1 / ((1L<<31)+1));	// 2 ^ 31 + 1
		Assert.assertEquals("test81", -16087149, v1 / -((1L<<31)+1));
		v1 = -34546890089234576L;
		Assert.assertEquals("test82", -16087149, v1 / ((1L<<31)+1));
		Assert.assertEquals("test83", 16087149, v1 / -((1L<<31)+1));
		
		v1 = 10000000000000L;
		Assert.assertEquals("test90", 2500, v1 / 4000000000L);	

		//Immediate test (power of 2)
		v1 = 103;
		Assert.assertEquals("imm1", 103, v1 / 1);
		Assert.assertEquals("imm2", 51, v1 / 2);
		Assert.assertEquals("imm3", 0, v1 / 128);
		Assert.assertEquals("imm4", 0, v1 / (1L<<30)); 
		Assert.assertEquals("imm5", 0, v1 / (1L<<31)); 
		Assert.assertEquals("imm6", 0, v1 / (1L<<32)); 
		Assert.assertEquals("imm6", 0, v1 / (1L<<33)); 
		Assert.assertEquals("imm7", 0, v1 / (1L<<62)); 
		v1 = (1L<<63)-1;
		Assert.assertEquals("imm11", (1L<<63)-1, v1 / 1);
		Assert.assertEquals("imm12", (1L<<62)-1, v1 / 2);
		Assert.assertEquals("imm13", (1L<<56)-1, v1 / 128);
		Assert.assertEquals("imm14", (1L<<33)-1, v1 / (1L<<30));
		Assert.assertEquals("imm15", (1L<<32)-1, v1 / (1L<<31)); 
		Assert.assertEquals("imm16", (1L<<31)-1, v1 / (1L<<32));
		Assert.assertEquals("imm17", (1L<<30)-1, v1 / (1L<<33));
		Assert.assertEquals("imm18", (1L<<29)-1, v1 / (1L<<34)); 
		Assert.assertEquals("imm19", 3, v1 / (1L<<61)); 
		Assert.assertEquals("imm20", 1, v1 / (1L<<62)); 
		v1 = (1L<<62);
		Assert.assertEquals("imm21", (1L<<62), v1 / 1);
		Assert.assertEquals("imm22", (1L<<61), v1 / 2);
		Assert.assertEquals("imm23", (1L<<55), v1 / 128);
		Assert.assertEquals("imm24", (1L<<32), v1 / (1L<<30));
		Assert.assertEquals("imm25", (1L<<31), v1 / (1L<<31)); 
		Assert.assertEquals("imm26", (1L<<30), v1 / (1L<<32)); 
		Assert.assertEquals("imm27", (1L<<29), v1 / (1L<<33));
		Assert.assertEquals("imm28", (1L<<28), v1 / (1L<<34)); 
		Assert.assertEquals("imm29", 2, v1 / (1L<<61));
		Assert.assertEquals("imm30", 1, v1 / (1L<<62)); 
		v1 = -528795923411L;
		Assert.assertEquals("imm31", -528795923411L, v1 / 1);
		Assert.assertEquals("imm32", -264397961705L, v1 / 2);
		Assert.assertEquals("imm33", -4131218151L, v1 / 128);
		Assert.assertEquals("imm34", -492, v1 / (1L<<30)); 
		Assert.assertEquals("imm35", -246, v1 / (1L<<31)); 
		if(!CmdTransmitter.host){
			// this test gives lower result on host, as it is done without shift operations on the host
			Assert.assertEquals("imm36", -124, v1 / (1L<<32)); 
		}
		Assert.assertEquals("imm37", 0, v1 / (1L<<62)); 
		Assert.assertEquals("imm38", 2246037, v1 / -235435); 

		//test in loop
		v1 = 10000000000000L; v2 = 1000;
		while(v1 > v2) v1 /= v2;
		Assert.assertEquals("loop1", 10, v1);	
		v1 = 10000000000000L; 
		while(v1 > 1000) v1 /= 1000;
		Assert.assertEquals("loop2", 10, v1);	
		v1 = 10000000000000L; 
		while(v1 > 7000000000L) v1 /= 6000000000L;
		Assert.assertEquals("loop3", 1666, v1);	
		v1 = 10000000000000L; ;
		while(v1 > 1000) v1 /= 2;
		Assert.assertEquals("loop4", 582, v1);	
		v1 = 10000000000000L; ;
		while(v1 > 1000) v1 /= 4;
		Assert.assertEquals("loop5", 582, v1);	
		v1 = 10000000000000L; ;
		while(v1 != 4656) v1 /= 1L<<31;
		Assert.assertEquals("loop6", 4656, v1);	
		v1 = 10000000000000L; ;
		while(v1 != 2328) v1 /= 1L<<32;
		Assert.assertEquals("loop7", 2328, v1);	
		v1 = 10000000000000L; ;
		while(v1 != 1164) v1 /= 1L<<33;
		Assert.assertEquals("loop8", 1164, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Remainder test
	public static void testRem(){
		long res,v1,v2;
		v1 = 234234586784415326L; v2 = 316176574678L;
		res = (v1 % v2);
		Assert.assertEquals("test1", 230259413874L, res);
		v1 = 234234586784415326L; v2 = -316176574678L;
		res = (v1 % v2);
		Assert.assertEquals("test2", 230259413874L, res);
		v1 = -234234586784415326L; v2 = 316176574678L;
		res = (v1 % v2);
		Assert.assertEquals("test3", -230259413874L, res);
		v1 = -234234586784415326L; v2 = -316176574678L;
		res = (v1 % v2);
		Assert.assertEquals("test4", -230259413874L, res);
		v1 = 678675867854415321L; v2 = 2;
		res = (v1 % v2);
		Assert.assertEquals("test5", 1, res);
		res = 4415326; v1 = 31617; v2 = 618;
		res %= v1 % v2;
		Assert.assertEquals("test6", 25, res);     
		
		// immediate test
		v1 = 678675867854415321L;
		Assert.assertEquals("imm1", 0, v1 % 1);
		Assert.assertEquals("imm2", 1, v1 % 2);
		Assert.assertEquals("imm3", 0, v1 % 3);
		Assert.assertEquals("imm4", 1, v1 % 4);
		Assert.assertEquals("imm5", 89, v1 % 128);
		
		Assert.assertEquals("imm11", 622306777, v1 % (1L<<30));
		Assert.assertEquals("imm12", 180631110, v1 % ((1L<<30)-1));
		Assert.assertEquals("imm13", 1063982446, v1 % ((1L<<30)+1));
		Assert.assertEquals("imm14", 622306777, v1 % (1L<<31));
		Assert.assertEquals("imm15", 938339855, v1 % ((1L<<31)-1));
		Assert.assertEquals("imm16", 306273699, v1 % ((1L<<31)+1));
	
		Assert.assertEquals("imm21", 622306777, v1 % (1L<<32));
		Assert.assertEquals("imm22", 780323316, v1 % ((1L<<32)-1));
		Assert.assertEquals("imm23", 464290238, v1 % ((1L<<32)+1));
		Assert.assertEquals("imm24", 4917274073L, v1 % (1L<<33));
		Assert.assertEquals("imm25", 4917274072L, v1 % (1L<<33)-1);
		Assert.assertEquals("imm26", 4917274074L, v1 % (1L<<33)+1);
		
		Assert.assertEquals("imm31", 678675867854415321L, v1 % (1L<<60));
		Assert.assertEquals("imm32", 678675867854415321L, v1 % ((1L<<60)-1));
		Assert.assertEquals("imm33", 678675867854415321L, v1 % ((1L<<60)+1));

		v1 = -528795923411L;
		Assert.assertEquals("imm41", 0, v1 % 1);
		Assert.assertEquals("imm42", -1, v1 % 2);
		Assert.assertEquals("imm43", -83, v1 % 128);
		Assert.assertEquals("imm44", -514946003, v1 % (1L<<30)); 
		Assert.assertEquals("imm45", -514946003, v1 % (1L<<31)); 
		if(!CmdTransmitter.host){
			// this test gives lower result on host, as it is done without shift operations on the host
			Assert.assertEquals("imm46", 3780021293L, v1 % (1L<<32)); 
		}
		Assert.assertEquals("imm47", -528795923411L, v1 % (1L<<62)); 
		Assert.assertEquals("imm48", -202316, v1 % -235435); 

		//test in loop
		v1 = 1000000000000L; v2 = 300;
		while(v1 > v2) v1 %= v2;
		Assert.assertEquals("loop1", 100, v1);	
		v1 = 1000000000000L; v2 = 300;
		while(v1 > v2) v1 %= 300;
		Assert.assertEquals("loop2", 100, v1);	
		v1 = 1000000000000L; v2 = 3000000000L;
		while(v1 > v2) v1 %= v2;
		Assert.assertEquals("loop3", 1000000000, v1);	
		v1 = 10000000000001L; ;
		while(v1 > 1000) v1 %= 2;
		Assert.assertEquals("loop4", 1, v1);	
		v1 = 10000000000003L; ;
		while(v1 > 1000) v1 %= 4;
		Assert.assertEquals("loop5", 3, v1);	
		v1 = 10000000000000L; ;
		while(v1 >= 1L<<30) v1 %= 1L<<30;
		Assert.assertEquals("loop6", 242393088, v1);	
		v1 = 10000000050000L; ;
		while(v1 >= 1L<<31) v1 %= 1L<<31;
		Assert.assertEquals("loop7", 1316184912, v1);	
		v1 = 10000000450000L; ;
		while(v1 >= 1L<<32) v1 %= 1L<<32;
		Assert.assertEquals("loop8", 1316584912, v1);	
		v1 = 10000003450000L; ;
		while(v1 >= 1L<<33) v1 %= 1L<<33;
		Assert.assertEquals("loop9", 1319584912, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Logical shift right test
	public static void testLsr(){
		long res,v1,v2;
		//Normal lsr test
		v1 = 0x9988776655443322L; 
		v2 = 0;
		res = (v1 >>> v2);
		Assert.assertEquals("var0", 0x9988776655443322L, res);
		v2 = 4;
		res = (v1 >>> v2);
		Assert.assertEquals("var4", 0x998877665544332L, res);
		v2 = 32;
		res = (v1 >>> v2);
		Assert.assertEquals("var32", 0x99887766L, res);
		v2 = 60;
		res = (v1 >>> v2);
		Assert.assertEquals("var60", 9, res);
		v2 = 63;
		res = (v1 >>> v2);
		Assert.assertEquals("var63", 1, res);
		v2 = 64;
		res = (v1 >>> v2);
		Assert.assertEquals("var64", 0x9988776655443322L, res);
		res = (v1 >>> 0);
		Assert.assertEquals("imm0", 0x9988776655443322L, res);
		res = (v1 >>> 4);
		Assert.assertEquals("imm4", 0x998877665544332L, res);
		res = (v1 >>> 32);
		Assert.assertEquals("imm32", 0x99887766L, res);
		res = (v1 >>> 60);
		Assert.assertEquals("imm60", 9, res);
		res = (v1 >>> 63);
		Assert.assertEquals("imm63", 1, res);
		res = (v1 >>> 64);
		Assert.assertEquals("imm64", 0x9988776655443322L, res);
		//Short form test
		res = 0x9988776655443322L; v1 = 18; v2 = 2;
		res >>>= v1 >>> v2;
		Assert.assertEquals("shortForm", 0x998877665544332L, res);                 

		//test in loop
		v1 = 0x88776655443322L; v2 = 4;
		while(v1 > 0x3322) v1 >>>= v2;
		Assert.assertEquals("loop1", 0x887, v1);	
		v1 = 0x88776655443322L; 
		while(v1 > 0x3322) v1 >>>= 4;
		Assert.assertEquals("loop2", 0x887, v1);	
		v1 = 0x88776655443322L; 
		while(v1 != 0x88776) v1 >>>= 36;
		Assert.assertEquals("loop3", 0x88776, v1);	
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift right test
	public static void testAsr(){
		long res,v1,v2;
		//Normal Asr test
		v1 = 0x9988776655443322L; 
		v2 = 0;
		res = (v1 >> v2);
		Assert.assertEquals("var0", 0x9988776655443322L, res);
		v2 = 4;
		res = (v1 >> v2);
		Assert.assertEquals("var4", 0xf998877665544332L, res);
		v2 = 32;
		res = (v1 >> v2);
		Assert.assertEquals("var32", 0xffffffff99887766L, res);
		v2 = 36;
		res = (v1 >> v2);
		Assert.assertEquals("var36", 0xfffffffff9988776L, res);
		v2 = 60;
		res = (v1 >> v2);
		Assert.assertEquals("var60", 0xfffffffffffffff9L, res);
		v2 = 63;
		res = (v1 >> v2);
		Assert.assertEquals("var63", 0xffffffffffffffffL, res);
		v2 = 64;
		res = (v1 >> v2);
		Assert.assertEquals("var64", 0x9988776655443322L, res);
		v1 = 0x3388776655443322L; 
		v2 = 0;
		res = (v1 >> v2);
		Assert.assertEquals("var0_1", 0x3388776655443322L, res);
		v2 = 4;
		res = (v1 >> v2);
		Assert.assertEquals("var4_1", 0x0338877665544332L, res);
		v2 = 32;
		res = (v1 >> v2);
		Assert.assertEquals("var32_1", 0x000000033887766L, res);
		v2 = 36;
		res = (v1 >> v2);
		Assert.assertEquals("var36_1", 0x000000003388776L, res);
		v2 = 60;
		res = (v1 >> v2);
		Assert.assertEquals("var60_1", 0x000000000000003L, res);
		v2 = 63;
		res = (v1 >> v2);
		Assert.assertEquals("var63_1", 0x000000000000000L, res);
		v2 = 64;
		res = (v1 >> v2);
		Assert.assertEquals("var64_1", 0x3388776655443322L, res);
		v1 = 0x9988776655443322L; 
		res = (v1 >> 0);
		Assert.assertEquals("imm0", 0x9988776655443322L, res);
		res = (v1 >> 4);
		Assert.assertEquals("imm4", 0xf998877665544332L, res);
		res = (v1 >> 32);
		Assert.assertEquals("imm32", 0xffffffff99887766L, res);
		res = (v1 >> 60);
		Assert.assertEquals("imm60", 0xfffffffffffffff9L, res);
		res = (v1 >> 63);
		Assert.assertEquals("imm63", 0xffffffffffffffffL, res);
		res = (v1 >> 64);
		Assert.assertEquals("imm64", 0x9988776655443322L, res);
		v1 = 0x3388776655443322L; 
		res = (v1 >> 0);
		Assert.assertEquals("imm0_1", 0x3388776655443322L, res);
		res = (v1 >> 4);
		Assert.assertEquals("imm4_1", 0x0338877665544332L, res);
		res = (v1 >> 32);
		Assert.assertEquals("imm32_1", 0x000000033887766L, res);
		res = (v1 >> 60);
		Assert.assertEquals("imm60_1", 0x000000000000003L, res);
		res = (v1 >> 63);
		Assert.assertEquals("imm63_1", 0x000000000000000L, res);
		res = (v1 >> 64);
		Assert.assertEquals("imm64_1", 0x3388776655443322L, res);
		//Short form test
		res = 0x9988776655443322L; v1 = 18; v2 = 2;
		res >>= v1 >> v2;
		Assert.assertEquals("shortForm", 0xf998877665544332L, res);                 

		//test in loop
		v1 = 0x88776655443322L; v2 = 4;
		while(v1 > 0x3322) v1 >>= v2;
		Assert.assertEquals("loop1", 0x887, v1);	
		v1 = 0x88776655443322L; 
		while(v1 > 0x3322) v1 >>= 4;
		Assert.assertEquals("loop2", 0x887, v1);	
		v1 = 0x88776655443322L; 
		while(v1 != 0x88776) v1 >>= 36;
		Assert.assertEquals("loop3", 0x88776, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Arithmetic shift left test
	public static void testAsl(){
		long res,v1,v2;
		//Normal asl test
		v1 = 0x9988776655443322L; 
		res = (v1 << 0);
		Assert.assertEquals("imm0", 0x9988776655443322L, res);
		res = (v1 << 4);
		Assert.assertEquals("imm4", 0x9887766554433220L, res);
		res = (v1 << 16);
		Assert.assertEquals("imm16", 0x7766554433220000L, res);
		res = (v1 << 32); 
		Assert.assertEquals("imm32", 0x5544332200000000L, res);
		res = (v1 << 60); 
		Assert.assertEquals("imm60", 0x2000000000000000L, res);
		res = (v1 << 63); 
		Assert.assertEquals("imm63", 0, res);
		res = (v1 << 64); 
		Assert.assertEquals("imm64", 0x9988776655443322L, res);
		v2 = 0;
		res = (v1 << v2);
		Assert.assertEquals("var0", 0x9988776655443322L, res);
		v2 = 4;
		res = (v1 << v2);
		Assert.assertEquals("var4", 0x9887766554433220L, res);
		v2 = 16;
		res = (v1 << v2);
		Assert.assertEquals("var16", 0x7766554433220000L, res);
		v2 = 32;
		res = (v1 << v2);
		Assert.assertEquals("var32", 0x5544332200000000L, res);
		v2 = 60;
		res = (v1 << v2);
		Assert.assertEquals("var60", 0x2000000000000000L, res);
		v2 = 63;
		res = (v1 << v2);
		Assert.assertEquals("var63", 0, res);
		v2 = 64;
		res = (v1 << v2);
		Assert.assertEquals("var64", 0x9988776655443322L, res);
		//Short form test
		res = 0x9988776655443322L; v1 = 2; v2 = 3;
		res <<= v1 << v2;
		Assert.assertEquals("shortForm", 0x7766554433220000L, res);              

		//test in loop
		v1 = 0x123L; v2 = 4;
		while(v1 < 0x88776655443322L) v1 <<= v2;
		Assert.assertEquals("loop1", 0x123000000000000L, v1);	
		v1 = 0x123L; 
		while(v1 < 0x88776655443322L) v1 <<= 4;
		Assert.assertEquals("loop2", 0x123000000000000L, v1);	
		v1 = 0x123L; 
		while(v1 != 0x123000000000L) v1 <<= 36;
		Assert.assertEquals("loop3", 0x123000000000L, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//And test
	public static void testAnd(){
		long res,v1,v2;
		//Normal and test
		v1 = 0x22334455; v2 = 0x9988776600000000L;
		res = (v1 & v2);
		Assert.assertEquals("and1", 0, res);
		v1 = 0xff22334455L; v2 = 0x9988776600000000L;
		res = (v1 & v2);
		Assert.assertEquals("and2", 0x6600000000L, res);
		res = (v1 & 0xfff);
		Assert.assertEquals("imm1", 0x455L, res);
		res = (v1 & 0xfffff);
		Assert.assertEquals("imm2", 0x34455L, res);
		res = (v2 & 0xf000000000L);
		Assert.assertEquals("imm3", 0x6000000000L, res);
		res = (v2 & 0xf000000000000000L);
		Assert.assertEquals("imm4", 0x9000000000000000L, res);
		res = (v2 & 0xf000000f0000f000L);
		Assert.assertEquals("imm5", 0x9000000600000000L, res);
		res = (0xfff & v1);
		Assert.assertEquals("imm6", 0x455L, res);
		res = (0xfffff & v1);
		Assert.assertEquals("imm7", 0x34455L, res);
		res = (0xf000000000L & v2);
		Assert.assertEquals("imm8", 0x6000000000L, res);
		res = (0xf000000000000000L & v2);
		Assert.assertEquals("imm9", 0x9000000000000000L, res);
		res = (0xf000000f0000f000L & v2);
		Assert.assertEquals("imm10", 0x9000000600000000L, res);
		res = (v1 & -1);
		Assert.assertEquals("imm11", 0xff22334455L, res);
		res = (-1 & v1);
		Assert.assertEquals("imm12", 0xff22334455L, res);
		//Short form test
		res = 0x888877776666L; v1 = 0xccAA3F3FL; v2 = 0xffFF5252L;
		res &= v1 & v2;
		Assert.assertEquals("shortForm1", 0x44220202, res);                
		res = 0x888877776666L; 
		res &= v2;
		Assert.assertEquals("shortForm2", 0x77774242, res);                

		//test in loop
		v1 = 0x9988776655443322L; v2 = 0xff00000000000L;
		if(v1 != 0) v1 &= v2;
		Assert.assertEquals("loop1", 0x8700000000000L, v1);	
		v1 = 0x9988776655443322L; 
		if(v1 != 0) v1 &= 0xff00000000000L;
		Assert.assertEquals("loop2", 0x8700000000000L, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Or test
	public static void testOr(){
		long res,v1,v2;
		//Normal or test
		v1 = 0x1111222233334444L; v2 = 0x00ff00ff00ff00ffL;
		res = (v1 | v2);
		Assert.assertEquals("normal", 0x11ff22ff33ff44ffL, res);
		res = (v1 | 0xfff);
		Assert.assertEquals("imm1", 0x1111222233334fffL, res);
		res = (v1 | 0xfffff);
		Assert.assertEquals("imm2", 0x11112222333fffffL, res);
		res = (v1 | 0xffffffff00000000L);
		Assert.assertEquals("imm3", 0xffffffff33334444L, res);
		res = (0xfff | v1);
		Assert.assertEquals("imm4", 0x1111222233334fffL, res);
		res = (0xfffff | v1);
		Assert.assertEquals("imm5", 0x11112222333fffffL, res);
		res = (0xffffffff00000000L | v1);
		Assert.assertEquals("imm6", 0xffffffff33334444L, res);
		res = (v1 | -1);
		Assert.assertEquals("imm11", 0xffffffffffffffffL, res);
		res = (-1 | v1);
		Assert.assertEquals("imm12", 0xffffffffffffffffL, res);
		//Short form test
		res = 0x1111222233334444L; v1 = 0xfffL; v2 = 0x88000000L;
		res |= v1 | v2;
		Assert.assertEquals("shortForm", 0x11112222bb334fffL, res);             

		//test in loop
		v1 = 0x9988776655443322L; v2 = 0xff00000000000L;
		if(v1 != 0) v1 |= v2;
		Assert.assertEquals("loop1", 0x998ff76655443322L, v1);	
		v1 = 0x9988776655443322L; 
		if(v1 != 0) v1 |= 0xff00000000000L;
		Assert.assertEquals("loop2", 0x998ff76655443322L, v1);	

		CmdTransmitter.sendDone();
	}
	
	@Test
	//Xor test
	public static void testXor(){
		long res,v1,v2;
		//Normal xor test
		v1 = 0x1111222233334444L; v2 = 0x0000ffff0000ffffL;
		res = (v1 ^ v2);
		Assert.assertEquals("normal", 0x1111dddd3333bbbbL, res);
		res = (v1 ^ 0xfff);
		Assert.assertEquals("imm1", 0x1111222233334bbbL, res);
		res = (v1 ^ 0xfffff);
		Assert.assertEquals("imm2", 0x11112222333cbbbbL, res);
		res = (v1 ^ 0xffff0000ffff0000L);
		Assert.assertEquals("imm3", 0xeeee2222cccc4444L, res);
		res = (0xfff ^ v1);
		Assert.assertEquals("imm4", 0x1111222233334bbbL, res);
		res = (0xfffff ^ v1);
		Assert.assertEquals("imm5", 0x11112222333cbbbbL, res);
		res = (0xffff0000ffff0000L ^ v1);
		Assert.assertEquals("imm6", 0xeeee2222cccc4444L, res);
		res = (v1 ^ -1);
		Assert.assertEquals("imm7", 0xeeeeddddccccbbbbL, res);
		res = (-1 ^ v1);
		Assert.assertEquals("imm8", 0xeeeeddddccccbbbbL, res);
		//Short form test
		res = 0x1111222233334444L; v1 = 0xf00000000L; v2 = 0xff000000fL;
		res ^= v1 ^ v2;
		Assert.assertEquals("shortForm", 0x11112222c333444bL, res);                
	
		CmdTransmitter.sendDone();
	}
	
	@Test
	//Not test
	public static void testNot(){
		long res,v1;
		//Normal not test
		v1 = 0xaaaa55554444eeeeL;
		res = ~v1;
		Assert.assertEquals("testNot", 0x5555aaaabbbb1111L, res);                

		CmdTransmitter.sendDone();
	}

	@Test
	//Neg test
	public static void testNeg(){
		long l = 1L;
		Assert.assertEquals("neg1", -1L, -l);                
		l = -1L;
		Assert.assertEquals("neg2", 1, -l);                
		l = 0x80000000L;
		Assert.assertEquals("neg3", 0xFFFFFFFF80000000L, -l);                
		l = 0x7fffffffffffffffL;
		Assert.assertEquals("neg4", 0x8000000000000001L, -l);                
		l = 0x8000000000000000L;
		Assert.assertEquals("neg5", 0x8000000000000000L, -l);                

		CmdTransmitter.sendDone();
	}

	@Test
	//Priority rules test
	public static void testPrio(){
		long res,v1,v2;
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
		long res,v1,v2;
		//Grade 2 and 3
		v1 = 0xABCD; v2 = 0x5555;
		res = ((v1 + v2) / (v1 - v2) * (v1 + v2) % (v1 + v2)); 
		Assert.assertEquals("grade23", 0, res);
		//Grade 3 and 4
		v1 = 130; v2 = 125;
		res = (v1 + (v2 >> v2) - (v1 << -v1) + (v2 >>> v2) - v1); 
		Assert.assertEquals("grade34", -9223372036854775808L, res);
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
