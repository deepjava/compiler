package ch.ntb.inf.deep.comp.targettest.unsafe;

import ch.ntb.inf.deep.lowLevel.LL;
import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 4.3.2011, Urs Graf
 * 
 *         Changes:
 */
@MaxErrors(100)
public class LowLevelTest {
	
	@Test
	public static void testDoubleToBits() {
		double d1 = 0.0;
		if(!CmdTransmitter.host){
			Assert.assertEquals("Test1", 0x0000000000000000L, LL.doubleToBits(d1));
			d1 = 1.0;
			Assert.assertEquals("Test2", 0x3FF0000000000000L, LL.doubleToBits(d1));
			d1 = 2.5;
			Assert.assertEquals("Test3", 0x4004000000000000L, LL.doubleToBits(d1));
			d1 = 0.1;
			Assert.assertEquals("Test4", 0x3FB999999999999AL, LL.doubleToBits(d1));
			d1 = -0.1;
			Assert.assertEquals("Test5", 0xBFB999999999999AL, LL.doubleToBits(d1));
			float f1 = 0.0f;
			Assert.assertEquals("Test6", 0x0000000000000000L, LL.doubleToBits(f1));
			f1 = 1.0f;
			Assert.assertEquals("Test7", 0x3FF0000000000000L, LL.doubleToBits(f1));
			f1 = 2.0f;
			Assert.assertEquals("Test8", 0x4000000000000000L, LL.doubleToBits(f1));
			f1 = 2.5f;
			Assert.assertEquals("Test9", 0x4004000000000000L, LL.doubleToBits(f1));
			f1 = 0.1f;
			Assert.assertEquals("Test10", 0x3FB99999A0000000L, LL.doubleToBits(f1));
			f1 = -0.1f;
			Assert.assertEquals("Test11", 0xBFB99999A0000000L, LL.doubleToBits(f1));
			
			d1 = 10;
			Assert.assertEquals("Test12", 0x4024000000000000L, LL.doubleToBits(d1));
			f1 = 10;
			Assert.assertEquals("Test13", 0x4024000000000000L, LL.doubleToBits(f1));
		}
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testBitsToDouble() {
		long bits = 0x4004000000000000L;
		if(!CmdTransmitter.host){
			Assert.assertEquals("Test1", 2.5, LL.bitsToDouble(bits), 0.0);
			bits = 0xBFB999999999999AL;
			Assert.assertEquals("Test2", -0.1, LL.bitsToDouble(bits), 0.0);
		}
		CmdTransmitter.sendDone();
	}
	
}
