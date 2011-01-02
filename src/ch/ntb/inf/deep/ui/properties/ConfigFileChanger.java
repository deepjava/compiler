package ch.ntb.inf.deep.ui.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigFileChanger {
	private BufferedReader configFile;
	private BufferedWriter out;
	private StringBuffer fileContent;
	//private int count;
	
	public ConfigFileChanger(String file) {
		try {
			//count = 0;
			fileContent = new StringBuffer();
			configFile = new BufferedReader(new FileReader(file));
			
			int ch  = configFile.read();
			while(ch  != -1){
				fileContent.append((char)ch);
				ch = configFile.read();
				//count++;
			}
			configFile.close();
			out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void changeContent(String key, String value) throws IOException{
		int indexOfKey = fileContent.indexOf(key);
		if(indexOfKey > -1){
			int indexOfEndtoken = fileContent.indexOf(";", indexOfKey);
			fileContent.replace(indexOfKey, indexOfEndtoken, key + " = " + value);
		}else{
			throw new IOException();
		}
	}
	
	public void save(){
		try {
			out.write(fileContent.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
