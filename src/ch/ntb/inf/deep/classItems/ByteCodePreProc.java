package ch.ntb.inf.deep.classItems;

import java.io.PrintStream;

import ch.ntb.inf.deep.debug.Dbg;

/**
 * Byte Code preprocessor:
 * <br>fixes constant pool indices in accordant instructions
 * <br>sets access and property flags {dpfReadAccess, dpfWriteAccess, dpfCall, dpfInterfCall, dpfNew, dpfTypeTest} accordingly
 */
public class ByteCodePreProc implements ICclassFileConsts, ICjvmInstructionOpcs {
	private static final boolean verbose = false, assertions = true;
	private static PrintStream vrb = Item.vrb;

	/*attribute table (classItems.ICjvmInstructionOpcs.bcAttrTab)
	 * format B:	0xsFFF'owLcc, binary: ssss ' ffff | ffff'ffff | oo ww ' LLLL 	| cccc'cccc
	 * 
	 * S	bit[31..28] (-8 <= o <= 7) 4 bit, change of operand stack pointer (in slots):		slotPointer := slotPointer + SignExtend(s)
	 * 						S=-8: stack change depends on operand type
	 * F	bit[27..16] (0 <= o <= 0xFFF) 12 bit, Flags: {Branch, CondBranch, UncondBranch, Return, Switch, Call, New, } (see const declarations)
	 * o	bit[15..14] (0 <= o <= 2) 2 bit, number of operands (0 undefined for this instruction)
	 * w	bit[13..12] (0 <= w <= 2) 2 bit, number of additional bytes for wide instructions
	 * L	bit[11.. 8] (0 <= o <= 5) 4 bit, instruction length: number of bytes
	 * 
	 * to be eliminated: cc
	 * cc	bit[ 7.. 0] (0 <= cc <= 255) 8 bit, operation code (opc)
	 */
	private static int[] cpNewIndices;
	private static Item[] newConstPool;
	private static byte[] byteCode;
	
	private static int getInt(byte[] bytes, int index){
		return (((bytes[index]<<8) | (bytes[index+1]&0xFF))<<8 | (bytes[index+2]&0xFF))<<8 | (bytes[index+3]&0xFF);
	}

	/**
	 * Replaces the one byte constant pool index <code>byteCode[addr]</code> with the new index in <code>cpNewIndices[oldIndex]</code>
	 * and coalesces the parameter flags (<code>accFlags</code>) with the flags in the field <code>accAndPorpFlags</code>
	 * of the item referenced by the new index.
	 * <br>For test purposes: the referenced item is also returned.
	 * @param addr  the position of the const pool index in the byte code
	 * @param accFlags  additional flags being set
	 * @return
	 */
	private static Item fix1ByteCpIndexAndSetAccFlags(int addr, int accFlags){
		int cpIndex = byteCode[addr] & 0xFF;
		cpIndex = cpNewIndices[cpIndex];
		byteCode[addr] = (byte)cpIndex;
		Item item = newConstPool[cpIndex];
		item.accAndPropFlags |= accFlags;
		return item;
	}

	/**
	 * Replaces the two byte constant pool index at <code>byteCode[addr]</code> with the new index in <code>cpNewIndices[oldIndex]</code>
	 * and coalesces the parameter flags (<code>accFlags</code>) with the flags in the field <code>accAndPorpFlags</code>
	 * of the item referenced by the new index.
	 * <br>For test purposes: the referenced item is also returned.
	 * @param addr  the position of the const pool index in the byte code
	 * @param accFlags  additional flags being set
	 * @return
	 */
	private static Item fix2ByteCpIndexAndSetAccFlags(int addr, int accFlags){
		int cpIndex = (byteCode[addr] & 0xFF)<<8 | (byteCode[addr+1] & 0xFF);
		cpIndex = cpNewIndices[cpIndex];
		byteCode[addr] = (byte)(cpIndex>>>8);
		byteCode[addr+1] = (byte)cpIndex;
		Item item = newConstPool[cpIndex];
		item.accAndPropFlags |= accFlags;
		return item;
	}

