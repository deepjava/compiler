package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({PrimitiveDatatypesTest.class, ObjectTest.class})
@MaxErrors(100)

public class AssertTestSuite {
	
}
