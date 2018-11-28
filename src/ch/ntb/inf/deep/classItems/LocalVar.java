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

package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class LocalVar extends Item {

	public int startPc, length; // life range in bytecode: [startPc, startPc+length]
	public SSAInstruction ssaInstrStart, ssaInstrEnd;	// associated ssa instructions
	
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.printf("[%1$2d] (%2$c)%3$s %4$s  [%5$d,%6$d]", index, (char)((Type)type).category, type.name, name, startPc, startPc+length);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name.toString() + ": index=" + index + ", category=" + (char)((Type)type).category + ", type=" + type.name);
		sb.append(", bytecode: from " + startPc + " to " + (startPc+length));
		if (ssaInstrStart != null) {
			sb.append(" <=> ssa instruction: from " + ssaInstrStart.result.n);
			if (ssaInstrEnd != null) sb.append(" to " + ssaInstrEnd.result.n);
			if (ssaInstrStart.machineCodeOffset != -1)
				sb.append(" <=> code: from " + ssaInstrStart.machineCodeOffset + " to " + ssaInstrEnd.machineCodeOffset);
		}
		return sb.toString();
	}

}
