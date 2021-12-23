/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.linker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.CRC32;

import org.deepjava.classItems.Array;
import org.deepjava.classItems.Class;
import org.deepjava.classItems.Field;
import org.deepjava.classItems.ICclassFileConsts;
import org.deepjava.classItems.ICdescAndTypeConsts;
import org.deepjava.classItems.InterfaceList;
import org.deepjava.classItems.Item;
import org.deepjava.classItems.Method;
import org.deepjava.classItems.RefType;
import org.deepjava.classItems.StdConstant;
import org.deepjava.classItems.StringLiteral;
import org.deepjava.classItems.Type;
import org.deepjava.config.Configuration;
import org.deepjava.config.Device;
import org.deepjava.config.Segment;
import org.deepjava.dwarf.DebugSymbols;
import org.deepjava.host.ErrorReporter;
import org.deepjava.host.StdStreams;
import org.deepjava.strings.HString;

import nl.lxtreme.binutils.elf.AbiType;
import nl.lxtreme.binutils.elf.Elf;
import nl.lxtreme.binutils.elf.ElfClass;
import nl.lxtreme.binutils.elf.Flags;
import nl.lxtreme.binutils.elf.MachineType;
import nl.lxtreme.binutils.elf.ObjectFileType;
import nl.lxtreme.binutils.elf.SectionHeader;
import nl.lxtreme.binutils.elf.SectionType;
import nl.lxtreme.binutils.elf.SegmentType;

public class Linker32 implements ICclassFileConsts, ICdescAndTypeConsts {
	
	// Slot size:
	public static final byte slotSize = 4; // 4 bytes

	public static final boolean dbg = false; // enable/disable debugging outputs for the linker

	// Class/type descriptor:
	public static final int tdSizeOffset = 0;
	public static final int tdExtensionLevelOffset = tdSizeOffset - 4;
	public static final int tdMethTabOffset = tdExtensionLevelOffset - 4;
	public static final int tdClassNameAddrOffset = tdSizeOffset + 4;
	public static final int tdInstPtrTableOffset = tdClassNameAddrOffset + 4;
	public static final int tdIntfTypeChkTableOffset = tdInstPtrTableOffset + 4;
	public static final int tdBaseClass0Offset = tdIntfTypeChkTableOffset + 4;
	public static int typeTableLength = -1;	// size of type table in bytes
	
	// String pool:
	public static final int stringHeaderConstSize = 3 * 4;
	public static final int spTagIndex = 1;
	public static final int spTagOffset = spTagIndex * 4;
	public static int stringHeaderSize = -1;
	
	// Error reporter and stdout:
	private static final ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream vrb = StdStreams.vrb;
	@SuppressWarnings("unused")
	private static PrintStream log = StdStreams.log;

	// Target image
	public static TargetMemorySegment targetImage;

	// Constant block: set by the configuration
	public static int cblkConstBlockSizeOffset;
	public static int cblkCodeBaseOffset;
	public static int cblkCodeSizeOffset;
	public static int cblkVarBaseOffset;
	public static int cblkVarSizeOffset;
	public static int cblkClinitAddrOffset;
	public static int cblkNofPtrsOffset;
	public static int cblkPtrAddr0Offset;
	
	// System table: set by the configuration
	public static int stClassConstOffset;
	public static int stStackOffset;
	public static int stHeapOffset;
	public static int stKernelClinitAddr;
	public static int stResetOffset;
	public static int stSizeToCopy;
	public static int stNofStacks;
	
	public static ConstBlkEntry systemTable;
	private static Segment[] sysTabSegments;
	private static FixedValueEntry sysTabSizeToCopy;
	private static int firstUsedAddress = Integer.MAX_VALUE;
	private static int lastUsedAddress = 0;
	
	// Global constants
	public static ConstBlkEntry globalConstantTable;
	private static int globalConstantTableOffset = -1;
	private static Segment globalConstantTableSegment;
	
	// Compiler specific methods
	private static int compilerSpecificMethodsCodeSize = 0;
	private static int compilerSpecificMethodsOffset = -1;
	private static Segment compilerSpecSubroutinesSegment;

