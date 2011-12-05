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

import java.io.IOException;
import java.io.OutputStream;

import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.mcdp.ide.uart.Uart0;

public class USBLogWriter extends Thread {
	private OutputStream out;
	private boolean isRunning = true;

	public USBLogWriter(String name, OutputStream out){
		super(name);
		this.out = out;
	}
	
	@Override
	public void run(){
		byte[] readed = null;
		try {
			while(isRunning){
				while(UsbMpc555Loader.getInstance() == null){
					Thread.sleep(500);
				}					
				readed = Uart0.read();
				if(readed != null){
					out.write(readed);
				}
				Thread.sleep(100);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setRunning(boolean run){
		isRunning = run;
	}
	
	
}
