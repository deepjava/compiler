package ch.ntb.inf.junitTarget.comp.conditions;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ ConditionTest.class, LongCompTest.class, FloatDoubleCompTest.class })
@MaxErrors(100)
public class ConditionsSuite {

}
