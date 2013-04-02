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
import java.io.InputStream;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.host.ClassFileAdmin;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.linker.*;
import ch.ntb.inf.deep.strings.HString;

public class Class extends RefType implements ICclassFileConsts, ICdescAndTypeConsts, ICjvmInstructionOpcs {
	//--- static fields
	private static final int fieldListArrayLength = 9;	// indices are calculated with sizeInBits>>3 -> 0,1,2,4,8
	private static final Item[] instFieldLists = new Item[fieldListArrayLength]; // instance field lists, used by method readFields
	private static final Item[] classFieldLists = new Item[fieldListArrayLength]; // class field lists, used by method readFields
	private static final Item[] constFieldLists = new Item[fieldListArrayLength]; // class constants lists, used by method readFields
	static {
		assert fieldSizeUnit >= 4 && (fieldSizeUnit & (fieldSizeUnit-1)) == 0;
	}

	public static Class initClasses, initClassesTail;	// classes with class constructor (StdClasses and Interfaces)
	public static Class nonInitClasses, nonInitClassesTail;	// classes without class constructor (only StdClasses)
	public static Class[] extLevelOrdClasses, extLevelOrdInterfaces;	// all classes and interfaces are linked in lists according to their extension level
//	private static Class enums, enumArrays;
	public static Array arrayClasses;	// array classes 
	public static Class constBlockInterfaces;	// interfaces which need a constant block and type descriptor
												// - are used as array component and whose type is checked for or (2)
												// - have a class constructor (clinit)
	public static Class[] rootClasses;	// contains all root classes
	public static int nofRootClasses;	// number of root classes

	public static int nofStdClasses; // number of objects of categories {StdClass, Enum, EnumArray}
	public static int nofInterfaceClasses; // number of objects of categories {Interface}
	public static int nofInitClasses; // number of objects of categories {StdClass, Interface} with class constructor (<clinit>)}
	public static int nofNonInitClasses; // number of objects of categories {StdClass} without class constructor (<clinit>)}
	public static int nofArrays;// number of objects of categories {Array}

	public static int maxExtensionLevelStdClasses, maxExtensionLevelInterfaces;	// maximum extension level of all classes and interfaces 
	public static int maxMethTabLen; // max methTabLength in any standard class
	public static int maxInterfMethTabLen; // max number of methods in any interface
	
	//--- instance fields
	public Class nextClass;	// to build lists with initClasses and nonInitClasses
	public Class nextExtLevelClass;	// to build lists with extLevelOrdClasses and extLevelOrdInterfaces
	public Class nextInterface;	// to build lists with starting point "constBlockInterfaces", see above
	public Item[] constPool; // reduced constant pool

	public Method[] methTable; // standard class: table with all instance methods including superclasses, interface: table with all methods excluding superclasses and excluding class constructor (clinit)
	public int nofMethods; // number of methods (class and instance methods)
	public int nofInstMethods, nofClassMethods; // number of methods defined in this class file
	public int methTabLength; // number of methods for the type descriptor, length of methTable (see above)
	
	public int extensionLevel; // extensionLevel of class java.lang.Object is 0
	public static short currInterfaceId;	// used to number interface id's
	public int chkId = -1;	// every interface whose type is checked for gets a unique identifier
	public static short currInterfaceChkId;	// used to number interface id's whose type is checked
	
	public Item instFields, firstInstReference;	// list of class fields, pointer to first field which is a reference
	public Item classFields, firstClassReference;	// list of class fields, pointer to first field which is a reference
	public Item constFields;	// list of const fields (static, final <primitive type>)
	public int nofInstFields, nofClassFields, nofConstFields;	// number of fields
	public int nofInstRefs, nofClassRefs;	// number of fields which are references
		
	public Class[] interfaces;	// all interfaces this class implements, if this class is an interface array contains all superinterfaces
	public int nofInterfaces;	// number of interfaces
	public InterfaceList intfCallList; // contains all implemented interfaces including superinterfaces which contain methods which are called by invokeinterface
	public InterfaceList intfTypeChkList; // contains all implemented interfaces including superinterfaces which type is checked for

	private Class[] imports;	// imported classes (StdClasses, Interfaces) which are found in the const pool of this class and which have a class file (no arrays)
	private int nofImports;	// number of imports, no arrays as they have no class files
	
	public FixedValueEntry constantBlock; // reference to the first entry of the constant block (=constBlockSize entry)
	public FixedValueEntry codeBase; // reference to the codeBase entry
	public FixedValueEntry typeDescriptor; // reference to the type descriptor (size entry)
	public FixedValueEntry constantBlockChecksum; // reference to the end of the constant block (=fcs entry)
	
	public int typeDescriptorSize; // size of the type descriptor on the target (in byte)
	public int typeDescriptorOffset;	// offset in bytes from start of constant block to type descriptor (size)
	public int stringPoolSize; // size of this pool on the target (in byte)
	public int constantPoolSize; // size of this pool on the target (in byte)
	
	public Segment codeSegment, varSegment, constSegment; // references to the memory segments for this class
	public int codeOffset, varOffset, constOffset; // the offset of the code/class fields/constant block in the dedicated segment

	HString srcFileName; // file ident + ".java", e.g.  String.java  for java/lang/String.java
	
	// debug fields
	int magic, version;
	
	// const pool arrays
	static Item[] cpItems;
	static HString[] cpStrings;
	static int[] cpIndices;
	static byte[] cpTags;
	static int prevCpLenth, constPoolCnt;

	public Class(HString registeredCpClassName){
		super(registeredCpClassName, null);
		category = tcRef;
		sizeInBits = 32;
	}
	
