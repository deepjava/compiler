package ch.ntb.inf.deep.classItems;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class Type extends Item {
	//--- class (static) fields
	static StringTable stab;
//	static RandomAccessFile classFile;

	static HString[] classFileAttributeTable;// well known attributes
//	static final byte[] primitveTypeChars = { 'D', 'F', 'J', 'Z', 'B', 'S', 'C', 'I', 'V' }; // inclusive 'V'
	static Type[] wellKnownTypes;;
	static HString hsNumber, hsString;

	public static Class[] rootClasses;
	public static int nofRootClasses = 0;

	static Class classList, classListTail;
	static int nofClasses = 0;

	//-- const pool arrays
	static Item[] cpItems;
	static HString[] cpStrings;
	static int[]  cpIndices;
	static byte[]  cpTags;
	static int prevCpLenth, constPoolCnt;

	//--- instance fields
	byte category; // { 'P', 'L', '[' } == { tcPrimitive, tcRef, tcArray } declared in: IDescAndTypeConsts
	byte sizeInBits;// { 1..8, 16, 32, 64 }
	char objectSizeOrDim; // [Byte],  if(category == 'L') object size in Byte,   if(category == '[') dimension of the array

	//--- class (static) methods
	protected static void appendClass(Class newClass){
		if(classListTail == null) classList = newClass;  else  classListTail.next = newClass;
		classListTail = newClass;
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

	protected static void skipAttributeAndLogCond(RandomAccessFile clf, int attrLength, int cpIndexOfAttribute) throws IOException{
		if(cpIndexOfAttribute > 0){
			log.print(" skipped attribute: ");
			log.printf("length=%1$d, cp[%2$d] = ", attrLength, cpIndexOfAttribute);
			log.println(cpStrings[cpIndexOfAttribute]);
		}
		clf.skipBytes(attrLength);
//		for(int n = 0; n < attrLength; n++){
//			int value = clf.readUnsignedByte();
//			log.printf("(%1$d,%2$d)", n, value);
//		}
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
		int index = txMaxOfStringOrPrimtiveTypes;
		while(index >= 0 && wellKnownTypes[index] != type) index--;
		if(index < 0) return null; else return wellKnownTypes[index];
	}
	
	protected static Type getPrimitiveTypeByCharName(char charName){
		int typeIndex = getPrimitiveTypeIndex(charName);
		assert typeIndex >= 0 && typeIndex <= txMaxOfPrimtiveTypes;
		return wellKnownTypes[typeIndex];
	}
	
	protected Type getTypeByNameAndUpdate(char typeCategory, HString registredTypeName, Type baseType){
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
			type = new Type(registredTypeName, baseType);
			type.category = tcArray;
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

	protected Type getTypeByDescriptor(HString singleTypeDescriptor){// syntax in EBNF: singleTypeDescriptor = "L" SName ";".
		int length = singleTypeDescriptor.length();
		char category = singleTypeDescriptor.charAt(0);
		Type type = null;
		if(length == 1) type = getPrimitiveTypeByCharName(category);
		else if( category == tcRef ){// singleTypeDescriptor = "L" SName ";".  (EBNF)
			HString sname = singleTypeDescriptor.substring(1, length-1); // assert: sname = class name
			sname = stab.insertCondAndGetEntry(sname);
			type = getTypeByNameAndUpdate(tcRef, sname, null);
		}else if( category == tcArray ){// singleTypeDescriptor = "[" { "[" } ( BaseTypeCategory |  ( "L" SName ";" ) ).  (EBNF)
			int dim = 1;
			while(singleTypeDescriptor.charAt(dim) == tcArray) dim++;
			HString sname = singleTypeDescriptor.substring(dim, length);
			sname = stab.insertCondAndGetEntry(sname);
			Type baseType = getTypeByDescriptor(sname);

			sname = singleTypeDescriptor.substring(0, dim);
			sname = stab.insertCondAndGetEntry(sname);
			type = getTypeByNameAndUpdate(tcArray, sname, baseType);
			type.objectSizeOrDim = (char)dim;
		}else assert false;
//vrb.println("<getTypeByDescriptor 99: type.name="+type.name +", length="+length +", category="+category);
		return type;
	}

	protected Type getReturnType(HString methodDescriptor){// syntax in EBNF: methodDescriptor = "(" FormalParDesc ")" ReturnTypeDesc.
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
		assert category == (byte)category: "pre2";
		assert sizeInBits == (byte)sizeInBits: "pre3";
		this.category = (byte)category;
		this.sizeInBits = (byte)sizeInBits;
		// this.objectSizeOrDim = 0;
	}

	protected Type(HString regName, Type baseType, char category, int sizeInBits, int objectSizeOrDim){
		super(regName, baseType);
		assert category == (byte)category: "pre3";
		assert sizeInBits == (byte)sizeInBits: "pre4";
		assert objectSizeOrDim == (char)objectSizeOrDim: "pre5";
		this.category = (byte)category;
		this.sizeInBits = (byte)sizeInBits;
		this.objectSizeOrDim = (char)objectSizeOrDim;
	}

	public void printTypeCategory() {
		vrb.print("(" + (char)category + ')');
	}

//	public void print(int indentLevel){
//		indent(indentLevel);
////		printJavaAccAndPropertyFlags(1);
//		vrb.print("\t("+ (char)category +')' + name);
//	}
}
