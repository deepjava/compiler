package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.Method;;

public class ClassTypeDIE extends DebugInformationEntry {

	public final String name;
	public final byte byteSize;
	public final int fileNo = 1;

	public ClassTypeDIE(Class clazz, DebugInformationEntry parent) {
		super(parent, true);
		System.out.println("Class: " + clazz.name);
		this.name = clazz.name.toString();
		this.byteSize = (byte)clazz.getTypeSize();
		
		Field field = (Field) clazz.instFields;
		while (field != null) {
			new MemberDIE(field, this);
			field = (Field) field.next;
		}
		
		
		Method method = (Method) clazz.methods;
		while (method != null) {
			if (method.address != -1) {
				new SubProgramDIE(method, this);
			}
			method = (Method) method.next;
		}
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}

}
