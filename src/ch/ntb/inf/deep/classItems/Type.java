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
	public static Type wktObject;
	static HString hsNumber, hsString;

	public static Class[] rootClasses;
	public static int nofRootClasses;

	public static Type classList, classListTail;
	public static int nofClasses = 0;

	//-- const pool arrays
	static Item[] cpItems;
	static HString[] cpStrings;
	static int[]  cpIndices;
	static byte[]  cpTags;
	static int prevCpLenth, constPoolCnt;

	//--- instance fields
	public char category; // { 'P', 'L', '[' } == { tcPrimitive, tcRef, tcArray } declared in: IDescAndTypeConsts
	public byte sizeInBits;// { 1..8, 16, 32, 64 }
	public char objectSizeOrDim; // [Byte],  if(category == 'L') object size in Byte,   if(category == '[') dimension of the array

	public int objectSize;
	public int classFieldsSize = -1; // [Byte], size of all non constant class fields on the target, rounded to the next multiple of "fieldSizeUnit" ( -1 => size not yet calculated)
	public int instanceFieldsSize = -1; // [Byte], size of all instance fields on the target, rounded to the next multiple of "fieldSizeUnit" ( -1 => size not yet calculated)

	//--- class (static) methods
	protected static void appendClass(Type newType){
		if(classListTail == null) classList = newType;  else  classListTail.next = newType;
		classListTail = newType;
	}

	protected static void appendRootClass(Class newRootClass){
		rootClasses[nofRootClasses++] = newRootClass;
		appendClass(newRootClass);
	}

	private static void registerWellKnownClasses(int tabIndex, String sname){
		assert wellKnownTypes[tabIndex] == null;
		HString hname = stab.insertCondAndGetEntry(sname);
		Class cls = new Class(hname);
		wellKnownTypes[tabIndex] = cls;
		appendClass(cls);
	}

	private static void registerPrimitiveType(int tabIndex, char[] sname, int sizeInBits){
		assert wellKnownTypes[tabIndex] == null;
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

	protected static int getPrimitiveTypeIndex(char charName){
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
			type = getTypeByNameAndUpdate(tcArray, singleTypeDescriptor, null);
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
		assert sizeInBits == (byte)sizeInBits: "pre3";
		this.category = category;
		this.sizeInBits = (byte)sizeInBits;
	}

	//--- instance methods
	public int getObjectSize(){
		int baseSize = -1;
		if( instanceFieldsSize >= 0 ){
			if( type == null ){
				 if( this == wktObject) baseSize = 0;
			}else{
				baseSize = type.getObjectSize();
			}
			if( baseSize >= 0) objectSize = baseSize + instanceFieldsSize;
		}
		return objectSize;
	}

	static void completeLoading(){
		if(verbose) vrb.println(">completeLoading");
		Type type = classList;
		while(type != null){
			int objSize = type.getObjectSize();
			vrb.printf("Type %1$s: clsFieldsSize=%2$d, instFieldsSize=%3$d, objSize=%4$d <%5$s>\n", type.name, type.classFieldsSize, type.instanceFieldsSize, objSize, type.getClass().getName());
			
			type = (Type)type.next;
		}
		if(verbose) vrb.println("<completeLoading");
	}


	//--- debug primitives
	public void printTypeCategory() {
		vrb.print("(" + (char)category + ')');
	}

//	public void print(int indentLevel){
//		indent(indentLevel);
////		printJavaAccAndPropertyFlags(1);
//		vrb.print("\t("+ (char)category +')' + name);
//	}
}