	/**
	 * loads a class with chosen set of attribute
	 * further attributes are skipped
	 * all imported classes in the constant pool are loaded as well
	 */
	protected void loadClass(int userReqAttributes) throws IOException {
		if (CFR.clsDbg) vrb.println("load class: " + name);
		if (verbose) vrb.println(">loadClass: " + name);
		if ((accAndPropFlags & ((1<<dpfClassLoaded) | (1<<dpfSynthetic))) == 0 ) {	// if not yet loaded and not synthetic
			InputStream inStrm = ClassFileAdmin.getClassFileInputStream(name);	// new FileInputStream

			if (inStrm == null) {errRep.error(300, name.toString()); return;}
			DataInputStream clfInStrm = new DataInputStream(inStrm);	// new DataInputStream

			loadConstPool(clfInStrm);
			accAndPropFlags |= clfInStrm.readUnsignedShort();	// read access and property flags of class file

			if (verbose) {
				printOrigConstPool("\nconstant pool \nstate: 0");
				printClassList("class list \nstate: 0");
				print(0);
			}

			updateConstPool();

			//				if(verbose){
			//					printOrigConstPool("\nconstant pool \nstate: 1");
			//					printClassList("class list \nstate: 1");
			//					print(0);
			//				}

			clfInStrm.readUnsignedShort();	// read this class index

			int thisSupClassCpInx = clfInStrm.readUnsignedShort();
			if (verbose) vrb.println("thisSupClassCpInx=" + thisSupClassCpInx);
			if (thisSupClassCpInx > 0) {
				int constPoolInx = cpIndices[thisSupClassCpInx];
				type = (Class)constPool[constPoolInx];
				if (verbose) {
					vrb.print("superClassName="); type.printName(); vrb.println();
				}
			}

			readInterfaces(clfInStrm);
			readFields(clfInStrm);
			readMethods(clfInStrm, userReqAttributes);
			readClassAttributes(clfInStrm, userReqAttributes);

//			if ((accAndPropFlags & (1<<apfEnum)) != 0) {
//				System.out.println("ist enum");
//				Dbg.printAccAndPropertyFlags(accAndPropFlags); Dbg.println();
//			}
//			if ((accAndPropFlags & (1<<apfEnumArray)) != 0) {
//				System.out.println("ist enum");
//				Dbg.printAccAndPropertyFlags(accAndPropFlags); Dbg.println();
//			}
//			if ((accAndPropFlags & (1<<apfEnum)) == 0) {	// if class or interface but not an enum
				// analyse byte code
				if (verbose) vrb.println(">analyseByteCode:");
				Item item = methods;
				while (item != null) {
					Method meth = (Method)item;
					if (verbose) {
						vrb.print("\nmethod: "); meth.printHeader(); vrb.print(" (owner="); meth.owner.printName(); vrb.println(')');
					}
					ByteCodePreProc.analyseCodeAndFixCpRefs(cpIndices, constPool, meth.code);
					item = item.next;
				}
				if (verbose) vrb.println("<analyseByteCode");

				this.accAndPropFlags |= (1<<dpfClassLoaded);
//			}

			if (verbose) {
				vrb.println("\n>dump of class: " + name);
				//					stab.print("String Table in state: 3");
				//					printOrigConstPool("\nconstant pool \nstate: 3");
									printReducedConstPool("\nconstant pool \nstate: 3");
				//					printClassList("class list \nstate: 3");
				//					print(0);
				Dbg.printAccAndPropertyFlags(accAndPropFlags); Dbg.println();
				vrb.println("\n<end of dump: " + name + "++++++++++++++++++++++++");
			}

			clfInStrm.close();
		}
		//--- load referenced classes
		if (CFR.clsDbg) vrb.println("\tload referenced classes: #=" + (nofImports-1));
		if ((accAndPropFlags & (1<<dpfClassLoaded)) != 0) {
			nofImports--;
			imports = new Class[nofImports];
			int impIndex = nofImports;
			for (int cpx = constPool.length-1; cpx >= 0; cpx--) {
				Item item = constPool[cpx];
				if (item instanceof Class) {
					Class cls = (Class)item;
					if (impIndex > 0) imports[--impIndex] = (Class)cls;	// without first entry (this class)
					if ((cls.accAndPropFlags & ((1<<dpfClassLoaded) | (1<<dpfSynthetic))) == 0) { // if not yet loaded and not synthetic
						cls.loadClass(userReqAttributes);
					}
				} // else is array which has no class file
			}
		} else if (CFR.clsDbg) vrb.println("\tclass not loaded yet: " + name);
		if (verbose) vrb.println("<loadClass");
		if (CFR.clsDbg) vrb.println("class: " + name + " loaded");
	}

	/**
	 * allocates arrays to parse constant pool, create new array if existing array is too small
	 */
	private static void allocatePoolArray(int length) {
		while (prevCpLenth-- > length){ // clear unused references
			cpItems[prevCpLenth] = null;
			cpStrings[prevCpLenth] = null;
		}
		if (cpItems == null || length > cpItems.length) {
			cpItems = new Item[length];
			cpStrings = new HString[length];
			cpIndices = new int[length];
			cpTags = new byte[length];
		}
	}

	/**
	 * parameter nameIndex is index into constant pool where the name of an attribute is found
	 * returns index in attribute table with that attribute 
	 */
	private static int selectAttribute(int nameIndex) {
		HString name = cpStrings[nameIndex];
		HString[] attrs = attributeTable;
		int atx = attrs.length-1;
		while (atx >= 0 && name != attrs[atx]) atx--;
		return atx;
	}

	/**
	 * skips next attribute with length attrLength, when index is > 0 skipped attribute is logged  
	 */
	private static void skipAttribute(DataInputStream clfInStrm, int attrLength, int cpIndexOfAttribute) throws IOException {
		if (verbose) {
			if (cpIndexOfAttribute > 0) {
				vrb.print(" skipped attribute: ");
				vrb.printf("length=%1$d, cp[%2$d] = ", attrLength, cpIndexOfAttribute);
				vrb.println(cpStrings[cpIndexOfAttribute]);
			}
		}
		clfInStrm.skipBytes(attrLength);
	}

	private static int getNextInterfaceId() {
		currInterfaceId++;
		return currInterfaceId;
	}

	/**
	 * append class to list of root classes, appends class to reference type list
	 */
	protected static void appendRootClass(Class newRootClass){
		rootClasses[nofRootClasses++] = newRootClass;
		RefType.appendRefType(newRootClass);
	}
	
	/**
	 * searches fieldName in die list of all fields
	 * beginning with instance fields
	 */
	Item getField(HString fieldName){
		Item item = instFields;
		while(item != null && item.name != fieldName) item = item.next;
		if(item == null && type != null) item = ((Class)type).getField(fieldName);
		return item;
	}

	public Method getClassConstructor() {
		if(this.methods != null) return (Method)this.methods.getItemByName(CFR.hsClassConstrName);
		return null;
	}

