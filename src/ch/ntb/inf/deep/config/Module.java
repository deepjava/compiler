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
