package ch.ntb.inf.deep.linker;

import ch.ntb.inf.deep.strings.HString;

public class FixedValueItem extends BlockItem {
	
	private static final int size = 4;
	
	int val;
		
	public FixedValueItem(HString name, int val) {
		this.name = name;
		this.val = val;
	}
	
	public FixedValueItem(int val) {
		this(HString.getHString("???"), val);
	}
	
	public FixedValueItem(HString name) {
		this(name, -1);
	}
	
	public FixedValueItem(String name) {
		this(HString.getHString(name), -1);
	}
	
	public FixedValueItem(String name, int val) {
		this(HString.getHString(name), val);
	}
	
	public void setValue(int val) {
		this.val = val;
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = val;
			written = size;
		}
		return written;
	}
	
	public String toString() {
		return String.format("[%08X]", val) + " (" + name + ")";
	}
	
	public int getValue() {
		return val;
	}

}
