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

package ch.ntb.inf.deep.linker;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.DataItem;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.IAttributes;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Linker32 implements ICclassFileConsts, ICdescAndTypeConsts, IAttributes {
	
	// Slot size:
	public static final byte slotSize = 4; // 4 bytes
	static{
		assert (slotSize & (slotSize-1)) == 0; // assert:  slotSize == power of 2
	}

	public static final boolean dbg = false; // enable/disable debugging outputs for the linker

	// Class/type descriptor:
	public static final int tdSizeOffset = 0;
	public static final int tdExtensionLevelOffset = tdSizeOffset + 4;
	public static final int tdMethTabOffset = tdExtensionLevelOffset + 4;
	public static final int tdClassNameAddrOffset = tdSizeOffset + 4;
	public static final int tdInstPtrOffsetOffset = tdClassNameAddrOffset + 4;
	public static final int tdBaseClass0Offset = tdInstPtrOffsetOffset + 4;
	public static int typeTableLength = -1;
	
	// String pool:
	public static final int stringHeaderConstSize = 3 * 4;
	public static final int spTagIndex = 1;
	public static final int spTagOffset = spTagIndex * 4;
	public static int stringHeaderSize = -1;
	public static Class stringClass;
	
	// Error reporter and stdout:
	private static final ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream vrb = StdStreams.vrb;
	private static PrintStream log = StdStreams.log;

	// Target image
	public static TargetMemorySegment targetImage;

	// Constant block:
	public static int cblkConstBlockSizeOffset;
	public static int cblkCodeBaseOffset;
	public static int cblkCodeSizeOffset;
	public static int cblkVarBaseOffset;
	public static int cblkVarSizeOffset;
	public static int cblkClinitAddrOffset;
	public static int cblkNofPtrsOffset;
	public static int cblkPtrAddr0Offset;
	
	// System table
	public static int stClassConstOffset;
	public static int stStackOffset;
	public static int stHeapOffset;
	public static int stKernelClinitAddr;
	public static int stResetOffset;
	public static int stSizeToCopy;
	public static int stNofStacks;
	private static int systemTableSize; // TODO remove this, use systemTable.getBlockSize() instead
	private static BlockItem systemTable;
	private static Segment[] sysTabSegments;
	private static FixedValueItem sysTabSizeToCopy;
	private static int firstUsedAddress = Integer.MAX_VALUE;
	private static int lastUsedAddress = 0;
	
	// Global constants
	private static BlockItem globalConstantTable;
	private static int globalConstantTableOffset = -1;
	private static Segment globalConstantTableSegment;
	
	// Compiler specific methods
	private static int compilerSpecificMethodsCodeSize = 0;
	private static int compilerSpecificMethodsOffset = -1;
	private static Segment compilerSpecSubroutinesSegment;
	
	
	public static void init() {
		if(dbg) vrb.println("[LINKER] START: Initializing:");
		
		if(dbg) vrb.print("  Setting size of string header: ");
		stringHeaderSize = stringHeaderConstSize + Type.wktObject.getObjectSize();
		if(dbg) vrb.println(stringHeaderSize + " byte");
		
		if(dbg) vrb.println("  Looking for segments for the system table: ");
		sysTabSegments = Configuration.getSysTabSegments();
		if(sysTabSegments != null && sysTabSegments.length > 0) {
			if(dbg) {
				for(int i = 0; i < sysTabSegments.length; i++) {
					vrb.println("     -> found: " + sysTabSegments[i].getName());
				}
			}
		}
		else {
			reporter.error(710, "No segment(s) for the systemtable defined!");
		}
		
		if(dbg) vrb.println("  Looking up compiler constants: ");
		cblkConstBlockSizeOffset = Configuration.getValOfCompConstByName("cblkConstBlockSizeOffset");
		if(dbg) vrb.println("  - cblkConstBlockSizeOffset = " + cblkConstBlockSizeOffset);
		cblkCodeBaseOffset = Configuration.getValOfCompConstByName("cblkCodeBaseOffset");
		if(dbg) vrb.println("  - cblkCodeBaseOffset = " + cblkCodeBaseOffset);
		cblkCodeSizeOffset = Configuration.getValOfCompConstByName("cblkCodeSizeOffset");
		if(dbg) vrb.println("  - cblkCodeSizeOffset = " + cblkCodeSizeOffset);
		cblkVarBaseOffset = Configuration.getValOfCompConstByName("cblkVarBaseOffset");
		if(dbg) vrb.println("  - cblkVarBaseOffset = " + cblkVarBaseOffset);
		cblkVarSizeOffset = Configuration.getValOfCompConstByName("cblkVarSizeOffset");
		if(dbg) vrb.println("  - cblkVarSizeOffset = " + cblkVarSizeOffset);
		cblkClinitAddrOffset = Configuration.getValOfCompConstByName("cblkClinitAddrOffset");
		if(dbg) vrb.println("  - cblkClinitAddrOffset = " + cblkClinitAddrOffset);
		cblkNofPtrsOffset = Configuration.getValOfCompConstByName("cblkNofPtrsOffset");
		if(dbg) vrb.println("  - cblkNofPtrsOffset = " + cblkNofPtrsOffset);
		cblkPtrAddr0Offset = Configuration.getValOfCompConstByName("cblkPtrAddr0Offset");
		if(dbg) vrb.println("  - cblkPtrAddr0Offset = " + cblkPtrAddr0Offset);
		
		stClassConstOffset = Configuration.getValOfCompConstByName("stClassConstOffset");
		if(dbg) vrb.println("  - stClassConstOffset = " + stClassConstOffset);
		stStackOffset = Configuration.getValOfCompConstByName("stStackOffset");
		if(dbg) vrb.println("  - stStackOffset = " + stStackOffset);
		stHeapOffset = Configuration.getValOfCompConstByName("stHeapOffset");
		if(dbg) vrb.println("  - stHeapOffset = " + stHeapOffset);
		stKernelClinitAddr = Configuration.getValOfCompConstByName("stKernelClinitAddr");
		if(dbg) vrb.println("  - stKernelClinitAddr = " + stKernelClinitAddr);
		stResetOffset = Configuration.getValOfCompConstByName("stResetOffset");
		if(dbg) vrb.println("  - stResetOffset = " + stResetOffset);
		stSizeToCopy = Configuration.getValOfCompConstByName("stSizeToCopy");
		if(dbg) vrb.println("  - stSizeToCopy = " + stSizeToCopy);
		stNofStacks = Configuration.getValOfCompConstByName("stNofStacks");
		if(dbg) vrb.println("  - stNofStacks = " + stNofStacks);
		
		if(dbg) vrb.println("  Deleting old target image... ");
		targetImage = null;
		
		if(dbg) vrb.println("[LINKER] END: Initializing.\n");
	}
	
	public static void createConstantBlock(Class clazz) {
			
		if(dbg) vrb.println("[LINKER] START: Preparing constant block for class \"" + clazz.name +"\":");

		if(dbg) {
			vrb.println("=== Extended Method Table ===");
			for(int i = 0; i < clazz.extMethTable.length; i++) {
				vrb.println("[" + i + "] Name = " + clazz.extMethTable[i].name + " Index = " + Integer.toHexString(clazz.extMethTable[i].index));
			}
			vrb.println("=============================");
		}
		
		// Header
		if(dbg) vrb.println("   Creating header");
		clazz.constantBlock = new FixedValueItem("constBlockSize");
		clazz.codeBase = new FixedValueItem("codeBase");
		clazz.codeBase.append(new FixedValueItem("codeSize"));
		clazz.constantBlock.append(clazz.codeBase);
		clazz.varBase = new FixedValueItem("varBase");
		clazz.varBase.append(new FixedValueItem("varSize"));
		clazz.constantBlock.append(clazz.varBase);
		Method classConstructor = clazz.getClassConstructor();
		if(classConstructor != null) {
			clazz.constantBlock.append(new AddressItem(classConstructor));
		}
		else {
			clazz.constantBlock.append(new FixedValueItem("<clinit>", -1));
		}
		
		// Pointer list (list of class fields which are references)
		if(dbg) vrb.println("   Creating pointer list (class fields)");
		clazz.classPtrList = new FixedValueItem("nofClassPtrs");
		int ptrCounter = 0;
		if(clazz.nofClassRefs > 0) {
			Item field = clazz.firstClassReference;
			while(field != clazz.constFields) {
				if((field.accAndPropFlags & (1 << dpfConst)) == 0 && (field.accAndPropFlags & (1 << apfStatic)) != 0 && (((Type)field.type).category == tcRef || ((Type)field.type).category == tcArray )) {
					clazz.classPtrList.append(new AddressItem(field));
					ptrCounter++;
				}
				field = field.next;
			}
			assert ptrCounter == clazz.nofClassRefs : "[Error] Number of added pointers (" + ptrCounter + ") not equal to number of pointers in class (" + clazz.nofClassRefs + ")!";
		}
		((FixedValueItem)clazz.classPtrList).setValue(ptrCounter);
		clazz.constantBlock.append(clazz.classPtrList);
		
		// Type descriptor: size and extension level
		if(dbg) vrb.println("   Creating type descriptor");
		if(dbg) vrb.println("    - Beginning with size");
		clazz.typeDescriptor = new FixedValueItem("size");
		if(dbg) vrb.println("    - Inserting the extension level");
		clazz.typeDescriptor.insertBefore(new FixedValueItem("extensionLevel", clazz.extensionLevel));
		
		// Type descriptor: create method table
		if(dbg) vrb.println("    - Inserting method table:");
		for(int i = 0; i < clazz.methTabLength; i++) {
			clazz.typeDescriptor.getHead().insertBefore(new AddressItem(clazz.extMethTable[i])); 
		}
		
		
		// Type descriptor: insert class name address
		if(dbg) vrb.println("    - Inserting class name address");
		clazz.typeDescriptor.insertAfter(new FixedValueItem("classNameAddr", 0x12345678));
		
		// Type descriptor: insert instance pointers offset
		if(dbg) vrb.println("    - Inserting instance pointer offset");
		clazz.instPtrOffset = new FixedValueItem("instPtrOffset");
		clazz.typeDescriptor.append(clazz.instPtrOffset);
		
		// Type descriptor: create type table (base classes)
		if(dbg) vrb.println("    - Inserting base classes:");
		Class baseClass = (Class)clazz.type;
		AddressItem typeTable = new AddressItem(clazz);
		for(int i = 0; i < Class.maxExtensionLevelStdClasses; i++) {
			if(baseClass != null) {
				if(dbg) vrb.println("      > " + baseClass.name);
				typeTable.getHead().insertBefore(new AddressItem(baseClass));
				baseClass = (Class)baseClass.type;
			}
			else {
				if(dbg) vrb.println("      > 0 (padding)");
				typeTable.getTail().insertAfter(new FixedValueItem("padding", 0));
			}
		}
		typeTableLength = typeTable.getBlockSize();
		clazz.typeDescriptor.append(typeTable.getHead());
		
		// Type descriptor: add interface table
		if(clazz.extMethTable.length > clazz.methTabLength) {
			if(dbg) vrb.println("    - Inserting interface table");
			int counter = clazz.methTabLength;
			if(dbg) vrb.println("      + clazz.extMethTable[" + counter + "]: " + clazz.extMethTable[counter].name + " (Owner: " + clazz.extMethTable[counter].owner + ")");
			AddressItem interfaceTable = new AddressItem(clazz.extMethTable[counter++]);
			int id, bmo;
			//vrb.println("      ==> ifaceTAbLength = " + clazz.ifaceTabLength + "; nofInterfaces = " + clazz.nofInterfaces);
			
			if (counter < clazz.extMethTable.length)
			{
				do {
					id = clazz.extMethTable[counter].index >>> 16;
					bmo = clazz.extMethTable[counter].index & 0xFFFF;
					if(bmo > clazz.methTabLength) {
						bmo = 8 + typeTableLength + (clazz.nofInterfaces + bmo - clazz.methTabLength) * 4; // TODO @Martin: fix offset (only interfaces with methods should be honored) 
					}
					else {
						bmo = -(tdMethTabOffset + bmo * 4);
					}
					if(dbg) vrb.println("      + clazz.extMethTable[" + counter + "]: ID = " + id + "; bmo = " + bmo);
					interfaceTable.append(new InterfaceItem(clazz.extMethTable[counter].owner.name, (short)id, (short)bmo));
				} while ((clazz.extMethTable[counter++].index >>> 16) > 0);

				while(counter < clazz.extMethTable.length) {
					if(dbg) vrb.println("      + clazz.extMethTable[" + counter + "]: Method = " + clazz.extMethTable[counter].name);
					interfaceTable.append(new AddressItem(clazz.extMethTable[counter++]));
				}
			}
			
			clazz.typeDescriptor.append(interfaceTable.getHead());
		}
		
		// Instance pointer list (list of instance fields which are references)
		if(dbg) vrb.println("   Creating pointer list (instance fields)");
		clazz.instPtrList = new FixedValueItem("nofInstPtrs");
		ptrCounter = 0;
		if(clazz.nofInstRefs > 0) {
			Item field = clazz.firstInstReference;
			while(field != clazz.classFields) {
				if((field.accAndPropFlags & (1 << dpfConst)) == 0 && (field.accAndPropFlags & (1 << apfStatic)) == 0 && (((Type)field.type).category == tcRef || ((Type)field.type).category == tcArray )) {
					clazz.instPtrList.append(new OffsetItem("instPtrOffset[" + ptrCounter + "]: ", field));
					ptrCounter++;
				}
				field = field.next;
			}
			assert ptrCounter == clazz.nofInstRefs : "[Error] Number of added pointers (" + ptrCounter + ") not equal to number of pointers in class (" + clazz.nofClassRefs + ")!";
		}
		((FixedValueItem)clazz.instPtrList).setValue(ptrCounter);
		clazz.typeDescriptor.append(clazz.instPtrList);
		
		// calculate type descriptor size
		clazz.typeDescriptorSize = clazz.typeDescriptor.getBlockSize();
		
		// add type descriptor to constant block
		clazz.constantBlock.append(clazz.typeDescriptor.getHead());
		
		// create string pool
		if(dbg) vrb.println("  Creating string pool");
		if(clazz.constPool != null) {
			Item cpe;
			for(int i = 0; i < clazz.constPool.length; i++) {
				cpe = clazz.constPool[i];
				if(cpe.type == Type.wellKnownTypes[txString] && (cpe.accAndPropFlags & (1 << dpfConst)) != 0) { // TODO @Martin is checking the const flag necessary?
					if(clazz.stringPool == null) clazz.stringPool = new StringItem(cpe);
					else clazz.stringPool.append(new StringItem(cpe));
				}
			}
		}
		if(clazz.stringPool != null) {
			clazz.stringPoolSize = clazz.stringPool.getBlockSize();
			clazz.constantBlock.append(clazz.stringPool);
		}
		
		// create constant pool
		if(dbg) vrb.println("  Creating constant pool");
		if(clazz.constPool != null) {
			Item cpe;
			for(int i = 0; i < clazz.constPool.length; i++) {
				cpe = clazz.constPool[i];
				if(checkConstantPoolType(cpe)) {
					if(clazz.constantPool == null) clazz.constantPool = new ConstantItem(cpe);
					else clazz.constantPool.append(new ConstantItem(cpe));
				}
			}
		}
		if(clazz.constantPool != null) {
			clazz.constantPoolSize = clazz.constantPool.getBlockSize();
			clazz.constantBlock.append(clazz.constantPool);
		}
				
		// append block item for checksum
		clazz.constantBlockChecksum = new FixedValueItem("fcs", 0);
		clazz.constantBlock.append(clazz.constantBlockChecksum);
		
		// Calculate size of constant block
		((FixedValueItem)clazz.constantBlock).setValue(clazz.constantBlock.getBlockSize());
				
		// Calculating indexes and offsets for the string- and constant pool
		int offset, index;
		
		if(clazz.stringPool != null) {
			if(dbg) vrb.println("  Calculating indexes and offsets for the string pool entries");
			BlockItem s = clazz.stringPool;
			offset = 0; index = 0;
			while(s != clazz.constantPool && s != clazz.instPtrList && s != clazz.constantBlockChecksum) {
				((StringItem)s).setIndex(index);
				((StringItem)s).setOffset(offset);
				
				index++;
				offset += s.getItemSize();
				
				s = s.next;
			}
		}
		
		if(clazz.constantPool != null) {
			if(dbg) vrb.println("  Calculating indexes and offsets for the constant pool entries");
			BlockItem c = clazz.constantPool;
			offset = 0; index = 0;
			while(c != clazz.instPtrList && c != clazz.constantBlockChecksum) {
				((ConstantItem)c).setIndex(index);
				((ConstantItem)c).setOffset(offset);
				
				index++;
				offset += c.getItemSize();
				
				c = c.next;
			}
		}
		
		// Calculating type descriptor offset
		BlockItem i = clazz.constantBlock;
		offset = 0;
		while(i != clazz.typeDescriptor) {
			offset += i.getItemSize();
			i = i.next;
		}
		clazz.typeDescriptorOffset = offset;
		
		// Calculating instance pointer list offset
		i = clazz.typeDescriptor;
		offset = 0;
		while(i != clazz.instPtrList) {
			offset += i.getItemSize();
			i = i.next;
		}
		clazz.instPtrOffset.setValue(offset);
		
		
		if(dbg) vrb.println("\n[LINKER] END: Preparing constant block for class \"" + clazz.name +"\"\n");
	}
		
	public static void createTypeDescriptor(Array array) {
		if(dbg) vrb.println("[LINKER] START: Creating type descriptor for array \"" + array.name +"\":");
		if(dbg) vrb.println("  Element type: " + array.componentType.name);
		if(dbg) vrb.println("  Element size: " + array.componentType.sizeInBits / 8 + " byte (" + array.componentType.sizeInBits + " bit)");
		if(dbg) vrb.println("  Dimension:    " + array.dimension);
				
		// Extentsion level
		array.typeDescriptor = new FixedValueItem("extensionLevel", 1); // the base type of an array is always object!
		
		// Array dimension, component size and array type flag
		byte arrayOfPrimitives = 0;
		if(array.componentType.category == tcPrimitive) arrayOfPrimitives = 1;
		array.typeDescriptor.append(new FixedValueItem("dimension/size", ((arrayOfPrimitives << 31) | array.dimension << 16) | (array.componentType.sizeInBits / 8)));
		
		// Array name address
		array.typeDescriptor.append(new FixedValueItem("arrayNameAddr", 0x12345678));
				
		// List of type descriptors of arrays with the same component type
		String arrayName; Item lowDimArray;
		for(int i = array.dimension; i > 0; i--) {
			arrayName = array.name.substring(array.dimension - i).toString();
			lowDimArray = Type.classList.getItemByName(arrayName);
			if(lowDimArray != null) {
				array.typeDescriptor.append(new AddressItem("arrayTD[" + i + "]: ", lowDimArray));
			}
			else {
				array.typeDescriptor.append(new FixedValueItem("arrayTD[" + i + "]: <not available> (" + arrayName + ")", -1));
				// TODO @Martin: insert warning or error!
			}
		}
		
		// Component type
		if(array.componentType.category == tcPrimitive) {
			array.typeDescriptor.append(new FixedValueItem("arrayComponentTD: <primitive> (" + array.componentType.name + ")", 0));
		}
		else {
			array.typeDescriptor.append(new AddressItem("arrayComponentTD: ", array.componentType));
		}
		
		if(dbg) vrb.println("[LINKER] END: Creating type descriptor for array \"" + array.name +"\"\n");
	}

	public static void createGlobalConstantTable() {
		if(dbg) vrb.println("[LINKER] START: Creating global constant table:\n");
		int offset = 0, index = 0;
		ConstantItem constant = (ConstantItem)globalConstantTable;

		while(constant != null) {
			constant.setIndex(index);
			constant.setOffset(offset);
			constant.setAddress(globalConstantTableSegment.getBaseAddress() + globalConstantTableOffset + offset);
			index++;
			offset += constant.getItemSize();
			constant = (ConstantItem)constant.next;
		}
		if(dbg) vrb.println("[LINKER] END: Creating global constant table\n");
	}
	
	public static void calculateCodeSizeAndOffsets(Class clazz) {
		if(dbg) vrb.println("[LINKER] START: Calculating code size and offsets for class \"" + clazz.name +"\":\n");
		
		// machine code size
		if(dbg) vrb.println("  1) Code:");
		Method m = (Method)clazz.methods;
		int codeSize = 0; // machine code size for the hole class
		while(m != null) {
			if(m.machineCode != null) {
				if(m.offset < 0) { // offset not given by configuration
					m.offset = codeSize;
					codeSize += m.machineCode.iCount * 4; // iCount = number of instructions!
				}
				else { // offset given by configuration
					if(codeSize < m.machineCode.iCount * 4 + m.offset) codeSize = m.offset + m.machineCode.iCount * 4;
				}
				if(dbg) vrb.println("    > " + m.name + ": codeSize = " + m.machineCode.iCount * 4 + " byte");
			}
			m = (Method)m.next;
		}
		((FixedValueItem)clazz.codeBase.next).setValue(codeSize);
		if(dbg) vrb.println("    Total code size: " + codeSize + " byte");
		
		if(dbg) vrb.println("\n[LINKER] END: Calculating code size for class \"" + clazz.name +"\"\n");
	}
	
	public static void calculateCodeSizeAndOffsetsForCompilerSpecSubroutines() {
		if(dbg) vrb.println("[LINKER] START: Calculating code size for compiler specific methods:\n");
		Method m = Method.compSpecSubroutines;
		int codeSize = 0; // machine code size for all compiler specific subroutines
		while(m != null) {
			if(m.machineCode != null) {
				m.offset = codeSize;
				codeSize += m.machineCode.iCount * 4; // iCount = number of instructions!
				if(dbg) vrb.println("    > " + m.name + ": codeSize = " + m.machineCode.iCount * 4 + " byte");
			}
			m = (Method)m.next;
		}
		compilerSpecificMethodsCodeSize = codeSize;
		if(dbg) vrb.println("    Total code size: " + codeSize + " byte");
		if(dbg) vrb.println("\n[LINKER] END: Calculating code size for compiler specific methods\n");
	}
	
	public static void createSystemTable() {
		if(dbg) vrb.println("[LINKER] START: Create system table:\n");
		
		// Number of stacks, heaps and classes
		int nofStacks = Configuration.getNumberOfStacks();
		int nofHeaps = Configuration.getNumberOfHeaps();
		if(dbg) vrb.println("  Number of stacks:  " + nofStacks);
		if(dbg) vrb.println("  Number of heaps:   " + nofHeaps);
		if(dbg) vrb.println("  Number of classes: " + Type.nofClasses);
		
		// Find the kernel
		HString kernelClassName = Configuration.getKernelClassname();
		if(kernelClassName == null) {
			kernelClassName = HString.getRegisteredHString("<undefined>");
			reporter.error(740, "kernel class not set");
		}
		Item kernelClass = Type.classList.getItemByName(kernelClassName.toString());
		Item kernelClinit = null;
		int kernelClinitAddr = -1;
		if(kernelClass != null) {
			kernelClinit = ((Class)kernelClass).getClassConstructor();
			if(kernelClinit != null) {
				kernelClinitAddr = kernelClinit.address;
			}
			else {
				reporter.error(730, kernelClassName.toString() + ".<clinit>");
			}
		}
		else {
			reporter.error(702, kernelClassName.toString());
		}
		
		if(dbg) vrb.println("  Kernel class:      " + kernelClass.name);
		if(dbg) vrb.println("  -> Clinit Addr.:   " + kernelClinitAddr);
				
		// Create the system table
		systemTable = new FixedValueItem("classConstOffset", stNofStacks + ( 2 * nofStacks + 2 * nofHeaps) * 4 + 12);
		systemTable.append(new FixedValueItem("stackOffset", stNofStacks));
		systemTable.append(new FixedValueItem("heapOffset", stNofStacks + 2 * nofStacks * 4 + 4));
		systemTable.append(new AddressItem("kernelClinitAddr: " + kernelClassName + ".",kernelClinit));
		systemTable.append(new FixedValueItem("resetOffset", Configuration.getResetOffset()));
		sysTabSizeToCopy = new FixedValueItem("sizeToCopy", -1);
		systemTable.append(sysTabSizeToCopy);
		systemTable.append(new FixedValueItem("nofStacks", nofStacks));
		for(int i = 0; i < nofStacks; i++) { // reference to each stack and the size of each stack
			systemTable.append(new AddressItem("baseStack" + i + ": ", Configuration.getStackSegments()[i])); // base address
			systemTable.append(new FixedValueItem("sizeStack" + i, Configuration.getStackSegments()[i].getSize()));
		}
		systemTable.append(new FixedValueItem("nofHeaps", nofHeaps));
		for(int i = 0; i < nofHeaps; i++) { //reference to each heap and the size of each heap
			systemTable.append(new AddressItem("baseHeap" + i + ": ", Configuration.getHeapSegments()[i])); // base address
			systemTable.append(new FixedValueItem("sizeHeap" + i, Configuration.getHeapSegments()[i].getSize()));
		}
		systemTable.append(new FixedValueItem("nofClasses", Class.nofInitClasses + Class.nofNonInitClasses));
		Class clazz = Class.initClasses; int i = 0;
		while(clazz != null) { // reference to the constant block of each class with a class constructor (clinit)
			if( clazz instanceof Class  && ((clazz.accAndPropFlags & (1 << apfInterface)) == 0)) {
				systemTable.append(new ConstantBlockItem("constBlkBaseClass" + i + ": ", clazz));
				i++;
			}
			clazz = clazz.nextClass;
		}
		clazz = Class.nonInitClasses;
		while(clazz != null) { // reference to the constant block of each class without a class constructor (no clinit)
			if( clazz instanceof Class  && ((clazz.accAndPropFlags & (1 << apfInterface)) == 0)) {
				systemTable.append(new ConstantBlockItem("constBlkBaseClass" + i + ": ", clazz));
				i++;
			}
			clazz = clazz.nextClass;
		}
		systemTable.append(new FixedValueItem("endOfSystemTable", 0));
		
		systemTableSize = systemTable.getBlockSize();
		
		if(dbg) vrb.println("  Size of the system table: " + systemTableSize + " byte (0x" + Integer.toHexString(systemTableSize) + ")");
		
		if(dbg) vrb.println("[LINKER] END: Create system table.\n");
	}
		
	public static void freezeMemoryMap() {
		if(dbg) vrb.println("[LINKER] START: Freeze memory map:\n");
		
		if(dbg) vrb.println("1) Set a segment for the code, the static fields and the constant block for each class");
		Item item = Type.classList;
		Segment s;
		while(item != null) {
			if(item instanceof Class  && ((item.accAndPropFlags & (1 << apfInterface)) == 0)){
				Class c = (Class)item;
				if(dbg) vrb.println("  Proceeding Class " + c.name);
				
				// Code
				s = Configuration.getCodeSegmentOf(c.name);
				if(s == null) reporter.error(710, "Can't get a memory segment for the code of class " + c.name + "!\n");
				else {
					int codeSize = ((FixedValueItem)c.codeBase.next).getValue();
					if(s.subSegments != null) s = getFirstFittingSegment(s.subSegments, atrCode, codeSize);
					c.codeOffset = s.getUsedSize();
					if(codeSize > 0) s.addToUsedSize(roundUpToNextWord(codeSize));
					c.codeSegment = s;
					if(dbg) {
						vrb.println("    Code-Segment: " + c.codeSegment.getFullName());
						vrb.println("    Code-Offset: " + Integer.toHexString(c.codeOffset));
					}
				}
				
				// Var
				s = Configuration.getVarSegmentOf(c.name);
				if(s == null) reporter.error(710, "Can't get a memory segment for the static variables of class " + c.name + "!\n");
				else {
					if(s.subSegments != null) s = getFirstFittingSegment(s, atrVar, c.classFieldsSize);
					c.varOffset = s.getUsedSize();
					if(c.classFieldsSize > 0) s.addToUsedSize(roundUpToNextWord(c.classFieldsSize));
					c.varSegment = s;
					if(dbg) vrb.println("    Var-Segment: " + c.varSegment.getFullName());
				}
				
				// Const
				s = Configuration.getConstSegmentOf(c.name);
				if(s == null) reporter.error(710, "Can't get a memory segment for the constant block of class " + c.name + "!\n");
				else {
					int constBlockSize = ((FixedValueItem)c.constantBlock).getValue();
					if(s.subSegments != null) s = getFirstFittingSegment(s, atrConst, constBlockSize);
					c.constOffset = s.getUsedSize();
					if(constBlockSize > 0) s.addToUsedSize(roundUpToNextWord(constBlockSize));
					c.constSegment = s;
					if(dbg) vrb.println("    Const-Segment: " + c.constSegment.getFullName());
				}		
			}
			else if(item instanceof Array) {
				Array a = (Array)item;
				s = Configuration.getDefaultConstSegment();
				
				if(dbg) vrb.println("  Proceeding Array " + a.name);
				
				if(s == null) reporter.error(710, "Can't get a memory segment for the typedecriptor of array " + a.name + "!\n");
				else {
					if(s.subSegments != null) s = getFirstFittingSegment(s, atrConst, a.typeDescriptor.getBlockSize());
					a.offset = roundUpToNextWord(s.getUsedSize()); // TODO check if this is correct!!!
					s.addToUsedSize(a.typeDescriptor.getBlockSize());
					a.segment = s;
					if(dbg) vrb.println("    Segment for type descriptor: " + a.segment.getName());
				}	
			}
			else {
				if(dbg) vrb.println("+++++++++++++++ The following item in classlist is neither a class nor an array: " + item.name); // it should be an interface...
			}
			item = item.next;
		}
		
		//Segment[] sysTabs = Configuration.getSysTabSegments(); // TODO @Martin: implement this for more than one system table!
		if(sysTabSegments != null && sysTabSegments.length > 0) {
			for(int i = 0; i < sysTabSegments.length; i++) {
				sysTabSegments[i].addToUsedSize(systemTableSize); 
			}
		}
		else reporter.error(710, "No segment(s) defined for the system table!");
		
		if(dbg) vrb.println("  Proceeding global constant table");
		s = Configuration.getDefaultConstSegment();
		if(s == null) reporter.error(710, "Can't get a memory segment for the global constant table!\n");
		else {
			if(s.subSegments != null) s = getFirstFittingSegment(s, atrConst, globalConstantTable.getBlockSize());
			globalConstantTableOffset = roundUpToNextWord(s.getUsedSize());
			s.addToUsedSize(globalConstantTable.getBlockSize());
			globalConstantTableSegment = s;
			if(dbg) vrb.println("    Segment for global constant table: " + globalConstantTableSegment.getName());
		}
		
		if(dbg) vrb.println("  Proceeding compiler specific methods");
		s = Configuration.getDefaultConstSegment();
		if(s == null) reporter.error(710, "Can't get a memory segment for the compiler specific methods!\n");
		else {
			if(s.subSegments != null) s = getFirstFittingSegment(s, atrCode, compilerSpecificMethodsCodeSize);
			compilerSpecificMethodsOffset = roundUpToNextWord(s.getUsedSize());
			s.addToUsedSize(compilerSpecificMethodsCodeSize);
			compilerSpecSubroutinesSegment = s;
			if(dbg) vrb.println("    Segment for compiler specific methods: " + compilerSpecSubroutinesSegment.getName());
		}
		
		if(dbg) vrb.println("2) Check and set the size for each used segment");
		Device[] d = Configuration.getBoard().getAllDevices();
		for(int i = 0; i < d.length; i++) {
			if(dbg) vrb.println("  Proceeding device " + d[i].getName());
			if(d[i].segments != null && d[i].segments.getTail() != null) setSegmentSize((Segment)d[i].segments.getTail());
		}
		
		if(dbg) vrb.println("3) Set base addresses for each used segment");
		for(int i = 0; i < d.length; i++) {
			if(dbg) vrb.println("  Proceeding device " + d[i].getName());
			if(d[i].segments != null) setBaseAddress(d[i].segments, d[i].getbaseAddress());
		}
		
		if(dbg) vrb.println("[LINKER] END: Freeze memory map.");
	}
	
	public static void calculateAbsoluteAddresses(Class clazz) {
		if(dbg) vrb.println("\n[LINKER] START: Calculating absolute addresses for class \"" + clazz.name +"\":\n");
		
		int varBase = clazz.varSegment.getBaseAddress() + clazz.varOffset;
		int codeBase = clazz.codeSegment.getBaseAddress() + clazz.codeOffset;
		int constBlockBase =  clazz.constSegment.getBaseAddress() + clazz.constOffset;
		int classDescriptorBase = constBlockBase + cblkNofPtrsOffset + (clazz.nofClassRefs + 1) * slotSize;
		int stringPoolBase = classDescriptorBase + clazz.typeDescriptorSize;
		int constPoolBase = stringPoolBase + clazz.stringPoolSize;
		
		if(dbg) {
			vrb.println("  Code base: 0x" + Integer.toHexString(codeBase));
			vrb.println("  Var base: 0x" + Integer.toHexString(varBase));
			vrb.println("  Const segment base address: 0x" + Integer.toHexString(clazz.constSegment.getBaseAddress()));
			vrb.println("  Const offset: 0x" + Integer.toHexString(clazz.constOffset));
			vrb.println("  Const block base address: 0x" + Integer.toHexString(constBlockBase));
			vrb.println("  Number of class refereces: " + clazz.nofClassRefs);
			vrb.println("  Class descriptor base: 0x" + Integer.toHexString(classDescriptorBase));
			vrb.println("  String pool base: 0x" + Integer.toHexString(stringPoolBase));
			vrb.println("  Const pool base: 0x" + Integer.toHexString(constPoolBase));
		}
		
		// Class/static fields
		if(clazz.nofClassFields > 0) {
			Item field = clazz.classFields; // class fields and constant fields
			if(dbg) vrb.println("  Static fields:");
			while(field != null) {
				if((field.accAndPropFlags & (1 << dpfConst)) != 0) { // constant field // TODO @Martin If-Teil sollte eigentlich ueberhaupt nicht notwendig sein, da konstante Referenzen das Const-Flag gar nicht gesetzt haben -> If-Teil entfernen?
					if(((Type)field.type).category == tcRef) { // reference but not literal string
						if(varBase != -1 && field.offset != -1) field.address = varBase + field.offset;
					}
				}
				else { // non constant field -> var section
					if(varBase != -1 && field.offset != -1) field.address = varBase + field.offset;
					else {
						if(varBase == -1) reporter.error(724, "varBase of class " + clazz.name + " not set");
						if(field.offset == -1) reporter.error(721, "offset of field " + field.name + " in class " + clazz.name + " not set!"); 
					}
				}
				if(dbg) vrb.print("    > " + field.name + ": Offset = 0x" + Integer.toHexString(field.offset) + ", Index = 0x" + Integer.toHexString(field.index) + ", Address = 0x" + Integer.toHexString(field.address) + "\n");
				field = field.next;
			}
		}
		
		// Methods
		if(clazz.nofMethods > 0) {
			Method method = (Method)clazz.methods;
			if(dbg) vrb.println("  Methods:");
			while(method != null) {
				if((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) { // TODO @Martin: fix this hack!!!
					if(method.offset != -1) {
						method.address = clazz.codeSegment.getBaseAddress() + method.offset;
					}
				//	else reporter.error(9999, "Error while calculating absolute address of fix set method " + method.name + ". Offset: " + method.offset + ", Segment: " + clazz.codeSegment.getName() + ", Base address of Segment: " + clazz.codeSegment.getBaseAddress());
				}
				else {
					if(codeBase != -1 && method.offset != -1) {
						method.address = codeBase + method.offset;
					}
				//	else reporter.error(9999, "Error while calculating absolute address of method " + method.name + ". Offset: " + method.offset + ", Codebase of Class " + clazz.name + ": " + codeBase);
				}
				if(dbg) vrb.print("    > " + method.name + ": Offset = 0x" + Integer.toHexString(method.offset) + ", Index = 0x" + Integer.toHexString(method.index) + ", Address = 0x" + Integer.toHexString(method.address) + "\n");
				method = (Method)method.next;
			}
		}
		
		// Constants
		if(clazz.constPool != null && clazz.constPool.length > 0) {
			Item cpe;
			if(dbg) vrb.println("  Constant pool:");
			for(int i = 0; i < clazz.constPool.length; i++) {
				cpe = clazz.constPool[i];
				if(cpe instanceof StdConstant && (cpe.type == Type.wellKnownTypes[txFloat] || cpe.type == Type.wellKnownTypes[txDouble])) { // constant float or double value -> constant pool
					if(cpe.offset != -1) {
						cpe.address = constPoolBase + cpe.offset;
					}
					else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
				}
				else if(cpe instanceof StringLiteral) { // string literal -> string pool
					if(cpe.offset != -1) {
						cpe.address = stringPoolBase + cpe.offset + 8;
					}
					else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
				}
				if(dbg) {
					if(cpe.type != null) vrb.print("    - #" + i + ": Type = " + cpe.type.name + ", Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + "\n");
					else vrb.print("    - #" + i + ": Type = <unknown>, Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + "\n");
				}
			}
		}
		
		// type descriptor
		clazz.address = clazz.constSegment.getBaseAddress() + clazz.constOffset + clazz.typeDescriptorOffset;
		
		if(dbg) vrb.println("\n[LINKER] END: Calculating absolute addresses for class \"" + clazz.name +"\"\n");
	}

	public static void calculateAbsoluteAddresses(Array array) {
		// TODO@Martin: merge this with calculateAbsoluteAddresses(Class clazz)
		array.address = array.segment.getBaseAddress() + array.offset + 4;
	}
	
	public static void calculateAbsoluteAddressesForCompSpecSubroutines() {
		if(dbg) vrb.println("\n[LINKER] START: Calculating absolute addresses for compiler specific methods:\n");
		Method m = Method.compSpecSubroutines;
		while(m != null) {
			m.address = compilerSpecSubroutinesSegment.getBaseAddress() + compilerSpecificMethodsOffset + m.offset;
			if(dbg) vrb.print("    > " + m.name + ": Offset = 0x" + Integer.toHexString(m.offset) + ", Index = 0x" + Integer.toHexString(m.index) + ", Address = 0x" + Integer.toHexString(m.address) + "\n");
			m = (Method)m.next;
		}
		if(dbg) vrb.println("\n[LINKER] END: Calculating absolute addresses for compiler specific methods.\n");
	}
	
	public static void updateConstantBlock(Class clazz) {
		if(dbg) vrb.println("[LINKER] START: Updating constant block for class \"" + clazz.name +"\":\n");

		if(dbg) vrb.println("  Inserting code base");
		((FixedValueItem)clazz.codeBase).setValue(clazz.codeSegment.getBaseAddress() + clazz.codeOffset); // codeBase
		// codeSize already set...
		if(dbg) vrb.println("  Inserting var base");
		((FixedValueItem)clazz.varBase).setValue(clazz.varSegment.getBaseAddress() + clazz.varOffset); // varBase
		if(dbg) vrb.println("  Inserting var size");
		((FixedValueItem)clazz.varBase.next).setValue(clazz.classFieldsSize); // varSize
		
		if(dbg) vrb.println("  Inserting object size");
		((FixedValueItem)clazz.typeDescriptor).setValue(clazz.objectSize); // size
		
		// Calculate checksum
		if(dbg) vrb.println("  Calculating checksum:");
		int fcs = BlockItem.setCRC32((FixedValueItem)clazz.constantBlockChecksum);
		if(dbg) vrb.println(String.format("CRC32: 0x%08X", fcs));
		
		if(dbg) vrb.println("\n[LINKER] END: Updating constant block for class \"" + clazz.name +"\"\n");
	}
	
	public static void updateSystemTable() {
		if(dbg) vrb.println("[LINKER] START: Updating system table\n");
		
		Item item = Type.classList;
		while(item != null) {
			if(item instanceof Class  && ((item.accAndPropFlags & (1 << apfInterface)) == 0)){
				Class clazz = (Class)item;
				// Code
				monitorUsedSpace(((FixedValueItem)clazz.codeBase).getValue(), ((FixedValueItem)clazz.codeBase.next).getValue());
				// Constant block
				monitorUsedSpace(clazz.constSegment.getBaseAddress() + clazz.constOffset, ((FixedValueItem)clazz.constantBlock).getValue());
			}
			else if(item instanceof Array) {
				Array array = (Array)item;
				monitorUsedSpace(array.address - slotSize ,array.typeDescriptor.getBlockSize());
			}
			item = item.next;
		}
		
		if(sysTabSegments != null && sysTabSegments.length > 0) {
			for(int i = 0; i < sysTabSegments.length; i++) {
				monitorUsedSpace(sysTabSegments[i].getBaseAddress(), systemTable.getBlockSize()); 
			}
		}
		
		int sizeToCopy = lastUsedAddress - firstUsedAddress;
		
		if(dbg) vrb.println("  Setting \"sizeToCopy\" to " + sizeToCopy + " bytes");
		
		sysTabSizeToCopy.setValue(sizeToCopy);
		
		if(dbg) vrb.println("\n[LINKER] END: Updating system table\n");
	}
	
	public static void generateTargetImage() {
		
		if(dbg) vrb.println("[LINKER] START: Generating target image:\n");
		
		Item item = Type.classList;
		Method m;
		while(item != null) {
			if (item instanceof Class && ((item.accAndPropFlags & (1 << apfInterface)) == 0)) {
				Class clazz = (Class)item;
				if(dbg) vrb.println("  Proceeding class \"" + clazz.name + "\":");
				// code
				m = (Method)clazz.methods;
				if(dbg) vrb.println("    1) Code:");
				while(m != null) {
					if(m.machineCode != null) {
						if(dbg) vrb.println("         > Method \"" + m.name + "\":");
						addTargetMemorySegment(new TargetMemorySegment(clazz.codeSegment, m.address, m.machineCode.instructions, m.machineCode.iCount));
					}
					m = (Method)m.next;
				}
				
				// consts
				if(dbg) vrb.println("    2) Constantblock:");
				addTargetMemorySegment(new TargetMemorySegment(clazz.constSegment, clazz.constSegment.getBaseAddress() + clazz.constOffset, clazz.constantBlock));
			}
			else if(item instanceof Array){
				Array array = (Array)item;
				if(dbg) vrb.println("  Proceeding array \"" + array.name + "\":");
				addTargetMemorySegment(new TargetMemorySegment(array.segment, array.segment.getBaseAddress() + array.offset, array.typeDescriptor));
			}
			item = item.next;
		}

		if(dbg) vrb.println("  Proceeding system table(s):");
		Segment[] s = Configuration.getSysTabSegments();
		if(dbg) vrb.println("  > Address: 0x" + Integer.toHexString(s[0].getBaseAddress()));
		for(int i = 0; i < sysTabSegments.length; i++) {
			addTargetMemorySegment(new TargetMemorySegment(s[i], s[i].getBaseAddress(), systemTable)); // TODO@Martin: Implement additive system table for ram/flash boot
		}
		
		if(dbg) vrb.println("  Proceeding global constant table:");
		addTargetMemorySegment(new TargetMemorySegment(globalConstantTableSegment, globalConstantTableSegment.getBaseAddress() + globalConstantTableOffset, globalConstantTable));
		
		if(dbg) vrb.println("  Proceeding compiler specific methods:");
		Method cssr = Method.compSpecSubroutines;
		while(cssr != null) {
			if(cssr.machineCode != null) {				
				addTargetMemorySegment(new TargetMemorySegment(compilerSpecSubroutinesSegment, cssr.address, cssr.machineCode.instructions, cssr.machineCode.iCount));
			}
			else {
				reporter.error(750, "of compiler specific subroutine: " + cssr.name);
			}
			cssr = (Method)cssr.next;
		}
		
		
		if(dbg) vrb.println("[LINKER] END: Generating target image\n");
	}
	
	public static void writeTargetImageToDtimFile(String fileName) throws IOException {
		if(dbg) vrb.println("[LINKER] START: Writing target image to file: \"" + fileName +"\":\n");
		
		FileOutputStream timFile = new FileOutputStream(fileName); // TODO @Martin: use DataOutputStream!
			
		timFile.write("#dtim-0\n".getBytes()); // Header (8 Byte)
		
		TargetMemorySegment tms = targetImage;
		int i = 0;
		while(tms != null) {
			if(dbg) vrb.println("TMS #" + i + ": Startaddress = 0x" + Integer.toHexString(tms.startAddress) + ", Size = 0x" + Integer.toHexString(tms.data.length * 4));
			timFile.write(getBytes(tms.startAddress));
			timFile.write(getBytes(tms.data.length*4));
			for(int j = 0; j < tms.data.length; j++) {
				timFile.write(getBytes(tms.data[j]));
			}
			i++;
			tms = tms.next;
		}
		
		timFile.close();
		if(dbg) vrb.println("[LINKER] END: Writing target image to file.\n");
	}
	
	public static void writeTargetImageToBinFile(String fileName) throws IOException {
		if(dbg) vrb.println("[LINKER] START: Writing target image to file: \"" + fileName +"\":\n");
		
		String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
		String pathAndFileName = fileName.substring(0, fileName.lastIndexOf('.'));
		String currentFileName;
		FileOutputStream binFile = null; // TODO @Martin: use DataOutputStream!
		
		TargetMemorySegment tms = targetImage;
		Device dev = tms.segment.owner;
		currentFileName = new String(pathAndFileName + "." +dev.getName() + fileExtension);
		binFile = new FileOutputStream(currentFileName);
		if(dbg) vrb.println("  Writing to file: " + currentFileName);
		int currentAddress = dev.getbaseAddress();
		while(tms != null) {
			while(currentAddress < tms.startAddress) {
				binFile.write(0);
				currentAddress++;
			}
			if(dbg) vrb.println("  > TMS #" + tms.id + ": Startaddress = 0x" + Integer.toHexString(tms.startAddress) + ", Size = 0x" + Integer.toHexString(tms.data.length * 4) + ", current address = " + Integer.toHexString(currentAddress));
			for(int j = 0; j < tms.data.length; j++) {
				binFile.write(getBytes(tms.data[j]));
				currentAddress += 4;
			}
			
			if(tms.next != null && tms.next.segment.owner != dev) {
				binFile.close();
				dev = tms.next.segment.owner;
				currentFileName = new String(pathAndFileName + "." +dev.getName() + fileExtension);
				binFile = new FileOutputStream(currentFileName);
				currentAddress = dev.getbaseAddress();
				if(dbg) vrb.println("  Writing to file: " + currentFileName);
			}
			tms = tms.next;
		}
		binFile.close();
		if(dbg) vrb.println("[LINKER] END: Writing target image to file.\n");
		log.println("Image file generated");
	}
	
	public static void writeCommandTableToFile(String fileName) throws IOException {
		if(dbg) vrb.println("[LINKER] START: Writing command table to file: \"" + fileName +"\":\n");
		
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date buildTime = new Date();
		
        BufferedWriter tctFile = new BufferedWriter(new FileWriter(fileName));
        tctFile.write("#dtct-0\n");
        tctFile.write("#File created: " + f.format(buildTime));
        tctFile.write("\n\n");
        
        DataItem cmdAddrField;
        int cmdAddr = -1;
        Class kernel = (Class)Type.classList.getItemByName(Configuration.getKernelClassname().toString());
        if(kernel != null) {
        	if(dbg) vrb.println("  Kernel: " + kernel.name);
        	cmdAddrField = (DataItem)kernel.classFields.getItemByName("cmdAddr");
        	if(cmdAddrField != null) {
        		if(dbg) vrb.println("  cmdAddrField: " + cmdAddrField.name + "@" + cmdAddrField.address);
        		cmdAddr = cmdAddrField.address;
        	}
        	else reporter.error(790, "Field cmdAddrField in the Kernel not set");
        }
        else reporter.error(701, "Kernel (" + Configuration.getKernelClassname() + ")");
        
        tctFile.write("cmdAddr@");
        tctFile.write(String.valueOf(cmdAddr));
        tctFile.write("\n\n");
        
        Item clazz = Type.classList;
        Method method;
        
        while(clazz != null) {
        	if(clazz instanceof Class) {
        		if(dbg) vrb.println("  Proceeding class \"" + clazz.name + "\"");
	        	method = (Method)((Class)clazz).methods;
	        	
	        	tctFile.write('>');
	        	tctFile.write(clazz.name.toString());
	        	tctFile.write('@');
	        	tctFile.write(String.valueOf(clazz.address));
	        	tctFile.write(" {\n");
	        	
	        	while(method != null) {
	        		if((method.accAndPropFlags & (1 << dpfCommand)) != 0) {
	        			if(dbg) vrb.println("\t\"" + method.name + "\" added");
		        		tctFile.write("\t!");
		        		tctFile.write(method.name.toString());
		        		tctFile.write('@');
		        		tctFile.write(String.valueOf(method.address));
		        		tctFile.write('\n');
	        		}
	        		method = (Method)method.next;
	        	}
	        	tctFile.write("}\n\n");
	        }
        	clazz = clazz.next;
        }
        tctFile.close();
        
        vrb.println("Command table written to \"" + fileName + "\"");
	
		if(dbg) vrb.println("[LINKER] END: Writing command table to file.");
	}
		
	public static void addGlobalConstant(StdConstant gconst) {
		if(globalConstantTable == null) {
			globalConstantTable = new ConstantItem(gconst);
		}
		else {
			globalConstantTable.append(new ConstantItem(gconst));
		}
	}
	
	
	/* ---------- private helper methods ---------- */
	
	private static byte[] getBytes(int number) {
		byte[] barray = new byte[4];
		for (int i = 0; i < 4; ++i) {
		    int shift = i << 3;
		    barray[3-i] = (byte)((number & (0xff << shift)) >>> shift);
		}
		return barray;
	}
	
	private static void setBaseAddress(Segment s, int baseAddress) {
		//descend
		if(s.subSegments != null) setBaseAddress(s.subSegments, baseAddress);
		//set baseaddress
		if((s.getSize() > 0 && s.getUsedSize() > 0) || ((s.getAttributes() & ((1 << atrStack) | (1 << atrHeap) | (1 << atrSysTab))) != 0)){ 
			if(s.getBaseAddress() == -1) s.setBaseAddress(baseAddress);
			if(dbg) vrb.println("    setting base address of segment " + s.getName() +" to "+ Integer.toHexString(baseAddress) + " with the size of " + s.getSize() + " bytes");
		}
		// traverse from left to right
		if(s.next != null) setBaseAddress((Segment)s.next, s.getSize()+ baseAddress);
	}

	private static Segment getFirstFittingSegment(Segment s, byte contentAttribute, int requiredSize) {
		Segment t = s;
		while(t != null) {
			if((t.getAttributes() & (1 << contentAttribute)) != 0) {
				if(t.subSegments != null) t = getFirstFittingSegment(t.subSegments, contentAttribute, requiredSize);
				if(t.getSize() <= 0 || t.getSize() - t.getUsedSize() > requiredSize) return t;
			}
			t = (Segment)t.next;
		}
		return null;
	}

	private static void setSegmentSize(Segment s) {
		if(s.subSegments != null && s.subSegments.getTail() != null) {
			setSegmentSize((Segment)s.subSegments.getTail());
		}
		if(s.getSize() <= 0) {
			if(dbg) vrb.println("    setting used size for segment " + s.getName());
			s.setSize(roundUpToNextWord(s.getUsedSize()));
		}
		else if(s.getSize() < s.getUsedSize()) { 
			reporter.error(711, "Segment " + s.getName() + " is too small! Size is manually set to " + s.getSize() + " byte, but required size is " + s.getUsedSize() + " byte!\n");
		}
		if(s.prev != null) {
			setSegmentSize((Segment)s.prev);
		}
	}
		
	protected static int roundUpToNextWord(int val) {
		return (val + (slotSize - 1)) & -slotSize;
	}
	
	private static void addTargetMemorySegment(TargetMemorySegment tms) {
		if(targetImage == null) {
			if(dbg) vrb.println("      >>>> Adding first target memory segment (#" + tms.id + ")");
			targetImage = tms;
		}
		else {
			TargetMemorySegment current = targetImage;
			if(current.startAddress < tms.startAddress) {
				while(current.next != null && tms.startAddress > current.next.startAddress) {
					current = current.next;
				}
				if(dbg) vrb.println("      >>>> Inserting target memory segment #" + tms.id + " after target memory segment #" + current.id);
				tms.next = current.next;
				current.next = tms;
			}
			else {
				if(dbg) vrb.println("      >>>> Inserting target memory segment #" + tms.id + " before first target memory segment segment");
				tms.next = current;
				targetImage = tms;
			}
		}
	}
	
	private static boolean checkConstantPoolType(Item item) {
		// TODO @Martin: Make this configurable...
		return item instanceof StdConstant && ((item.type == Type.wellKnownTypes[txFloat] || item.type == Type.wellKnownTypes[txDouble]));
	}
	
	private static void monitorUsedSpace(int startAddress, int size) {
		if(startAddress < firstUsedAddress) firstUsedAddress = startAddress;
		if(startAddress + size > lastUsedAddress) lastUsedAddress = startAddress + size;
	}
	
	/* ---------- debug primitives ---------- */
	
	public static void printSystemTable() {
		vrb.println("Size: " + systemTableSize + " byte");
		systemTable.printList();
	}

	public static void printTargetImage() {
		TargetMemorySegment tms = targetImage;
		while(tms != null) {
			vrb.print(tms);
			tms = tms.next;
		}
	}

	public static void printTargetImageSegmentList() {
		vrb.println("ID\tstart address\tsize (byte)");
		TargetMemorySegment tms = targetImage;
		while(tms != null) {
			vrb.println("#" + tms.id + "\t[" + String.format("[0x%08X]", tms.startAddress) + "]\t" + tms.data.length * 4);
			tms = tms.next;
		}
	}
	
	public static void printClassList() {
		printClassList(true, true, true, true);
	}

	public static void printClassList(boolean printMethods, boolean printFields, boolean printConstantFields, boolean printConstantBlock) {
		Method m;
		Item f;
		int cc = 0, mc = 0, fc = 0;
		Item item = Type.classList;
		while(item != null) {
			if (item instanceof Class && ((item.accAndPropFlags & (1 << apfInterface)) == 0)) {
				Class c = (Class)item;
				vrb.println("CLASS: " + c.name + " (#" + cc++ + ")");
				vrb.println("Number of class methods:     " + c.nofClassMethods);
				vrb.println("Number of instance methods:  " + c.nofInstMethods);
				vrb.println("Number of class fields:      " + c.nofClassFields);
				vrb.println("Number of instance fields:   " + c.nofInstFields);
				vrb.println("Number of interfaces:        " + c.nofInterfaces);
				vrb.println("Number of base classes:      " + c.extensionLevel);
				vrb.println("Number of references:        " + c.nofClassRefs);
				vrb.println("Max extension level:         " + Class.maxExtensionLevelStdClasses);
				vrb.println("Machine code size:           " + ((FixedValueItem)c.codeBase.next).getValue() + " byte");
				vrb.println("Constant block size:         " + ((FixedValueItem)c.constantBlock).getValue() + " byte");
				vrb.println("Class fields size:           " + c.classFieldsSize + " byte");
				vrb.println("Code offset:                 0x" + Integer.toHexString(c.codeOffset));
				vrb.println("Var offset:                  0x" + Integer.toHexString(c.varOffset));
				vrb.println("Const offset:                0x" + Integer.toHexString(c.constOffset));
				vrb.println("Code segment:                " + c.codeSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.codeSegment.getBaseAddress()) + ", size: " + c.codeSegment.getSize() + " byte)");
				vrb.println("Var segment:                 " + c.varSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.varSegment.getBaseAddress()) + ", size: " + c.varSegment.getSize() + " byte)");
				vrb.println("Const segment:               " + c.constSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.constSegment.getBaseAddress()) + ", size: " + c.constSegment.getSize() + " byte)");
				vrb.println("Type descriptor address:     0x" + Integer.toHexString(c.address));
				vrb.println("Constant block base address: 0x" + Integer.toHexString(c.constSegment.getBaseAddress() + c.constOffset));
				vrb.println("Code base address:           0x" + Integer.toHexString(c.codeSegment.getBaseAddress() + c.codeOffset));
				vrb.println("Class field base address:    0x" + Integer.toHexString(c.varSegment.getBaseAddress() + c.varOffset));
				
				if(printMethods) {
					vrb.println("Methods:");
					m = (Method)c.methods;
					mc = 0;
					if(m == null) vrb.println(">  No methods in this class");
					else {
						while(m != null) {
							vrb.println("> Method #" + mc++ + ": " + m.name +  m.methDescriptor);
							vrb.println("  Flags:     0x" + Integer.toHexString(m.accAndPropFlags));
							if((m.accAndPropFlags & ((1 << dpfNew) | (1 << dpfUnsafe) | (1 << dpfSysPrimitive) | (1 << dpfSynthetic))) != 0) {	
								if((m.accAndPropFlags & (1 << dpfNew)) != 0) {
									vrb.println("  Special:   New");
								}
								if((m.accAndPropFlags & (1 << dpfUnsafe)) != 0) {
									vrb.println("  Special:   Unsafe");
								}
								if((m.accAndPropFlags & (1 << dpfSysPrimitive)) != 0) {
									vrb.println("  Special:   System primitive");
								}
								if((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
									vrb.println("  Special:   Synthetic");
								}
								vrb.println("  Static:    yes");
							}
							else {
								if((m.accAndPropFlags & (1 << apfStatic)) != 0) vrb.println("        Static:    yes"); else vrb.println("        Static:    no");
							}
							vrb.println("  address:   0x" + Integer.toHexString(m.address));
							vrb.println("  offset:    0x" + Integer.toHexString(m.offset));
							vrb.println("  index:     0x" + Integer.toHexString(m.index));
							if(m.machineCode != null)
								vrb.println("  Code size: 0x" + Integer.toHexString(m.machineCode.iCount * 4) + " (" + m.machineCode.iCount * 4 +" byte)");
							m = (Method)m.next;
						}
					}
				}
				if(printFields) {
					vrb.println("Fields:");
					f = c.instFields;
					fc = 0;
					if(f == null) vrb.println("  No fields in this class");
					else {
						while(f != null) {
							if(printConstantFields || (f.accAndPropFlags & (1 << dpfConst)) == 0) { // printConstantsField || !constant
								vrb.println("> Field #" + fc++ + ": " + f.name);
								vrb.println("  Type:     " + f.type.name);
								vrb.println("  Flags:    0x" + Integer.toHexString(f.accAndPropFlags));
								if((f.accAndPropFlags & (1 << apfStatic)) != 0) vrb.println("  Static:   yes"); else vrb.println("        Static:   no");
								if((f.accAndPropFlags & (1 << dpfConst)) != 0) vrb.println("  Constant: yes"); else vrb.println("        Constant: no");
								vrb.println("  address:  0x" + Integer.toHexString(f.address));
								vrb.println("  offset:   0x" + Integer.toHexString(f.offset));
								vrb.println("  index:    0x" + Integer.toHexString(f.index));
							}
							f = f.next;
						}
					}
				}
				
				if(printConstantBlock) {
					vrb.println("Constant block:");
					c.printConstantBlock();
				}
				vrb.println("----------------------------------------------------------------------");
			}
			
			else if(item instanceof Array) {
				Array a = (Array)item;
				
				vrb.println("ARRAY: " + a.name);
				vrb.println("Component type: " + a.componentType.name);
				//vrb.println("Check type: " + a.checkType.name);
				vrb.println("Type descriptor:");
				a.typeDescriptor.printList();
				vrb.println("----------------------------------------------------------------------");
			}
			item = item.next;
		}
	}
	
	public static void printGlobalConstantTable() {	
		vrb.println("Size: " + globalConstantTable.getBlockSize() + " byte");
		globalConstantTable.printList();
	}
	
}