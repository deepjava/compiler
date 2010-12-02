package ch.ntb.inf.deep.host;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ch.ntb.inf.deep.strings.HString;

public class ClassFileAdmin {
	public static final String classFileType = Config.classFileType; //  = ".class";
	public static final String errMsgIllegalParentDir = "illegal parent dir of class files";

	private static File[] parentDirs; // parent directories of class files

	public static void  clear(){
		parentDirs = null;
	}

	/**
	 * Register parent directories of class file libraries.
	 * <br>These parent directories hold disjoint sets of class files for the project being built.
	 * Parent directories may be set only once per system build.
	 * <br>During registering, each given directory is checked for existence. An error is reported if it does not exist or if it is not a directory.
	 * @param parentDirectories   parent directories
	 */
	public static void registerParentDirs(String[] parentDirectories){
		PrintStream log = StdStreams.log;
		log.println("registering parent dirs of class files:");
		if(ClassFileAdmin.parentDirs != null)
			ErrorReporter.reporter.error("changing parent dirs of class files");
		else{
			int nofPaths = parentDirectories.length;
			ClassFileAdmin.parentDirs = new File[nofPaths];
			boolean error = false;
			for(int path = 0; path < nofPaths; path++){
				String parentPath = parentDirectories[path];
				log.print("  registering: "+ parentPath + '\t');
				File parentDir = new File( parentPath );
				if( ! parentDir.exists() || ! parentDir.isDirectory() ){
					log.println(errMsgIllegalParentDir);
					ErrorReporter.reporter.error(errMsgIllegalParentDir);
					error = true;
				}
				ClassFileAdmin.parentDirs[path] = parentDir;
				log.println();
			}
			if(error) clear();
		}
	}

	/**
	 * Returns the file reference of the class file specified by its <code>className</code> or <code>null</code>
	 * if it is not found.
	 * @param className
	 * @return  File reference of the class file or <code>null</code>
	 */
	public static File getClassFile(HString className){
		if( parentDirs == null ) return null;
		File clsFile;
		String clsFileName = className.toString() + classFileType;
		int pd = 0;
		do{
			clsFile = new File(parentDirs[pd], clsFileName);
			++pd;
		}while( !clsFile.isFile() && pd < parentDirs.length);
		if( ! clsFile.isFile() ) clsFile = null;
		return clsFile;
	}

	public static void printParentDirs(){
		PrintStream out = StdStreams.out;
		out.println("registered parent dirs of class files:");
		if( parentDirs != null ){
			int nofPaths = parentDirs.length;
			for(int pd = 0; pd < nofPaths; pd++){
				File parentDir = parentDirs[pd];
				try{
					out.printf("  parent dir [%1$d] = %2$s, name = %3$s", pd, parentDir.getCanonicalPath(), parentDir.getName() );
				}catch(IOException e){
					e.printStackTrace();
				}
				if( ! parentDir.exists() || ! parentDir.isDirectory() ) out.print(", " +errMsgIllegalParentDir);
				out.println();
			}
		}
	}
}
