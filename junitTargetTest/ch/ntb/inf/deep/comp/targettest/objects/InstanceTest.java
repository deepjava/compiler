package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.deep.comp.targettest.objects.helper.ClassA;
import ch.ntb.inf.deep.comp.targettest.objects.helper.ClassB;
import ch.ntb.inf.deep.comp.targettest.objects.helper.ClassC;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleA.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleB.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleC.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleD.*;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 12.03.2013
 * 
 * @author Urs Graf
 * 
 * Tests for instanceof and type guards for various classes (without interface classes)
 */

@MaxErrors(100)
public class InstanceTest {	
	
	@Test
	public static void testInstance1() {
		ClassA clzA = new ClassA(); 
		ClassB clzB = new ClassB(true);
		ClassC clzC = new ClassC(false);
		Object obj = null;
				
		Assert.assertTrue("instance1", clzA instanceof ClassA);		
		Assert.assertFalse("instance2", clzA instanceof ClassC);	
		Assert.assertTrue("instance3", clzC instanceof ClassA);	
		Assert.assertTrue("instance4", clzC instanceof ClassC);
		
		Assert.assertTrue("instance5", clzB instanceof Object);
		Assert.assertTrue("instance6", clzB instanceof ClassA);
		Assert.assertTrue("instance7", clzB instanceof ClassB);
		Assert.assertFalse("instance8", clzB instanceof ClassC);
		
		Assert.assertFalse("instance15", obj instanceof Object);
		Assert.assertFalse("instance16", obj instanceof Object[]);
		Assert.assertFalse("instance17", obj instanceof short[]);
		
		obj = clzB;
		Assert.assertTrue("instance21", obj instanceof Object);
		Assert.assertTrue("instance22", obj instanceof ClassA);
		Assert.assertTrue("instance23", obj instanceof ClassB);
		Assert.assertFalse("instance24", obj instanceof ClassC);
		Assert.assertFalse("instance25", obj instanceof Object[]);
		
		obj = null;
		Assert.assertFalse("instance31", obj instanceof Object);
		Assert.assertFalse("instance32", obj instanceof ClassA);
		Assert.assertFalse("instance33", obj instanceof ClassB);
		Assert.assertFalse("instance34", obj instanceof ClassC);
		Assert.assertFalse("instance35", obj instanceof Object[]);
		Assert.assertFalse("instance36", obj instanceof short[]);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testInstance2() {
		Object clz1 = new CAexD(); 
		Object clz2 = new CBexD(); 
		Object clz3 = new CCexD(); 
		Object clz4 = new CDexD();
		Object clz5 = new CEexD();
				
		Assert.assertTrue("instance5", clz1 instanceof CAexD);		
		Assert.assertFalse("instance6", clz1 instanceof CBexD);		
		Assert.assertFalse("instance7", clz1 instanceof CCexD);		
		Assert.assertFalse("instance8", clz1 instanceof CDexD);		
		Assert.assertFalse("instance9", clz1 instanceof CEexD);		
		
		Assert.assertFalse("instance15", clz2 instanceof CAexD);		
		Assert.assertTrue("instance16", clz2 instanceof CBexD);		
		Assert.assertFalse("instance17", clz2 instanceof CCexD);		
		Assert.assertFalse("instance18", clz2 instanceof CDexD);		
		Assert.assertFalse("instance19", clz2 instanceof CEexD);		
		
		Assert.assertTrue("instance25", clz3 instanceof CAexD);		
		Assert.assertFalse("instance26", clz3 instanceof CBexD);		
		Assert.assertTrue("instance27", clz3 instanceof CCexD);		
		Assert.assertFalse("instance28", clz3 instanceof CDexD);		
		Assert.assertFalse("instance29", clz3 instanceof CEexD);	
		
		Assert.assertTrue("instance35", clz4 instanceof CAexD);		
		Assert.assertFalse("instance36", clz4 instanceof CBexD);		
		Assert.assertFalse("instance37", clz4 instanceof CCexD);		
		Assert.assertTrue("instance38", clz4 instanceof CDexD);		
		Assert.assertFalse("instance39", clz4 instanceof CEexD);	
		
		Assert.assertFalse("instance45", clz5 instanceof CAexD);		
		Assert.assertFalse("instance46", clz5 instanceof CBexD);		
		Assert.assertFalse("instance47", clz5 instanceof CCexD);		
		Assert.assertFalse("instance48", clz5 instanceof CDexD);		
		Assert.assertTrue("instance49", clz5 instanceof CEexD);	
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance3() {
		Object cls = new CAexD[2]; 
				
		Assert.assertTrue("test1", cls instanceof Object);
		Assert.assertTrue("test2", cls instanceof Object[]);		
		Assert.assertFalse("test3", cls instanceof CAexD);		
		Assert.assertTrue("test4", cls instanceof CAexD[]);		
		Assert.assertFalse("test5", cls instanceof CAexD[][]);		

		cls = new CEexD[1]; 
		
		Assert.assertTrue("test10", cls instanceof Object);
		Assert.assertTrue("test11", cls instanceof Object[]);			
		Assert.assertTrue("test12", cls instanceof CEexD[]);		
		Assert.assertFalse("test13", cls instanceof CBexD[]);		
		Assert.assertFalse("test14", cls instanceof CEexD[][]);		

		cls = new CEexD[1][2]; 
		
		Assert.assertFalse("test20", cls instanceof CAexD[]);		
		Assert.assertTrue("test21", cls instanceof CEexD[][]);		
		Assert.assertFalse("test22", cls instanceof CEexD[][][]);		
		Assert.assertFalse("test23", cls instanceof CEexD[]);		
		Assert.assertFalse("test24", cls instanceof CEexD);			

		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance4() {
		Object o = new CEexA[2]; 
		
		Assert.assertFalse("test1", o instanceof CZexA[]);		
		Assert.assertTrue("test2", o instanceof CCexA[]);		
		Assert.assertTrue("test3", o instanceof Object[]);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance5() {
		Object o = new CDexC(); 
		
		Assert.assertTrue("test1", o instanceof CAexC);		
		Assert.assertFalse("test2", o instanceof CEexC);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test type guard
	public static void testTypeGuard(){
		Object obj = null;
		ClassB res = (ClassB)obj;
		res = (ClassC)obj;
		
		obj = new ClassC(true);	
		res = (ClassB)obj;
		res = (ClassC)obj;

		Object obj1 = (Object)obj;
		
		obj  = new ClassB(true);
		// must fail
//		res = (ClassC)obj;
		
		// must fail
//		obj1 = (short[])obj;
		
		// must fail
//		obj1 = (ClassA[])obj;
		
//		Object y1 = new InterfaceTestClass1(); 
//		Assert.assertEquals("test1.var0", 23, ((IAexD)y1).var0);
//		Assert.assertEquals("test1.m11", 5, ((IAexD) y1).method11());
//			Object obj = new InterfaceTestClass1();
//				
//			Assert.assertEquals("TypeGuardClass1.var0", 23,((InterfaceTestClass1)obj).var0);
//			Assert.assertEquals("TypeGuardClass1.var1", 2.125,((InterfaceTestClass1)obj).var1, 0.01);
//			Assert.assertEquals("TypeGuardClass1.var2", 1.15f, ((InterfaceTestClass1)obj).var2 , 0.01f);
//			Assert.assertTrue("TypeGuardClass1.var3", ((InterfaceTestClass1)obj).var3);
//			Assert.assertEquals("TypeGuardClass1.var4", 0x5555555555L, ((InterfaceTestClass1)obj).var4);
//			Assert.assertEquals("TypeGuardClass1.var5", (byte)127, ((InterfaceTestClass1)obj).var5);
//			Assert.assertEquals("TypeGuardClass1.var6", (short)256,((InterfaceTestClass1)obj).var6);
//			Assert.assertEquals("TypeGuardClass1.var7", 'Z', ((InterfaceTestClass1)obj).var7);
//					
//			obj = new InterfaceTestClass2();
//			
//			Assert.assertEquals("TypeGuardClass2.i1Var0", 23,((InterfaceTestClass2)obj).getVar0());
//			Assert.assertEquals("TypeGuardClass2.i1Var1", 2.125,((InterfaceTestClass2)obj).getVar1(), 0.01);
//			Assert.assertEquals("TypeGuardClass2.i1Var2", 1.15f, ((InterfaceTestClass2)obj).getVar2() , 0.01f);
//			Assert.assertTrue("TypeGuardClass2.i1Var3", ((InterfaceTestClass2)obj).getVar3());
//			Assert.assertEquals("TypeGuardClass2.i1Var4", 0x5555555555L, ((InterfaceTestClass2)obj).getVar4());
//			Assert.assertEquals("TypeGuardClass2.i1Var5", (byte)127, ((InterfaceTestClass2)obj).getVar5());
//			Assert.assertEquals("TypeGuardClass2.i1Var6", (short)256,((InterfaceTestClass2)obj).getVar6());
//			Assert.assertEquals("TypeGuardClass2.i1Var7", 'Z', ((InterfaceTestClass2)obj).getVar7());
//			Assert.assertEquals("TypeGuardClass2.i2Var0", 45,((InterfaceTestClass2)obj).getI2Var0());
//			Assert.assertEquals("TypeGuardClass2.i2Var1", 3.1459,((InterfaceTestClass2)obj).getI2Var1(), 0.01);
//			Assert.assertEquals("TypeGuardClass2.i2Var2", 3.33f, ((InterfaceTestClass2)obj).getI2Var2() , 0.01f);
//			Assert.assertFalse("TypeGuardClass2.i2Var3", ((InterfaceTestClass2)obj).getI2Var3());
//			Assert.assertEquals("TypeGuardClass2.i2Var4", 0xAAAAAAAAAAL, ((InterfaceTestClass2)obj).getI2Var4());
//			Assert.assertEquals("TypeGuardClass2.i2Var5", (byte)-128, ((InterfaceTestClass2)obj).getI2Var5());
//			Assert.assertEquals("TypeGuardClass2.i2Var6", (short)-264,((InterfaceTestClass2)obj).getI2Var6());
//			Assert.assertEquals("TypeGuardClass2.i2Var7", 'B', ((InterfaceTestClass2)obj).getI2Var7());
//			
//			obj = new InterfaceTestClass3();
//			
//			Assert.assertEquals("TypeGuardClass3.var0", 23,((InterfaceTestClass3)obj).var0);
//			Assert.assertEquals("TypeGuardClass3.var1", 2.125,((InterfaceTestClass3)obj).var1, 0.01);
//			Assert.assertEquals("TypeGuardClass3.var2", 1.15f, ((InterfaceTestClass3)obj).var2 , 0.01f);
//			Assert.assertTrue("TypeGuardClass3.var3", ((InterfaceTestClass3)obj).var3);
//			Assert.assertEquals("TypeGuardClass3.var4", 0x5555555555L, ((InterfaceTestClass3)obj).var4);
//			Assert.assertEquals("TypeGuardClass3.var5", (byte)127, ((InterfaceTestClass3)obj).var5);
//			Assert.assertEquals("TypeGuardClass3.var6", (short)256,((InterfaceTestClass3)obj).var6);
//			Assert.assertEquals("TypeGuardClass3.var7", 'Z', ((InterfaceTestClass3)obj).var7);
//			Assert.assertEquals("TypeGuardClass3.var8", 89,((InterfaceTestClass3)obj).var8);
//			Assert.assertEquals("TypeGuardClass3.var9", 7.775,((InterfaceTestClass3)obj).var9, 0.01);
//			Assert.assertEquals("TypeGuardClass3.var10", 9.32f, ((InterfaceTestClass3)obj).var10 , 0.01f);
//			Assert.assertFalse("TypeGuardClass3.var11", ((InterfaceTestClass3)obj).var11);
//			Assert.assertEquals("TypeGuardClass3.var12", 0xBBBBBBBBBBL, ((InterfaceTestClass3)obj).var12);
//			Assert.assertEquals("TypeGuardClass3.var13", (byte)64, ((InterfaceTestClass3)obj).var13);
//			Assert.assertEquals("TypeGuardClass3.var14", (short)1023,((InterfaceTestClass3)obj).var14);
//			Assert.assertEquals("TypeGuardClass3.var15", 'L', ((InterfaceTestClass3)obj).var15);
//			
//			obj = new InterfaceTestClass4();
//			
//			Assert.assertEquals("TypeGuardClass4.var0", 23,((InterfaceTestClass4)obj).var0);
//			Assert.assertEquals("TypeGuardClass4.var1", 2.125,((InterfaceTestClass4)obj).var1, 0.01);
//			Assert.assertEquals("TypeGuardClass4.var2", 1.15f, ((InterfaceTestClass4)obj).var2 , 0.01f);
//			Assert.assertTrue("TypeGuardClass4.var3", ((InterfaceTestClass4)obj).var3);
//			Assert.assertEquals("TypeGuardClass4.var4", 0x5555555555L, ((InterfaceTestClass4)obj).var4);
//			Assert.assertEquals("TypeGuardClass4.var5", (byte)127, ((InterfaceTestClass4)obj).var5);
//			Assert.assertEquals("TypeGuardClass4.var6", (short)256,((InterfaceTestClass4)obj).var6);
//			Assert.assertEquals("TypeGuardClass4.var7", 'Z', ((InterfaceTestClass4)obj).var7);

		CmdTransmitter.sendDone();
	}
	
}


