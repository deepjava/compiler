package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Configuration {
	private static Project project;
	private static SystemConstants constants;
	private static MemoryMap memoryMap;
	private static RegisterMap registerMap;
	private static SysModules sysModules;
	private static RegInit regInit;
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
	 * Sets the projectblock of this configuration. If a Project is already set it 
	 * will be overwritten.
	 * 
	 * @param project
	 */
	public static void setProject(Project project){
		Configuration.project = project;
		
	}
	
	/**
	 * @return the project which was set or null.
	 */
	public static Project getProject(){
		return Configuration.project;
	}
	
	/**
	 * @return the number of defined stacks.
	 */
	public static int getNumberOfStacks(){
		return 0;
	}
	
	/**
	 * @return the number of defined heaps.
	 */
	public static int getNumberOfHeaps(){
		return 0;
	}
	
	public static Class getReferenceToHeapClass(){
		//finde Heap klasse und cache sie
		return heap;
	}
	
}
