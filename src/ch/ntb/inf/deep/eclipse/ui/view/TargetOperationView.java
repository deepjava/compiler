/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.eclipse.ui.view;



import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.DataItem;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.NamedConst;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.RegisterMap;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.model.OperationObject;
import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.mcdp.ide.uart.Uart0;

public class TargetOperationView extends ViewPart implements ICdescAndTypeConsts {
	public static final String ID = "ch.ntb.inf.deep.eclipse.ui.view.TargetOperationView";
	private TableViewer viewer;
	private OperationObject[] elements;
	private Action toChar;
	private Action toHex;
	private Action toDez;
	private Action toDouble;
	private IEclipsePreferences prefs;
	
	private static String KERNEL;
	private static String cmdAddrName = "cmdAddr";

	private String[] choice =new String[]{"", "Register", "Variable","Address", "TargetCMD"};

	private static Image UP = DeepPlugin.createImage("full/obj32/up.gif");
	private static Image DOWN = DeepPlugin.createImage("full/obj32/down.gif");
	
	
	public static final int tVoid = 0, tRef = 3, tBoolean = 4, tChar = 5, tFloat = 6,
	tDouble = 7, tByte = 8, tShort = 9, tInteger = 10, tLong = 11;

	static final byte slotSize = 4; // 4 bytes
		
	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if ((obj instanceof OperationObject)) {
				OperationObject op = (OperationObject)obj;
				if(index == 0){
					return choice[op.operation];
				}else if(index == 1){
					return op.description;
				}else if(index == 2){
					if(op.isReaded && !choice[op.operation].equals("")){
						switch(op.operation){
							case 1:
								switch(op.registerType){
								case Parser.sCR:
								case Parser.sFPSCR:
								case Parser.sMSR:
									return String.format("0x%04X",(short)op.value);
								case Parser.sGPR:
									if(op.representation == 1){//Hex
										return String.format("0x%08X",(int)op.value);
									}else{
										return String.format("%d",(int)op.value);
									}
								case Parser.sFPR:
									if(op.representation == 1){//Hex
										return String.format("0x%016X",op.value);
									}else{
										return String.format("%f",Double.longBitsToDouble(op.value));
									}
								case Parser.sSPR:
								case Parser.sIOR:
									switch(op.registerSize){
									case 1:
										return String.format("0x%02X",(byte)op.value);
									case 2:
										return String.format("0x%04X",(short)op.value);
									case 4:
										return String.format("0x%08X",(int)op.value);
									case 8:
										return String.format("0x%016X",op.value);
									default:
										return String.format("0x%08X",(int)op.value);
									}
								}
							case 2:
								switch (op.valueType){
									case tBoolean:
										return String.valueOf(op.value > 0);
									case tByte:
										if(op.representation == 1){//Hex
											return String.format("0x%02X",(byte)op.value);
										}
										return Byte.toString((byte)op.value);
									case tChar:
										if(op.representation == 1){//Hex
											return String.format("0x%04X",(short)op.value);
										}
										if(op.representation == 2){//Dez{
											return String.format("%d",(int)op.value);
										}
										return String.format("%c",((char)op.value));
									case tShort:
										if(op.representation == 1){//Hex
											return String.format("0x%04X",(short)op.value);
										}
										return String.format("%d",((short)op.value));
									case tInteger:
										if(op.representation == 1){//Hex
											return String.format("0x%08X",(int)op.value);
										}
										return String.format("%d",(int)op.value);
									case tFloat:
										if(op.representation == 1){//Hex
											return String.format("0x%08X",(int)op.value);
										}
										return String.format("%f",Float.intBitsToFloat((int)op.value));
									case tLong:
										if(op.representation == 1){//Hex
											return String.format("0x%016X",op.value);
										}
										return String.format("%d",op.value);
									case tDouble:
										if(op.representation == 1){//Hex
											return String.format("0x%016X",op.value);
										}
										return String.format("%f",Double.longBitsToDouble(op.value));
									case tRef:
										return String.format("0x%08X",(int)op.value);
									default:
										throw new RuntimeException("Should not happen");
								}
							case 3:
								return String.format("0x%08X",(int)op.value);
							case 4:
							case 5:
								return "";
							default:
								throw new RuntimeException("Should not happen");
						}
					}
				
				}else if(index == 3){
					switch(op.operation){
					case 1:
						if(op.isReaded && op.registerType == Parser.sIOR){
							return String.format("0x%08X", op.addr);
						}else{
							return "";
						}
					case 2:
						if(op.isReaded){
							return String.format("0x%08X", op.addr);
						}else{
							return "";
						}
					case 4:
						if(op.cmdSend){
							return String.format("0x%08X", op.addr);
						}else{
							return "";
						}
					case 5:
						return "";
					default :
						return "";
					}
				}else if(index == 4){
					return null;
				}else if(index == 5){
					return null;
				}else if(index == 6){
					return op.errorMsg;
				}
			}
			return "";
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if ((element instanceof OperationObject)) {
				OperationObject op = (OperationObject)element;
				if(op.operation != 0 && op.operation < 4 && columnIndex == 4){
					return UP;
				}
				if(op.operation != 0 && columnIndex == 5){
					return DOWN;
				}
			}
			return null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		prefs = new InstanceScope().getNode(DeepPlugin.PLUGIN_ID);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		viewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		String[] titels = { "Operation", "Descriptor", "Value","MemAddr" , "", "", "Error Message" };
		int[] bounds = { 100, 100, 100, 100, 18, 18, 250 };

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[0]);
		column.getColumn().setWidth(bounds[0]);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(false);
		column.setEditingSupport(new OperationEditingSupport(viewer));
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[1]);
		column.getColumn().setWidth(bounds[1]);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(false);
		column.setEditingSupport(new DescriptorEditingSupport(viewer));
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[2]);
		column.getColumn().setWidth(bounds[2]);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(false);
		column.setEditingSupport(new ValueEditingSupport(viewer));
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[3]);
		column.getColumn().setWidth(bounds[3]);
		column.getColumn().setResizable(false);
		column.getColumn().setMoveable(false);

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[4]);
		column.getColumn().setWidth(bounds[4]);
		column.getColumn().setResizable(false);
		column.getColumn().setMoveable(false);
		column.setEditingSupport(new RefreshEditingSupport(viewer));
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[5]);
		column.getColumn().setWidth(bounds[5]);
		column.getColumn().setResizable(false);
		column.getColumn().setMoveable(false);
		column.setEditingSupport(new DownloadEditingSupport(viewer));
		
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(titels[6]);
		column.getColumn().setWidth(bounds[6]);
		column.getColumn().setResizable(false);
		column.getColumn().setMoveable(false);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		viewer.setColumnProperties(titels);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		getSite().setSelectionProvider(viewer);
		
		String storedOperations = prefs.get("storedOperations", "");
		String[] vars = storedOperations.split(";");

		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		elements = new OperationObject[32];
		if(vars.length > 1){
			for(int i = 0; i < vars.length && i < elements.length; i++){
				String[] obj = vars[i].split(",");
				if(obj.length > 1){
					elements[i] = new OperationObject(Integer.decode(obj[0]),obj[1]);
				}else{
					elements[i] = new OperationObject();
				}
			}
		}else{
			for (int i = 0; i < 32; i++) {
				elements[i] = new OperationObject();
			}
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
				TargetOperationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager menu) {
		menu.add(toHex);
		menu.add(toDez);
		menu.add(toDouble);
		menu.add(toChar);
		// Other plug-ins can contribute there actions here
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void createActions() {
		toChar =  new Action(){
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof OperationObject){
					((OperationObject)obj).representation = 4;
				}
				viewer.refresh();
			}
		};
		toChar.setText("ToChar");
		toHex =  new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof OperationObject){
							((OperationObject)obj).representation = 1;
						}
						viewer.refresh();
					}
		};
		toHex.setText("ToHex");
		toDez = new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof OperationObject){
							((OperationObject)obj).representation = 2;
						}
						viewer.refresh();
					}
		};
		toDez.setText("ToDez");
		toDouble = new Action(){
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof OperationObject){
					((OperationObject)obj).representation = 3;
				}
				viewer.refresh();
			}
		};
		toDouble.setText("ToDouble");		
	}
	
	public class DescriptorEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public DescriptorEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((OperationObject)element).description;

		}

		@Override
		protected void setValue(Object element, Object value) {
			OperationObject op = (OperationObject)element;
			if(value == null){
				return;
			}
			op.errorMsg = "";
			String param = String.valueOf(value);
			if(choice[op.operation].equals("Register")){
				HString register = HString.getHString(param.toUpperCase());
				readFromRegister(op, register);				
			}else if(choice[op.operation].equals("Variable")){
				if(!param.equals("")){
					readVariable(op, param);
				}
			}else if(choice[op.operation].equals("Address")){
				try{
					if(param.length() > 0){
						//work around for problem when in hex-int-number the most significant bit is set;
						if(param.charAt(0) == '0' && param.length() > 9 && param.charAt(2) > '7'){
							String most = param.substring(2, 3);
							param = "0x0" + param.substring(3);
							op.addr = (Integer.parseInt(most,16) << 28) |Integer.decode(param);
						}else{
							op.addr  = Integer.decode(param);
						}
					}
				op.description = param;// do it first, so we need only one parameter for the functions
				readFromAddress(op);
				}catch (Exception e) {
				}				
				
			}else if(choice[op.operation].equals("TargetCMD")){
				sendCMD(op, param);
			}else if(param.equals("stirb!!!")){
				MessageDialog dialog = new MessageDialog( viewer.getControl().getShell(), "bye bye", null, "aaaaaaaaaaahhhhhhhh",  MessageDialog.ERROR, new String[] { "tot" }, 0);
		        dialog.open();
				System.exit(0);
			}
			saveView();
			viewer.refresh();
		}
	}
	
	public class ValueEditingSupport extends EditingSupport {
		
		private final TableViewer viewer;
		
		public ValueEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}
		
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
		
		@Override
		protected Object getValue(Object element) {
			OperationObject op = (OperationObject) element;
			switch (op.operation) {
			case 1:
				switch (op.registerType) {
				case Parser.sCR:
				case Parser.sFPSCR:
				case Parser.sMSR:
					return String.format("0x%04X", op.value);
				case Parser.sGPR:
					if (op.representation == 1) {// Hex
						return String.format("0x%08X", op.value);
					} else {
						return String.format("%d", (int) op.value);
					}
				case Parser.sFPR:
					if (op.representation == 1) {// Hex
						return String.format("0x%016X", op.value);
					} else {
						return String.format("%f", Double.longBitsToDouble(op.value));
					}
				case Parser.sSPR:
				case Parser.sIOR:
					switch (op.registerSize) {
					case 1:
						return String.format("0x%02X", op.value);
					case 2:
						return String.format("0x%04X", op.value);
					case 4:
						return String.format("0x%08X", op.value);
					case 8:
						return String.format("0x%016X", op.value);
					default:
						return String.format("0x%08X", op.value);
					}
				}
			case 2:
				switch (op.valueType) {
				case tBoolean:
					return String.valueOf(op.value > 0);
				case tByte:
					if (op.representation == 1) {// Hex
						return String.format("0x%02X", op.value);
					}
					return Byte.toString((byte) op.value);
				case tChar:
					if (op.representation == 1) {// Hex
						return String.format("0x%04X", op.value);
					}
					if (op.representation == 2) {// Dez{
						return String.format("%d", (int) op.value);
					}
					return String.format("%c", ((char) op.value));
				case tShort:
					if (op.representation == 1) {// Hex
						return String.format("0x%04X", op.value);
					}
					return String.format("%d", ((short) op.value));
				case tInteger:
					if (op.representation == 1) {// Hex
						return String.format("0x%08X", op.value);
					}
					return String.format("%d", (int) op.value);
				case tFloat:
					if (op.representation == 1) {// Hex
						return String.format("0x%08X", op.value);
					}
					return String.format("%f", Float.intBitsToFloat((int) op.value));
				case tLong:
					if (op.representation == 1) {// Hex
						return String.format("0x%016X", op.value);
					}
					return String.format("%d", op.value);
				case tDouble:
					if (op.representation == 1) {// Hex
						return String.format("0x%016X", op.value);
					}
					return String.format("%f", Double.longBitsToDouble(op.value));
				case tRef:
					return String.format("0x%08X", op.value);
				default:
					return String.format("%d", op.value);
				}
			case 3:
				return String.format("0x%08X", op.value);
			case 4:
			case 5:
				return "";
			default:
				throw new RuntimeException("Should not happen");
			}
	}
		

		@Override
		protected void setValue(Object element, Object value) {
			OperationObject op = (OperationObject)element;
			try{
				if(choice[op.operation].equals("Register")){
					if(((OperationObject)element).registerType != -1){
						if(((OperationObject)element).registerType == Parser.sFPR){
							op.value = Double.doubleToLongBits(Double.parseDouble(String.valueOf(value)));
						}else{
							op.value = Long.decode(String.valueOf(value));
						}
						setToRegister(op);
					}
				}else if(choice[op.operation].equals("Variable")){
					setVariable(op,String.valueOf(value));
				}else if(choice[op.operation].equals("Address")){
					try{
						op.value = Long.decode(String.valueOf(value));
						setToAddress(op);
					}catch (Exception e) {
					}
				}else if(choice[op.operation].equals("TargetCMD")){
					op.value = Long.decode(String.valueOf(value));
					
				}				
			}catch(Exception e){
			}
			viewer.refresh();
		}
	}
		
	public class OperationEditingSupport extends EditingSupport {

			private final TableViewer viewer;

			public OperationEditingSupport(TableViewer viewer) {
				super(viewer);
				this.viewer = viewer;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(viewer.getTable(), choice);
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				OperationObject op = (OperationObject) element;
				return op.operation;
			}

			@Override
			protected void setValue(Object element, Object value) {
				OperationObject op = (OperationObject)element;
				op.errorMsg = "";
				if((Integer)value < 0){
					op.operation = 0;
				}else{
					op.operation = (Integer)value;
					if(!choice[op.operation].equals("Register")){
						op.registerType = -1;
					}
				}
				if(op.operation == 0){//reset all
					op.addr = 0;
					op.cmdSend = false;
					op.description = "";
					op.isReaded = false;
					op.registerSize = 0;
					op.registerType = 0;
					op.representation = 0;
					op.value = 0;
					op.valueType = 0;
				}
				saveView();
				viewer.refresh();
			}
		}
	
	public class RefreshEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public RefreshEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);

		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			OperationObject op = (OperationObject)element;
			op.errorMsg = "";
			if(choice[op.operation].equals("Register")){
				if(((OperationObject)element).registerType != -1){
					readFromRegister(op, HString.getHString(op.description));						
				}
			}else if(choice[op.operation].equals("Variable")){
				readVariable(op,op.description);
			}else if(choice[op.operation].equals("Address")){
				readFromAddress(op);
			}
			viewer.refresh();
			return false;//TODO check this
		}

		@Override
		protected void setValue(Object element, Object value) {
			viewer.refresh();
		}
	}
	
	public class DownloadEditingSupport extends EditingSupport {
		
		private final TableViewer viewer;
		
		public DownloadEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
			
		}
		
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
		
		@Override
		protected Object getValue(Object element) {
			OperationObject op = (OperationObject)element;
			op.errorMsg = "";
				if(choice[op.operation].equals("Register")){
					if(((OperationObject)element).registerType != -1){
						setToRegister(op);						
					}
				}else if(choice[op.operation].equals("Variable")){
					setVariable(op,String.valueOf(op.value));
				}else if(choice[op.operation].equals("Address")){
					setToAddress(op);
				}else if(choice[op.operation].equals("TargetCMD")){
					sendCMD(op, op.description);					
				}				
			return false;
			
		}
		
		@Override
		protected void setValue(Object element, Object value) {
			viewer.refresh();
		}
	}
	
	private void sendCMD(OperationObject op, String param) {
		if(param.equals("")){
			return;
		}
		boolean wasFreezeAsserted;
		HString kernelName = Configuration.getKernelClassname();
		if(kernelName == null){
			op.errorMsg = "configuration isn't loaded";
			return;
		}
		KERNEL = kernelName.toString(); 
		String fullQualName = param;
		int lastDot = fullQualName.lastIndexOf(".");
		if(lastDot < 0){
			op.errorMsg = "invalid description";
			return;
		}
		String clazzName = fullQualName.substring(0, lastDot);
		clazzName = clazzName.replace('.', '/');
		String methName = fullQualName.substring(lastDot + 1);
		Class classList = (Class)Type.classList;
		if(classList == null){
			op.errorMsg = "system not builded";
			return;
		}
		Class clazz = (Class)classList.getItemByName(clazzName);
		Class kernel = (Class) classList.getItemByName(KERNEL);
		if (clazz != null && kernel != null) {
			int cmdAddr = ((DataItem) kernel.classFields.getItemByName(cmdAddrName)).address;
			Method meth = (Method) clazz.methods.getItemByName(methName);
			if (meth != null) {
				//Save address for display
				op.addr = meth.address;
				Downloader bdi = UsbMpc555Loader.getInstance();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try {
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					wasFreezeAsserted = bdi.isFreezeAsserted();
					if (!wasFreezeAsserted) {
						bdi.stopTarget();
					}
					bdi.setMem(cmdAddr, meth.address, 4);
					op.description = param;
					op.cmdSend = true;
					
					if (!wasFreezeAsserted) {
						bdi.startTarget();
					}
				} catch (DownloaderException e) {
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "method not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	
	private void readFromAddress(OperationObject op){
		boolean wasFreezeAsserted;
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			
			op.value = bdi.getMem(op.addr, 4);
			op.isReaded = true;
			
			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			op.errorMsg = "target not initialized";
		}
	}

	private void readVariable(OperationObject op, String fullQualName){
		boolean wasFreezeAsserted;
		int lastDot = fullQualName.lastIndexOf(".");
		if (lastDot == -1){
			op.errorMsg = "invalid description";
			return;
		}
		String clazzName = fullQualName.substring(0, lastDot);
		clazzName = clazzName.replace('.', '/');
		String varName = fullQualName.substring(lastDot + 1);
		if(Type.classList == null){
			op.errorMsg = "system not builded";
			return;
		}
		Class clazz = (Class)Type.classList.getItemByName(clazzName);
		if(clazz != null){
			if((DataItem)clazz.classFields == null){
				op.errorMsg = "no fields";
				return;
			}
			Item var = clazz.classFields.getItemByName(varName);
			if(var != null){
				//save address for display
				op.addr = var.address;
				
				if(var instanceof NamedConst) {
					var = ((NamedConst)var).getConstantItem();
					if(var.type == Type.wellKnownTypes[txFloat] || var.type == Type.wellKnownTypes[txDouble]) { // constant is in constant pool // TODO: Replace by Linker32.checkConstantPoolType()
						op.errorMsg = "Warning: field is a constant!";
						op.addr = var.address;
					}
					else {
						op.errorMsg = "Only constants of type float or double can be read!";
						return;
					}
				}
				
				Downloader bdi = UsbMpc555Loader.getInstance();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try{
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					
					wasFreezeAsserted = bdi.isFreezeAsserted();
					if(!wasFreezeAsserted){
						bdi.stopTarget();
					}
					if(((Type)var.type).sizeInBits <= 2 * slotSize ) { // 1 or 8 bit
						op.value = bdi.getMem(var.address, slotSize/4);
						if(((Type)var.type).sizeInBits == 1 ){
							op.valueType = tBoolean;
						}else{
							op.valueType = tByte;										
						}
					}else if(((Type)var.type).sizeInBits == 4 * slotSize){ // 16 bit
						op.value = bdi.getMem(var.address, slotSize/2);
						if(var.type == Type.wellKnownTypes[txChar]){
							op.valueType = tChar;
						}else{
							op.valueType = tShort;										
						}
					}else if(((Type)var.type).sizeInBits == 8 * slotSize){ // 32 bit
						op.value = bdi.getMem(var.address, slotSize);
						if(var.type == Type.wellKnownTypes[txInt]){
							op.valueType = tInteger;
						}else if(var.type == Type.wellKnownTypes[txFloat]){
							op.valueType = tFloat;										
						}else{
							op.valueType = tRef;
						}
					}else if(((Type)var.type).sizeInBits > 8 * slotSize) { // 64 bit
						op.value = bdi.getMem(var.address, slotSize);
						op.value = op.value << (8 * slotSize) | (bdi.getMem(var.address + slotSize, slotSize) & 0xffffffffL);
						if(var.type == Type.wellKnownTypes[txLong]){
							op.valueType = tLong;
						}else{
							op.valueType = tDouble;										
						}
					}
					if(!wasFreezeAsserted){
						bdi.startTarget();
					}
					op.isReaded = true;
					op.description = fullQualName;
				}catch(DownloaderException e){
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "field not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	
	private void readFromRegister(OperationObject op, HString register){		
		RegisterMap regMap = Configuration.getRegisterMap();
		boolean found = false;
		
		Register current = regMap.getCR();
		if(current != null && current.getName().equals(register)){
			op.registerSize = current.getSize();
			op.registerType = current.getType();
			op.addr = current.getAddress();
			op.description = register.toString();
			found = true;
		}
		
		current = regMap.getFpscr();
		if(!found && current != null && current.getName().equals(register)){
			op.registerSize = current.getSize();
			op.registerType = current.getType();
			op.addr = current.getAddress();
			op.description = register.toString();		
			found = true;
		}
		
		current = regMap.getMSR();
		if(!found && current != null && current.getName().equals(register)){
			op.registerSize = current.getSize();
			op.registerType = current.getType();
			op.addr = current.getAddress();
			op.description = register.toString();		
			found = true;
		}
		
		current = regMap.getGprRegister();
		while(!found && current != null){
			if(current.getName().equals(register)){
				op.registerSize = current.getSize();
				op.registerType = current.getType();
				op.addr = current.getAddress();
				op.description = register.toString();		
				found = true;
			}
			current = current.next;
		}
		
		current = regMap.getFprRegister();
		while(!found && current != null){
			if(current.getName().equals(register)){
				op.registerSize = current.getSize();
				op.registerType = current.getType();
				op.addr = current.getAddress();
				op.description = register.toString();		
				found = true;
			}
			current = current.next;
		}
		
		current = regMap.getSprRegister();
		while(!found && current != null){
			if(current.getName().equals(register)){
				op.registerSize = current.getSize();
				op.registerType = current.getType();
				op.addr = current.getAddress();
				op.description = register.toString();		
				found = true;
			}
			current = current.next;
		}
		
		current = regMap.getIorRegister();
		while(!found && current != null){
			if(current.getName().equals(register)){
				op.registerSize = current.getSize();
				op.registerType = current.getType();
				op.addr = current.getAddress();
				op.description = register.toString();		
				found = true;
			}
			current = current.next;
		}
		if(found){
			boolean wasFreezeAsserted;
			Downloader bdi = UsbMpc555Loader.getInstance();
			if(bdi == null){
				op.errorMsg = "target not connected";
				return;
			}
			try {
				if(!bdi.isConnected()){
					bdi.openConnection();
				}				
				wasFreezeAsserted = bdi.isFreezeAsserted();
				if (!wasFreezeAsserted) {
					bdi.stopTarget();
				}
				switch(op.registerType){
				case Parser.sCR:
					op.value = bdi.getCR();
					break;
				case Parser.sMSR:
					op.value = bdi.getMSR();
					break;
				case Parser.sFPSCR:
					op.value = bdi.getFPSCR();
					break;
				case Parser.sGPR:
					op.value = bdi.getGPR(op.addr);
					break;
				case Parser.sFPR:
					op.value = bdi.getFPR(op.addr);
					break;
				case Parser.sSPR:
					op.value = bdi.getSPR(op.addr);
					break;
				case Parser.sIOR:
					op.value = bdi.getMem(op.addr, op.registerSize);
					break;
				default:
				}
				op.isReaded = true;
				if (!wasFreezeAsserted) {
					bdi.startTarget();
				}
			} catch (DownloaderException e) {
				op.errorMsg = "target not initialized";
			}
		}else{
			op.errorMsg = "register not found";
		}
	}
	
	private void setToAddress(OperationObject op){
		boolean wasFreezeAsserted;
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			
			bdi.setMem(op.addr, (int)op.value, 4);
			
			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			op.errorMsg = "target not initialized";
		}
		
	}
	
	private void setVariable(OperationObject op, String value){
		boolean wasFreezeAsserted;
		int lastDot = op.description.lastIndexOf(".");
		if(lastDot < 0){
			op.errorMsg = "invalide descriptor";
			return;
		}
		String clazzName = op.description.substring(0, lastDot);
		clazzName = clazzName.replace('.', '/');
		String varName = op.description.substring(lastDot + 1);
		if(Type.classList == null){
			op.errorMsg = "system not builded";
		}
		Class clazz = (Class)Type.classList.getItemByName(clazzName);
		if(clazz != null){
			if(clazz.classFields == null){
				op.errorMsg = "no fields";
				return;
			}
			Item var = clazz.classFields.getItemByName(varName);
			if(var != null){
				
				if(var instanceof NamedConst) {
					op.errorMsg = "Constant field, value can't be changed!";
					return;
				}
				
				Downloader bdi = UsbMpc555Loader.getInstance();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try{
					long val = Long.decode(value);
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					wasFreezeAsserted = bdi.isFreezeAsserted();
					if(!wasFreezeAsserted){
						bdi.stopTarget();
					}
					if(((Type)var.type).sizeInBits <= 2 * slotSize ) {
						bdi.setMem(var.address,(int)(val & 0xFF), slotSize/4);
					}else if(((Type)var.type).sizeInBits == 4 * slotSize){
						bdi.setMem(var.address,(int)(val & 0xFFFF), slotSize/2);
					}else if(((Type)var.type).sizeInBits == 8 * slotSize){
						bdi.setMem(var.address,(int)(val & 0xFFFFFFFF), slotSize);
					}else if(((Type)var.type).sizeInBits > 8 * slotSize) {
						bdi.setMem(var.address,(int)((val >> 32) & 0xFFFFFFFF), slotSize);
						bdi.setMem(var.address + slotSize,(int)(val & 0xFFFFFFFF), slotSize);
					}
					op.value = val;
					if(!wasFreezeAsserted){
						bdi.startTarget();
					}
				}catch(DownloaderException e){
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "field not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	private void setToRegister(OperationObject op){

		boolean wasFreezeAsserted;
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			switch (op.registerType) {
			case Parser.sCR:
				bdi.setCR((int)op.value);
				break;
			case Parser.sGPR:
				bdi.setGPR(op.addr, (int)op.value);
				break;
			case Parser.sFPR:
				bdi.setFPR(op.addr,op.value);
				break;
			case Parser.sSPR:
				bdi.setSPR(op.addr, (int)op.value);
				break;
			case Parser.sIOR:
				bdi.setMem(op.addr, (int)op.value, op.registerSize);
				break;
			default:
			}

			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			op.errorMsg = "target not initialized";
		}

	}
	
	private void saveView(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < elements.length; i++){
			sb.append(elements[i].operation + "," + elements[i].description + ";");
		}
		prefs.put("storedOperations", sb.toString());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
		
	
}
