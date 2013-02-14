package ch.ntb.inf.junitTarget.comp.objects;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ InheritanceTest.class, InstanceTest.class, InterfaceTest.class })

@MaxErrors(500)

public class ObjectsSuite {

}
