package ch.ntb.inf.deep.classItems;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import ch.ntb.inf.deep.config.SystemClass;
import ch.ntb.inf.deep.config.SystemMethod;
import ch.ntb.inf.deep.host.ClassFileAdmin;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class CFR implements ICclassFileConsts, ICdescAndTypeConsts, ICjvmInstructionOpcs {
	static final boolean clsDbg = false;
	static final boolean verbose = Item.verbose;
	static PrintStream vrb = Item.vrb;
	static PrintStream log = Item.log;
	static ErrorReporter errRep = Item.errRep;

	public static final byte nofNewMethods = 4; // bc instructions: {new[0], newarray[1], anewarray[2], multianewarray[3]}
	public static final Item[] newMethods = new Item[nofNewMethods];

	//-- registered well known names
	static HString hsNumber, hsString;	// names for number and literal string objects (objects of type Constant, i.e. StdConstant, StringLiteral)
	static HString hsClassConstrName;	// name of the class constructor method
	static HString hsCommandDescriptor;	// descriptor of a command method


	public static void buildSystem(HString[] rootClassNames, File[] parentDirsOfClassFiles, SystemClass[] sysClasses, int userReqAttributes) throws IOException {

		Item.errRep.nofErrors = 0;
		Method.compSpecSubroutines = null; // /TODO move this to init function?
		ClassFileAdmin.registerParentDirs(parentDirsOfClassFiles);

		int nofRootClasses = rootClassNames.length;
		initBuildSystem(nofRootClasses);

//		Class clsObject = (Class)Type.wellKnownTypes[Type.txObject];
//		clsObject.loadClass(userReqAttributes);
//		clsObject.completeLoadingOfRootClass();

//		Class clsString = (Class)Type.wellKnownTypes[Type.txString];
//		clsString.loadClass(userReqAttributes);
//		clsString.completeLoadingOfRootClass();

		for (SystemClass cls : sysClasses) loadSystemClass(cls, userReqAttributes);

		if(verbose) Class.printClassList("state: sysClasses loaded, class list:");

		for (int rc = 0; rc < nofRootClasses && errRep.nofErrors == 0; rc++){
			String sname = rootClassNames[rc].toString();
			if(verbose) vrb.println("\n\nRootClass["+rc +"] = "+ sname);
			loadRootClass(sname, userReqAttributes);
			if(errRep.nofErrors > 0) return;
		}

		// iterates through all classes and replaces all stubs in the constant pool of that class  
		if(verbose) vrb.println(">replace constant pool stubs:");
		Item type = RefType.refTypeList;
		while (type != null) {
			if (type instanceof Class) {
				Class cls = (Class)type;
				if(cls.constPool != null) {
					Item[] cp = cls.constPool;
					for(int cpx = cp.length-1; cpx >= 0; cpx--) cp[cpx] = cp[cpx].getReplacedStub();
				}
			}
			type = type.next;
		}
		if(verbose) vrb.println("<replace constant pool stubs");

		// iterates through all classes and fixup loaded classes  
		if(verbose) vrb.println(">fixup loaded classes:");
		type = RefType.refTypeList;
		while (type != null) {
			if (type instanceof Class) type.fixupLoadedClasses();
			type = type.next;
		}
		if(verbose) vrb.println("<fixup loaded classes");

		RefType.refTypeList = (RefType)RefType.refTypeList.next;	// delete front stub

		// split class groups, count referenced interfaces, 
		//vrb.println(">splitClassGroups..:");
		Item refType = RefType.refTypeList;
		Class.extLevelOrdredClasses = new Class[Class.maxExtensionLevelStdClasses+1];
		Class.extLevelOrdredInterfaces = new Class[Class.maxExtensionLevelInterfaces+1];
		Class.arrayClasses = null;
		
		//		int nofRefIntf = 0;
		while (refType != null){
			refType.accAndPropFlags &= ~(1<<dpfClassMark); // clear classMark
			int propFlags = refType.accAndPropFlags;
			if (refType instanceof Class){
				Class cls1 = (Class)refType;
				int extLevel = cls1.extensionLevel;
				if ((propFlags&(1<<apfInterface)) != 0 ){	// interface-Class
					//					if( (propFlags&(1<<dpfInterfCall)) != 0 ) nofRefIntf++;
					if( cls1.methTabLength > Class.maxInterfMethTabLen ) Class.maxInterfMethTabLen = cls1.methTabLength;
					cls1.nextExtLevelClass = Class.extLevelOrdredInterfaces[extLevel];
					Class.extLevelOrdredInterfaces[extLevel] = cls1;
					Class.nofInterfaceClasses++;
				} else {	// {(std-)Class, (enum-)Class, (enumArray-)Class}
					if (cls1.methTabLength > Class.maxMethTabLen ) Class.maxMethTabLen = cls1.methTabLength;
					cls1.nextExtLevelClass = Class.extLevelOrdredClasses[extLevel];
					Class.extLevelOrdredClasses[extLevel] = cls1;
					Class.nofStdClasses++;
					cls1.sortInterfaces();
				}
				//				}else if( (propFlags & (1<<apfEnum) ) != 0 ){
				//					cls.nextClass = enums;
				//					enums = cls;
				//				}else if( (propFlags & (1<<apfEnumArray) ) != 0 ){
				//					cls.nextClass = enumArrays;
				//					enumArrays = cls;
				//				}
			} else {	// assert classItem instanceof Array;
				Array a = (Array)refType;
				a.nextArray = Class.arrayClasses;
				Class.arrayClasses = a;
				Class.nofArrays++;
			}
			refType = refType.next;
		}
		
		//--- set interface identifiers (from max. extension level to 0)
		for(int exl = Class.maxExtensionLevelInterfaces; exl > 0; exl--){
			Class cls2 = Class.extLevelOrdredInterfaces[exl];
			while(cls2 != null){
				if( cls2.index <= 0 && (cls2.accAndPropFlags&(1<<dpfInterfCall)) != 0){
					cls2.createInterfaceIdsToRoot();
					cls2.cleanUpInterfaceMethsAndGetMethTabLength();
				}
				cls2 = cls2.nextExtLevelClass;
			}
		}
		
		//--- arrange instance methods according to interfaces
		MethodArrangement methA= new MethodArrangement( Class.maxExtensionLevelStdClasses, Class.maxMethTabLen, Class.maxInterfMethTabLen);
		for (int exl = Class.maxExtensionLevelStdClasses; exl > 0; exl--) {
			Class cls4 = Class.extLevelOrdredClasses[exl];
			while (cls4 != null) {
				methA.arrangeInterfaceMethodsForThisClassStack(cls4);
				cls4 = cls4.nextExtLevelClass;
			}
		}
		
		//--- generate instance method tables
		for (int exl = 0; exl <= Class.maxExtensionLevelStdClasses; exl++) {
			Class cls3 = Class.extLevelOrdredClasses[exl];
			while (cls3 != null){
				cls3.fixInstanceMethodsOfThisClass();
				methA.generateMethodTableForThisClass(cls3);
				cls3 = cls3.nextExtLevelClass;
			}
		}

		Class.releaseLoadingResources();
		log.print("Loading class files ");
		if (errRep.nofErrors == 0) log.println("successfully done"); else log.println("terminated with errors");
	}

	private static void initBuildSystem(int nofRootClasses) {
		if(verbose) vrb.println(">init build system:");

		Class.nofRootClasses = 0;
		Class.rootClasses = new Class[nofRootClasses];	// all root classes get registered separately
		Class.prevCpLenth = 0;  Class.constPoolCnt = 0;

		Item.stab = StringTable.getInstance();
		registerWellKnownNames();
		Class cls = new Class(hsClassConstrName);	// insert stub with any name, will be removed at end of build process
		RefType.refTypeList = cls; RefType.refTypeListTail = cls;
		RefType.nofRefTypes = 0;
		Class.nofStdClasses = 0; Class.nofInterfaceClasses = 0; Class.nofInitClasses = 0; Class.nofNonInitClasses = 0; Class.nofArrays = 0;
		Class.initClassesTail = null; Class.nonInitClassesTail = null;

		Type.setUpBaseTypeTable();
		Type.setAttributeTable(Item.stab);

		if(verbose) vrb.println("<init build system");
	}

	private static void loadRootClass(String rootClassName, int userReqAttributes) throws IOException {
		if(verbose) vrb.println(">loadRootClass: " + rootClassName);

		HString hRootClassName = Item.stab.insertCondAndGetEntry(rootClassName);
		Class root = new Class(hRootClassName);
		Class.appendRootClass(root);
		root.accAndPropFlags |= (1<<dpfRootClass);
		assert root.next == null;
		root.loadClass(userReqAttributes);

		if(verbose) vrb.println("<loadRootClass");
	}

	private static void loadSystemClass(SystemClass systemClass, int userReqAttributes) throws IOException {
		String systemClassName = systemClass.getName().toString();
		
		if (verbose) vrb.println(">loadSystemClass: "+systemClassName);
		if (verbose) {
			vrb.printf("  sysClsAttributes1=0x%1$x", systemClass.attributes);
			Dbg.printAccAndPropertyFlags(systemClass.attributes); Dbg.println();
		}
		
		// if class has methods defined in the configuration -> set dpfSysPrimitive
		// add all attributes from those methods to local variable 
		int sysClsAttr = systemClass.attributes;
		SystemMethod systemMeth = systemClass.methods;
		if (systemMeth != null) sysClsAttr |= 1<<dpfSysPrimitive;
		while (systemMeth != null){
			sysClsAttr |= (systemMeth.attributes & dpfSetMethProperties);
			systemMeth = (SystemMethod)systemMeth.next;
		}

		if (verbose) {
			vrb.printf("  unified sysClsAttributes1=0x%1$x", sysClsAttr);
			Dbg.printAccAndPropertyFlags(sysClsAttr); Dbg.println();
		}

		HString hSysClassName = Item.stab.insertCondAndGetEntry(systemClassName);
		Class cls = (Class)Class.getRefTypeByName(hSysClassName);
		if (cls == null) {
			cls = new Class(hSysClassName);
			RefType.appendRefType((RefType)cls);
		}
		cls.loadClass(userReqAttributes);
		// add flags which are set by the configuration to the class flags
		cls.accAndPropFlags |= sysClsAttr & (dpfSetClassProperties | dpfSetMethProperties);

		// class = Heap.java, set up new memory method table
		if ((sysClsAttr & (1<<dpfNew)) != 0 ){	
			systemMeth = systemClass.methods;
			while (systemMeth != null) {
				Item method = cls.methods.getItemByName(systemMeth.getName());
				if (method == null){
					errRep.error(301, systemMeth.getName() + " in system class " + systemClass.getName());
				} else {
					if (verbose)vrb.printf("lsc: method=%1$s, attr=0x%2$x\n", (cls.name + "." + method.name), systemMeth.attributes);
					int methIndex  = (systemMeth.id-1)&0xFF;
					if (methIndex >= nofNewMethods){
						errRep.error(302, systemMeth.getName() + " in system class " + systemClass.getName());
					} else {
						if (verbose) vrb.println(" ldSysCls: newMethInx="+methIndex);
						newMethods[methIndex] = method;
						if (verbose) vrb.printf("lsc: newMethods[%1$d]: %2$s\n", methIndex, method.name);
					}
				}
				systemMeth = (SystemMethod)systemMeth.next;
			}
		}

		// update method attributes (with system method attributes)
		systemMeth = systemClass.methods;
		while (systemMeth != null){
			Method method = (Method)cls.methods.getItemByName(systemMeth.getName());
			if (method != null){
				method.offset = systemMeth.offset;
				method.id = systemMeth.id;
				method.accAndPropFlags |= (systemMeth.attributes & dpfSetMethProperties) | (1<<dpfSysPrimitive);
				if ((method.accAndPropFlags & (1<<dpfSynthetic)) != 0) ((Method)method).clearCodeAndAssociatedFields();
			} // else: methods defined in the configuration but not present in class file
			systemMeth = (SystemMethod) systemMeth.next;
		}

		if(verbose) vrb.println("<loadSystemClass");
	}
	
	private static void registerWellKnownNames() {
		hsNumber = Class.stab.insertCondAndGetEntry("#");
		hsString = Class.stab.insertCondAndGetEntry("\"\"");
		hsClassConstrName = Class.stab.insertCondAndGetEntry("<clinit>");
		hsCommandDescriptor = Class.stab.insertCondAndGetEntry("()V");
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


}
