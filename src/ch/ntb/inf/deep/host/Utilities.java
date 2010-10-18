package ch.ntb.inf.deep.host;

public class Utilities {
	static String targetFilesFolder = "";

	public static void setTargetFilesFolder(String targetFilesFolder){
		if(targetFilesFolder == null) targetFilesFolder = "";
		Utilities.targetFilesFolder = targetFilesFolder;
	}

	public static String getFullClassPath(String relativeClassPath){
		return targetFilesFolder + (Config.pathSeparator+ Config.classFolder + Config.pathSeparator) + relativeClassPath + Config.classFileType;
	}
}
