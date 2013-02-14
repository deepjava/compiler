package ch.ntb.inf.junitTarget.lib;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ MathTest.class, GenericsTest.class, TaskTest.class })

@MaxErrors(500)
public class LibSuite {

}
