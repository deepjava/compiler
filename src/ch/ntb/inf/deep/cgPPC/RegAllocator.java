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
public class RegAllocator implements SSAInstructionOpcs, SSAValueType {
	private static final int TopGPR = 31;
	private static final int TopFPR = 31;
	private static final int volRegsGPRinitial = 0x00001ffc;

	private static int volRegsGPR, volRegsFPR;
	private static int nonVolRegsGPR, nonVolRegsFPR;

	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	public static void allocateRegisters(CFG cfg) {
		volRegsGPR = volRegsGPRinitial;
		nonVolRegsGPR = 0xffffe000;
		volRegsFPR = 0x00001ffc;
		nonVolRegsFPR = 0xffffe000;
		
//		SSA.renumberInstructions(cfg);	// renumber all SSAInstructions 
		buildIntervals(cfg);	// compute live intervals
		assignRegisters(cfg);

	}

	/**
	 * Computes the live ranges of all SSAValues 
	 */
	private static void buildIntervals(CFG cfg) {
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				SSAValue[] opds = phi.getOperands();
				for (int k = 0; k < opds.length - 1; k++)
					assertEquals("Parameters of phi function have different slot numbers", opds[k].index, opds[k+1].index); 
				phi.result.index = opds[0].index;
				phi.result.type = opds[0].type;
			}

			// for all instructions i in b do
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				int currNo = instr.result.n;
				instr.result.end = currNo;
				SSAValue[] opds = instr.getOperands();
				if (opds != null) { 
					for (SSAValue opd : opds) {
						opd.end = currNo;
					}
				}
			}
			b = (SSANode) b.next;
		}
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 */
	private static void assignRegisters(CFG cfg) {
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
//				System.out.println("assign register to phi function");
				PhiFunction phi = b.phiFunctions[i];
				if (phi.deleted) System.out.println("phi function is deleted");
//				System.out.println(phi.result.typeName());
				switch (phi.result.type) {
				case tFloat:
				case tDouble:
					phi.result.reg = TopFPR - phi.result.index;
					break;
				case tInteger:
					phi.result.reg = TopGPR - phi.result.index;
//					System.out.println("phi result reg " + phi.result.reg);
					break;
				default:
					assert true : "type error in phi function";
				}
			}
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				SSAValue[] opds = instr.getOperands();
				if (opds != null) {
					for (SSAValue opd : opds) {
						SSAValue val = instr.result;
						SSAValue startVal = b.instructions[0].result;
						if ((opd.reg != -1) && (opd.end - startVal.end <= i)) {
							freeVolatileGPR(opd.reg);
						}
					}
				}
				if (instr.ssaOpcode != sCbranch) {	// branch instruction has no result
					if (instr.result.index < 0) { 
						switch (instr.result.type) {
						case tFloat:
						case tDouble:
							instr.result.reg = reserveVolatileFPR();
							break;
						case tInteger:
							instr.result.reg = reserveVolatileGPR();
							break;
						default:
							assert true : "type not implemented";
						}
					}  else {	// is a local variable
						switch (instr.result.type) {
						case tFloat:
						case tDouble:
							instr.result.reg = TopFPR - instr.result.index;
							break;
						case tInteger:
							instr.result.reg = TopGPR - instr.result.index;
							break;
						default:
							assert true : "type not implemented";
						}
					}
				}
			}
			b = (SSANode) b.next;
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
		assert false: "not enough registers for volatiles";
		return 0;
	}

}