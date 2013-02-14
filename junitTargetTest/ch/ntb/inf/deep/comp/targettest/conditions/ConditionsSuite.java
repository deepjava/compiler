package ch.ntb.inf.deep.comp.targettest.conditions;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ ConditionTest.class, LongCompTest.class, FloatDoubleCompTest.class })
@MaxErrors(100)
public class ConditionsSuite {

}
