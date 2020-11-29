package org.deepjava.dwarf.die;

import java.io.File;

import org.deepjava.classItems.Class;
import org.deepjava.dwarf.DebugLineStateMachine;
import org.deepjava.dwarf.Utils;

public class CompilationUnitDIE extends DebugInformationEntry {

	private static final short version = 4;
	private static final byte pointer_size = 4;
	private static final short DW_LANG_JAVA = 0x000b;

	private final File srcFile;
	private final ClassTypeDIE classTypeDIE;
	private final Class clazz;
	private DebugLineStateMachine debugLineStateMaschine;
	private int low_pc;
	private int high_pc;

	public CompilationUnitDIE(Class clazz) {
		super(null, DwTagType.DW_TAG_compile_unit);
		srcFile = new File(clazz.name.toString() + ".java");

		this.clazz = clazz;
		classTypeDIE = new ClassTypeDIE(clazz, this);
		
	}

	public void insertChildInformation() {
		classTypeDIE.InsertMembers();
		classTypeDIE.InsertMethods();

		low_pc = classTypeDIE.getLowPc();
		high_pc = classTypeDIE.getHighPc();

		debugLineStateMaschine = new DebugLineStateMachine(clazz, srcFile);
	}

	@Override
	public void serialize(DWARF dwarf) {
		addHeader(dwarf);
		super.serialize(dwarf);
		baseAddress -= 11; // Base is before the Header which is 11 Bytes long!

		// Update Missing Length information
		int length = dwarf.debug_info.position() - baseAddress - 4; // Length without Length field itself
		dwarf.debug_info.putInt(baseAddress, length);

		Utils.writeUnsignedLeb128(dwarf.debug_abbrev, 0); // End Symbol with 0
	}

	private void addHeader(DWARF dwarf) {
		dwarf.debug_info.putInt(-1); // Dummy value for length. Update later
		dwarf.debug_info.putShort(version);
		dwarf.debug_info.putInt(dwarf.debug_abbrev.position()); // abbrev Offset
		dwarf.debug_info.put(pointer_size);
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_producer, "deepjava.org");
		dwarf.addShort(DwAtType.DW_AT_language, DwFormType.DW_FORM_data2, DW_LANG_JAVA);
		dwarf.add(DwAtType.DW_AT_name, srcFile.getPath());
		dwarf.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		dwarf.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		dwarf.addInt(DwAtType.DW_AT_stmt_list, DwFormType.DW_FORM_data4, dwarf.debug_line.position());

		// Serialize Line
		debugLineStateMaschine.serialize(dwarf.debug_line);
	}
}
