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

package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.CodeGen;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;

public class Method extends ClassMember {
	
	public static Method compSpecSubroutines;

	//--- instance fields
	public HString methDescriptor;

	public int id; // id given by configuration
	public byte[] code; // java byte code of this method
	public boolean fixed; // true, if offset is given by configuration
	
	public CodeGen machineCode; // machine code of this method
	public CFG cfg; // cfg of this method
	public SSA ssa; // ssa of this method
	
	public ExceptionTabEntry[] exceptionTab;
	public LocalVar[] localVars;
	public int[] lineNrTab; // entry: (startPc<<16) | lineNr

	public int nofParams;
	public int maxStackSlots, maxLocals;

	//--- constructors
	public Method(HString name) {
		super(name, null);
	}

	public Method(HString name, Type returnType, HString methDescriptor) {
		super(name, returnType);
		this.methDescriptor = methDescriptor;
		nofParams = nofParameters(methDescriptor);
	}

	protected Method getMethod(HString name, HString descriptor) {
		Method item = this;
		if (descriptor != null) {
			while (item != null && (item.name != name || item.methDescriptor != descriptor)) item = (Method)item.next;
		} else {
			while (item != null && item.name != name) {
				item = (Method)item.next;
			}
		}
		return item;
	}

	void clearCodeAndAssociatedFields(){
		code = null;
		localVars  = null;
		lineNrTab = null;
	}

	void insertLocalVar(LocalVar locVar){
		int key = locVar.index;
		Item lv = localVars[locVar.index], pred = null;
		while (lv != null && key >= locVar.index) {
			pred = lv;
			lv = lv.next;
		}
		locVar.next = lv;
		if (pred == null) localVars[locVar.index] = locVar; else pred.next = locVar;
	}

	public int getCodeSizeInBytes() {
		return machineCode.iCount * 4;
	}
	
	static int nofParameters(HString methDescriptor){
		int pos = methDescriptor.indexOf('(') + 1;
		int end = methDescriptor.lastIndexOf(')');
		assert pos == 1 && end >= 1;
		int nofParams = 0;
		while (pos < end) {
			char category = methDescriptor.charAt(pos);
			if (category == tcRef) {
				pos = methDescriptor.indexOf(';', pos);
				assert pos > 0;
			} else if (category == tcArray) {
				do {
					pos++;
					category = methDescriptor.charAt(pos);
				} while (category == tcArray);
				if (category == tcRef){
					pos = methDescriptor.indexOf(';', pos);
					assert pos > 0;
				}
			}
			pos++;
			nofParams++;
		}
		return nofParams;
	}

	protected static Type getReturnType(HString methodDescriptor) {// syntax in EBNF: methodDescriptor = "(" FormalParDesc ")" ReturnTypeDesc.
		int rparIndex = methodDescriptor.lastIndexOf(')');
		assert rparIndex > 0;
		HString retDesc = methodDescriptor.substring(rparIndex+1);
		Type returnType = Type.getTypeByDescriptor(retDesc);
		return returnType;
	}

	public static Method createCompSpecSubroutine(String jname) {
		HString name = HString.getRegisteredHString(jname);
		Method m = null;
		if (compSpecSubroutines == null) {
			m = new Method(name);
			compSpecSubroutines = m;
		} else {
			m = (Method)compSpecSubroutines.getItemByName(name);
			if(m == null) { // method doesn't exist -> create it
				m = new Method(name);
				m.next = compSpecSubroutines;
				compSpecSubroutines = m;
			}
		}		
		return m;
	}
	
	public static Method getCompSpecSubroutine(String jname) {
		HString name = HString.getRegisteredHString(jname);
		Method m = null;
		if (compSpecSubroutines != null) {
			m = (Method)compSpecSubroutines.getItemByName(name);
		}
		return m;
	}
	
	//--- debug primitives
	public void printItemCategory() {
		vrb.print("meth");
	}

	public void printLocalVars(int indentLevel) { 
		indent(indentLevel); vrb.print("local variables:");
		if (localVars == null) vrb.println(" none");
		else {
			vrb.println();
			for (int index=0; index < localVars.length; index++) {
				Item lv = localVars[index];
				while (lv != null) {
					lv.print(indentLevel+1); vrb.println();
					lv = lv.next;
				}
			}
		}
	}

	public void printLineNumberTable(int indentLevel) {
		indent(indentLevel);
		vrb.print("line numbers: ");
		if (lineNrTab == null) vrb.println("none");
		else{
			int length = lineNrTab.length;
			vrb.printf("%1$d pairs of (PC : line#):", length);
			for(int index = 0; index < length; index++) {
				if( (index&(8-1)) == 0) {
					vrb.println();
					indent(indentLevel);
				}
				int pair = lineNrTab[index];
				vrb.printf(" (%1$d : %2$d)", pair>>>16, pair&0xFFFF);
			}
			if( (length&(8-1)) != 0) vrb.println();
		}
	}

	public void printExceptionTable(int indentLevel) {
		if (exceptionTab != null) {
			indent(indentLevel);  vrb.println("exception table");
			for (ExceptionTabEntry e : exceptionTab) e.println(indentLevel + 1);
		}
	}

	public void printHeader(){
		int flags = accAndPropFlags;
//		if ((flags & (1<<dpfSysPrimitive)) != 0) flags &=  ~sysMethCodeMask;
		Dbg.printAccAndPropertyFlags(flags, 'M');
		if (type != null) {type.printTypeCategory(); type.printName();} // return type
		vrb.print(' ');  vrb.print(name);  
		if (methDescriptor != null) vrb.print(methDescriptor);
	}

	public void printHeaderX(int indentLevel){
		indent(indentLevel);
		vrb.printf("<%2$d> %3$s.%4$s%5$s", index, owner.name, name, methDescriptor);  
		Dbg.printAccAndPropertyFlags(accAndPropFlags, 'M');
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("meth  %1$s. %2$s%3$s", owner.name, name, methDescriptor);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'M');
	}

	public void print(int indentLevel){
		indent(indentLevel);
		printHeader();
		vrb.print(";//dFlags");
		if ((accAndPropFlags & (1<<dpfSysPrimitive)) == 0) Dbg.printDeepAccAndPropertyFlags(accAndPropFlags, 'M');
		else {
			Dbg.printDeepAccAndPropertyFlags(accAndPropFlags & ~sysMethCodeMask, 'M');
			vrb.printf("; mAttr=0x%1$3x", accAndPropFlags & sysMethCodeMask);
		}
		vrb.printf(", nofParams=%1$d, index=%2$d\n", nofParams, index);
		indent(indentLevel+1);
		vrb.print("code: ");
		if(this.code == null) vrb.println("none"); else vrb.printf("%1$d B\n", code.length);
		printLocalVars(indentLevel+1);
		printLineNumberTable(indentLevel+1);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
	
	public static void printCompSpecificSubroutines() {
		Method m = compSpecSubroutines;
		while(m != null) {
			vrb.printf("Name: %1$s\tOffset: 0x%2$3x\tAddress: 0x%3$3x\n", m.name, m.offset, m.address);
			vrb.println(m.machineCode.toString());
			m = (Method)m.next;
		}
	}

}
