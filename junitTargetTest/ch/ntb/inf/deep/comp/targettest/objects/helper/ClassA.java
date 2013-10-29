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

public class ClassA {
	public static int cVar0 = 12;
	public static float cVar1 = 3.1415926535897932384626433832795f; // 40 49 0f db
		
	public short iVar0 = 1;
	
	public int methodA() {
		return cVar0 + iVar0;
	}
	
	public int methodB() {
		return 0;
	}
	
	public void add(int y) {
		iVar0 += y;
	}
}

