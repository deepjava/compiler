package ch.ntb.inf.deep.target;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class BDI3000 {
	
	public BDI3000(String hostname) {
		socket = null;
		this.hostname = hostname;
		out = null;
		in = null;
	}
	
	public boolean isConnected() {
		if (socket == null) return false;
		return (socket.isConnected() && !socket.isClosed());
	}
	
	public void connect() throws Exception {
		socket = new Socket(hostname, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
	}
	
	public void disconnect() throws Exception {
		socket.close();
		socket = null;
		out = null;
		in = null;
	}
	
	public void halt() throws Exception {
		out.write("halt\r\n".getBytes());
	}
	
	public void go() throws Exception {
		out.write("go\r\n".getBytes());
	}
	
	public void writeMemory(int address, int value) throws Exception {
		writeMemory(address, value, 1);
	}
	
	public void writeMemory(int address, int value, int count) throws Exception {
		if (count <= 0) throw new Exception("count out of range");
		out.write(("mm 0x" + Integer.toHexString(address) +
				" 0x" + Integer.toHexString(value) +
				" " + Integer.toString(count) +
				"\r\n").getBytes());
	}
	
	public void targetCommand(int memoryAddress, int commandAddress) throws Exception {
		out.write(("halt; mm 0x" + Integer.toHexString(memoryAddress) +
				" 0x" + Integer.toHexString(commandAddress) +
				"; go\r\n").getBytes());
	}
	
	
	public String getHostname() { return hostname; }
	public void setHostname(String hostname) { this.hostname = hostname; }
	
	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	
	
	String hostname;
	int port = 23;

	Socket socket;
	OutputStream out;
	InputStream in;
}
