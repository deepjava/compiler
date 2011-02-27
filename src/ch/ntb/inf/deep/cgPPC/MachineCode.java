package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.ICjvmInstructionOpcs;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linkerPPC.Linker;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.ssa.SSAInstructionMnemonics;
import ch.ntb.inf.deep.ssa.SSAInstructionOpcs;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.SSAValueType;
import ch.ntb.inf.deep.ssa.instruction.Call;
import ch.ntb.inf.deep.ssa.instruction.DyadicRef;
import ch.ntb.inf.deep.ssa.instruction.MonadicRef;
import ch.ntb.inf.deep.ssa.instruction.NoOpndRef;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;
import ch.ntb.inf.deep.strings.HString;

public class MachineCode implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, InstructionOpcs, Registers, ICjvmInstructionOpcs, ICclassFileConsts {
	private static final boolean dbg = false;

	static final int maxNofParam = 32;
	private static final int defaultNofInstr = 16;
	private static final int defaultNofFixup = 8;
	private static final int arrayLenOffset = 6;	

	private static int objectSize, stringSize;
	private static final int constForDoubleConv = 32;// für double convert, kommt weg
	private static int idGET1, idGET2, idGET4, idGET8;
	private static int idPUT1, idPUT2, idPUT4, idPUT8;
	private static int idGETBIT, idASM, idHALT, idADR_OF_METHOD;
	static int idGETGPR, idGETFPR, idGETSPR;
	static int idPUTGPR, idPUTFPR, idPUTSPR;
	
	private static Method stringNewstringMethod;
	private static Method heapNewstringMethod;
	private static Method strInitC;
	private static Method strInitCII;
	private static Method strAllocC;
	private static Method strAllocCII;

	private static int LRoffset;	
	private static int CTRoffset;	
	private static int CRoffset;	
	private static int SRR0offset;	
	private static int SRR1offset;	
	private static int GPRoffset;	
	private static int FPRoffset;	
	private static int localVarOffset;
	private static int tempStorageOffset;	
	private static int paramOffset;
	private static int stackSize;
	static boolean tempStorage;
	
	private static int nofParam;
	private static int nofParamGPR, nofParamFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	static int[] paramType = new int[maxNofParam];
	static boolean[] paramHasNonVolReg = new boolean[maxNofParam];
	static int[] paramRegNr = new int[maxNofParam];
	
	// information about into which registers parameters of this method go 
	private static int nofMoveGPR, nofMoveFPR;
	private static int[] moveGPR = new int[maxNofParam];
	private static int[] moveFPR = new int[maxNofParam];
	
	// information about into which registers parameters of a call to a method go 
	private static int[] destGPR = new int[nofGPR];
	private static int[] destFPR = new int[nofFPR];
	
	private static SSAValue[] lastExitSet;
	private static boolean newString;
	private static Item stringRef;

	public SSA ssa;	// reference to the SSA of a method
	public int[] instructions;	//contains machine instructions for the ssa of a method
	public int iCount;	//nof instructions for this method
	
	Item[] fixups;	// contains all references whose address has to be fixed by the linker
	int fCount;	//nof fixups
	int lastFixup;	// instr number where the last fixup is found

	public MachineCode(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		fixups = new Item[defaultNofFixup];
		nofParamGPR = 0; nofParamFPR = 0;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		tempStorage = false;
		for (int i = 0; i < maxNofParam; i++) {
			paramType[i] = tVoid;
			paramRegNr[i] = -1;
		}

		// make local copy 
		int maxStackSlots = ssa.cfg.method.maxStackSlots;
		int i = maxStackSlots;
		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
			int type = ssa.paramType[i] & 0x7fffffff;
			paramType[i - maxStackSlots] = type;
			paramHasNonVolReg[i - maxStackSlots] = false;
			if (type == tLong || type == tDouble) i++;
			i++;
		}
		nofParam = i - maxStackSlots;
		assert nofParam <= maxNofParam : "method has too many parameters";
		
		if (dbg) StdStreams.out.println("build intervals for " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name);
//		ssa.cfg.printToLog();
//		ssa.print(0);
		RegAllocator.buildIntervals(ssa);
//		ssa.print(0);
		
