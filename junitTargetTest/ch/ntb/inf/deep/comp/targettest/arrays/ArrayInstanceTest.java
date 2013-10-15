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

package ch.ntb.inf.deep.comp.targettest.arrays;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 14.11.2011
 * 
 * @author Urs Graf
 * 
 */
@MaxErrors(100)
public class ArrayInstanceTest {
	
	@Test
	// test one dimensional arrays
	public static void testInstance1() {
		Object a1 = new A[3];
		Assert.assertTrue("checkType1", a1 instanceof A[]);
		Assert.assertFalse("checkType2", a1 instanceof AA[]);
		Assert.assertFalse("checkType3", a1 instanceof IA[]);
		Assert.assertFalse("checkType4", a1 instanceof B[]);
		Assert.assertFalse("checkType5", a1 instanceof A[][]);
		Assert.assertFalse("checkType6", a1 instanceof short[]);
		Assert.assertFalse("checkType7", a1 instanceof short[][]);
		Assert.assertTrue("checkType8", a1 instanceof Object);
		Assert.assertTrue("checkType9", a1 instanceof Object[]);
		Assert.assertFalse("checkType10", a1 instanceof Object[][]);
		
		a1 = new AA[3];
		Assert.assertTrue("checkType11", a1 instanceof A[]);
		Assert.assertTrue("checkType12", a1 instanceof AA[]);
		Assert.assertTrue("checkType13", a1 instanceof IA[]);
		Assert.assertFalse("checkType14", a1 instanceof B[]);
		Assert.assertFalse("checkType15", a1 instanceof A[][]);
		Assert.assertFalse("checkType16", a1 instanceof short[]);
		Assert.assertFalse("checkType17", a1 instanceof short[][]);
		Assert.assertTrue("checkType18", a1 instanceof Object);
		Assert.assertTrue("checkType19", a1 instanceof Object[]);
		Assert.assertFalse("checkType20", a1 instanceof Object[][]);
		
		a1 = new byte[4];
		Assert.assertFalse("checkType31", a1 instanceof A[]);
		Assert.assertFalse("checkType32", a1 instanceof IA[]);
		Assert.assertFalse("checkType33", a1 instanceof A[][]);
		Assert.assertFalse("checkType34", a1 instanceof short[]);
		Assert.assertFalse("checkType35", a1 instanceof short[][]);
		Assert.assertTrue("checkType36", a1 instanceof byte[]);
		Assert.assertFalse("checkType37", a1 instanceof byte[][]);
		Assert.assertTrue("checkType38", a1 instanceof Object);
		Assert.assertFalse("checkType39", a1 instanceof Object[]);
		Assert.assertFalse("checkType40", a1 instanceof Object[][]);

		CmdTransmitter.sendDone();
	}
	
