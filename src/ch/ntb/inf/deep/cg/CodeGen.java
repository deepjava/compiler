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

package ch.ntb.inf.deep.cg;

import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.strings.HString;

public abstract class CodeGen implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, ICjvmInstructionOpcs, ICclassFileConsts, ICdescAndTypeConsts {
	protected static final boolean dbg = true;

	protected static final int maxNofParam = 32;

	protected static final int defaultNofInstr = 32;
	protected static final int defaultNofFixup = 8;

	protected static int objectSize, stringSize;
	protected static StdConstant int2floatConst1 = null;	// 2^52+2^31, for int -> float conversions
	protected static StdConstant int2floatConst2 = null;	// 2^32, for long -> float conversions
	protected static StdConstant int2floatConst3 = null;	// 2^52, for long -> float conversions

	public static int idGET1, idGET2, idGET4, idGET8;
	public static int idPUT1, idPUT2, idPUT4, idPUT8;
	public static int idBIT, idASM, idADR_OF_METHOD, idREF;
	public static int idDoubleToBits, idBitsToDouble;
	public static int idFloatToBits, idBitsToFloat;

	protected static Method stringNewstringMethod;
	protected static Method heapNewstringMethod;
	protected static Method strInitC;
	protected static Method strInitCII;
	protected static Method strAllocC;
	protected static Method strAllocCII;
	
	// nof parameter for a method, set by SSA, includes "this", long and doubles count as 2 parameters
	protected static int nofParam;	
	// nofParamGPR + nofParamFPR = nofParam, set by last exit set of last node
	protected static int nofParamGPR, nofParamFPR;	 
	// maximum nof registers used by this method
	public static int nofNonVolGPR, nofNonVolFPR, nofVolGPR, nofVolFPR;
	// gives required stack space for parameters of this method if not enough registers
	protected static int recParamSlotsOnStack;
	// gives required stack space for parameters of any call in this method if not enough registers
	public static int callParamSlotsOnStack;
	// type of parameter, set by SSA, includes "this", long and doubles count as 2 parameters
	protected static int[] paramType = new int[maxNofParam];
	// register type of parameter, long and doubles count as 2 parameters
	public static boolean[] paramHasNonVolReg = new boolean[maxNofParam];
	// register of parameter, long and doubles count as 2 parameters
	public static int[] paramRegNr = new int[maxNofParam];
	// last instruction where parameters is used
	public static int[] paramRegEnd = new int[maxNofParam];
	
	// information about into which registers parameters of this method go 
	protected static int nofMoveGPR, nofMoveFPR;
	protected static int[] moveGPRsrc = new int[maxNofParam];
	protected static int[] moveGPRdst = new int[maxNofParam];
	protected static int[] moveFPRsrc = new int[maxNofParam];
	protected static int[] moveFPRdst = new int[maxNofParam];

	public CodeGen() {}
	
