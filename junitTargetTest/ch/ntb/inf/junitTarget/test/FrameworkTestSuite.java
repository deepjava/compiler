package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@MaxErrors(500)
@Suite({JunitTestSuite.class, AssertTestSuite.class})
public class FrameworkTestSuite {
 
}