		if(dbg) StdStreams.out.println("assign registers to parameters, nofParam = " + nofParam);
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) {
			b = (SSANode) b.next;
		}	
		lastExitSet = b.exitSet;
		// determine, which parameters go into which register
		parseExitSet(lastExitSet, maxStackSlots);
		
		if(dbg) StdStreams.out.println("allocate registers");
		RegAllocator.assignRegisters(this);
		if (dbg) ssa.print(0);
		
		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (ssa.cfg.method.name.equals(HString.getHString("reset"))) {	// reset has no prolog
			} else {
				stackSize = calcStackSizeException();
				insertPrologException();
			}
		} else {
			stackSize = calcStackSize();
			iCount = calcPrologSize();
		}
		
		SSANode node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartAddr = iCount;
			translateSSA(node);
			node.codeEndAddr = iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if (node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) {
					int code = this.instructions[node.codeEndAddr];
					//					StdStreams.out.println("target of branch instruction corrected: 0x" + Integer.toHexString(node.codeEndAddr*4));
					CFGNode[] successors = node.successors;
					switch (code & 0xfc000000) {
					case ppcB:			
						if ((code & 0xffff) != 0) {	// switch
							int nofCases = (code & 0xffff) >> 2;
							int k;
							for (k = 0; k < nofCases; k++) {
								int branchOffset = ((SSANode)successors[k]).codeStartAddr - (node.codeEndAddr+1-(nofCases-k)*2);
								this.instructions[node.codeEndAddr+1-(nofCases-k)*2] |= (branchOffset << 2) & 0x3ffffff;
							}
							int branchOffset = ((SSANode)successors[k]).codeStartAddr - node.codeEndAddr;
							this.instructions[node.codeEndAddr] &= 0xfc000000;
							this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						} else {
							//							StdStreams.out.println("abs branch found");
							int branchOffset = ((SSANode)successors[0]).codeStartAddr - node.codeEndAddr;
							this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						}
						break;
					case ppcBc:
						//						StdStreams.out.println("cond branch found");
						int branchOffset = ((SSANode)successors[1]).codeStartAddr - node.codeEndAddr;
						this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0xffff;
						break;
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = iCount - node.codeEndAddr;
						this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						//						StdStreams.out.println("return branch found, epilog start instr = " + iCount);
					}
				}
			}
			node = (SSANode) node.next;
		}
		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (ssa.cfg.method.name.equals(HString.getHString("reset"))) {	// reset needs no epilog
			} else {
				insertEpilogException(stackSize);
			}
		} else {
			insertEpilog(stackSize);
			insertProlog();
		}
		if (dbg) print();
	}

	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
		nofParamGPR = 0; nofParamFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		if(dbg) StdStreams.out.print("[");
		for (int i = 0; i < nofParam; i++) {
			int type = paramType[i];
			if(dbg) StdStreams.out.print("(" + svNames[type] + ")");
			if (type == tLong) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.out.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(gpr, true);
						int regLong = RegAllocator.reserveReg(gpr, true);
						moveGPR[nofMoveGPR] = nofParamGPR;
						moveGPR[nofMoveGPR+1] = nofParamGPR+1;
						nofMoveGPR += 2;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if(dbg) StdStreams.out.print(reg + ",r" + regLong);
					} else {
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR);
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR + 1);
						paramRegNr[i] = paramStartGPR + nofParamGPR;
						paramRegNr[i+1] = paramStartGPR + nofParamGPR + 1;
						if(dbg) StdStreams.out.print((paramStartGPR + nofParamGPR) + ",r" + (paramStartGPR + nofParamGPR + 1));
					}
				}
				nofParamGPR += 2;
				i++;
			} else if (type == tFloat || type == tDouble) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.out.print("fr");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(fpr, true);
						moveFPR[nofMoveFPR] = nofParamFPR;
						nofMoveFPR++;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.out.print(reg);
					} else {
						RegAllocator.reserveReg(fpr, paramStartFPR + nofParamFPR);
						paramRegNr[i] = paramStartFPR + nofParamFPR;
						if(dbg) StdStreams.out.print(paramStartFPR + nofParamFPR);
					}
				}
				nofParamFPR++;
				if (type == tDouble) i++;
			} else {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.out.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(gpr, true);
						moveGPR[nofMoveGPR] = nofParamGPR;
						nofMoveGPR++;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.out.print(reg);
					} else {
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR);
						paramRegNr[i] = paramStartGPR + nofParamGPR;
						if(dbg) StdStreams.out.print(paramStartGPR + nofParamGPR);
					}
				}
				nofParamGPR++;
			}
			if (i < nofParam - 1) if(dbg) StdStreams.out.print(", ");
		}
		if(dbg) StdStreams.out.println("]");
	}

	private static int calcStackSize() {
		int size = 16 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + (tempStorage? 8 : 0);
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		GPRoffset = size - 12 - nofNonVolGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (tempStorage) tempStorageOffset = FPRoffset - 8;
		else tempStorageOffset = FPRoffset;
		return size;
	}

	private static int calcStackSizeException() {
		int size = 24 + nofGPR * 4 + (tempStorage? 8 : 0);
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		CTRoffset = size - 8;
		CRoffset = size - 12;
		SRR0offset = size - 20;
		SRR1offset = size - 16;
		GPRoffset = size - 20 - nofGPR * 4;
		if (tempStorage) tempStorageOffset = GPRoffset - 8;
		else tempStorageOffset = GPRoffset;
		return size;
	}

	private static int calcPrologSize() {
		int size = 3;
		if (nofNonVolGPR > 0) size++;
		size = size + nofNonVolFPR + nofMoveGPR + nofMoveFPR;
		return size;
	}

	private void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg, refReg, indexReg, valReg, bci, offset, type, stringCharOffset, strReg=0;
		Item stringCharRef = null;
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			SSAValue res = instr.result;
//			if (dbg) StdStreams.out.println("ssa opcode at " + instr.result.n + ": " + instr.scMnemonics[instr.ssaOpcode]);
			switch (instr.ssaOpcode) { 
			case sCloadConst:
				opds = instr.getOperands();
				dReg = res.reg;
				if (dReg >= 0) {	// else immediate opd
//StdStreams.out.println("dReg = " + dReg);
					switch (res.type & 0x7fffffff) {
					case tByte: case tShort: case tInteger:
						int immVal = ((StdConstant)res.constant).valueH;
						loadConstant(immVal, dReg);
					break;
					case tLong:	
						StdConstant constant = (StdConstant)res.constant;
						long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
//						StdStreams.out.println("sdfsdge " + immValLong);
						loadConstant((int)(immValLong >> 32), res.regLong);
						loadConstant((int)immValLong, dReg);
						break;	
					case tFloat:	// load from const pool
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3f800000) {	// 1.0
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x3f80);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x40000000) {	// 2.0
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x4000);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regAux1, constant);
							createIrDrAd(ppcLfs, res.reg, res.regAux1, 0);
						}
						break;
					case tDouble:
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3ff00000) {	// 1.0{
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x3ff0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regAux1, constant);
							createIrDrAd(ppcLfd, res.reg, res.regAux1, 0);
						}
						break;
					case tRef:
						if (res.constant == null) // object = null
							loadConstant(0, dReg);
						else	// ref to constant string
							loadConstantAndFixup(res.reg, res.constant);
						break;
					default:
						assert false : "cg: wrong type";
					}
				} else 
				break;	// sCloadConst
			case sCloadLocal:
				break;	// sCloadLocal
			case sCloadFromField:
