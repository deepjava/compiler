package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cfg.JvmInstructionMnemonics;
import ch.ntb.inf.deep.ssa.instruction.Dyadic;
import ch.ntb.inf.deep.ssa.instruction.Monadic;
import ch.ntb.inf.deep.ssa.instruction.NoOpnd;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

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
		SSAValue value1, value2, result;
		int val;
		SSAInstruction instr;
		locals = entrySet.clone();// Don't change the entryset
		// Determine top of the Stack
		for (topStackframe = maxStack; topStackframe >= 0
				&& locals[topStackframe] == null; topStackframe--)
			;

		for (int bca = this.firstBCA; bca <= this.lastBCA; bca++) {
			int entry = bcAttrTab[ssa.cfg.code[bca] & 0xff];
			assert ((entry & (1 << bcapSSAnotImpl)) == 0) : "bytecode instruction not implemented";
			switch (ssa.cfg.code[bca] & 0xff) {
			case bCnop:
				break;
			case bCaconst_null:
				result = new SSAValue();
				result.type = SSAValue.t_object;
				result.constant = null;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_m1:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = -1;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_0:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 0;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_1:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 1;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_2:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 2;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_3:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 3;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_4:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 4;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCiconst_5:
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = 5;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClconst_0:
				result = new SSAValue();
				result.type = SSAValue.t_long;
				result.constant = 0;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bClconst_1:
				result = new SSAValue();
				result.type = SSAValue.t_long;
				result.constant = 1;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_0:
				result = new SSAValue();
				result.type = SSAValue.t_float;
				result.constant = 0.0f;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_1:
				result = new SSAValue();
				result.type = SSAValue.t_float;
				result.constant = 1.0f;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCfconst_2:
				result = new SSAValue();
				result.type = SSAValue.t_float;
				result.constant = 2.0f;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdconst_0:
				result = new SSAValue();
				result.type = SSAValue.t_double;
				result.constant = 0.0;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCdconst_1:
				result = new SSAValue();
				result.type = SSAValue.t_double;
				result.constant = 1.0;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCbipush:
				// get byte from Bytecode
				bca++;
				val = ssa.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = val;
				instr = new NoOpnd(sCload_const);
				instr.setResult(result);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCsipush:
				// get short from Bytecode
				bca++;
				val = (ssa.cfg.code[bca++] << 8) | ssa.cfg.code[bca];// sign-extended
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				result.constant = val;
				instr = new NoOpnd(sCload_const);
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
				val = (ssa.cfg.code[bca] & 0xff);//get index
				load(val, SSAValue.t_integer);
				break;
			case bClload:
				bca++;
				val = (ssa.cfg.code[bca] & 0xff);//get index
				load(val, SSAValue.t_long);
				break;
			case bCfload:
				bca++;
				val = (ssa.cfg.code[bca] & 0xff);//get index
				load(val, SSAValue.t_float);
				break;
			case bCdload:
				bca++;
				val = (ssa.cfg.code[bca] & 0xff);//get index
				load(val, SSAValue.t_double);
				break;
			case bCaload:
				bca++;
				val = (ssa.cfg.code[bca] & 0xff);//get index
				load(val, SSAValue.t_object);
				break;
			case bCiload_0:
				load(0, SSAValue.t_integer);
				break;
			case bCiload_1:
				load(1, SSAValue.t_integer);
				break;
			case bCiload_2:
				load(2, SSAValue.t_integer);
				break;
			case bCiload_3:
				load(3, SSAValue.t_integer);
				break;
			case bClload_0:
				load(0, SSAValue.t_long);
				break;
			case bClload_1:
				load(1, SSAValue.t_long);
				break;
			case bClload_2:
				load(2, SSAValue.t_long);
				break;
			case bClload_3:
				load(3, SSAValue.t_long);
				break;
			case bCfload_0:
				load(0, SSAValue.t_float);
				break;
			case bCfload_1:
				load(1, SSAValue.t_float);
				break;
			case bCfload_2:
				load(2, SSAValue.t_float);
				break;
			case bCfload_3:
				load(3, SSAValue.t_float);
				break;
			case bCdload_0:
				load(0, SSAValue.t_double);
				break;
			case bCdload_1:
				load(1, SSAValue.t_double);
				break;
			case bCdload_2:
				load(2, SSAValue.t_double);
				break;
			case bCdload_3:
				load(3, SSAValue.t_double);
				break;
			case bCaload_0:
				load(0, SSAValue.t_object);
				break;
			case bCaload_1:
				load(1, SSAValue.t_object);
				break;
			case bCaload_2:
				load(2, SSAValue.t_object);
				break;
			case bCaload_3:
				load(3, SSAValue.t_object);
				break;
			case bCiaload:
				break;
			case bClaload:
				break;
			case bCfaload:
				break;
			case bCdaload:
				break;
			case bCaaload:
				break;
			case bCbaload:
				break;
			case bCcaload:
				break;
			case bCsaload:
				break;
			case bCistore:
				break;
			case bClstore:
				break;
			case bCfstore:
				break;
			case bCdstore:
				break;
			case bCastore:
				break;
			case bCistore_0:
				break;
			case bCistore_1:
				break;
			case bCistore_2:
				break;
			case bCistore_3:
				break;
			case bClstore_0:
				break;
			case bClstore_1:
				break;
			case bClstore_2:
				break;
			case bClstore_3:
				break;
			case bCfstore_0:
				break;
			case bCfstore_1:
				break;
			case bCfstore_2:
				break;
			case bCfstore_3:
				break;
			case bCdstore_0:
				break;
			case bCdstore_1:
				break;
			case bCdstore_2:
				break;
			case bCdstore_3:
				break;
			case bCastore_0:
				break;
			case bCastore_1:
				break;
			case bCastore_2:
				break;
			case bCastore_3:
				break;
			case bCiastore:
				break;
			case bClastore:
				break;
			case bCfastore:
				break;
			case bCdastore:
				break;
			case bCaastore:
				break;
			case bCbastore:
				break;
			case bCcastore:
				break;
			case bCsastore:
				break;
			case bCpop:
				break;
			case bCpop2:
				break;
			case bCdup:
				break;
			case bCdup_x1:
				break;
			case bCdup_x2:
				break;
			case bCdup2:
				break;
			case bCdup2_x1:
				break;
			case bCdup2_x2:
				break;
			case bCswap:
				break;
			case bCiadd:
				value2 = popFromstack();
				value1 = popFromstack();
				result = new SSAValue();
				result.type = SSAValue.t_integer;
				instr = new Dyadic(sCadd, value1, value2);
				addInstruction(instr);
				pushToStack(result);
				break;
			case bCladd:
				break;
			case bCfadd:
				break;
			case bCdadd:
				break;
			case bCisub:
				break;
			case bClsub:
				break;
			case bCfsub:
				break;
			case bCdsub:
				break;
			case bCimul:
				break;
			case bClmul:
				break;
			case bCfmul:
				break;
			case bCdmul:
				break;
			case bCidiv:
				break;
			case bCldiv:
				break;
			case bCfdiv:
				break;
			case bCddiv:
				break;
			case bCirem:
				break;
			case bClrem:
				break;
			case bCfrem:
				break;
			case bCdrem:
				break;
			case bCineg:
				break;
			case bClneg:
				break;
			case bCfneg:
				break;
			case bCdneg:
				break;
			case bCishl:
				break;
			case bClshl:
				break;
			case bCishr:
				break;
			case bClshr:
				break;
			case bCiushr:
				break;
			case bClushr:
				break;
			case bCiand:
				break;
			case bCland:
				break;
			case bCior:
				break;
			case bClor:
				break;
			case bCixor:
				break;
			case bClxor:
				break;
			case bCiinc:
				break;
			case bCi2l:
				break;
			case bCi2f:
				break;
			case bCi2d:
				break;
			case bCl2i:
				break;
			case bCl2f:
				break;
			case bCl2d:
				break;
			case bCf2i:
				break;
			case bCf2l:
				break;
			case bCf2d:
				break;
			case bCd2i:
				break;
			case bCd2l:
				break;
			case bCd2f:
				break;
			case bCi2b:
				break;
			case bCi2c:
				break;
			case bCi2s:
				break;
			case bClcmp:
				break;
			case bCfcmpl:
				break;
			case bCfcmpg:
				break;
			case bCdcmpl:
				break;
			case bCdcmpg:
				break;
			case bCifeq:
				break;
			case bCifne:
				break;
			case bCiflt:
				break;
			case bCifge:
				break;
			case bCifgt:
				break;
			case bCifle:
				break;
			case bCif_icmpeq:
				break;
			case bCif_icmpne:
				break;
			case bCif_icmplt:
				break;
			case bCif_icmpge:
				break;
			case bCif_icmpgt:
				break;
			case bCif_icmple:
				break;
			case bCif_acmpeq:
				break;
			case bCif_acmpne:
				break;
			case bCgoto:
				break;
			case bCjsr:
				break;
			case bCret:
				break;
			case bCtableswitch:
				break;
			case bClookupswitch:
				break;
			case bCireturn:
				break;
			case bClreturn:
				break;
			case bCfreturn:
				break;
			case bCdreturn:
				break;
			case bCareturn:
				break;
			case bCreturn:
				break;
			case bCgetstatic:
				break;
			case bCputstatic:
				break;
			case bCgetfield:
				break;
			case bCputfield:
				break;
			case bCinvokevirtual:
				break;
			case bCinvokespecial:
				break;
			case bCinvokestatic:
				break;
			case bCinvokeinterface:
				break;
			case bCnew:
				break;
			case bCnewarray:
				break;
			case bCanewarray:
				break;
			case bCarraylength:
				break;
			case bCathrow:
				break;
			case bCcheckcast:
				break;
			case bCinstanceof:
				break;
			case bCmonitorenter:
				break;
			case bCmonitorexit:
				break;
			case bCwide:
				break;
			case bCmultianewarray:
				break;
			case bCifnull:
				break;
			case bCifnonnull:
				break;
			case bCgoto_w:
				break;
			case bCjsr_w:
				break;
			case bCbreakpoint:
				break;
			default:
				break;

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

	private SSAValue popFromstack() {
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
		
		if(result == null){//Local isn't initialized
			result = new SSAValue();
			result.type = type;
			Local operand = new Local(index);
			SSAInstruction instr = new Monadic(sCload_var, operand);
			instr.setResult(result);
			addInstruction(instr);
			locals[maxStack + index]= result;
		}
	
		pushToStack(result);
		
	}

}