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

package org.deepjava.comp.targettest.objects.helper;

/**
 * NTB 12.04.2011
 * 
 * @author Urs Graf
 * 
 */

public class ClassC extends ClassB {
	
	public static int cVar0 = 15;
	public static boolean cVar2 = false;

	public double iVar1 = 2 * 3.1415926535897932384626433832795; // 40 19 21 fb 54 44 2d 18


	public ClassC(boolean var) {
		super(var);
	}

	public int methodB() {
		return cVar0;
	}
	
	public int methodD(short x) {
		return iVar0 * x;
	}
}

