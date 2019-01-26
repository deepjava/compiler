package ch.ntb.inf.deep.eclipse.ui.view;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.dwarf.DebugLineStateMachine;
import ch.ntb.inf.deep.dwarf.LineMatrixEntry;
import ch.ntb.inf.deep.dwarf.SymbolTableEntry;
import nl.lxtreme.binutils.elf.DynamicEntry;
import nl.lxtreme.binutils.elf.Elf;
import nl.lxtreme.binutils.elf.ProgramHeader;
import nl.lxtreme.binutils.elf.SectionHeader;

public class ElfFileView extends ViewPart {
	private TabFolder tabFolder;
	private TabItem headerTab;
	private TabItem ProgHeaderTab;
	private TabItem DynamicTab;
	private TabItem SectionHeaderTab;
	private TabItem DebugTab;

	private Elf elf;
	private Action refresh;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(1000, 1000);
		Layout layout = new GridLayout(1, false);
		shell.setLayout(layout);

		Button button = new Button(shell, SWT.NONE);
		button.setText("Refresh");
		ElfFileView dut = new ElfFileView();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dut.setFocus();
			}
		});

		dut.createView(shell);
		dut.updateView();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		createView(parent);
		addActionBar();
	}

	private void createView(Composite parent) {
		tabFolder = new TabFolder(parent, SWT.NULL);
		headerTab = new TabItem(tabFolder, SWT.NULL);
		headerTab.setText("Header");
		ProgHeaderTab = new TabItem(tabFolder, SWT.NULL);
		ProgHeaderTab.setText("Program Header");
		DynamicTab = new TabItem(tabFolder, SWT.NULL);
		DynamicTab.setText("Dynamic Section");
		SectionHeaderTab = new TabItem(tabFolder, SWT.NULL);
		SectionHeaderTab.setText("Section Header");
		DebugTab = new TabItem(tabFolder, SWT.NULL);
		DebugTab.setText("Debug Information");
	}

	private void addActionBar() {
		IActionBars bars = getViewSite().getActionBars();
		// fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		refresh = new Action() {
			public void run() {
				updateView();
			}
		};
		refresh.setText("Refresh");
		ImageDescriptor img = ImageDescriptor
				.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_REDO));
		refresh.setImageDescriptor(img);
		manager.add(refresh);
	}

	@Override
	public void setFocus() {
		updateView();
	}

	private void updateView() {
		Project activeProject = Configuration.getActiveProject();
		String filePath = "C:\\Users\\Martin\\Documents\\MSE\\VT1\\testfiles\\a.out";
		if (activeProject != null) {
			filePath = activeProject.getImgFileName().toString();
		}
		try {
			elf = new Elf(new File(filePath));
			loadHeader();
			loadProgramHeader();
			loadDynamicSection();
			loadSectionHeader();
			loadDebugInformation();
		} catch (IOException e) {
			elf = null;
			tabFolder.setSelection(0);
			Label errorText = new Label(tabFolder, SWT.NULL);
			headerTab.setControl(errorText);
			errorText.setForeground(new Color(Display.getCurrent(), 255, 0, 0));
			errorText.setText(e.getClass().toString() + " " + e.getMessage());
		}
	}

	private void loadHeader() {
		Text text = new Text(tabFolder, SWT.MULTI | SWT.READ_ONLY);
		headerTab.setControl(text);
		text.setText(elf.header.toString());
	}

	private void loadProgramHeader() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		ProgHeaderTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Type", "File Size", "Memory Size", "Virtual Address", "Physical Address", "Offset",
				"Align", "Flags" };
		int[] bounds = { 200, 80, 80, 150, 150, 80, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		// Table Content
		for (ProgramHeader progHeader : elf.programHeaders) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, progHeader.type.toString());
			item.setText(1, String.format("0x%08X", progHeader.segmentFileSize));
			item.setText(2, String.format("0x%08X", progHeader.segmentMemorySize));
			item.setText(3, String.format("0x%016X", progHeader.virtualAddress));
			item.setText(4, String.format("0x%016X", progHeader.physicalAddress));
			item.setText(5, String.format("0x%08X", progHeader.offset));

			// Alignment

			item.setText(6, alignmentString(progHeader.segmentAlignment));

			// Flags
			Boolean execute = ((progHeader.flags & 0b001) > 0);
			Boolean write = ((progHeader.flags & 0b010) > 0);
			Boolean read = ((progHeader.flags & 0b100) > 0);
			String flags = (read ? "r" : "-") + (write ? "w" : "-") + (execute ? "x" : "-");
			item.setText(7, flags);
		}

	}

	private void loadDynamicSection() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		DynamicTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Name", "No", "Value" };
		int[] bounds = { 200, 150, 80, 80, 150, 80, 80, 150, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		if (elf.dynamicTable == null) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText("No Dynamic Entry");
			return;
		}
		for (DynamicEntry dynamicEntry : elf.dynamicTable) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, dynamicEntry.getTag().name());
			item.setText(1, String.format("%016X", dynamicEntry.getTag().ordinal()));
			item.setText(2, "" + dynamicEntry.getValue());
		}

	}

	private void loadSectionHeader() {
		Table table = new Table(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		SectionHeaderTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Name", "Type", "Size", "Entry Size", "Virtual Memory Address", "Offset", "Align", "Flags",
				"Link", "Info" };
		int[] bounds = { 150, 200, 80, 80, 150, 80, 80, 150, 40, 40 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		// Table Content
		for (SectionHeader sectionHeader : elf.sectionHeaders) {
			TableItem item = new TableItem(table, SWT.NONE);
			String name = sectionHeader.getName();
			if (name != null) {
				item.setText(0, sectionHeader.getName());
			}
			item.setText(1, sectionHeader.type.name());
			item.setText(2, String.format("%08X", sectionHeader.size));
			item.setText(3, String.format("%08X", sectionHeader.entrySize));
			item.setText(4, String.format("%016X", sectionHeader.virtualAddress));
			item.setText(5, String.format("%08X", sectionHeader.fileOffset));
			item.setText(6, alignmentString(sectionHeader.alignment));
			item.setText(7, sectionHeaderFlagString(sectionHeader.flags));
			item.setText(8, "" + sectionHeader.link);
			item.setText(9, "" + sectionHeader.info);
		}
	}

	private String alignmentString(long alignment) {
		String binaryAlignment = Long.toBinaryString(alignment);
		int potenz = binaryAlignment.length() - binaryAlignment.replace("0", "").length();
		return "2^" + potenz;
	}

	private String sectionHeaderFlagString(long flag) {
		List<String> result = new ArrayList<String>();
		if ((flag & 0x1) > 0)
			result.add("WRITE"); // Writable
		if ((flag & 0x2) > 0)
			result.add("ALLOC"); // Occupies memory during execution
		if ((flag & 0x4) > 0)
			result.add("EXECINSTR"); // Executable
		if ((flag & 0x10) > 0)
			result.add("MERGE"); // Might be merged
		if ((flag & 0x20) > 0)
			result.add("STRINGS"); // Contains nul-terminated strings
		if ((flag & 0x40) > 0)
			result.add("INFO"); // 'sh_info' contains SHT index
		if ((flag & 0x80) > 0)
			result.add("LINK_ORDER"); // Preserve order after combining
		if ((flag & 0x100) > 0)
			result.add("OS_NONCONFORMING"); // Non-standard OS specific handling required
		if ((flag & 0x200) > 0)
			result.add("GROUP"); // Section is member of a group
		if ((flag & 0x400) > 0)
			result.add("TLS"); // Section hold thread-local data
		if ((flag & 0x0ff00000) > 0)
			result.add("MASKOS"); // OS-specific
		if ((flag & 0xf0000000) > 0)
			result.add("MASKPROC"); // Processor-specific
		if ((flag & 0x4000000) > 0)
			result.add("ORDERED"); // Special ordering requirement (Solaris)
		if ((flag & 0x8000000) > 0)
			result.add("EXCLUDE"); // Section is excluded unless referenced or allocated (Solaris)
		return String.join(", ", result);
	}

	private void loadDebugInformation() {
		TabFolder debugTabFolder = new TabFolder(tabFolder, SWT.NULL);
		DebugTab.setControl(debugTabFolder);

		TabItem symbolTableTab = new TabItem(debugTabFolder, SWT.NULL);
		symbolTableTab.setText(".symtab");

		Table table = new Table(debugTabFolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		symbolTableTab.setControl(table);
		table.setHeaderVisible(true);

		// Table Header
		String[] titels = { "Name", "Value", "Size", "Type","Bind", "Visibility", "shndx" };
		int[] bounds = { 150, 80, 80, 80, 80, 80,80 };

		for (int i = 0; i < titels.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titels[i]);
			column.setWidth(bounds[i]);
			column.setResizable(true);
			column.setMoveable(false);
		}

		try {
			ByteBuffer buf = elf.getSectionByName(".symtab");
			while (buf.position() < buf.limit()) {
				TableItem item = new TableItem(table, SWT.NONE);
				SymbolTableEntry entry = new SymbolTableEntry(buf);
				item.setText(0, "" + entry.name);
				item.setText(1, String.format("%08X", entry.value));
				item.setText(2, "" + entry.size);
				item.setText(3, entry.type.toString());
				item.setText(4, entry.bind.toString());
				item.setText(5, entry.visibility.toString());
				item.setText(6, "" + entry.shndx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		TabItem debugStrTab = new TabItem(debugTabFolder, SWT.NULL);
		debugStrTab.setText(".debug_str");
		Text text = new Text(debugTabFolder, SWT.WRAP | SWT.V_SCROLL);
		debugStrTab.setControl(text);
		try {
			ByteBuffer buf = elf.getSectionByName(".debug_str");
			String[] strings = new String(buf.array(), StandardCharsets.UTF_8).split("\0");
			text.setText(String.join("\n", strings));
		} catch (IOException e) {
			text.setText(e.getMessage());
		}

		TabItem debugLineTab = new TabItem(debugTabFolder, SWT.NULL);
		debugLineTab.setText(".debug_line");
		SashForm sashForm = new SashForm(debugTabFolder, SWT.HORIZONTAL);
		debugLineTab.setControl(sashForm);
		try {
			ByteBuffer buf = elf.getSectionByName(".debug_line");
			while (buf.position() < buf.capacity()) {
				text = new Text(sashForm, SWT.WRAP | SWT.V_SCROLL);
				StringJoiner sj = new StringJoiner("\n");
				DebugLineStateMachine stateMaschine = new DebugLineStateMachine(buf, sj);

				stateMaschine.run();

				sj.add(String.format("%s\t\t\t%s\t\t%s\t\t%s", "File", "Line", "Column", "Address"));
				for (LineMatrixEntry entry : stateMaschine.matrix) {
					sj.add(String.format("%s\t\t%s\t\t\t%s\t\t\t\t0x%X", entry.filename, entry.line, entry.column,
							entry.address));
				}
				text.setText(sj.toString());
			}

		} catch (Exception e) {
			text = new Text(sashForm, SWT.WRAP | SWT.V_SCROLL);
			text.setText(e.getMessage());
		}
	}
}
