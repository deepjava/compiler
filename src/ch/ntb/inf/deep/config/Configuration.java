package ch.ntb.inf.deep.config;

public class Configuration {
	private static Project project;
	private static SystemConstants constants;
	private static MemoryMap memoryMap;
	private static RegisterMap registerMap;
	private static SysModules sysModules;
	private static RegInit regInit;
	private static OperatingSystem os;

	/**
	 * Returns the first Segment which contains the code for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param Class
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getCodeSegmentOf(String Class) {

		return null;
	}

	/**
	 * Returns the first Segment which contains the constants for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param Class
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getConstSegmentOf(String Class) {

		return null;
	}

	/**
	 * Returns the first Segment which contains the variables for the given
	 * classname. If no such segment exists the method returns null.
	 * 
	 * @param Class
	 *            the name of the desired Class
	 * @return a Segment or null
	 */
	public static Segment getVarSegmentOf(String Class) {

		return null;
	}

}
