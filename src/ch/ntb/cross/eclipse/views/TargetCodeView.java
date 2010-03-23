package ch.ntb.cross.eclipse.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.cross.eclipse.cg.InstructionDecoder;
import ch.ntb.cross.eclipse.cg.MPC555MachineInstruction;
import ch.ntb.cross.eclipse.linker.Linker;

public class TargetCodeView extends ViewPart {

	public static final String ID = "ch.ntb.cross.eclipse.views.TargetCodeView";

	private boolean showException = true;
	private Table table;
	private TableCursor cursor;

	public TargetCodeView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		init(parent);
	}

	private void init(Composite parent) {
		if (table != null) {
			table.dispose();
			cursor.dispose();
		}
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		cursor = new TableCursor(table, SWT.NONE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnAddress = new TableColumn(table, SWT.NONE);
		tblclmnAddress.setWidth(100);
		tblclmnAddress.setText("Address");

		TableColumn tblclmnInstruction = new TableColumn(table, SWT.NONE);
		tblclmnInstruction.setWidth(100);
		tblclmnInstruction.setText("Instruction");

		TableColumn tblclmnMnemonic = new TableColumn(table, SWT.NONE);
		tblclmnMnemonic.setWidth(200);
		tblclmnMnemonic.setText("Mnemonic");

		TableColumn tblclmnDebugtext = new TableColumn(table, SWT.NONE);
		tblclmnDebugtext.setWidth(350);
		tblclmnDebugtext.setText("Debug-Text");
	}

	@Override
	public void setFocus() {

	}

	public void update(final List<MPC555MachineInstruction> code) {

		table.removeAll();
		Display display = table.getDisplay();

		display.asyncExec(new Runnable() {
			public void run() {
				int address = 0;
				String mask = "0x00000000";
				final int maskLength = mask.length();

				for (int i = 0; i < code.size(); i++) {
					if (showException || address >= 0x2000) { // &&
						// code.get(i).get()
						// != 0
						final String hexAddress = Integer.toHexString(address);
						MPC555MachineInstruction machineInstr = code.get(i);

						TableItem instruction = coloredTableItem(address);

						String extendedAddr = mask.substring(0, maskLength - hexAddress.length()) + hexAddress;

						final Integer intInstruction = machineInstr.get();
						String extendedInstr = mask.substring(0, maskLength - Integer.toHexString(intInstruction).length())
						+ Integer.toHexString(intInstruction);

						String mnemonic = InstructionDecoder.getMnemonic(intInstruction);
						String debugText = machineInstr.getDebugText();

						if (address > 0x2000 && address < 0x2300) {
							System.out.println(extendedAddr + " " + extendedInstr + "   " + mnemonic + " (" + debugText + ")");
						}

						instruction.setText(new String[] { extendedAddr, extendedInstr, mnemonic, debugText });
					}
					address += 4;
				}
			}
		});
	}

	private TableItem coloredTableItem(int address) {
		TableItem item = new TableItem(table, SWT.NONE);
		Display display = table.getDisplay();
		Color color = null;

		if (address < 0x2000) {
			// Exception Table
			color = new Color(display, 255, 228, 228);
			item.setBackground(color);
		} else if (Linker.isMethodOffset(address)) {
			String classMethod = Linker.getMethod(address);
			String methodName = classMethod.substring(classMethod.lastIndexOf(".") + 1);
			String className = classMethod.substring(0, classMethod.lastIndexOf("."));
			item.setText(new String[] { className, methodName });
			color = display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			item.setBackground(color);

			item = new TableItem(table, SWT.NONE);
		} else if (Linker.isClassDescriptorStart(address)) {
			String className = Linker.getClassDescriptorName(address);
			item.setText(new String[] { "Class Descriptor", className });
			color = display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			item.setBackground(color);

			item = new TableItem(table, SWT.NONE);
		}

		return item;
	}

	public void toggleShowException() {
		showException = !showException;
		update(Linker.Link());
	}

	public void jumpToAddress(int address) {
		int offset;
		if (showException) {
			offset = address / 4;
		} else {
			offset = (address - 0x2000) / 4;
		}

		if (offset >= 0 && offset < table.getItemCount()) {
			TableItem item = table.getItem(offset); // Die Überschriften werden
			// nicht gezählt, tja...
			cursor.setSelection(item, 0);
			cursor.setVisible(false);
		}
	}
}
