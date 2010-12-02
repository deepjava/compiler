package ch.ntb.inf.deep.linkerPPC;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;


public class Linker implements ICclassFileConsts, ICdescAndTypeConsts {
		
	private static final int stringHeaderSize = 4; // byte
	private static final ErrorReporter reporter = ErrorReporter.reporter;
	
	public static int sizeInByte = 0;
	public static int sLength = 0;
	
	
	/**
	 * The target image is a list of target memory segments
	 */
	public static TargetMemorySegment targetImage;
	private static TargetMemorySegment lastTargetMemorySegment;

	private static int[] systemTable;

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
			
		// Constant pool
		c1 = 0; // offset counter for the constant floating point numbers (in byte)
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[txFloat]) {
					clazz.constPool[i].offset = c1;
					c1 += Float.SIZE/8;
				}
				else if(clazz.constPool[i].type == Type.wellKnownTypes[txDouble]) {
					clazz.constPool[i].offset = c1;
					c1 += Double.SIZE/8;
				}
			}
			clazz.constantPoolSize = roundUpToNextWord(c1);
		}
		
		// String pool
		c1 = 0; // offset counter for the constant strings (in byte)
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[txString]) {
					clazz.constPool[i].offset = c1;
					int stringSize = ((StringLiteral)clazz.constPool[i]).string.sizeInByte();
					c1 += stringHeaderSize + roundUpToNextWord(stringSize);
				}
			}
			clazz.stringPoolSize = roundUpToNextWord(c1);
		}
		
		// Class/static fields
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
					field.offset = c1; // save offset
					c1 += size; // prepare offset counter for next round
					if(((Type)field.type).category == tcRef) c3++; // count
				}
				else { // instance fields
					c2 = getCorrectOffset(c2, size);
					field.offset = c2;
					c2 += size;
				}
				field = field.next;
			}
			clazz.classFieldsSize = roundUpToNextWord(c1);
			clazz.nOfReferences = c3;
		}
		
		// Methods
		c1 = 0;
		if(clazz.nOfMethods > 0) {
			Method method = (Method)clazz.methods;
			while(method != null) {
				method.offset = c1;
				c1 += 4;
				method = (Method)method.next;
			}
		}
	}
	
	public static void calculateRequiredSize(Class clazz) {
		// machine code size
		Method m = (Method)clazz.methods;
		int codeSize = 0;
		while(m != null) {
			codeSize += m.machineCode.iCount * 4; // iCount = number of instructions!
			m = (Method)m.next;
		}
		clazz.machineCodeSize = codeSize;
		
		// size of class fields --> already set while calculating offsets
		
		// constant block size
		clazz.classDescriptorSize = clazz.nOfMethods + clazz.nOfInterfaces + clazz.nOfBaseClasses + 3;
		clazz.constantBlockSize = 4 // constBlockSize field
								+ 4 // codeBase field
								+ 4 // codeSize field
								+ 4 // varBase field
								+ 4 // clinitAddr field
								+ 4 // nofPtrs field
								+ 4 * clazz.nOfReferences
								+ clazz.classDescriptorSize
								+ clazz.constantPoolSize // size of the constant pool already set while calculating offsets
								+ clazz.stringPoolSize // size of the string pool already set while calculating offsets
								+ 4; // Checksum field
	}
	
	public static void freezeMemoryMap() {
		
		// 1) Calculate required size
		Class c = Type.classList;
		while(c != null) {
			c.codeSegment = Configuration.getCodeSegmentOf(c.name);
			if(c.codeSegment == null) reporter.error(550, "Can't get a memory segment for the code of class " + c.name + "!\n");
			else c.codeSegment.addToRequiredSize(c.machineCodeSize);
			
			c.constSegment = Configuration.getConstSegmentOf(c.name);
			if(c.constSegment == null) reporter.error(551, "Can't get a memory segment for the constant block of class " + c.name + "!\n");
			else c.constSegment.addToRequiredSize(c.constantBlockSize);
			
			c.varSegment = Configuration.getVarSegmentOf(c.name);
			if(c.varSegment == null) reporter.error(552, "Can't get a memory segment for the static variables of class " + c.name + "!\n");
			else c.varSegment.addToRequiredSize(c.classFieldsSize);
						
			c = (Class)c.next;
		}
	
		// 2) Set size for each used segment
		Device d = Configuration.getFirstDevice();
		Segment s;
		while(d != null) {
			System.out.println("Device: " + d.getName() + "\n");
			setSize(d.lastSegment);
			d = d.next;
		}
	}
	
	private static void setSize(Segment s) {
		if(s.lastSubSegment != null) {
			setSize(s.lastSubSegment);
		}
		if(s.getSize() <= 0) {
			s.setSize(s.getRequiredSize());
		}
		else if(s.getSize() < s.getRequiredSize()) { 
			reporter.error(560, "Segment " + s.getName() + " is too small! Size is manually set to " + s.getSize() + " byte, but required size is " + s.getRequiredSize() + " byte!\n");
		}
		System.out.println("  Segment " + s.getName() + ": size = " + s.getSize() + "byte!\n");
		if(s.prev != null) {
			setSize(s.prev);
		}
	}
	
	public static void createConstantBlock(Class clazz) {
		clazz.constantBlock = new int[clazz.constantBlockSize/4];
		
		// 1) Insert Header
		clazz.constantBlock[0] = clazz.constantBlockSize;				// constBlockSize
		clazz.constantBlock[1] = clazz.codeSegment.getBaseAddress();	// codeBase
		clazz.constantBlock[2] = clazz.machineCodeSize;					// codeSize
		clazz.constantBlock[3] = clazz.varSegment.getBaseAddress();		// varBase
		clazz.constantBlock[4] = clazz.classFieldsSize;					// varSize
		clazz.constantBlock[5] = 0;	// TODO Addresse des Klassendeskiptors einfügen
		
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
		// 3a) Insert method addresses
		int classDescriptorOffset = 7 + clazz.nOfReferences;
		if(clazz.nOfMethods > 0) {
			Method m = (Method)clazz.methods;
			for(int i = 0; i < clazz.nOfMethods; i++) {
				assert m != null: "ERROR: Method is NULL! Current Method: " + i + "/" + clazz.nOfMethods;
				clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods - i] = m.offset;
				m = (Method)m.next;
			}
		}
		
		// 3b) Insert interfaces
		if(clazz.nOfInterfaces > 0) {
			for(int i = 0; i < clazz.nOfInterfaces; i++) {
				assert clazz.interfaces[i] != null: "ERROR: Interface is NULL! Current Interface: " + i +"/" + clazz.nOfInterfaces;
				clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods + clazz.nOfInterfaces - i] = clazz.interfaces[i].address;
			}
		}
		
		// 3c) Insert extension level
		clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods + clazz.nOfInterfaces + 1] = clazz.nOfBaseClasses;
		
		// 3d) Insert size
		clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods + clazz.nOfInterfaces + 2] = clazz.objectSizeOrDim;
		
		// 3e) Insert class name address
		clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods + clazz.nOfInterfaces + 3] = 0; // TODO set the right value here!
		
		// 3f) Insert base classes
		if(clazz.nOfBaseClasses > 0) {
			Class bc = (Class)clazz.type;
			for(int i = 0; i < clazz.nOfBaseClasses; i++) {
				assert bc != null: "ERROR: Base class is NULL! Current base class: " + i + "/" + clazz.nOfBaseClasses;
				clazz.constantBlock[classDescriptorOffset + clazz.nOfMethods + clazz.nOfInterfaces + 3 + i] = bc.address;
				bc = (Class)bc.type;
			}
		}
		
		// 4) String pool
		int stringPoolOffset = classDescriptorOffset + clazz.classDescriptorSize / 4;
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
	}
	
	public static void calculateAbsoluteAddresses(Class clazz) {
		int varBase = clazz.varSegment.getBaseAddress();
		int codeBase = clazz.codeSegment.getBaseAddress();
		
		// Class/static fields
		if(clazz.nOfClassFields > 0) {
			Item field = clazz.fields;
			while(field != null) {
				if((field.accAndPropFlags & (1 << apfStatic)) > 1) { // static/class fields
					field.address = varBase + field.offset;
				}
				field = field.next;
			}
		}
		
		// Methods
		if(clazz.nOfMethods > 0) {
			Method method = (Method)clazz.methods;
			while(method != null) {
				method.address += codeBase + method.offset;
				method = (Method)method.next;
			}
		}
	}
		
	/**
	 * Creates the system table for the target
	 * 
	 */
	public static void createSystemTable() {
		int nOfStacks = Configuration.getNumberOfStacks();
		int nOfHeaps = Configuration.getNumberOfHeaps();
		
		// create the systemtable
		systemTable = new int[6 + 2 * nOfStacks + 2 * nOfHeaps + Type.nofClasses];
		
		// offset to the beginning of the class references
		systemTable[0] = 5 + 2 * nOfStacks + 2 * nOfHeaps;
		
		// offset to the beginning of the stack information 
		systemTable[1] = 3;
		
		// offset to the beginning of the
		systemTable[2] = 2 + 2 * nOfStacks;
		
		// number of stacks
		systemTable[3] = nOfStacks;
		
		// reference to each stack and the size of each stack
		for(int i = 0; i < nOfStacks; i++) {
			systemTable[3 + 2 * i] = Configuration.getStackSegments()[i].getBaseAddress();
			systemTable[3 + 2 * i + 1] = Configuration.getStackSegments()[i].getSize();
		}
		
		// number of heaps
		systemTable[4 + 2 * nOfStacks] = nOfHeaps;
		
		//reference to each heap and the size of each heap
		for(int i = 0; i < nOfHeaps; i++) {
			systemTable[4 + 2 * nOfStacks + 2 * i] = Configuration.getHeapSegments()[i].getBaseAddress();
			systemTable[4 + 2 * nOfStacks + 2 * i + 1] = Configuration.getHeapSegments()[i].getSize();
		}
		
		// reference to the constant block of each class
		Class clazz = Type.classList; int i = 6 + 2 * nOfStacks + 2 * nOfHeaps + Type.nofClasses;
		while(clazz != null) {
			systemTable[i] = clazz.address;
			i++;
			clazz = (Class)clazz.next;
		}
		
		// End of system table -> should always be zero!
		systemTable[systemTable.length - 1] = 0;
	}
	
	public static void generateTargetImage() {
		// TODO implement this
	}
	
	public static int getSizeOfObject() {
		// TODO implement this!!!
		return -1;
	}
		
		
	/* ---------- private helper methods ---------- */
	
	private static int getCorrectOffset(int potentialOffset, int size) {
		if(size == 8) size = 4; // handle double word items like word items
		if(potentialOffset % size == 0) return potentialOffset;
		return potentialOffset + (4 - (potentialOffset % size));
	}
	
	private static int roundUpToNextWord(int val) {
		if(val % 4 == 0) return val;
		return val + (4 - (val % 4));
	}
	
	
	/* ---------- debug primitives ---------- */
	
	
	
	/* ---------- this methods were no longer used -> TODO delete it! ---------- */
	
	/**
	 * Creates the constant pool (containing all constant float and double values) for the target.
	 * @param clazz is the class to process
	 */
