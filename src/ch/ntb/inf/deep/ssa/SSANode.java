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

package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.DataItem;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.ICjvmInstructionOpcs;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.instruction.Branch;
import ch.ntb.inf.deep.ssa.instruction.Call;
import ch.ntb.inf.deep.ssa.instruction.Dyadic;
import ch.ntb.inf.deep.ssa.instruction.DyadicRef;
import ch.ntb.inf.deep.ssa.instruction.Monadic;
import ch.ntb.inf.deep.ssa.instruction.MonadicRef;
import ch.ntb.inf.deep.ssa.instruction.NoOpnd;
import ch.ntb.inf.deep.ssa.instruction.NoOpndRef;
import ch.ntb.inf.deep.ssa.instruction.PhiFunction;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;
import ch.ntb.inf.deep.ssa.instruction.StoreToArray;
import ch.ntb.inf.deep.strings.HString;

/**
 * @author millischer
 */
public class SSANode extends CFGNode implements ICjvmInstructionOpcs,
		SSAInstructionOpcs, ICdescAndTypeConsts {
	private static boolean dbg = false;
	private SSA owner;
	boolean traversed;
	public int nofInstr;
	public int nofPhiFunc;
	public int nofDeletedPhiFunc; // its used for junit tests
	public int maxLocals;
	public int maxStack;
	private int stackpointer;
	public SSAValue exitSet[];
	public SSAValue entrySet[];
	public PhiFunction phiFunctions[];
	public SSAInstruction instructions[];
	public int codeStartAddr, codeEndAddr;

	public SSANode() {
		super();
		instructions = new SSAInstruction[4];
		phiFunctions = new PhiFunction[2];
		traversed = false;
		nofInstr = 0;
		nofPhiFunc = 0;
		nofDeletedPhiFunc = 0;
		maxLocals = 0;
		maxStack = 0;

	}

	/**
	 * The state arrays of the predecessors nodes of this node are merged into
	 * the new entry set. phi-functions are created, if there is more then one
	 * predecessor. iterates over all the bytecode instructions, emits SSA
	 * instructions and modifies the state array
	 */
	public void mergeAndDetermineStateArray(SSA ssa) {
		
		owner = ssa;
		maxLocals = ssa.cfg.method.getMaxLocals();
		maxStack = ssa.cfg.method.getMaxStckSlots();

		// check if all predecessors have their state array set
		if (!isLoopHeader()) {
			for (int i = 0; i < nofPredecessors && predecessors[i] != null; i++) {
				if (((SSANode) predecessors[i]).exitSet == null) {
					assert false : "exit set of predecessor is empty";
				}
			}
		}
		if (nofPredecessors == 0) {
			// create new entry and exit sets, locals are uninitialized
			entrySet = new SSAValue[maxStack + maxLocals];
		} else if (nofPredecessors == 1) {
			// only one predecessor --> no merge necessary but if it the predecessor is itself(loopheader) so create phiFunctions
			// they are used by regAllocator
			if (this.equals(predecessors[0]) || ((SSANode) predecessors[0]).exitSet == null) {// equal if it is the first node and it is from a while(...){} or do{...}while(..)
				if(!traversed){
					entrySet = new SSAValue[maxStack + maxLocals];
					for (int i = 0; (i < maxStack + maxLocals) ; i++) {
						//generate phiFunction
						SSAValue result = new SSAValue();
						result.index = i;
						PhiFunction phi = new PhiFunction(sCPhiFunc);
						result.owner = phi;
						phi.result = result;
						if(ssa.isParam[i]){
							phi.result.type = ssa.paramType[i];
							
							SSAValue param = new SSAValue();
							param.index = i;
							param.type = ssa.paramType[i];
							SSAInstruction instr = new NoOpnd(sCloadLocal);
							instr.result = param;
							instr.result.owner = instr;
							this.addInstruction(instr);
							phi.addOperand(param);
						}			
						addPhiFunction(phi);
						
						//Stack is empty
						if (i >= maxStack ) {
							entrySet[i] = result;
						}
					}
				}else{
					for (int i = 0; (i < maxStack + maxLocals) ; i++) {
						SSAValue value = ((SSANode) predecessors[0]).exitSet[i];
						if(value != null){
							phiFunctions[i].addOperand(value);
						}
					}
					eliminateRedundantPhiFunc();
				}
			} else {
				entrySet = ((SSANode) predecessors[0]).exitSet.clone();
			}
		} else if (nofPredecessors >= 2) {
			// multiple predecessors --> merge necessary
			if (isLoopHeader()) {
				// if true --> generate phi functions for all locals
				// if we have redundant phi functions, we eliminate it later
				if (!traversed) {
					// first visit --> insert phi function   

					// swap on the index 0 a predecessor thats already processed
					for (int i = 0; i < nofPredecessors; i++) {
						if (((SSANode) predecessors[i]).exitSet != null) {
							SSANode temp = (SSANode) predecessors[i];
							predecessors[i] = predecessors[0];
							predecessors[0] = temp;
						}
					}

					if (((SSANode) predecessors[0]).exitSet == null) {
						// if this is the root node and this is a loopheader
						// from case while(true){..}
						entrySet = new SSAValue[maxStack + maxLocals];
					} else {
						entrySet = ((SSANode) predecessors[0]).exitSet.clone();
					}

					for (int i = 0; i < maxStack + maxLocals; i++) {
						SSAValue result = new SSAValue();
						result.index = i;
						PhiFunction phi = new PhiFunction(sCPhiFunc);
						result.owner = phi;
						phi.result = result;
						if( ssa.isParam[i]){
							phi.result.type = ssa.paramType[i];
						}
						if (entrySet[i] != null) {
							phi.result.type = entrySet[i].type;
							SSAValue opd = entrySet[i];
							opd = insertRegMoves((SSANode) predecessors[0], i, opd);
							phi.addOperand(opd);
						}
						addPhiFunction(phi);
						// Stack will be set when it is necessary;
						if (i >= maxStack || entrySet[i] != null) {
							entrySet[i] = result;
						}
					}
				} else {
					// skip the first already processed predecessor
					for (int i = 1; i < nofPredecessors; i++) {
						for (int j = 0; j < maxStack + maxLocals; j++) {

							SSAValue param = ((SSANode) predecessors[i]).exitSet[j];
							SSAValue temp = param; // store

							// Check if it need a loadParam instruction
							if (ssa.isParam[j]&& (phiFunctions[j].nofOperands == 0 || param == null)) {
								param = generateLoadParameter((SSANode) idom, j);
							}
							if (temp != null && temp != param) {
								phiFunctions[j].result.type = temp.type;
								SSAValue opd = temp;
								opd = insertRegMoves((SSANode) predecessors[i], j, opd);
								phiFunctions[j].addOperand(opd);
							}
							if (param != null) {// stack could be empty
								phiFunctions[j].result.type = param.type;
								SSAValue opd = param;
								opd = insertRegMoves((SSANode) predecessors[i], j, opd);
								phiFunctions[j].addOperand(opd);
							}
						}
					}
					// second visit, merge is finished
					// eliminate redundant PhiFunctions
					eliminateRedundantPhiFunc();
				}
			} else {
				// it isn't a loop header
				for (int i = 0; i < nofPredecessors; i++) {
					if (entrySet == null) {
						// first predecessor --> create locals
						entrySet = ((SSANode) predecessors[i]).exitSet.clone();
					} else {
						// all other predecessors --> merge
						for (int j = 0; j < maxStack + maxLocals; j++) {
							// if both null, do nothing
							if ((entrySet[j] != null && (entrySet[j].type != SSAValue.tVoid ||(entrySet[j].type == SSAValue.tVoid && ssa.isParam[j])))|| (((SSANode) predecessors[i]).exitSet[j] != null && (((SSANode) predecessors[i]).exitSet[j].type != SSAValue.tVoid ||(((SSANode) predecessors[i]).exitSet[j].type == SSAValue.tVoid && ssa.isParam[j])))){
								if (entrySet[j] == null) {// predecessor is set
									if (ssa.isParam[j]) {
										// create phi function
										SSAValue result = new SSAValue();
										result.index = j;
										PhiFunction phi = new PhiFunction(sCPhiFunc);
										result.owner = phi;
										phi.result = result;
										entrySet[j] = result;
										// generate for all already proceed
										// predecessors a loadParameter
										// and add their results to the phi
										// function
										for (int x = 0; x < i; x++) {
											phi.addOperand(generateLoadParameter((SSANode) predecessors[x],j));
										}
										result.type = ssa.paramType[j];
										SSAValue opd = ((SSANode) predecessors[i]).exitSet[j];
										opd = insertRegMoves((SSANode) predecessors[i], j, opd);
										phi.addOperand(opd);
										addPhiFunction(phi);
									}
								} else {// entrySet[j] != null
									// if both equals, do nothing
									if (!(entrySet[j].equals(((SSANode) predecessors[i]).exitSet[j]))) {
										if (((SSANode) predecessors[i]).exitSet[j] == null) {
											if (ssa.isParam[j]) {
												if (entrySet[j].owner.ssaOpcode == sCPhiFunc) {
													PhiFunction func = null;
													// func == null if the phi functions are created by the predecessor
													for (int y = 0; y < nofPhiFunc; y++) {
														if (entrySet[j].equals(phiFunctions[y].result)) {
															func = phiFunctions[y];
															break;
														}
													}
													if (func == null) {
														SSAValue result = new SSAValue();
														SSAValue opd = entrySet[j];
														result.index = j;
														PhiFunction phi = new PhiFunction(sCPhiFunc);
														result.owner = phi;
														phi.result = result;
														phi.result.type = ssa.paramType[j];
														int predNo = 0;
														for(int k = 0; k < nofPredecessors; k++){
															if(entrySet[j]==((SSANode)predecessors[k]).exitSet[j]){
																predNo = k;
																break;
															}
														}
														assert predNo != -1 : "predecessor for entrySet not found";
														opd = insertRegMoves((SSANode) predecessors[predNo], j, opd);//entrySet[j]
														phi.addOperand(opd);
														phi.addOperand(generateLoadParameter((SSANode) predecessors[i],	j));
														entrySet[j] = result;
														addPhiFunction(phi);
													} else {
														// phi functions are created in this node
														SSAValue temp = generateLoadParameter((SSANode) predecessors[i],j);
														func.result.type = temp.type;
														func.addOperand(temp);
													}
												} else {
													// entrySet[j] != SSAValue.tPhiFunc
													SSAValue result = new SSAValue();
													SSAValue opd = entrySet[j];
													result.index = j;
													PhiFunction phi = new PhiFunction(sCPhiFunc);
													result.owner = phi;
													phi.result = result;
													phi.result.type = ssa.paramType[j];
													int predNo = 0;
													for(int k = 0; k < nofPredecessors; k++){
														if(entrySet[j]==((SSANode)predecessors[k]).exitSet[j]){
															predNo = k;
															break;
														}
													}
													assert predNo != -1 : "predecessor for entrySet not found";
													opd = insertRegMoves((SSANode) predecessors[predNo], j, opd);//entrySet[j]
													phi.addOperand(opd);
													phi.addOperand(generateLoadParameter((SSANode) predecessors[i],	j));
													entrySet[j] = result;
													addPhiFunction(phi);
												}
											} else {
												entrySet[j] = null;
											}
										} else {
											// entrySet[j] != null && ((SSANode) predecessors[i]).exitSet[j] != null
											if (entrySet[j].owner.ssaOpcode == sCPhiFunc) {
												PhiFunction func = null;
												// func == null if the phi functions are created by the predecessor
												for (int y = 0; y < nofPhiFunc; y++) {
													if (entrySet[j].equals(phiFunctions[y].result)) {
														func = phiFunctions[y];
														break;
													}
												}
												
												if (func == null ) {
													//check if operands are from same type
													boolean typeFlagsSet = false;
													if((entrySet[j].type & (1 << SSAValue.ssaTaFitIntoInt))!= 0 && (((SSANode) predecessors[i]).exitSet[j].type & (1 << SSAValue.ssaTaFitIntoInt)) != 0){
														typeFlagsSet = true;
													}
													if((entrySet[j].type == SSAValue.tRef && ((SSANode) predecessors[i]).exitSet[j].type > SSAValue.tAref) || (entrySet[j].type > SSAValue.tAref && ((SSANode) predecessors[i]).exitSet[j].type == SSAValue.tRef) ){
														typeFlagsSet = true;
													}
													if (typeFlagsSet || (entrySet[j].type == ((SSANode) predecessors[i]).exitSet[j].type)) {
														SSAValue result = new SSAValue();
														SSAValue opd = entrySet[j];
														result.index = j;
														PhiFunction phi = new PhiFunction(sCPhiFunc);
														result.owner = phi;
														phi.result = result;
														if(typeFlagsSet && ((SSANode) predecessors[i]).exitSet[j].type > SSAValue.tRef){														
															phi.result.type = ((SSANode) predecessors[i]).exitSet[j].type;
														}else{														
															phi.result.type = entrySet[j].type;
														}
														int predNo = 0;
														for(int k = 0; k < nofPredecessors; k++){
															if(entrySet[j]==((SSANode)predecessors[k]).exitSet[j]){
																predNo = k;
																break;
															}
														}
														assert predNo != -1 : "predecessor for entrySet not found";
														opd = insertRegMoves((SSANode) predecessors[predNo], j, opd);//entrySet[j]
														phi.addOperand(opd);
														opd = ((SSANode) predecessors[i]).exitSet[j];
														opd = insertRegMoves((SSANode) predecessors[i], j, opd);
														phi.addOperand(opd);
														entrySet[j] = result;
														addPhiFunction(phi);
													}else{
														entrySet[j] = null;
													}
												} else {
													// phi functions are created in this node 
													//check if operands are from same type
													SSAValue[] opnd = func.getOperands();

													// determine type
													boolean typeFlagsSet = false;
													if((opnd[0].type & (1 << SSAValue.ssaTaFitIntoInt))!= 0 && (((SSANode) predecessors[i]).exitSet[j].type & (1 << SSAValue.ssaTaFitIntoInt)) != 0){
														typeFlagsSet = true;
													}
													if((opnd[0].type == SSAValue.tRef && ((SSANode) predecessors[i]).exitSet[j].type > SSAValue.tAref) || (opnd[0].type > SSAValue.tAref && ((SSANode) predecessors[i]).exitSet[j].type == SSAValue.tRef) ){
														typeFlagsSet = true;
													}
													if (typeFlagsSet || (opnd[0].type == ((SSANode) predecessors[i]).exitSet[j].type)) {
														SSAValue opd = ((SSANode) predecessors[i]).exitSet[j];
														opd = insertRegMoves((SSANode) predecessors[i], j, opd);
														func.addOperand(opd);
													} else {
														// delete all Operands so the function will be deleted in the
														// method eleminateRedundantPhiFunc()
														func.setOperands(new SSAValue[0]);
														func.result.type = -1;
														entrySet[j] = null;
													}
												}
											} else {
												// entrySet[j].owner.ssaOpcode != sCPhiFunc
												boolean typeFlagsSet = false;
												if((entrySet[j].type & (1 << SSAValue.ssaTaFitIntoInt))!= 0 && (((SSANode) predecessors[i]).exitSet[j].type & (1 << SSAValue.ssaTaFitIntoInt)) != 0){
													typeFlagsSet = true;
												}
												if((entrySet[j].type == SSAValue.tRef && ((SSANode) predecessors[i]).exitSet[j].type > SSAValue.tAref) || (entrySet[j].type > SSAValue.tAref && ((SSANode) predecessors[i]).exitSet[j].type == SSAValue.tRef) ){
													typeFlagsSet = true;
												}
												if (typeFlagsSet || entrySet[j].type == ((SSANode) predecessors[i]).exitSet[j].type) {
													SSAValue result = new SSAValue();
													SSAValue opd = entrySet[j];
													result.index = j;
													PhiFunction phi = new PhiFunction(sCPhiFunc);
													result.owner = phi;
													phi.result = result;
													if(typeFlagsSet && ((SSANode) predecessors[i]).exitSet[j].type > SSAValue.tRef){														
														phi.result.type = ((SSANode) predecessors[i]).exitSet[j].type;
													}else{														
														phi.result.type = entrySet[j].type;
													}
													int predNo = 0;
													for(int k = 0; k < nofPredecessors; k++){
														if(entrySet[j]==((SSANode)predecessors[k]).exitSet[j]){
															predNo = k;
															break;
														}
													}
													assert predNo != -1 : "predecessor for entrySet not found";
													opd = insertRegMoves((SSANode) predecessors[predNo], j, opd);//entrySet[j]
													phi.addOperand(opd);
													opd = ((SSANode) predecessors[i]).exitSet[j];
													opd = insertRegMoves((SSANode) predecessors[i], j, opd);
													phi.addOperand(opd);
													addPhiFunction(phi);
													entrySet[j] = result;
												} else {
													entrySet[j] = null;
												}
											}
										}
									}
								}
							}
						}
					}
				}
				// isn't a loop header and merge is finished.
				// eliminate redundant phiFunctins
				eliminateRedundantPhiFunc();
			}
		}
		// fill instruction array
		if (!traversed) {
			traversed = true;
			this.traversCode();
		}

	}

	private void traversCode() {
		SSAValue value1, value2, value3, value4, result;
		SSAValue[] operands;
		int val, val1;
		DataItem field;
		SSAInstruction instr;
		boolean wide = false;
		exitSet = entrySet.clone();// Don't change the entry set
		owner.setLineNrTabIndex(this.firstBCA);
		// determine top of the stack
		for (stackpointer = maxStack - 1; stackpointer >= 0	&& exitSet[stackpointer] == null; stackpointer--);
		for (int bca = this.firstBCA; bca <= this.lastBCA; bca++) {
			if(dbg)StdStreams.vrb.println("BCA: " + bca + ", nofStackItems: " + (stackpointer + 1));
			switch (owner.cfg.code[bca] & 0xff) {
			case bCnop:
				break;
			case bCaconst_null:
				result = new SSAValue();
				result.type = SSAValue.tRef;
				result.constant = null;
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_m1:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(-1, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_0:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(0, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_1:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(1, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_2:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(2, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_3:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(3, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_4:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(4, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiconst_5:
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(5, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClconst_0:
				result = new SSAValue();
				result.type = SSAValue.tLong;
				result.constant = new StdConstant(0, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txLong];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClconst_1:
				result = new SSAValue();
				result.type = SSAValue.tLong;
				result.constant = new StdConstant(0, 1);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txLong];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfconst_0:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = new StdConstant(Float.floatToIntBits(0.0f), 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txFloat];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfconst_1:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = new StdConstant(Float.floatToIntBits(1.0f), 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txFloat];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfconst_2:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = new StdConstant(Float.floatToIntBits(2.0f), 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txFloat];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdconst_0:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = new StdConstant((int) (Double.doubleToLongBits(0.0) >> 32), (int) (Double.doubleToLongBits(0.0)));
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txDouble];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdconst_1:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = new StdConstant((int) (Double.doubleToLongBits(1.0) >> 32), (int) (Double.doubleToLongBits(1.0)));
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txDouble];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCbipush:
				// get byte from Bytecode
				bca++;
				val = owner.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(val, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 1, instr);
				pushToStack(result);
				break;
			case bCsipush:
				// get short from Bytecode
				bca++;
				short sval = (short) (((owner.cfg.code[bca++] & 0xff) << 8) | (owner.cfg.code[bca] & 0xff));
				val = sval;// sign-extended to int
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(val, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCldc:
				bca++;
				val = (owner.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (owner.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) owner.cfg.method.owner.constPool[val];
					if (constant.type == Type.wellKnownTypes[txInt]) {// is a int
						result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
						result.constant = constant;
					} else {
						if (constant.type == Type.wellKnownTypes[txFloat]) { // is a float
							result.type = SSAValue.tFloat;
							result.constant = constant;
							// result.constant =
							// Float.intBitsToFloat(constant.valueH);
						} else {
							assert false : "Wrong Constant type";
						}
					}
				} else {
					if (owner.cfg.method.owner.constPool[val] instanceof StringLiteral) {// is
						// a
						// String
						StringLiteral literal = (StringLiteral) owner.cfg.method.owner.constPool[val];
						result.type = SSAValue.tRef;
						result.constant = literal;
						// result.constant = literal.string;
					} else {
						assert false : "Wrong DataItem type";
						break;
					}
				}
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 1, instr);
				pushToStack(result);
				break;
			case bCldc_w:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | (owner.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (owner.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) owner.cfg.method.owner.constPool[val];
					if (constant.type == Type.wellKnownTypes[txInt]) {// is a int
						result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
						result.constant = constant;
					} else {
						if (constant.type == Type.wellKnownTypes[txFloat]) { // is a float
							result.type = SSAValue.tFloat;
							result.constant = constant;
							// result.constant =
							// Float.intBitsToFloat(constant.valueH);
						} else {
							assert false : "Wrong Constant type";
						}
					}
				} else {
					if (owner.cfg.method.owner.constPool[val] instanceof StringLiteral) {// is
						// a
						// String
						StringLiteral literal = (StringLiteral) owner.cfg.method.owner.constPool[val];
						result.type = SSAValue.tRef;
						result.constant = literal;
						// result.constant = literal.string;
					} else {
						assert false : "Wrong DataItem type";
						break;
					}
				}
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCldc2_w:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | (owner.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (owner.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) owner.cfg.method.owner.constPool[val];
					if (constant.type == Type.wellKnownTypes[txDouble]) {// is a Double
						result.type = SSAValue.tDouble;
						result.constant = constant;
					} else {
						if (constant.type == Type.wellKnownTypes[txLong]) { // is a Long
							result.type = SSAValue.tLong;
							result.constant = constant;
						} else {
							assert false : "Wrong Constant type";
						}
					}
				} else {
					assert false : "Wrong DataItem type";
					break;
				}
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCiload:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
				}
				if(owner.isParam[val + maxStack]){
					if(wide){
						load(maxStack + val, owner.paramType[val + maxStack], bca - 2);
					}else{
						load(maxStack + val, owner.paramType[val + maxStack],bca - 1);						
					}
				}else{
					if(wide){
						load(maxStack + val, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca - 2);						
					}else{
						load(maxStack + val, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca - 1);												
					}
				}
				wide = false;
				break;
			case bClload:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					load(maxStack + val, SSAValue.tLong, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					load(maxStack + val, SSAValue.tLong, bca - 1);
				}
				break;
			case bCfload:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					load(maxStack + val, SSAValue.tFloat, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					load(maxStack + val, SSAValue.tFloat, bca - 1);
				}
				break;
			case bCdload:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					load(maxStack + val, SSAValue.tDouble, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					load(maxStack + val, SSAValue.tDouble, bca - 1);
				}
				break;
			case bCaload:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
				}
				if(owner.isParam[val + maxStack]){
					if(wide){
						load(maxStack + val, owner.paramType[val + maxStack], bca - 2);
					}else{
						load(maxStack + val, owner.paramType[val + maxStack],bca - 1);						
					}
				}else{
					if(wide){
						load(maxStack + val, SSAValue.tRef, bca - 2);						
					}else{
						load(maxStack + val, SSAValue.tRef, bca - 1);												
					}
				}
				wide = false;
				break;
			case bCiload_0:
				if(owner.isParam[maxStack]){
					load(maxStack, owner.paramType[maxStack], bca);
				}else{
					load(maxStack, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca);
				}
				break;
			case bCiload_1:
				if(owner.isParam[maxStack + 1]){
					load(maxStack + 1, owner.paramType[maxStack + 1], bca);
				}else{
					load(maxStack + 1, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca);
				}
				break;
			case bCiload_2:
				if(owner.isParam[maxStack + 2]){
					load(maxStack + 2, owner.paramType[maxStack + 2], bca);
				}else{
					load(maxStack + 2, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca);
				}
				break;
			case bCiload_3:
				if(owner.isParam[maxStack + 3]){
					load(maxStack + 3, owner.paramType[maxStack + 3], bca);
				}else{
					load(maxStack + 3, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt), bca);
				}
				break;
			case bClload_0:
				load(maxStack, SSAValue.tLong, bca);
				break;
			case bClload_1:
				load(maxStack + 1, SSAValue.tLong, bca);
				break;
			case bClload_2:
				load(maxStack + 2, SSAValue.tLong, bca);
				break;
			case bClload_3:
				load(maxStack + 3, SSAValue.tLong, bca);
				break;
			case bCfload_0:
				load(maxStack, SSAValue.tFloat, bca);
				break;
			case bCfload_1:
				load(maxStack + 1, SSAValue.tFloat, bca);
				break;
			case bCfload_2:
				load(maxStack + 2, SSAValue.tFloat, bca);
				break;
			case bCfload_3:
				load(maxStack + 3, SSAValue.tFloat, bca);
				break;
			case bCdload_0:
				load(maxStack, SSAValue.tDouble, bca);
				break;
			case bCdload_1:
				load(maxStack + 1, SSAValue.tDouble, bca);
				break;
			case bCdload_2:
				load(maxStack + 2, SSAValue.tDouble, bca);
				break;
			case bCdload_3:
				load(maxStack + 3, SSAValue.tDouble, bca);
				break;
			case bCaload_0:
				if(owner.isParam[maxStack]){
					load(maxStack, owner.paramType[maxStack], bca);
				}else{
					load(maxStack, SSAValue.tRef, bca);
				}
				break;
			case bCaload_1:
				if(owner.isParam[1 + maxStack]){
					load(maxStack + 1, owner.paramType[1 + maxStack], bca);
				}else{
					load(maxStack + 1, SSAValue.tRef, bca);
				}
				break;
			case bCaload_2:
				if(owner.isParam[2 + maxStack]){
					load(maxStack + 2, owner.paramType[2 + maxStack], bca);
				}else{
					load(maxStack + 2, SSAValue.tRef, bca);
				}				
				break;
			case bCaload_3:
				if(owner.isParam[3 + maxStack]){
					load(maxStack + 3, owner.paramType[3 + maxStack], bca);
				}else{
					load(maxStack + 3, SSAValue.tRef, bca);
				}				
				break;
			case bCiaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAinteger;
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAlong;
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAfloat;
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAdouble;
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCaaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				if(value1.type >= SSAValue.tAref  && ((value1.index >= 0 && owner.isParam[value1.index]) || (value1.owner.getOperands() != null && value1.owner.getOperands().length > 1))){
					result.type = value1.type;
				}else{
					result.type = SSAValue.tRef;
				}
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCbaload:
				// Remember the result type isn't set here (it could be boolean
				// or byte)
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAbyte;
				result = new SSAValue();
				result.type = SSAValue.tByte | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCcaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAchar;
				result = new SSAValue();
				result.type = SSAValue.tChar | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCsaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAshort;
				result = new SSAValue();
				result.type = SSAValue.tShort | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCistore:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 1);
				}
				break;
			case bClstore:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 1);
				}
				break;
			case bCfstore:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 1);
				}
				break;
			case bCdstore:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 1);
				}
				break;
			case bCastore:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff)) & 0xffff;// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 2);
					wide = false;
				} else {
					val = (owner.cfg.code[bca] & 0xff);// get index
					// create register moves in creating of SSA was wished by U.Graf
					storeAndInsertRegMoves(maxStack + val, bca - 1);
				}
				break;
			case bCistore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack, bca);
				break;
			case bCistore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1, bca);
				break;
			case bCistore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2, bca);
				break;
			case bCistore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3, bca);
				break;
			case bClstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack, bca);
				break;
			case bClstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1, bca);
				break;
			case bClstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2, bca);
				break;
			case bClstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3, bca);
				break;
			case bCfstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack, bca);
				break;
			case bCfstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1, bca);
				break;
			case bCfstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2, bca);
				break;
			case bCfstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3, bca);
				break;
			case bCdstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack, bca);
				break;
			case bCdstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1, bca);
				break;
			case bCdstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2, bca);
				break;
			case bCdstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3, bca);
				break;
			case bCastore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack, bca);
				break;
			case bCastore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1, bca);
				break;
			case bCastore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2, bca);
				break;
			case bCastore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3, bca);
				break;
			case bCiastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAinteger;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bClastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAlong;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCfastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAfloat;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCdastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAdouble;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCaastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAref;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCbastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAbyte;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCcastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAchar;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCsastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAshort;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				break;
			case bCpop:
				popFromStack();
				owner.createLineNrPair(bca, null);
				break;
			case bCpop2:
				value1 = popFromStack();
				// false if value1 is a value of a category 2 computational type
				if (!((value1.type == SSAValue.tLong) || (value1.type == SSAValue.tDouble))) {
					popFromStack();
				}
				owner.createLineNrPair(bca, null);
				break;
			case bCdup:
				value1 = popFromStack();
				pushToStack(value1);
				pushToStack(value1);
				owner.createLineNrPair(bca, null);
				break;
			case bCdup_x1:
				value1 = popFromStack();
				value2 = popFromStack();
				pushToStack(value1);
				pushToStack(value2);
				pushToStack(value1);
				owner.createLineNrPair(bca, null);
				break;
			case bCdup_x2:
				value1 = popFromStack();
				value2 = popFromStack();
				// true if value2 is a value of a category 2 computational type
				if ((value2.type == SSAValue.tLong)
						|| (value2.type == SSAValue.tDouble)) {
					pushToStack(value1);
					pushToStack(value2);
					pushToStack(value1);
				} else {
					value3 = popFromStack();
					pushToStack(value1);
					pushToStack(value3);
					pushToStack(value2);
					pushToStack(value1);
				}
				owner.createLineNrPair(bca, null);
				break;
			case bCdup2:
				value1 = popFromStack();
				// true if value1 is a value of a category 2 computational type
				if ((value1.type == SSAValue.tLong)	|| (value1.type == SSAValue.tDouble)) {
					pushToStack(value1);
					pushToStack(value1);
				} else {
					value2 = popFromStack();
					pushToStack(value2);
					pushToStack(value1);
					pushToStack(value2);
					pushToStack(value1);
				}
				owner.createLineNrPair(bca, null);
				break;
			case bCdup2_x1:
				value1 = popFromStack();
				value2 = popFromStack();
				// true if value1 is a value of a category 2 computational type
				if ((value1.type == SSAValue.tLong)	|| (value1.type == SSAValue.tDouble)) {
					pushToStack(value1);
					pushToStack(value2);
					pushToStack(value1);
				} else {
					value3 = popFromStack();
					pushToStack(value2);
					pushToStack(value1);
					pushToStack(value3);
					pushToStack(value2);
					pushToStack(value1);
				}
				owner.createLineNrPair(bca, null);
				break;
			case bCdup2_x2:
				value1 = popFromStack();
				value2 = popFromStack();
				// true if value1 is a value of a category 2 computational type
				if ((value1.type == SSAValue.tLong)	|| (value1.type == SSAValue.tDouble)) {
					// true if value2 is a value of a category 2 computational type 
					//Form4 (the java virtual Machine Specification secondedition, Tim Lindholm, Frank Yellin, page 223)
					if ((value2.type == SSAValue.tLong)	|| (value2.type == SSAValue.tDouble)) {
						pushToStack(value1);
						pushToStack(value2);
						pushToStack(value1);
					} else {
						// Form 2 (the java virtual Machine Specification second
						// edition, Tim Lindholm, Frank Yellin, page 223)
						value3 = popFromStack();
						pushToStack(value1);
						pushToStack(value3);
						pushToStack(value2);
						pushToStack(value1);
					}
				} else {
					value3 = popFromStack();
					// true if value3 is a value of a category 2 computational type
					// Form 3 (the java virtual Machine Specification second
					// edition, Tim Lindholm, Frank Yellin, page 223)
					if ((value3.type == SSAValue.tLong) || (value3.type == SSAValue.tDouble)) {
						pushToStack(value2);
						pushToStack(value1);
						pushToStack(value3);
						pushToStack(value2);
						pushToStack(value1);
					} else {
						// Form 1 (the java virtual Machine Specification second
						// edition, Tim Lindholm, Frank Yellin, page 223)
						value4 = popFromStack();
						pushToStack(value2);
						pushToStack(value1);
						pushToStack(value4);
						pushToStack(value3);
						pushToStack(value2);
						pushToStack(value1);
					}
				}
				owner.createLineNrPair(bca, null);
				break;
			case bCswap:
				value1 = popFromStack();
				value2 = popFromStack();
				pushToStack(value1);
				pushToStack(value2);
				owner.createLineNrPair(bca, null);
				break;
			case bCiadd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCadd, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCladd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCadd, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfadd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCadd, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdadd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCadd, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCisub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCsub, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCsub, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCsub, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCsub, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCimul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCmul, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCmul, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCmul, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCmul, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCidiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCdiv, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCldiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfdiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCddiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCirem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCrem, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCrem, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCrem, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCrem, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCineg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCneg, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCneg, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCneg, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCneg, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCishl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCshl, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClshl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCshl, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCishr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCshr, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClshr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCshr, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiushr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCushr, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClushr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCushr, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiand:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCand, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCland:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCand, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCior:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCor, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCor, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCixor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCxor, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClxor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCxor, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCiinc:
				bca++;
				if (wide) {
					val = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca++] & 0xff)) & 0xffff;// get index
					val1 = ((owner.cfg.code[bca++] << 8) | (owner.cfg.code[bca] & 0xff));// get const
				} else {
					val = owner.cfg.code[bca++] & 0xFF;// get index
					val1 = owner.cfg.code[bca];// get const
				}
				if(exitSet[maxStack + val] == null){
					value1 = new SSAValue();
					value1.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
					value1.index = val + maxStack;
					SSAInstruction loadInstr = new NoOpnd(sCloadLocal);
					loadInstr.result = value1;
					loadInstr.result.owner = loadInstr;
					addInstruction(loadInstr);
				}else{					
					value1 = exitSet[maxStack + val];
				}
