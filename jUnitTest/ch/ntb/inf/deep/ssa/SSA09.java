package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class SSA09 extends TestSSA {
	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T09Types" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,Configuration.getSearchPaths(),null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createSSA(Class.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testM1(){
		SSANode[] nodes = getAndTestSSA("m1", 7, 0);
		testNode(nodes[0], 4, 0, 11);
		testNode(nodes[1], 3, 0, 11);
		testNode(nodes[2], 1, 0, 11);
		testNode(nodes[3], 6, 0, 11);
		testNode(nodes[4], 2, 0, 11);
		testNode(nodes[5], 1, 0, 11);
		testNode(nodes[6], 6, 1, 11);
	}
	
	@Test
	public void m2(){
		SSANode[] nodes = getAndTestSSA("m2", 1, 0);
		testNode(nodes[0], 23, 0, 16);
		
	}
	
	@Test
	public void callm2(){
		SSANode[] nodes = getAndTestSSA("callm2", 1, 0);
		testNode(nodes[0], 12, 0, 21);
	}
	
	@Test
	public void m3(){
		SSANode[] nodes = getAndTestSSA("m3", 4, 0);
		testNode(nodes[0], 2, 0, 7);
		testNode(nodes[1], 3, 0, 7);
		testNode(nodes[2], 1, 0, 7);
		testNode(nodes[3], 1, 1, 7);
	}
	
	}
