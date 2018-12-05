package ch.ntb.inf.deep.dwarf.die;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.dwarf.DebugLineStateMaschine;
import ch.ntb.inf.deep.dwarf.LineMatrixEntry;
import ch.ntb.inf.deep.dwarf.Utils;
import ch.ntb.inf.deep.ssa.LineNrSSAInstrPair;

public class CompilationUnitDIE extends DebugInformationEntry {

	private static final short version = 4;
	private static final byte pointer_size = 4;
	private static final short DW_LANG_JAVA = 0x000b;

	private final File srcFile;
	private final List<LineMatrixEntry> lineNumberTableMatrix;
	private final String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
	private final int low_pc;
	private int high_pc;

	public CompilationUnitDIE(Class clazz) {
		super(null, DwTagType.DW_TAG_compile_unit);

		File file = new File(clazz.name.toString());
		this.srcFile = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());

		this.lineNumberTableMatrix = new ArrayList<>();

		this.low_pc = clazz.codeBase.getValue();

		new ClassTypeDIE(clazz, this);

		Method method = (Method) clazz.methods;
		while (method != null) {
			if (method.ssa != null) {
				for (LineNrSSAInstrPair line : method.ssa.getLineNrTable()) {
					int address = method.address + line.ssaInstr.machineCodeOffset * 4;
					addLineNumberEntry(line.lineNr, address);
				}
			}
			high_pc = method.address + method.getCodeSizeInBytes();
			method = (Method) method.next;
		}
	}

	public void addLineNumberEntry(int srcLineNumber, int machineCodeAddress) {
		lineNumberTableMatrix
				.add(new LineMatrixEntry(srcFile.getName(), srcFile.getParent(), srcLineNumber, 0, machineCodeAddress));
	}

	@Override
	public void serialize(DWARF dwarf) {
		addHeader(dwarf);
		super.serialize(dwarf);
		baseAddress -= 11;	// Base is before the Header which is 11 Bytes long!
		
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
		dwarf.add(DwAtType.DW_AT_comp_dir, compileDirecotry);
		dwarf.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		dwarf.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		dwarf.addInt(DwAtType.DW_AT_stmt_list, DwFormType.DW_FORM_data4, dwarf.debug_line.position());
		
		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);
		stateMachine.serialize(dwarf.debug_line);
	}
}