	/**
	 * The method with methodName is selected and returned.
	 * If the method is not found, an new Method is created and inserted, if the method is found it is checked for the correct descriptor.
	 * @param methName  a registered string
	 * @param methDescriptor a registered string
	 * @return the selected method or newly created one
	 */
	Method insertCondAndGetMethod(HString methName, HString methDescriptor) {
		//pre: all strings in the const are already registered in the proper hash table.
		Item item = methods;
		while(item != null && (item.name != methName || ((Method)item).methDescriptor != methDescriptor) ) item = item.next;
		Method method;
		if(item != null) {
			method = (Method)item;
		} else {// create Method and update method list
			Type returnType = Method.getReturnType(methDescriptor);
			method = new Method(methName, returnType, methDescriptor);
			method.next = methods;  methods = method;
		}
		return method;
	}

	/**
	 * returns existing field or newly created stub of field
	 */
	private Item getFieldOrStub(int cpFieldInfoIndex) {
		//pre: all strings in the const are already registered in the proper hash table.
		Item field = cpItems[cpFieldInfoIndex];
		if(field == null) {
			int csx = cpIndices[cpFieldInfoIndex]; // get class and signature indices
			Class cls = (Class)getCpClassEntryAndUpdate(csx>>>16); // field belongs to this class
			int sx = cpIndices[csx & 0xFFFF];	// get NameAndType
			HString fieldName = cpStrings[sx>>>16];	// get name
			HString fieldDesc  = cpStrings[sx & 0xFFFF];	// get type as HString
			Type fieldType = getTypeByDescriptor(fieldDesc);	// get type object
			
			field = cls.getField(fieldName);	// search field list of that class
			if(field == null ) field = new ItemStub(cls, fieldName, fieldType);
		}
		return field;
	}

	/**
	 * reads all interfaces from class file
	 * class can be standard class, enum or interface itself
	 * interfaces are stored in array with references to those classes
	 */
	private void readInterfaces(DataInputStream clfInStrm) throws IOException {
		int cnt = clfInStrm.readUnsignedShort();
		if(cnt > 0) {
			nofInterfaces = cnt;
			interfaces = new Class[cnt];
			for (int intf = 0; intf < cnt; intf++){
				int intfInx = clfInStrm.readUnsignedShort();
				interfaces[intf] = (Class)cpItems[intfInx];
			}
		}
	}

	/** 
	 * adds item to field lists (instance, class, const) according to size
	 */
	private void addItemToFieldList(Item item){
		if(verbose) vrb.println(">addItemToFieldList");
		Type type = (Type)item.type;
		char typeCategory = type.category;
		char typeNickName = type.name.charAt(0);
		int sizeInBits = type.sizeInBits;
		int fieldListIndex = 0;
		int sizeInByte = sizeInBits>>3; // fieldList={0, 1, 2, 4, 8}
		switch(sizeInByte){
		case 8: // tcPrimitive: long, double
			if(typeNickName == tdLong) fieldListIndex = flxLong; else fieldListIndex = flxDouble;
			break;
		case 4: // tcRef, tcArray, tcPrimitive: int, float
			if(typeCategory != tcPrimitive) fieldListIndex = flxRef;
			else if(typeNickName == tdFloat) fieldListIndex = flxFloat; 
			else  fieldListIndex = flxInt;
			break;
		case 2: // tcPrimitive: short, char
			fieldListIndex = flxShortChar;
			break;
		case 1: // tcPrimitive: byte
			fieldListIndex = flxByte;
			break;
		case 0: // tcPrimitive: boolean ?? ever used?
			fieldListIndex = flxBits;
			break;
		default:
			assert false;
		}
		int flags = item.accAndPropFlags;
		if ((flags & (1<<apfStatic)) == 0) {	// not static => instance field
			item.next = instFieldLists[fieldListIndex];  instFieldLists[fieldListIndex] = item;
			nofInstFields++;
			if(fieldListIndex == flxRef) nofInstRefs++;
		} else if ((flags & (1<<dpfConst)) == 0) {	// static, but not constant => class field
			item.next = classFieldLists[fieldListIndex];  classFieldLists[fieldListIndex] = item;			
			nofClassFields++;
			if(fieldListIndex == flxRef) nofClassRefs++;
		} else {	// constant of primitive type or String
			item.next = constFieldLists[fieldListIndex];  constFieldLists[fieldListIndex] = item;			
			nofConstFields++;
		}
		if (verbose) vrb.println("<addItemToFieldList");
	}

	/**
	 * reads out field list with all the fields with the same size in order
	 */
	private Item getFieldListAndUpdate(Item[] fieldLists) {
		if(verbose) vrb.printf(">getFieldListAndUpdate: class: %1$s\n", name);

		Item head = null, tail = null;

		for (int category = fieldLists.length-1; category >= 0; category--) {
			Item list = fieldLists[category];
			fieldLists[category] = null;	// resets list for next class
			Item item = list;
			while (item != null) {
				list = item.next;
				item.next = null;
				//-- append
				if (tail == null) head = item; else tail.next = item;
				tail = item;
				item = list;
			}
		}

		if(verbose) vrb.println("<getFieldListAndUpdate");
		return head;
	}

