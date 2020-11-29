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

package org.deepjava.cg.ppc;

import org.deepjava.cg.CodeGen;
import org.deepjava.cg.RegAllocator;
import org.deepjava.classItems.ICclassFileConsts;
import org.deepjava.classItems.Method;
import org.deepjava.classItems.StdConstant;
import org.deepjava.host.ErrorReporter;
import org.deepjava.host.StdStreams;
import org.deepjava.ssa.SSAInstructionMnemonics;
import org.deepjava.ssa.SSAInstructionOpcs;
import org.deepjava.ssa.SSAValue;
import org.deepjava.ssa.SSAValueType;
import org.deepjava.ssa.instruction.Call;
import org.deepjava.ssa.instruction.NoOpnd;
import org.deepjava.ssa.instruction.SSAInstruction;

public class RegAllocatorPPC extends RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers, ICclassFileConsts {

	// used to indicate of used and free GPRs and FPRs
	public static int regsGPR, regsFPR;	
	// maximum nof registers used by this method, used to calculate stack size and for debugging output
	protected static int nofNonVolFPR, nofVolFPR;

	/**
	 * Assign a register or memory location to all SSAValues
	 * finally, determine how many parameters are passed on the stack
	 * checks if user wants to use floats in exception handlers
	 * checks if temporary space on stack is necessary
	 */
	static void assignRegisters() {
		// used to find call in this method with most parameters -> gives stack size
		int maxNofParamGPR = 0, maxNofParamFPR = 0;

		// handle loadLocal first
		if (dbg) StdStreams.vrb.println("\thandle load locals first:");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (instr.ssaOpcode == sCloadLocal) {
				if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
				if (((NoOpnd)instr).firstInCatch) {
					// if the variable of type Exception in a catch clause is loaded for further use,
					// it will be passed as a parameter in a prefixed register
					res.reg = reserveReg(gpr, true);
					continue;
				}
				int type = res.type;
				if (type == tLong) {
					res.regLong = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
					res.reg = CodeGenPPC.paramRegNr[res.index+1 - maxOpStackSlots];
				} else if ((type == tFloat) || (type == tDouble)) {
					res.reg = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
				} else if (type == tVoid) {
				} else {
					res.reg = CodeGenPPC.paramRegNr[res.index - maxOpStackSlots];
				}	
				SSAValue joinVal = res.join;
				if (joinVal != null) {
					if (res.type == tLong) {
						joinVal.regLong = res.regLong;
						joinVal.reg = res.reg;
					} else {
						joinVal.reg = res.reg;
					}
				}
				if (dbg) StdStreams.vrb.println("\treg = " + res.reg);
			} 
			
			if (instr.ssaOpcode == sCcall) {	// check if floats in exceptions or special instruction which uses temporary storage on stack
				Call call = (Call)instr;
				if ((call.item.accAndPropFlags & (1 << dpfSynthetic)) != 0)
					if ((((Method)call.item).id) == CodeGenPPC.idENABLE_FLOATS) {
					CodeGenPPC.enFloatsInExc = true;
				}
			}
		}
		
		if (dbg) StdStreams.vrb.println("\thandle all other instructions:");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			
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
			
			if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
			if (instr.ssaOpcode == sCPhiFunc && res.join == null) continue; 
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
			
			if (nofAuxRegGPR == 1) res.regGPR1 = reserveReg(gpr, false);
			else if (nofAuxRegGPR == 2) {
				res.regGPR1 = reserveReg(gpr, false);
				if (dbg) if (res.regGPR1 != -1) StdStreams.vrb.print("\tauxReg1 = " + res.regGPR1);
				res.regGPR2 = reserveReg(gpr, false);
				if (dbg) if (res.regGPR2 != -1) StdStreams.vrb.print("\tauxReg2 = " + res.regGPR2);
			}
			
