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
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.ConstField;
import ch.ntb.inf.deep.classItems.RefType;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.CPU;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.model.TargetOpObject;
import ch.ntb.inf.deep.launcher.Launcher;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class TargetOperationView extends ViewPart implements ICdescAndTypeConsts {
	public static final String ID = "ch.ntb.inf.deep.eclipse.ui.view.TargetOperationView";
	private TableViewer viewer;
	private TargetOpObject[] elements;
	private Action toChar;
	private Action toHex;
	private Action toDez;
	private Action toDouble;
	private IEclipsePreferences prefs;
	
	private static String cmdAddrName = "cmdAddr";

	private String[] choice = new String[]{"", "Register", "Variable","Address", "TargetCMD"};

	private static Image UP = DeepPlugin.createImage("full/obj32/up.gif");
	private static Image DOWN = DeepPlugin.createImage("full/obj32/down.gif");
	
	
	public static final int tVoid = 0, tRef = 3, tBoolean = 4, tChar = 5, tFloat = 6,
	tDouble = 7, tByte = 8, tShort = 9, tInteger = 10, tLong = 11;

	static final byte slotSize = 4; // 4 bytes
		
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if ((obj instanceof TargetOpObject)) {
				TargetOpObject op = (TargetOpObject)obj;
				if(index == 0){
					return choice[op.operation];
				}else if(index == 1){
					return op.description;
				}else if(index == 2){
					if(op.isRead && !choice[op.operation].equals("")){
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
						if(op.isRead && op.registerType == Parser.sIOR){
							return String.format("0x%08X", op.addr);
						}else{
							return "";
						}
					case 2:
						if(op.isRead){
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
			if ((element instanceof TargetOpObject)) {
				TargetOpObject op = (TargetOpObject)element;
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
		prefs = InstanceScope.INSTANCE.getNode(DeepPlugin.PLUGIN_ID);
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
		
		String storedOperations = prefs.get("storedTargetOperations", "");
		String[] vars = storedOperations.split(";");

		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		elements = new TargetOpObject[32];
		if(vars.length > 1){
			for(int i = 0; i < vars.length && i < elements.length; i++){
				String[] obj = vars[i].split(",");
				if(obj.length > 1){
					elements[i] = new TargetOpObject(Integer.decode(obj[0]),obj[1]);
				}else{
					elements[i] = new TargetOpObject();
				}
			}
		}else{
			for (int i = 0; i < 32; i++) {
				elements[i] = new TargetOpObject();
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
				if(obj instanceof TargetOpObject){
					((TargetOpObject)obj).representation = 4;
				}
				viewer.refresh();
			}
		};
		toChar.setText("ToChar");
		toHex =  new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof TargetOpObject){
							((TargetOpObject)obj).representation = 1;
						}
						viewer.refresh();
					}
		};
		toHex.setText("ToHex");
		toDez = new Action(){
					public void run() {
						ISelection selection = viewer.getSelection();
						Object obj = ((IStructuredSelection) selection).getFirstElement();
						if(obj instanceof TargetOpObject){
							((TargetOpObject)obj).representation = 2;
						}
						viewer.refresh();
					}
		};
		toDez.setText("ToDez");
		toDouble = new Action(){
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if(obj instanceof TargetOpObject){
					((TargetOpObject)obj).representation = 3;
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
			return ((TargetOpObject)element).description;

		}

		@Override
		protected void setValue(Object element, Object value) {
			TargetOpObject op = (TargetOpObject)element;
			if(value == null){
				return;
			}
			op.errorMsg = "";
			String param = String.valueOf(value);
			if(choice[op.operation].equals("Register")){
				HString register = HString.getRegisteredHString(param.toUpperCase());
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
			TargetOpObject op = (TargetOpObject) element;
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
			TargetOpObject op = (TargetOpObject)element;
			try{
				if(choice[op.operation].equals("Register")){
					if(((TargetOpObject)element).registerType != -1){
						if(((TargetOpObject)element).registerType == Parser.sFPR){
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
				TargetOpObject op = (TargetOpObject) element;
				return op.operation;
			}

			@Override
			protected void setValue(Object element, Object value) {
				TargetOpObject op = (TargetOpObject)element;
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
					op.isRead = false;
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
			TargetOpObject op = (TargetOpObject)element;
			op.errorMsg = "";
			if(choice[op.operation].equals("Register")){
				if(((TargetOpObject)element).registerType != -1){
					readFromRegister(op, HString.getRegisteredHString(op.description));						
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
			TargetOpObject op = (TargetOpObject)element;
			op.errorMsg = "";
				if(choice[op.operation].equals("Register")){
					if(((TargetOpObject)element).registerType != -1){
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
	
	private void sendCMD(TargetOpObject op, String fullQualName) {
		if (fullQualName.equals("")) return;
		if (Configuration.getOS() == null) {
			op.errorMsg = "configuration isn't loaded";
			return;
		}
		Class kernel = Configuration.getOS().kernelClass;

		fullQualName = fullQualName.replace('.', '/');
		fullQualName = fullQualName.replace('\\', '/');
		int lastDot = fullQualName.lastIndexOf("/");
		if (lastDot == -1) {
			op.errorMsg = "specify package.class.command";
			return;
		}
		String clazzName = fullQualName.substring(0, lastDot);
		String cmdName = fullQualName.substring(lastDot + 1);
		op.description = fullQualName;

		Class classList = (Class)RefType.refTypeList;
		if (classList == null) {
			op.errorMsg = "system not built";
			return;
		}
		Class clazz = (Class)classList.getItemByName(clazzName);
		if (clazz != null && kernel != null) {
			int cmdAddr = ((Field) kernel.classFields.getItemByName(cmdAddrName)).address;
			Method meth = (Method) clazz.methods.getItemByName(cmdName);
			if (meth != null) {
				//Save address for display
				op.addr = meth.address;
				TargetConnection bdi = Launcher.getTargetConnection();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try {
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					boolean wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
					if (!wasFreezeAsserted) {
						bdi.stopTarget();
					}
					bdi.writeWord(cmdAddr, meth.address);
					op.cmdSend = true;
					
					if (!wasFreezeAsserted) {
						bdi.startTarget();
					}
				} catch (TargetConnectionException e) {
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "method not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	
	private void readFromAddress(TargetOpObject op){
		boolean wasFreezeAsserted;
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			
			op.value = bdi.readWord(op.addr);
			op.isRead = true;
			
			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			op.errorMsg = "target not initialized";
		}
	}

	private void readVariable(TargetOpObject op, String fullQualName){
		fullQualName = fullQualName.replace('.', '/');
		fullQualName = fullQualName.replace('\\', '/');
		int lastDot = fullQualName.lastIndexOf("/");
		if (lastDot == -1) {
			op.errorMsg = "specify package.class.varName";
			return;
		}
		String clazzName = fullQualName.substring(0, lastDot);
		String varName = fullQualName.substring(lastDot + 1);
		op.description = fullQualName;
		if(RefType.refTypeList == null){
			op.errorMsg = "system not builded";
			return;
		}
		Class clazz = (Class)RefType.refTypeList.getItemByName(clazzName);
		if(clazz != null){
			if((Field)clazz.classFields == null){
				op.errorMsg = "no fields";
				return;
			}
			Item var = clazz.classFields.getItemByName(varName);
			if(var != null){
				//save address for display
				op.addr = var.address;
				
				if(var instanceof ConstField) {
					var = ((ConstField)var).getConstantItem();
					if(var.type == Type.wellKnownTypes[txFloat] || var.type == Type.wellKnownTypes[txDouble]) { // constant is in constant pool // TODO: Replace by Linker32.checkConstantPoolType()
						op.errorMsg = "Warning: field is a constant!";
						op.addr = var.address;
					}
					else {
						op.errorMsg = "Only constants of type float or double can be read!";
						return;
					}
				}
				
				TargetConnection bdi = Launcher.getTargetConnection();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try{
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					
					boolean wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
					if(!wasFreezeAsserted){
						bdi.stopTarget();
					}
					if(((Type)var.type).sizeInBits <= 2 * slotSize ) { // 1 or 8 bit
						op.value = bdi.readByte(var.address);
						if(((Type)var.type).sizeInBits == 1 ){
							op.valueType = tBoolean;
						}else{
							op.valueType = tByte;										
						}
					}else if(((Type)var.type).sizeInBits == 4 * slotSize){ // 16 bit
						op.value = bdi.readWord(var.address); // TDOO mask?
						if(var.type == Type.wellKnownTypes[txChar]){
							op.valueType = tChar;
						}else{
							op.valueType = tShort;										
						}
					}else if(((Type)var.type).sizeInBits == 8 * slotSize){ // 32 bit
						op.value = bdi.readWord(var.address);
						if(var.type == Type.wellKnownTypes[txInt]){
							op.valueType = tInteger;
						}else if(var.type == Type.wellKnownTypes[txFloat]){
							op.valueType = tFloat;										
						}else{
							op.valueType = tRef;
						}
					}else if(((Type)var.type).sizeInBits > 8 * slotSize) { // 64 bit
						op.value = bdi.readWord(var.address);
						op.value = op.value << (8 * slotSize) | (bdi.readWord(var.address + slotSize) & 0xffffffffL);
						if(var.type == Type.wellKnownTypes[txLong]){
							op.valueType = tLong;
						}else{
							op.valueType = tDouble;										
						}
					}
					if(!wasFreezeAsserted){
						bdi.startTarget();
					}
					op.isRead = true;
					op.description = fullQualName;
				}catch(TargetConnectionException e){
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "field not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	
	private void readFromRegister(TargetOpObject op, HString registerName) {
		boolean found = false;
		if (Configuration.getBoard() == null){
			op.errorMsg = "no configuration loaded";
			return;
		}
		CPU cpu = Configuration.getBoard().cpu;
		Register reg = null;
		if (cpu != null) reg = (Register) cpu.regs.getItemByName(registerName); 
		if (reg != null) {
			op.registerSize = reg.size;
			op.registerType = reg.regType;
			op.addr = reg.address;
			op.description = registerName.toString();
			found = true;
		}
		
		if (found) {
			boolean wasFreezeAsserted;
			TargetConnection bdi = Launcher.getTargetConnection();
			if (bdi == null){
				op.errorMsg = "target not connected";
				return;
			}
			try {
				if(!bdi.isConnected()){
					bdi.openConnection();
				}				
				wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
				if (!wasFreezeAsserted) {
					bdi.stopTarget();
				}
				switch (op.registerType) {
				case Parser.sCR:
					op.value = bdi.getRegisterValue("CR");
					break;
				case Parser.sMSR:
					op.value = bdi.getRegisterValue("MSR");
					break;
				case Parser.sFPSCR:
					op.value = bdi.getRegisterValue("FPSCR");
					break;
				case Parser.sGPR:
					op.value = bdi.getGprValue(op.addr);
					break;
				case Parser.sFPR:
					op.value = bdi.getFprValue(op.addr);
					break;
				case Parser.sSPR:
					op.value = bdi.getSprValue(op.addr);
					break;
				case Parser.sIOR:
					switch (op.registerSize) {
					case 1:
						op.value = bdi.readByte(op.addr);
						break;
					case 2:
						op.value = bdi.readHalfWord(op.addr);
						break;
					default:
						op.value = bdi.readWord(op.addr);
						break;
					}
					break;
				default:
				}
				op.isRead = true;
				if (!wasFreezeAsserted) {
					bdi.startTarget();
				}
			} catch (TargetConnectionException e) {
				op.errorMsg = "target not initialized";
			}
		}else{
			op.description = registerName.toString();
			op.errorMsg = "register not found";
		}
	}
	
	private void setToAddress(TargetOpObject op){
		boolean wasFreezeAsserted;
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			
			bdi.writeWord(op.addr, (int)op.value);
			
			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			op.errorMsg = "target not initialized";
		}
		
	}
	
	private void setVariable(TargetOpObject op, String value){
		boolean wasFreezeAsserted;
		String desc = op.description.replace('.', '/');
		int lastDot = desc.lastIndexOf("/");
		if(lastDot < 0){
			op.errorMsg = "invalid descriptor";
			return;
		}
		String clazzName = desc.substring(0, lastDot);
		clazzName = clazzName.replace('.', '/');
		String varName = desc.substring(lastDot + 1);
		if(RefType.refTypeList == null){
			op.errorMsg = "system not built";
		}
		Class clazz = (Class)RefType.refTypeList.getItemByName(clazzName);
		if(clazz != null){
			if(clazz.classFields == null){
				op.errorMsg = "no fields";
				return;
			}
			Item var = clazz.classFields.getItemByName(varName);
			if(var != null){
				
				if(var instanceof ConstField) {
					op.errorMsg = "Constant field, value can't be changed!";
					return;
				}
				
				TargetConnection bdi = Launcher.getTargetConnection();
				if(bdi == null){
					op.errorMsg = "target not connected";
					return;
				}
				try{
					long val = Long.decode(value);
					if(!bdi.isConnected()){
						bdi.openConnection();
					}
					wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
					if(!wasFreezeAsserted){
						bdi.stopTarget();
					}
					if(((Type)var.type).sizeInBits <= 2 * slotSize ) {
						bdi.writeByte(var.address,(byte)(val & 0xFF));
					}else if(((Type)var.type).sizeInBits == 4 * slotSize){
						bdi.writeHalfWord(var.address,(short)(val & 0xFFFF));
					}else if(((Type)var.type).sizeInBits == 8 * slotSize){
						bdi.writeWord(var.address,(int)(val & 0xFFFFFFFF));
					}else if(((Type)var.type).sizeInBits > 8 * slotSize) {
						bdi.writeWord(var.address,(int)((val >> 32) & 0xFFFFFFFF));
						bdi.writeWord(var.address + slotSize,(int)(val & 0xFFFFFFFF));
					}
					op.value = val;
					if(!wasFreezeAsserted){
						bdi.startTarget();
					}
				}catch(TargetConnectionException e){
					op.errorMsg = "target not initialized";
				}
			}else{
				op.errorMsg = "field not found";
			}
		}else{
			op.errorMsg = "class not found";
		}
	}
	
	private void setToRegister(TargetOpObject op){

		boolean wasFreezeAsserted;
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			op.errorMsg = "target not connected";
			return;
		}
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) {
				bdi.stopTarget();
			}
			switch (op.registerType) {
			case Parser.sCR:
				bdi.setRegisterValue("CR", (int)op.value);
				break;
			case Parser.sGPR:
				bdi.setGprValue(op.addr, (int)op.value);
				break;
			case Parser.sFPR:
				bdi.setFprValue(op.addr,op.value);
				break;
			case Parser.sSPR:
				bdi.setSprValue(op.addr, (int)op.value);
				break;
			case Parser.sIOR:
				switch (op.registerSize) {
				case 1:
					bdi.writeByte(op.addr, (byte)op.value);
					break;
				case 2:
					bdi.writeHalfWord(op.addr, (short)op.value);
					break;
				default:
					bdi.writeWord(op.addr, (int)op.value);
					break;
				}
				break;
			default:
			}

			if (!wasFreezeAsserted) {
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			op.errorMsg = "target not initialized";
		}

	}
	
	private void saveView(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < elements.length; i++){
			sb.append(elements[i].operation + "," + elements[i].description + ";");
		}
		prefs.put("storedTargetOperations", sb.toString());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
		
}
