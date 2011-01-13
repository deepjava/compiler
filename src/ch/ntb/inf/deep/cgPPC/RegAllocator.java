
package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

/**
 * register allocation
 * 
 * @author graf
 * 
 */
public class RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers, ICclassFileConsts {
	private static final boolean dbg = false;

	private static final int nofSSAInstr = 256;
	
	static int maxStackSlots;
	static int regsGPR, regsFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	
	// used for resolving phi-functions
	private static int[] regAtIndex = new int[64];
	
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
		
		nofInstructions = SSA.renumberInstructions(ssa.cfg);
		findLastNodeOfPhi(ssa);
		
		// copy into local array for faster access
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
		calcLiveRange(ssa);	// compute live intervals
		assignRegType();	// assign volatile or nonvolatile type
	}

	private static void findLastNodeOfPhi(SSA ssa) {
		if (dbg) System.out.println("determine end of range for phi functions in loop headers");
		SSANode b = (SSANode) ssa.cfg.rootNode;
		int last;
		while (b != null) {
			last = 0;
			if (b.isLoopHeader()) {
				// set variable last to last instruction of this node
				SSAValue val = b.instructions[b.nofInstr - 1].result;
				if (val != null) last = val.n;
				// check if predecessor is further down the code and has set last to a higher value
				CFGNode[] pred = b.predecessors;
				for (int i = 0; i < b.nofPredecessors; i++) {
					SSANode n = (SSANode)pred[i];
					val = n.instructions[n.nofInstr - 1].result;
					if (val != null) {
						int end = val.n;
						if (end > last) last = end;
					}
				}
				for (int i = 0; i < b.nofPhiFunc; i++) {
					PhiFunction phi = b.phiFunctions[i];
					phi.last = last;
				}
			}
			b = (SSANode) b.next;
		}			
	}

	// Resolve phi functions 
	private static void resolvePhiFunctions() {
		if (dbg) System.out.println("resolving phi functions");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
//			if (dbg) System.out.println("\t resolve at " + instr.result.n);
			SSAValue[] opds = instr.getOperands();
			if (instr.ssaOpcode != sCPhiFunc && opds != null) {
				for (SSAValue opd : opds) {
//					if (opd.n < 0) continue;
					SSAInstruction opdInstr = instrs[opd.n];
					if (opdInstr.ssaOpcode == sCPhiFunc) {	// iterativ machen!!!
						PhiFunction phi = (PhiFunction)opdInstr;
						if (phi.deleted) {
							phi.used = true;
							SSAValue delPhiOpd = phi.getOperands()[0];
							opdInstr = instrs[delPhiOpd.n];
							if (opdInstr.ssaOpcode == sCPhiFunc) {	// iterativ machen!!!
								phi = (PhiFunction)opdInstr;
								if (phi.deleted) {
									phi.used = true;
								}
							}
						}
					}
				}	
			} 
		}
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc) {
				PhiFunction phi = (PhiFunction)instr;
				((PhiFunction)instr).start = instr.result.n;		// schon hier???????
				SSAValue[] opds = instr.getOperands();
				SSAValue res = instr.result;
//		if (dbg) System.out.println("instr : " + res.n);
				//					while (res.join != null) res = res.join;
				for (SSAValue opd : opds) {
//		if (dbg) System.out.println("\topd : " + opd.n);
					if (phi.deleted && opd.n == res.n) continue;
					if (phi.deleted && !phi.used ) continue;
					if (phi.deleted && instrs[opd.n].ssaOpcode != sCPhiFunc && opd.n > res.n) continue;
					if (instrs[opd.n].ssaOpcode != sCPhiFunc)	// opd is regular SSA instruction
						if (opd.join == null) {
							opd.join = res;	// opd now points to phi function
//							res.type = opd.type;
						} else {	//opd already points to other phi function
							SSAValue val = opd;
							while (val.join != null) val = val.join;
							if (val != res) res.join = val; // no circle
						}
					else {	// opd of phi function is phi function again
						SSAValue val = opd;
						while (val.join != null) val = val.join;
						if (val != res) res.join = val; // no circle
					}
				}
				//					if (opds[0].type != tPhiFunc) res.type = opds[0].type;
			} 
		}
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (res.join != null) {
				if (res.join.join != null) res.join = res.join.join;
//				if (res.join.join != null && instrs[res.join.n].ssaOpcode != sCregMove) res.join = res.join.join;
			} 
		}
