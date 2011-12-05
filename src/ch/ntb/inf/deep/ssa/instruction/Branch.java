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

package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class Branch extends SSAInstruction {
	
	public Branch(int opCode, SSAValue operand1, SSAValue operand2){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1,operand2};
	}

	public Branch(int opCode, SSAValue operand1){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1};
	}

	public Branch(int opCode){
		ssaOpcode = opCode;
	}

	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		if (operands.length > 0) {
			this.operands = operands;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++) StdStreams.out.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		if (operands == null)
			StdStreams.vrb.print("Branch["+ scMnemonics[ssaOpcode]+"] ");
		else {
			if (operands.length == 2)
				StdStreams.vrb.print("Branch["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
			else
				StdStreams.vrb.print("Branch["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		}
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		StdStreams.vrb.println();

	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		if (operands == null)
			sb.append("Branch["+ scMnemonics[ssaOpcode]+"] ");
		else {
			if (operands.length == 2)
				sb.append("Branch["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
			else
				sb.append("Branch["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		}
		sb.append(" (" + result.typeName() + ")");
		
		return sb.toString();
	}
	
}
