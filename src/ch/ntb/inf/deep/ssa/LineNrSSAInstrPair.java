/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class LineNrSSAInstrPair implements SSAInstructionMnemonics {
	public int bca;	// instruction address in bytecode
	public int lineNr;	// line number in source file
	public SSAInstruction ssaInstr;	// ssa instruction
	
	/**
	 * creates a line number pair consisting of a byte code address and a SSA instruction
	 * @param bca
	 * @param lineNr
	 * @param instr
	 */
	public LineNrSSAInstrPair(int bca, int lineNr, SSAInstruction instr){
		this.bca = bca;
		this.lineNr = lineNr;
		this.ssaInstr = instr;
	}
	
	@Override
	public String toString(){
		if (ssaInstr == null)
			return "bytecode line: " + bca + " <=> " + "source line: " + lineNr + " has no SSAInstruction";
		if (ssaInstr.machineCodeOffset == -1)
			return "bytecode line: " + bca + " <=> " + "source line: " + lineNr + " <=> ssa instruction: " + ssaInstr.result.n;
		return "bytecode line: " + bca + " <=> source line: " + lineNr + " <=> ssa instruction:" + ssaInstr.result.n + " <=> code " + ssaInstr.machineCodeOffset;
	}

}
