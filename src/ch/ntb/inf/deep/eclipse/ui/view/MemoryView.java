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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.eclipse.ui.model.MemoryEntry;
import ch.ntb.inf.deep.launcher.Launcher;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class MemoryView extends ViewPart implements Listener {
	public static final String ID = "ch.ntb.inf.deep.eclipse.ui.view.MemoryView";
	private TableViewer viewer;
	private Text addr;
	private Text count;
	private Button button;
	private int width;
	final String[] choice = new String[]{"1 Byte", "2 Bytes", "4 Bytes"};
	private MemoryEntry[] segs;
	private int startAddr;
	
	static final byte slotSize = 4; // 4 bytes
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if(obj instanceof String){
				if(index == 0) 
					return (String)obj;
				
				return "";
			}
			switch (index) {
			case 0:
				if (((MemoryEntry) obj).addr == -1) {
					return "";
				}
				return String.format("0x%08X",((MemoryEntry) obj).addr);
			case 1:
				if (((MemoryEntry) obj).addr == -1) {
					return "";
				}
				switch (width) {
				case 1:
					return String.format("0x%02X", ((MemoryEntry) obj).value & 0xff);
				case 2:
					return String.format("0x%04X", ((MemoryEntry) obj).value & 0xffff);
				case 4:
					return String.format("0x%08X", ((MemoryEntry) obj).value);
				default:
					return String.format("0x%08X", ((MemoryEntry) obj).value);
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
		parent.setLayout(new GridLayout(7, false));
		Label label = new Label(parent, SWT.NONE);
		label.setText("Start address:   ");
		addr = new Text(parent, SWT.BORDER);
		addr.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = addr.getText() + e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				if (chars[0] == '0' && chars.length > 1) {// hex value
					if ((chars[1] == 'x' || chars[1] == 'X')) {
						if (chars.length > 2) {
							for (int i = 2; i < chars.length; i++) {
								if (!(('0' <= chars[i] && chars[i] <= '9')
										|| ('A' <= chars[i] && chars[i] <= 'F') || ('a' <= chars[i] && chars[i] <= 'f'))) {
									e.doit = false;
									return;
								}
							}
						}
					} else {
						e.doit = false;
						return;
					}
				} else {
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9')) {
							e.doit = false;
							return;
						}
					}
				}
			}
		});
		label = new Label(parent, SWT.NONE);
		label.setText("Element size:   ");
		
		final Combo combo = new Combo(parent,SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		combo.add(choice[0]);
		combo.add(choice[1]);
		combo.add(choice[2]);	
		combo.select(2);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (combo.getText().equals(choice[0])) width = 1;
				if (combo.getText().equals(choice[1])) width = 2;
				if (combo.getText().equals(choice[2])) width = 4;
			}
		});

		label = new Label(parent, SWT.NONE);
		label.setText("nof Elements:   ");
		count = new Text(parent, SWT.BORDER);
		count.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = count.getText() + e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				if (chars[0] == '0' && chars.length > 1) {// hex value
					if ((chars[1] == 'x' || chars[1] == 'X')) {
						if (chars.length > 2) {
							for (int i = 2; i < chars.length; i++) {
								if (!(('0' <= chars[i] && chars[i] <= '9')
										|| ('A' <= chars[i] && chars[i] <= 'F') || ('a' <= chars[i] && chars[i] <= 'f'))) {
									e.doit = false;
									return;
								}
							}
						}
					} else {
						e.doit = false;
						return;
					}
				} else {
					for (int i = 0; i < chars.length; i++) {
						if (!('0' <= chars[i] && chars[i] <= '9')) {
							e.doit = false;
							return;
						}
					}
				}
			}
		});

		button = new Button(parent, SWT.PUSH);
		button.setText("read");
		button.addListener(SWT.Selection, this);
		createViewer(parent);

	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		String[] titels = { "Address", "Value" };
		int[] bounds = { 100, 100 };
		for (int i = 0; i < titels.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titels[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setMoveable(false);
		}
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		// create the cell editors
		CellEditor[] editors = new CellEditor[2];
		editors[1] = new TextCellEditor(table);
		
		viewer.setColumnProperties(titels);
		viewer.setCellEditors(editors);
		viewer.setCellModifier(new MemoryCellModifier(viewer));

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		viewer.setInput(new MemoryEntry[] {new MemoryEntry(-1, 0), new MemoryEntry(-1, 0)});

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 7;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget.equals(button)) {
			startAddr = 0;
			int size = 0;
			String addrStr = addr.getText();
			String countStr = count.getText();
			// work around for problem when the most significant bit is set;
			if(addrStr.charAt(0) == '0' && addrStr.length() > 9 && addrStr.charAt(2) > '7'){
				String most = addrStr.substring(2, 3);
				addrStr = "0x0" + addrStr.substring(3);
				startAddr = (Integer.parseInt(most,16) << 28) |Integer.decode(addrStr);
			}else{
				startAddr = Integer.decode(addr.getText());
			}
			// do the same for the size
			if(countStr.charAt(0) == '0' && countStr.length() > 9 && countStr.charAt(2) > '7'){
				String most = countStr.substring(2, 3);
				countStr = "0x0" + countStr.substring(3);
				size = (Integer.parseInt(most,16) << 28) |Integer.decode(countStr);
			}else{
				size = Integer.decode(count.getText());
			}
			TargetConnection tc = Launcher.getTargetConnection();
			if (tc == null) {
				viewer.setInput(new String[]{"target not connected"});
				viewer.refresh();
				return;
			}
			if (!tc.isConnected()) {//reopen
				try {
					tc.openConnection();
				} catch (TargetConnectionException e) {
					viewer.setInput(new String[]{"target not initialized"});
					viewer.refresh();
					return;
				}
			}
			if (size > 0) {
				segs = new MemoryEntry[size];
				try {
					boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
					if (!wasFreezeAsserted) tc.stopTarget();
			
					for (int i = 0; i < size; i++) {
						switch (width) {
						case 1:
							int val = tc.readByte(startAddr + i);
							segs[i] = new MemoryEntry(startAddr + i, val);
							break;
						case 2: 
							segs[i] = new MemoryEntry(startAddr + i * 2, tc.readHalfWord(startAddr + i * 2));
							break;
						case 4: 
							segs[i] = new MemoryEntry(startAddr + i * 4, tc.readWord(startAddr + i * 4));
							break;
						default: 
							segs[i] = new MemoryEntry(startAddr + i * 4, tc.readWord(startAddr + i * 4));
						}
					}
					if (!wasFreezeAsserted) tc.startTarget(-1);
				} catch (TargetConnectionException e1) {
					viewer.setInput(new String[]{"target not initialized"});
					viewer.refresh();
					return;
				}
				viewer.setInput(segs);
				viewer.refresh();
			}
		}
	}

	/**
	 * This class represents the cell modifier for the Memory View
	 */

	class MemoryCellModifier implements ICellModifier {
		private Viewer viewer;

		public MemoryCellModifier(Viewer viewer) {
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
			MemoryEntry p = (MemoryEntry)element;
			if (p.addr > -1 && property.equals("Value")) {
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
			MemoryEntry p = (MemoryEntry) element;
			if ("Value".equals(property))
				switch (width) {
				case 1:
					return String.format("0x%02X", p.value & 0xff);
				case 2:
					return String.format("0x%04X", p.value & 0xffff); 
				case 4:
					return String.format("0x%08X", p.value);
				default:
					return String.format("0x%08X", p.value);
				}
			else if ("Address".equals(property))
				return String.format("0x%08X",p.addr);
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
			if (element instanceof Item) element = ((Item) element).getData();

			MemoryEntry p = (MemoryEntry) element;
			if ("Value".equals(property)){
				try{
					p.value =Integer.decode((String) value);
					TargetConnection tc = Launcher.getTargetConnection();
					if(tc == null){
						viewer.setInput(new String[]{"target not connected"});
						viewer.refresh();
						return;
					}
					
					boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
					if (!wasFreezeAsserted)	tc.stopTarget();
				
					switch (width) {
					case 1:
						tc.writeByte(p.addr, (byte)p.value);
						break;
					case 2:
						tc.writeHalfWord(p.addr, (short)p.value);
						break;
					case 4:
						tc.writeWord(p.addr, p.value);
						break;
					default:
						tc.writeWord(p.addr, p.value);
					}
					if (!wasFreezeAsserted)	tc.startTarget(-1);
				}catch (NumberFormatException e) {
				}catch (TargetConnectionException e1){
					viewer.setInput(new String[]{"target not initialized"});
					viewer.refresh();
					return;
				}

				// Force the viewer to refresh
				viewer.refresh();
			}
		}
	}
}


