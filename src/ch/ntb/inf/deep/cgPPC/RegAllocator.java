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
public class RegAllocator implements SSAInstructionOpcs, SSAValueType, SSAInstructionMnemonics {
	private static final int TopGPR = 31;
	private static final int TopFPR = 31;
	private static final int volRegsGPRinitial = 0x00001ffc;
	private static final int nonVolRegsGPRinitial = 0xffffe000;
	private static final int volRegsFPRinitial = 0x000001fe;
	private static final int nonVolRegsFPRinitial = 0xfffffe00;

	private static int volRegsGPR, volRegsFPR;
	private static int nonVolRegsGPR, nonVolRegsFPR;
	private static int[] regs;
	
	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	public static void allocateRegisters(SSA ssa) {
		volRegsGPR = volRegsGPRinitial;
		nonVolRegsGPR = nonVolRegsGPRinitial;
		volRegsFPR = volRegsFPRinitial;
		nonVolRegsFPR = nonVolRegsFPRinitial;
		regs = new int[ssa.cfg.method.maxLocals];

		System.out.println("allocate registers for " + ssa.cfg.method.name);
		buildIntervals(ssa);	// compute live intervals
		resolvePhiFunctions(ssa);
		assignRegisters(ssa);

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
			for (int i = 0; i < b.maxLocals; i++) {
				SSAValue val = b.exitSet[b.maxStack + i];
				if ((val != null) && (val.type == tPhiFunc)) {
					val.index = i;
				}
			}
			b = (SSANode) b.next;
		}

		b = (SSANode) ssa.cfg.rootNode;
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
				System.out.println("\t\thas phi as opds");
				int ssaLine = opds[i].n;
				PhiFunction phi1 = searchPhiFunction(ssa, ssaLine);
				handlePhiFunction(ssa, phi1);
			}
		}
		if (phi.result.index >= 0) {
			System.out.println("\t\tindex >= 0");
			for (int i = 0; i < opds.length; i++) {
				opds[i].index = phi.result.index;
			}
			phi.result.type = opds[0].type;
		} else {
			System.out.println("\t\tindex < 0");
			for (int i = 0; i < opds.length; i++) {
				phi.result.type = opds[0].type;
			}
		}
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
		boolean parsed = false;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				PhiFunction phi = b.phiFunctions[i];
				assert phi.result.index >= 0 : "phi func not resolved";
				System.out.println("parse exit set for phi function nr" + phi.result.n + " index is " + phi.result.index);
				parseExitSet(ssa, b, phi);
				System.out.println("phi function has now reg = " + phi.result.reg);
			}
			parsed = false;
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				SSAValue[] opds = instr.getOperands();
				if (opds != null) {
					// free volatile registers
					for (SSAValue opd : opds) {
						SSAValue val = instr.result;
						SSAValue startVal = b.instructions[0].result;
						if ((opd.reg >= -1) && (opd.end - startVal.end <= i)) {
							switch (instr.result.type) {
							case tFloat:
							case tDouble:
								assert false : "not yet";
								break;
							case tInteger:
							case tAinteger:
								System.out.println("free volatile reg " + opd.reg);
								freeVolatileGPR(opd.reg);
								break;
							case tVoid:
								break;
							default:
								System.out.println("cfg = " + ssa.cfg.method.name);
								System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
								System.out.println("type = " + svNames[instr.result.type]);
								assert false : "type not implemented";
							}
						}
					}
				}
				if (instr.ssaOpcode != sCbranch) {	// branch instruction has no result
//					System.out.println("handle ssa instruction assign register");
					if (instr.result.index < 0) { 
//						System.out.println("reserve volatile");
						switch (instr.result.type) {
						case tFloat:
						case tDouble:
							instr.result.reg = reserveVolatileFPR();
							break;
						case tInteger:
							instr.result.reg = reserveVolatileGPR();
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
						if (!parsed) {
//							System.out.println("parse exit set");
							parsed = true;
							parseExitSet(ssa, b, instr);
						}
						instr.result.reg = regs[instr.result.index];
//						System.out.println("register assigned " + instr.result.reg);
					}
				}
			}
			b = (SSANode) b.next;
		}	
	}

	private static void parseExitSet(SSA ssa, SSANode b, SSAInstruction instr) {
		nonVolRegsGPR = nonVolRegsGPRinitial;
		nonVolRegsFPR = nonVolRegsFPRinitial;
		int nofGPR = 0, nofFPR = 0;
		for (int i = 0; i < b.maxLocals; i++) {
			SSAValue val = b.exitSet[b.maxStack + i];
			if (val != null) {
				switch (val.type) {
				case tFloat:
					regs[i] = reserveNonVolatileFPR();
					instr.result.reg = regs[i];
					nofFPR++;
					break;
				case tDouble:
					regs[i] = reserveNonVolatileFPR();
					instr.result.reg = regs[i];
					nofFPR++;
					i++;
					break;
				case tInteger:
				case tThis:
				case tObject:
				case tAinteger:
					regs[i] = reserveNonVolatileGPR();
					instr.result.reg = regs[i];
					System.out.println("val " + i + " in exit set is of type " + val.typeName() + " Reg = " + instr.result.reg);
					nofGPR++;
					break;
				case tPhiFunc:
					break;
				default:
					System.out.println("cfg = " + ssa.cfg.method.name);
					System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
					System.out.println("type = " + svNames[val.type]);
					assert false : "type not implemented";
				}
			} else {
				if ((i == 0) && (ssa.paramType[b.maxStack] == SSAValue.tThis)) {
					System.out.println("reserve GPR for this");
					reserveNonVolatileGPR();
					nofGPR++;
				}				
			}
			if (nofGPR > ssa.nofGPR) ssa.nofGPR = nofGPR;
			if (nofFPR > ssa.nofFPR) ssa.nofFPR = nofFPR;
			instr.result.reg = regs[instr.result.index];
//			System.out.println("val " + i + " in exit set is of type " + val.typeName() + " Reg = " + instr.result.reg);

		}
		
	}

	private static void freeVolatileGPR(int reg) {
		volRegsGPR |= 1 << reg;
		volRegsGPR &= volRegsGPRinitial;
	}

	private static int reserveVolatileFPR() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static int getVolatileGPR() {
		int regs = volRegsGPR;
		int i = 0;
		while (regs != 0) {
			if ((regs & 1) != 0) return i;
			regs /= 2;
			i++;
		}
		assert false: "not enough registers for volatiles";
		return 0;
	}

	public static int reserveVolatileGPR() {
		int regs = volRegsGPR;
		int i = 0;
		while (regs != 0) {
			if ((regs & 1) != 0) {
				volRegsGPR &= ~(1 << i);
				return i;
			}
			regs /= 2;
			i++;
		}
		assert false: "not enough registers for volatile GPRs";
		return 0;
	}

	public static int reserveNonVolatileGPR() {
		int regs = nonVolRegsGPR;
		int i = 31;
		while (regs != 0) {
			if ((regs & (1 << i)) != 0) {
				nonVolRegsGPR &= ~(1 << i);
				return i;
			}
			i--;
		}
		assert false: "not enough registers for nonvolatile GPRs";
		return 0;
	}

	public static int reserveNonVolatileFPR() {
		int regs = nonVolRegsFPR;
		int i = 31;
		while (regs != 0) {
			if ((regs & (1 << 31)) != 0) {
				volRegsFPR &= ~(1 << i);
				return i;
			}
			regs <<= 1;
			i--;
		}
		assert false: "not enough registers for nonvolatile FPRs";
		return 0;
	}

}