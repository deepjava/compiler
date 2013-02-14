package ch.ntb.inf.junitTarget.test;

import ch.ntb.inf.junitTarget.After;
import ch.ntb.inf.junitTarget.Before;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.Test;
import ch.ntb.inf.junitTarget.Timeout;
import static ch.ntb.inf.junitTarget.Assert.assertEquals;

public class ObjectTest {

	String string;
	byte[] byteArray;
	char[] charArray;
	short[] shortArray;
	int[] intArray;
//	float[] floatArray;
//	double[] doubleArray;
	
	private static ObjectTest objA,objB,objC;
	
	
	public ObjectTest(String string, int deltaInt,float deltaFloat, int size){
		this.string = string;
		byteArray = new byte[size];
		charArray = new char[size];
		shortArray = new short[size];
		intArray = new int[size];
//		floatArray = new float[size];
//		doubleArray = new double[size];	
		for(int i = 0; i < size; i++){
			byteArray[i] = (byte)(i*deltaInt);
			charArray[i] = (char)(i*deltaInt);
			shortArray[i] = (short) (i*deltaInt);
			intArray[i] = i * deltaInt;
//			floatArray[i] = i * deltaFloat;
//			doubleArray[i] = i * deltaFloat;
		}
	}
	
	@Before
	@Timeout(20000)
	public static void setUp(){
		objA = new ObjectTest("String test",2,0.3f,100);
		objB = new ObjectTest("String test",2,0.3f,100);
		objC = new ObjectTest("String test false",3,0.2f,100);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void stringTest(){
		assertEquals("same object test",objA.string,objA.string);
		assertEquals(objA.string,objA.string);
		assertEquals("same values test",objA.string,objB.string);
		assertEquals(objA.string,objB.string);
		assertEquals("not same test",objA.string,objC.string);
		assertEquals(objA.string,objC.string);
		CmdTransmitter.sendDone();
	}
	
	
	@Test
	public static void byteArrayTest(){
		assertEquals("same object test",objA.byteArray,objA.byteArray);
		assertEquals(objA.byteArray,objA.byteArray);
		assertEquals("same values test",objA.byteArray,objB.byteArray);
		assertEquals(objA.byteArray,objB.byteArray);
		assertEquals("not same test",objA.byteArray,objC.byteArray);
		assertEquals(objA.byteArray,objC.byteArray);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void charArrayTest(){
		assertEquals("same object test",objA.charArray,objA.charArray);
		assertEquals(objA.charArray,objA.charArray);
		assertEquals("same values test",objA.charArray,objB.charArray);
		assertEquals(objA.charArray,objB.charArray);
		assertEquals("not same test",objA.charArray,objC.charArray);
		assertEquals(objA.charArray,objC.charArray);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void shortArrayTest(){
		assertEquals("same object test",objA.shortArray,objA.shortArray);
		assertEquals(objA.shortArray,objA.shortArray);
		assertEquals("same values test",objA.shortArray,objB.shortArray);
		assertEquals(objA.shortArray,objB.shortArray);
		assertEquals("not same test",objA.shortArray,objC.shortArray);
		assertEquals(objA.shortArray,objC.shortArray);
		CmdTransmitter.sendDone();
	}
	
	/*@Test
	public static void floatArrayTest(){
		assertEquals("same object test",objA.floatArray,objA.floatArray,0.003f);
		assertEquals(objA.floatArray,objA.floatArray,0.003f);
		assertEquals("same values test",objA.floatArray,objB.floatArray,0.003f);
		assertEquals(objA.floatArray,objB.floatArray,0.003f);
		assertEquals("not same test",objA.floatArray,objC.floatArray,0.003f);
		assertEquals(objA.floatArray,objC.floatArray,0.003f);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void doubleArrayTest(){
		assertEquals("same object test",objA.doubleArray,objA.doubleArray,0.003f);
		assertEquals(objA.doubleArray,objA.doubleArray,0.003f);
		assertEquals("same values test",objA.doubleArray,objB.doubleArray,0.003f);
		assertEquals(objA.doubleArray,objB.doubleArray,0.003f);
		assertEquals("not same test",objA.doubleArray,objC.doubleArray,0.003f);
		assertEquals(objA.doubleArray,objC.doubleArray,0.003f);
		CmdTransmitter.sendDone();
	}
	
	@Test
	public static void objectTest(){
		assertEquals("same object test",objA,objA);
		assertEquals(objA,objA);
		assertEquals("not same object test",objA,objB);
		assertEquals(objA,objB);
		CmdTransmitter.sendDone();
	}*/
	
	@After
	public static void clean(){
		objA = null;
		objB = null;
		objC = null;
		CmdTransmitter.sendMessage("14 failures expected");
		CmdTransmitter.sendDone();
	}
	
}
