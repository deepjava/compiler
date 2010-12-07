
package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

/**
 * register allocation
 * 
 * @author graf
 * 
 */
public class RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers {

	private static final int nofSSAInstr = 256;

	static int maxStackSlots;
	static int regsGPR, regsFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	private static int[] regAtIndex = new int[MachineCode.maxNofParam];
	
	// local and linear copy of all SSA-instructions of all nodes
	private static SSAInstruction[] instrs = new SSAInstruction[nofSSAInstr];	
	private static int nofInstructions;
	
	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	static void buildIntervals(SSA ssa) {
		maxStackSlots = ssa.cfg.method.maxStackSlots;
		regsGPR = volRegsGPRinitial | nonVolRegsGPRinitial;
		regsFPR = volRegsFPRinitial | nonVolRegsFPRinitial;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		for (int i = 0; i < regAtIndex.length; i++) regAtIndex[i] = -1;
		
		insertRegMoves(ssa);
		
		// copy into local array for faster access
		nofInstructions = SSA.renumberInstructions(ssa.cfg);
		if (nofInstructions > instrs.length) {
			instrs = new SSAInstruction[nofInstructions];
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
		
		resolvePhiFunctions();	
		calcLiveRange();	// compute live intervals
		assignRegType();	// assign volatile or nonvolatile type
	}

	// Inserts register moves for phi functions in case that opnd and 
	// result of phi function have different index
	private static void insertRegMoves(SSA ssa) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				SSAValue[] opds = phi.getOperands();
				SSAValue res = phi.result;
				if (res.index != opds[0].index && opds[0].index >= 0) {
					SSAValue[] newOpds = new SSAValue[opds.length];
//					for (int k = 0; k < b.nofPredecessors; k++) {
					for (int k = 0; k < opds.length; k++) {
						SSANode n = (SSANode)b.predecessors[k];
						SSAValue r = new SSAValue();
						r.type = opds[k].type;
						r.index = res.index;
						SSAInstruction move = new Monadic(sCregMove, opds[k]);
						move.result = r;
						n.addInstruction(move);
						n.exitSet[r.index] = r;
						newOpds[k] = r;
						phi.print(1);
					}
					phi.setOperands(newOpds);
				}
			}
			b = (SSANode) b.next;
		}	
	}

	// Resolve phi functions 
	private static void resolvePhiFunctions() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc) {
				((PhiFunction)instr).start = instr.result.n;
				SSAValue[] opds = instr.getOperands();
				SSAValue val = instr.result;
				while (val.join != null) val = val.join;
				for (SSAValue opd : opds) opd.join = val;
			} 
		}
	}

	// Computes the live ranges of all SSAValues 
	private static void calcLiveRange() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			int currNo = res.n;
			res.end = currNo;
			SSAValue[] opds = instr.getOperands();
			if (opds != null) { 
				for (SSAValue opd : opds) {
					if (opd.end < currNo) {
						opd.end = currNo;
						if (opd.join != null) {
							if (opd.end > opd.join.end) opd.join.end = opd.end;
						}
					}
				}
			}
			if (res.join != null) {
				int start = ((PhiFunction)instrs[res.join.n]).start;
				if (res.n < start) ((PhiFunction)instrs[res.join.n]).start = res.n;
				if (res.end > res.join.end) res.join.end = res.end;
			}
		}
	}

	// Assign volatile or nonvolatile register 
	private static void assignRegType() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			int currNo = instr.result.n;
			int endNo = instr.result.end;
			if (instr.ssaOpcode == sCPhiFunc) {
				// check if call instruction between start of phi-function and here
				for (int k = ((PhiFunction)instr).start+1; k < endNo; k++) {
					if ((scAttrTab[instrs[k].ssaOpcode] & (1 << ssaApCall)) != 0) {
						instr.result.nonVol = true;
						MachineCode.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
					}
				}
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start of method and here
				for (int k = 0; k < endNo; k++) {
					if ((scAttrTab[instrs[k].ssaOpcode] & (1 << ssaApCall)) != 0) {
						instr.result.nonVol = true;
						MachineCode.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
					}
				}
			} else {
				// check if call instruction in live range
				for (int k = currNo+1; k < endNo; k++) {
					if ((scAttrTab[instrs[k].ssaOpcode] & (1 << ssaApCall)) != 0)
						instr.result.nonVol = true;
				}
			}
		}
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 */
	static void assignRegisters(MachineCode code) {
		// handle loadLocal first
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue joinVal = instr.result.join;
			if (instr.ssaOpcode == sCloadLocal) {
				int type = instr.result.type;
				if (type == tLong) {
					instr.result.regLong = code.paramRegNr[instr.result.index - maxStackSlots];
					instr.result.reg = code.paramRegNr[instr.result.index+1 - maxStackSlots];
				} else if ((type == tFloat) || (type == tDouble)) {
					instr.result.reg = code.paramRegNr[instr.result.index - maxStackSlots];
				} else if (type == tVoid) {
				} else {
					instr.result.reg = code.paramRegNr[instr.result.index - maxStackSlots];
				}	
				if (joinVal != null) {
					if (instr.result.type == tLong) {
						joinVal.regLong = instr.result.regLong;
						joinVal.reg = instr.result.reg;
						regAtIndex[joinVal.index - maxStackSlots] = joinVal.regLong;
						regAtIndex[joinVal.index+1 - maxStackSlots] = joinVal.reg;
					} else {
						joinVal.reg = instr.result.reg;
						regAtIndex[joinVal.index - maxStackSlots] = joinVal.reg;			
					}
				}
			} 
		}
		
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
//			System.out.println("ssa opcode = " + instr.scMnemonics[instr.ssaOpcode] + " regsGPR="+Integer.toHexString(regsGPR));
			// reserve auxiliary register for this instruction
			int nofAuxReg = (scAttrTab[instr.ssaOpcode] >> 16) & 0xF;
			if (nofAuxReg == 4 && res.type == tLong) nofAuxReg = 2;
			else if ((nofAuxReg == 5 && res.type == tLong)
					|| (nofAuxReg == 6 && (res.type == tFloat)||(res.type == tDouble))) nofAuxReg = 1;
			if (nofAuxReg == 1)
				res.regAux1 = reserveReg(gpr, false);
			else if (nofAuxReg == 2) {
				res.regAux1 = reserveReg(gpr, false);
				res.regAux2 = reserveReg(gpr, false);
			}
			if (res.regAux1 != -1) freeReg(gpr, res.regAux1);
			if (res.regAux2 != -1) freeReg(gpr, res.regAux2);
