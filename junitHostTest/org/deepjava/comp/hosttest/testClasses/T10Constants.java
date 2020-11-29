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

public class T10Constants {
	public static final boolean z = true; 
	
	public static final byte b = 64; // 40
	
	public static final short s = 256;
	public static final char c = 'Z';
	
	public static final int i = 1024;
	public static final long l = 2048;
	
	public static final float f = 3.1415926535897932384626433832795f; // 40 49 0f db
	public static final double d = 2*3.1415926535897932384626433832795; // 40 19 21 fb 54 44 2d 18
	
	public static final String s1 = "Hello Tester!"; // 48 65 6c 6c 6f 20 54 65 73 74 65 72 21
	public static final String s2 = "How are you?"; // 48 6f 77 20 61 72 65 20 79 6f 75 3f
	public static final String s3 = "??????????"; // e4 f6 fc c4 d6 dc e0 e9 e8 ea
	
	public static final Object o = new Object();
}
