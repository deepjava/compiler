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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cg.Code32;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ClassMember;
import ch.ntb.inf.deep.classItems.ConstField;
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.RefType;
import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.MemMap;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.linker.FixedValueEntry;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;

public class ClassTreeView extends ViewPart implements ISelectionChangedListener, ICclassFileConsts {
	public static final String ID = "ch.ntb.inf.deep.eclipse.ui.view.ClassTreeView";
	private TreeViewer classTreeViewer;
	private TextViewer textViewer;
	private Action refresh;
	
	private Board b;
	
	class ClassTreeLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if(element instanceof Item){
				if(element instanceof Method){
					return ((Method)element).name.toString() + ((Method)element).methDescriptor.toString();
				}
				if (element instanceof Board) return "Memory Map";					
				if (element instanceof MemMap) return ((MemMap)element).name.toString();					
				if (element instanceof Device) return ((Device)element).name.toString();
				if (element instanceof Segment) return ((Segment)element).name.toString();
				return ((Item)element).name.toString();
			}else{
				if(element instanceof RootElement)return ((RootElement)element).name.toString();
				if(element instanceof CFG)return "CFG";
				if(element instanceof SSA)return "SSA";
				if(element instanceof Code32)return "MachineCode";
				if(element instanceof String)return (String) element;
				if(element instanceof SystemTableElement)return "System Table";
				if(element instanceof SubroutineElement)return "Subroutines";
				if(element instanceof SubroutineEntry)return ((SubroutineEntry)element).name.toString();
			}
			
			if(element instanceof String)return (String) element;

