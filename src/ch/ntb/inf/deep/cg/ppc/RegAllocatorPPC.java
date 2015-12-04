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

package ch.ntb.inf.deep.cg.ppc;

import ch.ntb.inf.deep.cg.CodeGen;
import ch.ntb.inf.deep.cg.RegAllocator;
import ch.ntb.inf.deep.classItems.ExceptionTabEntry;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.ssa.SSAInstructionMnemonics;
import ch.ntb.inf.deep.ssa.SSAInstructionOpcs;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.SSAValueType;
import ch.ntb.inf.deep.ssa.instruction.Call;
import ch.ntb.inf.deep.ssa.instruction.Monadic;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;
import ch.ntb.inf.deep.ssa.instruction.NoOpnd;

public class RegAllocatorPPC extends RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers, ICclassFileConsts {
	static int regsGPR, regsFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	static int nofVolGPR, nofVolFPR;
	// used to find call in this method with most parameters -> gives stack size
	static int maxNofParamGPR, maxNofParamFPR;
	// used to determine operands which can be spilled to stack if running out of registers to allocate
	static int gprInitNotLocal;

	/**
	 * Generates the live ranges of all SSAValues of a method and assigns registers to them
	 */
	static void buildIntervals(SSA ssa) {
		RegAllocatorPPC.ssa = ssa;
		maxOpStackSlots = ssa.cfg.method.maxStackSlots;
		regsGPR = regsGPRinitial;
		regsFPR = regsFPRinitial;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofVolGPR = 0; nofVolFPR = 0;
		maxNofParamGPR = 0; maxNofParamFPR = 0;
		stackSlotSpilledRegs = -1;
		maxLocVarStackSlots = 0;
		for (int i = 0; i < maxNofJoins; i++) {
			rootJoins[i] = null;
			joins[i] = rootJoins[i];
		}

		nofInstructions = SSA.renumberInstructions(ssa.cfg);
		findLastNodeOfPhi(ssa);
		
		// copy into local array for faster access
		if (nofInstructions > instrs.length) {
			instrs = new SSAInstruction[nofInstructions + nofSSAInstr];
		}
		SSANode b = (SSANode) ssa.cfg.rootNode;
		int count = 0;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				instrs[count++] = b.phiFunctions[i];
			}
			for (int i = 0; i < b.nofInstr; i++) {
				instrs[count++] = b.instructions[i];
			}
			b = (SSANode) b.next;
		}	
		
		markUsedPhiFunctions();
		resolvePhiFunctions();	
		calcLiveRange(ssa);	// compute live intervals
