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

import org.deepjava.cg.ppc.RegAllocatorPPC;
import org.deepjava.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	public int nofOperands;
	public boolean deleted = false;
	public boolean visited = false;
	public boolean used = false;
	public int last;	

	public PhiFunction(int opCode, int bca) {
		this.ssaOpcode = opCode;
		this.bca = bca;
		operands = new SSAValue[2];
		nofOperands = 0;
	}

	@Override
	/** returns a copy */
	public SSAValue[] getOperands() {
		SSAValue[] opnd = new SSAValue[nofOperands];
		for(int i = 0; i < nofOperands; i++){
			opnd[i]=operands[i];
		}
		return opnd;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
		nofOperands = operands.length;

	}
	public void addOperand(SSAValue operand) {
		int len = operands.length;
		if (len == nofOperands) {
			SSAValue[] newArray = new SSAValue[2 * len];
			for (int i = 0; i < len; i++) {
				newArray[i] = operands[i];
			}
			operands = newArray;
		}
		operands[nofOperands++] = operand;
		
	}
	
	public void insertOperand(SSAValue operand, int pos){
		if(pos >= nofOperands || pos < 0){
			throw new IndexOutOfBoundsException("invalid Position");
		}
		operands[pos] = operand;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("PhiFunction["+ scMnemonics[ssaOpcode]+"] {");
		for (int i=0;i<nofOperands-1;i++){
			if(operands[i] != null){
				sb.append(operands[i].n + ", ");
			}else{
				sb.append("null, ");
			}
		}
		if (nofOperands > 0) {
			if(operands[nofOperands-1] != null){
				sb.append(operands[nofOperands-1].n + "}");
			} else {
				sb.append("null)");
			}
		}	
		sb.append(" (" + result.typeName() + ")");
		if (result.index != -1) sb.append(", index=" + result.index);
		SSAValue res = result;
		if (res.join != null) {
			sb.append(", join=[" + result.index + "(");
			SSAValue join = RegAllocatorPPC.joins[result.index];
			int i = 0;
			while (join != null && join != result.join) {
				i++;
				join = join.next;
			}
			sb.append(i + ")]");
			res = result.join;
		} else 
			sb.append(", end=" + res.end);
		if (res.reg != -1) {
			if (res.nonVol) sb.append(", nonVol"); else sb.append(", vol");
		}
		if (res.regLong != -1) sb.append(", regLong=" + res.regLong);
		if (res.reg != -1) sb.append(", reg=" + res.reg);
		if (res.regGPR1 != -1) sb.append(", regAux1=" + res.regGPR1);

		if (last != 0) sb.append(", last=" + last);
		if (deleted) sb.append(" del");
		if (used) sb.append(" u");
		return sb.toString();
	}
}
