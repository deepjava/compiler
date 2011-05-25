package ch.ntb.inf.deep.eclipse.ui.view;

import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;

public class USBLog extends ViewPart {
	public static final String ID = "ch.ntb.inf.deep.ui.view.USBLog";
	private IOConsole log;
	private Action clear;
	private USBLogWriter writer;
	private TextViewer viewer;
	protected OutputStream out;
	
	@Override
	public void createPartControl(Composite parent) {
		//Needs only to connect to the device
		UsbMpc555Loader.getInstance();
		
		//get Display
		Display d =parent.getShell().getDisplay();
		
		//Create view
		viewer = new TextViewer(parent, SWT.WRAP | SWT.V_SCROLL | SWT.SCROLL_PAGE);
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(viewerData);
		
		if(d != null){
			FontData defaultFont = new FontData("Courier", 10, SWT.NORMAL);
			Font font = new Font(d, defaultFont);
			viewer.getControl().setFont(font);
		}
		
		viewer.setEditable(false);
		
		
		log = new IOConsole("USBLog", null);
		out = log.newOutputStream();
		viewer.setDocument(log.getDocument());
		
		writer = new USBLogWriter("Uart0", out);
		writer.start();
		
		//create actions and hook to the toolbar
		createAction();
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(clear);
		
	}
	
	protected void createAction(){
		clear = new Action(){
			public void run(){
				log.clearConsole();
			}
		};
		clear.setText("Clear Log");
		ImageDescriptor img = ImageDescriptor.createFromImage(DeepPlugin.createImage("full/obj16/Icons-mini-note_delete.gif"));
		clear.setImageDescriptor(img);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
	public void dispose(){
		writer.setRunning(false);
		super.dispose();
	}
	
}
