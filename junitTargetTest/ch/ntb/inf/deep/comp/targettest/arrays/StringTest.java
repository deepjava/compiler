/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

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
@SuppressWarnings("unused")
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

	@Test
	public static void compareTest() {
		String str1 = "abc";
		Assert.assertTrue("test1", str1.equals("abc"));
		Assert.assertFalse("test2", str1.equals("abcd"));
		Assert.assertEquals("test10", str1.compareTo("ab"), 1);
		Assert.assertEquals("test11", str1.compareTo("abc"), 0);
		Assert.assertEquals("test12", str1.compareTo("abcd"), -1);
		Assert.assertEquals("test13", str1.compareTo("xx"), -23);
		Assert.assertEquals("test14", str1.compareTo("b"), -1);
		Assert.assertEquals("test15", str1.compareTo("Abc"), 32);
		Assert.assertEquals("test16", str1.compareTo("ABC"), 32);
		CmdTransmitter.sendDone();
	}

}