	@Test
	// test multiarray instruction
	public static void testInstance2() {
		Object a1 = new A[3][2];

		Assert.assertTrue("checkType1", a1 instanceof A[][]);
		Assert.assertFalse("checkType2", a1 instanceof AA[][]);
		Assert.assertFalse("checkType3", a1 instanceof IA[][]);
		Assert.assertFalse("checkType4", a1 instanceof B[][]);
		Assert.assertFalse("checkType5", a1 instanceof A[]);
		Assert.assertFalse("checkType6", a1 instanceof short[]);
		Assert.assertFalse("checkType7", a1 instanceof short[][]);
		Assert.assertTrue("checkType8", a1 instanceof Object);
		Assert.assertTrue("checkType9", a1 instanceof Object[]);
		Assert.assertTrue("checkType10", a1 instanceof Object[][]);
		Assert.assertFalse("checkType11", a1 instanceof Object[][][]);
		
		a1 = new AA[3][2];
		Assert.assertTrue("checkType21", a1 instanceof A[][]);
		Assert.assertTrue("checkType22", a1 instanceof AA[][]);
		Assert.assertTrue("checkType23", a1 instanceof IA[][]);
		Assert.assertFalse("checkType24", a1 instanceof B[][]);
		Assert.assertFalse("checkType25", a1 instanceof AA[]);
		Assert.assertFalse("checkType26", a1 instanceof short[]);
		Assert.assertFalse("checkType27", a1 instanceof short[][]);
		Assert.assertTrue("checkType28", a1 instanceof Object);
		Assert.assertTrue("checkType29", a1 instanceof Object[]);
		Assert.assertTrue("checkType30", a1 instanceof Object[][]);
		Assert.assertFalse("checkType31", a1 instanceof Object[][][]);
		
		a1 = new IA[3][2];
		Assert.assertFalse("checkType41", a1 instanceof A[][]);
		Assert.assertFalse("checkType42", a1 instanceof AA[][]);
		Assert.assertTrue("checkType43", a1 instanceof IA[][]);
		Assert.assertFalse("checkType44", a1 instanceof IB[][]);
		Assert.assertFalse("checkType45", a1 instanceof IA[]);

		
		a1 = new byte[4][3];
		Assert.assertFalse("checkType61", a1 instanceof A[][]);
		Assert.assertFalse("checkType62", a1 instanceof IA[][]);
		Assert.assertFalse("checkType63", a1 instanceof A[]);
		Assert.assertFalse("checkType64", a1 instanceof short[]);
		Assert.assertFalse("checkType65", a1 instanceof short[][]);
		Assert.assertTrue("checkType66", a1 instanceof byte[][]);
		Assert.assertFalse("checkType67", a1 instanceof byte[]);
		Assert.assertFalse("checkType68", a1 instanceof byte[][][]);
		Assert.assertTrue("checkType69", a1 instanceof Object);
		Assert.assertTrue("checkType70", a1 instanceof Object[]);
		Assert.assertFalse("checkType71", a1 instanceof Object[][]);

		CmdTransmitter.sendDone();
	}
	
	@Test
	// test multiarray instruction
	public static void testInstance3() {
		Object a1 = new A[3][2][2];

		Assert.assertTrue("checkType1", a1 instanceof A[][][]);
		Assert.assertFalse("checkType2", a1 instanceof AA[][][]);
		Assert.assertFalse("checkType3", a1 instanceof IA[][][]);
		Assert.assertFalse("checkType4", a1 instanceof B[][][]);
		Assert.assertFalse("checkType5", a1 instanceof A[][]);
		Assert.assertFalse("checkType6", a1 instanceof short[][]);
		Assert.assertFalse("checkType7", a1 instanceof short[][][]);
		Assert.assertTrue("checkType8", a1 instanceof Object);
		Assert.assertTrue("checkType9", a1 instanceof Object[]);
		Assert.assertTrue("checkType10", a1 instanceof Object[][]);
		Assert.assertTrue("checkType11", a1 instanceof Object[][][]);
		Assert.assertFalse("checkType12", a1 instanceof Object[][][][]);
		
		a1 = new AA[3][2][2];
		Assert.assertTrue("checkType21", a1 instanceof A[][][]);
		Assert.assertTrue("checkType22", a1 instanceof AA[][][]);
		Assert.assertTrue("checkType23", a1 instanceof IA[][][]);
		Assert.assertFalse("checkType24", a1 instanceof B[][][]);
		Assert.assertFalse("checkType25", a1 instanceof AA[][]);
		Assert.assertFalse("checkType26", a1 instanceof short[][]);
		Assert.assertFalse("checkType27", a1 instanceof short[][][]);
		Assert.assertTrue("checkType28", a1 instanceof Object);
		Assert.assertTrue("checkType29", a1 instanceof Object[]);
		Assert.assertTrue("checkType30", a1 instanceof Object[][]);
		Assert.assertTrue("checkType31", a1 instanceof Object[][][]);
		Assert.assertFalse("checkType32", a1 instanceof Object[][][][]);
		Assert.assertFalse("checkType33", a1 instanceof IA[][]);
		Assert.assertFalse("checkType34", a1 instanceof IA[][][][]);
		Assert.assertFalse("checkType35", a1 instanceof IA);
		
		a1 = new IA[3][2][2];
		Assert.assertFalse("checkType41", a1 instanceof A[][][]);
		Assert.assertTrue("checkType42", a1 instanceof IA[][][]);
		Assert.assertFalse("checkType43", a1 instanceof A[][]);
		Assert.assertFalse("checkType44", a1 instanceof IB[][][]);
		Assert.assertFalse("checkType45", a1 instanceof B[][][]);
		Assert.assertFalse("checkType46", a1 instanceof AA[][][]);

		
		a1 = new byte[4][3][2];
		Assert.assertFalse("checkType61", a1 instanceof A[][][]);
		Assert.assertFalse("checkType62", a1 instanceof IA[][][]);
		Assert.assertFalse("checkType63", a1 instanceof A[][]);
		Assert.assertFalse("checkType64", a1 instanceof short[][]);
		Assert.assertFalse("checkType65", a1 instanceof short[][][]);
		Assert.assertTrue("checkType66", a1 instanceof byte[][][]);
		Assert.assertFalse("checkType67", a1 instanceof byte[][]);
		Assert.assertFalse("checkType68", a1 instanceof byte[][][][]);
		Assert.assertTrue("checkType69", a1 instanceof Object);
		Assert.assertTrue("checkType70", a1 instanceof Object[]);
		Assert.assertTrue("checkType71", a1 instanceof Object[][]);
		Assert.assertFalse("checkType72", a1 instanceof Object[][][]);

		CmdTransmitter.sendDone();
	}

