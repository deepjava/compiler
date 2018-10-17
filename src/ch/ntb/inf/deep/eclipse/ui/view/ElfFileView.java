package ch.ntb.inf.deep.eclipse.ui.view;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.debug.internal.ui.views.memory.renderings.TableRenderingContentDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import nl.lxtreme.binutils.elf.DynamicEntry;
import nl.lxtreme.binutils.elf.Elf;
import nl.lxtreme.binutils.elf.ProgramHeader;
import nl.lxtreme.binutils.elf.SectionHeader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ElfFileView extends ViewPart {
	private final String filePath = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\testfiles\\a.out";
	private TabFolder tabFolder;
	private TabItem headerTab;
	private TabItem ProgHeaderTab;
	private TabItem DynamicTab;
	private TabItem SectionHeaderTab;
	private TabItem DebugTab;

	private Elf elf;

	@Override
	public void createPartControl(Composite parent) {
		tabFolder = new TabFolder(parent, SWT.NULL);
		headerTab = new TabItem(tabFolder, SWT.NULL);
		headerTab.setText("Header");
		ProgHeaderTab = new TabItem(tabFolder, SWT.NULL);
		ProgHeaderTab.setText("Program Header");
		DynamicTab = new TabItem(tabFolder, SWT.NULL);
		DynamicTab.setText("Dynamic Section");
		SectionHeaderTab = new TabItem(tabFolder, SWT.NULL);
		SectionHeaderTab.setText("Section Header");
		DebugTab = new TabItem(tabFolder, SWT.NULL);
		DebugTab.setText("Debug Information");
	}

	@Override
	public void setFocus() {
		try {
			elf = new Elf(new File(filePath));
			loadHeader();
			loadProgramHeader();
			loadDynamicSection();
			loadSectionHeader();
			loadDebugInformation();
		} catch (IOException e) {
			elf = null;
			tabFolder.setSelection(0);
			Label errorText = new Label(tabFolder, SWT.NULL);
			headerTab.setControl(errorText);
			errorText.setForeground(new Color(Display.getCurrent(), 255, 0, 0));
			errorText.setText(e.getClass().toString() + " " + e.getMessage());
		}
	}

	private void loadHeader() {
		Text text = new Text(tabFolder, SWT.MULTI | SWT.READ_ONLY);
		headerTab.setControl(text);
		text.setText(elf.header.toString());
	}

	private void loadProgramHeader() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		ProgHeaderTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Type", "File Size", "Memory Size", "Virtual Address", "Physical Address", "Offset",
				"Align", "Flags" };
		int[] bounds = { 200, 80, 80, 150, 150, 80, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		// Table Content
		for (ProgramHeader progHeader : elf.programHeaders) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, progHeader.type.toString());
			item.setText(1, String.format("0x%08X", progHeader.segmentFileSize));
			item.setText(2, String.format("0x%08X", progHeader.segmentMemorySize));
			item.setText(3, String.format("0x%016X", progHeader.virtualAddress));
			item.setText(4, String.format("0x%016X", progHeader.physicalAddress));
			item.setText(5, String.format("0x%08X", progHeader.offset));

			// Alignment

			item.setText(6, alignmentString(progHeader.segmentAlignment));

			// Flags
			Boolean execute = ((progHeader.flags & 0b001) > 0);
			Boolean write = ((progHeader.flags & 0b010) > 0);
			Boolean read = ((progHeader.flags & 0b100) > 0);
			String flags = (read ? "r" : "-") + (write ? "w" : "-") + (execute ? "x" : "-");
			item.setText(7, flags);
		}

	}

	private void loadDynamicSection() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		DynamicTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Name", "No", "Value" };
		int[] bounds = { 200, 150, 80, 80, 150, 80, 80, 150, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		if (elf.dynamicTable == null) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText("No Dynamic Entry");
			return;
		}
		for (DynamicEntry dynamicEntry : elf.dynamicTable) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, dynamicEntry.getTag().name());
			item.setText(1, String.format("%016X", dynamicEntry.getTag().ordinal()));
			item.setText(2, "" + dynamicEntry.getValue());
		}

	}

	private void loadSectionHeader() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		SectionHeaderTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Name", "Type", "Size", "Entry Size", "Virtual Memory Address", "Offset", "Align", "Flags",
				"Link", "Info" };
		int[] bounds = { 150, 200, 80, 80, 150, 80, 80, 150, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		// Table Content
		for (SectionHeader sectionHeader : elf.sectionHeaders) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, sectionHeader.getName());
			item.setText(1, sectionHeader.type.name());
			item.setText(2, String.format("%08X", sectionHeader.size));
			item.setText(3, String.format("%08X", sectionHeader.entrySize));
			item.setText(4, String.format("%016X", sectionHeader.virtualAddress));
			item.setText(5, String.format("%08X", sectionHeader.fileOffset));
			item.setText(6, alignmentString(sectionHeader.sectionAlignment));
			item.setText(7, sectionHeaderFlagString(sectionHeader.flags));
			item.setText(8, "" + sectionHeader.link);
			item.setText(9, "" + sectionHeader.info);
		}
	}

	private String alignmentString(long alignment) {
		String binaryAlignment = Long.toBinaryString(alignment);
		int potenz = binaryAlignment.length() - binaryAlignment.replace("0", "").length();
		return "2^" + potenz;
	}

	private String sectionHeaderFlagString(long flag) {
		List<String> result = new ArrayList<String>();
		if ((flag & 0x1) > 0)
			result.add("WRITE"); // Writable
		if ((flag & 0x2) > 0)
			result.add("ALLOC"); // Occupies memory during execution
		if ((flag & 0x4) > 0)
			result.add("EXECINSTR"); // Executable
		if ((flag & 0x10) > 0)
			result.add("MERGE"); // Might be merged
		if ((flag & 0x20) > 0)
			result.add("STRINGS"); // Contains nul-terminated strings
		if ((flag & 0x40) > 0)
			result.add("INFO"); // 'sh_info' contains SHT index
		if ((flag & 0x80) > 0)
			result.add("LINK_ORDER"); // Preserve order after combining
		if ((flag & 0x100) > 0)
			result.add("OS_NONCONFORMING"); // Non-standard OS specific handling required
		if ((flag & 0x200) > 0)
			result.add("GROUP"); // Section is member of a group
		if ((flag & 0x400) > 0)
			result.add("TLS"); // Section hold thread-local data
		if ((flag & 0x0ff00000) > 0)
			result.add("MASKOS"); // OS-specific
		if ((flag & 0xf0000000) > 0)
			result.add("MASKPROC"); // Processor-specific
		if ((flag & 0x4000000) > 0)
			result.add("ORDERED"); // Special ordering requirement (Solaris)
		if ((flag & 0x8000000) > 0)
			result.add("EXCLUDE"); // Section is excluded unless referenced or allocated (Solaris)
		return String.join(", ", result);
	}

	private void loadDebugInformation() {
		TabFolder debugTabFolder = new TabFolder(tabFolder, SWT.NULL);
		DebugTab.setControl(debugTabFolder);

		TabItem debugStrTab = new TabItem(debugTabFolder, SWT.NULL);
		debugStrTab.setText(".debug_str");
		Text text = new Text(debugTabFolder, SWT.WRAP | SWT.V_SCROLL);
		debugStrTab.setControl(text);
		try {
			debugStrTab.setControl(text);

			ByteBuffer buf = getSectionByName(".debug_str");
			String[] strings = new String(buf.array(), StandardCharsets.UTF_8).split("\0");
			text.setText(String.join("\n", strings));
		} catch (IOException e) {
			text.setText(e.getMessage());
		}

		TabItem debugLineTab = new TabItem(debugTabFolder, SWT.NULL);
		debugLineTab.setText(".debug_line");
		text = new Text(debugTabFolder, SWT.WRAP | SWT.V_SCROLL);
		debugLineTab.setControl(text);
		StringJoiner sj = new StringJoiner("\n");
		try {
			ByteBuffer buf = getSectionByName(".debug_line");
			while (buf.position() < buf.capacity()) {
				int offset = buf.position();
				sj.add("Offset: " + offset);
				int length = buf.getInt(); // uword
				length += 4; // Add Length for Length itself
				sj.add("Length: " + length);
				int version = buf.getShort(); // uhalf
				sj.add("Version: " + version);
				int prologueLength = buf.getInt(); // uword
				sj.add("Prologue Length: " + prologueLength);
				int MinInstructionLength = buf.get(); // ubyte
				sj.add("Min Instruction Length: " + MinInstructionLength);
				byte InitialValueOfIs_stmt = buf.get(); // ubyte
				sj.add("Initial Value of 'is_stmt': " + InitialValueOfIs_stmt);
				boolean is_stmt_default = InitialValueOfIs_stmt == 0 ? false : true;
				byte LineBase = buf.get(); // sbyte
				sj.add("Line Base: " + LineBase);
				byte LineRange = buf.get(); // ubyte
				sj.add("Line Range: " + LineRange);
				byte opcodeBase = buf.get(); // ubyte
				sj.add("Opcode Base: " + opcodeBase);
				sj.add("");

				// standard_opcode_lengths (array of ubyte)
				Byte[] opcodeArguments = new Byte[opcodeBase];
				sj.add("Opcodes: ");
				for (int i = 1; i < opcodeBase; i++) {
					opcodeArguments[i] = buf.get();
					sj.add(String.format(" Opcode %d has %d args", i, opcodeArguments[i]));
				}
				sj.add("");

				// include_directories (sequence of path names)
				sj.add("Directory Table:");
				List<String> directories = parseStringTable(buf);
				if (directories.size() == 0) {
					sj.add(" Directory Table is empty");
				} else {
					directories.stream().forEach(sj::add);
				}
				sj.add("");

				sj.add("File Name Table:");
				List<SourceFileEntry> files = new ArrayList<>();
				String filename = parseString(buf);
				int i = 1;
				while (filename != null) {
					SourceFileEntry entry = new SourceFileEntry();
					entry.filename = filename;
					entry.No = i;
					entry.Dir = Utils.readUnsignedLeb128(buf);
					entry.Time = Utils.readUnsignedLeb128(buf);
					entry.Size = Utils.readUnsignedLeb128(buf);

					i++;
					files.add(entry);
					filename = parseString(buf);
				}

				if (files.size() == 0) {
					sj.add("File Table is empty");
				} else {
					sj.add(String.format(" %s\t%s\t%s\t%s\t\t%s", "Entry", "Dir", "Time", "Size", "Name"));
					files.stream().forEach(x -> sj
							.add(String.format(" %d\t\t%d\t%d\t\t%d\t\t%s", x.No, x.Dir, x.Time, x.Size, x.filename)));
				}
				sj.add("");

				DebugLineStateMaschine stateMaschine = new DebugLineStateMaschine(is_stmt_default, MinInstructionLength,
						opcodeBase, LineBase, LineRange);
				while (buf.position() < (offset + length)) {// buf.capacity() - 1) {
					Opcode opcode = OpcodeFactory.getOpcode(buf);
					opcode.execute(stateMaschine, buf);
				}

				sj.add(String.format("%s\t\t\t%s\t\t%s\t\t%s", "File", "Line", "Column", "Address"));
				for (LineMatrixEntry entry : stateMaschine.matrix) {
					String name = files.stream().filter(x -> x.No == entry.fileIndex).findFirst().orElse(null).filename;
					sj.add(String.format("%s\t\t%s\t\t\t%s\t\t\t\t0x%X", name , entry.line, entry.column,
							entry.address));
				}
				sj.add("------------------------END---------------------------\n");
			}

		} catch (Exception e) {
			sj.add(e.getMessage());
		}
		text.setText(sj.toString());
	}

	private List<String> parseStringTable(ByteBuffer buf) {
		List<String> strings = new ArrayList<String>();
		String str = parseString(buf);
		while (str != null) {
			strings.add(str);
			str = parseString(buf);
		}
		return strings;
	}

	private String parseString(ByteBuffer buf) {
		byte val = buf.get();
		if (val == 0) {
			return null;
		}

		String str = "";
		while (val != 0) {
			str += (char) val;
			val = buf.get();
		}
		return str;
	}

	private ByteBuffer getSectionByName(String SectionName) throws IOException {
		SectionHeader sectionHeader = Arrays.stream(elf.sectionHeaders).filter(x -> x.getName().equals(SectionName))
				.findFirst().orElse(null);
		if (sectionHeader == null) {
			throw new IOException("Section " + SectionName + " not found");
		}
		return elf.getSection(sectionHeader);
	}
}

