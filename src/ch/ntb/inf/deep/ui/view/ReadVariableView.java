package ch.ntb.inf.deep.ui.view;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.ui.model.ReadVariableElement;

public class ReadVariableView extends ViewPart {
	public static final String ID = "ch.ntb.inf.deep.view.ReadVariableView";
	private TableViewer viewer;
	private ReadVariableElement[] elements;

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if(!(obj instanceof ReadVariableElement)){
				return "";
			}
			switch (index) {
			case 0:
				return ((ReadVariableElement) obj).fullQualifiedName;
			case 1:
				if (!((ReadVariableElement) obj).isReaded) {
					return "";
				}
				switch(((ReadVariableElement) obj).representation){
				case 0:
					return Integer.toBinaryString((int)((ReadVariableElement) obj).result);
				case 1:
					return Integer.toHexString((int)((ReadVariableElement) obj).result);
				case 2:
					return Integer.toString((int)((ReadVariableElement) obj).result);
				case 3:
					return Double.toString(Double.longBitsToDouble(((ReadVariableElement) obj).result));
				default:
					return Integer.toString((int)((ReadVariableElement) obj).result);
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
		Composite composite = new Composite(parent, SWT.NONE);
	    composite.setLayout(new GridLayout(1, false));
				
		viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL |SWT.FULL_SELECTION | SWT.BORDER);
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
		
		//create the cell editors
		CellEditor[] editors = new CellEditor[1];
	    editors[0] = new TextCellEditor(table);

	    viewer.setColumnProperties(titels);
	    viewer.setCellEditors(editors);
	    viewer.setCellModifier(new ReadVarCellModifier(viewer));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		elements = new ReadVariableElement[32];
		for(int i = 0; i < 32; i++){
			elements[i] = new ReadVariableElement();
		}
		viewer.setInput(elements);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();

	}
	
	/**
	 * This class represents the cell modifier for the PersonEditor program
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
	   if(property.equals("Variable to read")){
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
	    if ("Variable to read".equals(property))
	      p.setFullQualifiedName((String) value);
	    else if ("Result".equals(property))
	      p.setResult(((Integer) value).intValue());
	    
	    // Force the viewer to refresh
	    viewer.refresh();
	  }
	}

}
