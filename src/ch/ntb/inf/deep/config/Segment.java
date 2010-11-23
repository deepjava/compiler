package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Segment implements IAttributes {
	public Segment subSegments;
	private Segment lastSubSegment;
	public Segment next;
	
	HString name;
	HString deviceAssignedTo;
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int requiredSize = 0;
	int width = 0;
	
	public Segment(HString name){
		this.name = name;
	}
	
	public Segment(HString name, HString deviceAssignedTo){
		this.name = name;
		this.deviceAssignedTo = deviceAssignedTo;
	}
	
	public Segment(HString name, HString deviceAssignedTo,int baseAddress) {
		this.name = name;
		this.deviceAssignedTo = deviceAssignedTo;
		this.baseAddress = baseAddress;
	}
	
	public Segment(HString name, HString deviceAssignedTo, int baseAddress, int size) {
		this.name = name;
		this.deviceAssignedTo = deviceAssignedTo;
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
	
	public void setDeviceAssignedTo(HString device ){
		deviceAssignedTo = device;
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
	
	public HString getDeviceAssignedTo(){
		return deviceAssignedTo;
	}
	
	public int getAttributes(){
		return attributes;
	}

	
	public boolean addSubSegment(Segment s) {
		if(s.width == this.width) {
			if(this.subSegments == null) {
				this.subSegments = s;
				this.lastSubSegment = this.subSegments;
			}
			else {
				this.lastSubSegment.next = s;
				this.lastSubSegment = this.subSegments.next;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Add Size to requiredSize.
	 * @param size
	 */
	public void addToRequiredSize(int size){
		requiredSize += size;
	}
	
	
	/**
	 * @return the required size of the Segement.
	 */
	public int getRequiredSize(){
		return requiredSize;
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
			System.out.print("  ");
		}
		System.out.println("Segment " + name.toString() + "{");
		for(int i = indentLevel + 1; i > 0; i--){
			System.out.print("  ");
		}
		System.out.print("attributes: 0x" + Integer.toHexString(attributes) + ", width: " + width );
		if(baseAddress != -1){
			System.out.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if(size > 0){
			System.out.print(", size: 0x" + Integer.toHexString(size));
		}
		System.out.println(";");
		Segment current = subSegments;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