			// reserve temporary storage on the stack for certain fpr operations
//			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
//				CodeGenPPC.tempStorage = true;
//			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
//				CodeGenPPC.tempStorage = true;
//			if ((instr.ssaOpcode == sCdiv || instr.ssaOpcode == sCrem) && res.type == tLong)
//				CodeGenPPC.tempStorage = true;

			// reserve register for result of instruction
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (res.join != null) {
				SSAValue joinVal = res.join;
				if (dbg) StdStreams.vrb.println("\tjoinVal != null");
				if (joinVal.reg < 0) {	// join: register not assigned yet
					if (res.type == tLong) {
						joinVal.regLong = reserveReg(gpr, joinVal.nonVol);
						joinVal.reg = reserveReg(gpr, joinVal.nonVol);
						res.regLong = joinVal.regLong;
						res.reg = joinVal.reg;
					} else if ((res.type == tFloat) || (res.type == tDouble)) {
						joinVal.reg = reserveReg(fpr, joinVal.nonVol);
						res.reg = joinVal.reg;
					} else if (res.type == tVoid) {
					} else {
						joinVal.reg = reserveReg(gpr, joinVal.nonVol);
						res.reg = joinVal.reg;
					}
				} else {// assign same register as phi function
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
						res.reg = joinVal.reg;
						reserveReg(gpr, res.reg);
					} else {
						res.reg = joinVal.reg;
						reserveReg(gpr, res.reg);
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
							if ((immValLong >= -32768) && (immValLong <= 32767)) {} else findReg(res);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {} else findReg(res);
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
							if ((immVal >= -32768) && (immVal <= 32767)) {} else findReg(res);
						} else {
							if (dbg) StdStreams.vrb.println(" not possible");
							findReg(res);
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
						if (!isPowerOf2) findReg(res);	
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
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTGPR) && (instr1.getOperands()[1] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTFPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idPUTSPR) && (instr1.getOperands()[0] == res))
							// calls to some unsafe methods
							|| ((instr1.ssaOpcode == sCbranch) && ((res.type & 0x7fffffff) == tInteger)))
						// branches but not switches (the second operand of a switch is already constant)
					{
						StdConstant constant = (StdConstant)res.constant;
						if (res.type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if ((immValLong >= -32768) && (immValLong <= 32767)) {} else findReg(res);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {} else findReg(res);
						}
					} else if (((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idASM))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenPPC.idADR_OF_METHOD))) {
					} else {	// opd cannot be used as immediate opd	
						if (dbg) StdStreams.vrb.println(" not possible");
						findReg(res);	
					}	
				} else 	// opd has index != -1 or cannot be used as immediate opd	
					findReg(res);			
			} else {	// all other instructions
				if (res.reg < 0) 	// not yet assigned
					findReg(res);
			}
			if (dbg) StdStreams.vrb.println("\treg = " + res.reg);

			if (res.regGPR1 != -1) freeReg(gpr, res.regGPR1);
			if (res.regGPR2 != -1) freeReg(gpr, res.regGPR2);

			// free registers of operands if end of live range reached 
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join == null) {
						if ((opd.owner != null) && (opd.owner.ssaOpcode == sCloadLocal)) {
							if (CodeGen.paramRegEnd[opd.owner.result.index - maxOpStackSlots] <= i) freeReg(opd);
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
	}

	public static boolean isPowerOf2(long val) {
		return (val > 0) && (val & (val-1)) == 0;
	}

	private static void findReg(SSAValue res) {
		int type = res.type;
		if (type == tLong) {
			res.regLong = reserveReg(gpr, res.nonVol);
			res.reg = reserveReg(gpr, res.nonVol);
			useLongs = true;
		} else if ((type == tFloat) || (type == tDouble)) {
			res.reg = reserveReg(fpr, res.nonVol);
		} else if (type == tVoid) {
		} else {
			res.reg = reserveReg(gpr, res.nonVol);
		}
	}

	static int reserveReg(boolean isGPR, boolean isNonVolatile) {
		if (dbg) StdStreams.vrb.print("\tbefore reserving " + Integer.toHexString(regsGPR));
		if (isGPR) {
			int i;
			if (!isNonVolatile) {	// is volatile
				i = paramStartGPR;
				while (i <= volEndGPR) {
					if ((regsGPR & (1 << i)) != 0) {
						regsGPR &= ~(1 << i);	
						if (i-paramStartGPR+1 > nofVolGPR) nofVolGPR = i+1-paramStartGPR;
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
			if (dbg) StdStreams.vrb.print("\tnot enough GPR's, reserve stack slot");
			fullRegSetGPR = false;
			return getEmptyStackSlot(false);
		} else {	// FPR
			int i;
			if (!isNonVolatile) {	// is volatile
				i = paramStartFPR;
				while (i < nonVolStartFPR) {
					if ((regsFPR & (1 << i)) != 0) {
						regsFPR &= ~(1 << i);	
						if (i-paramStartFPR+1 > nofVolFPR) nofVolFPR = i+1-paramStartFPR;
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
			if (dbg) StdStreams.vrb.print("\tnot enough FPR's, reserve stack slot");
			fullRegSetFPR = false;
			return getEmptyStackSlot(true);
		}
	}

	private static int getEmptyStackSlot(boolean pair) {
		int i = 0;
		if (!pair) {
			while (i < 32) {
				if ((stackSlotSpilledRegs & (1 << i)) != 0) {
					stackSlotSpilledRegs &= ~(1 << i);
					if (i > maxLocVarStackSlots - 1) maxLocVarStackSlots = i + 1;
					return i + 0x100;
				}
				i++;
			}
		} else {
			while (i < 31) {
				if (((stackSlotSpilledRegs >>> i) & 3) == 3) {
					stackSlotSpilledRegs &= ~(3 << i);
					if (i > maxLocVarStackSlots - 1) maxLocVarStackSlots = i + 2;
					return i + 0x100;
				}
				i++;
			}	
		}
		ErrorReporter.reporter.error(605);
		assert false: "not enough stack slots for spilling";
		return 0;
	}
	
	private static void releaseStackSlot(int slot) {
		stackSlotSpilledRegs |= 1 << slot;
	}

	static void reserveReg(boolean isGPR, int regNr) {
		if (isGPR) {
			if (regNr < 0x100) regsGPR &= ~(1 << regNr);
			else stackSlotSpilledRegs &= ~(1 << (regNr - 0x100));
		} else {
			if (regNr < 0x100) regsFPR &= ~(1 << regNr);
			else stackSlotSpilledRegs &= ~(3 << (regNr - 0x100));
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
			if (reg < 0x100) {
			regsGPR |= 1 << reg;
			if (dbg) StdStreams.vrb.println("\tfree reg " + reg + "\tregsGPR=0x" + Integer.toHexString(regsGPR));
			} else {
				releaseStackSlot(reg - 0x100);
				if (dbg) StdStreams.vrb.println("\tfree stack slot " + reg);
			}
		} else {
			if (reg < 0x100) {
				regsFPR |= 1 << reg;
				if (dbg) StdStreams.vrb.println("\tfree reg " + reg + "\tregsFPR=0x" + Integer.toHexString(regsFPR));
			} else {
				releaseStackSlot(reg - 0x100);
				releaseStackSlot(reg + 1 - 0x100);
				if (dbg) StdStreams.vrb.println("\tfree stack slot " + (reg + 1));
			}
		}
	}

	// reset all previously assigned registers
	public static void resetRegisters() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			res.reg = -1;
			res.regLong = -1;
			res.regGPR1 = -1;
			res.regGPR2 = -1;
			SSAValue join = res.join;
			if (join != null) {
				join.reg = -1;
				join.regLong = -1;
			}
		}
	}
}