	// Endianess of target
	public static boolean bigEndian;
	
	
	public static void init() {
		if(dbg) vrb.println("[LINKER] START: Initializing:");
		
		if(dbg) vrb.println("  Reseting static variables");
		firstUsedAddress = Integer.MAX_VALUE;
		lastUsedAddress = 0;
		
		if(dbg) vrb.print("  Setting size of string header: ");
		stringHeaderSize = stringHeaderConstSize + Type.wktObject.objectSize;
		if(dbg) vrb.println(stringHeaderSize + " byte");
		
		if(dbg) vrb.println("  Looking for segments for the system table: ");
		sysTabSegments = Configuration.getSysTabSegments();
		if (sysTabSegments != null && sysTabSegments.length > 0) {
			if (dbg) {
				for (int i = 0; i < sysTabSegments.length; i++) {
					vrb.println("     -> found: " + sysTabSegments[i].name);
				}
			}
		}
		else {
			reporter.error(710, "No segment(s) for the system table defined!");
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
		TargetMemorySegment.clearCounter();
		
		if(dbg) vrb.println("[LINKER] END: Initializing.\n");
	}

	public static void createConstantBlock(Class clazz) {			
		if (dbg) vrb.println("[LINKER] START: Preparing constant block for class \"" + clazz.name +"\":");
		if (dbg) {
			vrb.println("=== Extended Method Table ===");
			for (int i = 0; i < clazz.methTable.length; i++) {
				vrb.println(clazz.methTable[i].name);
				vrb.println("[" + i + "] Name = " + clazz.methTable[i].name + " Index = " + Integer.toHexString(clazz.methTable[i].index));
			}
			vrb.println("=============================");
		}

		// Header
		if (dbg) vrb.println("   Creating header");
		clazz.constantBlock = new FixedValueEntry("constBlockSize");
		clazz.codeBase = new FixedValueEntry("codeBase");
		clazz.constantBlock.appendTail(clazz.codeBase);
		clazz.constantBlock.appendTail(new FixedValueEntry("codeSize"));
		clazz.constantBlock.appendTail(new FixedValueEntry("varBase"));
		clazz.constantBlock.appendTail(new FixedValueEntry("varSize"));
		Method classConstructor = clazz.getClassConstructor();
		if (classConstructor != null) clazz.constantBlock.appendTail(new AddressEntry(classConstructor));
		else clazz.constantBlock.appendTail(new FixedValueEntry("<clinit>", -1));

		// Pointer list (list of class fields which are references)
		if (dbg) vrb.println("   Creating pointer list (class fields)");
		FixedValueEntry classPtrList = new FixedValueEntry("nofClassPtrs");
		int ptrCounter = 0;
		if (clazz.nofClassRefs > 0) {
			Item field = clazz.firstClassReference;
			while (field != clazz.constFields) {
				if (((Type)field.type).category == tcRef || ((Type)field.type).category == tcArray) {
					assert (field.accAndPropFlags & (1 << dpfConst)) == 0 && (field.accAndPropFlags & (1 << apfStatic)) != 0;
					classPtrList.appendTail(new AddressEntry(field));
					ptrCounter++;
				}
				field = field.next;
			}
			assert ptrCounter == clazz.nofClassRefs : "[Error] Number of added pointers (" + ptrCounter + ") not equal to number of pointers in class (" + clazz.nofClassRefs + ")!";
		}
		classPtrList.setValue(ptrCounter);
		clazz.constantBlock.appendTail(classPtrList);

		// Type descriptor: size and extension level
		if(dbg) vrb.println("   Creating type descriptor");
		if(dbg) vrb.println("    - Beginning with size");
		clazz.typeDescriptor = new FixedValueEntry("size");
		Item typeDescriptorHead = clazz.typeDescriptor;
		if(dbg) vrb.println("    - Inserting the extension level");
		typeDescriptorHead = clazz.typeDescriptor.insertHead(new FixedValueEntry("extensionLevel", clazz.extensionLevel));

		// Type descriptor: create method table
		if ((clazz.accAndPropFlags & (1 << apfInterface)) == 0) {	// is not interface 
			if(dbg) vrb.println("    - Inserting method table:");
			for(int i = 0; i < clazz.methTabLength; i++) {
				typeDescriptorHead = typeDescriptorHead.insertHead(new AddressEntry(clazz.methTable[i])); 
			}
		}

		// Type descriptor: insert class name address
		if(dbg) vrb.println("    - Inserting class name address");
		clazz.typeDescriptor.appendTail(new FixedValueEntry("classNameAddr", 0x12345678));

		// Type descriptor: insert instance pointer table offset
		if(dbg) vrb.println("    - Inserting instance pointer table offset");
		FixedValueEntry instPtrTableOffset = new FixedValueEntry("instPtrOffset");
		clazz.typeDescriptor.appendTail(instPtrTableOffset);

		// Type descriptor: insert interface type check table offset
		if(dbg) vrb.println("    - Inserting interface type check table offset");
		FixedValueEntry intfTypeChkTableOffset = new FixedValueEntry("intfTypeCheckOffset");
		clazz.typeDescriptor.appendTail(intfTypeChkTableOffset);

		assert getBlockSize(clazz.typeDescriptor) == tdBaseClass0Offset : "type descriptor not properly built";

		if ((clazz.accAndPropFlags & (1 << apfInterface)) == 0) {	// is not interface
			// Type descriptor: create type table (base classes)
			if(dbg) vrb.println("    - Inserting base classes:");
			Class baseClass = (Class)clazz.type;
			AddressEntry typeTable = new AddressEntry(clazz);
			Item typeTableHead = typeTable;
			for (int i = 0; i < Class.maxExtensionLevelStdClasses; i++) {
				if (baseClass != null) {
					if (dbg) vrb.println("      > " + baseClass.name);
					typeTableHead = typeTableHead.insertHead(new AddressEntry(baseClass));
					baseClass = (Class)baseClass.type;
				} else {
					if (dbg) vrb.println("      > 0 (padding)");
					typeTable.appendTail(new FixedValueEntry("padding", 0));
				}
			}
			typeTableLength = getBlockSize(typeTableHead);
			clazz.typeDescriptor.appendTail(typeTableHead);

			// Type descriptor: add interface table for interface methods
			InterfaceList list = clazz.intfCallList;
			if (list != null) {
				AddressEntry interfaceTable;				
				if (list.length != 1 || (list.length == 1) && list.getFront().methTabLength != 1) {
					if(dbg) vrb.print("    - Inserting interface table, length=");
					int len = clazz.intfCallList.length;
					if(dbg) vrb.println(len);

					interfaceTable = new AddressEntry(Method.getCompSpecSubroutine("imDelegIiMm"));
					int n = 0;
					while (n < len) {	// insert interfaces
						Class intf = clazz.intfCallList.getInterfaceAt(n);
						if(dbg) vrb.println("      > interface: " + intf.name);
						int id = intf.index;
						if (n == len - 1) id = 0;	// set id of last interface to 0
						interfaceTable.appendTail(new InterfaceEntry(intf.name, (short)id, (short)0));
						n++;
					}
					n = 0;
					int bmo = tdBaseClass0Offset + typeTableLength + (len+1) * Linker32.slotSize;	// method offset from field "size"
					while (n < len) {	// insert methods
						Class intf = clazz.intfCallList.getInterfaceAt(n);
						if(dbg) vrb.println("      methods for interface: " + intf.name);
						InterfaceEntry ie = (InterfaceEntry)interfaceTable.getItemByName(intf.name);
						ie.setBmo(bmo);	//correct method offset
						Method intfMeth = (Method)intf.methods;
						while (intfMeth != null) {
							if ((intfMeth.accAndPropFlags & (1<<apfStatic)) == 0) {	// omit static methods in interfaces
								if(dbg) vrb.println("        - " + intfMeth.name);
								Method[] table = clazz.methTable;
								int k = 0;
								while (intfMeth.name != table[k].name || intfMeth.methDescriptor != table[k].methDescriptor) k++;
								Method clsMeth = table[k];
								assert clsMeth != null : "interface method not found in class method table";
								interfaceTable.appendTail(new AddressEntry(clsMeth));
								bmo += Linker32.slotSize;
							}
							intfMeth = (Method)intfMeth.next;
						} 
						n++;
					}
				} else {	// 1 interface with 1 method, insert method directly
					Method[] table = clazz.methTable;
					Method intfMeth = (Method)list.getFront().methods;
					if ((intfMeth.accAndPropFlags & (1<<apfStatic)) != 0) intfMeth = (Method)intfMeth.next;	// first method could be clinit
					assert intfMeth != null : "no method in interface";
					int k = 0;
					while (intfMeth.name != table[k].name || intfMeth.methDescriptor != table[k].methDescriptor) k++;
					Method clsMeth = table[k];
					assert clsMeth != null : "interface method not found in class method table";
					interfaceTable = new AddressEntry(clsMeth);
				}

				clazz.typeDescriptor.appendTail(interfaceTable);
			}
		}

		// Type descriptor: interface type check table
		if (dbg) vrb.println("   Creating interface type check table");
		FixedValueEntry intfTypeChkTable = null;
		int len = 0;
		if (clazz.intfTypeChkList != null) len = clazz.intfTypeChkList.length;
		if (len > 0) {
			for (int n = 0; n < len; n += 2) {
				int val;
				if (bigEndian) {	// interfaces id's will be read per two byte entry
					Class interf = clazz.intfTypeChkList.getInterfaceAt(n);
					val = interf.chkId << 16;
					if (n < len - 1) {
						interf = clazz.intfTypeChkList.getInterfaceAt(n + 1);
						val |= interf.chkId;
					}
				} else {
					Class interf = clazz.intfTypeChkList.getInterfaceAt(n);
					val = interf.chkId;
					if (n < len - 1) {
						interf = clazz.intfTypeChkList.getInterfaceAt(n + 1);
						val |= interf.chkId  << 16;
					}				
				}
				if (intfTypeChkTable == null)
					intfTypeChkTable = new FixedValueEntry("intf id " + n, val);
				else
					intfTypeChkTable.appendTail(new FixedValueEntry("intf id " + n, val));
			}
			if (len % 2 == 0) {	// make sure last entry is 0
				intfTypeChkTable.appendTail(new FixedValueEntry("intf id " + len, 0));
			}
			clazz.typeDescriptor.appendTail(intfTypeChkTable);
		} else {
			if ((clazz.accAndPropFlags & (1 << apfInterface)) != 0) {
				// there must be an empty entry at least for interfaces
				intfTypeChkTable = new FixedValueEntry("intf id 0", 0); 
				clazz.typeDescriptor.appendTail(intfTypeChkTable);
			}
		}

		// Type descriptor: instance pointer table (list of instance fields which are references)
		FixedValueEntry instPtrTable = null;
		if ((clazz.accAndPropFlags & (1 << apfInterface)) == 0) {	// is not interface
			if (dbg) vrb.println("   Creating pointer table (instance fields)");
			instPtrTable = new FixedValueEntry("nofInstPtrs");
			ptrCounter = 0;
			if (clazz.nofInstRefs > 0 && clazz != Type.wktString) {	// do not include instance reference fields in class String 
				Item field = clazz.firstInstReference;
				while (field != clazz.classFields) {
					if (((Type)field.type).category == tcRef || ((Type)field.type).category == tcArray ) {
						assert (field.accAndPropFlags & (1 << dpfConst)) == 0 && (field.accAndPropFlags & (1 << apfStatic)) == 0;
						instPtrTable.appendTail(new OffsetEntry("instPtrOffset[" + ptrCounter + "]: ", field));
						ptrCounter++;
					}
					field = field.next;
				}
				assert ptrCounter == clazz.nofInstRefs : "[Error] Number of added pointers (" + ptrCounter + ") not equal to number of pointers in class (" + clazz.nofClassRefs + ")!";
			}
			instPtrTable.setValue(ptrCounter);
			clazz.typeDescriptor.appendTail(instPtrTable);
		}

		// calculate type descriptor size
		clazz.typeDescriptorSize = getBlockSize(typeDescriptorHead);

		// add type descriptor to constant block
		clazz.constantBlock.appendTail(typeDescriptorHead);

		// create string pool
		if (dbg) vrb.println("  Creating string pool");
		StringEntry stringPool = null;
		if (clazz.constPool != null) {
			Item cpe;
			for (int i = 0; i < clazz.constPool.length; i++) {
				cpe = clazz.constPool[i];
				if (cpe.type == Type.wellKnownTypes[txString] && (cpe.accAndPropFlags & (1<<dpfConst)) != 0) { // strings which are not marked as const must not be linked
					if (stringPool == null) stringPool = new StringEntry(cpe);
					else stringPool.appendTail(new StringEntry(cpe));
				}
			}
		}
		if (stringPool != null) {
			clazz.stringPoolSize = getBlockSize(stringPool);
			clazz.constantBlock.appendTail(stringPool);
		}

		// create constant pool
		if (dbg) vrb.println("  Creating constant pool");
		ConstantEntry constantPool = null;
		if (clazz.constPool != null) {
			Item cpe;
			for (int i = 0; i < clazz.constPool.length; i++) {
				cpe = clazz.constPool[i];
				if (checkConstantPoolType(cpe)) {
					if (constantPool == null) constantPool = new ConstantEntry(cpe);
					else constantPool.appendTail(new ConstantEntry(cpe));
				}
			}
		}
		if (constantPool != null) {
			clazz.constantPoolSize = getBlockSize(constantPool);
			clazz.constantBlock.appendTail(constantPool);
		}

		// append block item for checksum
		clazz.constantBlockChecksum = new FixedValueEntry("fcs", 0);
		clazz.constantBlock.appendTail(clazz.constantBlockChecksum);

		// Calculate size of constant block
		clazz.constantBlock.setValue(getBlockSize(clazz.constantBlock));

		// Calculating indexes and offsets for the string- and constant pool
		int offset, index;

		if (stringPool != null) {
			if (dbg) vrb.println("  Calculating indexes and offsets for the string pool entries");
			Item s = stringPool;
			offset = 0; index = 0;
			//				while (s != constantPool && s != instPtrTable && s != clazz.constantBlockChecksum) {
			while (s != constantPool && s != clazz.constantBlockChecksum) {
				((StringEntry)s).setIndex(index);
				((StringEntry)s).setOffset(offset);
				index++;
				offset += ((ConstBlkEntry)s).getItemSize();
				s = s.next;
			}
		}

		if (constantPool != null) {
			if(dbg) vrb.println("  Calculating indexes and offsets for the constant pool entries");
			Item c = constantPool;
			offset = 0; index = 0;
			//				while (c != instPtrTable && c != clazz.constantBlockChecksum) {
			while (c != clazz.constantBlockChecksum) {
				((ConstantEntry)c).setIndex(index);
				((ConstantEntry)c).setOffset(offset);
				index++;
				offset += ((ConstBlkEntry)c).getItemSize();
				c = c.next;
			}
		}

		// Calculating type descriptor offset
		Item i = clazz.constantBlock;
		offset = 0;
		while (i != clazz.typeDescriptor) {
			offset += ((ConstBlkEntry)i).getItemSize();
			i = i.next;
		}
		clazz.typeDescriptorOffset = offset;

		// Calculating interface type check list offset
		i = clazz.typeDescriptor;
		offset = 0;
		if (intfTypeChkTable != null) { 
			while (i != intfTypeChkTable) {
				offset += ((ConstBlkEntry)i).getItemSize();
				i = i.next;
			}
		}
		intfTypeChkTableOffset.setValue(offset);

		// Calculating instance pointer list offset
		if ((clazz.accAndPropFlags & (1 << apfInterface)) == 0) {	// is not interface
			i = clazz.typeDescriptor;
			offset = 0;
			while (i != instPtrTable) {
				offset += ((ConstBlkEntry)i).getItemSize();
				i = i.next;
			}
			instPtrTableOffset.setValue(offset);
		}

		if(dbg) vrb.println("\n[LINKER] END: Preparing constant block for class \"" + clazz.name +"\"\n");
	}
		
	public static void createTypeDescriptor(Array array) {
		if(dbg) vrb.println("[LINKER] START: Creating type descriptor for array \"" + array.name +"\":");
		if(dbg) vrb.println("  Element type: " + array.componentType.name);
		if(dbg) vrb.println("  Element size: " + array.componentType.sizeInBits / 8 + " byte (" + array.componentType.sizeInBits + " bit)");
		if(dbg) vrb.println("  Dimension:    " + array.dimension);
				
		// Extension level
		array.typeDescriptor = new FixedValueEntry("extensionLevel", 1); // the base type of an array is always object!
		
		// Array dimension, component size and array type flag
		byte arrayOfPrimitives = 0;
		if (array.componentType.category == tcPrimitive) arrayOfPrimitives = 1;
		array.typeDescriptor.appendTail(new FixedValueEntry("dimension/size", ((arrayOfPrimitives << 31) | array.dimension << 16) | (array.componentType.sizeInBits / 8)));
		
		// Array name address
		array.typeDescriptor.appendTail(new FixedValueEntry("arrayNameAddr", 0x12345678));
				
		// List of type descriptors of arrays with the same component type but lower dimension
		Array a = array;
		for (int i = array.dimension; i > 0; i--) {
			assert a != null;			
			array.typeDescriptor.appendTail(new AddressEntry("arrayTD[" + i + "]: ", a));
			a = a.nextLowerDim;
		}
		
		// Component type
		if (array.componentType.category == tcPrimitive) // append value 0
			array.typeDescriptor.appendTail(new FixedValueEntry("arrayComponentTD: <primitive> (" + array.componentType.name + ")", 0));
		else 
			array.typeDescriptor.appendTail(new AddressEntry("arrayComponentTD: ", array.componentType));
		if (dbg) vrb.println("[LINKER] END: Creating type descriptor for array \"" + array.name +"\"\n");
	}

	public static void createGlobalConstantTable() {
		if(dbg) vrb.println("[LINKER] START: Creating global constant table:\n");
		int offset = 0, index = 0;
		ConstantEntry constant = (ConstantEntry)globalConstantTable;

		while (constant != null) {
			constant.setIndex(index);
			constant.setOffset(offset);
			constant.setAddress(globalConstantTableSegment.address + globalConstantTableOffset + offset);
			index++;
			offset += constant.getItemSize();
			constant = (ConstantEntry)constant.next;
		}
		if (dbg) vrb.println("[LINKER] END: Creating global constant table\n");
	}
	
	public static void calculateCodeSizeAndMethodOffsets(Class clazz) {
		if (dbg) vrb.println("[LINKER] Calculating code size and methods offsets for class \"" + clazz.name +"\":");
		Method m = (Method)clazz.methods;
		int codeSize = 0; // machine code size for the whole class
		while (m != null) {
			if (m.machineCode != null) {
				if (m.offset < 0) { // offset not given by configuration
					m.offset = codeSize;
					codeSize += m.getCodeSizeInBytes();
				}
				if(dbg) vrb.println("    > " + m.name + ": codeSize = " + m.getCodeSizeInBytes() + " byte");
			}
			m = (Method)m.next;
		}
		((FixedValueEntry)clazz.codeBase.next).setValue(codeSize);	// set code size	
		if (dbg) vrb.println("    Total code size: " + codeSize + " byte");		
	}
	
	public static void calculateCodeSizeAndOffsetsForCompilerSpecSubroutines() {
		if (dbg) vrb.println("[LINKER] START: Calculating code size for compiler specific methods:\n");
		Method m = Method.compSpecSubroutines;
		int codeSize = 0; // machine code size for all compiler specific subroutines
		while (m != null) {
			if (m.machineCode != null) {
				m.offset = codeSize;
				codeSize += (m.machineCode.iCount + m.machineCode.nofBlInstrs * 2) * 4; // iCount = number of instructions!
				if(dbg) vrb.println("    > " + m.name + ": codeSize = " + (m.machineCode.iCount + m.machineCode.nofBlInstrs * 2) * 4 + " byte");
			}
			m = (Method)m.next;
		}
		compilerSpecificMethodsCodeSize = codeSize;
		if (dbg) vrb.println("    Total code size: " + codeSize + " byte");
		if (dbg) vrb.println("\n[LINKER] END: Calculating code size for compiler specific methods\n");
	}
	
	public static void createSystemTable() {
		if (dbg) vrb.println("[LINKER] START: Create system table:\n");
		
		// Number of stacks, heaps and classes
		int nofStacks = Configuration.getNumberOfStacks();
		int nofHeaps = Configuration.getNumberOfHeaps();
		if (dbg) vrb.println("  Number of stacks:  " + nofStacks);
		if (dbg) vrb.println("  Number of heaps:   " + nofHeaps);
		if (dbg) vrb.println("  Number of classes: " + RefType.nofRefTypes);
		
		Item kernelClass = Configuration.getOS().kernelClass;
		if (kernelClass == null) {reporter.error(740, "kernel class not set"); return;}
		Item heapClass = Configuration.getOS().heapClass;
		if (heapClass == null) {reporter.error(740, "heap class not set"); return;}
		Item kernelClinit = null;
		kernelClinit = ((Class)kernelClass).getClassConstructor();
		if (dbg) vrb.println("  Kernel class:      " + kernelClass.name);
				
		// Create the system table
		systemTable = new FixedValueEntry("classConstOffset", stNofStacks + ( 2 * nofStacks + 2 * nofHeaps) * 4 + 12);
		systemTable.appendTail(new FixedValueEntry("stackOffset", stNofStacks));
		systemTable.appendTail(new FixedValueEntry("heapOffset", stNofStacks + 2 * nofStacks * 4 + 4));
		systemTable.appendTail(new AddressEntry("kernelClinitAddr: " + kernelClass.name + ".", kernelClinit));
		systemTable.appendTail(new FixedValueEntry("resetOffset", Configuration.getResetOffset()));
		sysTabSizeToCopy = new FixedValueEntry("sizeToCopy", -1);
		systemTable.appendTail(sysTabSizeToCopy);
		systemTable.appendTail(new FixedValueEntry("nofStacks", nofStacks));
		for (int i = 0; i < nofStacks; i++) { // reference to each stack and the size of each stack
			systemTable.appendTail(new AddressEntry("baseStack" + i + ": ", Configuration.getStackSegments()[i])); // base address
			systemTable.appendTail(new FixedValueEntry("sizeStack" + i, Configuration.getStackSegments()[i].size));
		}
		systemTable.appendTail(new FixedValueEntry("nofHeaps", nofHeaps));
		for (int i = 0; i < nofHeaps; i++) { //reference to each heap and the size of each heap
			systemTable.appendTail(new AddressEntry("baseHeap" + i + ": ", Configuration.getHeapSegments()[i])); // base address
			systemTable.appendTail(new FixedValueEntry("sizeHeap" + i, Configuration.getHeapSegments()[i].size));
		}
		systemTable.appendTail(new FixedValueEntry("nofClasses", Class.nofInitClasses + Class.nofNonInitClasses));
		
		// insert heap and kernel class at beginning of the list
		if (dbg) vrb.println("  init classes");
		int i = 0;
		systemTable.appendTail(new SysTabEntry("constBlkBaseClass" + i + ": ", (Class)heapClass));
		if (dbg) vrb.println("    add class " + heapClass.name);
		i++;
		systemTable.appendTail(new SysTabEntry("constBlkBaseClass" + i + ": ", (Class)kernelClass));
		if (dbg) vrb.println("    add class " + kernelClass.name);
		i++;
		
		Class clazz = Class.initClasses; 
		while (clazz != null) { // reference to the constant block of each class with a class constructor (clinit)
			if (clazz != heapClass && clazz != kernelClass) {
				assert clazz instanceof Class; 
				if ((clazz.accAndPropFlags & (1 << dpfSynthetic)) == 0) {	// omit synthetic classes
					systemTable.appendTail(new SysTabEntry("constBlkBaseClass" + i + ": ", clazz));
					if (dbg) vrb.println("    add class " + clazz.name);
					i++;
				}
			}
			clazz = clazz.nextClass;
		}
		clazz = Class.nonInitClasses;
		if (dbg) vrb.println("  noninit classes");
		while (clazz != null) { // reference to the constant block of each class without a class constructor (no clinit)
			assert clazz instanceof Class; 
			if ((clazz.accAndPropFlags & (1 << dpfSynthetic)) == 0) {	// omit synthetic classes
				if ((clazz.accAndPropFlags & (1 << apfInterface)) == 0) {
					systemTable.appendTail(new SysTabEntry("constBlkBaseClass" + i + ": ", clazz));
					if (dbg) vrb.println("    add class " + clazz.name);
					i++;
				}
			}
			clazz = clazz.nextClass;
		}
		systemTable.appendTail(new FixedValueEntry("endOfSystemTable", 0));
		
		if (dbg) vrb.println("  Size of the system table: " + getBlockSize(systemTable) + " byte (0x" + Integer.toHexString(getBlockSize(systemTable)) + ")");
		
		if (dbg) vrb.println("[LINKER] END: Create system table.\n");
	}
		
	public static void freezeMemoryMap() {
		if (dbg) vrb.println("[LINKER] START: Freeze memory map:\n");
		if (dbg) vrb.println("1) Set a segment for the code, the static fields and the constant block for each class");
		Segment s;

		// handle exception handlers in system classes first
		if (dbg) vrb.println("  handle exception handlers in system classes first");
		Method[] meth = Configuration.getOS().getSystemMethodsWithOffsets();
		if (dbg) vrb.println("  nof methods with offset set by configuation = " + meth.length);
		if (meth != null) {
			for (Method m : meth) {
				if (dbg) vrb.println("    handle method: " + m.name + " in class: " + m.owner.name);
				Class c = (Class)RefType.getRefTypeByName(m.owner.name);
				s = Configuration.getCodeSegmentOf(c);
				if (s == null) reporter.error(710, "Can't get a memory segment for the code of class " + c.name + "!\n");
				else {
					int offset = m.offset;
					int size = ((Method)m).getCodeSizeInBytes();
//					if (offset <= s.usedSize) reporter.error(712);
					if (offset < s.usedSize) reporter.error(712);
					s.addToUsedSize(roundUpToNextWord(offset - s.usedSize + size));
					c.codeSegment = s;
					if (dbg) vrb.println("    Code-Segment: " + c.codeSegment.getFullName() + ", offset=0x" + Integer.toHexString(c.codeOffset));
				}
			}
		}

		if (dbg) vrb.println("\n  handle standard classes");
		// handle std classes
		for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
			Class c = Class.extLevelOrdClasses[extLevel];
			while (c != null && reporter.nofErrors <= 0) {
				if (dbg) vrb.println("  Proceeding Class " + c.name);
				if ((c.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (dbg) vrb.println("   is synthetic, omit");
				} else {

					// Code
					s = Configuration.getCodeSegmentOf(c);
					if (s == null) reporter.error(710, "Can't get a memory segment for the code of class " + c.name + "!\n");
					else {
						int codeSize = ((FixedValueEntry)c.codeBase.next).getValue();	// get code size
						c.codeOffset = s.usedSize;
						if (codeSize > 0) s.addToUsedSize(roundUpToNextWord(codeSize));
						c.codeSegment = s;
						if (dbg) vrb.println("    Code-Segment: " + c.codeSegment.getFullName() + ", offset=0x" + Integer.toHexString(c.codeOffset));
					}

					// Var
					s = Configuration.getVarSegmentOf(c);
					if (s == null) reporter.error(710, "Can't get a memory segment for the static variables of class " + c.name + "!\n");
					else {
						c.varOffset = s.usedSize;
						if (c.classFieldsSize > 0) s.addToUsedSize(roundUpToNextWord(c.classFieldsSize));
						c.varSegment = s;
						if (dbg) vrb.println("    Var-Segment: " + c.varSegment.getFullName());
					}

					// Const
					s = Configuration.getConstSegmentOf(c);
					if (s == null) reporter.error(710, "Can't get a memory segment for the constant block of class " + c.name + "!\n");
					else {
						int constBlockSize = c.constantBlock.getValue();
						c.constOffset = s.usedSize;
						if (constBlockSize > 0) s.addToUsedSize(roundUpToNextWord(constBlockSize));
						c.constSegment = s;
						if (dbg) vrb.println("    Const-Segment: " + c.constSegment.getFullName());
					}	
				}
				c = c.nextExtLevelClass;
			}
		}

		// handle arrays
		Array a = Class.arrayClasses;
		s = Configuration.getDefaultConstSegment();
		while (a != null) {
			if (dbg) vrb.println("  Proceeding Array " + a.name);
			if (s == null) reporter.error(710, "Can't get a memory segment for the typedecriptor of array " + a.name + "!\n");
			else {
				a.offset = roundUpToNextWord(s.usedSize);
				s.addToUsedSize(getBlockSize(a.typeDescriptor));
				a.segment = s;
				if (dbg) vrb.println("    Segment for type descriptor: " + a.segment.name);
			}	
			a = a.nextArray;
		}
		
		// handle interfaces
		Class intf = Class.constBlockInterfaces;
		while (intf != null) {
			if (dbg) vrb.println("  Proceeding interface " + intf.name);
			
			// Code
			s = Configuration.getCodeSegmentOf(intf);
			if (s == null) reporter.error(710, "Can't get a memory segment for the code of interface " + intf.name + "!\n");
			else {
				int codeSize = ((FixedValueEntry)intf.codeBase.next).getValue();	// get code size
				if (codeSize > 0) { 
					intf.codeOffset = s.usedSize;
					if (codeSize > 0) s.addToUsedSize(roundUpToNextWord(codeSize));
					intf.codeSegment = s;
					if (dbg) vrb.println("    Code-Segment: " + intf.codeSegment.getFullName() + ", offset=0x" + Integer.toHexString(intf.codeOffset));
				}
			}

			// Var
			s = Configuration.getVarSegmentOf(intf);
			if (s == null) reporter.error(710, "Can't get a memory segment for the static variables of interface " + intf.name + "!\n");
			else {
				if (intf.classFieldsSize > 0) { 
					intf.varOffset = s.usedSize;
					if (intf.classFieldsSize > 0) s.addToUsedSize(roundUpToNextWord(intf.classFieldsSize));
					intf.varSegment = s;
					if (dbg) vrb.println("    Var-Segment: " + intf.varSegment.getFullName());
				}
			}

			// Const
			s = Configuration.getConstSegmentOf(intf);
			if (s == null) reporter.error(710, "Can't get a memory segment for the typedecriptor of interface " + intf.name + "!\n");
			else {
				int constBlockSize = intf.constantBlock.getValue();
				intf.constOffset = s.usedSize;
				if (constBlockSize > 0) s.addToUsedSize(roundUpToNextWord(constBlockSize));
				intf.constSegment = s;
				if (dbg) vrb.println("    Segment for type descriptor: " + intf.constSegment.name);
			}	
			intf = intf.nextInterface;
		}
		
		if (sysTabSegments != null && sysTabSegments.length > 0) {
			for (int i = 0; i < sysTabSegments.length; i++) {
				sysTabSegments[i].addToUsedSize(getBlockSize(systemTable)); 
			}
		} else reporter.error(710, "No segment(s) defined for the system table!");
		
		if (dbg) vrb.println("  Proceeding global constant table");
		s = Configuration.getDefaultConstSegment();
		if (s == null) reporter.error(710, "Can't get a memory segment for the global constant table!\n");
		else {
			globalConstantTableOffset = roundUpToNextWord(s.usedSize);
			s.addToUsedSize(getBlockSize(globalConstantTable));
			globalConstantTableSegment = s;
			if(dbg) vrb.println("    Segment for global constant table: " + globalConstantTableSegment.name);
		}
		
		if (dbg) vrb.println("  Proceeding compiler specific methods");
		s = Configuration.getDefaultConstSegment();
		if (s == null) reporter.error(710, "Can't get a memory segment for the compiler specific methods!\n");
		else {
			compilerSpecificMethodsOffset = roundUpToNextWord(s.usedSize);
			s.addToUsedSize(compilerSpecificMethodsCodeSize);
			compilerSpecSubroutinesSegment = s;
			if(dbg) vrb.println("    Segment for compiler specific methods: " + compilerSpecSubroutinesSegment.name);
		}
		
		if(dbg) vrb.println("2) Check and set the size for each used segment");
		Device[] d = Configuration.getAllDevices();
		for (int i = 0; i < d.length; i++) {
			int sum = 0;
			if(dbg) vrb.println("\tProceeding device " + d[i].name);
			Segment seg = d[i].segments;
			while (seg != null) {
				setSegmentSize(seg);
				if(dbg) vrb.println("\t\tProceeding segment " + seg.name + ", set size to " + seg.size);
				sum += seg.size;
				seg = (Segment) seg.next;
			}
			if(sum > d[i].size) {reporter.error(741, "device = " + d[i].name); return;}
		}
		
		if (dbg) vrb.println("3) Set base addresses for each used segment (devices found: " + d.length + ")");
		for (int i = 0; i < d.length; i++) {
			if (dbg) vrb.println("\tProceeding device " + d[i].name);
			Segment seg = d[i].segments;
			int baseAddr = d[i].address;
			while (seg != null) {
				if(dbg) vrb.println("\t\tProceeding segment " + seg.name + ", set base address to " + baseAddr);
				baseAddr = setBaseAddress(seg, baseAddr);
				seg = (Segment) seg.next;
			}
		}
		if(dbg) vrb.println("[LINKER] END: Freeze memory map.");
	}
	
	public static void calculateAbsoluteAddresses(Class clazz) {
		if (dbg) vrb.println("\n[LINKER] START: Calculating absolute addresses for class \"" + clazz.name +"\":");
		if ((clazz.accAndPropFlags & (1 << apfInterface)) != 0) {	// interface
			if (clazz.codeSegment != null) {
				int varBase = clazz.varSegment.address + clazz.varOffset;
				int codeBase = clazz.codeSegment.address + clazz.codeOffset;
				int constBlockBase =  clazz.constSegment.address + clazz.constOffset;
				int classDescriptorBase = constBlockBase + cblkNofPtrsOffset + (clazz.nofClassRefs + 1) * slotSize;
				int stringPoolBase = classDescriptorBase + clazz.typeDescriptorSize;
				int constPoolBase = stringPoolBase + clazz.stringPoolSize;

				// static fields
				if (clazz.nofClassFields > 0) {
					Item field = clazz.classFields; // class fields 
					if (dbg) vrb.println("  Static fields:"); 
					while (field != null && field != clazz.constFields) { // go through all class fields, stop at const fields	
						if (varBase != -1 && field.offset != -1) field.address = varBase + field.offset;
						else {
							if (varBase == -1) reporter.error(724, "varBase of class " + clazz.name + " not set");
							if (field.offset == -1) reporter.error(721, "offset of field " + field.name + " in class " + clazz.name + " not set!"); 
						}

						if (dbg) vrb.print("    > " + field.name + ": Offset = 0x" + Integer.toHexString(field.offset) + ", Index = 0x" + Integer.toHexString(field.index) + ", Address = 0x" + Integer.toHexString(field.address) + "\n");
						field = field.next;
					}
				}
				// Methods
				if (clazz.nofMethods > 0) {
					Method method = (Method)clazz.methods;
					if (dbg) vrb.println("  Methods:");
					while (method != null) {
						if ((method.accAndPropFlags & (1 << apfStatic)) != 0) { 
							if (codeBase != -1 && method.offset != -1) {
								method.address = codeBase + method.offset;
							}
							//	else reporter.error(9999, "Error while calculating absolute address of method " + method.name + ". Offset: " + method.offset + ", Codebase of Class " + clazz.name + ": " + codeBase);
						}
						if (dbg) vrb.print("    > " + method.name + ": Offset = 0x" + Integer.toHexString(method.offset) + ", Index = 0x" + Integer.toHexString(method.index) + ", Address = 0x" + Integer.toHexString(method.address) + "\n");
						method = (Method)method.next;
					}
				}
				// Constants
				if (clazz.constPool != null && clazz.constPool.length > 0) {
					Item cpe;
					if (dbg) vrb.println("  Constant pool:");
					for (int i = 0; i < clazz.constPool.length; i++) {
						cpe = clazz.constPool[i];
						if (cpe instanceof StdConstant && (cpe.type == Type.wellKnownTypes[txFloat] || cpe.type == Type.wellKnownTypes[txDouble])) { // constant float or double value -> constant pool
							if (cpe.offset != -1) {
								cpe.address = constPoolBase + cpe.offset;
							}
							else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
						}
						else if (cpe instanceof StringLiteral) { // string literal -> string pool
							if (cpe.offset != -1) {
								cpe.address = stringPoolBase + cpe.offset + 8;
							}
							else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
						}
						if (dbg) {
							if (cpe.type != null) vrb.print("    - #" + i + ": Type = " + cpe.type.name + ", Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + ", Name = " + cpe.name + "\n");
							else vrb.print("    - #" + i + ": Type = <unknown>, Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + ", Name = " + cpe.name + "\n");
						}
					}
				}

			}
			// type descriptor
			clazz.address = clazz.constSegment.address + clazz.constOffset + clazz.typeDescriptorOffset;

		} else { // std class
			int varBase = clazz.varSegment.address + clazz.varOffset;
			int codeBase = clazz.codeSegment.address + clazz.codeOffset;
			int constBlockBase =  clazz.constSegment.address + clazz.constOffset;
			int classDescriptorBase = constBlockBase + cblkNofPtrsOffset + (clazz.nofClassRefs + 1) * slotSize;
			int stringPoolBase = classDescriptorBase + clazz.typeDescriptorSize;
			int constPoolBase = stringPoolBase + clazz.stringPoolSize;

			if (dbg) {
				vrb.println("  Code base: 0x" + Integer.toHexString(codeBase));
				vrb.println("  Var base: 0x" + Integer.toHexString(varBase));
				vrb.println("  Const segment base address: 0x" + Integer.toHexString(clazz.constSegment.address));
				vrb.println("  Const offset: 0x" + Integer.toHexString(clazz.constOffset));
				vrb.println("  Const block base address: 0x" + Integer.toHexString(constBlockBase));
				vrb.println("  Number of class references: " + clazz.nofClassRefs);
				vrb.println("  Class descriptor base: 0x" + Integer.toHexString(classDescriptorBase));
				vrb.println("  String pool base: 0x" + Integer.toHexString(stringPoolBase));
				vrb.println("  Const pool base: 0x" + Integer.toHexString(constPoolBase));
			}

			// Class/static fields
			if (clazz.nofClassFields > 0) {
				Item field = clazz.classFields; // class fields 
				if (dbg) vrb.println("  Static fields:"); 
				while (field != null && field != clazz.constFields) { // go through all class fields, stop at const fields	
					if (varBase != -1 && field.offset != -1) field.address = varBase + field.offset;
					else {
						if (varBase == -1) reporter.error(724, "varBase of class " + clazz.name + " not set");
						if (field.offset == -1) reporter.error(721, "offset of field " + field.name + " in class " + clazz.name + " not set!"); 
					}

					if (dbg) vrb.print("    > " + field.name + ": Offset = 0x" + Integer.toHexString(field.offset) + ", Index = 0x" + Integer.toHexString(field.index) + ", Address = 0x" + Integer.toHexString(field.address) + "\n");
					field = field.next;
				}
			}

			// Methods
			if (clazz.nofMethods > 0) {
				Method method = (Method)clazz.methods;
				if (dbg) vrb.println("  Methods:");
				while (method != null) {
					if ((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) { 
						if (method.offset != -1) {
							method.address = clazz.codeSegment.address + method.offset;
						}
						//	else reporter.error(9999, "Error while calculating absolute address of fix set method " + method.name + ". Offset: " + method.offset + ", Segment: " + clazz.codeSegment.getName() + ", Base address of Segment: " + clazz.codeSegment.getBaseAddress());
					}
					else {
						if (codeBase != -1 && method.offset != -1) {
							method.address = codeBase + method.offset;
						}
						//	else reporter.error(9999, "Error while calculating absolute address of method " + method.name + ". Offset: " + method.offset + ", Codebase of Class " + clazz.name + ": " + codeBase);
					}
					if (dbg) vrb.print("    > " + method.name + ": Offset = 0x" + Integer.toHexString(method.offset) + ", Index = 0x" + Integer.toHexString(method.index) + ", Address = 0x" + Integer.toHexString(method.address) + "\n");
					method = (Method)method.next;
				}
			}

			// Constants
			if (clazz.constPool != null && clazz.constPool.length > 0) {
				Item cpe;
				if (dbg) vrb.println("  Constant pool:");
				for (int i = 0; i < clazz.constPool.length; i++) {
					cpe = clazz.constPool[i];
					if (cpe instanceof StdConstant && (cpe.type == Type.wellKnownTypes[txFloat] || cpe.type == Type.wellKnownTypes[txDouble])) { // constant float or double value -> constant pool
						if(cpe.offset != -1) {
							cpe.address = constPoolBase + cpe.offset;
						}
						else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
					}
					else if (cpe instanceof StringLiteral) { // string literal -> string pool
						if (cpe.offset != -1) {
							cpe.address = stringPoolBase + cpe.offset + 8;
						}
						else reporter.error(721, "Class pool entry #" + i + " (" + cpe.type.name + ")");
					}
					if (dbg) {
						if (cpe.type != null) vrb.print("    - #" + i + ": Type = " + cpe.type.name + ", Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + ", Name = " + cpe.name + "\n");
						else vrb.print("    - #" + i + ": Type = <unknown>, Offset = 0x" + Integer.toHexString(cpe.offset) + ", Index = 0x" + Integer.toHexString(cpe.index) + ", Address = 0x" + Integer.toHexString(cpe.address) + ", Name = " + cpe.name + "\n");
					}
				}
			}

			// type descriptor
			clazz.address = clazz.constSegment.address + clazz.constOffset + clazz.typeDescriptorOffset;
		}

		if(dbg) vrb.println("[LINKER] END: Calculating absolute addresses for class \"" + clazz.name +"\"");
	}

	public static void calculateAbsoluteAddresses(Array array) {
		if (dbg) vrb.println("\n[LINKER] START: Calculating absolute addresses for array \"" + array.name +"\":");
		array.address = array.segment.address + array.offset + 4;	
		// array.offset is pointer to entry "extension level", set address to next entry  
	}
	
	public static void calculateAbsoluteAddressesForCompSpecSubroutines() {
		if (dbg) vrb.println("\n[LINKER] START: Calculating absolute addresses for compiler specific methods:\n");
		Method m = Method.compSpecSubroutines;
		while (m != null) {
			m.address = compilerSpecSubroutinesSegment.address + compilerSpecificMethodsOffset + m.offset;
			if (dbg) vrb.print("    > " + m.name + ": Offset = 0x" + Integer.toHexString(m.offset) + ", Index = 0x" + Integer.toHexString(m.index) + ", Address = 0x" + Integer.toHexString(m.address) + "\n");
			m = (Method)m.next;
		}
		if (dbg) vrb.println("\n[LINKER] END: Calculating absolute addresses for compiler specific methods.\n");
	}
	
	public static void updateConstantBlock(Class clazz) {
		if (dbg) vrb.println("[LINKER] START: Updating constant block for class \"" + clazz.name +"\":\n");

		if (dbg) vrb.println("  Inserting code base");
		if (clazz.codeSegment != null) ((FixedValueEntry)clazz.codeBase).setValue(clazz.codeSegment.address + clazz.codeOffset); // codeBase
		// codeSize already set...
		if (dbg) vrb.println("  Inserting var base");
		if (clazz.varSegment != null) ((FixedValueEntry)clazz.codeBase.next.next).setValue(clazz.varSegment.address + clazz.varOffset); // varBase
		if (dbg) vrb.println("  Inserting var size");
		((FixedValueEntry)clazz.codeBase.next.next.next).setValue(clazz.classFieldsSize); // varSize
		
		if (dbg) vrb.println("  Inserting object size");
		((FixedValueEntry)clazz.typeDescriptor).setValue(clazz.objectSize); // size
		
		// Calculate checksum
		if (dbg) vrb.println("  Calculating checksum:");
		int fcs = setCRC32(clazz.constantBlock, clazz.constantBlockChecksum);		
		if (dbg) vrb.println(String.format("  CRC32: 0x%08X", fcs));
		
		if (dbg) vrb.println("\n[LINKER] END: Updating constant block for class \"" + clazz.name +"\"\n");
	}
	
	public static void updateSystemTable() {
		if (dbg) vrb.println("[LINKER] START: Updating system table\n");
		
		// handle std classes
		for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
			Class c;
			c = Class.extLevelOrdClasses[extLevel];
			while (c != null && reporter.nofErrors <= 0) {
				if ((c.accAndPropFlags & (1 << dpfSynthetic)) == 0) {	// omit synthetic classes
					// Code
					monitorUsedSpace(((FixedValueEntry)c.codeBase).getValue(), ((FixedValueEntry)c.codeBase.next).getValue());
					// Constant block
					monitorUsedSpace(c.constSegment.address + c.constOffset, ((FixedValueEntry)c.constantBlock).getValue());
				}
				c = c.nextExtLevelClass;
			}
		}
		
		// handle arrays
		Array a = Class.arrayClasses;
		while (a != null) {
			monitorUsedSpace(a.address - slotSize, getBlockSize(a.typeDescriptor));
			a = a.nextArray;
		}

		// handle interfaces
		Class intf = Class.constBlockInterfaces;
		while (intf != null) {
			// Code
			monitorUsedSpace(((FixedValueEntry)intf.codeBase).getValue(), ((FixedValueEntry)intf.codeBase.next).getValue());
			// Constant block
			monitorUsedSpace(intf.constSegment.address + intf.constOffset, ((FixedValueEntry)intf.constantBlock).getValue());

			intf = intf.nextInterface;
		}
		
		if (sysTabSegments != null && sysTabSegments.length > 0) {
			for (int i = 0; i < sysTabSegments.length; i++) {
				monitorUsedSpace(sysTabSegments[i].address, getBlockSize(systemTable)); 
			}
		}
				
		if (compilerSpecificMethodsCodeSize > 0) {
			monitorUsedSpace(compilerSpecSubroutinesSegment.address + compilerSpecificMethodsOffset, compilerSpecificMethodsCodeSize);
		}
		
		if(globalConstantTable != null) {
			monitorUsedSpace(globalConstantTableSegment.address + globalConstantTableOffset, getBlockSize(globalConstantTable));
		}
		
		// handle exception handlers in system classes
		Method[] meth = Configuration.getOS().getSystemMethodsWithOffsets();
		if (meth != null) {
			for (Method m : meth) {
				Class c = (Class)Class.getRefTypeByName(m.owner.name);
				Item m1 = c.methods.getItemByName(m.name);
				monitorUsedSpace(((Method)m1).offset, ((Method)m1).getCodeSizeInBytes());
			}
		}

		int sizeToCopy = lastUsedAddress - firstUsedAddress;
		
		if (dbg) vrb.println("  First used address: 0x" + Integer.toHexString(firstUsedAddress));
		if (dbg) vrb.println("  Last used address: 0x" + Integer.toHexString(lastUsedAddress));
		if (dbg) vrb.println("  -> Setting \"sizeToCopy\" to " + sizeToCopy + " bytes");
		
		sysTabSizeToCopy.setValue(sizeToCopy);
		
		if (dbg) vrb.println("\n[LINKER] END: Updating system table\n");
	}
	
	public static void generateTargetImage() {
		if(dbg) vrb.println("[LINKER] START: Generating target image:\n");

		// handle std classes
		for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
			Class c;
			c = Class.extLevelOrdClasses[extLevel];
			while (c != null && reporter.nofErrors <= 0) {
				if (dbg) vrb.println("  Proceeding class \"" + c.name + "\":");
				if ((c.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (dbg) vrb.println("   is synthetic, omit");
				} else {
					// code
					Method m = (Method)c.methods;
					if (dbg) vrb.println("    1) Code:");
					while (m != null) {
						if (m.machineCode != null) {
							if (dbg) vrb.println("         > Method \"" + m.name + "\":" + Integer.toHexString(m.address));
							assert m.address != -1;
							addTargetMemorySegment(new TargetMemorySegment(c.codeSegment, m.address, m.machineCode.instructions, m.machineCode.iCount));
						}
						m = (Method)m.next;
					}

					// consts
					if(dbg) vrb.println("    2) Constantblock:");
					assert c.constSegment.address + c.constOffset != -1;
					if (dbg) vrb.println("         > Const \"" + c.name + "\":" + Integer.toHexString(c.address));
					addTargetMemorySegment(new TargetMemorySegment(c.constSegment, c.constSegment.address + c.constOffset, c.constantBlock));
				}
				c = c.nextExtLevelClass;
			}
		}

		// handle arrays
		Array a = Class.arrayClasses;
		while (a != null) {
			if (dbg) vrb.println("  Proceeding array \"" + a.name + "\":");
			assert a.segment.address + a.offset != -1;
			if (dbg) vrb.println("         > Const \"" + a.name + "\":" + Integer.toHexString(a.address));
			addTargetMemorySegment(new TargetMemorySegment(a.segment, a.segment.address + a.offset, a.typeDescriptor));
			a = a.nextArray;
		}
		
		// handle interfaces
		Class intf = Class.constBlockInterfaces;
		while (intf != null) {
			if (dbg) vrb.println("  Proceeding interface \"" + intf.name + "\":");
			// code
			Method m = (Method)intf.methods;
			while (m != null) {
				if (m.machineCode != null) {
					if (dbg) vrb.println("         > Method \"" + m.name + "\":" + Integer.toHexString(m.address));
					assert m.address != -1;
					addTargetMemorySegment(new TargetMemorySegment(intf.codeSegment, m.address, m.machineCode.instructions, m.machineCode.iCount));
				}
				m = (Method)m.next;
			}		
			// consts
			assert intf.constSegment.address + intf.constOffset != -1;
			if (dbg) vrb.println("         > Const \"" + intf.name + "\":" + Integer.toHexString(intf.address));
			addTargetMemorySegment(new TargetMemorySegment(intf.constSegment, intf.constSegment.address + intf.constOffset, intf.constantBlock));
			intf = intf.nextInterface;
		}
				
		if (dbg) vrb.println("  Proceeding system table(s):");
		Segment[] s = Configuration.getSysTabSegments();
		if (dbg) vrb.println("  > Address: 0x" + Integer.toHexString(s[0].address));
		for (int i = 0; i < sysTabSegments.length; i++) {
			addTargetMemorySegment(new TargetMemorySegment(s[i], s[i].address, systemTable)); 
		}
		
		if (dbg) vrb.println("  Proceeding global constant table:");
		addTargetMemorySegment(new TargetMemorySegment(globalConstantTableSegment, globalConstantTableSegment.address + globalConstantTableOffset, globalConstantTable));
		
		if (dbg) vrb.println("  Proceeding compiler specific methods:");
		Method cssr = Method.compSpecSubroutines;
		while (cssr != null) {
			if (cssr.machineCode != null) {				
				if (dbg) vrb.println("         > Method \"" + cssr.name + "\":" + Integer.toHexString(cssr.address));
				addTargetMemorySegment(new TargetMemorySegment(compilerSpecSubroutinesSegment, cssr.address, cssr.machineCode.instructions, cssr.machineCode.iCount));
			}
			cssr = (Method)cssr.next;
		}
		
		
		if (dbg) vrb.println("[LINKER] END: Generating target image\n");
	}
	
	public static long writeTargetImageToDtimFile(String fileName) throws IOException {
		if(dbg) vrb.println("[LINKER] START: Writing target image to file: \"" + fileName +"\":\n");
		
		DataOutputStream timFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			
		timFile.write("#dtim-0\n".getBytes()); // Header (8 Byte)
		
		TargetMemorySegment tms = targetImage;
		int i = 0;
		while (tms != null) {
			if(dbg) vrb.println("TMS #" + i + ": Startaddress = 0x" + Integer.toHexString(tms.startAddress) + ", Size = 0x" + Integer.toHexString(tms.data.length * 4));
			timFile.writeInt(tms.startAddress);
			timFile.writeInt(tms.data.length * 4);
			for (int j = 0; j < tms.data.length; j++) {
				timFile.writeInt(tms.data[j]);
			}
			i++;
			tms = tms.next;
		}
		
		long bytesWritten = timFile.size();
		if (dbg) vrb.println("[LINKER] END: Writing target image to file.\n");
		timFile.close();
		return bytesWritten;
	}
	
	public static long writeTargetImageToBinFile(String fileName) throws IOException {
		if (dbg) vrb.println("\n[LINKER] START: Writing target image to file: \"" + fileName +"\":");
		long bytesWritten = 0;
		String fileExtension = "bin";
		String pathAndFileName = fileName;
//		if (fileName.lastIndexOf('.') > 0) {
//			fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
//			pathAndFileName = fileName.substring(0, fileName.lastIndexOf('.'));
//		}		
		TargetMemorySegment tms = targetImage;
		Device dev = tms.segment.owner;
		String currentFileName = new String(pathAndFileName + "." + dev.name + "." + fileExtension);
		try {
			DataOutputStream binFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(currentFileName)));	
			if (dbg) vrb.println("  Writing to file: " + currentFileName);
			int currentAddress = dev.address;	// start with device start address
			Configuration.addImgFile(currentFileName, currentAddress);
			while (tms != null) {
				while (currentAddress < tms.startAddress) { // fill with 0 from end of last tms to address of actual tms
					binFile.write(0);
					currentAddress++;
				}
				if (dbg) vrb.print("  > TMS #" + tms.id + ": start address = 0x" + Integer.toHexString(tms.startAddress) + ", size = 0x" + Integer.toHexString(tms.data.length * 4));
				for(int j = 0; j < tms.data.length; j++) {
					if (bigEndian) binFile.writeInt(tms.data[j]);
					else binFile.writeInt(Integer.reverseBytes(tms.data[j]));
					currentAddress += 4;
				}
				if (dbg) vrb.println(" end address = 0x" + Integer.toHexString(currentAddress));
				
				if (tms.next != null && tms.next.segment.owner != dev) {	// next device file
					binFile.close();
					dev = tms.next.segment.owner;
					currentFileName = new String(pathAndFileName + "." + dev.name + "." + fileExtension);
					binFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(currentFileName)));
					currentAddress = dev.address;
					Configuration.addImgFile(currentFileName, currentAddress);
					if (dbg) vrb.println("  Writing to file: " + currentFileName);
				}
				tms = tms.next;
			}
			bytesWritten =  binFile.size();
			if (dbg) vrb.println("[LINKER] END: Writing target image to file.");
			binFile.close();
		} catch (Exception e) {
			reporter.error(11, "Writing image file");
		}
		return bytesWritten;
	}
	