/*	public static void createConstantPool(Class clazz) {
		clazz.targetConstantPool = new int[clazz.constantPoolSize/4];
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/4;
				if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txFloat]) {
					clazz.targetConstantPool[index] = ((Constant)clazz.constPool[i]).valueH;
				}
				else if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txDouble]) {
					clazz.targetConstantPool[index] = ((Constant)clazz.constPool[i]).valueH;
					clazz.targetConstantPool[index + 1] = ((Constant)clazz.constPool[i]).valueL;
				}
			}
		}
	} */

	/**
	 * Creates the string pool (containing all constant stings) for the target.
	 * @param clazz is the class to process
	 */
/*	public static void createStringPool(Class clazz) {
		clazz.targetStringPool = new int[clazz.stringPoolSize/4];
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/4;
				if(clazz.constPool[i].type == Type.wellKnownTypes[txString]) {
					HString s = ((StringLiteral)clazz.constPool[i]).string;
					sizeInByte = s.sizeInByte();
					sLength = s.length();
					if(s.sizeInByte()/s.length() == 1) { // string is a H8String
						int c = 0, word = 0;
						clazz.targetStringPool[index++] = (s.length() << 16) + 0x0800; // add string header
						for(int j = 0; j < s.length(); j++) {
							word = (word << 8) + s.charAt(j);
							c++;
							if(c > 3 || j == s.length() - 1) {
								clazz.targetStringPool[index] = word;
								c = 0;
								word = 0;
								index++;
							}
						}
					}
					else { // string is a H16String
						assert s.sizeInByte()/s.length() == 2: "String is neighter a 8bit nor a 16bit char array!";
						int c = 0, word = 0;
						clazz.targetStringPool[index++] = (s.length() << 16) + 0x1000; // add string header
						for(int j = 0; j < s.length(); j++) {
							word = (word << 16) + s.charAt(j);
							c++;
							if(c > 1 || j == s.length() - 1) {
								clazz.targetStringPool[index] = word;
								c = 0;
								word = 0;
								index++;
							}
						}
					}	
				}
			}
		}
	} */

	/**
	 * Creates the class descriptor for the target.<br /><strong>Remember:</strong> the class descriptor contains absolute addresses, therefore  call {@link #calculateAbsoluteAddresses(Class) calculateAbsoluteAddresses} before calling this method!
	 * @param clazz is the class to process
	 */
