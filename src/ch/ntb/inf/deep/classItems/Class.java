package ch.ntb.inf.deep.classItems;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.host.ClassFileAdmin;
import ch.ntb.inf.deep.config.SystemClass;
import ch.ntb.inf.deep.config.SystemMethod;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class Class extends Type implements ICclassFileConsts, ICdescAndTypeConsts, ICjvmInstructionOpcs {
	//--- static fields
	private static final int fieldListArrayLength = 9;
	private static final Item[] instFieldLists = new Item[fieldListArrayLength]; // instance field lists, used by method readFields
	private static final Item[] classFieldLists = new Item[fieldListArrayLength]; // class field lists, used by method readFields
	private static final Item[] constFieldLists = new Item[fieldListArrayLength]; //  class constants lists, used by method readFields
//	private static Item constFields; // constant field list (unsorted)
	static{
		assert fieldSizeUnit >= 4 && (fieldSizeUnit & (fieldSizeUnit-1)) == 0;
	}
	
	//--- instance fields
	public Item[] constPool; // reduced constant pool
	
	public Item methods; // list with all methods
	public int nofMethods; // number of methods
	public int nofInstMethods, nofClassMethods; // number of methods
	public int methTabLength; // nOfInstanceMethodsInCD
	
	public Item instFields, firstInstReference; // (fields), chained to list with all fields
	public Item classFields, firstClassReference; // list of class fields
	public Item constFields; // list of const fields (static final <primitive type>)
	public int nofInstFields, nofClassFields, nofConstFields; // number of fields
	public int nofInstRefs, nofClassRefs;
		
	public Class[] interfaces;
	public int nofInterfaces; // number of interfaces

	Class[] imports;
	public int nofImports; // number of imports (without arrays)

	public int nofBaseClasses; // number of base classes
	
	public int[] constantBlock; // the constant block for this class
	public int constantBlockSize; // size of the constant block
	public int constantPoolSize; // size of this pool on the target (in byte)
	public int stringPoolSize; // size of this pool on the target (in byte)
	public int classDescriptorSize; // size of the class descriptor on the target (in byte)
	public int classDescriptorOffset;

	public int machineCodeSize; // size of the machine code on the target (in byte)
	
	public Segment codeSegment, varSegment, constSegment; // references to the memory segments for this class
	public int codeOffset, varOffset, constOffset; // the offset of the code/class fields/constant block in the dedicated segment

	HString srcFileName; // file ident + ".java", e.g.  String.java  for java/lang/String.java
	
	//--- debug fields
	int magic, version;

	//--- instance methods

	public Class(HString registeredCpClassName){
		super(registeredCpClassName, null);
		name = registeredCpClassName;
		category = tcRef;
		sizeInBits = 32;
	}

	/**
	 * @param newByteCode  one of {new, newarray, anewarray, multianewarray}
	 * @return  the reference to the new-method, null for invalid byteCodes
	 */
	public static Method getNewMemoryMethod(int newByteCode){
		int methIndex;
		switch(newByteCode){
		case bCnew: methIndex = 0; break;
		case bCnewarray: methIndex = 1; break;
		case bCanewarray: methIndex = 2; break;
		case bCmultianewarray: methIndex = 3; break;
		default:
			return null;
		}
		return (Method)newMethods[methIndex];
	}

	Item getField(HString fieldName){
		Item item = instFields;
		while(item != null && item.name != fieldName) item = item.next;
		if(item == null && type != null) item = ((Class)type).getField(fieldName);
		return item;
	}

	protected Item getMethod(HString name, HString descriptor){
		Item item = null;
		if(methods != null)  item = methods.getMethod(name, descriptor);
		if(item == null && type != null) item = type.getMethod(name, descriptor);
		return item;
	}

	public Method getClassConstructor() {
		if(this.methods != null) return (Method)this.methods.getItemByName("<clinit>");
		return null;
	}

	/**
	 * The field with fieldName is selected and returned.
	 * If the field is not found, an new Field is created and insert, if the field is found it is checked for the correct descriptor.
	 * @param fieldName  a registered string
	 * @param fieldType a registred string
	 * @return the selected field or newly created one
	 */
	DataItem insertCondAndGetField(HString fieldName, Type fieldType){
		//pre: all strings in the const are already registered in the proper hash table.
		Item item = instFields;
		while(item != null && item.name != fieldName) item = item.next;
		DataItem field;
		if(item != null){
			field = (DataItem)item;
			assert field.type == fieldType;
		}else{// create Field and update field list
			field = new DataItem(fieldName, fieldType);
			field.next = instFields;  instFields = field;
		}
		return field;
	}


	/**
	 * The method with methodName is selected and returned.
	 * If the method is not found, an new Method is created and inserted, if the method is found it is checked for the correct descriptor.
	 * @param methName  a registered string
	 * @param methDescriptor a registered string
	 * @return the selected method or newly created one
	 */
	Method insertCondAndGetMethod(HString methName, HString methDescriptor){
		//pre: all strings in the const are already registered in the proper hash table.
		Item item = methods;
		while(item != null && (item.name != methName || ((Method)item).methDescriptor != methDescriptor) ) item = item.next;
		Method method;
		if(item != null){
			method = (Method)item;
		}else{// create Method and update method list
			Type retrunType = getReturnType(methDescriptor);
			method = new Method(methName, retrunType, methDescriptor);
			method.next = methods;  methods = method;
		}
		return method;
	}

	/**
	 * Check ClassInfo entry in const pool and update it accordingly if necessary.
	 * <br>That is: if there is not yet a direct reference to an object of type Class, then such an object is created and registered.
	 * @param cpClassInfoIndex index of ClassInfo entry
	 * @return object of this type Class
	 */
	Item updateAndGetCpClassEntry(int cpClassInfoIndex){
		//pre: all strings in the const pool are already registered in the proper hash table.
		Item cls = cpItems[cpClassInfoIndex];
		if(cls == null){
			HString registeredClassName = cpStrings[cpIndices[cpClassInfoIndex]];
			if(registeredClassName.charAt(0) == '[') cls = getTypeByNameAndUpdate(tcArray, registeredClassName, wktObject);
			else cls = getTypeByNameAndUpdate(tcRef, registeredClassName, null);
			cpItems[cpClassInfoIndex] = cls;
		}
		return cls;
	}

	private Item getFieldOrStub(HString fieldName, Type fieldType){
		Item field = getField(fieldName);
		if( field == null ) field = new ItemStub(this, fieldName, fieldType);
		return field;
	}
		
	private Item getFieldOrStub(int cpFieldInfoIndex){
		//pre: all strings in the const are already registered in the proper hash table.
		Item field = cpItems[cpFieldInfoIndex];
		if(field == null){
			int csx = cpIndices[cpFieldInfoIndex]; // get class and signature indices
			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
			int sx = cpIndices[csx & 0xFFFF];
			HString fieldName = cpStrings[sx>>>16];
			HString fieldDesc  = cpStrings[sx & 0xFFFF];
			Type fieldType = getTypeByDescriptor(fieldDesc);
			
			field = cls.getFieldOrStub(fieldName, fieldType);
		}
		return field;
	}

	private Item getMethodOrStub(HString name, HString descriptor){
		Item meth = getMethod(name, descriptor);
		if( meth == null ) meth = new ItemStub(this, name, descriptor);
		return meth;
	}

	private Item getMethodOrStub(int cpMethInfoIndex){
		//pre: all strings in the const are already registered in the proper hash table.
		Item method = null;
		if(cpItems[cpMethInfoIndex] == null){
			int csx = cpIndices[cpMethInfoIndex]; // get class and signature indices
			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
			int sx = cpIndices[csx & 0xFFFF];

			HString methName = cpStrings[sx>>>16];
			HString methDesc  = cpStrings[sx & 0xFFFF];

			method = cls.getMethodOrStub( methName, methDesc);
		}
		return method;
	}

	private void loadConstPool(DataInputStream clfInStrm) throws IOException{
		if(verbose) vrb.println(">loadConstPool:");
		
		magic = clfInStrm.readInt();
		if(magic != 0xcafeBabe) throw new IOException("illegal class file");
		if(verbose) vrb.printf("magic=0x%1$4x\n", magic);

		version = clfInStrm.readInt();
		if(verbose) vrb.printf("version=%1$d.%2$d\n", (version&0xFFFF), (version>>>16) );

		constPoolCnt = clfInStrm.readUnsignedShort();
		if(verbose) vrb.printf("constPoolCnt=%1$d\n", constPoolCnt );
		allocatePoolArray(constPoolCnt);
		for(int pEntry = 1; pEntry < constPoolCnt; pEntry++){
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
				break; // class index
			case cptString: cpIndices[pEntry] = clfInStrm.readUnsignedShort(); break; // string index
			
			case cptFieldRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptMethRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptIntfMethRef: cpIndices[pEntry] = clfInStrm.readInt(); break; // (class index) <<16, nameAndType index
			case cptNameAndType: cpIndices[pEntry] = clfInStrm.readInt(); break;// (name index) <<16, descriptor index
			default:
				throw new IOException("illegal tag in const pool");
			}
		}
		if(verbose) vrb.println("<loadConstPool");
	}

	private void updateConstPool() throws IOException{
		if(verbose) vrb.println(">updateConstPool:");
		//pre: all strings in the const are already registered in the proper hash table.
		int nofItems = 0;
		int pEntry;
		for(pEntry = 1; pEntry < constPoolCnt; pEntry++){// constPoolCnt
			int tag = cpTags[pEntry];
			switch(tag){
			 case cptExtSlot: case cptUtf8: // cptExtSlot, Utf8 string
				 break;
			case cptInteger: // integer literal
				cpItems[pEntry] = new StdConstant(hsNumber, wellKnownTypes[txInt], cpIndices[pEntry], 0);
				nofItems++;
				break;
			case cptFloat:  // float literal
				cpItems[pEntry] = new StdConstant(hsNumber, wellKnownTypes[txFloat], cpIndices[pEntry], 0);
				nofItems++;
				break; // float pattern
			case cptLong:
				cpItems[pEntry] = new StdConstant(hsNumber, wellKnownTypes[txLong], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptDouble:
				cpItems[pEntry] = new StdConstant(hsNumber, wellKnownTypes[txDouble], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptClass: // class index
				Item item = updateAndGetCpClassEntry(pEntry);
				cpItems[pEntry] = item;
				if( item instanceof Array) nofImports--; // arrays get not included in imports
				nofItems++;
				break;
			case cptString: 
				cpItems[pEntry] = new StringLiteral(hsString, cpStrings[cpIndices[pEntry]]);
				nofItems++;
				break;
			case cptFieldRef:
				cpItems[pEntry] = getFieldOrStub(pEntry);
				nofItems++;
				break;
			case cptMethRef:
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
		while(--pEntry > 0){
			Item item = cpItems[pEntry];
			if(item != null){
				constPool[--nofItems] = item;
//				cpItems[pEntry] = null;
				cpIndices[pEntry] = nofItems;
			}else{
				cpIndices[pEntry] = 0;
			}
		}
		assert nofItems == 0;
		if(verbose) vrb.println("<updateConstPool");
	}

	private void readInterfaces(DataInputStream clfInStrm) throws IOException{
		int cnt = clfInStrm.readUnsignedShort();
		nofInterfaces = cnt;
		if(cnt > 0){
			interfaces = new Class[cnt];
			for (int intf = 0; intf < cnt; intf++){
				int intfInx = clfInStrm.readUnsignedShort();
				interfaces[intf] = (Class)cpItems[intfInx];
			}
		}
	}

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
			if(typeNickName == tdLong) fieldListIndex = flxLong; else  fieldListIndex = flxDouble;
			break;
		case 4: // tcRef, tcArray
			if(typeCategory != tcPrimitive) fieldListIndex = flxRef;
			else if(typeNickName == tdFloat) fieldListIndex = flxFloat; 
			else  fieldListIndex = flxInt;
			break;
		case 2: // tcPrimitive: long, double
			fieldListIndex = flxShortChar;
			break;
		case 1: // tcPrimitive: long, double
			fieldListIndex = flxByte;
			break;
		case 0: // tcPrimitive: long, double
			fieldListIndex = flxBits;
			break;
		default:
			assert false;
		}
		int flags = item.accAndPropFlags;
		if( (flags & (1<<apfStatic)) == 0){// instance field
			item.next = instFieldLists[fieldListIndex];  instFieldLists[fieldListIndex] = item;
			nofInstFields++;
			if(fieldListIndex == flxRef) nofInstRefs++;
		}else if( (flags & (1<<dpfConst)) == 0){// class field
			item.next = classFieldLists[fieldListIndex];  classFieldLists[fieldListIndex] = item;			
			nofClassFields++;
			if(fieldListIndex == flxRef) nofClassRefs++;
		}else{// constant (of primitive type
			item.next = constFieldLists[fieldListIndex];  constFieldLists[fieldListIndex] = item;			
			nofConstFields++;
		}
		if(verbose) vrb.println("<addItemToFieldList");
	}

	private Item getFieldListAndUpdate(Item[] fieldLists){
		if(verbose) vrb.printf(">getFieldListAndUpdate: class: %1$s\n", name);

		Item head = null, tail = null;

		for(int category = fieldLists.length-1; category >= 0; category--){
			Item list = fieldLists[category];
			fieldLists[category] = null;
			Item item = list;
			while(item != null){
				list = item.next;
				item.next = null;
				//-- append
				if(tail == null)  head = item; else tail.next = item;
				tail = item;
				item = list;
			}
		}

		if(verbose) vrb.println("<getFieldListAndUpdate");
		return head;
	}

	private void readFields(DataInputStream clfInStrm) throws IOException{
		final boolean verbose = false;
		
		if(verbose) vrb.println(">readFields: "+name);
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

			DataItem field = null;

			if(verbose) vrb.printf(" readFields: cls=%1$s, desc=%2$s\n", name, descriptor);
			//--- read field attributes {ConstantValue, Deprecated, Synthetic}
			int attrCnt = clfInStrm.readUnsignedShort();
			while(attrCnt-- > 0){
				index = clfInStrm.readUnsignedShort();
				int attr = selectAttribute(index);
				int attrLength = clfInStrm.readInt();
				switch(attr){
				case atxConstantValue:
					index = clfInStrm.readUnsignedShort();
					int rcpIndex = cpIndices[index];
					Item cpField = constPool[rcpIndex];

					Type type = getTypeByDescriptor(descriptor);
					if(verbose) vrb.printf("   readFields: field.desc=%1$s, const: name=%2$s, type=%3$s\n", descriptor, cpField.name, cpField.type.name);

					assert cpField instanceof Constant;
					field = new NamedConst(name, type, (Constant)cpField);
					if( (flags & (1<<apfStatic) ) != 0) flags |= (1<<dpfConst);
					break;
				case atxDeprecated:
					flags |= (1<<dpfDeprecated);
					break;
				case atxSynthetic:
					flags |= (1<<dpfSynthetic);
					break;
				default:
					skipAttributeAndLogCond(clfInStrm, attrLength, index);
				}
			}

			if(field == null){
				Type type = getTypeByDescriptor(descriptor);
				field = new DataItem(name, type);
			}
			
			flags |= field.accAndPropFlags;
			field.accAndPropFlags = flags;

			addItemToFieldList(field);
			((ClassMember)field).owner = this;
			
			fieldCnt--;
		}
		
		assert instFields == null;
		constFields = getFieldListAndUpdate(constFieldLists);

		firstClassReference = classFieldLists[flxRef];
		classFields = getFieldListAndUpdate(classFieldLists);

		firstInstReference = instFieldLists[flxRef];
		instFields = getFieldListAndUpdate(instFieldLists);

		//--- chain the field lists
		Item tail = getTailItem(classFields);
		classFields = appendItem(classFields, tail, constFields);
		tail = getTailItem(instFields);
		instFields = appendItem(instFields, tail, classFields);

		if(verbose) vrb.println("<readFields");
	}

	private void readMethods(DataInputStream clfInStrm, int userReqAttributes) throws IOException{
		int methodCnt = clfInStrm.readUnsignedShort();
		nofMethods = methodCnt;
		assert methods == null;
		
		int nofClsMeths = 0,  nofInstMeths = 0;
		Item clsMethHead = null, clsMethTail = null;
		Item instMethHead = null, instMethTail = null;

		while(methodCnt-- > 0){
			int flags;
			HString name, descriptor;

			flags = clfInStrm.readUnsignedShort(); //read access and property flags
			//--- read name and descriptor
			int index = clfInStrm.readUnsignedShort();
			name = cpStrings[index];
			index = clfInStrm.readUnsignedShort();
			descriptor = cpStrings[index];
			if(descriptor == hsCommandDescriptor && name != hsClassConstrName && (flags & (1<<apfStatic)) != 0){
				flags |= (1<<dpfCommand);
			}
			Type returnType = getReturnType(descriptor);
			Method method = new Method(name, returnType, descriptor);
			method.owner = this;
			
			//--- read method attributes {Code, Deprecated, Synthetic}
			int attrCnt = clfInStrm.readUnsignedShort();
			while(attrCnt-- > 0){
				int cpInxOfAttr = clfInStrm.readUnsignedShort();
				int attr = selectAttribute( cpInxOfAttr );
				int attrLength = clfInStrm.readInt();
				switch(attr){
				case atxCode:
					if( (userReqAttributes&(1<<atxCode)) == 0){
						skipAttributeAndLogCond(clfInStrm, attrLength, 0); // skip without logging
						break;
					}
					method.maxStackSlots = clfInStrm.readUnsignedShort();
					method.maxLocals = clfInStrm.readUnsignedShort();
					int codeLen = clfInStrm.readInt();
					method.code = new byte[codeLen];
					clfInStrm.read(method.code);
					
					//--- read exception table
					int excTabLen = clfInStrm.readUnsignedShort();
					if(excTabLen > 0){
						method.exceptionTab = new ExceptionTabEntry[excTabLen];
						for(int exc = 0; exc < excTabLen; exc++){
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
					while(codAttrCnt-- > 0){
						int codAttrIndex = clfInStrm.readUnsignedShort();
						int codeAttr = selectAttribute( codAttrIndex );
						int codAttrLen = clfInStrm.readInt();
						if(codeAttr == atxLocalVariableTable){
							if( (userReqAttributes&(1<<atxLocalVariableTable)) == 0){
								skipAttributeAndLogCond(clfInStrm, codAttrLen, 0); // skip without logging
							}else{
								int locVarTabLength = clfInStrm.readUnsignedShort();
								if(locVarTabLength > 0){
									method.localVars = new LocalVar[method.maxLocals];
									while(locVarTabLength-- > 0){
										LocalVar locVar = new LocalVar();
										locVar.startPc = clfInStrm.readUnsignedShort();
										locVar.length = clfInStrm.readUnsignedShort();
										locVar.name = cpStrings[ clfInStrm.readUnsignedShort() ];
										locVar.type = getTypeByDescriptor( cpStrings[ clfInStrm.readUnsignedShort() ] );
										locVar.index = clfInStrm.readUnsignedShort();
										method.insertLocalVar(locVar);
									}
								}
							}
						}else if(codeAttr == atxLineNumberTable){
							if( (userReqAttributes&(1<<atxLineNumberTable)) == 0){
								skipAttributeAndLogCond(clfInStrm, codAttrLen, 0); // skip without logging
							}else{
								int lineNrTabLength = clfInStrm.readUnsignedShort();
								int[] lineNrTab = new int[lineNrTabLength];
								method.lineNrTab = lineNrTab;
								for(int lnp = 0; lnp < lineNrTabLength; lnp++) lineNrTab[lnp] = clfInStrm.readInt();
							}
						}else{// skip
							skipAttributeAndLogCond(clfInStrm, codAttrLen, codAttrIndex);
						}
					}
					break;
				case atxDeprecated:
					flags |= (1<<dpfDeprecated);
					break;
				case atxSynthetic:
					flags |= (1<<dpfSynthetic);
					break;
				default:
					skipAttributeAndLogCond(clfInStrm, attrLength, index);
				}
			}

			flags |= method.accAndPropFlags;
			method.accAndPropFlags = flags;
			
			//--- append method
			if( (flags & (1<<apfStatic)) != 0 ){ // class method
				nofClsMeths++;
				if(clsMethHead == null)  clsMethHead = method;  else  clsMethTail.next = method;
				clsMethTail = method;
			}else{// instance method
				nofInstMeths++;
				if(instMethHead == null)  instMethHead = method;  else  instMethTail.next = method;
				instMethTail = method;
			}
		}
		assert methods == null;
		methods = appendItem(clsMethHead, clsMethTail, instMethHead);	
		nofClassMethods = nofClsMeths;
		nofInstMethods = nofInstMeths;
	}

	private void readClassAttributes(DataInputStream clfInStrm, int userReqAttributes) throws IOException{
		int attrCnt = clfInStrm.readUnsignedShort();
		while(attrCnt-- > 0){
			int index = clfInStrm.readUnsignedShort();
			int attr = selectAttribute(index);
			int attrLength = clfInStrm.readInt();
			switch(attr){
			case atxSourceFile:
				index = clfInStrm.readUnsignedShort();
				srcFileName = cpStrings[index];
				break;
			case atxDeprecated:
				accAndPropFlags |= (1<<dpfDeprecated);
				break;
			case atxInnerClasses: // 4.7.5, p125
				if( (userReqAttributes&(1<<atxInnerClasses)) == 0) skipAttributeAndLogCond(clfInStrm, attrLength, index);
				else{
					// TODO Auto-generated method stub
					assert false: "TODO";
				}
				break;
			default:
				skipAttributeAndLogCond(clfInStrm, attrLength, index);
			}
		}
	}

	private void analyseByteCode(){
		if(verbose) vrb.println(">analyseByteCode:");
		Item item = methods;
		while(item != null){
			Method meth = (Method)item;
			if(verbose){
				vrb.print("\nmethod: "); meth.printHeader(); vrb.print(" (owner="); meth.owner.printName(); vrb.println(')');
			}
			ByteCodePreProc.analyseCodeAndFixCpRefs(cpIndices, constPool, meth.code);
			item = item.next;
		}
		if(verbose) vrb.println("<analyseByteCode");
	}

	private void loadClass(int userReqAttributes) throws IOException{
		if(verbose) vrb.println(">loadClass:");
		if( (accAndPropFlags & ((1<<dpfClassLoaded)|(1<<dpfSynthetic)) ) == 0 ){// if not yet loaded
			try{
				File classFile = ClassFileAdmin.getClassFile(name);
				log.println("opening class file of class: "+name );

				if(classFile == null) throw new FileNotFoundException();
				InputStream inStrm = new FileInputStream(classFile); // new FileInputStream
				DataInputStream clfInStrm = new DataInputStream(inStrm); // new DataInputStream

				loadConstPool(clfInStrm);
				accAndPropFlags |= clfInStrm.readUnsignedShort();
				
				if(verbose){
					printOrigConstPool("state: 0");
//					stab.print("String Table 0");
					printClassList("state: 0");
					print(0);
				}
	
				updateConstPool();
	
				if(verbose){
					printOrigConstPool("state: 1");
					print(0);
				}
	
				clfInStrm.readUnsignedShort(); // read this class index
	
				int thisSupClassCpInx = clfInStrm.readUnsignedShort();
				if(verbose) vrb.println("thisSupClassCpInx="+thisSupClassCpInx);
				if(thisSupClassCpInx > 0){
					int constPoolInx = cpIndices[thisSupClassCpInx];
					type = (Class)constPool[constPoolInx];
					if(verbose){
						vrb.print("superClassName="); type.printName();
					}
				}
	
				readInterfaces(clfInStrm);
				readFields(clfInStrm);
				readMethods(clfInStrm, userReqAttributes);
				readClassAttributes(clfInStrm, userReqAttributes);
	
//				if(verbose){
//					vrb.println("\nstate: 2");
//					printOrigConstPool("state: 2");
//					stab.print("String Table in state: 2");
//					printClassList("state: 2");
//					print(0);
//				}
				
				if( (accAndPropFlags & (1<<apfEnum)) == 0){// if class or interface but not an enum
					analyseByteCode();
					this.accAndPropFlags |= (1<<dpfClassLoaded);
				}

				if(verbose){
					vrb.println("\n>dump of class: "+name);
					vrb.println("\nstate: 3");
//					stab.print("String Table in state: 3");
					printOrigConstPool("state: 3");
					printReducedConstPool("state: 3");
//					printClassList("state: 3");
					print(0);
					vrb.println("\n<end of dump: "+name);
				}
//				printReducedConstPool("reduced cp, state: 3");
				
				clfInStrm.close();
			}catch (FileNotFoundException fnfE){
				errRep.error("class file not found"); errRep.println();
				fnfE.getCause();
			}
		}
		//--- load referenced classes
		if( (accAndPropFlags & (1<<dpfClassLoaded)) != 0){
			nofImports--;
			imports = new Class[nofImports];
			int impIndex = nofImports;
			for(int cpx = constPool.length-1; cpx >= 0; cpx--){
				Item item = constPool[cpx];
				if(item instanceof Class){
					Class refClass = (Class) item;
					if(impIndex > 0) imports[--impIndex] = refClass; // without first entry (this class)
					if( (refClass.accAndPropFlags & ((1<<dpfClassLoaded)|(1<<dpfSynthetic)) ) == 0) {
						refClass.loadClass(userReqAttributes);
					}
				}
			}
		}
		if(verbose) vrb.println("<loadClass");
	}

	public static void startLoading(int nofRootClasses){
		if(verbose) vrb.println(">startLoading:");
		
		rootClasses = new Class[nofRootClasses];
		nofRootClasses = 0;
		
		prevCpLenth = 0;  constPoolCnt = 0;

		if(StringTable.getInstance() != null) StringTable.resetTable();
		else{
			StringTable.createSingleton(1000, "??");
			stab = StringTable.getInstance();
			HString.setStringTable(stab);
		}
		registerWellKnownNames();
		Class cls = new Class(hsClassConstrName); // currently insert stub, later on root class referencing all classes with a calss constructor
		classList = cls; classInitListTail = cls; classListTail = cls;
		nofClasses = 0;
		
		setUpBaseTypeTable();
		setClassFileAttributeTable(stab);
		
		if(verbose) vrb.println("<startLoading");
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

	public static void loadRootClass(String rootClassName, int userReqAttributes) throws IOException{
		if(verbose) vrb.println(">loadRootClass: "+rootClassName);
		
		HString hRootClassName = stab.insertCondAndGetEntry(rootClassName);
		Class root = new Class(hRootClassName);
		appendRootClass(root);
		root.accAndPropFlags |= (1<<dpfRootClass);
		assert root.next == null;
		root.loadClass(userReqAttributes);
		root.comleteLoadingOfRootClass();
		
		if(verbose) vrb.println("<loadRootClass");
	}

	private static int unitedSysMethodFlags(SystemClass systemClass){
		SystemMethod systemMeth = systemClass.methods;
		int unitedFlags =  0;
		if(systemMeth != null)  unitedFlags =  1<<dpfSysPrimitive;
		while(systemMeth != null){
			unitedFlags |= (systemMeth.attributes & dpfSetSysMethProperties);
			systemMeth = systemMeth.next;
		}
		return unitedFlags;
	}

	private static void loadSystemClass(SystemClass systemClass, int userReqAttributes) throws IOException{
		final boolean verbose = false;

		String systemClassName = systemClass.name;
		int systemClassAttributes = systemClass.attributes | unitedSysMethodFlags(systemClass);

		if(verbose) vrb.println(">loadSystemClass: "+systemClassName);
		if(verbose) vrb.printf("  sysClsAttributes1=0x%1$x\n", systemClassAttributes);

		HString hSysClassName = stab.insertCondAndGetEntry(systemClassName);
		Class cls = (Class)getClassByName(hSysClassName);
		if(cls == null){
			cls = new Class(hSysClassName);
			appendClass(cls);
		}
		cls.loadClass(userReqAttributes);
		cls.accAndPropFlags |= systemClassAttributes & dpfSetSysClassProperties;
		cls.comleteLoadingOfRootClass();

		if( (systemClassAttributes & (1<<dpfNew)) != 0 ){// set up new memory method table
			SystemMethod systemMeth = systemClass.methods;
			while(systemMeth != null){
				Item method = cls.methods.getItemByName(systemMeth.name);
				if(method == null){
					errRep.error("method "+systemMeth.name +" in system class "+systemClass.name + " not found");
				}else{
					if(verbose)vrb.printf("lsc: method=%1$s, attr=0x%2$x\n", (cls.name + "." + method.name), systemMeth.attributes);
					int methIndex  = (systemMeth.attributes-1)&0xFF;
					if( methIndex >= nofNewMethods ){
						errRep.error("method id of"+systemMeth.name +" in system class "+systemClass.name + " out of range");
					}else{
						if(verbose) vrb.println(" ldSysCls: newMethInx="+methIndex);
						systemClassAttributes |= method.accAndPropFlags & dpfSetSysMethProperties;
						newMethods[methIndex] = method;
						if(verbose)vrb.printf("lsc: newMethods[%1$d]: %2$s\n", methIndex, method.name);
					}
				}
				systemMeth = systemMeth.next;
			}
		}

		//--- update method attributes (with system method attributes)
		SystemMethod systemMeth = systemClass.methods;
		Item method = null;
		while(systemMeth != null){
			method = cls.methods.getItemByName(systemMeth.name);
			if(method != null){
				method.offset = systemMeth.offset;
				int sysMethAttr = systemMeth.attributes & (dpfSetSysMethProperties | sysMethCodeMask);
				method.accAndPropFlags =  (method.accAndPropFlags & ~(dpfSetSysMethProperties | sysMethCodeMask) ) |(1<<dpfSysPrimitive) | sysMethAttr;
				if( (sysMethAttr & (1<<dpfSynthetic)) != 0) ((Method)method).clearCodeAndAssociatedFields();
			}
			systemMeth = systemMeth.next;
		}

		if(verbose) vrb.println("<loadSystemClass");
	}

	private static void loadSystemClasses(SystemClass sysClasses, int userReqAttributes) throws IOException{
		while(sysClasses != null){
			loadSystemClass(sysClasses, userReqAttributes); 
			sysClasses = sysClasses.next;
		}
	}

	private static void repalceConstPoolStubs(){
//		final boolean verbose = true;
		if(verbose) vrb.println(">repalceConstPoolStubs:");
		Item type = classList;
		while(type != null){
			if(type instanceof Class){
				Class cls = (Class)type;
				if( cls.constPool != null) {
					Item[] cp = cls.constPool;
					for(int cpx = cp.length-1; cpx >= 0; cpx--) 	cp[cpx] = cp[cpx].getReplacedStub();
				}
			}
			type = type.next;
		}
		if(verbose) vrb.println("<repalceConstPoolStubs");
	}

	protected void fixUpInstanceFields(){
		Item item = instFields;
		Item clsFields = classFields;
		objectSize = (objectSize + fieldSizeUnit-1) & -fieldSizeUnit;
		while(item != clsFields){
			item.offset = objectSize;
			objectSize +=  item.type.getTypeSize();
			item = item.next;
		}
	}

	protected void fixUpClassFields(){
		Item item = classFields;
		Item cFields = constFields;
		classFieldsSize = 0;
		while(item != cFields){
			item.offset = classFieldsSize;
			classFieldsSize +=  item.type.getTypeSize();
			item = item.next;
		}
	}

	protected void fixUpMethods(){
		Item meth = methods;
		if( type == null){//  java/lang/Object
			methTabLength = 0;
			while(meth != null){
				meth.index = -1;
				if( (meth.accAndPropFlags & (1<<apfStatic)) == 0) meth.index = methTabLength++;
				meth = meth.next;
			}
		}else{// any other class ( not java/lang/Object)
			Class baseCls = (Class)type;
			methTabLength = baseCls.methTabLength;
			while(meth != null){
				meth.index = -1;
				if( (meth.accAndPropFlags & (1<<apfStatic)) == 0){// instance method
					Item baseMeth = baseCls.getMethod(meth.name, ((Method)meth).methDescriptor);
					if(baseMeth == null) meth.index = methTabLength++;
					else meth.index = baseMeth.index;
				}
				meth = meth.next;
			}
		}
	}

	protected void selectAndMoveInitClasses(){
		if( (accAndPropFlags & 1<<dpfClassMark) == 0 ){
			accAndPropFlags |= 1<<dpfClassMark;

			if(type == null){
				objectSize = 0; nofBaseClasses = 0;
			}else{
				type.selectAndMoveInitClasses();
				Class baseCls = (Class)type;
				objectSize = baseCls.objectSize; nofBaseClasses = baseCls.nofBaseClasses + 1;
			}
			fixUpInstanceFields();
			fixUpClassFields();
			fixUpMethods();

			if(imports != null){
				for(int index = imports.length-1; index >= 0; index--)  imports[index].selectAndMoveInitClasses();
			}

			ClassMember clsInit = null;
			if(methods != null){
				clsInit = (ClassMember)methods.getItemByName(hsClassConstrName);
				if(clsInit != null){
					moveThisClassToInitList();
//					vrb.printf(" <<#%1$d, meth: %2$s, owner: %3$s>\n", nofInitClasses, clsInit.name, clsInit.owner.name);
				}
			}
		}
	}

	private void comleteLoadingOfRootClass(){
		repalceConstPoolStubs();
//		fixUpObjectSize();
		selectAndMoveInitClasses();
	}

	public static void buildSystem(String[] rootClassNames, String[] parentDirsOfClassFiles, SystemClass sysClasses, int userReqAttributes) throws IOException{
		errRep.nofErrors = 0;
		Type.nofRootClasses = 0;
		ClassFileAdmin.registerParentDirs(parentDirsOfClassFiles);
		
		int nofRootClasses = rootClassNames.length;
		startLoading(nofRootClasses);
	
		Class clsObject = (Class)wellKnownTypes[txObject];
		clsObject.loadClass(userReqAttributes);
		clsObject.comleteLoadingOfRootClass();

		Class clsString = (Class)wellKnownTypes[txString];
		clsString.loadClass(userReqAttributes);
		clsString.comleteLoadingOfRootClass();

		loadSystemClasses(sysClasses, userReqAttributes);
		if(verbose) printClassList("state: sysClasses loaded, class list:");

		for (int rc = 0; rc < nofRootClasses && errRep.nofErrors == 0; rc++){
			String sname = rootClassNames[rc];
			vrb.println("\n\nRootClass["+rc +"] = "+ sname);
			loadRootClass( sname, userReqAttributes);
			if(errRep.nofErrors > 0) break;
		}

		fixUpClassList();
		
//		boolean verbose = true;
//		if(verbose) {
//			printClassList("end state, class list:");
//			printConstPools();
//		}
		
		releaseLoadingResources();
		log.printf("number of errors %1$d\n", errRep.nofErrors);
		log.print("system building ");
		if(errRep.nofErrors == 0) log.println("successfully done"); else log.println("terminated with errors");
	}

	
	//--- debug primitives
	public void printItemCategory(){
		vrb.print("class");
	}

	public static void printClassList(String title){
		vrb.println();
		if(title != null) vrb.println(title);
		vrb.printf("class list: nofClasses=%1$d (with class constr. %2$d), nofArrays=%3$d\n", nofClasses, nofInitClasses, nofArrays);
		indent(1); vrb.printf("classListHead=%1$s", classList.name);
		if(nofInitClasses > 0) vrb.printf(", classInitListTail=%1$s", classInitListTail.name);
		vrb.printf(", classListTail=%1$s\n", classListTail.name);
		if(nofInitClasses > 0) vrb.println("//--- a) classes with class constructor");
		vrb.println();
		Item cls = classList;
		while(cls != null){
			cls.print(1);
			if(cls == classInitListTail) vrb.println("//--- b) classes without class constructor");
			cls = cls.next;
			vrb.println();
		}
		if(verbose) vrb.println("end of class list>");
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
		vrb.printf("methods: (instMeths #=%1$d,  clsMeths #=%2$d)\n", nofInstMethods, nofClassMethods);
		Item item = methods;
		while(item != null){
			item.println(indentLevel+1);
			item = item.next;
		}
	}

	private static void printConstPools(){
		Item type = classList;
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
		if(imports != null){
			indent(indentLevel); 	vrb.printf("imports: %1$d\n", nofImports);
			for(int imp = 0; imp < imports.length; imp++){
				indent(indentLevel+1); vrb.println(imports[imp].name);
			}
		}
	}

	public void printInterfaces(int indentLevel){
		if(interfaces != null){
			indent(indentLevel);
			vrb.print("implements "); vrb.print(interfaces[0].name);
			int nofIntf = interfaces.length;
			for(int inf = 1; inf < nofIntf; inf++){
				vrb.print(", ");  vrb.print(interfaces[inf].name);
			}
		}
	}

	void printOrigConstPool(String title){
		vrb.println("\nconstant pool:"); vrb.println(title);
		for(int pe = 1; pe < constPoolCnt; pe++){
			printCpEntry(pe, cpTags[pe], 1);
		}		
	}

	private void printReducedConstPool(String title){
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
		if(type != null)  vrb.printf(" extends %1$s", type.name);
		vrb.print(" //dFlags");  Dbg.printDeepAccAndPropertyFlags(accAndPropFlags, 'C'); vrb.println();
		indent(indentLevel+1);  vrb.printf("source file: %1$s, extLevel=%2$d\n", srcFileName, this.nofBaseClasses);
		printImports(indentLevel+1);
		printInterfaces(indentLevel+1);
		printFields(indentLevel+1);
		printMethods(indentLevel+1);
	}
	
	public void printConstantBlock() {
		printConstantBlock(0);
	}
	
	public void printConstantBlock(int indentLevel) {
		int i = 0;
		if(this.constantBlock != null) {
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] constBlockSize\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] codeBase\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] codeSize\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] varBase\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] varSize\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] clinitAddr\n"); i++;
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] nofPtrs\n"); i++;
			for(int j = 0; j < this.nofClassRefs; j++) {
				indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] ptr" + j + "\n"); i++;
			}
			for(int j = 0; j < this.classDescriptorSize / 4; j++) {
				indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] CD[" + j + "]\n"); i++;
			}
			for(int j = 0; j < this.stringPoolSize / 4; j++) {
				indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] SP[" + j + "]\n"); i++;
			}
			for(int j = 0; j < this.constantPoolSize / 4; j++) {
				indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] CP[" + j + "]\n"); i++;
			}
			indent(indentLevel); vrb.printf("> %4d", i); vrb.print(" ["); vrb.printf("%8x", this.constantBlock[i]); vrb.print("] fcs\n");
		}
		else {
			indent(indentLevel);
			vrb.print("<null>\n");
		}
	}
}
