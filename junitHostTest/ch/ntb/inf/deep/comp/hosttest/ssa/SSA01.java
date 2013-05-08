package ch.ntb.inf.deep.comp.hosttest.ssa;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.strings.HString;

public class SSA01 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T01SimpleMethods") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

 	@Test
	public void emptyMethodStatic() {
		SSANode[] nodes = getAndTestSSA("emptyMethodStatic", 1, 0);
		testNode(nodes[0],1,0,0);
 	}
	
	@Test
	public void emptyMethod() {
		SSANode[] nodes = getAndTestSSA("emptyMethod", 1, 0);
		testNode(nodes[0],1,0,1);
 	}

	@Test
	public void assignment1() {
		SSANode[] nodes = getAndTestSSA("assignment1", 1,0);
		testNode(nodes[0],2,0,2);
 	}
	
	@Test
	public void simple1() {
		SSANode[] nodes = getAndTestSSA("simple1", 1,0);
		testNode(nodes[0],11,0,4);
 	}
	
	@Test
	public void simple2() {
		SSANode[] nodes = getAndTestSSA("simple2", 1,0);
		testNode(nodes[0],8,0,4);
 	}
	
	@Test
	public void simple3() {
		SSANode[] nodes = getAndTestSSA("simple3", 1,0);
		testNode(nodes[0],5,0,4);
 	}

	@Test
	public void simple4() {
		SSANode[] nodes = getAndTestSSA("simple4", 1,0);
		testNode(nodes[0],19,0,2);
	}

	@Test
	public void simple5() {
		SSANode[] nodes = getAndTestSSA("simple5", 1,0);
		testNode(nodes[0],5,0,4);
	}

	@Test
	public void simple6() {
		SSANode[] nodes = getAndTestSSA("simple6", 1,0);
		testNode(nodes[0],7,0,7);
	}

}
