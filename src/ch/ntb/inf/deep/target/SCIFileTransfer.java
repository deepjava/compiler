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

package ch.ntb.inf.deep.target;

import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;

import ch.ntb.inf.deep.host.StdStreams;

import java.io.*;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/** files can be manually downloaded over SCI, resides on host */

public class SCIFileTransfer extends JFrame implements ActionListener, SerialPortEventListener {
	PrintStream log = StdStreams.log;

	private static final long serialVersionUID = 1L;
	private static final String comPort = "COM5";
	
	JButton startButton = new JButton();
	JButton stopButton = new JButton();
	JButton getDirButton = new JButton();
	JButton formatAllButton = new JButton();
	JButton sendFileButton = new JButton();
	JButton receiveFileButton = new JButton();
	JFileChooser chooser;
	Thread t;
	
	SerialPort serialPort;
	InputStream inStream;
	OutputStream outStream;

	public SCIFileTransfer() {
		JPanel contentPane = (JPanel)this.getContentPane();
		contentPane.setLayout(new GridLayout(6,1));
		this.setSize(300, 350);
		this.setResizable(false);
		this.setTitle("Download files to target");
		startButton.setText("open SCI");
		startButton.addActionListener(this);
		contentPane.add(startButton);
		stopButton.setText("close SCI");
		stopButton.addActionListener(this);
		contentPane.add(stopButton);
		getDirButton.setText("get directory");
		getDirButton.addActionListener(this);
		contentPane.add(getDirButton);
		formatAllButton.setText("format all");
		formatAllButton.addActionListener(this);
		contentPane.add(formatAllButton);
		sendFileButton.setText("send file to target");
		sendFileButton.addActionListener(this);
		contentPane.add(sendFileButton);
		receiveFileButton.setText("receive file from target");
		receiveFileButton.addActionListener(this);
		contentPane.add(receiveFileButton);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {dispose(); t.interrupt();}
		});
		setVisible(true);
		chooser = new JFileChooser();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(startButton)) {
			log.println("start " + comPort);
			try {
				CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(comPort);
				serialPort = (SerialPort)portId.open(this.getClass().getName(), 2000);
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
			} catch (NoSuchPortException e1) {
				log.println("port not found: " + comPort);
			} catch (PortInUseException e2) {
				log.println("port in use: " + comPort);
			} catch (UnsupportedCommOperationException e3) {
				log.println("unsupported comm operation: " + comPort);
			} catch (TooManyListenersException e4) {
				log.println("too many event listeners: " + comPort);
			}
	
			try {
				inStream = serialPort.getInputStream();
				if(inStream == null) log.println("Couldn't open input stream");
				outStream = new BufferedOutputStream(serialPort.getOutputStream());
				if(outStream == null) log.println("Couldn't open output stream");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (e.getSource().equals(stopButton)) {
			log.println("stop " + comPort);
			inStream = null;
			serialPort.close();
		}
		if (e.getSource().equals(getDirButton)) {
			log.println("read directory from target");
			try { // sends byte 'g' to target
				outStream.write('g');
				outStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (e.getSource().equals(formatAllButton)) {
			log.println("format all");
			try { // sends byte 'a' to target
				outStream.write('a');
				outStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (e.getSource().equals(sendFileButton)) {
			// send byte 'p' to target, followed by file name + 0 + file length in bytes + ' ' + file content 
			log.print("send file to target: ");
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				log.println(file.getName() + ", len = " + file.length() + "Bytes");
				FileInputStream in = null;
				try {
					in = new FileInputStream(file.getAbsoluteFile());
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				BufferedInputStream bufIn = new BufferedInputStream(in);
				try {
					outStream.write('p');
					outStream.write(0);
					outStream.write(0);
					outStream.write(file.getName().getBytes());
					outStream.write(0);
					outStream.write(Long.toString(file.length()).getBytes());					
					outStream.write(' ');
					outStream.flush();
					int data = bufIn.read();
					while (data >= 0) {outStream.write(data); data = bufIn.read();}
					outStream.flush();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}			
		}
		if (e.getSource().equals(receiveFileButton)) {
			// send byte 's' to target, followed by file name + 0 
			// target answers by sending file name + 0 + file length in bytes + ' ' + content
			log.print("read file from target: ");
			String fileName = JOptionPane.showInputDialog(null,"enter target file name","",JOptionPane.PLAIN_MESSAGE);
			try {
				outStream.write('s');
				outStream.write(fileName.getBytes());
				outStream.write(0);
				outStream.flush();
				inStream = null;	// stop the output to the log
				InputStream iStream = serialPort.getInputStream();
				if(iStream == null) log.println("Couldn't open input stream");
				int i = 0; byte b = -1; byte[] ch = new byte[64];
				do { 
					try {
						if (iStream != null && iStream.available() > 0) {
							b = (byte)(iStream.read());
//							log.println((char)b);
							if (b != 0) ch[i++] = b;
						}
					} catch (IOException e1) {e1.printStackTrace();}
				} while (b != 0);
				String retFileName = new String(ch, 0, i);
				log.print(retFileName);
				i = 0; b = 0;
				do { 
					try {
						if (iStream != null && iStream.available() > 0) {
							b = (byte)(iStream.read());
							if (b != ' ') ch[i++] = b;
						}
					} catch (IOException e1) {e1.printStackTrace();}
				} while (b != ' ');
				int len = Integer.parseInt(new String(ch, 0, i));
				if (len < 0) {log.print(": file does not exist");}
				else {
					log.print(", " + len + " Bytes");
					int returnVal = chooser.showOpenDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File recFile = chooser.getSelectedFile();

						FileWriter fileWriter = new FileWriter(recFile.getAbsolutePath());
						PrintWriter out = new PrintWriter(fileWriter);
						i = 0;
						do { 
							try {
								if (iStream != null && iStream.available() > 0) {
									b = (byte)(iStream.read());
									out.append((char)b);
									i++;
								}
							} catch (IOException e1) {e1.printStackTrace();}
						} while (i < len);
						out.flush();
						fileWriter.close();
						out.close();
						log.println(", written to disk file " + recFile.getAbsolutePath());
					}
				}
				iStream = null;
				inStream = serialPort.getInputStream();
				if(inStream == null) log.println("Couldn't open input stream");
				log.println();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case  SerialPortEvent.DATA_AVAILABLE:
			try {
				while (inStream != null && inStream.available() > 0) {
					byte data = (byte) inStream.read();
					log.print((char)data);
				}
			} catch (IOException e) {e.printStackTrace();}
			break;
		default:
			break;
		}
	}

	public static void main(String[] args) {
//		Runnable frame = new SCIFileTransfer();
//		((SCIFileTransfer)frame).t = new Thread(frame);
//		((SCIFileTransfer)frame).t.start();
		new SCIFileTransfer();
	}

}

/*
IMPORT Services, Log := StdLog, Ports := CommV24, TextModels, XUT := XdeUtilities, Views, Files, Stores, Converters, In,
UsbTargLog, SYS := XjoppcSystem, USB := UsbBDI;
(** files can be manually uploaded over SCI, resides on host *)
CONST
	nul = 0X; cr = 0DX; lf = 0AX; ht = 9X; sp = 20X;
	portOptions = {Ports.inRTS, Ports.inDTR}; (* 8 data bits, 1 stop bit, no parity, dtrCtrlEnable, rtsCtrlEnable *)
	defaultPortName = "COM1";
	logTitle = "Target Log";
	profilePath = "CrossProfile.odc";

VAR
	logTextModel: TextModels.Model;
	logWriter: TextModels.Writer;
	targetConn: Ports.Connection;
	i, n: INTEGER; x: BYTE;
	buf: ARRAY 32 OF BYTE;
	shortbuf: ARRAY 32 OF SHORTCHAR;

PROCEDURE SendInt (i: LONGINT);
CONST fieldLen = 5;
VAR
done : BOOLEAN;
digits: ARRAY 12 OF CHAR;
m, n: LONGINT;
digit: CHAR;
neg: BOOLEAN;
data: ARRAY 1 OF SHORTCHAR;
BEGIN
	n := - 1; neg := FALSE;
	REPEAT INC(n); digits[n] := CHR( ORD('0') + i MOD 10 ); i := i DIV 10 UNTIL i = 0;
	m := fieldLen - n - 1;
	WHILE m > 0 DO
		IF (SYS.IsUSBEnabled()) THEN
			data[0] := ' ';
			done := USB.UART0_write(data, 1);
		ELSE
			Ports.SendByte(targetConn, SHORT(ORD(' ')));
		END;
		DEC(m);
		Log.Int(SHORT(ORD(' ')));
		Log.Ln
	END;

	WHILE n >= 0 DO
		IF (SYS.IsUSBEnabled()) THEN
			shortbuf[0] := SHORT(digits[n]);
			done := USB.UART0_write(shortbuf, 1);
		ELSE
			Ports.SendByte(targetConn, SHORT(SHORT(ORD(digits[n]))));
		END;
		Log.Int(SHORT(SHORT(ORD(digits[n]))));
		Log.Ln; DEC(n);
	END
END SendInt;

PROCEDURE ClosePort*;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		UsbTargLog.Stop
	ELSE
		IF targetConn # NIL THEN Ports.Close(targetConn); targetConn := NIL END;
		logWriter := NIL; logTextModel := NIL
	END;
END ClosePort;

PROCEDURE OpenPort*;
(** Opens a serial bidirectional port to the target for receiving log info and for sending chars (to the target). *)
VAR
	prfSc: XUT.Scanner; beg, end: INTEGER;
	ch: CHAR;
	portName: ARRAY 32 OF SHORTCHAR;
	name2: ARRAY 32 OF CHAR;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		UsbTargLog.Start;
	ELSE
		(* OLD Serial Part *)
		ClosePort;
		portName := defaultPortName;
		Log.String("opening target log on port: "); Log.String(portName$); Log.Ln;
		Ports.Open(portName$, 9600, portOptions, targetConn);
		IF targetConn = NIL THEN
			Log.String(" error, opening failed")
		ELSE
			XUT.GetTextModel("", logTextModel, beg, end); logWriter := logTextModel.NewWriter(NIL);
			XUT.ShowTextView(logTextModel, logTitle);
		END;
		Log.Ln
	END;
END OpenPort;

PROCEDURE ClearText*;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		UsbTargLog.ClearText
	ELSE
		IF logTextModel # NIL THEN
			logTextModel.Delete(0, logTextModel.Length()); logWriter.SetPos(0)
		END;
	END;
END ClearText;

PROCEDURE GetFileDir*;
VAR
	data: ARRAY 1 OF SHORTCHAR;
	done: BOOLEAN;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		(* NEW USB Part *)
		data[0] := 'g';
		done := USB.UART0_write(data, 1);
	ELSE
		(* OLD Serial Part *)
		IF targetConn = NIL THEN OpenPort END;
		Ports.SendByte(targetConn, SHORT(ORD('g')))
	END;
END GetFileDir;

PROCEDURE GetFileLog*;
VAR
	data : ARRAY USB.MAX_UART_DATA_LENGTH OF SHORTCHAR;
	n, i, len: INTEGER;
	done: BOOLEAN;
	ch: CHAR;
	buff: ARRAY 32 OF BYTE;
	name: ARRAY 32 OF CHAR;
	byte: BYTE;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		(* NEW USB Part *)

		data[0] := 's';
		done := USB.UART0_write(data, 1);

		n := 0;
		In.Open;
		REPEAT
			In.Char(ch);
			name[n] := ch; INC(n);
		UNTIL ~In.Done OR (ch = cr) OR (ch = ht) OR (ch = lf) OR (ch = sp) OR (n >= LEN(name) - 2);
		DEC(n); name[n] := nul;
		FOR i := 0 TO n DO data[i] := SHORT(name[i]) END;
		done := USB.UART0_write(data, n + 1);
	ELSE
		(* OLD Serial Part *)
		IF targetConn = NIL THEN OpenPort END;
		Ports.SendByte(targetConn, SHORT(ORD('s')));
		n := 0;
		In.Open;
		REPEAT
			In.Char(ch);
			name[n] := ch; INC(n);
		UNTIL ~In.Done OR (ch = cr) OR (ch = ht) OR (ch = lf) OR (ch = sp) OR (n >= LEN(name) - 2);
		DEC(n); name[n] := nul;
		FOR i := 0 TO n DO buff[i] := SHORT(SHORT(ORD(name[i]))) END;
		Ports.SendBytes(targetConn, buff, 0, n + 1);
	END;
END GetFileLog;

PROCEDURE PutFile*;
VAR
	data : ARRAY USB.MAX_UART_DATA_LENGTH OF SHORTCHAR;
	v: Views.View;
	done: BOOLEAN;
	conv: Converters.Converter; loc: Files.Locator; name: Files.Name;
	file: Files.File; r: Stores.Reader;
	currentLen : INTEGER;
BEGIN
	loc := NIL; name := "";
	v := Views.Old(Views.ask, loc, name, conv);
	IF v # NIL THEN
		file := Files.dir.Old(loc, name, TRUE);
		r.ConnectTo(file);
		r.SetPos(0);
		Log.String(name); Log.String(" opened"); Log.Ln;

		IF (SYS.IsUSBEnabled()) THEN
			data[0] := 'p';
			done := USB.UART0_write(data, 1);
		ELSE
			IF targetConn = NIL THEN OpenPort END;
			Ports.SendByte(targetConn, SHORT(ORD('p')));
		END;

		i := 0;
		currentLen := 0;

		WHILE name[i] # 0X DO INC(i) END;

		IF (SYS.IsUSBEnabled()) THEN
			FOR n := 0 TO i DO
				data[currentLen] := SHORT(name[n]);
				INC(currentLen);

				IF currentLen >= 500 THEN
					done := USB.UART0_write(data, currentLen);
					IF ~done THEN
						HALT(102);
					END;
					currentLen := 0;
				END;
			END;

			IF currentLen > 0 THEN
				done := USB.UART0_write(data, currentLen);
				IF ~done THEN
					HALT(103);
				END;
			END;

		ELSE
			(* Serial writer *)
			FOR n := 0 TO i DO
				buf[n] := SHORT(SHORT(ORD(name[n])))
			END;
			Ports.SendBytes(targetConn, buf, 0, i + 1);
		END;

		SendInt(file.Length());
		currentLen := 0;
		r.ReadByte(x);
		WHILE ~r.rider.eof DO
			IF (SYS.IsUSBEnabled()) THEN
				(* USB Port *)
				data[currentLen] := SHORT(CHR(x));
				INC(currentLen);

				IF currentLen >= 500 THEN
					done := USB.UART0_write(data, currentLen);
					IF ~done THEN
						HALT(104);
					END;
					currentLen := 0;
				END;
			ELSE
				(* Serial Port *)
				Ports.SendByte(targetConn, x);
			END;
			r.ReadByte(x);
		END;

		IF (SYS.IsUSBEnabled()) THEN
			IF currentLen > 0 THEN
				done := USB.UART0_write(data, currentLen);
				IF ~done THEN
					HALT(105);
				END;
			END;
		END;
		Log.Int(i); Log.String(" bytes sent"); Log.Ln
	END;
END PutFile;
PROCEDURE FormatAll*;
VAR
		done : BOOLEAN;
		data: ARRAY 1 OF SHORTCHAR;
BEGIN
	IF (SYS.IsUSBEnabled()) THEN
		(* NEW USB Part *)
		data[0] := 'a';
		done := USB.UART0_write(data, 1);
	ELSE
		(* OLD Serial Part *)
		IF targetConn = NIL THEN OpenPort END;
		Ports.SendByte(targetConn, SHORT(ORD('a')))
	END;
END FormatAll;

END XjoHostFileTransfer.*/


