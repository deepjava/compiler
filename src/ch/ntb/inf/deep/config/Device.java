package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Device {
	public Segment segments;
	private Segment lastSegment;
	public Device next;
	
	HString name;
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int width = 0;
	
	public Device(HString name, int baseAddress, int size, int width, int attributes) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
	}
	
	public Device(HString name, int baseAddress, int size, int width) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
	}
	
	public void addSegment(Segment s) {
		if(this.segments == null) {
			this.segments = s;
			this.lastSegment = this.segments;
		}
		else {
			this.lastSegment.next = s;
			this.lastSegment = this.segments.next;
		}
	}
	public Segment getSegementByName(HString name){
		int segHash = name.hashCode();
		Segment current = segments;
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
		System.out.println("Device " + name.toString() + "{");
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
		Segment current = segments;
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
