package ch.ntb.inf.deep.host;

public class Utilities {
	
	public static String getFullClassPath(String relativeClassPath){
		return Config.classFolder + Config.pathSeparator + relativeClassPath + Config.classFileType;
	}
}
