package ch.ntb.inf.deep.eclipse.ui.refactoring;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import ch.ntb.inf.deep.eclipse.ui.properties.DeepFileChanger;

public class RenameDeepProject extends RenameParticipant {
	private IProject p;
	private DeepFileChanger dfc;
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor arg0,
			CheckConditionsContext arg1) throws OperationCanceledException {
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor arg0) throws CoreException,
			OperationCanceledException {
//		IProject p = pType.getProject();  // limit to the current project
		System.out.println("createChange");
		System.out.println(p.getName());
		System.out.println(p.getProject().getName());
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	protected boolean initialize(Object element) {
		
		System.out.println("initialize");
		System.out.println(getArguments().getNewName());
		// read deep project file
		p = (IProject) element;
		System.out.println(p.getName());
		System.out.println(p.getProject().getName());
		System.out.println("deep file ist: " + (p.getLocation()	+ "/" + p.getName() + ".deep"));
//		dfc = new DeepFileChanger(p.getLocation()	+ "/" + p.getName() + ".deep");
//		GregorianCalendar cal = new GregorianCalendar();
//		dfc.changeContent("version", "\"" + cal.getTime().toString() + "\"");
		dfc.save();
		return true;
	}

}