	@Test
	// Test type guard
	public static void testTypeGuard(){
		Object obj = null;
		Object a1 = (A[])obj;
		a1 = (AA[][])obj;
		
		obj = new A[2];
		a1 = (A[])obj;
		a1 = (Object[])obj;
		// must fail
//		a1 = (AA[][])obj;
		// must fail
//		a1 = (A[][])obj;
		// must fail
//		a1 = (int[])obj;
		// must fail
//		a1 = (Object[][])obj;
		
		CmdTransmitter.sendDone();
	}

	@Test
	// a complex example
	public static void testInstance4() {
		Object[][][] a1 = new Object[2][3][4];
		A[] x = new AA[5];
		a1[0][0] = x;
		a1[0][1][2] = x;
		short[][] y = new short[2][3];
		a1[1][0] = y;
		a1[1][1][0] = y;
		Assert.assertTrue("checkType1", a1[1][0] instanceof short[][]);
		Assert.assertFalse("checkType2", a1[1][1] instanceof short[][]);
		Assert.assertTrue("checkType3", a1[1][1][0] instanceof short[][]);
		Assert.assertFalse("checkType4", a1[0][1][0] instanceof short[][]);
		Assert.assertTrue("checkType5", a1[0] instanceof Object);
		Assert.assertTrue("checkType6", a1[0] instanceof Object[][]);
		Assert.assertTrue("checkType7", a1[0][0] instanceof A[]);
		Assert.assertTrue("checkType8", a1[0][1][2] instanceof A[]);
		Assert.assertTrue("checkType9", a1[0][0] instanceof AA[]);
		Assert.assertTrue("checkType10", a1[0][1][2] instanceof AA[]);
		Assert.assertTrue("checkType11", a1[0][0] instanceof Object[]);
		Assert.assertTrue("checkType12", a1[0][1][2] instanceof Object[]);
		Assert.assertFalse("checkType13", a1[0][0] instanceof Object[][]);
		Assert.assertFalse("checkType14", a1[0][1][2] instanceof Object[][]);
		
		Object z = (Object[][][])a1;
		z = (Object[][])a1;
		z = (Object[])a1;
		z = (Object)a1;
		z = (Object[][])a1[0];
		z = (Object[][])a1[1];
		// must fail
//		z = (AA[][])a1[1];
		
		z = (Object[])a1[0][0];
		z = (AA[])a1[0][0];
		z = (A[])a1[0][0];
		Assert.assertTrue("checkType20", a1[0][0] == a1[0][1][2]);
		Assert.assertFalse("checkType21", a1[0][0] == a1[0][1][1]);
		
		z = (Object[])a1[1][0];
		z = (short[][])a1[1][0];
		z = (short[][])a1[1][1][0];
		// must fail
//		z = (short[][][])a1[1][1][0];
		Assert.assertTrue("checkType22", a1[1][0] == a1[1][1][0]);
		Assert.assertFalse("checkType23", a1[0][0] == a1[0][0][2]);
			
		CmdTransmitter.sendDone();
	}

}

class A {}
class B implements IB{}
class AA extends A implements IA{}
interface IA {}
interface IB {}
