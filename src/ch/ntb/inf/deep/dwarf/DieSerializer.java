package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
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
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_base_type.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_byte_size.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());
		debug_info.put(die.sizeInBytes);

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_encoding.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());
		debug_info.put(die.encoding.value());

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.name));

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
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_compile_unit.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_producer.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize("deepjava.org"));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_language.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data2.value());
		debug_info.putShort(DW_LANG_JAVA);
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.srcFile.getPath()));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_comp_dir.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.compileDirecotry));
				

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());
		debug_info.putInt(die.low_pc);
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());
		debug_info.putInt(die.high_pc);
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_stmt_list.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data4.value());
		debug_info.putInt(debug_line.position());

		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		// End Abbrev

		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(die.lineNumberTableMatrix);
		stateMachine.serialize(debug_line);
		
		if (die.hasChildren()) {
			for(DebugInformationEntry child:  die.getChildren()) {
				child.accept(this);
			}
			// Last sibling terminated by a null entry
			Utils.writeUnsignedLeb128(debug_info, 0);
		}

		// Update Missing Length information
		int length = debug_info.position() - offset - 4; // Length without Length field itself
		debug_info.putInt(offset, length);

		Utils.writeUnsignedLeb128(debug_abbrev, 0); // End Symbol with 0
	}

	@Override
	public void visit(SubProgramDIE die) {
		die.baseAddress = debug_info.position();	
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_subprogram.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes		
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_external.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_flag.value());
		debug_info.put((byte) (die.isStatic ? 0 : 1));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());		
		debug_info.put(die.accessability);

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.name));

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_decl_file.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());
		debug_info.put(die.fileNo);

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());
		debug_info.putInt(die.startAddress);

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_addr.value());
		debug_info.putInt(die.endAddress);

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_ref4.value());
		debug_info.putInt(die.returnType.baseAddress - die.getRoot().baseAddress);

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
		die.baseAddress = debug_info.position();	
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_formal_parameter.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes	

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.name));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_ref4.value());
		debug_info.putInt(die.type.baseAddress - die.getRoot().baseAddress);
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_location.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_exprloc.value());
		die.expression.serialize(debug_info);

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
	}

	@Override
	public void visit(ClassTypeDIE die) {
		die.baseAddress = debug_info.position();	
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_class_type.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.name));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_byte_size.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());
		debug_info.put(die.byteSize);
		
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
	public void visit(MemberDIE die) {
		die.baseAddress = debug_info.position();	
		Utils.writeUnsignedLeb128(debug_info, die.abbrevCode);
		Utils.writeUnsignedLeb128(debug_abbrev, die.abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(debug_abbrev, DwTagType.DW_TAG_member.value());
		debug_abbrev.put((byte) (die.hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes	

		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_string.value());
		debug_info.put(Utils.serialize(die.name));
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_ref4.value());
		debug_info.putInt(die.type.baseAddress - die.getRoot().baseAddress);
		
		if (die instanceof ClassMemberDIE) {
			ClassMemberDIE classMemberDie = (ClassMemberDIE)die;
			
			Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_location.value());
			Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_exprloc.value());
			classMemberDie.location.serialize(debug_info);
			
			Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_external.value());
			Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_flag.value());
			debug_info.put((byte) 1);
		} else {
			InstanceMemberDIE instanceMemberDie = (InstanceMemberDIE)die;
			
			Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_data_member_location.value());
			Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data4.value());
			debug_info.putInt(instanceMemberDie.offset);
			
		}
		
		Utils.writeUnsignedLeb128(debug_abbrev, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(debug_abbrev, DwFormType.DW_FORM_data1.value());		
		debug_info.put(die.accessability);
		
		// Ending of Attribute List
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
		Utils.writeUnsignedLeb128(debug_abbrev, 0);
	}
}
