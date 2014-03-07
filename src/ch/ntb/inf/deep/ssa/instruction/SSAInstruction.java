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

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAInstructionMnemonics;
import ch.ntb.inf.deep.ssa.SSAValue;

public abstract class SSAInstruction implements SSAInstructionMnemonics {
	protected SSAValue[] operands;
	public SSAValue result;
	public int ssaOpcode;
	public int bca = -1; // used for debugging, marks bca of associated bytecode instruction
	public int machineCodeOffset = -1; // used for debugging, e.g. step into
	
	public abstract void setOperands(SSAValue[] operands);
	public abstract SSAValue[] getOperands();

	public void print(int level) {
		StdStreams.vrbPrintIndent(level);
		StdStreams.vrb.println(toString());
	}

}
