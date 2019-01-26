package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

public abstract class Opcode {
	short opcode;

	Opcode(ByteBuffer buffer) {
		this.opcode = (short) (0xFF & buffer.get());	// Read unsiged byte
		this.Parse(buffer);
	}
	
	Opcode(short opcode){
		this.opcode = opcode;
	}

	public abstract void Parse(ByteBuffer buffer);
	public abstract void execute(DebugLineStateMachine state);
	public abstract void serialize(ByteBuffer buffer);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + opcode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Opcode other = (Opcode) obj;
		if (opcode != other.opcode)
			return false;
		return true;
	}
}
