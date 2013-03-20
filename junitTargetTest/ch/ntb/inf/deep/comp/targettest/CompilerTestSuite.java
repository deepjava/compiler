package ch.ntb.inf.deep.comp.targettest;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;
import ch.ntb.inf.deep.comp.targettest.arrays.ArraySuite;
import ch.ntb.inf.deep.comp.targettest.casts.CastSuite;
import ch.ntb.inf.deep.comp.targettest.conditions.ConditionsSuite;
import ch.ntb.inf.deep.comp.targettest.objects.ObjectsSuite;
import ch.ntb.inf.deep.comp.targettest.primitives.PrimitivesSuite;
import ch.ntb.inf.deep.comp.targettest.statements.StatementSuite;
import ch.ntb.inf.deep.comp.targettest.unsafe.UnsafeSuite;
import ch.ntb.inf.deep.comp.targettest.various.VariousSuite;

@Suite({ PrimitivesSuite.class, StatementSuite.class, ArraySuite.class, CastSuite.class, ConditionsSuite.class, ObjectsSuite.class, UnsafeSuite.class, VariousSuite.class })//     
@MaxErrors(100)

public class CompilerTestSuite {

}
