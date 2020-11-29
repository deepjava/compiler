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


public class T05Returns {
	public static int multipleReturns1(boolean a) {
		int x = 0;
		if(a) {
			x = 1;
			return x;
		}
		x = 22;
		return x;
	}

	public static int multipleReturns2() {
		int x = 22;
		if(x == 1) {
			return 1;
		}
		else if(x == 3) {
			return 3;
		}
		else if(x == 5) {
			return 5;
		}
		else {
			if(x == 2) {
				return 2;
			}
			else if(x == 4) {
				return 4;
			}
			else {
				return 100;
			}
		}
	}
}
