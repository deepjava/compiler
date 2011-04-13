package ch.ntb.inf.deep.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.ntb.inf.deep.linker.Linker32;

public class ClasslistAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private static boolean printMethods = false;
	private static boolean printFields = false;
	private static boolean printConstantFields = false;
	private static boolean printConstantBlock = false;
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		String actionID = action.getId();
		if(actionID == null) return;
		if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.PrintClasslistAction")){
			Linker32.printClassList(printMethods, printFields, printConstantFields, printConstantBlock);
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintMethod")){
			printMethods = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintFields")){
			printFields = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintConstFields")){
			printConstantFields = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintConstantBlock")){
			printConstantBlock = action.isChecked();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {	
	}

}
