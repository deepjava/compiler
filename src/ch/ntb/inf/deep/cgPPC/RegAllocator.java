package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import static org.junit.Assert.*;
/**
 * register allocation
 * 
 * @author graf
 * 
 */
public class RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics, Registers {

	private static final boolean gpr = true;
	private static final boolean fpr = false;
	private static final boolean vol = true;
	private static final boolean nonVol = false;
	
	static int regsGPR, regsFPR;
	private static int[] regs;
	public static SSAValue[] lastExitSet;
	
	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	public static void allocateRegisters(SSA ssa) {
		regsGPR = volRegsGPRinitial | nonVolRegsGPRinitial;
		regsFPR = volRegsFPRinitial | nonVolRegsFPRinitial;
		regs = new int[ssa.cfg.method.maxLocals];

		System.out.println("allocate registers for " + ssa.cfg.method.name);
		insertRegMoves(ssa);
		SSA.renumberInstructions(ssa.cfg);
		buildIntervals(ssa);	// compute live intervals
		resolvePhiFunctions(ssa);
		assignRegisters(ssa);

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
	 * Computes the live ranges of all SSAValues 
	 */
	private static void buildIntervals(SSA ssa) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				int currNo = phi.result.n;
				SSAValue[] opds = phi.getOperands();
				for (SSAValue opd : opds) opd.end = currNo;
			}

