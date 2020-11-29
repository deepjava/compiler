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

package org.deepjava.classItems;

import org.deepjava.host.Dbg;
import org.deepjava.host.ErrorReporter;
import org.deepjava.strings.HString;

public class ItemStub extends Item {
	public RefType owner; // provisional owner of the field or method
	public HString descriptor; // descriptor of the method, null for fields

	ItemStub(RefType owner, HString fieldName, Type fieldType) {
		super(fieldName, fieldType);
		this.owner = owner;
	}

	ItemStub(HString clsName, RefType owner, HString methName, HString methDescriptor) {
		super(methName, null);
		this.owner = owner;
		this.descriptor = methDescriptor;
		if (owner.name.equals(HString.getRegisteredHString("java/lang/StringBuilder"))) {
			String str = "in class " + clsName;
			ErrorReporter.reporter.error(306, str);
		}
	}
	
	Item getReplacedStub() {
		Item item;
		if (type == null) {	// must be method
			item = owner.getMethod(name, descriptor); // searches in class and in all superclasses
			if (item == null) {	// method not found
				if (owner == Type.wktEnum) { // if class is enum, search for Enum.valueOf(Enum[],String)	
					item = owner.getMethod(name, HString.getRegisteredHString("([Ljava/lang/Enum;Ljava/lang/String;)Ljava/lang/Enum;"));
					assert item != null: "method valueOf in Enum not found";
				} else { // method must be in superinterface of owner, which must be interface itself
					assert (owner.accAndPropFlags & (1<<apfInterface)) != 0: "owner must be interface (name=" + name + descriptor + " owner=" + owner.name + ")";
					Class[] interfaces = ((Class)owner).interfaces;
					if (interfaces != null) item = checkInterfacesForMethod(interfaces);
				}
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
		if (type == null) vrb.printf("method: (%1$s).%2$s, d=%3$s", owner.name, name, descriptor);
		else vrb.printf("field: name=%1$s, t=%2$s", name, type.name);
		vrb.print(", dFlags:"); Dbg.printAccAndPropertyFlags(this.accAndPropFlags);
		if (owner != null) {
			vrb.print(", owner.Flags:"); Dbg.printAccAndPropertyFlags(owner.accAndPropFlags);			
		}
	}

	public void printShort(int indentLevel) {
		print(indentLevel);
	}
}
