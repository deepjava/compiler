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

package org.deepjava.comp.targettest.objects;

import org.deepjava.comp.targettest.objects.helper.exampleA.*;
import org.deepjava.comp.targettest.objects.helper.exampleB.*;
import org.deepjava.comp.targettest.objects.helper.exampleC.*;
import org.deepjava.comp.targettest.objects.helper.exampleD.*;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 12.03.2013
 * 
 * @author Urs Graf
 * 
 * Tests for interfaces (instanceof and methods)
 */

@MaxErrors(100)
@SuppressWarnings("static-access")
public class InterfaceTest {

	@Test
	// tests constants
	public static void testConst() {

		Assert.assertEquals("var0", 23,CAexD.var0);
		Assert.assertEquals("var1", 2.125,CAexD.var1, 0.01);
		Assert.assertEquals("var2", 1.15f, CAexD.var2 , 0.01f);
		Assert.assertTrue("var3", CAexD.var3);
		Assert.assertEquals("var4", 0x5555555555L, CAexD.var4);
		Assert.assertEquals("var5", (byte)127, CAexD.var5);
		Assert.assertEquals("var6", (short)256,CAexD.var6);
		Assert.assertEquals("var7", 'Z', CAexD.var7);
				
		Assert.assertEquals("i1Var0", 23,CBexD.getVar0());
		Assert.assertEquals("i1Var1", 2.125,CBexD.getVar1(), 0.01);
		Assert.assertEquals("i1Var2", 1.15f, CBexD.getVar2() , 0.01f);
		Assert.assertTrue("i1Var3", CBexD.getVar3());
		Assert.assertEquals("i1Var4", 0x5555555555L, CBexD.getVar4());
		Assert.assertEquals("i1Var5", (byte)127, CBexD.getVar5());
		Assert.assertEquals("i1Var6", (short)256,CBexD.getVar6());
		Assert.assertEquals("i1Var7", 'Z', CBexD.getVar7());
		Assert.assertEquals("i2Var0", 45,CBexD.getI2Var0());
		Assert.assertEquals("i2Var1", 3.1459,CBexD.getI2Var1(), 0.01);
		Assert.assertEquals("i2Var2", 3.33f, CBexD.getI2Var2() , 0.01f);
		Assert.assertFalse("i2Var3", CBexD.getI2Var3());
		Assert.assertEquals("i2Var4", 0xAAAAAAAAAAL, CBexD.getI2Var4());
		Assert.assertEquals("i2Var5", (byte)-128, CBexD.getI2Var5());
		Assert.assertEquals("i2Var6", (short)-264,CBexD.getI2Var6());
		Assert.assertEquals("i2Var7", 'B', CBexD.getI2Var7());
		
		Assert.assertEquals("var0", 23,CCexD.var0);
		Assert.assertEquals("var1", 2.125,CCexD.var1, 0.01);
		Assert.assertEquals("var2", 1.15f, CCexD.var2 , 0.01f);
		Assert.assertTrue("var3", CCexD.var3);
		Assert.assertEquals("var4", 0x5555555555L, CCexD.var4);
		Assert.assertEquals("var5", (byte)127, CCexD.var5);
		Assert.assertEquals("var6", (short)256,CCexD.var6);
		Assert.assertEquals("var7", 'Z', CCexD.var7);
		Assert.assertEquals("var8", 89,CCexD.var8);
		Assert.assertEquals("var9", 7.775,CCexD.var9, 0.01);
		Assert.assertEquals("var10", 9.32f, CCexD.var10 , 0.01f);
		Assert.assertFalse("var11", CCexD.var11);
		Assert.assertEquals("var12", 0xBBBBBBBBBBL, CCexD.var12);
		Assert.assertEquals("var13", (byte)64, CCexD.var13);
		Assert.assertEquals("var14", (short)1023,CCexD.var14);
		Assert.assertEquals("var15", 'L', CCexD.var15);
		
		Assert.assertEquals("var0", 23,CDexD.var0);
		Assert.assertEquals("var1", 2.125,CDexD.var1, 0.01);
		Assert.assertEquals("var2", 1.15f, CDexD.var2 , 0.01f);
		Assert.assertTrue("var3", CDexD.var3);
		Assert.assertEquals("var4", 0x5555555555L, CDexD.var4);
		Assert.assertEquals("var5", (byte)127, CDexD.var5);
		Assert.assertEquals("var6", (short)256,CDexD.var6);
		Assert.assertEquals("var7", 'Z', CDexD.var7);
		
		CmdTransmitter.sendDone();
	}
	
