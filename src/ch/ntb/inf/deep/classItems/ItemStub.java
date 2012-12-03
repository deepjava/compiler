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

package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class ItemStub extends Item {
	public Class owner; // provisional owner of the field or method
	public HString descriptor; // descriptor of the method, null for fields

	ItemStub(Class owner, HString fieldName, Type fieldType){
		super(fieldName, fieldType);
		this.owner = owner;
	}

	ItemStub(Class owner, HString methName, HString methDescriptor){
		super(methName, null);
		this.owner = owner;
		this.descriptor = methDescriptor;
	}
	

	Item getReplacedStub(){
		Item item;
		if( type == null) item = owner.getMethod(name, descriptor);
		else  item = owner.getField(name);
		if(enAssertion) assert item != null;
		item.accAndPropFlags |= this.accAndPropFlags;
		return item;
	}

	//--- debug primitives
	public void print(int indentLevel){
		indent(indentLevel);
		vrb.print("stub of ");
		if( type == null) vrb.printf("method: (%1$s).%2$s, d=%3$s", owner.name, name, descriptor);
		else vrb.printf("field: name=%1$s, t=%2$s", name, type.name);
		vrb.print(", dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags);
		if( owner != null ){
			vrb.print(", owner.Flags:");  Dbg.printDeepAccAndPropertyFlags(owner.accAndPropFlags);			
		}
	}

	public void printShort(int indentLevel){
		print(indentLevel);
	}
}
