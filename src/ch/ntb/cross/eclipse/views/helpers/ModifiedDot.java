package ch.ntb.cross.eclipse.views.helpers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.ntb.cross.eclipse.Activator;
import ch.ntb.cross.eclipse.preferences.PreferenceConstants;

public class ModifiedDot {

	/**
	 * @param dotString
	 * @return
	 */
	public static Image drawGraph(String dotString) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String DotExec = store.getString(PreferenceConstants.P_PATH);

		File temp = null;
		if (DotExec != null && DotExec.length() > 0) {
			try {
				temp = File.createTempFile("ch.ntb.cross", ".png");
				Process dotProcess = Runtime.getRuntime().exec(new String[] { DotExec, "-Tpng", "-o" + temp.getAbsolutePath() });

				PrintWriter out = new PrintWriter(dotProcess.getOutputStream());

				out.write(dotString);
				out.flush();
				out.close();

				dotProcess.waitFor();

				Image image = new Image(Display.getCurrent(), temp.getAbsolutePath());

				if (temp != null) {
					temp.delete();
				}

				return image;

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
