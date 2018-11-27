package ch.ntb.inf.deep.dwarf.die;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.LineMatrixEntry;
import ch.ntb.inf.deep.ssa.LineNrSSAInstrPair;

public class CompilationUnitDIE extends DebugInformationEntry {
	final String name;
	final File srcFile;
	final List<LineMatrixEntry> lineNumberTableMatrix;
	final String compileDirecotry = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\runtime-EclipseApplication\\test\\src";
	final int low_pc;
	int high_pc;
	final Map<String, BaseTypeDIE> knownTypes;

	public CompilationUnitDIE(Class clazz) {
		super(null);

		this.name = clazz.getSrcFileName().toString();
		File file = new File(clazz.name.toString());
		this.srcFile = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());

		this.lineNumberTableMatrix = new ArrayList<>();

		this.knownTypes = new HashMap<>();
		
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
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}

	public BaseTypeDIE getBaseTypeDie(Type type) {
		if (!knownTypes.containsKey(type.name.toString())) {
			knownTypes.put(type.name.toString(), new BaseTypeDIE(type, this));
		}
		return knownTypes.get(type.name.toString());
	}
}
