package ch.ntb.inf.deep.ui.view;

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
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.deep.ui.model.ReadVariableElement;
import ch.ntb.inf.deep.ui.view.TargetCMDView.MethodCall;

public class ReadVariableView extends ViewPart implements ICdescAndTypeConsts {
	public static final String ID = "ch.ntb.inf.deep.view.ReadVariableView";
	private TableViewer viewer;
	private ReadVariableElement[] elements;
	private Downloader bdi;
	private Action toHex;
	private Action toDez;
	private Action toDouble;
	private Action read;
	private IEclipsePreferences prefs;

	static final byte slotSize = 4; // 4 bytes
	static {
		assert (slotSize & (slotSize - 1)) == 0; // assert: slotSize == power of
													// 2
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof ReadVariableElement)) {
				return "";
			}
			switch (index) {
			case 0:
				return ((ReadVariableElement) obj).fullQualifiedName;
			case 1:
				if (!((ReadVariableElement) obj).isReaded) {
					return "";
				}
				switch (((ReadVariableElement) obj).representation) {
				case 1:
					return "0x" + Integer.toHexString((int) ((ReadVariableElement) obj).result);
				case 2:
					return Integer.toString((int) ((ReadVariableElement) obj).result);
				case 3:
					return Double.toString(Double.longBitsToDouble(((ReadVariableElement) obj).result));
				default:
					return Integer.toString((int) ((ReadVariableElement) obj).result);
				}

			default:
				throw new RuntimeException("Should not happen");
			}
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
		String[] titels = { "Variable to read", "Result" };
		int[] bounds = { 230, 100 };
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
		viewer.setCellModifier(new ReadVarCellModifier(viewer));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		
		String storedVariables = prefs.get("storedVars", "");
		String[] vars = storedVariables.split(";");
		
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		elements = new ReadVariableElement[32];
		for (int i = 0; i < vars.length; i++) {
			elements[i] = new ReadVariableElement(vars[i]);
		}
		for (int i = vars.length; i < 32; i++) {
			elements[i] = new ReadVariableElement("");
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
				ReadVariableView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(read);
		menu.add(toHex);
		menu.add(toDez);
		menu.add(toDouble);
		// Other plug-ins can contribute there actions here
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void createActions() {
		toHex =  new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof ReadVariableElement){
							((ReadVariableElement)obj).representation = 1;
						}
						viewer.refresh();
					}
		};
		toHex.setText("ToHex");
		toDez = new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof ReadVariableElement){
							((ReadVariableElement)obj).representation = 2;
						}
						viewer.refresh();
					}
		};
		toDez.setText("ToDez");
		toDouble = new Action(){
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof ReadVariableElement){
					((ReadVariableElement)obj).representation = 3;
				}
				viewer.refresh();
			}
		};
		toDouble.setText("ToDouble");
		read = new Action(){
			public void run(){
				ISelection selection = viewer.getSelection();
				boolean wasFreezeAsserted;
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof ReadVariableElement){
					int lastDot =((ReadVariableElement)obj).fullQualifiedName.lastIndexOf(".");
					String clazzName = ((ReadVariableElement)obj).fullQualifiedName.substring(0, lastDot);
					clazzName = clazzName.replace('.', '/');
					String varName = ((ReadVariableElement)obj).fullQualifiedName.substring(lastDot + 1);
					Class clazz = (Class)Type.classList.getItemByName(clazzName);
					if(clazz != null){
						DataItem var = (DataItem)clazz.classFields.getItemByName(varName);
						if(var != null){
							if(bdi == null){
								bdi = UsbMpc555Loader.getInstance();
							}
							try{
								wasFreezeAsserted = bdi.isFreezeAsserted();
								if(!wasFreezeAsserted){
									bdi.stopTarget();
								}
								((ReadVariableElement)obj).result = bdi.getMem(var.address, slotSize);
								System.out.println("High: " +Long.toHexString(((ReadVariableElement)obj).result));
								if(((Type)var.type).sizeInBits > 8 * slotSize) {
									((ReadVariableElement)obj).result = (((ReadVariableElement)obj).result << (8 * slotSize)) | bdi.getMem(var.address + slotSize, slotSize);
								}
								if(!wasFreezeAsserted){
									bdi.startTarget();
								}
								((ReadVariableElement)obj).isReaded = true;
							}catch(DownloaderException e){
								e.printStackTrace();
							}
						}
					}
				}
				viewer.refresh();
			}
		};
		read.setText("Read");
		
	}

	/**
	 * This class represents the cell modifier for the ReadVariable View
	 */

	class ReadVarCellModifier implements ICellModifier {
		private Viewer viewer;

		public ReadVarCellModifier(Viewer viewer) {
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
			if (property.equals("Variable to read")) {
				return true;
			}
			return false;
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
			ReadVariableElement p = (ReadVariableElement) element;
			if ("Variable to read".equals(property))
				return p.fullQualifiedName;
			else if ("Result".equals(property))
				return Long.toString(p.result);
			else
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

			ReadVariableElement p = (ReadVariableElement) element;
			if ("Variable to read".equals(property)){
				p.setFullQualifiedName((String) value);
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < elements.length; i++){
					sb.append(elements[i].fullQualifiedName + ";");
				}
				prefs.put("storedVars", sb.toString());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
			else if ("Result".equals(property))
				p.setResult(((Integer) value).intValue());

			// Force the viewer to refresh
			viewer.refresh();
		}
	}

}
