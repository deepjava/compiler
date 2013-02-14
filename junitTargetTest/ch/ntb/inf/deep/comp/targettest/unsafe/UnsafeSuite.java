package ch.ntb.inf.deep.comp.targettest.unsafe;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ UnsafeTest.class, LowLevelTest.class })

@MaxErrors(500)
public class UnsafeSuite {

}
