package ch.ntb.inf.deep.linker;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.DataItem;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.linkerPPC.Linker;

public class Linker00 extends TestLinker {
	@BeforeClass
	public static void setUp() {
		String workspace = System.getProperty("user.dir");
		String[] rootClasses = new String[] { "ch/ntb/inf/deep/testClasses/T10Constants" };
		int attributes = (1 << IClassFileConsts.atxCode) | (1 << IClassFileConsts.atxLocalVariableTable) | (1 << IClassFileConsts.atxExceptions) | (1 << IClassFileConsts.atxLineNumberTable);
		try {
			Class.buildSystem(rootClasses, workspace, attributes);
			
			Class clazz = Type.classList;
			Method method;
			DataItem field;
			while(clazz != null) {
				// Linker: Calculate offsets
				Linker.calculateOffsets(clazz);
				
				// Linker: Create constant pool
				Linker.createConstantPool(clazz);
				
				// Linker: Create string pool
				Linker.createStringPool(clazz);
				
				// Print for debuging
				System.out.println("Class: " + clazz.name.toString());
				
				System.out.println("  Methods:");
				method = (Method)clazz.methods;
				while(method  != null) {
					System.out.println("    Name:   " + method.name.toString());
					System.out.println("    Offset: 0x" + Integer.toHexString(method.offset) + " (" + method.offset + ")");
					method = (Method)method.next;
				}
				
				System.out.println("  Fields:");
				field = (DataItem)clazz.fields;
				while(field  != null) {
					System.out.println("    Name:   " + field.name.toString());
					System.out.println("    Offset: 0x" + Integer.toHexString(field.offset) + " (" + field.offset + ")");
					System.out.println("    Size: " + ((Type)field.type).sizeInBits);
					field = (DataItem)field.next;
				}
				
				System.out.println("  Constant pool:");
				System.out.println("    Size: " + clazz.targetConstantPoolSize + " bits");
				if(clazz.targetConstantPool != null) {
					for(int i = 0; i < clazz.targetConstantPool.length; i++) {
						System.out.println("    [0x" + Integer.toHexString(i * 32) + "] " + Integer.toHexString((clazz.targetConstantPool[i])));
					}
				}
				
				System.out.println("  String pool:");
				System.out.println("    Size: " + clazz.targetStringPoolSize + " bits");
				if(clazz.targetStringPool != null) {
					for(int i = 0; i < clazz.targetStringPool.length; i++) {
						System.out.println("    [0x" + Integer.toHexString(i * 32) + "] " + Integer.toHexString((clazz.targetStringPool[i])));
					}
				}
				
				System.out.println("  Class descriptor:");
				System.out.println("    Size: " + clazz.targetClassDescriptorSize + " bits");
				if(clazz.targetClassDescriptor != null) {
					for(int i = 0; i < clazz.targetClassDescriptor.length; i++) {
						System.out.println("    [0x" + Integer.toHexString(i * 32) + "] " + Integer.toHexString((clazz.targetClassDescriptor[i])));
					}
				}
				
				clazz = (Class)clazz.next;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(Float.toString(T10Constants.f) + " = " + Integer.toHexString(Float.floatToIntBits(T10Constants.f)));
//		System.out.println(Double.toString(T10Constants.d) + " = " + Long.toHexString(Double.doubleToLongBits(T10Constants.d)));
		
	}
	
	
	@Test
	public void checkNumberOfElements() {
		testNumberOfElements(Type.rootClasses[0], 0, 0, 10, 0, 1);
	}
}
