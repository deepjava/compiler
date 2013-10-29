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
	public static void testInstance6() {
			Object[] firstRow = new ClassB[5];
			Object[][] data = new ClassB[4][];
			int len = data.length;
			Assert.assertEquals("test1", 4, len);
			data[0] = firstRow;
			len = data[0].length;
			Assert.assertEquals("test2", 5, len);
			data[1] = new ClassB[3];
			len = data[1].length;
			Assert.assertEquals("test3", 3, len);

			CmdTransmitter.sendDone();
		}

	@Test
	// Test type guard
	public static void testTypeGuard1(){
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

	@Test
	// Test type guard
	public static void testTypeGuard2(){
		Object o1 = new ICexC[3];
		((ICexC[])o1)[2] = new CEexC();
		Assert.assertEquals("test1", 400, ((IAexC[])o1)[2].ima11());
		// must fail
		Assert.assertEquals("test2", 402, ((ICexC[])o1)[2].imc21());
		Assert.assertEquals("test3", 401, ((IBexC[])o1)[2].imb12());
		Assert.assertEquals("test4", 404, ((CEexC)((ICexC[])o1)[2]).ime31());
		
		CmdTransmitter.sendDone();
	}

}