	public static long writeTargetImageToMcsFile(String fname) throws IOException {
		if (dbg) vrb.println("\n[LINKER] START: Writing target image to mcs file: \"" + fname +"\":");
		long bytesWritten = 0;
		try {
			// open deep bin file for reading
			File binFile = new File(fname + '.' + Linker32.targetImage.segment.owner.name + ".bin");
			if (!binFile.exists()) {
				ErrorReporter.reporter.error(822, "check if proper target file format used");
				return 0;
			}
			BufferedInputStream readerBin = new BufferedInputStream(new FileInputStream(binFile));
			// open mcs file for writing
			fname = fname + '.' + Linker32.targetImage.segment.owner.name + ".mcs";
			BufferedWriter writerMcs = new BufferedWriter(new FileWriter(fname));
			String plFile = Configuration.getPlFile();
			if (plFile == null) {
				ErrorReporter.reporter.error(823, "add PL file to deep configuration");
				readerBin.close();
				writerMcs.close();
				return 0;
			}
			HString[] libs = Configuration.getLibPaths();
			boolean found = false;
			// open predefined Xilinx mcs file 
			for (HString lib : libs) {
				fname = lib + "rsc/BOOT" + plFile.substring(plFile.indexOf("flink"), plFile.length() - 4) + "App.mcs";
				File f = new File(fname);
				if (f.exists()) {
					found = true;
					break;
				}
			}
			if (!found) {
				ErrorReporter.reporter.error(824, "check if library path set correctly");
				readerBin.close();
				writerMcs.close();
				return 0;
			}
			BufferedReader readerMcs = new BufferedReader(new FileReader(fname));
			StringBuffer buf = new StringBuffer();
			String lineMcs;
			byte[] lineBin = new byte[128];
			int line = 1;
			
			// determine application size in bytes, this size must be written into header file info section (see below)
			int size = 0, k;
			final int len = 16;
			while ((k = readerBin.read(lineBin, 0, len)) == len) size += 16;
			if (k > 0) size += k;
			readerBin.close();
//			StdStreams.vrb.println("nof bytes in bin file = 0x" + Integer.toHexString(size));
			
			readerBin = new BufferedInputStream(new FileInputStream(binFile));
			// copy
			while (line < 159 && ((lineMcs = readerMcs.readLine()) != null)) {
				buf.append(lineMcs);
				buf.append('\n');
				line++;
			}
			int checksum = 3 * size;
			// change header file info for application
			lineMcs = readerMcs.readLine();
			String newLine = lineMcs.substring(0, 9) + String.format("%08X", Integer.reverseBytes(size)) + String.format("%08X", Integer.reverseBytes(size)) + String.format("%08X", Integer.reverseBytes(size)) + lineMcs.substring(33);
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(newLine);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(lineMcs);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(lineMcs);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum = ~checksum;
//			StdStreams.vrb.println("checksum = 0x" + Integer.toHexString(checksum));
			newLine = lineMcs.substring(0, 33) + String.format("%08X", Integer.reverseBytes(checksum)) + lineMcs.substring(41);
			buf.append(newLine);
			buf.append('\n');
			line++;

			// copy
			while (line < 137603 && ((lineMcs = readerMcs.readLine()) != null)) {
				buf.append(lineMcs);
				buf.append('\n');
				line++;
			}
			
			// replace with content of target image bin file
			int offset = 0xa300;
			int highAddr = 0x22;
			while ((k = readerBin.read(lineBin, 0, len)) == len) {
//				StdStreams.vrb.print("reading bin = " + count + ": ");
//				for (int i = 0; i < len; i++) StdStreams.vrb.print(String.format("%02X", lineBin[i]));        
//				StdStreams.vrb.println();
				buf.append(":10" + String.format("%04X", offset) + "00");
				for (int i = 0; i < len; i++) {
					buf.append(String.format("%02X", lineBin[i]));
				}
				buf.append("AB\n");
				if (offset == 0xfff0) {	// high address tag, MCS files need such tags every 64k block
					buf.append(":02" + String.format("%04X", 0) + "04" + String.format("%04X", highAddr) + "AB\n");
					highAddr++;
					offset = 0;
				} else offset += 0x10;
			} 
			if (k > 0) {	// end unfinished lines
//				StdStreams.vrb.print("reading bin = " + count + ": ");
//				for (int i = 0; i < k; i++) StdStreams.vrb.print(String.format("%02X", lineBin[i]));        
//				StdStreams.vrb.println();
				buf.append(":" + String.format("%02X", k) + String.format("%04X", offset) + "00");
				for (int i = 0; i < k; i++) {
					buf.append(String.format("%02X", (byte)lineBin[i]));
				}
				buf.append("AB\n");
			}
			buf.append(":00000001FF\n");	// eof tag
			readerMcs.close();
			readerBin.close();
			writerMcs.write(buf.toString());
			bytesWritten = buf.length();
			writerMcs.close();
		} catch (IOException e) {
			ErrorReporter.reporter.error(822, "check if proper target file format use");
			return 0;
		}
		return bytesWritten;
	}

