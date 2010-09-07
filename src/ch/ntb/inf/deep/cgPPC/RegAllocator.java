package ch.ntb.inf.deep.cgPPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;

/**
 * linear scan register allocation. Implements the algorithm described in "Linear Scan Register Allocation in the Context of SSA Form and Register Constraints"
 * by Mösenböck and Pfeiffer
 * 
 * @author graf
 * 
 */
public class RegAllocator implements SSAInstructionOpcs {
	/**
	 * Number of Memory-Slots used for this Method.
	 */
	private int nextMemorySlot = 0;

	/**
	 * Registers available for Register-Allocation.
	 */
	private final boolean[] registers = new boolean[Registers.NOF_WORKING_REGISTERS];


	// total number of SSA Instructions for the cfg of a method
	private static int nofSSAInstructions;

	
	private static int volRegs;
	private static int nonVolRegs;

	/**
	 * generates the live ranges of all SSAValues and assigns register to them
	 */
	public static void allocateRegisters(CFG cfg) {
		volRegs = 0x00001ffc;
		nonVolRegs = 0xffffe000;
		
//		generateMoves(cfg); 	// generate moves for phi functions
		renumberInstructions(cfg);	// renumber all SSAInstructions 
		buildIntervals(cfg);	// compute live intervals
//		joinIntervals(cfg);
		assignRegisters(cfg);

		// Save used Registers and Memory-Slots in SSA-Graph
//		ssa.nofRegistersUsed = maxRegistersUsed;
//		ssa.nofMemorySlotsUsed = nextMemorySlot;

		// printIntervals();
	}

	/**
	 * Generates the register moves to resolve phi functions
	 */
	private static void generateMoves(CFG cfg) {
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			if (b.nofPhiFunc > 0) { // process only if node has phi functions
				for (int i = 0; i < b.nofPredecessors; i++) {
					SSANode p = (SSANode) b.predecessors[i];
					SSANode node;
					if ((b.nofPredecessors > 1)&& (p.nofSuccessors > 1)) {
//						node = b.insertNode(p);
						node = p;
					} else {
						node = p;
					}
					for (int k = 0; k < b.nofPhiFunc; k++) { 
						PhiFunction phi = b.phiFunctions[k];
						// get the phi operand corresponding to predecessor
						SSAValue[] operands = phi.getOperands();	
						SSAInstruction instr = new Monadic(sCRegMove, operands[i]);
						SSAValue result = new SSAValue();
						result.type = operands[i].type;
						instr.result = result;
						node.addInstruction(instr);
						// replace operands in phi function with new instruction
						operands[i] = result;
						phi.setOperands(operands);
					}
				}
			}
			b = (SSANode) b.next;
		}
	}

	/**
	 * Renumber all the instructions in the SSA before computing live intervals
	 */
	public static void renumberInstructions(CFG cfg) {
		int counter = 0;
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				b.phiFunctions[i].result.n = counter++;
			}
			for (int i = 0; i < b.nofInstr; i++) {
				b.instructions[i].result.n = counter++;	
			}
			b = (SSANode) b.next;
		}
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
				phi.result.index = opds[0].index;
			}

			// for all instructions i in b do
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				int currNo = instr.result.n;
				instr.result.end = currNo;
				SSAValue[] opds = instr.getOperands();
				if (opds != null) { 
					for (SSAValue opd : instr.getOperands()) {
						opd.end = currNo;
					}
				}
			}
			b = (SSANode) b.next;
		}
	}

	/**
	 * Joins the intervals of different SSAValues
	 * Phi-Functions get resolved 
	 */
	private static void joinIntervals(CFG cfg) {
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			// handle phi instructions
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				SSAValue[] opds = phi.getOperands();
				int count = opds.length;
				for (int k = 0; k < count - 1; k++) {
					join(opds[k], opds[k+1]);
				}
			}
			// handle move instructions
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				if (instr.ssaOpcode == sCRegMove) {
					SSAValue[] opds = instr.getOperands();
					join(opds[0], opds[1]);
				}
			}
			b = (SSANode) b.next;
		}
	}

	private static void join(SSAValue x, SSAValue y) {
		SSAValue xRep = rep(x);
		SSAValue yRep = rep(y);
		int end = xRep.end;
		if (end < yRep.n) {	// intervals do not overlap, can be joined
			xRep.end = yRep.n;
			yRep.join = xRep;
		}
	}

	private static SSAValue rep(SSAValue x) {
		if (x.join == x) {
			return x;
		} else {
			return rep(x.join);
		}
	}

	/**
	 * Assign a register or memory location to all SSAValues
	 */
	private static void assignRegisters(CFG cfg) {
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				SSAInstruction phi = b.phiFunctions[i];
				phi.result.reg = 31 - phi.result.index;
			}
			for (int i = 0; i < b.nofInstr; i++) {
				SSAInstruction instr = b.instructions[i];
				if (instr.ssaOpcode != sCBranch) {	// branch instruction has no result
					if (instr.result.index < 0) { 
						instr.result.reg = reserveVolatile();
					}  else {	// is a local variable
						instr.result.reg = 31 - instr.result.index;
					}
				}
			}
			b = (SSANode) b.next;
		}
	}

	public static int getVolatile() {
		int regs = volRegs;
		int i = 0;
		while (regs != 0) {
			if ((regs & 1) != 0) return i;
			regs /= 2;
			i++;
		}
		assert false: "not enough registers for volatiles";
		return 0;
	}

	public static int reserveVolatile() {
		int regs = volRegs;
		int i = 0;
		while (regs != 0) {
			if ((regs & 1) != 0) {
				volRegs &= ~(1 << i);
				return i;
			}
			regs /= 2;
			i++;
		}
		assert false: "not enough registers for volatiles";
		return 0;
	}

	public static void print(CFG cfg) {
		SSANode node = (SSANode) cfg.rootNode;
		System.out.println("Register allocator for " + cfg.method.name);

		while (node != null) {
			for (int i = 0; i < node.nofInstr; i++){
				SSAInstruction instr = node.instructions[i];
				System.out.print(instr.result.n + ":  reg = " + instr.result.reg);
				if (instr.result.join != instr.result) 
					System.out.print("   join = " + instr.result.join.n);
				System.out.print("   index = " + instr.result.index);
				System.out.print("   end = " + instr.result.end);
				SSAValue[] opds = instr.getOperands();
				for (int j = 0; (opds != null) && (j < opds.length); j++)
					System.out.print("   op" + (j+1) + " = " + opds[j].n);
				System.out.println("");
			}
			
			System.out.println("");
			node = (SSANode) node.next;
		}
	}

}