package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.Method;

public class ClassTypeDIE extends TypeDIE {

	private final String name;
	private final byte byteSize;

	public ClassTypeDIE(Class clazz, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_class_type);
		System.out.println("Class: " + clazz.name);
		this.name = clazz.name.toString();
		this.byteSize = (byte)clazz.getTypeSize();
		
		clazz.dwarfDIE = new RefTypeDIE(clazz, parent, this); 
				
		Field field = (Field) clazz.instFields;
		while (field != null && field != clazz.classFields) {
			// Instance Fields
			System.out.println("\tInstance Field: " + field.name + " offset: " + field.offset);
			new InstanceMemberDIE(field, this);
			field = (Field) field.next;
		}
		
		field = (Field) clazz.classFields;
		while (field != null && field != clazz.constFields) {
			// Static Fields
			System.out.println("\tStatic Field: " + field.name + " address: " + field.address);
			new ClassMemberDIE(field, this);
			field = (Field) field.next;
		}

		// TODO: Insert Constant Member!
//		ConstField constant  = (ConstField) clazz.constFields;
//		while (constant != null) {
//			// Constant Fields
//			System.out.println("\tConstant Field: " + field.name + " address: " + field.offset);
//			new ConstantDIE(constant, this);
//			constant = (ConstField) field.next;
//		}
		
		
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
}
