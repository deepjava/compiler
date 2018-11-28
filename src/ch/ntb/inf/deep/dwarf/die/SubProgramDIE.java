package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.LocalVar;

public class SubProgramDIE extends DebugInformationEntry {

	final String name;
	final int startAddress;
	final int endAddress;
	final byte fileNo;
	final boolean isStatic;
	final byte accessability;
	final TypeDIE returnType;

	public SubProgramDIE(Method method, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_subprogram);
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
		this.endAddress = this.startAddress + method.getCodeSizeInBytes();

		this.fileNo = 1;

		returnType = getType((Type) method.type, parent.getRoot());

		// Method Parameters
		if (method.localVars != null && method.ssa != null) {
			for (LocalVar localVar : method.localVars) {
				while (localVar != null) {
					if (method.ssa.isParam[localVar.index + method.maxStackSlots]) {
						System.out.println("\t\tParameter " + localVar.name);
						new VariableDIE(localVar, this);
					} else {
						System.out.println("\t\tVariable " + localVar.name);
					}
					localVar = (LocalVar) localVar.next;
				}
			}
		}
	}

	@Override
	public void serializeDie(DieSerializer serialize) {
		if (isStatic) {
			serialize.addFlag(DwAtType.DW_AT_external);
		}

		serialize.addByte(DwAtType.DW_AT_accessibility, DwFormType.DW_FORM_data1, accessability);
		serialize.add(DwAtType.DW_AT_name, name);
		serialize.addByte(DwAtType.DW_AT_decl_file, DwFormType.DW_FORM_data1, fileNo);
		serialize.addInt(DwAtType.DW_AT_low_pc, DwFormType.DW_FORM_addr, startAddress);
		serialize.addInt(DwAtType.DW_AT_high_pc, DwFormType.DW_FORM_addr, endAddress);
		serialize.addInt(DwAtType.DW_AT_type, DwFormType.DW_FORM_ref4, returnType.baseAddress - getRoot().baseAddress);
	}
}
