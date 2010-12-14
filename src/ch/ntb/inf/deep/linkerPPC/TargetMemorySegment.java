package ch.ntb.inf.deep.linkerPPC;

public class TargetMemorySegment {
	public int startAddress;
	public int[] data;
	public int usedSize = 0; // words
	public TargetMemorySegment next;

	public TargetMemorySegment(int maxSize){
		this.data = new int[maxSize];
	}
	
	public TargetMemorySegment(int startAddress, int maxSize) {
		this.startAddress = startAddress;
		this.data = new int[maxSize/4];
	}
	
	public TargetMemorySegment(int startAddress, int[]data) {
		this.startAddress = startAddress;
		this.data = data;
		this.usedSize = data.length;
	}

	public void addData(int[] data, int length) {
		if (data != null && usedSize + length <= this.data.length) {
			for (int i = 0; i < length; i++) {
				this.data[usedSize + i] = data[i];
			}
			this.usedSize += length;
		}
		else {
			System.out.println("++++++++++ NO DATA ADDED ++++++++++");
			if(data == null) System.out.println("    data is null!");
			if(data.length == 0) System.out.println("    param: data.length is 0!");
			System.out.println("    this.usedSize = " + usedSize);
			System.out.println("    this.data.length = " + this.data.length);
			System.out.println("    param: length = " + length);
		}
	}
	
	public void addData(int[] data) {
		addData(data, data.length);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Target Memory Segment:\n  Base Address: 0x" + Integer.toHexString(startAddress) + "\n  Size: " + data.length + "\n  Content:\n");
		for(int i = 0; i < data.length; i++) {
			sb.append("    0x" + Integer.toHexString((startAddress + i * 4)) + " [" + Integer.toHexString(data[i]) + "]\n");
		}
		return sb.toString();
	}
}
