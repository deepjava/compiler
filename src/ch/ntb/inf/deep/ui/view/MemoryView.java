package ch.ntb.inf.deep.ui.view;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.deep.ui.model.MemorySegment;

public class MemoryView extends ViewPart implements Listener {
	public static final String ID = "ch.ntb.inf.deep.view.MemoryView";
	private TableViewer viewer;
	private Text addr;
	private Text count;
	private Button button;
	private Downloader bdi;

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			switch (index) {
			case 0:
				if (((MemorySegment) obj).addr == -1) {
					return "";
				}
				return "0x" + Integer.toHexString(((MemorySegment) obj).addr);
			case 1:
				if (((MemorySegment) obj).addr == -1) {
					return "";
				}
				return "0x" + Integer.toHexString(((MemorySegment) obj).value);
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
		GridLayout layout = new GridLayout(5, false);
		parent.setLayout(layout);
		Label label = new Label(parent, SWT.NONE);
		label.setText("start address:   ");
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
		Label label2 = new Label(parent, SWT.NONE);
		label2.setText("nofWords:   ");
		count = new Text(parent, SWT.BORDER);
		count.addListener(SWT.Verify, new Listener() {
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

		button = new Button(parent, SWT.PUSH);
		button.setText("read");
		button.addListener(SWT.Selection, this);
		createViewer(parent);

	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		String[] titels = { "Address", "Value" };
		int[] bounds = { 60, 230 };
		for (int i = 0; i < titels.length; i++) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(titels[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(false);
			column.getColumn().setMoveable(false);
		}
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		viewer.setInput(new MemorySegment[] { new MemorySegment(),
				new MemorySegment(), new MemorySegment() });// TODO fill Array
															// into

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 5;
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
			int startAddr = 0;
			int size = 0;
			String addrStr = addr.getText();
			String countStr = count.getText();
			//work around for problem when in hex-int-number the most significant bit is set;
			//once for the start addres
			if(addrStr.charAt(0) == '0' && addrStr.length() > 9 && addrStr.charAt(2) > '7'){
				String most = addrStr.substring(2, 3);
				addrStr = "0x0" + addrStr.substring(3);
				startAddr = (Integer.parseInt(most,16) << 28) |Integer.decode(addrStr);
			}else{
				startAddr = Integer.decode(addr.getText());
			}
			//once for the size
			if(countStr.charAt(0) == '0' && countStr.length() > 9 && countStr.charAt(2) > '7'){
				String most = countStr.substring(2, 3);
				countStr = "0x0" + countStr.substring(3);
				size = (Integer.parseInt(most,16) << 28) |Integer.decode(countStr);
			}else{
				size = Integer.decode(count.getText());
			}
			if (bdi == null) {
				bdi = UsbMpc555Loader.getInstance();
			}
			if (size > 0) {
				MemorySegment[] segs = new MemorySegment[size];
				try {
					boolean wasFreezeAsserted = bdi.isFreezeAsserted();
					if (!wasFreezeAsserted) {
						bdi.stopTarget();
					}
					for (int i = 0; i < size; i++) {
						segs[i] = new MemorySegment(startAddr + i * 4, bdi
								.getMem(startAddr + i * 4, 4));
					}
					if (!wasFreezeAsserted) {
						bdi.startTarget();
					}
				} catch (DownloaderException e1) {
					e1.printStackTrace();
				}
				viewer.setInput(segs);
				viewer.refresh();
			}
		}
	}

}