//		printJoins();
//		ssa.print(2);
		assignRegType();	// assign volatile or nonvolatile type
	}

	// assign volatile or nonvolatile register 
	// checks if user wants to use floats in exception handlers
	private static void assignRegType() {
		// handle all live ranges of phi functions first
		for (int i = 0; i < maxNofJoins; i++) {
			SSAValue val = joins[i];
			while (val != null) {
				for (int k = val.start; k < val.end; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
						val.nonVol = true;
					}
				}
				//TODO
				ExceptionTabEntry[] tab = ssa.cfg.method.exceptionTab;
				if (tab != null) {
					for (int k = 0; k < tab.length; k++) {
						ExceptionTabEntry entry = tab[k];
						SSAInstruction handlerInstr = ssa.searchBca(entry.handlerPc);
						assert handlerInstr != null;
						for (int n = val.start; n < val.end; n++) {
							SSAInstruction instr1 = instrs[n];
							if (instr1 == handlerInstr) {
								val.nonVol = true;
							}
						}
					}
				}

				val = val.next;
			}			
		}
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			int currNo = res.n;
			int endNo = res.end;
			if (instr.ssaOpcode != sCloadLocal && res.join != null) continue;
			else if (instr.ssaOpcode == sCloadLocal && res.join != null) {
				if (res.join.nonVol) CodeGenPPC.paramHasNonVolReg[res.join.index - maxOpStackSlots] = true;
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start of method and here
				// call to inline method is omitted
				for (int k = 0; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
						res.nonVol = true;
						CodeGenPPC.paramHasNonVolReg[res.index - maxOpStackSlots] = true;
					}
				}
			} else {
				// check if call instruction in live range
				// call to inline method is omitted
				for (int k = currNo+1; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) 
						res.nonVol = true;
				}
				//TODO
				ExceptionTabEntry[] tab = ssa.cfg.method.exceptionTab;
				if (tab != null) {
					for (int k = 0; k < tab.length; k++) {
						ExceptionTabEntry entry = tab[k];
						SSAInstruction handlerInstr = ssa.searchBca(entry.handlerPc);
						assert handlerInstr != null;
						for (int n = currNo+1; n < endNo; n++) {
							SSAInstruction instr1 = instrs[n];
							if (instr1 == handlerInstr) {
								res.nonVol = true;
							}
						}
					}
				}
			}
			
			if (instr.ssaOpcode == sCcall) {	// check if floats in exceptions or special instruction which uses temporary storage on stack
				Call call = (Call)instr;
				if ((call.item.accAndPropFlags & (1 << dpfSynthetic)) != 0)
					if ((((Method)call.item).id) == CodeGenPPC.idENABLE_FLOATS) {
					CodeGenPPC.enFloatsInExc = true;
				}
				int id = ((Method)call.item).id;
				if (id == CodeGenPPC.idDoubleToBits || (id == CodeGenPPC.idBitsToDouble) ||  // DoubleToBits or BitsToDouble
					id == CodeGenPPC.idFloatToBits || (id == CodeGenPPC.idBitsToFloat))  // FloatToBits or BitsToFloat
					CodeGenPPC.tempStorage = true;
			}
		}
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 * finally, determine how many parameters are passed on the stack
	 */
	static void assignRegisters() {
		// handle loadLocal first
		if (dbg) StdStreams.vrb.println("\thandle load locals first:");
		gprInitNotLocal = regsGPRinitial;
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (instr.ssaOpcode == sCloadLocal) {
				if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
				if (((NoOpnd)instr).firstInCatch) {
					// if the variable of type Exception in a catch clause is loaded for further use,
					// it will be passed as a parameter in a prefixed register
					res.reg = reserveReg(gpr, true);
					assert res.reg >= 0;
					continue;
				}
				int type = res.type;
				if (type == tLong) {
					res.regLong = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
					gprInitNotLocal &= ~(1 << res.regLong);
					res.reg = CodeGenPPC.paramRegNr[res.index+1 - maxOpStackSlots];
				} else if ((type == tFloat) || (type == tDouble)) {
					res.reg = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
				} else if (type == tVoid) {
					assert false;
				} else {
					res.reg = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
				}	
				gprInitNotLocal &= ~(1 << res.reg);
				
				SSAValue joinVal = res.join;
				if (joinVal != null) {
					if (res.type == tLong) {
						joinVal.regLong = res.regLong;
						joinVal.reg = res.reg;
					} else {
						joinVal.reg = res.reg;
					}
				}
				if (dbg) {
					if (res.regLong >= 0) StdStreams.vrb.print("\tregLong = " + res.regLong + "\t");
					StdStreams.vrb.println("\treg = " + res.reg);
				}
			} 
		}
		
		if (dbg) StdStreams.vrb.println("\thandle all other instructions:");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			System.out.println("handle " + instr.toString());
			
			SSAValue res = instr.result;
			if (instr.ssaOpcode == sCPhiFunc && res.join == null) continue; // not used
			
			// check if phi-functions which could have been valid up to the last 
			// SSA instruction of the last node can now release their registers 
			if (instr.ssaOpcode != sCPhiFunc) {
				SSAInstruction instr1 = instr.freePhi;
				if (dbg) {if (instr1 != null) StdStreams.vrb.println("\tfree registers for phi-functions of last node");}
				while (instr1 != null) {
					SSAValue val = instr1.result.join;
					if (val != null && val.reg > -1 && val.end < i) {
						if (dbg) StdStreams.vrb.println("\t\tfree register of phi function: " + instr1.toString());
						freeReg(val);
					}
					instr1 = instr1.freePhi;
				}
			}
			
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.owner.ssaOpcode == sCPhiFunc && opd.owner.result.join == null) continue;
					if (opd.join != null) opd = opd.join;
//					assert opd.reg >= 0 : "opd = " + opd.toString(); // kann -1 sein fuer immediate
					if (opd.reg >= 0x100) {
						if (dbg) {StdStreams.vrb.println("\tcopy opds on the stack to registers for instruction " + instr.toString() + "     i="+i);}
//						int slot = opd.reg - 0x100;
						spillStackToReg(opd, i);
//						releaseStackSlot(opd.reg - 0x100);
						i += findRegAndSpill(instrs[i].result, i);	// assign register for new move instruction
					}
				}
			}
			
			if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
