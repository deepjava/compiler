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

package ch.ntb.inf.deep.launcher;

import java.lang.reflect.InvocationTargetException;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Programmer;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.cg.InstructionDecoder;
import ch.ntb.inf.deep.cg.arm.InstructionDecoderARM;
import ch.ntb.inf.deep.cg.ppc.InstructionDecoderPPC;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;

/**
 * Launcher for tests only! Adapt this file to your configuration, but don't
 * commit those changes to the SVN! Please store your project file in the top
 * folder of the deep-Project. You can find an example project
 * "ExampleProject.deep" in this folder which you may use as base for your own
 * test project.
 */
@SuppressWarnings("unused")
public class DecodeInstruction {
	public static void main(String[] args) {
		InstructionDecoder.dec = new InstructionDecoderARM();
		System.out.println(InstructionDecoder.dec.getMnemonic(0x07f000f0));
	}
}
