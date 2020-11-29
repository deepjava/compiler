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

@SuppressWarnings("unused")
public class T09Types {
	static void m1() {
		boolean res;
		long a = -30000;
		if (a > 10000) {
			int b = 100;
			float bb = b;
		} else {
			float c = 1.0f;
		}
		float d = a * 2;
		res = a > 0? true : false;
		double e = a * 16;
		int f = (int) d;
	}

	float m2(long a, float b, double c, byte[] d, short e, int f, int g) {
		a = 0x7545 & a;
		e += 100;
		T08Calls.classMethod(d[2]);
		e = (short)(20 + e);
		g |= 0x223344;
		c = 3.2;
		int h = g - e;
		T08Calls.classMethod(h);
		short i = (short)h;		
		return b;
	}
	
	static void callm2() {
		T09Types obj = new T09Types();
		long a = 0;
		double c = 0;
		float b = 0;
		byte[] d = null;
		short e = 0;
		int g = 0;
		int f = 0;
		float res = obj.m2(a, b, c, d, e, f, g);
	}
	
	public static void m3(double a, int b, char[] c) {
		// Achtung: b erh?lt phi_Funktion, muss in if und in else geladen werden (loadLocal)
		// d wird nur in if gesetzt, in else nicht, erh?lt darum auch keine phi-Funktion,
		// d wird zwar ausserhalb von if deklariert aber nie gesetzt, der Java-Compiler w?rde eine 
		// Fehlermeldung liefern, wenn d sp?ter benutzt w?rde.
		int d;
		if (c == null) d = b + 1;
	}


}