/*else {
				SSAValue[] opds = instr.getOperands();
				if (opds != null) {
					for (SSAValue opd : opds) {
						if (instrs[opd.n].ssaOpcode == sCPhiFunc) {
							PhiFunction phi = (PhiFunction)instrs[opd.n];
							if (phi.deleted) {
								SSAValue val = phi.getOperands()[0];
								val.join = phi.result;
							}
						}
					}
				}
			}*/
		
	}

	// computes the live ranges of all SSAValues 
	private static void calcLiveRange(SSA ssa) {
		if (dbg) System.out.println("calculating live ranges");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (dbg) System.out.println("instr : " + res.n);
			int currNo = res.n;
			res.end = currNo;
			SSAValue[] opds = instr.getOperands();
//			if (opds == null) continue;
			if (instr.ssaOpcode == sCPhiFunc) {
				int last = ((PhiFunction)instr).last;
				if (res.end < last) res.end = last;
				if (res.join != null) {	// phi function points to other phi function
					PhiFunction joinPhi = ((PhiFunction)instrs[res.join.n]);
					if (joinPhi.start > ((PhiFunction)instr).start) joinPhi.start = ((PhiFunction)instr).start;
					if (joinPhi.result.end < res.end) joinPhi.result.end = res.end;
				}
				continue;
			}
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (dbg) System.out.println("\topd : " + opd.n);
					if (instrs[opd.n].ssaOpcode == sCPhiFunc) {
						int last = ((PhiFunction)instrs[opd.n]).last;
						if (!((PhiFunction)instrs[opd.n]).deleted && res.n < last) res.end = last;
						SSAValue val = opd;
						while (val.join != null) val = val.join;
						PhiFunction phi = (PhiFunction)instrs[val.n];
						if (res.end > phi.result.end) phi.result.end = res.end;
						if (res.n < phi.start) phi.start = res.n;
					} else {
						if (opd.end < currNo) opd.end = currNo;
					}
				}
			}
			if (res.join != null) {
				SSAValue val = res.join;
				while (val.join != null) val = val.join;
				int start = ((PhiFunction)instrs[val.n]).start;
				if (res.end > val.end) val.end = res.end;
				if (res.n < start) ((PhiFunction)instrs[val.n]).start = res.n;
			}
		}
	}

	// assign volatile or nonvolatile register 
	private static void assignRegType() {
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			int currNo = instr.result.n;
			int endNo = instr.result.end;
			if (instr.ssaOpcode == sCPhiFunc) {
				// check if call instruction between start of phi-function and here
				// call to inline method is omitted
				for (int k = ((PhiFunction)instr).start+1; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
						instr.result.nonVol = true;
						MachineCode.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
					}
				}
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start of method and here
				// call to inline method is omitted
				for (int k = 0; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
//					System.out.printf("accAndPropFlags = 0x%1$x\n", ((Call)instrs[k]).item.accAndPropFlags);
						instr.result.nonVol = true;
						MachineCode.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
					}
				}
			} else {
				// check if call instruction in live range
				// call to inline method is omitted
				for (int k = currNo+1; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) 
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
						regAtIndex[joinVal.index] = joinVal.regLong;
						regAtIndex[joinVal.index+1] = joinVal.reg;
					} else {
						joinVal.reg = instr.result.reg;
						regAtIndex[joinVal.index] = joinVal.reg;			
					}
				}
			} 
		}
		
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (dbg) {System.out.print("assign register for instr: "); instr.print(0);}
			if (instr.ssaOpcode == sCPhiFunc && ((PhiFunction)instr).deleted && ((PhiFunction)instr).start >= i && instr.result.join == null) continue; 
			SSAValue res = instr.result;
//			System.out.println("ssa opcode = " + instr.scMnemonics[instr.ssaOpcode] + " regsGPR="+Integer.toHexString(regsGPR));
			// reserve auxiliary register for this instruction
			int nofAuxReg = (scAttrTab[instr.ssaOpcode] >> 16) & 0xF;
			if (nofAuxReg == 4 && res.type == tLong) nofAuxReg = 2;
			else if ((nofAuxReg == 5 && res.type == tLong)
					|| (nofAuxReg == 6 && (res.type == tFloat)||(res.type == tDouble))) nofAuxReg = 1;
			else if (nofAuxReg == 7) {
				if (res.type == tFloat && res.type == tFloat) nofAuxReg = 1;
				else nofAuxReg = 0;
			}
			if (nofAuxReg == 1)
				res.regAux1 = reserveReg(gpr, false);
			else if (nofAuxReg == 2) {
				res.regAux1 = reserveReg(gpr, false);
				res.regAux2 = reserveReg(gpr, false);
			}
			if (dbg) {
				if (res.regAux1 != -1) System.out.print("\tauxReg1 = " + res.regAux1);
				if (res.regAux2 != -1) System.out.print("\tauxReg2 = " + res.regAux2);
			}
			
			// reserve temporary storage on the stack for certain fpr operations
			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
				MachineCode.tempStorage = true;
