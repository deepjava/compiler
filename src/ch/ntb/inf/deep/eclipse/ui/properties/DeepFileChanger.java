package ch.ntb.inf.deep.eclipse.ui.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DeepFileChanger {
	StringBuffer fileContent;
	String deepFile;
	
	public DeepFileChanger(String name) {
		deepFile = name;
		try {
			fileContent = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(deepFile));
			
			int ch  = reader.read();
			while(ch  != -1) {
				fileContent.append((char)ch);
				ch = reader.read();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getContent(String key) {
		int start = 0;
		int indexOfKey = fileContent.indexOf(key, start);
		while (indexOfKey > -1) {
			int indexOfComment = fileContent.lastIndexOf("#", indexOfKey);
			int indexOfNewLine = fileContent.lastIndexOf("\n", indexOfKey);
			if (indexOfComment < indexOfNewLine) {
				int indexOfStartToken = fileContent.indexOf("=", indexOfKey);
				int indexOfEndToken = fileContent.indexOf(";", indexOfKey);
				if (indexOfStartToken < 0 || indexOfEndToken < 0) return "not available";
				String str = fileContent.substring(indexOfStartToken+1, indexOfEndToken);
				return str.trim();	
			} else { // its a comment
				start = indexOfKey + 1;
				indexOfKey = fileContent.indexOf(key, start);
			}
		}
		return "not available";
	}

	public void changeContent(String key, String value) {
		int start = 0;
		int indexOfKey = fileContent.indexOf(key, start);
		while (indexOfKey > -1) {
			int indexOfComment = fileContent.lastIndexOf("#", indexOfKey);
			int indexOfNewLine = fileContent.lastIndexOf("\n", indexOfKey);
			int indexOfEndToken = fileContent.indexOf(";", indexOfKey);
			if (indexOfComment < indexOfNewLine) {
				fileContent.replace(indexOfKey, indexOfEndToken, key + " = " + value);
				return;
			} else { // its a comment
				start = indexOfKey + 1;
				indexOfKey = fileContent.indexOf(key, start);
			}
		}
	}
	
	public void changeProjectName(String newName) {
		int indexOfBracket = fileContent.indexOf("{", fileContent.indexOf("{", 0) + 1);
		int indexOfNewLine = (fileContent.substring(0, indexOfBracket)).lastIndexOf("\n", indexOfBracket);
		fileContent.replace(indexOfNewLine + 1, indexOfBracket, "project " + newName + " ");
	}
	
	public void changeLibPath(String newPath) {
		String key = "<classpathentry kind=\"lib\" path=\"";
		int indexOfKey = fileContent.indexOf(key);
		if (indexOfKey > -1){
			int indexOfEndtoken = fileContent.indexOf("/>", indexOfKey);
			File srcFolder = new File(newPath + "/src");
			if (srcFolder.exists()){
				fileContent.replace(indexOfKey, indexOfEndtoken, key + newPath + "/bin\" sourcepath=\"" + newPath + "/src\"");
			} else {				
				fileContent.replace(indexOfKey, indexOfEndtoken, key + newPath + "/bin\"");
			}
		}
	}

	public void save() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(deepFile));
			out.write(fileContent.toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