	public static void testInstance1() {
		Object clz1 = new CAexD(); 
		Object clz2 = new CBexD(); 
		Object clz3 = new CCexD(); 
		Object clz4 = new CDexD();
		Object clz5 = new CEexD();
				
		Assert.assertTrue("instance1", clz1 instanceof IAexD);		
		Assert.assertFalse("instance2", clz1 instanceof IBexD);		
		Assert.assertFalse("instance3", clz1 instanceof ICexD);		
		Assert.assertFalse("instance4", clz1 instanceof IDexD);
		
		Assert.assertTrue("instance11", clz2 instanceof IAexD);		
		Assert.assertFalse("instance12", clz2 instanceof IBexD);		
		Assert.assertTrue("instance13", clz2 instanceof ICexD);		
		Assert.assertFalse("instance14", clz2 instanceof IDexD);
		
		Assert.assertTrue("instance21", clz3 instanceof IAexD);		
		Assert.assertFalse("instance22", clz3 instanceof IBexD);		
		Assert.assertTrue("instance23", clz3 instanceof ICexD);		
		Assert.assertFalse("instance24", clz3 instanceof IDexD);
		
		Assert.assertTrue("instance31", clz4 instanceof IAexD);		
		Assert.assertFalse("instance32", clz4 instanceof IBexD);		
		Assert.assertFalse("instance33", clz4 instanceof ICexD);		
		Assert.assertFalse("instance34", clz4 instanceof IDexD);
		
		Assert.assertTrue("instance41", clz5 instanceof IAexD);		
		Assert.assertTrue("instance42", clz5 instanceof IBexD);		
		Assert.assertFalse("instance43", clz5 instanceof ICexD);		
		Assert.assertTrue("instance44", clz5 instanceof IDexD);
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance2() {
		Object cls = new CAexD[2]; 
				
		Assert.assertFalse("instance1", cls instanceof IAexD);		
		Assert.assertTrue("instance2", cls instanceof IAexD[]);		
		Assert.assertFalse("instance3", cls instanceof IAexD[][]);		
		Assert.assertFalse("instance9", cls instanceof IBexD);		
		Assert.assertFalse("instance10", cls instanceof IBexD[]);		
		Assert.assertFalse("instance11", cls instanceof IBexD[][]);		

		cls = new CEexD[1]; 
		
		Assert.assertTrue("instance21", cls instanceof IAexD[]);		
		Assert.assertTrue("instance22", cls instanceof IBexD[]);		
		Assert.assertFalse("instance23", cls instanceof IAexD[][]);		
		Assert.assertFalse("instance26", cls instanceof ICexD[]);		
		Assert.assertTrue("instance27", cls instanceof IDexD[]);		
		Assert.assertFalse("instance28", cls instanceof IDexD[][]);		

		cls = new CEexD[1][2]; 
		
		Assert.assertFalse("instance41", cls instanceof IAexD[]);		
		Assert.assertTrue("instance42", cls instanceof IAexD[][]);		
		Assert.assertFalse("instance43", cls instanceof IAexD[][][]);		
		Assert.assertFalse("instance44", cls instanceof IBexD[]);		
		Assert.assertTrue("instance45", cls instanceof IBexD[][]);			
		Assert.assertFalse("instance46", cls instanceof IBexD[][][]);			
		Assert.assertFalse("instance47", cls instanceof ICexD[]);		
		Assert.assertFalse("instance48", cls instanceof ICexD[][]);		
		Assert.assertFalse("instance49", cls instanceof IDexD[]);		
		Assert.assertTrue("instance50", cls instanceof IDexD[][]);		

		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance3() {
		Object o = new CXexA(); 
		
		Assert.assertFalse("instance1", o instanceof IAexD);		
		Assert.assertTrue("instance2", o instanceof IAexA);		
		Assert.assertFalse("instance3", o instanceof IAexA[]);		

		o = new CYexA(); 
		
		Assert.assertTrue("instance11", o instanceof IBexA);		
		Assert.assertFalse("instance12", o instanceof IBexA[]);		
		Assert.assertFalse("instance13", o instanceof IAexA);		

		o = new CYexA[2]; 
		
		Assert.assertTrue("instance21", o instanceof IBexA[]);		
		Assert.assertFalse("instance22", o instanceof IBexA);		
		Assert.assertFalse("instance23", o instanceof IAexA[]);		

		o = new CEexA[2]; 
		
		Assert.assertFalse("instance31", o instanceof IAexA[]);				
		Assert.assertTrue("instance33", o instanceof IBexA[]);		
		Assert.assertTrue("instance34", o instanceof ICexA[]);		
		Assert.assertTrue("instance35", o instanceof IDexA[]);		
		Assert.assertTrue("instance36", o instanceof IEexA[]);				
		Assert.assertFalse("instance38", o instanceof IBexA);			
		Assert.assertFalse("instance40", o instanceof IDexA[][]);	
		
		o = new IAexA[2];
		Assert.assertTrue("instance50", o instanceof IAexA[]);				
		Assert.assertFalse("instance51", o instanceof IBexA[]);				
		
		o = new IDexA[2];
		Assert.assertFalse("instance60", o instanceof IAexA[]);				
		Assert.assertTrue("instance61", o instanceof ICexA[]);				
		Assert.assertTrue("instance62", o instanceof IDexA[]);				
		Assert.assertFalse("instance63", o instanceof CCexA[]);				
		Assert.assertFalse("instance64", o instanceof ICexA);				
		Assert.assertFalse("instance65", o instanceof ICexA[][]);				
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance4() {
		Object o = new CDexC(); 
		Assert.assertTrue("test1", o instanceof IAexC);		
		Assert.assertTrue("test2", o instanceof IBexC);		
		Assert.assertTrue("test3", o instanceof ICexC);		
		Assert.assertTrue("test4", o instanceof IDexC);		
		Assert.assertTrue("test5", o instanceof IEexC);		
		
		o = new CEexC(); 
		Assert.assertTrue("test10", o instanceof IAexC);		
		Assert.assertTrue("test11", o instanceof IBexC);		
		Assert.assertTrue("test12", o instanceof ICexC);		
		Assert.assertTrue("test13", o instanceof IDexC);		
		Assert.assertTrue("test14", o instanceof IEexC);	
		
		o = new CCexC(); 
		Assert.assertTrue("test20", o instanceof IAexC);		
		Assert.assertTrue("test21", o instanceof IBexC);		
		Assert.assertTrue("test22", o instanceof ICexC);		
		Assert.assertFalse("test23", o instanceof IDexC);		
		Assert.assertFalse("test24", o instanceof IEexC);	
		
		o = new CBexC(); 
		Assert.assertFalse("test30", o instanceof IAexC);		
		Assert.assertFalse("test31", o instanceof IBexC);		
		Assert.assertFalse("test32", o instanceof ICexC);		
		Assert.assertFalse("test33", o instanceof IDexC);		
		Assert.assertFalse("test34", o instanceof IEexC);	
		
		CmdTransmitter.sendDone();
	}

	@Test
	// tests a class implementing a single interface with a single method
	public static void testMethods1() {
		IAexA cls = new CXexA();
		Assert.assertEquals("test1", 1, cls.ima11());
		IAexB cls1 = new CXexB();
		Assert.assertEquals("test1", 101, cls1.ima11());
		CmdTransmitter.sendDone();		
	}

	@Test
	// tests a class implementing a single interface with several methods
	public static void testMethods2(){
		IBexA cls = new CYexA();
		Assert.assertEquals("test1", 22 ,cls.imb11());
		Assert.assertEquals("test2", 23 ,cls.imb12());
		cls = new CAexA();
		Assert.assertEquals("test3", 26 ,cls.imb11());
		Assert.assertEquals("test4", 27 ,cls.imb12());
		IAexD cls1 = new CAexD();
		Assert.assertEquals("test5", 5 ,cls1.ima11());
		Assert.assertEquals("test6", 266 ,cls1.ima12(10));
		CmdTransmitter.sendDone();		
	}

	@Test
	// tests a class implementing several interfaces
	public static void testMethods3(){
		IBexA cls1 = new CCexA();
		Assert.assertEquals("test1", 26 ,cls1.imb11());
		Assert.assertEquals("test2", 27 ,cls1.imb12());
		ICexA cls2 = new CCexA();
		Assert.assertEquals("test3", 32, cls2.imc11());
		Assert.assertEquals("test4", 33, cls2.imc12());
		IDexA cls3 = new CZexA();
		Assert.assertEquals("test10", 122, cls3.imc11());
		Assert.assertEquals("test11", 123, cls3.imc12());
		Assert.assertEquals("test12", 124, cls3.imd21());
		CEexA cls4 = new CEexA();
		Assert.assertEquals("test20", 40, cls4.cme41());
		Assert.assertEquals("test21", 41, cls4.imd21());
		
		CmdTransmitter.sendDone();		
	}

	@Test
	//Test overriding methods and interface methods
	public static void testMethods4(){
		CAexD cls1 = new CAexD();
		CBexD cls2 = new CBexD();

		Assert.assertEquals("test1", 5, cls1.ima11());
		Assert.assertEquals("test2", 259, cls1.ima12(3));
		
		Assert.assertEquals("test11", -1, cls2.ima11());
		Assert.assertEquals("test12", 25, cls2.ima12(5));
		Assert.assertEquals("test13", 30, cls2.imc11());

		CmdTransmitter.sendDone();		
	}

	@Test
	// tests a class implementing several interfaces
	public static void testMethods5(){
		IEexB cls1 = new CZexB();
		Assert.assertEquals("test1", 104, cls1.imXY());
		Assert.assertEquals("test2", 102, cls1.imX1());
		IGexB cls2 = new CTexB(2);
		Assert.assertEquals("test3", 43, cls2.imXY());
		Assert.assertEquals("test4", 42, cls2.imf21());
		Assert.assertEquals("test5", 61, cls2.img31());
		IBexB cls3 = new CRexB();
		Assert.assertEquals("test6", 31, cls3.ima11());
		Assert.assertEquals("test7", 32, cls3.imX1());

		CmdTransmitter.sendDone();		
	}

	@SuppressWarnings("unused")
	@Test
	// tests the class constructor of an interface
	public static void testInterfaceConstructor1() {
		Object cls = new CXexA();
		int a = ((CXexA)cls).ima11();
		Object o = ((CXexA)cls).cmx11();
		Assert.assertTrue("test1", o != null);
		String str = ((CXexA)cls).str1;
		Assert.assertEquals("test2", str, "xyz");
		str = ((CXexA)cls).str2[0];
		Assert.assertEquals("test3", str, "abc");
		str = ((CXexA)cls).str2[2];
		Assert.assertEquals("test4", str, "ghi");

		CmdTransmitter.sendDone();		
	}

	@Test
	// tests the class constructor of an interface
	public static void testInterfaceConstructor2() {
		Object cls = new CAexA();
		String str = ((CAexA)cls).str1;
		Assert.assertEquals("test1", str, "xyz");
		str = ((CAexA)cls).str2[0];
		Assert.assertEquals("test2", str, "abc");
		str = ((CAexA)cls).str2[2];
		Assert.assertEquals("test3", str, "ghi");

		CmdTransmitter.sendDone();		
	}
}
