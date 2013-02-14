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

import ch.ntb.inf.deep.strings.HString;

public class RefType extends Type {

	public static RefType refTypeList, refTypeListTail;	// objects of reference type {(Std-)Class, (Interface-)Class, (Array-)Class, (Enum-)Class, (EnumArray-)Class}
	public static int nofRefTypes;	// total number of objects of reference type

	public Item methods;	// list with all methods

	public int classFieldsSize; // size of all class fields on the target, rounded to the next multiple of "fieldSizeUnit" (in bytes)
	public int objectSize;	// size of object of this type (in bytes)

	protected RefType(HString name, Type type){
		super(name, type);
	}

	/** append reference type to reference type list */
	protected static void appendRefType(RefType newType){
		if(enAssertion) assert refTypeList != null && refTypeListTail != null;
	
		refTypeListTail.next = newType;
		refTypeListTail = newType;
		nofRefTypes++;
	}

	/** search reference type name in list, returns type if found, else returns null */
	public static Item getRefTypeByName(HString registeredClassName) {
		Item cls = RefType.refTypeList;
		while (cls != null && cls.name != registeredClassName) cls = cls.next;
		return cls;
	}

	/** 
	 * search type in reference type list, 
	 * if not found, create type and insert into list,
	 * returns type
	*/
	protected static RefType getRefTypeByNameAndUpdate(char typeCategory, HString registeredTypeName, Type baseType){
		RefType type = null;
		if (typeCategory == tcRef) {	// class type or interface type
			Item cls = getRefTypeByName(registeredTypeName);
			if (cls == null) {
				Class newClass = new Class(registeredTypeName);
				RefType.appendRefType(newClass);
				cls = newClass;
			}
			type = (RefType)cls;
			type.category = tcRef;
			if (baseType != null) type.type = baseType;
		} else if (typeCategory == tcArray) {	// array type
			int dim = 1; 
			while (registeredTypeName.charAt(dim) == tcArray) dim++;
			HString firstDimArray = registeredTypeName.substring(dim - 1);	// one dimensional array of primitive or reference type
			HString compName = registeredTypeName.substring(dim);
			compName = stab.insertCondAndGetEntry(compName);
			firstDimArray = stab.insertCondAndGetEntry(firstDimArray);
			Type componentType = getTypeByDescriptor(compName);
			Array array = (Array)getRefTypeByName(firstDimArray);
			if (array == null) {	// array in first dimension not yet created
				array = new Array(firstDimArray, componentType, 1);
				RefType.appendRefType(array);
			}
			for (int i = 1; i < dim; i++) { // check if higher dimension arrays already created
				if (array.nextHigherDim != null) array = array.nextHigherDim;
				else {
					HString arrName = HString.getHString(Character.toString(tcArray) + array.name);
					Array newArray = new Array(arrName, componentType, i + 1);
					RefType.appendRefType(newArray);
					array.nextHigherDim = newArray;
					newArray.nextLowerDim = array;
					array = newArray;
				}
			} 
			type = array;
		} else
			assert false;
		return type;
	}

	protected Item getMethod(HString name, HString descriptor){
		Item item = null;
		if(methods != null)  item = methods.getMethod(name, descriptor);
		if(item == null && type != null) item = type.getMethod(name, descriptor);
		return item;
	}
	
	public Item getMethod(int methTabIndex) {
		Item m = this.methods;
		while(m != null && m.index != methTabIndex) m = m.next;
		if(m == null) m = ((Class)this.type).getMethod(methTabIndex);
		return m;
	}

	private Item getMethodOrStub(HString name, HString descriptor){
		Item meth = getMethod(name, descriptor);
		if(meth == null) meth = new ItemStub(this, name, descriptor);
		return meth;
	}

	protected Item getMethodOrStub(int cpMethInfoIndex){
		//pre: all strings in the const are already registered in the proper hash table.
		Item method = null;
		if(Class.cpItems[cpMethInfoIndex] == null){
			int csx = Class.cpIndices[cpMethInfoIndex]; // get class and signature indices
			RefType cls = (RefType) getCpClassEntryAndUpdate(csx>>>16);
			int sx = Class.cpIndices[csx & 0xFFFF];
			
			HString methName = Class.cpStrings[sx>>>16];
			HString methDesc  = Class.cpStrings[sx & 0xFFFF];
			method = cls.getMethodOrStub( methName, methDesc);
		}
		return method;
	}

	/**
	 * Check ClassInfo entry in const pool and update it accordingly if necessary.
	 * <br>That is: if there is not yet a direct reference to an object of type Class, then such an object is created and registered.
	 * @param cpClassInfoIndex index of ClassInfo entry
	 * @return object of this type Class
	 */
	Item getCpClassEntryAndUpdate(int cpClassInfoIndex){
		//pre: all strings in the const pool are already registered in the proper hash table.
		Item cls = Class.cpItems[cpClassInfoIndex];
		if (cls == null) {
			HString registeredClassName = Class.cpStrings[Class.cpIndices[cpClassInfoIndex]];
			if (registeredClassName.charAt(0) == '[') cls = getRefTypeByNameAndUpdate(tcArray, registeredClassName, wktObject);	// is array type
			else cls = getRefTypeByNameAndUpdate(tcRef, registeredClassName, null);	// is class type or interface type
			Class.cpItems[cpClassInfoIndex] = cls;
		}
		return cls;
	}



}
