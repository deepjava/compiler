package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Test;
import static ch.ntb.inf.junitTarget.Assert.fail;
public class JUnitErrorTest {

	
	@Test
	public static void errorTest(){
		fail("Error one of six");
		fail("Error two of six");
		fail("Error three of six");
		fail("Error four of six");
		fail("Error five of six");
		fail("Error six of six");
		CmdTransmitter.sendDone();
	}
}
