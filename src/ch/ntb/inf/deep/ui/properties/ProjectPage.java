/**
 * Copyright (c) 2010 NTB Interstaatliche Hochschule für Technick Buchs
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * Contributors:
 *	   NTB - initial implementation
 *	   Roger Millischer - initial implementation
 */
package ch.ntb.inf.deep.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class ProjectPage extends PropertyPage implements IWorkbenchPropertyPage {
	private Combo processor,board;
	private IEclipsePreferences pref;
	
	@Override
	protected Control createContents(Composite parent) {
		pref = getPref();
		Composite composite = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = true;
		rowLayout.marginLeft = 5;
		rowLayout.marginRight = 5;
		rowLayout.spacing = 5;
		composite.setLayout(rowLayout);
		
		Group group1 = new Group(composite, SWT.NONE);
		group1.setText("Processor");
		RowLayout rowLayout2 = new RowLayout(SWT.VERTICAL);
		rowLayout2.justify = true;
		rowLayout2.marginLeft = 5;
		rowLayout2.marginRight = 5;
		rowLayout2.spacing = 5;
		group1.setLayout(rowLayout2);
		Label label = new Label(group1, SWT.NONE);
		label.setText("Select a processor");
		processor = new Combo(group1, SWT.BORDER);
		processor.setItems(new String[]{"MPC555"});
		processor.setText(pref.get("proc", ""));
		
		Group group2 = new Group(composite, SWT.NONE);
		group2.setText("Configuration");
		RowLayout rowLayout3 = new RowLayout(SWT.VERTICAL);
		rowLayout3.justify = true;
		rowLayout3.marginLeft = 5;
		rowLayout3.marginRight = 5;
		rowLayout3.spacing = 5;
		group2.setLayout(rowLayout3);
		Label label1 = new Label(group2, SWT.NONE);
		label1.setText("Select a configuration");
		board = new Combo(group2, SWT.BORDER);
		board.setItems(new String[]{"NTB MPC555 Headerboard", "phyCORE-mpc555"});
		board.setText(pref.get("board",""));
		
		return composite;

	}
	
	private IEclipsePreferences getPref(){
		IProject project = (IProject)getElement();
		ProjectScope scope = new ProjectScope(project);
		return scope.getNode("deepStart");
	}
	
	protected void performApply(){
		save();
		super.performApply();
	}
	
	public boolean performOk(){
		save();
		return true;
	}
	@Override
	protected void performDefaults(){
		processor.setText("MPC555");
		board.setText("NTB MPC555 Headerboard");
	}
	
	private void save(){
		pref.put("proc", processor.getText());
		pref.put("board", board.getText());
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
}