/*	public static void createClassDescriptor(Class clazz) {
		
		// Create class descriptor
		clazz.targetClassDescriptor = new int[clazz.classDescriptorSize];
		
		// Insert method addresses
		if(clazz.nOfMethods > 0) {
			Method m = (Method)clazz.methods;
			for(int i = 0; i < clazz.nOfMethods; i++) {
				assert m != null: "ERROR: Method is NULL! Current Method: " + i + "/" + clazz.nOfMethods;
				clazz.targetClassDescriptor[clazz.classDescriptorSize - clazz.nOfMethods + i] = m.offset;
				m = (Method)m.next;
			}
		}
		
		// Insert interfaces
		if(clazz.nOfInterfaces > 0) {
			for(int i = 0; i < clazz.nOfInterfaces; i++) {
				assert clazz.interfaces[i] != null: "ERROR: Interface is NULL! Current Interface: " + i +"/" + clazz.nOfInterfaces;
				clazz.targetClassDescriptor[clazz.classDescriptorSize - clazz.nOfMethods - clazz.nOfInterfaces + i] = clazz.interfaces[i].address;
			}
		}
		
		// Insert extension level
		clazz.targetClassDescriptor[clazz.nOfMethods - clazz.nOfInterfaces - 1] = clazz.nOfBaseClasses;
		
		// Insert size
		clazz.targetClassDescriptor[clazz.nOfMethods - clazz.nOfInterfaces - 2] = clazz.objectSizeOrDim;
		
		// Insert base classes
		if(clazz.nOfBaseClasses > 0) {
			Class bc = (Class)clazz.type;
			for(int i = 0; i < clazz.nOfBaseClasses; i++) {
				assert bc != null: "ERROR: Base class is NULL! Current base class: " + i + "/" + clazz.nOfBaseClasses;
				clazz.targetClassDescriptor[i] = bc.address;
				bc = (Class)bc.type;
			}
		}
	} */
	
}
