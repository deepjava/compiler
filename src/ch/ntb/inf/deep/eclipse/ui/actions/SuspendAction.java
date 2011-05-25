package ch.ntb.inf.deep.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;

public class SuspendAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
    public static final String ID = "ch.ntb.inf.deep.eclipse.ui.action.SuspendAction";
	
	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		UsbMpc555Loader bdi = UsbMpc555Loader.getInstance();
		
		try {
		if(!bdi.isConnected()){//reopen
			bdi.openConnection();
		}
		if(!bdi.isFreezeAsserted()){
			bdi.stopTarget();
		}
		} catch (DownloaderException e) {
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

}
