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

package ch.ntb.inf.deep.comp.hosttest.testClasses;

import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.A;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.B1;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.B2;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.C1;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.D1;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.I1;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.I2;
import ch.ntb.inf.deep.comp.hosttest.testClasses.interfaceTest.Z;

public class T11Interfaces {
	static A a = new A();
	static B1 b1 = new B1();
	static I2 b2 = new B2();
	static I2 c1 = new C1();
	static D1 d1 = new D1();
	static I1 z = new Z();
}
