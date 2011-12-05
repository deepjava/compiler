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

public class SystemConstants implements ErrorCodes {
	private static SystemConstants sysBlock;
	ValueAssignment sysConst;

	private SystemConstants() {
	}

	public static SystemConstants getInstance() {
		if (sysBlock == null) {
			sysBlock = new SystemConstants();
		}
		return sysBlock;
	}

	public void addSysConst(HString constName, int value) {
		if (sysConst == null) {
			sysConst = new ValueAssignment(constName, value);
			return;
		}
		int constHash = constName.hashCode();
		ValueAssignment current = sysConst;
		ValueAssignment prev = null;
		while (current != null) {
			if (current.name.hashCode() == constHash) {
				if (current.name.equals(sysConst.name)) {
					ErrorReporter.reporter.error(errOverwriteProtectedConst, constName.toString());
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = new ValueAssignment(constName, value);

	}

	public ValueAssignment getSysConst() {
		return sysConst;
	}

	public void println(int indentLevel) {
		if (sysConst != null) {
			for (int i = indentLevel; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("sysconst {");
			ValueAssignment current = sysConst;
			while (current != null) {
				current.println(indentLevel + 1);
				current = current.next;
			}
			for (int i = indentLevel; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("}");
		}
	}

	public int getConstByName(HString name) {
		int constHash = name.hashCode();
		ValueAssignment current = sysConst;
		while (current != null) {
			if (current.name.hashCode() == constHash) {
				if (current.name.equals(name)) {
					return current.getValue();
				}
			}
			current = current.next;
		}
		return Integer.MAX_VALUE;
	}

	public static void clear() {
		sysBlock = null;		
	}
}
