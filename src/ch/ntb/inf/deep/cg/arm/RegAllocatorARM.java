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

package ch.ntb.inf.deep.cg.arm;

import ch.ntb.inf.deep.cg.CodeGen;
import ch.ntb.inf.deep.cg.RegAllocator;
import ch.ntb.inf.deep.cg.arm.CodeGenARM;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAInstructionMnemonics;
import ch.ntb.inf.deep.ssa.SSAInstructionOpcs;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.SSAValueType;
import ch.ntb.inf.deep.ssa.instruction.Call;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;
import ch.ntb.inf.deep.ssa.instruction.NoOpnd;

public class RegAllocatorARM extends RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers, ICclassFileConsts {

	// used to indicate used and free GPRs and EXTRs, the EXTRs must be differentiated between D0..D31 and S0..S31
	public static int regsGPR, regsEXTRD, regsEXTRS;

	/**
	 * Assign a register or memory location to all SSAValues
	 * finally, determine how many parameters are passed on the stack
	 * checks if user wants to use floats in exception handlers
	 * checks if temporary space on stack is necessary
	 */
	static void assignRegisters() {
		// used to find call in this method with most parameters -> gives stack size
		int maxNofParamGPR = 0, maxNofParamEXTR = 0;

		if (dbg) StdStreams.vrb.println("\nallocate registers for " + ssa.cfg.method.name);
		if (dbg) {
			StdStreams.vrb.println("local variable table");
			LocalVar[] lvTable = ssa.localVarTab;
			if (lvTable != null) {
				for (int i = 0; i < lvTable.length; i++) {
					LocalVar lv = lvTable[i];
					while (lv != null) {	// locals occupying the same slot are linked by field "next"
						StdStreams.vrb.println(lv.toString());
						lv = (LocalVar) lv.next;
					}
				}
			} 
		}
		

		// handle loadLocal first, these values are parameters
		if (dbg) StdStreams.vrb.println("assign registers");
		if (dbg) StdStreams.vrb.println("\thandle load locals first:");
		LocalVar[] lvTable = ssa.localVarTab;
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (instr.ssaOpcode == sCloadLocal) {
				if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
				if (((NoOpnd)instr).firstInCatch) {
					// if the variable of type Exception in a catch clause is loaded for further use,
					// it will be passed as a parameter in a prefixed register
					res.reg = reserveReg(gpr, true, false);
					continue;
				}
				LocalVar lv = null;
				if (lvTable != null) lv = lvTable[res.index - maxOpStackSlots];
				int type = res.type;
				if (type == tLong) {
					res.regLong = CodeGenARM.paramRegNr[res.index - maxOpStackSlots];
					res.reg = CodeGenARM.paramRegNr[res.index+1 - maxOpStackSlots];
					if (lv != null) {
						lv.ssaInstrStart = instrs[0];
						lv.ssaInstrEnd = instrs[res.end];
						lv.startRange(instrs[0], null, res.regLong, res.reg);
						lv.endRange(instrs[res.end]);
						if (dbg) StdStreams.vrb.println("\tset range of lv " + lv.toString());
					}
				} else if (type == tVoid) {
				} else {	// any other type such as int, float or double
					res.reg = CodeGenARM.paramRegNr[res.index - maxOpStackSlots];
					if (lv != null) {
						lv.ssaInstrStart = instrs[0];
						lv.ssaInstrEnd = instrs[res.end];
						lv.startRange(instrs[0], null, res.reg);
						lv.endRange(instrs[res.end]);
						if (dbg) StdStreams.vrb.println("\tset range of lv " + lv.toString());
					}
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
					if ((((Method)call.item).id) == CodeGenARM.idENABLE_FLOATS) {
					CodeGenARM.enFloatsInExc = true;
				}
//				int id = ((Method)call.item).id;
//				if (id == CodeGenARM.idDoubleToBits || (id == CodeGenARM.idBitsToDouble) ||  // DoubleToBits or BitsToDouble
//					id == CodeGenARM.idFloatToBits || (id == CodeGenARM.idBitsToFloat))  // FloatToBits or BitsToFloat
//					CodeGenARM.tempStorage = true;
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
				if (dbg) {if (instr1 != null) StdStreams.vrb.println("\t\tfree registers for phi-functions of last node");}
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
			int nofAuxRegGPR = (scAttrTab[instr.ssaOpcode] >> 20) & 0xF;
			if (nofAuxRegGPR == 4) {	// scDiv
				if ((res.type & ~(1<<ssaTaFitIntoInt)) == tInteger) {nofAuxRegGPR = 1;}
				else if ((res.type & ~(1<<ssaTaFitIntoInt)) == tLong) {
					if (fullRegSetGPR) {fullRegSetGPR = false; nofNonVolGPR = topGPR - nonVolStartGPR + 1;}
					else nofAuxRegGPR = 0;
				}
			} else if (nofAuxRegGPR == 5) {	// scRem
				if ((res.type & ~(1<<ssaTaFitIntoInt)) == tInteger || (res.type & ~(1<<ssaTaFitIntoInt)) == tLong) {
					if (fullRegSetGPR) {fullRegSetGPR = false; nofNonVolGPR = topGPR - nonVolStartGPR + 1;}
					else nofAuxRegGPR = 0;
				}
			} else if (nofAuxRegGPR == 8) {	// modulo division
				if (res.type == tLong) {nofAuxRegGPR = 2;}
				else if (res.type == tFloat || res.type == tDouble) nofAuxRegGPR = 1;
			}
			
			if (nofAuxRegGPR == 1) res.regGPR1 = reserveReg(gpr, false, false);
			else if (nofAuxRegGPR == 2) {
				res.regGPR1 = reserveReg(gpr, false, false);
				if (dbg) if (res.regGPR1 != -1) StdStreams.vrb.print("\t\tauxReg1 = " + res.regGPR1);
				res.regGPR2 = reserveReg(gpr, false, false);
				if (dbg) if (res.regGPR2 != -1) StdStreams.vrb.print("\t\tauxReg2 = " + res.regGPR2);
			}
			
			// reserve register for result of instruction
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (res.join != null) {
				SSAValue joinVal = res.join;
				if (dbg) StdStreams.vrb.println("\t\tjoinVal != null");
				if (joinVal.reg < 0) {	// join: register not assigned yet
					if (res.type == tLong) {
						joinVal.regLong = reserveReg(gpr, joinVal.nonVol, false);
						joinVal.reg = reserveReg(gpr, joinVal.nonVol, false);
						res.regLong = joinVal.regLong;
						res.reg = joinVal.reg;
					} else if ((res.type == tFloat) || (res.type == tDouble)) {
						joinVal.reg = reserveReg(extr, joinVal.nonVol, res.type == tFloat);
						res.reg = joinVal.reg;
					} else if (res.type == tVoid) {
					} else {
						joinVal.reg = reserveReg(gpr, joinVal.nonVol, false);
						res.reg = joinVal.reg;
					}
				} else {// assign same register as phi function
					if (dbg) StdStreams.vrb.println("\t\tassign same reg as join val");
					if (res.type == tLong) {
						res.regLong = joinVal.regLong;	
						res.reg = joinVal.reg;
						reserveReg(gpr, res.regLong, false);
						reserveReg(gpr, res.reg, false);
					} else if ((res.type == tFloat) || (res.type == tDouble)) {
						res.reg = joinVal.reg;
						reserveReg(extr, res.reg, res.type == tFloat);
					} else if (res.type == tVoid) {
						res.reg = joinVal.reg;
						reserveReg(gpr, res.reg, false);
					} else {
						res.reg = joinVal.reg;
						reserveReg(gpr, res.reg, false);
					}
				}
			} else if (instr.ssaOpcode == sCloadConst) {
				// check if operand can be used with immediate instruction format
				SSAInstruction instr1 = instrs[res.end];
				boolean imm = (scAttrTab[instr1.ssaOpcode] & (1 << ssaApImmOpd)) != 0;
				if (imm && res.index < maxOpStackSlots && res.join == null) {
					if (dbg) StdStreams.vrb.print("\t\timmediate");
					// opd must be used in an instruction with immediate form available
					// and opd must not be already in a register 
					// and opd must have join == null
					int type = res.type & 0x7fffffff;
					if ((instr1.ssaOpcode == sCadd) && (type == tInteger)) {
						StdConstant constant = (StdConstant)res.constant;
						int immVal = constant.valueH;
						if (immVal < 0) immVal = -immVal;
						int lead = Integer.numberOfLeadingZeros(immVal);
						lead -= lead % 2;	// make even, immediate operands can be shifted by an even number only
						if (lead + Integer.numberOfTrailingZeros(immVal) < 24) findReg(res);	
					} else if ((instr1.ssaOpcode == sCsub) && (type == tInteger)) {
							StdConstant constant = (StdConstant)res.constant;
							int immVal = constant.valueH;
							if (immVal < 0) immVal = -immVal;
							int lead = Integer.numberOfLeadingZeros(immVal);
							lead -= lead % 2;	// make even, immediate operands can be shifted by an even number only
							if (lead + Integer.numberOfTrailingZeros(immVal) < 24) findReg(res);
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
						} else {
							findReg(res);
						}
					} else if (((instr1.ssaOpcode == sCdiv)||(instr1.ssaOpcode == sCrem)) && ((type == tInteger)||(type == tLong)) && (res == instr1.getOperands()[1])) {
						// if type == iTnteger and const is divisor -> use immediate
						// if type == iLong and const is divisor and positive and a power of 2 -> use immediate
						StdConstant constant = (StdConstant)res.constant;
						if (type == tLong) {
							long immVal = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if (!isPowerOf2(immVal)) findReg(res);
						} 		
					} else if ((instr1.ssaOpcode == sCand)
							|| (instr1.ssaOpcode == sCor)
							|| (instr1.ssaOpcode == sCxor)
							// logical operators with integer and long
							|| (instr1.ssaOpcode == sCshl) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCshr) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCushr) && (res == instr1.getOperands()[1])
							// shift operators only if immediate is shift distance (and not value to be shifted)
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idGETGPR))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idGETEXTRD))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idGETEXTRS))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idGETCPR))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idPUTGPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idPUTEXTRD) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idPUTEXTRS) && (instr1.getOperands()[0] == res))
//							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idPUTCPR) && (instr1.getOperands()[0] == res))
							// calls to some unsafe methods
							|| ((instr1.ssaOpcode == sCbranch) && ((res.type & 0x7fffffff) == tInteger)))
						// branches but not switches (the second operand of a switch is already constant)
					{
						StdConstant constant = (StdConstant)res.constant;
						if (res.type == tLong) {
							long immVal = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if ((immVal >= 0) && (immVal <= 255)) {} else findReg(res);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= 0) && (immVal <= 255)) {} else findReg(res);
						}
					} else if (((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idASM))
							|| ((instr1.ssaOpcode == sCcall) && (((Method)((Call)instr1).item).id == CodeGenARM.idADR_OF_METHOD))) {
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
			if (dbg) StdStreams.vrb.println("\t\treg = " + res.reg);

			if (res.regGPR1 != -1) freeReg(gpr, res.regGPR1, false);
			if (res.regGPR2 != -1) freeReg(gpr, res.regGPR2, false);

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
				int gpr = 0, extr = 0;
				for (SSAValue opd : opds) {
					int type = opd.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) gpr += 2;
					else if (type == tFloat || type == tDouble) extr++;
					else gpr++;
				}
				if (gpr > maxNofParamGPR) maxNofParamGPR = gpr; 
				if (extr > maxNofParamEXTR) maxNofParamEXTR = extr; 
			}
			
			// if result of instruction is local variable -> record register ranges
			int lvIndex = res.index - maxOpStackSlots;
			if (lvIndex >= 0 && lvTable != null && lvIndex < lvTable.length) {	// there are rare cases where the java compiler generates bytecode with local variables but leaves the table empty (no source)!
				LocalVar lv = lvTable[lvIndex];
				while (lv != null && (lv.startPc + lv.length) < instr.bca) lv = (LocalVar) lv.next;	// choose active lv for this instruction
				if (lv != null) {
					if (dbg) StdStreams.vrb.println("\t\tres is lv "+ lv.toString());
					SSAInstruction start = instrs[i+1];
					if (lv.ssaInstrStart == null) {	// start of lv range
						if (dbg) StdStreams.vrb.println("\t\tstart first lv range");
						lv.ssaInstrStart = start;
						if (res.type == tLong) lv.startRange(start, null, res.regLong, res.reg);
						else lv.startRange(start, null, res.reg);
					} else {
						if (lv.curr.reg != res.reg) {	// start new lv range if register changed
							if (dbg) StdStreams.vrb.println("\t\tstart new lv range");
							if (res.type == tLong) lv.startRange(start, instr, res.regLong, res.reg);
							else lv.startRange(start, instr, res.reg);
						}
					}
					lv.ssaInstrEnd = instrs[res.end];
					lv.endRange(instrs[res.end]);
				}
			}
		}
		CodeGenARM.nofNonVolGPR = nofNonVolGPR;
		CodeGenARM.nofNonVolFPR = nofNonVolFPR;
		CodeGenARM.nofVolGPR = nofVolGPR;
		CodeGenARM.nofVolFPR = nofVolFPR;
		int nof = maxNofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) CodeGenARM.callParamSlotsOnStack += nof;
		nof = maxNofParamEXTR - (paramEndEXTR - paramStartEXTR + 1);
		if (nof > 0) CodeGenARM.callParamSlotsOnStack += nof*2;	// reserve 2 stack slots regardless if type is float or double
		
		if (dbg) {
			StdStreams.vrb.println("\nLocal Variable Table");
			if (lvTable != null) {
				for (int i = 0; i < lvTable.length; i++) {
					LocalVar lv = lvTable[i];
					while (lv != null) {	// locals occupying the same slot are linked by field "next"
						StdStreams.vrb.println(lv.toString());
						lv = (LocalVar) lv.next;
					}
				}
			}
		}
	}

	public static boolean isPowerOf2(long val) {
		return (val > 0) && (val & (val-1)) == 0;
	}

	private static void findReg(SSAValue res) {
		int type = res.type;
		if (type == tLong) {
			res.regLong = reserveReg(gpr, res.nonVol, false);
			res.reg = reserveReg(gpr, res.nonVol, false);
			useLongs = true;
		} else if ((type == tFloat) || (type == tDouble)) {
			res.reg = reserveReg(extr, res.nonVol, type == tFloat);
		} else if (type == tVoid) {
		} else {
			res.reg = reserveReg(gpr, res.nonVol, false);
		}
	}

	static int reserveReg(boolean isGPR, boolean isNonVolatile, boolean single) {
		if (isGPR) {
			if (dbg) StdStreams.vrb.print("\t\tbefore reserving " + Integer.toHexString(regsGPR));
			int i;
			if (!isNonVolatile) {	// is volatile
				i = paramStartGPR;
				while (i <= volEndGPR) {
					if ((regsGPR & (1 << i)) != 0) {
						regsGPR &= ~(1 << i);	
						if (i - paramStartGPR + 1 > nofVolGPR) nofVolGPR = i + 1 - paramStartGPR;
						return i;
					}
					i++;
				}
			} 
			i = topGPR;
			while (i >= nonVolStartGPR) {
				if ((regsGPR & (1 << i)) != 0) {
					regsGPR &= ~(1 << i);
					if (topGPR + 1 - i > nofNonVolGPR) nofNonVolGPR = topGPR + 1 - i;
					return i;
				}
				i--;
			}
			if (dbg) StdStreams.vrb.print("\t\tnot enough GPR's, reserve stack slot");
			fullRegSetGPR = false;
			return getEmptyStackSlot(false);
		} else {	// EXTR
			if (dbg) StdStreams.vrb.print("\t\tbefore reserving regsEXTRD:" + Integer.toHexString(regsEXTRD) + " regsEXTRS:" + Integer.toHexString(regsEXTRS));
			int i;
			if (!isNonVolatile) {	// is volatile
				if (single) {
					i = paramStartEXTR;
					while (i < nonVolStartEXTR * 2) {
						if ((regsEXTRS & (1 << i)) != 0) {
							regsEXTRS &= ~(1 << i);	
							regsEXTRD &= ~(1 << (i/2));	// mark double precision register as well	
							if (i - paramStartEXTR + 1 > nofVolFPR) nofVolFPR = i + 1 - paramStartEXTR;
							return i;
						}
						i++;						
					}
				} else {
					i = paramStartEXTR;
					while (i < nonVolStartEXTR) {
						if ((regsEXTRD & (1 << i)) != 0) {
							regsEXTRD &= ~(1 << i);	
							regsEXTRS &= ~(3 << (i*2));	// mark single precision registers as well	
							if (i - paramStartEXTR + 1 > nofVolFPR) nofVolFPR = i + 1 - paramStartEXTR;
							return i;
						}
						i++;
					}				
				}
			} 
			i = topEXTR;
			if (single) {
				while (i >= nonVolStartEXTR * 2) {
					if ((regsEXTRS & (1 << i)) != 0) {
						regsEXTRS &= ~(1 << i);
						regsEXTRD &= ~(1 << (i/2));	// mark double precision register as well	
						if (nofEXTR - i > nofNonVolFPR) nofNonVolFPR = nofEXTR - i;
						return i;
					}
					i--;
				}							
			} else {
				while (i >= nonVolStartEXTR) {
					if ((regsEXTRD & (1 << i)) != 0) {
						regsEXTRD &= ~(1 << i);
						if (i < 16) regsEXTRS &= ~(3 << (i*2));	// mark single precision registers as well	
						if (nofEXTR - i > nofNonVolFPR) nofNonVolFPR = nofEXTR - i;
						return i;
					}
					i--;
				}			
			}

			if (dbg) StdStreams.vrb.print("\t\tnot enough FPR's, reserve stack slot");
			fullRegSetFPR = false;
			if (single) return getEmptyStackSlot(false);
			else return getEmptyStackSlot(true);
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

	static void reserveReg(boolean isGPR, int regNr, boolean single) {
		if (isGPR) {
			if (regNr < 0x100) regsGPR &= ~(1 << regNr);
			else stackSlotSpilledRegs &= ~(1 << (regNr - 0x100));
		} else {
			if (single) {
				if (regNr < 0x100) {
					regsEXTRS &= ~(1 << regNr);
					regsEXTRD &= ~(1 << (regNr/2));	// mark double precision register as well
				} else stackSlotSpilledRegs &= ~(1 << (regNr - 0x100));				
			} else {
				if (regNr < 0x100) {
					regsEXTRD &= ~(1 << regNr);
					regsEXTRS &= ~(3 << (regNr*2));	// mark single precision registers as well
				} else stackSlotSpilledRegs &= ~(3 << (regNr - 0x100));				
			}
		}
	}

	private static void freeReg(SSAValue val) {
		if (val.type == tLong) {
			freeReg(gpr, val.regLong, false);
			freeReg(gpr, val.reg, false);
		} else if ((val.type == tFloat) || (val.type == tDouble)) {
			freeReg(extr, val.reg, val.type == tFloat);
		} else {
			freeReg(gpr, val.reg, false);
		}
	}

	private static void freeReg(boolean isGPR, int reg, boolean single) {
		if (isGPR) {
			if (reg < 0x100) {
			regsGPR |= 1 << reg;
			if (dbg) StdStreams.vrb.println("\t\tfree reg " + reg + "\tregsGPR=0x" + Integer.toHexString(regsGPR));
			} else {
				releaseStackSlot(reg - 0x100);
				if (dbg) StdStreams.vrb.println("\t\tfree stack slot " + reg);
			}
		} else {
			if (single) {
				if (reg < 0x100) {
					regsEXTRS |= 1 << reg;
					int r = reg % 2 == 0 ? reg + 1 : reg - 1;
					if ((regsEXTRS & (1 << r)) != 0) {
						regsEXTRD |= 1 << (reg/2);	// release double precision register as well
					}
					if (dbg) StdStreams.vrb.println("\t\tfree reg " + reg + "\tregsEXTRD=0x" + Integer.toHexString(regsEXTRD) + " regsEXTRS=0x" + Integer.toHexString(regsEXTRS));
				} else {
					releaseStackSlot(reg - 0x100);
					if (dbg) StdStreams.vrb.println("\t\tfree stack slot " + reg);
				}			
			} else {
				if (reg < 0x100) {
					regsEXTRD |= 1 << reg;
					regsEXTRS |= 3 << (reg*2);	// release single precision registers as well
					if (dbg) StdStreams.vrb.println("\t\tfree reg " + reg + "\tregsEXTRD=0x" + Integer.toHexString(regsEXTRD) + " regsEXTRS=0x" + Integer.toHexString(regsEXTRS));
				} else {
					releaseStackSlot(reg - 0x100);
					releaseStackSlot(reg + 1 - 0x100);
					if (dbg) StdStreams.vrb.println("\t\tfree stack slot " + (reg + 1));
				}			
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
