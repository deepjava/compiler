package ch.ntb.inf.abatronIface1;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class AbatronTelnet extends TargetConnection {

	private static boolean dbg = false;

	final static int SOH = 1;
	final static int ETX = 3;
	final static int WILL = 251;
	final static int IAC = 255;

	private static TargetConnection tc;
	String hostname;
	int port = 23;
	Socket socket;
	OutputStream out;
	InputStream in;

	private AbatronTelnet() { }
	
	public static TargetConnection getInstance() {
		if (tc != null && !tc.isConnected()) tc = null;
		if (tc == null) {
			if(dbg) StdStreams.vrb.println("[TARGET] AbatronTelnet: Creating new Abatron Telnet");
			tc = new AbatronTelnet();
		}
		return tc;
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
//					System.err.println();
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
		boolean IACreceived = false;
		boolean WILLreceived = false;
		byte[] value = new byte[9];
		int i = 0;
		int j = 0;
		
		out.write(("md 0x" + Integer.toHexString(address) +	" " + Integer.toString(count) +	"\r\n").getBytes());
		while (true) {
			int n = in.available();
			if (n <= 0) Thread.sleep(100);
			int c = in.read();
			byte b = 0;
			if (c >= 0 && c <= 0xff) b = (byte)c;
			if (b == IAC) {
				IACreceived = true;
				WILLreceived = false;
			} else if (b == WILL) {
				if (IACreceived) WILLreceived = true;
			} else if (b == SOH) {
				if (IACreceived && WILLreceived) {
//					StdStreams.err.println();
					break;
				}
			}
			if (j >= 13 && j <= 21) value[j - 13] = b;
			if (j == 21) {
				if (i < count) {
					buffer[i++] = parseHex(value, 8);
				}
			}
			if (b == '\r' || b == '\n')	j = 0;
			else j++;
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
		
	@Override
	public void openConnection() throws TargetConnectionException {
		try {
			socket = new Socket(hostname, port);
			out = socket.getOutputStream();
			in = socket.getInputStream();
			waitForPrompt();
			out.write((("update\r\n").getBytes()));	// reload config file
			waitForPrompt();
			if (dbg) StdStreams.vrb.println("[TARGET] Connected ");
		} catch (Exception e) {
			if (dbg) StdStreams.vrb.println("[TARGET] Connection failed on " + hostname);
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public void setOptions(HString opts) {
		hostname = opts.toString();		
	}

	@Override
	public void closeConnection() {
		try {
			if (socket != null) socket.close();
			socket = null;
			out = null;
			in = null;
		} catch (IOException e) {
			// do nothing
		}
		if (dbg) StdStreams.vrb.println("[TARGET] Connection closed");	
	}

	@Override
	public boolean isConnected() {
		if (socket == null) return false;
		return (socket.isConnected() && !socket.isClosed());
	}
	
	@Override
	public int getTargetState() throws TargetConnectionException {
		boolean IACreceived = false, WILLreceived = false;
		try {
			out.write(("info\r\n".getBytes()));
			if (dbg) StdStreams.vrb.println("[TARGET] send: info");
		} catch (IOException e) {
			throw new TargetConnectionException(e.getMessage());
		}
		char[] val = new char[1024];
		int i = 0;
		int c = 0;
		while (true) {
			int n;
			try {
				n = in.available();
				if (n <= 0) Thread.sleep(100);
				c = in.read();
			} catch (Exception e) {
				throw new TargetConnectionException("target connection lost");
			}
			if (c < 0) throw new TargetConnectionException("target not answering");
			if (c == IAC) {IACreceived = true; WILLreceived = false;
			} else if (c == WILL && IACreceived) {WILLreceived = true;
			} else if (c == SOH && IACreceived && WILLreceived) {if (dbg) StdStreams.vrb.println(); break;}
			if (dbg) StdStreams.vrb.print((char)c);
			val[i++] = (char) c;
		}
		String mesg = String.valueOf(val);
		if (mesg.contains("running")) return stateRunning;
		else return stateDebug;
	}

	@Override
	public void startTarget(int address) throws TargetConnectionException {
		try {
			if (address != -1) {
				if (Configuration.getBoard().cpu.arch.name.equals(HString.getHString("arm32"))) {
					if (dbg) StdStreams.vrb.println("[TARGET] arm: Starting from 0x" + Integer.toHexString(address+0x10000000));
					out.write((("go " + (address+0x10000000) + "\r\n").getBytes()));
				} else {
					if (dbg) StdStreams.vrb.println("[TARGET] ppc: Starting from 0x" + Integer.toHexString(address));
					out.write((("go " + address + "\r\n").getBytes()));
				}
			} else {
				if (dbg) StdStreams.vrb.println("[TARGET] Starting");
				out.write(("go\r\n".getBytes()));
			}
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public void stopTarget() throws TargetConnectionException {
		try {
			out.write(("halt\r\n".getBytes()));
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		if (dbg) StdStreams.vrb.println("[TARGET] stopped");
	}

	@Override
	public void resetTarget() throws TargetConnectionException {
		if (dbg) StdStreams.vrb.println("[TARGET] Reseting");
		try {
			out.write(("reset\r\n".getBytes()));
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public void setRegisterValue(String regName, long value) {
		Register reg = Configuration.getRegisterByName(regName);
		if (reg != null) setRegisterValue(reg, value);
	}

	@Override
	public void setRegisterValue(Register reg, long value) {
		if (dbg) {
			if (reg.regType == Parser.sFPR) StdStreams.vrb.println("  Setting register " + reg.name + " to 0x" + Long.toHexString(value));
			else StdStreams.vrb.println("  Setting register " + reg.name + " to 0x" + Integer.toHexString((int)value));
		}

	}

	@Override
	public long getRegisterValue(String regName) throws TargetConnectionException {
		Register reg = Configuration.getRegisterByName(regName);
		if (reg != null) return getRegisterValue(reg);
		return defaultValue;
	}

	@Override
	public long getRegisterValue(Register reg) throws TargetConnectionException {
		if (dbg) StdStreams.vrb.println("[TARGET] read from register " + reg.name);
		switch(reg.regType) {
		case Parser.sGPR:
			return getGprValue(reg.address);
//		case Parser.sFPR:
//			return getFprValue(reg.address);
//		case Parser.sSPR:
//			return getSprValue(reg.address);
//		case Parser.sIOR:
//			return getIorValue(reg.address);
//		case Parser.sMSR:
//			return getMsrValue();
//		case Parser.sCR:
//			return getCrValue();
//		case Parser.sFPSCR:
//			return getFpscrValue();
		default:
			return defaultValue;
		}
	}

	@Override
	public byte readByte(int address) throws TargetConnectionException {
		byte[] value = new byte[3];
		int j = 0, val;

		try {
			out.write(("mdb 0x" + Integer.toHexString(address) +	" 1\r\n").getBytes());
			while (true) {
				int n = in.available();
				if (n <= 0) Thread.sleep(100);
				int c = in.read();
				if (c < 0) throw new TargetConnectionException("target not answering");
				if (dbg) StdStreams.vrb.print((char)c);
				if (j >= 13 && j <= 14) value[j - 13] = (byte) c;
				if (j == 14) {val = parseHex(value, 2); break;}
				j++;
			}
			if (dbg) StdStreams.vrb.print("val = 0x" + Integer.toHexString(val));
			if (dbg) StdStreams.vrb.println();
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return (byte) val;
	}

	@Override
	public short readHalfWord(int address) throws TargetConnectionException {
		byte[] value = new byte[5];
		int j = 0, val;

		try {
			out.write(("mdh 0x" + Integer.toHexString(address) +	" 1\r\n").getBytes());
			while (true) {
				int n = in.available();
				if (n <= 0) Thread.sleep(100);
				int c = in.read();
				if (c < 0) throw new TargetConnectionException("target not answering");
				if (dbg) StdStreams.vrb.print((char)c);
				if (j >= 13 && j <= 16) value[j - 13] = (byte) c;
				if (j == 16) {val = parseHex(value, 4); break;}
				j++;
			}
			if (dbg) StdStreams.vrb.println();
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return (short) val;
	}

	@Override
	public int readWord(int address) throws TargetConnectionException {
		byte[] value = new byte[9];
		int j = 0, val;

		try {
			out.write(("md 0x" + Integer.toHexString(address) +	" 1\r\n").getBytes());
			while (true) {
				int n = in.available();
				if (n <= 0) Thread.sleep(100);
				int c = in.read();
				if (c < 0) throw new TargetConnectionException("target not answering");
				if (dbg) StdStreams.vrb.print((char)c);
				if (j >= 13 && j <= 21) value[j - 13] = (byte) c;
				if (j == 21) {val = parseHex(value, 8); break;}
				j++;
			}
			if (dbg) StdStreams.vrb.println();
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return val;
	}

	@Override
	public void writeByte(int address, byte data)
			throws TargetConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeHalfWord(int address, short data)
			throws TargetConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeWord(int address, int data) throws TargetConnectionException {
		try {
			if (dbg) StdStreams.vrb.println("[TARGET] writing word 0x" + Integer.toHexString(data) + " to address 0x" + Integer.toHexString(address) + " (" + address + ")");
			out.write(("mm 0x" + Integer.toHexString(address) + " 0x" + Integer.toHexString(data) + "\r\n").getBytes());
			waitForPrompt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeTMS(TargetMemorySegment tms) throws TargetConnectionException {
		// not supported here
	}

	@Override
	public void downloadImageFile(String filename) throws TargetConnectionException {
		try {
			int pos = filename.indexOf("ftp");
			String name = filename.substring(pos + 4);
			name = name.replace('\\', '/');
			name = name.replaceAll(".bin", ".ExternalRam.bin");
//			out.write((("halt; load 0x0 " + name + " bin; go 0x100\r\n").getBytes()));
			if (Configuration.getBoard().cpu.arch.name.equals(HString.getHString("arm32"))) 
				out.write((("mmu disable; load 0x10000000 " + name + " bin\r\n").getBytes()));				
			else 
				out.write((("load 0x0 " + name + " bin\r\n").getBytes()));
			if (dbg) StdStreams.vrb.println("[TARGET] loading: " + name);
			StdStreams.log.println(".....");
			waitForPrompt();
		} catch (Exception e) {
			new TargetConnectionException(e.getMessage(), e);
		}		
	}

	@Override
	public void setBreakPoint(int address) throws TargetConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBreakPoint(int address) throws TargetConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void confirmBreakPoint(int address) throws TargetConnectionException {
		// TODO Auto-generated method stub
		
	}

	/* private methods */
	
	private synchronized int getGprValue(int gpr) throws TargetConnectionException {
		byte[] value = new byte[9];
		int i = -1, val = 0;

		try {
			out.write(("rd r" + gpr + "\r\n").getBytes());
			while (true) {
				int n = in.available();
				if (n <= 0) Thread.sleep(100);
				int c = in.read();
				if (c < 0) throw new TargetConnectionException("target not answering");
				if (dbg) StdStreams.vrb.print((char)c);
				if (i >= 0) value[i++] = (byte) c;
				if ((char)c == 'x') i = 0;
				if (i == 8) {val = parseHex(value, 8); break;}
			}
			if (dbg) StdStreams.vrb.println();
			waitForPrompt();
		} catch (Exception e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return val;
	}

	private void waitForPrompt() throws Exception {
		boolean IACreceived = false, WILLreceived = false;
		while (true) {
			int n = in.available();
			if (n <= 0) Thread.sleep(100);
			int c = in.read();
			if (c < 0) throw new TargetConnectionException("target not answering");
			if (c == IAC) {IACreceived = true; WILLreceived = false;
			} else if (c == WILL && IACreceived) {WILLreceived = true;
			} else if (c == SOH && IACreceived && WILLreceived) break;
		}
	}
	
	private int parseHex(byte[] hex, int len)  {
		int value = 0;
		for (int i = 0; i < len; i++) {
			value <<= 4;
			if (hex[i] >= '0' && hex[i] <= '9') value += (byte)(hex[i] - (byte)'0');
			else {
				byte c = (byte)Character.toLowerCase(hex[i]);
				value += (byte)(c - (byte)'a' + (byte)10);
			}
		}
		return value;
	}

	@Override
	public long[] getRegisterBlock(String block) throws TargetConnectionException {
		// TODO Auto-generated method stub
		return null;
	}
	

}