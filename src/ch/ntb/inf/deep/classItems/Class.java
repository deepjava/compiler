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
//	private static Item constFields; // constant field list (unsorted)
	static{
		assert fieldSizeUnit >= 4 && (fieldSizeUnit & (fieldSizeUnit-1)) == 0;
	}

	//--- instance fields
	public Item[] constPool; // reduced constant pool
	
	public Item methods; // list with all methods
	public int nOfMethods; // number of methods
	public int nOfClassMethods, nOfInstanceMethods; // number of methods
	
	public Item fields; // list with all fields
	public int nOfClassFields, nOfInstanceFields; // number of fields
		
	public Class[] interfaces;
	public int nOfInterfaces; // number of interfaces

	Class[] imports;
	public int nOfImports; // number of imports (without arrays)
	
	public int nOfBaseClasses; // number of base classes
	
	public int nOfReferences; // number of class fields which are references
	
	public int[] constantBlock; // the constant block for this class
	public int constantBlockSize; // size of the constant block
	public int constantPoolSize; // size of this pool on the target (in byte)
	public int stringPoolSize; // size of this pool on the target (in byte)
	public int classDescriptorSize; // size of the class descriptor on the target (in byte)

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

	/**
	 * Select field by name and delete it in the fields list (if found).
	 * @param fieldName
	 * @return  the selected field or null if not found
	 */
//	private Item getAndExtractField(HString fieldName){
//		assert false;
//		Item item = fields, pred = null;
//		while(item != null && item.name != fieldName) {
//			pred = item;
//			item = item.next;
//		}
//		if(item != null){
//			if(pred == null) fields = item.next; else  pred.next = item.next;
//			item.next = null;
//		}
//		return item;
//	}

	Item getField(HString fieldName){
		Item item = fields;
		while(item != null && item.name != fieldName) item = item.next;
		if(item == null && type != null) item = ((Class)type).getField(fieldName);
		return item;
	}

	/**
	 * Select method by name and descriptor and delete it in the methods list if found.
	 * @param methName
	 * @param methDescriptor
	 * @return the selected method or null if not found
	 */
//	private Method getAndExtractMethod(HString methName, HString methDescriptor){
//		assert false;
//		Item item = methods, pred = null;
//		while(item != null && (item.name != methName || ((Method)item).methDescriptor != methDescriptor) ) {
//			pred = item;
//			item = item.next;
//		}
//		if(item != null){
//			if(pred == null) methods = item.next; else pred.next = item.next;
//			item.next = null;
//		}
//		return (Method)item;
//	}

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
			if(registeredClassName.charAt(0) == '[') cls = getTypeByNameAndUpdate(tcArray, registeredClassName, wktObject);
			else cls = getTypeByNameAndUpdate(tcRef, registeredClassName, null);
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
//	private Item updateAndGetCpFieldEntry(int cpFieldInfoIndex){
//		//pre: all strings in the const are already registered in the proper hash table.
//		Item field = cpItems[cpFieldInfoIndex];
//		if(field == null){
//			int csx = cpIndices[cpFieldInfoIndex]; // get class and signature indices
//			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
//			int sx = cpIndices[csx & 0xFFFF];
//			HString fieldName = cpStrings[sx>>>16];
//			HString fieldDesc  = cpStrings[sx & 0xFFFF];
//			Type fieldType = getTypeByDescriptor(fieldDesc);
//			field = cls.insertCondAndGetField( fieldName, fieldType);
//			cpItems[cpFieldInfoIndex] = field;
//		}
//		return field;
//	}

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

	/**
	 * Check MethRefInfo entry in constant pool and update it accordingly if necessary.
	 * <br>That is: if there is not yet a direct reference to an object of type Method, then such an object is created and registered.
	 * @param cpMethInfoIndex index of MethRefInfo entry
	 * @return object of this type Class
	 */
//	private Item updateAndGetCpInterfaceMethodEntry(int cpMethInfoIndex){
//		//pre: all strings in the const are already registered in the proper hash table.
//		Method method = null;
//		if(cpItems[cpMethInfoIndex] == null){
//			int csx = cpIndices[cpMethInfoIndex]; // get class and signature indices
//			Class cls = (Class)updateAndGetCpClassEntry(csx>>>16);
//			int sx = cpIndices[csx & 0xFFFF];
//
//			HString methName = cpStrings[sx>>>16];
//			HString methDesc  = cpStrings[sx & 0xFFFF];
//			method = cls.insertCondAndGetMethod( methName, methDesc);
//			method.owner = cls;
//			cpItems[cpMethInfoIndex] = method;
//		}
//		return method;
//	}

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
				nOfImports++;
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
//				updateAndGetCpClassEntry(pEntry);
				Item item = updateAndGetCpClassEntry(pEntry);
				cpItems[pEntry] = item;
				if( item instanceof Array) nOfImports--; // arrays get not included in imports
				nofItems++;
				break;
			case cptString: 
				cpItems[pEntry] = new StringLiteral(hsString, cpStrings[cpIndices[pEntry]]);
				nofItems++;
				break;
			case cptFieldRef:
