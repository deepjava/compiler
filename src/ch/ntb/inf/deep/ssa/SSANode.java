package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cfg.JvmInstructionMnemonics;
import ch.ntb.inf.deep.ssa.instruction.Call;
import ch.ntb.inf.deep.ssa.instruction.Dyadic;
import ch.ntb.inf.deep.ssa.instruction.Monadic;
import ch.ntb.inf.deep.ssa.instruction.MonadicString;
import ch.ntb.inf.deep.ssa.instruction.NoOpnd;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;
import ch.ntb.inf.deep.ssa.instruction.StoreToArray;

/**
 * @author  millischer
 */
public class SSANode extends CFGNode implements JvmInstructionMnemonics,
		SSAInstructionOpcs {
	boolean traversed;
	public int nofInstr;
	public int nofPhiFunc;
	private int maxLocals;
	private int maxStack;
	private int topStackframe;
	public SSAValue exitSet[];
	private SSAValue locals[];
	public SSAValue entrySet[];
	public PhiFunction phiFunctions[];
	public SSAInstruction instructions[];

	public SSANode() {
		super();
		instructions = new SSAInstruction[4];
		phiFunctions = new PhiFunction[2];
		traversed = false;
		nofInstr = 0;
		nofPhiFunc = 0;
		maxLocals = 0;
		maxStack = 0;

	}

	/**
	 * First it merges the state-arrays of the predecessors-nodes of this node.
	 * Creates Phi-Functions if there is more then one predecessor.
	 * 
	 * Second iterates over all the Bytecode Instructions and calls the
	 * corresponding Method
	 * 
	 * @param maxLocals
	 * @param maxStack
	 */
	public void mergeAndPopulateStateArray(SSA ssa) {

		maxLocals = ssa.cfg.method.getCodeAttribute().getMaxLocals();
		maxStack = ssa.cfg.method.getCodeAttribute().getMaxStack();

		// chek all predecessors have statearray set
		if (!isLoopHeader()) {
			for (int i = 0; predecessors[i] != null; i++) {
				if (((SSANode) predecessors[i]).exitSet == null) {
					// TODO throw empty exitSet Exception
				}
			}
		}
		if (nofPredecessors == 0) {
			// point the entry and exitset on localvariables(uninitialized)
			entrySet = new SSAValue[maxStack + maxLocals];
			exitSet = new SSAValue[maxStack + maxLocals];

		} else if (nofPredecessors == 1) {
			// only one predecessor --> no merge necessary
			if (this.equals(predecessors[0])) {// equal by "while(true){}
				entrySet = new SSAValue[maxStack + maxLocals];
				exitSet = new SSAValue[maxStack + maxLocals];
			} else {
				entrySet = ((SSANode) predecessors[0]).exitSet.clone();
			}
		} else if (nofPredecessors >= 2) {
			// multiple predecessors --> merge necessary
			if (isLoopHeader()) {
				// if true --> generate PhiFunctions for lacals
				if (nofInstr == 0) {
					// First Visit -->insert PhiFunction with 1 parameter
					// TODO First Visit force PhiFunction
				} else {
					// TODO Second Visit -->insert second param in PhiFunction

				}
			} else {
				// it isn't a loopheader
				for (int i = 0; i < nofPredecessors; i++) {
					if (entrySet == null) {
						// First Visit -->Creat Locals
						entrySet = ((SSANode) predecessors[i]).exitSet.clone();
					} else {
						// Second Visit --> merge
						// TODO merge an if necessary create new SSANode
					}
				}
			}
		}
		// Populate
		if (!traversed) {
			traversed = true;
			this.traversCode(ssa);
		}

	}

	public void traversCode(SSA ssa) {
		SSAValue value1, value2, value3, value4, result;
		int val, val1;
		SSAInstruction instr;
		boolean wide = false;
		locals = entrySet.clone();// Don't change the entryset
		// Determine top of the Stack
		for (topStackframe = maxStack; topStackframe >= 0 && locals[topStackframe] == null; topStackframe--);

		for (int bca = this.firstBCA; bca <= this.lastBCA; bca++) {
			int entry = bcAttrTab[ssa.cfg.code[bca] & 0xff];
			assert ((entry & (1 << bcapSSAnotImpl)) == 0) : "bytecode instruction not implemented";
			switch (ssa.cfg.code[bca] & 0xff) {
			case bCnop:
				break;
			case bCaconst_null:
				result = new SSAValue();
				result.type = SSAValue.tObject;
				result.constant = null;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_m1:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = -1;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_0:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 0;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_1:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 1;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_2:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 2;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_3:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 3;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_4:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 4;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_5:
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = 5;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClconst_0:
				result = new SSAValue();
				result.type = SSAValue.tLong;
				result.constant = 0;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClconst_1:
				result = new SSAValue();
				result.type = SSAValue.tLong;
				result.constant = 1;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_0:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = 0.0f;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_1:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = 1.0f;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_2:
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				result.constant = 2.0f;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdconst_0:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = 0.0;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdconst_1:
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				result.constant = 1.0;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCbipush:
				// get byte from Bytecode
				bca++;
				val = ssa.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = val;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCsipush:
				// get short from Bytecode
				bca++;
				val = (ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				result.constant = val;
				instr = new NoOpnd(sCloadConst);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCldc:
				// TODO How to access the Runtime Constantpool?
				break;
			case bCldc_w:
				// TODO How to access the Runtime Constantpool?
				break;
			case bCldc2_w:
				// TODO How to access the Runtime Constantpool?
				break;
			case bCiload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tInteger);
				break;
			case bClload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tLong);
				break;
			case bCfload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tFloat);
				break;
			case bCdload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tDouble);
				break;
			case bCaload:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				load(val, SSAValue.tObject);
				break;
			case bCiload_0:
				load(0, SSAValue.tInteger);
				break;
			case bCiload_1:
				load(1, SSAValue.tInteger);
				break;
			case bCiload_2:
				load(2, SSAValue.tInteger);
				break;
			case bCiload_3:
				load(3, SSAValue.tInteger);
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
				load(0, SSAValue.tObject);
				break;
			case bCaload_1:
				load(1, SSAValue.tObject);
				break;
			case bCaload_2:
				load(2, SSAValue.tObject);
				break;
			case bCaload_3:
				load(3, SSAValue.tObject);
				break;
			case bCiaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCaaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tObject;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCbaload:
				// TODO Remember the result type isn't set here (it could be
				// boolean or byte)
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCcaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tChar;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCsaload:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tShort;
				instr = new Dyadic(sCloadFromArray, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCistore:
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get
																					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				locals[maxStack + val] = popFromStack();
				break;
			case bClstore:
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get
																					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				locals[maxStack + val] = popFromStack();
				break;
			case bCfstore:
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get
																					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				locals[maxStack + val] = popFromStack();
				break;
			case bCdstore:
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get
																					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				locals[maxStack + val] = popFromStack();
				break;
			case bCastore:
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get
																					// index
					wide = false;
				} else {
					val = (ssa.cfg.code[bca] & 0xff);// get index
				}
				locals[maxStack + val] = popFromStack();
				break;
			case bCistore_0:
				locals[maxStack] = popFromStack();
				break;
			case bCistore_1:
				locals[maxStack + 1] = popFromStack();
				break;
			case bCistore_2:
				locals[maxStack + 2] = popFromStack();
				break;
			case bCistore_3:
				locals[maxStack + 3] = popFromStack();
				break;
			case bClstore_0:
				locals[maxStack] = popFromStack();
				break;
			case bClstore_1:
				locals[maxStack + 1] = popFromStack();
				break;
			case bClstore_2:
				locals[maxStack + 2] = popFromStack();
				break;
			case bClstore_3:
				locals[maxStack + 3] = popFromStack();
				break;
			case bCfstore_0:
				locals[maxStack] = popFromStack();
				break;
			case bCfstore_1:
				locals[maxStack + 1] = popFromStack();
				break;
			case bCfstore_2:
				locals[maxStack + 2] = popFromStack();
				break;
			case bCfstore_3:
				locals[maxStack + 3] = popFromStack();
				break;
			case bCdstore_0:
				locals[maxStack] = popFromStack();
				break;
			case bCdstore_1:
				locals[maxStack + 1] = popFromStack();
				break;
			case bCdstore_2:
				locals[maxStack + 2] = popFromStack();
				break;
			case bCdstore_3:
				locals[maxStack + 3] = popFromStack();
				break;
			case bCastore_0:
				locals[maxStack] = popFromStack();
				break;
			case bCastore_1:
				locals[maxStack + 1] = popFromStack();
				break;
			case bCastore_2:
				locals[maxStack + 2] = popFromStack();
				break;
			case bCastore_3:
				locals[maxStack + 3] = popFromStack();
				break;
			case bCiastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bClastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCfastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCdastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCaastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tObject;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCbastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				// TODO Remember the result type isn't set here (could be
				// boolean or byte)
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCcastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tChar;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCsastore:
				value3 = popFromStack();
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tShort;
				instr = new StoreToArray(sCstoreToArray, value1, value2,
						value3);
				instr.setResult(result);
				addInstruction(instr);
				break;
			case bCpop:
				popFromStack();
				break;
			case bCpop2:
				value1 = popFromStack();
				if (!((value1.type == SSAValue.tLong) || (value1.type == SSAValue.tDouble))) {// false
																								// if
																								// value1
																								// is
																								// a
																								// value
																								// of
																								// a
																								// category
																								// 2
																								// computational
																								// type
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
				if ((value2.type == SSAValue.tLong)
						|| (value2.type == SSAValue.tDouble)) {// true if
																// value2 is a
																// value of a
																// category 2
																// computational
																// type
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
				if ((value1.type == SSAValue.tLong)
						|| (value1.type == SSAValue.tDouble)) {// true if
																// value1 is a
																// value of a
																// category 2
																// computational
																// type
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
				if ((value1.type == SSAValue.tLong)
						|| (value1.type == SSAValue.tDouble)) {// true if
																// value1 is a
																// value of a
																// category 2
																// computational
																// type
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
				if ((value1.type == SSAValue.tLong)
						|| (value1.type == SSAValue.tDouble)) {// true if
																// value1 is a
																// value of a
																// category 2
																// computational
																// type
					if ((value2.type == SSAValue.tLong)
							|| (value2.type == SSAValue.tDouble)) {// true if
																	// value2 is
																	// a value
																	// of a
																	// category
																	// 2
																	// computational
																	// type
						// Form4 (the java virtual Machine Specification second
						// edition, Tim Lindholm, Frank Yellin, page 223)
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
					if ((value3.type == SSAValue.tLong)	|| (value3.type == SSAValue.tDouble)) {// true if value3 is
																	// a value
																	// of a
																	// category
																	// 2
																	// computational
																	// type
						// Form 3 (the java virtual Machine Specification second
						// edition, Tim Lindholm, Frank Yellin, page 223)
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
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCadd, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCladd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCadd, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfadd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCadd, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdadd:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCadd, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCisub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCsub, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCsub, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCsub, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdsub:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCsub, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCimul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCmul, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCmul, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCmul, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdmul:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCmul, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCidiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCldiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfdiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCddiv:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCdiv, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCirem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCrem, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCrem, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Dyadic(sCrem, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdrem:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Dyadic(sCrem, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCineg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Monadic(sCneg, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCneg, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCneg, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdneg:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCneg, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCishl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCshl, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClshl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCshl, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCishr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCshr, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClshr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCshr, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiushr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCushr, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClushr:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCushr, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiand:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCand, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCland:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCand, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCior:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCor, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCor, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCixor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCxor, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClxor:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Dyadic(sCxor, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiinc:
				// TODO is that right??
				bca++;
				if (wide) {
					val = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca++]) & 0xffff;// get index
					val1 = ((ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca]) & 0xffff;// get const
					wide = false;
				} else {
					val = ssa.cfg.code[bca++] & 0xff;// get index
					val1 = ssa.cfg.code[bca] & 0xff;// get const
				}

				load(val, SSAValue.tInteger);
				value1 = popFromStack();

				value2 = new SSAValue();
				value2.type = SSAValue.tInteger;
				value2.constant = val1;

				result = new SSAValue();
				result.type = SSAValue.tInteger;

				instr = new Dyadic(sCadd, value1, value2);
				instr.setResult(result);
				addInstruction(instr);

				locals[maxStack + val] = result;
				break;
			case bCi2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCi2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCi2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCl2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Monadic(sCconvLong, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCl2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvLong, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCl2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvLong, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCf2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Monadic(sCconvFloat, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCf2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvFloat, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCf2d:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tDouble;
				instr = new Monadic(sCconvFloat, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCd2i:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Monadic(sCconvDouble, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCd2l:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tLong;
				instr = new Monadic(sCconvDouble, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCd2f:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tFloat;
				instr = new Monadic(sCconvDouble, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCi2b:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tByte;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCi2c:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tChar;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCi2s:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tShort;
				instr = new Monadic(sCconvInt, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClcmp:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfcmpl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfcmpg:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCcmpg, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdcmpl:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCcmpl, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdcmpg:
				value2 = popFromStack();
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new Dyadic(sCcmpg, value1, value2);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCifeq:
			case bCifne:
			case bCiflt:
			case bCifge:
			case bCifgt:
			case bCifle:
				popFromStack();
				bca = bca+2; //step over branchbyte1 and branchbyte2
				break;
			case bCif_icmpeq:
			case bCif_icmpne:
			case bCif_icmplt:
			case bCif_icmpge:
			case bCif_icmpgt:
			case bCif_icmple:
			case bCif_acmpeq:
			case bCif_acmpne:
				popFromStack();
				popFromStack();
				bca = bca+2; //step over branchbyte1 and branchbyte2
				break;
			case bCgoto:
				bca = bca+2; //step over branchbyte1 and branchbyte2
				break;
			case bCjsr:
				//TODO I think it isn't necessary to push the adress onto the stack
				bca = bca+2; //step over branchbyte1 and branchbyte2
				break;
			case bCret:
				if (wide) {
					bca = bca+2; //step over indexbyte1 and indexbyte2
					wide = false;
				} else {
					bca++;//step over index
				}
				break;
			case bCtableswitch:
				popFromStack();
				//Step over whole bytecode instruction
				bca++;
				//pad bytes
				while((bca & 0x03) != 0){
					bca++;
				}
				//default jump adress
				bca = bca+4;
				//we need the low and high
				int low1 = (ssa.cfg.code[bca++]<<24)|(ssa.cfg.code[bca++]<<16)|(ssa.cfg.code[bca++]<<8)|ssa.cfg.code[bca++];
				int high1 =(ssa.cfg.code[bca++]<<24)|(ssa.cfg.code[bca++]<<16)|(ssa.cfg.code[bca++]<<8)|ssa.cfg.code[bca++];
				int nofPair1 = high1-low1+1;
				
				//jump offsets
				bca = bca + 4*nofPair1 - 1;
				break;
			case bClookupswitch:
				popFromStack();
				//Step over whole bytecode instruction
				bca++;
				//pad bytes
				while((bca & 0x03) != 0){
					bca++;
				}
				//default jump adress
				bca = bca+4;
				//npairs
				int nofPair2 = (ssa.cfg.code[bca++]<<24)|(ssa.cfg.code[bca++]<<16)|(ssa.cfg.code[bca++]<<8)|ssa.cfg.code[bca++];
				//jump offsets
				bca = bca + 8*nofPair2 - 1;
				break;
			case bCireturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bClreturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bCfreturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bCdreturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bCareturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bCreturn:
				//discard Stack
				while(topStackframe >= 0){
					locals[topStackframe]= null;
					topStackframe--;
				}
				break;
			case bCgetstatic:
				//TODO access into Constant pool
				break;
			case bCputstatic:
				//TODO access into Constant pool
				break;
			case bCgetfield:
				//TODO access into Constant pool
				break;
			case bCputfield:
				//TODO access into Constant pool
				break;
			case bCinvokevirtual:
				//TODO access into Constant pool
				break;
			case bCinvokespecial:
				//TODO access into Constant pool
				break;
			case bCinvokestatic:
				//TODO access into Constant pool
				break;
			case bCinvokeinterface:
				//TODO access into Constant pool
				break;
			case bCnew:
				//TODO access into Constant pool
				break;
			case bCnewarray:
				bca++;
				val = ssa.cfg.code[bca] & 0xff;//atype
				value1 = popFromStack();
				result = new SSAValue();
				result.type = val+10;
				SSAValue[] operand = {value1};
				instr = new Call(sCnew, operand);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCanewarray:
				//TODO access into Constant pool
				break;
			case bCarraylength:
				value1 = popFromStack();
				result = new SSAValue();
				result.type = SSAValue.tInteger;
				instr = new MonadicString(sCalength, value1);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCathrow:
				//TODO ??
				value1 = popFromStack();
				//clear stack
				while(topStackframe >=0){
					locals[topStackframe]=null;
					topStackframe--;
				}
				pushToStack(value1);
				break;
			case bCcheckcast:
				//TODO access into Constant pool
				break;
			case bCinstanceof:
				//TODO access into Constant pool
				break;
			case bCmonitorenter:
				//TODO ??
				break;
			case bCmonitorexit:
				//TODO ??
				break;
			case bCwide:
				wide = true;
				break;
			case bCmultianewarray:
				break;
			case bCifnull:
			case bCifnonnull:
				popFromStack();
				bca = bca+2; //step over branchbyte1 and branchbyte2
				break;
			case bCgoto_w:
				bca = bca+4; //step over branchbyte1 and branchbyte2...
				break;
			case bCjsr_w:
				//TODO I think it isn't necessary to push the adress onto the stack
				bca = bca+4; //step over branchbyte1 and branchbyte2...
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
		if (topStackframe + 1 >= maxStack) {
			throw new IndexOutOfBoundsException("Stack overflow");
		}
		locals[topStackframe + 1] = value;
		topStackframe++;

	}

	private SSAValue popFromStack() {
		SSAValue val;
		if (topStackframe < 0) {
			throw new IndexOutOfBoundsException("Empty Stack");
		}
		val = locals[topStackframe];
		locals[topStackframe] = null;
		topStackframe--;
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
		instructions[nofInstr] = instr;
		nofInstr++;
	}

	private void load(int index, int type) {
		SSAValue result = locals[maxStack + index];

		if (result == null) {// Local isn't initialized
			result = new SSAValue();
			result.type = type;
			Local operand = new Local(index);
			SSAInstruction instr = new Monadic(sCloadVar, operand);
			instr.setResult(result);
			addInstruction(instr);
			locals[maxStack + index] = result;
		}

		pushToStack(result);

	}

}