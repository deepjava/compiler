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

package ch.ntb.inf.deep.comp.targettest.objects.helper;

/**
 * NTB 12.04.2011
 * 
 * @author Urs Graf
 * 
 */

public class ClassB extends ClassA {

	public static boolean cVar0;

	public int iVar0 = 143;
	public byte iVar1 = 0x05;

	public ClassB(boolean var) {
		cVar0 = var;
	}

	public int methodB() {
		return iVar0 * iVar1;
	}

	public int methodC() {
		if (cVar0)
			return iVar0;
		return iVar1;
	}
}



