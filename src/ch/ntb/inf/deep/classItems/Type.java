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

import java.io.DataInputStream;
import java.io.IOException;

import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class Type extends Item {
	//--- class (static) fields
	public static final int fieldSizeUnit = 4; // 
	public static final byte nofNewMethods = 4; // bc instructions: {new[0], newarray[1], anewarray[2], multianewarray[3]}
	public static final Item[] newMethods = new Item[nofNewMethods];

	static HString[] classFileAttributeTable;// well known attributes
//	static final byte[] primitveTypeChars = { 'D', 'F', 'J', 'Z', 'B', 'S', 'C', 'I', 'V' }; // inclusive 'V'
	public static Type[] wellKnownTypes;
	public static final Array[] primTypeArrays = new Array[nofWellKnownTypes];
	public static Type wktObject;
	public static Type wktString;
	
	//-- registered well known names
	static HString hsNumber, hsString; // names for number and literal string objects (objects of type Constant, i.e. StdConstant, StringLiteral)
	static HString hsClassConstrName;// name of the class constructor method
	static HString hsCommandDescriptor;// descriptor of a command method

	public static Class[] rootClasses;
	public static int nofRootClasses;

	public static Type classList, classInitListTail, classListTail; // currently objects of type Class or Array
	public static int nofClasses, nofInitClasses;
//	public static Array arrayList;
	public static int nofArrays;

	//-- const pool arrays
	static Item[] cpItems;
	static HString[] cpStrings;
	static int[]  cpIndices;
	static byte[]  cpTags;
	static int prevCpLenth, constPoolCnt;

	//--- instance fields
	public char category; // { 'P', 'L', '[' } == { tcPrimitive, tcRef, tcArray } declared in: IDescAndTypeConsts
	public byte sizeInBits;// { 1..8, 16, 32, 64 }

	public int objectSize;
	public int classFieldsSize; // [Byte], size of all non constant class fields on the target, rounded to the next multiple of "fieldSizeUnit"
//	public int instanceFieldsSize; // [Byte], size of all instance fields on the target, rounded to the next multiple of "fieldSizeUnit"

	//--- class (static) methods
	protected static void appendClass(Type newType){
		if(enAssertion) assert classList != null && classListTail != null;

		classListTail.next = newType;
		classListTail = newType;
		if(newType.category == tcRef) nofClasses++;   else nofArrays++;
	}

	/**
	 * get size in Byte - always a power of 2
	 * @return  {1, 2, 4, 8}
	 */
	public int getTypeSize(){
		int size = sizeInBits>>3;
		if(size <= 0) size = 1;
		return size;
	}

	protected void moveThisClassToInitList(){
		Item pred = classInitListTail, item = pred.next;
		while(item != null && item != this){
			pred = item;
			item = item.next;
		}
		if(enAssertion){
			assert item != null;
			assert item == this;
		}
		if(this == classListTail) classListTail = (Type)pred;
		pred.next = item.next;
		this.next = classInitListTail.next;
		classInitListTail.next = this;
		if(classInitListTail == classListTail)  classListTail = this;
		classInitListTail = this;
		nofInitClasses++;
	}

	protected static void fixUpClassList(){
		//-- delete front stub
		if( classList == classInitListTail )  classInitListTail = null;
		classList = (Type)classList.next;
	}

	protected static void appendRootClass(Class newRootClass){
		rootClasses[nofRootClasses++] = newRootClass;
		appendClass(newRootClass);
	}

	protected static void registerWellKnownNames(){
		hsNumber = stab.insertCondAndGetEntry("#");
		hsString = stab.insertCondAndGetEntry("\"\"");
		hsClassConstrName = stab.insertCondAndGetEntry("<clinit>");
		hsCommandDescriptor = stab.insertCondAndGetEntry("()V");
	}

	private static void registerWellKnownClasses(int tabIndex, String sname){
		if(enAssertion) assert wellKnownTypes[tabIndex] == null;
		HString hname = stab.insertCondAndGetEntry(sname);
		Class cls = new Class(hname);
		wellKnownTypes[tabIndex] = cls;
		appendClass(cls);
	}

	private static void registerPrimitiveType(int tabIndex, char[] sname, int sizeInBits){
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
		
		wktObject = wellKnownTypes[txObject];
		wktString = wellKnownTypes[txString];
}

	protected static void setClassFileAttributeTable(StringTable stab){
		HString[] attrs = new HString[atxInnerClasses+1];
		classFileAttributeTable = attrs;
		attrs[atxConstantValue] = stab.insertCondAndGetEntry("ConstantValue");
		attrs[atxDeprecated] = stab.insertCondAndGetEntry("getHString");
		attrs[atxSynthetic] = stab.insertCondAndGetEntry("Synthetic");
		attrs[atxSourceFile] = stab.insertCondAndGetEntry("SourceFile");
		attrs[atxCode] = stab.insertCondAndGetEntry("Code");
		attrs[atxLocalVariableTable] = stab.insertCondAndGetEntry("LocalVariableTable");
		attrs[atxLineNumberTable] = stab.insertCondAndGetEntry("LineNumberTable");
		attrs[atxExceptions] = stab.insertCondAndGetEntry("Exceptions");
		attrs[atxInnerClasses] = stab.insertCondAndGetEntry("InnerClasses");
	}

	protected static int selectAttribute(int nameIndex){
		HString name = cpStrings[nameIndex];
		HString[] attrs = classFileAttributeTable;
		int atx = attrs.length-1;
		while(atx >= 0 && name != attrs[atx]) atx--;
		return atx;
	}

	protected static void skipAttributeAndLogCond(DataInputStream clfInStrm, int attrLength, int cpIndexOfAttribute) throws IOException{
		if(cpIndexOfAttribute > 0){
			log.print(" skipped attribute: ");
			log.printf("length=%1$d, cp[%2$d] = ", attrLength, cpIndexOfAttribute);
			log.println(cpStrings[cpIndexOfAttribute]);
		}
		clfInStrm.skipBytes(attrLength);
	}

	protected static Item getClassByName(HString registredClassName){
		Item cls = classList;
		while(cls != null && cls.name != registredClassName) cls = cls.next;
		return cls;
	}

	public static int getPrimitiveTypeIndex(char charName){
		int ptIndex;
		switch(charName){
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
	
	protected static Type selectInStringOrPrimitiveTypesByRef(Item type){
		int index = nofWellKnownTypes-1;
		while(index >= 0 && wellKnownTypes[index] != type) index--;
		if(index < 0) return null; else return wellKnownTypes[index];
	}
	
	protected static Type getPrimitiveTypeByCharName(char charName){
		int typeIndex = getPrimitiveTypeIndex(charName);
		assert typeIndex >= 0 && typeIndex < nofWellKnownTypes;
		return wellKnownTypes[typeIndex];
	}
	
	protected static Type getTypeByNameAndUpdate(char typeCategory, HString registredTypeName, Type baseType){
		 // updates for clysses only, not for arrays yet
		Type type = null;
		if(typeCategory == tcPrimitive){
			type =  getPrimitiveTypeByCharName(registredTypeName.charAt(0));
		}else if(typeCategory == tcRef){
			Item cls = getClassByName(registredTypeName);
			if(cls == null) {
				Class newClass = new Class(registredTypeName);
				appendClass(newClass);
				cls = newClass;
			}
			type = (Type)cls;
			type.category = tcRef;
			if(baseType != null) type.type = baseType;
		}else if(typeCategory == tcArray){
			Item cls = getClassByName(registredTypeName);
			if( cls != null) type = (Type)cls;
			else{
				type = new Array(registredTypeName);
				appendClass(type);				
			}
		}else
			assert false;
		return type;
	}

	protected static int nofParameters(HString methDescriptor){
		int pos = methDescriptor.indexOf('(') + 1;
		int end = methDescriptor.lastIndexOf(')');
		assert pos == 1 && end >= 1;
		int nofParams = 0;
		while(pos < end){
			char category = methDescriptor.charAt(pos);
			if(category == tcRef){
				pos = methDescriptor.indexOf(';', pos);
				assert pos > 0;
			}else if(category == tcArray){
				do{
					pos++;
					category = methDescriptor.charAt(pos);
				}while(category == tcArray );
				if(category == tcRef){
					pos = methDescriptor.indexOf(';', pos);
					assert pos > 0;
				}
			}
			pos++;
			nofParams++;
		}
		return nofParams;
	}

	protected static Type getTypeByDescriptor(HString singleTypeDescriptor){// syntax in EBNF: singleTypeDescriptor = "L" SName ";".
//		final boolean verbose = true;
		if(verbose) vrb.printf(">getTypeByDescriptor: descriptor=%1$s\n", singleTypeDescriptor);

		int length = singleTypeDescriptor.length();
		char category = singleTypeDescriptor.charAt(0);
		Type type = null;
		
		if(length == 1) type = getPrimitiveTypeByCharName(category);
		else if( category == tcRef ){// singleTypeDescriptor = "L" SName ";".  (EBNF)
			HString sname = singleTypeDescriptor.substring(1, length-1); // assert: sname = class name
			sname = stab.insertCondAndGetEntry(sname);
			type = getTypeByNameAndUpdate(tcRef, sname, null);
		}else if( category == tcArray ){// singleTypeDescriptor = "[" { "[" } ( BaseTypeCategory |  ( "L" SName ";" ) ).  (EBNF)
			HString sname = stab.insertCondAndGetEntry(singleTypeDescriptor);
			type = getTypeByNameAndUpdate(tcArray, sname, null);
		}else
			assert false;

		if(verbose) vrb.printf("<getTypeByDescriptor: type.name=%1$s\n", type.name);
		return type;
	}

	protected static Type getReturnType(HString methodDescriptor){// syntax in EBNF: methodDescriptor = "(" FormalParDesc ")" ReturnTypeDesc.
		int rparIndex = methodDescriptor.lastIndexOf(')');
		assert rparIndex > 0;
		HString retDesc = methodDescriptor.substring(rparIndex+1);
		Type returnType = getTypeByDescriptor(retDesc);
		return returnType;
	}

	protected static void allocatePoolArray(int length){
		while(prevCpLenth-- > length){// clear unused references
			cpItems[prevCpLenth] = null;
			cpStrings[prevCpLenth] = null;
		}
		if(cpItems == null || length > cpItems.length) {
			cpItems = new Item[length];
			cpStrings = new HString[length];
			cpIndices = new int[length];
			cpTags = new byte[length];
		}
	}

	//--- constructors
	protected Type(HString name, Type type){
		super(name, type);
	}

	protected Type(HString registeredName, char category, int sizeInBits){
		super(registeredName, null); // no base type
		if(enAssertion) assert sizeInBits == (byte)sizeInBits: "pre3";
		this.category = category;
		this.sizeInBits = (byte)sizeInBits;
	}

	//--- instance methods
	public int getObjectSize(){
		return objectSize;
	}

	//--- debug primitives
	public void printSize(){
		vrb.printf("size=%1$d bit", this.sizeInBits);
	}

	public void printTypeCategory() {
		vrb.print("(" + (char)category + ')');
	}
}
