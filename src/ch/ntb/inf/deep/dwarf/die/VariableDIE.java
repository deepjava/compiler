package ch.ntb.inf.deep.dwarf.die;

import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.LocalVarRange;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.location.BaseAddress;
import ch.ntb.inf.deep.dwarf.location.EndOfListEntry;
import ch.ntb.inf.deep.dwarf.location.LocationEntry;
import ch.ntb.inf.deep.dwarf.location.LocationListEntry;

public class VariableDIE extends DebugInformationEntry {

	private final String name;
	private final TypeDIE type;
	private final List<LocationListEntry> locationList;

	protected VariableDIE(LocalVar localVar, int localVarOffset, SubProgramDIE parent, DwTagType dwarfTagType) {
		super(parent, dwarfTagType);
		this.name = localVar.name.toString();
		this.type = TypeDIE.getType((Type) localVar.type, parent.getRoot());

		this.locationList = new ArrayList<>();
		this.locationList.add(new BaseAddress(parent.startAddress));

		LocalVarRange range = localVar.range;
		while (range != null) {
			if (range.ssaEnd != null) {
				this.locationList.add(new LocationEntry(range, localVarOffset));
			}
			range = range.next;
		}

		this.locationList.add(new EndOfListEntry());
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.add(type);
		dwarf.addInt(DwAtType.DW_AT_location, DwFormType.DW_FORM_sec_offset, dwarf.debug_loc.position());
		locationList.stream().forEach(x -> x.serialize(dwarf.debug_loc));
	}
}
