package ch.ntb.inf.deep.dwarf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class CompilationUnitDIE extends DebugInformationEntry {
	final File srcFile;
	final List<LineMatrixEntry> lineNumberTableMatrix;
	final int startAddress;
	final int endAddress;
	final List<SubProgramDIE> subProgramms;
	final Map<String, BaseTypeDIE> types;

	public CompilationUnitDIE(Class clazz) {
		super(true);
		File file = new File(clazz.name.toString());
		this.srcFile = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());
		this.startAddress = clazz.codeBase.getValue();

		this.lineNumberTableMatrix = new ArrayList<>();

		this.types = new HashMap<>();
		for (Type type : Type.wellKnownTypes) {
			if (type != null) {
				BaseTypeDIE newBaseTypeDIE = new BaseTypeDIE(type);
				types.put(type.name.toString(), newBaseTypeDIE);
			}
		}

		Type type = Class.refTypeList;
		while (type != null) {
			BaseTypeDIE newBaseTypeDIE = new BaseTypeDIE(type);
			types.put(type.name.toString(), newBaseTypeDIE);
			type = (Type) type.next;
		}

		this.subProgramms = new ArrayList<>();
		Method method = (Method) clazz.methods;
		while (method != null) {
			subProgramms.add(new SubProgramDIE(method, types));
			method = (Method) method.next;
		}
		this.endAddress = subProgramms.get(subProgramms.size() - 1).endAddress;
	}

	public void addLineNumberEntry(int srcLineNumber, int machineCodeAddress) {
		lineNumberTableMatrix
				.add(new LineMatrixEntry(srcFile.getName(), srcFile.getParent(), srcLineNumber, 0, machineCodeAddress));
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}
}
