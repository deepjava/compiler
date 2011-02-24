package ch.ntb.inf.deep.eclipse.ui.view;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;

import ch.ntb.inf.deep.DeepPlugin;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.DataItem;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;


public class TargetCMDView extends ViewPart implements ICdescAndTypeConsts {
	public static final String ID = "ch.ntb.inf.deep.view.TargetCMDView";
	private static final String KERNEL = "ch/ntb/inf/deep/runtime/mpc555/Kernel";
	private static final String cmdAddrName = "cmdAddr";
	private TableViewer viewer;
	private MethodCall[] elements;
	private Downloader bdi;
	private Action send;
	private IEclipsePreferences prefs;

	

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof MethodCall)) {
				return "";
			}
			return ((MethodCall)obj).fullQualifiedName;
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		prefs = new InstanceScope().getNode(DeepPlugin.PLUGIN_ID);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		String[] titels = { "Method to call"};
		int[] bounds = { 230 };
		for (int i = 0; i < titels.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titels[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			column.getColumn().setMoveable(false);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create the cell editors
		CellEditor[] editors = new CellEditor[1];
		editors[0] = new TextCellEditor(table);

		viewer.setColumnProperties(titels);
		viewer.setCellEditors(editors);
		viewer.setCellModifier(new TargetCmdCellModifier(viewer));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		
		String storedCalles = prefs.get("storedCalls", "");
		String[] calls = storedCalles.split(";");
		
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		elements = new MethodCall[32];

		for (int i = 0; i < calls.length; i++) {
			elements[i] = new MethodCall(calls[i]);
		}
		for (int i = calls.length; i < 32; i++) {
			elements[i] = new MethodCall("");
		}
		viewer.setInput(elements);

		createActions();
		hookContextMenu();	
	}
	

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TargetCMDView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(send);
		// Other plug-ins can contribute there actions here
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void createActions() {
		send = new Action(){
			public void run(){
				ISelection selection = viewer.getSelection();
				boolean wasFreezeAsserted;
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof MethodCall){
					int lastDot =((MethodCall)obj).fullQualifiedName.lastIndexOf(".");
					String clazzName = ((MethodCall)obj).fullQualifiedName.substring(0, lastDot);
					clazzName = clazzName.replace('.', '/');
					String methName = ((MethodCall)obj).fullQualifiedName.substring(lastDot + 1);
					Class clazz = (Class)Type.classList.getItemByName(clazzName);
					Class kernel = (Class)Type.classList.getItemByName(KERNEL);
					if(clazz != null && kernel != null){
						int cmdAddr = ((DataItem)kernel.classFields.getItemByName(cmdAddrName)).address;
						Method meth = (Method)clazz.methods.getItemByName(methName);
						if(meth != null){
							if(bdi == null){
								bdi = UsbMpc555Loader.getInstance();
							}
							try{
								wasFreezeAsserted = bdi.isFreezeAsserted();
								if(!wasFreezeAsserted){
									bdi.stopTarget();
								}
								bdi.setMem(cmdAddr, meth.address, 4);
								
								if(!wasFreezeAsserted){
									bdi.startTarget();
								}
							}catch(DownloaderException e){
								e.printStackTrace();
							}
						}
					}
				}
				viewer.refresh();
			}
		};
		send.setText("Send");
		
	}

	/**
	 * This class represents the cell modifier for the ReadVariable View
	 */

	class TargetCmdCellModifier implements ICellModifier {
		private Viewer viewer;

		public TargetCmdCellModifier(Viewer viewer) {
			this.viewer = viewer;
		}

		/**
		 * Returns whether the property can be modified
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @return boolean
		 */
		public boolean canModify(Object element, String property) {
			return true;
		}

		/**
		 * Returns the value for the property
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @return Object
		 */
		public Object getValue(Object element, String property) {
			MethodCall p = (MethodCall) element;
			if ("Method to call".equals(property))return p.fullQualifiedName;
			
			return null;
		}

		/**
		 * Modifies the element
		 * 
		 * @param element
		 *            the element
		 * @param property
		 *            the property
		 * @param value
		 *            the value
		 */
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item)
				element = ((Item) element).getData();

			
			if ("Method to call".equals(property)){
				((MethodCall)element).fullQualifiedName = (String) value;
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < elements.length; i++){
					sb.append(elements[i].fullQualifiedName + ";");
				}
				prefs.put("storedCalls", sb.toString());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
			// Force the viewer to refresh
			viewer.refresh();
		}
	}
	
	class MethodCall{
		public String fullQualifiedName;
		
		MethodCall(String fullQualifiedName){
			this.fullQualifiedName = fullQualifiedName;
		}
	}

}
