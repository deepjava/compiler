package org.deepjava.dwarf;

import java.nio.ByteBuffer;

public class StandardOpcode extends Opcode {
	public static final short DW_LNS_copy = 1;
	public static final short DW_LNS_advance_pc = 2;
	public static final short DW_LNS_advance_line = 3;
	public static final short DW_LNS_set_file = 4;
	public static final short DW_LNS_set_column = 5;
	public static final short DW_LNS_negate_stmt = 6;
	public static final short DW_LNS_set_basic_block = 7;
	public static final short DW_LNS_const_add_pc = 8;
	public static final short DW_LNS_fixed_advance_pc = 9;

	private int argument;

	public StandardOpcode(ByteBuffer buffer) {
		super(buffer);
	}
	
	public StandardOpcode(short opcode) {
		super(opcode);
		
	}

	public StandardOpcode(short opcode, int argument) {
		super(opcode);
		this.argument = argument;
	}

	@Override
	public void Parse(ByteBuffer buffer) {
		switch (opcode) {
		case DW_LNS_copy:
			// 0 arguments
			break;
		case DW_LNS_advance_pc:
			argument = Utils.readUnsignedLeb128(buffer);
			break;
		case DW_LNS_advance_line:
			argument = Utils.readSignedLeb128(buffer);
			break;
		case DW_LNS_set_file:
			argument = Utils.readUnsignedLeb128(buffer);
			break;
		case DW_LNS_set_column:
			argument = Utils.readUnsignedLeb128(buffer);
			break;
		case DW_LNS_negate_stmt:
		case DW_LNS_set_basic_block:
		case DW_LNS_const_add_pc:
			// 0 arguments
			break;
		case DW_LNS_fixed_advance_pc:
			argument = 0xFFFF & buffer.getShort(); // uhalf
			break;
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}

	@Override
	public void execute(DebugLineStateMachine state) {
		switch (opcode) {
		case DW_LNS_copy:
			state.appendRowToMatrix();
			state.basic_block = false;
			break;
		case DW_LNS_advance_pc:
			state.address += state.minimum_instruction_length * argument;
			break;
		case DW_LNS_advance_line:
			state.line += argument;
			break;
		case DW_LNS_set_file:
			state.fileIndex = argument;
			break;
		case DW_LNS_set_column:
			state.column = argument;
			break;
		case DW_LNS_negate_stmt:
			state.is_stmt = !state.is_stmt;
			break;
		case DW_LNS_set_basic_block:
			state.basic_block = true;
			break;
		case DW_LNS_const_add_pc:
			state.address += 17;
			break;
		case DW_LNS_fixed_advance_pc:
			state.address += argument;
			// TODO: Add functionality for this Operation
			throw new RuntimeException(
					"Not clear what this Operation should to. Please refer Document and Add functionality");
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}

	@Override
	public void serialize(ByteBuffer buffer) {
		buffer.put((byte) opcode);
		switch (opcode) {
		case DW_LNS_copy:
			// 0 arguments
			break;
		case DW_LNS_advance_pc:
			Utils.writeUnsignedLeb128(buffer, argument);
			break;
		case DW_LNS_advance_line:
			Utils.writeSignedLeb128(buffer, argument);
			break;
		case DW_LNS_set_file:
			Utils.writeUnsignedLeb128(buffer, argument);
			break;
		case DW_LNS_set_column:
			Utils.writeUnsignedLeb128(buffer, argument);
			break;
		case DW_LNS_negate_stmt:
		case DW_LNS_set_basic_block:
		case DW_LNS_const_add_pc:
			// 0 arguments
			break;
		case DW_LNS_fixed_advance_pc:
			buffer.putShort((short) argument);
			break;
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + argument;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardOpcode other = (StandardOpcode) obj;
		if (argument != other.argument)
			return false;
		return true;
	}
}
