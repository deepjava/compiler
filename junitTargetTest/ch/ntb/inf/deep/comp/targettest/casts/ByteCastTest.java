package ch.ntb.inf.deep.comp.targettest.casts;

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
public class ByteCastTest {

	@Test
	// Test byte to short
	public static void testByteToShort(){
		byte b = 24;
		short s = b;
		Assert.assertEquals("byteToShort1", (short)24, s);
		b = -1;
		s = b;
		Assert.assertEquals("byteToShort2", (short)-1, s);
		b = -128;
		s = b;
		Assert.assertEquals("byteToShort3", (short)-128, s);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test byte to char
	public static void testByteToChar(){
		byte b = 24;
		char ch = (char)b;
		Assert.assertEquals("byteToChar1", (char)24, ch);
		b = -1;
		ch = (char)b;
		Assert.assertEquals("byteToChar2", (char)-1, ch);
		b = -128;
		ch = (char)b;
		Assert.assertEquals("byteToChar3", (char)-128, ch);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test byte to int
	public static void testByteToInt(){
		byte b = 24;
		int i = b;
		Assert.assertEquals("byteToInt1", (int)24, i);
		b = -1;
		i = b;
		Assert.assertEquals("byteToInt2", (int)-1, i);
		b = -128;
		i = b;
		Assert.assertEquals("byteToInt3", (int)-128, i);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	// Test byte to long
	public static void testByteToLong(){
		byte b = 24;
		long l = b;
		Assert.assertEquals("byteToLong1", (long)24, l);
		b = -1;
		l = b;
		Assert.assertEquals("byteToLong2", (long)-1, l);
		b = -128;
		l = b;
		Assert.assertEquals("byteToLong3", (long)-128, l);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test byte to float
	public static void testByteToFloat(){
		byte b = 24;
		float f = b;
		Assert.assertEquals("byteToFloat1", 24.0f, f, 0);
		b = -1;
		f = b;
		Assert.assertEquals("byteToFloat2", -1.0f, f, 0);
		b = -128;
		f = b;
		Assert.assertEquals("byteToFloat3", -1.28e2f, f, 0);
		b = 1;
		f = b;
		Assert.assertEquals("byteToFloat4", 1.0f, f, 0);
		b = 2;
		f = b;
		Assert.assertEquals("byteToFloat5", 2.0f, f, 0);
		b = 0;
		f = b;
		Assert.assertEquals("byteToFloat6", 0.0f, f, 0);
		
		CmdTransmitter.sendDone();
	}

	@Test
	// Test byte to double
	public static void testByteToDouble(){
		byte b = 24;
		double d = b;
		Assert.assertEquals("byteToDouble1", 24.0, d, 0);
		b = -1;
		d = b;
		Assert.assertEquals("byteToDouble2", -1.0, d, 0);
		b = -128;
		d = b;
		Assert.assertEquals("byteToDouble3", -1.28e2, d, 0);
		b = 1;
		d = b;
		Assert.assertEquals("byteToDouble4", 1.0, d, 0);
		b = 2;
		d = b;
		Assert.assertEquals("byteToDouble5", 2.0, d, 0);
		b = 0;
		d = b;
		Assert.assertEquals("byteToDouble6", 0.0, d, 0);
		
		CmdTransmitter.sendDone();
	}
}
