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
	public RefType owner; // provisional owner of the field or method
	public HString descriptor; // descriptor of the method, null for fields

	ItemStub(RefType owner, HString fieldName, Type fieldType) {
		super(fieldName, fieldType);
		this.owner = owner;
	}

	ItemStub(RefType owner, HString methName, HString methDescriptor) {
		super(methName, null);
		this.owner = owner;
		this.descriptor = methDescriptor;
	}
	

	Item getReplacedStub() {
		Item item;
		if (type == null) {	// must be method
			item = owner.getMethod(name, descriptor); // searches in class and in all superclasses
			if (item == null) {	// method not found
				assert (owner.accAndPropFlags & (1<<apfInterface)) != 0: "owner must be interface";	// method must be in superinterface of owner, which must be interface itself
				Class[] interfaces = ((Class)owner).interfaces;
				if (interfaces != null) item = checkInterfacesForMethod(interfaces);
			}
		} else {
			item = ((Class)owner).getField(name);	// must be field, searches in class and in all superclasses
			if (item == null) {	// field not found
				// field must be in interface or superinterface of owner, field can only be of type Object
				Class[] interfaces = ((Class)owner).interfaces;
				if (interfaces != null) item = checkInterfacesForField(interfaces);
			}
		}
		if (enAssertion) assert item != null: "stub of " + name + " not found";
		item.accAndPropFlags |= this.accAndPropFlags;
		return item;
	}

	private Item checkInterfacesForMethod(Class[] interfaces) {
		Item item = null;
		for (int n = 0; n < interfaces.length; n++) {
			Class intf = interfaces[n];
			item = intf.getMethod(name, descriptor);
			if (item != null) return item;
			if (intf.interfaces != null) item = checkInterfacesForMethod(intf.interfaces);	// search in superinterfaces
			if (item != null) return item;
		}	
		return item;
	}

	private Item checkInterfacesForField(Class[] interfaces) {
		Item item = null;
		for (int n = 0; n < interfaces.length; n++) {
			Class intf = interfaces[n];
			item = intf.getField(name);
			if (item != null) return item;
			if (intf.interfaces != null) item = checkInterfacesForField(intf.interfaces);	// search in superinterfaces
			if (item != null) return item;
		}	
		return item;
	}

	//--- debug primitives
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.print("stub of ");
		if( type == null) vrb.printf("method: (%1$s).%2$s, d=%3$s", owner.name, name, descriptor);
		else vrb.printf("field: name=%1$s, t=%2$s", name, type.name);
		vrb.print(", dFlags:"); Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags);
		if( owner != null ){
			vrb.print(", owner.Flags:"); Dbg.printDeepAccAndPropertyFlags(owner.accAndPropFlags);			
		}
	}

	public void printShort(int indentLevel) {
		print(indentLevel);
	}
}
