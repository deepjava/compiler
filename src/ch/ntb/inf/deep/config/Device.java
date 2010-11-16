package ch.ntb.inf.deep.config;

public class Device {
	public Segment segments;
	private Segment lastSegment;
	public Device next;
	
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int width = 0;
	
	public Device(int baseAddress, int size, int width, int attributes) {
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
	}
	
	public Device(int baseAddress, int size, int width) {
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
	}
	
	public void addSegment(Segment s) {
		s.attributes |= this.attributes;
		if(s.width == 0) s.width = this.width;
		if(this.segments == null) {
			this.segments = s;
			this.lastSegment = this.segments;
		}
		else {
			this.lastSegment.next = s;
			this.lastSegment = this.segments.next;
		}
	}
}
