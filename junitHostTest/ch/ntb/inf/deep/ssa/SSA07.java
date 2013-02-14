package ch.ntb.inf.deep.ssa;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class SSA07 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T07Arrays") };
		try {
			CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 10, 0, 4);
	}
	
	@Test
	public void testEmptyIntArray() {
		SSANode[] nodes = getAndTestSSA("emptyIntArray", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIntArrayParam() {
		SSANode[] nodes = getAndTestSSA("intArray", 4, 1);
		testNode(nodes[0], 5, 0, 7);
		testNode(nodes[1], 4, 0, 7);
		testNode(nodes[2], 2, 1, 7);
		testNode(nodes[3], 3, 0, 7);
	}
	
	@Test
	public void testStringArray() {
		SSANode[] nodes = getAndTestSSA("stringArray", 1, 0);
		testNode(nodes[0], 20, 0, 5);
	}
	
	@Test
	public void testObjectArray() {
		SSANode[] nodes = getAndTestSSA("objectArray", 1, 0);
		testNode(nodes[0], 7, 0, 5);
	}
	
	@Test
	public void testMultiArray(){
		SSANode[] nodes = getAndTestSSA("multiArray", 1, 0);
		testNode(nodes[0], 39, 0, 12);
	}
	
	@Test
	public void testMultiObjectArray(){
		SSANode[] nodes = getAndTestSSA("multiObjectArray", 1, 0);
		testNode(nodes[0], 28, 0, 10);
	}
}
