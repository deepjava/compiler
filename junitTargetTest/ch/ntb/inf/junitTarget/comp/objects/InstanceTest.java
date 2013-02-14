package ch.ntb.inf.junitTarget.comp.objects;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.junitTarget.comp.objects.helper.ClassA;
import ch.ntb.inf.junitTarget.comp.objects.helper.ClassB;
import ch.ntb.inf.junitTarget.comp.objects.helper.ClassC;

/**
 * NTB 12.04.2011
 * 
 * @author Urs Graf
 * 
 */

@MaxErrors(100)
public class InstanceTest {	
	
	@Test
	public static void testInstance1(){
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
	public static void testInstance2(){
//		Object clz1 = new InterfaceTestClass1(); 
//		Object clz2 = new InterfaceTestClass2(); 
//		Object clz3 = new InterfaceTestClass3(); 
//		Object clz4 = new InterfaceTestClass4(); // Martin Problem
				
//		Assert.assertTrue("instance1", clz1 instanceof Interface1);		
//		Assert.assertFalse("instance2", clz1 instanceof Interface2);		
//		Assert.assertFalse("instance3", clz1 instanceof Interface3);		
//		Assert.assertTrue("instance4", clz1 instanceof InterfaceTestClass1);		
//		Assert.assertFalse("instance5", clz1 instanceof InterfaceTestClass2);		
//		Assert.assertFalse("instance6", clz1 instanceof InterfaceTestClass3);		
//		Assert.assertFalse("instance7", clz1 instanceof InterfaceTestClass4);		
//		
//		Assert.assertTrue("instance11", clz2 instanceof Interface1);		
//		Assert.assertTrue("instance12", clz2 instanceof Interface2);		
//		Assert.assertFalse("instance13", clz2 instanceof Interface3);		
//		Assert.assertTrue("instance14", clz2 instanceof InterfaceTestClass1);		
//		Assert.assertTrue("instance15", clz2 instanceof InterfaceTestClass2);		
//		Assert.assertFalse("instance16", clz2 instanceof InterfaceTestClass3);		
//		Assert.assertFalse("instance17", clz2 instanceof InterfaceTestClass4);		
//		
//		Assert.assertTrue("instance21", clz3 instanceof Interface1);		
//		Assert.assertFalse("instance22", clz3 instanceof Interface2);		
//		Assert.assertTrue("instance23", clz3 instanceof Interface3);		
//		Assert.assertTrue("instance24", clz3 instanceof InterfaceTestClass1);		
//		Assert.assertFalse("instance25", clz3 instanceof InterfaceTestClass2);		
//		Assert.assertTrue("instance26", clz3 instanceof InterfaceTestClass3);		
//		Assert.assertFalse("instance27", clz3 instanceof InterfaceTestClass4);		
//
//		Assert.assertTrue("instance31", clz4 instanceof Interface1);		
//		Assert.assertFalse("instance32", clz4 instanceof Interface2);		
//		Assert.assertFalse("instance33", clz4 instanceof Interface3);		
//		Assert.assertTrue("instance34", clz4 instanceof InterfaceTestClass1);		
//		Assert.assertFalse("instance35", clz4 instanceof InterfaceTestClass2);		
//		Assert.assertFalse("instance36", clz4 instanceof InterfaceTestClass3);		
//		Assert.assertTrue("instance37", clz4 instanceof InterfaceTestClass4);		

		CmdTransmitter.sendDone();
	}

	@Test
	public static void testInstance3(){
		Object obj = new ClassA(); 			
		Assert.assertTrue("instance1", obj instanceof ClassA);		
		Assert.assertFalse("instance2", obj instanceof ClassB);	
		Assert.assertFalse("instance3", obj instanceof ClassC);	
		Assert.assertFalse("instance4", obj instanceof ClassA[]);	
		Assert.assertTrue("instance5", obj instanceof Object);
		Assert.assertFalse("instance6", obj instanceof Object[]);
		Assert.assertFalse("instance7", obj instanceof short[]);
		
		obj = new ClassB(true); 			
		Assert.assertTrue("instance11", obj instanceof ClassA);		
		Assert.assertTrue("instance12", obj instanceof ClassB);	
		Assert.assertFalse("instance13", obj instanceof ClassC);	
		Assert.assertFalse("instance14", obj instanceof ClassA[]);	
		Assert.assertTrue("instance15", obj instanceof Object);
		Assert.assertFalse("instance16", obj instanceof Object[]);
		Assert.assertFalse("instance17", obj instanceof short[]);
		
		obj = new ClassC(true); 			
		Assert.assertTrue("instance21", obj instanceof ClassA);		
		Assert.assertTrue("instance22", obj instanceof ClassB);	
		Assert.assertTrue("instance23", obj instanceof ClassC);	
		Assert.assertFalse("instance24", obj instanceof ClassA[]);	
		Assert.assertTrue("instance25", obj instanceof Object);
		Assert.assertFalse("instance26", obj instanceof Object[]);
		Assert.assertFalse("instance27", obj instanceof short[]);
		
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
		
		CmdTransmitter.sendDone();
	}
	
}


