package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.LocalVar;

public class SubProgramDIE extends DebugInformationEntry {

	private final String name;
	private final int low_pc;
	private final int high_pc;
	private final byte fileNo;
	private final boolean isStatic;
	private final byte accessability;
	private final TypeDIE returnType;
	private VariableDIE objectRefcerence;

	public SubProgramDIE(Method method, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_subprogram);
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
		this.low_pc = method.address;
		this.high_pc = this.low_pc + method.getCodeSizeInBytes();

		this.fileNo = 1;

		returnType = getType((Type) method.type, parent.getRoot());

		// Method Parameters and Local Variables
		if (method.ssa != null && method.ssa.getLocalVarsTable() != null) {
			for (LocalVar localVar : method.ssa.getLocalVarsTable()) {
				while (localVar != null) {
					boolean isParameter = method.ssa.isParam[localVar.index + method.maxStackSlots];
					VariableDIE die = new VariableDIE(localVar, method.machineCode.localVarOffset, this, isParameter);
					if (localVar.name.toString().equals("this")) {
						objectRefcerence = die;
					}
					localVar = (LocalVar) localVar.next;
				}
			}
		}
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.addByte(DwAtType.DW_AT_accessibility, DwFormType.DW_FORM_data1, accessability);
		dwarf.addByte(DwAtType.DW_AT_decl_file, DwFormType.DW_FORM_data1, fileNo);
		dwarf.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, low_pc);
		dwarf.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, high_pc);
		
		if (isStatic) {
			dwarf.addFlag(DwAtType.DW_AT_external);
		}
		
		if (this.objectRefcerence != null) {
			dwarf.addReference(DwAtType.DW_AT_object_pointer, objectRefcerence);
		}
		dwarf.add(returnType);

	}

	public int getLow_pc() {
		return low_pc;
	}

	public int getHigh_pc() {
		return high_pc;
	}	
}
