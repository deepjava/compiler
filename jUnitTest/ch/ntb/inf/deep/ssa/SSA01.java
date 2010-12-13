package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA01 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T01SimpleMethods"};
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			createSSA(Type.rootClasses[0]);
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
		testNode(nodes[0],5,0,4);
 	}
	
	@Test
	public void simple3() {
		SSANode[] nodes = getAndTestSSA("simple3", 1,0);
		testNode(nodes[0],5,0,4);
 	}

}
