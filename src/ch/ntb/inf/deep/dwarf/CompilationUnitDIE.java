package ch.ntb.inf.deep.dwarf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class CompilationUnitDIE extends DebugInformationEntry {
	final File srcFile;
	final List<LineMatrixEntry> lineNumberTableMatrix;
	final int startAddress;
	int endAddress;
	final Map<String, BaseTypeDIE> knownTypes;

	public CompilationUnitDIE(Class clazz) {
		super(null);
		System.out.println("Class: " + clazz.name);
		File file = new File(clazz.name.toString());
		this.srcFile = new File(file.getParent() + "\\" + clazz.getSrcFileName().toString());
		this.startAddress = clazz.codeBase.getValue();

		this.lineNumberTableMatrix = new ArrayList<>();

		this.knownTypes = new HashMap<>();

		Field field = (Field) clazz.instFields;
		while (field != null) {
			new VariableDIE(field, this);
			field = (Field) field.next;
		}

		Method method = (Method) clazz.methods;
		while (method != null) {
			if (method.address != -1) {
				SubProgramDIE die = new SubProgramDIE(method, this);
				this.endAddress = die.endAddress;
			}
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
