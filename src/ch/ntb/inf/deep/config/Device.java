package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public class Device implements ErrorCodes{
	public Segment segments;
	private Segment lastSegment;
	public Device next;

	HString name;
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int width = 0;

	public Device(HString name, int baseAddress, int size, int width,
			int attributes) {
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
		if (s.width == this.width) {
			if (segments == null) {
				segments = s;
				lastSegment = segments;
			} else {
				lastSegment.next = s;
				lastSegment = lastSegment.next;
			}
		}else{
			ErrorReporter.reporter.error(errInconsistentattributes, "width form device " +this.name.toString()+ " is not equal with the width from the segment" + s.name.toString() + "\n");
			Parser.incrementErrors();
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

	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("Device " + name.toString() + "{");
		for (int i = indentLevel + 1; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.print("attributes: 0x" + Integer.toHexString(attributes)
				+ ", width: " + width);
		if (baseAddress != -1) {
			System.out.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if (size > 0) {
			System.out.print(", size: 0x" + Integer.toHexString(size));
		}
		System.out.println(";");
		Segment current = segments;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}
		for (int i = indentLevel; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");
	}
}
