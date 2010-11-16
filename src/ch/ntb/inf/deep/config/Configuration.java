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
	 * Returns the first Segment which matches the attributes and classname.
	 * If no match the methode returns null.
	 * 
	 * @param attributes defined in {@link IAttributes}
	 * @param Class the name of the desired Class 
	 * @return a Segment or null
	 */
	public static Segment getSegmentOf(int attributes, String Class){
		
		return null;
	}

}
