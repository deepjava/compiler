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

public class T02Branches {
    public static int if1(int a) {
        int i;
        if(a > 0) {
            i = a + 1;
        }
        else {
            i = a - 1;
        }
        return i;
    }

    public static void if2() {	// constants
        int a = 1;
        int b = 2;
        if(a >= b){
            a = 6;
        }
        else{
            a = 8;
        }
        b=a;
        a++;
        b++;
    }

    public static void if3() {	// nested if
        int x = 0, y = 1;
        if(x == 0) {
            if(y == 1) {
                x++;
                if(x == 1) {
                    y++;
                    if(y == 2) {
                        int a, b, c;                        
                        a = 1;
                        b = 2;
                        c = a - b;
                        c++;
                    }

                }
            }
        }
    }

	public void if4() {	// if boolean
		boolean bool = true;
		int a = 1;
		int b = 0;

		if(bool) {
			b = 8;
		}
		else {
			b = 2;
		}
		a = b;
		a++;
	}
	
	public int if5(int n, int m){ //example from m?ssenb?ck for loadParameter
		if(n < 0){
			n = 0; m = 0;
		}
		return n + m;
	}
	
	public int if6(double val){ // from doubleToChars
		int exp = 1000;
		int high = 12;
		if (exp < 1023){
			val = -val;
		}
		if (exp == 0){
			high = 13;
		} else {
			if (val > 100)
				val *= 3;
			else 
				val /=5;
			if (val > 200)
				high = 14;
		}
		if (val > 10) exp++;
		else val--;
		return exp;
	}

	public int if7(int val){
		int exp = 1000;
		if (exp < 1023){
			val = -val;
		}
		exp = val + 40;
		while (val > 20) {
			if (exp == 0){
				val += 13;
			} else {
				val *= 3;
			}
			exp = val + 30;
		}
		return val;
	}
}