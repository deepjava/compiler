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

package org.deepjava.cg;

import org.deepjava.cg.arm.InstructionOpcs;
import org.deepjava.classItems.*;
import org.deepjava.config.Arch;
import org.deepjava.ssa.*;
import org.deepjava.strings.HString;

public class Code32 implements ICclassFileConsts, InstructionOpcs {

	protected static final int defaultNofInstr = 32;
	protected static final int defaultNofFixup = 8;
	
	public static Arch arch;
	
	public SSA ssa;	// reference to the SSA of a method
	public int[] instructions;	// contains machine instructions for the ssa of a method
	public int iCount;	// nof instructions for this method, including exception information
	public int excTabCount;	// start of exception information in instruction array
	public int localVarOffset;	// stack offset (in bytes) to locals stored on the stack
	public Item[] fixups;	// contains all references whose address has to be fixed by the linker
	public int fCount;	// nof fixups
	public int lastFixup;	// instr number where the last fixup is found

	public Code32(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		fixups = new Item[defaultNofFixup];
	}

	public void incInstructionNum() {
		iCount++;
		int len = instructions.length;
		if (iCount == len) {
			int[] newInstructions = new int[2 * len];
			System.arraycopy(instructions, 0, newInstructions, 0, len);
			instructions = newInstructions;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (ssa != null)	// compiler specific subroutines have no ssa
			sb.append("Code for Method: " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name +  ssa.cfg.method.methDescriptor + "\n");
		int i;
		for (i = 0; i < iCount; i++) {
			if ((instructions[i] & 0xffffff00) == 0) break;	
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t" + String.format("%3d", i) + "[0x");
			sb.append(Integer.toHexString(i * 4));
			sb.append("]\t");
			sb.append(InstructionDecoder.dec.getMnemonic(instructions[i]));
			if (arch.name.equals(HString.getHString("ppc32"))) {
				int opcode = (instructions[i] & 0xFC000000) >>> (31 - 5);
				if (opcode == 0x10) {
					int BD = (short) (instructions[i] & 0xFFFC);
					sb.append(", [0x" + Integer.toHexString(BD + 4 * i) + "]\t");
				} else if (opcode == 0x12) {
					int li = (instructions[i] & 0x3FFFFFC) << 6 >> 6;
					sb.append(", [0x" + Integer.toHexString(li + 4 * i) + "]\t");
				}
			} else {	// arm
				int opcode = (instructions[i] & 0x0f000000);
				if (opcode == armB) {
					int jump = ((instructions[i] & 0xFFFFFF) << 8 >> 6) + 8;
					sb.append(", [0x" + Integer.toHexString(jump + 4 * i) + "]\t");
				}		
			}
			sb.append("\n");
		}
		if (ssa != null) {	// compiler specific subroutines have no ssa
			if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) return sb.toString();	// exception methods have no unwinding or exception table 
		
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
			sb.append((byte)instructions[i++]); sb.append("  (offset to unwind code)\n");
			while (instructions[i] != 0xffffffff) {
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (start address of try)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (end address of try)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (address of catch type)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (address of catch)\n");
			}
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
			sb.append("(end of method)\n"); i++;
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t   [0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
			sb.append("(address of class variables)\n");
		}
		return sb.toString();
	}

}


