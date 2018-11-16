package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;

public class SubProgramDIE extends DebugInformationEntry {

	final String name;
	final int startAddress;
	final int endAddress;
	final byte fileNo;
	final int lineNo;
	final boolean isStatic;
	final byte accessability;
	final BaseTypeDIE returnType;

	public SubProgramDIE(Method method, CompilationUnitDIE parent) {
		super(parent);
		System.out.println("\tMethod: " + method.name);
		if ((method.accAndPropFlags & (1 << ICclassFileConsts.apfStatic)) != 0
				|| (method.accAndPropFlags & (1 << ICclassFileConsts.dpfSysPrimitive)) != 0) {
			this.isStatic = true;
		} else {
			this.isStatic = false;
		}

		if ((method.accAndPropFlags & (1 << ICclassFileConsts.apfPublic)) != 0) {
			this.accessability = 0x01;
		} else if ((method.accAndPropFlags & (1 << ICclassFileConsts.apfPrivate)) != 0) {
			this.accessability = 0x03;
		} else if ((method.accAndPropFlags & (1 << ICclassFileConsts.apfProtected)) != 0) {
			this.accessability = 0x02;
		} else if ((method.accAndPropFlags & (1 << ICclassFileConsts.dpfSysPrimitive)) != 0) {
			this.accessability = 0; // special system primitive
		} else {
			this.accessability = 0x2;
		}

		this.name = method.name.toString();
		this.startAddress = method.address;
		if (this.startAddress == -1) {
			System.out.println(method.name);
		}
		this.endAddress = this.startAddress + method.getCodeSizeInBytes();

		this.fileNo = 1;
		// TODO: Set Method Declaration Line Number!
		if (method.ssa == null) {
			this.lineNo = 0;
		} else {
			this.lineNo = method.ssa.lowestLineNr - 1;
		}

		returnType = parent.getBaseTypeDie((Type) method.type);
		// Method Parameters
		for (int i = 0; i < method.nofParams; i++) {
			new VariableDIE(this, method.localVars[i]);
		}
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}
}
