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
 * @author Roger Millischer
 * 
 */

@MaxErrors(100)
public class InheritanceTest {	
	
	@Test
	//Test class Variables
	public static void testClassVar(){
		ClassA clzA = new ClassA(); 
		ClassB clzB = new ClassB(true);
		
		Assert.assertEquals("static0", 12 ,ClassA.cVar0);
		Assert.assertEquals("overInstance0", 12 ,clzA.cVar0);
		
		Assert.assertEquals("static1", 3.1415926535897932384626433832795f ,ClassA.cVar1,(float)0.01);
		Assert.assertEquals("overInstance1", 3.1415926535897932384626433832795f ,clzA.cVar1, (float)0.01);
		
		Assert.assertTrue("static2",ClassB.cVar0);
		Assert.assertTrue("overInstance2",clzB.cVar0);
		
		ClassC clzC = new ClassC(false);//calls the implicit super constructor of ClassB
		
		Assert.assertFalse("static3",ClassB.cVar0);
		Assert.assertFalse("overInstance3",clzB.cVar0);
		
		Assert.assertEquals("static4", 15, ClassC.cVar0);
		Assert.assertEquals("overInstance4", 15, clzC.cVar0);
		
		Assert.assertFalse("static5",ClassB.cVar0);
			
		CmdTransmitter.sendDone();
	}

	@Test
	//Test instance variables
	public static void testInstanceVar(){
		ClassA clzA = new ClassA(); 
		ClassB clzB = new ClassB(true);
		ClassC clzC = new ClassC(false);//calls the implicit super constructor of ClassB
		
		Assert.assertEquals("Instance0", 1 ,clzA.iVar0);
		Assert.assertEquals("Instance1", 143 ,clzB.iVar0);
		Assert.assertEquals("Instance2", 5 ,clzB.iVar1);
		Assert.assertEquals("Instance3", 143 ,clzC.iVar0);
		Assert.assertEquals("Instance4", 2*3.1415926535897932384626433832795 ,clzC.iVar1, 0.01);
		
		CmdTransmitter.sendDone();		
	}
	
	@Test
	//Test overriding methods
	public static void testMethods(){
		ClassA clzA = new ClassA(); 
		ClassB clzB = new ClassB(true);
		
		int res;
		
		res = clzA.methodA();
		Assert.assertEquals("methodAA", 13 ,res);
		
		res = clzA.methodB();
		Assert.assertEquals("methodBA", 0 ,res);
		
		res = clzB.methodA();
		Assert.assertEquals("methodAB", 13 ,res);
		
		res = clzB.methodB();
		Assert.assertEquals("methodBB", 715 ,res);
		
		res = clzB.methodC();
		Assert.assertEquals("methodCB", 143 ,res);
		
		ClassC clzC = new ClassC(false);//calls the implicit super constructor of ClassB
		
		res = clzC.methodA();
		Assert.assertEquals("methodAC", 13 ,res);
		
		res = clzC.methodB();
		Assert.assertEquals("methodBC", 15 ,res);
		
		res = clzC.methodC();
		Assert.assertEquals("methodCC", 5 ,res);
		
		res = clzC.methodD((short)8);
		Assert.assertEquals("methodDC", 1144 ,res);
		
		CmdTransmitter.sendDone();		
	}
	
}


