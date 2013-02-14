package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.After;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.junitTarget.Timeout;

@MaxErrors(20)
public class JUnitRunOrderTest {

	@Before
	@Timeout(1200)
	public static void setUpTest1(){
		Assert.fail("Before 1");
		CmdTransmitter.sendDone();
	}
	
	@Before
	public static void setUpTest2(){
		Assert.fail("Before 2");
		CmdTransmitter.sendDone();
	}
	
	@Before
	public static void setUpTest3(){
		Assert.fail("Before 3");
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testMethod1(){
		Assert.fail("Test 1");
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testMethod2(){
		Assert.fail("Test 2");
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testMethod3(){
		Assert.fail("Test 3");
		CmdTransmitter.sendDone();
	}
	
	@After
	public static void cleanUp1(){
		Assert.fail("After 1");
		CmdTransmitter.sendDone();
	}
	
	@After
	public static void cleanUp2(){
		Assert.fail("After 2");
		CmdTransmitter.sendDone();
	}
	
	@After
	public static void cleanUp3(){
		Assert.fail("After 3");
		CmdTransmitter.sendDone();
	}
	
}