	public static long writeTargetImageToBootBinFile(String fname) throws IOException {
		if (dbg) vrb.println("\n[LINKER] START: Writing target image to boot bin file: \"" + fname +"\":");
		long bytesWritten = 0;
		try {
			// open deep bin file for reading
			File deepBinFile = new File(fname + '.' + Linker32.targetImage.segment.owner.name + ".bin");
			if (!deepBinFile.exists()) {
				ErrorReporter.reporter.error(822, "check if proper target file format use");
				return 0;
			}
			BufferedInputStream readerBin = new BufferedInputStream(new FileInputStream(deepBinFile));
			// open bin file for writing
			fname = fname.substring(0, fname.lastIndexOf('/') + 1) + "BOOT.bin";
			BufferedOutputStream writerBin = new BufferedOutputStream(new FileOutputStream(fname));
			String plFile = Configuration.getPlFile();
			if (plFile == null) {
				ErrorReporter.reporter.error(823, "add PL file to deep configuration");
				readerBin.close();
				writerBin.close();
				return 0;
			}
			HString[] libs = Configuration.getLibPaths();
			boolean found = false;
			// open predefined Xilinx bin file 
			String bootBinFile = "";
			for (HString lib : libs) {
				bootBinFile = lib + "rsc/BOOT" + plFile.substring(plFile.indexOf("flink"), plFile.length() - 4) + "App.bin";
				File f = new File(bootBinFile);
				if (f.exists()) {
					found = true;
					break;
				}
			}
			if (!found) {
				ErrorReporter.reporter.error(824, "check if library path set correctly");
				readerBin.close();
				writerBin.close();
				return 0;
			}
			readerBin.close();
			
			// determine deep application size in bytes, this size must be written into header file info section (see below)
			readerBin = new BufferedInputStream(new FileInputStream(deepBinFile));
			byte[] lineBin = new byte[128];
			int size = 0, k;
			final int len = 16;
			while ((k = readerBin.read(lineBin, 0, len)) == len) size += 16;
			if (k > 0) size += k;
			readerBin.close();
//			StdStreams.vrb.println("nof bytes in bin file = 0x" + Integer.toHexString(size));
			
			readerBin = new BufferedInputStream(new FileInputStream(deepBinFile));
			BufferedInputStream readerBootBin = new BufferedInputStream(new FileInputStream(bootBinFile));
			ByteBuffer buf = ByteBuffer.allocate(0x400000);
			
			// copy header
			for (int i = 0; i < 0xd00; i++) buf.put((byte) readerBootBin.read());
			int checksum = 3 * size;
	
			// change header file info for application
			for (int i = 0; i < 12; i++) readerBootBin.read();	// read over size information, now at 0xd0c
			buf.putInt(Integer.reverseBytes(size));
			buf.putInt(Integer.reverseBytes(size));
			buf.putInt(Integer.reverseBytes(size));
			ByteBuffer b1 = ByteBuffer.allocate(4);
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd10
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd14
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd18
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd1c
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd20
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd24
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd28
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd2c
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd30
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd34
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd38
			checksum += getHeaderInfoEntryBin(b1);
			b1.rewind();
			for (int i = 0; i < 4; i++) b1.put((byte) readerBootBin.read());
			buf.put(b1.array());	// now at 0xd3c
			checksum += getHeaderInfoEntryBin(b1);
			checksum = ~checksum;
			buf.putInt(Integer.reverseBytes(checksum));
			for (int i = 0; i < 4; i++) readerBootBin.read();	// read over checksum, now at 0xd40

			// copy rest of header and bit file
			for (int i = 0xd40; i < 0x21a300; i++) buf.put((byte) readerBootBin.read());	// now at 0x21a300
			
			// replace with content of target image bin file
			int val = readerBin.read();
			while (val != -1) {
				buf.put((byte)val);
				val = readerBin.read();
			}

			readerBin.close();
			readerBootBin.close();
			writerBin.write(buf.array());
			bytesWritten = buf.array().length;
			writerBin.close();
		} catch (IOException e) {
			ErrorReporter.reporter.error(822, "check if proper target file format used");
			return 0;
		}
		return bytesWritten;
	}

