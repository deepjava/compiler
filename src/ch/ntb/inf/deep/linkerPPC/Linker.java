package ch.ntb.inf.deep.linkerPPC;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.IDescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;


public class Linker implements IClassFileConsts, IDescAndTypeConsts {
		
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
	 * <li>of the metods</li>
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
			clazz.targetConstantPoolSize = roundUpToNextWord(c1);
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
			clazz.targetStringPoolSize = roundUpToNextWord(c1);
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
			clazz.classFieldsSizeOnTarget = roundUpToNextWord(c1);
		}
		
		// Methods
		c1 = 0;
		if(clazz.nOfClassFields > 0) {
			Method method = (Method)clazz.methods;
			while(method != null) {
				method.offset = c1;
				c1 += 4;
				method = (Method)method.next;
			}
		}
	}
	
	public static void createMemorySegmentReferences(Class clazz) {
		clazz.codeSegment = Configuration.getCodeSegmentOf(clazz.name);
		clazz.constSegment = Configuration.getConstSegmentOf(clazz.name);
		clazz.varSegment = Configuration.getVarSegmentOf(clazz.name);
		if(clazz.codeSegment == null) reporter.error(550, "Can't get a memory segment for the code of class " + clazz.name + "!");
		if(clazz.constSegment == null) reporter.error(551, "Can't get a memory segment for the constant block of class " + clazz.name + "!");
		if(clazz.varSegment == null) reporter.error(552, "Can't get a memory segment for the static variables of class " + clazz.name + "!");
	}
	
	/**
	 * Creates the constant pool (containing all constant float and double values) for the target.
	 * @param clazz is the class to process
	 */
	public static void createConstantPool(Class clazz) {
		clazz.targetConstantPool = new int[clazz.targetConstantPoolSize/4];
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
	}
	
	
	/**
	 * Creates the string pool (containing all constant stings) for the target.
	 * @param clazz is the class to process
	 */
	public static void createStringPool(Class clazz) {
		clazz.targetStringPool = new int[clazz.targetStringPoolSize/4];
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
	}
	
	
	/**
	 * Creates the class descriptor for the target.<br /><strong>Remember:</strong> the class descriptor contains absolute addresses, therefore  call {@link #calculateAbsoluteAddresses(Class) calculateAbsoluteAddresses} before calling this method!
	 * @param clazz is the class to process
	 */
	public static void createClassDescriptor(Class clazz) {
		
		// Create class descriptor
		int sizeOfClassDescriptor = clazz.nOfMethods + clazz.nOfInterfaces + clazz.nOfBaseClasses + 3;
		clazz.targetClassDescriptor = new int[sizeOfClassDescriptor];
		
		// Insert method addresses
		if(clazz.nOfMethods > 0) {
			Method m = (Method)clazz.methods;
			for(int i = 0; i < clazz.nOfMethods; i++) {
				assert m != null: "ERROR: Method is NULL! Current Method: " + i + "/" + clazz.nOfMethods;
				clazz.targetClassDescriptor[sizeOfClassDescriptor - clazz.nOfMethods + i] = m.offset;
				m = (Method)m.next;
			}
		}
		
		// Insert interfaces
		if(clazz.nOfInterfaces > 0) {
			for(int i = 0; i < clazz.nOfInterfaces; i++) {
				assert clazz.interfaces[i] != null: "ERROR: Interface is NULL! Current Interface: " + i +"/" + clazz.nOfInterfaces;
				clazz.targetClassDescriptor[sizeOfClassDescriptor - clazz.nOfMethods - clazz.nOfInterfaces + i] = clazz.interfaces[i].address;
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
	}
	
	
	/**
	 * Sets the absolute addresses on the target for each method
	 * @param clazz is the class to process
	 */
	public static void calculateAbsoluteAddresses(Class clazz) {
		
		// constant block
		
		
		// variables
		
		
		// code
		Method m = (Method)clazz.methods;
		for(int i = 0; i < clazz.nOfMethods; i++) {
			assert m != null: "ERROR: Method is NULL! Current Method: " + i + "/" + clazz.nOfMethods;
//			TODO: go on here...
/*			if(clazz.codeSegment.tms == null) { // create a new target memory segment
				
			}
				clazz.codeSegment.tms = new TargetMemorySegment(clazz.codeSegment.getBaseAddress(), clazz.codeSegment.getSize());
*/
			m = (Method)m.next;
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
		
		// TODO what should the last entry be? a checksum?
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
	
}
