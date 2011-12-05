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

package ch.ntb.inf.deep.config;

public interface IAttributes {
	byte //--- access and content attributes
	atrRead = 0,		// 0x0001
	atrWrite = 1,		// 0x0002
	atrConst = 4,		// 0x0010
	atrCode = 5,		// 0x0020
	atrVar = 6,			// 0x0020
	atrHeap = 7,    	// 0x0080
	atrStack = 8,		// 0x0100
	atrSysTab = 9;		// 0x0200
}
