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

package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.deep.comp.targettest.objects.helper.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleA.*;
import ch.ntb.inf.deep.comp.targettest.objects.helper.exampleB.*;

/**
 * NTB 12.03.2013
 * 
 * @author Urs Graf
 * 
 * Tests for inheritance
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
	public static void testMethods1(){
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
	
	@Test
	public static void testMethods2(){
		CEexA o1 = new CEexA(); 
		Assert.assertEquals("test1", 25, o1.cma11());
		Assert.assertEquals("test2", 27, o1.imb12());
		Assert.assertEquals("test3", 37, o1.imc12());
		Assert.assertEquals("test4", 35, o1.cmd31());
		Assert.assertEquals("test5", 41, o1.imd21());
		Assert.assertEquals("test6", 40, o1.cme41());		
		CBexA o2 = new CBexA(); 
		Assert.assertEquals("test10", 300, o2.cmb21());
		Assert.assertEquals("test11", 301, o2.cma11());
		Assert.assertEquals("test12", 26, o2.imb11());
		Assert.assertEquals("test12", 302, o2.imb12());
		ICexA o3 = new CEexA(); 
		Assert.assertEquals("test20", 25, ((CAexA)o3).cma11());
		Assert.assertEquals("test21", 27, ((CAexA)o3).imb12());
		Assert.assertEquals("test22", 27, ((IBexA)o3).imb12());
		Assert.assertEquals("test23", 37, o3.imc12());
		Assert.assertEquals("test24", 35, ((CDexA)o3).cmd31());
		Assert.assertEquals("test25", 41, ((IDexA)o3).imd21());
		Assert.assertEquals("test26", 40, ((CEexA)o3).cme41());		

		IAexB o4 = new CXexB(); 
		Assert.assertEquals("test30", 111, ((CXexB)o4).cmx11());
		Assert.assertEquals("test31", 101, o4.ima11());
		o4 = new CYexB();
		Assert.assertEquals("test32", 23, ((IBexB)o4).imX1());
		o4 = new CZexB();
		Assert.assertEquals("test33", 103, ((ICexB)o4).imc31());
		o4 = new CSexB();
		Assert.assertEquals("test34", 41, ((CSexB)o4).cms21());
		Assert.assertEquals("test35", 42, ((IFexB)o4).imf21());
		Assert.assertEquals("test36", 43, ((IFexB)o4).imXY());		
		o4 = new CTexB(-1);
		Assert.assertEquals("test37", 59, ((CTexB)o4).cmt31());

		CmdTransmitter.sendDone();		
	}
}


