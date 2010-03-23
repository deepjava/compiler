package ch.ntb.cross.eclipse.views;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.cross.eclipse.builder.CompiledClass;
import ch.ntb.cross.eclipse.ssa.SSA;
import ch.ntb.cross.eclipse.views.helpers.ModifiedDot;
import ch.ntb.cross.eclipse.views.helpers.SWTImageCanvas;
import ch.ntb.cross.eclipse.views.printers.SSAPrinter;

/**
 * View to display the SSA-Graph of all the Methods in a Class. Uses GraphViz to
 * create the Graph-Images.
 * 
 * @author buesser
 * 
 */
public class SSAView extends ViewPart {
	/**
	 * Image-Canvas for the Graph-Image.
	 */
	public SWTImageCanvas imageCanvas;

	public static final String ID = "ch.ntb.cross.eclipse.views.SSAView";

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
	 * Called when the View is to be disposed.
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
		HashMap<String, SSA> ssaTrees = cc.getSSA();
		StringBuilder returnString = new StringBuilder("digraph g {");

		for (SSA ssa : ssaTrees.values()) {
			String s = SSAPrinter.getSSAString(ssa);
			returnString.append(s);
		}

		returnString.append("}");

		Image image = ModifiedDot.drawGraph(returnString.toString());
		imageCanvas.setImage(image);
	}
}