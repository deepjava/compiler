package ch.ntb.inf.deep.linkerPPC;

public class TargetMemorySegment {
	private static int tmsCounter = 0;
	
	public int id;
	public int startAddress;
	public int[] data;
	public TargetMemorySegment next;

	public TargetMemorySegment(int maxSize){
		this.id = tmsCounter++;
		this.data = new int[maxSize];
	}
	
	public TargetMemorySegment(int startAddress, int maxSize) {
		this.id = tmsCounter++;
		this.startAddress = startAddress;
		this.data = new int[maxSize/4];
	}
	
	public TargetMemorySegment(int startAddress, int[]data) {
		this.id = tmsCounter++;
		this.startAddress = startAddress;
		this.data = data;
	}
	
	// TODO @Martin: add return value (boolean)
	public void addData(int addr, int[] d, int length) {
	//	System.out.println("DBG: addr = " + addr + ", d.length = " + d.length + ", length = " + length);
		if(d != null && 
				d.length > 0 && 
				length > 0 && 
				length <= d.length && 
				addr >= this.startAddress && 
				addr + length * 4 <= this.startAddress + this.data.length * 4) {
			
			for(int i = 0; i < length; i++) {
	//			System.out.println("           > Writing 0x" + Integer.toHexString(d[i]) + " to 0x" + Integer.toHexString(addr + i * 4) + " (" + ((addr - this.startAddress) / 4 + i) + ")");
				this.data[(addr - this.startAddress) / 4 + i] = d[i];
			}
		}
		else { // TODO @Martin: remove all debung outputs!
			System.out.println("          ++++++++++ ERROR ++++++++++");
			System.out.println("            > Cound not add the data to the target memory segment.");
			System.out.print("            > The error was: ");
			if(d == null) System.out.println("the given data array (d) was null!");
			else if(d.length <= 0 || length <= 0) System.out.println("the array length or the given length was zero!");
			else if(addr < this.startAddress || addr + length * 4 > this.startAddress + this.data.length * 4) System.out.println("out of range!");
			else if(length > d.length) System.out.println("length > d.length");
			else System.out.println("<unknown>");
			System.out.println("              Start address of tms: 0x" + Integer.toHexString(this.startAddress));
			System.out.println("              End address of tms: 0x" + Integer.toHexString((this.startAddress + this.data.length * 4)));
			System.out.println("              Address for inserting data: 0x" + Integer.toHexString(addr));
			System.out.println("              Size of the data to insert: " + d.length * 4 + " byte (0x" + Integer.toHexString(d.length * 4) + " byte)");
			
		}
	}
	
	public void addData(int addr, int[] d) {
		addData(addr, d, d.length);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Target Memory Segment:\n  Base Address: 0x" + Integer.toHexString(startAddress) + "\n  Size: " + data.length * 4 + " byte\n  Content:\n");
		for(int i = 0; i < data.length; i++) {
			sb.append("    0x" + Integer.toHexString((startAddress + i * 4)) + " [" + Integer.toHexString(data[i]) + "]\n");
		}
		return sb.toString();
	}
}