	static void  analyseCodeAndFixCpRefs(int[] cpNewIndices, Item[] newConstPool, byte[] byteCode){
		ByteCodePreProc.cpNewIndices = cpNewIndices;
		ByteCodePreProc.newConstPool = newConstPool;
		ByteCodePreProc.byteCode = byteCode;
		if(byteCode == null) return;
		
		int bcLength = byteCode.length;
		int instrAddr = 0;
		while(instrAddr < bcLength){
			int opc = byteCode[instrAddr] & 0xFF;
			int bcAttr = bcAttrTab[opc];
			assert opc == (bcAttr & 0xFF);

			if(verbose) Dbg.printJvmInstr(instrAddr, opc);
 
			int instrLength = (bcAttr >> 8) & 0xF;
			if(instrLength == 0){
				if(opc == bCtableswitch||opc == bClookupswitch){
					int addr = instrAddr + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
//					int defaultOffset = getInt(bc, addr);
					addr += 4; // skip default offset
					if(opc == bCtableswitch){
						int low = getInt(byteCode, addr);
						int high = getInt(byteCode, addr+4);
						instrLength = ((high-low) + 3) * 4 + addr - instrAddr;
					}else{// opc == bClookupswitch
						int nofPairs = getInt(byteCode, addr);
						instrLength = (nofPairs * 2 + 2) * 4	 + (addr - instrAddr);
					}
				}else{// (opc != bCtableswitch & opc != bClookupswitch)
					assert false;
				}
			}else if(opc == bCwide){
				instrAddr++;
				opc = byteCode[instrAddr] & 0xFF;
				bcAttr = bcAttrTab[opc];
				if(assertions) {
					if(verbose) Dbg.printJvmInstr(instrAddr, opc);
					switch(opc){
					case bCiload: case bClload: case bCfload: case bCdload: case bCaload: 
					case bCistore: case bClstore: case bCfstore: case bCdstore: case bCastore: 
					case bCiinc: case bCret:
						break;
					default:
						assert false;
					}
				}
				instrLength = ((bcAttr >> 8) & 0xF) + ((bcAttr >> 12) & 0x3);
			}else if( (bcAttr&(1<<bcapCpRef)) != 0){// (opc != bCtableswitch & opc != bClookupswitch & opc != bCwide)
				int addr = instrAddr+1;
				Item item = null;
				switch(opc){
				case bCldc:
					item = fix1ByteCpIndexAndSetAccFlags(addr, 1<<dpfReadAccess);
					if(assertions) {
						Type type = Type.selectInStringOrPrimitiveTypesByRef(item.type);
						assert type != null;
						assert type.sizeInBits > 0 &&  type.sizeInBits  <= 32;
					}
					break;
				case bCldc_w:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfReadAccess);
					if(assertions){
						Type type = Type.selectInStringOrPrimitiveTypesByRef(item.type);
						assert type != null;
						assert type.sizeInBits > 0 &&  type.sizeInBits  <= 32;
					}
					break;
				case bCldc2_w:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfReadAccess);
					if(assertions){
						Type type = Type.selectInStringOrPrimitiveTypesByRef(item.type);
						assert type != null;
						assert type.sizeInBits == 64;
					}
					break;

				case bCgetstatic: case bCgetfield:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfReadAccess);
					if(assertions){
						assert item != null;
						assert item instanceof DataItem;
					}
					break;
				case bCputstatic: case bCputfield:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfWriteAccess);
					if(assertions) {
						assert item != null;
						assert item instanceof DataItem;
					}
					break;

				case bCinvokevirtual: case bCinvokespecial: case bCinvokestatic:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfCall);
					if(assertions) {
						assert item != null;
						assert item instanceof Method;
					}
					break;
				case bCinvokeinterface:
					item = fix2ByteCpIndexAndSetAccFlags(addr, (1<<dpfInterfCall) );
					if(assertions) {
						assert item != null;
						assert item instanceof Method;
					}
					break;

				case bCnew:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfInstances);
					if(assertions) {
						assert item != null;
						assert item instanceof Class;
					}
					break;
					
				case bCanewarray:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfInstances);
					if(assertions) {
						assert item != null;
						assert item instanceof Class;
					}
					break;

				case bCmultianewarray:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfInstances);
					if(assertions) {
						assert item != null;
						assert item instanceof Class;
					}
					break;

				case bCcheckcast:  case bCinstanceof:
					item = fix2ByteCpIndexAndSetAccFlags(addr, 1<<dpfTypeTest);
					if(assertions) {
						assert item != null;
						assert item instanceof Type;
					}
					break;

				default:
					assert false;
				}
				if(verbose){
					vrb.print("\t\trefed item: ");  item.printName();
					vrb.print(", item.type=");  item.type.printName();
					vrb.print(", owner=");  item.printOwner();
					vrb.print(";//dFlags");  Dbg.printDeepAccAndPropertyFlags(item.accAndPropFlags);
					vrb.println();
				}
			}
			instrAddr = instrAddr + instrLength;
		}
	}
}
