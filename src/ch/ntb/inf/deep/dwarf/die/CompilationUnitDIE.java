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

	final String name;
	final File srcFile;
	final List<LineMatrixEntry> lineNumberTableMatrix;
	final String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
	final int low_pc;
	int high_pc;

	private int offset;

	public CompilationUnitDIE(Class clazz) {
		super(null, DwTagType.DW_TAG_compile_unit);

		this.name = clazz.getSrcFileName().toString();
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
	public void serialize(DieSerializer serializer) {
		// Header
		baseAddress = serializer.debug_info.position();
		offset = serializer.debug_info.position();
		serializer.debug_info.putInt(0); // Dummy value for length. Update later
		serializer.debug_info.putShort(version);
		serializer.debug_info.putInt(serializer.debug_abbrev.position()); // abbrev Offset
		serializer.debug_info.put(pointer_size);

		Utils.writeUnsignedLeb128(serializer.debug_info, abbrevCode);
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, type.value());
		serializer.debug_abbrev.put((byte) (hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		serializeDie(serializer);
	}

	@Override
	protected void serializeDie(DieSerializer serialize) {

		serialize.add(DwAtType.DW_AT_producer, "deepjava.org");
		serialize.addShort(DwAtType.DW_AT_language, DwFormType.DW_FORM_data2, DW_LANG_JAVA);
		serialize.add(DwAtType.DW_AT_name, srcFile.getPath());
		serialize.add(DwAtType.DW_AT_comp_dir, compileDirecotry);
		serialize.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		serialize.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		serialize.addInt(DwAtType.DW_AT_stmt_list, DwFormType.DW_FORM_data4, serialize.debug_line.position());

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, 0);
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, 0);
		// End Abbrev

		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);
		stateMachine.serialize(serialize.debug_line);

		if (hasChildren()) {
			for (DebugInformationEntry child : getChildren()) {
				child.serialize(serialize);
			}
			// Last sibling terminated by a null entry
			Utils.writeUnsignedLeb128(serialize.debug_info, 0);
		}

		// Update Missing Length information
		int length = serialize.debug_info.position() - offset - 4; // Length without Length field itself
		serialize.debug_info.putInt(offset, length);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, 0); // End Symbol with 0
	}
}
