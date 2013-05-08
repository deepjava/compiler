package ch.ntb.inf.deep.classItems;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ClassFileAdmin;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class CFR implements ICclassFileConsts, ICdescAndTypeConsts, ICjvmInstructionOpcs {
	static final boolean clsDbg = false;
	static final boolean dbg = Item.dbg;
	static PrintStream vrb = Item.vrb;
	static PrintStream log = Item.log;
	static ErrorReporter errRep = Item.errRep;

	public static final byte nofNewMethods = 4; // bc instructions: {new[0], newarray[1], anewarray[2], multianewarray[3]}
	public static final Item[] newMethods = new Item[nofNewMethods];

	//-- registered well known names
	static HString hsNumber, hsString;	// names for number and literal string objects (objects of type Constant, i.e. StdConstant, StringLiteral)
	static HString hsClassConstrName;	// name of the class constructor method
	static HString hsCommandDescriptor;	// descriptor of a command method

	public static void buildSystem(HString[] rootClassNames, File[] parentDirsOfClassFiles, Class[] sysClasses, int userReqAttributes) {
//		boolean dbg = true;
		Item.errRep.nofErrors = 0;
		ClassFileAdmin.registerParentDirs(parentDirsOfClassFiles);

		int nofRootClasses = rootClassNames.length;
		Class.rootClasses = new Class[nofRootClasses];	// all root classes get registered separately

		if (sysClasses != null) for (Class cls : sysClasses) loadSystemClass(cls, userReqAttributes);

		for (int rc = 0; rc < nofRootClasses && errRep.nofErrors == 0; rc++){
			String sname = rootClassNames[rc].toString();
			if (dbg) vrb.println("\n\nRootClass["+rc +"] = "+ sname);
			loadRootClass(sname, userReqAttributes);
		}

		// iterates through all classes and replaces all stubs in the constant pool of that class  
		if (dbg) vrb.println(">replace constant pool stubs:");
		Item type = RefType.refTypeList;
		while (type != null) {
			if (type instanceof Class) {
				Class cls = (Class)type;
				if (cls.constPool != null) {
					Item[] cp = cls.constPool;
					for (int cpx = cp.length-1; cpx >= 0; cpx--) cp[cpx] = cp[cpx].getReplacedStub();
				}
			}
			type = type.next;
		}
		if (dbg) vrb.println("<replace constant pool stubs");

		// iterates through all classes and fixup loaded classes  
		if (dbg) vrb.println(">fixup loaded classes:");
		type = RefType.refTypeList;
		while (type != null) {	// handle stdClasses and interfaces (no arrays)
			if (type instanceof Class) ((Class)type).fixupLoadedClasses();
			type = type.next;
		}
		if (dbg) vrb.println("<fixup loaded classes");

		RefType.refTypeList = (RefType)RefType.refTypeList.next;	// delete front stub
		
		// split class groups into std classes, interfaces and arrays
		if (dbg) vrb.println(">split class groups");
		Item refType = RefType.refTypeList;
		Class.extLevelOrdClasses = new Class[Class.maxExtensionLevelStdClasses+1];
		Class.extLevelOrdInterfaces = new Class[Class.maxExtensionLevelInterfaces+1];
		Class.arrayClasses = null;
		
		while (refType != null){
			refType.accAndPropFlags &= ~(1<<dpfClassMark); // clear mark
			int propFlags = refType.accAndPropFlags;
			if (refType instanceof Class){
				Class cls = (Class)refType;
				int extLevel = cls.extensionLevel;
				if ((propFlags & (1<<apfInterface)) != 0 ) {	// is interface
					if (cls.methTabLength > Class.maxInterfMethTabLen ) Class.maxInterfMethTabLen = cls.methTabLength;
					cls.nextExtLevelClass = Class.extLevelOrdInterfaces[extLevel];
					Class.extLevelOrdInterfaces[extLevel] = cls;
					Class.nofInterfaceClasses++;
				} else {	// is std-class or enum
					if (cls.methTabLength > Class.maxMethTabLen ) Class.maxMethTabLen = cls.methTabLength;
					cls.nextExtLevelClass = Class.extLevelOrdClasses[extLevel];
					Class.extLevelOrdClasses[extLevel] = cls;
					Class.nofStdClasses++;
				}
			} else {	
				assert refType instanceof Array;
				Array arr = (Array)refType;
				arr.nextArray = Class.arrayClasses;
				Class.arrayClasses = arr;
				Class.nofArrays++;
				if ((arr.accAndPropFlags & (1<<dpfTypeTest)) != 0) { // if array of interfaces whose type is tested for
					if ((arr.componentType.accAndPropFlags & (1<<apfInterface)) != 0) { 	
						Class compType = (Class)arr.componentType;
						compType.accAndPropFlags |= (1<<dpfTypeTest);	// set flag in component type 
						// add to list if not already present
						Class intf = Class.constBlockInterfaces;
						while (intf != null && intf != compType) intf = intf.nextInterface;
						if (intf == null) {
							compType.nextInterface = Class.constBlockInterfaces; 
							Class.constBlockInterfaces = compType;
						}
					}
				}
			}
			refType = refType.next;
		}
		if(dbg) vrb.println(">split class groups");
		
		// set interface identifiers (from max. extension level to 0)
		for (int exl = Class.maxExtensionLevelInterfaces; exl > 0; exl--) {
			Class cls = Class.extLevelOrdInterfaces[exl];
			while (cls != null) {
				// set interface identifiers for interfaces with methods called by invokeinterface
				if (cls.index < 0 && (cls.accAndPropFlags&(1<<dpfInterfCall)) != 0) {
					cls.index = Class.currInterfaceId++;
					cls.setIntfIdToRoot(cls.interfaces);	// set id's in superinterfaces
				}
				// set interface check identifiers for interfaces whose type is checked
				if ((cls.accAndPropFlags & (1<<dpfTypeTest)) != 0) {
					cls.chkId = Class.currInterfaceChkId++;
				}
				cls = cls.nextExtLevelClass;
			}
		}
		
		// generate instance method tables
		if (dbg) vrb.println(">generating instance method tables");
		for (int exl = 0; exl <= Class.maxExtensionLevelStdClasses; exl++) {
			Class cls = Class.extLevelOrdClasses[exl];
			while (cls != null) {
				if (dbg) vrb.println(cls.name + ": method table length=" + cls.methTabLength);
				cls.methTable = new Method[cls.methTabLength];
				cls.insertMethods(cls.methTable);
				cls.createIntfCallList();
				InterfaceList list = cls.intfCallList;
				if (list != null) {
					list.sortId();
					if (list.length != 1 || (list.length == 1) && list.getFront().methTabLength != 1) Method.createCompSpecSubroutine("imDelegIiMm"); //imDelegIiMm;
				}
				cls = cls.nextExtLevelClass;
			}
		}
		if (dbg) vrb.println("<generating instance method tables");

		// creates a list with all interfaces this class and its superclasses implement and whose type is checked for
		for (int exl = 0; exl <= Class.maxExtensionLevelStdClasses; exl++) {
			Class cls = Class.extLevelOrdClasses[exl];
			while (cls != null) {
				cls.createIntfTypeChkList();
				cls = cls.nextExtLevelClass;
			}
		}
		// the same for all interfaces
		for (int exl = 0; exl <= Class.maxExtensionLevelInterfaces; exl++) {
			Class intf = Class.extLevelOrdInterfaces[exl];
			while (intf != null) {
				intf.createIntfTypeChkList();
				intf = intf.nextExtLevelClass;
			}
		}

		if (dbg) vrb.println("max ext level std classes = " + Class.maxExtensionLevelStdClasses);
		if (dbg) vrb.println("max ext level interfaces = " + Class.maxExtensionLevelInterfaces);
//		if (dbg) Class.printIntfCallMethods();
//		if (dbg) Class.printInterfaces();
//		if (dbg) Class.printArrays();
//		if (dbg) Class.printConstBlockInterfaces();
//		Class.printClassList("");
		
		Class.releaseLoadingResources();
		log.print("Loading class files ");
		if (errRep.nofErrors == 0) log.println("successfully done"); else log.println("terminated with errors");
	}

	public static void initBuildSystem() {
		if (dbg) vrb.println(">init build system:");

		Class.nofRootClasses = 0;
		Class.prevCpLenth = 0;  Class.constPoolCnt = 0;

		Item.stab = StringTable.getInstance();
		registerWellKnownNames();
		Class cls = new Class(hsClassConstrName);	// insert stub with any name, will be removed at end of build process
		RefType.refTypeList = cls; RefType.refTypeListTail = cls;
		RefType.nofRefTypes = 0;
		Class.nofStdClasses = 0; Class.nofInterfaceClasses = 0; Class.nofInitClasses = 0; Class.nofNonInitClasses = 0; Class.nofArrays = 0;
		Class.initClassesTail = null; Class.nonInitClassesTail = null;
		Class.currInterfaceId = 1;
		Class.currInterfaceChkId = 1;
		Class.constBlockInterfaces = null;

		Type.setUpBaseTypeTable();
		Type.setAttributeTable(Item.stab);

		Method.compSpecSubroutines = null; 
		
		if (dbg) vrb.println("<init build system");
	}

	private static void loadRootClass(String rootClassName, int userReqAttributes) {
		if (dbg) vrb.println(">loadRootClass: " + rootClassName);

		HString hRootClassName = Item.stab.insertCondAndGetEntry(rootClassName);
		Class root = new Class(hRootClassName);
		Class.appendRootClass(root);
		root.accAndPropFlags |= (1<<dpfRootClass);
		assert root.next == null;
		root.loadClass(userReqAttributes);

		if(dbg) vrb.println("<loadRootClass");
	}

	private static void loadSystemClass(Class sysClass, int userReqAttributes) {
		// a system class is already defined in the configuration -> dpfSysPrimitive is set
		assert ((sysClass.accAndPropFlags & (1<<dpfSysPrimitive)) != 0);

		if (dbg) vrb.println(">loadSystemClass: " + sysClass.name);
		if (dbg) {vrb.printf("  sysClsAttributes=0x%1$x", sysClass.accAndPropFlags); Dbg.printAccAndPropertyFlags(sysClass.accAndPropFlags); Dbg.println();}
		
		sysClass.loadClass(userReqAttributes);

		// class = Heap.java, set up new memory method table
		if ((sysClass.accAndPropFlags & (1<<dpfNew)) != 0 ){	
			Method[] sysMethods = Configuration.getOS().getSystemMethods(sysClass);
			int i = 0;
			while (i < sysMethods.length) {
				Method m = sysMethods[i];
				if (m == null) {errRep.error(301, m.name + " in system class " + sysClass.name);
				} else {
					if (dbg) vrb.printf("lsc: method=%1$s, attr=0x%2$x, id=0x%3$x\n", (sysClass.name + "." + m.name), m.accAndPropFlags, m.id);
					int methIndex = 0;
					if (m.id != 0) { // if id == 0 do nothing
						methIndex = (m.id - 1) & 0xFF;
						if (methIndex >= nofNewMethods) {errRep.error(302, m.name + " in system class " + sysClass.name);
						} else {
							if (dbg) vrb.println(" ldSysCls: newMethInx=" + methIndex);
							newMethods[methIndex] = m;
							if (dbg) vrb.printf("lsc: newMethods[%1$d]: %2$s\n", methIndex, m.name);
						}
					}				
				}
				i++;
			}
		}
		if (dbg) vrb.println("<loadSystemClass");
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
