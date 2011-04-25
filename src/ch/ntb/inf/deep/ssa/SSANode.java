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
			// only one predecessor --> no merge necessary
			if (this.equals(predecessors[0])
					|| ((SSANode) predecessors[0]).exitSet == null) {// equal by
				// "while(true){}
				entrySet = new SSAValue[maxStack + maxLocals];
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
						// if this ist the root node and this is a loopheader
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
								param = generateLoadParameter((SSANode) idom, j, ssa);
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
											phi.addOperand(generateLoadParameter((SSANode) predecessors[x],j, ssa));
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
														phi.addOperand(generateLoadParameter((SSANode) predecessors[i],	j, ssa));
														entrySet[j] = result;
														addPhiFunction(phi);
													} else {
														// phi functions are created in this node
														SSAValue temp = generateLoadParameter((SSANode) predecessors[i],j, ssa);
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
													phi.addOperand(generateLoadParameter((SSANode) predecessors[i],	j, ssa));
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
												if (func == null) {
													SSAValue result = new SSAValue();
													SSAValue opd = entrySet[j];
													result.index = j;
													PhiFunction phi = new PhiFunction(sCPhiFunc);
													result.owner = phi;
													phi.result = result;
													phi.result.type = entrySet[j].type;
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
												} else {
													// phi functions are created in this node 
													//check if operands are from same type
													SSAValue[] opnd = func.getOperands();

													// determine type
													boolean typeFlagsSet = false;
													if((opnd[0].type & 0x7fffffff)!= 0 && (((SSANode) predecessors[i]).exitSet[j].type & 0x7fffffff) != 0){
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
												if((entrySet[j].type & 0x7fffffff)!= 0 && (((SSANode) predecessors[i]).exitSet[j].type & 0x7fffffff) != 0){
													typeFlagsSet = true;
												}
												if (typeFlagsSet || entrySet[j].type == ((SSANode) predecessors[i]).exitSet[j].type) {
													SSAValue result = new SSAValue();
													SSAValue opd = entrySet[j];
													result.index = j;
													PhiFunction phi = new PhiFunction(sCPhiFunc);
													result.owner = phi;
													phi.result = result;
													phi.result.type = entrySet[j].type;
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
			this.traversCode(ssa);
		}

	}

	public void traversCode(SSA ssa) {
		SSAValue value1, value2, value3, value4, result;
		SSAValue[] operands;
		int val, val1;
		DataItem field;
		SSAInstruction instr;
		boolean wide = false;
		exitSet = entrySet.clone();// Don't change the entry set
		// determine top of the stack
		for (stackpointer = maxStack - 1; stackpointer >= 0	&& exitSet[stackpointer] == null; stackpointer--);
		for (int bca = this.firstBCA; bca <= this.lastBCA; bca++) {
			if(dbg)StdStreams.vrb.println("BCA: " + bca + ", nofStackItems: " + (stackpointer + 1));
			switch (ssa.cfg.code[bca] & 0xff) {
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
				pushToStack(result);
				break;
			case bCdconst_0:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = new StdConstant((int) (Double
						.doubleToLongBits(0.0) >> 32), (int) (Double
						.doubleToLongBits(0.0)));
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txDouble];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdconst_1:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = new StdConstant((int) (Double
						.doubleToLongBits(1.0) >> 32), (int) (Double
						.doubleToLongBits(1.0)));
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txDouble];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCbipush:
				// get byte from Bytecode
				bca++;
				val = ssa.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				result.constant = new StdConstant(val, 0);
				result.constant.name = HString.getHString("#");
				result.constant.type = Type.wellKnownTypes[txInt];
				instr = new NoOpnd(sCloadConst);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCsipush:
				// get short from Bytecode
				bca++;
				short sval = (short) (((ssa.cfg.code[bca++] & 0xff) << 8) | (ssa.cfg.code[bca] & 0xff));
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
				pushToStack(result);
				break;
			case bCldc:
				bca++;
				val = (ssa.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (ssa.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) ssa.cfg.method.owner.constPool[val];
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
					if (ssa.cfg.method.owner.constPool[val] instanceof StringLiteral) {// is
						// a
						// String
						StringLiteral literal = (StringLiteral) ssa.cfg.method.owner.constPool[val];
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
				pushToStack(result);
				break;
			case bCldc_w:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | (ssa.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (ssa.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) ssa.cfg.method.owner.constPool[val];
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
					if (ssa.cfg.method.owner.constPool[val] instanceof StringLiteral) {// is
						// a
						// String
						StringLiteral literal = (StringLiteral) ssa.cfg.method.owner.constPool[val];
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
				pushToStack(result);
				break;
			case bCldc2_w:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | (ssa.cfg.code[bca] & 0xFF);
				result = new SSAValue();
				if (ssa.cfg.method.owner.constPool[val] instanceof StdConstant) {
					StdConstant constant = (StdConstant) ssa.cfg.method.owner.constPool[val];
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
				pushToStack(result);
				break;
			case bCiload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				if(ssa.isParam[val + maxStack]){//TODO check this
					load(val, ssa.paramType[val + maxStack]);
				}else{
					load(val, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));
				}
				break;
			case bClload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tLong);
				break;
			case bCfload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tFloat);
				break;
			case bCdload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tDouble);
				break;
			case bCaload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				if(ssa.isParam[val + maxStack]){//TODO check this
					load(val, ssa.paramType[val + maxStack]);
				}else{
					load(val, SSAValue.tRef);
				}
				break;
			case bCiload_0:
				if(ssa.isParam[maxStack]){//TODO check this
					load(0, ssa.paramType[maxStack]);
				}else{
					load(0, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));
				}
				break;
			case bCiload_1:
				if(ssa.isParam[maxStack + 1]){//TODO check this
					load(1, ssa.paramType[maxStack + 1]);
				}else{
					load(1, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));
				}
				break;
			case bCiload_2:
				if(ssa.isParam[maxStack + 2]){//TODO check this
					load(2, ssa.paramType[maxStack + 2]);
				}else{
					load(2, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));
				}
				break;
			case bCiload_3:
				if(ssa.isParam[maxStack + 3]){//TODO check this
					load(3, ssa.paramType[maxStack + 3]);
				}else{
					load(3, SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt));
				}
				break;
			case bClload_0:
				load(0, SSAValue.tLong);
				break;
			case bClload_1:
				load(1, SSAValue.tLong);
				break;
			case bClload_2:
				load(2, SSAValue.tLong);
				break;
			case bClload_3:
				load(3, SSAValue.tLong);
				break;
			case bCfload_0:
				load(0, SSAValue.tFloat);
				break;
			case bCfload_1:
				load(1, SSAValue.tFloat);
				break;
			case bCfload_2:
				load(2, SSAValue.tFloat);
				break;
			case bCfload_3:
				load(3, SSAValue.tFloat);
				break;
			case bCdload_0:
				load(0, SSAValue.tDouble);
				break;
			case bCdload_1:
				load(1, SSAValue.tDouble);
				break;
			case bCdload_2:
				load(2, SSAValue.tDouble);
				break;
			case bCdload_3:
				load(3, SSAValue.tDouble);
				break;
			case bCaload_0:
				if(ssa.isParam[maxStack]){
					load(0, ssa.paramType[maxStack]);
				}else{
					load(0, SSAValue.tRef);
				}
				break;
			case bCaload_1:
				if(ssa.isParam[1 + maxStack]){
					load(1, ssa.paramType[1 + maxStack]);
				}else{
					load(1, SSAValue.tRef);
				}
				break;
			case bCaload_2:
				if(ssa.isParam[2 + maxStack]){
					load(2, ssa.paramType[2 + maxStack]);
				}else{
					load(2, SSAValue.tRef);
				}				break;
			case bCaload_3:
				if(ssa.isParam[3 + maxStack]){
					load(3, ssa.paramType[3 + maxStack]);
				}else{
					load(3, SSAValue.tRef);
				}				break;
			case bCiaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAinteger;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAlong;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAfloat;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAdouble;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCaaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				if(value1.type >= SSAValue.tAref  && ((value1.index >= 0 && ssa.isParam[value1.index]) || (value1.owner.getOperands() != null && value1.owner.getOperands().length > 1))){
					result.type = value1.type;
				}else{
					result.type = SSAValue.tRef;
				}
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCbaload:
				// Remember the result type isn't set here (it could be boolean
				// or byte)
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAbyte;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tByte | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCcaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAchar;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tChar | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCsaload:
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAshort;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tShort | (1 << SSAValue.ssaTaFitIntoInt);
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCistore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + val);
				break;
			case bClstore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + val);
				break;
			case bCfstore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + val);
				break;
			case bCdstore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + val);
				break;
			case bCastore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff)) & 0xffff;// get
					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + val);
				break;
			case bCistore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack);
				break;
			case bCistore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1);
				break;
			case bCistore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2);
				break;
			case bCistore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3);
				break;
			case bClstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack);
				break;
			case bClstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1);
				break;
			case bClstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2);
				break;
			case bClstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3);
				break;
			case bCfstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack);
				break;
			case bCfstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1);
				break;
			case bCfstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2);
				break;
			case bCfstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3);
				break;
			case bCdstore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack);
				break;
			case bCdstore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1);
				break;
			case bCdstore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2);
				break;
			case bCdstore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3);
				break;
			case bCastore_0:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack);
				break;
			case bCastore_1:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 1);
				break;
			case bCastore_2:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 2);
				break;
			case bCastore_3:
				// create register moves in creating of SSA was wished by U.Graf
				storeAndInsertRegMoves(maxStack + 3);
				break;
			case bCiastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAinteger;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bClastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAlong;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCfastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAfloat;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCdastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAdouble;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCaastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAref;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCbastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAbyte;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCcastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAchar;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCsastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				value1.type = SSAValue.tAshort;//TODO @Roger and Urs verify
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				instr = new StoreToArray(sCstoreToArray, value1, value2, value3);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCpop:
				popFromStack();
				break;
			case bCpop2:
				value1 = popFromStack();
				// false if value1 is a value of a category 2 computational type
				if (!((value1.type == SSAValue.tLong) || (value1.type == SSAValue.tDouble))) {
					popFromStack();
				}
				break;
			case bCdup:
				value1 = popFromStack();
				pushToStack(value1);
				pushToStack(value1);
				break;
			case bCdup_x1:
				value1 = popFromStack();
				value2 = popFromStack();
				pushToStack(value1);
				pushToStack(value2);
				pushToStack(value1);
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
				break;
			case bCswap:
				value1 = popFromStack();
				value2 = popFromStack();
				pushToStack(value1);
				pushToStack(value2);
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
				pushToStack(result);
				break;
			case bCiinc:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca++] & 0xff)) & 0xffff;// get index
					val1 = ((ssa.cfg.code[bca++] << 8) | (ssa.cfg.code[bca] & 0xff));// get const
					wide = false;
				} else {
					val = ssa.cfg.code[bca++] & 0xFF;// get index
					val1 = ssa.cfg.code[bca];// get const
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
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCgoto:
				// val = (short) (ssa.cfg.code[bca + 1] & 0xff << 8 |
				// ssa.cfg.code[bca + 2]);
				instr = new Branch(sCbranch);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCjsr:
				// I think it isn't necessary to push the address onto the stack
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCret:
				if (wide) {
					bca = bca + 2; // step over indexbyte1 and indexbyte2
					wide = false;
				} else {
					bca++;// step over index
				}
				break;
			case bCtableswitch:
				value1 = popFromStack();
				instr = new Branch(sCbranch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// Step over whole bytecode instruction
				bca++;
				// pad bytes
				while ((bca & 0x03) != 0) {
					bca++;
				}
				// default jump address
				bca = bca + 4;
				// we need the low and high
				int low1 = ((ssa.cfg.code[bca++] & 0xFF) << 24) | ((ssa.cfg.code[bca++] & 0xFF) << 16) | ((ssa.cfg.code[bca++] & 0xFF) << 8) | (ssa.cfg.code[bca++] & 0xFF);
				int high1 = ((ssa.cfg.code[bca++] & 0xFF) << 24) | ((ssa.cfg.code[bca++] & 0xFF) << 16) | ((ssa.cfg.code[bca++] & 0xFF) << 8) | (ssa.cfg.code[bca++] & 0xFF);
				int nofPair1 = high1 - low1 + 1;

				// jump offsets
				bca = bca + 4 * nofPair1 - 1;
				break;
			case bClookupswitch:
				value1 = popFromStack();
				instr = new Branch(sCbranch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// Step over whole bytecode instruction
				bca++;
				// pad bytes
				while ((bca & 0x03) != 0) {
					bca++;
				}
				// default jump adress
				bca = bca + 4;
				// npairs
				int nofPair2 = ((ssa.cfg.code[bca++] & 0xFF) << 24) | ((ssa.cfg.code[bca++] & 0xFF) << 16) | ((ssa.cfg.code[bca++] & 0xFF) << 8) | (ssa.cfg.code[bca++] & 0xFF);
				// jump offsets
				bca = bca + 8 * nofPair2 - 1;
				break;
			case bCireturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bClreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bCfreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bCdreturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bCareturn:
				value1 = popFromStack();
				instr = new Branch(sCreturn, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bCreturn:
				instr = new Branch(sCreturn);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				// discard Stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				ssa.countAndMarkReturns(this);
				break;
			case bCgetstatic:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				// determine the type of the field
				if (!(ssa.cfg.method.owner.constPool[val] instanceof DataItem)) {
					assert false : "Constantpool entry isn't a DataItem. Used in getstatic";
				}
				field = (DataItem) ssa.cfg.method.owner.constPool[val];
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
				pushToStack(result);
				break;
			case bCputstatic:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				value1 = popFromStack();
				if (ssa.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new MonadicRef(sCstoreToField,
							(DataItem) ssa.cfg.method.owner.constPool[val],
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in putstatic";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCgetfield:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				// determine the type of the field
				field = (DataItem) ssa.cfg.method.owner.constPool[val];
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
				if (ssa.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new MonadicRef(sCloadFromField,
							(DataItem) ssa.cfg.method.owner.constPool[val],
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in getfield";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCputfield:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tVoid;
				value2 = popFromStack();
				value1 = popFromStack();
				if (ssa.cfg.method.owner.constPool[val] instanceof DataItem) {
					instr = new DyadicRef(sCstoreToField,
							(DataItem) ssa.cfg.method.owner.constPool[val],
							value1, value2);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a DataItem. Used in putfield";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				break;
			case bCinvokevirtual:
				bca++;
				// index into cp
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) ssa.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(
						((Method) ssa.cfg.method.owner.constPool[val]).methDescriptor,
						ssa);
				instr = new Call(sCcall,
						((Method) ssa.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokespecial:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) ssa.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(
						((Method) ssa.cfg.method.owner.constPool[val]).methDescriptor,
						ssa);
				instr = new Call(sCcall,
						((Method) ssa.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				((Call) instr).invokespecial = true;
				addInstruction(instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokestatic:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				// cp entry must be a MethodItem
				val1 = ((Method) ssa.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1];// nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(
						((Method) ssa.cfg.method.owner.constPool[val]).methDescriptor,
						ssa);
				instr = new Call(sCcall,
						((Method) ssa.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCinvokeinterface:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				bca = bca + 2;// step over count and zero byte
				// cp entry must be a MethodItem
				val1 = ((Method) ssa.cfg.method.owner.constPool[val]).nofParams;
				operands = new SSAValue[val1 + 1];// objectref + nargs
				for (int i = operands.length - 1; i > -1; i--) {
					operands[i] = popFromStack();
				}
				result = new SSAValue();
				result.type = decodeReturnDesc(
						((Method) ssa.cfg.method.owner.constPool[val]).methDescriptor,
						ssa);
				instr = new Call(sCcall,
						((Method) ssa.cfg.method.owner.constPool[val]),
						operands);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				if (result.type != SSAValue.tVoid) {
					pushToStack(result);
				}
				break;
			case bCnew:		// TODO @Urs: Check this
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				// value1 = new SSAValue();
				result = new SSAValue();
				Item type = null;
				if (ssa.cfg.method.owner.constPool[val] instanceof Class) {
					type = ssa.cfg.method.owner.constPool[val];
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
				pushToStack(result);
				break;
			case bCnewarray:		// TODO @Urs: Check this
				bca++;
				val = ssa.cfg.code[bca] & 0xff; // atype (Array type)
				value1 = popFromStack();
				Item atype = Type.classList.getItemByName(Character.toString(tcArray) + Type.wellKnownTypes[val].name);
				assert atype != null : "[BCA " + bca + "] can't find a array item for the given atype (\"" + Character.toString(tcArray) + Type.wellKnownTypes[val].name +"\")!";
				result = new SSAValue();
				result.type = val + 10;
				SSAValue[] operand = { value1 };
				instr = new Call(sCnew, atype, operand);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCanewarray:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tAref;
				value1 = popFromStack();
				SSAValue[] opnd = { value1 };
				if (ssa.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new Call(sCnew,
							((Type) ssa.cfg.method.owner.constPool[val]), opnd);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in anewarray";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
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
				pushToStack(result);
				break;
			case bCathrow:
				value1 = popFromStack();
				result = value1;
				instr = new Monadic(sCthrow, value1);
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				// clear stack
				while (stackpointer >= 0) {
					exitSet[stackpointer] = null;
					stackpointer--;
				}
				pushToStack(result);
				break;
			case bCcheckcast:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				value1 = popFromStack();
				result = new SSAValue();
				if (ssa.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new MonadicRef(sCthrow,
							(Type) ssa.cfg.method.owner.constPool[val], value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in checkcast";
				}
				instr.result = result;
				instr.result.owner = instr;
				pushToStack(value1);
				addInstruction(instr);
				break;
			case bCinstanceof:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca] & 0xFF;
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger | (1 << SSAValue.ssaTaFitIntoInt);
				if (ssa.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new MonadicRef(sCinstanceof,
							((Type) ssa.cfg.method.owner.constPool[val]),
							value1);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in instanceof";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCmonitorenter:
				popFromStack();
				break;
			case bCmonitorexit:
				popFromStack();
				break;
			case bCwide:
				wide = true;
				break;
			case bCmultianewarray:
				bca++;
				val = ((ssa.cfg.code[bca++] & 0xFF) << 8) | ssa.cfg.code[bca++] & 0xFF;
				val1 = ssa.cfg.code[bca] & 0xFF;
				result = new SSAValue();
				result.type = SSAValue.tAref;
				operands = new SSAValue[val1];
				for (int i = operands.length - 1; i >= 0; i--) {
					operands[i] = popFromStack();
				}
				if (ssa.cfg.method.owner.constPool[val] instanceof Type) {
					instr = new Call(sCnew,
							((Type) ssa.cfg.method.owner.constPool[val]),
							operands);
				} else {
					instr = null;
					assert false : "Constantpool entry isn't a class, array or interface type. Used in multianewarray";
				}
				instr.result = result;
				instr.result.owner = instr;
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCifnull:
			case bCifnonnull:
				value1 = popFromStack();
				instr = new Branch(sCbranch, value1);
				instr.result = new SSAValue();
				instr.result.owner = instr;
				addInstruction(instr);
				bca = bca + 2; // step over branchbyte1 and branchbyte2
				break;
			case bCgoto_w:
				bca = bca + 4; // step over branchbyte1 and branchbyte2...
				break;
			case bCjsr_w:
				// I think it isn't necessary to push the adress onto the stack
				bca = bca + 4; // step over branchbyte1 and branchbyte2...
				break;
			case bCbreakpoint:
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

	public void addInstruction(SSAInstruction instr) {
		int len = instructions.length;
		if (nofInstr == len) {
			SSAInstruction[] newArray = new SSAInstruction[2 * len];
			for (int k = 0; k < len; k++)
				newArray[k] = instructions[k];
			instructions = newArray;

		}
		if ((nofInstr > 0)	&& (instructions[nofInstr - 1].ssaOpcode == sCbranch)) { // insert before branch instruction
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

	private void load(int index, int type) {
		SSAValue result = exitSet[maxStack + index];

		if (result == null) {// local isn't initialized
			result = new SSAValue();
			result.type = type;
			result.index = index + maxStack;
			SSAInstruction instr = new NoOpnd(sCloadLocal);
			instr.result = result;
			instr.result.owner = instr;
			addInstruction(instr);
			exitSet[maxStack + index] = result;
		}

		pushToStack(result);

	}
	
	protected void loadLocal(int index, int type){
		SSAValue result = exitSet[maxStack + index];

		if (result == null) {// local isn't initialized
			result = new SSAValue();
			result.type = type;
			result.index = index + maxStack;
			SSAInstruction instr = new NoOpnd(sCloadLocal);
			instr.result = result;
			instr.result.owner = instr;
			exitSet[maxStack + index] = result;
			
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

	private SSAValue generateLoadParameter(SSANode predecessor, int index,
			SSA ssa) {
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
			node = this.insertNode(predecessor, ssa);
		}

		SSAValue result = new SSAValue();
		result.index = index;
		result.type = ssa.paramType[index];
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
	public SSANode insertNode(SSANode predecessor, SSA ssa) {
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
		SSANode lastNode = (SSANode) ssa.cfg.rootNode;
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
	public void eliminateRedundantPhiFunc() {
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
						if (res == tempOperands[j] || res.owner.ssaOpcode != sCPhiFunc) {
							// the PhiFunctions doesn't lives but have 1 operand from an SSAinstruction which is not a PhiFunction
							// replace the virtual deleted phiFunction with this operand
							SSAValue[] opnd = phiFunctions[i].getOperands();
							opnd[j] = res;
							phiFunctions[i].setOperands(opnd);
						} 
						tempOperands[j] = res;// protect for cycles

					}
				}
				if (tempRes != (tempOperands[j])) {
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
				// if the Phifunc has no parameter or it has only itself as paramter so delete it real 
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

	private int decodeReturnDesc(HString methodDescriptor, SSA ssa) {
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
			type = (ssa.decodeFieldType(ch) & 0x7fffffff) + 10;// +10 is for Arrays

		} else {
			type = ssa.decodeFieldType(ch);
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

	private void storeAndInsertRegMoves(int index) {
		// create register moves in creating of SSA was wished by U.Graf
		SSAValue value1 = popFromStack();
		SSAValue value2 = insertRegMoves(this, index, value1);
		if (value1 == value2) {
			exitSet[index] = value1;
			exitSet[index].index = index;
		}
	}
	
	private SSAValue insertRegMoves(SSANode addTo, int index, SSAValue val){
		if(val.index > -1 && val.index != index){
			System.out.println("val.index = " + val.index);
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