			if(Configuration.getBoard() == null) return "Memory Map";
			return "not listed";
		}
	}

	class ClassTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeInput) {
				System.out.println("treeinput");
			}
			
			if (parent instanceof Board) {
				Board b = (Board)parent;
				if (b.memorymap == null || b.cpu.memorymap == null) return new Object[]{"No memory map loaded"};
				MemMap[] maps = new MemMap[2];
				maps[0] = b.memorymap;
				maps[1] = b.cpu.memorymap;
				return maps;
			}			
			if (parent instanceof MemMap) {
				MemMap memMap = (MemMap)parent;
				if (memMap.devs == null) return new Object[]{"No devices loaded"};
				Device[] devices = new Device[memMap.getNofDevices()];
				Device dev = memMap.devs;
				for (int i = 0; i < devices.length && dev != null; i++) {
					devices[i] = dev;
					dev = (Device)dev.next;
				}			
				return devices;
			}			
			if (parent instanceof Device){
				Segment segs =((Device)parent).segments;
				Segment current = segs;
				int count;
				for (count = 0; current != null; count++)current = (Segment)current.next;
				if (count > 0){
					Segment seg[] = new Segment[count];
					for (int i = 0; i < seg.length && segs != null; i++){
						seg[i] = segs;
						segs = (Segment)segs.next;
					}
					return seg;
				}				
			}
			if(parent instanceof Segment){
				Segment segs =((Device)parent).segments;
				Segment current = segs;
				int count;
				for(count = 0; current != null; count++)current = (Segment)current.next;
				if(count > 0){
					Segment seg[] = new Segment[count];
					for(int i = 0; i < seg.length && segs != null; i++){
						seg[i] = segs;
						segs = (Segment)segs.next;
					}
					return seg;
				}				
			}
			
			Object[] item = null;
			if (parent instanceof Class) {
				Class clazz = (Class)parent;
				int nofChildren = 0;
				//determine number of children
				//check if methods exists
				if(clazz.nofMethods > 0){
					nofChildren++;
				}
				//check if classFields exists
				if(clazz.nofClassFields > 0){
					nofChildren++;
				}
				
				//check if constFields exists
				if(clazz.nofConstFields > 0){
					nofChildren++;
				}
				
				//create array for children
				item = new Object[nofChildren];
				
				//fill array
				int index = 0;
				//add constFields if they exists
				if(clazz.nofConstFields > 0){
					item[index++] = new ClassChild(HString.getRegisteredHString("ConstFields"), clazz, clazz.constFields);
				}
				//add classFields if they exists
				if(clazz.nofClassFields > 0){
					item[index++] = new ClassChild(HString.getRegisteredHString("ClassFields"), clazz, clazz.classFields);
				}
				//add methods if they exists
				if(clazz.nofMethods > 0){					
					item[index++] = new ClassChild(HString.getRegisteredHString("Methods"), clazz, clazz.methods);				
				}
				return item;
			}
			
			if (parent instanceof SubroutineElement) {
				int index = 0;
				Method m = Method.compSpecSubroutines;
				SubroutineEntry[] arrSE = new SubroutineEntry[32];
				while(m != null) {
					SubroutineEntry se;
					String data = "";
					data += "Name: "+m.name + "\tOffset: "+m.offset+"\tAddress: "+m.address+"\r\n";
					data += m.machineCode.toString() + "\r\n";
					se = new SubroutineEntry(m.name.toString(), data);
					arrSE[index] = se;
					m = (Method)m.next;
					index++;
				}
				item = new Object[index];
				index = 0;
				while(arrSE[index] != null) {
					item[index] = arrSE[index];
					index++;
				}

				return item;
			}
			
			if (parent instanceof Method && (((Item)parent).accAndPropFlags & (1<<dpfSynthetic)) == 0) {
				Method meth = (Method)parent;
				//every method have 3 children: cfg, ssa and machineCode
				//create array for children
				item = new Object[3];
				
				//fill array
				item[0] = meth.cfg;
				item[1] = meth.ssa;
				item[2] = meth.machineCode;
				
				return item;
			}
			if (parent instanceof ClassChild) {
				int index = 0;
				Class clazz = (Class)((ClassChild)parent).owner;
				Item child;
				if(((ClassChild)parent).name.equals(HString.getHString("ConstFields"))){
					item = new Object[clazz.nofConstFields];
					child = clazz.constFields;
					while(child != null && index < item.length){
						item[index++] = child;
						child = child.next;
					}
					return item;
				}
				if(((ClassChild)parent).name.equals(HString.getHString("ClassFields"))){
					item = new Object[clazz.nofClassFields];
					child = clazz.classFields;
					while(child != null && index < item.length){
						item[index++] = child;
						child = child.next;
					}
					return item;
				}
				if(((ClassChild)parent).name.equals(HString.getHString("Methods"))){
					item = new Object[clazz.nofMethods];
					child = clazz.methods;
					while(child != null && index < item.length){
						item[index++] = child;
						child = child.next;
					}
					return item;
				}
			}
			if (parent instanceof RootElement) {
				if (RefType.nofRefTypes < 1) return new Object[]{"No Classes loaded"};
				Item cls = ((RootElement)parent).children;
				Class[] stdCls = new Class[Class.nofStdClasses]; 
				int count = 0;
				while (cls != null) {	// standard classes
					if (((cls.accAndPropFlags & ((1<<dpfSynthetic) | (1<<apfInterface))) == 0) && (cls instanceof Class)) stdCls[count++] = (Class) cls;
					cls = cls.next;
				}
				// sort classes according to machine code base address
			    Arrays.sort(stdCls, new Comparator<Class>() {
			        public int compare(Class c1, Class c2) {
			            return (c1.codeSegment.address + c1.codeOffset) - (c2.codeSegment.address + c2.codeOffset);
			        }
			    });
				cls = ((RootElement)parent).children;
				Class[] intfCls = new Class[Class.nofInterfaceClasses];
				count = 0;
				while (cls != null) {
					if (((cls.accAndPropFlags & (1<<dpfSynthetic)) == 0) && ((cls.accAndPropFlags & (1<<apfInterface)) != 0)) intfCls[count++] = (Class) cls;
					cls = cls.next;
				}
				// sort interfaces according to name
			    Arrays.sort(intfCls, new Comparator<Class>() {
			        public int compare(Class c1, Class c2) {
			            return c1.name.toString().compareTo(c2.name.toString());
			        }
			    });
				cls = ((RootElement)parent).children;
				Array[] arrays = new Array[Class.nofArrays];
				count = 0;
				while (cls != null) {
					if (((cls.accAndPropFlags & (1<<dpfSynthetic)) == 0) && (cls instanceof Array)) arrays[count++] = (Array) cls;
					cls = cls.next;
				}
				// sort arrays according to name
			    Arrays.sort(arrays, new Comparator<Array>() {
			        public int compare(Array a1, Array a2) {
			            return a1.name.toString().compareTo(a2.name.toString());
			        }
			    });
				RefType[] classes = new RefType[Class.nofStdClasses + Class.nofInterfaceClasses + Class.nofArrays];
				System.arraycopy(stdCls, 0, classes, 0, Class.nofStdClasses);
				System.arraycopy(intfCls, 0, classes, Class.nofStdClasses, Class.nofInterfaceClasses);
				System.arraycopy(arrays, 0, classes, Class.nofStdClasses + Class.nofInterfaceClasses, Class.nofArrays);

				return classes;
			}
			return item;
		}

		public Object getParent(Object element) {
			if(element instanceof ClassMember){
				return ((ClassMember)element).owner;
			}
			if (element instanceof Segment){
				Segment seg = (Segment)element;
				return seg.owner;
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			//Classes and methods have always children
			if(element instanceof Class){
				if(((Class)element).nofMethods > 0)return true;
				if(((Class)element).nofClassFields > 0)return true;
				if(((Class)element).nofConstFields > 0)return true;
			}
			if (element instanceof Method)return true;
			if (element instanceof ClassChild)return true;
			if (element instanceof RootElement)return true;
			if (element instanceof Board) return true;
			if (element instanceof MemMap) return true;
			if (element instanceof SubroutineElement) {
				if(b != null) return true;
				else return false;
			}
			if (element instanceof Device) {
				if(((Device)element).segments != null) return true;
			}
			return false;
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			//if(!(inputElement instanceof TreeInput))return new Object[]{""};
			Object[] retObject = new Object[4];
			retObject[0] = new RootElement(HString.getHString("Classes, Interfaces and Arrays"), RefType.refTypeList); 
			if(b!=null)retObject[1] = b;
			else retObject[1] = new TreeInput("not connected");
			retObject[2] = new SubroutineElement("Subroutines"); 
			retObject[3] = new SystemTableElement("System Table"); 

			return retObject;
		}

		@Override
		public void dispose() {			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
		}
	}
		
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(5, true);
		parent.setLayout(layout);
				
		classTreeViewer = new TreeViewer(parent, SWT.SINGLE);
		GridData classTreeViewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
		classTreeViewerData.horizontalSpan = 2;
		classTreeViewer.getControl().setLayoutData(classTreeViewerData);
		classTreeViewer.setLabelProvider(new ClassTreeLabelProvider());
		classTreeViewer.setContentProvider(new ClassTreeContentProvider());
		classTreeViewer.setAutoExpandLevel(1);
		classTreeViewer.addSelectionChangedListener(this);

		textViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SCROLL_PAGE);
		GridData textViewerData = new GridData(GridData.FILL, GridData.FILL, true, true);
		textViewerData.horizontalSpan = 3;
		textViewerData.verticalSpan = 1;
		textViewer.getControl().setLayoutData(textViewerData);
		Document doc = new Document();
		textViewer.setDocument(doc);
		
		
		//get Display needs to set the font
		Display d =parent.getShell().getDisplay();
		if(d != null){
			FontData defaultFont = new FontData("Courier", 9, SWT.NORMAL);
			Font font = new Font(d, defaultFont);
			textViewer.getControl().setFont(font);
		}
		textViewer.setEditable(false);
		
		createActions();
		contributeToActionBars();
	}

	@Override
	public void setFocus() {
		classTreeViewer.getControl().setFocus();
	}
	
	private void createActions() {
		refresh = new Action() {
			public void run() {
				textViewer.getDocument().set(" ");
				textViewer.refresh();
				b = Configuration.getBoard();
				String root = "Class Tree Viewer";
				classTreeViewer.setInput(root);
				classTreeViewer.getControl().setEnabled(true);
				classTreeViewer.refresh();
			}
		};
		refresh.setText("Refresh");
		ImageDescriptor img = ImageDescriptor.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_REDO));
		refresh.setImageDescriptor(img);
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager menu) {
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refresh);
	}
	
	class ClassChild extends RootElement{

		Item owner;
		
		ClassChild(HString name, Item owner, Item children){
			super(name, children);
			this.owner = owner;
			
		}
	}

	class RootElement {
		HString name;
		Item children;
				
		RootElement(HString name, Item children){
			this.name = name;
			this.children = children;
		}
	}
	
	class TreeInput{
		public Object obj;
				
		TreeInput(Object obj) {
			this.obj = obj;
		}
	}
	
	class SubroutineElement{
		String name;
		SubroutineElement(String name){
			this.name = name;
		}
	}
	class SubroutineEntry{
		String name;
		Object data;
		SubroutineEntry(String name, Object data){
			this.name = name;
			this.data = data;
		}
	}
	class SystemTableElement{
		String name;
		SystemTableElement(String name){
			this.name = name;
		}
	}
	class MemoryMapElement{
		String name;
		Object obj;
		MemoryMapElement(String name, Object obj){
			this.name = name;
			this.obj = obj;
		}
	}



	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object obj = ((IStructuredSelection)event.getSelection()).getFirstElement();
		StringBuilder sb = new StringBuilder();
		if(Configuration.getBoard() == null) {
			sb.append("not connected");
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if (obj instanceof Class) {
			Class c = (Class)obj;
			sb.append("Name:                        " + c.name + "\n");
			sb.append("Number of class methods:     " + c.nofClassMethods + "\n");
			sb.append("Number of instance methods:  " + c.nofInstMethods + "\n");
			sb.append("Number of class fields:      " + c.nofClassFields + "\n");
			if((c.accAndPropFlags & (1 << apfInterface)) == 0) {				
				sb.append("Class field base address:    0x" + Integer.toHexString(c.varSegment.address + c.varOffset) + "\n");
			}
			sb.append("Class fields size:           " + c.classFieldsSize + " byte\n");
			sb.append("Number of instance fields:   " + c.nofInstFields + "\n");
			sb.append("Instance size:               " + c.objectSize + " byte\n");
			sb.append("Number of interfaces:        " + c.nofInterfaces + "\n");
			sb.append("Number of base classes:      " + c.extensionLevel + "\n");
			sb.append("Number of references:        " + c.nofClassRefs + "\n");
			sb.append("Max extension level:         " + Class.maxExtensionLevelStdClasses + "\n");
			if (c.codeSegment != null) {
				sb.append("Machine code base address:   0x" + Integer.toHexString(c.codeSegment.address + c.codeOffset) + "\n");				
				sb.append("Machine code size:           " + ((FixedValueEntry)c.codeBase.next).getValue() + " byte\n");
			}
			if (c.constSegment != null) {
				sb.append("Constant block base address: 0x" + Integer.toHexString(c.constSegment.address + c.constOffset) + "\n");
				sb.append("Constant block size:         " + ((FixedValueEntry)c.constantBlock).getValue() + " byte\n");
			}
			sb.append("Type descriptor address:     0x" + Integer.toHexString(c.address) + "\n");
			if (c.constantBlock != null) {
				sb.append("\nConstantblock:\n");	
				Item item = c.constantBlock;
				while(item != null){
					sb.append(item.toString() + "\n");
					item = item.next;
				}
			}
			
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if (obj instanceof Array) {
			Array a = (Array)obj;
			sb.append("Name:                        " + a.name + "\n");
			sb.append("Dimension:                   " + a.dimension + "\n");
			sb.append("Element type:                " + a.componentType.name + "\n");
			sb.append("Element size:                " + a.componentType.sizeInBits / 8 + " byte (" + a.componentType.sizeInBits + " bit)\n");
			sb.append("Type descriptor address:     0x" + Integer.toHexString(a.address) + "\n");
		
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if(obj instanceof Method){
			Method m = (Method)obj;
			sb.append("Name:                " + m.name + "\n");
			sb.append("Accessibility:       ");
			if((m.accAndPropFlags & (1 << apfPublic)) != 0){
				sb.append("public\n");
			}else if((m.accAndPropFlags & (1 << apfPrivate)) != 0){
				sb.append("private\n");
			}else if((m.accAndPropFlags & (1 << apfProtected)) != 0){
				sb.append("protected\n");
			}else if ((m.accAndPropFlags & (1 << dpfSysPrimitive)) != 0){
				sb.append("special system primitive");
			}else{
				sb.append("protected\n");
			}
			sb.append("Static:              ");
			if((m.accAndPropFlags & (1 << apfStatic)) != 0 || (m.accAndPropFlags & (1 << dpfSysPrimitive)) != 0 ){
				sb.append("yes\n");
			}else{
				sb.append("no\n");				
			}
			sb.append("Synthetic:           ");
			if((m.accAndPropFlags & (1 << dpfSynthetic)) != 0){
				sb.append("yes\n");
			}else{
				sb.append("no\n");				
			}
			sb.append("Address:             0x" + Integer.toHexString(m.address) + "\n");
			sb.append("Offset:              0x" + Integer.toHexString(m.offset) + "\n");
			sb.append("Index:               0x" + Integer.toHexString(m.index) + "\n");
			
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if(obj instanceof Field){
			Field field = (Field)obj;
			sb.append("Name:            " + field.name.toString() + "\n");
			sb.append("Type:            " + decodeFieldType(field.type.name) + "\n");
			sb.append("Accessibility:   ");
			if((field.accAndPropFlags & (1 << apfPublic)) != 0){
				sb.append("public\n");
			}else if((field.accAndPropFlags & (1 << apfPrivate)) != 0){
				sb.append("private\n");
			}else if((field.accAndPropFlags & (1 << apfProtected)) != 0){
				sb.append("protected\n");
			}else if ((field.accAndPropFlags & (1 << dpfSysPrimitive)) != 0){
				sb.append("special system primitive");
			}else{
				sb.append("protected\n");
			}
			sb.append("Constant:        ");
			if((field.accAndPropFlags & (1 << dpfConst)) != 0){
				sb.append("yes\n");
				sb.append("Value:           " + ((ConstField)field).getConstantItem().toString() + "\n");
			}else{
				sb.append("no\n");
			}
			sb.append("address:         0x" + Integer.toHexString(field.address) + "\n");
			sb.append("offset:          0x" + Integer.toHexString(field.offset) + "\n");
			
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if(obj instanceof CFG){
			CFG cfg = (CFG)obj;
			textViewer.getDocument().set(cfg.toString());
			textViewer.refresh();
			return;
		}
		if(obj instanceof SSA){
			SSA ssa = (SSA)obj;
			textViewer.getDocument().set(ssa.toString());
			textViewer.refresh();
			return;
		}
		if(obj instanceof Code32){
			Code32 machineCode = (Code32)obj;
			textViewer.getDocument().set(machineCode.toString());
			textViewer.refresh();
			return;
		}

		if (obj instanceof Device) {
			Device dev = (Device)obj;
			sb.append("device = " + dev.name.toString() + " {\n");
			sb.append("   technology = ");
			if (dev.technology == 0) sb.append("Ram\n");
			else if (dev.technology == 1) sb.append("Flash\n");
			else sb.append("Unkown\n");
			sb.append("   attributes = 0x" + Integer.toHexString(dev.attributes) + "\n");
			sb.append("   width = " + dev.width + "\n");
			sb.append("   base = 0x" + Integer.toHexString(dev.address) + "\n");
			sb.append("   size = 0x" + Integer.toHexString(dev.size) + "\n");
			sb.append("}\n");
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		if (obj instanceof Segment) {
			Segment seg = (Segment)obj;
			sb.append("segment = " + seg.name.toString() + " {\n");
			sb.append("   attributes = 0x" + Integer.toHexString(seg.attributes) + "\n");
			sb.append("   width = " + seg.width + "\n");
			sb.append("   base = 0x" + Integer.toHexString(seg.address) + "\n");
			sb.append("   size = 0x" + Integer.toHexString(seg.size) + "\n");
			sb.append("}\n");
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		
		if (obj instanceof SystemTableElement) {
			if(Configuration.getBoard() != null) {
				sb.append(Linker32.systemTable.getList());
				textViewer.getDocument().set(sb.toString());
				textViewer.refresh();
				return;
			}
		}

		if(obj instanceof SubroutineEntry) {
			sb.append(((SubroutineEntry) obj).data).toString();
			textViewer.getDocument().set(sb.toString());
			textViewer.refresh();
			return;
		}
		
		sb.append("");
		textViewer.getDocument().set(sb.toString());
		textViewer.refresh();
	}
	
	private String decodeFieldType(HString type){
		StringBuilder sb = new StringBuilder();
		int index = 0;
		int dim = 0;
		while(type.charAt(index) == '['){
			index++;
			dim++;
		}
		
		switch(type.charAt(index)){
		case 'B':
			sb.append("byte");
			break;
		case 'C':
			sb.append("char");
			break;
		case 'D':
			sb.append("double");
			break;
		case 'F':
			sb.append("float");
			break;
		case 'I':
			sb.append("int");
			break;
		case 'J':
			sb.append("long");
			break;
		case 'S':
			sb.append("short");
			break;
		case 'Z':
			sb.append("boolean");
			break;
		default:
			sb.append(type.toString());
			break;
		}
		for(int i = 0; i < dim; i++){
			sb.append("[]");
		}
		
		return sb.toString();
	}
}