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
import ch.ntb.inf.deep.cgPPC.CodeGen;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ClassMember;
import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.ConstField;
import ch.ntb.inf.deep.classItems.RefType;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Device;
import ch.ntb.inf.deep.config.MemMap;
import ch.ntb.inf.deep.config.MemSector;
import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.linker.ConstBlkEntry;
import ch.ntb.inf.deep.linker.FixedValueEntry;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;

public class ClassTreeView extends ViewPart implements ISelectionChangedListener, ICclassFileConsts {
	public static final String ID = "ch.ntb.inf.deep.eclipse.ui.view.ClassTreeView";
	private TreeViewer classTreeViewer;
	private TreeViewer deviceTreeViewer;
	private TextViewer textViewer;
	private Action refresh;
	
	class ClassTreeLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}

		public String getText(Object element) {
			if(element instanceof Item){
				if(element instanceof Method){
					return ((Method)element).name.toString() + ((Method)element).methDescriptor.toString();
				}
				return ((Item)element).name.toString();
			}else{
				if(element instanceof RootElement)return ((RootElement)element).name.toString();
				if(element instanceof CFG)return "CFG";
				if(element instanceof SSA)return "SSA";
				if(element instanceof CodeGen)return "MachineCode";
				if(element instanceof String)return (String) element;
				return "";
			}
		}
	}

	class ClassTreeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
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
			if (parent instanceof RootElement){
				if (RefType.nofRefTypes < 1) return new Object[]{"No Classes loaded"};
				int nof = 0;
				Item cls = ((RootElement)parent).children;
				while (cls != null) {
					if ((cls.accAndPropFlags & (1<<dpfSynthetic)) == 0) nof++;
					cls = cls.next;
				}
				Item[] classes = new Item[nof];
				int count = 0;
				cls = ((RootElement)parent).children;
				while (cls != null && count < classes.length) {
					if ((cls.accAndPropFlags & (1<<dpfSynthetic)) == 0) classes[count++] = cls;
					cls = cls.next;
				}
				
				return classes;
			}
			return item;
		}

		public Object getParent(Object element) {
			if(element instanceof ClassMember){
				return ((ClassMember)element).owner;
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
			if(element instanceof Method)return true;
			if(element instanceof ClassChild)return true;
			if(element instanceof RootElement)return true;
			return false;
			
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(!(inputElement instanceof TreeInput))return new Object[]{""};
			return new Object[]{((TreeInput)inputElement).obj};
		}

		@Override
		public void dispose() {			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
		}
	}
	
	class DeviceTreeLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			if (element instanceof Board) return "Memory Map";					
			if (element instanceof MemMap) return ((MemMap)element).name.toString();					
			if (element instanceof Device) return ((Device)element).name.toString();
			if (element instanceof Segment) return ((Segment)element).name.toString();
			if (element instanceof String) return (String)element;
			return "";
		}
	}
	
	class DeviceTreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Board) {
				Board b = (Board)parentElement;
				if (b.memorymap == null || b.cpu.memorymap == null) return new Object[]{"No memory map loaded"};
				MemMap[] maps = new MemMap[2];
				maps[0] = b.memorymap;
				maps[1] = b.cpu.memorymap;
				return maps;
			}			
			if (parentElement instanceof MemMap) {
				MemMap memMap = (MemMap)parentElement;
				if (memMap.devs == null) return new Object[]{"No devices loaded"};
				Device[] devices = new Device[memMap.getNofDevices()];
				Device dev = memMap.devs;
				for (int i = 0; i < devices.length && dev != null; i++) {
					devices[i] = dev;
					dev = (Device)dev.next;
				}			
				return devices;
			}			
			if (parentElement instanceof Device){
				Segment segs =((Device)parentElement).segments;
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
			if(parentElement instanceof Segment){
				Segment segs =((Device)parentElement).segments;
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
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof Segment){
				Segment seg = (Segment)element;
				return seg.owner;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Board) return true;
			if (element instanceof MemMap) return true;
			if (element instanceof Device) {
				if(((Device)element).segments != null) return true;
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if(!(inputElement instanceof TreeInput))return new Object[]{""};
			return new Object[]{((TreeInput)inputElement).obj};
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
		classTreeViewer.setAutoExpandLevel(2);
		classTreeViewer.addSelectionChangedListener(this);

		textViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SCROLL_PAGE);
		GridData textViewerData = new GridData(GridData.FILL, GridData.FILL, true, true);
		textViewerData.horizontalSpan = 3;
		textViewerData.verticalSpan = 2;
		textViewer.getControl().setLayoutData(textViewerData);
		Document doc = new Document();
		textViewer.setDocument(doc);
		
		deviceTreeViewer = new TreeViewer(parent, SWT.SINGLE);
		GridData deviceTreeViewerData = new GridData(SWT.FILL, SWT.FILL,true, true);
		deviceTreeViewerData.horizontalSpan = 2;
		deviceTreeViewer.getControl().setLayoutData(deviceTreeViewerData);
		deviceTreeViewer.setLabelProvider(new DeviceTreeLabelProvider());
		deviceTreeViewer.setContentProvider(new DeviceTreeContentProvider());
		deviceTreeViewer.setAutoExpandLevel(2);
		deviceTreeViewer.addSelectionChangedListener(this);
		
		
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
		refresh = new Action(){
			public void run(){
				classTreeViewer.setInput(new TreeInput(new RootElement(HString.getHString("Classes, Interfaces and Arrays:"), RefType.refTypeList)));
				classTreeViewer.getControl().setEnabled(true);
				classTreeViewer.refresh();
				Board b = Configuration.getBoard();
				if (b != null) deviceTreeViewer.setInput(new TreeInput(b)); else deviceTreeViewer.setInput(new TreeInput("not loaded"));
				deviceTreeViewer.getControl().setEnabled(true);
				deviceTreeViewer.refresh();
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

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object obj = ((IStructuredSelection)event.getSelection()).getFirstElement();
		StringBuilder sb = new StringBuilder();
		if (obj instanceof Class) {
			Class c = (Class)obj;
			sb.append("Name:                        " + c.name + "\n");
			sb.append("Number of class methods:     " + c.nofClassMethods + "\n");
			sb.append("Number of instance methods:  " + c.nofInstMethods + "\n");
			sb.append("Number of class fields:      " + c.nofClassFields + "\n");
			if((c.accAndPropFlags & (1 << apfInterface)) == 0){				
				sb.append("Class field base address:    0x" + Integer.toHexString(c.varSegment.address + c.varOffset) + "\n");
			}
			sb.append("Class fields size:           " + c.classFieldsSize + " byte\n");
			sb.append("Number of instance fields:   " + c.nofInstFields + "\n");
			sb.append("Instance size:               " + c.objectSize + " byte\n");
			sb.append("Number of interfaces:        " + c.nofInterfaces + "\n");
			sb.append("Number of base classes:      " + c.extensionLevel + "\n");
			sb.append("Number of references:        " + c.nofClassRefs + "\n");
			sb.append("Max extension level:         " + Class.maxExtensionLevelStdClasses + "\n");
			if((c.accAndPropFlags & (1 << apfInterface)) == 0){	
				sb.append("Machine code base address:   0x" + Integer.toHexString(c.codeSegment.address + c.codeOffset) + "\n");				
				sb.append("Machine code size:           " + ((FixedValueEntry)c.codeBase.next).getValue() + " byte\n");
				sb.append("Constant block base address: 0x" + Integer.toHexString(c.constSegment.address + c.constOffset) + "\n");
				sb.append("Constant block size:         " + ((FixedValueEntry)c.constantBlock).getValue() + " byte\n");
			}
			sb.append("Type descriptor address:     0x" + Integer.toHexString(c.address) + "\n");
			if((c.accAndPropFlags & (1 << apfInterface)) == 0){	
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
		if(obj instanceof CodeGen){
			CodeGen machineCode = (CodeGen)obj;
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
