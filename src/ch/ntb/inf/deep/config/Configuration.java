package ch.ntb.inf.deep.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Path;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.strings.HString;

public class Configuration {
	private static Project project;
	private static SystemConstants sysConst = SystemConstants.getInstance();
	private static Consts consts = Consts.getInstance();
	private static MemoryMap memoryMap = MemoryMap.getInstance();
	private static RegisterMap registerMap = RegisterMap.getInstance();
	private static TargetConfiguration targetConfig;
	private static ValueAssignment regInit;
	private static OperatingSystem os;
	private static Class heap;
	private static final int maxNumbersOfHeaps = 4;
	private static final int maxNumbersOfStacks = 4;
	public static Segment[] heaps = new Segment[maxNumbersOfHeaps];
	public static Segment[] stacks = new Segment[maxNumbersOfStacks];

	/**
	 * Returns the first Segment which contains the code for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param clazz
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getCodeSegmentOf(HString clazz) {

		return null;
	}

	/**
	 * Returns the first Segment which contains the constants for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param clazz
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getConstSegmentOf(HString clazz) {

		return null;
	}

	/**
	 * Returns the first Segment which contains the variables for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param clazz
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getVarSegmentOf(HString clazz) {

		return null;
	}

	/**
	 * Sets the projectblock of this configuration. If a Project is already set
	 * it will be overwritten.
	 * 
	 * @param project
	 */
	public static void setProject(Project project) {
		Configuration.project = project;

	}

	/**
	 * @return the project which was set or null.
	 */
	public static Project getProject() {
		return Configuration.project;
	}

	/**
	 * @return the number of defined stacks.
	 */
	public static int getNumberOfStacks() {
		return 0;
	}

	/**
	 * @return the number of defined heaps.
	 */
	public static int getNumberOfHeaps() {
		return 0;
	}

	public static Class getReferenceToHeapClass() {
		// TODO finde Heap klasse und cache sie
		return heap;
	}

	public static void setOperatingSystem(OperatingSystem os) {
		Configuration.os = os;
	}

	public static OperatingSystem getOperatingSystem() {
		return os;
	}

	public static void addTargetConfiguration(TargetConfiguration targetConf) {
		if (Configuration.targetConfig == null) {
			targetConfig = targetConf;
			return;
		}
		TargetConfiguration current = targetConfig;
		TargetConfiguration prev = null;
		int nameHash = targetConf.name.hashCode();
		while (current != null) {
			if (current.name.hashCode() == nameHash) {
				if (current.name.equals(targetConf.name)) {
					targetConf.next = current.next;
					if (prev != null) {
						prev.next = targetConf;
					} else {
						targetConfig = targetConf;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = targetConf;
	}

	public static TargetConfiguration getTargetConfigurations() {
		return targetConfig;
	}

	public static void setRegInit(HString register, int initValue) {
		if (regInit == null) {
			regInit = new ValueAssignment(register, initValue);
		}
		int regHash = register.hashCode();
		ValueAssignment current = regInit;
		while (current != null) {
			if (current.name.hashCode() == regHash) {
				if (current.name.equals(register)) {
					current.setValue(initValue);
					return;
				}
			}
			current = current.next;
		}
		ValueAssignment init = new ValueAssignment(register, initValue);
		init.next = regInit;
		regInit = init;
	}

	public static ValueAssignment getRegInit() {
		return regInit;
	}

	public static ValueAssignment getSysConstants() {
		if (sysConst == null) {
			return null;
		}
		return sysConst.getSysConst();
	}

	public static ValueAssignment getConsts() {
		if (consts == null) {
			return null;
		}
		return consts.getConst();
	}

	public static int getValueFor(HString constName) {
		int res = consts.getConstByName(constName);
		if (res == Integer.MIN_VALUE) {
			res = sysConst.getConstByName(constName);
		}
		return res;
	}

	public static void print() {
		System.out.println("Configuration {");
		if (project != null) {
			project.println(1);
		}
		sysConst.println(1);
		consts.println(1);
		TargetConfiguration tarConf = targetConfig;
		while (tarConf != null) {
			tarConf.print(1);
			tarConf = tarConf.next;
		}
		if (os != null) {
			os.println(1);
		}
		registerMap.println(1);
		if (regInit != null) {
			System.out.println("  regInit{");
			ValueAssignment initReg = regInit;
			while (initReg != null) {
				initReg.print(2);
				initReg = initReg.next;
			}
			System.out.println("  }");
		}
		memoryMap.println(1);
	}
	
	public static void createInterfaceFile(String location, String fileName){
		int indexOf;
		String pack;
		String className;
		try {
			FileWriter fw = new FileWriter(location + "/" + fileName);
			indexOf = fileName.lastIndexOf(Path.SEPARATOR);
			if(indexOf != -1){
				pack = fileName.substring(0, indexOf) + ";";
				className = fileName.substring(indexOf + 1);
			}else{
				pack = "";
				className = fileName;
			}
			pack = pack.replace(Path.SEPARATOR, '.');
			fw.write("package " +pack + "\n\n");
			indexOf = className.indexOf(".");
			fw.write("public interface "+ className.substring(0, indexOf) + "{\n");
			ValueAssignment current =sysConst.getSysConst();
			while(current != null){
				fw.write("\tpublic static final int "+ current.getName().toString() + " = 0x" + Integer.toHexString(current.getValue()) + ";\n");
				current = current.next;
			}
			fw.write("}");
			fw.flush();
			fw.close();		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
