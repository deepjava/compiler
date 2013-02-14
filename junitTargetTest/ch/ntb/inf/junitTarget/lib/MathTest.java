package ch.ntb.inf.junitTarget.lib;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 30.6.2011, Urs Graf
 * 
 *         Changes:
 */

@MaxErrors(100)
public class MathTest {
	
	
	@Test
	public static void testPower1() {
		Assert.assertEquals("Test1", 4.0, Math.powIntExp(2.0, 2), 1e-10);
		Assert.assertEquals("Test2", 1024.0, Math.powIntExp(2.0, 10), 1e-10);
		Assert.assertEquals("Test3", -172.10368, Math.powIntExp(-2.8, 5), 1e-10);
		Assert.assertEquals("Test4", 4.1007644162774e-7, Math.powIntExp(134.6, -3), 1e-10);
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void testSqrt() {
		Assert.assertEquals("Test1", 4.0, Math.sqrt(16.0), 1e-10);
		Assert.assertEquals("Test2", Double.NaN, Math.sqrt(-2.3), 1e-10);
		Assert.assertEquals("Test3", 1.531553013969807e89, Math.sqrt(2.3456546346e178), 1e-8);
		Assert.assertEquals("Test4", 1.53155301396980e-89, Math.sqrt(2.3456546346e-178), 1e-8);
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void testSin() {
		Assert.assertEquals("Test1", 0.0, Math.sin(0.0), 1e-5);
		Assert.assertEquals("Test2", 0.5, Math.sin(Math.PI/6), 1e-5);
		Assert.assertEquals("Test3", 1.0, Math.sin(Math.PI/2), 1e-5);
		Assert.assertEquals("Test4", -0.5, Math.sin(-Math.PI/6), 1e-5);
		Assert.assertEquals("Test5", -1.0, Math.sin(Math.PI + Math.PI/2), 1e-5);
		Assert.assertEquals("Test6", 0.5, Math.sin(4 * Math.PI + Math.PI/6), 1e-5);
		Assert.assertEquals("Test7", -0.5, Math.sin(5 * Math.PI + Math.PI/6), 1e-5);
		CmdTransmitter.sendDone();
	}	

	@Test
	public static void testCos() {
		Assert.assertEquals("Test1", 1.0, Math.cos(0.0), 1e-5);
		Assert.assertEquals("Test2", 0.5, Math.cos(Math.PI/3), 1e-5);
		Assert.assertEquals("Test3", 0.0, Math.cos(Math.PI/2), 1e-5);
		Assert.assertEquals("Test4", 0.5, Math.cos(-Math.PI/3), 1e-5);
		Assert.assertEquals("Test5", -0.5, Math.cos(Math.PI + Math.PI/3), 1e-5);
		Assert.assertEquals("Test6", 0.5, Math.cos(2 * Math.PI + Math.PI/3), 1e-5);
		Assert.assertEquals("Test7", -0.5, Math.cos(3 * Math.PI + Math.PI/3), 1e-5);
		CmdTransmitter.sendDone();
	}	
}