class SourceFileEntry {
	public int No;
	public int Dir;
	public int Time;
	public int Size;
	public String filename;
}

class LineMatrixEntry {
	public final int fileIndex;
	public final int line;
	public final int column;
	public final long address;

	public LineMatrixEntry(int fileIndex, int line, int column, long address) {
		this.fileIndex = fileIndex;
		this.line = line;
		this.column = column;
		this.address = address;
	}
}

class DebugLineStateMaschine {
	public final int minimum_instuction_length;
	public final int opcodeBase;
	public final int lineBase;
	public final int lineRange;
	private final boolean is_stmt_default;
	public long address;
	public int file;
	public int line;
	public int column;
	public boolean is_stmt;
	public boolean basic_block;
	public boolean end_sequence;
	public List<LineMatrixEntry> matrix;

	public DebugLineStateMaschine(boolean is_stmt_default, int minimum_insturction_length, int opcodeBase, int lineBase,
			int lineRange) {
		this.minimum_instuction_length = minimum_insturction_length;
		this.opcodeBase = opcodeBase;
		this.lineRange = lineRange;
		this.lineBase = lineBase;
		this.is_stmt_default = is_stmt_default;
		matrix = new ArrayList<>();
		init();
	}

	public void init() {
		address = 0;
		file = 1;
		line = 1;
		column = 0;
		this.is_stmt = is_stmt_default;
		basic_block = false;
		end_sequence = false;
	}

