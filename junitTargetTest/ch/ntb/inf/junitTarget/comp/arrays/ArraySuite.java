package ch.ntb.inf.junitTarget.comp.arrays;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ ArrayTest.class, StringTest.class, ArrayTwoDimTest.class, ArrayThreeDimTest.class, ArrayInstanceTest.class })

@MaxErrors(500)
public class ArraySuite {

}