			// for all instructions i in b do
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				int currNo = instr.result.n;
				instr.result.end = currNo;
				SSAValue[] opds = instr.getOperands();
				if (opds != null) { 
					for (SSAValue opd : opds) opd.end = currNo;
				}
			}
			b = (SSANode) b.next;
		}
	}

	/**
	 * Resolve phi functions 
	 */
	private static void resolvePhiFunctions(SSA ssa) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				PhiFunction phi = b.phiFunctions[i];
				handlePhiFunction(ssa, phi);
			}
			b = (SSANode) b.next;
		}
	}

	private static void handlePhiFunction(SSA ssa, PhiFunction phi) {
		if (phi.visited) return;
		phi.visited = true;
		SSAValue[] opds = phi.getOperands();
		System.out.println("handle phi function on line " + phi.result.n);
		for (int i = 0; i < opds.length; i++) {
			if (opds[i].type == tPhiFunc) {
				int ssaLine = opds[i].n;
				PhiFunction phi1 = searchPhiFunction(ssa, ssaLine);
				handlePhiFunction(ssa, phi1);
			}
			if (opds[i].index < 0)
				opds[i].index = phi.result.index;
		}
		phi.result.type = opds[0].type;
	}

	private static PhiFunction searchPhiFunction(SSA ssa, int n) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				PhiFunction phi = b.phiFunctions[i];
				if (phi.result.n == n) return phi;
			}
			b = (SSANode) b.next;		
		}
		return null;
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 */
	private static void assignRegisters(SSA ssa) {
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) b = (SSANode) b.next;
		lastExitSet = b.exitSet;
		parseExitSet(ssa, b);
		
		b = (SSANode) ssa.cfg.rootNode;
		while (b != null) {
			parseExitSet(ssa, b);
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				SSAValue[] opds = phi.getOperands();
				SSAValue res = phi.result;
				phi.result.reg = regs[phi.result.index];
			}
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				System.out.println("ssa opcode = " + instr.scMnemonics[instr.ssaOpcode]);
				System.out.println("\tregsGPR before assigning = " + Integer.toHexString(regsGPR));
				// reserve additional volatile registers
				switch (instr.ssaOpcode) {
				case sCloadConst:
				case sCcall:
				case sCloadFromField:
				case sCstoreToField:
					instr.result.regAux1 = reserveReg(gpr, vol);
					break;
				case sCloadFromArray:
				case sCstoreToArray:
					instr.result.regAux1 = reserveReg(gpr, vol);
					instr.result.regAux2 = reserveReg(gpr, vol);
					break;
				default:
					break;
				}
				if (instr.result.regAux1 != -1) freeVolReg(gpr, instr.result.regAux1);
				if (instr.result.regAux2 != -1) freeVolReg(gpr, instr.result.regAux2);
				SSAValue[] opds = instr.getOperands();
				SSAValue res = instr.result;
				if (opds != null) {
					// free volatile registers reserved for operands of this instruction
					for (SSAValue opd : opds) {
						SSAValue startVal = b.instructions[0].result;
						if ((opd.reg >= -1) && (opd.end <= startVal.n + i)) {
							switch (opd.type) {
							case tFloat: case tDouble:
								freeVolReg(fpr, opd.reg);
								break;
							case tRef: case tBoolean: case tChar: case tByte:
							case tShort: case tInteger: case tAref: case tAboolean:
							case tAchar: case tAfloat: case tAdouble: case tAbyte:
							case tAshort: case tAinteger: case tAlong:
//								System.out.println("free volatile reg " + opd.reg);
								freeVolReg(gpr, opd.reg);
								break;
							case tLong:
								freeVolReg(gpr, opd.reg);
								freeVolReg(gpr, opd.regLong);
								break;
							default:
								System.out.println("cfg = " + ssa.cfg.method.name);
								System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
								System.out.println("type = " + svNames[opd.type]);
								assert false : "type not implemented";
							}
							System.out.println("\tfree register of opd " + opd.n + ", regsGPR is now = " + Integer.toHexString(regsGPR));
							System.out.println("\topd.reg = " + opd.reg + ", opd.end = " + opd.end + ", startVal.n = " + startVal.end + ", i = " + i);
						}
					}
				}
				if (instr.ssaOpcode != sCbranch) {	// branch instruction has no result
					if (instr.result.index < 0) { 
						switch (instr.result.type) {
						case tFloat:
						case tDouble:
							instr.result.reg = reserveReg(fpr, vol);
							break;
						case tRef: case tBoolean: case tChar: 
						case tByte: case tShort: case tInteger: 	
						case tAref: case tAboolean: case tAchar: case tAfloat:
						case tAdouble: case tAbyte: case tAshort: case tAinteger:
						case tAlong:
							instr.result.reg = reserveReg(gpr, vol);
							break;
						case tLong:
							instr.result.reg = reserveReg(gpr, vol);
							instr.result.regLong = reserveReg(gpr, vol);
							break;
						case tVoid:
							break;
						default:
							System.out.println("cfg = " + ssa.cfg.method.name);
							System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
							System.out.println("type = " + svNames[instr.result.type]);
							assert false : "type not implemented";
						}
					}  else {	// is a local variable
//						System.out.println("reserve nonvolatile");
						instr.result.reg = regs[instr.result.index];
						if (instr.result.type == tLong)
							instr.result.regLong = regs[instr.result.index+1];					
//						System.out.println("register assigned " + instr.result.reg);
					}
				}
			}
			b = (SSANode) b.next;
		}	
	}

	private static void parseExitSet(SSA ssa, SSANode b) {
		int nofGPR = 0, nofFPR = 0;
		regsGPR |= nonVolRegsGPRinitial;
		regsFPR |= nonVolRegsFPRinitial;
		for (int i = 0; i < b.maxLocals; i++) {
			SSAValue val = b.exitSet[b.maxStack + i];
			if (val == null) {
				val = lastExitSet[b.maxStack + i];
			}
			if (val != null) {
				switch (val.type) {
				case tFloat:
					regs[i] = reserveReg(fpr, nonVol);
					nofFPR++;
					break;
				case tDouble:
					regs[i] = reserveReg(fpr, nonVol);
					nofFPR++;
					i++;
					break;
				case tRef: case tBoolean: case tChar: 
				case tByte: case tShort: case tInteger: 	
				case tAref: case tAboolean: case tAchar: case tAfloat:
				case tAdouble: case tAbyte: case tAshort: case tAinteger:
				case tAlong:
					regs[i] = reserveReg(gpr, nonVol);
					nofGPR++;
					break;
				case tLong:
					regs[i] = reserveReg(gpr, nonVol);
					regs[i+1] = reserveReg(gpr, nonVol);
					nofGPR += 2;
					i++;
					break;
				case tPhiFunc:
					break;
				default:
					System.out.println("cfg = " + ssa.cfg.method.name);
					//				System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
					System.out.println("type = " + svNames[val.type]);
					assert false : "type not implemented";
				}
				/*			if ((i == 0) && (ssa.paramType[b.maxStack] == SSAValue.tRef)) {
//				System.out.println("reserve GPR for this");
				regs[i] = reserveReg(gpr, nonVol);
				nofGPR++;
			}			*/	
				if (nofGPR > ssa.nofGPR) ssa.nofGPR = nofGPR;
				if (nofFPR > ssa.nofFPR) ssa.nofFPR = nofFPR;
			}
		}	
	}

	private static int reserveReg(boolean isGPR, boolean isVolatile) {
		int regs;
		if (isGPR) {
			if (isVolatile) {
				regs = regsGPR & volRegsGPRinitial;
				int i = 0;
				while (regs != 0) {
					if ((regs & 1) != 0) {
						regsGPR &= ~(1 << i);
						System.out.println("\tregsGPR is now = " + Integer.toHexString(regsGPR));
						return i;
					}
					regs /= 2;
					i++;
				}
				assert false: "not enough registers for volatile GPRs";
				return 0;
			} else {
				regs = regsGPR & nonVolRegsGPRinitial;			
				int i = 31;
				while (regs != 0) {
					if ((regs & (1 << i)) != 0) {
						regsGPR &= ~(1 << i);
						return i;
					}
					i--;
				}
				assert false: "not enough registers for nonvolatile GPRs";
				return 0;
			}
		} else {
			if (isVolatile) {
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
			} else {
				regs = regsFPR & nonVolRegsFPRinitial;			
				int i = 31;
				while (regs != 0) {
					if ((regs & (1 << i)) != 0) {
						regsFPR &= ~(1 << i);
						return i;
					}
					i--;
				}
				assert false: "not enough registers for nonvolatile FPRs";
				return 0;
			}		
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