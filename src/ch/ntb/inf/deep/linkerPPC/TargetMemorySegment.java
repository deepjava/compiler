package ch.ntb.inf.deep.linkerPPC;

public class TargetMemorySegment {
	public int startAddress;
	public int[] data;
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
	}

/*	public void addData(int[] data, int length) {
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
	}*/
	
	
	public void addData(int addr, int[] d) {
		if(d != null && d.length > 0 && addr >= this.startAddress && addr + d.length * 4 <= this.startAddress + this.data.length * 4) {
			for(int i = 0; i < d.length; i++) {
				this.data[(addr - this.startAddress) / 4 + i] = d[i];
			}
		}
		else {
			System.out.println("          ++++++++++ ERROR ++++++++++");
			System.out.println("            > Cound not add the data to the target memory segment.");
			System.out.print("            > The error was: ");
			if(d == null) System.out.println("the given data array (d) was null!");
			else if(d.length <= 0) System.out.println("he array length (of d) was zero!");
			else if(addr < this.startAddress || addr + d.length * 4 > this.startAddress + this.data.length * 4) {
				System.out.println("out of range!");
				System.out.println("              Start address of tms: 0x" + Integer.toHexString(this.startAddress));
				System.out.println("              End address of tms: 0x" + Integer.toHexString((this.startAddress + this.data.length * 4)));
				System.out.println("              Address for inserting data: 0x" + Integer.toHexString(addr));
				System.out.println("              Size of the data to insert: " + d.length * 4 + " byte (0x" + Integer.toHexString(d.length * 4) + " byte)");
			}
			else {
				System.out.println("<unknown>");
				System.out.println("              Start address of tms: 0x" + Integer.toHexString(this.startAddress));
				System.out.println("              End address of tms: 0x" + Integer.toHexString((this.startAddress + this.data.length * 4)));
				System.out.println("              Address for inserting data: 0x" + Integer.toHexString(addr));
				System.out.println("              Size of the data to insert: " + d.length * 4 + " byte (0x" + Integer.toHexString(d.length * 4) + " byte)");
			}
			
		}
	}
	
/*	public void addData(int[] data) {
		addData(data, data.length);
	}*/
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Target Memory Segment:\n  Base Address: 0x" + Integer.toHexString(startAddress) + "\n  Size: " + data.length * 4 + " byte\n  Content:\n");
		for(int i = 0; i < data.length; i++) {
			sb.append("    0x" + Integer.toHexString((startAddress + i * 4)) + " [" + Integer.toHexString(data[i]) + "]\n");
		}
		return sb.toString();
	}
}