	/**
	 * read fields from class file, fields are sorted to type and chained
	 */
	private void readFields(DataInputStream clfInStrm) throws IOException {
		final boolean verbose = false;
		
		if(verbose) vrb.println(">readFields: " + name);
		assert instFields == null;
		
		int fieldCnt = clfInStrm.readUnsignedShort();
		while(fieldCnt > 0){
			int flags;
			HString name, descriptor;

			flags = clfInStrm.readUnsignedShort(); //read access and property flags
			//--- read name and descriptor
			int index = clfInStrm.readUnsignedShort();
			name = cpStrings[index];
			index = clfInStrm.readUnsignedShort();
			descriptor = cpStrings[index];

			Field field = null;

			if(verbose) vrb.printf(" readFields: cls=%1$s, desc=%2$s\n", name, descriptor);
			//--- read field attributes {ConstantValue, Deprecated, Synthetic}
			int attrCnt = clfInStrm.readUnsignedShort();
			while(attrCnt-- > 0){
				index = clfInStrm.readUnsignedShort();
				int attr = selectAttribute(index);
				int attrLength = clfInStrm.readInt();
				switch(attr) {
				case atxConstantValue:	// apfFinal is set
					index = clfInStrm.readUnsignedShort();
					int rcpIndex = cpIndices[index];
					Item cpField = constPool[rcpIndex];

					Type type = getTypeByDescriptor(descriptor);
					if(verbose) vrb.printf("   readFields: field.desc=%1$s, const: name=%2$s, type=%3$s\n", descriptor, cpField.name, cpField.type.name);

					assert cpField instanceof Constant;
					field = new ConstField(name, type, (Constant)cpField);
					if((flags & (1<<apfStatic)) != 0) flags |= (1<<dpfConst);
					break;
				case atxSynthetic:
					flags |= (1<<dpfSynthetic);
					break;
				default:
					skipAttribute(clfInStrm, attrLength, index);
				}
			}

			if(field == null) {	// is regular, non constant field
				Type type = getTypeByDescriptor(descriptor);
				field = new Field(name, type);
			}
			
			flags |= field.accAndPropFlags;
			field.accAndPropFlags = flags;

			addItemToFieldList(field);
			field.owner = this;
			
			fieldCnt--;
		}
		
		assert instFields == null;
		constFields = getFieldListAndUpdate(constFieldLists);

		firstClassReference = classFieldLists[flxRef];
		classFields = getFieldListAndUpdate(classFieldLists);

		firstInstReference = instFieldLists[flxRef];
		instFields = getFieldListAndUpdate(instFieldLists);

		// chain the field lists
		Item tail = null;
		if (classFields != null) tail = classFields.getTail();
		classFields = appendItemList(classFields, tail, constFields);
		tail = null;
		if (instFields != null) tail = instFields.getTail();
		instFields = appendItemList(instFields, tail, classFields);

		if(verbose) vrb.println("<readFields");
	}

	/**
	 * read methods from class files, methods are chained
	 */
	private void readMethods(DataInputStream clfInStrm, int userReqAttributes) throws IOException {
		int methodCnt = clfInStrm.readUnsignedShort();
		nofMethods = methodCnt;
		assert methods == null;
		
		int nofClsMeths = 0,  nofInstMeths = 0;
		Item clsMethHead = null, clsMethTail = null;
		Item instMethHead = null, instMethTail = null;

		while (methodCnt-- > 0){
			int flags;
			HString name, descriptor;

			flags = clfInStrm.readUnsignedShort(); //read access and property flags
			//--- read name and descriptor
			int index = clfInStrm.readUnsignedShort();
			name = cpStrings[index];
			index = clfInStrm.readUnsignedShort();
			descriptor = cpStrings[index];
			if (descriptor == CFR.hsCommandDescriptor && name != CFR.hsClassConstrName && (flags & (1<<apfStatic)) != 0){
				flags |= (1<<dpfCommand);	// this method can be a command
			}
			Type returnType = Method.getReturnType(descriptor);
			Method method = new Method(name, returnType, descriptor);
			method.owner = this;
			
			//--- read method attributes {Code, Deprecated, Synthetic}
			int attrCnt = clfInStrm.readUnsignedShort();
			while (attrCnt-- > 0){
				int cpInxOfAttr = clfInStrm.readUnsignedShort();
				int attr = selectAttribute( cpInxOfAttr );
				int attrLength = clfInStrm.readInt();
				switch (attr){
				case atxCode:
					if ((userReqAttributes&(1<<atxCode)) == 0) {
						skipAttribute(clfInStrm, attrLength, 0); // skip without logging
						break;
					}
					method.maxStackSlots = clfInStrm.readUnsignedShort();
					method.maxLocals = clfInStrm.readUnsignedShort();
					int codeLen = clfInStrm.readInt();
					method.code = new byte[codeLen];
					clfInStrm.read(method.code);
					
					//--- read exception table
					int excTabLen = clfInStrm.readUnsignedShort();
					if (excTabLen > 0) {
						method.exceptionTab = new ExceptionTabEntry[excTabLen];
						for (int exc = 0; exc < excTabLen; exc++) {
							ExceptionTabEntry entry = new ExceptionTabEntry();
							method.exceptionTab[exc] = entry;
							entry.startPc = clfInStrm.readUnsignedShort();
							entry.endPc = clfInStrm.readUnsignedShort();
							entry.handlerPc = clfInStrm.readUnsignedShort();
							int catchTypeInx = clfInStrm.readUnsignedShort();
							entry.catchType = (Class)cpItems[catchTypeInx];
						}
					}
					
					//--- read attributes of the code attribute {LineNumberTable, LocalVariableTable}
					int codAttrCnt = clfInStrm.readUnsignedShort();
					while (codAttrCnt-- > 0) {
						int codAttrIndex = clfInStrm.readUnsignedShort();
						int codeAttr = selectAttribute(codAttrIndex);
						int codAttrLen = clfInStrm.readInt();
						if (codeAttr == atxLocalVariableTable){
							if ((userReqAttributes & (1<<atxLocalVariableTable)) == 0){
								skipAttribute(clfInStrm, codAttrLen, 0); // skip without logging
							} else {
								int locVarTabLength = clfInStrm.readUnsignedShort();
								if (locVarTabLength > 0){
									method.localVars = new LocalVar[method.maxLocals];
									while (locVarTabLength-- > 0){
										LocalVar locVar = new LocalVar();
										locVar.startPc = clfInStrm.readUnsignedShort();
										locVar.length = clfInStrm.readUnsignedShort();
										locVar.name = cpStrings[ clfInStrm.readUnsignedShort() ];
										locVar.type = getTypeByDescriptor(cpStrings[clfInStrm.readUnsignedShort()] );
										locVar.index = clfInStrm.readUnsignedShort();
										method.insertLocalVar(locVar);
									}
								}
							}
						} else if (codeAttr == atxLineNumberTable){
							if ((userReqAttributes & (1<<atxLineNumberTable)) == 0) {
								skipAttribute(clfInStrm, codAttrLen, 0); // skip without logging
							} else {
								int lineNrTabLength = clfInStrm.readUnsignedShort();
								int[] lineNrTab = new int[lineNrTabLength];
								method.lineNrTab = lineNrTab;
								for (int lnp = 0; lnp < lineNrTabLength; lnp++) lineNrTab[lnp] = clfInStrm.readInt();
							}
						} else {	// skip
							skipAttribute(clfInStrm, codAttrLen, codAttrIndex);
						}
					}
					break;
				case atxSynthetic:
					flags |= (1<<dpfSynthetic);
					break;
				default:
					skipAttribute(clfInStrm, attrLength, index);
				}
			}

			flags |= method.accAndPropFlags;
			method.accAndPropFlags = flags;
			
			//--- append method
			if ((flags & (1<<apfStatic)) != 0) {	// class method
				nofClsMeths++;
				if (clsMethHead == null) clsMethHead = method; else clsMethTail.next = method;
				clsMethTail = method;
			} else {	// instance method
				nofInstMeths++;
				if(instMethHead == null) instMethHead = method; else instMethTail.next = method;
				instMethTail = method;
			}
		}
		assert methods == null;
		methods = appendItemList(clsMethHead, clsMethTail, instMethHead);	// chain class and instance methods
		nofClassMethods = nofClsMeths;
		nofInstMethods = nofInstMeths;
	}

