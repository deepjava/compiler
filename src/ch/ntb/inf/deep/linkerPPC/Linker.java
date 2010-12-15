package ch.ntb.inf.deep.linkerPPC;

import java.io.PrintStream;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.IAttributes;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;


public class Linker implements ICclassFileConsts, ICdescAndTypeConsts, IAttributes {
	
	public static int sizeInByte = 0;
	public static int sLength = 0;

	public static TargetMemorySegment targetImage;
	private static TargetMemorySegment lastTargetMemorySegment;

	private static final int stringHeaderSize = 4; // byte
	private static final ErrorReporter reporter = ErrorReporter.reporter;

	private static PrintStream vrb = System.out;
	private static Class object;
	
	private static Segment[] usedSegments = new Segment[100]; // TODO remove this
	private static int usedSegmentsIndex = 0; // TODO remove this
	
	private static int[] systemTable;
	private static int systemTableSize;

	/**
	 * Calculates the offsets... <ul>
	 * <li>of the constant float and double values in the constant pool</li>
	 * <li>of the constant stings in the string pool</li>
	 * <li>of the class/static fields</li>
	 * <li>of the instance fields</li>
	 * <li>of the methods</li>
	 * from the given class.
	 * </ul>
	 * @param clazz is the class to process
	 */
	public static void calculateOffsets(Class clazz) {
		int c1, c2, c3;
		
		vrb.println("[LINKER] START: Calculating offsets for class \"" + clazz.name +"\":\n");
		
		// Constant pool
		vrb.println("  1) Constant pool:");
		c1 = 0; // offset counter for the constant floating point numbers (in byte)
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[txFloat]) {
					vrb.println("    constPool[" + i + "] Type: float, Offset: " + c1);
					clazz.constPool[i].offset = c1;
					c1 += Float.SIZE/8;
				}
				else if(clazz.constPool[i].type == Type.wellKnownTypes[txDouble]) {
					vrb.println("    constPool[" + i + "] Type: double, Offset: " + c1);
					clazz.constPool[i].offset = c1;
					c1 += Double.SIZE/8;
				}
			}
			clazz.constantPoolSize = roundUpToNextWord(c1);
		}
		
		// String pool
		vrb.println("  2) String pool:");
		c1 = 0; // offset counter for the constant strings (in byte)
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[txString]) {
					vrb.println("    constPool[=" + i + "] Type: String, Offset: " + c1);
					clazz.constPool[i].offset = c1;
					int stringSize = ((StringLiteral)clazz.constPool[i]).string.sizeInByte();
					c1 += stringHeaderSize + roundUpToNextWord(stringSize);
				}
			}
			clazz.stringPoolSize = roundUpToNextWord(c1);
		}
		
		// Class/static fields
		vrb.println("  3) Class fields:");
		c1 = 0; // offset counter for static fields (in byte)
		c2 = 0; // offset counter for instance fields (in byte)
		c3 = 0; // counter for the number or static fields which are references to objects
		if(clazz.nOfClassFields > 0 || clazz.nOfInstanceFields > 0) {
			Item field = clazz.fields;
			int size;
			while(field != null) {
				size = ((Type)field.type).sizeInBits; // size in bits
				if(size < 8) size = 1; // use one byte even if the size is smaller (e.g. for a boolean)
				else size /= 8; // bits -> byte
				if((field.accAndPropFlags & (1 << apfStatic)) > 1) { // static/class fields
					c1 = getCorrectOffset(c1, size);
					vrb.println("    Name: " + field.name + ", Static: yes, Type: " + field.type.name + ",Offset: " + c1);
					field.offset = c1; // save offset
					c1 += size; // prepare offset counter for next round
					if(((Type)field.type).category == tcRef) c3++; // count
				}
				else { // instance fields
					c2 = getCorrectOffset(c2, size);
					vrb.println("    Name: " + field.name + ", Static: no, Type: " + field.type.name + ",Offset: " + c2);
					field.offset = c2;
					c2 += size;
				}
				field = field.next;
			}
			clazz.classFieldsSize = roundUpToNextWord(c1);
			clazz.nOfReferences = c3;
		}
		
		// Methods
		vrb.println("  4) Methods:");
		c1 = 0;
		if(clazz.nOfMethods > 0) {
			Method method = (Method)clazz.methods;
			while(method != null) {
				vrb.println("    Name: " + method.name + ", Index: " + c1);
				method.index = c1;
				c1 += 4;
				method = (Method)method.next;
			}
		}
		
		vrb.println("\n[LINKER] END: calculating offsets for class \"" + clazz.name +"\"\n");
	}
	
	public static void calculateRequiredSize(Class clazz) {
		
		vrb.println("[LINKER] START: Calculating required size for class \"" + clazz.name +"\":\n");
		
		// machine code size
		vrb.print("  1) Code:");
		Method m = (Method)clazz.methods;
		int codeSize = 0;
		while(m != null) {
			if(m.machineCode != null) {
				if(m.offset <= 0) {
					m.offset = codeSize;
					codeSize += m.machineCode.iCount * 4; // iCount = number of instructions!
				}
				else {
					if(codeSize < m.machineCode.iCount * 4 + m.offset) codeSize = m.machineCode.iCount * 4 + m.offset;
				}
				vrb.println("    > " + m.name + ": codeSize = " + m.machineCode.iCount * 4 + " byte");
			}
			m = (Method)m.next;
		}
		clazz.machineCodeSize = codeSize;
		vrb.println("    Total code size: " + codeSize + " byte");
		
		// size of class fields --> already set while calculating offsets
		
		vrb.print("  2) Constant block: ");
		// constant block size
		clazz.classDescriptorSize = (clazz.nOfInstanceMethods + clazz.nOfInterfaces + clazz.nOfBaseClasses + 3) * 4;
		clazz.constantBlockSize = 4 // constBlockSize field
								+ 4 // codeBase field
								+ 4 // codeSize field
								+ 4 // varBase field
								+ 4 // varSize field
								+ 4 // clinitAddr field
								+ 4 // nofPtrs field
								+ 4 * clazz.nOfReferences
								+ clazz.classDescriptorSize
								+ clazz.constantPoolSize // size of the constant pool already set while calculating offsets
								+ clazz.stringPoolSize // size of the string pool already set while calculating offsets
								+ 4; // Checksum field
		
		vrb.println(clazz.constantBlockSize + " byte");
		
		vrb.println("\n[LINKER] END: Calculating required size for class \"" + clazz.name +"\"\n");
	}
	
	public static void freezeMemoryMap() {
	
		systemTableSize = 8 + 2 * Configuration.getNumberOfStacks() + 2 * Configuration.getNumberOfHeaps() + Type.nofClasses; // TODO @Martin this should'nt be here! Move it to a better place...
		
		// 1) Set a segment for the code, the static fields and the constant block for each class
		Class c = Type.classList;
		Segment s;
		while(c != null) {
						
			// Code
			s = Configuration.getCodeSegmentOf(c.name);
			if(s == null) reporter.error(550, "Can't get a memory segment for the code of class " + c.name + "!\n");
			else {
				if(s.subSegments != null) s = getFirstFittingSegment(s.subSegments, atrCode, c.machineCodeSize);
				c.codeOffset = s.getRequiredSize(); // TODO better to use a own counter for this?
				s.addToRequiredSize(c.machineCodeSize); // TODO move this to getFirstFittingSegment and rename the method
				c.codeSegment = s;
			}
			
			// Var
			s = Configuration.getVarSegmentOf(c.name);
			if(s == null) reporter.error(551, "Can't get a memory segment for the static variables of class " + c.name + "!\n");
			else {
				if(s.subSegments != null) s = getFirstFittingSegment(s, atrVar, c.classFieldsSize);
				c.varOffset = s.getRequiredSize(); // TODO better to use a own counter for this?
				s.addToRequiredSize(c.classFieldsSize); // TODO move this to getFirstFittingSegment and rename the method
				c.varSegment = s;
			}
			
			// Const
			s = Configuration.getConstSegmentOf(c.name);
			if(s == null) reporter.error(552, "Can't get a memory segment for the constant block of class " + c.name + "!\n");
			else {
				if(s.subSegments != null) s = getFirstFittingSegment(s, atrConst, c.constantBlockSize);
				c.constOffset = s.getRequiredSize(); // TODO better to use a own counter for this?
				s.addToRequiredSize(c.constantBlockSize); // TODO move this to getFirstFittingSegment and rename the method
				c.constSegment = s;
				
			}		
			
			c = (Class)c.next;
		}
		Segment[] sysTabs = Configuration.getSysTabSegments();
		if(sysTabs != null) {
			for(int i = 0; i < sysTabs.length; i++) {
				sysTabs[i].addToRequiredSize(systemTableSize * 4); // TODO @Martin: don't use the same size for each system table!
			}
		}
		else reporter.error(9445, "Fatal Error: no Systemtable defined!\n"); // TODO define drror number
	
		// 2) Check and set the size for each used segment
		Device d = Configuration.getFirstDevice();
		while(d != null) {
//			System.out.println("Device: " + d.getName() + "\n");
			if(d.lastSegment != null) setSegmentSize(d.lastSegment);
			d = d.next;
		}
		
		// 3) Set base addresses for each used segment
		d = Configuration.getFirstDevice();
		//usedSegments = new Segment[nOfUsedSegments];
		while(d != null) {
			vrb.println("Start setting base addresses for segments in device \"" + d.getName() +"\":");
			//System.out.println("Device: " + d.getName() + "\n");
			if(d.segments != null) setBaseAddress(d.segments, d.getbaseAddress());
			vrb.println("End setting base addresses for segments in device \"" + d.getName() +"\":\n");		
			d = d.next;
		}
		vrb.println("USED SEGMENTS:");
		for(int i = 0; i < usedSegmentsIndex; i++) {
			vrb.println("  > [" + i + "] " + usedSegments[i].getName());
		}
	}
	
	public static void calculateAbsoluteAddresses(Class clazz) {
		vrb.println("\n[LINKER] START: Calculating absolute addresses for class \"" + clazz.name +"\":\n");
		
		int varBase = clazz.varSegment.getBaseAddress() + clazz.varOffset;
		int codeBase = clazz.codeSegment.getBaseAddress() + clazz.codeOffset;
		
		// Class/static fields
		if(clazz.nOfClassFields > 0) {
			Item field = clazz.fields;
			vrb.println("  Static fields:");
			while(field != null) {
				if((field.accAndPropFlags & (1 << apfStatic)) > 1) { // static/class fields
					if(varBase != -1 && field.offset != -1) field.address = varBase + field.offset;
					vrb.print("    > " + field.name + ": Offset = " + field.offset + ", Address = 0x" + Integer.toHexString(field.address) + "\n");
				}
				field = field.next;
			}
		}
		
		// Methods
		if(clazz.nOfMethods > 0) {
			Method method = (Method)clazz.methods;
			vrb.println("  Methods:");
			while(method != null) {
				if(codeBase != -1 && method.offset != -1) method.address = codeBase + method.offset;
				vrb.print("    > " + method.name + ": Offset = " + method.offset + ", Address = 0x" + Integer.toHexString(method.address) + "\n");
				method = (Method)method.next;
			}
		}
		vrb.println("\n[LINKER] END: Calculating absolute addresses for class \"" + clazz.name +"\"\n");
		
	}
		
	public static void createConstantBlock(Class clazz) {
		
		vrb.println("[LINKER] START: Creating constant block for class \"" + clazz.name +"\":\n");
		
		clazz.constantBlock = new int[clazz.constantBlockSize/4];
		
		vrb.println("  Constant block size: " + clazz.constantBlockSize + " byte -> array size: " + clazz.constantBlock.length);
		vrb.println("  Number of references: " + clazz.nOfReferences);
		vrb.println("  Class descriptor size: " + clazz.classDescriptorSize + " byte");
		vrb.println("  Number of instance methods: " + clazz.nOfInstanceMethods);
		vrb.println("  Number of interfaces: " + clazz.nOfInterfaces);
		
		
		// 1) Insert Header
		clazz.constantBlock[0] = clazz.constantBlockSize;								// constBlockSize
		clazz.constantBlock[1] = clazz.codeSegment.getBaseAddress() + clazz.codeOffset;	// codeBase
		clazz.constantBlock[2] = clazz.machineCodeSize;									// codeSize
		clazz.constantBlock[3] = clazz.varSegment.getBaseAddress() + clazz.varOffset;	// varBase
		clazz.constantBlock[4] = clazz.classFieldsSize;									// varSize
		Method clinit = clazz.getClassConstructor();									// clinitAddr
		if(clinit != null) clazz.constantBlock[5] = clinit.address;
		else clazz.constantBlock[5] = -1; // TODO @Urs is this ok?
		
		// 2) Insert References (Pointers)
		clazz.constantBlock[6] = clazz.nOfReferences;
		if(clazz.nOfReferences > 0) {
			Item field = clazz.fields;
			int index = 0;
			while(field != null) {
				if((field.accAndPropFlags & (1 << apfStatic)) != 0 && ((Type)field.type).category == tcRef)
					clazz.constantBlock[7 + index] = field.address;
				field = field.next;
			}
		}
		
		
		// 3) Class descriptor 
		// 3a) Insert instance method addresses
		int classDescriptorOffset = 6 + clazz.nOfReferences;
		int imc = 0;
		if(clazz.nOfInstanceMethods > 0) {
			Method m = (Method)clazz.methods;
			while(m != null) {
				if((m.accAndPropFlags & (1 << apfStatic)) == 0 && (m.accAndPropFlags & (1 << dpfSynthetic)) == 0) {
					clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods - imc] = m.address;
					imc++;
				}
				m = (Method)m.next;
			}
		}
		
		// 3b) Insert interfaces
		if(clazz.nOfInterfaces > 0) {
			for(int i = 0; i < clazz.nOfInterfaces; i++) {
				assert clazz.interfaces[i] != null: "ERROR: Interface is NULL! Current Interface: " + i +"/" + clazz.nOfInterfaces;
				clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods + clazz.nOfInterfaces - i] = clazz.interfaces[i].address;
			}
		}
		
		// 3c) Insert extension level
		clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods + clazz.nOfInterfaces + 1] = clazz.nOfBaseClasses;
		
		// 3d) Insert size
		clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods + clazz.nOfInterfaces + 2] = clazz.objectSizeOrDim;
		
		// 3e) Insert class name address
		clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods + clazz.nOfInterfaces + 3] = 0x12345678; // TODO set the right value here!
		
		// 3f) Insert base classes
		if(clazz.nOfBaseClasses > 0) {
			Class bc = (Class)clazz.type;
			for(int i = 0; i < clazz.nOfBaseClasses; i++) {
				assert bc != null: "ERROR: Base class is NULL! Current base class: " + i + "/" + clazz.nOfBaseClasses;
				clazz.constantBlock[classDescriptorOffset + clazz.nOfInstanceMethods + clazz.nOfInterfaces + 3 + i] = bc.address;
				bc = (Class)bc.type;
			}
		}
		
		// 4) String pool
		int stringPoolOffset = classDescriptorOffset + clazz.classDescriptorSize / 4 + 1;
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/4;
				if(clazz.constPool[i].type == Type.wellKnownTypes[txString]) {
					HString s = ((StringLiteral)clazz.constPool[i]).string;
					sizeInByte = s.sizeInByte();
					sLength = s.length();
					if(s.sizeInByte()/s.length() == 1) { // string is a H8String
						int c = 0, word = 0;
						clazz.constantBlock[stringPoolOffset + index++] = (s.length() << 16) + 0x0800; // add string header
						for(int j = 0; j < s.length(); j++) {
							word = (word << 8) + s.charAt(j);
							c++;
							if(c > 3 || j == s.length() - 1) {
								clazz.constantBlock[stringPoolOffset + index] = word;
								c = 0;
								word = 0;
								index++;
							}
						}
					}
					else { // string is a H16String
						assert s.sizeInByte()/s.length() == 2: "String is neighter a 8bit nor a 16bit char array!";
						int c = 0, word = 0;
						clazz.constantBlock[stringPoolOffset + index++] = (s.length() << 16) + 0x1000; // add string header
						for(int j = 0; j < s.length(); j++) {
							word = (word << 16) + s.charAt(j);
							c++;
							if(c > 1 || j == s.length() - 1) {
								clazz.constantBlock[stringPoolOffset + index] = word;
								c = 0;
								word = 0;
								index++;
							}
						}
					}	
				}
			}
		}
	
		// 5) Constant pool
		int constantPoolOffset = stringPoolOffset + clazz.stringPoolSize / 4;
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/4;
				if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txFloat]) {
					clazz.constantBlock[constantPoolOffset + index] = ((Constant)clazz.constPool[i]).valueH;
				}
				else if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txDouble]) {
					clazz.constantBlock[constantPoolOffset + index] = ((Constant)clazz.constPool[i]).valueH;
					clazz.constantBlock[constantPoolOffset + index + 1] = ((Constant)clazz.constPool[i]).valueL;
				}
			}
		}
		
		// 6 Checksum
		clazz.constantBlock[clazz.constantBlock.length - 1] = 0; // TODO implement crc32 checksum
		
		// print
	//	clazz.printConstantBlock();
		
		vrb.println("\n[LINKER] END: Creating constant block for class \"" + clazz.name +"\"\n");
	}

	/**
	 * Creates the system table for the target
	 * 
	 */
	public static void createSystemTable() {
		
		vrb.println("[LINKER] START: Creating systemtable:\n");
		
		int nOfStacks = Configuration.getNumberOfStacks();
		int nOfHeaps = Configuration.getNumberOfHeaps();
		
		vrb.println("  Number of stacks: " + nOfStacks);
		vrb.println("  Number of heaps: " + nOfHeaps);
		vrb.println("  Number of classes: " + Type.nofClasses);
		
		
		// create the systemtable
		systemTable = new int[systemTableSize];
		
		vrb.println("  Size of the system table: " + systemTable.length * 4 + " byte  -> array size: " + systemTable.length);
		
		
		// offset to the beginning of the class references
		systemTable[0] = 6 + 2 * nOfStacks + 2 * nOfHeaps;
		
		// offset to the beginning of the stack information 
		systemTable[1] = 5;
		
		// offset to the beginning of the heap information
		systemTable[2] = 5 + 2 * nOfStacks;
		
		Item c = Type.classList;
		
		HString kernelClassName = Configuration.getKernelClassname();
		Item kernelClinit = null;
		int kernelClinitAddr = 0;
		while(c != null && !c.name.equals(kernelClassName)) {
			c = c.next;
		}
		if(c != null) {
//			System.out.println("Kernel class name: " + c.name);
		//	kernelClinit = ((Class)c).methods.getItemByName("<clinit>");
			kernelClinit = ((Class)c).getClassConstructor();
			if(kernelClinit != null) {
//				System.out.println("kernelClinit: " + kernelClinit.name);
				kernelClinitAddr = kernelClinit.address;
			}
		}
		systemTable[3] = kernelClinitAddr;
		
		// number of stacks
		systemTable[4] = nOfStacks;
		
		// reference to each stack and the size of each stack
		for(int i = 0; i < nOfStacks; i++) {
			systemTable[5 + 2 * i] = Configuration.getStackSegments()[i].getBaseAddress();
			systemTable[5 + 2 * i + 1] = Configuration.getStackSegments()[i].getSize();
		}
		
		// number of heaps
		systemTable[5 + 2 * nOfStacks] = nOfHeaps;
		
		//reference to each heap and the size of each heap
		for(int i = 0; i < nOfHeaps; i++) {
			systemTable[6 + 2 * nOfStacks + 2 * i] = Configuration.getHeapSegments()[i].getBaseAddress();
			systemTable[6 + 2 * nOfStacks + 2 * i + 1] = Configuration.getHeapSegments()[i].getSize();
		}
		
		systemTable[7 + 2 * nOfStacks + 2 * nOfHeaps] = Type.nofClasses;
		
		// reference to the constant block of each class
		Class clazz = Type.classList; int i = 7 + 2 * nOfStacks + 2 * nOfHeaps;
		int offset = 0;
		while(clazz != null) {
			systemTable[i] = clazz.constSegment.getBaseAddress() + offset;
			offset += clazz.constantBlockSize;
//			System.out.println("       Class: " + clazz.name + " -> Index: " + i);
			i++;
			clazz = (Class)clazz.next;
		}
		
		// End of system table -> should always be zero!
		systemTable[systemTable.length - 1] = 0;
		
		vrb.println("[LINKER] END: Creating systemtable\n");
	}
	
	public static void generateTargetImage() {
		
		vrb.println("[LINKER] START: Generating target image:\n");
		
		Class clazz = Type.classList;
		Method m;
		while(clazz != null) {
			vrb.println("  Proceeding class \"" + clazz.name + "\":");
			// code
			m = (Method)clazz.methods;
			vrb.println("    1) Code:");
			while(m != null) {
				if(m.machineCode != null) {
					vrb.println("         > Method \"" + m.name + "\":");
					clazz.codeSegment.tms.addData(clazz.codeSegment.getBaseAddress() + clazz.codeOffset + m.offset, m.machineCode.instructions);
					addTargetMemorySegment(clazz.codeSegment.tms);
				}
				m = (Method)m.next;
			}
			
			// consts
			vrb.println("    2) Constantblock:");
			clazz.constSegment.tms.addData(clazz.constSegment.getBaseAddress() + clazz.constOffset, clazz.constantBlock);
			addTargetMemorySegment(clazz.constSegment.tms);
			
			clazz = (Class)clazz.next;
		}

		vrb.println("  Proceeding system table:");
		Segment[] s = Configuration.getSysTabSegments();
		s[0].tms.addData(s[0].getBaseAddress(), systemTable);
		addTargetMemorySegment(s[0].tms);
		
		vrb.println("[LINKER] END: Generating target image\n");
	}
	
	public static int getSizeOfObject() {
		if(object == null) {
			Class clazz = Class.classList;
			while(clazz != null && clazz.name != HString.getHString("java/lang/Object")) {
				clazz = (Class)clazz.next;
			}
			object = clazz;
		}
		return object.objectSizeOrDim;
	}
	
	
	/* ---------- private helper methods ---------- */
	
	private static void setBaseAddress(Segment s, int baseAddress) {
		//descend
		if(s.subSegments != null) setBaseAddress(s.subSegments, baseAddress);
		//set baseaddress
		if((s.getSize() > 0 && s.getRequiredSize() > 0) || ((s.getAttributes() & ((1 << atrStack) | (1 << atrHeap) | (1 << atrSysTab))) != 0)){ 
			s.setBaseAddress(baseAddress);
			usedSegments[usedSegmentsIndex++] = s; // TODO remove this
			s.tms = new TargetMemorySegment(s.getBaseAddress(), s.getSize());
			vrb.println("\t Segment "+s.getName() +" address = "+ Integer.toHexString(baseAddress) + ", size = " + s.getSize());
		}
		// traverse from left to right
		if(s.next != null) setBaseAddress(s.next, s.getSize()+ baseAddress);
	}

	private static Segment getFirstFittingSegment(Segment s, byte contentAttribute, int requiredSize) {
		Segment t = s;
		while(t != null) {
			if((t.getAttributes() & (1 << contentAttribute)) != 0) {
				if(t.subSegments != null) t = getFirstFittingSegment(t.subSegments, contentAttribute, requiredSize);
				if(t.getSize() <= 0 || t.getSize() - t.getRequiredSize() > requiredSize) return t;
			}
			t = t.next;
		}
		return null;
	}

	private static void setSegmentSize(Segment s) {
		if(s.lastSubSegment != null) {
			setSegmentSize(s.lastSubSegment);
		}
		if(s.getSize() <= 0) {
			s.setSize(roundUpToNextWord(s.getRequiredSize()));
		}
		else if(s.getSize() < s.getRequiredSize()) { 
			reporter.error(560, "Segment " + s.getName() + " is too small! Size is manually set to " + s.getSize() + " byte, but required size is " + s.getRequiredSize() + " byte!\n");
		}
//		System.out.println("  Segment " + s.getName() + ": size = " + s.getSize() + "byte!\n");
		if(s.prev != null) {
			setSegmentSize(s.prev);
		}
	}
	
	private static int getCorrectOffset(int potentialOffset, int size) {
		if(size == 8) size = 4; // handle double word items like word items
		if(potentialOffset % size == 0) return potentialOffset;
		return potentialOffset + (4 - (potentialOffset % size));
	}
	
	private static int roundUpToNextWord(int val) {
		if(val % 4 == 0) return val;
		return val + (4 - (val % 4));
	}
	
	private static void addTargetMemorySegment(TargetMemorySegment tms) {
		if(targetImage == null) {
			targetImage = tms;
			lastTargetMemorySegment = tms;
		}
		else {
			TargetMemorySegment current = targetImage;
			while(current != null) {
				if(current == tms) return;
				current = current.next;
			}
			lastTargetMemorySegment.next = tms;
			lastTargetMemorySegment = lastTargetMemorySegment.next;
		}
	}
	
	
	/* ---------- debug primitives ---------- */
	
	public static void printSystemTable() {
		int i = 0;
		int nOfStacks = systemTable[4];
		int nOfHeaps = systemTable[5 + 2 * nOfStacks];
		int nOfClasses = Type.nofClasses;
		vrb.print("System table:\n");
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] classConstOffset\n"); i++;
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] stackOffset\n"); i++;
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] heapOffset\n"); i++;
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] kernelClinitAddr\n"); i++;
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] nofStacks\n"); i++;
		for(int j = 0; j < nOfStacks; j++) {
			vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] baseStack" + j + "\n"); i++;
			vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] sizeStack" + j + "\n"); i++;
		}
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] nofHeaps\n"); i++;
		for(int j = 0; j < nOfHeaps; j++) {
			vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] baseHeap" + j + "\n"); i++;
			vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] sizeHeap" + j + "\n"); i++;
		}
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] nofClasses\n"); i++;
		for(int j = 0; j < nOfClasses; j++) {
			vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] constBlkBaseClass" + j + "\n"); i++;
		}
		vrb.printf("  >%4d", i); vrb.print(" ["); vrb.printf("%8x", systemTable[i]); vrb.print("] endOfSysTab\n"); i++;
	}

	public static void printTargetImage() {
		TargetMemorySegment tms = targetImage;
		while(tms != null) {
			vrb.print(tms);
			tms = tms.next;
		}
	}

	public static void printClassList() {
		vrb.println("\n[LINKER] PRINT: This is a list of all classes with their methodes, fields and constant blocks\n");
		Class c = Type.classList;
		Method m;
		Item f;
		int cc = 0, mc = 0, fc = 0;
		while(c != null) {
			vrb.println("  Class: " + c.name + " (#" + cc++ + ")");
			vrb.println("    Number of class methods: " + c.nOfClassMethods);
			vrb.println("    Number of instance methods: " + c.nOfInstanceMethods);
			vrb.println("    Number of class fields: " + c.nOfClassFields);
			vrb.println("    Number of instance fields: " + c.nOfInstanceFields);
			vrb.println("    Number of interfaces: " + c.nOfInterfaces);
			vrb.println("    Number of base classes: " + c.nOfBaseClasses);
			vrb.println("    Number of references: " + c.nOfReferences);
			vrb.println("    Machine code size: " + c.machineCodeSize + " byte");
			vrb.println("    Constant block size: " + c.constantBlockSize + " byte");
			vrb.println("    Class fields size: " + c.classFieldsSize + " byte");
			vrb.println("    Code offset: 0x" + Integer.toHexString(c.codeOffset));
			vrb.println("    Var offset: 0x" + Integer.toHexString(c.varOffset));
			vrb.println("    Const offset: 0x" + Integer.toHexString(c.constOffset));
			vrb.println("    Code segment: " + c.codeSegment.getName() + " (Base address: 0x" + Integer.toHexString(c.codeSegment.getBaseAddress()) + ", size: " + c.codeSegment.getSize() + " byte)");
			vrb.println("    Var segment: " + c.varSegment.getName() + " (Base address: 0x" + Integer.toHexString(c.varSegment.getBaseAddress()) + ", size: " + c.varSegment.getSize() + " byte)");
			vrb.println("    Const segment: " + c.constSegment.getName() + " (Base address: 0x" + Integer.toHexString(c.constSegment.getBaseAddress()) + ", size: " + c.constSegment.getSize() + " byte)");
			
			vrb.println("    Method list:");
			m = (Method)c.methods;
			mc = 0;
			if(m == null) vrb.println("      No methods in this class");
			else {
				while(m != null) {
					vrb.println("      > Method: " + m.name + " (#" + mc++ + ")");
					vrb.println("        Access and property flags: 0x" + Integer.toHexString(m.accAndPropFlags));
					vrb.println("        Absolute address: 0x" + Integer.toHexString(m.address));
					vrb.println("        Address offset: 0x" + Integer.toHexString(m.offset));
					vrb.println("        CD offset: 0x" + Integer.toHexString(m.index));
					m = (Method)m.next;
				}
			}
			
			vrb.println("    Field list:");
			f = c.fields;
			fc = 0;
			if(f == null) vrb.println("      No fields in this class");
			else {
				while(f != null) {
					vrb.println("      > Field: " + f.name + " (#" + fc++ + ")");
					vrb.println("        Access and property flags: 0x" + Integer.toHexString(f.accAndPropFlags));
					vrb.println("        Absolute address: 0x" + Integer.toHexString(f.address));
					vrb.println("        Address offset: 0x" + Integer.toHexString(f.offset));
					f = f.next;
				}
			}
			
			vrb.println("    Constant block:");
			c.printConstantBlock(2);
			
			c = (Class)c.next;
			
			vrb.println("  ----------------------------------------------------------------------");
		}
		
		vrb.println("\n[LINKER] PRINT: End of class list\n");
	}

}
