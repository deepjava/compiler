package ch.ntb.inf.deep.comp.hosttest.dwarf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import ch.ntb.inf.deep.dwarf.ExtendedOpcode;
import ch.ntb.inf.deep.dwarf.Opcode;
import ch.ntb.inf.deep.dwarf.OpcodeFactory;
import ch.ntb.inf.deep.dwarf.SpecialOpcode;
import ch.ntb.inf.deep.dwarf.StandardOpcode;
import ch.ntb.inf.deep.dwarf.Utils;

public class LineNumberTableTest {

	@Test()
	public void SpecialOpcodeTest() {
		SpecialOpcode opcode = new SpecialOpcode((short) 55);
		SerializeParseTest(opcode);
	}

	@Test()
	public void ExtendedOpcodeTest() {
		ExtendedOpcode opcodeEndSequence = new ExtendedOpcode(ExtendedOpcode.DW_LNE_end_sequence);
		SerializeParseTest(opcodeEndSequence);

		ExtendedOpcode opcodeSetAddress = new ExtendedOpcode(ExtendedOpcode.DW_LNE_set_address, 0x42848);
		SerializeParseTest(opcodeSetAddress);
	}

	@Test
	public void StandardOpcodeTest() {
		StandardOpcode opcode = new StandardOpcode(StandardOpcode.DW_LNS_advance_line, 45);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_advance_pc, 456);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_const_add_pc); // 0 Arguments
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_copy);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_fixed_advance_pc, 48);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_negate_stmt);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_set_basic_block);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_set_column, 65);
		SerializeParseTest(opcode);
		opcode = new StandardOpcode(StandardOpcode.DW_LNS_set_file, 45);
		SerializeParseTest(opcode);
	}

	private void SerializeParseTest(Opcode opcode) {
		ByteBuffer buf = ByteBuffer.allocate(0xFF);
		opcode.serialize(buf);

		buf.flip();
		Opcode parsedOpcode = OpcodeFactory.getOpcode(buf);
		assertEquals(opcode, parsedOpcode);
	}

	@Test
	public void StringSerializeTest() {
		assertArrayEquals(new byte[] { 0x68, 0x65, 0x6C, 0x6C, 0x6f,0 }, Utils.serialize("hello"));
		assertArrayEquals(new byte[] {0}, Utils.serialize(""));
		
		String[] dut = null;
		assertArrayEquals(new byte[] {0}, Utils.serialize(dut));
		
		String[] dut1 = new String[0];
		assertArrayEquals(new byte[] {0}, Utils.serialize(dut1));
	}
}
