package ch.ntb.inf.deep.comp.targettest.objects;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ InheritanceTest.class, InstanceTest.class, InterfaceTest.class, EnumTest.class })

@MaxErrors(500)

public class ObjectsSuite {

}
