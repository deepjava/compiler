package ch.ntb.inf.deep.ssa;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class SSA03 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T03Switch") };
		try {
			Class.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemPrimitives(), attributes);
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
	
//	@Ignore
	@Test
	public void testSwitchNear1(){
		SSANode[] nodes = getAndTestSSA("switchNear1", 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);
	}
	
//	@Ignore
	@Test
	public void testSwitchNear2(){
		SSANode[] nodes = getAndTestSSA("switchNear2", 9, 0);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 3, 0, 3);
		testNode(nodes[4], 2, 0, 3);
		testNode(nodes[5], 2, 0, 3);
		testNode(nodes[6], 3, 1, 3);
		testNode(nodes[7], 2, 0, 3);
		testNode(nodes[8], 3, 1, 3);
	}
	
//	@Ignore
	@Test
	public void testSwitchNear3(){
		SSANode[] nodes = getAndTestSSA("switchNear3", 4, 0);
		testNode(nodes[0], 4, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 1, 0, 6);
		testNode(nodes[3], 4, 0, 6);
	}
	
//	@Ignore
	@Test
	public void testSwitchFar1(){
		SSANode[] nodes = getAndTestSSA("switchFar1", 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);		
	}
	
//	@Ignore
	@Test
	public void testSwitchFar2(){
		SSANode[] nodes = getAndTestSSA("switchFar2", 10, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 1, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 3, 0, 2);
		testNode(nodes[5], 2, 0, 2);
		testNode(nodes[6], 1, 1, 2);
		testNode(nodes[7], 3, 0, 2);
		testNode(nodes[8], 2, 0, 2);
		testNode(nodes[9], 1, 1, 2);
	}

	@Test
	public void testSwitchWhile(){
		SSANode[] nodes = getAndTestSSA("switchWhile", 6, 1);
		testNode(nodes[0], 3, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 1, 0, 3);
		testNode(nodes[4], 1, 0, 3);
		testNode(nodes[5], 1, 1, 3);
	}
}
