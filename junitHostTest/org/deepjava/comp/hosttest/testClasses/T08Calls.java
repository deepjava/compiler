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

package org.deepjava.comp.hosttest.testClasses;

public class T08Calls {
	short a = 31000;
	static byte b = -128;
	static boolean c = true;
	
	public static int classMethCall(){
		int y = 0;
		int x = classMethod(3);
		b += 100;
		return y+x;		
	}
	
	public static int objectMethCall(){
		T08Calls call = new T08Calls();
		return call.objectMethod(9);
	}
	
	public static void callToAnotherClass(){
		T01SimpleMethods.emptyMethodStatic();
	}
	
	
	/*
	 * Helper method
	 */
	public static int classMethod(int param){
		int x = 0;
		for(int i = 0; i < param; i++){
			x = x+3;
		}
		return x;		
	}
	/*
	 * Helper method
	 */
	public int objectMethod(int param){
		a += 200;
		b += 300;
		return param % 2;		
	}

}