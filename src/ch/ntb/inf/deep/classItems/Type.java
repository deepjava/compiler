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

package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class Type extends Item {
	
	public static final int fieldSizeUnit = 4; // 

	static HString[] attributeTable;// well known attributes
	public static Type[] wellKnownTypes;	// contains primitive types and reference types
	public static RefType wktObject;
	public static RefType wktString;
	public static RefType wktEnum;

	public char category;	// { 'P', 'L', '[' } == { tcPrimitive, tcRef, tcArray } declared in: IDescAndTypeConsts
	public byte sizeInBits; // { 1..8, 16, 32, 64 }

	protected Type(HString name, Type baseType) {
		super(name, baseType);
	}

	protected Type(HString registeredName, char category, int sizeInBits) {
		super(registeredName); // no base type
		if(enAssertion) assert sizeInBits == (byte)sizeInBits: "pre3";
		this.category = category;
		this.sizeInBits = (byte)sizeInBits;
	}

	/**
	 * get size in Byte - always a power of 2
	 * @return  {1, 2, 4, 8}
	 */
	public int getTypeSize() {
		int size = sizeInBits>>3;
		if (size <= 0) size = 1;
		return size;
	}

	private static void registerWellKnownClasses(int tabIndex, String sname) {
		if(enAssertion) assert wellKnownTypes[tabIndex] == null;
		HString hname = stab.insertCondAndGetEntry(sname);
		Class cls = new Class(hname);
		wellKnownTypes[tabIndex] = cls;
		RefType.appendRefType((RefType)cls);
	}

	private static void registerPrimitiveType(int tabIndex, char[] sname, int sizeInBits) {
		if(enAssertion) assert wellKnownTypes[tabIndex] == null;
		HString hname = stab.insertCondAndGetEntry(sname, 1); // register category as HString of length 1
		Type type = new Type(hname, tcPrimitive, sizeInBits); // category = 'P' (primitive)
		wellKnownTypes[tabIndex] = type;
	}

	protected static void setUpBaseTypeTable(){
		wellKnownTypes = new Type[nofWellKnownTypes];
		char[] sName = new char[2];
		sName[0] = tdVoid; registerPrimitiveType(txVoid, sName, 0);
		sName[0] = tdBoolean; registerPrimitiveType(txBoolean, sName, 1);
		sName[0] = tdByte; registerPrimitiveType(txByte, sName, 8);
		sName[0] = tdShort; registerPrimitiveType(txShort, sName, 16);
		sName[0] = tdChar; registerPrimitiveType(txChar, sName, 16);
		sName[0] = tdInt; registerPrimitiveType(txInt, sName, 32);
		sName[0] = tdLong; registerPrimitiveType(txLong, sName, 64);
		sName[0] = tdFloat; registerPrimitiveType(txFloat, sName, 32);
		sName[0] = tdDouble; registerPrimitiveType(txDouble, sName, 64);

		registerWellKnownClasses(txObject, "java/lang/Object");
		registerWellKnownClasses(txString, "java/lang/String");
		registerWellKnownClasses(txEnum, "java/lang/Enum");
		
		wktObject = (RefType) wellKnownTypes[txObject];
		wktString = (RefType) wellKnownTypes[txString];
		wktEnum = (RefType) wellKnownTypes[txEnum];
	}

	protected static void setAttributeTable(StringTable stab) {
		HString[] attrs = new HString[atxInnerClasses+1];
		attributeTable = attrs;
		attrs[atxConstantValue] = stab.insertCondAndGetEntry("ConstantValue");
		attrs[atxSynthetic] = stab.insertCondAndGetEntry("Synthetic");
		attrs[atxSourceFile] = stab.insertCondAndGetEntry("SourceFile");
		attrs[atxCode] = stab.insertCondAndGetEntry("Code");
		attrs[atxLocalVariableTable] = stab.insertCondAndGetEntry("LocalVariableTable");
		attrs[atxLineNumberTable] = stab.insertCondAndGetEntry("LineNumberTable");
		attrs[atxExceptions] = stab.insertCondAndGetEntry("Exceptions");
		attrs[atxInnerClasses] = stab.insertCondAndGetEntry("InnerClasses");
	}

	public static int getPrimitiveTypeIndex(char charName) {
		int ptIndex;
		switch(charName) {
		case tdVoid: ptIndex = txVoid; break;
		case tdByte: ptIndex = txByte; break;
		case tdShort: ptIndex = txShort; break;
		case tdChar: ptIndex = txChar; break;
		case tdInt: ptIndex = txInt; break;
		case tdLong: ptIndex = txLong; break;
		case tdFloat: ptIndex = txFloat; break;
		case tdDouble: ptIndex = txDouble; break;
		default:
			if( charName == tdBoolean)  ptIndex = txBoolean;  else ptIndex = -1;
		}
		return ptIndex;
	}
	
	/** search well known types (primitive types and well known classes) for type 
	 *  returns null or type
	 */
	protected static Type selectInWellKnownTypes(Item type) {
		int index = nofWellKnownTypes-1;
		while (index >= 0 && wellKnownTypes[index] != type) index--;
		if (index < 0) return null; else return wellKnownTypes[index];
	}
	
	/** search primitive type list for type given by char name 
	 *  returns type
	 */
	protected static Type getPrimitiveTypeByCharName(char charName){
		int typeIndex = getPrimitiveTypeIndex(charName);
		assert typeIndex >= 0 && typeIndex < nofWellKnownTypes;
		return wellKnownTypes[typeIndex];
	}
	
	/**
	 * parses parameter typeDesc and returns type
	 */
	protected static Type getTypeByDescriptor(HString typeDesc) {
//		if(dbg) vrb.printf(">getTypeByDescriptor: descriptor=%1$s\n", typeDesc);

		int length = typeDesc.length();
		char category = typeDesc.charAt(0);
		Type type = null;
		
		if (length == 1) type = getPrimitiveTypeByCharName(category);
		else if (category == tcRef) {	// singleTypeDescriptor = "L" sname ";".  (EBNF)
			HString sname = typeDesc.substring(1, length-1); // assert: sname = class name
			sname = stab.insertCondAndGetEntry(sname);
			type = RefType.getRefTypeByNameAndUpdate(tcRef, sname, null);
		} else if (category == tcArray ){	// singleTypeDescriptor = "[" { "[" } ( BaseTypeCategory |  ( "L" SName ";" ) ).  (EBNF)
			HString sname = stab.insertCondAndGetEntry(typeDesc);
			type = RefType.getRefTypeByNameAndUpdate(tcArray, sname, null);
		} else
			assert false;

//		if (dbg) vrb.printf("<getTypeByDescriptor: type.name=%1$s\n", type.name);
		return type;
	}

	//--- debug primitives
	public void printSize() {
		vrb.printf("size=%1$d bit", this.sizeInBits);
	}

	public void printTypeCategory() {
		vrb.print("(" + (char)category + ')');
	}
}
