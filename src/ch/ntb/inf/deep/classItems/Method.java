package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class Method extends Item {

	//--- instance fields
	HString methDescriptor;

	public Class owner;
	public byte[] code;
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
//	public void markCall(){ accAndPorpFlags += (1<<dpfCall);  }
//	public void markInterfaceCall(){ accAndPorpFlags += (1<<dpfInterfCall);  }

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

	public Class getOwner(){
		return owner;
	}
	
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
		if(lineNrTab == null) vrb.println("- no LineNumberTable -");
		else{
			int length = lineNrTab.length;
			vrb.printf("line numbers: %1$d pairs of (PC : line#):", length);
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
	
	public void printOwner(){
		owner.printName();
	}

	public void printHeader(){
		Dbg.printJavaAccAndPropertyFlags(accAndPorpFlags);
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
		vrb.print(";//dFlags");  Dbg.printDeepAccAndPropertyFlags(accAndPorpFlags); vrb.println(", nofParams="+nofParams);
		printLocalVars(indentLevel+1);
		printLineNumberTable(indentLevel+1);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
