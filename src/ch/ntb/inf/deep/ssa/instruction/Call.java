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
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.ssa.SSAValue;

public class Call extends SSAInstruction {
	public Item item;
	public boolean invokespecial;

	public Call(int opCode, Item item, int bca) {
		ssaOpcode = opCode;
		this.item = item;
		this.bca = bca;
	}

	public Call(int opCode, Item item, SSAValue[] operands, int bca) {
		ssaOpcode = opCode;
		this.item = item;
		this.operands = operands;
		this.bca = bca;
	}

	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("Call["+ scMnemonics[ssaOpcode]+"]");
		if (operands != null) {
			sb.append(" {");
			for (int i=0;i<operands.length-1;i++){
				sb.append(operands[i].n + ", ");
			}
			if (operands.length > 0){
				sb.append(operands[operands.length-1].n + "}");
			} else {
				sb.append("}");
			}
		}
		sb.append(" (" + result.typeName() + ") ");
		if (item != null) { 
			if (item instanceof Method) {
				sb.append(((Method)item).owner.name);
				sb.append("." + item.name + ((Method)item).methDescriptor);
			} else
				sb.append(item.name);
		}
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
		return sb.toString();
	}

}
