package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;

public class Device implements ErrorCodes{
	public Segment segments;
	public Segment lastSegment;
	public Device next;
	public Memorysector sector; 
	public Memorysector lastSector; 

	HString name;
	HString memorytype;
	int technology = -1; // 0 = RAM, 1 = FLASH
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int width = 0;

	public Device(HString name, int baseAddress, int size, int width, int attributes, int technology) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
		this.technology = technology;
	}

	public Device(HString name, int baseAddress, int size, int width) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
	}
	public int getbaseAddress(){
		return baseAddress;
	}
	
	public int getSize(){
		return size;
	}

	public void addSegment(Segment s) {
		if (s.width == this.width) {
			if (segments == null) {
				segments = s;
				lastSegment = segments;
			} else {
				lastSegment.next = s;
				s.prev = lastSegment;
				lastSegment = lastSegment.next;
			}
		}else{
			ErrorReporter.reporter.error(errInconsistentattributes, "width form device " +this.name.toString()+ " is not equal with the width from the segment" + s.name.toString() + "\n");
			Parser.incrementErrors();
		}
	}
	public void addSector(Memorysector s) {
		if (sector == null) {
			sector = s;
			lastSector = sector;
		} else {
			if(lastSector.baseAddress < s.baseAddress){
				lastSector.next = s;
				s.prev = lastSector;
				lastSector = lastSector.next;				
			}else{
				Memorysector current = sector;
				while(current.baseAddress < s.baseAddress){
					current = current.next; 
				}
				s.prev = current.prev;
				s.next = current;
				current.prev.next = s;
				current.prev = s;
			}
			
		}
	}

	public Segment getSegementByName(HString name) {
		int segHash = name.hashCode();
		Segment current = segments;
		while (current != null) {
			if (current.name.hashCode() == segHash) {
				if (current.name.equals(name)) {
					return current;
				}
			}
			current = current.next;
		}
		return current;
	}
	
	public HString getName(){
		return name;
	}

	public HString getMemoryType(){
		return memorytype;
	}
	
	public int getTechnology(){
		return technology;
	}
	/**
	 * valid method only till new linker is ready
	 */
	public void markUsedSectors(){
		Segment seg = segments;	
		while(seg != null){
			if(seg.tms != null){
				markUsedSectors(seg.tms);
			}
			if(seg.subSegments != null && seg.subSegments.getUsedSize() == 0){
				seg = seg.subSegments;
			}else if(seg.next != null){
				seg = seg.next;
				while(seg != null && seg.getUsedSize() == 0){
					seg = seg.next;
				}
			}else if (seg.parent != null){
				seg = seg.parent.next;
			}else{
				seg = null;
			}
		}
	}
	
	public void markUsedSectors(TargetMemorySegment tms){
		if(tms == null)return;
		int tmsEnd = tms.startAddress + tms.data.length * 4;
		boolean marked = false; //only for mark time optimization
		if(tms != null){
			Memorysector current = sector;
			while(current != null){
				if((current.baseAddress < tms.startAddress && tms.startAddress < (current.baseAddress + size)) || (tms.startAddress < current.baseAddress && (current.baseAddress + current.size) < tmsEnd) || (current.baseAddress < tmsEnd && tmsEnd <(current.baseAddress + current.size))){
					current.used = true;
					marked = true;
				}else if(marked){
					return;
				}
				current = current.next;
			}	
		}		
	}

	public int nofMarkedSectors(){
		int count = 0;
		Memorysector current = sector;
		while(current != null){
			if(current.used){
				count++;
			}
			current = current.next;
		}
		return count;
	}

	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("device " + name.toString() + "{");
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.print("attributes: 0x" + Integer.toHexString(attributes)
				+ ", width: " + width);
		if (baseAddress != -1) {
			StdStreams.vrb.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if (size > 0) {
			StdStreams.vrb.print(", size: 0x" + Integer.toHexString(size));
		}
		StdStreams.vrb.println(";");
		
		Memorysector cur = sector;
		while (cur != null){
			cur.println(indentLevel + 1);
			cur = cur.next;
		}		
		
		Segment current = segments;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.out.println("}");
	}
}
