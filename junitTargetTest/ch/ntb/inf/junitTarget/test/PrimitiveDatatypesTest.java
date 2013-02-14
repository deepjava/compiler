package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

@MaxErrors(200)
public class PrimitiveDatatypesTest {

	@Test
	public static void failTest(){
		Assert.fail("Fail test");
		Assert.fail();
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void booleanTest(){
		Assert.assertTrue("True test",true);
		Assert.assertTrue(true);
		Assert.assertTrue("False test",false);
		Assert.assertTrue(false);
		Assert.assertFalse("True test",false);
		Assert.assertFalse(false);
		Assert.assertFalse("False test",true);
		Assert.assertFalse(true);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void byteTest(){
		Assert.assertEquals("True test",(byte) 12,(byte) 12);
		Assert.assertEquals((byte) 12,(byte) 12);
		Assert.assertEquals("False test",(byte) 12,(byte) 150);
		Assert.assertEquals((byte) 12,(byte) 150);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void charTest(){
		Assert.assertEquals("True test",'a', 'a');
		Assert.assertEquals('a', 'a');
		Assert.assertEquals("False test",'a', 'b');
		Assert.assertEquals('a', 'b');
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void shortTest(){
		Assert.assertEquals("True test",(short) 12,(short) 12);
		Assert.assertEquals((short) 12,(short) 12);
		Assert.assertEquals("False test",(short) 12,(short) 150);
		Assert.assertEquals((short) 12,(short) 150);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void intTest(){
		Assert.assertEquals("True test",12, 12);
		Assert.assertEquals(12, 12);
		Assert.assertEquals("False test", 12, 150);
		Assert.assertEquals(12, 150);
		
		CmdTransmitter.sendDone();
	}
	
//	@Test
//	public static void floatTest(){
//		Assert.assertEquals("True test",12.333f, 12.333f,0.001f);
//		Assert.assertEquals(12.333f, 12.333f,0.001f);
//		Assert.assertEquals("False test", 12.333f, 12.323f,0.001f);
//		Assert.assertEquals( 12.333f, 12.323f,0.001f);
//		Assert.assertEquals("Negative Infinity true test", Assert.floatNegInfinity, Assert.floatNegInfinity,0.001f);
//		Assert.assertEquals( Assert.floatNegInfinity, Assert.floatNegInfinity,0.001f);
//		Assert.assertEquals("Negative Infinity false test", Assert.floatNegInfinity, 2.33f,0.001f);
//		Assert.assertEquals(Assert.floatNegInfinity, 2.33f,0.001f);
//		Assert.assertEquals("Positive Infinity true test", Assert.floatPosInfinity, Assert.floatPosInfinity,0.001f);
//		Assert.assertEquals( Assert.floatPosInfinity, Assert.floatPosInfinity,0.001f);
//		Assert.assertEquals("Positive Infinity false test", Assert.floatPosInfinity, 2.33f,0.001f);
//		Assert.assertEquals( Assert.floatPosInfinity, 2.33f,0.001f);
//		
//		CmdTransmitter.sendDone();
//	}
	
	/*@Test
	public static void doubleTest(){
		Assert.assertEquals("True test",12.333, 12.333,0.001);
		Assert.assertEquals(12.333, 12.333,0.001);
		Assert.assertEquals("False test", 12.333, 12.323,0.001);
		Assert.assertEquals( 12.333, 12.323,0.001);
		Assert.assertEquals("Negative Infinity true test", Assert.doubleNegInfinity, Assert.doubleNegInfinity,0.001);
		Assert.assertEquals(Assert.doubleNegInfinity, Assert.doubleNegInfinity,0.001);
		Assert.assertEquals("Negative Infinity false test", Assert.doubleNegInfinity, 2.33,0.001);
		Assert.assertEquals(Assert.doubleNegInfinity, 2.33,0.001);
		Assert.assertEquals("Positive Infinity true test", Assert.doublePosInfinity, Assert.doublePosInfinity,0.001f);
		Assert.assertEquals(Assert.doublePosInfinity, Assert.doublePosInfinity,0.001f);
		Assert.assertEquals("Positive Infinity false test", Assert.doublePosInfinity, 2.33,0.001);
		Assert.assertEquals(Assert.doublePosInfinity, 2.33,0.001);
		
		CmdTransmitter.sendDone();
	}*/
	
}
