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

package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.cg.ppc.RegAllocator;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.ssa.SSAValue;

public class NoOpnd extends SSAInstruction {
	public boolean firstInCatch;	// true, if this instruction is the first in a catch node
	
	public NoOpnd(int opcode, int bca) {
		ssaOpcode = opcode;		
		this.bca = bca;
	}

	@Override
	public SSAValue[] getOperands() {
		return null;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		//return immediately
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("NoOpnd["+ scMnemonics[ssaOpcode]+"] ");
		if (ssaOpcode == sCloadConst) {
			if (result.constant instanceof StdConstant) {
				StdConstant constant = (StdConstant)result.constant;
				if (constant.name == null) {
					sb.append(constant.valueH);
				} else {
					long value = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
					char category = constant.type.name.charAt(0);
					if (category == 'I') 
						sb.append(constant.valueH);
					else if (category == 'J') 
						sb.append(value);
					else if (category == 'F') 
						sb.append(Float.toString(Float.intBitsToFloat(constant.valueH)));
					else
						sb.append(Double.longBitsToDouble(value));
				}
				sb.append(" (" + result.typeName() + ")");
			} else if (result.constant instanceof StringLiteral) {
				StringLiteral str = (StringLiteral)result.constant;
				if (str != null) sb.append("\"" + str.string + "\"");
				else sb.append("null");
				sb.append(" (" + result.typeName() + ")");
			} else if (result.constant instanceof ch.ntb.inf.deep.classItems.Class) {
				sb.append(result.constant.name);
			} else if (result.constant instanceof ch.ntb.inf.deep.classItems.Field) {
				sb.append(result.constant.name);
			} else if (result.constant == null) {
				sb.append("null");
			}
		} else sb.append("(" + result.typeName() + ")");
		if (result.index != -1) sb.append(", index=" + result.index);
		if (result.join != null) {
			sb.append(", join=[" + result.index + "(");
			SSAValue join = RegAllocator.joins[result.index];
			int i = 0;
			while (join != null && join != result.join) {
				i++;
				join = join.next;
			}
			sb.append(i + ")]");
		} else {
			sb.append(", end=" + result.end);
			if (result.reg != -1) {
				if (result.nonVol) sb.append(", nonVol"); else sb.append(", vol");
			}
			if (result.regLong != -1) sb.append(", regLong=" + result.regLong);
			if (result.reg != -1) sb.append(", reg=" + result.reg);
			if (result.regGPR1 != -1) sb.append(", regAux1=" + result.regGPR1);
		}
		sb.append(", bca=" + bca);
		if (firstInCatch) sb.append(", firstInCatch");
		return sb.toString();
	}
}
