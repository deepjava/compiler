package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.deep.comp.targettest.objects.helper.*;

/**
 * NTB 12.04.2011
 * 
 * @author Roger Millischer
 * 
 */

@MaxErrors(100)
@SuppressWarnings("static-access")
public class InterfaceTest {

	@Test
	// tests constants
	public static void testConst(){

		Assert.assertEquals("TestInterfaceClass1.var0", 23,InterfaceTestClass1.var0);
		Assert.assertEquals("TestInterfaceClass1.var1", 2.125,InterfaceTestClass1.var1, 0.01);
		Assert.assertEquals("TestInterfaceClass1.var2", 1.15f, InterfaceTestClass1.var2 , 0.01f);
		Assert.assertTrue("TestInterfaceClass1.var3", InterfaceTestClass1.var3);
		Assert.assertEquals("TestInterfaceClass1.var4", 0x5555555555L, InterfaceTestClass1.var4);
		Assert.assertEquals("TestInterfaceClass1.var5", (byte)127, InterfaceTestClass1.var5);
		Assert.assertEquals("TestInterfaceClass1.var6", (short)256,InterfaceTestClass1.var6);
		Assert.assertEquals("TestInterfaceClass1.var7", 'Z', InterfaceTestClass1.var7);
				
		Assert.assertEquals("TestInterfaceClass2.i1Var0", 23,InterfaceTestClass2.getVar0());
		Assert.assertEquals("TestInterfaceClass2.i1Var1", 2.125,InterfaceTestClass2.getVar1(), 0.01);
		Assert.assertEquals("TestInterfaceClass2.i1Var2", 1.15f, InterfaceTestClass2.getVar2() , 0.01f);
		Assert.assertTrue("TestInterfaceClass2.i1Var3", InterfaceTestClass2.getVar3());
		Assert.assertEquals("TestInterfaceClass2.i1Var4", 0x5555555555L, InterfaceTestClass2.getVar4());
		Assert.assertEquals("TestInterfaceClass2.i1Var5", (byte)127, InterfaceTestClass2.getVar5());
		Assert.assertEquals("TestInterfaceClass2.i1Var6", (short)256,InterfaceTestClass2.getVar6());
		Assert.assertEquals("TestInterfaceClass2.i1Var7", 'Z', InterfaceTestClass2.getVar7());
		Assert.assertEquals("TestInterfaceClass2.i2Var0", 45,InterfaceTestClass2.getI2Var0());
		Assert.assertEquals("TestInterfaceClass2.i2Var1", 3.1459,InterfaceTestClass2.getI2Var1(), 0.01);
		Assert.assertEquals("TestInterfaceClass2.i2Var2", 3.33f, InterfaceTestClass2.getI2Var2() , 0.01f);
		Assert.assertFalse("TestInterfaceClass2.i2Var3", InterfaceTestClass2.getI2Var3());
		Assert.assertEquals("TestInterfaceClass2.i2Var4", 0xAAAAAAAAAAL, InterfaceTestClass2.getI2Var4());
		Assert.assertEquals("TestInterfaceClass2.i2Var5", (byte)-128, InterfaceTestClass2.getI2Var5());
		Assert.assertEquals("TestInterfaceClass2.i2Var6", (short)-264,InterfaceTestClass2.getI2Var6());
		Assert.assertEquals("TestInterfaceClass2.i2Var7", 'B', InterfaceTestClass2.getI2Var7());
		
		Assert.assertEquals("TestInterfaceClass3.var0", 23,InterfaceTestClass3.var0);
		Assert.assertEquals("TestInterfaceClass3.var1", 2.125,InterfaceTestClass3.var1, 0.01);
		Assert.assertEquals("TestInterfaceClass3.var2", 1.15f, InterfaceTestClass3.var2 , 0.01f);
		Assert.assertTrue("TestInterfaceClass3.var3", InterfaceTestClass3.var3);
		Assert.assertEquals("TestInterfaceClass3.var4", 0x5555555555L, InterfaceTestClass3.var4);
		Assert.assertEquals("TestInterfaceClass3.var5", (byte)127, InterfaceTestClass3.var5);
		Assert.assertEquals("TestInterfaceClass3.var6", (short)256,InterfaceTestClass3.var6);
		Assert.assertEquals("TestInterfaceClass3.var7", 'Z', InterfaceTestClass3.var7);
		Assert.assertEquals("TestInterfaceClass3.var8", 89,InterfaceTestClass3.var8);
		Assert.assertEquals("TestInterfaceClass3.var9", 7.775,InterfaceTestClass3.var9, 0.01);
		Assert.assertEquals("TestInterfaceClass3.var10", 9.32f, InterfaceTestClass3.var10 , 0.01f);
		Assert.assertFalse("TestInterfaceClass3.var11", InterfaceTestClass3.var11);
		Assert.assertEquals("TestInterfaceClass3.var12", 0xBBBBBBBBBBL, InterfaceTestClass3.var12);
		Assert.assertEquals("TestInterfaceClass3.var13", (byte)64, InterfaceTestClass3.var13);
		Assert.assertEquals("TestInterfaceClass3.var14", (short)1023,InterfaceTestClass3.var14);
		Assert.assertEquals("TestInterfaceClass3.var15", 'L', InterfaceTestClass3.var15);
		
		Assert.assertEquals("TestInterfaceClass4.var0", 23,InterfaceTestClass4.var0);
		Assert.assertEquals("TestInterfaceClass4.var1", 2.125,InterfaceTestClass4.var1, 0.01);
		Assert.assertEquals("TestInterfaceClass4.var2", 1.15f, InterfaceTestClass4.var2 , 0.01f);
		Assert.assertTrue("TestInterfaceClass4.var3", InterfaceTestClass4.var3);
		Assert.assertEquals("TestInterfaceClass4.var4", 0x5555555555L, InterfaceTestClass4.var4);
		Assert.assertEquals("TestInterfaceClass4.var5", (byte)127, InterfaceTestClass4.var5);
		Assert.assertEquals("TestInterfaceClass4.var6", (short)256,InterfaceTestClass4.var6);
		Assert.assertEquals("TestInterfaceClass4.var7", 'Z', InterfaceTestClass4.var7);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// tests instances
	public static void testInstance(){
//		int res;
//		Object obj = new InterfaceTestClass1();
//		
//		if (obj instanceof Object) res = 1; else res = 2;
//		Assert.assertEquals("Testclass10", 1, res);
//		if (obj instanceof InterfaceTestClass1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass11", 1, res);
//		if (obj instanceof Interface1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass12", 1, res);
//		if (obj instanceof Interface2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass13", 2, res);
//		if (obj instanceof Interface3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass14", 2, res);
//		if (obj instanceof InterfaceTestClass2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass15", 2, res);
//		if (obj instanceof InterfaceTestClass3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass16", 2, res);
//		if (obj instanceof InterfaceTestClass4) res = 1; else res = 2;
//		Assert.assertEquals("Testclass17", 2, res);
//		
//		obj = new InterfaceTestClass2();
//		
//		if (obj instanceof Object) res = 1; else res = 2;
//		Assert.assertEquals("Testclass20", 1, res);
//		if (obj instanceof InterfaceTestClass2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass21", 1, res);
//		if (obj instanceof InterfaceTestClass1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass22", 1, res);
//		if (obj instanceof Interface1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass23", 1, res);
//		if (obj instanceof Interface2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass24", 1, res);
//		if (obj instanceof Interface3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass25", 2, res);
//		if (obj instanceof InterfaceTestClass3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass26", 2, res);
//		if (obj instanceof InterfaceTestClass4) res = 1; else res = 2;
//		Assert.assertEquals("Testclass27", 2, res);
//		
//		obj = new InterfaceTestClass3();
//		
//		if (obj instanceof Object) res = 1; else res = 2;
//		Assert.assertEquals("Testclass30", 1, res);
//		if (obj instanceof InterfaceTestClass3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass31", 1, res);
//		if (obj instanceof InterfaceTestClass1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass32", 1, res);
//		if (obj instanceof Interface1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass33", 1, res);
//		if (obj instanceof Interface3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass34", 1, res);
//		if (obj instanceof Interface2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass35", 2, res);
//		if (obj instanceof InterfaceTestClass2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass36", 2, res);
//		if (obj instanceof InterfaceTestClass4) res = 1; else res = 2;
//		Assert.assertEquals("Testclass37", 2, res);
//		
//		obj = new InterfaceTestClass4();
//		
//		if (obj instanceof Object) res = 1; else res = 2;
//		Assert.assertEquals("Testclass40", 1, res);
//		if (obj instanceof InterfaceTestClass4) res = 1; else res = 2;
//		Assert.assertEquals("Testclass41", 1, res);
//		if (obj instanceof InterfaceTestClass1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass42", 1, res);
//		if (obj instanceof Interface1) res = 1; else res = 2;
//		Assert.assertEquals("Testclass43", 1, res);
//		if (obj instanceof Interface2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass44", 2, res);
//		if (obj instanceof Interface3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass45", 2, res);
//		if (obj instanceof InterfaceTestClass2) res = 1; else res = 2;
//		Assert.assertEquals("Testclass46", 2, res);
//		if (obj instanceof InterfaceTestClass3) res = 1; else res = 2;
//		Assert.assertEquals("Testclass47", 2, res);		
//		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// tests type guards
	public static void testTypeGuard(){
//		Object obj = new InterfaceTestClass1();
//			
//		Assert.assertEquals("TypeGuardClass1.var0", 23,((InterfaceTestClass1)obj).var0);
//		Assert.assertEquals("TypeGuardClass1.var1", 2.125,((InterfaceTestClass1)obj).var1, 0.01);
//		Assert.assertEquals("TypeGuardClass1.var2", 1.15f, ((InterfaceTestClass1)obj).var2 , 0.01f);
//		Assert.assertTrue("TypeGuardClass1.var3", ((InterfaceTestClass1)obj).var3);
//		Assert.assertEquals("TypeGuardClass1.var4", 0x5555555555L, ((InterfaceTestClass1)obj).var4);
//		Assert.assertEquals("TypeGuardClass1.var5", (byte)127, ((InterfaceTestClass1)obj).var5);
//		Assert.assertEquals("TypeGuardClass1.var6", (short)256,((InterfaceTestClass1)obj).var6);
//		Assert.assertEquals("TypeGuardClass1.var7", 'Z', ((InterfaceTestClass1)obj).var7);
//				
//		obj = new InterfaceTestClass2();
//		
//		Assert.assertEquals("TypeGuardClass2.i1Var0", 23,((InterfaceTestClass2)obj).getVar0());
//		Assert.assertEquals("TypeGuardClass2.i1Var1", 2.125,((InterfaceTestClass2)obj).getVar1(), 0.01);
//		Assert.assertEquals("TypeGuardClass2.i1Var2", 1.15f, ((InterfaceTestClass2)obj).getVar2() , 0.01f);
//		Assert.assertTrue("TypeGuardClass2.i1Var3", ((InterfaceTestClass2)obj).getVar3());
//		Assert.assertEquals("TypeGuardClass2.i1Var4", 0x5555555555L, ((InterfaceTestClass2)obj).getVar4());
//		Assert.assertEquals("TypeGuardClass2.i1Var5", (byte)127, ((InterfaceTestClass2)obj).getVar5());
//		Assert.assertEquals("TypeGuardClass2.i1Var6", (short)256,((InterfaceTestClass2)obj).getVar6());
//		Assert.assertEquals("TypeGuardClass2.i1Var7", 'Z', ((InterfaceTestClass2)obj).getVar7());
//		Assert.assertEquals("TypeGuardClass2.i2Var0", 45,((InterfaceTestClass2)obj).getI2Var0());
//		Assert.assertEquals("TypeGuardClass2.i2Var1", 3.1459,((InterfaceTestClass2)obj).getI2Var1(), 0.01);
//		Assert.assertEquals("TypeGuardClass2.i2Var2", 3.33f, ((InterfaceTestClass2)obj).getI2Var2() , 0.01f);
//		Assert.assertFalse("TypeGuardClass2.i2Var3", ((InterfaceTestClass2)obj).getI2Var3());
//		Assert.assertEquals("TypeGuardClass2.i2Var4", 0xAAAAAAAAAAL, ((InterfaceTestClass2)obj).getI2Var4());
//		Assert.assertEquals("TypeGuardClass2.i2Var5", (byte)-128, ((InterfaceTestClass2)obj).getI2Var5());
//		Assert.assertEquals("TypeGuardClass2.i2Var6", (short)-264,((InterfaceTestClass2)obj).getI2Var6());
//		Assert.assertEquals("TypeGuardClass2.i2Var7", 'B', ((InterfaceTestClass2)obj).getI2Var7());
//		
//		obj = new InterfaceTestClass3();
//		
//		Assert.assertEquals("TypeGuardClass3.var0", 23,((InterfaceTestClass3)obj).var0);
//		Assert.assertEquals("TypeGuardClass3.var1", 2.125,((InterfaceTestClass3)obj).var1, 0.01);
//		Assert.assertEquals("TypeGuardClass3.var2", 1.15f, ((InterfaceTestClass3)obj).var2 , 0.01f);
//		Assert.assertTrue("TypeGuardClass3.var3", ((InterfaceTestClass3)obj).var3);
//		Assert.assertEquals("TypeGuardClass3.var4", 0x5555555555L, ((InterfaceTestClass3)obj).var4);
//		Assert.assertEquals("TypeGuardClass3.var5", (byte)127, ((InterfaceTestClass3)obj).var5);
//		Assert.assertEquals("TypeGuardClass3.var6", (short)256,((InterfaceTestClass3)obj).var6);
//		Assert.assertEquals("TypeGuardClass3.var7", 'Z', ((InterfaceTestClass3)obj).var7);
//		Assert.assertEquals("TypeGuardClass3.var8", 89,((InterfaceTestClass3)obj).var8);
//		Assert.assertEquals("TypeGuardClass3.var9", 7.775,((InterfaceTestClass3)obj).var9, 0.01);
//		Assert.assertEquals("TypeGuardClass3.var10", 9.32f, ((InterfaceTestClass3)obj).var10 , 0.01f);
//		Assert.assertFalse("TypeGuardClass3.var11", ((InterfaceTestClass3)obj).var11);
//		Assert.assertEquals("TypeGuardClass3.var12", 0xBBBBBBBBBBL, ((InterfaceTestClass3)obj).var12);
//		Assert.assertEquals("TypeGuardClass3.var13", (byte)64, ((InterfaceTestClass3)obj).var13);
//		Assert.assertEquals("TypeGuardClass3.var14", (short)1023,((InterfaceTestClass3)obj).var14);
//		Assert.assertEquals("TypeGuardClass3.var15", 'L', ((InterfaceTestClass3)obj).var15);
//		
//		obj = new InterfaceTestClass4();
//		
//		Assert.assertEquals("TypeGuardClass4.var0", 23,((InterfaceTestClass4)obj).var0);
//		Assert.assertEquals("TypeGuardClass4.var1", 2.125,((InterfaceTestClass4)obj).var1, 0.01);
//		Assert.assertEquals("TypeGuardClass4.var2", 1.15f, ((InterfaceTestClass4)obj).var2 , 0.01f);
//		Assert.assertTrue("TypeGuardClass4.var3", ((InterfaceTestClass4)obj).var3);
//		Assert.assertEquals("TypeGuardClass4.var4", 0x5555555555L, ((InterfaceTestClass4)obj).var4);
//		Assert.assertEquals("TypeGuardClass4.var5", (byte)127, ((InterfaceTestClass4)obj).var5);
//		Assert.assertEquals("TypeGuardClass4.var6", (short)256,((InterfaceTestClass4)obj).var6);
//		Assert.assertEquals("TypeGuardClass4.var7", 'Z', ((InterfaceTestClass4)obj).var7);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// tests a class implementing a single interface with a single method
	public static void testMethods1(){
		Interface4 itc1 = new InterfaceTestClass5();
		Assert.assertEquals("test1", 100 ,itc1.method41());
		
		CmdTransmitter.sendDone();		
	}

	@Test
	// tests a class implementing a single interface with several methods
	public static void testMethods2(){
		Interface1 itc1 = new InterfaceTestClass1();
		Assert.assertEquals("test1", 5 ,itc1.method11());
		Assert.assertEquals("test2", 255 ,itc1.method12(-1));
		
		CmdTransmitter.sendDone();		
	}

	@Test
	// tests a class implementing several interfaces
	public static void testMethods3(){
		Interface1 itc1 = new InterfaceTestClass2();
		Assert.assertEquals("test1", -1, itc1.method11());
		Assert.assertEquals("test2", 19, itc1.method12(-1));
		Interface3 itc2 = new InterfaceTestClass2();
		Assert.assertEquals("test3", 30, itc2.method32());
		Assert.assertEquals("test4", 12, ((InterfaceTestClass2)itc2).method2(2));
		CmdTransmitter.sendDone();		
	}
	
	@Test
	//Test overriding methods
	public static void testMethods4(){
//		InterfaceTestClass1 itc1 = new InterfaceTestClass1();
//		InterfaceTestClass2 itc2 = new InterfaceTestClass2();
//		InterfaceTestClass3 itc3 = new InterfaceTestClass3();
//		Object itc4 = new InterfaceTestClass4();
//
//		Assert.assertEquals("itc1m11", 5 ,itc1.method11());
//		Assert.assertEquals("itc1m12", 259 ,itc1.method12(3));
//		
//		Assert.assertEquals("itc2m11", -1 ,itc2.method11());
//		Assert.assertEquals("itc2m12", 261 ,itc2.method12(5));
//		Assert.assertFalse("itc2m22", itc2.method22(false));
//		Assert.assertEquals("((InterfaceTestClass1)itc2)m11", -1 ,((InterfaceTestClass1)itc2).method11());
//				
//		
//		Assert.assertEquals("itc3m11", 5 ,itc3.method11());
//		Assert.assertEquals("itc3m12", 251 ,itc3.method12(5));
//		Assert.assertEquals("itc3m12", 1023 ,itc3.method32());
//		Assert.assertEquals("((InterfaceTestClass1)itc3)m12", 251 ,((InterfaceTestClass1)itc3).method12(5));
//		
//		Assert.assertEquals("itc4m11", 2921 ,itc4.method11());
//		Assert.assertEquals("itc4m12", 259 ,itc4.method12(3));
//		Assert.assertEquals("((InterfaceTestClass1)itc4)m11", 2921 ,((InterfaceTestClass1)itc4).method11());
//		
		CmdTransmitter.sendDone();		
	}

		
}
