package ch.ntb.inf.deep.launcher;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ch.ntb.inf.deep.DeepPlugin;
import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.ssa.SSA;

public class DeepLaunchDelegate extends JavaLaunchDelegate implements ICclassFileConsts{
	/**
	 * Used to map temp file to launch obejct.
	 */
	private ILaunch fLaunch;
	private CFG[] cfg;
	private SSA[] ssa;

	@Override
	public final void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)throws CoreException{
		String[] rootClassNames;
		// root class name
		String program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, (String)null);
		String location = configuration.getAttribute(DeepPlugin.ATTR_DEEP_LOCATION, "");
		if (program == null) {
			throw new CoreException(new Status(IStatus.ERROR, DeepPlugin.PLUGIN_ID, 0, "Root class unspecified.", null));
		}

		//Extract deep specific path
		//cut project folder
		int indexOfSep = program.indexOf('/',1); //full path starts with '/' so we start at index 1
		program = program.substring(indexOfSep +1);
		//cut source folder
		indexOfSep = program.indexOf('/');
		program = program.substring(indexOfSep + 1);
		
		// cut file suffix
		indexOfSep = program.indexOf('.');
		program = program.substring(0, indexOfSep);
		
		rootClassNames = new String[]{program};
	
		try {
			//TODO correct the call
			Class.buildSystem(rootClassNames, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			// create CFG
			Method m1,m2;
			
			int count = 0;	
			m1 =(Method) Type.rootClasses[0].methods;
			m2 = m1;
			while (m2 != null){
				count++;
				m2 = (Method)m2.next;
			}
			cfg = new CFG[count];
			for (int i = 0; i < count; i++) {
				cfg[i] = new CFG(m1);
				m1 = (Method)m1.next;
				cfg[i].printToLog();
			}
			ssa = new SSA[cfg.length];
			for (int i = 0; i < cfg.length; i++){
				ssa[i] = new SSA(cfg[i]);
				System.out.println();
				ssa[i].print(0);
			}		
		}
	}
}
