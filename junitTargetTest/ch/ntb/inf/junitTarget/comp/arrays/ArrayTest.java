package ch.ntb.inf.junitTarget.comp.arrays;

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
public class ArrayTest {
	
	@Test
	// Main test method for array length testing
	public static void testArrayLength() {
		int res;
		res = (new byte[3]).length;
		Assert.assertEquals("byteArrayLength", 3, res);
		res = (new short[3]).length;
		Assert.assertEquals("shortArrayLength", 3, res);
		res = (new char[3]).length;
		Assert.assertEquals("charArrayLength", 3, res);
		res = (new int[3]).length;
		Assert.assertEquals("intArrayLength", 3, res);
		res = (new long[3]).length;
		Assert.assertEquals("longArrayLength", 3, res);
		res = (new float[3]).length;
		Assert.assertEquals("floatArrayLength", 3, res);
		res = (new double[3]).length;
		Assert.assertEquals("doubleArrayLength", 3, res);
		CmdTransmitter.sendDone();
	}

	@Test
	// Main test method for array value test
	public static void testArrayValue() {
		int res;
		byte[] b = {0, 1, 2, 3, 4};
		res = b[0];
		Assert.assertEquals("byteArrayValue1", 0, res);
		res = b[4];
		Assert.assertEquals("byteArrayValue2", 4, res);
		
		short[] s = {250, 251, 252, 253, 254};
		res = s[0];
		Assert.assertEquals("shortArrayValue1", 250, res);
		res = s[4];
		Assert.assertEquals("shortArrayValue2", 254, res);

		char[] ch = {'a', 'b', 'c', 'd', 'e'};
		res = ch[0];
		Assert.assertEquals("charArrayValue1", 'a', res);
		res = ch[4];
		Assert.assertEquals("charArrayValue1", 'e', res);
		
		int[] i = {65000, 65001, 65002, 65003, 65004};
		res = i[0];
		Assert.assertEquals("intArrayValue1", 65000, res);
		res = i[4];
		Assert.assertEquals("intArrayValue2", 65004, res);
		
		long[] l = {23348456749506556L, 6501, -234563455, 345345665456457L, -2343596845645463453L};
		long resLong = l[0];
		Assert.assertEquals("longArrayValue1", 23348456749506556L, resLong);
		resLong = l[4];
		Assert.assertEquals("longArrayValue2", -2343596845645463453L, resLong);
		
		float[] f = {0.5f, 1.5f, 2.5f, 3.5f, 4.5f};
		float resf = f[0];
		Assert.assertEquals("floatArrayValue1", 0.5f, resf, 0.0001f);
		resf = f[4];
		Assert.assertEquals("floatArrayValue2", 4.5f, resf, 0);
		
		double[] d = {0.5, 1.5, 2.5, 3.5, 4.5};
		double resd = d[0];
		Assert.assertEquals("doubleArrayValue1", 0.5, resd, 0.0);
		resd = d[4];
		Assert.assertEquals("doubleArrayValue2", 4.5, resd, 0.0);
			
		CmdTransmitter.sendDone();
	}
	
}
