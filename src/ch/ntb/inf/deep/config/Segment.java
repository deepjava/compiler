package ch.ntb.inf.deep.config;

public class Segment implements IAttributes {
	public Segment subSegments;
	private Segment lastSubSegment;
	public Segment next;
	
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int requiredSize = 0;
	int width = 0;
	
	public Segment() {}
	
	public Segment(int baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	public Segment(int baseAddress, int size) {
		this.baseAddress = baseAddress;
		this.size = size;
	}
	
	public Segment(int baseAddress, int size, int attributes) {
		this.baseAddress = baseAddress;
		this.size = size;
		this.attributes = attributes;
	}
	
	public Segment(int baseAddress, int size, int width, int attributes) {
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
	
	public void removeAttributes(int attributes) {
		this.attributes &= ~attributes;
	}
	
	public void setBaseAddress(int baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	public void setSize(int size) {
		this.size = size;
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
	
	public boolean addSubSegment(Segment s) {
		s.attributes |= this.attributes;
		if(s.width == 0) s.width = this.width;
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
}
