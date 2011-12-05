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

import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.ViewPart;

import ch.ntb.inf.deep.eclipse.DeepPlugin;

public class USBLog extends ViewPart {
	public static final String ID = "ch.ntb.inf.deep.ui.view.USBLog";
	private IOConsole log;
	private Action clear;
	private USBLogWriter writer;
	private TextConsoleViewer viewer;
	protected OutputStream out;
	
	@Override
	public void createPartControl(Composite parent) {
		
		//get Display
		Display d = parent.getShell().getDisplay();
		
		//Create view

		log = new IOConsole("USBLog", null);
		out = log.newOutputStream();
		viewer = new TextConsoleViewer(parent, log);
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(viewerData);
		if(d != null){
			FontData defaultFont = new FontData("Courier", 10, SWT.NORMAL);
			Font font = new Font(d, defaultFont);
			viewer.getControl().setFont(font);
		}
		
		viewer.setEditable(false);
		viewer.addTextListener(new ITextListener() {
			@Override
			public void textChanged(TextEvent event) {//autoscroll
				
				IDocument document= viewer.getDocument();
				int nofLines = document.getNumberOfLines();
				try {
					int start= document.getLineOffset(nofLines - 1);
					int length= document.getLineLength(nofLines - 1);
					viewer.getTextWidget().setSelection(start, start);
					viewer.revealRange(start, length);
				} catch (BadLocationException x) {
				}
			}
		});
		
		writer = new USBLogWriter("Uart0", out);
		writer.start();
		
		//create actions and hook to the toolbar
		createAction();
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(clear);
		
	}
	
	protected void createAction(){
		
		clear = new Action(){
			public void run(){
				log.clearConsole();
			}
		};
		clear.setText("Clear Log");
		ImageDescriptor img = ImageDescriptor.createFromImage(DeepPlugin.createImage("full/obj16/Icons-mini-note_delete.gif"));
		clear.setImageDescriptor(img);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	@Override
	public void dispose(){
		writer.setRunning(false);
		super.dispose();
	}
	
}
