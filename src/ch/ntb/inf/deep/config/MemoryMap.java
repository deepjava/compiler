package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

/**
 * Designed as a singleton
 * 
 * @author millischer
 * 
 */
public class MemoryMap implements IAttributes, ErrorCodes {
	private static MemoryMap memoryMap = null;

	private Device dev;
	private Module modulesMap;

	private MemoryMap() {
	}

	public static MemoryMap getInstance() {
		if (memoryMap == null) {
			memoryMap = new MemoryMap();
		}
		return memoryMap;
	}

	public void addDevice(Device dev) {
		if (this.dev == null) {
			this.dev = dev;
			return;
		}
		Device current = this.dev;
		while (current.next != null) {
			current = current.next;
		}
		current.next = dev;
	}

	public void addSegment(Segment seg) {
		if (this.dev == null) {
			ErrorReporter.reporter.error(errNoDevices,
					"Create device befor adding segments");
		}
		int indexOf = seg.name.indexOf('.', 0);
		if (indexOf != -1) {
			HString deviceName = seg.name.substring(0, indexOf);
			seg.name = seg.name.substring(indexOf + 1);
			int devHash = deviceName.hashCode();
			Device current = this.dev;
			while (current != null) {
				if (current.name.hashCode() == devHash) {
					if (current.name.equals(deviceName)) {
						break;
					}
				}
				current = current.next;
			}
			if (current != null) {
				indexOf = seg.name.indexOf('.', 0);
				if (indexOf != -1) {// it is true when the new Segment is a
					// Subsegment
					HString segment = seg.name.substring(0, indexOf);
					Segment parentSeg = current.getSegementByName(segment);
					seg.name = seg.name.substring(indexOf + 1);
					indexOf = seg.name.indexOf('.', 0);
					while (indexOf != -1) {
						segment = seg.name.substring(0, indexOf);
						parentSeg = parentSeg.getSubSegmentByName(segment);
						seg.name = seg.name.substring(indexOf + 1);
						indexOf = seg.name.indexOf('.', 0);
					}
					parentSeg.addSubSegment(seg);
				} else {
					current.addSegment(seg);
				}
			} else {
				ErrorReporter.reporter.error(errNoSuchDevice, "Device "
						+ deviceName.toString() + "for Segment "
						+ seg.getName().toString());
			}
		} else {
			ErrorReporter.reporter.error(errSyntax,
					"Error in memorymap segement definition ("
							+ seg.name.toString()
							+ "), segment names starts with the device name!");
		}
	}

	public void setModule(Module mod) {
		if (modulesMap == null) {
			modulesMap = mod;
		}
		int modHash = mod.name.hashCode();
		Module current = modulesMap;
		Module prev = null;
		while (current != null) {
			if (current.name.hashCode() == modHash) {
				if (current.name.equals(mod.name)) {
					// TODO warn the User
					mod.next = current.next;
					if (prev != null) {
						prev.next = mod;
					} else {
						modulesMap = mod;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = mod;
	}

	public Device getDevices() {
		return dev;
	}

	public Module getModules() {
		return modulesMap;
	}

	public Device getDeviceByName(HString name) {
		int devHash = name.hashCode();
		Device current = dev;
		while (current != null) {
			if (current.name.hashCode() == devHash) {
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
		System.out.println("memorymap {");
		Device current = dev;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}

		if (modulesMap != null) {
			for (int i = indentLevel + 1; i > 0; i--) {
				System.out.print("  ");
			}
			System.out.println("modules {");
			Module mod = modulesMap;
			while (mod != null) {
				mod.println(indentLevel + 2);
			}
			for (int i = indentLevel + 1; i > 0; i--) {
				System.out.print("  ");
			}
			System.out.println("}");
		}

		for (int i = indentLevel; i > 0; i--) {
			System.out.print("  ");
		}
		System.out.println("}");

	}

}
