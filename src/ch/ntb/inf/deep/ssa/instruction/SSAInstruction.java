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

import ch.ntb.inf.deep.ssa.SSAInstructionMnemonics;
import ch.ntb.inf.deep.ssa.SSAValue;

/**
 * @author  millischer
 */
public abstract class SSAInstruction implements SSAInstructionMnemonics {
	protected SSAValue[] operands;
	public SSAValue result;
	public int ssaOpcode;
	public int machineCodeOffset = -1; // needs for debug purpose ex. step into
	
	public abstract void setOperands(SSAValue[] operands);
	public abstract SSAValue[] getOperands();
	public abstract void print(int level);
	
}
