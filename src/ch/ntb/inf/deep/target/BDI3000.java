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
		waitForPrompt();
	}
	
	public void disconnect() throws Exception {
		socket.close();
		socket = null;
		out = null;
		in = null;
	}
	
	public void halt() throws Exception {
		out.write("halt\r\n".getBytes());
		waitForPrompt();
	}
	
	public void go() throws Exception {
		out.write("go\r\n".getBytes());
		waitForPrompt();
	}
	
	public int readMemory(int address, byte[] buffer, int count) throws Exception {
		out.write(("mdb 0x" + Integer.toHexString(address) +
				" " + Integer.toString(count) +
				"\r\n").getBytes());
		
		boolean IACreceived = false;
		boolean WILLreceived = false;
		int i = 0;
		int j = 0;
		byte value = 0;
		
		while (true) {
			int n = in.available();
			if (n <= 0) Thread.sleep(100);
			int c = in.read();
			byte b = 0;
			if (c >= 0 && c <= 0xff) b = (byte)c;
			if (b == IAC) {
				IACreceived = true;
				WILLreceived = false;
			}
			else if (b == WILL) {
				if (IACreceived) {
					WILLreceived = true;
				}
			}
			else if (b == SOH) {
				if (IACreceived && WILLreceived) {
					System.err.println();
					break;
				}
			}
			
			if (j == 18) {
				if (b >= '0' && b <= '9')
					value = (byte)(b - (byte)'0');
				else
					value = 0;
			}
			else if (j == 19 || j == 20) {
				if (b >= '0' && b <= '9') {
					value *= 10;
					value += (byte)(b - (byte)'0');
				}
			}
			if (j == 20) {
				if (i < count) {
					buffer[i++] = value;
				}
			}
			
			if (b == '\r' || b == '\n')
				j = 0;
			else
				j++;
		}
		
		return i;
	}
	
	public int readMemory(int address, int[] buffer, int count) throws Exception {
		out.write(("md 0x" + Integer.toHexString(address) +
				" " + Integer.toString(count) +
				"\r\n").getBytes());
		
		boolean IACreceived = false;
		boolean WILLreceived = false;
		byte[] value = new byte[9];
		int i = 0;
		int j = 0;
		
		while (true) {
			int n = in.available();
			if (n <= 0) Thread.sleep(100);
			int c = in.read();
			byte b = 0;
			if (c >= 0 && c <= 0xff) b = (byte)c;
			if (b == IAC) {
				IACreceived = true;
				WILLreceived = false;
			}
			else if (b == WILL) {
				if (IACreceived) {
					WILLreceived = true;
				}
			}
			else if (b == SOH) {
				if (IACreceived && WILLreceived) {
					System.err.println();
					break;
				}
			}
			
			if (j >= 13 && j <= 21) {
				value[j - 13] = b;
			}
			if (j == 21) {
				if (i < count) {
					buffer[i++] = parseHex(value);
				}
			}

			
			if (b == '\r' || b == '\n')
				j = 0;
			else
				j++;
		}
		
		return i;
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
		waitForPrompt();
	}
	
	public void targetCommand(int memoryAddress, int commandAddress) throws Exception {
		out.write(("halt; mm 0x" + Integer.toHexString(memoryAddress) +
				" 0x" + Integer.toHexString(commandAddress) +
				"; go\r\n").getBytes());
		waitForPrompt();
	}
	
	private void waitForPrompt() throws Exception {
		boolean IACreceived = false;
		boolean WILLreceived = false;
		
		while (true) {
			int n = in.available();
			if (n <= 0) Thread.sleep(100);
			int c = in.read();
			byte b = 0;
			if (c >= 0 && c <= 0xff) b = (byte)c;
			if (b == IAC) {
				IACreceived = true;
				WILLreceived = false;
			}
			else if (b == WILL) {
				if (IACreceived) {
					WILLreceived = true;
				}
			}
			else if (b == SOH) {
				if (IACreceived && WILLreceived) {
					System.err.println();
					break;
				}
			}
		}
	}
	
	private int parseHex(byte[] hex) throws Exception {
		if (hex.length < 8) throw new Exception("invalid array size");
		int value = 0;
		for (int i = 0; i < 8; i++) {
			value <<= 4;
			if (hex[i] >= '0' && hex[i] <= '9') {
				value += (byte)(hex[i] - (byte)'0');
			}
			else {
				byte c = (byte)Character.toLowerCase(hex[i]);
				value += (byte)(hex[i] - (byte)'a' + (byte)10);
			}
		}
		return value;
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

	final static byte SOH = (byte)0x01;
	final static byte ETX = (byte)0x03;
	final static byte IAC = (byte)0xff;
	final static byte WILL = (byte)0xfb;
}