	/**
	 * read attributes from class file
	 */
	private void readClassAttributes(DataInputStream clfInStrm, int userReqAttributes) throws IOException {
		int attrCnt = clfInStrm.readUnsignedShort();
		while (attrCnt-- > 0) {
			int index = clfInStrm.readUnsignedShort();
			int attr = selectAttribute(index);
			int attrLength = clfInStrm.readInt();
			switch (attr){
			case atxSourceFile:
				index = clfInStrm.readUnsignedShort();
				srcFileName = cpStrings[index];
				break;
			case atxInnerClasses: // 4.7.5, p125
				if ((userReqAttributes & (1<<atxInnerClasses)) == 0) skipAttribute(clfInStrm, attrLength, index);
				else{
					// TODO Auto-generated method stub
					assert false: "TODO";
				}
				break;
			default:
				skipAttribute(clfInStrm, attrLength, index);
			}
		}
	}

	public static void releaseLoadingResources(){
		cpItems = null;  cpStrings = null;  cpIndices = null;  cpTags = null;
		prevCpLenth = 0;  	constPoolCnt = 0;
		HString.releaseBuffers();
		ClassFileAdmin.clear();

//		StringTable.resetTable();
//		hsNumber = null;
//		wellKnownTypes = null;
//		classFileAttributeTable = null;
	}

	void fixInstanceMethodsOfThisClass(){
		Item meth = methods;
		while (meth != null){
			if (meth.index >= 0) ((Method)meth).fixed = true;
			meth = meth.next;
		}
	}

	private void loadConstPool(DataInputStream clfInStrm) throws IOException{
		if (verbose) vrb.println(">loadConstPool:");
		
		magic = clfInStrm.readInt();
		if (magic != 0xcafeBabe) throw new IOException("illegal class file");
		if (verbose) vrb.printf("magic=0x%1$4x\n", magic);

		version = clfInStrm.readInt();
		if (verbose) vrb.printf("version=%1$d.%2$d\n", (version&0xFFFF), (version>>>16));

		constPoolCnt = clfInStrm.readUnsignedShort();
		if (verbose) vrb.printf("constPoolCnt=%1$d\n", constPoolCnt);
		allocatePoolArray(constPoolCnt);
		for (int pEntry = 1; pEntry < constPoolCnt; pEntry++) {
			int tag = clfInStrm.readUnsignedByte();
			cpTags[pEntry] = (byte)tag;
			cpIndices[pEntry] = 0;  cpItems[pEntry] = null;  cpStrings[pEntry] = null;
			switch(tag){
			case cptUtf8:  cpStrings[pEntry] = HString.readUTFandRegister(clfInStrm); break;
			
			case cptInteger: cpIndices[pEntry] = clfInStrm.readInt(); break; // integer value
			case cptFloat: cpIndices[pEntry] = clfInStrm.readInt(); break; // float pattern
			
			case cptLong: case cptDouble:
				cpIndices[pEntry++] = clfInStrm.readInt();
				cpIndices[pEntry] = clfInStrm.readInt();
				cpTags[pEntry] = cptExtSlot;  cpItems[pEntry] = null;  cpStrings[pEntry] = null;
				break;
			
			case cptClass:
				cpIndices[pEntry] = clfInStrm.readUnsignedShort(); 
				nofImports++;
				break;
			case cptString: cpIndices[pEntry] = clfInStrm.readUnsignedShort(); break; // string index
			
			case cptFieldRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptMethRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptIntfMethRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptNameAndType: cpIndices[pEntry] = clfInStrm.readInt(); break;// (name index) <<16, descriptor index
			default:
				throw new IOException("illegal tag in const pool");
			}
		}
		if (verbose) vrb.println("<loadConstPool");
	}

