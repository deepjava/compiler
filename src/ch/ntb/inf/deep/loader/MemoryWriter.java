package ch.ntb.inf.deep.loader;

import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.linker.TargetMemorySegment;

public interface MemoryWriter {
	public void eraseDevice(Device dev);
	public void eraseMarkedSectors(Device dev);
	public int writeSequence(TargetMemorySegment seg);
	public int writeWord(int addr, int data);
	public int writeByte(int addr, byte data);

}
