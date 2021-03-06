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

package org.deepjava.comp.targettest;

import org.deepjava.comp.targettest.arrays.ArraySuite;
import org.deepjava.comp.targettest.casts.CastSuite;
import org.deepjava.comp.targettest.conditions.ConditionsSuite;
import org.deepjava.comp.targettest.exceptions.ExceptionsSuite;
import org.deepjava.comp.targettest.objects.ObjectsSuite;
import org.deepjava.comp.targettest.primitives.PrimitivesSuite;
import org.deepjava.comp.targettest.statements.StatementSuite;
import org.deepjava.comp.targettest.unsafe.UnsafePPCSuite;
import org.deepjava.comp.targettest.various.VariousSuite;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ PrimitivesSuite.class, StatementSuite.class, ArraySuite.class, CastSuite.class, ConditionsSuite.class, ObjectsSuite.class, UnsafePPCSuite.class, VariousSuite.class, ExceptionsSuite.class })    
@MaxErrors(100)

public class CompilerTestSuitePPC {

}