	public static long writeTargetImageToElfFile(String fileName) throws IOException {		
		if(dbg) vrb.println("[LINKER] START: Writing target image to file: \"" + fileName +"\":\n");	
		ByteBuffer buf = ByteBuffer.allocate(0x200000);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		TargetMemorySegment tms = targetImage;
		for(int i = 0; tms != null; i++) {
			if(dbg) vrb.println("TMS #" + i + ": Startaddress = 0x" + Integer.toHexString(tms.startAddress) + ", Size = 0x" + Integer.toHexString(tms.data.length * 4));
			buf.limit(tms.startAddress + tms.data.length * 4);
			buf.position(tms.startAddress);
			for(int d : tms.data) {
				buf.putInt(d);
			}
			tms = tms.next;
		}
		int size = buf.position();
		buf.flip();
		
		Elf elf = new Elf(ElfClass.CLASS_32, ByteOrder.LITTLE_ENDIAN, AbiType.SYSV, ObjectFileType.EXEC, MachineType.ARM, Flags.EF_ARM_EABI_VER5 | Flags.EF_ARM_VFP_FLOAT, 0x100);
		elf.AddSection(buf, ".text", SectionType.PROGBITS, 7, 0, 0, 0, 0);
		SectionHeader section = elf.sectionHeaders.get(elf.sectionHeaders.size() - 1);
		elf.addProgramHeader(SegmentType.LOAD, 7, section.fileOffset, 0, 0, size, size, 0);
		
		
		// Add Debug Line Number Information
		DebugSymbols debugSymbols = new DebugSymbols(ByteOrder.LITTLE_ENDIAN);		

		
		buf = debugSymbols.getDebug_abbrev();
		buf.flip();
		elf.AddSection(buf, ".debug_abbrev", SectionType.PROGBITS, 0, 0, 0, 1, 0);
		buf = debugSymbols.getDebug_info();
		buf.flip();
		elf.AddSection(buf, ".debug_info", SectionType.PROGBITS, 0, 0, 0, 1, 0);
		
		buf = debugSymbols.getDebug_line();
		buf.flip();
		elf.AddSection(buf, ".debug_line", SectionType.PROGBITS, 0, 0, 0, 1, 0);
		
		buf = debugSymbols.getDebug_loc();
		buf.flip();
		elf.AddSection(buf, ".debug_loc", SectionType.PROGBITS, 0, 0, 0, 1, 0);
		
		elf.saveToFile(fileName + ".elf");
		elf.close();
		if (dbg) vrb.println("[LINKER] END: Writing target image to file.\n");
		return 0;
	}
	
