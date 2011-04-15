
package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.host.StdStreams;
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
	static int nofVolGPR, nofVolFPR;
	//used to find call in this method with most parameters -> gives stack size
	static int nofParamGPR, nofParamFPR;
	
	// local and linear copy of all SSA-instructions of all nodes
	private static SSAInstruction[] instrs = new SSAInstruction[nofSSAInstr];	
	private static int nofInstructions;
	
	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	static void buildIntervals(SSA ssa) {
		maxStackSlots = ssa.cfg.method.maxStackSlots;
		regsGPR = regsGPRinitial;
		regsFPR = regsFPRinitial;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofVolGPR = 0; nofVolFPR = 0;
		nofParamGPR = 0; nofParamFPR = 0;
		
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
		if (dbg) StdStreams.out.println("determine end of range for phi functions in loop headers");
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
		if (dbg) StdStreams.out.println("resolving phi functions");
		// first run, determine which deleted phi-functions are still used further down
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
//			if (dbg) StdStreams.out.println("\t resolve at " + instr.result.n);
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					SSAInstruction opdInstr = opd.owner;
					if (opdInstr.ssaOpcode == sCPhiFunc) {	// make iterativ
						if (dbg) StdStreams.out.println("\t\topd is phi-function at " + opdInstr.result.n);
						PhiFunction phi = (PhiFunction)opdInstr;
						if (phi.deleted) {
							if (dbg) StdStreams.out.println("\t\tphi-function is deleted");
							phi.used = true;
							SSAValue delPhiOpd = phi.getOperands()[0];
							opdInstr = delPhiOpd.owner;
							if (opdInstr.ssaOpcode == sCPhiFunc) {	
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
		// 2nd run, set all joins
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc) {
				PhiFunction phi = (PhiFunction)instr;
				((PhiFunction)instr).start = instr.result.n;		// schon hier???????
				SSAValue[] opds = instr.getOperands();
				SSAValue res = instr.result;
				if (dbg) StdStreams.out.println("instr : " + res.n);
				//					while (res.join != null) res = res.join;
				for (SSAValue opd : opds) {
					if (dbg) StdStreams.out.println("\topd : " + opd.n);
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
		// 3rd run, all simplify joins
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (res.join != null) {
				if (res.join.join != null) res.join = res.join.join;
//				if (res.join.join != null && instrs[res.join.n].ssaOpcode != sCregMove) res.join = res.join.join;
			} 
		}		
	}

	// computes the live ranges of all SSAValues 
	private static void calcLiveRange(SSA ssa) {
		if (dbg) StdStreams.out.println("calculating live ranges");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (dbg) StdStreams.out.println("\tinstr : " + res.n + " end=" + res.end);
			int currNo = res.n;
			res.end = currNo;
			SSAValue[] opds = instr.getOperands();
//			if (opds == null) continue;
			// handle phi-functions separately
			// set start and end for this phi-function and its join   
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
			// for all instructions which are not phi-functions:
			// set interval end for all operands
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (dbg) StdStreams.out.println("\t\topd : " + opd.n + " end=" + res.end);
					SSAInstruction opdInstr = opd.owner;
					if (opdInstr.ssaOpcode == sCPhiFunc) {
						// if opd is phi-function: change start and end of this phi-function
						int last = ((PhiFunction)opdInstr).last;
//						if (!((PhiFunction)opdInstr).deleted && res.n < last) {
//							res.end = last; 
//							if (dbg) StdStreams.out.println("\t\tlast="+last);
//						}
						SSAValue val = opd;
						while (val.join != null) val = val.join;
						PhiFunction phi = (PhiFunction)val.owner;
						if (res.end > phi.result.end) {
							phi.result.end = res.end; 
							if (dbg) StdStreams.out.println("\t\tend="+res.end);
						}
						if (res.n < phi.start) phi.start = res.n;
					} else if (opdInstr.ssaOpcode == sCloadLocal) {
						if (opd.end < currNo) opd.end = currNo;
						// store last use of a parameter
						CodeGen.paramRegEnd[opdInstr.result.index - maxStackSlots] = currNo;
//						StdStreams.out.print("currNo="+currNo);
//						StdStreams.out.println(", paramRegEnd["+(opdInstr.result.index - maxStackSlots)+"]="+MachineCode.paramRegEnd[opdInstr.result.index - maxStackSlots]);
					} else {
						if (opd.end < currNo) opd.end = currNo;
//						if (opd.owner.ssaOpcode == sCloadLocal) 
//							MachineCode.paramRegEnd[opd.index - maxStackSlots] = currNo;
					}
				}
			}
		}
		if (dbg) StdStreams.out.println("\tsecond run");
		// 2nd run, set interval end for all joins 
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (res.join != null) {
				if (dbg) StdStreams.out.print("\tinstr : " + res.n + ", join != null");
				SSAValue val = res.join;
				while (val.join != null) val = val.join;
				if (dbg) StdStreams.out.println(", res.end = "+res.end + ", val.end="+val.end);
				int start = ((PhiFunction)val.owner).start;
				if (res.end > val.end) val.end = res.end;
				if (res.n < start) ((PhiFunction)val.owner).start = res.n;
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
						CodeGen.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
					}
				}
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start of method and here
				// call to inline method is omitted
				for (int k = 0; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
						instr.result.nonVol = true;
						CodeGen.paramHasNonVolReg[instr.result.index - maxStackSlots] = true;
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
			
			if (instr.ssaOpcode == sCcall) {	// check if floats in exceptions
				Call call = (Call)instr;
				if ((call.item.accAndPropFlags & (1 << dpfSynthetic)) != 0)
					if ((call.item.accAndPropFlags & sysMethCodeMask) == CodeGen.idENABLE_FLOATS) {
					CodeGen.enFloatsInExc = true;
				}
			}
		}
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 */
	static void assignRegisters(CodeGen code) {
		// handle loadLocal first
		if (dbg) StdStreams.out.println("\thandle load local");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			SSAValue joinVal = res.join;
			if (instr.ssaOpcode == sCloadLocal) {
				if (dbg) {StdStreams.out.print("\t\tassign reg for instr "); instr.print(0);}
				int type = res.type;
				if (type == tLong) {
					res.regLong = CodeGen.paramRegNr[res.index - maxStackSlots];
					res.reg = CodeGen.paramRegNr[res.index+1 - maxStackSlots];
				} else if ((type == tFloat) || (type == tDouble)) {
					res.reg = CodeGen.paramRegNr[res.index - maxStackSlots];
				} else if (type == tVoid) {
				} else {
					res.reg = CodeGen.paramRegNr[res.index - maxStackSlots];
				}	
				
				if (joinVal != null) {
					if (res.type == tLong) {
						joinVal.regLong = res.regLong;
						joinVal.reg = res.reg;
//						phiRegAtIndex[joinVal.index] = joinVal.regLong;
//						phiRegAtIndex[joinVal.index+1] = joinVal.reg;
					} else {
						joinVal.reg = res.reg;
//						phiRegAtIndex[joinVal.index] = joinVal.reg;			
					}
				}
				if (dbg) StdStreams.out.println("\t\treg = " + res.reg);
			} 
		}
		
		if (dbg) StdStreams.out.println("\thandle all other instructions");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (dbg) {StdStreams.out.print("\tassign reg for instr "); instr.print(0);}
			if (instr.ssaOpcode == sCPhiFunc && ((PhiFunction)instr).deleted && ((PhiFunction)instr).start >= i && instr.result.join == null) continue; 
			SSAValue res = instr.result;
			// reserve auxiliary register for this instruction
			int nofAuxReg = (scAttrTab[instr.ssaOpcode] >> 16) & 0xF;
			int nofAuxRegFpr = 0;
			if (nofAuxReg == 4 && res.type == tLong) nofAuxReg = 2;
			else if ((nofAuxReg == 5 && res.type == tLong)
					|| ((nofAuxReg == 6 && (res.type == tFloat || res.type == tDouble)))) nofAuxReg = 1;
			else if (nofAuxReg == 7) {
				if (res.type == tFloat || res.type == tDouble) nofAuxReg = 1;
				else nofAuxReg = 0;
			} else if (nofAuxReg == 8) {
				if (res.type == tFloat || res.type == tDouble) {nofAuxReg = 1; nofAuxRegFpr = 1;}
				else nofAuxReg = 0;
			}
			if (nofAuxReg == 1)
				res.regAux1 = reserveReg(gpr, false);
			else if (nofAuxReg == 2) {
				res.regAux1 = reserveReg(gpr, false);
				res.regAux2 = reserveReg(gpr, false);
			}
			if (nofAuxRegFpr == 1) res.regAux2 = reserveReg(fpr, false);
			if (dbg) {
				if (res.regAux1 != -1) StdStreams.out.print("\tauxReg1 = " + res.regAux1);
				if (res.regAux2 != -1) StdStreams.out.print("\tauxReg2 = " + res.regAux2);
			}
			
			// reserve temporary storage on the stack for certain fpr operations
			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
				CodeGen.tempStorage = true;
			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
				CodeGen.tempStorage = true;

			// reserve register for result of instruction
			SSAValue joinVal = res.join;
			//			if (joinVal != null) while (joinVal.join != null) joinVal = joinVal.join;
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (joinVal != null) {
				if (dbg) StdStreams.out.println("\tjoinVal != null");
				if (joinVal.reg < 0) {	// phi function: register not assigned yet
					if (dbg) StdStreams.out.println("\tjoin: assign new reg to phi-function");
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
					if (dbg) StdStreams.out.println("\tassign same reg as phi function");
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
				if (imm && res.index < 0 && res.join == null) {
					if (dbg) StdStreams.out.println("\timmediate");
					// opd must be used in an instruction with immediate form available
					// and opd must not be already in a register 
					// and opd must have join == null
					if (((instr1.ssaOpcode == sCadd) && ((res.type & 0x7fffffff) == tInteger))	
							|| ((instr1.ssaOpcode == sCsub) && ((res.type & 0x7fffffff) == tInteger))
							|| ((instr1.ssaOpcode == sCmul) && ((res.type & 0x7fffffff) == tInteger))
							// add, sub, mul only with integer
							|| (instr1.ssaOpcode == sCand)
							|| (instr1.ssaOpcode == sCor)
							|| (instr1.ssaOpcode == sCxor)
							// logical operators with integer and long
							|| (instr1.ssaOpcode == sCshl) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCshr) && (res == instr1.getOperands()[1])
							|| (instr1.ssaOpcode == sCushr) && (res == instr1.getOperands()[1])
							// shift operators only if immediate is shift distance (and not value to be shifted)
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETGPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETFPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("GETSPR")))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTGPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((Call)instr1).item.name.equals(HString.getHString("PUTFPR")) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCcall) && ((((Call)instr1).item.accAndPropFlags & sysMethCodeMask) == CodeGen.idPUTSPR) && (instr1.getOperands()[0] == res))
							|| ((instr1.ssaOpcode == sCbranch) && ((res.type & 0x7fffffff) == tInteger ))) {
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
			if (dbg) StdStreams.out.println("\treg = " + res.reg);

			if (res.regAux1 != -1) {
				freeReg(gpr, res.regAux1);	if (dbg) StdStreams.out.println("\tfreeing aux reg1");
			}
			if (res.regAux2 != -1) {
				if (nofAuxRegFpr == 0) {
					freeReg(gpr, res.regAux2);	if (dbg) StdStreams.out.println("\tfreeing aux reg2");
				} else  freeReg(fpr, res.regAux2);
			}

			// free registers of operands if end of live range reached 
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join != null) opd = opd.join;
					if (opd.owner.ssaOpcode == sCloadLocal) {
						if (CodeGen.paramRegEnd[opd.owner.result.index - maxStackSlots] <= i)
							if (opd.type == tLong) {
								freeReg(gpr, opd.regLong);
								freeReg(gpr, opd.reg);
							} else if ((opd.type == tFloat) || (opd.type == tDouble)) {
								freeReg(fpr, opd.reg);
							} else {
								freeReg(gpr, opd.reg);
							}
						continue;
					}
					if (opd.end <= i && opd.reg > -1) {
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
					if (dbg) StdStreams.out.println("\tfree reg for phi function in this node at " + k);
					if (instr1.result.reg > -1) freeReg(gpr, instr1.result.reg);
				}
			}

			// free registers of result if end of live range is current instruction
			if (res != null) {
				SSAValue val = res;
				while (val.join != null) val = val.join;
				if (val.end == i && val.reg > -1) {	
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
			
			// find call which needs most registers for parameters
			// this determines the size of the stack for this method
			if (instr.ssaOpcode == sCcall || instr.ssaOpcode == sCcall) {
				int gpr = 0, fpr = 0;
				for (SSAValue opd : opds) {
					int type = opd.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) gpr += 2;
					else if (type == tFloat || type == tDouble) fpr++;
					else gpr++;
				}
				if (gpr > nofParamGPR) nofParamGPR = gpr; 
				if (fpr > nofParamFPR) nofParamGPR = fpr; 
			}
		}
		CodeGen.nofNonVolGPR = nofNonVolGPR;
		CodeGen.nofNonVolFPR = nofNonVolFPR;
		CodeGen.nofVolGPR = nofVolGPR;
		CodeGen.nofVolFPR = nofVolFPR;
		int nof = nofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) CodeGen.paramSlotsOnStack = nof;
		nof = nofParamFPR - (paramEndFPR - paramStartFPR + 1);
		if (nof > 0) CodeGen.paramSlotsOnStack += nof;
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
		if (dbg) StdStreams.out.println("\tbefore reserving " + Integer.toHexString(regsGPR));
		int regs;
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
			assert false: "not enough registers for GPRs";
			return 0;
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
			assert false: "not enough registers for FPRs";
			return 0;
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
		if (dbg) StdStreams.out.println("\tfree reg " + reg + "\tregsGPR=0x" + Integer.toHexString(regsGPR));
	}

}