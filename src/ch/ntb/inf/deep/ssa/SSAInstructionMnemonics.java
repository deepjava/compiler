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

package ch.ntb.inf.deep.ssa;

public interface SSAInstructionMnemonics extends SSAInstructionOpcs {
	public static String[] scMnemonics = {
		"sCloadConst",
		"sCloadLocal",
		"sCloadFromField",
		"sCloadFromArray",
		"sCstoreToField",
		"sCstoreToArray",
		"sCadd",
		"sCsub",
		"sCmul",
		"sCdiv",
		"sCrem",
		"sCneg",
		"sCshl",
		"sCshr",
		"sCushr",
		"sCand",
		"sCor",
		"sCxor",
		"sCconvInt",
		"sCconvLong",
		"sCconvFloat",
		"sCconvDouble",
		"sCcmpl",
		"sCcmpg",
		"sCinstanceof",
		"sCalength",
		"sCcall",
		"sCnew",
		"sCreturn",
		"sCthrow",
		"sCbranch",
		"sCswitch",
		"sCregMove",		
		"sCphiFunc"
	};

}