	public static void writeCommandTableToFile(String fileName) throws IOException {
		if(dbg) vrb.println("[LINKER] START: Writing command table to file: \"" + fileName +"\":");
		
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date buildTime = new Date();
		
        BufferedWriter tctFile = new BufferedWriter(new FileWriter(fileName));
        tctFile.write("#dtct-0\n");
        tctFile.write("#File created: " + f.format(buildTime));
        tctFile.write("\n\n");
        
        Field cmdAddrField;
        int cmdAddr = -1;
        Class kernel = (Class)RefType.refTypeList.getItemByName(Configuration.getOS().kernelClass.name);
        if (kernel != null) {
        	if (dbg) vrb.println("  Kernel: " + kernel.name);
        	cmdAddrField = (Field)kernel.classFields.getItemByName("cmdAddr");
        	if(cmdAddrField != null) {
        		if (dbg) vrb.println("  cmdAddrField: " + cmdAddrField.name + "@" + cmdAddrField.address);
        		cmdAddr = cmdAddrField.address;
        	}
        	else reporter.error(790, "Field cmdAddrField in the Kernel not set");
        }
        else reporter.error(701, "Kernel (" + Configuration.getOS().kernelClass.name + ")");
        
        tctFile.write("cmdAddr@");
        tctFile.write(String.valueOf(cmdAddr));
        tctFile.write("\n\n");
        
        Item clazz = RefType.refTypeList;
        Method method;
        
        while (clazz != null) {
        	if (clazz instanceof Class) {
        		if (dbg) vrb.println("  proceeding class \"" + clazz.name + "\"");
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
        if(dbg) vrb.println("Command table written to \"" + fileName + "\"");
		if(dbg) vrb.println("[LINKER] END: Writing command table to file.");
	}
		
	public static void addGlobalConstant(StdConstant gconst) {
		if (globalConstantTable == null) globalConstantTable = new ConstantEntry(gconst);
		else globalConstantTable.appendTail(new ConstantEntry(gconst));
	}
	
	
	/* ---------- private helper methods ---------- */
	
	private static int setBaseAddress(Segment s, int addr) {
//		if(s.subSegments != null) setBaseAddress(s.subSegments, baseAddress);
		if ((s.size > 0 && s.usedSize > 0) || ((s.attributes & ((1 << dpfSegStack) | (1 << dpfSegHeap) | (1 << dpfSegSysTab))) != 0)) { 
			if (s.address == -1) s.address = addr;
			if (dbg) vrb.println("    setting base address of segment " + s.name +" to 0x"+ Integer.toHexString(addr) + " with the size of 0x" + Integer.toHexString(s.size) + " bytes");
		} else if (dbg) vrb.println("    segment " + s.name +" not used");
		return s.size + addr;
	}

	@SuppressWarnings("unused")
	private static Segment getFirstFittingSegment(Segment s, byte contentAttribute, int requiredSize) {
		Segment t = s;
		while (t != null) {
			if ((t.attributes & (1 << contentAttribute)) != 0) {
				if(t.size <= 0 || t.size - t.usedSize > requiredSize) return t;
			}
			t = (Segment)t.next;
		}
		return null;
	}

	private static void setSegmentSize(Segment s) {
//		if (s.subSegments != null && s.subSegments.getTail() != null) {
//			setSegmentSize((Segment)s.subSegments.getTail());
//		}
		if (s.size <= 0) {
			s.size = roundUpToNextWord(s.usedSize);
			if (dbg) vrb.println("    setting used size for segment " + s.name + " to 0x" + Integer.toHexString(s.size));
		} else if (s.size < s.usedSize) {reporter.error(711, "Segment " + s.name + " is too small! Size is manually set to " + s.size + " byte, but required size is " + s.usedSize + " byte!\n");
		} else if (dbg) vrb.println("    size for segment " + s.name + " set by configuration to 0x" + Integer.toHexString(s.size));
	}
		
	protected static int roundUpToNextWord(int val) {
		return (val + (slotSize - 1)) & -slotSize;
	}
	
	private static void addTargetMemorySegment(TargetMemorySegment tms) {
		if (targetImage == null) {
			if(dbg) vrb.println("      >>>> Adding first target memory segment (#" + tms.id + ")");
			targetImage = tms;
		} else {
			TargetMemorySegment current = targetImage;
			if (current.startAddress < tms.startAddress) {
				while (current.next != null && tms.startAddress > current.next.startAddress) {
					current = current.next;
				}
				if (dbg) vrb.println("      >>>> Inserting target memory segment #" + tms.id + " after target memory segment #" + current.id);
				tms.next = current.next;
				current.next = tms;
			} else {
				if (dbg) vrb.println("      >>>> Inserting target memory segment #" + tms.id + " before first target memory segment segment");
				tms.next = current;
				targetImage = tms;
			}
		}
	}

	private static int getHeaderInfoEntry(String str) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= str.length() - 2; i += 2) {
			result.append(new StringBuilder(str.substring(i, i + 2)).reverse());
		}
		return (int)Long.parseLong(result.reverse().toString(), 16);   
	}

