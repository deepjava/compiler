package org.deepjava.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandLineParser {
	private Map<String, ArrayList<String>> parsedArguments = new HashMap<>();
	private ArrayList<String> currentParameterList = null;
	
	public void parse(String[] argv) {
		for (String arg : argv) {
			if (isCommandLineOption(arg)) {
				currentParameterList = new ArrayList<>();
				parsedArguments.put(strip(arg), currentParameterList);
			} else {
				currentParameterList.add(arg);
			}
		}
	}
	
	private boolean isCommandLineOption(String arg) {
		return arg.startsWith("--");
	}
	
	private String strip(String arg) {
		return arg.substring(2);
	}
	
	public boolean isPresent(String option) {
		return parsedArguments.containsKey(option);
	}
	
	public ArrayList<String> getParametersFor(String option) {
		return parsedArguments.get(option);
	}
	
	public String getFirstParameterFor(String option) {
		return parsedArguments.get(option).get(0);
	}
	
	public String[] getMissingOptions(String ... requiredOptions) {
		return (String[]) Arrays.stream(requiredOptions).filter(requiredOption -> !parsedArguments.containsKey(requiredOption)).toArray(String[]::new);
	}
	
public boolean checkRequiredOptions(String ... requiredOptions) {
	String[] missingArgs = getMissingOptions(requiredOptions);
	if (missingArgs.length == 0)
		return true;
	
		for (String arg : missingArgs)
			System.err.println("missing required command line argument: --" + arg);
		return false;
}
	
	// Nur zum testen/demo
	public static void main(String[] argv) {
		CommandLineParser p = new CommandLineParser();
		p.parse(argv);
		p.parsedArguments.forEach((k, v) -> {System.out.println(k + ": " + v);});
	}
}