	private void updateConstPool() throws IOException {
		if (verbose) vrb.println(">updateConstPool:");
		//pre: all strings in the const are already registered in the proper hash table.
		int nofItems = 0;
		int pEntry;
		for (pEntry = 1; pEntry < constPoolCnt; pEntry++) {// constPoolCnt
			int tag = cpTags[pEntry];
			switch(tag) {
			 case cptExtSlot: case cptUtf8: // cptExtSlot, Utf8 string
				 break;
			case cptInteger: // integer literal
				cpItems[pEntry] = new StdConstant(CFR.hsNumber, wellKnownTypes[txInt], cpIndices[pEntry], 0);
				nofItems++;
				break;
			case cptFloat:  // float literal
				cpItems[pEntry] = new StdConstant(CFR.hsNumber, wellKnownTypes[txFloat], cpIndices[pEntry], 0);
				nofItems++;
				break; // float pattern
			case cptLong:
				cpItems[pEntry] = new StdConstant(CFR.hsNumber, wellKnownTypes[txLong], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptDouble:
				cpItems[pEntry] = new StdConstant(CFR.hsNumber, wellKnownTypes[txDouble], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptClass: // class index
				Item item = getCpClassEntryAndUpdate(pEntry);
				cpItems[pEntry] = item;
				if (item instanceof Array) nofImports--; // arrays get not included in imports, they have no associated class file
				nofItems++;
				break;
			case cptString: 
				cpItems[pEntry] = new StringLiteral(CFR.hsString, cpStrings[cpIndices[pEntry]]);
				nofItems++;
				break;
			case cptFieldRef:
				cpItems[pEntry] = getFieldOrStub(pEntry);
				nofItems++;
				break;
			case cptMethRef:
//				printOrigConstPool("\nconstant pool \nstate: x");
				cpItems[pEntry] = getMethodOrStub(pEntry);
				nofItems++;
				break;
			case cptIntfMethRef:
				cpItems[pEntry] = getMethodOrStub(pEntry);
				nofItems++;
				break;
			case cptNameAndType: break;// (name index) <<16, descriptor index
			default:
				throw new IOException("illegal tag in const pool");
			}
		}

		assert pEntry == constPoolCnt;
		constPool = new Item[nofItems];
		while (--pEntry > 0){
			Item item = cpItems[pEntry];
			if (item != null){
				constPool[--nofItems] = item;
				cpIndices[pEntry] = nofItems;
			} else {
				cpIndices[pEntry] = 0;
			}
		}
		assert nofItems == 0;
		if (verbose) vrb.println("<updateConstPool");
	}
	
	/**
	 * extension level is set, size of class and instance fields are calculated, index of methods are set
	 * imports are handled, class is added to initClassesList or nonInitClassesList
	 * superclasses or superinterfaces are handled first (recursively)
	 */
	protected void fixupLoadedClasses() {
//		boolean verbose = true;
		if ((accAndPropFlags & 1<<dpfClassMark) == 0 ) {	// mark not set -> class not done yet
			if (verbose) vrb.println(">fixup of class: " + this.name);
			accAndPropFlags |= 1<<dpfClassMark;	// set mark

			if (type == null) {objectSize = 0; extensionLevel = 0;}	// java/lang/object
			else if ((accAndPropFlags & 1<<apfInterface) == 0 ) {	// std-class
				((Class)type).fixupLoadedClasses();	// handle superclass first
				Class baseCls = (Class)type;
				baseCls.accAndPropFlags |= (1<<dpfExtended);	// class has extensions 
				objectSize = baseCls.objectSize;
				extensionLevel = baseCls.extensionLevel + 1;
				if (extensionLevel > maxExtensionLevelStdClasses ) maxExtensionLevelStdClasses = extensionLevel;
			} else {	// interface
				if (interfaces != null) {
					for (Class intf : interfaces) intf.fixupLoadedClasses();	// first handle superinterface
					extensionLevel = interfaces[0].extensionLevel + 1;
					accAndPropFlags |= (1<<dpfExtended);	// interface has extensions 
				} else extensionLevel = 1;
				if (extensionLevel > maxExtensionLevelInterfaces ) maxExtensionLevelInterfaces = extensionLevel;
			}
			
			// calculate size of all instance fields (excluding fields of superclasses)
			Item item = instFields;
			Item clsFields = classFields;
			objectSize = (objectSize + fieldSizeUnit-1) & -fieldSizeUnit;
			while (item != clsFields) {
				item.offset = objectSize;
				objectSize +=  ((Type)item.type).getTypeSize();
				item = item.next;
			}

			// calculate size of all class fields
			item = classFields;
			clsFields = constFields;
			classFieldsSize = 0;
			while (item != clsFields) {
				item.offset = classFieldsSize;
				classFieldsSize +=  ((Type)item.type).getTypeSize();
				item = item.next;
			}

			// all instance methods get index, index of class methods will be -1
			// overwritten methods get index of method of superclass
			// handle interfaces differently
			Item meth = methods;
			if ((accAndPropFlags & 1<<apfInterface) == 0 ) {	// std-class	
				if (type == null) {	//  java/lang/Object
					methTabLength = 0;
					while (meth != null) {
						meth.index = -1;
						if ((meth.accAndPropFlags & (1<<apfStatic)) == 0) meth.index = methTabLength++;
						meth = meth.next;
					}
				} else {	// any other class ( not java/lang/Object)
					Class baseCls = (Class)type;
					methTabLength = baseCls.methTabLength;
					while (meth != null) {
						meth.index = -1;
						if ((meth.accAndPropFlags & (1<<apfStatic)) == 0) {	// instance method
							Item baseMeth = baseCls.getMethod(meth.name, ((Method)meth).methDescriptor);
							if (baseMeth == null) meth.index = methTabLength++;
							else meth.index = baseMeth.index;
						}
						meth = meth.next;
					}
				}
			} else {	// interface
				methTabLength = 0;
				while (meth != null) {
					meth.index = -1;
					if ((meth.accAndPropFlags & (1<<apfStatic)) == 0) {	// is not clinit
						meth.index = methTabLength++;
					}
					meth = meth.next;
				}
			}

			if (imports != null) {	// handle all imports
				for(int index = imports.length-1; index >= 0; index--) imports[index].fixupLoadedClasses();
			}

			// add classes or interfaces with class constructor to list "initClasses"
			// add classes without class constructor and which are not interfaces to "nonInitClasses"
			if (methods != null) {
				ClassMember clsInit = (Method)methods.getItemByName(CFR.hsClassConstrName);
				if (clsInit != null) {	// std-class or interface with class constructor
					if (initClassesTail == null ) initClasses = this; else initClassesTail.nextClass = this;
					initClassesTail = this;
					nofInitClasses++;
					if ((accAndPropFlags & (1 << apfInterface)) != 0) {	// additionally add interface with class constructor to interface list
						nextInterface = Class.constBlockInterfaces; 
						Class.constBlockInterfaces = this;
					}
				} else if ((accAndPropFlags & 1<<apfInterface) == 0) {
					if( nonInitClassesTail == null ) nonInitClasses = this; else nonInitClassesTail.nextClass = this;
					nonInitClassesTail = this;
					nofNonInitClasses++;
				}
			}
			if (verbose) vrb.println("<fixup of class: " + this.name);
		}
	}

	/**
	 * sets interface id in superinterfaces, recursively up to root interfaces
	 */
	protected void setIntfIdToRoot(Class[] interfaces) { 
		if (interfaces != null) {
			for (Class intf : interfaces) {
				intf.index = Class.currInterfaceId++;
				setIntfIdToRoot(intf.interfaces);
			}
		}
	}

	/**
	 * creates a list with all interfaces and their superinterfaces this class implements 
	 * and whose index is >= 0. That is, those interfaces contain methods which are called by invokeinterface
	 * the list is then sorted according to it's id (field <code>index</code>).
	 */
	protected void createIntfCallList() { 
		Item item = this;
		do {
			Class[] interfaces = ((Class)item).interfaces;
			if (interfaces != null) addCallInterfaceList(interfaces);
			item = item.type;	// go to super class of this class
		} while (item != null);
	}

	private void addCallInterfaceList(Class[] interfaces) {
		for (int n = 0; n < interfaces.length; n++) {
			Class interf = interfaces[n];
			if (interf.index > 0) {
				if (intfCallList == null) intfCallList = new InterfaceList();
				intfCallList.appendCallId(interf);
			}
			if (interf.interfaces != null) addCallInterfaceList(interf.interfaces);	// interface has superinterfaces
		}	
	}

	/**
	 * creates a list with all interfaces this class and its superclasses implement
	 * and whose type is checked for
	 * the list is then sorted according to it's field <code>chkId</code>
	 */
	protected void createIntfTypeChkList() { 
		Item item = this;
		do {
			Class[] interfaces = ((Class)item).interfaces;
			if (interfaces != null) addCheckInterfaceList(interfaces);
			item = item.type;	// go to super class of this class
		} while (item != null);
		if ((accAndPropFlags & (1<<apfInterface)) != 0 && (accAndPropFlags & (1<<dpfTypeTest)) != 0) {
			if (intfTypeChkList == null) intfTypeChkList = new InterfaceList();
			intfTypeChkList.appendChkId(this);	// interfaces must include own chkId
		}
		if (intfTypeChkList != null) intfTypeChkList.sortChkId();
	}
	
	private void addCheckInterfaceList(Class[] interfaces) {
		for (int n = 0; n < interfaces.length; n++) {
			Class interf = interfaces[n];
			if (interf.chkId > 0) {
				if (intfTypeChkList == null) intfTypeChkList = new InterfaceList();
				intfTypeChkList.appendChkId(interf);
			}
			if (interf.interfaces != null) addCheckInterfaceList(interf.interfaces);	// interface has superinterfaces
		}	
	}

	/**
	 * 
	 */
	public void insertMethods(Method[] table) {
		if (type != null) ((Class)type).insertMethods(table);
		Item meth = methods;
		while (meth != null) {
			if (meth.index >= 0) table[meth.index] = (Method)meth;
			meth = meth.next;
		}
	}
	
	//--- debug primitives
	public void printItemCategory() {
		vrb.print("class");
	}

	public static void printInitSequence() {
		vrb.printf("\n--- init sequence: (#=%1$d)\n", nofInitClasses);
		Class cls = initClasses;
		while (cls != null ){
			if ((cls.accAndPropFlags&(1<<apfInterface)) != 0) vrb.print("  interface"); else vrb.print("  class    ");  
			vrb.printf(" %1$s (extLevel=%2$d)\n", cls.name, cls.extensionLevel );
			cls = cls.nextClass;
		}
	}

	public static void printStdClasses() {
		vrb.printf("\n--- standard classes: (#=%1$d)\n", nofStdClasses);
		for (int exl = 0; exl <= maxExtensionLevelStdClasses; exl++) {
			if (extLevelOrdClasses == null) break;
			Class cls = extLevelOrdClasses[exl];
			while (cls != null) {
				cls.print(1);
				cls = cls.nextExtLevelClass;
			}
		}
	}

	public static void printInterfaces() {
		vrb.printf("\n--- interface classes: (#=%1$d)\n", nofInterfaceClasses);
		for (int exl = 1; exl <= maxExtensionLevelInterfaces; exl++) {
			if (extLevelOrdInterfaces == null) break;
			Class cls = extLevelOrdInterfaces[exl];
			while (cls != null) {
				cls.print(1);
				cls = cls.nextExtLevelClass;
			}
		}
	}

	public static void printIntfCallMethods() {
		vrb.printf("\n--- calls of interface methods in standard classes:\n");
		for (int exl = 1; exl <= maxExtensionLevelStdClasses; exl++) {
			if (extLevelOrdClasses == null) break;
			Class cls = extLevelOrdClasses[exl];
			while (cls != null) {
				cls.printIntfCallMethods(1);
				cls = cls.nextExtLevelClass;
			}
		}
		vrb.printf("\n\n");
	}

	public static void printArrays() {
		vrb.printf("\n--- arrays: (#=%1$d)\n", nofArrays);
		Array array = arrayClasses;
		while (array != null) {
			array.print(1);
			array = array.nextArray;
		}
	}

	public static void printConstBlockInterfaces() {
		vrb.printf("\n--- interfaces with constant block\n");
		Class cls = constBlockInterfaces;
		while (cls != null) {
			cls.print(1);
			cls = cls.nextInterface;
		}
	}

	public static void printClassList(String title) {
		vrb.println();
		if (title != null) vrb.println(title);
		printInitSequence();
		printStdClasses();
		printInterfaces();
		printArrays();
		vrb.println("\nend of class list\n");
	}

	public static void printClassListNames() {
		vrb.println("\nclass list\n");
		Item type = RefType.refTypeList;
		while (type != null) {
			vrb.print(type.name); vrb.println();
			type = type.next;
		}
		vrb.println("\nend of class list\n");
	}

	public void printFields(int indentLevel){
		indent(indentLevel);
		vrb.printf("fields: (instFields: #=%1$d, objSize=%2$d B", nofInstFields, objectSize);
		vrb.printf("; clsFields: #=%1$d, clsFieldsSize=%2$d B; constFields: #=%3$d)\n", nofClassFields, classFieldsSize, nofConstFields);
		indent(indentLevel+1);
		vrb.printf("references: (instFields: #=%1$d", nofInstRefs);
		if(nofInstRefs > 0) vrb.printf(", firstRef=(%1$s, offset=%2$d)", firstInstReference.name, firstInstReference.offset);
		vrb.printf("); (clsFields: #=%1$d", nofClassRefs);
		if(nofClassRefs > 0) vrb.printf(", firstRef=(%1$s, offset=%2$d)", firstClassReference.name, firstClassReference.offset); else vrb.print(')');
		vrb.println();
	
		Item item = instFields;
		while(item != null){
			item.println(indentLevel+1);
			item = item.next;
		}
	}

	public void printMethods(int indentLevel){
		indent(indentLevel);
		vrb.printf("methods: clsMeths #=%1$d, instMeths #=%2$d, methTabLength=%3$d", nofClassMethods, nofInstMethods, methTabLength);
		if( methTable != null ){
			vrb.printf( ", extMethTable.length=%1$d\n", methTable.length );
			int n = 0;
			while( n < methTable.length ){
				indent(indentLevel+1);
				vrb.printf("[%1$2d] ", n);
				methTable[n].printHeaderX(0);
				vrb.println();
				n++;
			}
		}else{
			vrb.println();
			Item item = methods;
			while(item != null){
				item.println(indentLevel+1);
				item = item.next;
			}
		}
		vrb.println();
	}

	protected static void printConstPools(){
		Item type = RefType.refTypeList;
		while(type != null){
			if(type instanceof Class){
				Class cls = (Class)type;
				if( cls.constPool != null) cls.printReducedConstPool(cls.name.toString());
			}
			type = type.next;
		}
	}

	private void printRedCpEntry(int redCpInd){
		Item item = constPool[redCpInd];
		item.printShort(0);
		vrb.printf(" <%1$s>", item.getClass().getName());
	}

	private void printRedCpEntryCond(int cpIndex, int tag){
//		if(tag < 0){// has entry in the reduced const pool
			if(constPool != null && tag != cptUtf8 &&  tag != cptNameAndType){
				int redCpInd = cpIndices[cpIndex];
				indent(1);
				vrb.printf(" \t=> [%1$3d] ", redCpInd);
				printRedCpEntry(redCpInd);
			}
//		}
	}

	private void printCpEntry(int cpIndex, int tag, int indentLevel){
		indent(indentLevel+1);
		int cpIntValue = cpIndices[cpIndex];
		int cpIndH = cpIntValue >>> 16;
		int cpIndL = cpIntValue & 0xFFFF;
		vrb.printf("[%1$4d]%2$4d ", cpIndex, tag); Dbg.printCpTagIdent(tag, 12);
		vrb.printf(" <%1$5d,%2$5d>", cpIndH, cpIndL);
		switch(tag){
		case cptExtSlot:
			vrb.printf("=0x%1$x", cpIntValue);
			break;
		case cptUtf8:
			vrb.printf("=%1$s", cpStrings[cpIndex]);
			printRedCpEntryCond(cpIndex, tag);
			break;
		case cptInteger:
			vrb.printf("=0x%1$x", cpIntValue);
			printRedCpEntryCond(cpIndex, tag);
			break;
		case cptFloat:
			vrb.printf("=0x%1$x", cpIntValue);
			printRedCpEntryCond(cpIndex, tag);
			break; // float pattern
		case cptLong: case cptDouble:
			printRedCpEntryCond(cpIndex, tag);
			break;
		case cptClass:
			printRedCpEntryCond(cpIndex, tag);
			break;
		case cptString:
			printRedCpEntryCond(cpIndex, tag);
			break; // string index
		case cptFieldRef: case cptMethRef: case cptIntfMethRef: case cptNameAndType:// (class index) <<16, nameAndType index
			printRedCpEntryCond(cpIndex, tag);
			break;
		default:
			assert false;
		}
		vrb.println();
	}

	public void printImports(int indentLevel){
		indent(indentLevel); 	
		if (imports != null) {
			vrb.printf("imports: %1$d\n", nofImports);
			for (int imp = 0; imp < imports.length; imp++){
				indent(indentLevel+1); vrb.println(imports[imp].name);
			}
		} else vrb.println("no imports");
	}

	public void printInterface(Class interf) {
		String icall, iTest;
		if ((interf.accAndPropFlags&(1<<dpfInterfCall)) == 0 ) icall = ""; else icall = "iCall, ";
		if ((interf.accAndPropFlags&(1<<dpfTypeTest)) == 0 ) iTest = ""; else iTest = "iTest, ";
		vrb.printf("%1$s{%2$s%3$sid=%4$d, chkId=%5$d, extLevel=%6$d, methodTabLength=%7$d}", interf.name, icall, iTest, interf.index, interf.chkId, interf.extensionLevel, interf.methTabLength);
	}

	public void printInterfaces(int indentLevel) {
		if (interfaces != null) {
			indent(indentLevel++);
			vrb.println("implements:");
			indent(indentLevel); printInterface(interfaces[0]); vrb.println(); 
			int nofIntf = interfaces.length;
			for (int n = 1; n < nofIntf; n++){
				indent(indentLevel); printInterface(interfaces[n]); vrb.println(); 
			}
		}
	}

	public void printIntfCallMethods(int indentLevel) {
		if (intfCallList != null) {
			vrb.println("class " + name + " has methods which are called by invokeinterface in");
			for( int n = 0; n < intfCallList.length; n++){
				Class interf = intfCallList.getInterfaceAt(n);
				vrb.printf("\tname=%1$s, id=%2$d, #meths=%3$d\n", interf.name, interf.index, interf.methTabLength);
				Item m = interf.methods;
				while (m != null) {
					if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {((Method)m).printName(3); vrb.println();}
					m = m.next;
				}
			}
		}
	}

	void printOrigConstPool(String title) {
		vrb.println(title);
		for(int pe = 1; pe < constPoolCnt; pe++) {
			printCpEntry(pe, cpTags[pe], 1);
		}		
	}

	private void printReducedConstPool(String title) {
		vrb.printf("\nreduced constant pool: %1$s (nOfImports=%2$d)\n", title, nofImports);
		for(int pe = 0; pe < constPool.length; pe++){
			vrb.printf("  [%1$3d] ", pe);
			printRedCpEntry(pe);
			vrb.println();
		}		
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("class %1$s, flags=", name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'C');
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(accAndPropFlags, 'C');  vrb.printf(" class %1$s", name);
		if (type != null)  vrb.printf(" extends %1$s", type.name);
		vrb.print(" //dFlags");  Dbg.printDeepAccAndPropertyFlags(accAndPropFlags, 'C'); vrb.println();
		indent(indentLevel+1);
		
		vrb.printf("source file: %1$s, extLevel=%2$d(max=)", srcFileName, this.extensionLevel);
		if ((this.accAndPropFlags&(1<<apfInterface)) != 0) {
			String icall, iTest;
			if ((this.accAndPropFlags&(1<<dpfInterfCall)) == 0 ) icall = ""; else icall = ", iCall";
			if ((this.accAndPropFlags&(1<<dpfTypeTest)) == 0 ) iTest = ""; else iTest = ", iTest";
			vrb.printf("%1$d, id=%2$d, chkId=%3$d%4$s%5$s, methodTabLength=%6$d\n", maxExtensionLevelInterfaces, index, chkId, icall, iTest, methTabLength);  
		} else
			vrb.printf("%1$d, id=%2$d\n", maxExtensionLevelStdClasses, index);  
		
		printImports(indentLevel+1);
		printInterfaces(indentLevel+1);
//		printFields(indentLevel+1);
		printMethods(indentLevel+1);
	}
	
	public void printConstantBlock() {
		constantBlock.printList();
//		vrb.println("----------");
//		constantBlock.printListRaw();
	}


}
