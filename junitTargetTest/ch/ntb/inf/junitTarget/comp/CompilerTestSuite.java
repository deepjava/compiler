package ch.ntb.inf.junitTarget.comp;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;
import ch.ntb.inf.junitTarget.comp.arrays.ArraySuite;
import ch.ntb.inf.junitTarget.comp.casts.CastSuite;
import ch.ntb.inf.junitTarget.comp.conditions.ConditionsSuite;
import ch.ntb.inf.junitTarget.comp.objects.ObjectsSuite;
import ch.ntb.inf.junitTarget.comp.primitives.PrimitivesSuite;
import ch.ntb.inf.junitTarget.comp.statements.StatementSuite;
import ch.ntb.inf.junitTarget.comp.unsafe.UnsafeSuite;
import ch.ntb.inf.junitTarget.comp.various.VariousSuite;

@Suite({ PrimitivesSuite.class, StatementSuite.class, ArraySuite.class, CastSuite.class, ConditionsSuite.class, ObjectsSuite.class, UnsafeSuite.class, VariousSuite.class })//     
@MaxErrors(100)

public class CompilerTestSuite {

}
