package ch.ntb.inf.deep.eclipse.ui.refactoring;

import java.io.File;
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
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	protected boolean initialize(Object element) {
		String newName = getArguments().getNewName();
		p = (IProject) element;
		dfc = new DeepFileChanger(p.getLocation()	+ "/" + p.getName() + ".deep");
		dfc.changeContent("description", "\"deep project file for " + newName + "\"");
		dfc.changeProjectName(newName);
		dfc.save();
		File oldFile = new File(p.getLocation()	+ "/" + p.getName() + ".deep"); 
		oldFile.renameTo(new File(p.getLocation()	+ "/" + newName + ".deep"));
		return true;
	}

}
