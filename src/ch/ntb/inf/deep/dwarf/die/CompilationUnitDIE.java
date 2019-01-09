package ch.ntb.inf.deep.dwarf.die;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

//	private final File srcFile;
	private final String projectFilePath;
	private final List<LineMatrixEntry> lineNumberTableMatrix;
	private final HashMap<Class, ClassTypeDIE> classes;
	private final String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
	private final int low_pc = 0;
	private final int high_pc = 0xFFFF;

	public CompilationUnitDIE() {
		super(null, DwTagType.DW_TAG_compile_unit);
		this.lineNumberTableMatrix = new ArrayList<>();
		this.classes = new HashMap<>();
		projectFilePath = Configuration.getActiveProject().getProjectFileName().toString();

		ClassIterator classIterator = new ClassIterator();
		// First Iteration for Types which are used in Second Iteration
		while (classIterator.hasNext()) {
			Class clazz = classIterator.next();
			classes.put(clazz, new ClassTypeDIE(clazz, this));
		}

		for (Class clazz: classes.keySet()) {
			classes.get(clazz).InsertMembers(clazz);
			classes.get(clazz).InsertMethods(clazz);

			File file = new File(clazz.name.toString());
			file = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());

			// this.low_pc = Math.min(this.low_pc, c.codeBase.getValue());

			Method method = (Method) clazz.methods;
			while (method != null) {
				if (method.ssa != null) {
					for (LineNrSSAInstrPair line : method.ssa.getLineNrTable()) {
						int address = method.address + line.ssaInstr.machineCodeOffset * 4;
						addLineNumberEntry(file, line.lineNr, address);
					}
				}
//				this.high_pc = Math.max(this.high_pc, method.address + method.getCodeSizeInBytes());
				method = (Method) method.next;
			}
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
		dwarf.add(DwAtType.DW_AT_comp_dir, compileDirecotry);
		dwarf.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		dwarf.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		dwarf.addInt(DwAtType.DW_AT_stmt_list, DwFormType.DW_FORM_data4, dwarf.debug_line.position());

		// Serialize Line
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);
		stateMachine.serialize(dwarf.debug_line);
	}

	private class ClassIterator implements Iterator<Class> {

		private Class actual;

		public ClassIterator() {
			actual = Class.initClasses;
		}

		@Override
		public boolean hasNext() {
			return actual != null;
		}

		@Override
		public Class next() {
			Class result = actual;
			if (actual == Class.initClassesTail) {
				actual = Class.nonInitClasses;
			} else {
				actual = actual.nextClass;
			}
			return result;
		}
	}
}
