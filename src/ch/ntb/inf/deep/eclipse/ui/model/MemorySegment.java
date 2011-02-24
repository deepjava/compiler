package ch.ntb.inf.deep.eclipse.ui.model;

public class MemorySegment {
	public int addr;
	public int value;
	
	public MemorySegment(){
		addr = -1;
		value = 0;
	}
	
	public MemorySegment(int addr, int value){
		this.addr = addr;
		this.value = value;
	}
	
}
