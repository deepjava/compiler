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

import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class LineNrSSAInstrPair implements SSAInstructionMnemonics {
	public int bca;
	public int lineNr;
	public SSAInstruction instr;
	
	public LineNrSSAInstrPair(int bca, int lineNr, SSAInstruction instr){
		this.bca = bca;
		this.lineNr = lineNr;
		this.instr = instr;
	}
	
	@Override
	public String toString(){
		if(instr == null)
			return "Pc: " + bca + " <=> " + "Line: " + lineNr + " has no SSAInstruction";
		if(instr.machineCodeOffset == -1)
			return "Pc: " + bca + " <=> " + "Line: " + lineNr + " <=> " + instr.toString();
		return "Pc: " + bca + " <=> " + "Line: " + lineNr + " <=> " + instr.toString() + " <=> CodeOffset " + instr.machineCodeOffset;
	}

}
