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

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Module extends ConfigElement {
	SegmentAssignment root;
	SegmentAssignment tail;
	
	public Module(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public Module(HString name) {
		this.name = name;
	}
	
	public void setSegmentAssignment(SegmentAssignment assign){
		if(root == null){
			root = assign;
			tail = root;
		}
		int segHash = assign.contentAttribute.hashCode();
		SegmentAssignment current = root;
		while(current != null){
			if(current.contentAttribute.hashCode() == segHash){
				if(current.contentAttribute.equals(assign.contentAttribute)){
					//TODO warn the User
					//Overwrite the old assignment
					current.segmentDesignator = assign.segmentDesignator;
					return;
				}
			}
			current = current.next;			
		}
		tail.next = assign;
		tail = tail.next;
	}
	
	/**
	 * without protection of duplicated entries
	 * @param assign
	 */
	public void addSegmentAssignment(SegmentAssignment assign){
		if(root == null){
			root = assign;
			tail = root;
		}else{
			tail.next = assign;
			tail = tail.next;
		}
	}
	
	public SegmentAssignment getSegmentAssignments(){
		return root;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.print(name.toString() + " : ");
		SegmentAssignment current = root;
		while(current != null){
			current.print(indentLevel + 1);
			if(current.next != null){
				StdStreams.vrb.print(", ");
			}
			current = current.next;
		}
		StdStreams.out.println(";");		
	}
}
