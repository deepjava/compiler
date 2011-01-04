package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.MachineCode;
import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;

public class Method extends ClassMember {

	//--- instance fields
	public HString methDescriptor;

//	public Class owner;
	public byte[] code; // java byte code of this method
	
	public MachineCode machineCode; // machine code of this method
	public CFG cfg; // cfg of this method
	public SSA ssa; // ssa of this method
	
	ExceptionTabEntry[] exceptionTab;
	LocalVar[] localVars;
	int[] lineNrTab; // entry: (startPc<<16) | lineNr

	public byte nofParams;
	public int maxStackSlots, maxLocals;

	//--- constructors
	public Method(HString name, Type returnType, HString methDescriptor){
		super(name, returnType);
		this.methDescriptor = methDescriptor;
		nofParams = (byte)Type.nofParameters(methDescriptor);
	}

	//--- instance methods
//	public void markCall(){ accAndPropFlags += (1<<dpfCall);  }
//	public void markInterfaceCall(){ accAndPropFlags += (1<<dpfInterfCall);  }


	Method getMethod(HString name, HString descriptor){
		Method item = this;
		while(item != null && (item.name != name || item.methDescriptor != descriptor) ) item = (Method)item.next;
		return item;
	}

	void clearCodeAndAssociatedFields(){
		code = null;
		localVars  = null;
		lineNrTab = null;
	}

	void insertLocalVar(LocalVar locVar){
		int key = locVar.index;
		Item lv = localVars[locVar.index], pred = null;
		while(lv != null && key >= locVar.index){
			pred = lv;
			lv = lv.next;
		}
		locVar.next = lv;
		if(pred == null) localVars[locVar.index] = locVar;  else  pred.next = locVar;
	}

	void preProcessCode(){
		// TODO
	}
	
	public byte[] getCode(){
		return code;
	}

//	public Class getOwner(){
//		return owner;
//	}
	
	public int getMaxLocals(){
		return maxLocals;
	}
	
	public int getMaxStckSlots(){
		return maxStackSlots;
	}

	//--- debug primitives
	public void printItemCategory(){
		vrb.print("meth");
	}

	public void printLocalVars(int indentLevel){
		indent(indentLevel); vrb.print("local variables:");
		if(localVars == null) vrb.println(" none");
		else{
			vrb.println();
			for(int index=0; index < localVars.length; index++){
				Item lv = localVars[index];
				while(lv != null){
					lv.print(indentLevel+1); vrb.println();
					lv = lv.next;
				}
			}
		}
	}

	public void printLineNumberTable(int indentLevel){
		indent(indentLevel);
		vrb.print("line numbers: ");
		if(lineNrTab == null) vrb.println("none");
		else{
			int length = lineNrTab.length;
			vrb.printf("%1$d pairs of (PC : line#):", length);
			for(int index = 0; index < length; index++){
				if( (index&(8-1)) == 0) {
					vrb.println();
					indent(indentLevel);
				}
				int pair = lineNrTab[index];
				vrb.printf(" (%1$d : %2$d)", pair>>>16, pair&0xFFFF);
			}
			if( (length&(8-1)) != 0) vrb.println();
		}
	}

	public void printHeader(){
		int flags = accAndPropFlags;
		if( (flags & (1<<dpfSysPrimitive)) != 0 ) flags &=  ~sysMethCodeMask;
		Dbg.printJavaAccAndPropertyFlags(flags);
		type.printTypeCategory(); type.printName(); // return type
		vrb.print(' ');  vrb.print(name);  vrb.print(methDescriptor);
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.print("meth  ");
		vrb.print(this.owner.name); vrb.print(":: ");
		super.printShort(0);
		vrb.print(methDescriptor);
	}

	public void print(int indentLevel){
		indent(indentLevel);
		printHeader();
		vrb.print(";//dFlags");
		if( (accAndPropFlags & (1<<dpfSysPrimitive)) == 0 ) Dbg.printDeepAccAndPropertyFlags(accAndPropFlags);
		else{
			Dbg.printDeepAccAndPropertyFlags(accAndPropFlags & ~sysMethCodeMask);
			vrb.printf("; mAttr=0x%1$3x", accAndPropFlags & sysMethCodeMask);
		}
		vrb.println(", nofParams="+nofParams);
		indent(indentLevel+1);
		vrb.print("code: ");
		if(this.code == null) vrb.println("none"); else vrb.printf("%1$d B\n", code.length);
		printLocalVars(indentLevel+1);
		printLineNumberTable(indentLevel+1);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
