package ch.ntb.inf.deep.classItems;

public class LocalVar extends DataItem {

	//--- instance fields
	int startPc, length; // life range: [startPc, startPc+length]
	int index; // starts at this slot, long and double occupy this slot and next slot (index+1)

	//--- constructors
	LocalVar(){
	}

	//--- debug primitives
	
	public void print(int indentLevel){
		indent(indentLevel);
		vrb.printf("[%1$2d] (%2$c)%3$s %4$s  [%5$d,%6$d]", index, (char)((Type)type).category, type.name, name, startPc, startPc+length);
	}
}
