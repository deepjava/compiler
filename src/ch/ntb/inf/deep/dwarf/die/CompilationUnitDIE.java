package ch.ntb.inf.deep.dwarf.die;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.dwarf.DebugLineStateMaschine;
import ch.ntb.inf.deep.dwarf.LineMatrixEntry;
import ch.ntb.inf.deep.dwarf.Utils;
import ch.ntb.inf.deep.ssa.LineNrSSAInstrPair;

public class CompilationUnitDIE extends DebugInformationEntry {

	private static final short version = 4;
	private static final byte pointer_size = 4;
	private static final short DW_LANG_JAVA = 0x000b;

	private final String projectFilePath;
	private final List<LineMatrixEntry> lineNumberTableMatrix;
	private final ClassTypeDIE classTypeDIE;
	private final Class clazz;
	private int low_pc;
	private int high_pc;

	public CompilationUnitDIE(Class clazz) {
		super(null, DwTagType.DW_TAG_compile_unit);
		this.lineNumberTableMatrix = new ArrayList<>();
		projectFilePath = Configuration.getActiveProject().getProjectFileName().toString();

		this.clazz = clazz;
		classTypeDIE = new ClassTypeDIE(clazz, this);
	}

	public void insertChildInformation() {
		classTypeDIE.InsertMembers();
		classTypeDIE.InsertMethods();

		low_pc = classTypeDIE.getLowPc();
		high_pc = classTypeDIE.getHighPc();

		File file = new File(clazz.name.toString());
		file = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());

		Method method = (Method) clazz.methods;
		while (method != null) {
			if (method.ssa != null) {
				for (LineNrSSAInstrPair line : method.ssa.getLineNrTable()) {
					int address = method.address + line.ssaInstr.machineCodeOffset * 4;
					addLineNumberEntry(file, line.lineNr, address);
				}
			}
			method = (Method) method.next;
		}
	}

	public void addLineNumberEntry(File file, int srcLineNumber, int machineCodeAddress) {
		lineNumberTableMatrix.add(new LineMatrixEntry(file.getName(), file.getParent().replace('\\', '/'),
				srcLineNumber, 0, machineCodeAddress));
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
		dwarf.add(DwAtType.DW_AT_name, projectFilePath);
		dwarf.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		dwarf.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		dwarf.addInt(DwAtType.DW_AT_stmt_list, DwFormType.DW_FORM_data4, dwarf.debug_line.position());

		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);
		stateMachine.serialize(dwarf.debug_line);
	}
}
