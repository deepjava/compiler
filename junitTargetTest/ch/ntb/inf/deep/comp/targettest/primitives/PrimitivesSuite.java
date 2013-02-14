package ch.ntb.inf.deep.comp.targettest.primitives;

import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;

@Suite({ ShortTest.class, IntTest.class, ByteTest.class, CharTest.class, LongTest.class, FloatTest.class, DoubleTest.class })
@MaxErrors(100)
public class PrimitivesSuite {

}
