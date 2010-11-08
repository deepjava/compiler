package ch.ntb.inf.deep.linkerPPC;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.strings.HString;


public class Linker implements IClassFileConsts{
		
	private static final int stringHeaderSize = 32;
	
	
	/**
	 * The target image is a list of target memory segments
	 */
	public static TargetMemorySegment targetImage;
	private static TargetMemorySegment lastTargetMemorySegment;


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
		int offsetCounter1, offsetCounter2;
			
		// Constant pool
		offsetCounter1 = 0;
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txFloat]) {
					clazz.constPool[i].offset = offsetCounter1;
					offsetCounter1 += Float.SIZE;
				}
				else if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txDouble]) {
					clazz.constPool[i].offset = offsetCounter1;
					offsetCounter1 += Double.SIZE;
				}
			}
			clazz.targetConstantPoolSize = roundUpToNextWord(offsetCounter1);
		}
		
		// String pool
		offsetCounter1 = 0;
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txString]) {
					clazz.constPool[i].offset = offsetCounter1;
					int stringSize = ((StringLiteral)clazz.constPool[i]).string.sizeInBits();
					//if(stringSize % 32 > 0) stringSize += 32 - (stringSize % 32);
					stringSize = roundUpToNextWord(stringSize);
					offsetCounter1 += stringHeaderSize + stringSize;
				}
			}
			clazz.targetStringPoolSize = roundUpToNextWord(offsetCounter1);
		}
		
		// Class/static fields
		offsetCounter1 = 0; // used for static fields
		offsetCounter2 = 0; // used for instance fields
		if(clazz.nOfClassFields > 0 || clazz.nOfInstanceFields > 0) {
			Item field = clazz.fields;
			int size;
			while(field != null) {
				size = ((Type)field.type).sizeInBits;
				if(size < 8) size = 8; // use a byte for a boolean
				if((field.accAndPropFlags & (1 << apfStatic)) > 1) {
					offsetCounter1 = getCorrectOffset(offsetCounter1, size);
					field.offset = offsetCounter1;
					offsetCounter1 += size;
				}
				else {
					offsetCounter2 = getCorrectOffset(offsetCounter2, size);
					field.offset = offsetCounter2;
					offsetCounter2 += size;
				}
				field = field.next;
			}
			clazz.classFieldsSizeOnTarget = roundUpToNextWord(offsetCounter1);
		}
		
		// Methods
		offsetCounter1 = 0;
		if(clazz.nOfClassFields > 0) {
			Method method = (Method)clazz.methods;
			while(method != null) {
				method.offset = offsetCounter1;
				offsetCounter1 += 32;
				method = (Method)method.next;
			}
		}
	}
	
	
	/**
	 * Creates the constant pool (containing all constant float and double values) for the target.
	 * @param clazz is the class to process
	 */
	public static void createConstantPool(Class clazz) {
		clazz.targetConstantPool = new int[clazz.targetConstantPoolSize/32];
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/32;
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
		clazz.targetStringPool = new int[clazz.targetStringPoolSize/32];
		if(clazz.constPool != null) {
			for(int i = 0; i < clazz.constPool.length; i++) {
				int index = clazz.constPool[i].offset/32;
				if(clazz.constPool[i].type == Type.wellKnownTypes[Type.txString]) {
					HString s = ((StringLiteral)clazz.constPool[i]).string;
					if(s.sizeInBits()/s.length() == 8) { // string is a H8String
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
						assert s.sizeInBits()/s.length() == 16: "String is neighter 8bit nor 16bit char array!";
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
		// TODO implement this
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
		assert potentialOffset % 8 == 0: "ERROR: Potential offset is not a multiple of 8! (Potential offset = " + potentialOffset + ")";
		assert size % 8 == 0: "ERROR: Size is not a multiple of 8! (Size = " + size + ")";
		
		if(size == 64) size = 32; // handle double word items like word items
		
		if(potentialOffset % size == 0) return potentialOffset;
		return potentialOffset + (32 - (potentialOffset % size));
	}
	
	private static int roundUpToNextWord(int val) {
		if(val % 32 == 0) return val;
		return val + (32 - (val % 32));
	}
}
