package ch.ntb.inf.deep.dwarf;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
import ch.ntb.inf.deep.dwarf.die.DwTagType;

public class CompilationUnit implements DebugInformationEntry {
	private final File srcFile;
	private final List<LineMatrixEntry> lineNumberTableMatrix;
	private final int startAddress;
	private final int endAddress;
	private final List<SubProgramDIE> subProgramms;

	private static final byte abbrev_code = 1;
	private static final short version = 4;
	private static final byte pointer_size = 4;
	private static final short DW_LANG_JAVA = 0x000b;

	public CompilationUnit(Class clazz) {
		super();
		File file = new File(clazz.name.toString());
		this.srcFile = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());
		startAddress = clazz.methods.address;

		// TODO: Start and End Address doe not completely match with Assembly Code!
		endAddress = clazz.address + clazz.typeDescriptorSize - clazz.typeDescriptorOffset;
		this.lineNumberTableMatrix = new ArrayList<>();
		this.subProgramms = new ArrayList<>();
		Method method = (Method) clazz.methods;
		while (method != null) {
			subProgramms.add(new SubProgramDIE(method));
			method = (Method) method.next;
		}
	}

	public void serialize(ByteBuffer buf, int debugLinePosition) {
		// Header

		int offset = buf.position();
		buf.putInt(0); // Dummy value for length. Update later
		buf.putShort(version);
		buf.putInt(0); // abbrev Offset
		buf.put(pointer_size);
		// End Header
		Utils.writeUnsignedLeb128(buf, abbrev_code);
		buf.put(Utils.serialize("deepjava.org"));
		buf.putShort(DW_LANG_JAVA);
		buf.put(Utils.serialize(srcFile.getPath()));
		String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
		buf.put(Utils.serialize(compileDirecotry));
		buf.putInt(startAddress);
		buf.putInt(endAddress);
		buf.putInt(debugLinePosition);
		
		for( SubProgramDIE subProg: subProgramms) {
			subProg.serialize(buf, 0);
		}
		// Last sibling terminated by a null entry
		Utils.writeUnsignedLeb128(buf, 0);

		// Update Missing Length information
		int length = buf.position() - offset - 4; // Length without Length field itself
		buf.putInt(offset, length);
	}

	@Override
	public void serializeAbbrev(ByteBuffer buf) {
		Utils.writeUnsignedLeb128(buf, abbrev_code); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(buf, DwTagType.DW_TAG_compile_unit.value());
		buf.put((byte) (hasChildern() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		// Attributes first name (ULEB128) second form (ULEB128)
		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_producer.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_language.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data2.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_comp_dir.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_stmt_list.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data4.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(buf, 0);
		Utils.writeUnsignedLeb128(buf, 0);

		
		subProgramms.get(0).serializeAbbrev(buf);	
		
		Utils.writeUnsignedLeb128(buf, 0); // End Symbol with 0
	}

	public void addLineNumberEntry(int srcLineNumber, int machineCodeAddress) {
		lineNumberTableMatrix
				.add(new LineMatrixEntry(srcFile.getName(), srcFile.getParent(), srcLineNumber, 0, machineCodeAddress));
	}

	public void serializeLine(ByteBuffer buf) {
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);
		stateMachine.serialize(buf);
	}

	@Override
	public boolean hasChildern() {
		return true;
	}
}