	private static int getHeaderInfoEntryBin(ByteBuffer buf) {
		buf.rewind();
		Integer val = buf.getInt();
		return Integer.reverseBytes(val);   
	}

	private static boolean checkConstantPoolType(Item item) {
		return item instanceof StdConstant && ((item.type == Type.wellKnownTypes[txFloat] || item.type == Type.wellKnownTypes[txDouble]));
	}

	private static void monitorUsedSpace(int startAddress, int size) {
		if (startAddress < firstUsedAddress) firstUsedAddress = startAddress;
		if (startAddress + size > lastUsedAddress) lastUsedAddress = startAddress + size;
	}

	/**
	 * Calculates total size of all items in bytes in the linked list of ConstBlkENtry starting with <code>item</code>
	 * 
	 * @return Size in bytes
	 */
	static int getBlockSize(Item item) {
		int size = 0;
		while (item != null) {
			size += ((ConstBlkEntry)item).getItemSize();
			item = item.next;
		}
		return size;
	}

	/**
	 * Calculates the CRC32 checksum of the list for all
	 * elements from the beginning to the given item and
	 * saves the result in the given item. 
	 * 
	 * @param fcsItem item to write the checksum in.
	 * @return the CRC32 checksum
	 */
	private static int setCRC32(ConstBlkEntry startItem, ConstBlkEntry fcsItem) {
		CRC32 checksum = new CRC32();
		ConstBlkEntry i = startItem;
		while (i != fcsItem) {
			checksum.update(i.getBytes());
			i = (ConstBlkEntry)i.next;
		}
		int fcs = (int)checksum.getValue();
		if (Linker32.dbg) vrb.println("    fsc = 0x" + Integer.toHexString(fcs));
		if (bigEndian) {		// change endianess and complement
			fcs = Integer.reverseBytes(fcs) ^ 0xffffffff;
		} else {	// complement
			fcs ^= 0xffffffff;		
		}
		((FixedValueEntry)fcsItem).setValue(fcs);
		return fcs;
	}


