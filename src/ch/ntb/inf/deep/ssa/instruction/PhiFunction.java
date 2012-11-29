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

import ch.ntb.inf.deep.cgPPC.RegAllocator;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	public int nofOperands;
	public boolean deleted = false;
	public boolean visited = false;
	public boolean used = false;
	public int last;	

	public PhiFunction(int opCode) {
		this.ssaOpcode = opCode;
		operands = new SSAValue[2];
		nofOperands = 0;
	}

	@Override
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
			SSAValue join = RegAllocator.joins[result.index];
			int i = 0;
			while (join != null && join != result.join) {
				i++;
				join = join.next;
			}
			sb.append(i + ")]");
			res = result.join;
		} else {
			sb.append(", end=" + res.end);
			if (res.reg != -1) {
				if (res.nonVol) sb.append(", nonVol"); else sb.append(", vol");
			}
			if (res.regLong != -1) sb.append(", regLong=" + res.regLong);
			if (res.reg != -1) sb.append(", reg=" + res.reg);
			if (res.regGPR1 != -1) sb.append(", regAux1=" + res.regGPR1);
		}
		if (last != 0) sb.append(", last=" + last);
		if (deleted) sb.append(" del");
		if (used) sb.append(" u");
		return sb.toString();
	}
}
