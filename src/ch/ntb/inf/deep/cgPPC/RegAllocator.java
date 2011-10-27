/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */


package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.host.ErrorReporter;
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
	public static final int maxNofJoins = 32;
	
	static SSA ssa;
	static int maxStackSlots;
	static int regsGPR, regsFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	static int nofVolGPR, nofVolFPR;
	// used to find call in this method with most parameters -> gives stack size
	static int maxNofParamGPR, maxNofParamFPR;
	
	// local and linear copy of all SSA-instructions of all nodes
	private static SSAInstruction[] instrs = new SSAInstruction[nofSSAInstr];	
	private static int nofInstructions;
	
	// used for resolving phi functions
	public static SSAValue[] joins = new SSAValue[maxNofJoins], rootJoins = new SSAValue[maxNofJoins];
	private static int range;

	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	static void buildIntervals(SSA ssa) {
		RegAllocator.ssa = ssa;
		maxStackSlots = ssa.cfg.method.maxStackSlots;
		regsGPR = regsGPRinitial;
		regsFPR = regsFPRinitial;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofVolGPR = 0; nofVolFPR = 0;
		maxNofParamGPR = 0; maxNofParamFPR = 0;
		range = 0;
		for (int i = 0; i < maxNofJoins; i++) {
			rootJoins[i] = null;
			joins[i] = rootJoins[i];
		}

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
		
		markUsedPhiFunctions();
		resolvePhiFunctions();	
		calcLiveRange(ssa);	// compute live intervals
//		printJoins();
//		ssa.print(2);
		assignRegType();	// assign volatile or nonvolatile type
	}

	private static void findLastNodeOfPhi(SSA ssa) {
		if (dbg) StdStreams.vrb.println("determine end of range for phi functions in loop headers");
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

	private static void markUsedPhiFunctions() {
		// determine which deleted phi-functions are still used further down
		if (dbg) StdStreams.vrb.println("determine which phi functions are used further down");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					SSAInstruction opdInstr = opd.owner;
					if (opdInstr.ssaOpcode == sCPhiFunc) {	
						PhiFunction phi = (PhiFunction)opdInstr;
						if (phi.deleted && instr.ssaOpcode == sCPhiFunc && ((PhiFunction)instr).deleted) continue;
						if (phi.deleted && phi != instr) {
							if (dbg) StdStreams.vrb.println("\tphi-function is deleted");
							phi.used = true;
							SSAValue delPhiOpd = phi.getOperands()[0];
							opdInstr = delPhiOpd.owner;
							if (opdInstr.ssaOpcode == sCPhiFunc) {	
								phi = (PhiFunction)opdInstr;
								if (phi.deleted) {
									phi.used = true;
								}
							}
							// set "used" in all del phi-functions in dominators
							SSANode origin = getNode(opdInstr);
							SSANode current = getNode(phi);
							markInDominators(origin, current, phi.result.index);
						}
					}
				}	
			} 
		}
	}

	private static SSANode getNode(SSAInstruction opdInstr) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		boolean found = false;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				if (b.phiFunctions[i] == opdInstr) found = true;
			}
			for (int i = 0; i < b.nofInstr; i++) {
				if (b.instructions[i] == opdInstr) found = true;
			}
			if (found) break;
			b = (SSANode) b.next;
		}	
		if (found) return b; else return null;
	}

	private static void markInDominators(SSANode origin, SSANode current, int index) {
		SSANode node = current;
		while (node.idom != null && node != origin && node.idom != origin) {
			node = (SSANode)node.idom;
			SSAValue val = node.exitSet[index];
			if (val != null) {
				SSAInstruction instr = node.exitSet[index].owner;
				if (instr.ssaOpcode == sCPhiFunc) {
					PhiFunction phi = (PhiFunction)instr;
					if (phi.deleted) phi.used = true;
				}
			}
		}
	}

	private static void resolvePhiFunctions() {
		if (dbg) StdStreams.vrb.println("resolving phi functions, set joins");
		// first run
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
//				instr.print(2);
			if (instr.ssaOpcode == sCPhiFunc) {
				PhiFunction phi = (PhiFunction)instr;
				if (phi.deleted && !phi.used) continue;
				SSAValue[] opds = instr.getOperands();
				SSAValue res = phi.result;
				for (SSAValue opd : opds) {	// set range of phi functions
					if (opd.owner.ssaOpcode == sCloadLocal) res.start = 0;
					if (res.start > opd.n) res.start = opd.n;
					if (res.start > res.n) res.start = res.n;
					if (res.end < opd.n) res.end = opd.n;
					if (res.end < res.n) res.end = res.n;
				}
				
				SSAValue joinVal;
				joinVal = joins[res.index];
				while (joinVal != null && joinVal.next != null) joinVal = joinVal.next;
				if (joinVal == null) {
					joinVal = joins[res.index] = new SSAValue();
					joinVal.start = res.start;
					joinVal.end = res.end;
					joinVal.type = res.type;
				} else {
//						StdStreams.vrb.println("res: " + res.start + " to " + res.end);
//						StdStreams.vrb.println("joinVal: " + joinVal.start + " to " + joinVal.end);
					if (res.start <= joinVal.end) { // does range overlap with current join?
//						StdStreams.vrb.println("joinVal overlaps");
						SSAValue prevJoin = joins[res.index];
						while (prevJoin.next != null && prevJoin.next.next != null) prevJoin = prevJoin.next;
						if (res.start <= prevJoin.end && prevJoin != joinVal) { // does range overlap with previous join, then merge
//							StdStreams.vrb.println("merge joins: prevJoin:" + prevJoin.start + " to " + prevJoin.end + ", join:" + joinVal.start + " to " + joinVal.end);
							assert prevJoin.next == joinVal; 
							res.join = prevJoin;
							prevJoin.next = null;
							for (int k = 0; k < nofInstructions; k++) {
								if (instrs[k].ssaOpcode == sCPhiFunc && instrs[k].result.join == joinVal) 
									instrs[k].result.join = prevJoin;
							}
							joinVal = prevJoin;
						}
						if (res.start < joinVal.start) joinVal.start = res.start;
						if (res.end > joinVal.end) joinVal.end = res.end;
					} else {	// does not overlap, create new join
//						StdStreams.vrb.println("joinVal does not overlap, create new join value");
						joinVal.next = new SSAValue();
						joinVal = joinVal.next;
						joinVal.start = res.start;
						joinVal.end = res.end;
						joinVal.type = res.type;
					}
				}

				assert joinVal != null;
				res.join = joinVal;	// set join of phi function
				res.join.index = res.index;
			} 				
		}

		// 2nd run, set joins of all operands of phi functions
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			if (instr.ssaOpcode == sCPhiFunc) {
				PhiFunction phi = (PhiFunction)instr;
				if ((phi.deleted && !phi.used)) continue;
				SSAValue[] opds = instr.getOperands();
				for (SSAValue opd : opds) {
					if (opd.owner.ssaOpcode == sCPhiFunc) continue;	// already set
					if (phi.result.join != null) {
						opd.join = phi.result.join;
					}
				}
			} 		
		}
	}

	// computes the live ranges of all SSAValues 
	private static void calcLiveRange(SSA ssa) {
		if (dbg) StdStreams.vrb.println("calculating live ranges");
		for (int i = 0; i < nofInstructions; i++) {
//			instr.print(2);
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			int currNo = res.n;
			res.end = currNo;	// set end to current instruction
			SSAValue[] opds = instr.getOperands();
			
			if (instr.ssaOpcode == sCPhiFunc) {	
				if (res.join == null) continue;	// phi is deleted and not used
				int last = ((PhiFunction)instr).last;
				if (res.join.end < last) res.join.end = last;
			}
			
			if (res.join != null) {	// set start of join
				if (res.join.start > currNo) {
//					StdStreams.vrb.println("start was: " + res.join.start);
					res.join.start = currNo;
//					StdStreams.vrb.println("start set to: " + currNo);
				}
			}
			
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (dbg) StdStreams.vrb.println("\t\topd : " + opd.n);
					SSAInstruction opdInstr = opd.owner;
					if (opd.join == null) {	// regular operand
						if (opd.end < currNo) opd.end = currNo; 
					} else {	// is operand of phi function 
						if (opd.join.end < currNo) opd.join.end = currNo;
						if (opd.join.start > currNo) opd.join.start = currNo;
					}
					if (opdInstr.ssaOpcode == sCloadLocal) {
						if (opd.join != null) opd.join.start = 0;
						// store last use of a parameter
						CodeGen.paramRegEnd[opdInstr.result.index - maxStackSlots] = currNo;
					}
				}
			}
		}
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
				if (res.join.nonVol) CodeGen.paramHasNonVolReg[res.join.index - maxStackSlots] = true;
			} else if (instr.ssaOpcode == sCloadLocal) {
				// check if call instruction between start of method and here
				// call to inline method is omitted
				for (int k = 0; k < endNo; k++) {
					SSAInstruction instr1 = instrs[k];
					if (instr1.ssaOpcode == sCnew || (instr1.ssaOpcode == sCcall && 
							(((Call)instr1).item.accAndPropFlags & (1 << dpfSynthetic)) == 0)) {
						res.nonVol = true;
						CodeGen.paramHasNonVolReg[res.index - maxStackSlots] = true;
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
	 * finally, it determines how many parameters are passed on the stack
	 */
	static void assignRegisters(CodeGen code) {
		// handle loadLocal first, 
		if (dbg) StdStreams.vrb.println("\thandle load local:");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (instr.ssaOpcode == sCloadLocal) {
				if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
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
		}
		
		if (dbg) StdStreams.vrb.println("\thandle all other instructions:");
		for (int i = 0; i < nofInstructions; i++) {
			SSAInstruction instr = instrs[i];
			SSAValue res = instr.result;
			if (dbg) {StdStreams.vrb.print("\tassign reg for instr "); instr.print(0);}
//			if (instr.ssaOpcode == sCPhiFunc && ((PhiFunction)instr).deleted && ((PhiFunction)instr).start >= i && instr.result.join == null) continue; 
			if (instr.ssaOpcode == sCPhiFunc && res.join == null) continue; 
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
			} else if (nofAuxReg == 9) {
				if (res.type == tLong) {nofAuxReg = 2; nofAuxRegFpr = 3;}
				else nofAuxReg = 1;
			}
			
			if (nofAuxReg == 1) res.regAux1 = reserveReg(gpr, false);
			else if (nofAuxReg == 2) {
				res.regAux1 = reserveReg(gpr, false);
				res.regAux2 = reserveReg(gpr, false);
			}
			if (nofAuxRegFpr == 1) res.regAux3 = reserveReg(fpr, false);
			else if (nofAuxRegFpr == 3) {
				res.regAux3 = reserveReg(fpr, false);
				res.regAux4 = reserveReg(fpr, false);
				res.regAux5 = reserveReg(fpr, false);
			}
			if (dbg) {
				if (res.regAux1 != -1) StdStreams.vrb.print("\tauxReg1 = " + res.regAux1);
				if (res.regAux2 != -1) StdStreams.vrb.print("\tauxReg2 = " + res.regAux2);
			}
			
			// reserve temporary storage on the stack for certain fpr operations
			if ((scAttrTab[instr.ssaOpcode] & (1 << ssaApTempStore)) != 0) 
				CodeGen.tempStorage = true;
			if (instr.ssaOpcode == sCloadConst && (res.type == tFloat || res.type == tDouble))
				CodeGen.tempStorage = true;
			if (instr.ssaOpcode == sCdiv && res.type == tLong)
				CodeGen.tempStorage = true;

			// reserve register for result of instruction
			if (instr.ssaOpcode == sCloadLocal) {
				// already done
			} else if (res.join != null) {
				SSAValue joinVal = res.join;
//				instr.print(2);
//				StdStreams.vrb.println("\tjoinVal != null");
//				StdStreams.vrb.println("\tjoinVal.reg = " + joinVal.reg);
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
//				if (imm && res.index < 0 && res.join == null) {
				if (imm && res.index < maxStackSlots && res.join == null) {
					if (dbg) StdStreams.vrb.println("\timmediate");
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
			if (dbg) StdStreams.vrb.println("\t\treg = " + res.reg);

			if (res.regAux1 != -1) {
				freeReg(gpr, res.regAux1);	if (dbg) StdStreams.vrb.println("\tfreeing aux reg1");
			}
			if (res.regAux2 != -1) {
				freeReg(gpr, res.regAux2);	if (dbg) StdStreams.vrb.println("\tfreeing aux reg2");
			}
			if (res.regAux3 != -1) {
				freeReg(fpr, res.regAux3);	if (dbg) StdStreams.vrb.println("\tfreeing aux reg3");
			}
			if (res.regAux4 != -1) {
				freeReg(fpr, res.regAux4);	if (dbg) StdStreams.vrb.println("\tfreeing aux reg4");
			}
			if (res.regAux5 != -1) {
				freeReg(fpr, res.regAux5);	if (dbg) StdStreams.vrb.println("\tfreeing aux reg5");
			}

			// free registers of operands if end of live range reached 
			SSAValue[] opds = instr.getOperands();
			if (opds != null) {
				for (SSAValue opd : opds) {
					if (opd.join == null) {
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
					} else {
						SSAValue val = opd.join;
						if (val.end <= i && val.reg > -1) {
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
			}

			// free registers of result if end of live range is current instruction
			if (res != null) {
				if (res.join != null) res = res.join;
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
		CodeGen.nofNonVolGPR = nofNonVolGPR;
		CodeGen.nofNonVolFPR = nofNonVolFPR;
		CodeGen.nofVolGPR = nofVolGPR;
		CodeGen.nofVolFPR = nofVolFPR;
		int nof = maxNofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) CodeGen.callParamSlotsOnStack = nof;
		nof = maxNofParamFPR - (paramEndFPR - paramStartFPR + 1);
		if (nof > 0) CodeGen.callParamSlotsOnStack += nof*2;
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
		if (dbg) StdStreams.vrb.println("\tbefore reserving " + Integer.toHexString(regsGPR));
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
			ErrorReporter.reporter.error(603);
			assert false: "not enough GPR's for locals";
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
			ErrorReporter.reporter.error(604);
			assert false: "not enough FPR's for locals";
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
		if (dbg) StdStreams.vrb.println("\tfree reg " + reg + "\tregsGPR=0x" + Integer.toHexString(regsGPR));
	}
	
	static void printJoins() {
		StdStreams.vrb.print("joins at index: [");
		for (int i = 0; i < 32; i++) {
			if (joins[i] != null) StdStreams.vrb.print("x");
			StdStreams.vrb.print(",");
		}
		StdStreams.vrb.println("]\nlive ranges of phi functions");
		for (int i = 0; i < 32; i++) {
			if (joins[i] != null) {
				StdStreams.vrb.print("\tindex=" + joins[i].index);
				SSAValue next = joins[i];
				while (next != null) {
					StdStreams.vrb.print(": start=" + next.start);
					StdStreams.vrb.print(", end=" + next.end);
					if (next.nonVol) StdStreams.vrb.print(", nonVol"); else StdStreams.vrb.print(", vol");
					StdStreams.vrb.print(", type=" + next.typeName());
					StdStreams.vrb.print(", reg=" + next.reg);
					if (next.regAux1 > -1) StdStreams.vrb.print(", regAux1=" + next.regAux1);
					next = next.next;
				}
				StdStreams.vrb.println();
			}
		}

	}

}
