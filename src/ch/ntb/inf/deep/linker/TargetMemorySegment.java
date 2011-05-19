package ch.ntb.inf.deep.linker;

import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.host.StdStreams;

public class TargetMemorySegment {
	private static int tmsCounter = 0;
	
	public int id;
	public int startAddress;
	public int[] data;
	public TargetMemorySegment next;
	public Segment segment;
	
	public TargetMemorySegment(int startAddress, int[] data) {
		this(null, startAddress, data);
	}
	
	public TargetMemorySegment(Segment segment, int startAddress, int[] data) {
		this.id = tmsCounter++;
		this.segment = segment;
		this.startAddress = startAddress;
		this.data = data;
	}
	
	public TargetMemorySegment(int startAddress, int[] data, int length) {
		this(null, startAddress, data, length);
	}
	
	public TargetMemorySegment(Segment segment, int startAddress, int[] data, int length) {
		this.id = tmsCounter++;
		this.segment = segment;
		this.startAddress = startAddress;
		this.data = new int[length];
		for(int i = 0; i < length; i++) {
			this.data[i] = data[i];
		}
	}
	
	public TargetMemorySegment(int startAddress, BlockItem item) {
		this(null, startAddress, item);
	}
	
	public TargetMemorySegment(Segment segment, int startAddress, BlockItem item) {
		this.id = tmsCounter++;
		this.segment = segment;
		this.startAddress = startAddress;
		this.data = new int[item.getBlockSize() / 4];
		int offset = 0;
		while(item != null) {
			item.insertIntoArray(data, offset);
			offset += item.getItemSize();
			item = item.next;
		}
	}

	public void addData(int addr, int[] d, int length) {
		if(d != null && 
				d.length > 0 && 
				length > 0 && 
				length <= d.length && 
				addr >= this.startAddress && 
				addr + length * 4 <= this.startAddress + this.data.length * 4) {
			
			for(int i = 0; i < length; i++) {
				//if(addr == 0x0 || addr == 0x800000)StdStreams.vrb.println("  >>> Writing 0x" + Integer.toHexString(d[i]) + " to 0x" + Integer.toHexString(addr + i * 4) + " (" + ((addr - this.startAddress) / 4 + i) + ")");
				this.data[(addr - this.startAddress) / 4 + i] = d[i];
			}
		}
		else { // TODO @Martin: remove all debung outputs!
			StdStreams.vrb.println("          ++++++++++ ERROR ++++++++++");
			StdStreams.vrb.println("            > Cound not add the data to the target memory segment.");
			StdStreams.vrb.print("            > The error was: ");
			if(d == null) StdStreams.vrb.println("the given data array (d) was null!");
			else if(d.length <= 0 || length <= 0) StdStreams.vrb.println("the array length or the given length was zero!");
			else if(addr < this.startAddress || addr + length * 4 > this.startAddress + this.data.length * 4) StdStreams.vrb.println("out of range!");
			else if(length > d.length) StdStreams.vrb.println("length > d.length");
			else StdStreams.vrb.println("<unknown>");
			StdStreams.vrb.println("              Start address of tms: 0x" + Integer.toHexString(this.startAddress));
			StdStreams.vrb.println("              End address of tms: 0x" + Integer.toHexString((this.startAddress + this.data.length * 4)));
			StdStreams.vrb.println("              Address for inserting data: 0x" + Integer.toHexString(addr));
			StdStreams.vrb.println("              Size of the data to insert: " + d.length * 4 + " byte (0x" + Integer.toHexString(d.length * 4) + " byte)");
			
		}
	}
	
	public void addData(int addr, int[] d) {
		addData(addr, d, d.length);
	}
	
	public void addData(int addr, BlockItem item) {
		int offset = 0;
		while(item != null) {
		//	System.out.println(">>>>>> Inserting BlockItem: " + item.name);
		//	if(addr + offset < 0x60 | addr + offset == 0x800000) StdStreams.vrb.println("++++++ Written to " + addr + "++++++");
			item.insertIntoArray(data, addr - this.startAddress + offset);
			offset += item.getItemSize();
			item = item.next;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Target Memory Segment #" + this.id + ":\n  Base Address: 0x" + Integer.toHexString(startAddress) + "\n  Size: " + data.length * 4 + " byte\n  Content:\n");
		for(int i = 0; i < data.length; i++) {
			sb.append("    0x" + Integer.toHexString((startAddress + i * 4)) + " [" + Integer.toHexString(data[i]) + "]\n");
		}
		return sb.toString();
	}
}
