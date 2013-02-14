package ch.ntb.inf.junitTarget.test;

import static ch.ntb.inf.junitTarget.Assert.assertTrue;
import static ch.ntb.inf.junitTarget.Assert.fail;
import ch.ntb.inf.deep.runtime.mpc555.Task;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.junitTarget.Timeout;


@MaxErrors(5)
public class JUnitWaitTest {

	private static final int waitTime = 2000;
	private static int startTime;
	
	@Before
	public static void setUp(){
		startTime = Task.time();
		CmdTransmitter.sendWait(waitTime);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void waitTest(){
		int diff = Task.time() - (startTime + waitTime);
		CmdTransmitter.sendWait(waitTime);
		assertTrue(diff >= 0 && diff <= 1000);
		CmdTransmitter.sendDone();
	}
	
	
	
	@Test
	@Timeout(1000)
	public static void timeoutTest(){
		for(int i = 0; i < 1000000; i++);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void timeoutFollowTest(){
		//for(int i = 0; i < 1000000; i++);
		fail("it is okay");
		CmdTransmitter.sendDone();
	}
	
	
}