	/* ---------- debug primitives ---------- */

	public static void printSystemTable() {
		vrb.println("Size: " + getBlockSize(systemTable) + " byte");
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
		int cc = 0;
		for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
			Class c = Class.extLevelOrdClasses[extLevel];
			while (c != null) {
				vrb.println("CLASS: " + c.name + " (#" + cc++ + ")");
				vrb.println("Number of class methods:     " + c.nofClassMethods);
				vrb.println("Number of instance methods:  " + c.nofInstMethods);
				vrb.println("Number of class fields:      " + c.nofClassFields);
				vrb.println("Number of instance fields:   " + c.nofInstFields);
				vrb.println("Number of interfaces:        " + c.nofInterfaces);
				vrb.println("Number of base classes:      " + c.extensionLevel);
				vrb.println("Number of references:        " + c.nofClassRefs);
				vrb.println("Max extension level:         " + Class.maxExtensionLevelStdClasses);
				vrb.println("Machine code size:           " + ((FixedValueEntry)c.codeBase.next).getValue() + " byte");
				vrb.println("Constant block size:         " + ((FixedValueEntry)c.constantBlock).getValue() + " byte");
				vrb.println("Class fields size:           " + c.classFieldsSize + " byte");
				vrb.println("Code offset:                 0x" + Integer.toHexString(c.codeOffset));
				vrb.println("Var offset:                  0x" + Integer.toHexString(c.varOffset));
				vrb.println("Const offset:                0x" + Integer.toHexString(c.constOffset));
				vrb.println("Code segment:                " + c.codeSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.codeSegment.address) + ", size: " + c.codeSegment.size + " byte)");
				vrb.println("Var segment:                 " + c.varSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.varSegment.address) + ", size: " + c.varSegment.size + " byte)");
				vrb.println("Const segment:               " + c.constSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(c.constSegment.address) + ", size: " + c.constSegment.size + " byte)");
				vrb.println("Type descriptor address:     0x" + Integer.toHexString(c.address));
				vrb.println("Constant block base address: 0x" + Integer.toHexString(c.constSegment.address + c.constOffset));
				vrb.println("Code base address:           0x" + Integer.toHexString(c.codeSegment.address + c.codeOffset));
				vrb.println("Class field base address:    0x" + Integer.toHexString(c.varSegment.address + c.varOffset));

				if (printMethods) {
					vrb.println("Methods:");
					Method m = (Method)c.methods;
					int mc = 0;
					if (m == null) vrb.println(">  No methods in this class");
					else {
						while (m != null) {
							vrb.println("> Method #" + mc++ + ": " + m.name +  m.methDescriptor);
							vrb.println("  Flags:     0x" + Integer.toHexString(m.accAndPropFlags));
							if ((m.accAndPropFlags & ((1 << dpfNew) | (1 << dpfUnsafe) | (1 << dpfSysPrimitive) | (1 << dpfSynthetic))) != 0) {	
								if ((m.accAndPropFlags & (1 << dpfNew)) != 0) vrb.println("  Special:   New");
								if((m.accAndPropFlags & (1 << dpfUnsafe)) != 0) vrb.println("  Special:   Unsafe");
								if((m.accAndPropFlags & (1 << dpfSysPrimitive)) != 0) vrb.println("  Special:   System primitive");
								if((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) vrb.println("  Special:   Synthetic");
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
				if (printFields) {
					vrb.println("Fields:");
					Item f = c.instFields;
					int fc = 0;
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

				if (printConstantBlock) {
					vrb.println("Constant block:");
					c.printConstantBlock();
				}
				vrb.println("----------------------------------------------------------------------");
				c = c.nextExtLevelClass;			
			}
		}

		Array a = Class.arrayClasses;
		int ac = 0;
		while (a != null) {
			vrb.println("ARRAY: " + a.name + " (#" + ac++ + ")");
			vrb.println("Component type: " + a.componentType.name);
			vrb.println("Address:  0x" + Integer.toHexString(a.address));
			vrb.println("Type descriptor:");
			a.typeDescriptor.printList();
			vrb.println("----------------------------------------------------------------------");
			a = a.nextArray;
		}

		Class intf = Class.constBlockInterfaces;
		int ic = 0;
		while (intf != null) {
			vrb.println("INTERFACE: " + intf.name + " (#" + ic++ + ") (id=" + intf.index + " chkId="+ intf.chkId + ")");
			vrb.println("Number of class methods:     " + intf.nofClassMethods);
			vrb.println("Number of instance methods:  " + intf.nofInstMethods);
			vrb.println("Number of class fields:      " + intf.nofClassFields);
			vrb.println("Number of instance fields:   " + intf.nofInstFields);
			vrb.println("Number of interfaces:        " + intf.nofInterfaces);
			vrb.println("Number of base classes:      " + intf.extensionLevel);
			vrb.println("Number of references:        " + intf.nofClassRefs);
			vrb.println("Max extension level:         " + Class.maxExtensionLevelStdClasses);
			vrb.println("Machine code size:           " + ((FixedValueEntry)intf.codeBase.next).getValue() + " byte");
			vrb.println("Constant block size:         " + ((FixedValueEntry)intf.constantBlock).getValue() + " byte");
			vrb.println("Class fields size:           " + intf.classFieldsSize + " byte");
			vrb.println("Code offset:                 0x" + Integer.toHexString(intf.codeOffset));
			vrb.println("Const offset:                0x" + Integer.toHexString(intf.constOffset));
//			vrb.println("Code segment:                " + intf.codeSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(intf.codeSegment.getBaseAddress()) + ", size: " + intf.codeSegment.getSize() + " byte)");
			assert intf.constSegment != null;
			vrb.println("Const segment:               " + intf.constSegment.getFullName() + " (Base address: 0x" + Integer.toHexString(intf.constSegment.address) + ", size: " + intf.constSegment.size + " byte)");
			vrb.println("Type descriptor address:     0x" + Integer.toHexString(intf.address));
			vrb.println("Constant block base address: 0x" + Integer.toHexString(intf.constSegment.address + intf.constOffset));
//			vrb.println("Code base address:           0x" + Integer.toHexString(intf.codeSegment.getBaseAddress() + intf.codeOffset));
//			vrb.println("Class field base address:    0x" + Integer.toHexString(intf.varSegment.getBaseAddress() + intf.varOffset));
			vrb.println("Address:  0x" + Integer.toHexString(intf.address));
			if (printConstantBlock) {
				vrb.println("Constant block:");
				intf.printConstantBlock();
			}
			vrb.println("----------------------------------------------------------------------");
			intf = intf.nextInterface;
		}
	}

	public static void printGlobalConstantTable() {	
		vrb.println("Size: " + getBlockSize(globalConstantTable) + " byte");
		globalConstantTable.printList();
	}	
	
}
