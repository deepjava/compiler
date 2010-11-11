package ch.ntb.inf.deep.launcher;

import java.io.IOException;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.MachineCode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.linkerPPC.Linker;
import ch.ntb.inf.deep.ssa.SSA;

public class Launcher {
	
	public static void buildAll(String[] rootClasses, String[] paths) {
		int attributes = (1 << IClassFileConsts.atxCode) | (1 << IClassFileConsts.atxLocalVariableTable) | (1 << IClassFileConsts.atxExceptions);
		attributes |= (1 << IClassFileConsts.atxLineNumberTable); // TODO always necessary?
		
		try {
			// TODO update call to Class.buildSystem
			Class.buildSystem(rootClasses,paths[0], attributes);
			
			Class clazz = Type.classList;
			Method method;
			while(clazz != null) {
				method = (Method)clazz.methods;
				
				// Linker: Calculate offsets
				Linker.calculateOffsets(clazz);
				
				// Linker: Create constant pool
				Linker.createConstantPool(clazz);
				
				// Linker: Create string pool
				Linker.createStringPool(clazz);
				
				while(method  != null) {
					// CFG: create CFG
					method.cfg = new CFG(method);
					
					// SSA: create SSA
					method.ssa = new SSA(method.cfg);
					
					// Code generator: generate machine code
					method.machineCode = new MachineCode(method.ssa);
					
					method = (Method)method.next;
				}
				clazz = (Class)clazz.next;
			}
			
			while(clazz != null) {
				
				// Linker: Calculate absolute addresses
				Linker.calculateAbsoluteAddresses(clazz);
				
				method = (Method)clazz.methods;
				while(method  != null) {
					// Code generator: update machine code
					// TODO call function for updating machine code
				}

				// Linker: Create class descriptor
				Linker.createClassDescriptor(clazz);
			}
			
			Linker.generateTargetImage();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void downloadTargetImage() {
		
	}
	
	public static void saveTargetImage2File(String file) {
		
	}
	
	public static void buildCfgOnly(String[] rootClasses, String[] paths) {
		int attributes = (1 << IClassFileConsts.atxCode) | (1 << IClassFileConsts.atxLocalVariableTable) | (1 << IClassFileConsts.atxExceptions);
		attributes |= (1 << IClassFileConsts.atxLineNumberTable); // TODO always necessary?
		try {
			Class.buildSystem(rootClasses,paths[0], attributes);
			
			Class clazz = Type.classList;
			Method method;
			while(clazz != null) {
				method = (Method)clazz.methods;
				while(method  != null) {
					// CFG: create CFG
					method.cfg = new CFG(method);
				}
				clazz = (Class)clazz.next;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void buildSsaOnly(String[] rootClasses, String[] paths) {
		int attributes = (1 << IClassFileConsts.atxCode) | (1 << IClassFileConsts.atxLocalVariableTable) | (1 << IClassFileConsts.atxExceptions);
		attributes |= (1 << IClassFileConsts.atxLineNumberTable); // TODO always necessary?
		try {
			Class.buildSystem(rootClasses,paths[0], attributes);
			
			Class clazz = Type.classList;
			Method method;
			while(clazz != null) {
				method = (Method)clazz.methods;
				while(method  != null) {
					// CFG: create CFG
					method.cfg = new CFG(method);
					
					// SSA: create SSA
					method.ssa = new SSA(method.cfg);
				}
				clazz = (Class)clazz.next;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
/*	public static void buildLinkerStepOneOnly(String[] rootClasses, String[] paths) {
		int attributes = (1 << IClassFileConsts.atxCode) | (1 << IClassFileConsts.atxLocalVariableTable) | (1 << IClassFileConsts.atxExceptions);
		attributes |= (1 << IClassFileConsts.atxLineNumberTable); // TODO always necessary?
		try {
			Class.buildSystem(rootClasses, paths[0], attributes);
			
			Class clazz = Type.classList;
			while(clazz != null) {
				// Linker: Calculate offsets
				Linker.calculateOffsets(clazz);
				
				// Linker: Create constant pool
				Linker.createConstantPool(clazz);
				
				// Linker: Create string pool
				Linker.createStringPool(clazz);
				
				clazz = (Class)clazz.next;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
}
