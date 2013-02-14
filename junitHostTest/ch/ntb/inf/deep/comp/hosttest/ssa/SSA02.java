package ch.ntb.inf.deep.comp.hosttest.ssa;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.strings.HString;

public class SSA02 extends TestSSA {
	
	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T02Branches") };
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
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIf1(){
		SSANode[] nodes = getAndTestSSA("if1", 4, 0);
		testNode(nodes[0], 2, 0, 4);//1 Instruction because load parameter for first use
		testNode(nodes[1], 3, 0, 4);
		testNode(nodes[2], 2, 0, 4);
		testNode(nodes[3], 1, 1, 4);
	}
	
	@Test
	public void testIf2(){
		SSANode[] nodes = getAndTestSSA("if2", 4, 0);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 1, 0, 4);
		testNode(nodes[3], 6, 1, 4);
	}

	@Test
	public void testIf3(){
		SSANode[] nodes = getAndTestSSA("if3", 6, 0);
		testNode(nodes[0], 3, 0, 7);
		testNode(nodes[1], 2, 0, 7);
		testNode(nodes[2], 4, 0, 7);
		testNode(nodes[3], 4, 0, 7);
		testNode(nodes[4], 5, 0, 7);
		testNode(nodes[5], 1, 2, 7);
	}
	
	@Test
	public void testIf4(){
		SSANode[] nodes = getAndTestSSA("if4", 4, 0);
		testNode(nodes[0], 4, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 4, 1, 5);
	}
	
	@Test
	public void testIf5(){
		SSANode[] nodes = getAndTestSSA("if5", 4, 0);
		testNode(nodes[0], 2, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 2, 2, 5);
	}
	
	@Test
	public void testIf6(){
		SSANode[] nodes = getAndTestSSA("if6", 14, 0);
		testNode(nodes[0], 4, 0, 9);
		testNode(nodes[1], 2, 0, 9);
		testNode(nodes[2], 1, 0, 9);
		testNode(nodes[3], 1, 1, 9);
		testNode(nodes[4], 2, 0, 9);
		testNode(nodes[5], 3, 0, 9);
		testNode(nodes[6], 3, 0, 9);
		testNode(nodes[7], 2, 0, 9);
		testNode(nodes[8], 3, 1, 9);
		testNode(nodes[9], 1, 0, 9);
		testNode(nodes[10], 3, 2, 9);
		testNode(nodes[11], 3, 0, 9);
		testNode(nodes[12], 2, 0, 9);
		testNode(nodes[13], 1, 2, 9);
	}
	
	
}
