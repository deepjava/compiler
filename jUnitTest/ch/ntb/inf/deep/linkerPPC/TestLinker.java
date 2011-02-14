package ch.ntb.inf.deep.linkerPPC;

import static org.junit.Assert.assertEquals;

import ch.ntb.inf.deep.classItems.Class;

public class TestLinker {

	/**
	 * Check the number of elements of a class
	 * 
	 * @param clazz
	 *            clazz to check
	 * @param nOfMethodes
	 *            expected number of methods in this class
	 * @param nOfInstanceFields
	 *            expected number of instance fields in this class
	 * @param nOfClassFields
	 *            expected number of static fields in this class
	 * @param nOfInterfaces
	 * 			  expected number of interfaces which this class implements
	 * @param nOfBaseClasses
	 * 			  expected number of base classes of this class
	 */
	public static void testNumberOfElements(Class clazz, int nOfMethodes, int nOfInstanceFields, int nOfClassFields, int nOfInterfaces, int nOfBaseClasses) {
		assertEquals("Number of methods not as expected", nOfMethodes, clazz.nofMethods);
		assertEquals("Number of instance fields not as expected", nOfInstanceFields, clazz.nofInstFields);
		assertEquals("Number of class/static fields not as expected", nOfClassFields, clazz.nofClassFields);
		assertEquals("Number of interfaces not as expected", nOfInterfaces, clazz.nofInterfaces);
		assertEquals("Number of base classes not as expected", nOfBaseClasses, clazz.nofBaseClasses);
	}
	
}
