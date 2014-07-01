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

package ch.ntb.inf.deep.comp.targettest.various;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 25.5.2011, Urs Graf
 * tests various forms of phi functions
 *  
 *         Changes:
 */
@MaxErrors(100)
public class VariousTest3 {

	// checks if phi functions with same index are splitted correctly 
	static int phiFunction1(int a) {
		if (a > 0) {
			float f2 = 3.0f;
			while (f2 < 10.0) f2 += 5.0;
			a = (int)f2;
		} else {
			int b = 4;
			while (b < 10) b += 5;
			a = b;
		}
		return (a);
	}

	@Test
	public static void phiFunction1Test() {
		Assert.assertEquals("Test1", 13, phiFunction1(2));
		Assert.assertEquals("Test2", 14, phiFunction1(-2));

		CmdTransmitter.sendDone();
	}	

	private static int forIfFor() {
		int  offset, k; 
		offset = 0; 
		int val = 10;
		if (val == 10) {
			offset += 4;	
			for (int i = 0; i < 5; i++) {
				offset += 2;
			}
			for (int i = 0; i < 3; i++) {
				boolean valid = true;
				if(valid == false) offset += 138;
				else {
					for (k = 0; k < 32; k++) {
						offset += 2;
					}
					for (k = 0; k < 7; k++) {
						offset += 4;
					}	
				} 
			}
		} 
		return offset;
	}

	@Test
	public static void forIfForTest() {
		Assert.assertEquals("Test1", 290, forIfFor());

		CmdTransmitter.sendDone();
	}	

	// tests if the inc operator in a loop works correctly
	// there was an error with the resolving phi-function up to version 1.1
	static int[] a = new int[4];
	
	@Test
	public static void arrayInc() {
		int i = 0;
		while (i < 3) a[i++] = 10;
		Assert.assertEquals("test1", a[0], 10);
		Assert.assertEquals("test2", a[1], 10);
		Assert.assertEquals("test3", a[2], 10);
		Assert.assertEquals("test4", a[3], 0);
		
		for (int k = 0; k < 4; k++) a[k] = 0;
		
		int n = 0;
		while (n < 3) {
			a[n] = 10;
			n++;
		}
		Assert.assertEquals("test11", a[0], 10);
		Assert.assertEquals("test12", a[1], 10);
		Assert.assertEquals("test13", a[2], 10);
		Assert.assertEquals("test14", a[3], 0);

		for (int k = 0; k < 4; k++) a[k] = 0;
				
		int x = 0;
		do a[++x] = x; while (x < 3);
		Assert.assertEquals("test21", a[0], 0);
		Assert.assertEquals("test22", a[1], 1);
		Assert.assertEquals("test23", a[2], 2);
		Assert.assertEquals("test24", a[3], 3);
		
		for (int k = 0; k < 4; k++) a[k] = 0;
				
		int m = 0;
		do a[m++] = 10; while (m < 3);
		Assert.assertEquals("test31", a[0], 10);
		Assert.assertEquals("test32", a[1], 10);
		Assert.assertEquals("test33", a[2], 10);
		Assert.assertEquals("test34", a[3], 0);

		for (int k = 0; k < 4; k++) a[k] = 0;
		
		int o = 0;
		do a[o++] = o + 10; while (o < 3);
		Assert.assertEquals("test41", a[0], 11);
		Assert.assertEquals("test42", a[1], 12);
		Assert.assertEquals("test43", a[2], 13);
		Assert.assertEquals("test44", a[3], 0);

		for (int k = 0; k < 4; k++) a[k] = 0;
		
		int p = 0;
		do a[p] = p++ + 10; while (p < 3);
		Assert.assertEquals("test51", a[0], 10);
		Assert.assertEquals("test52", a[1], 11);
		Assert.assertEquals("test53", a[2], 12);
		Assert.assertEquals("test54", a[3], 0);

		for (int k = 0; k < 4; k++) a[k] = 10;
		
		int q = 1;
		do a[q-1] = a[q++] + q; while (q < 3);
		Assert.assertEquals("test61", a[0], 12);
		Assert.assertEquals("test62", a[1], 13);
		Assert.assertEquals("test63", a[2], 10);
		Assert.assertEquals("test64", a[3], 10);
		
		for (int k = 0; k < 4; k++) a[k] = 0;
		
		int y = 0;
		int sum = 0;
		do sum += y++ + 10; while (y < 3);
		Assert.assertEquals("test71", sum, 33);
		
		CmdTransmitter.sendDone();
	}	

}
