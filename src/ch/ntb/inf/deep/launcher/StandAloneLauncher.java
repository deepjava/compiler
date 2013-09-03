/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.launcher;

import java.util.HashMap;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.linker.Linker32;

public class StandAloneLauncher {
	public static final String PARAM_PROJECT = "project";
	public static final String PARAM_CONFIG = "config";
	public static final String PARAM_MAP = "map";
	protected static HashMap<String, String> params = new HashMap<String, String>();

	/* Default parameters that can be changed by the command line parser later. */
	static {
		params.put(PARAM_CONFIG, "BootFromRam");
		params.put(PARAM_PROJECT, "");
	}

	protected static String getBaseCommand() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		StackTraceElement main = stack[stack.length - 1];

		return main.getClassName();
	}

	protected static void UsagePrint() {
		System.out.println("Usage: java " + getBaseCommand() + " [/config ConfigName] deepfile");
		System.out.println();
		System.out.println("  /config     By default the project is compiled for \"BootFromRam\".");
		System.out.println("              By specifying this switch, you can set a different");
		System.out.println("              configuration to compile for.");
		System.out.println("  /map        Dumps a mapfile");
		System.out.println("  deepfile    Project file you want to compile");
	}

	/**
	 * Fills the parameter table for the compiler invocation with the parameters
	 * specified by the command line.
	 * 
	 * @param args
	 *            Command line arguments to parse.
	 * @return - true in case the command line could be parsed successfully.<br>
	 *         - false if the command line could not be parsed (missing
	 *         arguments or something comparable.)<br>
	 */
	protected static boolean CommandLineParse(String[] args) {
		boolean result = true;

		if (args.length <= 0)
			return false;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String optionName = PARAM_PROJECT;

			if (arg.startsWith("/") || arg.startsWith("-")) {
				if (++i >= args.length) {
					result = false;
					break;
				}

				optionName = arg.substring(1);
				arg = args[i];
			}

			params.put(optionName, arg);
		}

		return result;
	}

	public static void main(String[] args) {
		if (!CommandLineParse(args)) {
			UsagePrint();
			return;
		}

		Launcher.buildAll(params.get(PARAM_PROJECT), params.get(PARAM_CONFIG));
		
		if (params.containsKey(PARAM_MAP)) {
			System.out.println("%%%%%%%%%%%%%%% Class List %%%%%%%%%%%%%%%"); Linker32.printClassList(false, false, false, true);
			System.out.println("%%%%%%%%%%%%%%% System Table %%%%%%%%%%%%%%%"); Linker32.printSystemTable();
			System.out.println("%%%%%%%%%%%%%%% Global Constants %%%%%%%%%%%%%%%"); Linker32.printGlobalConstantTable();		
			System.out.println("%%%%%%%%%%%%%%% Compiler specific subroutines %%%%%%%%%%%%%%%"); Method.printCompSpecificSubroutines();
		}

		long bytesWritten = Launcher.saveTargetImageToFile();
		if (bytesWritten >= 0) {
			System.out.println("Image size: " + bytesWritten + " bytes.");
		}
	}

}
