package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class SSA00 extends TestSSA {

	@Before
	public void setUp() {
    	String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T00EmptyClass"};
    	Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,Configuration.getSearchPaths(),null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));
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
    	// constructor
		SSANode[] node = getAndTestSSA("<init>",1,0);
		testNode(node[0], 3, 0, 2);
	}
}