	public void appendRowToMatrix() {
		matrix.add(new LineMatrixEntry(file, line, column, address));
	}
}

abstract class Opcode {
	short opcode;

	Opcode(short opcode) {
		this.opcode = opcode;
	}

	abstract void execute(DebugLineStateMaschine state, ByteBuffer buffer);
}

class OpcodeFactory {
	public static Opcode getOpcode(ByteBuffer buffer) {
		short opcode = (short) (0xFF & buffer.get());
		if (opcode == 0) {
			return new ExtendedOpcode(opcode);
		} else if (opcode > 0 && opcode < 13) {
			return new StandardOpcode(opcode);
		} else {
			return new SpecialOpcode(opcode);
		}
	}
}

class SpecialOpcode extends Opcode {
	SpecialOpcode(short opcode) {
		super(opcode);
	}

	@Override
	public void execute(DebugLineStateMaschine state, ByteBuffer buffer) {
		int adjustedOpcode = (opcode - state.opcodeBase);
		// TODO: not sure if it need to multiply with minimum_instruction_length
		state.address += state.minimum_instuction_length * (adjustedOpcode / state.lineRange);
		state.line += state.lineBase + (adjustedOpcode % state.lineRange);
		state.appendRowToMatrix();
		state.basic_block = false;
	}
}

