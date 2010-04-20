package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cfg.JvmInstructionMnemonics;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class SSANode extends CFGNode implements JvmInstructionMnemonics,
		SSAInstructionOpcs {
	boolean traversed;
	public int nofInstr;
	public int nofPhiFunc;
	public SSAValue exitSet[];
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

		int maxLocals = ssa.cfg.method.getCodeAttribute().getMaxLocals();
		int maxStack = ssa.cfg.method.getCodeAttribute().getMaxStack();

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

			// empty stackpart
			for (int i = maxStack; i < maxStack + maxLocals; i++) {
				SSAValue val = new Local(i - maxStack);
				entrySet[i] = val;
				exitSet[i] = val;
			}
		} else if (nofPredecessors == 1) {
			// only one predecessor --> no merge necessary
			if (this.equals(predecessors[0])) {// equal by "while(true){}
				entrySet = new SSAValue[maxStack + maxLocals];
				exitSet = new SSAValue[maxStack + maxLocals];

				// empty stackpart
				for (int i = maxStack; i < maxStack + maxLocals; i++) {
					SSAValue val = new Local(i - maxStack);
					entrySet[i] = val;
					exitSet[i] = val;
				}
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

		}

	}

	public void traversCode(SSA ssa) {

		for (int bca = this.firstBCA; bca <= this.lastBCA; bca++) {
			int entry = bcAttrTab[ssa.cfg.code[bca] & 0xff];
			assert ((entry & (1 << bcapSSAnotImpl)) == 0) : "bytecode instruction not implemented";
			switch (ssa.cfg.code[bca] & 0xff) {
			case bCnop:
				break;
			case bCaconst_null:
				break;
			case bCiconst_m1:
				break;
			case bCiconst_0:
				break;
			case bCiconst_1:
				break;
			case bCiconst_2:
				break;
			case bCiconst_3:
				break;
			case bCiconst_4:
				break;
			case bCiconst_5:
				break;
			case bClconst_0:
				break;
			case bClconst_1:
				break;
			case bCfconst_0:
				break;
			case bCfconst_1:
				break;
			case bCfconst_2:
				break;
			case bCdconst_0:
				break;
			case bCdconst_1:
				break;
			case bCbipush:
				break;
			case bCsipush:
				break;
			case bCldc:
				break;
			case bCldc_w:
				break;
			case bCldc2_w:
				break;
			case bCiload:
				break;
			case bClload:
				break;
			case bCfload:
				break;
			case bCdload:
				break;
			case bCaload:
				break;
			case bCiload_0:
				break;
			case bCiload_1:
				break;
			case bCiload_2:
				break;
			case bCiload_3:
				break;
			case bClload_0:
				break;
			case bClload_1:
				break;
			case bClload_2:
				break;
			case bClload_3:
				break;
			case bCfload_0:
				break;
			case bCfload_1:
				break;
			case bCfload_2:
				break;
			case bCfload_3:
				break;
			case bCdload_0:
				break;
			case bCdload_1:
				break;
			case bCdload_2:
				break;
			case bCdload_3:
				break;
			case bCaload_0:
				break;
			case bCaload_1:
				break;
			case bCaload_2:
				break;
			case bCaload_3:
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

}