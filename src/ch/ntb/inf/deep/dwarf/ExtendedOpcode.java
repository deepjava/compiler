package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ExtendedOpcode extends Opcode {

	public static final byte DW_LNE_end_sequence = 1;
	public static final byte DW_LNE_set_address = 2;
	public static final byte DW_LNE_define_file = 3;

	private byte internalOpcode;
	private long argument;

	public ExtendedOpcode(ByteBuffer buffer) {
		super(buffer);
	}
	
	public ExtendedOpcode(byte internalOpcode) {
		super((short)0);
		this.internalOpcode = internalOpcode;
	}
	
	public ExtendedOpcode(byte internalOpcode, long argument) {
		super((short)0);
		this.internalOpcode = internalOpcode;
		this.argument = argument;
	}

	@Override
	public void Parse(ByteBuffer buffer) {
		byte noOfArguments = buffer.get();
		noOfArguments--;
		internalOpcode = buffer.get();

		switch (internalOpcode) {
		case DW_LNE_end_sequence:
			// 0 arguments
			break;
		case DW_LNE_set_address:
			if (noOfArguments == 4) {
				argument = buffer.getInt();
			} else if (noOfArguments == 8) {
				argument = buffer.getLong();
			} else {
				throw new RuntimeException("Wrong lenght of Arguments! Can not decide if 32 or 64 Bit Address!");
			}
			break;
		case DW_LNE_define_file:
			// TODO: add Implementation!
			throw new NotImplementedException();
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}

	@Override
	public void execute(DebugLineStateMaschine state) {
		switch (internalOpcode) {
		case DW_LNE_end_sequence:
			state.end_sequence = true;
			state.appendRowToMatrix();
			state.init();
			break;
		case DW_LNE_set_address:
			state.address = argument;
			break;
		case DW_LNE_define_file:
			// TODO: add Implementation!
			throw new NotImplementedException();
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}

	@Override
	public void serialize(ByteBuffer buffer) {
		buffer.put((byte) opcode);
		byte noOfArguments = 1;
		if (internalOpcode == DW_LNE_set_address) {
			// Writing Support only 32 Bit
			noOfArguments = 5;
		}
		buffer.put(noOfArguments);
		buffer.put(internalOpcode);
		if (internalOpcode == DW_LNE_set_address) {
			buffer.putInt((int)argument);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (argument ^ (argument >>> 32));
		result = prime * result + internalOpcode;
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
		ExtendedOpcode other = (ExtendedOpcode) obj;
		if (argument != other.argument)
			return false;
		if (internalOpcode != other.internalOpcode)
			return false;
		return true;
	}
}