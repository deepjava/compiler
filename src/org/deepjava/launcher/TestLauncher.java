/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.launcher;

import java.lang.reflect.InvocationTargetException;

import org.deepjava.cg.arm.InstructionDecoderARM;
import org.deepjava.classItems.Class;
import org.deepjava.classItems.Method;
import org.deepjava.config.Configuration;
import org.deepjava.config.Programmer;
import org.deepjava.host.ErrorReporter;
import org.deepjava.linker.Linker32;
import org.deepjava.strings.HString;
import org.deepjava.target.TargetConnection;

/**
 * Launcher for tests only! Adapt this file to your configuration, but don't
 * commit those changes to the SVN! Please store your project file in the top
 * folder of the deep-Project. You can find an example project
 * "ExampleProject.deep" in this folder which you may use as base for your own
 * test project.
 */
@SuppressWarnings("unused")
public class TestLauncher {
	public static void main(String[] args) {
//		Launcher.buildAll("555ExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("555ExampleProject.deep", "BootFromFlash", false);
		Launcher.buildAll("555junitTarget.deep", "BootFromRam", false);
//		Launcher.buildAll("5200tinyExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("5200ioExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("5200junitTarget.deep", "BootFromRam", false);
//		Launcher.buildAll("iMX6ExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("zyboExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("microzedExampleProject.deep", "BootFromRam", false);
//		Launcher.buildAll("microzedjunitTarget.deep", "BootFromRam", false);
		if (ErrorReporter.reporter.nofErrors == 0) {
			Programmer programmer = Configuration.getProgrammer();
			if (programmer != null) {
				java.lang.Class<?> cls;
				try {
					cls = java.lang.Class.forName(programmer.getClassName().toString());
					java.lang.reflect.Method m;
					m = cls.getDeclaredMethod("getInstance");
					TargetConnection tc = (TargetConnection) m.invoke(cls);
					Launcher.setTargetConnection(tc);
					Launcher.openTargetConnection();
					long time = System.currentTimeMillis();
					while (System.currentTimeMillis() - time < 100);
					Launcher.downloadTargetImage();
					Launcher.startTarget(Launcher.getResetAddr());
//					Launcher.closeTargetConnection();
				} catch (ClassNotFoundException e) {
					ErrorReporter.reporter.error(811, programmer.getClassName().toString());
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} else
				System.out.println("no programmer defined");
		}

//		Launcher.createInterfaceFiles("M:/EUser/JCC/org.deepjava.trglib");

		/* DEBUG OUTPRINTS */
//		System.out.println("%%%%%%%%%%%%%%% Class List %%%%%%%%%%%%%%%"); Linker32.printClassList(false, false, false, true);
//		System.out.println("%%%%%%%%%%%%%%% System Table %%%%%%%%%%%%%%%"); Linker32.printSystemTable();
//		System.out.println("%%%%%%%%%%%%%%% Global Constants %%%%%%%%%%%%%%%"); Linker32.printGlobalConstantTable();		
//		System.out.println("%%%%%%%%%%%%%%% Target Image (Full image) %%%%%%%%%%%%%%%"); Linker32.printTargetImage();
//		System.out.println("%%%%%%%%%%%%%%% Target Image (Segment List) %%%%%%%%%%%%%%%"); Linker32.printTargetImageSegmentList();	
//		System.out.println("%%%%%%%%%%%%%%% Project Configuration %%%%%%%%%%%%%%%"); Configuration.print();
//		System.out.println("%%%%%%%%%%%%%%% Compiler specific subroutines %%%%%%%%%%%%%%%"); Method.printCompSpecificSubroutines();
	}
}