	protected void init(Method m) {
		SSA ssa = m.ssa;
		nofParamGPR = 0; nofParamFPR = 0;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofVolGPR = 0; nofVolFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		recParamSlotsOnStack = 0; callParamSlotsOnStack = 0;
		if (dbg) StdStreams.vrb.println("generate code for " + m.owner.name + "." + m.name);
		for (int i = 0; i < maxNofParam; i++) {
			paramType[i] = tVoid;
			paramRegNr[i] = -1;
			paramRegEnd[i] = -1;
		}
		
		// make local copy
		int maxStackSlots = m.maxStackSlots;
		int i = maxStackSlots;
		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
			int type = ssa.paramType[i] & ~(1<<ssaTaFitIntoInt);
			paramType[i - maxStackSlots] = type;
			paramHasNonVolReg[i - maxStackSlots] = false;
			if (type == tLong || type == tDouble) i++;
			i++;
		}
		nofParam = i - maxStackSlots;
		if (nofParam > maxNofParam) {ErrorReporter.reporter.error(601); return;}
		if (dbg) StdStreams.vrb.println("nofParam = " + nofParam + ", double / long count as 2");
	}
	
	public abstract void translateMethod(Method m);

	public abstract void doFixups(Code32 code);

	public abstract void generateCompSpecSubroutines();

	protected static int getInt(byte[] bytes, int index){
		return (((bytes[index]<<8) | (bytes[index+1]&0xFF))<<8 | (bytes[index+2]&0xFF))<<8 | (bytes[index+3]&0xFF);
	}

	public void init() { 
		Class cls = Configuration.getOS().usClass;
		if (cls == null) {ErrorReporter.reporter.error(630); return;}
		Method m = Configuration.getOS().getSystemMethodByName(cls, "PUT1");
		if (m != null) idPUT1 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT2"); 
		if (m != null) idPUT2 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT4"); 
		if (m != null) idPUT4 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT8"); 
		if (m != null) idPUT8 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET1"); 
		if (m != null) idGET1 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET2"); 
		if (m != null) idGET2 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET4"); 
		if (m != null) idGET4 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET8"); 
		if (m != null) idGET8 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "BIT"); 
		if (m != null) idBIT = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "ASM"); 
		if (m != null) idASM = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "ADR_OF_METHOD"); 
		if (m != null) idADR_OF_METHOD = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "REF"); 
		if (m != null) idREF = m.id; else {ErrorReporter.reporter.error(631); return;}
		
		cls = Configuration.getOS().llClass;
		if (cls == null) {ErrorReporter.reporter.error(632); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "doubleToBits"); 
		if(m != null) idDoubleToBits = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "bitsToDouble"); 
		if(m != null) idBitsToDouble = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "floatToBits"); 
		if(m != null) idFloatToBits = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "bitsToFloat"); 
		if(m != null) idBitsToFloat = m.id; else {ErrorReporter.reporter.error(633); return;}
		
		objectSize = Type.wktObject.objectSize;
		stringSize = Type.wktString.objectSize;
		
		int2floatConst1 = new StdConstant(HString.getRegisteredHString("int2floatConst1"), (double)(0x10000000000000L + 0x80000000L));
		int2floatConst2 = new StdConstant(HString.getRegisteredHString("int2floatConst2"), (double)0x100000000L);
		int2floatConst3 = new StdConstant(HString.getRegisteredHString("int2floatConst3"), (double)0x10000000000000L);
		Linker32.globalConstantTable = null;
		Linker32.addGlobalConstant(int2floatConst1);
		Linker32.addGlobalConstant(int2floatConst2);
		Linker32.addGlobalConstant(int2floatConst3);
		
		Method.createCompSpecSubroutine("handleException");
		
		final Class stringClass = (Class)Type.wktString;
		final Class heapClass = Configuration.getOS().heapClass;	
		if ((stringClass != null) && (stringClass.methods != null)) {	// check if string class is loaded at all
			stringNewstringMethod = (Method)stringClass.methods.getItemByName("newstring"); 
			if(heapClass != null) {
				heapNewstringMethod = (Method)heapClass.methods.getItemByName("newstring"); 
			}
			if(dbg) {
				if (stringNewstringMethod != null) StdStreams.vrb.println("stringNewstringMethod = " + stringNewstringMethod.name + stringNewstringMethod.methDescriptor); else StdStreams.vrb.println("stringNewstringMethod: not found");
				if (heapNewstringMethod != null) StdStreams.vrb.println("heapNewstringMethod = " + heapNewstringMethod.name + heapNewstringMethod.methDescriptor); else StdStreams.vrb.println("heapNewstringMethod: not found");
			}
			
			m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getRegisteredHString("<init>"))) {
					if (m.methDescriptor.equals(HString.getRegisteredHString("([C)V"))) strInitC = m; 
					else if (m.methDescriptor.equals(HString.getRegisteredHString("([CII)V"))) strInitCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strInitC != null) StdStreams.vrb.println("stringInitC = " + strInitC.name + strInitC.methDescriptor); else StdStreams.vrb.println("stringInitC: not found");
				if (strInitCII != null) StdStreams.vrb.println("stringInitCII = " + strInitCII.name + strInitCII.methDescriptor); else StdStreams.vrb.println("stringInitCII: not found");
			}
			
			m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getRegisteredHString("allocateString"))) {
					if (m.methDescriptor.equals(HString.getRegisteredHString("(I[C)Ljava/lang/String;"))) strAllocC = m; 
					else if (m.methDescriptor.equals(HString.getRegisteredHString("(I[CII)Ljava/lang/String;"))) strAllocCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strAllocC != null) StdStreams.vrb.println("allocateStringC = " + strAllocC.name + strAllocC.methDescriptor); else StdStreams.vrb.println("allocateStringC: not found");
				if (strAllocCII != null) StdStreams.vrb.println("allocateStringCII = " + strAllocCII.name + strAllocCII.methDescriptor); else StdStreams.vrb.println("allocateStringCII: not found");
			}
		}
	}
}


