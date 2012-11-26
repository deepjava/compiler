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

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Constants implements ErrorCodes {
	private ValueAssignment consts;
	private boolean overwriteProtected;

	public Constants(String jname, boolean overwriteProtected) {
		this.overwriteProtected = overwriteProtected;
	}
	
	public void addConst(String jname, int value) {
		if (consts == null) {
			consts = new ValueAssignment(jname, value);
		}
		else {
			ValueAssignment c = (ValueAssignment)consts.getElementByName(jname);
			if(c != null) {
				if(!overwriteProtected) {
					c.setValue(value);
					//TODO add warning here!!!
				}
				else {
					ErrorReporter.reporter.error(errOverwriteProtectedConst, c.getName().toString());
				}
			}
			else {
				consts.append(new ValueAssignment(jname, value));
			}
		}
	}

	public void println(int indentLevel) {
		if (consts != null) {
			for (int i = indentLevel; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("sysconst {");
			ValueAssignment current = consts;
			while (current != null) {
				current.println(indentLevel + 1);
				current = (ValueAssignment)current.next;
			}
			for (int i = indentLevel; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("}");
		}
	}

	public ValueAssignment getFirstConstant() {
		return consts;
	}
	
	public ValueAssignment getConstantByName(HString name) {
		return (ValueAssignment)consts.getElementByName(name);
	}
	
	public int getValueOfConstant(HString name) {
		if(consts != null) {
			ValueAssignment c = (ValueAssignment)consts.getElementByName(name);
			if(c != null) return c.getValue();
		}
		return Integer.MIN_VALUE;
	}
	
	public int getValueOfConstant(String jname) {
		return getValueOfConstant(HString.getRegisteredHString(jname));
	}
}
