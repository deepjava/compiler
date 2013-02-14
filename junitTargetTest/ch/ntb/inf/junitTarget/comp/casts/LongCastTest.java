package ch.ntb.inf.junitTarget.comp.casts;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 8.4.2011
 * 
 * @author Urs Graf
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class LongCastTest {

	@Test
	// Test long to byte
	public static void testLongToByte(){
		long l = 0xdd77cc88dd66cc88L;
		byte b = (byte)l;
		Assert.assertEquals("longToByte1", (byte)0xdd77cc88dd66cc88L, b);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test long to short
	public static void testLongToShort(){
		long l = 0x144445555L;
		short s = (short)l;
		Assert.assertEquals("longToShort1", (short)0x5555, s);
		l = -1;
		s = (short)l;
		Assert.assertEquals("longToShort2", (short)-1, s);
		l = 0xaaaaccccaaaa5555L;
		s = (short)l;
		Assert.assertEquals("longToShort3", (short)0x5555, s);
		l = 0xaaaacccc84448555L;
		s = (short)l;
		Assert.assertEquals("longToShort4", (short)0x8555, s);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test long to char
	public static void testLongToChar(){
		long l = 0xdd77cc88dd66cc88L;
		char ch = (char)l;
		Assert.assertEquals("longToChar1", (char)0xcc88, ch);
		CmdTransmitter.sendDone();
	}

	@Test
	// Test long to int
	public static void testLongToInt(){
		long l = 0x144445555L;
		int i = (int)l;
		Assert.assertEquals("longToInt1", 0x44445555, i);
		l = -1;
		i = (int)l;
		Assert.assertEquals("longToInt2", -1, i );
		l = 0xaaaacccc44445555L;
		i = (int)l;
		Assert.assertEquals("longToInt3", 0x44445555, i);
		l = 0xaaaacccc84445555L;
		i = (int)l;
		Assert.assertEquals("longToInt4", 0x84445555, i);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test long to float
	public static void testLongToFloat(){
		long l = 0x100000000L;
		float f = l;
		Assert.assertEquals("longToFloat1", 4.294967296e9f, f, 0);
		l = 0x100000001L;
		f = l;
		Assert.assertEquals("longToFloat2", 4.294967297e9f, f, 0);
		l = 0x180000000L;
		f = l;
		Assert.assertEquals("longToFloat3", 6.442450944e9f, f, 0);
		l = 0x7fffffffffffffffL;
		f = l;
		Assert.assertEquals("longToFloat4", 9.223372036854775807e18f, f, 0);
		l = -1;
		f = l;
		Assert.assertEquals("longToFloat5", -1.0, f, 0);
		l = 0x8000000000000000L;
		f = l;
		Assert.assertEquals("longToFloat6", -9.223372036854775808e18f, f, 0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test long to double
	public static void testLongToDouble(){
		long l = 0x100000000L;
		double d = l;
		Assert.assertEquals("test1", 4.294967296e9, d, 0);
		l = 0x100000001L;
		d = l;
		Assert.assertEquals("test2", 4.294967297e9, d, 0);
		l = 0x180000000L;
		d = l;
		Assert.assertEquals("test3", 6.442450944e9, d, 0);
		l = 0x80000000L;
		d = l;
		Assert.assertEquals("test4", 2.147483648e9, d, 0);
		l = 0x80000001L;
		d = l;
		Assert.assertEquals("test5", 2.147483649e9, d, 0);
		l = 0x7fffffffffffffffL;
		d = l;
		Assert.assertEquals("test6", 9.223372036854775807e18, d, 0);
		l = -1;
		d = l;
		Assert.assertEquals("test7", -1.0, d, 0);
		l = 0x8000000000000000L;
		d = l;
		Assert.assertEquals("test8", -9.223372036854775808e18, d, 0);
		
		CmdTransmitter.sendDone();
	}

}
