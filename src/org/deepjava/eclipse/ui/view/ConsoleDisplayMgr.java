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
package org.deepjava.eclipse.ui.view;

import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
 
/**
 * Create an instance of this class in any of your plugin classes.
 * 
 * Use it as follows ...
 * 
 * ConsoleDisplayMgr.getDefault().println("Some error msg", ConsoleDisplayMgr.MSG_ERROR);
 * ...
 * ...
 * ConsoleDisplayMgr.getDefault().clear();
 * ...  
 */
public class ConsoleDisplayMgr
{
	private static ConsoleDisplayMgr fDefault = null;
	private String fTitle = null;
	private IOConsole fIOConsole = null;
	private static final Color BLUE = new Color(null, 20, 20, 255);
	private static final Color RED = new Color(null, 255, 0, 0);
	private static final Color BLACK = new Color(null, 0, 0, 0);
	private static final Color DARK_YELLOW = new Color(null, 255, 180, 0);
	
	
	public static final int MSG_INFORMATION = 1;
	public static final int MSG_ERROR = 2;
	public static final int MSG_WARNING = 3;
	public static final int MSG_VERBOSE = 4;
	
		
	private ConsoleDisplayMgr(String messageTitle)
	{		
		fDefault = this;
		fTitle = messageTitle;
	}
	
	public static ConsoleDisplayMgr getDefault() {
		if(fDefault == null) new ConsoleDisplayMgr("Deep-build");
		return fDefault;
	}	
		
	public void println(String msg, int msgKind)
	{		
		if( msg == null ) return;
		
		/* if console-view in Java-perspective is not active, then show it and
		 * then display the message in the console attached to it */		
		if( !displayConsoleView() )
		{
			/*If an exception occurs while displaying in the console, then just diplay atleast the same in a message-box */
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", msg);
			return;
		}
		
		/* display message on console */	
		try {
			getNewIOConsoleOutputStream(msgKind).write(msg.getBytes());
		} catch (IOException e) {
		}				
	}
	
	public void clear()
	{		
		if(fIOConsole != null){
			fIOConsole.clearConsole();
		}
	}	
		
	private boolean displayConsoleView(){
		try
		{
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if( activeWorkbenchWindow != null )
			{
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if( activePage != null ){
					IConsoleView view =(IConsoleView) activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
					view.display(fIOConsole);
				}
			}
			
		} catch (PartInitException partEx) {			
			return false;
		}
		
		return true;
	}
	
	public IOConsoleOutputStream getNewIOConsoleOutputStream(int msgKind)
	{		
		IOConsoleOutputStream ioConsoleStream = getIOConsole().newOutputStream();		
		switch (msgKind)
		{
			case MSG_INFORMATION:
				ioConsoleStream.setColor(BLACK);			
				break;
			case MSG_ERROR:
				ioConsoleStream.setColor(RED);
				break;
			case MSG_WARNING:
				ioConsoleStream.setColor(DARK_YELLOW);
				break;
			case MSG_VERBOSE:
				ioConsoleStream.setColor(BLUE);
				break;
			default:
		}	

		return ioConsoleStream;
	}
	
	private IOConsole getIOConsole()
	{
		if( fIOConsole == null )
			createIOConsoleStream(fTitle);	
		
		return fIOConsole;
	}
		
	private void createIOConsoleStream(String title)
	{
		fIOConsole = new IOConsole(title, null); 
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ fIOConsole });
		displayConsoleView();
	}	
}
 