//				if (dbg) StdStreams.out.println("ssa opcode at " + instr.result.n + ": " + instr.scMnemonics[instr.ssaOpcode]);
				opds = instr.getOperands();
				offset = 0;			
				if (opds == null) {	// getstatic
					sReg1 = res.regAux1;
					Item field = ((NoOpndRef)instr).field;
					loadConstantAndFixup(sReg1, field);
				} else {	// getfield
					if ((ssa.cfg.method.owner == Type.wktString) &&	// string access needs special treatment
							((MonadicRef)instr).item.name.equals(HString.getHString("value"))) {
//						StdStreams.out.println("*****");
						createIrArSrB(ppcOr, res.reg, opds[0].reg, opds[0].reg);	// result contains ref to string
						stringCharRef = ((MonadicRef)instr).item;	// ref to "value"
						stringCharOffset = ((MonadicRef)instr).item.index;	// offset is start of char array
						break;	
					} else {
						sReg1 = opds[0].reg;
						offset = ((MonadicRef)instr).item.offset;
						createItrap(ppcTwi, TOifequal, sReg1, 0);
					}
				}
				switch (res.type & 0x7fffffff) {
				case tBoolean: case tByte:
					createIrDrAd(ppcLbz, res.reg, sReg1, offset);
					createIrArS(ppcExtsb, res.reg, res.reg);
					break;
				case tShort: 
					createIrDrAd(ppcLha, res.reg, sReg1, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrDrAd(ppcLwz, res.reg, sReg1, offset);
					break;
				case tChar: 
					createIrDrAd(ppcLhz, res.reg, sReg1, offset);
					break;
				case tLong:
					createIrDrAd(ppcLwz, res.regLong, sReg1, offset);
					createIrDrAd(ppcLwz, res.reg, sReg1, offset + 4);
					break;
				case tFloat: 
					createIrDrAd(ppcLfs, res.reg, sReg1, offset);
					break;
				case tDouble: 
					createIrDrAd(ppcLfd, res.reg, sReg1, offset);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;	// sCloadFromField
			case sCloadFromArray:
				opds = instr.getOperands();
				if (ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					indexReg = opds[1].reg;	// index into array
					createIrDrAd(ppcLwz, res.regAux1, opds[0].reg, objectSize);	// read field "count", must be first field
					createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte:
						createIrDrAsimm(ppcAddi, res.regAux2, opds[0].reg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(ppcLbzx, res.reg, res.regAux2, indexReg);
						createIrArS(ppcExtsb, res.reg, res.reg);
						break;
					case tChar: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regAux2, opds[0].reg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(ppcLhzx, res.reg, res.regAux1, res.regAux2);
						break;
					default:
						assert false : "cg: type not implemented";
					}
				} else {
					refReg = opds[0].reg;	// ref to array
					indexReg = opds[1].reg;	// index into array
					createItrap(ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(ppcLha, res.regAux1, refReg, -arrayLenOffset);
					createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte: case tBoolean:
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLbzx, res.reg, res.regAux2, indexReg);
						createIrArS(ppcExtsb, res.reg, res.reg);
						break;
					case tShort: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLhax, res.reg, res.regAux1, res.regAux2);
						break;
					case tInteger: case tRef:
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLwzx, res.reg, res.regAux1, res.regAux2);
						break;
					case tLong: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLwzux, res.regLong, res.regAux1, res.regAux2);
						createIrDrAd(ppcLwz, res.reg, res.regAux1, 4);
						break;
					case tFloat:
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLfsx, res.reg, res.regAux1, res.regAux2);
						break;
					case tDouble: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLfdx, res.reg, res.regAux1, res.regAux2);
						break;
					case tChar: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLhzx, res.reg, res.regAux1, res.regAux2);
						break;
					case tAref: case tAchar: case tAfloat: case tAdouble:    //TODO @roger evt. remove this whole case
					case tAbyte: case tAshort: case tAinteger: case tAlong: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrDrArB(ppcLwzx, res.reg, res.regAux1, res.regAux2);
						break;
					default:
						if(dbg) StdStreams.out.println(res.type & 0x7fffffff);//TODO @roger remove this
						assert false : "cg: type not implemented";
					}
				}
				break;	// sCloadFromArray
			case sCstoreToField:
				opds = instr.getOperands();
				if (opds.length == 1) {	// putstatic
					sReg1 = opds[0].reg;
					sReg2 = opds[0].regLong;
					refReg = res.regAux1;
					Item item = ((MonadicRef)instr).item;
					if (item.type.name.charAt(0) == '[')
						type = tRef;
					else
						type = Type.getPrimitiveTypeIndex(item.type.name.charAt(0));
					if (type == -1) type = tRef;
					offset = 0;
					loadConstantAndFixup(res.regAux1, item);
				} else {	// putfield
					refReg = opds[0].reg;
					sReg1 = opds[1].reg;
					sReg2 = opds[1].regLong;
					if (((DyadicRef)instr).field.type.name.charAt(0) == '[')
						type = tRef;
					else
						type = Type.getPrimitiveTypeIndex(((DyadicRef)instr).field.type.name.charAt(0));
					if (type == -1) type = tRef;//TODO @Urs please check this!!!!!!!!!!!!!!
					offset = ((DyadicRef)instr).field.offset;
					createItrap(ppcTwi, TOifequal, refReg, 0);
				}
				switch (type) {
				case tBoolean: case tByte: 
					createIrSrAd(ppcStb, sReg1, refReg, offset);
					break;
				case tShort: case tChar:
					createIrSrAd(ppcSth, sReg1, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrSrAd(ppcStw, sReg1, refReg, offset);
					break;
				case tLong:
					createIrSrAd(ppcStw, sReg2, refReg, offset);
					createIrSrAd(ppcStw, sReg1, refReg, offset + 4);
					break;
				case tFloat: 
					createIrSrAd(ppcStfs, sReg1, refReg, offset);
					break;
				case tDouble: 
					createIrSrAd(ppcStfd, sReg1, refReg, offset);
					break;
				default:
//					if(dbg) StdStreams.out.println(instr.toString());
//					if(dbg) StdStreams.out.println(instr.result.n);
					if(dbg) StdStreams.out.println(type);
					assert false : "cg: wrong type";
				}
				break;	// sCstoreToField
			case sCstoreToArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;	// ref to array
				indexReg = opds[1].reg;	// index into array
				valReg = opds[2].reg;	// value to store
				if (ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
//StdStreams.out.println("stringCharRef found");
					indexReg = opds[1].reg;	// index into array
//					createIrDrAd(ppcLwz, res.regAux1, opds[0].reg, objectSize);	// read field "count", must be first field
//					createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
//					switch (opds[2].type & 0x7fffffff) {
//					case tByte:
//						createIrDrAsimm(ppcAddi, res.regAux2, opds[0].reg, stringSize - 4);	// add index of field "value" to index
//						createIrSrArB(ppcStbx, valReg, indexReg, res.regAux2);
//						break;
//					case tChar: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regAux2, opds[0].reg, stringSize - 4);	// add index of field "value" to index
						createIrSrArB(ppcSthx, valReg, res.regAux1, res.regAux2);
//						break;
//					default:
//						assert false : "cg: type not implemented";
//					}
				} else {
					createItrap(ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(ppcLha, res.regAux1, refReg, -arrayLenOffset);
					createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
					switch (opds[0].type & 0x7fffffff) {
					case tAbyte: case tAboolean:
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcStbx, valReg, indexReg, res.regAux2);
						break;
					case tAshort: case tAchar: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcSthx, valReg, res.regAux1, res.regAux2);
						break;
					case tAref: case tRef: case tAinteger:
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcStwx, valReg, res.regAux1, res.regAux2);
						break;
					case tAlong: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcStwux, opds[2].regLong, res.regAux1, res.regAux2);
						createIrSrAd(ppcStw, valReg, res.regAux1, 4);
						break;
					case tAfloat:  
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcStfsx, valReg, res.regAux1, res.regAux2);
						break;
					case tAdouble: 
						createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regAux2, refReg, objectSize);
						createIrSrArB(ppcStfdx, valReg, res.regAux1, res.regAux2);
						break;
					default:
						assert false : "cg: type not implemented";
					}
				}
				break;	// sCstoreToArray
			case sCadd:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type  & 0x7fffffff) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, immVal);
					} else {
						createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					createIrDrArB(ppcAddc, dReg, sReg1, sReg2);
					createIrDrArB(ppcAdde, res.regLong, opds[0].regLong, opds[1].regLong);
					break;
				case tFloat:
					createIrDrArB(ppcFadds, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFadd, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCsub:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcSubfic, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, -immVal);
					} else {
						createIrDrArB(ppcSubf, dReg, sReg2, sReg1);
					}
					break;
				case tLong:
					createIrDrArB(ppcSubfc, dReg, sReg2, sReg1);
					createIrDrArB(ppcSubfe, res.regLong, opds[1].regLong, opds[0].regLong);
					break;
				case tFloat:
					createIrDrArB(ppcFsubs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFsub, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCmul:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcMulli, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(ppcMulli, dReg, sReg1, immVal);
					} else {
						createIrDrArB(ppcMullw, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					createIrDrArB(ppcMullw, res.regAux1, opds[0].regLong, sReg2);
					createIrDrArB(ppcMullw, res.regAux2, sReg1, opds[1].regLong);
					createIrDrArB(ppcAdd, res.regAux1, res.regAux1, res.regAux2);
					createIrDrArB(ppcMulhwu, res.regAux2, sReg1, sReg2);
					createIrDrArB(ppcAdd, res.regLong, res.regAux1, res.regAux2);
					createIrDrArB(ppcMullw, res.reg, sReg1, sReg2);
					break;
				case tFloat:
					createIrDrArC(ppcFmuls, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArC(ppcFmul, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCdiv:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tByte: case tShort: case tInteger:
					createItrap(ppcTwi, TOifequal, sReg2, 0);
					createIrDrArB(ppcDivw, dReg, sReg1, sReg2);
					break;
				case tLong:
					assert false : "cg: type not implemented";
					break;
				case tFloat:
					createIrDrArB(ppcFdivs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFdiv, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCrem:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type  & 0x7fffffff) {
				case tByte: case tShort: case tInteger:
					createItrap(ppcTwi, TOifequal, sReg2, 0);
					createIrDrArB(ppcDivw, res.regAux1, sReg1, sReg2);
					createIrDrArB(ppcMullw, res.regAux1, res.regAux1, sReg2);
					createIrDrArB(ppcSubf, dReg, res.regAux1 ,sReg1);
					break;
				case tLong:
					assert false : "cg: type not implemented";
					break;
				case tFloat:
					assert false : "cg: type not implemented";
					break;
				case tDouble:
					assert false : "cg: type not implemented";
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCneg:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				type = res.type  & 0x7fffffff;
				if (type == tInteger)
					createIrDrA(ppcNeg, dReg, sReg1);
				else if (type == tLong) {
					createIrDrAsimm(ppcSubfic, dReg, sReg1, 0);
					createIrDrA(ppcSubfze, res.regLong, opds[0].regLong);
				} else if (type == tFloat || type == tDouble)
					createIrDrB(ppcFneg, dReg, sReg1);
				else
					assert false : "cg: wrong type";
				break;
			case sCshl:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type  & 0x7fffffff;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, immVal, 32-immVal, 31);
							createIrArSSHMBME(ppcRlwimi, res.regLong, opds[0].regLong, immVal, 0, 31-immVal);
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
						} else {
							createIrDrAsimm(ppcAddi, dReg, 0, 0);
							createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, immVal-32, 0, 63-immVal);
						}
					} else { 
						createIrDrAsimm(ppcSubfic, res.regAux1, sReg2, 32);
						createIrArSrB(ppcSlw, res.regLong, opds[0].regLong, sReg2);
						createIrArSrB(ppcSrw, 0, sReg1, res.regAux1);
						createIrArSrB(ppcOr, res.regLong, res.regLong, 0);
						createIrDrAsimm(ppcAddi, res.regAux1, sReg2, -32);
						createIrArSrB(ppcSlw, 0, sReg1, res.regAux1);
						createIrArSrB(ppcOr, res.regLong, res.regLong, 0);
						createIrArSrB(ppcSlw, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;	// sCshl
			case sCshr:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type & 0x7fffffff;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSH(ppcSrawi, dReg, sReg1, immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSraw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 32-immVal, immVal, 31);
							createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, 32-immVal, 0, immVal-1);
							createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, immVal);
						} else {
							immVal %= 32;
							createIrArSSH(ppcSrawi, res.reg, opds[0].regLong, immVal);
							createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, 31);
						}
					} else {
						createIrDrAsimm(ppcSubfic, res.regAux1, sReg2, 32);
						createIrArSrB(ppcSrw, dReg, sReg1, sReg2);
						createIrArSrB(ppcSlw, 0, opds[0].regLong, res.regAux1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrDrAsimm(ppcAddi, res.regAux1, sReg2, -32);
						createIrArSrB(ppcSraw, 0, opds[0].regLong, res.regAux1);
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 2);
						createIrArSuimm(ppcOri, dReg, 0, 0);
						createIrArSrB(ppcSraw, res.regLong, opds[0].regLong, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;	//sCshr
			case sCushr:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type  & 0x7fffffff;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, (32-immVal)%32, immVal, 31);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSrw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, (32-immVal)%32, immVal, 31);
							createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, (32-immVal)%32, 0, (immVal-1)&0x1f);
							createIrArSSHMBME(ppcRlwinm, res.regLong, opds[0].regLong, (32-immVal)%32, immVal, 31);
						} else {
							createIrDrAsimm(ppcAddi, res.regLong, 0, 0);
							createIrArSSHMBME(ppcRlwinm, dReg, opds[0].regLong, (64-immVal)%32, immVal-32, 31);
						}
					} else {
						createIrDrAsimm(ppcSubfic, res.regAux1, sReg2, 32);
						createIrArSrB(ppcSrw, dReg, sReg1, sReg2);
						createIrArSrB(ppcSlw, 0, opds[0].regLong, res.regAux1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrDrAsimm(ppcAddi, res.regAux1, sReg2, -32);
						createIrArSrB(ppcSrw, 0, opds[0].regLong, res.regAux1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrArSrB(ppcSrw, res.regLong, opds[0].regLong, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;	// sCushr
			case sCand:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if ((res.type & 0x7fffffff) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						if (immVal >= 0)
							createIrArSuimm(ppcAndi, dReg, sReg2, immVal);
						else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcAnd, dReg, 0, sReg2);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (immVal >= 0)
							createIrArSuimm(ppcAndi, dReg, sReg1, immVal);
						else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcAnd, dReg, 0, sReg1);
						}
					} else
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(ppcAndi, res.regLong, opds[1].regLong, 0);
							createIrArSuimm(ppcAndi, dReg, sReg2, (int)immVal);
						} else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
							createIrArSrB(ppcAnd, dReg, sReg2, 0);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(ppcAndi, res.regLong, opds[0].regLong, 0);
							createIrArSuimm(ppcAndi, dReg, sReg1, (int)immVal);
						} else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
							createIrArSrB(ppcAnd, dReg, sReg1, 0);
						}
					} else {
						createIrArSrB(ppcAnd, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
			break;	// sCand
			case sCor:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if ((res.type & 0x7fffffff) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg2, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg1, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
					} else
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
						createIrArSuimm(ppcOri, dReg, sReg2, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);	
							createIrDrAsimm(ppcAddi, res.regLong, 0, -1);	
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						createIrArSuimm(ppcOri, dReg, sReg1, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
							createIrDrAsimm(ppcAddi, res.regLong, 0, -1);	
						}
					} else {
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;	//sCor
			case sCxor:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if ((res.type & 0x7fffffff) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg2, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg1, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
					} else
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSuimm(ppcXori, dReg, sReg2, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(ppcXori, res.regLong, opds[1].regLong, 0xffff);
							createIrArSuimm(ppcXoris, res.regLong, res.regLong, 0xffff);
						} else {
							createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSuimm(ppcXori, dReg, sReg1, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(ppcXori, res.regLong, opds[0].regLong, 0xffff);
							createIrArSuimm(ppcXoris, res.regLong, res.regLong, 0xffff);
						} else {
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
						}
					} else {
						createIrArSrB(ppcXor, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;	// sCxor
			case sCconvInt:	// int -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tByte:
					createIrArS(ppcExtsb, dReg, sReg1);
					break;
				case tChar: 
					createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(ppcExtsh, dReg, sReg1);
					break;
				case tLong:
					createIrArSrB(ppcOr, dReg, sReg1, sReg1);
					createIrArSSH(ppcSrawi, res.regLong, sReg1, 31);
					break;
				case tFloat:
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					Item item = null;	// hier ref auf 2^52;
					loadConstantAndFixup(res.regAux1, item);
					createIrDrAd(ppcLfd, dReg, res.regAux1, constForDoubleConv);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					createIrDrB(ppcFrsp, dReg, dReg);
					break;
				case tDouble:
					item = null;
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regAux1, item);
					createIrDrAd(ppcLfd, dReg, res.regAux1, constForDoubleConv);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvLong:	// long -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff){
				case tByte:
					createIrArS(ppcExtsb, dReg, sReg1);
					break;
				case tChar: 
					createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(ppcExtsh, dReg, sReg1);
					break;
				case tInteger:
					createIrArSrB(ppcOr, dReg, sReg1, sReg1);
					break;
				case tFloat:
					// noch machen
					break;
				case tDouble:
					// noch machen
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvFloat:	// float -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tByte:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	// noch machen
					break;
				case tDouble:
					createIrDrB(ppcFmr, dReg, sReg1);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvDouble:	// double -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type & 0x7fffffff) {
				case tByte:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					tempStorage = true;
					break;
				case tLong:	
					// noch machen
					break;
				case tFloat:
					createIrDrB(ppcFrsp, dReg, sReg1);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCcmpl:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = opds[0].type & 0x7fffffff;
				if (type == tLong) {
					int sReg1L = opds[0].regLong;
					int sReg2L = opds[1].regLong;
					createICRFrArB(ppcCmp, CRF0, sReg1L, sReg2L);
					createICRFrArB(ppcCmpl, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					if (instr.ssaOpcode == sCregMove) {i++; instr = node.instructions[i+1];}
					assert instr.ssaOpcode == sCbranch : "sCcompl is not followed by branch instruction";
					bci = ssa.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) {
						createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(ppcBc, BOtrue, CRF0EQ, 0);
					} else if (bci == bCifne) {
						createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(ppcBc, BOfalse, CRF0EQ, 0);
					} else if (bci == bCiflt) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifge) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(ppcBc, BOfalse, CRF0LT, 0);
					} else if (bci == bCifgt) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifle) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(ppcBc, BOfalse, CRF0LT, 0);
					} else 
						assert false : "wrong branch instruction after sCcompl";
				} else if (type == tFloat  || type == tDouble) {
					createICRFrArB(ppcFcmpu, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					assert instr.ssaOpcode == sCbranch;
					opds = instr.getOperands();
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					bci = ssa.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) 
						createIBOBIBD(ppcBc, BOtrue, CRF1EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(ppcBc, BOfalse, CRF1EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(ppcBc, BOtrue, CRF1LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(ppcBc, BOfalse, CRF1LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(ppcBc, BOtrue, CRF1GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(ppcBc, BOfalse, CRF1GT, 0);
					else 
						assert false : "wrong branch instruction after sCcompl";
				} else 
					assert false : "cg: wrong type";
				i++;
				break;
			case sCcmpg:
				assert false : "ssa instruction not implemented";
			break;
			case sCinstanceof:
				break;
			case sCalength:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, res.reg , refReg, -arrayLenOffset);
				break;
			case sCcall:
				opds = instr.getOperands();
				Call call = (Call)instr;
				if ((call.item.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if ((call.item.accAndPropFlags & sysMethCodeMask) == idGET1) {	//GET1
						createIrDrAd(ppcLbz, res.reg, opds[0].reg, 0);
						createIrArS(ppcExtsb, res.reg, res.reg);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idGET2) { // GET2
						createIrDrAd(ppcLha, res.reg, opds[0].reg, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idGET4) { // GET4
						createIrDrAd(ppcLwz, res.reg, opds[0].reg, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUT1) { // PUT1
						createIrSrAd(ppcStb, opds[1].reg, opds[0].reg, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUT2) { // PUT2
						createIrSrAd(ppcSth, opds[1].reg, opds[0].reg, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUT4) { // PUT4
						createIrSrAd(ppcStw, opds[1].reg, opds[0].reg, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idGETGPR) { // GETGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createIrArSrB(ppcOr, res.reg, gpr, gpr);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idGETFPR) { // GETFPR
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createIrDrAd(ppcStfd, fpr, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLwz, res.regLong, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLwz, res.reg, stackPtr, tempStorageOffset + 4);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idGETSPR) { // GETSPR
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(ppcMfspr, spr, res.reg);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUTGPR) { // PUTGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createIrArSrB(ppcOr, gpr, opds[1].reg, opds[1].reg);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUTFPR) { // PUTFPR
						createIrDrB(ppcFmr, opds[1].reg, opds[0].reg);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idPUTSPR) { // PUTSPR
						createIrArSrB(ppcOr, 0, opds[1].reg, opds[1].reg);
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(ppcMtspr, spr, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idHALT) { // HALT
						createItrap(ppcTw, TOalways, 0, 0);
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idASM) { // ASM
						instructions[iCount] = InstructionDecoder.getCode(((StringLiteral)opds[0].constant).string.toString());
						iCount++;
						int len = instructions.length;
						if (iCount == len) {
							int[] newInstructions = new int[2 * len];
							for (int k = 0; k < len; k++)
								newInstructions[k] = instructions[k];
							instructions = newInstructions;
						}
					} else if ((call.item.accAndPropFlags & sysMethCodeMask) == idADR_OF_METHOD) { // ADR_OF_METHOD
						HString name = ((StringLiteral)opds[0].constant).string;
						int last = name.lastIndexOf('/');
						HString className = name.substring(0, last);
						HString methName = name.substring(last + 1);
						Class clazz = (Class)(Type.classList.getItemByName(className.toString()));
						Item method = clazz.methods.getItemByName(methName.toString());
						loadConstantAndFixup(res.reg, method);	// addr of method
					}
				} else {
					if ((call.item.accAndPropFlags & (1<<apfStatic)) != 0) {	// invokestatic
						if (call.item == stringNewstringMethod) {	// replace newstring stub with Heap.newstring
							call.item = heapNewstringMethod;
							loadConstantAndFixup(res.regAux1, call.item);	
							createIrSspr(ppcMtspr, LR, res.regAux1); 
						} else {
							loadConstantAndFixup(res.regAux1, call.item);	// addr of method
							createIrSspr(ppcMtspr, LR, res.regAux1);
						}
					} else if ((call.item.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						refReg = opds[0].reg;
						offset = call.item.index;
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, 0);
						createIrSspr(ppcMtspr, LR, res.regAux1);
					} else if (call.invokespecial) {	// invokespecial
						if (newString) {	// special treatment for strings
							if (call.item == strInitC) call.item = strAllocC;
							else if (call.item == strInitCII) call.item = strAllocCII;	// addr of corresponding allocate method
							else if (call.item == strInitCII) call.item = strAllocCII;
							loadConstantAndFixup(res.regAux1, call.item);	
							createIrSspr(ppcMtspr, LR, res.regAux1);
						} else {
							refReg = opds[0].reg;
							createItrap(ppcTwi, TOifequal, refReg, 0);
							loadConstantAndFixup(res.regAux1, call.item);	// addr of init method
							createIrSspr(ppcMtspr, LR, res.regAux1);
						}
					} else {	// invokevirtual 
//						StdStreams.out.println("invokevirtual");
						refReg = opds[0].reg;
						offset = Linker.cdInterface0AddrOffset + ((Method)call.item).owner.nofInterfaces * Linker.slotSize;
						offset += call.item.index * Linker.slotSize; 
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
						createIrSspr(ppcMtspr, LR, res.regAux1);
//						StdStreams.out.println("offset = " + offset);
					}
					for (int k = 0; k < nofGPR; k++) destGPR[k] = 0;
					for (int k = 0; k < nofFPR; k++) destFPR[k] = 0;
					for (int k = 0, kGPR = 0, kFPR = 0; k < opds.length; k++) {
						type = opds[k].type & 0x7fffffff;
						if (type == tLong) {
							destGPR[opds[k].regLong] = kGPR + paramStartGPR;
							destGPR[opds[k].reg] = kGPR + 1 + paramStartGPR;
							kGPR += 2;
						} else if (type == tFloat || type == tDouble) {
							destFPR[opds[k].reg] = kFPR + paramStartFPR;
							kFPR++;
						} else {
							destGPR[opds[k].reg] = kGPR + paramStartGPR;
							kGPR++;
						}
					}
//StdStreams.out.println("destGPR = ");
//for (int h=0; h < 32; h++) 
//	StdStreams.out.print(destGPR[h] + ",");
//StdStreams.out.println();
//StdStreams.out.println("destFPR = ");
//for (int h=0; h < 32; h++) 
//	StdStreams.out.print(destFPR[h] + ",");
//StdStreams.out.println();
					for (int k = 0; k < nofGPR; k++) {
						if (destGPR[k] != 0 && destGPR[k] != k) {
							if (destGPR[destGPR[k]] == 0) {
								createIrArSrB(ppcOr, destGPR[k], k, k);
								destGPR[k] = 0;
							} else {
								createIrArSrB(ppcOr, 0, destGPR[k], destGPR[k]);
								createIrArSrB(ppcOr, destGPR[k], k, k);
								createIrArSrB(ppcOr, k, 0, 0);
								int temp = destGPR[k];
								destGPR[k] = destGPR[temp];
								destGPR[temp] = temp;
								k--;
							}
						}
					}
					for (int k = 0; k < nofFPR; k++) {
						if (destFPR[k] != 0 && destFPR[k] != k) {
							if (destFPR[destFPR[k]] == 0) {
								createIrDrB(ppcFmr, destFPR[k], k);
								destGPR[k] = 0;
							} else {
								createIrDrB(ppcFmr, 0, destFPR[k]);
								createIrDrB(ppcFmr, destFPR[k], k);
								createIrDrB(ppcFmr, k, 0);
								int temp = destFPR[k];
								destFPR[k] = destFPR[temp];
								destFPR[temp] = temp;
								k--;
							}
						}
					}
					if (newString) {
//						loadConstantAndFixup(paramStartGPR, stringRef);	// first reg contains ref to string
						int sizeOfObject = Type.wktObject.getObjectSize();
						createIrDrAsimm(ppcAddi, paramStartGPR+opds.length, 0, sizeOfObject); // reg after last parameter
					}
					createIBOBILK(ppcBclr, BOalways, 0, true);
					type = res.type & 0x7fffffff;
					if (type == tLong) {
						if (res.regLong == returnGPR2) {
							if (res.reg == returnGPR1) {	// returnGPR2 -> r0, returnGPR1 -> r3, r0 -> r2
								createIrArSrB(ppcOr, 0, returnGPR2, returnGPR2);
								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
								createIrArSrB(ppcOr, res.reg, 0, 0);
							} else {	// returnGPR2 -> reg, returnGPR1 -> r3
								createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
							}
						} else { // returnGPR1 -> regLong, returnGPR2 -> reg
							createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
							createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
						}
					} else if (type == tFloat || type == tDouble) {
						createIrDrB(ppcFmr, res.reg, returnFPR);
					} else if (type == tVoid) {
						if (newString) {
							newString = false;
							createIrArSrB(ppcOr, strReg, returnGPR1, returnGPR1);
						}
					} else
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
					
				}
				break;
			case sCnew:
				opds = instr.getOperands();
				Item item = ((Call)instr).item;	// item = ref
				Item method;
				if (opds == null) {	// bCnew
					if (item == Type.wktString) {
						newString = true;	// allocation for strings is postponed
						strReg = res.reg;
						loadConstantAndFixup(res.reg, item);	// ref to string
					} else {
						method = Class.getNewMemoryMethod(bCnew);
						loadConstantAndFixup(paramStartGPR, method);	// addr of new
						createIrSspr(ppcMtspr, LR, paramStartGPR);
						loadConstantAndFixup(paramStartGPR, item);	// ref
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
					}
				} else if (opds.length == 1) {
					switch (res.type & 0x7fffffff) {
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						method = Class.getNewMemoryMethod(bCnewarray);
						loadConstantAndFixup(res.regAux1, method);	// addr of newarray
						createIrSspr(ppcMtspr, LR, res.regAux1);
						// copy parameters
						for (int k = 0; k < nofGPR; k++) destGPR[k] = 0;
						for (int k = 0; k < opds.length; k++) 
							destGPR[opds[k].reg] = k + paramStartGPR;				
						for (int k = 0; k < nofGPR; k++) {
							if (destGPR[k] != 0 && destGPR[k] != k) {
								if (destGPR[destGPR[k]] == 0) {
									createIrArSrB(ppcOr, destGPR[k], k, k);
									destGPR[k] = 0;
								} else {
									createIrArSrB(ppcOr, 0, destGPR[k], destGPR[k]);
									createIrArSrB(ppcOr, destGPR[k], k, k);
									createIrArSrB(ppcOr, k, 0, 0);
									int temp = destGPR[k];
									destGPR[k] = destGPR[temp];
									destGPR[temp] = temp;
									k--;
								}
							}
						}
//						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						createIrDrAsimm(ppcAddi, paramStartGPR + 1, 0, (instr.result.type & 0x7fffffff) - 10);	// type
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
						break;
					case tAref:	// bCanewarray
						method = Class.getNewMemoryMethod(bCanewarray);
						loadConstantAndFixup(res.regAux1, method);	// addr of anewarray
						createIrSspr(ppcMtspr, LR, res.regAux1);
						// copy parameters
						for (int k = 0; k < nofGPR; k++) destGPR[k] = 0;
						for (int k = 0; k < opds.length; k++) 
							destGPR[opds[k].reg] = k + paramStartGPR;				
						for (int k = 0; k < nofGPR; k++) {
							if (destGPR[k] != 0 && destGPR[k] != k) {
								if (destGPR[destGPR[k]] == 0) {
									createIrArSrB(ppcOr, destGPR[k], k, k);
									destGPR[k] = 0;
								} else {
									createIrArSrB(ppcOr, 0, destGPR[k], destGPR[k]);
									createIrArSrB(ppcOr, destGPR[k], k, k);
									createIrArSrB(ppcOr, k, 0, 0);
									int temp = destGPR[k];
									destGPR[k] = destGPR[temp];
									destGPR[temp] = temp;
									k--;
								}
							}
						}
//						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						loadConstantAndFixup(paramStartGPR + 1, item);	// ref
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
						break;
					default:
						assert false : "cg: instruction not implemented";
					}
				} else { // bCmultianewarray:
					method = Class.getNewMemoryMethod(bCmultianewarray);
					loadConstantAndFixup(res.regAux1, method);	// addr of multianewarray
					createIrSspr(ppcMtspr, LR, res.regAux1);
					// copy dimensions
					for (int k = 0; k < nofGPR; k++) destGPR[k] = 0;
					for (int k = 0; k < opds.length; k++) 
						destGPR[opds[k].reg] = k + paramStartGPR + 2;				
//StdStreams.out.println("destGPR = ");
//for (int h=0; h < 32; h++) 
//	StdStreams.out.print(destGPR[h] + ",");
//StdStreams.out.println();
					for (int k = 0; k < nofGPR; k++) {
						if (destGPR[k] != 0 && destGPR[k] != k) {
							if (destGPR[destGPR[k]] == 0) {
								createIrArSrB(ppcOr, destGPR[k], k, k);
								destGPR[k] = 0;
							} else {
								createIrArSrB(ppcOr, 0, destGPR[k], destGPR[k]);
								createIrArSrB(ppcOr, destGPR[k], k, k);
								createIrArSrB(ppcOr, k, 0, 0);
								int temp = destGPR[k];
								destGPR[k] = destGPR[temp];
								destGPR[temp] = temp;
								k--;
							}
						}
					}
					loadConstantAndFixup(paramStartGPR, item);	// ref
					createIrDrAsimm(ppcAddi, paramStartGPR+1, 0, opds.length);	// nofDimensions
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
				}
				break;
			case sCreturn:
				opds = instr.getOperands();
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					break;
				case bClreturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].regLong, opds[0].regLong);
					createIrArSrB(ppcOr, returnGPR2, opds[0].reg, opds[0].reg);
					break;
				case bCfreturn:
				case bCdreturn:
					createIrDrB(ppcFmr, returnFPR, opds[0].reg);
					break;
				default:
					assert false : "cg: return instruction not implemented";
				}
				if (node.next != null)	// last node needs no branch
					createIli(ppcB, 0, false);
				break;
			case sCthrow:
/*				refReg = opds[0].reg;
				offset = call.item.offset;
				createItrap(ppcTwi, TOifequal, refReg, 0);
			createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
			createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
			createIrDrAd(ppcLwz, res.regAux1, res.regAux1, 0);
			createIrSspr(ppcMtspr, LR, res.regAux1);
				opds = instr.getOperands();
				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);*/
				break;
			case sCbranch:
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createIli(ppcB, 0, false);
					break;
				case bCif_acmpeq:
				case bCif_acmpne:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					if (bci == bCif_acmpeq)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					break;
				case bCif_icmpeq:
				case bCif_icmpne:
				case bCif_icmplt:
				case bCif_icmpge:
				case bCif_icmpgt:
				case bCif_icmple:
					boolean inverted = false;
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
//					assertEquals("cg: wrong type", opds[0].type, tInteger);
//					assertEquals("cg: wrong type", opds[1].type, tInteger);
					if (sReg1 < 0) {
						if (opds[0].constant != null) {
//							StdStreams.out.println("constant is not null");
							int immVal = ((StdConstant)opds[0].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767))
								createICRFrASimm(ppcCmpi, CRF0, sReg2, immVal);
							else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else if (sReg2 < 0) {
						if (opds[1].constant != null) {
							int immVal = ((StdConstant)opds[1].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {
								inverted = true;
								createICRFrASimm(ppcCmpi, CRF0, sReg1, immVal);
							} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
							createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else {
						createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
					} else {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
					}
					break;
				case bCifeq:
				case bCifne:
				case bCiflt:
				case bCifge:
				case bCifgt:
				case bCifle:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
//					assertEquals("cg: wrong type", opds[0].type, tInteger);
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifeq) 
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
					break;
				case bCifnonnull:
				case bCifnull:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifnonnull)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					break;
				case bCtableswitch:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					int addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int low = getInt(ssa.cfg.code, addr);
					int high = getInt(ssa.cfg.code, addr + 4);
					int nofCases = high - low + 1;
					for (int k = 0; k < nofCases; k++) {
						createICRFrASimm(ppcCmpi, CRF0, sReg1, low + k);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(ppcB, nofCases, false);
					break;
				case bClookupswitch:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int nofPairs = getInt(ssa.cfg.code, addr);
					for (int k = 0; k < nofPairs; k++) {
						int key = getInt(ssa.cfg.code, addr + 4 + k * 8);
						createICRFrASimm(ppcCmpi, CRF0, sReg1, key);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(ppcB, nofPairs, true);
					break;
				default:
//					StdStreams.out.println(bci);
					assert false : "cg: no such branch instruction";
				}
				break;
			case sCregMove:
				opds = instr.getOperands();
				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);
				break;
			default:
				assert false : "cg: no code generated for " + instr.scMnemonics[instr.ssaOpcode] + " function";
			}
		}
	}

	private static int getInt(byte[] bytes, int index){
		return (((bytes[index]<<8) | (bytes[index+1]&0xFF))<<8 | (bytes[index+2]&0xFF))<<8 | (bytes[index+3]&0xFF);
	}

	private void createIrArS(int opCode, int rA, int rS) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16);
		incInstructionNum();
	}

	private void createIrD(int opCode, int rD) {
		instructions[iCount] = opCode | (rD << 21);
		incInstructionNum();
	}

	private void createIrDrArB(int opCode, int rD, int rA, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrDrArC(int opCode, int rD, int rA, int rC) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6);
		incInstructionNum();
	}

	private void createIrSrArB(int opCode, int rS, int rA, int rB) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrArSrB(int opCode, int rA, int rS, int rB) {
		if ((opCode == ppcOr) && (rA == rS) && (rA == rB)) return; 	// lr x,x makes no sense
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrDrAd(int opCode, int rD, int rA, int d) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (d  & 0xffff);
		incInstructionNum();
	}

	private void createIrDrAsimm(int opCode, int rD, int rA, int simm) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (simm  & 0xffff);
		incInstructionNum();
	}

	private void createIrArSuimm(int opCode, int rA, int rS, int uimm) {
		instructions[iCount] = opCode | (rA << 16) | (rS << 21) | (uimm  & 0xffff);
		incInstructionNum();
	}

	private void createIrSrASimm(int opCode, int rS, int rA, int simm) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (simm  & 0xffff);
		incInstructionNum();
	}

	private void createIrSrAd(int opCode, int rS, int rA, int d) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (d  & 0xffff);
		incInstructionNum();
	}

	private void createIrArSSH(int opCode, int rA, int rS, int SH) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11);
		incInstructionNum();
	}

	private void createIrArSSHMBME(int opCode, int rA, int rS, int SH, int MB, int ME) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11) | (MB << 6) | (ME << 1);
		incInstructionNum();
	}

	private void createIrArSrBMBME(int opCode, int rA, int rS, int rB, int MB, int ME) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11) | (MB << 6) | (ME << 1);
		incInstructionNum();
	}

	private void createItrap(int opCode, int TO, int rA, int rB) {
		instructions[iCount] = opCode | (TO << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createItrapSimm(int opCode, int TO, int rA, int imm) {
		instructions[iCount] = opCode | (TO << 21) | (rA << 16) | (imm & 0xffff);
		incInstructionNum();
	}

	private void createIli(int opCode, int LI, boolean link) {
		instructions[iCount] = opCode | (LI << 2 | (link ? 1 : 0));
		incInstructionNum();
	}

	private void createIBOBIBD(int opCode, int BO, int BI, int BD) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (BD << 2);
		incInstructionNum();
	}

	private void createIBOBILK(int opCode, int BO, int BI, boolean link) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (link?1:0);
		incInstructionNum();
	}

	private void createICRFrArB(int opCode, int crfD, int rA, int rB) {
		instructions[iCount] = opCode | (crfD << 23) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createICRFrASimm(int opCode, int crfD, int rA, int simm) {
		instructions[iCount] = opCode | (crfD << 23) | (rA << 16) | (simm & 0xffff);
		incInstructionNum();
	}

	private void createIcrbDcrbAcrbB(int opCode, int crbD, int crbA, int crbB) {
		instructions[iCount] = opCode | (crbD << 21) | (crbA << 16) | (crbB << 11);
		incInstructionNum();
	}
	
	private void createICRMrS(int opCode, int CRM, int rS) {
		instructions[iCount] = opCode | (rS << 21) | (CRM << 12);
		incInstructionNum();
	}
	
	private void createIrDrA(int opCode, int rD, int rA) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16);
		incInstructionNum();
	}

	private void createIrDrB(int opCode, int rD, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rB << 11);
		incInstructionNum();
	}

	private void createIrSspr(int opCode, int spr, int rS) {
		int temp = ((spr & 0x1F) << 5) | ((spr & 0x3E0) >> 5);
		if (spr == 268 || spr == 269) opCode = ppcMftb;
		instructions[iCount] = opCode | (temp << 11) | (rS << 21);
		incInstructionNum();
	}

	private void createIrfi(int opCode) {
		instructions[iCount] = opCode;
		incInstructionNum();
	}

	private void loadConstant(int val, int reg) {
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if ((low >> 15) == 0) {
			if (low != 0 && high != 0) {
				createIrDrAsimm(ppcAddi, reg, 0, low);
				createIrDrAsimm(ppcAddis, reg, reg, high);
			} else if (low == 0 && high != 0) {
				createIrDrAsimm(ppcAddis, reg, 0, high);		
			} else if (low != 0 && high == 0) {
				createIrDrAsimm(ppcAddi, reg, 0, low);
			} else createIrDrAsimm(ppcAddi, reg, 0, 0);
		} else {
			createIrDrAsimm(ppcAddi, reg, 0, low);
			if (((high + 1) & 0xffff) != 0) createIrDrAsimm(ppcAddis, reg, reg, high + 1);
		}
	}
	
	private void loadConstantAndFixup(int reg, Item item) {
		assert lastFixup >= 0 && lastFixup <= 32768 : "fixup is out of range";
		createIrDrAsimm(ppcAddi, reg, 0, lastFixup);
		createIrDrAsimm(ppcAddis, reg, reg, 0);
		lastFixup = iCount - 2;
		fixups[fCount] = item;
//		StdStreams.out.print("insert fixup, item = "); item.printName(); StdStreams.out.println();
		fCount++;
		int len = fixups.length;
		if (fCount == len) {
			Item[] newFixups = new Item[2 * len];
			for (int k = 0; k < len; k++)
				newFixups[k] = fixups[k];
			fixups = newFixups;
		}		
	}
	
	public void doFixups() {
		int currInstr = lastFixup;
		int currFixup = fCount - 1;
		while (currFixup >= 0) {
			Item item = fixups[currFixup];
			int addr;
			if (item == null) // item is null if constant null is loaded (aconst_null) 
				addr = 0;
			else 
				addr = fixups[currFixup].address;
			if (dbg) { 
				StdStreams.out.print("\t fix item ");
				if(item == null) StdStreams.out.print("null"); 
				else item.printName();
				StdStreams.out.println(" at address = " + Integer.toHexString(addr));
			}
			int low = addr & 0xffff;
			int high = (addr >> 16) & 0xffff;
			if (!((low >> 15) == 0)) high++;
			int nextInstr = instructions[currInstr] & 0xffff;
			instructions[currInstr] = (instructions[currInstr] & 0xffff0000) | (low & 0xffff);
			instructions[currInstr+1] = (instructions[currInstr+1] & 0xffff0000) | (high & 0xffff);
			currInstr = nextInstr;
			currFixup--;
		}
	}

	private void incInstructionNum() {
		iCount++;
		int len = instructions.length;
		if (iCount == len) {
			int[] newInstructions = new int[2 * len];
			for (int k = 0; k < len; k++)
				newInstructions[k] = instructions[k];
			instructions = newInstructions;
		}
	}

	private void insertProlog() {
		int count = iCount;
		iCount = 0;
		createIrSrASimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, LRoffset);
		if (nofNonVolGPR > 0) {
			createIrSrAd(ppcStmw, nofGPR-nofNonVolGPR, stackPtr, GPRoffset);
		}
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++)
				createIrSrAd(ppcStfd, topFPR-i, stackPtr, FPRoffset + i * 8);
		}
		for (int i = 0; i < nofMoveGPR; i++)
			createIrArSrB(ppcOr, topGPR-i, moveGPR[i]+paramStartGPR, moveGPR[i]+paramStartGPR);
		for (int i = 0; i < nofMoveFPR; i++)
			createIrDrB(ppcFmr, topFPR-i, moveFPR[i]+paramStartFPR);
		iCount = count;
	}

	private void insertPrologException() {
		iCount = 0;
		createIrSrASimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSrASimm(ppcStw, 0, stackPtr, GPRoffset);
		createIrSspr(ppcMfspr, SRR0, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, SRR0offset);
		createIrSspr(ppcMfspr, SRR1, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, SRR1offset);
		createIrSspr(ppcMtspr, EID, 0);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, LRoffset);
		createIrSspr(ppcMfspr, CTR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, CTRoffset);
		createIrD(ppcMfcr, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, CRoffset);
		createIrSrAd(ppcStmw, 2, stackPtr, GPRoffset + 8);
	}

	private void insertEpilog(int stackSize) {
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++)
				createIrDrAd(ppcLfd, topFPR-i, stackPtr, FPRoffset + i * 8);
		}
		if (nofNonVolGPR > 0)
			createIrDrAd(ppcLmw, nofGPR - nofNonVolGPR, stackPtr, GPRoffset);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBILK(ppcBclr, BOalways, 0, false);
	}

	private void insertEpilogException(int stackSize) {
		createIrDrAd(ppcLmw, 2, stackPtr, GPRoffset + 8);
		createIrDrAd(ppcLwz, 0, stackPtr, CRoffset);
		createICRMrS(ppcMtcrf, 0xff, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, CTRoffset);
		createIrSspr(ppcMtspr, CTR, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, SRR1offset);
		createIrSspr(ppcMtspr, SRR1, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, SRR0offset);
		createIrSspr(ppcMtspr, SRR0, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, GPRoffset);
		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIrfi(ppcRfi);
	}
	
	public void print(){
//		StdStreams.out.println("Method information for " + ssa.cfg.method.name);
		Method m = ssa.cfg.method;
//		StdStreams.out.println("Method has " + m.maxLocals + " locals where " + m.nofParams + " are parameters");
//		StdStreams.out.println("stackSize = " + stackSize);
//		StdStreams.out.println("Method uses " + nofNonVolGPR + " GPR and " + nofNonVolFPR + " FPR for locals where " + nofParamGPR + " GPR and " + nofParamFPR + " FPR are for parameters");
//		StdStreams.out.println();
		
		StdStreams.out.println("Code for Method: " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name +  ssa.cfg.method.methDescriptor);

		for (int i = 0; i < iCount; i++){
			StdStreams.out.print("\t" + Integer.toHexString(instructions[i]));
			StdStreams.out.print("\t[0x");
			StdStreams.out.print(Integer.toHexString(i*4));
			StdStreams.out.print("]\t" + InstructionDecoder.getMnemonic(instructions[i]));
			int opcode = (instructions[i] & 0xFC000000) >>> (31 - 5);
		if (opcode == 0x10) {
			int BD = (short)(instructions[i] & 0xFFFC);
			StdStreams.out.print(", [0x" + Integer.toHexString(BD + 4 * i) + "]\t");
		} else if (opcode == 0x12) {
			int li = (instructions[i] & 0x3FFFFFC) << 6 >> 6;
			StdStreams.out.print(", [0x" + Integer.toHexString(li + 4 * i) + "]\t");
		}
		StdStreams.out.println();
		}
	}

	public static void init() { 
		idPUT1 = 0x001;	// same as in rsc/ntbMpc555STS.deep
		idPUT2 = 0x002;
		idPUT4 = 0x003;
		idPUT8 = 0x004;
		idGET1 = 0x005;	
		idGET2 = 0x006;
		idGET4 = 0x007;
		idGET8 = 0x008;
		idGETBIT = 0x009;
		idASM = 0x00a;
		idGETGPR = 0x00b;
		idGETFPR = 0x00c;
		idGETSPR = 0x00d;
		idPUTGPR = 0x00e;
		idPUTFPR = 0x00f;
		idPUTSPR = 0x010;
		idADR_OF_METHOD = 0x011;
		idHALT = 0x012;
		objectSize = Type.wktObject.getObjectSize();
		stringSize = Type.wktString.getObjectSize();
		final Class stringClass = (Class)Type.wktString;
		final Class heapClass = (Class)Type.classList.getItemByName(Configuration.getHeapClassname().toString());
		if (stringClass != null) {
			stringNewstringMethod = (Method)stringClass.methods.getItemByName("newstring"); // TODO improve this
			if(heapClass != null) {
				heapNewstringMethod = (Method)heapClass.methods.getItemByName("newstring"); // TODO improve this
			}
			if(dbg) {
				if (stringNewstringMethod != null) StdStreams.out.println("stringNewstringMethod = " + stringNewstringMethod.name + stringNewstringMethod.methDescriptor); else StdStreams.out.println("stringNewstringMethod: not found");
				if (heapNewstringMethod != null) StdStreams.out.println("heapNewstringMethod = " + heapNewstringMethod.name + heapNewstringMethod.methDescriptor); else StdStreams.out.println("heapNewstringMethod: not found");
			}
			
			Method m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getHString("<init>"))) {
					if (m.methDescriptor.equals(HString.getHString("([C)V"))) strInitC = m; 
					else if (m.methDescriptor.equals(HString.getHString("([CII)V"))) strInitCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strInitC != null) StdStreams.out.println("stringInitC = " + strInitC.name + strInitC.methDescriptor); else StdStreams.out.println("stringInitC: not found");
				if (strInitCII != null) StdStreams.out.println("stringInitCII = " + strInitCII.name + strInitCII.methDescriptor); else StdStreams.out.println("stringInitCII: not found");
			}
			
			m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getHString("allocateString"))) {
					if (m.methDescriptor.equals(HString.getHString("(I[C)Ljava/lang/String;"))) strAllocC = m; 
					else if (m.methDescriptor.equals(HString.getHString("(I[CII)Ljava/lang/String;"))) strAllocCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strAllocC != null) StdStreams.out.println("allocateStringC = " + strAllocC.name + strAllocC.methDescriptor); else StdStreams.out.println("allocateStringC: not found");
				if (strAllocCII != null) StdStreams.out.println("allocateStringCII = " + strAllocCII.name + strAllocCII.methDescriptor); else StdStreams.out.println("allocateStringCII: not found");
			}
		}
	}

}