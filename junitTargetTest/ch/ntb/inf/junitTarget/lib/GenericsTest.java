package ch.ntb.inf.junitTarget.lib;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 25.3.2011, Urs Graf
 * 
 * generics need wrapper classes for base types!!
 */
@MaxErrors(100)
public class GenericsTest {
	
//	@Ignore
	@Test
	public static void testBoxing() {
		Byte byteObj = -13;
		byte b = byteObj;
		Assert.assertEquals("testByte", -13, b);
		
		Short shortObj = -1000;
		short sh = shortObj;
		Assert.assertEquals("testShort", -1000, sh);
		
		Integer intObj1 = new Integer(1500346578);
		Assert.assertEquals("testInteger1", 1500346578, intObj1.intValue());
		Integer intObj2 = -2500000;
		int res = intObj2;
		Assert.assertEquals("testInteger2", -2500000, res);
		
		Long longObj = 23453253894675L;
		long l = longObj;
		Assert.assertEquals("testLong", 23453253894675L, l);
		
		Character charObj = 'x';
		char ch = charObj;
		Assert.assertEquals("testCharacter", 'x', ch);
		
		Float floatObj = 1.523e5f;
		float f = floatObj;
		Assert.assertEquals("testFloat", 1.523e5f, f, 0.0);
		
		Double doubleObj = -3.2e-67;
		double d = doubleObj;
		Assert.assertEquals("testDouble", -3.2e-67, d, 0.0);
		
		CmdTransmitter.sendDone();
	}	

//	@Ignore
	@Test
	public static void testClassList() {
		List<Integer> list = new List<Integer>();
		list.add(3000);
		int res = list.remove();
		Assert.assertEquals("test1", 3000, res);
		list.add(-30);
		res = list.remove();
		Assert.assertEquals("test2", -30, res);
		
		CmdTransmitter.sendDone();
	}	
	
	public static <T> T random(int x, T m, T n ) {
		return x > 0 ? m : n;  
	}
	
//	@Ignore
	@Test
	public static void randomTest() {
		byte b = random(1, (byte)10, (byte)20);
		Assert.assertEquals("testByte1", 10, b);
		b = random(-1, (byte)10, (byte)20);
		Assert.assertEquals("testByte2", 20, b);
		
		short sh = random(1, (byte)100, (byte)200);
		Assert.assertEquals("testShort1", 100, sh);
		sh = random(-1, (short)100, (short)200);
		Assert.assertEquals("testShort2", 200, sh);
		
		int i = random(1, 1000, 2000);
		Assert.assertEquals("testInteger1", 1000, i);
		i = random(-1, 1000, 2000);
		Assert.assertEquals("testInteger2", 2000, i);
		
		long l = random(1, 10000, 20000);
		Assert.assertEquals("testLong1", 10000, l);
		l = random(-1, 10000, 20000);
		Assert.assertEquals("testLong2", 20000, l);
		
		char ch = random(1, 'a', 'b');
		Assert.assertEquals("testCharacter1", 'a', ch);
		ch = random(-1, 'a', 'b');
		Assert.assertEquals("testCharacter2", 'b', ch);
		
		float f = random(1, 1.5f, 2.5f);
		Assert.assertEquals("testFloat1", 1.5f, f, 0);
		f = random(-1, 1.5f, 2.5f);
		Assert.assertEquals("testFloat2", 2.5f, f, 0);	
		
		double d = random(1, 1.5, 2.5);
		Assert.assertEquals("testDouble1", 1.5, d, 0);
		d = random(-1, 1.5, 2.5);
		Assert.assertEquals("testDouble2", 2.5, d, 0);	
		CmdTransmitter.sendDone();
	}	
	
}

class List<T> {
	T[] data = (T[])new Object[10];
	static int in, out;
	void add(T x) {data[in++] = x;}
	T remove() {return data[out++];}
}