//				load(val, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));

				value2 = new SSAValue();
				value2.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				value2.constant = new StdConstant(val1, 0);
				value2.constant.name = HString.getHString("#");
				value2.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = value2;
				instr.result.owner = instr;
				addInstruction(instr);
				if(wide){
					owner.createLineNrPair(bca - 4, instr);
				}else{
					owner.createLineNrPair(bca - 2, instr);
				}
				wide = false;
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);

				instr = new Dyadic(sCadd, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);

				exitSet[maxStack + val] = result;
				exitSet[maxStack + val].index = maxStack + val;
				break;
			case bCi2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCi2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCi2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCl2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvLong, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCl2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvLong, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCl2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvLong, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCf2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvFloat, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCf2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvFloat, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCf2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvFloat, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCd2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvDouble, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCd2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvDouble, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCd2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvDouble, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCi2b:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tByte | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCi2c:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tChar | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCi2s:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tShort | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Monadic(sCconvInt, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bClcmp:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfcmpl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCfcmpg:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCcmpg, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdcmpl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCdcmpg:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCcmpg, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCifeq:
			case bCifne:
			case bCiflt:
			case bCifge:
			case bCifgt:
			case bCifle:
				value1 = popFromStack();
				instr = new Branch(sCbranch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCif_icmpeq:
			case bCif_icmpne:
			case bCif_icmplt:
			case bCif_icmpge:
			case bCif_icmpgt:
			case bCif_icmple:
			case bCif_acmpeq:
			case bCif_acmpne:
				value1 = popFromStack();
				value2 = popFromStack();
				instr = new Branch(sCbranch, value1, value2);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCgoto:
				// val = (short) (ssa.cfg.code[bca + 1] & 0xff << 8 |
				// ssa.cfg.code[bca + 2]);
				instr = new Branch(sCbranch);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCjsr:
				owner.createLineNrPair(bca, null);
				// I think it isn't necessary to push the address onto the stack
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCret:
				owner.createLineNrPair(bca, null);
				if (wide) {
					bca = bca + 2; // step over indexbyte1 and indexbyte2
					wide = false;
				} else {
					bca++;// step over index
				}
				break;
			case bCtableswitch:
				value1 = popFromStack();
				instr = new Branch(sCswitch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// Step over whole bytecode instruction
				bca++;
				// pad bytes
				while ((bca & 0x03) != 0) {
					bca++;
				}
				// default jump address
				bca = bca + 4;
				// we need the low and high
				int low1 = ((owner.cfg.code[bca++] & 0xFF) << 24) | ((owner.cfg.code[bca++] & 0xFF) << 16) | ((owner.cfg.code[bca++] & 0xFF) << 8) | (owner.cfg.code[bca++] & 0xFF);
				int high1 = ((owner.cfg.code[bca++] & 0xFF) << 24) | ((owner.cfg.code[bca++] & 0xFF) << 16) | ((owner.cfg.code[bca++] & 0xFF) << 8) | (owner.cfg.code[bca++] & 0xFF);
				int nofPair1 = high1 - low1 + 1;

				// jump offsets
				bca = bca + 4 * nofPair1 - 1;
				break;
			case bClookupswitch:
				value1 = popFromStack();
				instr = new Branch(sCswitch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// Step over whole bytecode instruction
				bca++;
				// pad bytes
				while ((bca & 0x03) != 0) {
					bca++;
				}
				// default jump adress
				bca = bca + 4;
				// npairs
				int nofPair2 = ((owner.cfg.code[bca++] & 0xFF) << 24) | ((owner.cfg.code[bca++] & 0xFF) << 16) | ((owner.cfg.code[bca++] & 0xFF) << 8) | (owner.cfg.code[bca++] & 0xFF);
				// jump offsets
				bca = bca + 8 * nofPair2 - 1;
				break;
			case bCireturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bClreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bCfreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bCdreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bCareturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bCreturn:
				instr = new Branch(sCreturn);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				owner.countAndMarkReturns(this);
				break;
			case bCgetstatic:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				// determine the type of the field
				if (!(owner.cfg.method.owner.constPool[val] instanceof DataItem)) {
					assert false : "Constantpool entry isn't a DataItem. Used in getstatic";
				}
				field = (DataItem) owner.cfg.method.owner.constPool[val];
				if (((Type)field.type).category == tcArray) {
					switch (field.type.name.charAt(1)) {
					case 'B':
						result.type = SSAValue.tAbyte;
						break;
					case 'C':
						result.type = SSAValue.tAchar;
						break;
					case 'D':
						result.type = SSAValue.tAdouble;
						break;
					case 'F':
						result.type = SSAValue.tAfloat;
						break;
					case 'I':
						result.type = SSAValue.tAinteger;
						break;
					case 'J':
						result.type = SSAValue.tAlong;
						break;
					case 'S':
						result.type = SSAValue.tAshort;
						break;
					case 'Z':
						result.type = SSAValue.tAboolean;
						break;
					case 'L':
						result.type = SSAValue.tAref;
						break;
					case '[':
						result.type = SSAValue.tAref;
						break;
					default:
						result.type = SSAValue.tVoid;
					}
				} else if(((Type)field.type).category == tcPrimitive) {
					switch (field.type.name.charAt(0)) {
					case 'B':
						result.type = SSAValue.tByte | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'C':
						result.type = SSAValue.tChar | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'D':
						result.type = SSAValue.tDouble;
						break;
					case 'F':
						result.type = SSAValue.tFloat;
						break;
					case 'I':
						result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'J':
						result.type = SSAValue.tLong;
						break;
					case 'S':
						result.type = SSAValue.tShort | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'Z':
						result.type = SSAValue.tBoolean | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					default:
						result.type = SSAValue.tVoid;
					}
				} else {//tcRef
					result.type = SSAValue.tRef;
				}
				instr = new NoOpndRef(sCloadFromField, field);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCputstatic:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				value1 = popFromStack();
				if (owner.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new MonadicRef(sCstoreToField,
							(DataItem) owner.cfg.method.owner.constPool[val],
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in putstatic";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				break;
			case bCgetfield:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				// determine the type of the field
				field = (DataItem) owner.cfg.method.owner.constPool[val];
				if (((Type)field.type).category == tcArray) {
					switch (field.type.name.charAt(1)) {
					case 'B':
						result.type = SSAValue.tAbyte;
						break;
					case 'C':
						result.type = SSAValue.tAchar;
						break;
					case 'D':
						result.type = SSAValue.tAdouble;
						break;
					case 'F':
						result.type = SSAValue.tAfloat;
						break;
					case 'I':
						result.type = SSAValue.tAinteger;
						break;
					case 'J':
						result.type = SSAValue.tAlong;
						break;
					case 'S':
						result.type = SSAValue.tAshort;
						break;
					case 'Z':
						result.type = SSAValue.tAboolean;
						break;
					case 'L':
						result.type = SSAValue.tAref;
						break;
					default:
						result.type = SSAValue.tVoid;
					}
				} else if(((Type)field.type).category == tcPrimitive) {
					switch (field.type.name.charAt(0)) {
					case 'B':
						result.type = SSAValue.tByte | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'C':
						result.type = SSAValue.tChar | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'D':
						result.type = SSAValue.tDouble;
						break;
					case 'F':
						result.type = SSAValue.tFloat;
						break;
					case 'I':
						result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'J':
						result.type = SSAValue.tLong;
						break;
					case 'S':
						result.type = SSAValue.tShort | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					case 'Z':
						result.type = SSAValue.tBoolean | (1 << SSAValue.ssaTaFitIntoInt);
						break;
					default:
						result.type = SSAValue.tVoid;
					}
				}else{//tcRef
					result.type = SSAValue.tRef;
				}
				value1 = popFromStack();
				if (owner.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new MonadicRef(sCloadFromField,
							(DataItem) owner.cfg.method.owner.constPool[val],
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in getfield";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCputfield:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				value2 = popFromStack();
				value1 = popFromStack();
				if (owner.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new DyadicRef(sCstoreToField,
							(DataItem) owner.cfg.method.owner.constPool[val],
							value1, value2);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in putfield";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				break;
			case bCinvokevirtual:
				bca++;
				// index into cp
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) owner.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(((Method) owner.cfg.method.owner.constPool[val]).methDescriptor);
				instr = new Call(sCcall,
						((Method) owner.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokespecial:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) owner.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(((Method) owner.cfg.method.owner.constPool[val]).methDescriptor);
				instr = new Call(sCcall,
						((Method) owner.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				((Call) instr).invokespecial = true;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokestatic:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) owner.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1];// nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(((Method) owner.cfg.method.owner.constPool[val]).methDescriptor);
				instr = new Call(sCcall,
						((Method) owner.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokeinterface:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				bca = bca + 2;// step over count and zero byte
				// cp entry must be a MethodItem
				val1 = ((Method) owner.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(((Method) owner.cfg.method.owner.constPool[val]).methDescriptor);
				instr = new Call(sCcall,
						((Method) owner.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 4, instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCnew:	
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				// value1 = new SSAValue();
				result = new SSAValue();
				Item type = null;
				if (owner.cfg.method.owner.constPool[val] instanceof Class) {
					type = owner.cfg.method.owner.constPool[val];
				} 
				else {
					assert false : "Unknown Parametertype for new";
				}
				result.type = SSAValue.tRef;
				// instr = new Call(sCnew, new SSAValue[] { value1 });
				instr = new Call(sCnew, type);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCnewarray:
				bca++;
				val = owner.cfg.code[bca] & 0xff; // atype (Array type)
				value1 = popFromStack();
				Item atype = Type.primTypeArrays[val];  
				assert atype != null : "[BCA " + bca + "] can't find a array item for the given atype (\"" + Character.toString(tcArray) + Type.wellKnownTypes[val].name +"\")!";
				result = new SSAValue();
				result.type = val + 10;
				SSAValue[] operand = { value1 };
				instr = new Call(sCnew, atype, operand);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 1, instr);
				pushToStack(result);
				break;
			case bCanewarray:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tAref;
				value1 = popFromStack();
				SSAValue[] opnd = { value1 };
				if (owner.cfg.method.owner.constPool[val] instanceof Type) { // TODO @Martin: improve the lookup of the array reference
					Item arrayRef = Type.classList.getItemByName("[L" + owner.cfg.method.owner.constPool[val].name.toString() + ";");
					instr = new Call(sCnew,	(Type)arrayRef, opnd);
//					if(arrayRef != null) instr = new Call(sCnew,	(Type)arrayRef, opnd);
//					else instr = new Call(sCnew, ((Type)owner.cfg.method.owner.constPool[val]), opnd);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in anewarray";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);				
				pushToStack(result);
				break;
			case bCarraylength:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new MonadicRef(sCalength, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				pushToStack(result);
				break;
			case bCathrow:
				value1 = popFromStack();
				result = new SSAValue();
				instr = new Monadic(sCthrow, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				// clear stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				pushToStack(value1);
				break;
			case bCcheckcast:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				value1 = popFromStack();
				result = new SSAValue();
				if (owner.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new MonadicRef(sCthrow,
							(Type) owner.cfg.method.owner.constPool[val], value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in checkcast";
				}
				instr.result = result;
				instr.result.owner = instr;
				pushToStack(value1);
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				break;
			case bCinstanceof:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca] & 0xFF;
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				if (owner.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new MonadicRef(sCinstanceof,
							((Type) owner.cfg.method.owner.constPool[val]),
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in instanceof";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 2, instr);
				pushToStack(result);
				break;
			case bCmonitorenter:
				popFromStack();
				owner.createLineNrPair(bca, null);
				break;
			case bCmonitorexit:
				popFromStack();
				owner.createLineNrPair(bca, null);
				break;
			case bCwide:
				wide = true;
				owner.createLineNrPair(bca, null);
				break;
			case bCmultianewarray:
				bca++;
				val = ((owner.cfg.code[bca++] & 0xFF) << 8) | owner.cfg.code[bca++] & 0xFF;
				val1 = owner.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tAref;
				operands = new SSAValue[val1];
				for (int i = operands.length - 1; i >= 0; i--) {
					operands[i] = popFromStack();
				}
				if (owner.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new Call(sCnew,
							((Type) owner.cfg.method.owner.constPool[val]),
							operands);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in multianewarray";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca - 3, instr);
				pushToStack(result);
				break;
			case bCifnull:
			case bCifnonnull:
				value1 = popFromStack();
				instr = new Branch(sCbranch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				owner.createLineNrPair(bca, instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCgoto_w:
				owner.createLineNrPair(bca, null);
				bca = bca + 4; // step over branchbyte1 and branchbyte2...
				break;
			case bCjsr_w:
				owner.createLineNrPair(bca, null);
				// I think it isn't necessary to push the adress onto the stack
				bca = bca + 4; // step over branchbyte1 and branchbyte2...
				break;
			case bCbreakpoint:
				owner.createLineNrPair(bca, null);
				// do nothing
				break;
			default:
				// do nothing
			}
		}
	}

	private void pushToStack(SSAValue value) {
		if (stackpointer + 1 >= maxStack) {
			throw new IndexOutOfBoundsException("Stack overflow");
		}
		//set index for unsubscripted SSAValues
		if(value.index < 0){
			value.index = stackpointer + 1;
		}
		exitSet[stackpointer + 1] = value;
		stackpointer++;

	}

	private SSAValue popFromStack() {
		SSAValue val;
		if (stackpointer < 0) {
			throw new IndexOutOfBoundsException("Empty Stack");
		}
		val = exitSet[stackpointer];
		exitSet[stackpointer] = null;
		stackpointer--;
		return val;

	}

	private void addInstruction(SSAInstruction instr) {
		int len = instructions.length;
		if (nofInstr == len) {
			SSAInstruction[] newArray = new SSAInstruction[2 * len];
			for (int k = 0; k < len; k++)
				newArray[k] = instructions[k];
			instructions = newArray;

		}
		if ((nofInstr > 0)	&& ((instructions[nofInstr - 1].ssaOpcode == sCbranch) || (instructions[nofInstr - 1].ssaOpcode == sCswitch))) { // insert before branch instruction
			if(nofInstr > 1 &&  (instructions[nofInstr - 2].ssaOpcode == sCcmpl || instructions[nofInstr - 2].ssaOpcode == sCcmpg)){ //insert befor cmp instructions
				instructions[nofInstr] = instructions[nofInstr - 1];
				instructions[nofInstr - 1] = instructions[nofInstr - 2];
				instructions[nofInstr - 2] = instr;
			}else{				
				instructions[nofInstr] = instructions[nofInstr - 1];
				instructions[nofInstr - 1] = instr;
			}
		} else
			instructions[nofInstr] = instr;
		nofInstr++;
	}

	private void addPhiFunction(PhiFunction func) {
		int len = phiFunctions.length;
		if (nofPhiFunc == len) {
			PhiFunction[] newArray = new PhiFunction[2 * len];
			for (int k = 0; k < len; k++)
				newArray[k] = phiFunctions[k];
			phiFunctions = newArray;

		}
		phiFunctions[nofPhiFunc] = func;
		nofPhiFunc++;
	}

	private void load(int index, int type, int bca) {
		SSAValue result = exitSet[index];

		if (result == null) {// local isn't initialized
			result = new SSAValue();
			result.type = type;
			result.index = index;
			SSAInstruction instr = new NoOpnd(sCloadLocal);
			instr.result = result;
			instr.result.owner = instr;
			addInstruction(instr);
			exitSet[index] = result;
			owner.createLineNrPair(bca, instr);
		}else{
			owner.createLineNrPair(bca, null);
		}

		pushToStack(result);

	}
	
	protected void loadLocal(int index, int type){
		SSAValue result = exitSet[index];

		if (result == null) {// local isn't initialized
			result = new SSAValue();
			result.type = type;
			result.index = index;
			SSAInstruction instr = new NoOpnd(sCloadLocal);
			instr.result = result;
			instr.result.owner = instr;
			exitSet[index] = result;
			
			//insert before return statement
			int len = instructions.length;
			if (nofInstr == len) {
				SSAInstruction[] newArray = new SSAInstruction[2 * len];
				for (int k = 0; k < len; k++)
					newArray[k] = instructions[k];
				instructions = newArray;

			}
							
			instructions[nofInstr] = instructions[nofInstr - 1];
			instructions[nofInstr - 1] = instr;
			nofInstr++;			
		}
	}

	private SSAValue generateLoadParameter(SSANode predecessor, int index) {
		boolean needsNewNode = false;
		SSANode node = predecessor;
		for (int i = 0; i < this.nofPredecessors; i++) {
			if (!this.predecessors[i].equals(predecessor) && !needsNewNode) {
				needsNewNode = this.idom.equals(predecessor)
						&& !(this.equals(this.predecessors[i].idom))
						&& !isLoopHeader();
			}
		}
		if (needsNewNode) {
			node = this.insertNode(predecessor);
		}

		SSAValue result = new SSAValue();
		result.index = index;
		result.type = owner.paramType[index];
		SSAInstruction instr = new NoOpnd(sCloadLocal);
		instr.result = result;
		instr.result.owner = instr;
		node.addInstruction(instr);
		node.exitSet[index] = result;

		return result;
	}

	/**
	 * 
	 * @param base
	 *            SSANode that immediate follow of predecessor
	 * @param predecessor
	 *            SSANode that is immediate for the base node
	 * @return on success the inserted SSANode, otherwise null
	 */
	private SSANode insertNode(SSANode predecessor) {
		int index = -1;
		SSANode node = null;
		// check if base follows predecessor immediately a save index
		for (int i = 0; i < nofPredecessors; i++) {
			if (predecessors[i].equals(predecessor)) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			node = new SSANode();

			node.firstBCA = -1;
			node.lastBCA = -1;
			node.maxLocals = this.maxLocals;
			node.maxStack = this.maxStack;
			node.idom = idom;
			node.entrySet = predecessor.exitSet.clone();
			node.exitSet = node.entrySet.clone();

			node.addSuccessor(this);
			predecessors[index] = node;

			node.addPredecessor(predecessor);
			for (int i = 0; i < predecessor.successors.length; i++) {
				if (predecessor.successors[i].equals(this)) {
					predecessor.successors[i] = node;
					break;
				}
			}
		}
		// insert node
		SSANode lastNode = (SSANode) owner.cfg.rootNode;
		while ((lastNode.next != null) && (lastNode.next != this)) {
			lastNode = (SSANode) lastNode.next;
		}
		lastNode.next = node;
		node.next = this;

		return node;
	}

	/**
	 * Eliminate phi functions that was unnecessarily generated. There are tow
	 * Cases in which a phi function becomes redundant.
	 * <p>
	 * <b>Case 1:</b><br>
	 * Phi functions of the form
	 * 
	 * <pre>
	 * x = [y,x,x,...,x]
	 * </pre>
	 * 
	 * can be replaced by y.
	 * <p>
	 * <b>Case 2:</b><br>
	 * Phi functions of the form
	 * 
	 * <pre>
	 * x = [x,x,...,x]
	 * </pre>
	 * 
	 * can be replaced by x.
	 * <p>
	 */
	protected void eliminateRedundantPhiFunc() {
		SSAValue tempRes;
		SSAValue[] tempOperands;
		int indexOfDiff;
		boolean redundant, diffAlreadyOccured;
		int count = 0;
		PhiFunction[] temp = new PhiFunction[nofPhiFunc];
		// Traverse phiFunctions
		for (int i = 0; i < nofPhiFunc; i++) {
			indexOfDiff = 0;
			redundant = true;
			diffAlreadyOccured = false;
			tempRes = phiFunctions[i].result;
			tempOperands = phiFunctions[i].getOperands();
			// Compare result with operands.
			// determine if the function is redundant
			for (int j = 0; j < tempOperands.length; j++) {
				if (tempOperands[j].owner.ssaOpcode == sCPhiFunc) {
					// handle virtual deleted PhiFunctions special
					if (((PhiFunction) tempOperands[j].owner).deleted) {
						SSAValue res = tempOperands[j].owner.getOperands()[0];
						while(res.owner.ssaOpcode == sCPhiFunc){//if is a phiFunction too
							if(((PhiFunction)res.owner).deleted){
								if(res == res.owner.getOperands()[0]){//it is the same phiFunction
									break;
								}
								res = res.owner.getOperands()[0];
							} else {
								break;
							}
						}
						if (res != tempOperands[j] || res.owner.ssaOpcode != sCPhiFunc) {
							// the PhiFunctions doesn't lives but have 1 operand from an SSAinstruction which is not a PhiFunction
							// replace the virtual deleted phiFunction with this operand
							SSAValue[] opnd = phiFunctions[i].getOperands();
							opnd[j] = res;
							phiFunctions[i].setOperands(opnd);
						} 
						tempOperands[j] = res;// protect for cycles

					}
				}
				//true if:
				//two different SSAValues and tempOperand[j] is not a result from a deleted phiFunction which have as operand[0] its own result
				if (tempRes != (tempOperands[j]) && !(tempOperands[j].owner.ssaOpcode == sCPhiFunc && ((PhiFunction)tempOperands[j].owner).deleted && tempOperands[j] == tempOperands[j].owner.getOperands()[0])) {
					if (diffAlreadyOccured) {
						if(tempOperands[indexOfDiff] != tempOperands[j]){
							redundant = false;
							break;
						}
					}else{
						diffAlreadyOccured = true;
						indexOfDiff = j;
					}
				}
			}
			if (redundant) {
				// if the Phifunc has no parameter so delete it real 
				if (phiFunctions[i].nofOperands > 0) {
						if (!phiFunctions[i].deleted) {
							// delete it virtually an set the operand for replacement
							phiFunctions[i].deleted = true;
							phiFunctions[i].setOperands(new SSAValue[] { tempOperands[indexOfDiff] });
							nofDeletedPhiFunc++;
						}
						temp[count++] = phiFunctions[i];
					}
			} else {
				temp[count++] = phiFunctions[i];
			}
		}
		phiFunctions = temp;
		nofPhiFunc = count;
	}

	private int decodeReturnDesc(HString methodDescriptor) {
		int type, i;
		char ch = methodDescriptor.charAt(0);
		for (i = 0; ch != ')'; i++) {// travers (....) we need only the
			// Returnvalue;
			ch = methodDescriptor.charAt(i);
		}
		ch = methodDescriptor.charAt(i);
		if (ch == '[') {
			while (ch == '[') {
				i++;
				ch = methodDescriptor.charAt(i);
			}
			type = (owner.decodeFieldType(ch) & 0x7fffffff) + 10;// +10 is for Arrays

		} else {
			type = owner.decodeFieldType(ch);
		}
		return type;
	}

	/**
	 * Prints out the SSANode readable.
	 * <p>
	 * <b>Example:</b>
	 * <p>
	 * 
	 * <pre>
	 * SSANode 0:
	 *       EntrySet {[ , ], [ ,  ]}
	 *          NoOpnd[sCloadConst]
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Monadic[sCloadVar] ( Void )
	 *          NoOpnd[sCloadConst]
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *       ExitSet {[ , ], [ Integer (null), Integer (null) ]}
	 * </pre>
	 * 
	 * @param level
	 *            defines how much to indent
	 * @param nodeNr
	 *            the Number of the node in this SSA
	 */
	public void print(int level, int nodeNr) {

		for (int i = 0; i < level * 3; i++)
			StdStreams.vrb.print(" ");
		StdStreams.vrb.println("SSANode " + nodeNr + ":");

		// Print EntrySet with Stack and Locals
		for (int i = 0; i < (level + 1) * 3; i++)
			StdStreams.vrb.print(" ");
		StdStreams.vrb.print("EntrySet {");
		if (entrySet.length > 0)
			StdStreams.vrb.print("[ ");
		for (int i = 0; i < entrySet.length - 1; i++) {

			if (entrySet[i] != null)
				StdStreams.vrb.print(entrySet[i].toString());

			if (i == maxStack - 1) {
				StdStreams.vrb.print("], [ ");
			} else {
				StdStreams.vrb.print(", ");
			}
		}
		if (entrySet.length > 0) {
			if (entrySet[entrySet.length - 1] != null) {
				StdStreams.vrb.println(entrySet[entrySet.length - 1].toString()
						+ " ]}");
			} else {
				StdStreams.vrb.println("]}");
			}
		} else {
			StdStreams.vrb.println("}");
		}

		// Print Phifunctions
		for (int i = 0; i < nofPhiFunc; i++) {
			phiFunctions[i].print(level + 2);
		}
		// Print Instructions
		for (int i = 0; i < nofInstr; i++) {
			instructions[i].print(level + 2);
		}

		// Print ExitSet with Stack and Locals
		for (int i = 0; i < (level + 1) * 3; i++)
			StdStreams.vrb.print(" ");
		StdStreams.vrb.print("ExitSet {");
		if (exitSet.length > 0)
			StdStreams.vrb.print("[ ");

		for (int i = 0; i < exitSet.length - 1; i++) {

			if (exitSet[i] != null)
				StdStreams.vrb.print(exitSet[i].toString());

			if (i == maxStack - 1) {
				StdStreams.vrb.print("], [ ");
			} else {
				StdStreams.vrb.print(", ");
			}

		}
		if (exitSet.length > 0) {
			if (exitSet[exitSet.length - 1] != null) {
				StdStreams.vrb.println(exitSet[exitSet.length - 1].toString()
						+ " ]}");
			} else {
				StdStreams.vrb.println("]}");
			}
		} else {
			StdStreams.vrb.println("}");
		}
	}

	public String nodeToString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 3; i++) sb.append(" ");
		sb.append("SSANode ["+ this.firstBCA + ":" + this.lastBCA + "]:\n" );
		
		// Print EntrySet with Stack and Locals
		for (int i = 0; i < 6; i++) sb.append(" ");
		sb.append("EntrySet {");
		if (entrySet.length > 0)
			sb.append("[ ");
		for (int i = 0; i < entrySet.length - 1; i++) {
			
			if (entrySet[i] != null)
				sb.append(entrySet[i].toString());
			
			if (i == maxStack - 1) {
				sb.append("], [ ");
			} else {
				sb.append(", ");
			}
		}
		if (entrySet.length > 0) {
			if (entrySet[entrySet.length - 1] != null) {
				sb.append(entrySet[entrySet.length - 1].toString() + " ]}\n");
			} else {
				sb.append("]}\n");
			}
		} else {
			sb.append("}\n");
		}
		
		// write Phifunctions
		for (int i = 0; i < nofPhiFunc; i++) {
			for (int j = 0; j < 9; j++)	sb.append(" ");
			sb.append(phiFunctions[i].toString() + "\n");
		}
		// Print Instructions
		for (int i = 0; i < nofInstr; i++) {
			for (int j = 0; j < 9; j++)	sb.append(" ");
			sb.append(instructions[i].toString() + "\n");
		}
		
		// Print ExitSet with Stack and Locals
		for (int i = 0; i <  6; i++) sb.append(" ");
		sb.append("ExitSet {");
		if (exitSet.length > 0)
			sb.append("[ ");
		
		for (int i = 0; i < exitSet.length - 1; i++) {
			
			if (exitSet[i] != null)
				sb.append(exitSet[i].toString());
			
			if (i == maxStack - 1) {
				sb.append("], [ ");
			} else {
				sb.append(", ");
			}
			
		}
		if (exitSet.length > 0) {
			if (exitSet[exitSet.length - 1] != null) {
				sb.append(exitSet[exitSet.length - 1].toString() + " ]}\n");
			} else {
				sb.append("]}\n");
			}
		} else {
			sb.append("}\n");
		}
		return sb.toString();
	}

	private void storeAndInsertRegMoves(int index, int bca) {
		// create register moves in creating of SSA was wished by U.Graf
		SSAValue value1 = popFromStack();
		SSAValue value2;
		
		//if the value exists only on the stack, the register to store into is not decided now
		//so we don't need a register move
		if(value1.owner.ssaOpcode != sCPhiFunc && value1.index > -1 && value1.index < maxStack){
			value1.index = index;
			value2 = value1;
		}else{
			value2 = insertRegMoves(this, index, value1);			
		}
		
		if (value1 == value2) {
			exitSet[index] = value1;
			exitSet[index].index = index;
			owner.createLineNrPair(bca, null);
		}else{
			owner.createLineNrPair(bca, value2.owner);			
		}
	}
	
	private SSAValue insertRegMoves(SSANode addTo, int index, SSAValue val){
		if(val.index > -1 && val.index != index){
			if(dbg)StdStreams.vrb.println("val.index = " + val.index);
			SSAValue r = new SSAValue();
			r.type = val.type;
			r.index = index;
			SSAInstruction move = new Monadic(sCregMove, val);
			move.result = r;
			move.result.owner = move;
			addTo.addInstruction(move);
			addTo.exitSet[index] = r;
			return r;			
		}		
		return val;
	}
}
