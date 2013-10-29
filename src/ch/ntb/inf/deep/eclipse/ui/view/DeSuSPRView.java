/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.eclipse.ui.view;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import ch.ntb.inf.deep.eclipse.ui.model.RegModel;
import ch.ntb.inf.deep.eclipse.ui.model.Register;
import ch.ntb.inf.deep.launcher.Launcher;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

/**
 * The view is connected to the model using a content provider.
 * <p>
 * It displays the contents of the Development Support SPRs
 * <p>
 */

public class DeSuSPRView extends ViewPart implements ISelectionListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ch.ntb.inf.deep.ui.DeSuSPRView";


	private TableViewer viewer;
	private Action toHex;
	private Action toDez;
	private Action toBin;
	private Action refresh;
	private Action suspend;
	private Action resume;
	private RegModel model;
	private final String helpContextId = "ch.ntb.inf.deep.ui.register.viewer";

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			Register dummy = new Register();
			Register[] regs = null;
			if (model != null) {
				regs = model.getMod(5);
			}
			if(model == null || model.getMod(5) == null) {
				// Defaul all is Zero
				regs = new Register[16];
				regs[0] = new Register("CMPA",0,0);
				regs[1] = new Register("CMPB",0,0);
				regs[2] = new Register("CMPC",0,0);
				regs[3] = new Register("CMPD",0,0);
				regs[4] = new Register("ECR",0,0);
				regs[5] = new Register("DER",0,0);
				regs[6] = new Register("COUNTA",0,0);
				regs[7] = new Register("COUNTB",0,0);
				regs[8] = new Register("CMPE",0,0);
				regs[9] = new Register("CMPF",0,0);
				regs[10] = new Register("CMPG",0,0);
				regs[11] = new Register("CMPH",0,0);
				regs[12] = new Register("LCTRL1",0,0);
				regs[13] = new Register("LCTRL2",0,0);
				regs[14] = new Register("ICTRL",0,0);
				regs[15] = new Register("BAR",0,0);
			}
			if(regs.length < 16){
				return regs;
			}
			//Group in blocks of 4 elements
			int regCount = 0;
			Register[] deSuSPR = new Register[19];
			for(int i = 0; i < deSuSPR.length; i++){
				if(i == 4 || i == 9 || i == 14){
					deSuSPR[i] = dummy;
				}else{
					deSuSPR[i] = regs[regCount];
					regCount++;
				}				
			}
			return deSuSPR;
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if(obj instanceof Register){
				switch(index){
				case 0:
					if(((Register)obj).name == null){
						return "";
					}
					return ((Register)obj).name;
				case 1:
					if(((Register)obj).name == null){
						return "";
					}if (((Register)obj).representation == 0){//BIN
						String value = Integer.toBinaryString(((Register)obj).value);
						String temp = "";
						
						// complete to 32 Bit
						int length = 32 - value.length();
						for (int y = 0; y < length; y++) {
							temp = temp + "0";
						}
						value = temp + value;

						// insert Spaces
						int z = 4;
						temp = value.substring(0, 4);
						for (int x = 0; x < 7; x++) {
							temp = temp + " " + value.substring(z, z + 4);
							z = z + 4;
						}
						return temp;
					}
					if (((Register)obj).representation == 1){//HEX
						return "0x"+Integer.toHexString(((Register)obj).value);
					}
					if (((Register)obj).representation == 2){//DEZ
						return Integer.toString(((Register)obj).value);
					}					
				default:
					throw new RuntimeException("Should not happen");
				}
			}
			return "";
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		// Create Viewer
		viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.FULL_SELECTION);
		//Create Columns
		String[] titels ={"Register","Value"};
		int[] bounds = { 60, 230};
		for(int i = 0;i < titels.length; i++){
			TableViewerColumn column = new TableViewerColumn(viewer,SWT.NONE);
			column.getColumn().setText(titels[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		//Set Providers after table init
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setSorter(null);
		//set input after init Providers
		update();//needs to init model
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), helpContextId);
		createActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DeSuSPRView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(toHex);
		manager.add(toBin);
		manager.add(toDez);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refresh);
		manager.add(suspend);
		manager.add(resume);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public RegModel getModel() {
		return model;
	}

	protected void createActions() {
		toHex =  new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof Register){
							((Register)obj).representation = 1;
						}
						viewer.refresh();
					}
		};
		toHex.setText("ToHex");
		toDez = new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof Register){
							((Register)obj).representation = 2;
						}
						viewer.refresh();
					}
		};
		toDez.setText("ToDez");
		toBin = new Action(){
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof Register){
					((Register)obj).representation = 0;
				}
				viewer.refresh();
			}
		};
		toBin.setText("ToBin");
		refresh = new Action(){
			public void run(){
				update();
				viewer.refresh();
			}
		};
		refresh.setText("Refresh");
		ImageDescriptor img = ImageDescriptor.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_REDO));
		refresh.setImageDescriptor(img);
		suspend = new Action(){
			public void run(){
				TargetConnection bdi = Launcher.getTargetConnection();
				if (bdi == null)return;
				try {
					if(!bdi.isConnected()){//reopen
						bdi.openConnection();
					}
					if(bdi.getTargetState() != TargetConnection.stateDebug){
						bdi.stopTarget();
					}
				} catch (TargetConnectionException e) {
					e.printStackTrace();
				}
				update();
				viewer.refresh();
			}
		};
		suspend.setText("Suspend");
		img = ImageDescriptor.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_STOP));
		suspend.setImageDescriptor(img);
		resume = new Action(){
			public void run(){
				TargetConnection bdi = Launcher.getTargetConnection();
				if (bdi == null)return;
				try {
					if(!bdi.isConnected()){//reopen
						bdi.openConnection();
					}
					if(bdi.getTargetState() != TargetConnection.stateDebug){
						bdi.startTarget();
					}
				} catch (TargetConnectionException e) {
					e.printStackTrace();
				}
			}
		};
		resume.setText("Resume");
		img = ImageDescriptor.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
		resume.setImageDescriptor(img);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	private synchronized void update() {
		if (model == null) {
			model = RegModel.getInstance();
		}else{
			model.updateDeSuSPRMod();
		}
		if(model.getMod(5) != null){
			viewer.setInput(model);
			viewer.getControl().setEnabled(true);
			viewer.refresh();
		}
	}

	public Viewer getViewer() {
		return viewer;
	}

	@Override
	public void dispose() {
		model.clearMod(5);
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		super.dispose();
	}
}
