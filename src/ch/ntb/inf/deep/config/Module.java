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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class Module extends Item {
	SegmentAssignment segAssign;
	
	public Module(HString name) {
		this.name = name;
	}
	
	public void addSegmentAssignment(SegmentAssignment a) {
		if (segAssign == null) segAssign = a;
		else segAssign.appendTail(a);
	}

	public Segment getSegment(HString attr) {
		SegmentAssignment a = segAssign;
		while (a != null) {
			if (a.contentAttribute == attr) return a.seg;
			a = (SegmentAssignment) a.next;
		}
		return null;
	}
	
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.println("module = " + name.toString() + " {");
		indent(indentLevel+1);
		vrb.println("segment assignments = {");
		SegmentAssignment a = segAssign;
		while (a != null) {
			indent(indentLevel+2);
			vrb.println(a.toString());
			a = (SegmentAssignment) a.next;
		}
		indent(indentLevel+1);
		vrb.println("}");
		indent(indentLevel);
		vrb.println("}");
	}

}
