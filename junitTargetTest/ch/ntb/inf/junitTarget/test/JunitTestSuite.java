package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.deep.runtime.mpc555.driver.MPIOSM_DIO;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Suite;
import ch.ntb.inf.junitTarget.Test;

@Suite({JUnitRunOrderTest.class,JUnitErrorTest.class,JUnitWaitTest.class})
@MaxErrors(100)
public class JunitTestSuite {
	
	
	@Test
	public static void suiteTest(){
		MPIOSM_DIO.init(5, true);
		MPIOSM_DIO.set(5, !MPIOSM_DIO.get(5));
		Assert.fail();
		CmdTransmitter.sendDone();
	}
}