//				updateAndGetCpFieldEntry(pEntry);
				cpItems[pEntry] = getFieldOrStub(pEntry);
				nofItems++;
				break;
			case cptMethRef:
//				updateAndGetCpMethodEntry(pEntry);
				cpItems[pEntry] = getMethodOrStub(pEntry);
				nofItems++;
				break;
			case cptIntfMethRef:
//				Item meth = updateAndGetCpInterfaceMethodEntry(pEntry);
//				meth.accAndPropFlags |= (1<<dpfInterfCall);
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
		nOfInterfaces = cnt;
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
		int sizeInBits = type.sizeInBits;
		int fieldList = sizeInBits>>3; // fieldList={0, 1, 2, 4, 8}
		if( (item.accAndPropFlags & (1<<apfStatic)) == 0){// instance field
			item.next = instFieldLists[fieldList];  instFieldLists[fieldList] = item;
			nOfInstanceFields++;
		}else{// class field
			item.next = classFieldLists[fieldList];  classFieldLists[fieldList] = item;			
			nOfClassFields++;
		}
		if(verbose) vrb.println("<addItemToFieldList");
	}

	private void clearFieldLists(){
		if(verbose) vrb.println(">clearFieldLists");
		nOfClassFields = 0;  nOfInstanceFields = 0;
		instanceFieldsSize = 0; classFieldsSize = 0;
		for(int n = fieldListArrayLength-1; n >= 0; n--){
			instFieldLists[n] = null;  classFieldLists[n] = null;
		}
		if(verbose) vrb.println("<clearFieldLists");
	}

	private Item getFieldListAndUpdate(Item[] fieldLists){
//		final boolean verbose = true;
		if(verbose) vrb.printf(">getFieldListAndUpdate: class: %1$s\n", name);

		Item head = null, tail = null;
		Item rest = null;
		
		//--- select and extract reference fields
		int fieldsSize = 0;
		
		Item list = fieldLists[4];
		Item item = list;
		while(item != null){
			list = item.next;
			Type type = (Type)item.type;
			if( type.category != tcPrimitive ){// reference (tcRef || tcArray)
				fieldsSize += 4;
				item.next = head; head = item;
				if(tail == null) tail = item;
			}else{
				item.next = rest; rest = item;
			}
			item = list;
		}
		fieldLists[4] = rest;
//		vrb.printf(">getF1: fieldsSize=%1$d\n", fieldsSize);
		
		//--- select and extract any other fields
		Item consts = null;
		for(int category = 8; category >= 0; category--){
			list = fieldLists[category];
			fieldLists[category] = null;
			item = list;
			while(item != null){
				list = item.next;
				if( (item.accAndPropFlags & (1<<dpfConst)) != 0){
					item.next = consts;
					consts = item;
//					vrb.printf(">getF1a: const.name=%1$s\n", consts.name);
				}else{
					fieldsSize += Math.max(category, 1); // reserve for boolean etc to 1 B
					if(tail == null)  head = item; else tail.next = item;
					tail = item;
				}
				item = list;
			}			
		}
//		vrb.printf(">getF2: fieldsSize=%1$d\n", fieldsSize);

		if( fieldLists == instFieldLists)
			instanceFieldsSize = (fieldsSize + (fieldSizeUnit-1)) & -fieldSizeUnit;
		else
			classFieldsSize = (fieldsSize + (fieldSizeUnit-1)) & -fieldSizeUnit;
		
//		vrb.printf(">getF3: objectSizeOrDim=%1$d, classFieldsSize=%2$d\n", instanceFieldsSize, classFieldsSize);
		
		if( tail == null ) head = consts; else tail.next = consts;

		if(verbose) vrb.println("<getFieldListAndUpdate");
		return head;
	}

	private Item appendItem(Item head1, Item tail1, Item head2){
		if(tail1 == null)  head1 = head2;  else  tail1.next = head2;
		return head1;
	}

	private void readFields(DataInputStream clfInStrm) throws IOException{
		final boolean verbose = false;
		
		if(verbose) vrb.println(">readFields: "+name);
		clearFieldLists();
		assert fields == null;
		
		int fieldCnt = clfInStrm.readUnsignedShort();
		while(fieldCnt > 0){
			int flags;
			HString name, descriptor;

			flags = clfInStrm.readUnsignedShort(); //read access and property flags
			//--- read name and descriptor
			int index = clfInStrm.readUnsignedShort();
			name = cpStrings[index];
//			Item field = getAndExtractField(name);
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
		
		assert fields == null;
//		if(fields != null){
//			Item item = fields;
//			while(item != null){
//				vrb.print("("+item.name +") ");
//				item.println(1);
//				item = item.next;
//			}
//			vrb.println(" Class.readFieldsE-end");
//		}
		
		
		Item clsFields = getFieldListAndUpdate(classFieldLists);
		Item instFields = getFieldListAndUpdate(instFieldLists);
		if(clsFields == null) clsFields = instFields;
		else{
			Item tail = null, item = clsFields;
			while(item != null){
				tail = item; item = item.next;
			}
			tail.next = instFields;
		}
		fields = clsFields;
		if(verbose) vrb.println("<readFields");
	}

	private void readMethods(DataInputStream clfInStrm, int userReqAttributes) throws IOException{
		int methodCnt = clfInStrm.readUnsignedShort();
		nOfMethods = methodCnt;
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
//				vrb.printf(">command: %1$s%2$s\n", name, descriptor);
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
			nOfImports--;
			imports = new Class[nOfImports];
			int impIndex = nOfImports;
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

	protected void selectAndMoveInitClasses(){
		if( (accAndPropFlags & 1<<dpfClassMark) == 0 ){
			accAndPropFlags |= 1<<dpfClassMark;

			if(type != null) type.selectAndMoveInitClasses();
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
		fixUpObjectSize();
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
		if(verbose) {
//			printClassList("end state, class list:");
//			printConstPools();
		}
		
		releaseLoadingResources();
		log.printf("number of errors %1$d\n", errRep.nofErrors);
		log.print("system building ");
		if(errRep.nofErrors == 0) log.println("successfully done"); else log.println("terminated with errors");
	}

//	public static void buildSystem(String[] rootClassNames, int userReqAttributes) throws IOException{
//		buildSystem(rootClassNames, new String[] {"bin"}, null, userReqAttributes);
//	}

	//--- debug primitives
	public void printItemCategory(){
		vrb.print("class");
	}

	public static void printClassList(String title){
		if(title != null) vrb.println(title);
		vrb.printf("\nclass list: nofClasses=%1$d (with class constr. %2$d), nofArrays=%3$d\n", nofClasses, nofInitClasses, nofArrays);
		vrb.printf("\tclassListHead=%1$s", classList.name);
		if(nofInitClasses > 0) vrb.printf(", classInitListTail=%1$s", classInitListTail.name);
		vrb.printf(", classListTail=%1$s\n", classListTail.name);
		if(nofInitClasses > 0) vrb.println("//--- a) classes with class constructor");
		Item cls = classList;
		while(cls != null){
			Dbg.indent(1);
			Dbg.printJavaAccAndPropertyFlags(cls.accAndPropFlags, 'C');  vrb.printf("class %1$s", cls.name);
			if(cls.type != null)  vrb.printf(" extends %1$s", cls.type.name);
			vrb.print(" //dFlags");  Dbg.printDeepAccAndPropertyFlags(cls.accAndPropFlags, 'C'); vrb.println();
			if (cls instanceof Class) ((Class)cls).printImports(2);
			cls.printFields(2);
			cls.printMethods(2);
			if(cls == classInitListTail) vrb.println("//--- b) classes without class constructor");
			cls = cls.next;
			vrb.println();
		}
		if(verbose) vrb.println("end of class list>");
	}

	public void printFields(int indentLevel){
		indent(indentLevel);
		vrb.printf("fields: (clsFields: #=%1$d, size=%2$d B; instFields: #=%3$d, size=%4$d B)\n", nOfClassFields, classFieldsSize, nOfInstanceFields, instanceFieldsSize);
		Item item = fields;
		while(item != null){
			item.println(indentLevel+1);
			item = item.next;
		}
	}

	public void printMethods(int indentLevel){
		indent(indentLevel);
		vrb.println("methods: (#clsMeths="+ nOfClassMethods + ",#InstMeths=" + nOfInstanceMethods +')' );
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
			indent(indentLevel); 	vrb.printf("imports: %1$d\n", nOfImports);
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
		vrb.printf("\nreduced constant pool: %1$s (nOfImports=%2$d)\n", title, nOfImports);
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
		Dbg.printJavaAccAndPropertyFlags(accAndPropFlags, 'C');
		vrb.print("class ");  vrb.print(name);
		if(type != null) {
			vrb.print(" extends "); vrb.print(type.name);
		}
		vrb.print("\n\t// dFlags");  Dbg.printDeepAccAndPropertyFlags(accAndPropFlags, 'C');
		vrb.print("\n\t// category: ");  vrb.print((char)category);
		vrb.print("\n\t// source file: ");  vrb.println(srcFileName);

		printInterfaces(indentLevel+1);
		vrb.println('{');
		printImports(indentLevel+1);
		vrb.println();

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
			for(int j = 0; j < this.nOfReferences; j++) {
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
