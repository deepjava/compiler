package ch.ntb.inf.deep.comp.targettest.arrays;

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
public class StringTest {
	
	@Test
	public static void stringLenTest() {
		String str1 = "hello world";
		int len;
		len = str1.length();
		Assert.assertEquals("stringLenTest1", 11, len);
		char[] a1 = new char[] {'a', 'b', 'c', 'd', 'e'};
		str1 = new String(a1);
		len = str1.length();
		Assert.assertEquals("stringLenTest2", 5, len);
		str1 = new String(a1, 3, 2);
		len = str1.length();
		Assert.assertEquals("stringLenTest3", 2, len);
		str1 = new String(a1, 2, 0);
		len = str1.length();
		Assert.assertEquals("stringLenTest4", 0, len);
		CmdTransmitter.sendDone();
	}

	@Test
	public static void stringCharTest() {
		char ch;
		String str1 = "hello world";
		ch = str1.charAt(0);
		Assert.assertEquals("stringCharTest1", 'h', ch);
		ch = str1.charAt(10);
		Assert.assertEquals("stringCharTest2", 'd', ch);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void stringGetBytesTest() {
		char ch;
		String str1 = "abc";
		byte[] a1 = str1.getBytes();
		Assert.assertEquals("stringGetBytesTest", new byte[] {'a','b','c'}, a1);
		char[] a2 = new char[5];
//		str1.getChars(a2, 1);	// cannot be tested, String in Std-Lib is different
//		Assert.assertEquals("stringGetBytesTest", new byte[] {'a','b','c'}, a1);
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void typeTest() {
		Object o = "hello";
		String str1 = (String) o;
		Assert.assertEquals("test1", str1, "hello");
		char[] a1 = new char[] {'a', 'b', 'c', 'd', 'e'};
		o = new String(a1);
		str1 = (String) o;
		Assert.assertEquals("test2", str1, "abcde");
		
		CmdTransmitter.sendDone();
	}
	
}
