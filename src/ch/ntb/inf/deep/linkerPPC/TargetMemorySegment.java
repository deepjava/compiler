package ch.ntb.inf.deep.linkerPPC;

public class TargetMemorySegment {
	public int startAddress;
	public int[] data;
	public int size;
	public TargetMemorySegment next;

	public TargetMemorySegment(int startAddress, int segmentSize) {
		this.startAddress = startAddress;
		this.data = new int[segmentSize];
	}
	
	public TargetMemorySegment(int startAddress, int[]data) {
		this.startAddress = startAddress;
		this.data = data;
	}

	public void addData(int offset, int[] data) {
		if (offset + data.length < this.data.length) {
			for (int i = 0; i < data.length; i++) {
				this.data[i + offset] = data[i];
			}
		}
	}

}