class StandardOpcode extends Opcode {
	StandardOpcode(short opcode) {
		super(opcode);
	}

	@Override
	public void execute(DebugLineStateMaschine state, ByteBuffer buffer) {
		switch (opcode) {
		case 1: // DW_LNS_copy
			state.appendRowToMatrix();
			state.basic_block = false;
			break;
		case 2: // DW_LNS_advance_pc
			int operand = Utils.readUnsignedLeb128(buffer);
			state.address += state.minimum_instuction_length * operand;
			break;
		case 3: // DW_LNS_advance_line
			state.line += Utils.readUnsignedLeb128(buffer);
			break;
		case 4: // DW_LNS_set_file
			state.file = Utils.readUnsignedLeb128(buffer);
			break;
		case 5: // DW_LNS_set_column
			state.column = Utils.readUnsignedLeb128(buffer);
			break;
		case 6: // DW_LNS_negate_stmt
			state.is_stmt = !state.is_stmt;
			break;
		case 7: // DW_LNS_set_basic_block
			state.basic_block = true;
			break;
		case 8: // DW_LNS_const_add_pc
			throw new RuntimeException(
					"Not clear what this Operation should to. Please refer Document and Add functionality");
		case 9: // DW_LNS_fixed_advance_pc
			operand = 0xFFFF & buffer.getShort(); // uhalf
			state.address += operand;
			throw new RuntimeException(
					"Not clear what this Operation should to. Please refer Document and Add functionality");
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}
}

class ExtendedOpcode extends Opcode {

	ExtendedOpcode(short opcode) {
		super(opcode);
	}

	@Override
	public void execute(DebugLineStateMaschine state, ByteBuffer buffer) {
		byte noOfArguments = buffer.get();
		noOfArguments--;
		byte opcode = buffer.get();

		switch (opcode) {
		case 1: // DW_LNE_end_sequence
			state.end_sequence = true;
			state.appendRowToMatrix();
			state.init();
			break;
		case 2: // DW_LNE_set_address
			if (noOfArguments == 4) {
				state.address = buffer.getInt();
			} else if (noOfArguments == 8) {
				state.address = buffer.getLong();
			} else {
				throw new RuntimeException("Wrong lenght of Arguments! Can not decide if 32 or 64 Bit Address!");
			}
			break;
		case 3: // DW_LNE_define_file
		default:
			throw new RuntimeException("Illegal operation type");
		}
	}
}

class Utils {
	static int readUnsignedLeb128(ByteBuffer in) {
		int result = 0;
		int cur;
		int count = 0;

		do {
			cur = in.get() & 0xff;
			result |= (cur & 0x7f) << (count * 7);
			count++;
		} while (((cur & 0x80) == 0x80) && count < 5);

		if ((cur & 0x80) == 0x80) {
			throw new RuntimeException("invalid LEB128 sequence");
		}

		return result;
	}
}
