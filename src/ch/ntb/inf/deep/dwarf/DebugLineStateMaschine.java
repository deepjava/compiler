package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class DebugLineStateMaschine {
	public static final short Version = 2;
	private static final byte[] standard_opcode_lengths = new byte[] { 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 1 };

	public final int minimum_instruction_length;
	public final int opcode_base;
	public final int line_base;
	public final int line_range;
	public final boolean is_stmt_default;
	public long address;
	public int fileIndex;
	public int line;
	public int column;
	public boolean is_stmt;
	public boolean basic_block;
	public boolean end_sequence;
	public final List<LineMatrixEntry> matrix;

	private List<String> directories;
	public final List<SourceFileEntry> files;
	private final List<Opcode> program;

	public DebugLineStateMaschine(ByteBuffer buf, StringJoiner sj) {
		matrix = new ArrayList<>();
		this.program = new ArrayList<Opcode>();

		int offset = buf.position();
		sj.add("Offset: " + offset);
		int length = buf.getInt(); // uword
		sj.add("Length: " + length);
		length += 4; // Add Length for Length itself
		int version = buf.getShort(); // uhalf
		sj.add("Version: " + version);
		if (DebugLineStateMaschine.Version != version) {
			throw new RuntimeException("Debug Line Parser Supports only Version 2 but was Version " + version);
		}
		int prologueLength = buf.getInt(); // uword
		sj.add("Prologue Length: " + prologueLength);
		minimum_instruction_length = Byte.toUnsignedInt(buf.get()); // ubyte
		sj.add("Min Instruction Length: " + minimum_instruction_length);
		is_stmt_default = buf.get() == 0 ? false : true;
		sj.add("Initial Value of 'is_stmt': " + is_stmt_default);
		line_base = buf.get(); // sbyte
		sj.add("Line Base: " + line_base);
		line_range = buf.get(); // ubyte
		sj.add("Line Range: " + line_range);
		opcode_base = buf.get(); // ubyte
		sj.add("Opcode Base: " + opcode_base);
		sj.add("");
		init();

		// standard_opcode_lengths (array of ubyte)
		Byte[] opcodeArguments = new Byte[opcode_base];
		sj.add("Opcodes: ");
		for (int i = 1; i < opcode_base; i++) {
			opcodeArguments[i] = buf.get();
			sj.add(String.format(" Opcode %d has %d args", i, opcodeArguments[i]));
		}
		sj.add("");

		// include_directories (sequence of path names)
		sj.add("Directory Table:");
		directories = Utils.parseStringTable(buf);
		if (directories.size() == 0) {
			sj.add(" Directory Table is empty");
		} else {
			directories.stream().forEach(sj::add);
		}
		sj.add("");

		sj.add("File Name Table:");
		files = new ArrayList<>();
		int i = 1;
		while (buf.get(buf.position()) != 0) {
			SourceFileEntry entry = new SourceFileEntry(i, buf);
			i++;
			files.add(entry);
		}
		buf.get(); // Read Termination 0

		if (files.size() == 0) {
			sj.add("File Table is empty");
		} else {
			sj.add(String.format(" %s\t%s\t%s\t%s\t\t%s", "Entry", "Dir", "Time", "Size", "Name"));
			files.stream().forEach(
					x -> sj.add(String.format(" %d\t\t%d\t%d\t\t%d\t\t%s", x.No, x.Dir, x.Time, x.Size, x.filename)));
		}
		sj.add("");

		while (buf.position() < (offset + length)) {// buf.capacity() - 1) {
			this.program.add(OpcodeFactory.getOpcode(buf));
		}
	}

	public DebugLineStateMaschine(List<LineMatrixEntry> matrix) {
		this.matrix = new ArrayList<>();
		this.program = new ArrayList<Opcode>();
		this.directories = new ArrayList<>();
		this.files = new ArrayList<>();

		this.minimum_instruction_length = 1;
		this.opcode_base = 13;
		this.line_range = 14;
		this.line_base = -5;
		this.is_stmt_default = true;

		init();

		for (LineMatrixEntry line : matrix) {
			if (!directories.contains(line.directoryName)) {
				directories.add(line.directoryName);
			}
			int directoryIndex = directories.indexOf(line.directoryName) + 1;

			if (!files.stream().anyMatch(x -> x.filename.equals(line.filename))) {
				files.add(new SourceFileEntry(files.size(), line.filename, directoryIndex));
			}
			generateNextOpCodes(line);
		}

		// this.matrix and matrix should be the same now!

		if (!matrix.isEmpty()) {
			InsertEndOfSequence();
		}
	}

	public void init() {
		address = 0;
		fileIndex = 1;
		line = 1;
		column = 0;
		this.is_stmt = is_stmt_default;
		basic_block = false;
		end_sequence = false;
	}

	public void appendRowToMatrix() {
		String filename = files.get(fileIndex - 1).filename;
		int directoryIndex = files.get(fileIndex - 1).Dir;
		String directorName = directoryIndex > 0 ? directories.get(directoryIndex - 1) : "";
		matrix.add(new LineMatrixEntry(filename, directorName, line, column, address));
	}

	public void generateNextOpCodes(LineMatrixEntry line) {
		int maxLineIncrement = line_base + line_range - 1;
		int lineIncrement = line.line - this.line;
		int desiredAddressIncrement = (int) (line.address - this.address);
		if (!files.get(fileIndex - 1).filename.equals(line.filename)) {
			InsertEndOfSequence();
			int fileIndex = files.stream().filter(x -> x.filename.equals(line.filename)).findFirst().get().No + 1;
			addOpcodeAndExecute(new StandardOpcode(StandardOpcode.DW_LNS_set_file, fileIndex));
			generateNextOpCodes(line);
		} else if (lineIncrement > maxLineIncrement) {
			addOpcodeAndExecute(new StandardOpcode(StandardOpcode.DW_LNS_advance_line, lineIncrement));
			generateNextOpCodes(line);
		} else if (lineIncrement < line_base) {
			addOpcodeAndExecute(new StandardOpcode(StandardOpcode.DW_LNS_advance_line, lineIncrement));
			generateNextOpCodes(line);
		} else if (desiredAddressIncrement < 0) {
			addOpcodeAndExecute(new ExtendedOpcode(ExtendedOpcode.DW_LNE_set_address, line.address));
			generateNextOpCodes(line);
		} else {
			// Special Op Code;
			int addressAdvance = desiredAddressIncrement / minimum_instruction_length;
			int opcode = (lineIncrement - line_base) + (line_range * addressAdvance) + opcode_base;
			if (opcode > 255) {
				addOpcodeAndExecute(new StandardOpcode(StandardOpcode.DW_LNS_advance_pc, addressAdvance));
				generateNextOpCodes(line);
			} else {
				addOpcodeAndExecute(new SpecialOpcode((short) opcode));
			}
		}
	}

	public void InsertEndOfSequence() {
		addOpcodeAndExecute(new ExtendedOpcode(ExtendedOpcode.DW_LNE_end_sequence));
	}

	private void addOpcodeAndExecute(Opcode opcode) {
		program.add(opcode);
		opcode.execute(this);
	}

	public void serialize(ByteBuffer buf) {
		// Add Header!
		int offset = buf.position();
		int totalLengthPosition = offset;
		buf.putInt(0); // Total Length dummy Value. Update later!
		buf.putShort(Version); // Version
		int headerLengthPosition = buf.position();
		buf.putInt(0); // Header Length dummy Value. Update later!
		buf.put((byte) minimum_instruction_length);
		buf.put((byte) (is_stmt_default ? 1 : 0));
		buf.put((byte) line_base);
		buf.put((byte) line_range);
		buf.put((byte) opcode_base);
		buf.put(standard_opcode_lengths);

		buf.put(Utils.serialize(directories.toArray(new String[] {})));

		// Files!
		for (SourceFileEntry file : files) {
			file.serialize(buf);
		}
		buf.put((byte) 0);

		int headerLength = buf.position() - offset - 4 - 2 - 4; // Header Length does not include TotalLength, Version
																// and HeaderLength

		// Add program
		for (Opcode opcode : program) {
			opcode.serialize(buf);
		}

		int totalLength = buf.position() - offset - 4; // Total Length without Length field itself

		// Update Missing fields
		buf.putInt(totalLengthPosition, totalLength);
		buf.putInt(headerLengthPosition, headerLength);
	}

	public void run() {
		for (Opcode opcode : program) {
			opcode.execute(this);
		}
	}
}
