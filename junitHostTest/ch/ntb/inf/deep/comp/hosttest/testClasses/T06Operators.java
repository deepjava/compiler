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

package ch.ntb.inf.deep.comp.hosttest.testClasses;


public class T06Operators {

	public static boolean conditionalOperator1() {
		// example, where operand stack is not empty at end of node
		int mark;
		boolean bRes;
		boolean a = false, b = true, c = false;
		mark = 101;
		bRes = a ? b : c;
		mark = 102;
		bRes = a ? (b ? c : a) : (c ? a : b);
		mark = 103;
		return bRes;
	}
	
	public static boolean conditionalOperator2() {
		int mark;
		boolean bRes = false;
		boolean a = false, b = true, c = false;
		int d = 1, e = 2, f = 3;
		int iRes;
		mark = 101;
		iRes = d < e ? d : f;
		mark = 102;
		iRes = d < e ? (a ? d : e) : (b != c ? e : f);
		mark = 103;
		return (mark > iRes) && bRes;
	}

	public static boolean conditionalOperator3() {
		boolean bRes = false; boolean a = false, b = true;
		bRes = a && bRes ? a != b : bRes;
		return bRes;
	}

	public static boolean conditionalOperator4() {
		int i = 10;
		boolean a = true;
		return i > 20 ? false : a;
	}

}
