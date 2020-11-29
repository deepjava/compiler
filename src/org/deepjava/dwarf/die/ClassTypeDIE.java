package org.deepjava.dwarf.die;

import org.deepjava.classItems.Class;
import org.deepjava.classItems.ConstField;
import org.deepjava.classItems.Field;
import org.deepjava.classItems.Method;
import org.deepjava.classItems.StdConstant;
import org.deepjava.classItems.StringLiteral;
import org.deepjava.classItems.Type;

public class ClassTypeDIE extends TypeDIE {

	private final String name;
	private byte byteSize;
	private final Class clazz;

	public ClassTypeDIE(Class clazz, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_class_type);
		this.clazz = clazz;
		this.name = clazz.name.toString();
		clazz.dwarfDIE = new RefTypeDIE(clazz, parent, this);

		if (clazz.type != null) {
			// java/lang/Object has no Base Type
			new InheritanceDIE((Type) clazz.type, this);
		}
	}

	public void InsertMembers() {
		System.out.println("Class: " + clazz.name);
		Field field = (Field) clazz.instFields;
		byteSize = 0;
		while (field != null && field != clazz.classFields) {
			// Instance Fields
			System.out.println("\tInstance Field: " + field.name + " offset: " + field.offset);
			new InstanceMemberDIE(field, this);
			// To get Object Size take the offset of the Last Element and ad its Size
			byteSize = (byte) (field.offset + ((Type) field.type).getTypeSize());
			field = (Field) field.next;
		}

		field = (Field) clazz.classFields;
		while (field != null && field != clazz.constFields) {
			// Static Fields
			new ClassMemberDIE(field, this);
			field = (Field) field.next;
		}

		ConstField constant = (ConstField) clazz.constFields;
		while (constant != null) {
			System.out.println("\tConstant Field: " + field.name);
			if (constant.getConstantItem() instanceof StdConstant) {
				new ConstantDIE((StdConstant) constant.getConstantItem(), constant.name, this);
			} else if (constant.getConstantItem() instanceof StringLiteral) {
				new ClassMemberDIE(constant, constant.getConstantItem().address, this);
			}
			constant = (ConstField) constant.next;
		}
	}

	public void InsertMethods() {
		Method method = (Method) clazz.methods;
		while (method != null) {
			if (method.address != -1) {
				new SubProgramDIE(method, this);
			}
			method = (Method) method.next;
		}
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.addByte(DwAtType.DW_AT_byte_size, DwFormType.DW_FORM_data1, byteSize);
	}
	
	public int getLowPc() {
		int low_pc = Integer.MAX_VALUE;
		
		for(DebugInformationEntry die: this.getChildren()) {
			if (die instanceof SubProgramDIE) {
				low_pc = Math.min(low_pc, ((SubProgramDIE)die).getLow_pc());
			}
		}
		return low_pc;
	}
	
	public int getHighPc() {
		int high_pc = Integer.MIN_VALUE;
		
		for(DebugInformationEntry die: this.getChildren()) {
			if (die instanceof SubProgramDIE) {
				high_pc = Math.max(high_pc, ((SubProgramDIE)die).getHigh_pc());
			}
		}
		return high_pc;
	}
}
