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

package ch.ntb.inf.deep.comp.targettest;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;
import ch.ntb.inf.deep.comp.targettest.arrays.ArraySuite;
import ch.ntb.inf.deep.comp.targettest.casts.CastSuite;
import ch.ntb.inf.deep.comp.targettest.conditions.ConditionsSuite;
import ch.ntb.inf.deep.comp.targettest.exceptions.ExceptionsSuite;
import ch.ntb.inf.deep.comp.targettest.objects.ObjectsSuite;
import ch.ntb.inf.deep.comp.targettest.primitives.PrimitivesSuite;
import ch.ntb.inf.deep.comp.targettest.statements.StatementSuite;
import ch.ntb.inf.deep.comp.targettest.unsafe.UnsafePPCSuite;
import ch.ntb.inf.deep.comp.targettest.various.VariousSuite;

@Suite({ PrimitivesSuite.class, StatementSuite.class, ArraySuite.class, CastSuite.class, ConditionsSuite.class, ObjectsSuite.class, UnsafePPCSuite.class, VariousSuite.class, ExceptionsSuite.class })    
@MaxErrors(100)

public class CompilerTestSuitePPC {

}
