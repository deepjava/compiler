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

package org.deepjava.ssa.instruction;

import org.deepjava.ssa.SSAValue;

public class Branch extends SSAInstruction {
	
	public Branch(int opCode, SSAValue operand1, SSAValue operand2, int bca) {
		this.bca = bca;
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1,operand2};
	}

	public Branch(int opCode, SSAValue operand1, int bca) {
		this.bca = bca;
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1};
	}

	public Branch(int opCode, int bca) {
		this.bca = bca;
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
		sb.append(", bca=" + bca);
		return sb.toString();
	}
	
}
