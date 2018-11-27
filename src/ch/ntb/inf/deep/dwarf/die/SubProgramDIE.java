package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.Utils;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;

public class SubProgramDIE extends DebugInformationEntry {

	final String name;
	final int startAddress;
	final int endAddress;
	final byte fileNo;
	final boolean isStatic;
	final byte accessability;
	final BaseTypeDIE returnType;

	public SubProgramDIE(Method method, ClassTypeDIE classTypeDIE) {
		super(classTypeDIE, DwTagType.DW_TAG_subprogram);
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

		returnType = ((CompilationUnitDIE)classTypeDIE.getRoot()).getBaseTypeDie((Type) method.type);
		
		// Method Parameters
//		for (int i = 0; i < method.nofParams; i++) {
//			new VariableDIE(method.localVars[i], this);
//		}
	}

	@Override
	public void serializeDie(DieSerializer serialize) {
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_external.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_flag.value());
		serialize.debug_info.put((byte) (isStatic ? 0 : 1));
		
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data1.value());		
		serialize.debug_info.put(accessability);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_string.value());
		serialize.debug_info.put(Utils.serialize(name));

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_decl_file.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data1.value());
		serialize.debug_info.put(fileNo);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_addr.value());
		serialize.debug_info.putInt(startAddress);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_addr.value());
		serialize.debug_info.putInt(endAddress);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_ref4.value());
		serialize.debug_info.putInt(returnType.baseAddress - getRoot().baseAddress);
	}
}