//			System.out.println("at pos 1: regsGPR="+Integer.toHexString(regsGPR));

			// free registers of operands if end of live range reached 
			// and if operands are not of type long
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join != null) opd = opd.join;
					if (opd.end <= i && opd.reg > -1) {
						if (opd.type == tLong) {
						} else if ((opd.type == tFloat) || (opd.type == tDouble)) {
							freeReg(fpr, opd.reg);
						} else {
							freeReg(gpr, opd.reg);
						}
					}
				}
			}
			
			// reserve temporary storage on the stack for certain fpr operations
			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
				MachineCode.tempStorage = true;
//			System.out.println("temp storage = true");
			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
				MachineCode.tempStorage = true;

			// reserve register for result of instruction
			SSAValue joinVal = res.join;
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (joinVal != null) {
				if (joinVal.reg < 0) {	// phi function: register not assigned yet
//				System.out.println("join: assign register to phi-function");
					// check if other phi-function at same index
					if (regAtIndex[joinVal.index - maxStackSlots] > -1) {
//				System.out.println("bereits register bei diesem index " + joinVal.index);
						if (res.type == tLong) {
							res.regLong = regAtIndex[joinVal.index - maxStackSlots];
							res.reg = regAtIndex[joinVal.index+1 - maxStackSlots];
							joinVal.regLong = res.regLong;
							joinVal.reg = res.reg;
						} else if ((res.type == tFloat) || (res.type == tDouble)) {
							res.reg = reserveReg(fpr, res.nonVol);
							joinVal.reg = res.reg;
						} else if (res.type == tVoid) {
						} else {
							res.reg = regAtIndex[joinVal.index - maxStackSlots];
							joinVal.reg = res.reg;
//				System.out.println("register has no " + joinVal.reg);
						}
					} else {	// reserve register for phi-function						
						if (res.type == tLong) {
							joinVal.regLong = reserveReg(gpr, res.nonVol);
							joinVal.reg = reserveReg(gpr, res.nonVol);
							res.regLong = joinVal.regLong;
							res.reg = joinVal.reg;
							regAtIndex[joinVal.index - maxStackSlots] = joinVal.regLong;
							regAtIndex[joinVal.index+1 - maxStackSlots] = joinVal.reg;
						} else if ((res.type == tFloat) || (res.type == tDouble)) {
							joinVal.reg = reserveReg(fpr, res.nonVol);
							res.reg = joinVal.reg;
							regAtIndex[joinVal.index - maxStackSlots] = joinVal.reg;
						} else if (res.type == tVoid) {
						} else {
							joinVal.reg = reserveReg(gpr, res.nonVol);
							res.reg = joinVal.reg;
							regAtIndex[joinVal.index - maxStackSlots] = joinVal.reg;
							System.out.println("register reserved for phi function at "+joinVal.n+" reg = " + joinVal.reg);
						}
					}
				} else // assign same register as phi function
					// hier fehlt long
					res.reg = joinVal.reg;	
			} else if (instr.ssaOpcode == sCloadConst) {
				// check if operand is immediate
				SSAInstruction instr1 = instrs[res.end];
				boolean imm = (scAttrTab[instr1.ssaOpcode] & (1 << ssaApImmOpd)) != 0;
				if (imm && res.index < 0 && res.join == null) {
					if (((instr1.ssaOpcode == sCadd) && (res.type == tInteger || res.type == tLong))
							|| ((instr1.ssaOpcode == sCsub) && (res.type == tInteger || res.type == tLong))
							|| ((instr1.ssaOpcode == sCmul) && (res.type == tInteger ))
							|| (instr1.ssaOpcode == sCand)
							|| (instr1.ssaOpcode == sCor)
							|| (instr1.ssaOpcode == sCxor)
							|| (instr1.ssaOpcode == sCshl)
							|| (instr1.ssaOpcode == sCshr)
							|| (instr1.ssaOpcode == sCushr)
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETGPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETFPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETSPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTGPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTFPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTSPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCbranch) && (res.type == tInteger ))) {
						Constant constant = (Constant)res.constant;
						if (res.type == tLong) {
							long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
							if ((immValLong >= -32768) && (immValLong <= 32767)) {
							} else
								findReg(res);
						} else {	
							int immVal = constant.valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {
							} else 
								findReg(res);
						}
					} else {	// opd has index != -1 or cannot be used as immediate opd	
						findReg(res);			
					}
				} else 
					findReg(res);	
			} else {	// all other instructions
				if (res.reg < 0) 	// not yet assigned
					findReg(res);
			}

			// free registers of operands of type long if end of live range reached
			opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join != null) opd = opd.join;
					if (opd.end <= i && opd.reg > -1) {
						if (opd.type == tLong) {
							freeReg(gpr, opd.regLong);
							freeReg(gpr, opd.reg);
						}
					}
				}
			}
			// free registers of result if end of live range is current instruction
			if (res != null) {
				if (res.end == i && res.reg > -1) {	
					if (res.type == tLong) {
						freeReg(gpr, res.regLong);
						freeReg(gpr, res.reg);
					} else if ((res.type == tFloat) || (res.type == tDouble)) {
						freeReg(fpr, res.reg);
					} else {
						freeReg(gpr, res.reg);
					}
				}
			}
		}
		MachineCode.nofNonVolGPR = nofNonVolGPR;
		MachineCode.nofNonVolFPR = nofNonVolFPR;
	}

	private static void findReg(SSAValue res) {
		int type = res.type;
		if (type == tLong) {
			res.regLong = reserveReg(gpr, res.nonVol);
			res.reg = reserveReg(gpr, res.nonVol);
		} else if ((type == tFloat) || (type == tDouble)) {
			res.reg = reserveReg(fpr, res.nonVol);
		} else if (type == tVoid) {
		} else {
			res.reg = reserveReg(gpr, res.nonVol);
		}
	}

	static int reserveReg(boolean isGPR, boolean isNonVolatile) {
		int regs;
		if (isGPR) {
			if (isNonVolatile) {
				regs = regsGPR & nonVolRegsGPRinitial;			
				int i = topGPR;
				while (regs != 0) {
					if ((regs & (1 << i)) != 0) {
						regsGPR &= ~(1 << i);
						if (nofGPR - i > nofNonVolGPR) nofNonVolGPR = nofGPR - i;
//	check if ok			
						return i;
					}
					i--;
				}
				assert false: "not enough registers for nonvolatile GPRs";
				return 0;
			} else {
				regs = regsGPR & volRegsGPRinitial;
				int i = 0;
				while (regs != 0) {
					if ((regs & 1) != 0) {
						regsGPR &= ~(1 << i);
//						System.out.println("\tregsGPR is now = " + Integer.toHexString(regsGPR));
						return i;
					}
					regs /= 2;
					i++;
				}
				assert false: "not enough registers for volatile GPRs";
				return 0;
			}
		} else {
			if (isNonVolatile) {
				regs = regsFPR & nonVolRegsFPRinitial;			
				int i = topFPR;
				while (regs != 0) {
					if ((regs & (1 << i)) != 0) {
						regsFPR &= ~(1 << i);
						if (nofFPR - i > nofNonVolFPR) nofNonVolFPR = nofFPR - i;
						return i;
					}
					i--;
				}
				assert false: "not enough registers for nonvolatile FPRs";
				return 0;
			} else {
				regs = regsFPR & volRegsFPRinitial;
				int i = 0;
				while (regs != 0) {
					if ((regs & 1) != 0) {
						regsFPR &= ~(1 << i);
						return i;
					}
					regs /= 2;
					i++;
				}
				assert false: "not enough registers for volatile FPRs";
				return 0;
			}		
		}
	}

	static void reserveReg(boolean isGPR, int regNr) {
		if (isGPR) {
			regsGPR &= ~(1 << regNr);
		} else {
			regsFPR &= ~(1 << regNr);
		}
	}

	private static void freeReg(boolean isGPR, int reg) {
		if (isGPR) {
			regsGPR |= 1 << reg;
		} else {
			regsFPR |= 1 << reg;
		}
	}

}