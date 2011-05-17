package ch.ntb.inf.deep.linker;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class AddressItem extends BlockItem {
	
	private static final int size = 4;
	
	Item ref;
	
	public AddressItem(Item ref) {
		this.ref = ref;
		if(ref.name != null) this.name = ref.name;
		else name = HString.getHString("???");
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = ref.address;
			written = size;
		}
		return written;
	}
	
	public String toString() {
		return String.format("[%08X]", ref.address) + " (" + name + ")";
	}
	
	public int getAddress() {
		return ref.address;
	}

}
