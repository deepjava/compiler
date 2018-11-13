package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
import ch.ntb.inf.deep.dwarf.die.DwTagType;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;

public class SubProgramDIE implements DebugInformationEntry {

	private static final int abbrev_code = 2;

	private final String name;
	private final int startAddress;
	private final int endAddress;
	private final byte fileNo;
	private final int lineNo;
	private final boolean isStatic;
	private final byte accessability;

	public SubProgramDIE(Method method) {
		if ((method.accAndPropFlags & (1 << ICclassFileConsts.apfStatic)) != 0
				|| (method.accAndPropFlags & (1 << ICclassFileConsts.dpfSysPrimitive)) != 0) {
			this.isStatic = true;
		} else {
			this.isStatic = false;
		}
		
		if((method.accAndPropFlags & (1 << ICclassFileConsts.apfPublic)) != 0){
			this.accessability = 0x01;
		}else if((method.accAndPropFlags & (1 << ICclassFileConsts.apfPrivate)) != 0){
			this.accessability = 0x03;
		}else if((method.accAndPropFlags & (1 << ICclassFileConsts.apfProtected)) != 0){
			this.accessability = 0x02;
		}else if ((method.accAndPropFlags & (1 << ICclassFileConsts.dpfSysPrimitive)) != 0){
			this.accessability = 0; //special system primitive
		}else{
			this.accessability = 0x2;
		}		
		
		this.name = method.name.toString();
		this.startAddress = method.address;

		// TODO: find end Address
		if (method.next != null) {
			this.endAddress = method.next.address;
		} else {
			this.endAddress = this.startAddress;
		}

		this.fileNo = 1;
		// TODO: Set Method Decleartion Line Number!
		if (method.ssa == null) {
			this.lineNo = 0;
		} else {
			this.lineNo = method.ssa.lowestLineNr - 1;
		}
	}

	@Override
	public boolean hasChildern() {
		return false;
	}

	@Override
	public void serialize(ByteBuffer buf, int debugLinePosition) {
		Utils.writeUnsignedLeb128(buf, abbrev_code);
		buf.put((byte) (isStatic ? 0: 1));
		buf.put(accessability);
		buf.put(Utils.serialize(name));
		buf.put(fileNo);
		buf.putInt(lineNo);
		buf.putInt(startAddress);
		buf.putInt(endAddress);
	}

	@Override
	public void serializeAbbrev(ByteBuffer buf) {
		Utils.writeUnsignedLeb128(buf, abbrev_code); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(buf, DwTagType.DW_TAG_subprogram.value());
		buf.put((byte) (hasChildern() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_external.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_flag.value());
		
		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_decl_file.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_decl_line.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data4.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(buf, 0);
		Utils.writeUnsignedLeb128(buf, 0);
	}

}
