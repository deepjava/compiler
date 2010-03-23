package ch.ntb.cross.eclipse.views;

import java.util.HashMap;

import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.cross.eclipse.builder.CompiledClass;
import ch.ntb.cross.eclipse.cfg.CFG;
import ch.ntb.cross.eclipse.views.helpers.ModifiedDot;
import ch.ntb.cross.eclipse.views.helpers.SWTImageCanvas;
import ch.ntb.cross.eclipse.views.printers.CFGPrinter;

/**
 * View to display the CFG-Graph of all the Methods in a Class. Uses GraphViz to
 * create the Graph-Images.
 * 
 * @author buesser
 * 
 */
public class CFGView extends ViewPart {
	public SWTImageCanvas imageCanvas;

	public static final String ID = "ch.ntb.cross.eclipse.views.CFGView";

	/**
	 * Create the GUI.
	 * 
	 * @param frame
	 *            The Composite handle of parent
	 */
	@Override
	public void createPartControl(Composite frame) {
		imageCanvas = new SWTImageCanvas(frame);
	}

	/**
	 * Called when we must grab focus.
	 * 
	 * @see org.eclipse.ui.part.ViewPart#setFocus
	 */
	@Override
	public void setFocus() {
		imageCanvas.setFocus();
	}

	/**
	 * Called when the View is to be disposed
	 */
	@Override
	public void dispose() {
		imageCanvas.dispose();
		super.dispose();
	}

	/**
	 * Update the View with a new Class.
	 * 
	 * @param cc
	 */
	public void update(CompiledClass cc) {
		HashMap<IMethodInfo, CFG> cfgTree = cc.getCFG();
		StringBuilder returnString = new StringBuilder("digraph g {");

		for (CFG cfg : cfgTree.values()) {
			returnString.append(CFGPrinter.getCFGString(cfg));
		}

		returnString.append("}");

		Image image = ModifiedDot.drawGraph(returnString.toString());
		imageCanvas.setImage(image);
	}
}