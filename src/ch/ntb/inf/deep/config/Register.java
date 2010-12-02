package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Register {
	HString name;
	Register next;
	Register nextWithInitValue;
	int type = -1;
	int addr = -1;
	int size = -1;
	int repr;
	ValueAssignment init;
	
	public Register(HString name){
		this.name = name;
	}
	
	public void setAddress(int addr){
		this.addr = addr;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setInit(ValueAssignment init){
		this.init = init;
	}
	
	public void setType(int type){
		this.type = type;
		if(repr == 0){
			if(type == Parser.sFPR){
				repr = Parser.sFloat;
			}else{
				repr = Parser.sHex;
			}
		}
	}
	
	public void setInitalValue(ValueAssignment init){
		this.init = init;
	}
	
	public ValueAssignment getInit(){
		return init;
	}
	
	public void setRepresentation(int repr){
		this.repr = repr;
	}
	
	public int getAddress(){
		return addr;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getType(){
		return type;
	}
	
	public int getRepresentation(){
		return repr;
	}
	
	public HString getName(){
		return name;
	}
	
	public HString getTypeName(){
		if(type == Parser.sGPR){
			return HString.getHString("GPR");
		}
		if(type == Parser.sFPR){
			return HString.getHString("FPR");
		}
		if(type == Parser.sSPR){
			return HString.getHString("SPR");
		}
		if(type == Parser.sIOR){
			return HString.getHString("IOR");
		}
		if (type == Parser.sMSR){
			return HString.getHString("MSR");
		}
		if (type == Parser.sCR){
			return HString.getHString("CR");
		}
		if (type == Parser.sFPSCR){
			return HString.getHString("FPSCR");
		}
		
		return HString.getHString("Undefined Type");
	}
	
	public HString getReprName(){
		if(repr == Parser.sDez){
			return HString.getHString("Dez");
		}
		if(repr == Parser.sBin){
			return HString.getHString("Bin");
		}
		if(repr == Parser.sHex){
			return HString.getHString("Hex");
		}
		if(repr == Parser.sFloat){
			return HString.getHString("Float");
		}
		return HString.getHString("Undefined Representation");
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("register " + name.toString() + " {");
		
		for(int i = indentLevel + 1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("type: " + getTypeName() + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("address: " + addr + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("size: " + size + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("repr: " + getReprName() + ";");
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
