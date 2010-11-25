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
		this.data = new int[maxSize];
	}
	
	public TargetMemorySegment(int startAddress, int[]data) {
		this.startAddress = startAddress;
		this.data = data;
		this.usedSize = data.length;
	}

	public void addData(int[] data) {
		if (data != null && usedSize + data.length < this.data.length) {
			for (int i = 0; i < data.length; i++) {
				this.data[usedSize + i] = data[i];
			}
			this.usedSize += data.length;
		}
	}
}
