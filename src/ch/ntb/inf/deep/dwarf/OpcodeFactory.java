package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

public class OpcodeFactory {
	public static Opcode getOpcode(ByteBuffer buffer) {
		// look one Position in Front without change buffers Position to decide which Opcode to take!
		short opcodeNumber = (short) (0xFF & buffer.get(buffer.position()));	
		Opcode opcode;
		if (opcodeNumber == 0) {
			opcode = new ExtendedOpcode(buffer);
		} else if (opcodeNumber > 0 && opcodeNumber < 13) {
			opcode = new StandardOpcode(buffer);
		} else {
			opcode = new SpecialOpcode(buffer);
		}
		return opcode;
	}
}