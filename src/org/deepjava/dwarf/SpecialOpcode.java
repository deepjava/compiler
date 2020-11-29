package org.deepjava.dwarf;

import java.nio.ByteBuffer;

public class SpecialOpcode extends Opcode {

	public SpecialOpcode(ByteBuffer buffer) {
		super(buffer);
	}

	public SpecialOpcode(short opcode) {
		super(opcode);
	}

	@Override
	public void Parse(ByteBuffer buffer) {
		// 0 arguments
	}

	@Override
	public void execute(DebugLineStateMachine state) {
		int adjustedOpcode = (opcode - state.opcode_base);
		int addressIncrement = state.minimum_instruction_length * (adjustedOpcode / state.line_range);
		int lineIncrement = state.line_base + (adjustedOpcode % state.line_range);
		state.address += addressIncrement;
		state.line += lineIncrement;
		state.appendRowToMatrix();
		state.basic_block = false;
	}

	@Override
	public void serialize(ByteBuffer buffer) {
		buffer.put((byte) opcode);
	}
}