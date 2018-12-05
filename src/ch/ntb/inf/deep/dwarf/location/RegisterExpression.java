package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;
import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class RegisterExpression implements DwarfExpression {

	private final byte operation;

	public RegisterExpression(int registerNumber) {
		if (registerNumber >= 0 && registerNumber <= 31) {
			this.operation = (byte) (DwOpType.DW_OP_reg0.value() + registerNumber);
		} else {
			throw new IllegalArgumentException("Registers only between 0 to 31 are supported!");
		}
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.put((byte) 1); // Length
		buf.put(operation);
	}
}
