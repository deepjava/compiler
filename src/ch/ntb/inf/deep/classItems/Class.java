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
	//--- instance fields
	public Item[] constPool; // reduced constant pool
	
	// TODO: counting of the methods, fields, interfaces and base classes doas'nt work properly 
	
	public Item methods; // list with all methods
	public int nOfMethods; // number of methods
	public int nOfClassMethods, nOfInstanceMethods; // number of methods
	
	public Item fields; // list with all fields
	public int nOfClassFields, nOfInstanceFields; // number of fields
		
	public Class[] interfaces;
	public int nOfInterfaces = 0; // number of interfaces
	
	public int nOfBaseClasses = 0; // number of base classes
	
	public int nOfReferences = 0; // number of class fields which are references
	
	public int[] constantBlock; // the constant block for this class
	public int constantBlockSize; // size of the constant block
	
	public int machineCodeSize = 0; // size of the machine code on the target (in byte)
	public int classFieldsSize = 0; // size of all class/static fields on the target
	public int constantPoolSize = 0; // size of this pool on the target (in byte)
	public int stringPoolSize = 0; // size of this pool on the target (in byte)
	public int classDescriptorSize = 0; // size of the class descriptor on the target (in byte)
	
	// TODO delete:
//	public int[] targetStringPool; // pool with constant stings for the target 
//	public int[] targetClassDescriptor; // class descriptor (for the target)
//	public int[] targetConstantPool; // pool with float and double constants for the target
	
	public Segment codeSegment, varSegment, constSegment; // references to the memory segments for this class
	
	Class[] imports;
	
	HString srcFileName; // file ident + ".java", e.g.  String.java  for java/lang/String.java
	
	//--- debug fields
	int magic, version;

	//--- instance methods

	public Class(HString registeredCpClassName){
		super(registeredCpClassName, null);
		name = registeredCpClassName;
		category = tcRef;
		sizeInBits = 32;
		if(classList == null) classList = this;
		nofClasses++;
	}

	/**
	 * @param newByteCode  one of {new, newarray, anewarray, multianewarray}
	 * @return  the reference to the new-method, null for invalid byteCodes
	 */
	public Method getNewMemoryMethod(int newByteCode){
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

	/**
	 * Select field by name and delete it in the fields list (if found).
	 * @param fieldName
	 * @return  the selected field or null if not found
	 */
	private DataItem getAndExtractField(HString fieldName){
		Item item = fields, pred = null;
		while(item != null && item.name != fieldName) {
			pred = item;
			item = item.next;
		}
		if(item != null){
			if(pred == null) fields = item.next; else  pred.next = item.next;
			item.next = null;
		}
		return (DataItem)item;
	}

	/**
	 * Select method by name and descriptor and delete it in the methods list if found.
	 * @param methName
	 * @param methDescriptor
	 * @return the selected method or null if not found
	 */
	private Method getAndExtractMethod(HString methName, HString methDescriptor){
		Item item = methods, pred = null;
		while(item != null && (item.name != methName || ((Method)item).methDescriptor != methDescriptor) ) {
			pred = item;
			item = item.next;
		}
		if(item != null){
			if(pred == null) methods = item.next; else pred.next = item.next;
			item.next = null;
		}
		return (Method)item;
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
		Item item = fields;
		while(item != null && item.name != fieldName) item = item.next;
		DataItem field;
		if(item != null){
			field = (DataItem)item;
			assert field.type == fieldType;
		}else{// create Field and update field list
			field = new DataItem(fieldName, fieldType);
			field.next = fields;  fields = field;
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
			cls = getTypeByNameAndUpdate(tcRef, registeredClassName, null);
			cpItems[cpClassInfoIndex] = cls;
		}
		return cls;
	}

	/**
	 * Check FieldRefInfo entry in constant pool and update it accordingly if necessary.
	 * <br>That is: if there is not yet a direct reference to an object of type Field, then such an object is created and registered.
	 * @param cpFieldInfoIndex index of FieldRefInfo entry
	 * @return object of this type Class
	 */
	Item updateAndGetCpFieldEntry(int cpFieldInfoIndex){
		//pre: all strings in the const are already registered in the proper hash table.
		Item field = cpItems[cpFieldInfoIndex];
		if(field == null){
			int csx = cpIndices[cpFieldInfoIndex]; // get class and signature indices
			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
			int sx = cpIndices[csx & 0xFFFF];
			HString fieldName = cpStrings[sx>>>16];
			HString fieldDesc  = cpStrings[sx & 0xFFFF];
			Type fieldType = getTypeByDescriptor(fieldDesc);
			field = cls.insertCondAndGetField( fieldName, fieldType);
			cpItems[cpFieldInfoIndex] = field;
		}
		return field;
	}

	/**
	 * Check MethRefInfo entry in constant pool and update it accordingly if necessary.
	 * <br>That is: if there is not yet a direct reference to an object of type Method, then such an object is created and registered.
	 * @param cpMethInfoIndex index of MethRefInfo entry
	 * @return object of this type Class
	 */
	Item updateAndGetCpMethodEntry(int cpMethInfoIndex){
		//pre: all strings in the const are already registered in the proper hash table.
		Method method = null;
		if(cpItems[cpMethInfoIndex] == null){
			int csx = cpIndices[cpMethInfoIndex]; // get class and signature indices
			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
			int sx = cpIndices[csx & 0xFFFF];

			HString methName = cpStrings[sx>>>16];
			HString methDesc  = cpStrings[sx & 0xFFFF];
			method = cls.insertCondAndGetMethod( methName, methDesc);
			method.owner = cls;
			cpItems[cpMethInfoIndex] = method;
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
			
			case cptClass: cpIndices[pEntry] = clfInStrm.readUnsignedShort(); break; // class index
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
//				vrb.println("updateInteger: pEntry="+pEntry +", cpIndices[pEntry]="+cpIndices[pEntry]);
				cpItems[pEntry] = new Constant(hsNumber, wellKnownTypes[txInt], cpIndices[pEntry], 0);
				nofItems++;
				break;
			case cptFloat:  // float literal
				cpItems[pEntry] = new Constant(hsNumber, wellKnownTypes[txFloat], cpIndices[pEntry], 0);
				nofItems++;
				break; // float pattern
			case cptLong:
				cpItems[pEntry] = new Constant(hsNumber, wellKnownTypes[txLong], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptDouble:
				cpItems[pEntry] = new Constant(hsNumber, wellKnownTypes[txDouble], cpIndices[pEntry], cpIndices[pEntry+1]);
				nofItems++;
				pEntry++; 
				break;
			case cptClass: // class index
				updateAndGetCpClassEntry(pEntry);
				nofItems++;
				break;
			case cptString: 
				cpItems[pEntry] = new StringLiteral(hsString, cpStrings[cpIndices[pEntry]]);
				nofItems++;
				break;
			case cptFieldRef:
				updateAndGetCpFieldEntry(pEntry);
				nofItems++;
				break;
			case cptMethRef:
				updateAndGetCpMethodEntry(pEntry);
				nofItems++;
				break;
			case cptIntfMethRef:
				Item meth = updateAndGetCpMethodEntry(pEntry);
				meth.accAndPropFlags |= (1<<dpfInterfCall);
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
		nOfInterfaces = cnt;
		if(cnt > 0){
			interfaces = new Class[cnt];
			for (int intf = 0; intf < cnt; intf++){
				int intfInx = clfInStrm.readUnsignedShort();
				interfaces[intf] = (Class)cpItems[intfInx];
			}
		}
	}

	private Item appendItem(Item head1, Item tail1, Item head2){
		if(tail1 == null)  head1 = head2;  else  tail1.next = head2;
		return head1;
	}
	private void readFields(DataInputStream clfInStrm) throws IOException{
		if(verbose) vrb.println(">readFields: "+name);
		nOfClassFields = 0;
		nOfInstanceFields = 0;

		Item clsFieldHead = null, clsFieldTail = null;
		Item instFieldHead = null, instFieldTail = null;

		int fieldCnt = clfInStrm.readUnsignedShort();
		while(fieldCnt > 0){
			int flags;
			HString name, descriptor;

			flags = clfInStrm.readUnsignedShort(); //read access and property flags
			//--- read name and descriptor
			int index = clfInStrm.readUnsignedShort();
			name = cpStrings[index];
			Item field = getAndExtractField(name);
			index = clfInStrm.readUnsignedShort();
			descriptor = cpStrings[index];

			//--- read field attributes {ConstantValue, Deprecated, Synthetic}
			int attrCnt = clfInStrm.readUnsignedShort();
			while(attrCnt-- > 0){
				index = clfInStrm.readUnsignedShort();
				int attr = selectAttribute(index);
				int attrLength = clfInStrm.readInt();
				switch(attr){
				case atxConstantValue:
					index = clfInStrm.readUnsignedShort();
					int rcpIndex =  cpIndices[index];
					Item cpField = constPool[rcpIndex].clone();
					assert field == null || field.type == cpField.type;
					cpField.name = name;
					field = cpField;
					flags |= (1<<dpfConst);
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

			Type type = getTypeByDescriptor(descriptor);
			if(field == null) field = new DataItem(name, type);  else field.type = type;
			
			flags |= field.accAndPropFlags;
			field.accAndPropFlags = flags;
			
vrb.println(" nOfClassFields="+nOfClassFields +", nOfInstanceFields="+nOfInstanceFields +", field.name="+field.name);
			// add field
			if( (flags & (1<<apfStatic)) != 0 ){ // class field
				nOfClassFields++;
				if( (flags & (1<<dpfConst)) != 0 ){
					field.next = clsFieldHead;  clsFieldHead = field;
					if(clsFieldTail == null) clsFieldTail = field;
				}else{
					if(clsFieldHead == null)  clsFieldHead = field;  else  clsFieldTail.next = field;
					clsFieldTail = field;
				}
			}else{// instance field
				nOfInstanceFields++;
				if( (flags & (1<<dpfConst)) != 0 ){
					field.next = instFieldHead;  instFieldHead = field;
					if(instFieldTail == null) instFieldTail = field;
				}else{
					if(instFieldHead == null)  instFieldHead = field;  else  instFieldTail.next = field;
					instFieldTail = field;
				}
			}

			fieldCnt--;
		}
		assert fields == null;
		fields = appendItem(clsFieldHead, clsFieldTail, instFieldHead);	
//vrb.println("nOfClassFields="+nOfClassFields +", nOfInstanceFields="+nOfInstanceFields);
		if(verbose) vrb.println("<readFields");
	}

	private void readMethods(DataInputStream clfInStrm, int userReqAttributes) throws IOException{
		int methodCnt = clfInStrm.readUnsignedShort();
		nOfMethods = methodCnt;

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
			Type returnType = getReturnType(descriptor);
			Method method = getAndExtractMethod(name, descriptor);
			if(method == null){
				method = new Method(name, returnType, descriptor);
			}else{
				method.type = returnType;
			}
			method.owner = this;
			assert method.type == returnType;
			
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
//		methods = clsMethHead;   clsMethTail.next = instMethHead;	
		nOfClassMethods = nofClsMeths;
		nOfInstanceMethods = nofInstMeths;
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
					stab.print("String Table 0");
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
					int contPoolInx = cpIndices[thisSupClassCpInx];
					type = (Class)constPool[contPoolInx];
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
				
				if( (accAndPropFlags & ((1<<apfInterface)|(1<<apfEnum))) == 0){
					analyseByteCode();
					this.accAndPropFlags |= (1<<dpfClassLoaded);
				}

//				if(verbose){
					vrb.println("\n>dump of class: "+name);
					vrb.println("\nstate: 3");
//					stab.print("String Table in state: 3");
					printOrigConstPool("state: 3");
					printReducedConstPool("state: 3");
					printClassList("state: 3");
					print(0);
					vrb.println("\n<end of dump: "+name);
//				}
				
				clfInStrm.close();
			}catch (FileNotFoundException fnfE){
				errRep.error("class file not found"); errRep.println();
				fnfE.getCause();
			}
		}
		//--- load referenced classes
		if( (accAndPropFlags & (1<<dpfClassLoaded)) != 0){
			for(int cpx = constPool.length-1; cpx >= 0; cpx--){
				Item item = constPool[cpx];
				if(item instanceof Class){
					Class refClass = (Class) item;
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
		
		classList = null; classListTail = null;  nofClasses = 0;
		prevCpLenth = 0;  constPoolCnt = 0;

		if(StringTable.getInstance() != null) StringTable.resetTable();
		else{
			StringTable.createSingleton(1000, "??");
			stab = StringTable.getInstance();
			HString.setStringTable(stab);
		}
		hsNumber = stab.insertCondAndGetEntry("#");
		hsString = stab.insertCondAndGetEntry("\"\"");
		
		setUpBaseTypeTable();
		setClassFileAttributeTable(stab);
		
		if(verbose) vrb.println("<startLoading");
	}

	public static void releaseLoadingResources(){
		cpItems = null;  cpStrings = null;  cpIndices = null;  cpTags = null;
		prevCpLenth = 0;  	constPoolCnt = 0;
		HString.releaseBuffers();

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
		
		if(verbose) vrb.println("<loadRootClass");
	}

	private static Class loadSystemClass(SystemClass sysClass, int userReqAttributes) throws IOException{
		String systemClassName = sysClass.name;
		int sysClsAttributes = sysClass.attributes;
		if(verbose) vrb.println(">loadSystemClass: "+systemClassName);
		if(verbose) vrb.printf("  sysClsAttributes1=0x%1$x\n", sysClsAttributes);
		
		HString hSysClassName = stab.insertCondAndGetEntry(systemClassName);
		Class sysCls = (Class)getClassByName(hSysClassName);
		if(sysCls == null){
			sysCls = new Class(hSysClassName);
			appendClass(sysCls);
		}
		if( (sysClsAttributes & (1<<dpfSynthetic) ) != 0 ){// synthetic class, no loading
			sysCls.loadClass(0);
			sysCls.accAndPropFlags &= ~(1<<dpfClassLoaded); // no code loaded
		}else{//load class and mark methods
			sysCls.loadClass(userReqAttributes);
			if( (sysClsAttributes&(1<<dpfNew)) != 0 ){// set up new memory method table
				SystemMethod meth = sysClass.methods;
				while(meth != null){
					Item method = sysCls.methods.getItemByName(meth.name);
					if(method == null){
						errRep.error("method "+meth.name +" in system class "+sysClass.name + " not found");
					}else{
						if(verbose)vrb.printf("method=%1$s, attr=0x%2$x\n", (sysCls.name + "." + method.name), meth.attributes);
						int methIndex  = (meth.attributes-1)&0xFF;
						if( methIndex >= nofNewMethods ){
							errRep.error("method id of"+meth.name +" in system class "+sysClass.name + " out of range");
						}else{
							newMethods[methIndex] = method;
							if(verbose)vrb.printf("newMethods[%1$d]: %2$s\n", methIndex, method.name);
						}
					}
					meth = meth.next;
				}
			}
		}

//		vrb.printf("sysClass: %1$s\n\taccAndPropFlags3=0x%2$8x\n\tsysClsAttributes=0x%3$8x\n", sysCls.name, sysCls.accAndPropFlags, sysClsAttributes);
		sysCls.accAndPropFlags |= (sysClsAttributes & ((1<<dpfUnsafe)|(1<<dpfNew)|(1<<dpfSynthetic) ) | (1<<dpfSysPrimitive) );
//		vrb.printf("\taccAndPropFlags4=0x%1$8x\n", sysCls.accAndPropFlags);
		int methAttrAddOn = sysCls.accAndPropFlags & ((1<<dpfUnsafe)|(1<<dpfNew)|(1<<dpfSynthetic)|(1<<dpfSysPrimitive));
//		vrb.printf("\t  methAttrAddOn5=0x%1$8x\n", methAttrAddOn);
		
		SystemMethod meth = sysClass.methods;
		while(meth != null){
			Item method = sysCls.methods.getItemByName(meth.name);
			if(method != null){
				method.accAndPropFlags |= methAttrAddOn;
			}
			meth = meth.next;
		}
		
		if(verbose) vrb.printf("  sysClsAttributes2=0x%1$x\n", sysClsAttributes);
//
//		if(verbose){
//			vrb.println(" system class: "+sysCls.name);
//			sysCls.print(0);
//			vrb.println(" end of system class: "+sysCls.name);
//		}
		if(verbose) vrb.println("<loadSystemClass");
		return sysCls;
	}

	private static void loadSystemClasses(SystemClass sysClasses, int userReqAttributes) throws IOException{
		while(sysClasses != null){
			Class sysCls = loadSystemClass(sysClasses, userReqAttributes);
			if(verbose){
				vrb.println(" *system class: "+sysCls.name);
				sysCls.print(0);
//				printClassList(" *** class list:");
				vrb.println(" end of *system class: "+sysCls.name);
//				sysCls.print(0);
			}
			sysClasses = sysClasses.next;
		}
	}

	public static void buildSystem(String[] rootClassNames, String[] parentDirsOfClassFiles, SystemClass sysClasses, int userReqAttributes) throws IOException{
//		Utilities.setTargetFilesFolder(targetFilesFolder);
		errRep.nofErrors = 0;
		Type.nofRootClasses = 0;
		ClassFileAdmin.registerParentDirs(parentDirsOfClassFiles);
		
		int nofRootClasses = rootClassNames.length;
//		rootClasses = new Class[nofRootClasses];
		startLoading(nofRootClasses);
		
		loadSystemClasses(sysClasses, userReqAttributes);
//		assert false;
		if(verbose) printClassList("state: sysClasses loaded, class list:");

		for (int rc = 0; rc < nofRootClasses && errRep.nofErrors == 0; rc++){
			String sname = rootClassNames[rc];
			vrb.println("\n\nRootClass["+rc +"] = "+ sname);
			loadRootClass( sname, userReqAttributes);
		}

		if(verbose) printClassList("end state, class list:");
		
		releaseLoadingResources();
		log.printf("number of errors %1$d\n", errRep.nofErrors);
		log.print("system building ");
		if(errRep.nofErrors == 0) log.println("successfully done"); else log.println("terminated with errors");
	}

	public static void buildSystem(String[] rootClassNames, int userReqAttributes) throws IOException{
		buildSystem(rootClassNames, new String[] {"bin"}, null, userReqAttributes);
	}

	//--- debug primitives
	public void printItemCategory(){
		vrb.print("class");
	}

	public static void printClassList(String title){
		vrb.println("\nclass list: (nofClasses="+nofClasses +')');
		vrb.println(title);
		if(verbose) vrb.println("\n<class list:");
		Item cls = classList;
		while(cls != null){
			Dbg.indent(1);
			Dbg.printJavaAccAndPropertyFlags(cls.accAndPropFlags);  vrb.print(cls.name);
			vrb.print(";//dFlags");  Dbg.printDeepAccAndPropertyFlags(cls.accAndPropFlags); vrb.println();
			cls.printFields(2);
			cls.printMethods(2);
			cls = cls.next;
			vrb.println();
		}
		if(verbose) vrb.println("end of class list>");
	}

	public void printFields(int indentLevel){
		indent(indentLevel);
		vrb.println("fields: ("+nOfClassFields + ',' + nOfInstanceFields +')');
		Item item = fields;
		while(item != null){
			item.println(indentLevel+1);
			item = item.next;
		}
	}

	public void printMethods(int indentLevel){
		indent(indentLevel);
		vrb.println("methods:");
		Item item = methods;
		while(item != null){
			item.println(indentLevel+1);
			item = item.next;
		}
	}

	private void printRedCpEntry(int redCpInd){
		Item item = constPool[redCpInd];
		item.printShort(0);
		Dbg.printSpace(); Dbg.printJavaAccAndPropertyFlags(item.accAndPropFlags);
		Dbg.print('+'); Dbg.printDeepAccAndPropertyFlags(item.accAndPropFlags);
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
			indent(indentLevel);
			vrb.print("imports: "); vrb.print(imports[0].name);
			int nofImp = imports.length;
			for(int imp = 1; imp < nofImp; imp++){
				vrb.print(", ");  vrb.print(imports[imp].name);
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
		vrb.println("\nreduced constant pool:"); vrb.println(title);
		for(int pe = 0; pe < constPool.length; pe++){
			vrb.printf("  [%1$3d] ", pe);
			printRedCpEntry(pe);
			vrb.println();
		}		
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.print("class ");  vrb.print(name);
		if(type != null) {
			vrb.print(" extends "); vrb.print(type.name);
		}
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(accAndPropFlags);
		vrb.print("class ");  vrb.print(name);
		if(type != null) {
			vrb.print(" extends "); vrb.print(type.name);
		}
		vrb.print("\n\t// dFlags");  Dbg.printDeepAccAndPropertyFlags(accAndPropFlags);
		vrb.print("\n\t// category: ");  vrb.print((char)category);
		vrb.print("\n\t// source file: ");  vrb.println(srcFileName);

		printInterfaces(indentLevel+1);
		vrb.println('{');
		printImports(indentLevel+1);
		vrb.println();

		printFields(indentLevel+1);
		printMethods(indentLevel+1);
	}
}
