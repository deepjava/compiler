package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;

/**
 * register allocation
 * 
 * @author graf
 * 
 */
public class RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers {

	private static final int nofSSAInstr = 256;

	static int regsGPR, regsFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	
	// local and linear copy of all SSA-instructions of all nodes
	private static SSAInstruction[] instrs = new SSAInstruction[nofSSAInstr];	
	private static int nofInstructions;
	
	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	static void buildIntervals(SSA ssa) {
		regsGPR = volRegsGPRinitial | nonVolRegsGPRinitial;
		regsFPR = volRegsFPRinitial | nonVolRegsFPRinitial;
		
//		insertRegMoves(ssa);	// nicht mehr nötig
		SSA.renumberInstructions(ssa.cfg);
		nofInstructions = 64; // ANzahl noch bestimmen
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
		nofInstructions = count;
		
		resolvePhiFunctions();	
		calcLiveRange();	// compute live intervals
		assignRegType();	// compute live intervals
	}

	/**
	 * Inserts register moves for phi functions in case that ....
	 */
	private static void insertRegMoves(SSA ssa) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				SSAValue[] opds = phi.getOperands();
				SSAValue res = phi.result;
				if (res.index != opds[0].index && opds[0].index >= 0) {
					SSAValue[] newOpds = new SSAValue[opds.length];
					for (int k = 0; k < b.nofPredecessors; k++) {
						SSANode n = (SSANode)b.predecessors[k];
						SSAValue r = new SSAValue();
						r.type = opds[k].type;
						r.index = res.index;
						SSAInstruction move = new Monadic(sCregMove, opds[k]);
						move.result = r;
						n.addInstruction(move);
						n.exitSet[b.maxStack - 1 + r.index] = r;
						newOpds[k] = r;
						phi.print(1);
					}
					phi.setOperands(newOpds);
				}
			}
			b = (SSANode) b.next;
		}	
	}

	/**
	 * Resolve phi functions 
	 */
	private static void resolvePhiFunctions() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc) {
				SSAValue[] opds = instr.getOperands();
				for (SSAValue opd : opds) opd.join = instr.result;
			} 
		}
	}

	/**
	 * Computes the live ranges of all SSAValues 
	 */
	private static void calcLiveRange() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			int currNo = instr.result.n;
			instr.result.end = currNo;
			SSAValue[] opds = instr.getOperands();
			if (opds != null) { 
				for (SSAValue opd : opds) {
						opd.end = currNo;
				}
			}
		}
	}

	/**
	 * Assign volatile or nonvolatile register 
	 */
	private static void assignRegType() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			int currNo = instr.result.n;
			int endNo = instr.result.end;
			if (instr.ssaOpcode == sCPhiFunc) {
				SSAValue[] opds = instr.getOperands();
				for (SSAValue opd : opds) opd.end = currNo;
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start and here
				for (int k = 0; k < endNo; k++) {
					if ((scAttrTab[instrs[k].ssaOpcode] & (1 << ssaApCall)) != 0) {
						instr.result.nonVol = true;
						MachineCode.paramHasNonVolReg[instr.result.index] = true;
					}
				}
			} else {
				// check if call instruction in live range
				for (int k = currNo; k < endNo; k++) {
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
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			// reserve auxiliary register for this instruction
			switch (instr.ssaOpcode) {
			case sCloadConst: case sCcall: case sCloadFromField: case sCstoreToField:
				instr.result.regAux1 = reserveReg(gpr, false);
				break;
			case sCloadFromArray: case sCstoreToArray:
				instr.result.regAux1 = reserveReg(gpr, false);
				instr.result.regAux2 = reserveReg(gpr, false);
				break;
			default:
				break;
			}
			if (instr.result.regAux1 != -1) freeVolReg(gpr, instr.result.regAux1);
			if (instr.result.regAux2 != -1) freeVolReg(gpr, instr.result.regAux2);

			// free registers of operands if end of live range reached
			SSAValue[] opds = instr.getOperands();
			SSAValue res = instr.result;
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.end <= i) {
						if (opd.type == tLong) {
							freeVolReg(gpr, opd.reg);
							freeVolReg(gpr, opd.regLong);
						} else if ((opd.type == tFloat) || (opd.type == tDouble)) {
							freeVolReg(fpr, opd.reg);
						} else {
							freeVolReg(gpr, opd.reg);
						}
					}
				}
			}

			// reserve register for result of instruction
			SSAValue joinVal = instr.result.join;
			if (instr.ssaOpcode == sCloadLocal) {
				int type = instr.result.type;
				if (type == tLong) {
					instr.result.reg = code.paramRegNr[instr.result.index];
					instr.result.regLong = code.paramRegNr[instr.result.index+1];
				} else if ((type == tFloat) || (type == tDouble)) {
					instr.result.reg = code.paramRegNr[instr.result.index];
				} else if (type == tVoid) {
				} else {
					instr.result.reg = code.paramRegNr[instr.result.index];
				}	
				if (joinVal != null) {
					joinVal.reg = instr.result.reg;
					joinVal.regLong = instr.result.regLong;
				}
			} else if (joinVal != null) {
				System.out.println("join != null");
				if (joinVal.reg < 0) {	// not yet assigned
					if (instr.result.type == tLong) {
						joinVal.reg = reserveReg(gpr, instr.result.nonVol);
						joinVal.regLong = reserveReg(gpr, instr.result.nonVol);
						instr.result.reg = joinVal.reg;
						instr.result.regLong = joinVal.regLong;
					} else if ((instr.result.type == tFloat) || (instr.result.type == tDouble)) {
						joinVal.reg = reserveReg(fpr, instr.result.nonVol);
						instr.result.reg = joinVal.reg;
					} else if (instr.result.type == tVoid) {
					} else {
						joinVal.reg = reserveReg(gpr, instr.result.nonVol);
						instr.result.reg = joinVal.reg;
					}
				} else 
					instr.result.reg = joinVal.reg;
			} else if (instr.ssaOpcode == sCloadConst)  {
				if (instr.result.index < 0) {
					// check if operand is immediate
					if ((instr.result.type == tInteger) || (instr.result.type == tLong)) {
						Object obj = instr.result.constant;
						long immValLong;
						if (obj instanceof Long) immValLong = (Long)obj;	
						else immValLong = (Integer)obj;
						if ((immValLong >= -32768) && (immValLong <= 32767)) {
							// constant used for ssa instruction further down
							SSAValue val = instr.result;
							SSAInstruction instr1 = instrs[val.end];
							switch (instr1.ssaOpcode) {	// instruction, where the const is used
							case sCadd:
							case sCand:
							case sCor:
							case sCxor:
							case sCsub:
							case sCmul:
							case sCbranch:
								break;
							default:
								instr.result.reg = reserveReg(gpr, instr.result.nonVol);
							}
						}
					}
				} else {	
					int type = instr.result.type;
					if (type == tLong) {
						instr.result.reg = reserveReg(gpr, instr.result.nonVol);
						instr.result.regLong = reserveReg(gpr, instr.result.nonVol);
					} else if ((type == tFloat) || (type == tDouble)) {
						instr.result.reg = reserveReg(fpr, instr.result.nonVol);
					} else if (type == tVoid) {
					} else {
						instr.result.reg = reserveReg(gpr, instr.result.nonVol);
					}			
				}
			} else {
				if (instr.result.reg < 0) {	// not yet assigned
					int type = instr.result.type;
					if (type == tLong) {
						instr.result.reg = reserveReg(gpr, instr.result.nonVol);
						instr.result.regLong = reserveReg(gpr, instr.result.nonVol);
					} else if ((type == tFloat) || (type == tDouble)) {
						instr.result.reg = reserveReg(fpr, instr.result.nonVol);
					} else if (type == tVoid) {
					} else {
						instr.result.reg = reserveReg(gpr, instr.result.nonVol);
					}
				}
			}

			// free registers of result if end of live range is current instruction
			if (res != null) {
				if (res.end == i) {
					if (res.type == tLong) {
						freeVolReg(gpr, res.reg);
						freeVolReg(gpr, res.regLong);
					} else if ((res.type == tFloat) || (res.type == tDouble)) {
						freeVolReg(fpr, res.reg);
					} else {
						freeVolReg(gpr, res.reg);
					}
				}
			}

		}
		MachineCode.nofNonVolGPR = nofNonVolGPR;
		MachineCode.nofNonVolFPR = nofNonVolFPR;
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

	private static void freeVolReg(boolean isGPR, int reg) {
		int mask; 	// ??????????????
		if (isGPR) {
			mask = (1 << reg) & volRegsGPRinitial;
			regsGPR |= 1 << reg;
		} else {
			mask = (1 << reg) & volRegsFPRinitial;
			regsFPR |= 1 << reg;
		}
	}

}