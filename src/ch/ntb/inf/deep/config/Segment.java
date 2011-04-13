package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;

public class Segment implements IAttributes {
	public Segment subSegments;
	public Segment parent;
	public Segment lastSubSegment;
	public Segment next;
	public Segment prev;
	public TargetMemorySegment tms;
	
	HString name;
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int usedSize = 0;
	int width = 0;
	
	public Segment(HString name){
		this.name = name;
	}
	
	public Segment(HString name, int baseAddress) {
		this.name = name;
		this.baseAddress = baseAddress;
	}
	
	public Segment(HString name, int baseAddress, int size) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
	}
	
	public Segment(HString name, int baseAddress, int size, int width) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
	}
	
	public Segment(HString name, int baseAddress, int size, int width, int attributes) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
	}
		
	public void setAttribute(int attributes) {
		this.attributes = attributes;
	}
	
	public void addAttributes(int attributes) {
		this.attributes |= attributes;
	}
	
	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}
	
	public void removeAttributes(int attributes) {
		this.attributes &= ~attributes;
	}
	
	public void setBaseAddress(int baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setEndAddress(int endAddress) {
		if(this.baseAddress >= 0) {
			this.size = endAddress - this.baseAddress;
		}
	}
	
	public int getBaseAddress() {
		return this.baseAddress;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public int getWidth() {
		return width;
	}
	
	public HString getName(){
		return name;
	}
	
	public int getAttributes(){
		return attributes;
	}

	
	public boolean addSubSegment(Segment s) {
		s.parent = this;
		if(s.width == this.width) {
			if(subSegments == null) {
				subSegments = s;
				lastSubSegment = subSegments;
			}
			else {
				lastSubSegment.next = s;
				s.prev = lastSubSegment;
				lastSubSegment = lastSubSegment.next;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Add Size to usedSize.
	 * @param size
	 */
	public void addToUsedSize(int size){
		usedSize += size;
	}
	
	
	/**
	 * @return the used size of the segment.
	 */
	public int getUsedSize(){
		return usedSize;
	}
	
	public Segment getSubSegmentByName(HString name){
		int segHash = name.hashCode();
		Segment current = subSegments;
		while(current != null){
			if(current.name.hashCode() == segHash){
				if(current.name.equals(name)){
					return current;
				}
			}
			current = current.next;
		}
		return current;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("segment " + name.toString() + "{");
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.print("attributes: 0x" + Integer.toHexString(attributes) + ", width: " + width );
		if(baseAddress != -1){
			StdStreams.vrb.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if(size > 0){
			StdStreams.vrb.print(", size: 0x" + Integer.toHexString(size));
		}
		StdStreams.vrb.println(";");
		Segment current = subSegments;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}
}
