package ch.ntb.inf.deep.eclipse.ui.view;

import java.io.IOException;
import java.io.OutputStream;

import ch.ntb.mcdp.blackbox.uart.Uart0;

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
				readed = Uart0.read();
				if(readed != null){
					out.write(readed);
				}
			Thread.sleep(50);
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
