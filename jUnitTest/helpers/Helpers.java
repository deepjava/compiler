package helpers;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.ClassFileReader;

public class Helpers {

	/**
	 * Create a default classfile reader, able to expose the internal
	 * representation of a given classfile according to the decoding flag used
	 * to initialize the reader. Answer null if the file named fileName doesn't
	 * represent a valid .class file. The fileName has to be an absolute OS path
	 * to the given .class file.
	 * 
	 * The decoding flags are described in IClassFileReader.
	 * 
	 * @param fileName
	 *            the name of the file to be read
	 * @param decodingFlag
	 *            the flag used to decode the class file reader.
	 * @return a default classfile reader
	 * 
	 * @see IClassFileReader
	 */
	public static IClassFileReader createDefaultClassFileReader(String fileName, int decodingFlag) {
		try {
			return new ClassFileReader(Util.getFileByteContent(new File(fileName)), decodingFlag);
		} catch (ClassFormatException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Gets the complete filename and path for a class object
	 * 
	 * @param clazz
	 *            class object
	 * @return complete filename and path
	 */
	public static String getFilenameFromClass(Class clazz) {
		return (new File("")).getAbsolutePath() + File.separatorChar + "bin" + File.separatorChar + clazz.getName().replace('.', File.separatorChar)
				+ ".class";
	}

}
