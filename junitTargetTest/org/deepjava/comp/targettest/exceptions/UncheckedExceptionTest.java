package org.deepjava.comp.targettest.exceptions;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Test;

@SuppressWarnings("unused")
public class UncheckedExceptionTest {
	
	static int x;
	
	@Test
	public static void testArray1() {
		try {
			x = 10;
			int[] a1 = new int[-10]; 
		} catch (NegativeArraySizeException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testArray1", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testArray2() {
		try {
			x = 10;
			int[] a1 = new int[3];
			a1[-1] = 100;
		} catch (ArrayIndexOutOfBoundsException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testArray2", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testArray3() {
		try {
			x = 10;
			int[] a1 = new int[3];
			a1[10] = 100;
		} catch (ArrayIndexOutOfBoundsException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testArray3", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testArray4() {
		x = -1;
		while (x < 10) {
			try {
				char[] a1 = new char[3];
				a1[x] = 100;
				x = 100;
			} catch (ArrayIndexOutOfBoundsException e1) {
				x++;
			}
		}
		Assert.assertEquals("testArray4", 100, x);		
		
		CmdTransmitter.sendDone();
	}

	@SuppressWarnings("null")
	@Test
	public static void testNullPointer() {
		try {
			x = 10;
			int[] a1 = null;
			a1[10] = 100;
		} catch (NullPointerException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testNullPointer", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testClassCast1() {
		try {
			x = 10;
			Object a1 = new int[2];
			short[] a2 = (short[]) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast1", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testClassCast2() {
		try {
			x = 10;
			Object a1 = new Object[2];
			short[] a2 = (short[]) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast2", x, 20);		
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testClassCast3() {
		try {
			x = 10;
			Object a1 = new E1[2];
			short[] a2 = (short[]) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast3", x, 20);		
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testClassCast4() {
		try {
			x = 10;
			Object a1 = new int[2];
			E1[] a2 = (E1[]) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast4", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testClassCast5() {
		try {
			x = 10;
			Object a1 = new E1[2];
			E2[] a2 = (E2[]) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast5", x, 20);		
		
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void testClassCast6() {
		try {
			x = 10;
			Object a1 = new E1[2];
			E2 e2 = (E2) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast6", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testClassCast7() {
		try {
			x = 10;
			Object a1 = new E1();
			E2 e2 = (E2) a1;
		} catch (ClassCastException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testClassCast7", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv1() {
		try {
			x = 10;
			int a = 0;
			int b = 100 / a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv1", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv2() {
		try {
			x = 10;
			int a = 0;
			int b = x / a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv1", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv3() {
		try {
			x = 10;
			long a = 0;
			long b = 100 / a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv3", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv4() {
		try {
			x = 10;
			long a = 0;
			long b = x / a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv4", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv5() {
		try {
			x = 10;
			int a = 0;
			int b = 100 % a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv5", x, 20);		
		
		CmdTransmitter.sendDone();
	}

	@Test
	public static void testDiv6() {
		try {
			x = 10;
			long a = 0;
			long b = 100 % a;
		} catch (ArithmeticException e1) {
			x = 20;
		} catch (Exception e) {
			x = 30;
		}
		Assert.assertEquals("testDiv6", x, 20);		
		
		CmdTransmitter.sendDone();
	}


}

@SuppressWarnings("serial")
class E1 extends Exception { }
@SuppressWarnings("serial")
class E2 extends Exception { }