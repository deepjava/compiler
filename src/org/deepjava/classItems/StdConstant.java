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

package org.deepjava.classItems;

import org.deepjava.host.Dbg;
import org.deepjava.strings.HString;

public class StdConstant extends Constant {
	// for constants with values <= 32 bit the value is placed into valueH 
	public int valueH, valueL;

	StdConstant(HString name, Type type, int valueHigh, int valueLow){
		super(name, type);
		this.valueH = valueHigh;
		this.valueL = valueLow;
		this.accAndPropFlags |= (1<<apfFinal);
	}

	public StdConstant(int valueHigh, int valueLow){
		this(stab.undefIdent, null, valueHigh, valueLow);
	}

	public StdConstant(HString name, int val) {
		this(name, Type.wellKnownTypes[txInt], val, 0);
	}
	
	public StdConstant(HString name, long val) {
		this(name, Type.wellKnownTypes[txLong], (int)(val >> 32 & 0xFFFFFFFF), (int)(val & 0xFFFFFFFF));
	}
	
	public StdConstant(HString name, float val) {
		this(name, Type.wellKnownTypes[txFloat], Float.floatToIntBits(val), 0);
	}
	
	public StdConstant(HString name, double val) {
		this(name, Type.wellKnownTypes[txDouble], (int)(Double.doubleToLongBits(val) >> 32 & 0xFFFFFFFF), (int)(Double.doubleToLongBits(val) & 0xFFFFFFFF));

	}

	//--- debug primitives
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("const %1$s %2$s ", type.name, name);
		char typeDesc = type.name.charAt(0);
		if (typeDesc == tdFloat){
			vrb.printf("= 0x%1$x = %2$7e", valueH, Float.intBitsToFloat(valueH));
		} else if (typeDesc == tdLong || typeDesc == cptDouble){
			int low = valueL;
			int high = valueH - (low>>31);;
			long lval = (long)low + ((long)high<<32);
			vrb.printf("= 0x%1$x", lval);
			if (typeDesc == cptDouble)  vrb.printf(" = %1$7e", Double.longBitsToDouble(lval));
		} else {// typeDesc == {tdInt, tdShort, tdByte, tdChar, tdBool}
			vrb.printf("=0x%1$x = %2$d", valueH, valueH);
		}
		vrb.print(", flags=");  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	private void printConstVal(Item type, int valueH, int valueL) {
		long value = ((long)(valueH)<<32) | (valueL&0xFFFFFFFFL);
		char category = type.name.charAt(0);
		switch(category){
		case tdBoolean:
			vrb.print(valueH != 0);
			break;
		case tdByte: case tdShort: case tdInt:
			vrb.print(Integer.toString(valueH));
			break;
		case tdChar:
			vrb.print((char)valueH );
			break;
		case tdFloat:
			vrb.print(Float.toString(Float.intBitsToFloat(valueH)));
			break;
		case tdLong:
			vrb.printf("%1$d = 0x%1$x", value, value );
			break;
		case tdDouble:
			vrb.print(Double.longBitsToDouble(value));
			break;
		default:
			vrb.print("<?>"); vrb.print(getClass().getName());
		}
	}

	public void printValue() {
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}

	public void printValue(int indentLevel) {
		indent(indentLevel);
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}
	
	public void print(int indentLevel){
		super.print(indentLevel);
		indent(1);
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}

	public void println(int indentLevel) {
		print(indentLevel);  vrb.println();
	}
	
	public String toString() {
		long value = ((long)(valueH)<<32) | (valueL&0xFFFFFFFFL);
		char category = type.name.charAt(0);
		String s;
		switch(category){
		case tdBoolean:
			s = Boolean.toString(valueH != 0);
			break;
		case tdByte: case tdShort: case tdInt:
			s = Integer.toString(valueH);
			break;
		case tdChar:
			s = Character.toString((char)valueH);
			break;
		case tdFloat:
			s = Float.toString(Float.intBitsToFloat(valueH));
			break;
		case tdLong:
			s = Long.toString(value);
			break;
		case tdDouble:
			s = Double.toString((Double.longBitsToDouble(value)));
			break;
		default:
			s = "<???>";
		}
		return s;
	}
}
