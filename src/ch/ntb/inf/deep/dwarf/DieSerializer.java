package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
import ch.ntb.inf.deep.dwarf.die.DwOpType;
import ch.ntb.inf.deep.dwarf.die.DwTagType;

public class DieSerializer implements DieVisitor {

	private static final short version = 4;
	private static final byte pointer_size = 4;
	private static final short DW_LANG_JAVA = 0x000b;

	public final ByteBuffer debug_info;
	public final ByteBuffer debug_abbrev;
	public final ByteBuffer debug_line;

	public DieSerializer(ByteOrder byteOrder) {
		debug_info = ByteBuffer.allocate(0xFFFF);
		debug_info.order(byteOrder);
		debug_abbrev = ByteBuffer.allocate(0xFFFF);
		debug_abbrev.order(byteOrder);
		debug_line = ByteBuffer.allocate(0xFFFF);
		debug_line.order(byteOrder);
	}

	@Override
	public void visit(BaseTypeDIE die) {
		die.baseAddress = debug_info.position();
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		debug_info.put((byte) die.sizeInBytes);
		debug_info.put(die.encoding.value());
		debug_info.put(Utils.serialize(die.name));

		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_base_type.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_byte_size.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_encoding.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
	}

	@Override
	public void visit(CompilationUnitDIE die) {
		// Header
		die.baseAddress = debug_info.position();
		int offset = debug_info.position();
		debug_info.putInt(0); // Dummy value for length. Update later
		debug_info.putShort(version);
		debug_info.putInt(debug_abbrev.position()); // abbrev Offset
		debug_info.put(pointer_size);
		// End Header
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		debug_info.put(Utils.serialize("deepjava.org"));
		debug_info.putShort(DW_LANG_JAVA);
		debug_info.put(Utils.serialize(die.srcFile.getPath()));
		String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
		debug_info.put(Utils.serialize(compileDirecotry));
		debug_info.putInt(die.startAddress);
		debug_info.putInt(die.endAddress);
		debug_info.putInt(debug_line.position());

		// Abbrev
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_compile_unit.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		// Attributes first name (ULEB128) second form (ULEB128)
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_producer.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_language.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data2.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_comp_dir.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_stmt_list.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data4.value());

		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		// End Abbrev

		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(die.lineNumberTableMatrix);
		stateMachine.serialize(debug_line);

		for (DebugInformationEntry child : die.getChildren()) {
			child.accept(this);
		}
		// Last sibling terminated by a null entry
		Utils.writeUnsignedLeb128(debug_info, 0);

		// Update Missing Length information
		int length = debug_info.position() - offset - 4; // Length without Length field itself
		debug_info.putInt(offset, length);

		Utils.writeUnsignedLeb128(debug_abbrev, 0); // End Symbol with 0
	}

	@Override
	public void visit(SubProgramDIE die) {
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		debug_info.put((byte) (die.isStatic ? 0 : 1));
		debug_info.put(die.accessability);
		debug_info.put(Utils.serialize(die.name));
		debug_info.put(die.fileNo);
		debug_info.putInt(die.lineNo);
		debug_info.putInt(die.startAddress);
		debug_info.putInt(die.endAddress);
		debug_info.putInt(die.returnType.baseAddress - die.getParent().baseAddress);
//		Utils.writeUnsignedLeb128(debug_info, 1);		// Expr Length
//		debug_info.put(DwOpType.DW_OP_call_frame_cfa.value());
//		Utils.writeUnsignedLeb128(debug_info, 1);		// Expr Length
//		debug_info.put(DwOpType.DW_OP_reg0.value());

		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_subprogram.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_external.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_flag.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_decl_file.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_decl_line.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data4.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_ref4.value());

//		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_frame_base.value());
//		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_exprloc.value());

//		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_location.value());
//		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_exprloc.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		
		if (die.hasChildren()) {
			for(DebugInformationEntry child:  die.getChildren()) {
				child.accept(this);
			}
			// Last sibling terminated by a null entry
			Utils.writeUnsignedLeb128(debug_info, 0);
		}
	}

	@Override
	public void visit(VariableDIE die) {
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		debug_info.put(Utils.serialize(die.name));
		debug_info.putInt(die.type.baseAddress - die.getParent().baseAddress);
		Utils.writeUnsignedLeb128(debug_info, 1);		// Expr Length
		debug_info.put(DwOpType.DW_OP_reg0.value());

		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_formal_parameter.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_ref4.value());
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_location.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_exprloc.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
	}
}