//			System.out.println("temp storage = true");
			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
				MachineCode.tempStorage = true;

			// reserve register for result of instruction
			SSAValue joinVal = res.join;
//			if (joinVal != null) while (joinVal.join != null) joinVal = joinVal.join;
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (joinVal != null) {
//				System.out.println("joinVal != null");
				if (joinVal.reg < 0) {	// phi function: register not assigned yet
//				System.out.println("join: assign register to phi-function");
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
				} else // assign same register as phi function
					// hier fehlt long
					res.regLong = joinVal.regLong;	
					res.reg = joinVal.reg;	
			} else if (instr.ssaOpcode == sCloadConst) {
				// check if operand is immediate
// call.item.accAndPropFlags & sysMethCodeMask) == idGET2
				SSAInstruction instr1 = instrs[res.end];
				boolean imm = (scAttrTab[instr1.ssaOpcode] & (1 << ssaApImmOpd)) != 0;
				if (imm && res.index < 0 && res.join == null) {
					if (((instr1.ssaOpcode == sCadd) && (res.type == tInteger || res.type == tLong))
							|| ((instr1.ssaOpcode == sCsub) && (res.type == tInteger || res.type == tLong))
							|| ((instr1.ssaOpcode == sCmul) && (res.type == tInteger ))
							|| (instr1.ssaOpcode == sCand)
							|| (instr1.ssaOpcode == sCor)
							|| (instr1.ssaOpcode == sCxor)
							|| (instr1.ssaOpcode == sCshl) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCshr) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCushr) && (res == instr1.getOperands()[1])
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETGPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETFPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETSPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTGPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTFPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((((Call)instr1).item.accAndPropFlags & sysMethCodeMask) == MachineCode.idPUTSPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCbranch) && (res.type == tInteger ))) {
						StdConstant constant = (StdConstant)res.constant;
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
					} else if (((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("ASM")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("ADR_OF_METHOD")))) {
						// asm instruction
					} 
					else {	// opd has index != -1 or cannot be used as immediate opd	
						findReg(res);			
					}
				} else 
					findReg(res);	
			} else {	// all other instructions
				if (res.reg < 0) 	// not yet assigned
					findReg(res);
			}

			if (res.regAux1 != -1) freeReg(gpr, res.regAux1);	//System.out.println("freeing aux reg1");
			if (res.regAux2 != -1) freeReg(gpr, res.regAux2);	// System.out.println("freeing aux reg2")

			// free registers of operands if end of live range reached 
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join != null) opd = opd.join;
					if (opd.end <= i && opd.reg > -1) {
//						System.out.println("freeing operands at end of live range");
						if (opd.type == tLong) {
							freeReg(gpr, opd.regLong);
							freeReg(gpr, opd.reg);
						} else if ((opd.type == tFloat) || (opd.type == tDouble)) {
							freeReg(fpr, opd.reg);
						} else {
							freeReg(gpr, opd.reg);
						}
					}
				}
			}
			// check if phi functions further up stop at this instruction
			for (int k = 0; k < res.n; k++) {
				SSAInstruction instr1 = instrs[k];
				if (instr1.ssaOpcode == sCPhiFunc && instr1.result.end == res.n && instr1.result.join == null) {
					if (dbg) System.out.println("free reg for phi function in this node at " + k);
					if (instr1.result.reg > -1) freeReg(gpr, instr1.result.reg);
				}
			}

			// free registers of result if end of live range is current instruction
			if (res != null) {
				SSAValue val = res;
				while (val.join != null) val = val.join;
				if (val.end == i && val.reg > -1) {	
//					System.out.println("freeing result at end of live range");
					if (val.type == tLong) {
						freeReg(gpr, val.regLong);
						freeReg(gpr, val.reg);
					} else if ((val.type == tFloat) || (val.type == tDouble)) {
						freeReg(fpr, val.reg);
					} else {
						freeReg(gpr, val.reg);
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
		if (dbg) System.out.println("\tfree reg " + reg);
		if (isGPR) {
			regsGPR |= 1 << reg;
		} else {
			regsFPR |= 1 << reg;
		}
	}

}