//			if (instr.ssaOpcode == sCPhiFunc && res.join == null) continue; // not used
			// reserve auxiliary register for this instruction
			int nofAuxRegGPR = (scAttrTab[instr.ssaOpcode] >> 16) & 0xF;
			if (nofAuxRegGPR == 4 && res.type == tLong) nofAuxRegGPR = 2; // long multiplication 
			else if ((nofAuxRegGPR == 5 && res.type == tLong)	// long shift
					|| ((nofAuxRegGPR == 6 && (res.type == tFloat || res.type == tDouble)))) // float loading, int -> float conversion
				nofAuxRegGPR = 1;
			else if (nofAuxRegGPR == 7 && res.type == tLong)	// long division
				nofAuxRegGPR = 2;
			else if (nofAuxRegGPR == 8) {	// modulo division
				if (res.type == tLong) {nofAuxRegGPR = 2;}
				else if (res.type == tFloat || res.type == tDouble) nofAuxRegGPR = 1;
			}			
			if (nofAuxRegGPR == 1) {
				res.regGPR1 = reserveReg(gpr, false);
				assert res.regGPR1 >= 0;
			} else if (nofAuxRegGPR == 2) {
				res.regGPR1 = reserveReg(gpr, false);
				assert res.regGPR1 >= 0;
				res.regGPR2 = reserveReg(gpr, false);
				assert res.regGPR2 >= 0;
			}
			if (dbg) {
				if (res.regGPR1 != -1) StdStreams.vrb.print("\tauxReg1 = " + res.regGPR1);
				if (res.regGPR2 != -1) StdStreams.vrb.print("\tauxReg2 = " + res.regGPR2);
			}
			
			// reserve temporary storage on the stack for certain fpr operations
			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
				CodeGenPPC.tempStorage = true;
			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
				CodeGenPPC.tempStorage = true;
			if ((instr.ssaOpcode == sCdiv || instr.ssaOpcode == sCrem) && res.type == tLong)
				CodeGenPPC.tempStorage = true;

			// reserve register for result of instruction
			if (instr.ssaOpcode == sCloadLocal) {	// already done
			} else if (res.join != null) {
				if (instr.ssaOpcode == sCPhiFunc) {
					SSAValue joinVal = res.join;
					if (dbg) StdStreams.vrb.println("\tis phi function, joinVal != null");
					if (joinVal.reg < 0) {	// join: register not assigned yet
						findRegOrStackSlot(joinVal);
						res.regLong = joinVal.regLong;
						res.reg = joinVal.reg;
//						if (res.type == tLong) {
//							joinVal.regLong = reserveReg(gpr, joinVal.nonVol);
//							joinVal.reg = reserveReg(gpr, joinVal.nonVol);
//							res.regLong = joinVal.regLong;
//							res.reg = joinVal.reg;
//						} else if ((res.type == tFloat) || (res.type == tDouble)) {
//							joinVal.reg = reserveReg(fpr, joinVal.nonVol);
//							res.reg = joinVal.reg;
//						} else if (res.type == tVoid) {
//							assert false;
//						} else {
//							joinVal.reg = reserveReg(gpr, joinVal.nonVol);
//							res.reg = joinVal.reg;
//						}
					} else if (joinVal.reg < 256) {	// assign same register as join val
						if (dbg) StdStreams.vrb.println("\tassign same reg as join val");
						if (res.type == tLong) {
							res.regLong = joinVal.regLong;	
							res.reg = joinVal.reg;
							reserveReg(gpr, res.regLong);
							reserveReg(gpr, res.reg);
						} else if ((res.type == tFloat) || (res.type == tDouble)) {
							res.reg = joinVal.reg;
							reserveReg(fpr, res.reg);
						} else if (res.type == tVoid) {
							assert false;
							//						res.reg = joinVal.reg;
							//						reserveReg(gpr, res.reg);
						} else {
							res.reg = joinVal.reg;
							reserveReg(gpr, res.reg);
						}
					} else {	// assign same stack slot as phi function
						if (dbg) StdStreams.vrb.println("\tassign same stack slot as join val");
						if (res.type == tLong) {
							res.regLong = joinVal.regLong;	
							res.reg = joinVal.reg;
							reserveStackSlot(res.regLong - 0x100);
							reserveStackSlot(res.reg - 0x100);
						} else if ((res.type == tFloat) || (res.type == tDouble)) {
							res.reg = joinVal.reg;
							reserveStackSlot(res.reg - 0x100);
						} else if (res.type == tVoid) {
							assert false;
							//						res.reg = joinVal.reg;
							//						reserveReg(gpr, res.reg);
						} else {
							res.reg = joinVal.reg;
							reserveStackSlot(res.reg - 0x100);
						}
					}
				} else { // is not a phi function
					SSAValue joinVal = res.join;
					if (dbg) StdStreams.vrb.println("\tnon phi function, joinVal != null");
					if (joinVal.reg < 0) {	// join: register not assigned yet
						if (dbg) StdStreams.vrb.println("\tjoin value has no register yet");
						i += findRegAndSpill(joinVal, i);
						res.regLong = joinVal.regLong;
						res.reg = joinVal.reg;
//						if (res.type == tLong) {
//							joinVal.regLong = reserveReg(gpr, joinVal.nonVol);
//							joinVal.reg = reserveReg(gpr, joinVal.nonVol);
//							res.regLong = joinVal.regLong;
//							res.reg = joinVal.reg;
//						} else if ((res.type == tFloat) || (res.type == tDouble)) {
//							joinVal.reg = reserveReg(fpr, joinVal.nonVol);
//							res.reg = joinVal.reg;
//						} else if (res.type == tVoid) {
//							assert false;
//						} else {
//							joinVal.reg = reserveReg(gpr, joinVal.nonVol);
//							res.reg = joinVal.reg;
//						}
					} else if (joinVal.reg < 256) {	// assign same register as join val
						if (dbg) StdStreams.vrb.println("\tassign same reg as join val");
						if (res.type == tLong) {
							res.regLong = joinVal.regLong;	
							res.reg = joinVal.reg;
							reserveReg(gpr, res.regLong);
							reserveReg(gpr, res.reg);
						} else if ((res.type == tFloat) || (res.type == tDouble)) {
							res.reg = joinVal.reg;
							reserveReg(fpr, res.reg);
						} else if (res.type == tVoid) {
							assert false;
							//						res.reg = joinVal.reg;
							//						reserveReg(gpr, res.reg);
						} else {
							res.reg = joinVal.reg;
							reserveReg(gpr, res.reg);
						}
					} else {	// insert spilling instruction and get freed register
						i += findRegAndSpill(joinVal, i);
//						if (dbg) StdStreams.vrb.println("\tassign same reg as join val");
//						if (res.type == tLong) {
//							res.regLong = joinVal.regLong;	
//							res.reg = joinVal.reg;
//							reserveReg(gpr, res.regLong);
//							reserveReg(gpr, res.reg);
//						} else if ((res.type == tFloat) || (res.type == tDouble)) {
//							res.reg = joinVal.reg;
//							reserveReg(fpr, res.reg);
//						} else if (res.type == tVoid) {
//							assert false;
//							//						res.reg = joinVal.reg;
//							//						reserveReg(gpr, res.reg);
//						} else {
//							res.reg = joinVal.reg;
//							reserveReg(gpr, res.reg);
//						}
					}
					
				}
			} else if (instr.ssaOpcode == sCloadConst) {
				// check if operand can be used with immediate instruction format
				SSAInstruction instr1 = instrs[res.end];
				boolean imm = (scAttrTab[instr1.ssaOpcode] & (1 << ssaApImmOpd)) != 0;
				if (imm && res.index < maxOpStackSlots && res.join == null) {
					if (dbg) StdStreams.vrb.print("\timmediate");
					// opd must be used in an instruction with immediate form available
					// and opd must not be already in a register 
					// and opd must have join == null
					int type = res.type & 0x7fffffff;
					if (((instr1.ssaOpcode == sCadd) && ((type == tInteger) || (type == tLong)))	
							|| ((instr1.ssaOpcode == sCsub) && ((type == tInteger) || (type == tLong)))) {
							// add, sub only with integer and long
						StdConstant constant = (StdConstant)res.constant;
						if (res.type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if ((immValLong >= -32768) && (immValLong <= 32767)) {} else i += findRegAndSpill(res, i);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {} else i += findRegAndSpill(res, i);
						}
					} else if (instr1.ssaOpcode == sCmul) {
						StdConstant constant = (StdConstant)res.constant;
						boolean isPowerOf2; int immVal = 0;
						if (type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							isPowerOf2 = isPowerOf2(immValLong);
						} else {	
							immVal = constant.valueH;
							isPowerOf2 = isPowerOf2(immVal);
						}
						if (((type == tInteger)||(type == tLong)) && (res == instr1.getOperands()[1]) && isPowerOf2) {							
							// check if multiplication by const which is a power of 2, const must be multiplicator and positive
							res.reg = -2;
						} else if (type == tInteger) { 
							// check if multiplication by const which is smaller than 2^15
							if ((immVal >= -32768) && (immVal <= 32767)) {} else i += findRegAndSpill(res, i);
						} else {
							if (dbg) StdStreams.vrb.println(" not possible");
							i += findRegAndSpill(res, i);
						}
					} else if (((instr1.ssaOpcode == sCdiv)||(instr1.ssaOpcode == sCrem)) && ((type == tInteger)||(type == tLong)) && (res == instr1.getOperands()[1])) {
						// check if division by const which is a power of 2, const must be divisor and positive
						StdConstant constant = (StdConstant)res.constant;
						boolean isPowerOf2;
						if (type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							isPowerOf2 = isPowerOf2(immValLong);
						} else {	
							int immVal = constant.valueH;
							isPowerOf2 = isPowerOf2(immVal);
						}
						if (!isPowerOf2) i += findRegAndSpill(res, i);	
					} else if ((instr1.ssaOpcode == sCand)
							|| (instr1.ssaOpcode == sCor)
							|| (instr1.ssaOpcode == sCxor)
							// logical operators with integer and long
							|| (instr1.ssaOpcode == sCshl) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCshr) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCushr) && (res == instr1.getOperands()[1])
							// shift operators only if immediate is shift distance (and not value to be shifted)
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idGETGPR))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idGETFPR))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idGETSPR))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTGPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTFPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTSPR) && (instr1.getOperands()[0] == res))
							// calls to some unsafe methods
							|| ((instr1.ssaOpcode == sCbranch) && ((res.type & 0x7fffffff) == tInteger)))
						// branches but not switches (the second operand of a switch is already constant)
					{
						StdConstant constant = (StdConstant)res.constant;
						if (res.type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if ((immValLong >= -32768) && (immValLong <= 32767)) {} else i += findRegAndSpill(res, i);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {} else i += findRegAndSpill(res, i);
						}
					} else if (((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idASM))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idADR_OF_METHOD))) {
					} else {	// opd cannot be used as immediate opd	
						if (dbg) StdStreams.vrb.println(" not possible");
						i += findRegAndSpill(res, i);	
					}	
				} else {	// opd has index != -1 or cannot be used as immediate opd	
					i += findRegAndSpill(res, i);	
				}
			} else {	// all other instructions
				if (res.reg < 0) {	// not yet assigned
					i += findRegAndSpill(res, i);
				}
			}
			if (dbg) {
				if (res.regLong >= 0) StdStreams.vrb.print("\tregLong = " + res.regLong + "\t");
				StdStreams.vrb.println("\treg = " + res.reg);
				StdStreams.vrb.println(Integer.toHexString(regsGPR));
			}

			if (res.regGPR1 != -1) freeReg(gpr, res.regGPR1);
			if (res.regGPR2 != -1) freeReg(gpr, res.regGPR2);

			// free registers of operands if end of live range reached 
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join == null) {
						if ((opd.owner != null) && (opd.owner.ssaOpcode == sCloadLocal)) {
							if (CodeGenPPC.paramRegEnd[opd.owner.result.index - maxOpStackSlots] <= i)
								freeReg(opd);
							continue;
						}
						if (opd.end <= i && opd.reg > -1) freeReg(opd);
					} else {
						SSAValue val = opd.join;
						if (val.end <= i && val.reg > -1) freeReg(val);
					}
				}
			}

			// free registers of result if end of live range is current instruction
			if (res != null) {
				if (res.join != null) res = res.join;
				if (res.end == i && res.reg > -1) freeReg(res);
			}
			
			// find call which needs most registers for parameters
			// this determines the size of the stack for this method
			if (instr.ssaOpcode == sCcall) {
				int gpr = 0, fpr = 0;
				for (SSAValue opd : opds) {
					int type = opd.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) gpr += 2;
					else if (type == tFloat || type == tDouble) fpr++;
					else gpr++;
				}
				if (gpr > maxNofParamGPR) maxNofParamGPR = gpr; 
				if (fpr > maxNofParamFPR) maxNofParamFPR = fpr; 
			}
		}
		CodeGenPPC.nofNonVolGPR = nofNonVolGPR;
		CodeGenPPC.nofNonVolFPR = nofNonVolFPR;
		CodeGenPPC.nofVolGPR = nofVolGPR;
		CodeGenPPC.nofVolFPR = nofVolFPR;
		int nof = maxNofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) CodeGenPPC.callParamSlotsOnStack = nof;
		nof = maxNofParamFPR - (paramEndFPR - paramStartFPR + 1);
		if (nof > 0) CodeGenPPC.callParamSlotsOnStack += nof*2;
		if (spill) {
			refreshSSANodes();
			spill = false;
		}
	}

	// the array with the SSA instructions of every node which has new spilling instructions
	// must be updated 
	private static void refreshSSANodes() {
		if (dbg) StdStreams.vrb.println("\nstart refreshing");
		SSANode b = (SSANode) ssa.cfg.rootNode;
		int startIndex = 0, endIndex = 0;
		while (b != null) {
			SSAInstruction[] instr = b.instructions;
			SSAInstruction last = instr[b.nofInstr-1];
			while (instrs[startIndex].ssaOpcode == sCPhiFunc) startIndex++; // omit phi functions
			endIndex = startIndex;
			while (instrs[endIndex] != last) endIndex++;
			if (endIndex - startIndex + 1 != b.nofInstr) {
				if (dbg) StdStreams.vrb.println("needs refreshing" );
				if (dbg) StdStreams.vrb.println(b.toString());
				if (dbg) StdStreams.vrb.println("startIndex="+startIndex + "  endIndex=" + endIndex + "  bnof=" + b.nofInstr + "  bnofPhi=" + b.nofPhiFunc);
				SSAInstruction[] newArray = new SSAInstruction[endIndex - startIndex + 1];
				for (int i = 0; i <= endIndex - startIndex; i++) newArray[i] = instrs[startIndex + i]; 
				b.instructions = newArray;
				b.nofInstr = endIndex - startIndex + 1;
			}
			startIndex = endIndex + 1;
			b = (SSANode) b.next;
		}
	}

	// insert a register move into the SSA instruction array
	// opd = value to spill (if type == long, spill one of its registers)
	// slot: >= 256 slot where opd is spilled to, 
	// pos in instruction array
	private static void spillRegToStack(SSAValue opd, int pos) {
		if (dbg) StdStreams.vrb.println("\tinsert new RegMove in method before instr: " + instrs[pos].toString());
		spill = true;
		SSAInstruction[] newInstrs;
		if (nofInstructions >= instrs.length) 
			newInstrs = new SSAInstruction[nofInstructions + nofSSAInstr];
		else 
			newInstrs = new SSAInstruction[instrs.length];
		System.arraycopy(instrs, 0, newInstrs, 0, pos);
		SSAInstruction move = new Monadic(sCregMove, opd, 0);
		SSAValue res = new SSAValue(opd);
		res.join = null;
		if (res.type == tLong) {
			res.regLong = getEmptyStackSlot();
			freeReg(gpr, opd.regLong);
		}
		res.reg = getEmptyStackSlot();
		freeReg(gpr, opd.reg);
		res.n = pos;
		res.end = opd.end+1;
		res.owner = move;
		opd.end = pos;	
		move.result = res;
		newInstrs[pos] = move;
		nofInstructions++;
		System.arraycopy(instrs, pos, newInstrs, pos+1, nofInstructions-pos);
		instrs = newInstrs;
		// correct end index of live span for the results of all SSA instructions
		for (int i = 0; i < pos; i++) {
			SSAValue val = instrs[i].result;
			if (val.end > pos) val.end++;
		}
		for (int i = pos + 1; i < nofInstructions; i++) {
			SSAValue val = instrs[i].result;
			val.end++;
		}
		// correct end index of live span for all join values
		for (int i = 0; i < maxNofJoins; i++) {
			SSAValue join = joins[i];
			while (join != null) {
				if (join.end >= pos) join.end++;
				join  = join.next;
			}
		}
		// replace all opds which use the spilled instruction with the result of the new instruction
		for (int i = pos + 1; i < nofInstructions; i++) {
			SSAValue[] opds = instrs[i].getOperands();
			if (opds != null) {
				for (int k = 0; k < opds.length; k++) {
					SSAValue val = opds[k];
					if (val == opd) {
						System.out.print("\treplace opd in " + instrs[i].toString());						
						opds[k] = res;
						System.out.println("\t is now: " + instrs[i].toString());
					}
				}
			}
		}
		System.out.println("\t" + move.toString() + "\n");
	}

	// insert a register move into the SSA instruction array
	// opd = value to fetch from stack
	// pos in instruction array
	private static void spillStackToReg(SSAValue opd, int pos) {
		if (dbg) StdStreams.vrb.println("\tinsert new RegMove in method before instr: " + instrs[pos].toString() + " parameter 'slot'=" + (opd.reg - 0x100));
		spill = true;
		SSAInstruction[] newInstrs;
		if (nofInstructions >= instrs.length) 
			newInstrs = new SSAInstruction[nofInstructions + nofSSAInstr];
		else 
			newInstrs = new SSAInstruction[instrs.length];
		System.arraycopy(instrs, 0, newInstrs, 0, pos);
		SSAInstruction move = new Monadic(sCregMove, opd, 0);
		SSAValue res = new SSAValue(opd);
		res.join = null;
		if (res.type == tLong) {
			res.regLong = -1;
			releaseStackSlot(opd.regLong - 0x100);
		}
		res.reg = -1;
		releaseStackSlot(opd.reg - 0x100);
		res.n = pos;
		res.owner = move;
		res.end = opd.end + 1;	
		opd.end = pos;
		move.result = res;
		newInstrs[pos] = move;
		nofInstructions++;
		System.arraycopy(instrs, pos, newInstrs, pos+1, nofInstructions-pos);
		instrs = newInstrs;
		// correct end index of live span for the results of all SSA instructions
		for (int i = 0; i < pos; i++) {
			SSAValue val = instrs[i].result;
			if (val.end > pos) val.end++;
		}
		for (int i = pos + 1; i < nofInstructions; i++) {
			SSAValue val = instrs[i].result;
			val.end++;
		}
		// correct end index of live span for all join values
		for (int i = 0; i < maxNofJoins; i++) {
			SSAValue join = joins[i];
			while (join != null) {
				if (join.end >= pos) join.end++;
				join  = join.next;
			}
		}
		// replace all opds which use the spilled instruction with the result of the new instruction
		for (int i = pos + 1; i < nofInstructions; i++) {
			SSAValue[] opds = instrs[i].getOperands();
			if (opds != null) {
				for (int k = 0; k < opds.length; k++) {
					SSAValue val = opds[k];
					if (val == opd) {
						System.out.print("\treplace opd in " + instrs[i].toString());						
						opds[k] = res;
						System.out.println("\t is now: " + instrs[i].toString());
					}
				}
			}
		}
		System.out.println("\t" + move.toString() + "\n");
	}

	public static boolean isPowerOf2(long val) {
		return (val > 0) && (val & (val-1)) == 0;
	}

	// assign a register to a SSAValue, insert a spill instruction if no registers available
	// return nof inserted spill instructions
	private static int findRegAndSpill(SSAValue val, int pos) {
		int num = 0;
		int type = val.type;
		if (type == tLong) {
//			val.regLong = reserveReg(gpr, val.nonVol);
			int regLong = val.regLong;
			if (regLong < 0) regLong = reserveReg(gpr, val.nonVol);	// could have already been assigned
			//			assert val.reg >= 0;
			//			if (reg < 0) reg = reserveReg(gpr, val.nonVol);
			if (regLong < 0) {
				if (dbg) StdStreams.vrb.println("\tno register available for long part of result of " + instrs[pos].toString());
				//				int slot = getEmptyStackSlot();
				SSAValue valSpill = findLastUsedOpd(pos, val.nonVol);
				spillRegToStack(valSpill, pos);	
				num++;
				regLong = reserveReg(gpr, val.nonVol);
				val.regLong = regLong;
				//				freeReg(gpr, valSpill.reg);
				//				val.regLong = valSpill.reg;			
			} else //{
				val.regLong = regLong;
				int reg = val.reg;
				if (reg < 0) reg = reserveReg(gpr, val.nonVol);
				if (reg < 0) {
					if (dbg) StdStreams.vrb.println("\tno register available for result of " + instrs[pos].toString());
					//				int slot = getEmptyStackSlot();
					SSAValue valSpill = findLastUsedOpd(pos, val.nonVol);
					spillRegToStack(valSpill, pos);	
					num++;
					reg = reserveReg(gpr, val.nonVol);
					val.reg = reg;
					//				freeReg(gpr, valSpill.reg);
					//				val.reg = valSpill.reg;			
				} else val.reg = reg;
			//}
		} else if ((type == tFloat) || (type == tDouble)) {
			val.reg = reserveReg(fpr, val.nonVol);
			
		} else if (type == tVoid) { // nothing to do
		} else {
			int reg = val.reg;
			if (reg < 0) reg = reserveReg(gpr, val.nonVol);
			if (reg < 0) {
				if (dbg) StdStreams.vrb.println("\tno register available for result of " + instrs[pos].toString());
//				int slot = getEmptyStackSlot();
				SSAValue valSpill = findLastUsedOpd(pos, val.nonVol);
				spillRegToStack(valSpill, pos);	
				num++;
				reg = reserveReg(gpr, val.nonVol);
				val.reg = reg;
//				freeReg(gpr, valSpill.reg);
//				val.reg = valSpill.reg;			
			} else val.reg = reg;
		}
		return num;
	}

	// assign a register or stack slot to a SSAValue
	private static void findRegOrStackSlot(SSAValue val) {
		int type = val.type;
		if (type == tLong) {
			int regLong = val.regLong;
			if (regLong < 0) regLong = reserveReg(gpr, val.nonVol);	// could have already been assigned
			int reg = reserveReg(gpr, val.nonVol);
			if (regLong < 0) {
				if (dbg) StdStreams.vrb.println("\tno register available for long part of result of " + val.owner.toString());
				val.regLong = getEmptyStackSlot();
			} else val.regLong = regLong;
			
			if (reg < 0) {
				if (dbg) StdStreams.vrb.println("\tno register available for result of " + val.owner.toString());
				val.reg = getEmptyStackSlot();
			} else val.reg = reg;

		} else if ((type == tFloat) || (type == tDouble)) {
			val.reg = reserveReg(fpr, val.nonVol);
			
		} else if (type == tVoid) { // nothing to do
		} else {
			int reg = reserveReg(gpr, val.nonVol);
			if (reg < 0) {
				if (dbg) {
					if (val.owner != null) StdStreams.vrb.println("\tno register available for result of " + val.owner.toString());
					else StdStreams.vrb.println("\tno register available for join");
				}
				val.reg = getEmptyStackSlot();
			} else val.reg = reg;
		}
	}

	// search SSAValue (= result of SSA instruction) which is used further down in the 
	// instruction stream starting with 'pos' and which has the longest span of not being used  
	// either search for value with nonvolatile register or any
	private static SSAValue findLastUsedOpd(int pos, boolean nonVol) {
		if (dbg) StdStreams.vrb.println("\tsearch for opd to be spilled, start at pos=" + pos + (nonVol?" nonVol":" volatile"));
		int regs = gprInitNotLocal;
		if (nonVol) regs &= regsGPRinitialNonVol;
		int i = pos;
		boolean stop = false;
		SSAValue opd = null;
		while (i < nofInstructions && !stop) {	// start from actual 
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc && instr.result.join == null) {i++; continue;} // not used
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for(int n = 0; n < opds.length; n++) {
					opd = opds[n];
					int start = opd.n;
					SSAValue val = opd;
					if (opd.join != null) {val = opd.join; start = val.start;} 
					if (start < pos) {
//						if (nonVol && !val.nonVol) continue;	// braucht es das noch???????????
						if (val.type == tLong) {
							regs &= ~(1 << val.regLong);
							if (dbg) StdStreams.vrb.println("\t\t" + Integer.toHexString(regs) + "  del long reg=" + val.regLong + "  of instr " + instr.toString());
							if ((regs & (regs-1)) == 0) {stop = true; break;}
						}
						regs &= ~(1 << val.reg);
						if (dbg) StdStreams.vrb.println("\t\t" + Integer.toHexString(regs) + "  del reg=" + val.reg + "  of instr " + instr.toString());
						if ((regs & (regs-1)) == 0) {stop = true; break;}
					}
				}
			}
			i++;
		}
		if (dbg) {
			// if val is joinVal -> has no owner
			StdStreams.vrb.print("\tspill result of instruction ");
			if (opd.owner != null) StdStreams.vrb.print(opd.owner.toString()); else StdStreams.vrb.print(" result is join value");
			StdStreams.vrb.println("\tresult first used at instr " + instrs[i-1].toString());
		}
		return opd;
	}

	// reserve register, returns -1 in case no register available
	static int reserveReg(boolean isGPR, boolean isNonVolatile) {
		if (dbg) StdStreams.vrb.print("\tbefore reserving 0x" + Integer.toHexString(regsGPR));
		if (isGPR) {
			int i;
			if (!isNonVolatile) {	// is volatile
				i = paramStartGPR;
				while (i < nonVolStartGPR) {
					if ((regsGPR & (1 << i)) != 0) {
						regsGPR &= ~(1 << i);	
						if (i-paramStartGPR > nofVolGPR) nofVolGPR = i+1-paramStartGPR;
						return i;
					}
					i++;
				}
			} 
			i = topGPR;
			while (i >= nonVolStartGPR) {
				if ((regsGPR & (1 << i)) != 0) {
					regsGPR &= ~(1 << i);
					if (nofGPR - i > nofNonVolGPR) nofNonVolGPR = nofGPR - i;
					return i;
				}
				i--;
			}
			if (dbg) StdStreams.vrb.print("\tnot enough GPR's, spilling");
//			ErrorReporter.reporter.error(603);
//			assert false: "not enough GPR's for locals";
			return -1;
		} else {
			int i;
			if (!isNonVolatile) {
				i = paramStartFPR;
				while (i < nonVolStartFPR) {
					if ((regsFPR & (1 << i)) != 0) {
						regsFPR &= ~(1 << i);	
						if (i-paramStartFPR > nofVolFPR) nofVolFPR = i+1-paramStartFPR;
						return i;
					}
					i++;
				}
			} 
			i = topFPR;
			while (i >= nonVolStartFPR) {
				if ((regsFPR & (1 << i)) != 0) {
					regsFPR &= ~(1 << i);
					if (nofFPR - i > nofNonVolFPR) nofNonVolFPR = nofFPR - i;
					return i;
				}
				i--;
			}
			ErrorReporter.reporter.error(604);
			assert false: "not enough FPR's for locals";
			return 0;
		}
	}

	private static int getEmptyStackSlot() {
		int i = 0;
		while (i < 32) {
			if ((stackSlotSpilledRegs & (1 << i)) != 0) {
				stackSlotSpilledRegs &= ~(1 << i);
				if (i > maxLocVarStackSlots - 1) maxLocVarStackSlots = i + 1;
				return i + 0x100;
			}
			i++;
		}
		ErrorReporter.reporter.error(605);
		assert false: "not enough stack slots for spilling";
		return 0;
	}
	
	
	// zusammen fassen mit reserve Register
	private static void reserveStackSlot(int slot) {
		stackSlotSpilledRegs &= ~(1 << slot);
	}

	private static void releaseStackSlot(int slot) {
		stackSlotSpilledRegs |= 1 << slot;
	}

	static void reserveReg(boolean isGPR, int regNr) {
		if (isGPR) {
			regsGPR &= ~(1 << regNr);
		} else {
			regsFPR &= ~(1 << regNr);
		}
	}

	private static void freeReg(SSAValue val) {
		if (val.type == tLong) {
			freeReg(gpr, val.regLong);
			freeReg(gpr, val.reg);
		} else if ((val.type == tFloat) || (val.type == tDouble)) {
			freeReg(fpr, val.reg);
		} else {
			freeReg(gpr, val.reg);
		}
	}

	private static void freeReg(boolean isGPR, int reg) {
		if (isGPR) {
			regsGPR |= 1 << reg;
		} else {
			regsFPR |= 1 << reg;
		}
		if (dbg) StdStreams.vrb.println("\tfree reg " + reg + "\tregsGPR=0x" + Integer.toHexString(regsGPR));
	}
	
}
