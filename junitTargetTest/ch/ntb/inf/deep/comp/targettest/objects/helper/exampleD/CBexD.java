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

package ch.ntb.inf.deep.comp.targettest.objects.helper.exampleD;


public class CBexD implements IAexD, ICexD {

	@Override
	public int ima11() {
		return IAexD.var5 + IBexD.var5;
	}

	public int cmb11(int val) {
		return val + 10;
	}
	
	@Override
	public int ima12(int i) {
		return i + 20;
	}

	@Override
	public short imc11() {
		return 30;
	}
	
	
	public static int getI2Var0(){
		return IBexD.var0;
	}
	public static double getI2Var1(){
		return IBexD.var1;
	}
	public static float getI2Var2(){
		return IBexD.var2;
	}
	public static boolean getI2Var3(){
		return IBexD.var3;
	}
	public static long getI2Var4(){
		return IBexD.var4;
	}
	public static byte getI2Var5(){
		return IBexD.var5;
	}
	public static short getI2Var6(){
		return IBexD.var6;
	}
	public static char getI2Var7(){
		return IBexD.var7;
	}

	public static int getVar0(){
		return IAexD.var0;
	}
	public static double getVar1(){
		return IAexD.var1;
	}
	public static float getVar2(){
		return IAexD.var2;
	}
	public static boolean getVar3(){
		return IAexD.var3;
	}
	public static long getVar4(){
		return IAexD.var4;
	}
	public static byte getVar5(){
		return IAexD.var5;
	}
	public static short getVar6(){
		return IAexD.var6;
	}
	public static char getVar7(){
		return IAexD.var7;
	}


}
